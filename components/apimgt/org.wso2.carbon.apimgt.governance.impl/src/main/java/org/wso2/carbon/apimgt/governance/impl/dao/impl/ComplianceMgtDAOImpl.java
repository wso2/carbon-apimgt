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
     * @param artifactRefId       Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType        Artifact Type
     * @param policyAttachmentIds Policy Attachment IDs
     * @throws APIMGovernanceException If an error occurs while adding the artifact
     *                                 compliance evaluation request event
     */
    @Override
    public void addComplianceEvalRequest(String artifactRefId, ArtifactType artifactType,
                                         List<String> policyAttachmentIds, String organization)
            throws APIMGovernanceException {


        String requestId;
        String artifactKey;
        try (Connection connection = APIMGovernanceDBUtil.getConnection()) {

            try {
                connection.setAutoCommit(false);

                // Check for any pending requests for the artifact, if no pending requests add a new request,
                // else just add the policy attachment mappings
                requestId = getPendingEvalRequest(connection, artifactRefId, artifactType, organization);
                if (requestId == null) {
                    requestId = APIMGovernanceUtil.generateUUID();
                    artifactKey = checkAndAddArtifact(connection, artifactRefId, artifactType, organization);
                    String sqlQuery = SQLConstants.ADD_GOV_EVAL_REQ;
                    try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
                        prepStmnt.setString(1, requestId);
                        prepStmnt.setString(2, artifactKey);
                        prepStmnt.executeUpdate();
                    }
                }
                addRequestPolicyAttachmentMappings(connection, requestId, policyAttachmentIds);
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
     * Add request policy attachment mappings
     *
     * @param connection          Connection
     * @param requestId           Request ID
     * @param policyAttachmentIds Policy Attachemnt IDs
     * @throws SQLException If an error occurs while adding the request
     *                      policy mappings (Captured at a higher level)
     */
    private void addRequestPolicyAttachmentMappings(Connection connection,
                                                    String requestId, List<String> policyAttachmentIds)
            throws SQLException {

        String sqlQuery = SQLConstants.ADD_REQ_POLICY_ATTACHMENT_MAPPING;

        try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            for (String attachmentId : policyAttachmentIds) {
                try {
                    prepStmnt.setString(1, requestId);
                    prepStmnt.setString(2, attachmentId);
                    prepStmnt.execute();
                } catch (SQLIntegrityConstraintViolationException e) { // to catch and ignore duplicates
                    if (log.isDebugEnabled()) {
                        log.debug("The policy attachment mapping already exists for the request: " + requestId +
                                " and policy attachment: " + attachmentId);
                    }
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
            prepStmnt.setString(1, requestId);
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
                String sqlQuery = SQLConstants.DELETE_REQ_POLICY_ATTACHMENT_MAPPING;
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
                String sqlQuery = SQLConstants.DELETE_REQ_POLICY_ATTACHMENT_MAPPING_FOR_ARTIFACT;
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
                request.setPolicyAttachmentIds(getPolicyAttachmentIdsForRequest(connection, request.getId()));
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
     * Get policy attachment IDs for a request
     *
     * @param connection Connection
     * @param requestId  Request ID
     * @return List of policy attachemnt IDs
     * @throws SQLException If an error occurs while getting the policy attachment IDs (Captured at a higher level)
     */
    private List<String> getPolicyAttachmentIdsForRequest(Connection connection, String requestId) throws SQLException {

        String sqlQuery = SQLConstants.GET_REQ_POLICY_ATTACHMENT_MAPPING;
        List<String> policyAttachmentIds = new ArrayList<>();
        try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, requestId);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                while (resultSet.next()) {
                    policyAttachmentIds.add(resultSet.getString("POLICY_ATTACHMENT_ID"));
                }
            }
            return policyAttachmentIds;
        }
    }

    /**
     * Add compliance evaluation results
     *
     * @param artifactRefId       Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType        Artifact Type
     * @param policyAttachmentId  Policy Attachment ID
     * @param policyViolationsMap Map of Policies to Rule Violations
     * @param organization        Organization
     * @throws APIMGovernanceException If an error occurs while adding the compliance evaluation results
     */
    @Override
    public void addComplianceEvalResults(String artifactRefId, ArtifactType artifactType, String policyAttachmentId,
                                         Map<String, List<RuleViolation>> policyViolationsMap, String organization)
    throws APIMGovernanceException {

        List<String> policyIds = new ArrayList<>(policyViolationsMap.keySet());
        try (Connection connection = APIMGovernanceDBUtil.getConnection()) {
            connection.setAutoCommit(false);

            try {
                clearOldPolicyViolations(connection, artifactRefId, artifactType, policyIds, organization);
                clearOldPolicyRuns(connection, artifactRefId, artifactType, policyIds, organization);
                clearOldPolicyAttachmentRun(connection, artifactRefId, artifactType, policyAttachmentId, organization);

                String artifactKey = getArtifactKey(connection, artifactRefId, artifactType, organization);
                addPolicyAttachmentRun(connection, artifactKey, policyAttachmentId);

                for (Map.Entry<String, List<RuleViolation>> entry : policyViolationsMap.entrySet()) {
                    String policyId = entry.getKey();
                    List<RuleViolation> ruleViolations = entry.getValue();
                    String policyRunId = addPolicyRuns(connection, artifactKey, policyId,
                            ruleViolations.isEmpty());
                    addRuleViolations(connection, policyRunId, ruleViolations);
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
     * Clear old policy attachment run results for the artifact
     *
     * @param connection    Connection
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param policyAttachmentId      Policy ID
     * @param organization  Organization
     * @throws SQLException If an error occurs while clearing the old policy result
     */
    private void clearOldPolicyAttachmentRun(Connection connection, String artifactRefId, ArtifactType artifactType,
                                             String policyAttachmentId, String organization)
            throws SQLException {

        String sqlQuery = SQLConstants.DELETE_POLICY_ATTACHMENT_RUN_FOR_ARTIFACT_AND_POLICY_ATTACHMENT;
        try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, artifactRefId);
            prepStmnt.setString(2, String.valueOf(artifactType));
            prepStmnt.setString(3, organization);
            prepStmnt.setString(4, policyAttachmentId);
            prepStmnt.executeUpdate();
        }
    }

    /**
     * Clear old policy runs for the artifact
     *
     * @param connection    Connection
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param policyIds    List of Policy IDs
     * @param organization  Organization
     * @throws SQLException If an error occurs while clearing the old policy results
     */
    private void clearOldPolicyRuns(Connection connection, String artifactRefId, ArtifactType artifactType,
                                    List<String> policyIds, String organization) throws SQLException {

        String sqlQuery = SQLConstants.DELETE_POLICY_RUN_FOR_ARTIFACT_AND_POLICY;
        try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            for (String policyId : policyIds) {
                prepStmnt.setString(1, artifactRefId);
                prepStmnt.setString(2, String.valueOf(artifactType));
                prepStmnt.setString(3, organization);
                prepStmnt.setString(4, policyId);
                prepStmnt.addBatch();
            }
            prepStmnt.executeBatch();
        }
    }

    /**
     * Clear rule violations for the artifact and policies
     *
     * @param connection    Connection
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param policyIds    List of Policy IDs
     * @param organization  Organization
     * @throws SQLException If an error occurs while clearing the rule violations
     */
    private void clearOldPolicyViolations(Connection connection, String artifactRefId, ArtifactType artifactType,
                                          List<String> policyIds, String organization) throws SQLException {

        String sqlQuery = SQLConstants.DELETE_RULE_VIOLATIONS_FOR_ARTIFACT_AND_POLICY;
        try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            for (String policyId : policyIds) {
                prepStmnt.setString(1, artifactRefId);
                prepStmnt.setString(2, String.valueOf(artifactType));
                prepStmnt.setString(3, organization);
                prepStmnt.setString(4, policyId);
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
     * Add a policy attachment compliance evaluation result
     *
     * @param connection  Connection
     * @param artifactKey Artifact Key
     * @param policyAttachmentId    Policy Attachment ID
     * @throws SQLException If an error occurs while adding the policy compliance evaluation result
     */
    private void addPolicyAttachmentRun(Connection connection, String artifactKey, String policyAttachmentId)
            throws SQLException {

        String sqlQuery = SQLConstants.ADD_POLICY_ATTACHMENT_RUN;
        try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, artifactKey);
            prepStmnt.setString(2, policyAttachmentId);
            prepStmnt.executeUpdate();
        }
    }

    /**
     * Add a policy run
     *
     * @param connection           Connection
     * @param artifactKey          Artifact Key
     * @param policyId            Policy ID
     * @param isPolicyEvalSuccess Evaluation result
     * @return String Policy ID
     * @throws SQLException If an error occurs while adding the policy compliance evaluation result
     */
    private String addPolicyRuns(Connection connection, String artifactKey, String policyId,
                                 boolean isPolicyEvalSuccess) throws SQLException {

        String sqlQuery = SQLConstants.ADD_POLICY_RUN;
        try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            String policyResultId = APIMGovernanceUtil.generateUUID();
            prepStmnt.setString(1, policyResultId);
            prepStmnt.setString(2, artifactKey);
            prepStmnt.setString(3, policyId);
            prepStmnt.setInt(4, isPolicyEvalSuccess ? 1 : 0);
            prepStmnt.executeUpdate();
            return policyResultId;
        }
    }

    /**
     * Add rule violations
     *
     * @param connection      Connection
     * @param policyResultId Result ID for the policy
     * @param ruleViolations  List of rule violations
     * @throws SQLException If an error occurs while adding the rule violations
     */
    private void addRuleViolations(Connection connection, String policyResultId,
                                   List<RuleViolation> ruleViolations) throws SQLException {

        String sqlQuery = SQLConstants.ADD_RULE_VIOLATION;
        try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            for (RuleViolation ruleViolation : ruleViolations) {
                prepStmnt.setString(1, APIMGovernanceUtil.generateUUID());
                prepStmnt.setString(2, policyResultId);
                prepStmnt.setString(3, ruleViolation.getPolicyId());
                prepStmnt.setString(4, ruleViolation.getRuleName());
                prepStmnt.setString(5, ruleViolation.getViolatedPath());
                prepStmnt.setString(6, ruleViolation.getRuleMessage());
                prepStmnt.addBatch();
            }
            prepStmnt.executeBatch();
        }
    }

    /**
     * Get the policy violations
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param policyId      Policy ID
     * @param organization  Organization
     * @return List of rule violations
     * @throws APIMGovernanceException If an error occurs while getting the rule violations
     */
    @Override
    public List<RuleViolation> getPolicyRuleViolations(String artifactRefId, ArtifactType artifactType,
                                                       String policyId, String organization)
            throws APIMGovernanceException {

        String sqlQuery = SQLConstants.GET_RULE_VIOLATIONS;
        List<RuleViolation> ruleViolations = new ArrayList<>();
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, artifactRefId);
            prepStmnt.setString(2, String.valueOf(artifactType));
            prepStmnt.setString(3, organization);
            prepStmnt.setString(4, policyId);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                while (resultSet.next()) {
                    RuleViolation ruleViolation = new RuleViolation();
                    ruleViolation.setArtifactRefId(artifactRefId);
                    ruleViolation.setArtifactType(artifactType);
                    ruleViolation.setPolicyId(policyId);
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
    public List<RuleViolation> getPolicyRuleViolationsForArtifact(String artifactRefId, ArtifactType artifactType,
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
                    ruleViolation.setPolicyId(resultSet.getString("POLICY_ID"));
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
     * Get evaluated policy attchments for an artifact
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param organization  Organization
     * @return List of Policy Attachment Ids
     * @throws APIMGovernanceException If an error occurs while getting the compliance evaluation results
     */
    @Override
    public List<String> getEvaluatedPolicyAttachmentsForArtifact(String artifactRefId, ArtifactType artifactType,
                                                                 String organization) throws APIMGovernanceException {

        String sqlQuery = SQLConstants.GET_POLICY_ATTACHMENT_RUNS_FOR_ARTIFACT;
        List<String> policyAttachmentIds = new ArrayList<>();
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, artifactRefId);
            prepStmnt.setString(2, String.valueOf(artifactType));
            prepStmnt.setString(3, organization);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                while (resultSet.next()) {
                    policyAttachmentIds.add(resultSet.getString("POLICY_ATTACHMENT_ID"));
                }
            }
            return policyAttachmentIds;
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_GOVERNANCE_RESULTS, e);
        }
    }

    /**
     * Get policy runs for an artifact
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param organization  Organization
     * @return List of evaluated policies
     * @throws APIMGovernanceException If an error occurs while getting the compliance evaluation results
     */
    @Override
    public List<String> getEvaluatedPoliciesForArtifact(String artifactRefId, ArtifactType artifactType,
                                                        String organization)
            throws APIMGovernanceException {
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
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_GOVERNANCE_RESULTS, e);
        }
        return policyIds;
    }

    /**
     * Check if a policy is evaluated for an artifact
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param policyId     Policy ID
     * @param organization  Organization
     * @return True if the policy is evaluated for the artifact
     * @throws APIMGovernanceException If an error occurs while getting the compliance evaluation results
     */
    @Override
    public boolean isPolicyEvaluatedForArtifact(String artifactRefId, ArtifactType artifactType,
                                                String policyId, String organization)
    throws APIMGovernanceException {
        String sqlQuery = SQLConstants.GET_POLICY_RUN_FOR_ARTIFACT_AND_POLICY;
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, artifactRefId);
            prepStmnt.setString(2, String.valueOf(artifactType));
            prepStmnt.setString(3, organization);
            prepStmnt.setString(4, policyId);
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
        String sqlQuery = SQLConstants.GET_ALL_EVALUATED_ARTIFACTS;
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
     * Get list of all compliance evaluated policy attachments
     *
     * @param organization Organization
     * @return List of all compliance evaluated policy attachments
     * @throws APIMGovernanceException If an error occurs while getting the list of all compliance evaluated policies
     */
    @Override
    public List<String> getAllComplianceEvaluatedPolicyAttachments(String organization) throws APIMGovernanceException {

        String sqlQuery = SQLConstants.GET_POLICY_ATTACHMENT_RUNS;
        Set<String> policyAttachmentIds = new HashSet<>();
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, organization);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                while (resultSet.next()) {
                    policyAttachmentIds.add(resultSet.getString("POLICY_ATTACHMENT_ID"));
                }
            }
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_GOVERNANCE_RESULTS, e);
        }
        return new ArrayList<>(policyAttachmentIds);
    }

    /**
     * Get list of all violated policies
     *
     * @param organization Organization
     * @return List of all violated policies
     * @throws APIMGovernanceException If an error occurs while getting the list of all violated policies
     */
    @Override
    public List<String> getViolatedPolicies(String organization) throws APIMGovernanceException {

        String sqlQuery = SQLConstants.GET_FAILED_POLICY_RUNS;
        List<String> policyIds = new ArrayList<>();
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
        return policyIds;
    }

    /**
     * Get list of all violated policies for an artifact
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param organization  Organization
     * @return List of all violated policies for an artifact
     * @throws APIMGovernanceException If an error occurs while getting the list of all
     *                                 violated policies for an artifact
     */
    @Override
    public List<String> getViolatedPoliciesForArtifact(String artifactRefId, ArtifactType artifactType,
                                                       String organization) throws APIMGovernanceException {
        String sqlQuery = SQLConstants.GET_FAILED_POLICY_RUNS_FOR_ARTIFACT;
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
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_GOVERNANCE_RESULTS, e);
        }
        return policyIds;
    }

    /**
     * Get list of all evaluated artifacts for a policy attachment
     *
     * @param policyAttachmentId Policy Attachment ID
     * @param organization       Organization
     * @return List of all evaluated artifacts for a policy attachment
     * @throws APIMGovernanceException If an error occurs while getting the list of all
     *                                 evaluated artifacts for a policy attachment
     */
    @Override
    public List<ArtifactInfo> getEvaluatedArtifactsForPolicyAttachment(String policyAttachmentId,
                                                                       String organization)
            throws APIMGovernanceException {
        String sqlQuery = SQLConstants.GET_ARTIFACTS_FOR_POLICY_ATTACHMENT_RUN;
        List<ArtifactInfo> artifactInfos = new ArrayList<>();
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, policyAttachmentId);
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
                        .DELETE_POLICY_ATTACHMENT_RUNS_FOR_ARTIFACT)) {
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
                        .DELETE_REQ_POLICY_ATTACHMENT_MAPPING_FOR_ARTIFACT)) {
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
