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
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernableState;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernanceAction;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernanceActionType;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernancePolicyAttachment;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernancePolicyAttachmentList;
import org.wso2.carbon.apimgt.governance.api.model.ExtendedArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.PolicyCategory;
import org.wso2.carbon.apimgt.governance.api.model.RuleSeverity;
import org.wso2.carbon.apimgt.governance.api.model.PolicyType;
import org.wso2.carbon.apimgt.governance.api.model.Policy;
import org.wso2.carbon.apimgt.governance.api.model.PolicyContent;
import org.wso2.carbon.apimgt.governance.api.model.PolicyInfo;
import org.wso2.carbon.apimgt.governance.impl.APIMGovernanceConstants;
import org.wso2.carbon.apimgt.governance.impl.dao.GovernancePolicyAttachmentMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceDBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the GovernancePolicyMgtDAO interface.
 */
public class GovernancePolicyMgtDAOImpl implements GovernancePolicyAttachmentMgtDAO {

    private static final Log log = LogFactory.getLog(GovernancePolicyMgtDAOImpl.class);

    private GovernancePolicyMgtDAOImpl() {
    }

    private static class SingletonHelper {
        private static final GovernancePolicyAttachmentMgtDAO INSTANCE = new GovernancePolicyMgtDAOImpl();
    }

    /**
     * Get instance of GovernancePolicyMgtDAOImpl
     *
     * @return GovernancePolicyMgtDAOImpl instance
     */
    public static GovernancePolicyAttachmentMgtDAO getInstance() {
        return SingletonHelper.INSTANCE;
    }

    /**
     * Create a new Governance Policy
     *
     * @param governancePolicyAttachment Governance Policy Info with Ruleset Ids
     * @param organization     Organization
     * @return APIMGovernancePolicy Created object
     */
    @Override
    public APIMGovernancePolicyAttachment createGovernancePolicyAttachment(APIMGovernancePolicyAttachment
                                                                                       governancePolicyAttachment,
                                                                           String organization)
            throws APIMGovernanceException {
        try (Connection connection = APIMGovernanceDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.CREATE_POLICY_ATTACHMENT)) {
                    prepStmt.setString(1, governancePolicyAttachment.getId());
                    prepStmt.setString(2, governancePolicyAttachment.getName());
                    prepStmt.setString(3, governancePolicyAttachment.getDescription());
                    prepStmt.setString(4, organization);
                    prepStmt.setString(5, governancePolicyAttachment.getCreatedBy());
                    prepStmt.setInt(6, governancePolicyAttachment.isGlobal() ? 1 : 0);
                    prepStmt.execute();
                }

