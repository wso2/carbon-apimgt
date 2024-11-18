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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.api.model.RulesetInfo;
import org.wso2.carbon.apimgt.governance.api.model.RulesetList;
import org.wso2.carbon.apimgt.governance.api.model.Severity;
import org.wso2.carbon.apimgt.governance.impl.dao.RulsetMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.governance.impl.util.GovernanceDBUtil;
import org.wso2.carbon.apimgt.governance.impl.util.GovernanceUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the RulsetMgtDAO interface.
 */
public class RulsetMgtDAOImpl implements RulsetMgtDAO {

    private static final Log log = LogFactory.getLog(RulsetMgtDAOImpl.class);
    private static RulsetMgtDAO INSTANCE = null;

    private RulsetMgtDAOImpl() {
    }

    /**
     * Get an instance of the RulsetMgtDAO
     *
     * @return RulsetMgtDAO instance
     */
    public static RulsetMgtDAO getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RulsetMgtDAOImpl();
        }
        return INSTANCE;
    }

    /**
     * Create a new Governance Ruleset
     *
     * @param organization Organization
     * @param ruleset      Ruleset object
     * @return
     * @throws GovernanceException
     */
    @Override
    public Ruleset createRuleset(String organization, Ruleset ruleset) throws GovernanceException {

        InputStream rulesetContent = new ByteArrayInputStream(
                ruleset.getRulesetContent().getBytes(Charset.defaultCharset()));

        String sqlQuery = SQLConstants.CREATE_RULESET;
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(sqlQuery)) {
            try {
                connection.setAutoCommit(false);
                prepStmt.setString(1, ruleset.getId());
                prepStmt.setString(2, ruleset.getName());
                prepStmt.setString(3, ruleset.getDescription());
                prepStmt.setBlob(4, rulesetContent);
                prepStmt.setString(5, ruleset.getAppliesTo());
                prepStmt.setString(6, ruleset.getDocumentationLink());
                prepStmt.setString(7, ruleset.getProvider());
                prepStmt.setString(8, organization);
                prepStmt.setString(9, ruleset.getCreatedBy());
                prepStmt.setInt(10, ruleset.isDefault());
                prepStmt.execute();
                insertRules(ruleset.getId(), ruleset.getRulesetContent(), connection);
                connection.commit();
            } catch (SQLException | GovernanceException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            if (e instanceof SQLIntegrityConstraintViolationException) {
                if (getRulesetByName(organization, ruleset.getName()) != null) {
                    throw new GovernanceException(GovernanceExceptionCodes.RULESET_ALREADY_EXIST, ruleset.getName(),
                            organization);
                }
            }
            throw new GovernanceException(GovernanceExceptionCodes.RULESET_CREATION_FAILED, e,
                    ruleset.getName(), organization
            );
        }
        return ruleset;
    }

    /**
     * Insert rules into the database
     *
     * @param rulesetId      Ruleset ID
     * @param rulesetContent Ruleset content
     * @param connection     Database connection
     * @throws GovernanceException Governance exception
     */
    private void insertRules(String rulesetId, String rulesetContent, Connection connection)
            throws GovernanceException {

        // Parse YAML content
        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        Map<String, Object> rulesetMap;
        try {
            rulesetMap = yamlReader.readValue(rulesetContent, Map.class);
        } catch (JsonProcessingException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_FAILED_TO_PARSE_RULESET_CONETENT, e);
        }
        // Check if 'rules' is present and not null
        if (rulesetMap.containsKey("rules") && rulesetMap.get("rules") instanceof Map) {
            // Extract rules
            Map<String, Map<String, Object>> rules =
                    (Map<String, Map<String, Object>>) rulesetMap.get("rules");
            String sqlQuery = SQLConstants.INSERT_RULES;
            try (PreparedStatement prepStmt = connection.prepareStatement(sqlQuery);) {
                for (Map.Entry<String, Map<String, Object>> entry : rules.entrySet()) {
                    String ruleCode = entry.getKey();
                    Map<String, Object> ruleDetails = entry.getValue();
                    String message = (String) ruleDetails.get("message");
                    String description = (String) ruleDetails.get("description");
                    String severityString = (String) ruleDetails.get("severity");
                    int severity = Severity.fromString(severityString).getValue();

                    prepStmt.setString(1, GovernanceUtil.generateUUID());
                    prepStmt.setString(2, rulesetId);
                    prepStmt.setString(3, ruleCode);
                    prepStmt.setString(4, message);
                    prepStmt.setString(5, description);
                    prepStmt.setInt(6, severity);
                    prepStmt.addBatch();
                }
                prepStmt.executeBatch();
            } catch (SQLException e) {
                throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_INSERTING_RULES, e, rulesetId);
            }
        } else {
            throw new GovernanceException(GovernanceExceptionCodes.INVALID_RULESET_CONTENT, rulesetId);
        }
    }

    /**
     * Retrieves rulesets in the organization.
     *
     * @param organization Organization whose rulesets are to be retrieved
     * @return a list of rulesets associated with the organization
     * @throws GovernanceException if there is an error retrieving the rulesets
     */
    @Override
    public RulesetList getRulesets(String organization) throws GovernanceException {
        RulesetList rulesetList = new RulesetList();
        List<RulesetInfo> rulesetInfoList = new ArrayList<>();
        String sqlQuery = SQLConstants.GET_RULESETS;
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(sqlQuery)) {
            prepStmt.setString(1, organization);
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
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_RETRIEVING_RULESETS,
                    e, organization);
        }
        rulesetList.setCount(rulesetInfoList.size());
        rulesetList.setRulesetList(rulesetInfoList);
        return rulesetList;
    }

    /**
     * Retrieves a ruleset by name.
     *
     * @param organization Organization whose ruleset is to be retrieved
     * @param name         Name of the ruleset
     * @return the ruleset with the given name
     * @throws GovernanceException if there is an error retrieving the ruleset
     */
    @Override
    public RulesetInfo getRulesetByName(String organization, String name) throws GovernanceException {
        String sqlQuery = SQLConstants.GET_RULESET_BY_NAME;
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(sqlQuery)) {
            prepStmt.setString(1, name);
            prepStmt.setString(2, organization);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
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
                    return rulesetInfo;
                }
            }
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_RETRIEVING_RULESET_BY_NAME,
                    e, organization);
        }
        return null;
    }

    /**
     * Retrieves a ruleset by ID.
     *
     * @param organization Organization whose ruleset is to be retrieved
     * @param rulesetId    Ruleset ID of the ruleset
     * @return the ruleset with the given ID
     * @throws GovernanceException if there is an error retrieving the ruleset
     */
    @Override
    public RulesetInfo getRulesetById(String organization, String rulesetId) throws GovernanceException {
        String sqlQuery = SQLConstants.GET_RULESETS_BY_ID;
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(sqlQuery)) {
            prepStmt.setString(1, rulesetId);
            prepStmt.setString(2, organization);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
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
                    return rulesetInfo;
                }
            }
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_RETRIEVING_RULESET_BY_ID,
                    e, organization);
        }
        return null;
    }

    /**
     * Get the content of a Governance Ruleset
     *
     * @param organization Organization
     * @param rulesetId    Ruleset ID
     * @return String Content of the ruleset
     * @throws GovernanceException If an error occurs while getting the ruleset content
     */
    @Override
    public String getRulesetContent(String organization, String rulesetId) throws GovernanceException {
        String rulesetContent = null;
        String sqlQuery = SQLConstants.GET_RULESET_CONTENT;
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(sqlQuery);) {
            prepStmt.setString(1, rulesetId);
            prepStmt.setString(2, organization);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    InputStream inputStream = rs.getBinaryStream("RULESET_CONTENT");
                    if (inputStream != null) {
                        rulesetContent = GovernanceDBUtil.getStringFromInputStream(inputStream);
                    }
                    return rulesetContent;
                }
            }
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_RETRIEVING_RULESET_BY_ID,
                    e, organization);
        } catch (IOException e) {
            throw new GovernanceException(GovernanceExceptionCodes.RULESET_CONTENT_CONVERSION_ERROR, e,
                    rulesetId, organization);
        }
        return null;
    }

    /**
     * Delete a Governance Ruleset
     *
     * @param organization Organization
     * @param rulesetId    Ruleset ID
     * @throws GovernanceException If an error occurs while deleting the ruleset
     */
    @Override
    public void deleteRuleset(String organization, String rulesetId) throws GovernanceException {

        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.DELETE_RULESET)) {
            try {
                connection.setAutoCommit(false);
                prepStmt.setString(1, rulesetId);
                prepStmt.setString(2, organization);
                prepStmt.executeUpdate();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_DELETING_RULESET,
                    e, rulesetId, organization);
        }
    }

    /**
     * Update a Governance Ruleset
     *
     * @param organization Organization
     * @param rulesetId    Ruleset ID
     * @param ruleset      Ruleset object
     * @return Ruleset Updated object
     * @throws GovernanceException If an error occurs while updating the ruleset
     */
    @Override
    public Ruleset updateRuleset(String organization, String rulesetId, Ruleset ruleset) throws GovernanceException {
        InputStream rulesetContent = new ByteArrayInputStream(
                ruleset.getRulesetContent().getBytes(Charset.defaultCharset()));
        try (Connection connection = GovernanceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.UPDATE_RULESET)) {
            try {
                connection.setAutoCommit(false);
                prepStmt.setString(1, ruleset.getName());
                prepStmt.setString(2, ruleset.getDescription());
                prepStmt.setBlob(3, rulesetContent);
                prepStmt.setString(4, ruleset.getAppliesTo());
                prepStmt.setString(5, ruleset.getDocumentationLink());
                prepStmt.setString(6, ruleset.getProvider());
                prepStmt.setString(7, ruleset.getUpdatedBy());
                prepStmt.setString(8, rulesetId);
                prepStmt.setString(9, organization);
                prepStmt.executeUpdate();
                // Delete existing rules related to this ruleset.
                deleteRules(rulesetId, connection);
                // Insert updated rules.
                insertRules(rulesetId, ruleset.getRulesetContent(), connection);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.
                    ERROR_WHILE_UPDATING_RULESET, e, rulesetId, organization);
        }
        return ruleset;
    }


    /**
     * Delete rules related to a ruleset
     *
     * @param rulesetId  Ruleset ID
     * @param connection Database connection
     * @throws GovernanceException If an error occurs while deleting the rules
     */
    private void deleteRules(String rulesetId, Connection connection) throws GovernanceException {
        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.DELETE_RULES)) {
            try {
                prepStmt.setString(1, rulesetId);
                int rowsAffected = prepStmt.executeUpdate();
                if (rowsAffected == 0) {
                    log.error("No rules found to delete for this ruleset with ID: " + rulesetId);
                }
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_DELETING_RULES, e, rulesetId);
        }
    }


}

