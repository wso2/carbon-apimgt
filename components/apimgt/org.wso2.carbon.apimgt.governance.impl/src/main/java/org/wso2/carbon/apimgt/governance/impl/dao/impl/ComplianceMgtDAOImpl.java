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
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.ComplianceEvaluationRequest;
import org.wso2.carbon.apimgt.governance.api.model.ComplianceEvaluationResult;
import org.wso2.carbon.apimgt.governance.api.model.RuleViolation;
import org.wso2.carbon.apimgt.governance.api.model.Severity;
import org.wso2.carbon.apimgt.governance.impl.dao.ComplianceMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.governance.impl.util.GovernanceDBUtil;
import org.wso2.carbon.apimgt.governance.impl.util.GovernanceUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * @param artifactId   Artifact ID
     * @param artifactType Artifact Type
     * @param policyIds    Policy IDs
     * @throws GovernanceException If an error occurs while adding the artifact
     *                             compliance evaluation request event
     */
    @Override
    public void addComplianceEvalRequest(String artifactId, ArtifactType artifactType,
                                         List<String> policyIds, String organization)
            throws GovernanceException {


        String requestId;
        String artifactKey;
        try (Connection connection = GovernanceDBUtil.getConnection()) {

            try {
                connection.setAutoCommit(false);

                // Check for any pending requests for the artifact, if no pending requests add a new request,
                // else just add the policy mappings
                requestId = getPendingEvalRequest(artifactId, artifactType, organization, connection);
                if (requestId == null) {
                    requestId = GovernanceUtil.generateUUID();
                    artifactKey = checkAndAddArtifact(artifactId, artifactType, organization, connection);
                    String sqlQuery = SQLConstants.ADD_GOV_EVAL_REQ;
                    try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
                        prepStmnt.setString(1, requestId);
                        prepStmnt.setString(2, artifactKey);
                        prepStmnt.executeUpdate();
                    }
                }
                addRequestPolicyMappings(requestId, policyIds, connection);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new GovernanceException(
                    GovernanceExceptionCodes.ERROR_WHILE_ADDING_NEW_GOV_EVAL_REQUEST, e, artifactId);
        }
    }

    /**
     * Add an artifact compliance evaluation request event
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact Type
     * @param organization Organization
     * @param connection   Connection
     * @return Request ID
     * @throws SQLException If an error occurs while adding the artifact compliance evaluation
     *                      request event (Captured at a higher level)
     */
    private String getPendingEvalRequest(String artifactId, ArtifactType artifactType, String organization,
                                         Connection connection) throws SQLException {

        String sqlQuery = SQLConstants.GET_PENDING_REQ_FOR_ARTIFACT;
        try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, artifactId);
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
     * @param artifactId   Artifact ID
     * @param artifactType Artifact Type
     * @param organization Organization
     * @param connection   Connection
     * @return Artifact Key (or newly added artifact's key)
     * @throws SQLException If an error occurs while adding or retrieving the governance artifact
     */
    private String checkAndAddArtifact(String artifactId, ArtifactType artifactType, String organization,
                                       Connection connection) throws SQLException {

        String checkQuery = SQLConstants.GET_ARTIFACT_KEY;
        try (PreparedStatement checkStmnt = connection.prepareStatement(checkQuery)) {
            checkStmnt.setString(1, artifactId);
            checkStmnt.setString(2, String.valueOf(artifactType));
            checkStmnt.setString(3, organization);

            try (ResultSet resultSet = checkStmnt.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("ARTIFACT_KEY");
                }
            }
        }

        String artifactKey = GovernanceUtil.generateUUID();
        String insertQuery = SQLConstants.ADD_GOV_ARTIFACT;
        try (PreparedStatement insertStmnt = connection.prepareStatement(insertQuery)) {
            insertStmnt.setString(1, artifactKey);
            insertStmnt.setString(2, artifactId);
            insertStmnt.setString(3, String.valueOf(artifactType));
            insertStmnt.setString(4, organization);
            insertStmnt.executeUpdate();
            return artifactKey;
        }
    }


    /**
     * Add request policy mappings
     *
     * @param requestId  Request ID
     * @param policyIds  Policy IDs
     * @param connection Connection
     * @throws SQLException If an error occurs while adding the request
     *                      policy mappings (Captured at a higher level)
     */
    private void addRequestPolicyMappings(String requestId, List<String> policyIds, Connection connection)
            throws SQLException {

        String sqlQuery = SQLConstants.ADD_REQ_POLICY_MAPPING;

        try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            for (String policyId : policyIds) {
                try {
                    prepStmnt.setString(1, requestId);
                    prepStmnt.setString(2, policyId);
                    prepStmnt.execute();
                } catch (SQLIntegrityConstraintViolationException e) { // to catch and ignore duplicates
                    if (log.isDebugEnabled()) {
                        log.debug("The policy mapping already exists for the request: " + requestId +
                                " and policy: " + policyId);
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
     * @throws GovernanceException If an error occurs while updating the evaluation status
     */
    @Override
    public boolean updatePendingRequestToProcessing(String requestId) throws GovernanceException {

        String sqlQuery = SQLConstants.UPDATE_GOV_REQ_STATUS_TO_PROCESSING;
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, requestId);
            int affectedRows = prepStmnt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            return false;
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes
                    .ERROR_WHILE_UPDATING_GOV_EVAL_REQUEST, e, requestId);
        }
    }

    /**
     * Update the evaluation status of all processing requests to pending
     *
     * @throws GovernanceException If an error occurs while updating the evaluation status
     */
    @Override
    public void updateProcessingRequestToPending() throws GovernanceException {

        String sqlQuery = SQLConstants.UPDATE_GOV_REQ_STATUS_FROM_PROCESSING_TO_PENDING;
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.executeUpdate();
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes
                    .ERROR_WHILE_CHANGING_PROCESSING_REQ_TO_PENDING, e);
        }
    }

    /**
     * Delete an evaluation request
     *
     * @param requestId Evaluation request ID
     * @throws GovernanceException If an error occurs while deleting the evaluation request
     */
    @Override
    public void deleteComplianceEvalRequest(String requestId) throws GovernanceException {

        try (Connection connection = GovernanceDBUtil.getConnection()) {
            connection.setAutoCommit(false);

            String sqlQuery = SQLConstants.DELETE_REQ_POLICY_MAPPING;
            try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
                prepStmnt.setString(1, requestId);
                prepStmnt.executeUpdate();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

            sqlQuery = SQLConstants.DELETE_GOV_REQ;
            try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
                prepStmnt.setString(1, requestId);
                prepStmnt.executeUpdate();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
            connection.commit();
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes
                    .ERROR_WHILE_DELETING_GOVERNANCE_EVAL_REQUEST,
                    e, requestId);
        }
    }

    /**
     * Delete evaluation requests for an artifact
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact Type
     * @param organization Organization
     * @throws GovernanceException If an error occurs while deleting the evaluation request
     */
    @Override
    public void deleteComplianceEvalReqsForArtifact(String artifactId, ArtifactType
            artifactType, String organization) throws GovernanceException {

        try (Connection connection = GovernanceDBUtil.getConnection()) {
            connection.setAutoCommit(false);

            String sqlQuery = SQLConstants.DELETE_REQ_POLICY_MAPPING_FOR_ARTIFACT;
            try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
                prepStmnt.setString(1, artifactId);
                prepStmnt.setString(2, String.valueOf(artifactType));
                prepStmnt.setString(3, organization);
                prepStmnt.executeUpdate();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

            sqlQuery = SQLConstants.DELETE_GOV_REQ_FOR_ARTIFACT;
            try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
                prepStmnt.setString(1, artifactId);
                prepStmnt.setString(2, String.valueOf(artifactType));
                prepStmnt.setString(3, organization);
                prepStmnt.executeUpdate();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes
                    .ERROR_WHILE_DELETING_GOVERNANCE_EVAL_REQUESTS, e);
        }
    }

    /**
     * Get pending evaluation requests
     *
     * @return List of pending evaluation requests
     * @throws GovernanceException If an error occurs while getting the pending evaluation requests
     */
    @Override
    public List<ComplianceEvaluationRequest> getPendingComplianceEvalRequests() throws GovernanceException {

        String sqlQuery = SQLConstants.GET_PENDING_REQ;
        List<ComplianceEvaluationRequest> complianceEvaluationRequests = new ArrayList<>();

        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = prepStmnt.executeQuery()) {
            while (resultSet.next()) {
                ComplianceEvaluationRequest request = new ComplianceEvaluationRequest();
                request.setId(resultSet.getString("REQ_ID"));
                request.setArtifactId(resultSet.getString("ARTIFACT_ID"));
                request.setArtifactType(ArtifactType.fromString(resultSet
                        .getString("ARTIFACT_TYPE")));
                request.setOrganization(resultSet.getString("ORGANIZATION"));
                request.setPolicyIds(getPolicyIdsForRequest(request.getId(), connection));
                complianceEvaluationRequests.add(request);
            }

            return complianceEvaluationRequests;
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes
                    .ERROR_WHILE_GETTING_GOV_EVAL_REQUESTS, e);
        }
    }

    /**
     * Get policy IDs for a request
     *
     * @param requestId  Request ID
     * @param connection Connection
     * @return List of policy IDs
     * @throws SQLException If an error occurs while getting the policy IDs (Captured at a higher level)
     */
    private List<String> getPolicyIdsForRequest(String requestId, Connection connection) throws SQLException {

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
     * @param artifactId          Artifact ID
     * @param artifactType        Artifact Type
     * @param policyId            Policy ID
     * @param ruleViolationsMap   Map of Rule Violations
     * @param organization        Organization
     * @param isPolicyEvalSuccess Policy evaluation result
     * @throws GovernanceException If an error occurs while adding the compliance evaluation results
     */
    @Override
    public void addComplianceEvalResults(String artifactId, ArtifactType artifactType, String policyId,
                                         Map<String, List<RuleViolation>> ruleViolationsMap,
                                         String organization, boolean isPolicyEvalSuccess)
            throws GovernanceException {

        try (Connection connection = GovernanceDBUtil.getConnection()) {
            connection.setAutoCommit(false);

            try {
                clearOldRuleViolations(artifactId, artifactType, policyId, organization, connection);
                clearOldRulesetResults(artifactId, artifactType, policyId, organization, connection);
                clearOldPolicyResult(artifactId, artifactType, policyId, organization, connection);
                String artifactKey = getArtifactKey(artifactId, artifactType, organization, connection);
                if (artifactKey == null) {
                    throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_RETRIEVING_ARTIFACT_INFO,
                            artifactId);
                }
                String policyResultId = addToPolicyResults(artifactKey, policyId,
                        isPolicyEvalSuccess, connection);
                for (Map.Entry<String, List<RuleViolation>> entry : ruleViolationsMap.entrySet()) {
                    String rulesetId = entry.getKey();
                    List<RuleViolation> ruleViolations = entry.getValue();
                    String rulesetResultId = addToRulesetResults(policyResultId, rulesetId,
                            ruleViolations.isEmpty(), connection);
                    addRuleViolations(rulesetResultId, ruleViolations, connection);
                }

                connection.commit();
            } catch (SQLException | GovernanceException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_SAVING_GOVERNANCE_RESULT,
                    e, artifactId);
        }
    }

    /**
     * Clear old policy result for the artifact, policy combination
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact Type
     * @param policyId     Policy ID
     * @param organization Organization
     * @param connection   Connection
     * @throws SQLException If an error occurs while clearing the old policy result
     */
    private void clearOldPolicyResult(String artifactId, ArtifactType artifactType, String policyId,
                                      String organization, Connection connection)
            throws SQLException {

        String sqlQuery = SQLConstants.DELETE_POLICY_RESULT_FOR_ARTIFACT_AND_POLICY;
        try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, artifactId);
            prepStmnt.setString(2, String.valueOf(artifactType));
            prepStmnt.setString(3, organization);
            prepStmnt.setString(4, policyId);
            prepStmnt.executeUpdate();
        }
    }

    /**
     * Clear old ruleset results for the artifact, policy combination
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact Type
     * @param policyId     Policy ID
     * @param organization Organization
     * @param connection   Connection
     * @throws SQLException If an error occurs while clearing the old ruleset results
     */
    private void clearOldRulesetResults(String artifactId, ArtifactType artifactType, String policyId,
                                        String organization, Connection connection)
            throws SQLException {

        String sqlQuery = SQLConstants.DELETE_RULESET_RESULT_FOR_ARTIFACT_AND_POLICY;
        try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, artifactId);
            prepStmnt.setString(2, String.valueOf(artifactType));
            prepStmnt.setString(3, organization);
            prepStmnt.setString(4, policyId);
            prepStmnt.executeUpdate();
        }
    }

    /**
     * Clear rule violations for the artifact, policy combination
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact Type
     * @param policyId     Policy ID
     * @param organization Organization
     * @param connection   Connection
     * @throws SQLException If an error occurs while clearing the rule violations
     */
    private void clearOldRuleViolations(String artifactId, ArtifactType artifactType, String policyId,
                                        String organization, Connection connection)
            throws SQLException {

        String sqlQuery = SQLConstants.DELETE_RULE_VIOLATIONS_FOR_ARTIFACT_AND_POLICY;
        try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, artifactId);
            prepStmnt.setString(2, String.valueOf(artifactType));
            prepStmnt.setString(3, organization);
            prepStmnt.setString(4, policyId);
            prepStmnt.executeUpdate();
        }
    }

    /**
     * Get the artifact key
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact Type
     * @param organization Organization
     * @param connection   Connection
     * @return Artifact Key
     * @throws SQLException If an error occurs while getting the artifact key
     */
    private String getArtifactKey(String artifactId, ArtifactType artifactType,
                                  String organization,
                                  Connection connection) throws SQLException {

        String sqlQuery = SQLConstants.GET_ARTIFACT_KEY;
        try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, artifactId);
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
     * <p>
     * Check for existing result if not present add a new result
     *
     * @param artifactKey         Artifact Key
     * @param policyId            Policy ID
     * @param isPolicyEvalSuccess Evaluation result
     * @param connection          Connection
     * @return Policy Result ID
     * @throws SQLException If an error occurs while adding the policy compliance evaluation result
     */
    private String addToPolicyResults(String artifactKey, String policyId, boolean isPolicyEvalSuccess,
                                      Connection connection) throws SQLException {

        String sqlQuery = SQLConstants.ADD_POLICY_RESULT;
        try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            String policyResultId = GovernanceUtil.generateUUID();
            prepStmnt.setString(1, policyResultId);
            prepStmnt.setString(2, artifactKey);
            prepStmnt.setString(3, policyId);
            prepStmnt.setInt(4, isPolicyEvalSuccess ? 1 : 0);
            prepStmnt.executeUpdate();
            return policyResultId;
        }
    }

    /**
     * Add a ruleset compliance evaluation result
     * <p>
     * Check for existing result if not present add a new result
     *
     * @param policyResultId       Policy Result ID
     * @param rulesetId            Ruleset ID
     * @param isRulesetEvalSuccess Evaluation result
     * @param connection           Connection
     * @return Ruleset Result ID
     * @throws SQLException If an error occurs while adding the ruleset compliance evaluation result
     */
    private String addToRulesetResults(String policyResultId, String rulesetId,
                                       boolean isRulesetEvalSuccess, Connection connection) throws SQLException {

        String sqlQuery = SQLConstants.ADD_RULESET_RESULT;
        try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            String rulesetResultId = GovernanceUtil.generateUUID();
            prepStmnt.setString(1, rulesetResultId);
            prepStmnt.setString(2, policyResultId);
            prepStmnt.setString(3, rulesetId);
            prepStmnt.setInt(4, isRulesetEvalSuccess ? 1 : 0);
            prepStmnt.executeUpdate();
            return rulesetResultId;
        }
    }

    /**
     * Add rule violations
     *
     * @param rulesetResultId Result ID for the ruleset
     * @param ruleViolations  List of rule violations
     * @param connection      Connection
     * @throws SQLException If an error occurs while adding the rule violations
     */
    private void addRuleViolations(String rulesetResultId, List<RuleViolation> ruleViolations,
                                   Connection connection)
            throws SQLException {

        String sqlQuery = SQLConstants.ADD_RULE_VIOLATION;
        try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            for (RuleViolation ruleViolation : ruleViolations) {
                prepStmnt.setString(1, rulesetResultId);
                prepStmnt.setString(2, ruleViolation.getRulesetId());
                prepStmnt.setString(3, ruleViolation.getRuleName());
                prepStmnt.setString(4, ruleViolation.getViolatedPath());
                prepStmnt.addBatch();
            }
            prepStmnt.executeBatch();
        }
    }

    /**
     * Get the rule violations
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact Type
     * @param policyId     Policy ID
     * @param rulesetId    Ruleset ID
     * @param organization Organization
     * @return List of rule violations
     * @throws GovernanceException If an error occurs while getting the rule violations
     */
    @Override
    public List<RuleViolation> getRuleViolations(String artifactId, ArtifactType artifactType, String policyId,
                                                 String rulesetId, String organization) throws GovernanceException {

        String sqlQuery = SQLConstants.GET_RULE_VIOLATIONS_WITH_POLICY;
        List<RuleViolation> ruleViolations = new ArrayList<>();
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, artifactId);
            prepStmnt.setString(2, String.valueOf(artifactType));
            prepStmnt.setString(3, policyId);
            prepStmnt.setString(4, rulesetId);
            prepStmnt.setString(5, organization);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                while (resultSet.next()) {
                    RuleViolation ruleViolation = new RuleViolation();
                    ruleViolation.setArtifactId(artifactId);
                    ruleViolation.setArtifactType(artifactType);
                    ruleViolation.setPolicyId(policyId);
                    ruleViolation.setRulesetId(rulesetId);
                    ruleViolation.setRuleName(resultSet.getString("RULE_NAME"));
                    ruleViolation.setViolatedPath(resultSet.getString("VIOLATED_PATH"));
                    ruleViolation.setSeverity(Severity.fromString(resultSet.getString("SEVERITY")));
                    ruleViolation.setOrganization(organization);
                    ruleViolations.add(ruleViolation);
                }
            }
            return ruleViolations;
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_GETTING_RULE_VIOLATIONS,
                    e);
        }
    }

    /**
     * Get the rule violations
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact Type
     * @param rulesetId    Ruleset ID
     * @param organization Organization
     * @return List of rule violations
     * @throws GovernanceException If an error occurs while getting the rule violations
     */
    @Override
    public List<RuleViolation> getRuleViolations(String artifactId, ArtifactType artifactType,
                                                 String rulesetId, String organization) throws GovernanceException {

        String sqlQuery = SQLConstants.GET_RULE_VIOLATIONS;
        List<RuleViolation> ruleViolations = new ArrayList<>();
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, artifactId);
            prepStmnt.setString(2, String.valueOf(artifactType));
            prepStmnt.setString(3, rulesetId);
            prepStmnt.setString(4, organization);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                while (resultSet.next()) {
                    RuleViolation ruleViolation = new RuleViolation();
                    ruleViolation.setArtifactId(artifactId);
                    ruleViolation.setArtifactType(artifactType);
                    ruleViolation.setRulesetId(rulesetId);
                    ruleViolation.setRuleName(resultSet.getString("RULE_NAME"));
                    ruleViolation.setViolatedPath(resultSet.getString("VIOLATED_PATH"));
                    ruleViolation.setSeverity(Severity.fromString(resultSet.getString("SEVERITY")));
                    ruleViolation.setOrganization(organization);
                    ruleViolations.add(ruleViolation);
                }
            }
            return ruleViolations;
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_GETTING_RULE_VIOLATIONS,
                    e);
        }
    }

    /**
     * Get the rule violations for an artifact
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact Type
     * @param organization Organization
     * @return List of Rule Violations
     * @throws GovernanceException If an error occurs while getting the rule violations
     */
    @Override
    public List<RuleViolation> getRuleViolationsForArtifact(String artifactId, ArtifactType artifactType,
                                                            String organization) throws GovernanceException {

        String sqlQuery = SQLConstants.GET_RULE_VIOLATIONS_FOR_ARTIFACT;
        List<RuleViolation> ruleViolations = new ArrayList<>();
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, artifactId);
            prepStmnt.setString(2, String.valueOf(artifactType));
            prepStmnt.setString(3, organization);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                while (resultSet.next()) {
                    RuleViolation ruleViolation = new RuleViolation();
                    ruleViolation.setArtifactId(artifactId);
                    ruleViolation.setArtifactType(artifactType);
                    ruleViolation.setPolicyId(resultSet.getString("POLICY_ID"));
                    ruleViolation.setRulesetId(resultSet.getString("RULESET_ID"));
                    ruleViolation.setRuleName(resultSet.getString("RULE_NAME"));
                    ruleViolation.setViolatedPath(resultSet.getString("VIOLATED_PATH"));
                    ruleViolation.setSeverity(Severity.fromString(resultSet.getString("SEVERITY")));
                    ruleViolation.setOrganization(organization);
                    ruleViolations.add(ruleViolation);
                }
            }
            return ruleViolations;
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_GETTING_RULE_VIOLATIONS,
                    e);
        }
    }

    /**
     * Get the compliance evaluation result, null if not found
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact Type
     * @param policyId     Policy ID
     * @param rulesetId    Ruleset ID
     * @param organization Organization
     * @return ComplianceEvaluationResult
     * @throws GovernanceException If an error occurs while getting the compliance evaluation result
     */
    @Override
    public ComplianceEvaluationResult getComplianceEvaluationResult(String artifactId, ArtifactType artifactType,
                                                                    String policyId,
                                                                    String rulesetId, String organization)
            throws GovernanceException {

        String sqlQuery = SQLConstants.GET_GOV_COMPLIANCE_EVALUATION_RESULT;
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, artifactId);
            prepStmnt.setString(2, String.valueOf(artifactType));
            prepStmnt.setString(3, policyId);
            prepStmnt.setString(4, rulesetId);
            prepStmnt.setString(5, organization);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                if (resultSet.next()) {
                    ComplianceEvaluationResult result = new ComplianceEvaluationResult();
                    result.setArtifactId(artifactId);
                    result.setArtifactType(artifactType);
                    result.setPolicyId(policyId);
                    result.setRulesetId(rulesetId);
                    result.setEvaluationSuccess(resultSet.getInt("EVALUATION_RESULT") == 1);
                    result.setOrganization(organization);
                    return result;
                }
            }
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_GETTING_GOVERNANCE_RESULTS,
                    e);
        }
        return null;
    }

    /**
     * Get compliance evaluation results by artifact ID and type
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact Type
     * @param organization Organization
     * @return List of ComplianceEvaluationResult
     * @throws GovernanceException If an error occurs while getting the compliance evaluation results
     */
    @Override
    public List<ComplianceEvaluationResult> getComplianceEvaluationResultsForArtifact(String artifactId,
                                                                                      ArtifactType artifactType,
                                                                                      String organization)
            throws GovernanceException {

        String sqlQuery = SQLConstants.GET_GOV_COMPLIANCE_EVALUATION_RESULTS_FOR_ARTIFACT;
        List<ComplianceEvaluationResult> complianceEvaluationResults = new ArrayList<>();
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, artifactId);
            prepStmnt.setString(2, String.valueOf(artifactType));
            prepStmnt.setString(3, organization);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                while (resultSet.next()) {
                    ComplianceEvaluationResult result = new ComplianceEvaluationResult();
                    result.setArtifactId(artifactId);
                    result.setArtifactType(artifactType);
                    result.setPolicyId(resultSet.getString("POLICY_ID"));
                    result.setRulesetId(resultSet.getString("RULESET_ID"));
                    result.setEvaluationSuccess(resultSet.getInt("EVALUATION_RESULT") == 1);
                    result.setOrganization(organization);
                    complianceEvaluationResults.add(result);
                }
            }
            return complianceEvaluationResults;
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_GETTING_GOVERNANCE_RESULTS,
                    e);
        }
    }

    /**
     * Get compliance evaluation results for an artifact and policy
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact Type
     * @param policyId     Policy ID
     * @param organization Organization
     * @return List of ComplianceEvaluationResult
     * @throws GovernanceException If an error occurs while getting the compliance evaluation results
     */
    @Override
    public List<ComplianceEvaluationResult>
    getComplianceEvaluationResultsForArtifactAndPolicy(String artifactId, ArtifactType artifactType,
                                                       String policyId, String organization)
            throws GovernanceException {

        String sqlQuery = SQLConstants.GET_GOV_COMPLIANCE_EVALUATION_RESULTS_BY_ARTIFACT_AND_POLICY;
        List<ComplianceEvaluationResult> complianceEvaluationResults = new ArrayList<>();
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, artifactId);
            prepStmnt.setString(2, String.valueOf(artifactType));
            prepStmnt.setString(3, policyId);
            prepStmnt.setString(4, organization);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                while (resultSet.next()) {
                    ComplianceEvaluationResult result = new ComplianceEvaluationResult();
                    result.setArtifactId(artifactId);
                    result.setArtifactType(artifactType);
                    result.setPolicyId(policyId);
                    result.setRulesetId(resultSet.getString("RULESET_ID"));
                    result.setEvaluationSuccess(resultSet.getInt("EVALUATION_RESULT") == 1);
                    result.setOrganization(organization);
                    complianceEvaluationResults.add(result);
                }
            }
            return complianceEvaluationResults;
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_GETTING_GOVERNANCE_RESULTS,
                    e);
        }

    }

    /**
     * Get compliance evaluation results by artifact and ruleset
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact Type
     * @param rulesetId    Ruleset ID
     * @param organization Organization
     * @return List of ComplianceEvaluationResult
     * @throws GovernanceException If an error occurs while getting the compliance evaluation results
     */
    @Override
    public List<ComplianceEvaluationResult>
    getComplianceEvaluationResultsForArtifactAndRuleset(String artifactId, ArtifactType artifactType,
                                                        String rulesetId, String organization)
            throws GovernanceException {

        String sqlQuery = SQLConstants.GET_GOV_COMPLIANCE_EVALUATION_RESULTS_ARTIFACT_AND_RULESET;
        List<ComplianceEvaluationResult> complianceEvaluationResults = new ArrayList<>();
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, artifactId);
            prepStmnt.setString(2, String.valueOf(artifactType));
            prepStmnt.setString(3, rulesetId);
            prepStmnt.setString(4, organization);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                while (resultSet.next()) {
                    ComplianceEvaluationResult result = new ComplianceEvaluationResult();
                    result.setArtifactId(artifactId);
                    result.setArtifactType(artifactType);
                    result.setPolicyId(resultSet.getString("POLICY_ID"));
                    result.setRulesetId(resultSet.getString("RULESET_ID"));
                    result.setEvaluationSuccess(resultSet.getInt("EVALUATION_RESULT") == 1);
                    result.setOrganization(organization);
                    complianceEvaluationResults.add(result);
                }
            }
            return complianceEvaluationResults;
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_GETTING_GOVERNANCE_RESULTS,
                    e);
        }
    }

    /**
     * Get list of all compliance evaluated artifacts
     *
     * @param artifactType Artifact Type
     * @param organization Organization
     * @return List of all compliance evaluated artifacts
     * @throws GovernanceException If an error occurs while getting the list of all compliance evaluated artifacts
     */
    @Override
    public List<String> getAllComplianceEvaluatedArtifacts(ArtifactType artifactType, String organization)
            throws GovernanceException {

        String sqlQuery = SQLConstants.GET_ALL_COMP_EVALUATED_ARTIFACTS;
        List<String> artifactIds = new ArrayList<>();
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, String.valueOf(artifactType));
            prepStmnt.setString(2, organization);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                while (resultSet.next()) {
                    artifactIds.add(resultSet.getString("ARTIFACT_ID"));
                }
            }
            return artifactIds;
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_GETTING_GOVERNANCE_RESULTS,
                    e);
        }
    }

    /**
     * Get list of non-compliant artifacts
     *
     * @param artifactType Artifact Type
     * @param organization Organization
     * @return List of non-compliant artifacts
     * @throws GovernanceException If an error occurs while getting the list of non-compliant artifacts
     */
    @Override
    public List<String> getNonCompliantArtifacts(ArtifactType artifactType, String organization)
            throws GovernanceException {

        String sqlQuery = SQLConstants.GET_NON_COMPLIANT_ARTIFACTS;
        List<String> artifactIds = new ArrayList<>();
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, String.valueOf(artifactType));
            prepStmnt.setString(2, organization);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                while (resultSet.next()) {
                    artifactIds.add(resultSet.getString("ARTIFACT_ID"));
                }
            }
            return artifactIds;
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_GETTING_GOVERNANCE_RESULTS,
                    e);
        }
    }

    /**
     * Get list of all compliance evaluated policies
     *
     * @param organization Organization
     * @return List of all compliance evaluated policies
     * @throws GovernanceException If an error occurs while getting the list of all compliance evaluated policies
     */
    @Override
    public List<String> getAllComplianceEvaluatedPolicies(String organization) throws GovernanceException {

        String sqlQuery = SQLConstants.GET_ALL_COMP_EVALUATED_POLICIES;
        List<String> policyIds = new ArrayList<>();
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, organization);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                while (resultSet.next()) {
                    policyIds.add(resultSet.getString("POLICY_ID"));
                }
            }
            return policyIds;
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_GETTING_GOVERNANCE_RESULTS,
                    e);
        }
    }

    /**
     * Get list of violated policies
     *
     * @param organization Organization
     * @return List of violated policies
     * @throws GovernanceException If an error occurs while getting the list of violated policies
     */
    @Override
    public List<String> getViolatedPolicies(String organization) throws GovernanceException {

        String sqlQuery = SQLConstants.GET_VIOLATED_POLICIES;
        List<String> policyIds = new ArrayList<>();
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, organization);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                while (resultSet.next()) {
                    policyIds.add(resultSet.getString("POLICY_ID"));
                }
            }
            return policyIds;
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_GETTING_GOVERNANCE_RESULTS,
                    e);
        }
    }

    /**
     * Get compliance evaluation results for a given policy as a map of artifact type to list of
     * compliance evaluation results
     *
     * @param policyId Policy ID
     * @return Map of compliance evaluation results
     * @throws GovernanceException If an error occurs while getting the compliance evaluation results
     */
    @Override
    public Map<ArtifactType, List<ComplianceEvaluationResult>> getEvaluationResultsForPolicy(String policyId)
            throws GovernanceException {

        String sqlQuery = SQLConstants.GET_GOV_COMPLIANCE_EVALUATION_RESULTS_FOR_POLICY;
        Map<ArtifactType, List<ComplianceEvaluationResult>> complianceEvaluationResultsMap = new HashMap<>();
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, policyId);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                while (resultSet.next()) {
                    ComplianceEvaluationResult result = new ComplianceEvaluationResult();
                    ArtifactType artifactType = ArtifactType.fromString(resultSet.getString("ARTIFACT_TYPE"));
                    result.setPolicyId(policyId);
                    result.setArtifactId(resultSet.getString("ARTIFACT_ID"));
                    result.setArtifactType(artifactType);
                    result.setRulesetId(resultSet.getString("RULESET_ID"));
                    result.setEvaluationSuccess(resultSet.getInt("EVALUATION_RESULT") == 1);
                    result.setOrganization(resultSet.getString("ORGANIZATION"));

                    if (complianceEvaluationResultsMap.containsKey(artifactType)) {
                        complianceEvaluationResultsMap.get(artifactType).add(result);
                    } else {
                        List<ComplianceEvaluationResult> complianceEvaluationResults = new ArrayList<>();
                        complianceEvaluationResults.add(result);
                        complianceEvaluationResultsMap.put(artifactType, complianceEvaluationResults);
                    }
                }
            }
            return complianceEvaluationResultsMap;
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_GETTING_GOVERNANCE_RESULTS,
                    e);
        }

    }


    /**
     * Delete all governance data related to the artifact
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact Type
     * @param organization Organization
     * @throws GovernanceException If an error occurs while deleting the governance data
     */
    @Override
    public void deleteArtifact(String artifactId, ArtifactType artifactType, String organization)
            throws GovernanceException {
        try (Connection connection = GovernanceDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                deleteEvaluationRequestsForArtifact(artifactId, artifactType, organization, connection);
                deleteRuleViolationsForArtifact(artifactId, artifactType, organization, connection);
                deleteArtifactEvaluationResults(artifactId, artifactType, organization, connection);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_DELETING_GOVERNANCE_DATA,
                        e, artifactId);
            }
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_DELETING_GOVERNANCE_DATA,
                    e, artifactId);
        }
    }

    /**
     * Get compliance evaluation requests for a given artifact
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact Type
     * @param organization Organization
     * @param connection   Connection
     * @throws GovernanceException If an error occurs while getting the compliance evaluation requests
     */
    private void deleteEvaluationRequestsForArtifact(String artifactId, ArtifactType artifactType,
                                                     String organization, Connection connection)
            throws GovernanceException {

        String sqlQuery = SQLConstants.DELETE_EVALUATION_RESULT_FOR_ARTIFACT;
        try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, artifactId);
            prepStmnt.setString(2, String.valueOf(artifactType));
            prepStmnt.setString(3, organization);
            prepStmnt.executeUpdate();
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_DELETING_GOVERNANCE_DATA,
                    e, artifactId);
        }
    }

    /**
     * Delete evaluation results for an artifact
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact Type
     * @param organization Organization
     * @param connection   Connection
     * @throws GovernanceException If an error occurs while deleting the governance data
     */
    private void deleteArtifactEvaluationResults(String artifactId, ArtifactType artifactType,
                                                 String organization, Connection connection)
            throws GovernanceException {

        String sqlQuery = SQLConstants.DELETE_EVALUATION_RESULT_FOR_ARTIFACT;
        try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, artifactId);
            prepStmnt.setString(2, String.valueOf(artifactType));
            prepStmnt.setString(3, organization);
            prepStmnt.executeUpdate();
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_DELETING_GOVERNANCE_DATA,
                    e, artifactId);
        }
    }

    /**
     * Delete all governance rule violations related to the artifact
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact Type
     * @param organization Organization
     * @param connection   Connection
     * @throws GovernanceException If an error occurs while deleting the rule violations
     */
    private void deleteRuleViolationsForArtifact(String artifactId, ArtifactType artifactType,
                                                 String organization, Connection connection)
            throws GovernanceException {
        String sqlQuery = SQLConstants.DELETE_RULE_VIOLATIONS_FOR_ARTIFACT;
        try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, artifactId);
            prepStmnt.setString(2, String.valueOf(artifactType));
            prepStmnt.setString(3, organization);
            prepStmnt.executeUpdate();
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_DELETING_GOVERNANCE_DATA,
                    e, artifactId);
        }
    }
}
