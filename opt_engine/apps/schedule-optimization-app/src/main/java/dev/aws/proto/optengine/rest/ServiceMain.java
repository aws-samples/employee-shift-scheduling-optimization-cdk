// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package dev.aws.proto.optengine.rest;

import dev.aws.proto.optengine.persistance.DdbSolverJobService;
import dev.aws.proto.optengine.persistance.S3SolutionBucketService;
import dev.aws.proto.optengine.rest.request.ScheduleSolveRequest;
import dev.aws.proto.optengine.rest.response.SolveResponse;
import dev.aws.proto.optengine.rest.response.StatusResponse;
import dev.aws.proto.optengine.solution.builder.EmployeeScheduleSolutionBuilder;
import dev.aws.proto.optengine.solver.SolverService;
import dev.aws.proto.optengine.util.Constants;
import dev.aws.proto.optengine.util.ScheduleObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
@Path("/schedule")
public class ServiceMain {

    @Inject
    SolverService solverService;

    @Inject
    DdbSolverJobService ddbSolverJobTable;

    @Inject
    S3SolutionBucketService s3SolutionBucket;

    private static final Logger logger = LoggerFactory.getLogger(ServiceMain.class);

    private ExecutorService executor = Executors.newFixedThreadPool(10);

    // To try, open http://localhost:8080/schedule
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String isLive() {
        String message = "Schedule Optimization Engine OK - JobEnqueued : "+solverService.getSolutionMapCount();
        logger.info(message);
        return message;
    }

    @POST
    @Path("/solve")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public SolveResponse solve(ScheduleSolveRequest req) {
        // Log Request Data
        logger.info("Solve Request : " + req.getSummary());
        for(var shift : req.getShiftRequirements()) logger.info(shift.toString());
        for(var employee : req.getEmployees()) logger.info(employee.toString());
        for(var rq : req.getEmployeeRequests()) logger.info(rq.toString());

        // Setup problem data
        var problem = new EmployeeScheduleSolutionBuilder<>(req).build();

        // Start solve-job
        var problemId = solverService.solveProblem(problem);

        // store request data
        executor.submit(()->{
            String reqDataFileName = getRequestFileName(problemId.toString());
            String reqDataFilePath = getRequestDataPath(reqDataFileName);
            ScheduleObjectWriter.writeJsonFile(req, reqDataFilePath);
            ddbSolverJobTable.saveRequestFileName(problemId.toString(), reqDataFileName);
            s3SolutionBucket.uploadFile(problemId+"/"+reqDataFileName, reqDataFilePath);
        });

        // response
        SolveResponse resp = new SolveResponse(problemId.toString());
        logger.info(resp.toString());

        return resp;
    }

    @GET
    @Path("/status/{problemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public StatusResponse jobStatus(@PathParam("problemId") String id) {
        var jobStatus = ddbSolverJobTable.getJobStatus(id);
        return new StatusResponse(jobStatus);
    }

    @GET
    @Path("/problem/{problemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRequestedProblems(@PathParam("problemId") String id) {
        logger.info("getRequestedProblems : {}", id);

        var jobInfo = ddbSolverJobTable.getFileInfo(id);
        String key = id+"/"+jobInfo.getApiRequestFile();
        Response res = createFileResponseFromS3Object(key);

        return res;
    }

    @GET
    @Path("/solution/{problemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOptimizationResult(@PathParam("problemId") String id) {
        logger.info("getOptimizationResult : {}", id);

        var jobInfo = ddbSolverJobTable.getFileInfo(id);
        String key = id+"/"+jobInfo.getSolutionFile();
        Response res = createFileResponseFromS3Object(key);

        return res;
    }

    @GET
    @Path("/inspection/{problemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInspectionReport(@PathParam("problemId") String id) {
        logger.info("getInspectionReport : {}", id);

        var jobInfo = ddbSolverJobTable.getFileInfo(id);
        String key = id+"/"+jobInfo.getInspectionReportFile();
        Response res = createFileResponseFromS3Object(key);

        return res;
    }

    private Response createFileResponseFromS3Object(String key){
        Response.ResponseBuilder response;
        try {
            // temp file
            File tempFile = File.createTempFile("opt-engine-", ".tmp");
            if(tempFile.exists()) tempFile.delete();

            // download
            s3SolutionBucket.downloadFile(key, tempFile.getAbsolutePath());
            if(!tempFile.exists()) throw new FileNotFoundException("s3 file download error : "+key);

            // build response
            response = Response.ok((Object)tempFile);

            // delete temp file on application terminated
            tempFile.deleteOnExit();
        } catch (Exception e) {
            logger.error("Cannot create file response : {}", e.getMessage());
            response = Response.serverError();
        }

        return response.build();
    }

    private String getRequestFileName(String problemId) {
        String now = LocalDateTime.now().format(Constants.DefaultDateTimeFormatter);
        return String.format("req-%s-%s.json", now, problemId);
    }

    private String getRequestDataPath(String fileName) {
        String outputDir = solverService.getSolverOutputPath();
        return String.format("%s/%s", outputDir, fileName);
    }
}
