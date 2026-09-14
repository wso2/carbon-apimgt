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
import org.apache.commons.lang3.StringUtils;
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
import org.wso2.carbon.apimgt.governance.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceDBUtil;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dto.APIMGovernanceConfigDTO;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        // The configuration alone decides which insert runs, and nothing inspects the schema. With it off the
        // policy is written exactly as it was before the feature existed, by a statement which does not name the
        // optional column; with it on the severity selection rides the same insert, so it lands inside the
        // transaction that creates the policy rather than in a second write that could fail on its own.
        if (!isPerPolicySeverityFilteringEnabled()) {
            return createPolicy(governancePolicy, organization);
        }
        return createPolicyWithSeverities(governancePolicy, organization);
    }

    /**
     * Create a policy without naming the optional compliance affecting severity column
     *
     * @param governancePolicy Governance Policy Info with Ruleset Ids
     * @param organization     Organization
     * @return APIMGovernancePolicy Created object
     * @throws APIMGovernanceException If an error occurs while creating the policy
     */
    private APIMGovernancePolicy createPolicy(APIMGovernancePolicy governancePolicy, String organization)
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
     * Create a policy and its compliance affecting severities with one insert
     * <p>
     * A blank selection means every severity is judged, which the column holds as null. Nothing has probed for the
     * column, so a deployment which enabled the configuration without adding it fails here: the insert is refused,
     * the transaction rolls back and no policy survives, which is reported as a request error naming the missing
     * column rather than as a server fault.
     *
     * @param governancePolicy Governance Policy Info with Ruleset Ids
     * @param organization     Organization
     * @return APIMGovernancePolicy Created object
     * @throws APIMGovernanceException If an error occurs while creating the policy
     */
    private APIMGovernancePolicy createPolicyWithSeverities(APIMGovernancePolicy governancePolicy,
                                                            String organization) throws APIMGovernanceException {
        try (Connection connection = APIMGovernanceDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement prepStmt = connection
                        .prepareStatement(SQLConstants.CREATE_POLICY_WITH_SEVERITIES)) {
                    prepStmt.setString(1, governancePolicy.getId());
                    prepStmt.setString(2, governancePolicy.getName());
                    prepStmt.setString(3, governancePolicy.getDescription());
                    prepStmt.setString(4, organization);
                    prepStmt.setString(5, governancePolicy.getCreatedBy());
                    prepStmt.setInt(6, governancePolicy.isGlobal() ? 1 : 0);

                    Timestamp createdTime = new Timestamp(System.currentTimeMillis());
                    prepStmt.setTimestamp(7, createdTime);
                    governancePolicy.setCreatedTime(createdTime.toString());

                    prepStmt.setString(8, storedSeverities(governancePolicy.getComplianceAffectingSeverities()));

                    prepStmt.execute();
                } catch (SQLException e) {
                    throw new SeverityStatementException(e);
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
            throw severityColumnException(e, APIMGovExceptionCodes.ERROR_WHILE_CREATING_POLICY, organization);
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

        // As with create, the configuration alone decides which update runs. A request which did not send the
        // field is also written by the statement that omits the column, which is what makes an absent field
        // preserve whatever was stored while a blank one clears it.
        if (!isPerPolicySeverityFilteringEnabled() || governancePolicy.getComplianceAffectingSeverities() == null) {
            return updatePolicy(policyId, governancePolicy, organization);
        }
        return updatePolicyWithSeverities(policyId, governancePolicy, organization);
    }

    /**
     * Update a policy without naming the optional compliance affecting severity column
     *
     * @param policyId         Policy ID
     * @param governancePolicy Governance Policy
     * @param organization     Organization
     * @return APIMGovernancePolicy Updated object
     * @throws APIMGovernanceException If an error occurs while updating the policy
     */
    private APIMGovernancePolicy updatePolicy(String policyId, APIMGovernancePolicy governancePolicy,
                                              String organization) throws APIMGovernanceException {
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
     * Update a policy and its compliance affecting severities with one statement
     * <p>
     * A blank selection means every severity is judged, which the column holds as null. The severity is part of the
     * same transaction as the rest of the policy, so a deployment which enabled the configuration without adding
     * the column changes nothing at all: the update is refused and rolled back, and the response names the missing
     * column instead of reporting a server fault.
     *
     * @param policyId         Policy ID
     * @param governancePolicy Governance Policy
     * @param organization     Organization
     * @return APIMGovernancePolicy Updated object
     * @throws APIMGovernanceException If an error occurs while updating the policy
     */
    private APIMGovernancePolicy updatePolicyWithSeverities(String policyId, APIMGovernancePolicy governancePolicy,
                                                            String organization) throws APIMGovernanceException {
        try (Connection connection = APIMGovernanceDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                // Update policy details
                try (PreparedStatement updateStatement = connection
                        .prepareStatement(SQLConstants.UPDATE_POLICY_WITH_SEVERITIES)) {
                    updateStatement.setString(1, governancePolicy.getName());
                    updateStatement.setString(2, governancePolicy.getDescription());
                    updateStatement.setString(3, governancePolicy.getUpdatedBy());
                    updateStatement.setInt(4, governancePolicy.isGlobal() ? 1 : 0);

                    Timestamp updatedTime = new Timestamp(System.currentTimeMillis());
                    updateStatement.setTimestamp(5, updatedTime);
                    governancePolicy.setUpdatedTime(updatedTime.toString());

                    updateStatement.setString(6,
                            storedSeverities(governancePolicy.getComplianceAffectingSeverities()));
                    updateStatement.setString(7, policyId);
                    updateStatement.setString(8, organization);
                    updateStatement.executeUpdate();
                } catch (SQLException e) {
                    throw new SeverityStatementException(e);
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
            throw severityColumnException(e, APIMGovExceptionCodes.ERROR_WHILE_UPDATING_POLICY, policyId);
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
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_POLICY_BY_NAME,
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
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_POLICY_BY_ID,
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
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_POLICIES,
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
                                .ERROR_WHILE_GETTING_RULESET_CONTENT, e, ruleset.getId());
                    }
                    rulesetContent.setFileName(rs.getString("FILE_NAME"));
                    ruleset.setRulesetContent(rulesetContent);

                    rulesetList.add(ruleset);
                }
            }
            return rulesetList;
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.
                    ERROR_WHILE_GETTING_RULESETS_ASSOCIATED_WITH_POLICY, e, policyId);
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
                    .ERROR_WHILE_GETTING_RULESETS_ASSOCIATED_WITH_POLICY, e, policyId);
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
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_POLICIES, e,
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
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_POLICIES, e,
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
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_POLICIES, e,
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
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_POLICIES, e,
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
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_ACTIONS_BY_POLICY_ID,
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
            prepStmt.setString(2, "%" + searchCriteria.getOrDefault(
                    APIMGovernanceConstants.PolicySearchAttributes.NAME, "") + "%");
            prepStmt.setString(3, "%" + searchCriteria.getOrDefault(
                    APIMGovernanceConstants.PolicySearchAttributes.STATE, "") + "%");
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
     * @param label label
     * @throws APIMGovernanceException If an error occurs while deleting the mappings
     */
    @Override
    public void deleteLabelPolicyMappings(String label) throws APIMGovernanceException {
        try (Connection connection = APIMGovernanceDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                        .DELETE_GOVERNANCE_POLICIES_BY_LABEL)) {
                    prepStmt.setString(1, label);
                    prepStmt.executeUpdate();
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes
                    .ERROR_WHILE_DELETING_LABEL_POLICY_MAPPINGS, e, label);
        }
    }

    /**
     * Check whether per policy severity filtering has been enabled in the configuration.
     * <p>
     * This is the only switch. Nothing probes the schema for the optional GOV_POLICY column: enabling the
     * configuration selects the statements which name it, and a deployment which enabled it without adding the
     * column is told so by the statement that fails rather than by a metadata lookup. Adding the column to a
     * running server therefore takes effect on the next request, with no restart and nothing cached.
     *
     * @return True when the configuration allows per policy severity filtering
     */
    static boolean isPerPolicySeverityFilteringEnabled() {

        APIManagerConfigurationService configurationService = ServiceReferenceHolder.getInstance()
                .getAPIMConfigurationService();
        if (configurationService == null || configurationService.getAPIManagerConfiguration() == null) {
            return false;
        }
        APIMGovernanceConfigDTO governanceConfig = configurationService.getAPIManagerConfiguration()
                .getAPIMGovernanceConfigurationDto();
        return governanceConfig != null && governanceConfig.isPerPolicySeverityFilteringEnabled();
    }

    @Override
    public boolean isComplianceAffectingSeverityFilteringEnabled() {

        // Reports the configuration only and deliberately does not touch the database, matching the ruleset side.
        return isPerPolicySeverityFilteringEnabled();
    }

    @Override
    public Map<String, String> getComplianceAffectingSeverities(String organization) throws APIMGovernanceException {

        Map<String, String> severitiesByPolicy = new HashMap<>();
        // With the configuration off no statement naming the optional column is issued at all, so a deployment
        // which has not opted in behaves exactly as it did before the feature existed. An empty map means no
        // policy has narrowed its severities, which resolves to every severity affecting compliance.
        if (!isPerPolicySeverityFilteringEnabled()) {
            return severitiesByPolicy;
        }
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection
                     .prepareStatement(SQLConstants.GET_POLICY_COMPLIANCE_AFFECTING_SEVERITIES_BY_ORGANIZATION)) {
            prepStmnt.setString(1, organization);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                while (resultSet.next()) {
                    String severities =
                            resultSet.getString(SQLConstants.COMPLIANCE_AFFECTING_SEVERITIES_COLUMN);
                    if (severities != null) {
                        severitiesByPolicy.put(resultSet.getString("POLICY_ID"), severities);
                    }
                }
            }
        } catch (SQLException e) {
            // An empty map is the documented answer for the half configured state: no policy has narrowed its
            // severities, so every severity affects compliance. The listing renders rather than failing.
            if (severityColumnMissing(e)) {
                severitiesByPolicy.clear();
                return severitiesByPolicy;
            }
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_POLICIES, e, organization);
        }
        return severitiesByPolicy;
    }

    @Override
    public String getComplianceAffectingSeverities(String policyId, String organization)
            throws APIMGovernanceException {

        if (!isPerPolicySeverityFilteringEnabled()) {
            return null;
        }
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmnt = connection
                     .prepareStatement(SQLConstants.GET_POLICY_COMPLIANCE_AFFECTING_SEVERITIES)) {
            prepStmnt.setString(1, policyId);
            prepStmnt.setString(2, organization);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString(SQLConstants.COMPLIANCE_AFFECTING_SEVERITIES_COLUMN);
                }
            }
        } catch (SQLException e) {
            // Null is what a policy which has not narrowed its severities reads as, and it resolves to every
            // severity affecting compliance, so it is also the right answer while the column is absent.
            if (severityColumnMissing(e)) {
                return null;
            }
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_POLICY_BY_ID, e, policyId);
        }
        return null;
    }

    /**
     * Value to store in the optional column for a requested severity selection
     * <p>
     * Selecting every severity is the same statement as selecting none, and both are held as null rather than as a
     * list of everything, so a policy has one representation of "every severity counts" instead of two.
     *
     * @param requestedSeverities Comma separated severities from the request, blank when every severity counts
     * @return Value to store, null when every severity counts
     */
    private static String storedSeverities(String requestedSeverities) {

        return StringUtils.isBlank(requestedSeverities) ? null : requestedSeverities;
    }

    /**
     * SQLStates which mean the statement named a column the table does not have.
     * <p>
     * PostgreSQL and DB2 report 42703, MySQL and MariaDB 42S22, H2 42122. Oracle reports ORA-00904 under the
     * generic 42000 syntax bucket, which is far too broad to match on, so Oracle is recognised by its code below.
     */
    private static final Set<String> MISSING_COLUMN_SQL_STATES =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList("42703", "42S22", "42122")));

    /**
     * Vendor error codes which mean the statement named a column the table does not have.
     * <p>
     * MySQL 1054, SQL Server 207, DB2 -206, H2 42122, and Oracle ORA-00904, which drivers surface as either sign.
     */
    private static final Set<Integer> MISSING_COLUMN_ERROR_CODES =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList(1054, 207, -206, 42122, 904, -904)));

    /**
     * An identifier as a vendor quotes it when naming the column it rejected.
     * <p>
     * Double quotes for PostgreSQL, Oracle and H2, single for MySQL and SQL Server, backticks and brackets for the
     * delimited forms those two also accept.
     */
    private static final Pattern QUOTED_IDENTIFIER = Pattern.compile("[\"'`\\[]([A-Za-z0-9_$.#@]+)[\"'`\\]]");

    /**
     * The identifier DB2 opens its message with, which it does not quote.
     */
    private static final Pattern LEADING_IDENTIFIER = Pattern.compile("^\\s*([A-Za-z0-9_$.#@]+)");

    /**
     * Where a vendor stops describing the fault and starts repeating the statement it was sent.
     */
    private static final String[] STATEMENT_MARKERS = {"SQL STATEMENT:", "QUERY:", "STATEMENT IS:"};

    /**
     * Report a failed severity aware write, distinguishing a missing column from any other database failure
     * <p>
     * The optional column is not probed for, so "the operator enabled the configuration but has not run the ALTER
     * TABLE" arrives here as an ordinary SQLException. Left alone it would be wrapped as a server error, which is
     * misleading for what is a deployment step the caller can act on, so it is turned into a request error naming
     * the statement to run. Anything else propagates untouched: a deadlock, a lock timeout or a denied permission
     * must never be reported as a missing column, and neither must a missing column reported by any of the other
     * statements in the same transaction, which is why only a {@link SeverityStatementException} is classified.
     *
     * @param e             Failure from the write, classified only when it came from the severity aware statement
     * @param fallbackCode  Code to report when the failure is not a missing column
     * @param fallbackParam Parameter of the fallback code, the policy ID or the organization
     * @return Exception to throw
     */
    private static APIMGovernanceException severityColumnException(SQLException e,
                                                                   APIMGovExceptionCodes fallbackCode,
                                                                   String fallbackParam) {

        // Only the statement naming the optional column can be short of it. The rest of the transaction writes the
        // ruleset, label, state and action tables, whose columns are shipped, so a missing column reported by one of
        // them is a different fault entirely and must not be answered with the ALTER TABLE for this one.
        if (!(e instanceof SeverityStatementException)) {
            return new APIMGovernanceException(fallbackCode, e, fallbackParam);
        }
        SQLException failure = ((SeverityStatementException) e).getStatementFailure();
        if (isMissingSeverityColumn(failure)) {
            logMissingSeverityColumn(failure);
            return new APIMGovernanceException(APIMGovExceptionCodes.PER_POLICY_SEVERITY_FILTERING_UNAVAILABLE, failure,
                    missingSeverityColumnMessage());
        }
        return new APIMGovernanceException(fallbackCode, failure, fallbackParam);
    }

    /**
     * Marks a failure as having come from the one statement which names the optional column
     * <p>
     * Both writes carry the severity on the policy statement itself and then go on to write the ruleset, label,
     * state and action tables in the same transaction, all of which reach the same catch. Without this marker a
     * missing column reported by any of those would be answered with the ALTER TABLE for the severity column,
     * pointing an operator at the wrong table. The original failure is carried untouched and is what the caller
     * reports, so the marker changes which failures are classified and nothing else.
     */
    private static final class SeverityStatementException extends SQLException {

        private static final long serialVersionUID = 1L;

        private final SQLException statementFailure;

        private SeverityStatementException(SQLException statementFailure) {

            super(statementFailure.getMessage(), statementFailure.getSQLState(), statementFailure.getErrorCode(),
                    statementFailure);
            this.statementFailure = statementFailure;
        }

        private SQLException getStatementFailure() {

            return statementFailure;
        }
    }

    /**
     * Decide whether a failed read should fall back to counting every severity, and say so in the log if it should
     * <p>
     * The configuration documents that every severity affects compliance while either half of the feature is
     * missing, so a read which fails because the column is not there has an answer already defined for it: the one
     * it would give if no policy had narrowed its severities. Reads take it rather than failing, which is what
     * keeps the policy listing and the compliance screens working through a rollout that has enabled the
     * configuration but not yet run the ALTER TABLE.
     * <p>
     * Nothing asks the schema whether the column exists. The statement is issued, and only a failure that names a
     * missing column is treated this way; a deadlock, a lock timeout or a denied permission is a real fault and
     * still reaches the caller, because silently counting every severity would hide it.
     *
     * @param e Failure from the read
     * @return True when the read should substitute the every severity answer
     */
    static boolean severityColumnMissing(SQLException e) {

        if (!isMissingSeverityColumn(e)) {
            return false;
        }
        logMissingSeverityColumn(e);
        return true;
    }

    /**
     * Report the half configured state as a configuration problem rather than as a database failure
     * <p>
     * The warning deliberately does not carry the SQLException. A recognised missing column is fully described by
     * the message below, and the driver adds nothing to it but the statement text: every vendor appends the
     * failing SQL to its message, so passing the exception here fills the log with the whole INSERT or SELECT on
     * every request while the state lasts. The exception is still available at debug level, and still travels as
     * the cause of what is thrown.
     *
     * @param e Failure from the statement
     */
    private static void logMissingSeverityColumn(SQLException e) {

        log.warn(missingSeverityColumnMessage());
        if (log.isDebugEnabled()) {
            log.debug("Statement which found the " + SQLConstants.COMPLIANCE_AFFECTING_SEVERITIES_COLUMN
                    + " column missing", e);
        }
    }

    /**
     * The one description of the half configured state, so the write path, the read path and the log cannot drift
     *
     * @return Message naming the missing column and the statement which adds it
     */
    private static String missingSeverityColumnMessage() {

        return "Per policy severity filtering is enabled by "
                + "apim.governance.per_policy_severity_filtering_enabled, but the optional "
                + SQLConstants.COMPLIANCE_AFFECTING_SEVERITIES_COLUMN + " column is missing from "
                + SQLConstants.GOV_POLICY_TABLE + ". Add it with: ALTER TABLE "
                + SQLConstants.GOV_POLICY_TABLE + " ADD "
                + SQLConstants.COMPLIANCE_AFFECTING_SEVERITIES_COLUMN + " VARCHAR(64), or set the configuration "
                + "back to false";
    }

    /**
     * Decide whether a failure is the database refusing a column the table does not have
     * <p>
     * Both the chained SQLExceptions a driver may attach and the cause chain a pool may wrap the failure in are
     * walked, because the statement is executed through a pooled connection and the informative exception is not
     * always the outermost one.
     *
     * @param e Failure from the write
     * @return True when the failure names a missing column
     */
    private static boolean isMissingSeverityColumn(SQLException e) {

        for (Throwable current = e; current != null; current = current.getCause()) {
            if (!(current instanceof SQLException)) {
                continue;
            }
            for (SQLException chained = (SQLException) current; chained != null;
                 chained = chained.getNextException()) {
                if ((MISSING_COLUMN_SQL_STATES.contains(chained.getSQLState())
                        || MISSING_COLUMN_ERROR_CODES.contains(chained.getErrorCode()))
                        && namesTheSeverityColumn(chained)) {
                    return true;
                }
                if (chained == chained.getNextException()) {
                    break;
                }
            }
        }
        return false;
    }

    /**
     * Whether the database named the optional column as the identifier it rejected
     * <p>
     * The state and the code say only that some column was not found, and the statements which name the optional
     * one name a good many other columns beside it. The compliance queries are the sharp case: they join the
     * policy, ruleset, violation and rule tables, so a column missing from any of those reports the same state,
     * and treating that as the optional column being absent would answer with the severity unaware query and
     * return a compliance verdict computed from a broken schema. Which column was rejected is therefore part of
     * the question, not a detail.
     * <p>
     * It cannot be answered by searching the whole message, because vendors append the failing statement to it
     * and that statement names the optional column every time. H2 reports a missing GRULE.SEVERITY as
     * {@code Column "GRULE.SEVERITY" not found; SQL statement: SELECT ... GP.COMPLIANCE_AFFECTING_SEVERITIES ...},
     * which a substring search reads as the optional column being the missing one. Only the part before the
     * appended statement describes what was rejected, and within it only the quoted identifier is the subject.
     *
     * @param e Failure from the statement
     * @return True when the rejected identifier is the optional column
     */
    private static boolean namesTheSeverityColumn(SQLException e) {

        String reported = rejectedIdentifierPart(e.getMessage());
        if (reported == null) {
            return false;
        }
        Matcher identifiers = QUOTED_IDENTIFIER.matcher(reported);
        while (identifiers.find()) {
            if (isSeverityColumn(identifiers.group(1))) {
                return true;
            }
        }
        // DB2 opens with the identifier unquoted, so there is nothing for the pattern above to find
        Matcher leading = LEADING_IDENTIFIER.matcher(reported);
        return leading.find() && isSeverityColumn(leading.group(1));
    }

    /**
     * The part of a failure message which describes what the database rejected
     * <p>
     * Everything from the appended statement onwards is the SQL that was sent, not a description of the fault, and
     * it names every column the statement used.
     *
     * @param message Message from the failure, which may be null
     * @return The describing part, or null when there is no message
     */
    private static String rejectedIdentifierPart(String message) {

        if (message == null) {
            return null;
        }
        String upper = message.toUpperCase(Locale.ENGLISH);
        int end = message.length();
        for (String marker : STATEMENT_MARKERS) {
            int at = upper.indexOf(marker);
            if (at >= 0 && at < end) {
                end = at;
            }
        }
        return message.substring(0, end);
    }

    /**
     * Whether an identifier from a failure message is the optional column, with or without a table qualifier
     *
     * @param identifier Identifier the database quoted
     * @return True when it is the optional column
     */
    private static boolean isSeverityColumn(String identifier) {

        if (identifier == null) {
            return false;
        }
        String name = identifier.trim();
        int qualifier = name.lastIndexOf('.');
        if (qualifier >= 0) {
            name = name.substring(qualifier + 1);
        }
        return SQLConstants.COMPLIANCE_AFFECTING_SEVERITIES_COLUMN.equalsIgnoreCase(name);
    }
}
