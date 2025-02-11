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
import org.wso2.carbon.apimgt.governance.api.model.ExtendedArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.Rule;
import org.wso2.carbon.apimgt.governance.api.model.PolicyCategory;
import org.wso2.carbon.apimgt.governance.api.model.RuleSeverity;
import org.wso2.carbon.apimgt.governance.api.model.PolicyType;
import org.wso2.carbon.apimgt.governance.api.model.Policy;
import org.wso2.carbon.apimgt.governance.api.model.PolicyContent;
import org.wso2.carbon.apimgt.governance.api.model.PolicyInfo;
import org.wso2.carbon.apimgt.governance.api.model.PolicyList;
import org.wso2.carbon.apimgt.governance.impl.APIMGovernanceConstants;
import org.wso2.carbon.apimgt.governance.impl.dao.PolicyMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceDBUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the RulesetMgtDAO interface.
 */
public class PolicyMgtDAOImpl implements PolicyMgtDAO {

    private static final Log log = LogFactory.getLog(PolicyMgtDAOImpl.class);

    private PolicyMgtDAOImpl() {
    }

    private static class SingletonHelper {
        private static final PolicyMgtDAO INSTANCE = new PolicyMgtDAOImpl();
    }

    /**
     * Get the instance of the RulesetMgtDAOImpl
     *
     * @return RulesetMgtDAOImpl instance
     */
    public static PolicyMgtDAO getInstance() {

        return SingletonHelper.INSTANCE;
    }

