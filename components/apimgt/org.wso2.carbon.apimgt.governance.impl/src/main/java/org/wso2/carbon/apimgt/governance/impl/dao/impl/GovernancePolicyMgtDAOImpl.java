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

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernableState;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernanceAction;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernanceActionType;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernancePolicy;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernancePolicyList;
import org.wso2.carbon.apimgt.governance.api.model.ExtendedArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.RuleCategory;
import org.wso2.carbon.apimgt.governance.api.model.RuleSeverity;
import org.wso2.carbon.apimgt.governance.api.model.RuleType;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.api.model.RulesetContent;
import org.wso2.carbon.apimgt.governance.api.model.RulesetInfo;
import org.wso2.carbon.apimgt.governance.impl.APIMGovernanceConstants;
import org.wso2.carbon.apimgt.governance.impl.dao.GovernancePolicyMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceDBUtil;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the GovernancePolicyMgtDAO interface.
 */
public class GovernancePolicyMgtDAOImpl implements GovernancePolicyMgtDAO {

    private static final Log log = LogFactory.getLog(GovernancePolicyMgtDAOImpl.class);

    private GovernancePolicyMgtDAOImpl() {
    }

    private static class SingletonHelper {
        private static final GovernancePolicyMgtDAO INSTANCE = new GovernancePolicyMgtDAOImpl();
    }

    /**
     * Get instance of GovernancePolicyMgtDAOImpl
     *
     * @return GovernancePolicyMgtDAOImpl instance
     */
    public static GovernancePolicyMgtDAO getInstance() {
        return SingletonHelper.INSTANCE;
    }

    /**
     * Create a new Governance Policy
     *
     * @param governancePolicy Governance Policy Info with Ruleset Ids
     * @param organization     Organization
     * @return APIMGovernancePolicy Created object
     */
    @Override
    public APIMGovernancePolicy createGovernancePolicy(APIMGovernancePolicy governancePolicy, String organization)
            throws APIMGovernanceException {
        try (Connection connection = APIMGovernanceDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.CREATE_POLICY)) {
                    prepStmt.setString(1, governancePolicy.getId());
                    prepStmt.setString(2, governancePolicy.getName());
                    prepStmt.setString(3, governancePolicy.getDescription());
                    prepStmt.setString(4, organization);
                    prepStmt.setString(5, governancePolicy.getCreatedBy());
                    prepStmt.setInt(6, governancePolicy.isGlobal() ? 1 : 0);

                    Timestamp createdTime = new Timestamp(System.currentTimeMillis());
                    prepStmt.setTimestamp(7, createdTime);
                    governancePolicy.setCreatedTime(createdTime.toString());

                    prepStmt.execute();
                }

