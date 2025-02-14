/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.impl.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactInfo;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.ComplianceEvaluationRequest;
import org.wso2.carbon.apimgt.governance.api.model.RuleSeverity;
import org.wso2.carbon.apimgt.governance.api.model.RuleViolation;
import org.wso2.carbon.apimgt.governance.impl.dao.ComplianceMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceDBUtil;
import org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class represents the DAO class related to assessing compliance of APIs
 */
public class ComplianceMgtDAOImpl implements ComplianceMgtDAO {

    private static final Log log = LogFactory.getLog(ComplianceMgtDAOImpl.class);
    private ComplianceMgtDAOImpl() {

    }

    private static class SingletonHelper {
        private static final ComplianceMgtDAO INSTANCE = new ComplianceMgtDAOImpl();
    }

    /**
     * Get the instance of ComplianceMgtDAOImpl
     *
     * @return ComplianceMgtDAOImpl instance
     */
    public static ComplianceMgtDAO getInstance() {
        return SingletonHelper.INSTANCE;
    }

    /**
     * Add an artifact compliance evaluation request event
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param policyIds     Policy IDs
     * @throws APIMGovernanceException If an error occurs while adding the artifact
     *                                 compliance evaluation request event
     */
    @Override
    public void addComplianceEvalRequest(String artifactRefId, ArtifactType artifactType,
                                         List<String> policyIds, String organization)
            throws APIMGovernanceException {


        String requestId;
        String artifactKey;
        try (Connection connection = APIMGovernanceDBUtil.getConnection()) {

            try {
                connection.setAutoCommit(false);

                // Check for any pending requests for the artifact, if no pending requests add a new request,
                // else just add the policy mappings
                requestId = getPendingEvalRequest(connection, artifactRefId, artifactType, organization);
                if (requestId == null) {
                    requestId = APIMGovernanceUtil.generateUUID();
                    artifactKey = checkAndAddArtifact(connection, artifactRefId, artifactType, organization);
                    String sqlQuery = SQLConstants.ADD_GOV_EVAL_REQ;
                    try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
                        prepStmnt.setString(1, requestId);
                        prepStmnt.setString(2, artifactKey);
                        Timestamp requestedTime = new Timestamp(System.currentTimeMillis());
                        prepStmnt.setTimestamp(3, requestedTime);
                        prepStmnt.executeUpdate();
                    }
                }
                addRequestPolicyMappings(connection, requestId, policyIds);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new APIMGovernanceException(
                    APIMGovExceptionCodes.ERROR_WHILE_ADDING_NEW_GOV_EVAL_REQUEST, e, artifactRefId);
        }
    }