    /**
     * Create a new Governance Ruleset
     *
     * @param policy      Ruleset object
     * @param rules        List of rules
     * @param organization Organization
     * @return RulesetInfo Created object
     * @throws APIMGovernanceException If an error occurs while creating the policy
     */
    @Override
    public PolicyInfo createPolicy(Policy policy, List<Rule> rules,
                                   String organization) throws APIMGovernanceException {

        String sqlQuery = SQLConstants.CREATE_POLICY;
        try (Connection connection = APIMGovernanceDBUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                try (PreparedStatement prepStmt = connection.prepareStatement(sqlQuery)) {
                    prepStmt.setString(1, policy.getId());
                    prepStmt.setString(2, policy.getName());
                    prepStmt.setString(3, policy.getDescription());
                    prepStmt.setString(4, String.valueOf(policy.getPolicyCategory()));
                    prepStmt.setString(5, String.valueOf(policy.getPolicyType()));
                    prepStmt.setString(6, String.valueOf(policy.getArtifactType()));
                    prepStmt.setString(7, policy.getDocumentationLink());
                    prepStmt.setString(8, policy.getProvider());
                    prepStmt.setString(9, organization);
                    prepStmt.setString(10, policy.getCreatedBy());
                    prepStmt.execute();
                }
                addRuleContent(connection, policy.getId(), policy.getPolicyContent());
                addRules(policy.getId(), rules, connection);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException | IOException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.POLICY_CREATION_FAILED, e,
                    policy.getName(), organization
            );
        }
        return getPolicyById(policy.getId(), organization); // to return RulesetInfo object
    }

    /**
     * Update a Governance Ruleset
     *
     * @param rulesetId    Ruleset ID
     * @param policy      Ruleset object
     * @param rules        List of rules
     * @param organization Organization
     * @return RulesetInfo Created object
     * @throws APIMGovernanceException If an error occurs while updating the policy
     */
    @Override
    public PolicyInfo updatePolicy(String rulesetId, Policy policy, List<Rule> rules,
                                   String organization)
            throws APIMGovernanceException {

        try (Connection connection = APIMGovernanceDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.UPDATE_POLICY)) {
                    prepStmt.setString(1, policy.getName());
                    prepStmt.setString(2, policy.getDescription());
                    prepStmt.setString(3, String.valueOf(policy.getPolicyCategory()));
                    prepStmt.setString(4, String.valueOf(policy.getPolicyType()));
                    prepStmt.setString(5, String.valueOf(policy.getArtifactType()));
                    prepStmt.setString(6, policy.getDocumentationLink());
                    prepStmt.setString(7, policy.getProvider());
                    prepStmt.setString(8, policy.getUpdatedBy());
                    prepStmt.setString(9, rulesetId);
                    prepStmt.setString(10, organization);
                    prepStmt.executeUpdate();
                }
                updateRuleContent(connection, policy.getId(), policy.getPolicyContent());

                // Delete existing rules and rule evaluation results related to this policy
                deleteRuleViolationsForRuleset(connection, rulesetId);
                deleteRulesetResultsForRuleset(connection, rulesetId);
                deleteRules(connection, rulesetId);

                // Insert updated rules to the database
                addRules(policy.getId(), rules, connection);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException | IOException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_UPDATING_POLICY, e, rulesetId);
        }
        return getPolicyById(rulesetId, organization); // to return all info of the updated policy
    }

    /**
     * Add rules in a policy to DB
     *
     * @param rulesetId  Ruleset ID
     * @param rules      List of rules
     * @param connection Database connection
     * @throws SQLException If an error occurs while adding the rules
     */
    private void addRules(String rulesetId, List<Rule> rules, Connection connection)
            throws SQLException {
        String sqlQuery = SQLConstants.ADD_RULES;
        try (PreparedStatement prepStmt = connection.prepareStatement(sqlQuery);) {
            for (Rule rule : rules) {
                prepStmt.setString(1, rule.getId());
                prepStmt.setString(2, rulesetId);
                prepStmt.setString(3, rule.getName());
                prepStmt.setString(4, rule.getDescription());
                prepStmt.setString(5, String.valueOf(rule.getSeverity()));
                prepStmt.setBlob(6, new ByteArrayInputStream(rule.getContent()
                        .getBytes(Charset.defaultCharset())));
                prepStmt.addBatch();
            }
            prepStmt.executeBatch();
        }

    }

    /**
     * Add the content of a Governance Ruleset
     *
     * @param connection     Database connection
     * @param rulesetId      Ruleset ID
     * @param policyContent Ruleset content
     * @throws SQLException If an error occurs while adding the policy content
     * @throws IOException  If an error occurs while adding the policy content
     */
    private void addRuleContent(Connection connection, String rulesetId, PolicyContent policyContent)
            throws SQLException, IOException {
        String sqlQuery = SQLConstants.ADD_POLICY_CONTENT;
        try (PreparedStatement prepStmt = connection.prepareStatement(sqlQuery);
             InputStream rulesetContentStream = new ByteArrayInputStream(policyContent.getContent())) {
            prepStmt.setString(1, rulesetId);
            prepStmt.setBlob(2, rulesetContentStream);
            prepStmt.setString(3, policyContent.getContentType().toString());
            prepStmt.setString(4, policyContent.getFileName());
            prepStmt.execute();
        }
    }

    /**
     * Update the content of a Governance Ruleset
     *
     * @param connection     Database connection
     * @param rulesetId      Ruleset ID
     * @param policyContent Ruleset content
     * @throws SQLException If an error occurs while updating the policy content
     * @throws IOException  If an error occurs while updating the policy content
     */
    private void updateRuleContent(Connection connection, String rulesetId, PolicyContent policyContent)
            throws SQLException, IOException {
        String sqlQuery = SQLConstants.UPDATE_POLICY_CONTENT;
        try (PreparedStatement prepStmt = connection.prepareStatement(sqlQuery);
             InputStream rulesetContentStream = new ByteArrayInputStream(policyContent.getContent())) {
            prepStmt.setBlob(1, rulesetContentStream);
            prepStmt.setString(2, policyContent.getContentType().toString());
            prepStmt.setString(3, policyContent.getFileName());
            prepStmt.setString(4, rulesetId);
            prepStmt.execute();
        }
    }

    /**
     * Delete a Governance Ruleset
     *
     * @param rulesetId    Ruleset ID
     * @param organization Organization
     * @throws APIMGovernanceException If an error occurs while deleting the policy
     */
    @Override
    public void deletePolicy(String rulesetId, String organization) throws APIMGovernanceException {

        try (Connection connection = APIMGovernanceDBUtil.getConnection()) {

            connection.setAutoCommit(false);
            try {
                deleteRuleViolationsForRuleset(connection, rulesetId);
                deleteRulesetResultsForRuleset(connection, rulesetId);
                deleteRulesetContent(connection, rulesetId);
                deleteRules(connection, rulesetId);

                try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.DELETE_POLICY)) {
                    prepStmt.setString(1, rulesetId);
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
                    e, rulesetId);
        }
    }

    /**
     * Delete the content of a Governance Ruleset
     *
     * @param connection Database connection
     * @param rulesetId  Ruleset ID
     * @throws SQLException If an error occurs while deleting the policy content
     */
    private void deleteRulesetContent(Connection connection, String rulesetId) throws SQLException {
        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.DELETE_POLICY_CONTENT)) {
            prepStmt.setString(1, rulesetId);
            prepStmt.executeUpdate();
        }
    }

    /**
     * Delete rules related to a policy
     *
     * @param connection Database connection
     * @param rulesetId  Ruleset ID
     * @throws SQLException If an error occurs while deleting the rules
     */
    private void deleteRules(Connection connection, String rulesetId) throws SQLException {
        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.DELETE_RULES)) {
            prepStmt.setString(1, rulesetId);
            prepStmt.executeUpdate();
        }
    }

    /**
     * Delete rule violations related to a policy
     *
     * @param connection Database connection
     * @param rulesetId  Ruleset ID
     * @throws SQLException If an error occurs while checking the association
     */
    private void deleteRuleViolationsForRuleset(Connection connection, String rulesetId)
            throws SQLException {
        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                .DELETE_RULE_VIOLATIONS_FOR_POLICY)) {
            prepStmt.setString(1, rulesetId);
            prepStmt.executeUpdate();
        }
    }

    /**
     * Delete policy results related to a policy
     *
     * @param connection Database connection
     * @param rulesetId  Ruleset ID
     * @throws SQLException If an error occurs while deleting
     */
    private void deleteRulesetResultsForRuleset(Connection connection, String rulesetId)
            throws SQLException {
        try (PreparedStatement prepStmt = connection.
                prepareStatement(SQLConstants.DELETE_POLICY_RUN_FOR_POLICY)) {
            prepStmt.setString(1, rulesetId);
            prepStmt.executeUpdate();
        }
    }

    /**
     * Retrieves policies in the organization.
     *
     * @param organization Organization whose policies are to be retrieved
     * @return a list of policies associated with the organization
     * @throws APIMGovernanceException if there is an error retrieving the policies
     */
    @Override
    public PolicyList getPolicies(String organization) throws APIMGovernanceException {
        PolicyList policyList = new PolicyList();
        List<PolicyInfo> policyInfoList = new ArrayList<>();
        String sqlQuery = SQLConstants.GET_POLICIES;
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(sqlQuery)) {
            prepStmt.setString(1, organization);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    policyInfoList.add(getRulesetInfoFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_RETRIEVING_POLICIES,
                    e, organization);
        }
        policyList.setCount(policyInfoList.size());
        policyList.setPolicyList(policyInfoList);
        return policyList;
    }

    /**
     * Retrieves a policy by name.
     *
     * @param name         Name of the policy
     * @param organization Organization whose policy is to be retrieved
     * @return the policy with the given name
     * @throws APIMGovernanceException if there is an error retrieving the policy
     */
    @Override
    public PolicyInfo getPolicyByName(String name, String organization) throws APIMGovernanceException {
        String sqlQuery = SQLConstants.GET_POLICY_BY_NAME;
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(sqlQuery)) {
            prepStmt.setString(1, name);
            prepStmt.setString(2, organization);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    return getRulesetInfoFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_RETRIEVING_POLICY_BY_NAME,
                    e, organization);
        }
        return null;
    }

    /**
     * Retrieves a policy by ID.
     *
     * @param rulesetId    Ruleset ID of the policy
     * @param organization Organization whose policy is to be retrieved
     * @return the policy with the given ID
     * @throws APIMGovernanceException if there is an error retrieving the policy
     */
    @Override
    public PolicyInfo getPolicyById(String rulesetId, String organization) throws APIMGovernanceException {
        String sqlQuery = SQLConstants.GET_POLICIES_BY_ID;
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(sqlQuery)) {
            prepStmt.setString(1, rulesetId);
            prepStmt.setString(2, organization);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    return getRulesetInfoFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_RETRIEVING_POLICY_BY_ID,
                    e);
        }
        return null;
    }

    /**
     * Search for Governance Rulesets based on the search criteria
     *
     * @param searchCriteria Search attributes
     * @param organization   Organization
     * @return List of RulesetInfo objects
     * @throws APIMGovernanceException If an error occurs while searching for policies
     */
    @Override
    public PolicyList searchPolicies(Map<String, String> searchCriteria, String organization)
            throws APIMGovernanceException {
        PolicyList policyList = new PolicyList();
        List<PolicyInfo> policyInfoList = new ArrayList<>();

        String sqlQuery = SQLConstants.SEARCH_POLICIES;
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(sqlQuery)) {
            prepStmt.setString(1, organization);
            prepStmt.setString(2, searchCriteria
                    .getOrDefault(APIMGovernanceConstants.PolicySearchAttributes.NAME, ""));
            prepStmt.setString(3, searchCriteria
                    .getOrDefault(APIMGovernanceConstants.PolicySearchAttributes.POLICY_TYPE, ""));
            prepStmt.setString(4, searchCriteria
                    .getOrDefault(APIMGovernanceConstants.PolicySearchAttributes.ARTIFACT_TYPE, ""));
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    policyInfoList.add(getRulesetInfoFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_SEARCHING_POLICIES,
                    e, organization);
        }
        policyList.setCount(policyInfoList.size());
        policyList.setPolicyList(policyInfoList);
        return policyList;
    }

    /**
     * Retrieves rulesetInfo object from the result set
     *
     * @param rs ResultSet
     * @return RulesetInfo object
     * @throws SQLException If an error occurs while retrieving the policy
     */
    private PolicyInfo getRulesetInfoFromResultSet(ResultSet rs) throws SQLException {
        PolicyInfo policyInfo = new PolicyInfo();
        policyInfo.setId(rs.getString("POLICY_ID"));
        policyInfo.setName(rs.getString("NAME"));
        policyInfo.setDescription(rs.getString("DESCRIPTION"));
        policyInfo.setPolicyCategory(PolicyCategory.fromString(
                rs.getString("RULE_CATEGORY")));
        policyInfo.setPolicyType(PolicyType.fromString(rs.getString("RULE_TYPE")));
        policyInfo.setArtifactType(ExtendedArtifactType.fromString(
                rs.getString("ARTIFACT_TYPE")));
        policyInfo.setDocumentationLink(rs.getString("DOCUMENTATION_LINK"));
        policyInfo.setProvider(rs.getString("PROVIDER"));
        policyInfo.setCreatedBy(rs.getString("CREATED_BY"));
        policyInfo.setCreatedTime(rs.getString("CREATED_TIME"));
        policyInfo.setUpdatedBy(rs.getString("UPDATED_BY"));
        policyInfo.setUpdatedTime(rs.getString("LAST_UPDATED_TIME"));
        return policyInfo;
    }

    /**
     * Get the content of a Governance Ruleset
     *
     * @param rulesetId    Ruleset ID
     * @param organization Organization
     * @return String Content of the policy
     * @throws APIMGovernanceException If an error occurs while getting the policy content
     */
    @Override
    public PolicyContent getPolicyContent(String rulesetId, String organization) throws APIMGovernanceException {
        String sqlQuery = SQLConstants.GET_POLICY_CONTENT;
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(sqlQuery);) {
            prepStmt.setString(1, rulesetId);
            prepStmt.setString(2, organization);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    PolicyContent policyContentObj = new PolicyContent();
                    policyContentObj.setFileName(rs.getString("FILE_NAME"));
                    policyContentObj.setContent(rs.getBytes("CONTENT"));
                    return policyContentObj;
                }
                return null;
            }
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_RETRIEVING_POLICY_BY_ID,
                    e);
        }
    }

    /**
     * Get the associated policies for a Ruleset
     *
     * @param rulesetId    Ruleset ID
     * @param organization Organization
     * @return List of associated policies
     */
    @Override
    public List<String> getAssociatedPolicyAttachmentForPolicy(String rulesetId, String organization)
            throws APIMGovernanceException {
        List<String> policyIds = new ArrayList<>();
        String sqlQuery = SQLConstants.GET_POLICY_ATTACHMENT_FOR_POLICY;
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(sqlQuery)) {
            prepStmt.setString(1, rulesetId);
            prepStmt.setString(2, organization);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    policyIds.add(rs.getString("POLICY_ID"));
                }
            }
        } catch (SQLException e) {
            throw new APIMGovernanceException(
                    APIMGovExceptionCodes.ERROR_WHILE_RETRIEVING_ASSOCIATED_POLICY_ATTACHMENTS, e, rulesetId);
        }
        return policyIds;
    }

    /**
     * Get the rules of a Ruleset (without the content)
     *
     * @param rulesetId    Ruleset ID
     * @param organization Organization
     * @return List of rules
     */
    @Override
    public List<Rule> getPolicyByPolicyId(String rulesetId, String organization) throws APIMGovernanceException {
        List<Rule> rules = new ArrayList<>();
        String sqlQuery = SQLConstants.GET_RULES_WITHOUT_CONTENT;
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(sqlQuery)) {
            prepStmt.setString(1, rulesetId);
            prepStmt.setString(2, organization);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    Rule rule = new Rule();
                    rule.setId(rs.getString("POLICY_RULE_ID"));
                    rule.setName(rs.getString("RULE_NAME"));
                    rule.setDescription(rs.getString("RULE_DESCRIPTION"));
                    rule.setSeverity(RuleSeverity.fromString(rs.getString("SEVERITY")));
                    rules.add(rule);
                }
            }
            return rules;
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_RETRIEVING_RULES_BY_POLICY_ID
                    , e, rulesetId);
        }
    }
}