                insertPolicyRulesetMapping(connection, governancePolicyAttachment);
                insertPolicyLabels(connection, governancePolicyAttachment);
                insertPolicyStatesAndActions(connection, governancePolicyAttachment);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_CREATING_POLICY, e, organization);
        }
        return getGovernancePolicyAttachmentByID(governancePolicyAttachment.getId(), organization);
    }

    /**
     * Insert policy attachment-policy mappings into the database.
     *
     * @param connection       Connection
     * @param governancePolicyAttachment Governance Policy
     * @throws SQLException If an error occurs while inserting the mappings (Captured at higher level)
     */
    private void insertPolicyRulesetMapping(Connection connection,
                                            APIMGovernancePolicyAttachment governancePolicyAttachment)
            throws SQLException {
        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                .CREATE_POLICY_ATTACHMENT_POLICY_MAPPING)) {
            for (String policyId : governancePolicyAttachment.getPolicyIds()) {
                prepStmt.setString(1, governancePolicyAttachment.getId());
                prepStmt.setString(2, policyId);
                prepStmt.addBatch();
            }
            prepStmt.executeBatch();
        }
    }

    /**
     * Insert policy-label mappings into the database.
     *
     * @param connection       Connection
     * @param governancePolicyAttachment Governance Policy
     * @throws SQLException If an error occurs while inserting the mappings (Captured at higher level)
     */
    private void insertPolicyLabels(Connection connection, APIMGovernancePolicyAttachment governancePolicyAttachment)
            throws SQLException {
        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                .CREATE_GOVERNANCE_POLICY_ATTACHMENT_LABEL_MAPPING)) {
            for (String label : governancePolicyAttachment.getLabels()) {
                prepStmt.setString(1, governancePolicyAttachment.getId());
                prepStmt.setString(2, label);
                prepStmt.addBatch();
            }
            prepStmt.executeBatch();
        }
    }

    /**
     * Insert policy-states and action mappings into the database.
     *
     * @param connection       Connection
     * @param governancePolicyAttachment Governance Policy
     * @throws SQLException If an error occurs while inserting the mappings
     */
    private void insertPolicyStatesAndActions(Connection connection,
                                              APIMGovernancePolicyAttachment governancePolicyAttachment)
            throws SQLException {
        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                .CREATE_GOVERNANCE_POLICY_ATTACHMENT_STATE_MAPPING)) {
            for (APIMGovernableState state : governancePolicyAttachment.getGovernableStates()) {
                prepStmt.setString(1, governancePolicyAttachment.getId());
                prepStmt.setString(2, String.valueOf(state));
                prepStmt.addBatch();
            }
            prepStmt.executeBatch();
        }

        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                .CREATE_GOVERNANCE_POLICY_ATTACHMENT_ACTION_MAPPING)) {
            for (APIMGovernanceAction action : governancePolicyAttachment.getActions()) {
                prepStmt.setString(1, governancePolicyAttachment.getId());
                prepStmt.setString(2, String.valueOf(action.getGovernableState()));
                prepStmt.setString(3, String.valueOf(action.getRuleSeverity()));
                prepStmt.setString(4, String.valueOf(action.getType()));
                prepStmt.addBatch();
            }
            prepStmt.executeBatch();
        }
    }

    /**
     * Update a Governance Policy
     *
     * @param policyId         Policy ID
     * @param governancePolicyAttachment Governance Policy
     * @param organization     Organization
     * @return APIMGovernancePolicy Updated object
     * @throws APIMGovernanceException If an error occurs while updating the policy
     */
    @Override
    public APIMGovernancePolicyAttachment updateGovernancePolicyAttachment(String policyId,
                                                                           APIMGovernancePolicyAttachment
                                                                                   governancePolicyAttachment,
                                                                           String organization)
            throws APIMGovernanceException {
        try (Connection connection = APIMGovernanceDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                // Update policy details
                try (PreparedStatement updateStatement = connection.prepareStatement(SQLConstants.UPDATE_POLICY_ATTACHMENT)) {
                    updateStatement.setString(1, governancePolicyAttachment.getName());
                    updateStatement.setString(2, governancePolicyAttachment.getDescription());
                    updateStatement.setString(3, governancePolicyAttachment.getUpdatedBy());
                    updateStatement.setInt(4, governancePolicyAttachment.isGlobal() ? 1 : 0);
                    updateStatement.setString(5, policyId);
                    updateStatement.setString(6, organization);
                    updateStatement.executeUpdate();
                }
                updatePolicyAttachmentPolicyMappings(connection, policyId, governancePolicyAttachment);
                updatePolicyAttachmentLabels(connection, policyId, governancePolicyAttachment);
                updateStatesAndPolicyAttachmentActions(connection, policyId, governancePolicyAttachment);
                deletePolicyAttachmentResultsForPolicyAttachment(connection, policyId);

                // return updated APIMGovernancePolicy object
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_UPDATING_POLICY, e,
                    policyId);
        }
        return getGovernancePolicyAttachmentByID(policyId, organization);
    }

    /**
     * Delete a Governance Policy
     *
     * @param connection       DB Connection
     * @param policyAttachmentId         Policy ID
     * @param governancePolicyAttachment Governance Policy
     * @throws SQLException If an error occurs while deleting the policy (Captured at higher level)
     */
    private void updatePolicyAttachmentPolicyMappings(Connection connection, String policyAttachmentId,
                                                      APIMGovernancePolicyAttachment governancePolicyAttachment)
            throws SQLException {

        // Delete old mappings and add new mappings
        try (PreparedStatement ps =
                     connection.prepareStatement(SQLConstants.DELETE_POLICY_ATTACHMENT_POLICY_MAPPING_BY_POLICY_ATTACHMENT_ID)) {
            ps.setString(1, policyAttachmentId);
            ps.executeUpdate();
        }

        List<String> policiesToAdd = governancePolicyAttachment.getPolicyIds();
        if (policiesToAdd != null && !policiesToAdd.isEmpty()) {
            try (PreparedStatement ps =
                         connection.prepareStatement(SQLConstants.CREATE_POLICY_ATTACHMENT_POLICY_MAPPING)) {
                for (String policyId : policiesToAdd) {
                    ps.setString(1, policyId);
                    ps.setString(2, policyId);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }
    }

    /**
     * Update labels for a Policy Attachment
     *
     * @param connection       DB Connection
     * @param policyAttachmentId         Policy ID
     * @param governancePolicyAttachment Governance Policy
     * @throws SQLException If an error occurs while updating the labels (Captured at higher level)
     */
    private void updatePolicyAttachmentLabels(Connection connection, String policyAttachmentId,
                                              APIMGovernancePolicyAttachment governancePolicyAttachment)
            throws SQLException {
        // Delete old mappings and add new mappings
        try (PreparedStatement ps =
                     connection.prepareStatement(SQLConstants
                             .DELETE_GOVERNANCE_POLICY_ATTACHMENT_LABEL_MAPPING_BY_POLICY_ID)) {
            ps.setString(1, policyAttachmentId);
            ps.executeUpdate();
        }

        List<String> labelsToAdd = governancePolicyAttachment.getLabels();
        if (labelsToAdd != null && !labelsToAdd.isEmpty()) {
            try (PreparedStatement ps =
                         connection.prepareStatement(SQLConstants.CREATE_GOVERNANCE_POLICY_ATTACHMENT_LABEL_MAPPING)) {
                for (String label : labelsToAdd) {
                    ps.setString(1, policyAttachmentId);
                    ps.setString(2, label);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }
    }

    /**
     * Update states, actions for a Policy
     *
     * @param connection       DB Connection
     * @param policyAttachmentId         Policy ID
     * @param governancePolicyAttachment Governance Policy
     * @throws SQLException If an error occurs while updating the actions (Captured at higher level)
     */
    private void updateStatesAndPolicyAttachmentActions(Connection connection, String policyAttachmentId,
                                                        APIMGovernancePolicyAttachment governancePolicyAttachment)
            throws SQLException {

        // Delete old mappings and add new mappings
        try (PreparedStatement ps =
                     connection.prepareStatement(SQLConstants
                             .DELETE_GOVERNANCE_POLICY_ATTACHMENT_ACTION_MAPPING_BY_POLICY_ATTACHMENT_ID)) {
            ps.setString(1, policyAttachmentId);
            ps.executeUpdate();
        }

        try (PreparedStatement ps =
                     connection.prepareStatement(SQLConstants
                             .DELETE_GOVERNANCE_POLICY_ATTACHMENT_STATE_MAPPING_BY_POLICY_ID)) {
            ps.setString(1, policyAttachmentId);
            ps.executeUpdate();
        }

        List<APIMGovernableState> statesToAdd = governancePolicyAttachment.getGovernableStates();
        if (statesToAdd != null && !statesToAdd.isEmpty()) {
            try (PreparedStatement ps =
                         connection.prepareStatement(SQLConstants.CREATE_GOVERNANCE_POLICY_ATTACHMENT_STATE_MAPPING)) {
                for (APIMGovernableState state : statesToAdd) {
                    ps.setString(1, policyAttachmentId);
                    ps.setString(2, String.valueOf(state));
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }

        List<APIMGovernanceAction> actionsToAdd = governancePolicyAttachment.getActions();
        if (actionsToAdd != null && !actionsToAdd.isEmpty()) {
            try (PreparedStatement ps =
                         connection.prepareStatement(SQLConstants.CREATE_GOVERNANCE_POLICY_ATTACHMENT_ACTION_MAPPING)) {
                for (APIMGovernanceAction action : actionsToAdd) {
                    ps.setString(1, policyAttachmentId);
                    ps.setString(2, String.valueOf(action.getGovernableState()));
                    ps.setString(3, String.valueOf(action.getRuleSeverity()));
                    ps.setString(4, String.valueOf(action.getType()));
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }

    }

    /**
     * Get Governance Policy by Name
     *
     * @param policyAttachmentName   Policy Name
     * @param organization Organization
     * @return APIMGovernancePolicy
     * @throws APIMGovernanceException If an error occurs while retrieving the policy
     */
    @Override
    public APIMGovernancePolicyAttachment getGovernancePolicyAttachmentByName(String policyAttachmentName,
                                                                              String organization)
            throws APIMGovernanceException {

        APIMGovernancePolicyAttachment policyAttachment = null;
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.GET_POLICY_ATTACHMENT_BY_NAME)) {
            prepStmt.setString(1, organization);
            prepStmt.setString(2, policyAttachmentName);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                if (resultSet.next()) {
                    policyAttachment = new APIMGovernancePolicyAttachment();
                    policyAttachment.setId(resultSet.getString("POLICY_ID"));
                    policyAttachment.setName(resultSet.getString("NAME"));
                    policyAttachment.setDescription(resultSet.getString("DESCRIPTION"));
                    policyAttachment.setCreatedBy(resultSet.getString("CREATED_BY"));
                    policyAttachment.setCreatedTime(resultSet.getString("CREATED_TIME"));
                    policyAttachment.setUpdatedBy(resultSet.getString("UPDATED_BY"));
                    policyAttachment.setUpdatedTime(resultSet.getString("LAST_UPDATED_TIME"));
                    policyAttachment.setGlobal(resultSet.getInt("IS_GLOBAL") == 1);
                }
            }
            return policyAttachment;
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_RETRIEVING_POLICY_BY_NAME,
                    e, policyAttachmentName, organization);
        }
    }

    /**
     * Get Governance Policy by ID
     *
     * @param policyAttachmentId     Policy ID
     * @param organization Organization
     * @return APIMGovernancePolicy
     * @throws APIMGovernanceException If an error occurs while retrieving the policy
     */
    @Override
    public APIMGovernancePolicyAttachment getGovernancePolicyAttachmentByID(String policyAttachmentId,
                                                                            String organization)
            throws APIMGovernanceException {
        APIMGovernancePolicyAttachment policyAttachment = null;
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.GET_POLICY_ATTACHMENT_BY_ID)) {
            prepStmt.setString(1, policyAttachmentId);
            prepStmt.setString(2, organization);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                if (resultSet.next()) {
                    policyAttachment = new APIMGovernancePolicyAttachment();
                    policyAttachment.setId(resultSet.getString("POLICY_ID"));
                    policyAttachment.setName(resultSet.getString("NAME"));
                    policyAttachment.setDescription(resultSet.getString("DESCRIPTION"));
                    policyAttachment.setCreatedBy(resultSet.getString("CREATED_BY"));
                    policyAttachment.setCreatedTime(resultSet.getString("CREATED_TIME"));
                    policyAttachment.setUpdatedBy(resultSet.getString("UPDATED_BY"));
                    policyAttachment.setUpdatedTime(resultSet.getString("LAST_UPDATED_TIME"));
                    policyAttachment.setGlobal(resultSet.getInt("IS_GLOBAL") == 1);
                    policyAttachment.setPolicyIds(getPoliciesByPolicyAttachmentId(connection,
                            policyAttachment.getId()));
                    policyAttachment.setLabels(getLabelsByPolicyAttachmentId(connection, policyAttachment.getId()));
                    policyAttachment.setGovernableStates(getStatesByPolicyAttachmentId(connection,
                            policyAttachment.getId()));
                    policyAttachment.setActions(getActionsByPolicyAttachmentId(connection, policyAttachment.getId()));
                }
            }
            return policyAttachment;
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_RETRIEVING_POLICY_BY_ID,
                    e, policyAttachmentId);
        }
    }

    /**
     * Get all the Governance Policies
     *
     * @param organization Organization
     * @return APIMGovernancePolicyList object
     * @throws APIMGovernanceException If an error occurs while getting the policies
     */
    @Override
    public APIMGovernancePolicyAttachmentList getGovernancePolicyAttachments(String organization)
            throws APIMGovernanceException {
        APIMGovernancePolicyAttachmentList policyAttachmentList = new APIMGovernancePolicyAttachmentList();
        List<APIMGovernancePolicyAttachment> policyAttachmentsList = new ArrayList<>();
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.GET_POLICY_ATTACHMENT)) {
            prepStmt.setString(1, organization);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    APIMGovernancePolicyAttachment policyAttachment = new APIMGovernancePolicyAttachment();
                    policyAttachment.setId(resultSet.getString("POLICY_ID"));
                    policyAttachment.setName(resultSet.getString("NAME"));
                    policyAttachment.setDescription(resultSet.getString("DESCRIPTION"));
                    policyAttachment.setCreatedBy(resultSet.getString("CREATED_BY"));
                    policyAttachment.setCreatedTime(resultSet.getString("CREATED_TIME"));
                    policyAttachment.setUpdatedBy(resultSet.getString("UPDATED_BY"));
                    policyAttachment.setUpdatedTime(resultSet.getString("LAST_UPDATED_TIME"));
                    policyAttachment.setGlobal(resultSet.getInt("IS_GLOBAL") == 1);
                    policyAttachment.setPolicyIds(getPoliciesByPolicyAttachmentId(connection,
                            policyAttachment.getId()));
                    policyAttachment.setLabels(getLabelsByPolicyAttachmentId(connection, policyAttachment.getId()));
                    policyAttachment.setActions(getActionsByPolicyAttachmentId(connection, policyAttachment.getId()));
                    policyAttachment.setGovernableStates(getStatesByPolicyAttachmentId(connection,
                            policyAttachment.getId()));
                    policyAttachmentsList.add(policyAttachment);
                }
            }
            policyAttachmentList.setCount(policyAttachmentsList.size());
            policyAttachmentList.setGovernancePolicyAttachmentList(policyAttachmentsList);
            return policyAttachmentList;
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_RETRIEVING_POLICIES,
                    e, organization);
        }
    }

    /**
     * Get all the Rulesets associated with Policies
     *
     * @param policyAttachmentId     Policy ID
     * @param organization Organization
     * @return List of Rulesets
     * @throws APIMGovernanceException If an error occurs while getting the policies
     */
    @Override
    public List<Policy> getPoliciesWithContentByPolicyAttachmentId(String policyAttachmentId, String organization)
            throws APIMGovernanceException {
        List<Policy> policyList = new ArrayList<>();
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                     .GET_POLICIES_WITH_CONTENT_BY_POLICY_ATTACHMENT_ID)) {
            prepStmt.setString(1, policyAttachmentId);
            prepStmt.setString(2, organization);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    Policy policy = new Policy();
                    policy.setId(rs.getString("RULESET_ID"));
                    policy.setName(rs.getString("NAME"));
                    policy.setPolicyCategory(PolicyCategory.fromString(
                            rs.getString("RULE_CATEGORY")));
                    policy.setPolicyType(PolicyType.fromString(rs.getString("POLICY_TYPE")));
                    policy.setArtifactType(ExtendedArtifactType.fromString(
                            rs.getString("ARTIFACT_TYPE")));

                    PolicyContent policyContent = new PolicyContent();
                    policyContent.setContent(rs.getBytes("CONTENT"));
                    policyContent.setFileName(rs.getString("FILE_NAME"));
                    policy.setPolicyContent(policyContent);

                    policyList.add(policy);
                }
            }
            return policyList;
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.
                    ERROR_WHILE_RETRIEVING_RULESETS_ASSOCIATED_WITH_POLICY, e, policyAttachmentId);
        }
    }

    /**
     * Get the list of policies for a given policy
     *
     * @param policyAttachmentId     Policy ID
     * @param organization Organization
     * @return List of policies
     * @throws APIMGovernanceException If an error occurs while getting the policies
     */
    @Override
    public List<PolicyInfo> getPoliciesByPolicyAttachmentId(String policyAttachmentId, String organization)
            throws APIMGovernanceException {
        List<PolicyInfo> policyList = new ArrayList<>();
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.GET_POLICIES_BY_POLICY_ATTACHMENT_ID)) {
            prepStmt.setString(1, policyAttachmentId);
            prepStmt.setString(2, organization);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    PolicyInfo policy = new PolicyInfo();
                    policy.setId(rs.getString("RULESET_ID"));
                    policy.setName(rs.getString("NAME"));
                    policy.setPolicyCategory(PolicyCategory.fromString(
                            rs.getString("RULE_CATEGORY")));
                    policy.setPolicyType(PolicyType.fromString(rs.getString("RULE_TYPE")));
                    policy.setArtifactType(ExtendedArtifactType.fromString(
                            rs.getString("ARTIFACT_TYPE")));
                    policyList.add(policy);
                }
            }
            return policyList;
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes
                    .ERROR_WHILE_RETRIEVING_RULESETS_ASSOCIATED_WITH_POLICY, e, policyAttachmentId);
        }
    }

    /**
     * Get the list of policies by label
     *
     * @param label        label
     * @param organization organization
     * @return Map of Policy IDs, Policy Names
     * @throws APIMGovernanceException If an error occurs while getting the policies
     */
    @Override
    public Map<String, String> getPolicyAttachmentsByLabel(String label, String organization)
            throws APIMGovernanceException {
        Map<String, String> policyIds = new HashMap();
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.GET_POLICIES_BY_LABEL)) {
            prepStmt.setString(1, label);
            prepStmt.setString(2, organization);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    policyIds.put(resultSet.getString("POLICY_ID"),
                            resultSet.getString("NAME"));
                }
            }
            return policyIds;
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_RETRIEVING_POLICIES, e,
                    organization);
        }

    }

    /**
     * Get PolicyIds by label
     *
     * @param label        Label
     * @param state        Governable State for the policy
     * @param organization Organization
     * @return List of Policy IDs
     */
    @Override
    public List<String> getPolicyAttachmentsByLabelAndState(String label, APIMGovernableState state,
                                                            String organization) throws APIMGovernanceException {
        List<String> policyIds = new ArrayList<>();
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.GET_POLICIES_BY_LABEL_AND_STATE)) {
            prepStmt.setString(1, label);
            prepStmt.setString(2, String.valueOf(state));
            prepStmt.setString(3, organization);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    policyIds.add(resultSet.getString("POLICY_ID"));
                }
            }
            return policyIds;
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_RETRIEVING_POLICIES, e,
                    organization);
        }
    }

    /**
     * Get Global Policies
     *
     * @param organization Organization
     * @return Map of Policy IDs, Policy Names
     */
    public Map<String, String> getGlobalPolicyAttachments(String organization)
            throws APIMGovernanceException {
        Map<String, String> policyIds = new HashMap<>();
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.GET_GLOBAL_POLICIES)) {
            prepStmt.setString(1, organization);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    policyIds.put(resultSet.getString("POLICY_ID"),
                            resultSet.getString("NAME"));
                }
            }
            return policyIds;
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_RETRIEVING_POLICIES, e,
                    organization);
        }
    }

    /**
     * Get Global Policies by State
     *
     * @param state        Governable State for the policy
     * @param organization Organization
     * @return List of Policy IDs
     */
    @Override
    public List<String> getGlobalPolicyAttachmentsWithState(APIMGovernableState state, String organization)
            throws APIMGovernanceException {
        List<String> policyIds = new ArrayList<>();
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                     .GET_GLOBAL_POLICIES_BY_STATE)) {
            prepStmt.setString(1, APIMGovernanceConstants.GLOBAL_LABEL);
            prepStmt.setString(1, String.valueOf(state));
            prepStmt.setString(2, organization);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    policyIds.add(resultSet.getString("POLICY_ID"));
                }
            }
            return policyIds;
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_RETRIEVING_POLICIES, e,
                    organization);
        }
    }

    /**
     * Get the actions of a policy
     *
     * @param policyAttachmentId Policy ID
     * @return List of Governance Actions
     * @throws APIMGovernanceException If an error occurs while getting the actions
     */
    @Override
    public List<APIMGovernanceAction> getActionsByPolicyAttachmentId(String policyAttachmentId)
            throws APIMGovernanceException {
        try (Connection connection = APIMGovernanceDBUtil.getConnection()) {
            return getActionsByPolicyAttachmentId(connection, policyAttachmentId);
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_RETRIEVING_ACTIONS_BY_POLICY_ID,
                    e, policyAttachmentId);
        }
    }

    /**
     * Search for Governance Policies
     *
     * @param searchCriteria Search criteria
     * @param organization   Organization
     * @return APIMGovernancePolicyList object
     * @throws APIMGovernanceException If an error occurs while searching for policies
     */
    @Override
    public APIMGovernancePolicyAttachmentList searchPolicyAttachments(Map<String, String> searchCriteria,
                                                                      String organization)
            throws APIMGovernanceException {
        APIMGovernancePolicyAttachmentList policyListObj = new APIMGovernancePolicyAttachmentList();
        List<APIMGovernancePolicyAttachment> policyList = new ArrayList<>();
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.SEARCH_POLICY_ATTACHMENT)) {
            prepStmt.setString(1, organization);
            prepStmt.setString(2, searchCriteria.getOrDefault(
                    APIMGovernanceConstants.PolicyAttachmentSearchAttributes.NAME, ""));
            prepStmt.setString(3, searchCriteria.getOrDefault(
                    APIMGovernanceConstants.PolicyAttachmentSearchAttributes.STATE, ""));
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    APIMGovernancePolicyAttachment policy = new APIMGovernancePolicyAttachment();
                    policy.setId(resultSet.getString("POLICY_ID"));
                    policy.setName(resultSet.getString("NAME"));
                    policy.setDescription(resultSet.getString("DESCRIPTION"));
                    policy.setCreatedBy(resultSet.getString("CREATED_BY"));
                    policy.setCreatedTime(resultSet.getString("CREATED_TIME"));
                    policy.setUpdatedBy(resultSet.getString("UPDATED_BY"));
                    policy.setUpdatedTime(resultSet.getString("LAST_UPDATED_TIME"));
                    policy.setPolicyIds(getPoliciesByPolicyAttachmentId(connection, policy.getId()));
                    policy.setLabels(getLabelsByPolicyAttachmentId(connection, policy.getId()));
                    policy.setActions(getActionsByPolicyAttachmentId(connection, policy.getId()));
                    policy.setGovernableStates(getStatesByPolicyAttachmentId(connection, policy.getId()));
                    policyList.add(policy);
                }
            }
            policyListObj.setCount(policyList.size());
            policyListObj.setGovernancePolicyAttachmentList(policyList);
            return policyListObj;
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_SEARCHING_POLICIES,
                    e, organization);
        }
    }

    /**
     * Get all the Rulesets attached to a Policy
     *
     * @param connection DB Connection
     * @param policyAttachmentId   Policy ID
     * @return List of Rulesets
     * @throws SQLException If an error occurs while retrieving the policies (Captured at higher level)
     */
    private List<String> getPoliciesByPolicyAttachmentId(Connection connection, String policyAttachmentId)
            throws SQLException {
        List<String> policyIds = new ArrayList<>();
        String sqlQuery = SQLConstants.GET_POLICY_IDS_BY_POLICY_ATTACHMENT_ID;
        try (PreparedStatement prepStmt = connection.prepareStatement(sqlQuery)) {
            prepStmt.setString(1, policyAttachmentId);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    policyIds.add(rs.getString("RULESET_ID"));
                }
            }
        }
        return policyIds;
    }

    /**
     * Get all the Labels attached to a Policy
     *
     * @param connection DB Connection
     * @param policyAttachmentId   Policy ID
     * @return List of Labels
     * @throws SQLException If an error occurs while retrieving the labels (Captured at higher level)
     */
    private List<String> getLabelsByPolicyAttachmentId(Connection connection, String policyAttachmentId)
            throws SQLException {
        List<String> labels = new ArrayList<>();
        try (PreparedStatement prepStmt =
                     connection.prepareStatement(SQLConstants.GET_LABELS_BY_POLICY_ATTACHMENT_ID)) {
            prepStmt.setString(1, policyAttachmentId);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    labels.add(resultSet.getString("LABEL"));
                }
            }
        }
        return labels;
    }

    /**
     * Get all the Governable States attached to a Policy
     *
     * @param connection DB Connection
     * @param policyAttachmentId   Policy ID
     * @return List of States
     * @throws SQLException If an error occurs while retrieving the states (Captured at higher level)
     */
    private List<APIMGovernableState> getStatesByPolicyAttachmentId(Connection connection, String policyAttachmentId)
            throws SQLException {
        List<APIMGovernableState> states = new ArrayList<>();
        try (PreparedStatement prepStmt =
                     connection.prepareStatement(SQLConstants.GET_STATES_BY_POLICY_ATTACHMENT_ID)) {
            prepStmt.setString(1, policyAttachmentId);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    states.add(APIMGovernableState.fromString(resultSet.getString("STATE")));
                }
            }
        }
        return states;
    }

    /**
     * Get all the Actions attached to a Policy
     *
     * @param connection DB Connection
     * @param policyAttachmentId   Policy ID
     * @return List of Actions
     * @throws SQLException If an error occurs while retrieving the actions (Captured at higher level)
     */
    private List<APIMGovernanceAction> getActionsByPolicyAttachmentId(Connection connection,
                                                                      String policyAttachmentId) throws SQLException {
        List<APIMGovernanceAction> actions = new ArrayList<>();
        try (PreparedStatement prepStmt =
                     connection.prepareStatement(SQLConstants.GET_ACTIONS_BY_POLICY_ATTACHMENT_ID)) {
            prepStmt.setString(1, policyAttachmentId);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    APIMGovernanceAction action = new APIMGovernanceAction();
                    action.setGovernableState(APIMGovernableState.fromString(resultSet
                            .getString("STATE")));
                    action.setRuleSeverity(RuleSeverity.fromString(resultSet.getString("SEVERITY")));
                    action.setType(APIMGovernanceActionType.fromString(resultSet.getString("TYPE")));
                    actions.add(action);
                }
            }
        }
        return actions;
    }


    /**
     * Delete a Governance Policy
     *
     * @param policyAttachmentId     Policy ID
     * @param organization Organization
     * @throws APIMGovernanceException If an error occurs while deleting the policy
     */
    @Override
    public void deletePolicyAttachments(String policyAttachmentId, String organization)
            throws APIMGovernanceException {
        try (Connection connection = APIMGovernanceDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {

                deletePolicyAttachmentResultsForPolicyAttachment(connection, policyAttachmentId);
                deleteEvalRequestsForPolicyAttachment(connection, policyAttachmentId);
                deletePolicyAttachmentPolicyMappingsForPolicyAttachment(connection, policyAttachmentId);
                deleteActionsMappingsForPolicyAttachment(connection, policyAttachmentId);
                deleteStatesMappingsForPolicyAttachment(connection, policyAttachmentId);
                deleteLabelsMappingsForPolicy(connection, policyAttachmentId);

                try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                        .DELETE_GOVERNANCE_POLICY_ATTACHMENT)) {
                    prepStmt.setString(1, policyAttachmentId);
                    prepStmt.setString(2, organization);
                    prepStmt.executeUpdate();
                }

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_DELETING_POLICY,
                    e, policyAttachmentId);
        }
    }

    /**
     * Delete Policy Evaluation Results for a Policy
     *
     * @param connection DB Connection
     * @param policyAttachmentId   Policy ID
     * @throws SQLException If an error occurs while deleting the results
     */
    private void deletePolicyAttachmentResultsForPolicyAttachment(Connection connection, String policyAttachmentId)
            throws SQLException {
        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                .DELETE_POLICY_ATTACHMENT_RUN_FOR_POLICY_ATTACHMENT)) {
            prepStmt.setString(1, policyAttachmentId);
            prepStmt.executeUpdate();
        }

    }

    /**
     * Delete Eval Requests for a Policy
     *
     * @param connection DB Connection
     * @param policyAttachmentId   Policy ID
     * @throws SQLException, APIMGovernanceException If an error occurs while deleting the requests
     */
    private void deleteEvalRequestsForPolicyAttachment(Connection connection, String policyAttachmentId)
            throws SQLException {
        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                .DELETE_REQ_POLICY_ATTACHMENT_MAPPING_FOR_POLICY_ATTACHMENT)) {
            prepStmt.setString(1, policyAttachmentId);
            prepStmt.executeUpdate();
        }
        //TODO: Check for currently processing requests for policy?
    }

    /**
     * Delete Policy Ruleset Mappings
     *
     * @param connection DB Connection
     * @param policyAttachmentId   Policy ID
     * @throws SQLException If an error occurs while deleting the mappings
     */
    private void deletePolicyAttachmentPolicyMappingsForPolicyAttachment(Connection connection,
                                                                         String policyAttachmentId)
            throws SQLException {
        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                .DELETE_POLICY_ATTACHMENT_POLICY_MAPPING_BY_POLICY_ATTACHMENT_ID)) {
            prepStmt.setString(1, policyAttachmentId);
            prepStmt.executeUpdate();
        }
    }

    /**
     * Delete Policy Actions Mappings
     *
     * @param connection DB Connection
     * @param policyAttachmentId   Policy ID
     * @throws SQLException If an error occurs while deleting the mappings
     */
    private void deleteActionsMappingsForPolicyAttachment(Connection connection, String policyAttachmentId)
            throws SQLException {
        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                .DELETE_GOVERNANCE_POLICY_ATTACHMENT_ACTION_MAPPING_BY_POLICY_ATTACHMENT_ID)) {
            prepStmt.setString(1, policyAttachmentId);
            prepStmt.executeUpdate();
        }
    }

    /**
     * Delete Policy States Mappings
     *
     * @param connection DB Connection
     * @param policyAttachmentId   Policy ID
     * @throws SQLException If an error occurs while deleting the mappings
     */
    private void deleteStatesMappingsForPolicyAttachment(Connection connection, String policyAttachmentId)
            throws SQLException {
        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                .DELETE_GOVERNANCE_POLICY_ATTACHMENT_STATE_MAPPING_BY_POLICY_ID)) {
            prepStmt.setString(1, policyAttachmentId);
            prepStmt.executeUpdate();
        }
    }

    /**
     * Delete Policy Labels Mappings
     *
     * @param connection DB Connection
     * @param policyAttachmentId   Policy ID
     * @throws SQLException If an error occurs while deleting the mappings
     */
    private void deleteLabelsMappingsForPolicy(Connection connection, String policyAttachmentId)
            throws SQLException {
        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                .DELETE_GOVERNANCE_POLICY_ATTACHMENT_LABEL_MAPPING_BY_POLICY_ID)) {
            prepStmt.setString(1, policyAttachmentId);
            prepStmt.executeUpdate();
        }
    }

    /**
     * Delete policy label mappings for a given label
     *
     * @param label        label
     * @param organization organization
     * @throws APIMGovernanceException If an error occurs while deleting the mappings
     */
    @Override
    public void deleteLabelPolicyAttachmentMappings(String label, String organization)
            throws APIMGovernanceException {
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                     .DELETE_GOVERNANCE_POLICY_ATTACHMENT_BY_LABEL)) {
            prepStmt.setString(1, label);
            prepStmt.setString(2, organization);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes
                    .ERROR_WHILE_DELETING_LABEL_POLICY_MAPPINGS, e, label);
        }
    }
}