    /**
     * Add an artifact compliance evaluation request event
     *
     * @param connection    Connection
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param organization  Organization
     * @return Request ID
     * @throws SQLException If an error occurs while adding the artifact compliance evaluation
     *                      request event (Captured at a higher level)
     */
    private String getPendingEvalRequest(Connection connection, String artifactRefId, ArtifactType artifactType,
                                         String organization) throws SQLException {

        String sqlQuery = SQLConstants.GET_PENDING_REQ_FOR_ARTIFACT;
        try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, artifactRefId);
            prepStmnt.setString(2, String.valueOf(artifactType));
            prepStmnt.setString(3, organization);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("REQ_ID");
                }
            }
        }
        return null;
    }

    /**
     * Add a governance artifact if it does not already exist.
     *
     * @param connection    Connection
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param organization  Organization
     * @return Artifact Key (or newly added artifact's key)
     * @throws SQLException If an error occurs while adding or retrieving the governance artifact
     */
    private String checkAndAddArtifact(Connection connection, String artifactRefId, ArtifactType artifactType,
                                       String organization) throws SQLException {

        String checkQuery = SQLConstants.GET_ARTIFACT_KEY;
        try (PreparedStatement checkStmnt = connection.prepareStatement(checkQuery)) {
            checkStmnt.setString(1, artifactRefId);
            checkStmnt.setString(2, String.valueOf(artifactType));
            checkStmnt.setString(3, organization);

            try (ResultSet resultSet = checkStmnt.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("ARTIFACT_KEY");
                }
            }
        }

        String artifactKey = APIMGovernanceUtil.generateUUID();
        String insertQuery = SQLConstants.ADD_GOV_ARTIFACT;
        try (PreparedStatement insertStmnt = connection.prepareStatement(insertQuery)) {
            insertStmnt.setString(1, artifactKey);
            insertStmnt.setString(2, artifactRefId);
            insertStmnt.setString(3, String.valueOf(artifactType));
            insertStmnt.setString(4, organization);
            insertStmnt.executeUpdate();
            return artifactKey;
        }
    }


    /**
     * Add request policy mappings
     *
     * @param connection Connection
     * @param requestId  Request ID
     * @param policyIds  Policy IDs
     * @throws SQLException If an error occurs while adding the request
     *                      policy mappings (Captured at a higher level)
     */
    private void addRequestPolicyMappings(Connection connection,
                                          String requestId, List<String> policyIds)
            throws SQLException {

        List<String> existingPolicyIds = getPolicyIdsForRequest(connection, requestId);
        policyIds.removeAll(existingPolicyIds);

        String sqlQuery = SQLConstants.ADD_REQ_POLICY_MAPPING;
        try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            for (String policyId : policyIds) {
                try {
                    prepStmnt.setString(1, requestId);
                    prepStmnt.setString(2, policyId);
                    prepStmnt.execute();
                } catch (SQLException e) {
                    throw e;
                }
            }
        }
    }


    /**
     * Update the evaluation status of a pending request to processing
     *
     * @param requestId Request ID
     * @return True if the update is successful, false otherwise
     * @throws APIMGovernanceException If an error occurs while updating the evaluation status
     */
    @Override
    public boolean updatePendingRequestToProcessing(String requestId) throws APIMGovernanceException {

        String sqlQuery = SQLConstants.UPDATE_GOV_REQ_STATUS_TO_PROCESSING;
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            Timestamp processingTime = new Timestamp(System.currentTimeMillis());
            prepStmnt.setTimestamp(1, processingTime);
            prepStmnt.setString(2, requestId);
            int affectedRows = prepStmnt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            return false;
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes
                    .ERROR_WHILE_UPDATING_GOV_EVAL_REQUEST, e, requestId);
        }
    }

    /**
     * Update the evaluation status of all processing requests to pending
     *
     * @throws APIMGovernanceException If an error occurs while updating the evaluation status
     */
    @Override
    public void updateProcessingRequestToPending() throws APIMGovernanceException {

        String sqlQuery = SQLConstants.UPDATE_GOV_REQ_STATUS_FROM_PROCESSING_TO_PENDING;
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.executeUpdate();
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes
                    .ERROR_WHILE_CHANGING_PROCESSING_REQ_TO_PENDING, e);
        }
    }

    /**
     * Delete an evaluation request
     *
     * @param requestId Evaluation request ID
     * @throws APIMGovernanceException If an error occurs while deleting the evaluation request
     */
    @Override
    public void deleteComplianceEvalRequest(String requestId) throws APIMGovernanceException {

        try (Connection connection = APIMGovernanceDBUtil.getConnection()) {
            connection.setAutoCommit(false);

            try {
                String sqlQuery = SQLConstants.DELETE_REQ_POLICY_MAPPING;
                try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
                    prepStmnt.setString(1, requestId);
                    prepStmnt.executeUpdate();
                }

                sqlQuery = SQLConstants.DELETE_GOV_REQ;
                try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
                    prepStmnt.setString(1, requestId);
                    prepStmnt.executeUpdate();
                }

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes
                    .ERROR_WHILE_DELETING_GOVERNANCE_EVAL_REQUEST,
                    e, requestId);
        }
    }

    /**
     * Delete evaluation requests for an artifact
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param organization  Organization
     * @throws APIMGovernanceException If an error occurs while deleting the evaluation request
     */
    @Override
    public void deleteComplianceEvalReqsForArtifact(String artifactRefId, ArtifactType
            artifactType, String organization) throws APIMGovernanceException {

        try (Connection connection = APIMGovernanceDBUtil.getConnection()) {
            connection.setAutoCommit(false);

            try {
                String sqlQuery = SQLConstants.DELETE_REQ_POLICY_MAPPING_FOR_ARTIFACT;
                try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
                    prepStmnt.setString(1, artifactRefId);
                    prepStmnt.setString(2, String.valueOf(artifactType));
                    prepStmnt.setString(3, organization);
                    prepStmnt.executeUpdate();
                }

                sqlQuery = SQLConstants.DELETE_GOV_REQ_FOR_ARTIFACT;
                try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
                    prepStmnt.setString(1, artifactRefId);
                    prepStmnt.setString(2, String.valueOf(artifactType));
                    prepStmnt.setString(3, organization);
                    prepStmnt.executeUpdate();
                }

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes
                    .ERROR_WHILE_DELETING_GOVERNANCE_EVAL_REQUESTS, e);
        }
    }

    /**
     * Get pending evaluation requests
     *
     * @return List of pending evaluation requests
     * @throws APIMGovernanceException If an error occurs while getting the pending evaluation requests
     */
    @Override
    public List<ComplianceEvaluationRequest> getPendingComplianceEvalRequests() throws APIMGovernanceException {

        String sqlQuery = SQLConstants.GET_PENDING_REQ;
        List<ComplianceEvaluationRequest> complianceEvaluationRequests = new ArrayList<>();

        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = prepStmnt.executeQuery()) {
            while (resultSet.next()) {
                ComplianceEvaluationRequest request = new ComplianceEvaluationRequest();
                request.setId(resultSet.getString("REQ_ID"));
                request.setArtifactRefId(resultSet.getString("ARTIFACT_REF_ID"));
                request.setArtifactType(ArtifactType.fromString(resultSet
                        .getString("ARTIFACT_TYPE")));
                request.setOrganization(resultSet.getString("ORGANIZATION"));
                request.setPolicyIds(getPolicyIdsForRequest(connection, request.getId()));
                complianceEvaluationRequests.add(request);
            }

            return complianceEvaluationRequests;
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes
                    .ERROR_WHILE_GETTING_GOV_EVAL_REQUESTS, e);
        }
    }

    /**
     * Add an artifact compliance evaluation request event
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param organization  Organization
     * @return Request ID
     * @throws APIMGovernanceException If an error occurs while adding the artifact compliance evaluation
     *                                 request
     */
    @Override
    public String getPendingEvalRequest(String artifactRefId, ArtifactType artifactType,
                                        String organization) throws APIMGovernanceException {
        try (Connection connection = APIMGovernanceDBUtil.getConnection()) {
            return getPendingEvalRequest(connection, artifactRefId, artifactType, organization);
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes
                    .ERROR_WHILE_GETTING_GOV_EVAL_REQUEST_FOR_ARTIFACT, e, artifactRefId);
        }
    }

    /**
     * Get compliance pending artifacts
     *
     * @param artifactType Artifact Type
     * @param organization Organization
     * @return List of compliance pending artifacts
     * @throws APIMGovernanceException If an error occurs while getting the compliance pending artifacts
     */
    @Override
    public List<String> getCompliancePendingArtifacts(ArtifactType artifactType, String organization)
            throws APIMGovernanceException {
        String sqlQuery = SQLConstants.GET_COMPLIANCE_PENDING_ARTIFACTS;
        List<String> artifactIds = new ArrayList<>();
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, String.valueOf(artifactType));
            prepStmnt.setString(2, organization);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                while (resultSet.next()) {
                    artifactIds.add(resultSet.getString("ARTIFACT_REF_ID"));
                }
            }
            return artifactIds;
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes
                    .ERROR_WHILE_GETTING_COMPLIANCE_PENDING_ARTIFACTS, e);
        }
    }

    /**
     * Get policy IDs for a request
     *
     * @param connection Connection
     * @param requestId  Request ID
     * @return List of policy IDs
     * @throws SQLException If an error occurs while getting the policy IDs (Captured at a higher level)
     */
    private List<String> getPolicyIdsForRequest(Connection connection, String requestId) throws SQLException {

        String sqlQuery = SQLConstants.GET_REQ_POLICY_MAPPING;
        List<String> policyIds = new ArrayList<>();
        try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, requestId);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                while (resultSet.next()) {
                    policyIds.add(resultSet.getString("POLICY_ID"));
                }
            }
            return policyIds;
        }
    }

    /**
     * Add compliance evaluation results
     *
     * @param artifactRefId        Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType         Artifact Type
     * @param policyId             Policy ID
     * @param rulesetViolationsMap Map of Rulesets to Rule Violations
     * @param organization         Organization
     * @throws APIMGovernanceException If an error occurs while adding the compliance evaluation results
     */
    @Override
    public void addComplianceEvalResults(String artifactRefId, ArtifactType artifactType, String policyId,
                                         Map<String, List<RuleViolation>> rulesetViolationsMap, String organization)
            throws APIMGovernanceException {

        List<String> rulesetIds = new ArrayList<>(rulesetViolationsMap.keySet());
        try (Connection connection = APIMGovernanceDBUtil.getConnection()) {
            connection.setAutoCommit(false);

            try {
                clearOldRuleViolations(connection, artifactRefId, artifactType, rulesetIds, organization);
                clearOldRulesetRuns(connection, artifactRefId, artifactType, rulesetIds, organization);
                clearOldPolicyRun(connection, artifactRefId, artifactType, policyId, organization);

                String artifactKey = getArtifactKey(connection, artifactRefId, artifactType, organization);
                addPolicyRun(connection, artifactKey, policyId);

                for (Map.Entry<String, List<RuleViolation>> entry : rulesetViolationsMap.entrySet()) {
                    String rulesetId = entry.getKey();
                    List<RuleViolation> ruleViolations = entry.getValue();
                    String rulesetResultId = addRulesetRuns(connection, artifactKey, rulesetId,
                            ruleViolations.isEmpty());
                    addRuleViolations(connection, rulesetResultId, ruleViolations);
                }

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_SAVING_GOVERNANCE_RESULT,
                    e, artifactRefId);
        }
    }

    /**
     * Clear old policy run results for the artifact
     *
     * @param connection    Connection
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param policyId      Policy ID
     * @param organization  Organization
     * @throws SQLException If an error occurs while clearing the old policy result
     */
    private void clearOldPolicyRun(Connection connection, String artifactRefId, ArtifactType artifactType,
                                   String policyId, String organization)
            throws SQLException {

        String sqlQuery = SQLConstants.DELETE_POLICY_RUN_FOR_ARTIFACT_AND_POLICY;
        try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, artifactRefId);
            prepStmnt.setString(2, String.valueOf(artifactType));
            prepStmnt.setString(3, organization);
            prepStmnt.setString(4, policyId);
            prepStmnt.executeUpdate();
        }
    }

    /**
     * Clear old ruleset runs for the artifact
     *
     * @param connection    Connection
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param rulesetIds    List of Ruleset IDs
     * @param organization  Organization
     * @throws SQLException If an error occurs while clearing the old ruleset results
     */
    private void clearOldRulesetRuns(Connection connection, String artifactRefId, ArtifactType artifactType,
                                     List<String> rulesetIds, String organization) throws SQLException {

        String sqlQuery = SQLConstants.DELETE_RULESET_RUN_FOR_ARTIFACT_AND_RULESET;
        try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            for (String rulesetId : rulesetIds) {
                prepStmnt.setString(1, artifactRefId);
                prepStmnt.setString(2, String.valueOf(artifactType));
                prepStmnt.setString(3, organization);
                prepStmnt.setString(4, rulesetId);
                prepStmnt.addBatch();
            }
            prepStmnt.executeBatch();
        }
    }

    /**
     * Clear rule violations for the artifact and rulesets
     *
     * @param connection    Connection
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param rulesetIds    List of Ruleset IDs
     * @param organization  Organization
     * @throws SQLException If an error occurs while clearing the rule violations
     */
    private void clearOldRuleViolations(Connection connection, String artifactRefId, ArtifactType artifactType,
                                        List<String> rulesetIds, String organization) throws SQLException {

        String sqlQuery = SQLConstants.DELETE_RULE_VIOLATIONS_FOR_ARTIFACT_AND_RULESET;
        try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            for (String rulesetId : rulesetIds) {
                prepStmnt.setString(1, artifactRefId);
                prepStmnt.setString(2, String.valueOf(artifactType));
                prepStmnt.setString(3, organization);
                prepStmnt.setString(4, rulesetId);
                prepStmnt.addBatch();
            }
            prepStmnt.executeBatch();
        }
    }

    /**
     * Get the artifact key
     *
     * @param connection    Connection
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param organization  Organization
     * @return Artifact Key
     * @throws SQLException If an error occurs while getting the artifact key
     */
    private String getArtifactKey(Connection connection, String artifactRefId, ArtifactType artifactType,
                                  String organization) throws SQLException {

        String sqlQuery = SQLConstants.GET_ARTIFACT_KEY;
        try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, artifactRefId);
            prepStmnt.setString(2, String.valueOf(artifactType));
            prepStmnt.setString(3, organization);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("ARTIFACT_KEY");
                }
            }
        }
        return null;
    }

    /**
     * Add a policy compliance evaluation result
     *
     * @param connection  Connection
     * @param artifactKey Artifact Key
     * @param policyId    Policy ID
     * @throws SQLException If an error occurs while adding the policy compliance evaluation result
     */
    private void addPolicyRun(Connection connection, String artifactKey, String policyId) throws SQLException {

        String sqlQuery = SQLConstants.ADD_POLICY_RUN;
        try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, artifactKey);
            prepStmnt.setString(2, policyId);
            Timestamp runTime = new Timestamp(System.currentTimeMillis());
            prepStmnt.setTimestamp(3, runTime);
            prepStmnt.executeUpdate();
        }
    }

    /**
     * Add a ruleset compliance evaluation result
     *
     * @param connection           Connection
     * @param artifactKey          Artifact Key
     * @param rulesetId            Ruleset ID
     * @param isRulesetEvalSuccess Evaluation result
     * @return Ruleset Result ID
     * @throws SQLException If an error occurs while adding the ruleset compliance evaluation result
     */
    private String addRulesetRuns(Connection connection, String artifactKey, String rulesetId,
                                  boolean isRulesetEvalSuccess) throws SQLException {

        String sqlQuery = SQLConstants.ADD_RULESET_RUN;
        try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            String rulesetResultId = APIMGovernanceUtil.generateUUID();
            prepStmnt.setString(1, rulesetResultId);
            prepStmnt.setString(2, artifactKey);
            prepStmnt.setString(3, rulesetId);
            prepStmnt.setInt(4, isRulesetEvalSuccess ? 1 : 0);
            Timestamp runTime = new Timestamp(System.currentTimeMillis());
            prepStmnt.setTimestamp(5, runTime);
            prepStmnt.executeUpdate();
            return rulesetResultId;
        }
    }

    /**
     * Add rule violations
     *
     * @param connection      Connection
     * @param rulesetResultId Result ID for the ruleset
     * @param ruleViolations  List of rule violations
     * @throws SQLException If an error occurs while adding the rule violations
     */
    private void addRuleViolations(Connection connection, String rulesetResultId,
                                   List<RuleViolation> ruleViolations) throws SQLException {

        String sqlQuery = SQLConstants.ADD_RULE_VIOLATION;
        try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            for (RuleViolation ruleViolation : ruleViolations) {
                prepStmnt.setString(1, APIMGovernanceUtil.generateUUID());
                prepStmnt.setString(2, rulesetResultId);
                prepStmnt.setString(3, ruleViolation.getRulesetId());
                prepStmnt.setString(4, ruleViolation.getRuleName());
                prepStmnt.setString(5, ruleViolation.getViolatedPath());
                prepStmnt.setString(6, ruleViolation.getRuleMessage());
                prepStmnt.addBatch();
            }
            prepStmnt.executeBatch();
        }
    }

    /**
     * Get the rule violations
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param rulesetId     Ruleset ID
     * @param organization  Organization
     * @return List of rule violations
     * @throws APIMGovernanceException If an error occurs while getting the rule violations
     */
    @Override
    public List<RuleViolation> getRuleViolations(String artifactRefId, ArtifactType artifactType,
                                                 String rulesetId, String organization) throws APIMGovernanceException {

        String sqlQuery = SQLConstants.GET_RULE_VIOLATIONS;
        List<RuleViolation> ruleViolations = new ArrayList<>();
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, artifactRefId);
            prepStmnt.setString(2, String.valueOf(artifactType));
            prepStmnt.setString(3, organization);
            prepStmnt.setString(4, rulesetId);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                while (resultSet.next()) {
                    RuleViolation ruleViolation = new RuleViolation();
                    ruleViolation.setArtifactRefId(artifactRefId);
                    ruleViolation.setArtifactType(artifactType);
                    ruleViolation.setRulesetId(rulesetId);
                    ruleViolation.setRuleName(resultSet.getString("RULE_NAME"));
                    ruleViolation.setViolatedPath(resultSet.getString("VIOLATED_PATH"));
                    ruleViolation.setRuleMessage(resultSet.getString("MESSAGE"));
                    ruleViolation.setSeverity(RuleSeverity.fromString(resultSet.getString("SEVERITY")));
                    ruleViolation.setOrganization(organization);
                    ruleViolations.add(ruleViolation);
                }
            }
            return ruleViolations;
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_RULE_VIOLATIONS,
                    e);
        }
    }

    /**
     * Get the rule violations for an artifact
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param organization  Organization
     * @return List of Rule Violations
     * @throws APIMGovernanceException If an error occurs while getting the rule violations
     */
    @Override
    public List<RuleViolation> getRuleViolationsForArtifact(String artifactRefId, ArtifactType artifactType,
                                                            String organization) throws APIMGovernanceException {

        String sqlQuery = SQLConstants.GET_RULE_VIOLATIONS_FOR_ARTIFACT;
        List<RuleViolation> ruleViolations = new ArrayList<>();
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, artifactRefId);
            prepStmnt.setString(2, String.valueOf(artifactType));
            prepStmnt.setString(3, organization);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                while (resultSet.next()) {
                    RuleViolation ruleViolation = new RuleViolation();
                    ruleViolation.setArtifactRefId(artifactRefId);
                    ruleViolation.setArtifactType(artifactType);
                    ruleViolation.setRulesetId(resultSet.getString("RULESET_ID"));
                    ruleViolation.setRuleName(resultSet.getString("RULE_NAME"));
                    ruleViolation.setViolatedPath(resultSet.getString("VIOLATED_PATH"));
                    ruleViolation.setRuleMessage(resultSet.getString("MESSAGE"));
                    ruleViolation.setSeverity(RuleSeverity.fromString(resultSet.getString("SEVERITY")));
                    ruleViolation.setOrganization(organization);
                    ruleViolations.add(ruleViolation);
                }
            }
            return ruleViolations;
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_RULE_VIOLATIONS,
                    e);
        }
    }

    /**
     * Get evaluated policies for an artifact
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param organization  Organization
     * @return List of Policy Ids
     * @throws APIMGovernanceException If an error occurs while getting the compliance evaluation results
     */
    @Override
    public List<String> getEvaluatedPoliciesForArtifact(String artifactRefId, ArtifactType artifactType,
                                                        String organization) throws APIMGovernanceException {

        String sqlQuery = SQLConstants.GET_POLICY_RUNS_FOR_ARTIFACT;
        List<String> policyIds = new ArrayList<>();
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, artifactRefId);
            prepStmnt.setString(2, String.valueOf(artifactType));
            prepStmnt.setString(3, organization);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                while (resultSet.next()) {
                    policyIds.add(resultSet.getString("POLICY_ID"));
                }
            }
            return policyIds;
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_GOVERNANCE_RESULTS, e);
        }
    }

    /**
     * Get ruleset runs for an artifact
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param organization  Organization
     * @return List of evaluated rulesets
     * @throws APIMGovernanceException If an error occurs while getting the compliance evaluation results
     */
    @Override
    public List<String> getEvaluatedRulesetsForArtifact(String artifactRefId, ArtifactType artifactType,
                                                        String organization)
            throws APIMGovernanceException {
        String sqlQuery = SQLConstants.GET_RULESET_RUNS_FOR_ARTIFACT;
        List<String> rulesetIds = new ArrayList<>();
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, artifactRefId);
            prepStmnt.setString(2, String.valueOf(artifactType));
            prepStmnt.setString(3, organization);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                while (resultSet.next()) {
                    rulesetIds.add(resultSet.getString("RULESET_ID"));
                }
            }
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_GOVERNANCE_RESULTS, e);
        }
        return rulesetIds;
    }

    /**
     * Check if a ruleset is evaluated for an artifact
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param rulesetId     Ruleset ID
     * @param organization  Organization
     * @return True if the ruleset is evaluated for the artifact
     * @throws APIMGovernanceException If an error occurs while getting the compliance evaluation results
     */
    @Override
    public boolean isRulesetEvaluatedForArtifact(String artifactRefId, ArtifactType artifactType,
                                                 String rulesetId, String organization)
            throws APIMGovernanceException {
        String sqlQuery = SQLConstants.GET_RULESET_RUN_FOR_ARTIFACT_AND_RULESET;
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, artifactRefId);
            prepStmnt.setString(2, String.valueOf(artifactType));
            prepStmnt.setString(3, organization);
            prepStmnt.setString(4, rulesetId);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_GOVERNANCE_RESULTS, e);
        }
    }

    /**
     * Get list of all compliance evaluated artifacts
     *
     * @param artifactType Artifact Type
     * @param organization Organization
     * @return List of all compliance evaluated artifacts
     * @throws APIMGovernanceException If an error occurs while getting the list of all compliance evaluated artifacts
     */
    @Override
    public List<String> getAllComplianceEvaluatedArtifacts(ArtifactType artifactType,
                                                           String organization) throws APIMGovernanceException {
        String sqlQuery = SQLConstants.GET_ALL_EVALUTED_ARTIFACTS;
        Set<String> artifactRefIds = new HashSet<>();
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, String.valueOf(artifactType));
            prepStmnt.setString(2, organization);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                while (resultSet.next()) {
                    artifactRefIds.add(resultSet.getString("ARTIFACT_REF_ID"));
                }
            }
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_GOVERNANCE_RESULTS, e);
        }
        return new ArrayList<>(artifactRefIds);
    }

    /**
     * Get list of non-compliant artifacts
     *
     * @param artifactType Artifact Type
     * @param organization Organization
     * @return List of non-compliant artifacts
     * @throws APIMGovernanceException If an error occurs while getting the list of non-compliant artifacts
     */
    @Override
    public List<String> getNonCompliantArtifacts(ArtifactType artifactType, String organization)
            throws APIMGovernanceException {
        String sqlQuery = SQLConstants.GET_NON_COMPLIANT_ARTIFACTS;
        Set<String> artifactRefIds = new HashSet<>();
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, String.valueOf(artifactType));
            prepStmnt.setString(2, organization);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                while (resultSet.next()) {
                    artifactRefIds.add(resultSet.getString("ARTIFACT_REF_ID"));
                }
            }
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_GOVERNANCE_RESULTS, e);
        }
        return new ArrayList<>(artifactRefIds);
    }

    /**
     * Get list of all compliance evaluated policies
     *
     * @param organization Organization
     * @return List of all compliance evaluated policies
     * @throws APIMGovernanceException If an error occurs while getting the list of all compliance evaluated policies
     */
    @Override
    public List<String> getAllComplianceEvaluatedPolicies(String organization) throws APIMGovernanceException {

        String sqlQuery = SQLConstants.GET_POLICY_RUNS;
        Set<String> policyIds = new HashSet<>();
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, organization);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                while (resultSet.next()) {
                    policyIds.add(resultSet.getString("POLICY_ID"));
                }
            }
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_GOVERNANCE_RESULTS, e);
        }
        return new ArrayList<>(policyIds);
    }

    /**
     * Get list of all violated rulesets
     *
     * @param organization Organization
     * @return List of all violated rulesets
     * @throws APIMGovernanceException If an error occurs while getting the list of all violated rulesets
     */
    @Override
    public List<String> getViolatedRulesets(String organization) throws APIMGovernanceException {

        String sqlQuery = SQLConstants.GET_FAILED_RULESET_RUNS;
        List<String> rulesetIds = new ArrayList<>();
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, organization);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                while (resultSet.next()) {
                    rulesetIds.add(resultSet.getString("RULESET_ID"));
                }
            }
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_GOVERNANCE_RESULTS, e);
        }
        return rulesetIds;
    }

    /**
     * Get list of all violated rulesets for an artifact
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param organization  Organization
     * @return List of all violated rulesets for an artifact
     * @throws APIMGovernanceException If an error occurs while getting the list of all
     *                                 violated rulesets for an artifact
     */
    @Override
    public List<String> getViolatedRulesetsForArtifact(String artifactRefId, ArtifactType artifactType,
                                                       String organization) throws APIMGovernanceException {
        String sqlQuery = SQLConstants.GET_FAILED_RULESET_RUNS_FOR_ARTIFACT;
        List<String> rulesetIds = new ArrayList<>();
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, artifactRefId);
            prepStmnt.setString(2, String.valueOf(artifactType));
            prepStmnt.setString(3, organization);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                while (resultSet.next()) {
                    rulesetIds.add(resultSet.getString("RULESET_ID"));
                }
            }
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_GOVERNANCE_RESULTS, e);
        }
        return rulesetIds;
    }

    /**
     * Get list of all evaluated artifacts for a policy
     *
     * @param policyId     Policy ID
     * @param organization Organization
     * @return List of all evaluated artifacts for a policy
     * @throws APIMGovernanceException If an error occurs while getting the list of all
     *                                 evaluated artifacts for a policy
     */
    @Override
    public List<ArtifactInfo> getEvaluatedArtifactsForPolicy(String policyId, String organization)
            throws APIMGovernanceException {
        String sqlQuery = SQLConstants.GET_ARITFCATS_FOR_POLICY_RUN;
        List<ArtifactInfo> artifactInfos = new ArrayList<>();
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, policyId);
            prepStmnt.setString(2, organization);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                while (resultSet.next()) {
                    ArtifactInfo artifactInfo = new ArtifactInfo();
                    artifactInfo.setArtifactRefId(resultSet.getString("ARTIFACT_REF_ID"));
                    artifactInfo.setArtifactType(ArtifactType.fromString
                            (resultSet.getString("ARTIFACT_TYPE")));
                    artifactInfos.add(artifactInfo);
                }
            }
            return artifactInfos;
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_GOVERNANCE_RESULTS, e);
        }
    }

    /**
     * Delete all governance data related to the artifact
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param organization  Organization
     * @throws APIMGovernanceException If an error occurs while deleting the governance data
     */
    @Override
    public void deleteArtifact(String artifactRefId, ArtifactType artifactType, String organization)
            throws APIMGovernanceException {
        try (Connection connection = APIMGovernanceDBUtil.getConnection()) {
            connection.setAutoCommit(false);

            try {
                try (PreparedStatement prepStmnt = connection.prepareStatement(SQLConstants
                        .DELETE_RULE_VIOLATIONS_FOR_ARTIFACT)) {
                    prepStmnt.setString(1, artifactRefId);
                    prepStmnt.setString(2, String.valueOf(artifactType));
                    prepStmnt.setString(3, organization);
                    prepStmnt.executeUpdate();
                }

                try (PreparedStatement prepStmnt = connection.prepareStatement(SQLConstants
                        .DELETE_RULESET_RUNS_FOR_ARTIFACT)) {
                    prepStmnt.setString(1, artifactRefId);
                    prepStmnt.setString(2, String.valueOf(artifactType));
                    prepStmnt.setString(3, organization);
                    prepStmnt.executeUpdate();
                }

                try (PreparedStatement prepStmnt = connection.prepareStatement(SQLConstants
                        .DELETE_POLICY_RUNS_FOR_ARTIFACT)) {
                    prepStmnt.setString(1, artifactRefId);
                    prepStmnt.setString(2, String.valueOf(artifactType));
                    prepStmnt.setString(3, organization);
                    prepStmnt.executeUpdate();
                }

                try (PreparedStatement prepStmnt = connection.prepareStatement(SQLConstants
                        .DELETE_REQ_POLICY_MAPPING_FOR_ARTIFACT)) {
                    prepStmnt.setString(1, artifactRefId);
                    prepStmnt.setString(2, String.valueOf(artifactType));
                    prepStmnt.setString(3, organization);
                    prepStmnt.executeUpdate();
                }

                try (PreparedStatement prepStmnt = connection.prepareStatement(SQLConstants
                        .DELETE_GOV_REQ_FOR_ARTIFACT)) {
                    prepStmnt.setString(1, artifactRefId);
                    prepStmnt.setString(2, String.valueOf(artifactType));
                    prepStmnt.setString(3, organization);
                    prepStmnt.executeUpdate();
                }

                try (PreparedStatement prepStmnt = connection.prepareStatement(SQLConstants
                        .DELETE_GOV_ARTIFACT)) {
                    prepStmnt.setString(1, artifactRefId);
                    prepStmnt.setString(2, String.valueOf(artifactType));
                    prepStmnt.setString(3, organization);
                    prepStmnt.executeUpdate();
                }

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes
                    .ERROR_WHILE_DELETING_GOVERNANCE_DATA, e, artifactRefId);
        }
    }
}
