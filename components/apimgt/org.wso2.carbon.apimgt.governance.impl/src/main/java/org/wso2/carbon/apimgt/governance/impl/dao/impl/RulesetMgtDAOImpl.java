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
import org.wso2.carbon.apimgt.governance.api.model.ExtendedArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.Rule;
import org.wso2.carbon.apimgt.governance.api.model.RuleCategory;
import org.wso2.carbon.apimgt.governance.api.model.RuleSeverity;
import org.wso2.carbon.apimgt.governance.api.model.RuleType;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.api.model.RulesetContent;
import org.wso2.carbon.apimgt.governance.api.model.RulesetInfo;
import org.wso2.carbon.apimgt.governance.api.model.RulesetList;
import org.wso2.carbon.apimgt.governance.impl.APIMGovernanceConstants;
import org.wso2.carbon.apimgt.governance.impl.dao.RulesetMgtDAO;
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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the RulesetMgtDAO interface.
 */
public class RulesetMgtDAOImpl implements RulesetMgtDAO {

    private static final Log log = LogFactory.getLog(RulesetMgtDAOImpl.class);

    private RulesetMgtDAOImpl() {
    }

    private static class SingletonHelper {
        private static final RulesetMgtDAO INSTANCE = new RulesetMgtDAOImpl();
    }

    /**
     * Get the instance of the RulesetMgtDAOImpl
     *
     * @return RulesetMgtDAOImpl instance
     */
    public static RulesetMgtDAO getInstance() {

        return SingletonHelper.INSTANCE;
    }

