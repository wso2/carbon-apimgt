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
import org.wso2.carbon.apimgt.governance.api.model.ExtendedArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.GovernableState;
import org.wso2.carbon.apimgt.governance.api.model.GovernanceAction;
import org.wso2.carbon.apimgt.governance.api.model.GovernanceActionType;
import org.wso2.carbon.apimgt.governance.api.model.GovernancePolicy;
import org.wso2.carbon.apimgt.governance.api.model.GovernancePolicyList;
import org.wso2.carbon.apimgt.governance.api.model.RuleCategory;
import org.wso2.carbon.apimgt.governance.api.model.RuleType;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.api.model.Severity;
import org.wso2.carbon.apimgt.governance.impl.GovernanceConstants;
import org.wso2.carbon.apimgt.governance.impl.dao.GovernancePolicyMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.governance.impl.util.GovernanceDBUtil;
import org.wso2.carbon.apimgt.governance.impl.util.GovernanceUtil;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
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

    /**
     * Bill Pugh Singleton Implementation
     */
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
     * @return GovernancePolicy Created object
     */
    @Override
    public GovernancePolicy createGovernancePolicy(GovernancePolicy governancePolicy, String organization)
            throws GovernanceException {
        List<String> rulesetIds;
        Timestamp timestamp;
        List<String> labels;
        List<GovernableState> states;
        List<GovernanceAction> actions;
        try (Connection connection = GovernanceDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.CREATE_POLICY)) {
                    prepStmt.setString(1, governancePolicy.getId());
                    prepStmt.setString(2, governancePolicy.getName());
                    prepStmt.setString(3, governancePolicy.getDescription());
                    prepStmt.setString(4, organization);
                    prepStmt.setString(5, governancePolicy.getCreatedBy());

                    timestamp = new Timestamp(System.currentTimeMillis());
                    governancePolicy.setCreatedTime(timestamp.toString());
                    prepStmt.setTimestamp(6, timestamp);
                    prepStmt.execute();
                }
                // Insert into GOV_POLICY_RULESET_MAPPING table
                try (PreparedStatement prepStmt =
                             connection.prepareStatement(SQLConstants.CREATE_POLICY_RULESET_MAPPING)) {
                    rulesetIds = governancePolicy.getRulesetIds();
                    for (String rulesetId : rulesetIds) {
                        prepStmt.setString(1, GovernanceUtil.generateUUID());
                        prepStmt.setString(2, governancePolicy.getId());
                        prepStmt.setString(3, rulesetId);
                        prepStmt.addBatch();
                    }
                    prepStmt.executeBatch();
                }

                // Insert into GOV_POLICY_LABEL table
                try (PreparedStatement prepStmt =
                             connection.prepareStatement(SQLConstants.CREATE_GOVERNANCE_POLICY_LABEL_MAPPING)) {
                    labels = governancePolicy.getLabels();
                    for (String label : labels) {
                        if (label.equals(GovernanceConstants.GLOBAL_LABEL)) {
                            label = GovernanceConstants.GLOBAL_LABEL; // To handle case-sensitive Dbs
                        }
                        prepStmt.setString(1, GovernanceUtil.generateUUID());
                        prepStmt.setString(2, governancePolicy.getId());
                        prepStmt.setString(3, label);
                        prepStmt.addBatch();
                    }
                    prepStmt.executeBatch();
                }

                // Insert into GOV_POLICY_GOVERNABLE_STATE table
                try (PreparedStatement prepStmt =
                             connection.prepareStatement(SQLConstants.CREATE_GOVERNANCE_POLICY_STATE_MAPPING)) {
                    states = governancePolicy.getGovernableStates();
                    for (GovernableState state : states) {
                        prepStmt.setString(1, GovernanceUtil.generateUUID());
                        prepStmt.setString(2, governancePolicy.getId());
                        prepStmt.setString(3, String.valueOf(state));
                        prepStmt.addBatch();
                    }
                    prepStmt.executeBatch();
                }

                // Insert into GOV_POLICY_ACTION table
                try (PreparedStatement prepStmt =
                             connection.prepareStatement(SQLConstants.CREATE_GOVERNANCE_POLICY_ACTION_MAPPING)) {
                    actions = governancePolicy.getActions();
                    for (GovernanceAction action : actions) {
                        prepStmt.setString(1, GovernanceUtil.generateUUID());
                        prepStmt.setString(2, governancePolicy.getId());
                        prepStmt.setString(3, String.valueOf(action.getGovernableState()));
                        prepStmt.setString(4, String.valueOf(action.getRuleSeverity()));
                        prepStmt.setString(5, String.valueOf(action.getType()));
                        prepStmt.addBatch();
                    }
                    prepStmt.executeBatch();
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            if (e instanceof SQLIntegrityConstraintViolationException) {
                if (getGovernancePolicyByName(governancePolicy.getName(), organization) != null) {
                    throw new GovernanceException(GovernanceExceptionCodes.POLICY_ALREADY_EXISTS, e,
                            governancePolicy.getName(), organization);
                }
            }
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_CREATING_POLICY, e, organization);
        }
        return getGovernancePolicyByID(governancePolicy.getId()); // to return saved policy
    }

    /**
     * Get Governance Policy by Name
     *
     * @param policyName   Policy Name
     * @param organization Organization
     * @return GovernancePolicy
     * @throws GovernanceException If an error occurs while retrieving the policy
     */
    @Override
    public GovernancePolicy getGovernancePolicyByName(String policyName, String organization)
            throws GovernanceException {

        GovernancePolicy policy = null;
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.GET_POLICY_BY_NAME)) {
            prepStmt.setString(1, organization);
            prepStmt.setString(2, policyName);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                if (resultSet.next()) {
                    policy = new GovernancePolicy();
                    policy.setId(resultSet.getString("POLICY_ID"));
                    policy.setName(resultSet.getString("NAME"));
                    policy.setDescription(resultSet.getString("DESCRIPTION"));
                    policy.setCreatedBy(resultSet.getString("CREATED_BY"));
                    policy.setCreatedTime(resultSet.getString("CREATED_TIME"));
                    policy.setUpdatedBy(resultSet.getString("UPDATED_BY"));
                    policy.setUpdatedTime(resultSet.getString("LAST_UPDATED_TIME"));
                }
            }
            return policy;
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_RETRIEVING_POLICY_BY_NAME, e, policyName,
                    organization);
        }
    }

    /**
     * Get Governance Policy by ID
     *
     * @param policyID Policy ID
     * @return GovernancePolicy
     * @throws GovernanceException If an error occurs while retrieving the policy
     */
    @Override
    public GovernancePolicy getGovernancePolicyByID(String policyID) throws GovernanceException {
        GovernancePolicy policy = null;
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.GET_POLICY_BY_ID)) {
            prepStmt.setString(1, policyID);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                if (resultSet.next()) {
                    policy = new GovernancePolicy();
                    policy.setId(resultSet.getString("POLICY_ID"));
                    policy.setName(resultSet.getString("NAME"));
                    policy.setDescription(resultSet.getString("DESCRIPTION"));
                    policy.setCreatedBy(resultSet.getString("CREATED_BY"));
                    policy.setCreatedTime(resultSet.getString("CREATED_TIME"));
                    policy.setUpdatedBy(resultSet.getString("UPDATED_BY"));
                    policy.setUpdatedTime(resultSet.getString("LAST_UPDATED_TIME"));
                    policy.setRulesetIds(getRulesetsByPolicyId(policy.getId(), connection));
                    policy.setLabels(getLabelsByPolicyId(policy.getId(), connection));
                    policy.setGovernableStates(getStatesByPolicyId(policy.getId(), connection));
                    policy.setActions(getActionsByPolicyId(policy.getId(), connection));
                }
            }
            return policy;
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_RETRIEVING_POLICY_BY_ID, e, policyID);
        }
    }

    /**
     * Get all the Governance Policies
     *
     * @param organization Organization
     * @return GovernancePolicyList object
     * @throws GovernanceException If an error occurs while getting the policies
     */
    @Override
    public GovernancePolicyList getGovernancePolicies(String organization) throws GovernanceException {
        GovernancePolicyList policyListObj = new GovernancePolicyList();
        List<GovernancePolicy> policyList = new ArrayList<>();
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.GET_POLICIES)) {
            prepStmt.setString(1, organization);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    GovernancePolicy policy = new GovernancePolicy();
                    policy.setId(resultSet.getString("POLICY_ID"));
                    policy.setName(resultSet.getString("NAME"));
                    policy.setDescription(resultSet.getString("DESCRIPTION"));
                    policy.setCreatedBy(resultSet.getString("CREATED_BY"));
                    policy.setCreatedTime(resultSet.getString("CREATED_TIME"));
                    policy.setUpdatedBy(resultSet.getString("UPDATED_BY"));
                    policy.setUpdatedTime(resultSet.getString("LAST_UPDATED_TIME"));
                    policy.setRulesetIds(getRulesetsByPolicyId(policy.getId(), connection));
                    policy.setLabels(getLabelsByPolicyId(policy.getId(), connection));
                    policy.setActions(getActionsByPolicyId(policy.getId(), connection));
                    policy.setGovernableStates(getStatesByPolicyId(policy.getId(), connection));
                    policyList.add(policy);
                }
            }
            policyListObj.setCount(policyList.size());
            policyListObj.setGovernancePolicyList(policyList);
            return policyListObj;
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_RETRIEVING_POLICIES,
                    e, organization);
        }
    }

    /**
     * Update a Governance Policy
     *
     * @param policyId         Policy ID
     * @param governancePolicy Governance Policy Info with Ruleset Ids
     * @return GovernancePolicy Updated object
     * @throws GovernanceException If an error occurs while updating the policy
     */
    @Override
    public GovernancePolicy updateGovernancePolicy(String policyId, GovernancePolicy governancePolicy)
            throws GovernanceException {
        Timestamp timestamp;
        try (Connection connection = GovernanceDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                // Update policy details
                try (PreparedStatement updateStatement = connection.prepareStatement(SQLConstants.UPDATE_POLICY)) {
                    updateStatement.setString(1, governancePolicy.getName());
                    updateStatement.setString(2, governancePolicy.getDescription());
                    updateStatement.setString(3, governancePolicy.getUpdatedBy());

                    timestamp = new Timestamp(System.currentTimeMillis());
                    governancePolicy.setUpdatedTime(timestamp.toString());
                    updateStatement.setTimestamp(4, timestamp);

                    updateStatement.setString(5, policyId);
                    updateStatement.executeUpdate();
                }
                // Retrieve existing rulesets
                List<String> existingRulesets = new ArrayList<>();
                try (PreparedStatement getRulesetsStatement =
                             connection.prepareStatement(SQLConstants.GET_RULESET_IDS_BY_POLICY_ID)) {
                    getRulesetsStatement.setString(1, policyId);
                    try (ResultSet resultSet = getRulesetsStatement.executeQuery()) {
                        while (resultSet.next()) {
                            existingRulesets.add(resultSet.getString("RULESET_ID"));
                        }
                    }
                }
                // Determine rulesets to add and remove
                List<String> rulesetsToBeUpdatedList = governancePolicy.getRulesetIds();
                List<String> rulesetsToAdd = new ArrayList<>(rulesetsToBeUpdatedList);
                List<String> rulesetsToRemove = new ArrayList<>(existingRulesets);
                rulesetsToAdd.removeAll(existingRulesets);
                rulesetsToRemove.removeAll(rulesetsToBeUpdatedList);
                // Remove old rulesets
                if (!rulesetsToRemove.isEmpty()) {
                    try (PreparedStatement deleteStatement =
                                 connection.prepareStatement(SQLConstants.DELETE_POLICY_RULESET_MAPPING)) {
                        for (String rulesetId : rulesetsToRemove) {
                            deleteStatement.setString(1, policyId);
                            deleteStatement.setString(2, rulesetId);
                            deleteStatement.addBatch();
                        }
                        deleteStatement.executeBatch();
                    }
                }
                // Add new rulesets
                if (!rulesetsToAdd.isEmpty()) {
                    try (PreparedStatement insertStatement =
                                 connection.prepareStatement(SQLConstants.CREATE_POLICY_RULESET_MAPPING)) {
                        for (String rulesetId : rulesetsToAdd) {
                            insertStatement.setString(1, GovernanceUtil.generateUUID());
                            insertStatement.setString(2, policyId);
                            insertStatement.setString(3, rulesetId);
                            insertStatement.addBatch();
                        }
                        insertStatement.executeBatch();
                    }
                }
                updateLabelsForPolicy(policyId, governancePolicy, connection);
                updateGovernableStatesForPolicy(policyId, governancePolicy, connection);
                updateActionsForPolicy(policyId, governancePolicy, connection);
                deleteComplianceEvaluationResultsForPolicy(policyId, connection);

                // return updated GovernancePolicy object
                connection.commit();
            } catch (SQLException | GovernanceException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_UPDATING_POLICY, e,
                    policyId);
        }
        return getGovernancePolicyByID(policyId);
    }

    /**
     * Update governable states for a Policy
     *
     * @param policyId         Policy ID
     * @param governancePolicy Governance Policy Info with Ruleset Ids
     * @param connection       DB Connection
     * @throws SQLException If an error occurs while updating the states (Captured at higher level)
     */
    private void updateGovernableStatesForPolicy(String policyId, GovernancePolicy governancePolicy,
                                                 Connection connection) throws SQLException {
        // Retrieve applicable States
        List<GovernableState> existingStates = new ArrayList<>();
        try (PreparedStatement getStatesStatement =
                     connection.prepareStatement(SQLConstants.GET_STATES_BY_POLICY_ID)) {
            getStatesStatement.setString(1, policyId);
            try (ResultSet resultSet = getStatesStatement.executeQuery()) {
                while (resultSet.next()) {
                    existingStates.add(GovernableState.fromString(resultSet.getString("STATE")));
                }
            }
        }
        // Determine states to add and remove
        List<GovernableState> statesToBeUpdated = governancePolicy.getGovernableStates();
        List<GovernableState> statesToAdd = new ArrayList<>(statesToBeUpdated);
        List<GovernableState> statesToRemove = new ArrayList<>(existingStates);
        statesToAdd.removeAll(existingStates);
        statesToRemove.removeAll(statesToBeUpdated);
        // Remove old states
        if (!statesToRemove.isEmpty()) {
            try (PreparedStatement deleteStatement =
                         connection.prepareStatement(SQLConstants.DELETE_GOVERNANCE_POLICY_STATE_MAPPING)) {
                for (GovernableState state : statesToRemove) {
                    deleteStatement.setString(1, policyId);
                    deleteStatement.setString(2, String.valueOf(state));
                    deleteStatement.addBatch();
                }
                deleteStatement.executeBatch();
            }
        }
        // Add new states
        if (!statesToAdd.isEmpty()) {
            try (PreparedStatement insertStatement =
                         connection.prepareStatement(SQLConstants.CREATE_GOVERNANCE_POLICY_STATE_MAPPING)) {
                for (GovernableState state : statesToAdd) {
                    insertStatement.setString(1, GovernanceUtil.generateUUID());
                    insertStatement.setString(2, policyId);
                    insertStatement.setString(3, String.valueOf(state));
                    insertStatement.addBatch();
                }
                insertStatement.executeBatch();
            }
        }

    }

    /**
     * Update labels for a Policy
     *
     * @param policyId         Policy ID
     * @param governancePolicy Governance Policy Info with Ruleset Ids
     * @param connection       DB Connection
     * @throws SQLException If an error occurs while updating the labels (Captured at higher level)
     */
    private void updateLabelsForPolicy(String policyId, GovernancePolicy governancePolicy,
                                       Connection connection) throws SQLException {
        // Retrieve existing labels
        List<String> existingLabels = new ArrayList<>();
        try (PreparedStatement getLabelsStatement =
                     connection.prepareStatement(SQLConstants.GET_LABELS_BY_POLICY_ID)) {
            getLabelsStatement.setString(1, policyId);
            try (ResultSet resultSet = getLabelsStatement.executeQuery()) {
                while (resultSet.next()) {
                    existingLabels.add(resultSet.getString("LABEL"));
                }
            }
        }
        // Determine labels to add and remove
        List<String> labelsToBeUpdated = governancePolicy.getLabels();
        List<String> labelsToAdd = new ArrayList<>(labelsToBeUpdated);
        List<String> labelsToRemove = new ArrayList<>(existingLabels);
        labelsToAdd.removeAll(existingLabels);
        labelsToRemove.removeAll(labelsToBeUpdated);
        // Remove old labels
        if (!labelsToRemove.isEmpty()) {
            try (PreparedStatement deleteStatement =
                         connection.prepareStatement(SQLConstants.DELETE_GOVERNANCE_POLICY_LABEL_MAPPING)) {
                for (String label : labelsToRemove) {
                    deleteStatement.setString(1, policyId);
                    deleteStatement.setString(2, label);
                    deleteStatement.addBatch();
                }
                deleteStatement.executeBatch();
            }
        }
        // Add new labels
        if (!labelsToAdd.isEmpty()) {
            try (PreparedStatement insertStatement =
                         connection.prepareStatement(SQLConstants.CREATE_GOVERNANCE_POLICY_LABEL_MAPPING)) {
                for (String label : labelsToAdd) {
                    if (label.equals(GovernanceConstants.GLOBAL_LABEL)) {
                        label = GovernanceConstants.GLOBAL_LABEL; // To handle case-sensitive Dbs
                    }
                    insertStatement.setString(1, GovernanceUtil.generateUUID());
                    insertStatement.setString(2, policyId);
                    insertStatement.setString(3, label);
                    insertStatement.addBatch();
                }
                insertStatement.executeBatch();
            }
        }
    }

    /**
     * Update actions for a Policy
     *
     * @param policyId         Policy ID
     * @param governancePolicy Governance Policy Info with Ruleset Ids
     * @param connection       DB Connection
     * @throws SQLException If an error occurs while updating the actions (Captured at higher level)
     */
    private void updateActionsForPolicy(String policyId, GovernancePolicy governancePolicy,
                                        Connection connection) throws SQLException {
        // Retrieve existing actions
        List<GovernanceAction> existingActions = new ArrayList<>();
        try (PreparedStatement getActionsStatement =
                     connection.prepareStatement(SQLConstants.GET_ACTIONS_BY_POLICY_ID)) {
            getActionsStatement.setString(1, policyId);
            try (ResultSet resultSet = getActionsStatement.executeQuery()) {
                while (resultSet.next()) {
                    GovernanceAction action = new GovernanceAction();
                    action.setGovernableState(GovernableState.fromString(resultSet.getString("STATE")));
                    action.setRuleSeverity(Severity.fromString(resultSet.getString("SEVERITY")));
                    action.setType(GovernanceActionType.fromString(resultSet.getString("TYPE")));
                    existingActions.add(action);
                }
            }
        }
        // Determine actions to add and remove
        List<GovernanceAction> actionsToBeUpdated = governancePolicy.getActions();
        List<GovernanceAction> actionsToAdd = new ArrayList<>(actionsToBeUpdated);
        List<GovernanceAction> actionsToRemove = new ArrayList<>(existingActions);
        actionsToAdd.removeAll(existingActions);
        actionsToRemove.removeAll(actionsToBeUpdated);
        // Remove old actions
        if (!actionsToRemove.isEmpty()) {
            try (PreparedStatement deleteStatement =
                         connection.prepareStatement(SQLConstants.DELETE_GOVERNANCE_POLICY_ACTION_MAPPING)) {
                for (GovernanceAction action : actionsToRemove) {
                    deleteStatement.setString(1, policyId);
                    deleteStatement.setString(2, String.valueOf(action.getGovernableState()));
                    deleteStatement.setString(3, String.valueOf(action.getRuleSeverity()));
                    deleteStatement.setString(4, String.valueOf(action.getType()));
                    deleteStatement.addBatch();
                }
                deleteStatement.executeBatch();
            }
        }
        // Add new actions
        if (!actionsToAdd.isEmpty()) {
            try (PreparedStatement insertStatement =
                         connection.prepareStatement(SQLConstants.CREATE_GOVERNANCE_POLICY_ACTION_MAPPING)) {
                for (GovernanceAction action : actionsToAdd) {
                    insertStatement.setString(1, GovernanceUtil.generateUUID());
                    insertStatement.setString(2, policyId);
                    insertStatement.setString(3, String.valueOf(action.getGovernableState()));
                    insertStatement.setString(4, String.valueOf(action.getRuleSeverity()));
                    insertStatement.setString(5, String.valueOf(action.getType()));
                    insertStatement.addBatch();
                }
                insertStatement.executeBatch();
            }
        }

    }

    /**
     * Get all the Rulesets attached to a Policy
     *
     * @param policyId   Policy ID
     * @param connection DB Connection
     * @return List of Rulesets
     * @throws SQLException If an error occurs while retrieving the rulesets (Captured at higher level)
     */
    private List<String> getRulesetsByPolicyId(String policyId, Connection connection) throws SQLException {
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
     * @param policyId   Policy ID
     * @param connection DB Connection
     * @return List of Labels
     * @throws SQLException If an error occurs while retrieving the labels (Captured at higher level)
     */
    private List<String> getLabelsByPolicyId(String policyId, Connection connection) throws SQLException {
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
     * @param policyId   Policy ID
     * @param connection DB Connection
     * @return List of States
     * @throws SQLException If an error occurs while retrieving the states (Captured at higher level)
     */
    private List<GovernableState> getStatesByPolicyId(String policyId, Connection connection) throws SQLException {
        List<GovernableState> states = new ArrayList<>();
        try (PreparedStatement prepStmt =
                     connection.prepareStatement(SQLConstants.GET_STATES_BY_POLICY_ID)) {
            prepStmt.setString(1, policyId);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    states.add(GovernableState.fromString(resultSet.getString("STATE")));
                }
            }
        }
        return states;
    }

    /**
     * Get all the Actions attached to a Policy
     *
     * @param policyId   Policy ID
     * @param connection DB Connection
     * @return List of Actions
     * @throws SQLException If an error occurs while retrieving the actions (Captured at higher level)
     */
    private List<GovernanceAction> getActionsByPolicyId(String policyId, Connection connection) throws SQLException {
        List<GovernanceAction> actions = new ArrayList<>();
        try (PreparedStatement prepStmt =
                     connection.prepareStatement(SQLConstants.GET_ACTIONS_BY_POLICY_ID)) {
            prepStmt.setString(1, policyId);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    GovernanceAction action = new GovernanceAction();
                    action.setGovernableState(GovernableState.fromString(resultSet
                            .getString("STATE")));
                    action.setRuleSeverity(Severity.fromString(resultSet.getString("SEVERITY")));
                    action.setType(GovernanceActionType.fromString(resultSet.getString("TYPE")));
                    actions.add(action);
                }
            }
        }
        return actions;
    }


    /**
     * Get all the Rulesets associated with Policies
     *
     * @param policyId Policy ID
     * @return List of Rulesets
     * @throws GovernanceException If an error occurs while getting the rulesets
     */
    @Override
    public List<Ruleset> getRulesetsByPolicyId(String policyId)
            throws GovernanceException {
        List<Ruleset> rulesetList = new ArrayList<>();
        String query;
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.GET_RULESETS_BY_POLICY_ID)) {
            prepStmt.setString(1, policyId);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    Ruleset ruleset = new Ruleset();
                    ruleset.setId(rs.getString("RULESET_ID"));
                    ruleset.setName(rs.getString("NAME"));
                    byte[] rulesetContent = rs.getBytes("RULESET_CONTENT");
                    ruleset.setRulesetContent(new String(rulesetContent, StandardCharsets.UTF_8));
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
            throw new GovernanceException(GovernanceExceptionCodes.
                    ERROR_WHILE_RETRIEVING_RULESETS_ASSOCIATED_WITH_POLICY,
                    e, policyId);
        }
    }

    /**
     * Get the list of policies by label
     *
     * @param label        label
     * @param organization organization
     * @return Map of Policy IDs, Policy Names
     * @throws GovernanceException If an error occurs while getting the policies
     */
    @Override
    public Map<String, String> getPoliciesByLabel(String label, String organization)
            throws GovernanceException {
        Map<String, String> policyIds = new HashMap();
        try (Connection connection = GovernanceDBUtil.getConnection();
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
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_RETRIEVING_POLICIES, e,
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
    public List<String> getPoliciesByLabelAndState(String label, GovernableState state, String organization)
            throws GovernanceException {
        List<String> policyIds = new ArrayList<>();
        try (Connection connection = GovernanceDBUtil.getConnection();
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
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_RETRIEVING_POLICIES, e,
                    organization);
        }
    }

    /**
     * Get Policies with Global Label
     *
     * @param organization Organization
     * @return Map of Policy IDs, Policy Names
     */
    public Map<String, String> getPoliciesWithGlobalLabel(String organization)
            throws GovernanceException {
        Map<String, String> policyIds = new HashMap<>();
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.GET_POLICIES_BY_LABEL)) {
            prepStmt.setString(1, GovernanceConstants.GLOBAL_LABEL);
            prepStmt.setString(2, organization);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    policyIds.put(resultSet.getString("POLICY_ID"),
                            resultSet.getString("NAME"));
                }
            }
            return policyIds;
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_RETRIEVING_POLICIES,
                    organization);
        }
    }

    /**
     * Get Policies with Global Label by State
     *
     * @param state        Governable State for the policy
     * @param organization Organization
     * @return List of Policy IDs
     */
    @Override
    public List<String> getPoliciesWithGlobalLabelByState(GovernableState state, String organization)
            throws GovernanceException {
        List<String> policyIds = new ArrayList<>();
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                     .GET_POLICIES_BY_LABEL_AND_STATE)) {
            prepStmt.setString(1, GovernanceConstants.GLOBAL_LABEL);
            prepStmt.setString(2, String.valueOf(state));
            prepStmt.setString(3, organization);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    policyIds.add(resultSet.getString("POLICY_ID"));
                }
            }
            return policyIds;
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_RETRIEVING_POLICIES,
                    organization);
        }
    }

    /**
     * Get the actions of a policy
     *
     * @param policyId Policy ID
     * @return List of Governance Actions
     * @throws GovernanceException If an error occurs while getting the actions
     */
    @Override
    public List<GovernanceAction> getActionsByPolicyId(String policyId) throws GovernanceException {
        List<GovernanceAction> actions = new ArrayList<>();
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.GET_ACTIONS_BY_POLICY_ID)) {
            prepStmt.setString(1, policyId);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    GovernanceAction action = new GovernanceAction();
                    action.setGovernableState(GovernableState.fromString(resultSet
                            .getString("STATE")));
                    action.setRuleSeverity(Severity.fromString(resultSet.getString("SEVERITY")));
                    action.setType(GovernanceActionType.fromString(resultSet.getString("TYPE")));
                    actions.add(action);
                }
            }
            return actions;
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_RETRIEVING_ACTIONS_BY_POLICY_ID,
                    e, policyId);
        }
    }

    /**
     * Get the labels of a policy
     *
     * @param policyId Policy ID
     * @return List of Labels
     * @throws GovernanceException If an error occurs while getting the labels
     */
    @Override
    public List<String> getLabelsByPolicyId(String policyId) throws GovernanceException {
        try (Connection connection = GovernanceDBUtil.getConnection()) {
            return getLabelsByPolicyId(policyId, connection);
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_RETRIEVING_LABELS_BY_POLICY_ID,
                    e, policyId);
        }
    }


    /**
     * Delete a Governance Policy
     *
     * @param policyId Policy ID
     * @throws GovernanceException If an error occurs while deleting the policy
     */
    @Override
    public void deletePolicy(String policyId) throws GovernanceException {
        try (Connection connection = GovernanceDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {

                deleteRuleViolationsForPolicy(policyId, connection);
                deleteComplianceEvaluationResultsForPolicy(policyId, connection);
                deleteComplianceEvaluationRequestsForPolicy(policyId, connection);
                deletePolicyRulesetMappingsForPolicy(policyId, connection);
                deleteActionsMappingsForPolicy(policyId, connection);
                deleteStatesMappingsForPolicy(policyId, connection);
                deleteLabelsMappingsForPolicy(policyId, connection);

                try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                        .DELETE_GOVERNANCE_POLICY)) {
                    prepStmt.setString(1, policyId);
                    int rowsAffected = prepStmt.executeUpdate();
                    if (rowsAffected == 0) {
                        throw new GovernanceException(GovernanceExceptionCodes.POLICY_NOT_FOUND, policyId);
                    }
                }

                connection.commit();
            } catch (SQLException | GovernanceException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_DELETING_POLICY,
                    e, policyId);
        }
    }

    /**
     * Delete all rule violations for a Policy
     *
     * @param policyId   Policy ID
     * @param connection DB Connection
     * @throws GovernanceException If an error occurs while deleting the violations
     */
    private void deleteRuleViolationsForPolicy(String policyId, Connection connection) throws GovernanceException {
        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.DELETE_RULE_VIOLATIONS_BY_POLICY)) {
            prepStmt.setString(1, policyId);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_DELETING_RULE_VIOLATIONS_FOR_POLICY,
                    e, policyId);
        }
    }

    /**
     * Delete Compliance Evaluation Results for a Policy
     *
     * @param policyId   Policy ID
     * @param connection DB Connection
     * @throws GovernanceException If an error occurs while deleting the results
     */
    private void deleteComplianceEvaluationResultsForPolicy(String policyId, Connection connection)
            throws GovernanceException {
        try (PreparedStatement prepStmt = connection.prepareStatement
                (SQLConstants.DELETE_GOV_COMPLIANCE_EVALUATION_RESULT_BY_POLICY)) {
            prepStmt.setString(1, policyId);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes
                    .ERROR_WHILE_DELETING_COMPLIANCE_EVALUATION_RESULTS_FOR_POLICY,
                    e, policyId);
        }
    }

    /**
     * Delete Actions Mappings for a Policy
     *
     * @param policyId   Policy ID
     * @param connection DB Connection
     * @throws GovernanceException If an error occurs while deleting the actions
     */
    private void deleteComplianceEvaluationRequestsForPolicy(String policyId, Connection connection)
            throws GovernanceException {
        try (PreparedStatement prepStmt = connection.prepareStatement
                (SQLConstants.DELETE_GOV_EVALUATION_REQUEST_FOR_POLICY)) {
            prepStmt.setString(1, policyId);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes
                    .ERROR_WHILE_DELETING_COMPLIANCE_EVALUATION_REQUESTS_FOR_POLICY,
                    e, policyId);
        }
    }

    /**
     * Delete Policy Ruleset Mappings
     *
     * @param policyId   Policy ID
     * @param connection DB Connection
     * @throws GovernanceException If an error occurs while deleting the mappings
     */
    private void deletePolicyRulesetMappingsForPolicy(String policyId, Connection connection)
            throws GovernanceException {
        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                .DELETE_POLICY_RULESET_MAPPING_BY_POLICY_ID)) {
            prepStmt.setString(1, policyId);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_DELETING_POLICY_RULESET_MAPPINGS,
                    e, policyId);
        }
    }

    /**
     * Delete Policy Actions Mappings
     *
     * @param policyId   Policy ID
     * @param connection DB Connection
     * @throws GovernanceException If an error occurs while deleting the mappings
     */
    private void deleteActionsMappingsForPolicy(String policyId, Connection connection)
            throws GovernanceException {
        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                .DELETE_GOVERNANCE_POLICY_ACTION_MAPPING_BY_POLICY_ID)) {
            prepStmt.setString(1, policyId);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_DELETING_POLICY_ACTIONS_MAPPINGS,
                    e, policyId);
        }
    }

    /**
     * Delete Policy States Mappings
     *
     * @param policyId   Policy ID
     * @param connection DB Connection
     * @throws GovernanceException If an error occurs while deleting the mappings
     */
    private void deleteStatesMappingsForPolicy(String policyId, Connection connection)
            throws GovernanceException {
        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                .DELETE_GOVERNANCE_POLICY_STATE_MAPPING_BY_POLICY_ID)) {
            prepStmt.setString(1, policyId);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_DELETING_POLICY_STATE_MAPPINGS,
                    e, policyId);
        }
    }

    /**
     * Delete Policy Labels Mappings
     *
     * @param policyId   Policy ID
     * @param connection DB Connection
     * @throws GovernanceException If an error occurs while deleting the mappings
     */
    private void deleteLabelsMappingsForPolicy(String policyId, Connection connection)
            throws GovernanceException {
        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                .DELETE_GOVERNANCE_POLICY_LABEL_MAPPING_BY_POLICY_ID)) {
            prepStmt.setString(1, policyId);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_DELETING_POLICY_LABELS_MAPPINGS,
                    e, policyId);
        }
    }

    /**
     * Search for Governance Policies
     *
     * @param searchCriteria Search criteria
     * @param organization   Organization
     * @return GovernancePolicyList object
     * @throws GovernanceException If an error occurs while searching for policies
     */
    @Override
    public GovernancePolicyList searchPolicies(Map<String, String> searchCriteria,
                                               String organization) throws GovernanceException {
        GovernancePolicyList policyListObj = new GovernancePolicyList();
        List<GovernancePolicy> policyList = new ArrayList<>();
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.SEARCH_POLICIES)) {
            prepStmt.setString(1, organization);
            prepStmt.setString(2, "%" + searchCriteria.getOrDefault(
                    GovernanceConstants.PolicySearchAttributes.NAME, "") + "%");
            prepStmt.setString(3, "%" + searchCriteria.getOrDefault(
                    GovernanceConstants.PolicySearchAttributes.LABEL, "") + "%");
            prepStmt.setString(4, "%" + searchCriteria.getOrDefault(
                    GovernanceConstants.PolicySearchAttributes.STATE, "") + "%");
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    GovernancePolicy policy = new GovernancePolicy();
                    policy.setId(resultSet.getString("POLICY_ID"));
                    policy.setName(resultSet.getString("NAME"));
                    policy.setDescription(resultSet.getString("DESCRIPTION"));
                    policy.setCreatedBy(resultSet.getString("CREATED_BY"));
                    policy.setCreatedTime(resultSet.getString("CREATED_TIME"));
                    policy.setUpdatedBy(resultSet.getString("UPDATED_BY"));
                    policy.setUpdatedTime(resultSet.getString("LAST_UPDATED_TIME"));
                    policy.setRulesetIds(getRulesetsByPolicyId(policy.getId(), connection));
                    policy.setLabels(getLabelsByPolicyId(policy.getId(), connection));
                    policy.setActions(getActionsByPolicyId(policy.getId(), connection));
                    policy.setGovernableStates(getStatesByPolicyId(policy.getId(), connection));
                    policyList.add(policy);
                }
            }
            policyListObj.setCount(policyList.size());
            policyListObj.setGovernancePolicyList(policyList);
            return policyListObj;
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_SEARCHING_POLICIES,
                    e, organization);
        }
    }
}
