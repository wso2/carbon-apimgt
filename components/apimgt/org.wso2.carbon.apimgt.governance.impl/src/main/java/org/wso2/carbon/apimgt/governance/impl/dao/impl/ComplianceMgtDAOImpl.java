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
import org.wso2.carbon.apimgt.governance.api.model.ArtifactInfo;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.ComplianceEvaluationRequest;
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
     * @throws GovernanceException If an error occurs while adding the artifact
     *                             compliance evaluation request event
     */
    @Override
    public void addComplianceEvalRequest(String artifactRefId, ArtifactType artifactType,
                                         List<String> policyIds, String organization)
            throws GovernanceException {


        String requestId;
        String artifactKey;
        try (Connection connection = GovernanceDBUtil.getConnection()) {

            try {
                connection.setAutoCommit(false);

                // Check for any pending requests for the artifact, if no pending requests add a new request,
                // else just add the policy mappings
                requestId = getPendingEvalRequest(artifactRefId, artifactType, organization, connection);
                if (requestId == null) {
                    requestId = GovernanceUtil.generateUUID();
                    artifactKey = checkAndAddArtifact(artifactRefId, artifactType, organization, connection);
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
                    GovernanceExceptionCodes.ERROR_WHILE_ADDING_NEW_GOV_EVAL_REQUEST, e, artifactRefId);
        }
    }

    /**
     * Add an artifact compliance evaluation request event
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param organization  Organization
     * @param connection    Connection
     * @return Request ID
     * @throws SQLException If an error occurs while adding the artifact compliance evaluation
     *                      request event (Captured at a higher level)
     */
    private String getPendingEvalRequest(String artifactRefId, ArtifactType artifactType, String organization,
                                         Connection connection) throws SQLException {

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
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param organization  Organization
     * @param connection    Connection
     * @return Artifact Key (or newly added artifact's key)
     * @throws SQLException If an error occurs while adding or retrieving the governance artifact
     */
    private String checkAndAddArtifact(String artifactRefId, ArtifactType artifactType, String organization,
                                       Connection connection) throws SQLException {

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

        String artifactKey = GovernanceUtil.generateUUID();
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
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param organization  Organization
     * @throws GovernanceException If an error occurs while deleting the evaluation request
     */
    @Override
    public void deleteComplianceEvalReqsForArtifact(String artifactRefId, ArtifactType
            artifactType, String organization) throws GovernanceException {

        try (Connection connection = GovernanceDBUtil.getConnection()) {
            connection.setAutoCommit(false);

            String sqlQuery = SQLConstants.DELETE_REQ_POLICY_MAPPING_FOR_ARTIFACT;
            try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
                prepStmnt.setString(1, artifactRefId);
                prepStmnt.setString(2, String.valueOf(artifactType));
                prepStmnt.setString(3, organization);
                prepStmnt.executeUpdate();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

            sqlQuery = SQLConstants.DELETE_GOV_REQ_FOR_ARTIFACT;
            try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
                prepStmnt.setString(1, artifactRefId);
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
                request.setArtifactRefId(resultSet.getString("ARTIFACT_REF_ID"));
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
     * Add an artifact compliance evaluation request event
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param organization  Organization
     * @return Request ID
     * @throws GovernanceException If an error occurs while adding the artifact compliance evaluation
     *                             request
     */
    @Override
    public String getPendingEvalRequest(String artifactRefId, ArtifactType artifactType,
                                        String organization) throws GovernanceException {
        try (Connection connection = GovernanceDBUtil.getConnection()) {
            return getPendingEvalRequest(artifactRefId, artifactType, organization, connection);
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes
                    .ERROR_WHILE_GETTING_GOV_EVAL_REQUEST_FOR_ARTIFACT, e, artifactRefId);
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
     * @param artifactRefId        Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType         Artifact Type
     * @param policyId             Policy ID
     * @param rulesetViolationsMap Map of Rulesets to Rule Violations
     * @param organization         Organization
     * @throws GovernanceException If an error occurs while adding the compliance evaluation results
     */
    @Override
    public void addComplianceEvalResults(String artifactRefId, ArtifactType artifactType, String policyId,
                                         Map<String, List<RuleViolation>> rulesetViolationsMap, String organization)
            throws GovernanceException {

        List<String> rulesetIds = new ArrayList<>(rulesetViolationsMap.keySet());
        try (Connection connection = GovernanceDBUtil.getConnection()) {
            connection.setAutoCommit(false);

            try {
                clearOldRuleViolations(artifactRefId, artifactType, rulesetIds, organization, connection);
                clearOldRulesetRuns(artifactRefId, artifactType, rulesetIds, organization, connection);
                clearOldPolicyRun(artifactRefId, artifactType, policyId, organization, connection);

                String artifactKey = getArtifactKey(artifactRefId, artifactType, organization, connection);
                if (artifactKey == null) {
                    throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_RETRIEVING_ARTIFACT_INFO,
                            artifactRefId);
                }

                addPolicyRun(artifactKey, policyId, connection);

                for (Map.Entry<String, List<RuleViolation>> entry : rulesetViolationsMap.entrySet()) {
                    String rulesetId = entry.getKey();
                    List<RuleViolation> ruleViolations = entry.getValue();
                    String rulesetResultId = addRulesetRuns(artifactKey, rulesetId, ruleViolations.isEmpty(),
                            connection);
                    addRuleViolations(rulesetResultId, ruleViolations, connection);
                }

                connection.commit();
            } catch (SQLException | GovernanceException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_SAVING_GOVERNANCE_RESULT,
                    e, artifactRefId);
        }
    }

    /**
     * Clear old policy run results for the artifact
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param policyId      Policy ID
     * @param organization  Organization
     * @param connection    Connection
     * @throws SQLException If an error occurs while clearing the old policy result
     */
    private void clearOldPolicyRun(String artifactRefId, ArtifactType artifactType, String policyId,
                                   String organization, Connection connection)
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
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param rulesetIds    List of Ruleset IDs
     * @param organization  Organization
     * @param connection    Connection
     * @throws SQLException If an error occurs while clearing the old ruleset results
     */
    private void clearOldRulesetRuns(String artifactRefId, ArtifactType artifactType, List<String> rulesetIds,
                                     String organization, Connection connection)
            throws SQLException {

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
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param rulesetIds    List of Ruleset IDs
     * @param organization  Organization
     * @param connection    Connection
     * @throws SQLException If an error occurs while clearing the rule violations
     */
    private void clearOldRuleViolations(String artifactRefId, ArtifactType artifactType, List<String> rulesetIds,
                                        String organization, Connection connection)
            throws SQLException {

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
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param organization  Organization
     * @param connection    Connection
     * @return Artifact Key
     * @throws SQLException If an error occurs while getting the artifact key
     */
    private String getArtifactKey(String artifactRefId, ArtifactType artifactType,
                                  String organization,
                                  Connection connection) throws SQLException {

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
     * @param artifactKey Artifact Key
     * @param policyId    Policy ID
     * @param connection  Connection
     * @throws SQLException If an error occurs while adding the policy compliance evaluation result
     */
    private void addPolicyRun(String artifactKey, String policyId, Connection connection) throws SQLException {

        String sqlQuery = SQLConstants.ADD_POLICY_RUN;
        try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, artifactKey);
            prepStmnt.setString(2, policyId);
            prepStmnt.executeUpdate();
        }
    }

    /**
     * Add a ruleset compliance evaluation result
     *
     * @param artifactKey          Artifact Key
     * @param rulesetId            Ruleset ID
     * @param isRulesetEvalSuccess Evaluation result
     * @param connection           Connection
     * @return Ruleset Result ID
     * @throws SQLException If an error occurs while adding the ruleset compliance evaluation result
     */
    private String addRulesetRuns(String artifactKey, String rulesetId,
                                  boolean isRulesetEvalSuccess, Connection connection) throws SQLException {

        String sqlQuery = SQLConstants.ADD_RULESET_RUN;
        try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            String rulesetResultId = GovernanceUtil.generateUUID();
            prepStmnt.setString(1, rulesetResultId);
            prepStmnt.setString(2, artifactKey);
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
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param rulesetId     Ruleset ID
     * @param organization  Organization
     * @return List of rule violations
     * @throws GovernanceException If an error occurs while getting the rule violations
     */
    @Override
    public List<RuleViolation> getRuleViolations(String artifactRefId, ArtifactType artifactType,
                                                 String rulesetId, String organization) throws GovernanceException {

        String sqlQuery = SQLConstants.GET_RULE_VIOLATIONS;
        List<RuleViolation> ruleViolations = new ArrayList<>();
        try (Connection connection = GovernanceDBUtil.getConnection();
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
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param organization  Organization
     * @return List of Rule Violations
     * @throws GovernanceException If an error occurs while getting the rule violations
     */
    @Override
    public List<RuleViolation> getRuleViolationsForArtifact(String artifactRefId, ArtifactType artifactType,
                                                            String organization) throws GovernanceException {

        String sqlQuery = SQLConstants.GET_RULE_VIOLATIONS_FOR_ARTIFACT;
        List<RuleViolation> ruleViolations = new ArrayList<>();
        try (Connection connection = GovernanceDBUtil.getConnection();
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
     * Get evaluated policies for an artifact
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param organization  Organization
     * @return List of Policy Ids
     * @throws GovernanceException If an error occurs while getting the compliance evaluation results
     */
    @Override
    public List<String> getEvaluatedPoliciesForArtifact(String artifactRefId, ArtifactType artifactType,
                                                        String organization) throws GovernanceException {

        String sqlQuery = SQLConstants.GET_POLICY_RUNS_FOR_ARTIFACT;
        List<String> policyIds = new ArrayList<>();
        try (Connection connection = GovernanceDBUtil.getConnection();
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
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_GETTING_GOVERNANCE_RESULTS, e);
        }
    }

    /**
     * Get ruleset runs for an artifact
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param organization  Organization
     * @return List of evaluated rulesets
     * @throws GovernanceException If an error occurs while getting the compliance evaluation results
     */
    @Override
    public List<String> getEvaluatedRulesetsForArtifact(String artifactRefId, ArtifactType artifactType,
                                                        String organization)
            throws GovernanceException {
        String sqlQuery = SQLConstants.GET_RULESET_RUNS_FOR_ARTIFACT;
        List<String> rulesetIds = new ArrayList<>();
        try (Connection connection = GovernanceDBUtil.getConnection();
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
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_GETTING_GOVERNANCE_RESULTS, e);
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
     * @throws GovernanceException If an error occurs while getting the compliance evaluation results
     */
    @Override
    public boolean isRulesetEvaluatedForArtifact(String artifactRefId, ArtifactType artifactType,
                                                 String rulesetId, String organization)
            throws GovernanceException {
        String sqlQuery = SQLConstants.GET_RULESET_RUN_FOR_ARTIFACT_AND_RULESET;
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, artifactRefId);
            prepStmnt.setString(2, String.valueOf(artifactType));
            prepStmnt.setString(3, organization);
            prepStmnt.setString(4, rulesetId);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_GETTING_GOVERNANCE_RESULTS, e);
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
    public List<String> getAllComplianceEvaluatedArtifacts(ArtifactType artifactType,
                                                           String organization) throws GovernanceException {
        String sqlQuery = SQLConstants.GET_ALL_EVALUTED_ARTIFACTS;
        Set<String> artifactRefIds = new HashSet<>();
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, String.valueOf(artifactType));
            prepStmnt.setString(2, organization);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                while (resultSet.next()) {
                    artifactRefIds.add(resultSet.getString("ARTIFACT_REF_ID"));
                }
            }
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_GETTING_GOVERNANCE_RESULTS, e);
        }
        return new ArrayList<>(artifactRefIds);
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
        Set<String> artifactRefIds = new HashSet<>();
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, String.valueOf(artifactType));
            prepStmnt.setString(2, organization);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                while (resultSet.next()) {
                    artifactRefIds.add(resultSet.getString("ARTIFACT_REF_ID"));
                }
            }
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_GETTING_GOVERNANCE_RESULTS, e);
        }
        return new ArrayList<>(artifactRefIds);
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

        String sqlQuery = SQLConstants.GET_POLICY_RUNS;
        Set<String> policyIds = new HashSet<>();
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, organization);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                while (resultSet.next()) {
                    policyIds.add(resultSet.getString("POLICY_ID"));
                }
            }
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_GETTING_GOVERNANCE_RESULTS, e);
        }
        return new ArrayList<>(policyIds);
    }

    /**
     * Get list of all violated rulesets
     *
     * @param organization Organization
     * @return List of all violated rulesets
     * @throws GovernanceException If an error occurs while getting the list of all violated rulesets
     */
    @Override
    public List<String> getViolatedRulesets(String organization) throws GovernanceException {

        String sqlQuery = SQLConstants.GET_FAILED_RULESET_RUNS;
        List<String> rulesetIds = new ArrayList<>();
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, organization);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                while (resultSet.next()) {
                    rulesetIds.add(resultSet.getString("RULESET_ID"));
                }
            }
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_GETTING_GOVERNANCE_RESULTS, e);
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
     * @throws GovernanceException If an error occurs while getting the list of all
     *                             violated rulesets for an artifact
     */
    @Override
    public List<String> getViolatedRulesetsForArtifact(String artifactRefId, ArtifactType artifactType,
                                                       String organization) throws GovernanceException {
        String sqlQuery = SQLConstants.GET_FAILED_RULESET_RUNS_FOR_ARTIFACT;
        List<String> rulesetIds = new ArrayList<>();
        try (Connection connection = GovernanceDBUtil.getConnection();
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
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_GETTING_GOVERNANCE_RESULTS, e);
        }
        return rulesetIds;
    }

    /**
     * Get list of all evaluated artifacts for a policy
     *
     * @param policyId Policy ID
     * @return List of all evaluated artifacts for a policy
     * @throws GovernanceException If an error occurs while getting the list of all
     *                             evaluated artifacts for a policy
     */
    @Override
    public List<ArtifactInfo> getEvaluatedArtifactsForPolicy(String policyId)
            throws GovernanceException {
        String sqlQuery = SQLConstants.GET_ARITFCATS_FOR_POLICY_RUN;
        List<ArtifactInfo> artifactInfos = new ArrayList<>();
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, policyId);
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
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_GETTING_GOVERNANCE_RESULTS, e);
        }
    }

    /**
     * Delete all governance data related to the artifact
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param organization  Organization
     * @throws GovernanceException If an error occurs while deleting the governance data
     */
    @Override
    public void deleteArtifact(String artifactRefId, ArtifactType artifactType, String organization)
            throws GovernanceException {
        try (Connection connection = GovernanceDBUtil.getConnection()) {
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
            throw new GovernanceException(GovernanceExceptionCodes
                    .ERROR_WHILE_DELETING_GOVERNANCE_DATA, e, artifactRefId);
        }
    }
}
