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
import org.wso2.carbon.apimgt.governance.api.model.ComplianceEvaluationStatus;
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

    /**
     * Bill Pugh Singleton Design to initialize the instance lazily and thread-safely
     */
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
     * @param policyId     Policy ID
     * @throws GovernanceException If an error occurs while adding the artifact
     *                             compliance evaluation request event
     */
    @Override
    public void addComplianceEvaluationRequest(String artifactId, ArtifactType artifactType,
                                               String policyId, String organization)
            throws GovernanceException {

        String sqlQuery = SQLConstants.ADD_GOV_EVALUATION_REQUEST;

        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, GovernanceUtil.generateUUID());
            prepStmnt.setString(2, artifactId);
            prepStmnt.setString(3, String.valueOf(artifactType));
            prepStmnt.setString(4, policyId);
            prepStmnt.setString(5, organization);
            prepStmnt.execute();
        } catch (SQLException e) {
            if (e instanceof SQLIntegrityConstraintViolationException) {
                // Ignore if the artifact compliance evaluation request already exists
                if (log.isDebugEnabled()) {
                    log.debug("Artifact compliance evaluation request already exists for artifact: " + artifactId);
                }
            } else {
                throw new GovernanceException(
                        GovernanceExceptionCodes.ERROR_WHILE_PROCESSING_GOVERNANCE_EVALUATION_REQUEST,
                        e, artifactId, organization
                );
            }
        }
    }

    /**
     * Get pending evaluation requests
     *
     * @return List of pending evaluation requests
     * @throws GovernanceException If an error occurs while getting the pending evaluation requests
     */
    @Override
    public List<ComplianceEvaluationRequest> getPendingComplianceEvaluationRequests() throws GovernanceException {

        String sqlQuery = SQLConstants.GET_PENDING_EVALUATION_REQUESTS;
        List<ComplianceEvaluationRequest> evaluationRequests = new ArrayList<>();
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                while (resultSet.next()) {
                    ComplianceEvaluationRequest evaluationRequest = new ComplianceEvaluationRequest();
                    evaluationRequest.setId(resultSet.getString("REQUEST_ID"));
                    evaluationRequest.setArtifactId(resultSet.getString("ARTIFACT_ID"));
                    evaluationRequest.setArtifactType(ArtifactType.fromString(resultSet.getString("ARTIFACT_TYPE")));
                    evaluationRequest.setPolicyId(resultSet.getString("POLICY_ID"));
                    evaluationRequest.setOrganization(resultSet.getString("ORGANIZATION"));
                    evaluationRequest.setEvaluationStatus(ComplianceEvaluationStatus.PENDING);
                    evaluationRequests.add(evaluationRequest);
                }
                return evaluationRequests;
            }
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes
                    .ERROR_WHILE_GETTING_GOVERNANCE_EVALUATION_REQUESTS, e);
        }

    }


    /**
     * Get the processing compliance evaluation request by artifact ID, artifact Type, policy ID and Org, if any
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact Type
     * @param policyId     Policy ID
     * @param organization Organization
     * @return ComplianceEvaluationRequest
     * @throws GovernanceException If an error occurs while getting the processing compliance evaluation request
     */
    @Override
    public ComplianceEvaluationRequest getProcessingComplianceEvaluationRequest(String artifactId,
                                                                                ArtifactType artifactType,
                                                                                String policyId,
                                                                                String organization)
            throws GovernanceException {

        String sqlQuery = SQLConstants.GET_PROCESSING_EVALUATION_REQUEST_BY_ARTIFACT_AND_POLICY;
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, artifactId);
            prepStmnt.setString(2, String.valueOf(artifactType));
            prepStmnt.setString(3, policyId);
            prepStmnt.setString(4, organization);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                if (resultSet.next()) {
                    ComplianceEvaluationRequest evaluationRequest = new ComplianceEvaluationRequest();
                    evaluationRequest.setId(resultSet.getString("REQUEST_ID"));
                    evaluationRequest.setArtifactId(artifactId);
                    evaluationRequest.setArtifactType(artifactType);
                    evaluationRequest.setPolicyId(policyId);
                    evaluationRequest.setEvaluationStatus(ComplianceEvaluationStatus.PROCESSING);
                    return evaluationRequest;
                }
            }
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes
                    .ERROR_WHILE_GETTING_GOVERNANCE_EVALUATION_REQUESTS, e);
        }
        return null;
    }

    /**
     * Update the evaluation status of a request
     *
     * @param requestId Evaluation request ID
     * @param status    Evaluation status
     * @throws GovernanceException If an error occurs while updating the evaluation status
     */
    @Override
    public void updateComplianceEvaluationStatus(String requestId, ComplianceEvaluationStatus status)
            throws GovernanceException {

        String sqlQuery = SQLConstants.UPDATE_GOV_EVALUATION_REQUEST_STATUS;
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, String.valueOf(status));
            prepStmnt.setString(2, requestId);
            prepStmnt.executeUpdate();
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes
                    .ERROR_WHILE_UPDATING_GOVERNANCE_EVALUATION_REQUEST,
                    e, requestId);
        }
    }

    /**
     * Update the evaluation status of all processing requests to pending
     *
     * @throws GovernanceException If an error occurs while updating the evaluation status
     */
    @Override
    public void updateProcessingRequestToPending() throws GovernanceException {

        String sqlQuery = SQLConstants.UPDATE_GOV_REQUEST_STATUS_FROM_PROCESSING_TO_PENDING;
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
    public void deleteComplianceEvaluationRequest(String requestId) throws GovernanceException {

        String sqlQuery = SQLConstants.DELETE_GOV_EVALUATION_REQUEST;
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, requestId);
            prepStmnt.executeUpdate();
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes
                    .ERROR_WHILE_DELETING_GOVERNANCE_EVALUATION_REQUEST,
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
    public void deleteComplianceEvaluationRequestsForArtifact(String artifactId, ArtifactType
            artifactType, String organization) throws GovernanceException {

        String sqlQuery = SQLConstants.DELETE_GOV_EVALUATION_REQUEST_FOR_ARTIFACT;
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, artifactId);
            prepStmnt.setString(2, String.valueOf(artifactType));
            prepStmnt.setString(3, organization);
            prepStmnt.executeUpdate();
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes
                    .ERROR_WHILE_DELETING_GOVERNANCE_EVALUATION_REQUESTS, e);
        }
    }

    /**
     * Add a compliance evaluation result to DB
     *
     * @param result         Evaluation result
     * @param ruleViolations List of rule violations from policy evaluation
     * @throws GovernanceException If an error occurs while adding the compliance evaluation result
     */
    @Override
    public void addComplianceEvaluationResult(ComplianceEvaluationResult result,
                                              List<RuleViolation> ruleViolations)
            throws GovernanceException {

        String sqlQuery = SQLConstants.ADD_GOV_COMPLIANCE_EVALUATION_RESULT;
        String artifactId = result.getArtifactId();
        ArtifactType artifactType = result.getArtifactType();
        String policyId = result.getPolicyId();
        String rulesetId = result.getRulesetId();
        String organization = result.getOrganization();
        try (Connection connection = GovernanceDBUtil.getConnection()) {
            connection.setAutoCommit(false);

            clearOldComplianceEvaluationResult(artifactId, artifactType, policyId, rulesetId, connection);

            try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
                prepStmnt.setString(1, GovernanceUtil.generateUUID());
                prepStmnt.setString(2, artifactId);
                prepStmnt.setString(3, String.valueOf(artifactType));
                prepStmnt.setString(4, policyId);
                prepStmnt.setString(5, rulesetId);
                prepStmnt.setInt(6, result.isEvaluationSuccess() ? 1 : 0);
                prepStmnt.setString(7, organization);
                prepStmnt.execute();
            }

            if (!ruleViolations.isEmpty()) {
                addRuleViolations(ruleViolations, connection);
            }
            connection.commit();
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_SAVING_GOVERNANCE_RESULT,
                    e, artifactId);
        }
    }

    /**
     * Clear compliance evaluation result for the artifact, policy combination
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact Type
     * @param policyId     Policy ID
     * @param rulesetId    Ruleset ID
     * @param connection   Connection
     * @throws GovernanceException If an error occurs while clearing the compliance evaluation result
     */
    private void clearOldComplianceEvaluationResult(String artifactId, ArtifactType artifactType, String policyId,
                                                    String rulesetId, Connection connection)
            throws GovernanceException {

        String sqlQuery = SQLConstants.DELETE_GOV_COMPLIANCE_EVALUATION_RESULT;
        try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            prepStmnt.setString(1, artifactId);
            prepStmnt.setString(2, String.valueOf(artifactType));
            prepStmnt.setString(3, policyId);
            prepStmnt.setString(4, rulesetId);
            prepStmnt.executeUpdate();
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_DELETING_GOVERNANCE_RESULT,
                    e, artifactId);
        }
    }

    /**
     * Add rule violations
     *
     * @param ruleViolations List of rule violations
     * @param connection     Connection
     * @throws GovernanceException If an error occurs while adding the rule violations
     */
    private void addRuleViolations(List<RuleViolation> ruleViolations, Connection connection)
            throws GovernanceException {

        String sqlQuery = SQLConstants.ADD_RULE_VIOLATION;
        try (PreparedStatement prepStmnt = connection.prepareStatement(sqlQuery)) {
            for (RuleViolation ruleViolation : ruleViolations) {
                prepStmnt.setString(1, GovernanceUtil.generateUUID());
                prepStmnt.setString(2, ruleViolation.getArtifactId());
                prepStmnt.setString(3, String.valueOf(ruleViolation.getArtifactType()));
                prepStmnt.setString(4, ruleViolation.getPolicyId());
                prepStmnt.setString(5, ruleViolation.getRulesetId());
                prepStmnt.setString(6, ruleViolation.getRuleCode());
                prepStmnt.setString(7, ruleViolation.getViolatedPath());
                prepStmnt.setString(8, ruleViolation.getOrganization());
                prepStmnt.addBatch();
            }

            prepStmnt.executeBatch();
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_SAVING_RULE_VIOLATIONS,
                    e);
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
                    ruleViolation.setRuleCode(resultSet.getString("RULE_CODE"));
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
                    ruleViolation.setRuleCode(resultSet.getString("RULE_CODE"));
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
                    ruleViolation.setRuleCode(resultSet.getString("RULE_CODE"));
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
                deleteArtifactEvaluationResults(artifactId, artifactType, organization, connection);
                deleteRuleViolationsForArtifact(artifactId, artifactType, organization, connection);

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
