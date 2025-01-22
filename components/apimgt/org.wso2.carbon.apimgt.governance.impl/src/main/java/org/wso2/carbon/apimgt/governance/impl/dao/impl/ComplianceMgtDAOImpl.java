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
import org.wso2.carbon.apimgt.governance.impl.dao.ComplianceMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.governance.impl.util.GovernanceDBUtil;
import org.wso2.carbon.apimgt.governance.impl.util.GovernanceUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the DAO class related to assessing compliance of APIs
 */
public class ComplianceMgtDAOImpl implements ComplianceMgtDAO {

    private static final Log log = LogFactory.getLog(ComplianceMgtDAOImpl.class);
    private static ComplianceMgtDAO INSTANCE = null;

    private ComplianceMgtDAOImpl() {
    }


    /**
     * Get an instance of the ComplianceMgtDAOImpl class
     *
     * @return Instance of the ComplianceMgtDAOImpl class
     */
    public static ComplianceMgtDAO getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ComplianceMgtDAOImpl();
        }
        return INSTANCE;
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
                                               String policyId, String organization) throws GovernanceException {
        String SQLQuery = SQLConstants.ADD_GOV_EVALUATION_REQUEST;
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(SQLQuery)) {
            addGovernanceArtifactInfo(artifactId, artifactType, organization, connection);
            prepStmnt.setString(1, GovernanceUtil.generateUUID());
            prepStmnt.setString(2, artifactId);
            prepStmnt.setString(3, policyId);
            prepStmnt.executeUpdate();
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_PROCESSING_GOVERNANCE_EVALUATION_REQUEST,
                    e, artifactId, organization);
        }
    }

    /**
     * Add an artifact governance event
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact Type
     * @param organization Organization
     * @param connection   Connection
     * @throws GovernanceException If an error occurs while adding the artifact governance event
     */
    private void addGovernanceArtifactInfo(String artifactId, ArtifactType artifactType, String organization,
                                           Connection connection) throws GovernanceException {
        String SQLQuery = SQLConstants.ADD_GOV_ARTIFACT_INFO;
        try (PreparedStatement prepStmnt = connection.prepareStatement(SQLQuery)) {
            prepStmnt.setString(1, artifactId);
            prepStmnt.setString(2, String.valueOf(artifactType));
            prepStmnt.setString(3, organization);
            prepStmnt.executeUpdate();
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_SAVING_ARTIFACT_INFO,
                    e, artifactId, organization);
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
        String SQLQuery = SQLConstants.GET_PENDING_EVALUATION_REQUESTS;
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(SQLQuery)) {
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                List<ComplianceEvaluationRequest> evaluationRequests = new ArrayList<>();
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
     * Update the evaluation status of a request
     *
     * @param requestId Evaluation request ID
     * @param status    Evaluation status
     * @throws GovernanceException If an error occurs while updating the evaluation status
     */
    @Override
    public void updateComplianceEvaluationStatus(String requestId, ComplianceEvaluationStatus status) throws GovernanceException {
        String SQLQuery = SQLConstants.UPDATE_GOV_EVALUATION_REQUEST_STATUS;
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(SQLQuery)) {
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
     * Delete an evaluation request
     *
     * @param requestId Evaluation request ID
     * @throws GovernanceException If an error occurs while deleting the evaluation request
     */
    @Override
    public void deleteComplianceEvaluationRequest(String requestId) throws GovernanceException {
        String SQLQuery = SQLConstants.DELETE_GOV_EVALUATION_REQUEST;
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(SQLQuery)) {
            prepStmnt.setString(1, requestId);
            prepStmnt.executeUpdate();
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes
                    .ERROR_WHILE_DELETING_GOVERNANCE_EVALUATION_REQUEST,
                    e, requestId);
        }
    }

    /**
     * Add a evaluation result
     *
     * @param result Evaluation result
     * @throws GovernanceException If an error occurs while adding the compliance evaluation result
     */
    @Override
    public void addComplianceEvaluationResult(ComplianceEvaluationResult result) throws GovernanceException {
        String SQLQuery = SQLConstants.ADD_GOV_COMPLIANCE_EVALUATION_RESULT;
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(SQLQuery)) {
            prepStmnt.setString(1, GovernanceUtil.generateUUID());
            prepStmnt.setString(2, result.getArtifactId());
            prepStmnt.setString(3, result.getArtifactId());
            prepStmnt.setInt(4, result.isEvaluationSuccess() ? 1 : 0);
            prepStmnt.executeUpdate();
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_SAVING_GOVERNANCE_RESULT,
                    e, result.getArtifactId());
        }
    }

    /**
     * Add a rule violation
     *
     * @param ruleViolation Rule violation
     * @throws GovernanceException If an error occurs while adding the rule violation
     */
    @Override
    public void addRuleViolation(RuleViolation ruleViolation) throws GovernanceException {
        String SQLQuery = SQLConstants.ADD_RULE_VIOLATION;
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(SQLQuery)) {
            prepStmnt.setString(1, GovernanceUtil.generateUUID());
            prepStmnt.setString(2, ruleViolation.getArtifactId());
            prepStmnt.setString(3, ruleViolation.getPolicyId());
            prepStmnt.setString(4, ruleViolation.getRulesetId());
            prepStmnt.setString(5, ruleViolation.getRuleCode());
            prepStmnt.setString(6, ruleViolation.getViolatedPath());
            prepStmnt.executeUpdate();
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_SAVING_RULE_VIOLATION,
                    e, ruleViolation.getArtifactId());
        }
    }


}
