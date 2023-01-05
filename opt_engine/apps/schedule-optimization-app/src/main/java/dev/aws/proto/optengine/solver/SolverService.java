// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package dev.aws.proto.optengine.solver;

import dev.aws.proto.appcore.config.SolutionConfig;
import dev.aws.proto.optengine.persistance.DdbSolverJobService;
import dev.aws.proto.optengine.persistance.S3SolutionBucketService;
import dev.aws.proto.optengine.persistance.data.SolverJobStatus;
import dev.aws.proto.optengine.solution.EmployeeScheduleSolution;
import dev.aws.proto.optengine.solution.inspect.EmployeeScheduleSolutionInspector;
import dev.aws.proto.optengine.util.Constants;
import dev.aws.proto.optengine.util.ScheduleObjectWriter;
import dev.aws.proto.optengine.util.ScheduleSolutionResultUtil;
import org.optaplanner.core.api.solver.SolverJob;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.SolverManagerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.aws.proto.appcore.solution.SolutionBase;
import dev.aws.proto.appcore.solution.SolutionState;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class SolverService {

    private static final Logger logger = LoggerFactory.getLogger(SolverService.class);

    /**
     * Optaplanner Solver manager.
     */
    protected SolverManager<EmployeeScheduleSolution, UUID> solverManager;

    /**
     * Lookup table for solutions based on their ID.
     */
    protected Map<UUID, SolutionState<EmployeeScheduleSolution, UUID>> solutionMap;

    /**
     * Solution configuration
     */
    @Inject
    protected SolutionConfig solutionConfig;

    /**
     * DynamoDB service instance for update solver status
     */
    @Inject
    DdbSolverJobService ddbSolverJobTable;

    /**
     * S3 service instance for store result json file
     */
    @Inject
    S3SolutionBucketService s3SolutionBucket;

    SolverService(SolutionConfig solutionConfig) {
        this.solutionConfig = solutionConfig;

        // solver config
        logger.info("solver config path : "+solutionConfig.getSolverConfigXmlPath());
        SolverConfig solverConfig = SolverConfig.createFromXmlFile(java.nio.file.Path.of(this.solutionConfig.getSolverConfigXmlPath()).toFile());

        // create the solver config and the solver manager
        this.solverManager = SolverManager.create(solverConfig, new SolverManagerConfig());
        this.solutionMap = new ConcurrentHashMap<>();

        logger.info("solver service initialized");

    }

    public UUID solveProblem(EmployeeScheduleSolution problem) {
        // setup
        UUID problemId = UUID.randomUUID();
        long createdAt = Timestamp.valueOf(LocalDateTime.now()).getTime();
        logger.info("solveProblem :: problemId={} ", problemId);

        // start job
        problem.setId(problemId);
        problem.setCreatedAt(createdAt);
        var optaSolverJob = this.solverManager.solve(problemId, this::problemFinder, this::finalBestSolutionConsumer);

        // save the state
        this.solutionMap.put(problemId, new SolutionState<>(optaSolverJob, problem, System.currentTimeMillis()));
        saveSolverJobStarted(problemId);

        return problemId;
    }

    protected EmployeeScheduleSolution problemFinder(UUID problemId) {
        return this.solutionMap.get(problemId).problem;
    }

    protected void finalBestSolutionConsumer(EmployeeScheduleSolution solution) {
        UUID problemId = solution.getId();

        SolverJob<? extends SolutionBase, UUID> solverJob = this.solutionMap.get(problemId).solverJob;
        long solverDurationInMs = solverJob.getSolvingDuration().getSeconds() * 1000 + (solverJob.getSolvingDuration().getNano() / 1_000_000);

        // custom implementations for storing data, cleaning up, etc
        this.finalBestSolutionConsumerHook(solution, solverDurationInMs);

        logger.debug("Removing problemId {} from solutionMap at finalBestSolutionConsumer", problemId);
        this.solutionMap.remove(problemId);
    }

    protected void finalBestSolutionConsumerHook(EmployeeScheduleSolution bestSolution, long solverDurationInMs) {
        logger.info("final best score : "+bestSolution.getScore());

        // Extract best solution
        var packedSolution = ScheduleSolutionResultUtil.extractBestSolution(bestSolution, solverDurationInMs);
        String problemId = bestSolution.getId().toString();

        // Final best solution
        String solutionOutputFileName = getDataFilePath(problemId, "sol");
        String solutionOutputPath = getSolverOutputPath()+"/"+solutionOutputFileName;
        ScheduleObjectWriter.writeJsonFile(packedSolution, solutionOutputPath);

        s3SolutionBucket.uploadFile(problemId+"/"+solutionOutputFileName, solutionOutputPath);
        logger.info("Save final best solution :: " + solutionOutputFileName);

        // Inspection report
        String inspectionOutputFileName = getDataFilePath(problemId, "inspect");
        String inspectionOutputPath = getSolverOutputPath()+"/"+inspectionOutputFileName;
        var inspectionReport = new EmployeeScheduleSolutionInspector(this.solverManager, bestSolution).inspect();
        ScheduleObjectWriter.writeJsonFile(inspectionReport, inspectionOutputPath);

        s3SolutionBucket.uploadFile(problemId+"/"+inspectionOutputFileName, inspectionOutputPath);
        logger.info("Save inspection report :: " + inspectionOutputFileName);

        // save status
        saveSolverJobFinished(bestSolution, solverDurationInMs, solutionOutputFileName, inspectionOutputFileName);

        // (DEV) Print solution overview
        var overview = ScheduleSolutionResultUtil.createSolutionOverview(bestSolution);
        logger.info(overview);
    }

    public String getSolverOutputPath() {
        return solutionConfig.getSolutionOutputLocalPath();
    }

    private String getDataFilePath(String problemId, String prefix) {
        String now = LocalDateTime.now().format(Constants.DefaultDateTimeFormatter);
        return String.format("%s-%s-%s.json", prefix, now, problemId);
    }

    public void saveSolverJobStarted(UUID problemId) {
        ddbSolverJobTable.save(SolverJobStatus.builder()
                .problemId(problemId.toString())
                .createdAt(Timestamp.valueOf(LocalDateTime.now()).getTime())
                .state("STARTED")
                .score("NA")
                .build());
    }

    public void saveSolverJobFinished(EmployeeScheduleSolution solution, long solverDurationInMs, String solutionOutputFileName, String inspectionOutputFileName) {
        String problemId = solution.getId().toString();
        var fileInfo = ddbSolverJobTable.getFileInfo(problemId);

        ddbSolverJobTable.save(SolverJobStatus.builder()
                .problemId(problemId)
                .createdAt(solution.getCreatedAt())
                .score(solution.getScore().toString())
                .solverDurationInMs(solverDurationInMs)
                .apiRequestFile(fileInfo.getApiRequestFile())
                .solutionFile(solutionOutputFileName)
                .inspectionReportFile(inspectionOutputFileName)
                .state("FINISHED")
                .build());
    }

    public int getSolutionMapCount(){
        return solutionMap==null?-1:solutionMap.size();
    }
}
