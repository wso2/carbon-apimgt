/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.impl.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.DevportalGovernanceApplicationSnapshot;
import org.wso2.carbon.apimgt.governance.api.model.DevportalGovernanceKeyManagerScope;
import org.wso2.carbon.apimgt.governance.api.model.DevportalGovernanceRulesetBinding;
import org.wso2.carbon.apimgt.governance.api.model.DevportalGovernanceRulesetSnapshot;
import org.wso2.carbon.apimgt.governance.api.model.DevportalGovernanceRulesetSnapshotKeyManagerScope;
import org.wso2.carbon.apimgt.governance.api.model.DevportalGovernanceTemplate;
import org.wso2.carbon.apimgt.governance.api.model.DevportalGovernanceTemplateList;
import org.wso2.carbon.apimgt.governance.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceUtil;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
 * DAO for Admin Portal Devportal Governance template persistence.
 */
public class DevportalGovernanceDAO {

    private static final Log log = LogFactory.getLog(DevportalGovernanceDAO.class);
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper()
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    private static final TypeReference<Map<String, Object>> FORM_CONFIG_TYPE =
            new TypeReference<Map<String, Object>>() { };
    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    private DevportalGovernanceDAO() {
    }

    private static class SingletonHelper {
        private static final DevportalGovernanceDAO INSTANCE = new DevportalGovernanceDAO();
    }

    public static DevportalGovernanceDAO getInstance() {

        return SingletonHelper.INSTANCE;
    }

