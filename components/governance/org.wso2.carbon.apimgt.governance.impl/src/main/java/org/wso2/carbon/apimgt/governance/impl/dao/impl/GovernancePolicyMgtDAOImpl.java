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
import org.wso2.carbon.apimgt.governance.api.model.GovernancePolicyInfo;
import org.wso2.carbon.apimgt.governance.api.model.GovernancePolicyInfoWithRulesetIds;
import org.wso2.carbon.apimgt.governance.api.model.GovernancePolicyList;
import org.wso2.carbon.apimgt.governance.api.model.RulesetId;
import org.wso2.carbon.apimgt.governance.api.model.RulesetInfo;
import org.wso2.carbon.apimgt.governance.impl.dao.GovernancePolicyMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.governance.impl.util.GovernanceDBUtil;
import org.wso2.carbon.apimgt.governance.impl.util.GovernanceUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the GovernancePolicyMgtDAO interface.
 */
public class GovernancePolicyMgtDAOImpl implements GovernancePolicyMgtDAO {

    private static final Log log = LogFactory.getLog(GovernancePolicyMgtDAOImpl.class);
    private static GovernancePolicyMgtDAO INSTANCE = null;

    private GovernancePolicyMgtDAOImpl() {
    }

    /**
     * Get an instance of GovernancePolicyMgtDAO
     *
     * @return GovernancePolicyMgtDAO instance
     */
    public static GovernancePolicyMgtDAO getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GovernancePolicyMgtDAOImpl();
        }
        return INSTANCE;
    }

    /**
     * Create a new Governance Policy
     *
     * @param organization                       Organization
     * @param governancePolicyInfoWithRulesetIds Governance Policy Info with Ruleset Ids
     * @return GovernancePolicyInfo Created object
     */
    @Override
    public GovernancePolicyInfo createGovernancePolicy(String organization, GovernancePolicyInfoWithRulesetIds
            governancePolicyInfoWithRulesetIds) throws GovernanceException {
        List<RulesetInfo> rulesetInfoList = new ArrayList<>();
        List<RulesetId> rulesetIds;
        Timestamp timestamp;
        List<String> labels;
        try (Connection connection = GovernanceDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.CREATE_POLICY)) {
                    prepStmt.setString(1, governancePolicyInfoWithRulesetIds.getId());
                    prepStmt.setString(2, governancePolicyInfoWithRulesetIds.getName());
                    prepStmt.setString(3, governancePolicyInfoWithRulesetIds.getDescription());
                    prepStmt.setString(4, organization);
                    prepStmt.setString(5, governancePolicyInfoWithRulesetIds.getCreatedBy());
                    timestamp = new Timestamp(System.currentTimeMillis());
                    prepStmt.setTimestamp(6, timestamp);
                    prepStmt.execute();
                }
                // Insert into GOV_POLICY_RULESET_MAPPING table
                try (PreparedStatement prepStmt =
                             connection.prepareStatement(SQLConstants.CREATE_POLICY_RULESET_MAPPING)) {
                    rulesetIds = governancePolicyInfoWithRulesetIds.getRulesets();
                    for (RulesetId rulesetId : rulesetIds) {
                        prepStmt.setString(1, GovernanceUtil.generateUUID());
                        prepStmt.setString(2, governancePolicyInfoWithRulesetIds.getId());
                        prepStmt.setString(3, rulesetId.getId());
                        prepStmt.addBatch();
                    }
                    prepStmt.executeBatch();
                }

                // Insert into GOV_POLICY_LABEL table
                try (PreparedStatement prepStmt =
                             connection.prepareStatement(SQLConstants.CREATE_GOVERNANCE_POLICY_LABEL)) {
                    labels = governancePolicyInfoWithRulesetIds.getLabels();
                    for (String label : labels) {
                        prepStmt.setString(1, GovernanceUtil.generateUUID());
                        prepStmt.setString(2, governancePolicyInfoWithRulesetIds.getId());
                        prepStmt.setString(3, label);
                        prepStmt.addBatch();
                    }
                    prepStmt.executeBatch();
                }
                connection.commit();
                // Retrieve RulesetInfo
                String placeholders = String.join(",", Collections.nCopies(rulesetIds.size(), "?"));
                String query = String.format(SQLConstants.GET_RULESETS_BY_IDS, placeholders);
                try (PreparedStatement prepStmt = connection.prepareStatement(query)) {
                    int index = 1;
                    for (RulesetId rulesetId : rulesetIds) {
                        prepStmt.setString(index++, rulesetId.getId());
                    }
                    prepStmt.setString(index, organization);
                    ResultSet resultSet = prepStmt.executeQuery();
                    while (resultSet.next()) {
                        RulesetInfo rulesetInfo = new RulesetInfo();
                        rulesetInfo.setId(resultSet.getString("RULESET_ID"));
                        rulesetInfo.setName(resultSet.getString("NAME"));
                        rulesetInfo.setDescription(resultSet.getString("DESCRIPTION"));
                        rulesetInfo.setAppliesTo(resultSet.getString("APPLIES_TO"));
                        rulesetInfo.setDocumentationLink(resultSet.getString("DOCUMENTATION_LINK"));
                        rulesetInfo.setProvider(resultSet.getString("PROVIDER"));
                        rulesetInfo.setCreatedBy(resultSet.getString("CREATED_BY"));
                        rulesetInfo.setCreatedTime(resultSet.getString("CREATED_TIME"));
                        rulesetInfo.setUpdatedBy(resultSet.getString("UPDATED_BY"));
                        rulesetInfo.setUpdatedTime(resultSet.getString("LAST_UPDATED_TIME"));
                        rulesetInfo.setIsDefault(resultSet.getInt("IS_DEFAULT"));
                        rulesetInfoList.add(rulesetInfo);
                    }
                }
                GovernancePolicyInfo governancePolicyInfo = new GovernancePolicyInfo();
                governancePolicyInfo.setId(governancePolicyInfoWithRulesetIds.getId());
                governancePolicyInfo.setName(governancePolicyInfoWithRulesetIds.getName());
                governancePolicyInfo.setDescription(governancePolicyInfoWithRulesetIds.getDescription());
                governancePolicyInfo.setRulesets(rulesetInfoList);
                governancePolicyInfo.setLabels(labels);
                governancePolicyInfo.setCreatedBy(governancePolicyInfoWithRulesetIds.getCreatedBy());
                governancePolicyInfo.setCreatedTime(timestamp.toString());
                return governancePolicyInfo;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            if (e instanceof SQLIntegrityConstraintViolationException) {
                if (getGovernancePolicyByName(organization, governancePolicyInfoWithRulesetIds.getName()) != null) {
                    throw new GovernanceException(GovernanceExceptionCodes.POLICY_ALREADY_EXISTS, e,
                            governancePolicyInfoWithRulesetIds.getName(), organization);
                }
            }
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_CREATING_POLICY, e, organization);
        }
    }


    /**
     * Get Governance Policy by Name
     *
     * @param organization Organization
     * @param policyName   Policy Name
     * @return GovernancePolicyInfo
     * @throws GovernanceException If an error occurs while retrieving the policy
     */
    @Override
    public GovernancePolicyInfo getGovernancePolicyByName(String organization, String policyName) throws GovernanceException {
        GovernancePolicyInfo policyInfo = null;
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.GET_POLICY_BY_NAME)) {
            prepStmt.setString(1, organization);
            prepStmt.setString(2, policyName);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                if (resultSet.next()) {
                    policyInfo = new GovernancePolicyInfo();
                    policyInfo.setId(resultSet.getString("POLICY_ID"));
                    policyInfo.setName(resultSet.getString("NAME"));
                    policyInfo.setDescription(resultSet.getString("DESCRIPTION"));
                    policyInfo.setCreatedBy(resultSet.getString("CREATED_BY"));
                    policyInfo.setCreatedTime(resultSet.getString("CREATED_TIME"));
                    policyInfo.setUpdatedBy(resultSet.getString("UPDATED_BY"));
                    policyInfo.setUpdatedTime(resultSet.getString("LAST_UPDATED_TIME"));
                }
            }
            return policyInfo;
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_RETRIEVING_POLICY_BY_NAME, e, policyName,
                    organization);
        }
    }

    /**
     * Get Governance Policy by ID
     *
     * @param organization Organization
     * @param policyID     Policy ID
     * @return GovernancePolicyInfo
     * @throws GovernanceException If an error occurs while retrieving the policy
     */
    @Override
    public GovernancePolicyInfo getGovernancePolicyByID(String organization, String policyID) throws GovernanceException {
        GovernancePolicyInfo policyInfo = null;
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.GET_POLICY_BY_ID)) {
            prepStmt.setString(1, organization);
            prepStmt.setString(2, policyID);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                if (resultSet.next()) {
                    policyInfo = new GovernancePolicyInfo();
                    policyInfo.setId(resultSet.getString("POLICY_ID"));
                    policyInfo.setName(resultSet.getString("NAME"));
                    policyInfo.setDescription(resultSet.getString("DESCRIPTION"));
                    policyInfo.setCreatedBy(resultSet.getString("CREATED_BY"));
                    policyInfo.setCreatedTime(resultSet.getString("CREATED_TIME"));
                    policyInfo.setUpdatedBy(resultSet.getString("UPDATED_BY"));
                    policyInfo.setUpdatedTime(resultSet.getString("LAST_UPDATED_TIME"));
                    policyInfo.setRulesets(getRulesetsByPolicyId(policyInfo.getId(), connection));
                    policyInfo.setLabels(getLabelsByPolicyId(policyInfo.getId(), connection));
                }
            }
            return policyInfo;
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_RETRIEVING_POLICY_BY_ID, e, policyID,
                    organization);
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
        GovernancePolicyList policyList = new GovernancePolicyList();
        List<GovernancePolicyInfo> policyInfoList = new ArrayList<>();
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.GET_POLICIES)) {
            prepStmt.setString(1, organization);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    GovernancePolicyInfo policyInfo = new GovernancePolicyInfo();
                    policyInfo.setId(resultSet.getString("POLICY_ID"));
                    policyInfo.setName(resultSet.getString("NAME"));
                    policyInfo.setDescription(resultSet.getString("DESCRIPTION"));
                    policyInfo.setCreatedBy(resultSet.getString("CREATED_BY"));
                    policyInfo.setCreatedTime(resultSet.getString("CREATED_TIME"));
                    policyInfo.setUpdatedBy(resultSet.getString("UPDATED_BY"));
                    policyInfo.setUpdatedTime(resultSet.getString("LAST_UPDATED_TIME"));
                    policyInfo.setRulesets(getRulesetsByPolicyId(policyInfo.getId(), connection));
                    policyInfo.setLabels(getLabelsByPolicyId(policyInfo.getId(), connection));
                    policyInfoList.add(policyInfo);
                }
            }
            policyList.setCount(policyInfoList.size());
            policyList.setGovernancePolicyList(policyInfoList);
            return policyList;
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_RETRIEVING_POLICIES,
                    e, organization);
        }
    }


    /**
     * Delete a Governance Policy
     *
     * @param policyId     Policy ID
     * @param organization Organization
     * @throws GovernanceException If an error occurs while deleting the policy
     */
    @Override
    public void deletePolicy(String policyId, String organization) throws GovernanceException {
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                     .DELETE_GOVERNANCE_POLICY)) {
            connection.setAutoCommit(false);
            try {
                prepStmt.setString(1, policyId);
                prepStmt.setString(2, organization);
                int rowsAffected = prepStmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new GovernanceException(GovernanceExceptionCodes.POLICY_NOT_FOUND,
                            policyId, organization);
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_DELETING_POLICY,
                    e, policyId, organization);
        }
    }

    /**
     * Update a Governance Policy
     *
     * @param policyId                           Policy ID
     * @param organization                       Organization
     * @param governancePolicyInfoWithRulesetIds Governance Policy Info with Ruleset Ids
     * @return GovernancePolicyInfo Updated object
     * @throws GovernanceException If an error occurs while updating the policy
     */
    @Override
    public GovernancePolicyInfo updateGovernancePolicy(String policyId, String organization, GovernancePolicyInfoWithRulesetIds governancePolicyInfoWithRulesetIds) throws GovernanceException {
        try (Connection connection = GovernanceDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                // Update policy details
                try (PreparedStatement updateStatement = connection.prepareStatement(SQLConstants.UPDATE_POLICY)) {
                    updateStatement.setString(1, governancePolicyInfoWithRulesetIds.getName());
                    updateStatement.setString(2, governancePolicyInfoWithRulesetIds.getDescription());
                    updateStatement.setString(3, governancePolicyInfoWithRulesetIds.getUpdatedBy());
                    updateStatement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                    updateStatement.setString(5, policyId);
                    updateStatement.setString(6, organization);
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
                List<String> rulesetsToBeUpdatedList = governancePolicyInfoWithRulesetIds.getRulesets().stream()
                        .map(RulesetId::getId)
                        .collect(Collectors.toList());
                List<String> rulesetsToAdd = new ArrayList<>(rulesetsToBeUpdatedList);
                List<String> rulesetsToRemove = new ArrayList<>(existingRulesets);
                rulesetsToAdd.removeAll(existingRulesets);
                rulesetsToRemove.removeAll(rulesetsToBeUpdatedList);
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
                List<String> labelsToBeUpdated = governancePolicyInfoWithRulesetIds.getLabels();
                List<String> labelsToAdd = new ArrayList<>(labelsToBeUpdated);
                List<String> labelsToRemove = new ArrayList<>(existingLabels);
                labelsToAdd.removeAll(existingLabels);
                labelsToRemove.removeAll(labelsToBeUpdated);
                // Add new labels
                if (!labelsToAdd.isEmpty()) {
                    try (PreparedStatement insertStatement =
                                 connection.prepareStatement(SQLConstants.CREATE_GOVERNANCE_POLICY_LABEL)) {
                        for (String label : labelsToAdd) {
                            insertStatement.setString(1, GovernanceUtil.generateUUID());
                            insertStatement.setString(2, policyId);
                            insertStatement.setString(3, label);
                            insertStatement.addBatch();
                        }
                        insertStatement.executeBatch();
                    }
                }
                // Remove old labels
                if (!labelsToRemove.isEmpty()) {
                    try (PreparedStatement deleteStatement =
                                 connection.prepareStatement(SQLConstants.DELETE_GOVERNANCE_POLICY_LABEL)) {
                        for (String label : labelsToRemove) {
                            deleteStatement.setString(1, policyId);
                            deleteStatement.setString(2, label);
                            deleteStatement.addBatch();
                        }
                        deleteStatement.executeBatch();
                    }
                }
                // Retrieve updated ruleset info
                List<RulesetInfo> rulesetInfoList = new ArrayList<>();
                String placeholders = String.join(",", Collections.nCopies(rulesetsToBeUpdatedList.size(), "?"));
                String query = String.format(SQLConstants.GET_RULESETS_BY_IDS, placeholders);
                try (PreparedStatement getRulesetsInfoStatement = connection.prepareStatement(query)) {
                    int index = 1;
                    for (String rulesetId : rulesetsToBeUpdatedList) {
                        getRulesetsInfoStatement.setString(index++, rulesetId);
                    }
                    getRulesetsInfoStatement.setString(index, organization);
                    try (ResultSet resultSet = getRulesetsInfoStatement.executeQuery()) {
                        while (resultSet.next()) {
                            RulesetInfo rulesetInfo = new RulesetInfo();
                            rulesetInfo.setId(resultSet.getString("RULESET_ID"));
                            rulesetInfo.setName(resultSet.getString("NAME"));
                            rulesetInfo.setDescription(resultSet.getString("DESCRIPTION"));
                            rulesetInfo.setAppliesTo(resultSet.getString("APPLIES_TO"));
                            rulesetInfo.setDocumentationLink(resultSet.getString("DOCUMENTATION_LINK"));
                            rulesetInfo.setProvider(resultSet.getString("PROVIDER"));
                            rulesetInfo.setCreatedBy(resultSet.getString("CREATED_BY"));
                            rulesetInfo.setCreatedTime(resultSet.getString("CREATED_TIME"));
                            rulesetInfo.setUpdatedBy(resultSet.getString("UPDATED_BY"));
                            rulesetInfo.setUpdatedTime(resultSet.getString("LAST_UPDATED_TIME"));
                            rulesetInfo.setIsDefault(resultSet.getInt("IS_DEFAULT"));
                            rulesetInfoList.add(rulesetInfo);
                        }
                    }
                }
                // Create and return GovernancePolicyInfo object
                GovernancePolicyInfo governancePolicyInfo = new GovernancePolicyInfo();
                governancePolicyInfo.setId(policyId);
                governancePolicyInfo.setName(governancePolicyInfoWithRulesetIds.getName());
                governancePolicyInfo.setDescription(governancePolicyInfoWithRulesetIds.getDescription());
                governancePolicyInfo.setRulesets(rulesetInfoList);
                governancePolicyInfo.setLabels(labelsToBeUpdated);
                governancePolicyInfo.setCreatedBy(governancePolicyInfoWithRulesetIds.getCreatedBy());
                governancePolicyInfo.setCreatedTime(governancePolicyInfoWithRulesetIds.getCreatedTime());
                governancePolicyInfo.setUpdatedBy(governancePolicyInfoWithRulesetIds.getUpdatedBy());
                governancePolicyInfo.setUpdatedTime(new Timestamp(System.currentTimeMillis()).toString());
                connection.commit();
                return governancePolicyInfo;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_UPDATING_POLICY, e,
                    policyId, organization);
        }
    }

    /**
     * Get all the Rulesets attached to a Policy
     *
     * @param policyId   Policy ID
     * @param connection DB Connection
     * @return List of RulesetInfo
     * @throws SQLException If an error occurs while retrieving the policies (Captured at higher level)
     */
    private List<RulesetInfo> getRulesetsByPolicyId(String policyId, Connection connection) throws SQLException {
        List<RulesetInfo> rulesetInfoList = new ArrayList<>();
        String sqlQuery = SQLConstants.GET_RULESETS_FOR_POLICY;
        try (PreparedStatement prepStmt = connection.prepareStatement(sqlQuery)) {
            prepStmt.setString(1, policyId);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    RulesetInfo rulesetInfo = new RulesetInfo();
                    rulesetInfo.setId(rs.getString("RULESET_ID"));
                    rulesetInfo.setName(rs.getString("NAME"));
                    rulesetInfo.setDescription(rs.getString("DESCRIPTION"));
                    rulesetInfo.setAppliesTo(rs.getString("APPLIES_TO"));
                    rulesetInfo.setDocumentationLink(rs.getString("DOCUMENTATION_LINK"));
                    rulesetInfo.setProvider(rs.getString("PROVIDER"));
                    rulesetInfo.setCreatedBy(rs.getString("CREATED_BY"));
                    rulesetInfo.setCreatedTime(rs.getString("CREATED_TIME"));
                    rulesetInfo.setUpdatedBy(rs.getString("UPDATED_BY"));
                    rulesetInfo.setUpdatedTime(rs.getString("LAST_UPDATED_TIME"));
                    rulesetInfo.setIsDefault(rs.getInt("IS_DEFAULT"));
                    rulesetInfoList.add(rulesetInfo);
                }
            }
        }
        return rulesetInfoList;
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

}



