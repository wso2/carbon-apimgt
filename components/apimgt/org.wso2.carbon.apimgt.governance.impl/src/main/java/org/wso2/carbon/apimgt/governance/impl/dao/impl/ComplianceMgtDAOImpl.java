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
import org.wso2.carbon.apimgt.governance.impl.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.impl.error.GovernanceExceptionCodes;
import org.wso2.carbon.apimgt.governance.impl.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.impl.model.EvaluationRequest;
import org.wso2.carbon.apimgt.governance.impl.model.EvaluationStatus;
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
     * @param organization Organization
     * @throws GovernanceException If an error occurs while adding the artifact compliance evaluation request event
     */
    @Override
    public void addComplianceEvaluationRequest(String artifactId, String artifactType,
                                               String policyId,
                                               String organization) throws GovernanceException {
        String SQLQuery = SQLConstants.ADD_GOV_EVALUATION_REQUEST;
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(SQLQuery)) {
            prepStmnt.setString(1, GovernanceUtil.generateUUID());
            prepStmnt.setString(2, artifactId);
            prepStmnt.setString(3, artifactType);
            prepStmnt.setString(4, policyId);
            prepStmnt.setString(6, organization);
            prepStmnt.executeUpdate();
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_PROCESSING_GOVERNANCE_EVALUATION_REQUEST,
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
    public List<EvaluationRequest> getPendingEvaluationRequests() throws GovernanceException {
        String SQLQuery = SQLConstants.GET_PENDING_EVALUATION_REQUESTS;
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection.prepareStatement(SQLQuery)) {
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                List<EvaluationRequest> evaluationRequests = new ArrayList<>();
                while (resultSet.next()) {
                    EvaluationRequest evaluationRequest = new EvaluationRequest();
                    evaluationRequest.setId(resultSet.getString("REQUEST_ID"));
                    evaluationRequest.setArtifactId(resultSet.getString("ARTIFACT_ID"));
                    evaluationRequest.setArtifactType(ArtifactType.fromString(resultSet.getString("ARTIFACT_TYPE")));
                    evaluationRequest.setPolicyId(resultSet.getString("POLICY_ID"));
                    evaluationRequest.setOrganization(resultSet.getString("ORGANIZATION"));
                    evaluationRequest.setEvaluationStatus(EvaluationStatus.valueOf(resultSet.
                            getString("STATUS")));
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
    public void updateEvaluationStatus(String requestId, EvaluationStatus status) throws GovernanceException {
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
    public void deleteEvaluationRequest(String requestId) throws GovernanceException {
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


}