    /**
     * Create a new Governance Ruleset
     *
     * @param ruleset      Ruleset object
     * @param rules        List of rules
     * @param organization Organization
     * @return RulesetInfo Created object
     * @throws APIMGovernanceException If an error occurs while creating the ruleset
     */
    @Override
    public RulesetInfo createRuleset(Ruleset ruleset, List<Rule> rules,
                                     String organization) throws APIMGovernanceException {

        String sqlQuery = SQLConstants.CREATE_RULESET;
        try (Connection connection = APIMGovernanceDBUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                try (PreparedStatement prepStmt = connection.prepareStatement(sqlQuery)) {
                    prepStmt.setString(1, ruleset.getId());
                    prepStmt.setString(2, ruleset.getName());
                    prepStmt.setString(3, ruleset.getDescription());
                    prepStmt.setString(4, String.valueOf(ruleset.getRuleCategory()));
                    prepStmt.setString(5, String.valueOf(ruleset.getRuleType()));
                    prepStmt.setString(6, String.valueOf(ruleset.getArtifactType()));
                    prepStmt.setString(7, ruleset.getDocumentationLink());
                    prepStmt.setString(8, ruleset.getProvider());
                    prepStmt.setString(9, organization);
                    prepStmt.setString(10, ruleset.getCreatedBy());

                    Timestamp createdTime = new Timestamp(System.currentTimeMillis());
                    prepStmt.setTimestamp(11, createdTime);
                    ruleset.setCreatedTime(createdTime.toString());

                    prepStmt.execute();
                }
                addRuleContent(connection, ruleset.getId(), ruleset.getRulesetContent());
                addRules(ruleset.getId(), rules, connection);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException | IOException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.RULESET_CREATION_FAILED, e,
                    ruleset.getName(), organization
            );
        }
        return getRulesetInfoFromRuleset(ruleset);
    }

    /**
     * Update a Governance Ruleset
     *
     * @param rulesetId    Ruleset ID
     * @param ruleset      Ruleset object
     * @param rules        List of rules
     * @param organization Organization
     * @return RulesetInfo Created object
     * @throws APIMGovernanceException If an error occurs while updating the ruleset
     */
    @Override
    public RulesetInfo updateRuleset(String rulesetId, Ruleset ruleset, List<Rule> rules,
                                     String organization)
            throws APIMGovernanceException {

        try (Connection connection = APIMGovernanceDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.UPDATE_RULESET)) {
                    prepStmt.setString(1, ruleset.getName());
                    prepStmt.setString(2, ruleset.getDescription());
                    prepStmt.setString(3, String.valueOf(ruleset.getRuleCategory()));
                    prepStmt.setString(4, String.valueOf(ruleset.getRuleType()));
                    prepStmt.setString(5, String.valueOf(ruleset.getArtifactType()));
                    prepStmt.setString(6, ruleset.getDocumentationLink());
                    prepStmt.setString(7, ruleset.getProvider());
                    prepStmt.setString(8, ruleset.getUpdatedBy());

                    Timestamp updatedTime = new Timestamp(System.currentTimeMillis());
                    prepStmt.setTimestamp(9, updatedTime);
                    ruleset.setUpdatedTime(updatedTime.toString());

                    prepStmt.setString(10, rulesetId);
                    prepStmt.setString(11, organization);
                    prepStmt.executeUpdate();
                }
                updateRuleContent(connection, ruleset.getId(), ruleset.getRulesetContent());

                // Delete existing rules and rule evaluation results related to this ruleset
                deleteRuleViolationsForRuleset(connection, rulesetId);
                deleteRulesetResultsForRuleset(connection, rulesetId);
                deleteRules(connection, rulesetId);

                // Insert updated rules to the database
                addRules(ruleset.getId(), rules, connection);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException | IOException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_UPDATING_RULESET, e, rulesetId);
        }
        return getRulesetInfoFromRuleset(ruleset);
    }

    /**
     * Get the RulesetInfo object from a Ruleset object
     *
     * @param ruleset Ruleset object
     * @return RulesetInfo object
     */
    private RulesetInfo getRulesetInfoFromRuleset(Ruleset ruleset) {
        RulesetInfo rulesetInfo = new RulesetInfo();
        rulesetInfo.setId(ruleset.getId());
        rulesetInfo.setName(ruleset.getName());
        rulesetInfo.setDescription(ruleset.getDescription());
        rulesetInfo.setRuleCategory(ruleset.getRuleCategory());
        rulesetInfo.setRuleType(ruleset.getRuleType());
        rulesetInfo.setArtifactType(ruleset.getArtifactType());
        rulesetInfo.setDocumentationLink(ruleset.getDocumentationLink());
        rulesetInfo.setProvider(ruleset.getProvider());
        rulesetInfo.setCreatedBy(ruleset.getCreatedBy());
        rulesetInfo.setCreatedTime(ruleset.getCreatedTime());
        rulesetInfo.setUpdatedBy(ruleset.getUpdatedBy());
        rulesetInfo.setUpdatedTime(ruleset.getUpdatedTime());
        return rulesetInfo;
    }

    /**
     * Add rules in a ruleset to DB
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
                prepStmt.setBinaryStream(6, new ByteArrayInputStream(rule.getContent()
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
     * @param rulesetContent Ruleset content
     * @throws SQLException If an error occurs while adding the ruleset content
     * @throws IOException  If an error occurs while adding the ruleset content
     */
    private void addRuleContent(Connection connection, String rulesetId, RulesetContent rulesetContent)
            throws SQLException, IOException {
        String sqlQuery = SQLConstants.ADD_RULESET_CONTENT;
        try (PreparedStatement prepStmt = connection.prepareStatement(sqlQuery);
             InputStream rulesetContentStream = new ByteArrayInputStream(rulesetContent.getContent())) {
            prepStmt.setString(1, rulesetId);
            prepStmt.setBinaryStream(2, rulesetContentStream);
            prepStmt.setString(3, rulesetContent.getContentType().toString());
            prepStmt.setString(4, rulesetContent.getFileName());
            prepStmt.execute();
        }
    }

    /**
     * Update the content of a Governance Ruleset
     *
     * @param connection     Database connection
     * @param rulesetId      Ruleset ID
     * @param rulesetContent Ruleset content
     * @throws SQLException If an error occurs while updating the ruleset content
     * @throws IOException  If an error occurs while updating the ruleset content
     */
    private void updateRuleContent(Connection connection, String rulesetId, RulesetContent rulesetContent)
            throws SQLException, IOException {
        String sqlQuery = SQLConstants.UPDATE_RULESET_CONTENT;
        try (PreparedStatement prepStmt = connection.prepareStatement(sqlQuery);
             InputStream rulesetContentStream = new ByteArrayInputStream(rulesetContent.getContent())) {
            prepStmt.setBinaryStream(1, rulesetContentStream);
            prepStmt.setString(2, rulesetContent.getContentType().toString());
            prepStmt.setString(3, rulesetContent.getFileName());
            prepStmt.setString(4, rulesetId);
            prepStmt.execute();
        }
    }

    /**
     * Delete a Governance Ruleset
     *
     * @param rulesetId    Ruleset ID
     * @param organization Organization
     * @throws APIMGovernanceException If an error occurs while deleting the ruleset
     */
    @Override
    public void deleteRuleset(String rulesetId, String organization) throws APIMGovernanceException {

        try (Connection connection = APIMGovernanceDBUtil.getConnection()) {

            connection.setAutoCommit(false);
            try {
                deleteRuleViolationsForRuleset(connection, rulesetId);
                deleteRulesetResultsForRuleset(connection, rulesetId);
                deleteRulesetContent(connection, rulesetId);
                deleteRules(connection, rulesetId);

                try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.DELETE_RULESET)) {
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
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_DELETING_RULESET,
                    e, rulesetId);
        }
    }

    /**
     * Delete the content of a Governance Ruleset
     *
     * @param connection Database connection
     * @param rulesetId  Ruleset ID
     * @throws SQLException If an error occurs while deleting the ruleset content
     */
    private void deleteRulesetContent(Connection connection, String rulesetId) throws SQLException {
        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.DELETE_RULESET_CONTENT)) {
            prepStmt.setString(1, rulesetId);
            prepStmt.executeUpdate();
        }
    }

    /**
     * Delete rules related to a ruleset
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
     * Delete rule violations related to a ruleset
     *
     * @param connection Database connection
     * @param rulesetId  Ruleset ID
     * @throws SQLException If an error occurs while checking the association
     */
    private void deleteRuleViolationsForRuleset(Connection connection, String rulesetId)
            throws SQLException {
        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                .DELETE_RULE_VIOLATIONS_FOR_RULESET)) {
            prepStmt.setString(1, rulesetId);
            prepStmt.executeUpdate();
        }
    }

    /**
     * Delete rulseset results related to a ruleset
     *
     * @param connection Database connection
     * @param rulesetId  Ruleset ID
     * @throws SQLException If an error occurs while deleting
     */
    private void deleteRulesetResultsForRuleset(Connection connection, String rulesetId)
            throws SQLException {
        try (PreparedStatement prepStmt = connection.
                prepareStatement(SQLConstants.DELETE_RULESET_RUN_FOR_RULESET)) {
            prepStmt.setString(1, rulesetId);
            prepStmt.executeUpdate();
        }
    }

    /**
     * Retrieves rulesets in the organization.
     *
     * @param organization Organization whose rulesets are to be retrieved
     * @return a list of rulesets associated with the organization
     * @throws APIMGovernanceException if there is an error retrieving the rulesets
     */
    @Override
    public RulesetList getRulesets(String organization) throws APIMGovernanceException {
        RulesetList rulesetList = new RulesetList();
        List<RulesetInfo> rulesetInfoList = new ArrayList<>();
        String sqlQuery = SQLConstants.GET_RULESETS;
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(sqlQuery)) {
            prepStmt.setString(1, organization);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    rulesetInfoList.add(getRulesetInfoFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_RETRIEVING_RULESETS,
                    e, organization);
        }
        rulesetList.setCount(rulesetInfoList.size());
        rulesetList.setRulesetList(rulesetInfoList);
        return rulesetList;
    }

    /**
     * Retrieves a ruleset by name.
     *
     * @param name         Name of the ruleset
     * @param organization Organization whose ruleset is to be retrieved
     * @return the ruleset with the given name
     * @throws APIMGovernanceException if there is an error retrieving the ruleset
     */
    @Override
    public RulesetInfo getRulesetByName(String name, String organization) throws APIMGovernanceException {
        String sqlQuery = SQLConstants.GET_RULESET_BY_NAME;
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
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_RETRIEVING_RULESET_BY_NAME,
                    e, organization);
        }
        return null;
    }

    /**
     * Retrieves a ruleset by ID.
     *
     * @param rulesetId    Ruleset ID of the ruleset
     * @param organization Organization whose ruleset is to be retrieved
     * @return the ruleset with the given ID
     * @throws APIMGovernanceException if there is an error retrieving the ruleset
     */
    @Override
    public RulesetInfo getRulesetById(String rulesetId, String organization) throws APIMGovernanceException {
        String sqlQuery = SQLConstants.GET_RULESETS_BY_ID;
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
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_RETRIEVING_RULESET_BY_ID,
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
     * @throws APIMGovernanceException If an error occurs while searching for rulesets
     */
    @Override
    public RulesetList searchRulesets(Map<String, String> searchCriteria, String organization)
            throws APIMGovernanceException {
        RulesetList rulesetList = new RulesetList();
        List<RulesetInfo> rulesetInfoList = new ArrayList<>();

        String sqlQuery = SQLConstants.SEARCH_RULESETS;
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(sqlQuery)) {
            prepStmt.setString(1, organization);
            prepStmt.setString(2, searchCriteria
                    .getOrDefault(APIMGovernanceConstants.RulesetSearchAttributes.NAME, ""));
            prepStmt.setString(3, searchCriteria
                    .getOrDefault(APIMGovernanceConstants.RulesetSearchAttributes.RULE_TYPE, ""));
            prepStmt.setString(4, searchCriteria
                    .getOrDefault(APIMGovernanceConstants.RulesetSearchAttributes.ARTIFACT_TYPE, ""));
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    rulesetInfoList.add(getRulesetInfoFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_SEARCHING_RULESETS,
                    e, organization);
        }
        rulesetList.setCount(rulesetInfoList.size());
        rulesetList.setRulesetList(rulesetInfoList);
        return rulesetList;
    }

    /**
     * Retrieves rulesetInfo object from the result set
     *
     * @param rs ResultSet
     * @return RulesetInfo object
     * @throws SQLException If an error occurs while retrieving the ruleset
     */
    private RulesetInfo getRulesetInfoFromResultSet(ResultSet rs) throws SQLException {
        RulesetInfo rulesetInfo = new RulesetInfo();
        rulesetInfo.setId(rs.getString("RULESET_ID"));
        rulesetInfo.setName(rs.getString("NAME"));
        rulesetInfo.setDescription(rs.getString("DESCRIPTION"));
        rulesetInfo.setRuleCategory(RuleCategory.fromString(
                rs.getString("RULE_CATEGORY")));
        rulesetInfo.setRuleType(RuleType.fromString(rs.getString("RULE_TYPE")));
        rulesetInfo.setArtifactType(ExtendedArtifactType.fromString(
                rs.getString("ARTIFACT_TYPE")));
        rulesetInfo.setDocumentationLink(rs.getString("DOCUMENTATION_LINK"));
        rulesetInfo.setProvider(rs.getString("PROVIDER"));
        rulesetInfo.setCreatedBy(rs.getString("CREATED_BY"));
        rulesetInfo.setCreatedTime(rs.getString("CREATED_TIME"));
        rulesetInfo.setUpdatedBy(rs.getString("UPDATED_BY"));
        rulesetInfo.setUpdatedTime(rs.getString("LAST_UPDATED_TIME"));
        return rulesetInfo;
    }

    /**
     * Get the content of a Governance Ruleset
     *
     * @param rulesetId    Ruleset ID
     * @param organization Organization
     * @return String Content of the ruleset
     * @throws APIMGovernanceException If an error occurs while getting the ruleset content
     */
    @Override
    public RulesetContent getRulesetContent(String rulesetId, String organization) throws APIMGovernanceException {
        String sqlQuery = SQLConstants.GET_RULESET_CONTENT;
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(sqlQuery);) {
            prepStmt.setString(1, rulesetId);
            prepStmt.setString(2, organization);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    RulesetContent rulesetContentObj = new RulesetContent();
                    rulesetContentObj.setFileName(rs.getString("FILE_NAME"));
                    try (InputStream contentStream = rs.getBinaryStream("CONTENT")) {
                        byte[] content = IOUtils.toByteArray(contentStream);
                        rulesetContentObj.setContent(content);
                    } catch (IOException e) {
                        throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_RETRIEVING_RULESET_CONTENT,
                                e, rulesetId);
                    }
                    return rulesetContentObj;
                }
                return null;
            }
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_RETRIEVING_RULESET_BY_ID,
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
    public List<String> getAssociatedPoliciesForRuleset(String rulesetId, String organization)
            throws APIMGovernanceException {
        List<String> policyIds = new ArrayList<>();
        String sqlQuery = SQLConstants.GET_POLICIES_FOR_RULESET;
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
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_RETRIEVING_ASSOCIATED_POLICIES,
                    e, rulesetId);
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
    public List<Rule> getRulesByRulesetId(String rulesetId, String organization) throws APIMGovernanceException {
        List<Rule> rules = new ArrayList<>();
        String sqlQuery = SQLConstants.GET_RULES_WITHOUT_CONTENT;
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(sqlQuery)) {
            prepStmt.setString(1, rulesetId);
            prepStmt.setString(2, organization);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    Rule rule = new Rule();
                    rule.setId(rs.getString("RULESET_RULE_ID"));
                    rule.setName(rs.getString("RULE_NAME"));
                    rule.setDescription(rs.getString("RULE_DESCRIPTION"));
                    rule.setSeverity(RuleSeverity.fromString(rs.getString("SEVERITY")));
                    rules.add(rule);
                }
            }
            return rules;
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_RETRIEVING_RULES_BY_RULESET_ID
                    , e, rulesetId);
        }
    }
}

