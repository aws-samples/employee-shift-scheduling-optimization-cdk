// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package dev.aws.proto.optengine.persistance;

import dev.aws.proto.appcore.data.DdbServiceBase;
import dev.aws.proto.core.util.aws.SsmUtility;
import dev.aws.proto.optengine.config.DdbProperties;
import dev.aws.proto.optengine.persistance.data.SolverJobStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class DdbSolverJobService extends DdbServiceBase {
    private static final Logger logger = LoggerFactory.getLogger(DdbSolverJobService.class);

    private static final String PRIMARY_KEY = "Id";

    @Inject
    DdbProperties ddbProperties;

    final String tableName;

    DdbSolverJobService(DdbProperties ddbProperties) {
        this.ddbProperties = ddbProperties;
        this.tableName = SsmUtility.getParameterValue(ddbProperties.solverJobTableNameParam());
        super.dbClient = super.createDBClient();
    }

    @Override
    protected String getTableName() {
        return this.tableName;
    }

    @Override
    protected Map<String, AttributeValue> getPutItemMap(Object _solverJob) {
        var solverJob = (SolverJobStatus) _solverJob;

        Map<String, AttributeValue> item = new HashMap<>();
        // primary key
        item.put(PRIMARY_KEY, AttributeValue.builder().s(solverJob.getProblemId()).build());
        // status
        if(solverJob.getState() != null) item.put("state", AttributeValue.builder().s(solverJob.getState()).build());
        if(solverJob.getScore() != null) item.put("score", AttributeValue.builder().s(solverJob.getScore()).build());
        if(solverJob.getCreatedAt() > 0) item.put("createdAt", AttributeValue.builder().n(String.valueOf(solverJob.getCreatedAt())).build());
        if(solverJob.getSolverDurationInMs() > 0) item.put("solverDurationInMs", AttributeValue.builder().n(String.valueOf(solverJob.getSolverDurationInMs())).build());
        // result
        if(solverJob.getApiRequestFile() != null) item.put("fileRequest", AttributeValue.builder().s(solverJob.getApiRequestFile()).build());
        if(solverJob.getSolutionFile() != null) item.put("fileSolution", AttributeValue.builder().s(solverJob.getSolutionFile()).build());
        if(solverJob.getInspectionReportFile() != null) item.put("fileInspection", AttributeValue.builder().s(solverJob.getInspectionReportFile()).build());
        return item;
    }

    public void save(SolverJobStatus solverJob) {
        super.dbClient.putItem(super.putRequest(solverJob));

        logger.info("SolverJob saved: {}", solverJob);
    }

    public void saveRequestFileName(String problemId, String fileName) {
        var req = updateRequest(PRIMARY_KEY, problemId, "fileRequest", fileName);
        super.dbClient.updateItem(req);
        logger.info("saveRequestFileName -> {}",fileName);
    }

    public void saveSolutionFileName(String problemId, String fileName) {
        var req = updateRequest(PRIMARY_KEY, problemId, "fileSolution", fileName);
        var res = super.dbClient.updateItem(req);
        logger.info("saveSolutionFileName -> {}",res);
    }

    public void saveInspectionReportFileName(String problemId, String fileName) {
        var req = updateRequest(PRIMARY_KEY, problemId, "fileInspection", fileName);
        var res = super.dbClient.updateItem(req);
        logger.info("saveInspectionReportFileName -> {}",res);
    }

    private Map<String, AttributeValue> getItem(String problemId) {
        List<Map<String, AttributeValue>> dbItems = super.dbClient.query(this.getQueryRequest(PRIMARY_KEY, problemId)).items();
        if (dbItems.size() == 0) {
            return null;
        }

        return dbItems.get(0);
    }

    public SolverJobStatus getJobStatus(String problemId) {
        Map<String, AttributeValue> dbItem = getItem(problemId);

        SolverJobStatus result = SolverJobStatus.builder()
                .problemId(problemId)
                .createdAt(Long.parseLong(dbItem.get("createdAt").n()))
                .score(getStringAttribute(dbItem.get("score")))
                .solverDurationInMs(Long.parseLong(getNumberAttribute(dbItem.get("solverDurationInMs"))))
                .state(getStringAttribute(dbItem.get("state")))
                .build();

        return result;
    }

    public SolverJobStatus getFileInfo(String problemId) {
        Map<String, AttributeValue> dbItem = getItem(problemId);
        var a = dbItem.get("fileInspection");
        SolverJobStatus result = SolverJobStatus.builder()
                .problemId(problemId)
                .apiRequestFile(getStringAttribute(dbItem.get("fileRequest")))
                .solutionFile(getStringAttribute(dbItem.get("fileRequest")))
                .inspectionReportFile(getStringAttribute(dbItem.get("fileInspection")))
                .build();

        return result;
    }

    private String getStringAttribute(AttributeValue value) {
        if(value != null) return value.s();
        else return null;
    }

    private String getNumberAttribute(AttributeValue value) {
        if(value != null) return value.n();
        else return null;
    }
}