                insertPolicyRulesetMapping(connection, governancePolicy);
                insertPolicyLabels(connection, governancePolicy);
                insertPolicyStatesAndActions(connection, governancePolicy);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_CREATING_POLICY, e, organization);
        }
        return governancePolicy;
    }

    /**
     * Insert policy-ruleset mappings into the database.
     *
     * @param connection       Connection
     * @param governancePolicy Governance Policy
     * @throws SQLException If an error occurs while inserting the mappings (Captured at higher level)
     */
    private void insertPolicyRulesetMapping(Connection connection, APIMGovernancePolicy governancePolicy)
            throws SQLException {
        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                .CREATE_POLICY_RULESET_MAPPING)) {
            for (String rulesetId : governancePolicy.getRulesetIds()) {
                prepStmt.setString(1, governancePolicy.getId());
                prepStmt.setString(2, rulesetId);
                prepStmt.addBatch();
            }
            prepStmt.executeBatch();
        }
    }

    /**
     * Insert policy-label mappings into the database.
     *
     * @param connection       Connection
     * @param governancePolicy Governance Policy
     * @throws SQLException If an error occurs while inserting the mappings (Captured at higher level)
     */
    private void insertPolicyLabels(Connection connection, APIMGovernancePolicy governancePolicy)
            throws SQLException {
        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                .CREATE_GOVERNANCE_POLICY_LABEL_MAPPING)) {
            for (String label : governancePolicy.getLabels()) {
                prepStmt.setString(1, governancePolicy.getId());
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
     * @param governancePolicy Governance Policy
     * @throws SQLException If an error occurs while inserting the mappings
     */
    private void insertPolicyStatesAndActions(Connection connection, APIMGovernancePolicy governancePolicy)
            throws SQLException {
        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                .CREATE_GOVERNANCE_POLICY_STATE_MAPPING)) {
            for (APIMGovernableState state : governancePolicy.getGovernableStates()) {
                prepStmt.setString(1, governancePolicy.getId());
                prepStmt.setString(2, String.valueOf(state));
                prepStmt.addBatch();
            }
            prepStmt.executeBatch();
        }

        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                .CREATE_GOVERNANCE_POLICY_ACTION_MAPPING)) {
            for (APIMGovernanceAction action : governancePolicy.getActions()) {
                prepStmt.setString(1, governancePolicy.getId());
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
     * @param governancePolicy Governance Policy
     * @param organization     Organization
     * @return APIMGovernancePolicy Updated object
     * @throws APIMGovernanceException If an error occurs while updating the policy
     */
    @Override
    public APIMGovernancePolicy updateGovernancePolicy(String policyId, APIMGovernancePolicy governancePolicy,
                                                       String organization)
            throws APIMGovernanceException {
        try (Connection connection = APIMGovernanceDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                // Update policy details
                try (PreparedStatement updateStatement = connection.prepareStatement(SQLConstants.UPDATE_POLICY)) {
                    updateStatement.setString(1, governancePolicy.getName());
                    updateStatement.setString(2, governancePolicy.getDescription());
                    updateStatement.setString(3, governancePolicy.getUpdatedBy());
                    updateStatement.setInt(4, governancePolicy.isGlobal() ? 1 : 0);

                    Timestamp updatedTime = new Timestamp(System.currentTimeMillis());
                    updateStatement.setTimestamp(5, updatedTime);
                    governancePolicy.setUpdatedTime(updatedTime.toString());

                    updateStatement.setString(6, policyId);
                    updateStatement.setString(7, organization);
                    updateStatement.executeUpdate();
                }
                updatePolicyRulesetMappings(connection, policyId, governancePolicy);
                updatePolicyLabels(connection, policyId, governancePolicy);
                updateStatesAndPolicyActions(connection, policyId, governancePolicy);
                deletePolicyResultsForPolicy(connection, policyId);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_UPDATING_POLICY, e,
                    policyId);
        }
        governancePolicy.setId(policyId);
        return governancePolicy;
    }

    /**
     * Delete a Governance Policy
     *
     * @param connection       DB Connection
     * @param policyId         Policy ID
     * @param governancePolicy Governance Policy
     * @throws SQLException If an error occurs while deleting the policy (Captured at higher level)
     */
    private void updatePolicyRulesetMappings(Connection connection, String policyId,
                                             APIMGovernancePolicy governancePolicy) throws SQLException {

        // Delete old mappings and add new mappings
        try (PreparedStatement ps =
                     connection.prepareStatement(SQLConstants.DELETE_POLICY_RULESET_MAPPING_BY_POLICY_ID)) {
            ps.setString(1, policyId);
            ps.executeUpdate();
        }

        List<String> rulesetsToAdd = governancePolicy.getRulesetIds();
        if (rulesetsToAdd != null && !rulesetsToAdd.isEmpty()) {
            try (PreparedStatement ps =
                         connection.prepareStatement(SQLConstants.CREATE_POLICY_RULESET_MAPPING)) {
                for (String rulesetId : rulesetsToAdd) {
                    ps.setString(1, policyId);
                    ps.setString(2, rulesetId);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }
    }

    /**
     * Update labels for a Policy
     *
     * @param connection       DB Connection
     * @param policyId         Policy ID
     * @param governancePolicy Governance Policy
     * @throws SQLException If an error occurs while updating the labels (Captured at higher level)
     */
    private void updatePolicyLabels(Connection connection, String policyId,
                                    APIMGovernancePolicy governancePolicy) throws SQLException {
        // Delete old mappings and add new mappings
        try (PreparedStatement ps =
                     connection.prepareStatement(SQLConstants
                             .DELETE_GOVERNANCE_POLICY_LABEL_MAPPING_BY_POLICY_ID)) {
            ps.setString(1, policyId);
            ps.executeUpdate();
        }

        List<String> labelsToAdd = governancePolicy.getLabels();
        if (labelsToAdd != null && !labelsToAdd.isEmpty()) {
            try (PreparedStatement ps =
                         connection.prepareStatement(SQLConstants.CREATE_GOVERNANCE_POLICY_LABEL_MAPPING)) {
                for (String label : labelsToAdd) {
                    ps.setString(1, policyId);
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
     * @param policyId         Policy ID
     * @param governancePolicy Governance Policy
     * @throws SQLException If an error occurs while updating the actions (Captured at higher level)
     */
    private void updateStatesAndPolicyActions(Connection connection, String policyId,
                                              APIMGovernancePolicy governancePolicy) throws SQLException {

        // Delete old mappings and add new mappings
        try (PreparedStatement ps =
                     connection.prepareStatement(SQLConstants
                             .DELETE_GOVERNANCE_POLICY_ACTION_MAPPING_BY_POLICY_ID)) {
            ps.setString(1, policyId);
            ps.executeUpdate();
        }

        try (PreparedStatement ps =
                     connection.prepareStatement(SQLConstants
                             .DELETE_GOVERNANCE_POLICY_STATE_MAPPING_BY_POLICY_ID)) {
            ps.setString(1, policyId);
            ps.executeUpdate();
        }

        List<APIMGovernableState> statesToAdd = governancePolicy.getGovernableStates();
        if (statesToAdd != null && !statesToAdd.isEmpty()) {
            try (PreparedStatement ps =
                         connection.prepareStatement(SQLConstants.CREATE_GOVERNANCE_POLICY_STATE_MAPPING)) {
                for (APIMGovernableState state : statesToAdd) {
                    ps.setString(1, policyId);
                    ps.setString(2, String.valueOf(state));
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }

        List<APIMGovernanceAction> actionsToAdd = governancePolicy.getActions();
        if (actionsToAdd != null && !actionsToAdd.isEmpty()) {
            try (PreparedStatement ps =
                         connection.prepareStatement(SQLConstants.CREATE_GOVERNANCE_POLICY_ACTION_MAPPING)) {
                for (APIMGovernanceAction action : actionsToAdd) {
                    ps.setString(1, policyId);
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
     * @param policyName   Policy Name
     * @param organization Organization
     * @return APIMGovernancePolicy
     * @throws APIMGovernanceException If an error occurs while retrieving the policy
     */
    @Override
    public APIMGovernancePolicy getGovernancePolicyByName(String policyName, String organization)
            throws APIMGovernanceException {

        APIMGovernancePolicy policy = null;
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.GET_POLICY_BY_NAME)) {
            prepStmt.setString(1, organization);
            prepStmt.setString(2, policyName);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                if (resultSet.next()) {
                    policy = new APIMGovernancePolicy();
                    policy.setId(resultSet.getString("POLICY_ID"));
                    policy.setName(resultSet.getString("NAME"));
                    policy.setDescription(resultSet.getString("DESCRIPTION"));
                    policy.setCreatedBy(resultSet.getString("CREATED_BY"));
                    policy.setCreatedTime(resultSet.getString("CREATED_TIME"));
                    policy.setUpdatedBy(resultSet.getString("UPDATED_BY"));
                    policy.setUpdatedTime(resultSet.getString("LAST_UPDATED_TIME"));
                    policy.setGlobal(resultSet.getInt("IS_GLOBAL") == 1);
                }
            }
            return policy;
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_RETRIEVING_POLICY_BY_NAME,
                    e, policyName, organization);
        }
    }

    /**
     * Get Governance Policy by ID
     *
     * @param policyID     Policy ID
     * @param organization Organization
     * @return APIMGovernancePolicy
     * @throws APIMGovernanceException If an error occurs while retrieving the policy
     */
    @Override
    public APIMGovernancePolicy getGovernancePolicyByID(String policyID, String organization)
            throws APIMGovernanceException {
        APIMGovernancePolicy policy = null;
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.GET_POLICY_BY_ID)) {
            prepStmt.setString(1, policyID);
            prepStmt.setString(2, organization);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                if (resultSet.next()) {
                    policy = new APIMGovernancePolicy();
                    policy.setId(resultSet.getString("POLICY_ID"));
                    policy.setName(resultSet.getString("NAME"));
                    policy.setDescription(resultSet.getString("DESCRIPTION"));
                    policy.setCreatedBy(resultSet.getString("CREATED_BY"));
                    policy.setCreatedTime(resultSet.getString("CREATED_TIME"));
                    policy.setUpdatedBy(resultSet.getString("UPDATED_BY"));
                    policy.setUpdatedTime(resultSet.getString("LAST_UPDATED_TIME"));
                    policy.setGlobal(resultSet.getInt("IS_GLOBAL") == 1);
                    policy.setRulesetIds(getRulesetsByPolicyId(connection, policy.getId()));
                    policy.setLabels(getLabelsByPolicyId(connection, policy.getId()));
                    policy.setGovernableStates(getStatesByPolicyId(connection, policy.getId()));
                    policy.setActions(getActionsByPolicyId(connection, policy.getId()));
                }
            }
            return policy;
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_RETRIEVING_POLICY_BY_ID,
                    e, policyID);
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
    public APIMGovernancePolicyList getGovernancePolicies(String organization) throws APIMGovernanceException {
        APIMGovernancePolicyList policyListObj = new APIMGovernancePolicyList();
        List<APIMGovernancePolicy> policyList = new ArrayList<>();
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.GET_POLICIES)) {
            prepStmt.setString(1, organization);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    APIMGovernancePolicy policy = new APIMGovernancePolicy();
                    policy.setId(resultSet.getString("POLICY_ID"));
                    policy.setName(resultSet.getString("NAME"));
                    policy.setDescription(resultSet.getString("DESCRIPTION"));
                    policy.setCreatedBy(resultSet.getString("CREATED_BY"));
                    policy.setCreatedTime(resultSet.getString("CREATED_TIME"));
                    policy.setUpdatedBy(resultSet.getString("UPDATED_BY"));
                    policy.setUpdatedTime(resultSet.getString("LAST_UPDATED_TIME"));
                    policy.setGlobal(resultSet.getInt("IS_GLOBAL") == 1);
                    policy.setRulesetIds(getRulesetsByPolicyId(connection, policy.getId()));
                    policy.setLabels(getLabelsByPolicyId(connection, policy.getId()));
                    policy.setActions(getActionsByPolicyId(connection, policy.getId()));
                    policy.setGovernableStates(getStatesByPolicyId(connection, policy.getId()));
                    policyList.add(policy);
                }
            }
            policyListObj.setCount(policyList.size());
            policyListObj.setGovernancePolicyList(policyList);
            return policyListObj;
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_RETRIEVING_POLICIES,
                    e, organization);
        }
    }

    /**
     * Get all the Rulesets associated with Policies
     *
     * @param policyId     Policy ID
     * @param organization Organization
     * @return List of Rulesets
     * @throws APIMGovernanceException If an error occurs while getting the rulesets
     */
    @Override
    public List<Ruleset> getRulesetsWithContentByPolicyId(String policyId, String organization)
            throws APIMGovernanceException {
        List<Ruleset> rulesetList = new ArrayList<>();
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                     .GET_RULESETS_WITH_CONTENT_BY_POLICY_ID)) {
            prepStmt.setString(1, policyId);
            prepStmt.setString(2, organization);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    Ruleset ruleset = new Ruleset();
                    ruleset.setId(rs.getString("RULESET_ID"));
                    ruleset.setName(rs.getString("NAME"));
                    ruleset.setRuleCategory(RuleCategory.fromString(
                            rs.getString("RULE_CATEGORY")));
                    ruleset.setRuleType(RuleType.fromString(rs.getString("RULE_TYPE")));
                    ruleset.setArtifactType(ExtendedArtifactType.fromString(
                            rs.getString("ARTIFACT_TYPE")));

                    RulesetContent rulesetContent = new RulesetContent();
                    try (InputStream contentStream = rs.getBinaryStream("CONTENT")) {
                        byte[] content = IOUtils.toByteArray(contentStream);
                        rulesetContent.setContent(content);
                    } catch (IOException e) {
                        throw new APIMGovernanceException(APIMGovExceptionCodes
                                .ERROR_WHILE_RETRIEVING_RULESET_CONTENT, e, ruleset.getId());
                    }
                    rulesetContent.setFileName(rs.getString("FILE_NAME"));
                    ruleset.setRulesetContent(rulesetContent);

                    rulesetList.add(ruleset);
                }
            }
            return rulesetList;
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.
                    ERROR_WHILE_RETRIEVING_RULESETS_ASSOCIATED_WITH_POLICY, e, policyId);
        }
    }

    /**
     * Get the list of rulesets for a given policy
     *
     * @param policyId     Policy ID
     * @param organization Organization
     * @return List of rulesets
     * @throws APIMGovernanceException If an error occurs while getting the rulesets
     */
    @Override
    public List<RulesetInfo> getRulesetsByPolicyId(String policyId, String organization)
            throws APIMGovernanceException {
        List<RulesetInfo> rulesetList = new ArrayList<>();
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.GET_RULESETS_BY_POLICY_ID)) {
            prepStmt.setString(1, policyId);
            prepStmt.setString(2, organization);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    RulesetInfo ruleset = new RulesetInfo();
                    ruleset.setId(rs.getString("RULESET_ID"));
                    ruleset.setName(rs.getString("NAME"));
                    ruleset.setRuleCategory(RuleCategory.fromString(
                            rs.getString("RULE_CATEGORY")));
                    ruleset.setRuleType(RuleType.fromString(rs.getString("RULE_TYPE")));
                    ruleset.setArtifactType(ExtendedArtifactType.fromString(
                            rs.getString("ARTIFACT_TYPE")));
                    rulesetList.add(ruleset);
                }
            }
            return rulesetList;
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes
                    .ERROR_WHILE_RETRIEVING_RULESETS_ASSOCIATED_WITH_POLICY, e, policyId);
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
    public Map<String, String> getPoliciesByLabel(String label, String organization)
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
    public List<String> getPoliciesByLabelAndState(String label, APIMGovernableState state, String organization)
            throws APIMGovernanceException {
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
    public Map<String, String> getGlobalPolicies(String organization)
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
    public List<String> getGlobalPoliciesWithState(APIMGovernableState state, String organization)
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
     * @param policyId Policy ID
     * @return List of Governance Actions
     * @throws APIMGovernanceException If an error occurs while getting the actions
     */
    @Override
    public List<APIMGovernanceAction> getActionsByPolicyId(String policyId) throws APIMGovernanceException {
        try (Connection connection = APIMGovernanceDBUtil.getConnection()) {
            return getActionsByPolicyId(connection, policyId);
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_RETRIEVING_ACTIONS_BY_POLICY_ID,
                    e, policyId);
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
    public APIMGovernancePolicyList searchPolicies(Map<String, String> searchCriteria,
                                                   String organization) throws APIMGovernanceException {
        APIMGovernancePolicyList policyListObj = new APIMGovernancePolicyList();
        List<APIMGovernancePolicy> policyList = new ArrayList<>();
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.SEARCH_POLICIES)) {
            prepStmt.setString(1, organization);
            prepStmt.setString(2, searchCriteria.getOrDefault(
                    APIMGovernanceConstants.PolicySearchAttributes.NAME, ""));
            prepStmt.setString(3, searchCriteria.getOrDefault(
                    APIMGovernanceConstants.PolicySearchAttributes.STATE, ""));
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    APIMGovernancePolicy policy = new APIMGovernancePolicy();
                    policy.setId(resultSet.getString("POLICY_ID"));
                    policy.setName(resultSet.getString("NAME"));
                    policy.setDescription(resultSet.getString("DESCRIPTION"));
                    policy.setCreatedBy(resultSet.getString("CREATED_BY"));
                    policy.setCreatedTime(resultSet.getString("CREATED_TIME"));
                    policy.setUpdatedBy(resultSet.getString("UPDATED_BY"));
                    policy.setUpdatedTime(resultSet.getString("LAST_UPDATED_TIME"));
                    policy.setRulesetIds(getRulesetsByPolicyId(connection, policy.getId()));
                    policy.setLabels(getLabelsByPolicyId(connection, policy.getId()));
                    policy.setActions(getActionsByPolicyId(connection, policy.getId()));
                    policy.setGovernableStates(getStatesByPolicyId(connection, policy.getId()));
                    policyList.add(policy);
                }
            }
            policyListObj.setCount(policyList.size());
            policyListObj.setGovernancePolicyList(policyList);
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
     * @param policyId   Policy ID
     * @return List of Rulesets
     * @throws SQLException If an error occurs while retrieving the rulesets (Captured at higher level)
     */
    private List<String> getRulesetsByPolicyId(Connection connection, String policyId) throws SQLException {
        List<String> rulesetIds = new ArrayList<>();
        String sqlQuery = SQLConstants.GET_RULESET_IDS_BY_POLICY_ID;
        try (PreparedStatement prepStmt = connection.prepareStatement(sqlQuery)) {
            prepStmt.setString(1, policyId);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    rulesetIds.add(rs.getString("RULESET_ID"));
                }
            }
        }
        return rulesetIds;
    }

    /**
     * Get all the Labels attached to a Policy
     *
     * @param connection DB Connection
     * @param policyId   Policy ID
     * @return List of Labels
     * @throws SQLException If an error occurs while retrieving the labels (Captured at higher level)
     */
    private List<String> getLabelsByPolicyId(Connection connection, String policyId) throws SQLException {
        List<String> labels = new ArrayList<>();
        try (PreparedStatement prepStmt =
                     connection.prepareStatement(SQLConstants.GET_LABELS_BY_POLICY_ID)) {
            prepStmt.setString(1, policyId);
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
     * @param policyId   Policy ID
     * @return List of States
     * @throws SQLException If an error occurs while retrieving the states (Captured at higher level)
     */
    private List<APIMGovernableState> getStatesByPolicyId(Connection connection, String policyId) throws SQLException {
        List<APIMGovernableState> states = new ArrayList<>();
        try (PreparedStatement prepStmt =
                     connection.prepareStatement(SQLConstants.GET_STATES_BY_POLICY_ID)) {
            prepStmt.setString(1, policyId);
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
     * @param policyId   Policy ID
     * @return List of Actions
     * @throws SQLException If an error occurs while retrieving the actions (Captured at higher level)
     */
    private List<APIMGovernanceAction> getActionsByPolicyId(Connection connection,
                                                            String policyId) throws SQLException {
        List<APIMGovernanceAction> actions = new ArrayList<>();
        try (PreparedStatement prepStmt =
                     connection.prepareStatement(SQLConstants.GET_ACTIONS_BY_POLICY_ID)) {
            prepStmt.setString(1, policyId);
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
     * @param policyId     Policy ID
     * @param organization Organization
     * @throws APIMGovernanceException If an error occurs while deleting the policy
     */
    @Override
    public void deletePolicy(String policyId, String organization) throws APIMGovernanceException {
        try (Connection connection = APIMGovernanceDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {

                deletePolicyResultsForPolicy(connection, policyId);
                deleteEvalRequestsForPolicy(connection, policyId);
                deletePolicyRulesetMappingsForPolicy(connection, policyId);
                deleteActionsMappingsForPolicy(connection, policyId);
                deleteStatesMappingsForPolicy(connection, policyId);
                deleteLabelsMappingsForPolicy(connection, policyId);

                try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                        .DELETE_GOVERNANCE_POLICY)) {
                    prepStmt.setString(1, policyId);
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
                    e, policyId);
        }
    }

    /**
     * Delete Policy Evaluation Results for a Policy
     *
     * @param connection DB Connection
     * @param policyId   Policy ID
     * @throws SQLException If an error occurs while deleting the results
     */
    private void deletePolicyResultsForPolicy(Connection connection, String policyId) throws SQLException {
        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                .DELETE_POLICY_RUN_FOR_POLICY)) {
            prepStmt.setString(1, policyId);
            prepStmt.executeUpdate();
        }

    }

    /**
     * Delete Eval Requests for a Policy
     *
     * @param connection DB Connection
     * @param policyId   Policy ID
     * @throws SQLException, APIMGovernanceException If an error occurs while deleting the requests
     */
    private void deleteEvalRequestsForPolicy(Connection connection, String policyId) throws SQLException {
        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                .DELETE_REQ_POLICY_MAPPING_FOR_POLICY)) {
            prepStmt.setString(1, policyId);
            prepStmt.executeUpdate();
        }
        //TODO: Check for currently processing requests for policy?
    }

    /**
     * Delete Policy Ruleset Mappings
     *
     * @param connection DB Connection
     * @param policyId   Policy ID
     * @throws SQLException If an error occurs while deleting the mappings
     */
    private void deletePolicyRulesetMappingsForPolicy(Connection connection, String policyId)
            throws SQLException {
        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                .DELETE_POLICY_RULESET_MAPPING_BY_POLICY_ID)) {
            prepStmt.setString(1, policyId);
            prepStmt.executeUpdate();
        }
    }

    /**
     * Delete Policy Actions Mappings
     *
     * @param connection DB Connection
     * @param policyId   Policy ID
     * @throws SQLException If an error occurs while deleting the mappings
     */
    private void deleteActionsMappingsForPolicy(Connection connection, String policyId)
            throws SQLException {
        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                .DELETE_GOVERNANCE_POLICY_ACTION_MAPPING_BY_POLICY_ID)) {
            prepStmt.setString(1, policyId);
            prepStmt.executeUpdate();
        }
    }

    /**
     * Delete Policy States Mappings
     *
     * @param connection DB Connection
     * @param policyId   Policy ID
     * @throws SQLException If an error occurs while deleting the mappings
     */
    private void deleteStatesMappingsForPolicy(Connection connection, String policyId)
            throws SQLException {
        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                .DELETE_GOVERNANCE_POLICY_STATE_MAPPING_BY_POLICY_ID)) {
            prepStmt.setString(1, policyId);
            prepStmt.executeUpdate();
        }
    }

    /**
     * Delete Policy Labels Mappings
     *
     * @param connection DB Connection
     * @param policyId   Policy ID
     * @throws SQLException If an error occurs while deleting the mappings
     */
    private void deleteLabelsMappingsForPolicy(Connection connection, String policyId)
            throws SQLException {
        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                .DELETE_GOVERNANCE_POLICY_LABEL_MAPPING_BY_POLICY_ID)) {
            prepStmt.setString(1, policyId);
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
    public void deleteLabelPolicyMappings(String label, String organization) throws APIMGovernanceException {
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                     .DELETE_GOVERNANCE_POLICIES_BY_LABEL)) {
            prepStmt.setString(1, label);
            prepStmt.setString(2, organization);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes
                    .ERROR_WHILE_DELETING_LABEL_POLICY_MAPPINGS, e, label);
        }
    }
}