    /**
     * Create a Devportal Governance template with ruleset bindings in a single transaction.
     *
     * @param template     Devportal Governance template
     * @param organization organization
     * @return Created template
     * @throws APIMGovernanceException if an error occurs while creating the template
     */
    public DevportalGovernanceTemplate createTemplate(DevportalGovernanceTemplate template, String organization)
            throws APIMGovernanceException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                Timestamp now = new Timestamp(System.currentTimeMillis());
                if (template.isDefault()) {
                    resetDefaultTemplate(connection, organization, template.isGlobal(), template.getCreatedBy(), now,
                            null);
                }
                addTemplate(connection, template, organization, now);
                addRulesetBindings(connection, template, organization, template.getCreatedBy(), now);
                connection.commit();
                return getTemplateById(template.getId(), organization);
            } catch (JsonProcessingException | NoSuchAlgorithmException | SQLException e) {
                rollbackConnection(connection, "Error while creating Devportal Governance template");
                throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_CREATING_DEVPORTAL_TEMPLATE, e,
                        template.getName(), organization);
            } finally {
                APIMgtDBUtil.setAutoCommit(connection, true);
            }
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_CREATING_DEVPORTAL_TEMPLATE, e,
                    template.getName(), organization);
        }
    }

    /**
     * Update a Devportal Governance template and replace its ruleset bindings in a single transaction.
     *
     * @param templateId   template ID
     * @param template     Devportal Governance template
     * @param organization organization
     * @return Updated template
     * @throws APIMGovernanceException if an error occurs while updating the template
     */
    public DevportalGovernanceTemplate updateTemplate(String templateId, DevportalGovernanceTemplate template,
                                                      String organization) throws APIMGovernanceException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                Timestamp now = new Timestamp(System.currentTimeMillis());
                if (template.isDefault()) {
                    resetDefaultTemplate(connection, organization, template.isGlobal(), template.getUpdatedBy(), now,
                            templateId);
                }
                updateTemplateMetadata(connection, templateId, template, organization, now);
                deleteRulesetBindings(connection, templateId);
                addRulesetBindings(connection, template, organization, template.getUpdatedBy(), now);
                connection.commit();
                return getTemplateById(templateId, organization);
            } catch (JsonProcessingException | NoSuchAlgorithmException | SQLException e) {
                rollbackConnection(connection, "Error while updating Devportal Governance template");
                throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_UPDATING_DEVPORTAL_TEMPLATE, e,
                        templateId);
            } finally {
                APIMgtDBUtil.setAutoCommit(connection, true);
            }
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_UPDATING_DEVPORTAL_TEMPLATE, e,
                    templateId);
        }
    }

    /**
     * Delete a Devportal Governance template.
     *
     * @param templateId   template ID
     * @param organization organization
     * @throws APIMGovernanceException if an error occurs while deleting the template
     */
    public void deleteTemplate(String templateId, String organization) throws APIMGovernanceException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.DELETE_DEVPORTAL_TEMPLATE)) {
            prepStmt.setString(1, templateId);
            prepStmt.setString(2, organization);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_DELETING_DEVPORTAL_TEMPLATE, e,
                    templateId);
        }
    }

    /**
     * Get a Devportal Governance template by ID.
     *
     * @param templateId   template ID
     * @param organization organization
     * @return template if found, otherwise null
     * @throws APIMGovernanceException if an error occurs while retrieving the template
     */
    public DevportalGovernanceTemplate getTemplateById(String templateId, String organization)
            throws APIMGovernanceException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            DevportalGovernanceTemplate template;
            try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.GET_DEVPORTAL_TEMPLATE_BY_ID)) {
                prepStmt.setString(1, templateId);
                prepStmt.setString(2, organization);
                try (ResultSet resultSet = prepStmt.executeQuery()) {
                    template = resultSet.next() ? getTemplate(resultSet) : null;
                }
            }
            if (template != null) {
                template.setRulesetBindings(getRulesetBindings(connection, template.getId()));
            }
            return template;
        } catch (JsonProcessingException | SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_RETRIEVING_DEVPORTAL_TEMPLATE, e,
                    templateId);
        }
    }

    /**
     * Get a Devportal Governance template by name.
     *
     * @param templateName template name
     * @param organization organization
     * @return template if found, otherwise null
     * @throws APIMGovernanceException if an error occurs while retrieving the template
     */
    public DevportalGovernanceTemplate getTemplateByName(String templateName, String organization)
            throws APIMGovernanceException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            DevportalGovernanceTemplate template;
            try (PreparedStatement prepStmt =
                         connection.prepareStatement(SQLConstants.GET_DEVPORTAL_TEMPLATE_BY_NAME)) {
                prepStmt.setString(1, templateName);
                prepStmt.setString(2, organization);
                try (ResultSet resultSet = prepStmt.executeQuery()) {
                    template = resultSet.next() ? getTemplate(resultSet) : null;
                }
            }
            if (template != null) {
                template.setRulesetBindings(getRulesetBindings(connection, template.getId()));
            }
            return template;
        } catch (JsonProcessingException | SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_RETRIEVING_DEVPORTAL_TEMPLATES, e,
                    organization);
        }
    }

    /**
     * Get the default Devportal Governance template for an organization.
     *
     * @param organization organization
     * @return default template if configured, otherwise null
     * @throws APIMGovernanceException if an error occurs while retrieving the template
     */
    public DevportalGovernanceTemplate getDefaultTemplate(String organization) throws APIMGovernanceException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            DevportalGovernanceTemplate template = getLocalDefaultTemplate(connection, organization);
            if (template == null) {
                template = getGlobalDefaultTemplate(connection);
            }
            if (template != null) {
                template.setRulesetBindings(getRulesetBindings(connection, template.getId()));
            }
            return template;
        } catch (JsonProcessingException | SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_RETRIEVING_DEVPORTAL_TEMPLATES, e,
                    organization);
        }
    }

    /**
     * Get all Devportal Governance templates for an organization.
     *
     * @param organization organization
     * @return template list
     * @throws APIMGovernanceException if an error occurs while retrieving templates
     */
    public DevportalGovernanceTemplateList getTemplates(String organization) throws APIMGovernanceException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            List<DevportalGovernanceTemplate> templates = new ArrayList<>();
            try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.GET_DEVPORTAL_TEMPLATES)) {
                prepStmt.setString(1, organization);
                try (ResultSet resultSet = prepStmt.executeQuery()) {
                    while (resultSet.next()) {
                        templates.add(getTemplate(resultSet));
                    }
                }
            }
            for (DevportalGovernanceTemplate template : templates) {
                template.setRulesetBindings(getRulesetBindings(connection, template.getId()));
            }
            DevportalGovernanceTemplateList templateList = new DevportalGovernanceTemplateList();
            templateList.setTemplateList(templates);
            return templateList;
        } catch (JsonProcessingException | SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_RETRIEVING_DEVPORTAL_TEMPLATES, e,
                    organization);
        }
    }

    /**
     * Check whether a ruleset belongs to the requested organization.
     *
     * @param rulesetId    ruleset ID
     * @param organization organization
     * @return true if the ruleset belongs to the organization
     * @throws APIMGovernanceException if an error occurs while checking the ruleset
     */
    public boolean isRulesetInOrganization(String rulesetId, String organization) throws APIMGovernanceException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.CHECK_RULESET_IN_ORGANIZATION)) {
            prepStmt.setString(1, rulesetId);
            prepStmt.setString(2, organization);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_RULESET_BY_ID, e);
        }
    }

    /**
     * Retrieve live template-bound rulesets as in-memory snapshots for pre-persistence validation.
     *
     * @param templateId   selected template ID, or null to resolve the default template
     * @param organization organization
     * @return in-memory ruleset snapshots
     * @throws APIMGovernanceException if an error occurs while retrieving the rulesets
     */
    public List<DevportalGovernanceRulesetSnapshot> getRulesetSnapshotsForTemplate(String templateId,
                                                                                   String organization)
            throws APIMGovernanceException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            DevportalGovernanceTemplate template = resolveSnapshotTemplate(connection, templateId, organization);
            if (template == null) {
                throw new APIMGovernanceException(APIMGovExceptionCodes.DEVPORTAL_TEMPLATE_NOT_FOUND,
                        templateId == null ? "default" : templateId);
            }
            return getRulesetSnapshotsForTemplate(connection, null, template.getId());
        } catch (JsonProcessingException | NoSuchAlgorithmException | SQLException e) {
            throw new APIMGovernanceException(
                    APIMGovExceptionCodes.ERROR_WHILE_RETRIEVING_DEVPORTAL_GOVERNANCE_SNAPSHOT, e,
                    templateId == null ? "default" : templateId);
        }
    }

    /**
     * Capture a point-in-time application governance snapshot from a live template.
     *
     * @param applicationId   application database ID
     * @param applicationUuid application UUID
     * @param templateId      selected template ID, or null to resolve the default template
     * @param organization    application organization
     * @return application governance snapshot
     * @throws APIMGovernanceException if an error occurs while capturing the snapshot
     */
    public DevportalGovernanceApplicationSnapshot captureApplicationSnapshot(int applicationId,
                                                                             String applicationUuid,
                                                                             String templateId,
                                                                             String organization)
            throws APIMGovernanceException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                DevportalGovernanceTemplate template = resolveSnapshotTemplate(connection, templateId, organization);
                if (template == null) {
                    throw new APIMGovernanceException(APIMGovExceptionCodes.DEVPORTAL_TEMPLATE_NOT_FOUND,
                            templateId == null ? "default" : templateId);
                }
                deleteApplicationSnapshot(connection, applicationId);
                String snapshotId = APIMGovernanceUtil.generateUUID();
                Timestamp capturedAt = new Timestamp(System.currentTimeMillis());
                addApplicationSnapshot(connection, snapshotId, applicationId, applicationUuid, template,
                        organization, capturedAt);
                List<DevportalGovernanceRulesetSnapshot> rulesetSnapshots =
                        addRulesetSnapshots(connection, snapshotId, template.getId());
                connection.commit();

                DevportalGovernanceApplicationSnapshot snapshot = getApplicationSnapshot(applicationId);
                if (snapshot == null) {
                    snapshot = buildApplicationSnapshot(snapshotId, applicationId, applicationUuid, template,
                            organization, capturedAt);
                    snapshot.setRulesetSnapshots(rulesetSnapshots);
                }
                return snapshot;
            } catch (JsonProcessingException | NoSuchAlgorithmException | SQLException e) {
                rollbackConnection(connection, "Error while capturing Devportal Governance application snapshot");
                throw new APIMGovernanceException(
                        APIMGovExceptionCodes.ERROR_WHILE_CAPTURING_DEVPORTAL_GOVERNANCE_SNAPSHOT, e,
                        applicationId);
            } finally {
                APIMgtDBUtil.setAutoCommit(connection, true);
            }
        } catch (SQLException e) {
            throw new APIMGovernanceException(
                    APIMGovExceptionCodes.ERROR_WHILE_CAPTURING_DEVPORTAL_GOVERNANCE_SNAPSHOT, e,
                    applicationId);
        }
    }

    /**
     * Retrieve a hydrated application governance snapshot.
     *
     * @param applicationId application database ID
     * @return application governance snapshot if found, otherwise null
     * @throws APIMGovernanceException if an error occurs while retrieving the snapshot
     */
    public DevportalGovernanceApplicationSnapshot getApplicationSnapshot(int applicationId)
            throws APIMGovernanceException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            DevportalGovernanceApplicationSnapshot snapshot = null;
            try (PreparedStatement prepStmt =
                         connection.prepareStatement(SQLConstants.GET_APP_GOVERNANCE_SNAPSHOT)) {
                prepStmt.setInt(1, applicationId);
                try (ResultSet resultSet = prepStmt.executeQuery()) {
                    if (resultSet.next()) {
                        snapshot = getApplicationSnapshot(resultSet);
                    }
                }
            }
            if (snapshot != null) {
                snapshot.setRulesetSnapshots(getRulesetSnapshots(connection, snapshot.getSnapshotId()));
            }
            return snapshot;
        } catch (JsonProcessingException | SQLException e) {
            throw new APIMGovernanceException(
                    APIMGovExceptionCodes.ERROR_WHILE_RETRIEVING_DEVPORTAL_GOVERNANCE_SNAPSHOT, e,
                    applicationId);
        }
    }

    private void addTemplate(Connection connection, DevportalGovernanceTemplate template, String organization,
                             Timestamp createdTime)
            throws SQLException, JsonProcessingException, NoSuchAlgorithmException {

        String formConfig = getFormConfigAsJson(template);
        String formConfigHash = getSha256Hash(formConfig);
        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.CREATE_DEVPORTAL_TEMPLATE)) {
            prepStmt.setString(1, template.getId());
            prepStmt.setString(2, template.getName());
            prepStmt.setString(3, template.getDescription());
            prepStmt.setString(4, formConfig);
            prepStmt.setString(5, formConfigHash);
            prepStmt.setString(6, template.getStatus());
            prepStmt.setInt(7, template.isDefault() ? 1 : 0);
            prepStmt.setInt(8, template.isGlobal() ? 1 : 0);
            prepStmt.setString(9, organization);
            prepStmt.setString(10, template.getCreatedBy());
            prepStmt.setTimestamp(11, createdTime);
            prepStmt.executeUpdate();
        }
        template.setFormConfigHash(formConfigHash);
        template.setOrganization(organization);
        template.setCreatedTime(createdTime.toString());
    }

    private void updateTemplateMetadata(Connection connection, String templateId, DevportalGovernanceTemplate template,
                                        String organization, Timestamp updatedTime)
            throws SQLException, JsonProcessingException, NoSuchAlgorithmException {

        String formConfig = getFormConfigAsJson(template);
        String formConfigHash = getSha256Hash(formConfig);
        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.UPDATE_DEVPORTAL_TEMPLATE)) {
            prepStmt.setString(1, template.getName());
            prepStmt.setString(2, template.getDescription());
            prepStmt.setString(3, formConfig);
            prepStmt.setString(4, formConfigHash);
            prepStmt.setString(5, template.getStatus());
            prepStmt.setInt(6, template.isDefault() ? 1 : 0);
            prepStmt.setInt(7, template.isGlobal() ? 1 : 0);
            prepStmt.setString(8, template.getUpdatedBy());
            prepStmt.setTimestamp(9, updatedTime);
            prepStmt.setString(10, templateId);
            prepStmt.setString(11, organization);
            prepStmt.executeUpdate();
        }
        template.setFormConfigHash(formConfigHash);
        template.setUpdatedTime(updatedTime.toString());
    }

    private void addRulesetBindings(Connection connection, DevportalGovernanceTemplate template, String organization,
                                    String username, Timestamp createdTime) throws SQLException {

        List<DevportalGovernanceRulesetBinding> rulesetBindings = template.getRulesetBindings();
        for (DevportalGovernanceRulesetBinding binding : rulesetBindings) {
            try (PreparedStatement prepStmt =
                         connection.prepareStatement(SQLConstants.CREATE_TEMPLATE_RULESET_BINDING)) {
                prepStmt.setString(1, binding.getBindingId());
                prepStmt.setString(2, template.getId());
                prepStmt.setString(3, binding.getRulesetId());
                prepStmt.setInt(4, binding.getBindingOrder());
                prepStmt.setString(5, username);
                prepStmt.setTimestamp(6, createdTime);
                prepStmt.executeUpdate();
            }
            addKeyManagerScopes(connection, binding, organization);
            binding.setTemplateId(template.getId());
            binding.setCreatedBy(username);
            binding.setCreatedTime(createdTime.toString());
        }
    }

    private void addKeyManagerScopes(Connection connection, DevportalGovernanceRulesetBinding binding,
                                     String organization) throws SQLException {

        if (binding.getKeyManagerScopes().isEmpty()) {
            return;
        }
        try (PreparedStatement prepStmt =
                     connection.prepareStatement(SQLConstants.CREATE_TEMPLATE_RULESET_KM_SCOPE)) {
            for (DevportalGovernanceKeyManagerScope keyManagerScope : binding.getKeyManagerScopes()) {
                prepStmt.setString(1, binding.getBindingId());
                prepStmt.setString(2, keyManagerScope.getKeyManagerUuid());
                prepStmt.setString(3, organization);
                prepStmt.addBatch();
            }
            prepStmt.executeBatch();
        }
    }

    private void deleteRulesetBindings(Connection connection, String templateId) throws SQLException {

        try (PreparedStatement prepStmt =
                     connection.prepareStatement(SQLConstants.DELETE_TEMPLATE_RULESET_BINDINGS)) {
            prepStmt.setString(1, templateId);
            prepStmt.executeUpdate();
        }
    }

    private DevportalGovernanceTemplate resolveSnapshotTemplate(Connection connection, String templateId,
                                                                String organization)
            throws SQLException, JsonProcessingException {

        DevportalGovernanceTemplate template;
        if (templateId != null) {
            template = getTemplateById(connection, templateId, organization);
        } else {
            template = getLocalDefaultTemplate(connection, organization);
            if (template == null) {
                template = getGlobalDefaultTemplate(connection);
            }
        }
        return template;
    }

    private DevportalGovernanceTemplate getTemplateById(Connection connection, String templateId, String organization)
            throws SQLException, JsonProcessingException {

        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.GET_DEVPORTAL_TEMPLATE_BY_ID)) {
            prepStmt.setString(1, templateId);
            prepStmt.setString(2, organization);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                return resultSet.next() ? getTemplate(resultSet) : null;
            }
        }
    }

    private DevportalGovernanceTemplate getLocalDefaultTemplate(Connection connection, String organization)
            throws SQLException, JsonProcessingException {

        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.GET_DEFAULT_DEVPORTAL_TEMPLATE)) {
            prepStmt.setString(1, organization);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                return resultSet.next() ? getTemplate(resultSet) : null;
            }
        }
    }

    private DevportalGovernanceTemplate getGlobalDefaultTemplate(Connection connection)
            throws SQLException, JsonProcessingException {

        try (PreparedStatement prepStmt =
                     connection.prepareStatement(SQLConstants.GET_GLOBAL_DEFAULT_DEVPORTAL_TEMPLATE);
             ResultSet resultSet = prepStmt.executeQuery()) {
            return resultSet.next() ? getTemplate(resultSet) : null;
        }
    }

    private void deleteApplicationSnapshot(Connection connection, int applicationId) throws SQLException {

        try (PreparedStatement prepStmt =
                     connection.prepareStatement(SQLConstants.DELETE_APP_GOVERNANCE_SNAPSHOT)) {
            prepStmt.setInt(1, applicationId);
            prepStmt.executeUpdate();
        }
    }

    private void addApplicationSnapshot(Connection connection, String snapshotId, int applicationId,
                                        String applicationUuid, DevportalGovernanceTemplate template,
                                        String organization, Timestamp capturedAt)
            throws SQLException, JsonProcessingException {

        try (PreparedStatement prepStmt =
                     connection.prepareStatement(SQLConstants.CREATE_APP_GOVERNANCE_SNAPSHOT)) {
            prepStmt.setString(1, snapshotId);
            prepStmt.setInt(2, applicationId);
            prepStmt.setString(3, applicationUuid);
            prepStmt.setString(4, template.getId());
            prepStmt.setString(5, template.getName());
            prepStmt.setString(6, template.getDescription());
            prepStmt.setString(7, getFormConfigAsJson(template));
            prepStmt.setString(8, template.getFormConfigHash());
            prepStmt.setString(9, organization);
            prepStmt.setString(10, null);
            prepStmt.setTimestamp(11, capturedAt);
            prepStmt.executeUpdate();
        }
    }

    private List<DevportalGovernanceRulesetSnapshot> addRulesetSnapshots(Connection connection, String snapshotId,
                                                                         String templateId)
            throws SQLException, NoSuchAlgorithmException {

        List<DevportalGovernanceRulesetSnapshot> rulesetSnapshots =
                getRulesetSnapshotsForTemplate(connection, snapshotId, templateId);
        for (DevportalGovernanceRulesetSnapshot rulesetSnapshot : rulesetSnapshots) {
            addRulesetSnapshot(connection, rulesetSnapshot);
            addRulesetSnapshotKeyManagerScopes(connection, rulesetSnapshot.getKeyManagerScopes());
        }
        return rulesetSnapshots;
    }

    private List<DevportalGovernanceRulesetSnapshot> getRulesetSnapshotsForTemplate(Connection connection,
                                                                                    String snapshotId,
                                                                                    String templateId)
            throws SQLException, NoSuchAlgorithmException {

        List<DevportalGovernanceRulesetSnapshot> rulesetSnapshots = new ArrayList<>();
        try (PreparedStatement prepStmt =
                     connection.prepareStatement(SQLConstants.GET_TEMPLATE_RULESET_SNAPSHOT_SOURCES)) {
            prepStmt.setString(1, templateId);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    DevportalGovernanceRulesetSnapshot rulesetSnapshot =
                            getRulesetSnapshotFromTemplateBinding(resultSet, snapshotId);
                    rulesetSnapshot.setKeyManagerScopes(getRulesetSnapshotKeyManagerScopesFromBinding(connection,
                            resultSet.getString("BINDING_ID"), rulesetSnapshot.getSnapshotRulesetId()));
                    rulesetSnapshots.add(rulesetSnapshot);
                }
            }
        }
        return rulesetSnapshots;
    }

    private DevportalGovernanceRulesetSnapshot getRulesetSnapshotFromTemplateBinding(ResultSet resultSet,
                                                                                    String snapshotId)
            throws SQLException, NoSuchAlgorithmException {

        byte[] content = resultSet.getBytes("CONTENT");
        String yamlContent = content == null ? "" : new String(content, StandardCharsets.UTF_8);
        DevportalGovernanceRulesetSnapshot rulesetSnapshot = new DevportalGovernanceRulesetSnapshot();
        rulesetSnapshot.setSnapshotRulesetId(APIMGovernanceUtil.generateUUID());
        rulesetSnapshot.setSnapshotId(snapshotId);
        rulesetSnapshot.setSourceRulesetId(resultSet.getString("RULESET_ID"));
        rulesetSnapshot.setRulesetName(resultSet.getString("NAME"));
        rulesetSnapshot.setRulesetDescription(resultSet.getString("DESCRIPTION"));
        rulesetSnapshot.setArtifactType(resultSet.getString("ARTIFACT_TYPE"));
        rulesetSnapshot.setRulesetType(resultSet.getString("RULE_TYPE"));
        rulesetSnapshot.setYamlContent(yamlContent);
        rulesetSnapshot.setContentSha256(getSha256Hash(yamlContent));
        rulesetSnapshot.setBindingOrder(resultSet.getInt("BINDING_ORDER"));
        return rulesetSnapshot;
    }

    private void addRulesetSnapshot(Connection connection, DevportalGovernanceRulesetSnapshot rulesetSnapshot)
            throws SQLException {

        try (PreparedStatement prepStmt =
                     connection.prepareStatement(SQLConstants.CREATE_APP_RULESET_SNAPSHOT)) {
            prepStmt.setString(1, rulesetSnapshot.getSnapshotRulesetId());
            prepStmt.setString(2, rulesetSnapshot.getSnapshotId());
            prepStmt.setString(3, rulesetSnapshot.getSourceRulesetId());
            prepStmt.setString(4, rulesetSnapshot.getRulesetName());
            prepStmt.setString(5, rulesetSnapshot.getRulesetDescription());
            prepStmt.setString(6, rulesetSnapshot.getArtifactType());
            prepStmt.setString(7, rulesetSnapshot.getRulesetType());
            prepStmt.setString(8, rulesetSnapshot.getYamlContent());
            prepStmt.setString(9, rulesetSnapshot.getContentSha256());
            prepStmt.setInt(10, rulesetSnapshot.getBindingOrder());
            prepStmt.executeUpdate();
        }
    }

    private List<DevportalGovernanceRulesetSnapshotKeyManagerScope> getRulesetSnapshotKeyManagerScopesFromBinding(
            Connection connection, String bindingId, String snapshotRulesetId) throws SQLException {

        List<DevportalGovernanceRulesetSnapshotKeyManagerScope> keyManagerScopes = new ArrayList<>();
        try (PreparedStatement getScopesStmt =
                     connection.prepareStatement(SQLConstants.GET_TEMPLATE_RULESET_SNAPSHOT_KM_SCOPES)) {
            getScopesStmt.setString(1, bindingId);
            try (ResultSet resultSet = getScopesStmt.executeQuery()) {
                while (resultSet.next()) {
                    DevportalGovernanceRulesetSnapshotKeyManagerScope keyManagerScope =
                            new DevportalGovernanceRulesetSnapshotKeyManagerScope();
                    keyManagerScope.setSnapshotRulesetId(snapshotRulesetId);
                    keyManagerScope.setKeyManagerUuid(resultSet.getString("KEY_MANAGER_UUID"));
                    keyManagerScope.setKeyManagerName(resultSet.getString("KEY_MANAGER_NAME"));
                    keyManagerScopes.add(keyManagerScope);
                }
            }
        }
        return keyManagerScopes;
    }

    private void addRulesetSnapshotKeyManagerScopes(Connection connection,
            List<DevportalGovernanceRulesetSnapshotKeyManagerScope> keyManagerScopes) throws SQLException {

        if (keyManagerScopes.isEmpty()) {
            return;
        }
        try (PreparedStatement prepStmt =
                     connection.prepareStatement(SQLConstants.CREATE_APP_RULESET_SNAPSHOT_KM_SCOPE)) {
            for (DevportalGovernanceRulesetSnapshotKeyManagerScope keyManagerScope : keyManagerScopes) {
                prepStmt.setString(1, keyManagerScope.getSnapshotRulesetId());
                prepStmt.setString(2, keyManagerScope.getKeyManagerUuid());
                prepStmt.setString(3, keyManagerScope.getKeyManagerName());
                prepStmt.addBatch();
            }
            prepStmt.executeBatch();
        }
    }

    private void resetDefaultTemplate(Connection connection, String organization, boolean global, String username,
                                      Timestamp updatedTime, String templateIdToExclude) throws SQLException {

        if (templateIdToExclude == null) {
            if (global) {
                try (PreparedStatement prepStmt =
                             connection.prepareStatement(SQLConstants.RESET_GLOBAL_DEFAULT_DEVPORTAL_TEMPLATE)) {
                    prepStmt.setString(1, username);
                    prepStmt.setTimestamp(2, updatedTime);
                    prepStmt.executeUpdate();
                }
            } else {
                try (PreparedStatement prepStmt =
                             connection.prepareStatement(SQLConstants.RESET_DEFAULT_DEVPORTAL_TEMPLATE)) {
                    prepStmt.setString(1, username);
                    prepStmt.setTimestamp(2, updatedTime);
                    prepStmt.setString(3, organization);
                    prepStmt.executeUpdate();
                }
            }
        } else {
            if (global) {
                try (PreparedStatement prepStmt = connection.prepareStatement(
                        SQLConstants.RESET_GLOBAL_DEFAULT_DEVPORTAL_TEMPLATE_EXCLUDING_ID)) {
                    prepStmt.setString(1, username);
                    prepStmt.setTimestamp(2, updatedTime);
                    prepStmt.setString(3, templateIdToExclude);
                    prepStmt.executeUpdate();
                }
            } else {
                try (PreparedStatement prepStmt = connection.prepareStatement(
                        SQLConstants.RESET_DEFAULT_DEVPORTAL_TEMPLATE_EXCLUDING_ID)) {
                    prepStmt.setString(1, username);
                    prepStmt.setTimestamp(2, updatedTime);
                    prepStmt.setString(3, organization);
                    prepStmt.setString(4, templateIdToExclude);
                    prepStmt.executeUpdate();
                }
            }
        }
    }

    private DevportalGovernanceTemplate getTemplate(ResultSet resultSet)
            throws SQLException, JsonProcessingException {

        DevportalGovernanceTemplate template = new DevportalGovernanceTemplate();
        template.setId(resultSet.getString("TEMPLATE_ID"));
        template.setName(resultSet.getString("NAME"));
        template.setDescription(resultSet.getString("DESCRIPTION"));
        template.setFormConfig(getFormConfigFromJson(resultSet.getString("FORM_CONFIG")));
        template.setFormConfigHash(resultSet.getString("FORM_CONFIG_HASH"));
        template.setStatus(resultSet.getString("STATUS"));
        template.setDefault(resultSet.getInt("IS_DEFAULT") == 1);
        template.setGlobal(resultSet.getInt("IS_GLOBAL") == 1);
        template.setOrganization(resultSet.getString("ORGANIZATION"));
        template.setCreatedBy(resultSet.getString("CREATED_BY"));
        Timestamp createdTime = resultSet.getTimestamp("CREATED_TIME");
        template.setCreatedTime(createdTime != null ? createdTime.toString() : null);
        template.setUpdatedBy(resultSet.getString("UPDATED_BY"));
        Timestamp updatedTime = resultSet.getTimestamp("LAST_UPDATED_TIME");
        template.setUpdatedTime(updatedTime != null ? updatedTime.toString() : null);
        return template;
    }

    private List<DevportalGovernanceRulesetBinding> getRulesetBindings(Connection connection, String templateId)
            throws SQLException {

        Map<String, List<DevportalGovernanceKeyManagerScope>> scopesByBinding =
                getKeyManagerScopes(connection, templateId);
        List<DevportalGovernanceRulesetBinding> rulesetBindings = new ArrayList<>();
        try (PreparedStatement prepStmt =
                     connection.prepareStatement(SQLConstants.GET_TEMPLATE_RULESET_BINDINGS)) {
            prepStmt.setString(1, templateId);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    DevportalGovernanceRulesetBinding binding = new DevportalGovernanceRulesetBinding();
                    String bindingId = resultSet.getString("BINDING_ID");
                    binding.setBindingId(bindingId);
                    binding.setTemplateId(resultSet.getString("TEMPLATE_ID"));
                    binding.setRulesetId(resultSet.getString("RULESET_ID"));
                    binding.setBindingOrder(resultSet.getInt("BINDING_ORDER"));
                    binding.setCreatedBy(resultSet.getString("CREATED_BY"));
                    Timestamp createdTime = resultSet.getTimestamp("CREATED_TIME");
                    binding.setCreatedTime(createdTime != null ? createdTime.toString() : null);
                    binding.setKeyManagerScopes(scopesByBinding.get(bindingId));
                    rulesetBindings.add(binding);
                }
            }
        }
        return rulesetBindings;
    }

    private Map<String, List<DevportalGovernanceKeyManagerScope>> getKeyManagerScopes(Connection connection,
                                                                                      String templateId)
            throws SQLException {

        Map<String, List<DevportalGovernanceKeyManagerScope>> scopesByBinding = new HashMap<>();
        try (PreparedStatement prepStmt =
                     connection.prepareStatement(SQLConstants.GET_TEMPLATE_RULESET_KM_SCOPES)) {
            prepStmt.setString(1, templateId);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    DevportalGovernanceKeyManagerScope keyManagerScope = new DevportalGovernanceKeyManagerScope();
                    String bindingId = resultSet.getString("BINDING_ID");
                    keyManagerScope.setBindingId(bindingId);
                    keyManagerScope.setKeyManagerUuid(resultSet.getString("KEY_MANAGER_UUID"));
                    keyManagerScope.setOrganization(resultSet.getString("ORGANIZATION"));
                    scopesByBinding.computeIfAbsent(bindingId, key -> new ArrayList<>()).add(keyManagerScope);
                }
            }
        }
        return scopesByBinding;
    }

    private DevportalGovernanceApplicationSnapshot getApplicationSnapshot(ResultSet resultSet)
            throws SQLException, JsonProcessingException {

        DevportalGovernanceApplicationSnapshot snapshot = new DevportalGovernanceApplicationSnapshot();
        snapshot.setSnapshotId(resultSet.getString("SNAPSHOT_ID"));
        snapshot.setApplicationId(resultSet.getInt("APPLICATION_ID"));
        snapshot.setApplicationUuid(resultSet.getString("APPLICATION_UUID"));
        snapshot.setSourceTemplateId(resultSet.getString("SOURCE_TEMPLATE_ID"));
        snapshot.setTemplateName(resultSet.getString("TEMPLATE_NAME"));
        snapshot.setTemplateDescription(resultSet.getString("TEMPLATE_DESCRIPTION"));
        snapshot.setFormConfig(getFormConfigFromJson(resultSet.getString("FORM_CONFIG")));
        snapshot.setFormConfigHash(resultSet.getString("FORM_CONFIG_HASH"));
        snapshot.setOrganization(resultSet.getString("ORGANIZATION"));
        snapshot.setCapturedBy(resultSet.getString("CAPTURED_BY"));
        Timestamp capturedAt = resultSet.getTimestamp("CAPTURED_AT");
        snapshot.setCapturedAt(capturedAt != null ? capturedAt.toString() : null);
        return snapshot;
    }

    private DevportalGovernanceApplicationSnapshot buildApplicationSnapshot(String snapshotId, int applicationId,
                                                                            String applicationUuid,
                                                                            DevportalGovernanceTemplate template,
                                                                            String organization,
                                                                            Timestamp capturedAt) {

        DevportalGovernanceApplicationSnapshot snapshot = new DevportalGovernanceApplicationSnapshot();
        snapshot.setSnapshotId(snapshotId);
        snapshot.setApplicationId(applicationId);
        snapshot.setApplicationUuid(applicationUuid);
        snapshot.setSourceTemplateId(template.getId());
        snapshot.setTemplateName(template.getName());
        snapshot.setTemplateDescription(template.getDescription());
        snapshot.setFormConfig(template.getFormConfig());
        snapshot.setFormConfigHash(template.getFormConfigHash());
        snapshot.setOrganization(organization);
        snapshot.setCapturedAt(capturedAt != null ? capturedAt.toString() : null);
        return snapshot;
    }

    private List<DevportalGovernanceRulesetSnapshot> getRulesetSnapshots(Connection connection, String snapshotId)
            throws SQLException {

        List<DevportalGovernanceRulesetSnapshot> rulesetSnapshots = new ArrayList<>();
        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.GET_APP_RULESET_SNAPSHOTS)) {
            prepStmt.setString(1, snapshotId);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    DevportalGovernanceRulesetSnapshot rulesetSnapshot = new DevportalGovernanceRulesetSnapshot();
                    rulesetSnapshot.setSnapshotRulesetId(resultSet.getString("SNAPSHOT_RULESET_ID"));
                    rulesetSnapshot.setSnapshotId(resultSet.getString("SNAPSHOT_ID"));
                    rulesetSnapshot.setSourceRulesetId(resultSet.getString("SOURCE_RULESET_ID"));
                    rulesetSnapshot.setRulesetName(resultSet.getString("RULESET_NAME"));
                    rulesetSnapshot.setRulesetDescription(resultSet.getString("RULESET_DESCRIPTION"));
                    rulesetSnapshot.setArtifactType(resultSet.getString("ARTIFACT_TYPE"));
                    rulesetSnapshot.setRulesetType(resultSet.getString("RULESET_TYPE"));
                    rulesetSnapshot.setYamlContent(resultSet.getString("YAML_CONTENT"));
                    rulesetSnapshot.setContentSha256(resultSet.getString("CONTENT_SHA256"));
                    rulesetSnapshot.setBindingOrder(resultSet.getInt("BINDING_ORDER"));
                    rulesetSnapshot.setKeyManagerScopes(getRulesetSnapshotKeyManagerScopes(connection,
                            rulesetSnapshot.getSnapshotRulesetId()));
                    rulesetSnapshots.add(rulesetSnapshot);
                }
            }
        }
        return rulesetSnapshots;
    }

    private List<DevportalGovernanceRulesetSnapshotKeyManagerScope> getRulesetSnapshotKeyManagerScopes(
            Connection connection, String snapshotRulesetId) throws SQLException {

        List<DevportalGovernanceRulesetSnapshotKeyManagerScope> keyManagerScopes = new ArrayList<>();
        try (PreparedStatement prepStmt =
                     connection.prepareStatement(SQLConstants.GET_APP_RULESET_SNAPSHOT_KM_SCOPES)) {
            prepStmt.setString(1, snapshotRulesetId);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    DevportalGovernanceRulesetSnapshotKeyManagerScope keyManagerScope =
                            new DevportalGovernanceRulesetSnapshotKeyManagerScope();
                    keyManagerScope.setSnapshotRulesetId(resultSet.getString("SNAPSHOT_RULESET_ID"));
                    keyManagerScope.setKeyManagerUuid(resultSet.getString("KEY_MANAGER_UUID"));
                    keyManagerScope.setKeyManagerName(resultSet.getString("KEY_MANAGER_NAME"));
                    keyManagerScopes.add(keyManagerScope);
                }
            }
        }
        return keyManagerScopes;
    }

    private String getFormConfigAsJson(DevportalGovernanceTemplate template) throws JsonProcessingException {

        return JSON_MAPPER.writeValueAsString(template.getFormConfig());
    }

    private Map<String, Object> getFormConfigFromJson(String formConfig) throws JsonProcessingException {

        return JSON_MAPPER.readValue(formConfig, FORM_CONFIG_TYPE);
    }

    private String getSha256Hash(String content) throws NoSuchAlgorithmException {

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(content.getBytes(StandardCharsets.UTF_8));
        char[] hexChars = new char[hashBytes.length * 2];
        for (int i = 0; i < hashBytes.length; i++) {
            int value = hashBytes[i] & 0xff;
            hexChars[i * 2] = HEX_ARRAY[value >>> 4];
            hexChars[i * 2 + 1] = HEX_ARRAY[value & 0x0f];
        }
        return new String(hexChars);
    }

    private void rollbackConnection(Connection connection, String message) {

        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            log.error(message, rollbackException);
        }
    }
}
