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

package org.wso2.carbon.apimgt.governance.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.DevportalGovernanceKeyManagerScope;
import org.wso2.carbon.apimgt.governance.api.model.DevportalGovernanceRulesetBinding;
import org.wso2.carbon.apimgt.governance.api.model.DevportalGovernanceRulesetSnapshot;
import org.wso2.carbon.apimgt.governance.api.model.DevportalGovernanceRulesetSnapshotKeyManagerScope;
import org.wso2.carbon.apimgt.governance.api.model.DevportalGovernanceTemplate;
import org.wso2.carbon.apimgt.governance.api.model.DevportalGovernanceTemplateList;
import org.wso2.carbon.apimgt.governance.api.model.KeyManagerGovernanceContext;
import org.wso2.carbon.apimgt.governance.api.model.RuleType;
import org.wso2.carbon.apimgt.governance.api.model.RuleViolation;
import org.wso2.carbon.apimgt.governance.api.model.RulesetContent;
import org.wso2.carbon.apimgt.governance.api.model.RulesetInfo;
import org.wso2.carbon.apimgt.governance.impl.dao.DevportalGovernanceDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.RulesetMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.impl.RulesetMgtDAOImpl;
import org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceUtil;
import org.wso2.carbon.apimgt.governance.impl.util.AuditLogger;
import org.wso2.carbon.apimgt.governance.impl.util.KeyManagerContextResolver;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manager for Admin Portal Devportal Governance templates.
 */
public class DevportalGovernanceManager {

    private final DevportalGovernanceDAO devportalGovernanceDAO;
    private final RulesetMgtDAO rulesetMgtDAO;

    public DevportalGovernanceManager() {

        devportalGovernanceDAO = DevportalGovernanceDAO.getInstance();
        rulesetMgtDAO = RulesetMgtDAOImpl.getInstance();
    }

    /**
     * Create a Devportal Governance template.
     *
     * @param template     template
     * @param organization organization
     * @return created template
     * @throws APIMGovernanceException if an error occurs while creating the template
     */
    public DevportalGovernanceTemplate createTemplate(DevportalGovernanceTemplate template, String organization)
            throws APIMGovernanceException {

        validateTemplate(template);
        DevportalGovernanceTemplate existingTemplate =
                devportalGovernanceDAO.getTemplateByName(template.getName(), organization);
        if (existingTemplate != null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.DEVPORTAL_TEMPLATE_ALREADY_EXISTS,
                    template.getName(), organization);
        }
        validateGlobalTemplatePrivilege(template, organization, template.getCreatedBy());
        template.setId(APIMGovernanceUtil.generateUUID());
        template.setOrganization(organization);
        if (isBlank(template.getStatus())) {
            template.setStatus(DevportalGovernanceTemplate.STATUS_DRAFT);
        }
        prepareRulesetBindings(template, organization);
        if (DevportalGovernanceTemplate.STATUS_PUBLISHED.equals(template.getStatus())) {
            enforceTemplateDefaultsAgainstRulesets(template, organization);
        }

        DevportalGovernanceTemplate createdTemplate =
                devportalGovernanceDAO.createTemplate(template, organization);
        AuditLogger.log("Devportal Governance Template",
                "Template %s with id %s created by user %s in organization %s",
                createdTemplate.getName(), createdTemplate.getId(), template.getCreatedBy(), organization);
        return createdTemplate;
    }

    /**
     * Update a Devportal Governance template.
     *
     * @param templateId   template ID
     * @param template     template
     * @param organization organization
     * @return updated template
     * @throws APIMGovernanceException if an error occurs while updating the template
     */
    public DevportalGovernanceTemplate updateTemplate(String templateId, DevportalGovernanceTemplate template,
                                                      String organization) throws APIMGovernanceException {

        DevportalGovernanceTemplate existingTemplate = getTemplateById(templateId, organization);
        validateGlobalTemplateMutation(existingTemplate, organization, template.getUpdatedBy());
        validateTemplate(template);
        validateGlobalTemplatePrivilege(template, organization, template.getUpdatedBy());
        DevportalGovernanceTemplate existingTemplateByName =
                devportalGovernanceDAO.getTemplateByName(template.getName(), organization);
        if (existingTemplateByName != null && !existingTemplateByName.getId().equals(templateId)) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.DEVPORTAL_TEMPLATE_ALREADY_EXISTS,
                    template.getName(), organization);
        }
        template.setId(templateId);
        template.setOrganization(organization);
        if (isBlank(template.getStatus())) {
            template.setStatus(existingTemplate.getStatus());
        }
        prepareRulesetBindings(template, organization);
        if (DevportalGovernanceTemplate.STATUS_PUBLISHED.equals(template.getStatus())) {
            enforceTemplateDefaultsAgainstRulesets(template, organization);
        }

        DevportalGovernanceTemplate updatedTemplate =
                devportalGovernanceDAO.updateTemplate(templateId, template, organization);
        AuditLogger.log("Devportal Governance Template",
                "Template %s with id %s updated by user %s in organization %s",
                updatedTemplate.getName(), updatedTemplate.getId(), template.getUpdatedBy(), organization);
        return updatedTemplate;
    }

    /**
     * Delete a Devportal Governance template.
     *
     * @param templateId   template ID
     * @param username     username
     * @param organization organization
     * @throws APIMGovernanceException if an error occurs while deleting the template
     */
    public void deleteTemplate(String templateId, String username, String organization) throws APIMGovernanceException {

        DevportalGovernanceTemplate existingTemplate = getTemplateById(templateId, organization);
        validateGlobalTemplateMutation(existingTemplate, organization, username);
        devportalGovernanceDAO.deleteTemplate(templateId, organization);
        AuditLogger.log("Devportal Governance Template",
                "Template %s with id %s deleted by user %s in organization %s",
                existingTemplate.getName(), existingTemplate.getId(), username, organization);
    }

    /**
     * Get a Devportal Governance template by ID.
     *
     * @param templateId   template ID
     * @param organization organization
     * @return template
     * @throws APIMGovernanceException if the template is not found or an error occurs while retrieving it
     */
    public DevportalGovernanceTemplate getTemplateById(String templateId, String organization)
            throws APIMGovernanceException {

        DevportalGovernanceTemplate template = devportalGovernanceDAO.getTemplateById(templateId, organization);
        if (template == null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.DEVPORTAL_TEMPLATE_NOT_FOUND, templateId);
        }
        return template;
    }

    /**
     * Get the default Devportal Governance template.
     *
     * @param organization organization
     * @return default template
     * @throws APIMGovernanceException if the template is not found or an error occurs while retrieving it
     */
    public DevportalGovernanceTemplate getDefaultTemplate(String organization) throws APIMGovernanceException {

        DevportalGovernanceTemplate template = devportalGovernanceDAO.getDefaultTemplate(organization);
        if (template == null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.DEVPORTAL_TEMPLATE_NOT_FOUND, "default");
        }
        return template;
    }

    /**
     * Get all Devportal Governance templates.
     *
     * @param organization organization
     * @return template list
     * @throws APIMGovernanceException if an error occurs while retrieving templates
     */
    public DevportalGovernanceTemplateList getTemplates(String organization) throws APIMGovernanceException {

        return devportalGovernanceDAO.getTemplates(organization);
    }

    private void validateTemplate(DevportalGovernanceTemplate template) throws APIMGovernanceException {

        if (template == null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.BAD_REQUEST,
                    "Devportal Governance template payload is required");
        }
        if (isBlank(template.getName())) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.BAD_REQUEST,
                    "Devportal Governance template name is required");
        }
        // Required+hidden defaults are validated by enforceTemplateDefaultsAgainstRulesets at publish time.
        // Repeating the check here blocks legitimate operations (e.g. toggling isDefault) on existing templates.
    }

    private void validateGlobalTemplatePrivilege(DevportalGovernanceTemplate template, String organization,
                                                 String username) throws APIMGovernanceException {

        if (template.isGlobal() && !canManageGlobalTemplates(organization, username)) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.BAD_REQUEST,
                    "Only super-tenant administrators can create or update global Devportal Governance templates");
        }
    }

    private void validateGlobalTemplateMutation(DevportalGovernanceTemplate template, String organization,
                                                String username) throws APIMGovernanceException {

        if (template.isGlobal() && !canManageGlobalTemplates(organization, username)) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.BAD_REQUEST,
                    "Global Devportal Governance templates are read-only for tenant administrators");
        }
    }

    private boolean canManageGlobalTemplates(String organization, String username) {

        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(organization)) {
            return true;
        }
        return username != null &&
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(MultitenantUtils.getTenantDomain(username));
    }

    private void prepareRulesetBindings(DevportalGovernanceTemplate template, String organization)
            throws APIMGovernanceException {

        List<DevportalGovernanceRulesetBinding> preparedBindings = new ArrayList<>();
        Set<String> rulesetIds = new HashSet<>();
        Set<Integer> bindingOrders = new HashSet<>();
        List<String> allowedKeyManagerNames = resolveAllowedKeyManagerNames(template.getFormConfig());
        int bindingOrder = 0;
        for (DevportalGovernanceRulesetBinding binding : template.getRulesetBindings()) {
            if (isBlank(binding.getRulesetId())) {
                throw new APIMGovernanceException(APIMGovExceptionCodes.BAD_REQUEST,
                        "Ruleset ID is required for each template binding");
            }
            if (!rulesetIds.add(binding.getRulesetId())) {
                throw new APIMGovernanceException(APIMGovExceptionCodes.BAD_REQUEST,
                        "Duplicate ruleset bindings are not allowed for a template");
            }
            if (!devportalGovernanceDAO.isRulesetInOrganization(binding.getRulesetId(), organization)) {
                throw new APIMGovernanceException(APIMGovExceptionCodes.RULESET_NOT_FOUND, binding.getRulesetId());
            }
            RulesetInfo rulesetInfo = rulesetMgtDAO.getRulesetById(binding.getRulesetId(), organization);
            RuleType ruleType = rulesetInfo != null ? rulesetInfo.getRuleType() : null;
            if (isBlank(binding.getBindingId())) {
                binding.setBindingId(APIMGovernanceUtil.generateUUID());
            }
            binding.setTemplateId(template.getId());
            binding.setBindingOrder(binding.getBindingOrder() >= 0 ? binding.getBindingOrder() : bindingOrder);
            if (!bindingOrders.add(binding.getBindingOrder())) {
                throw new APIMGovernanceException(APIMGovExceptionCodes.BAD_REQUEST,
                        "Duplicate binding orders are not allowed for a template");
            }
            binding.setKeyManagerScopes(prepareKeyManagerScopes(binding, ruleType, allowedKeyManagerNames,
                    organization));
            preparedBindings.add(binding);
            bindingOrder++;
        }
        template.setRulesetBindings(preparedBindings);
    }

    /**
     * Returns the set of key-manager names a template considers "governed". Prefers the new
     * per-KM schema {@code formConfig.keyManagers.<kmName>.enabled=true}; falls back to the
     * legacy {@code formConfig.keyGeneration.allowedKeyManagers.defaultValue} list so old
     * snapshots and templates that have not yet been migrated continue to validate.
     *
     * Empty result means the template doesn't restrict — every enabled KM is allowed.
     */
    private List<String> resolveAllowedKeyManagerNames(Map<String, Object> formConfig) {

        if (formConfig == null) {
            return Collections.emptyList();
        }
        // New shape: per-KM dynamic object with enabled flag.
        Map<String, Object> keyManagersSection = asMap(formConfig.get("keyManagers"));
        if (keyManagersSection != null && !keyManagersSection.isEmpty()) {
            List<String> enabledKms = new ArrayList<>();
            for (Map.Entry<String, Object> entry : keyManagersSection.entrySet()) {
                Map<String, Object> kmConfig = asMap(entry.getValue());
                if (kmConfig != null && isTrue(kmConfig.get("enabled"))) {
                    enabledKms.add(entry.getKey());
                }
            }
            if (!enabledKms.isEmpty()) {
                return enabledKms;
            }
        }
        // Legacy shape (kept for backward compat with old templates and existing snapshots).
        Map<String, Object> keyGenSection = asMap(formConfig.get("keyGeneration"));
        if (keyGenSection == null) {
            return Collections.emptyList();
        }
        Map<String, Object> allowedKmConfig = asMap(keyGenSection.get("allowedKeyManagers"));
        if (allowedKmConfig == null) {
            return Collections.emptyList();
        }
        Object defaultValue = allowedKmConfig.get("defaultValue");
        if (defaultValue instanceof List) {
            List<String> names = new ArrayList<>();
            for (Object item : (List<?>) defaultValue) {
                if (item instanceof String) {
                    names.add((String) item);
                }
            }
            return names;
        }
        return Collections.emptyList();
    }

    private List<DevportalGovernanceKeyManagerScope> prepareKeyManagerScopes(
            DevportalGovernanceRulesetBinding binding, RuleType ruleType, List<String> allowedKeyManagerNames,
            String organization) throws APIMGovernanceException {

        List<DevportalGovernanceKeyManagerScope> keyManagerScopes = binding.getKeyManagerScopes();

        if (RuleType.APP_OAUTH != ruleType) {
            if (!keyManagerScopes.isEmpty()) {
                throw new APIMGovernanceException(APIMGovExceptionCodes.BAD_REQUEST,
                        "Key manager scopes are only applicable to APP_OAUTH ruleset bindings");
            }
            return Collections.emptyList();
        }

        List<DevportalGovernanceKeyManagerScope> preparedScopes = new ArrayList<>();
        Set<String> keyManagerUuids = new HashSet<>();
        for (DevportalGovernanceKeyManagerScope keyManagerScope : keyManagerScopes) {
            if (isBlank(keyManagerScope.getKeyManagerUuid())) {
                throw new APIMGovernanceException(APIMGovExceptionCodes.BAD_REQUEST,
                        "Key Manager UUID is required for each template ruleset key manager scope");
            }
            if (!keyManagerUuids.add(keyManagerScope.getKeyManagerUuid())) {
                throw new APIMGovernanceException(APIMGovExceptionCodes.BAD_REQUEST,
                        "Duplicate key manager scopes are not allowed for a template ruleset binding");
            }
            KeyManagerConfigurationDTO kmConfig = getKeyManagerConfig(keyManagerScope.getKeyManagerUuid(),
                    organization);
            if (kmConfig == null) {
                throw new APIMGovernanceException(APIMGovExceptionCodes.BAD_REQUEST,
                        "Key Manager with UUID " + keyManagerScope.getKeyManagerUuid()
                                + " does not exist in organization " + organization);
            }
            if (!allowedKeyManagerNames.isEmpty() && !allowedKeyManagerNames.contains(kmConfig.getName())) {
                throw new APIMGovernanceException(APIMGovExceptionCodes.BAD_REQUEST,
                        "Key Manager '" + kmConfig.getName()
                                + "' is not in the template's allowed key managers list");
            }
            keyManagerScope.setBindingId(binding.getBindingId());
            keyManagerScope.setOrganization(organization);
            preparedScopes.add(keyManagerScope);
        }
        return preparedScopes;
    }

    private KeyManagerConfigurationDTO getKeyManagerConfig(String uuid, String organization)
            throws APIMGovernanceException {

        try {
            return ApiMgtDAO.getInstance().getKeyManagerConfigurationByID(organization, uuid);
        } catch (APIManagementException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.BAD_REQUEST,
                    "Error while looking up Key Manager with UUID " + uuid);
        }
    }

    /**
     * Dry-run validation of a template's hidden field defaults against its bound rulesets.
     * Returns the list of violations without throwing. Used by the preview endpoint so the
     * admin portal can show problems before the admin clicks Publish.
     *
     * @param templateId   template ID
     * @param organization organization
     * @return violations found (empty means safe to publish)
     */
    public List<RuleViolation> validateTemplateDefaults(String templateId, String organization)
            throws APIMGovernanceException {

        DevportalGovernanceTemplate template = getTemplateById(templateId, organization);
        return evaluateHiddenDefaultsAgainstRulesets(template, organization);
    }

    /**
     * Throws if the template's hidden defaults violate its bound rulesets.
     * Called inline during create/update when status == PUBLISHED.
     */
    private void enforceTemplateDefaultsAgainstRulesets(DevportalGovernanceTemplate template, String organization)
            throws APIMGovernanceException {

        List<RuleViolation> violations = evaluateHiddenDefaultsAgainstRulesets(template, organization);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (RuleViolation v : violations) {
                sb.append('[').append(v.getRuleName()).append("] ")
                        .append(v.getRuleMessage())
                        .append(" (at ").append(v.getViolatedPath()).append("); ");
            }
            throw new APIMGovernanceException(APIMGovExceptionCodes.TEMPLATE_DEFAULT_VIOLATES_RULESET,
                    sb.toString().trim());
        }
    }

    /**
     * Evaluates a template's hidden defaults against its bound rulesets across all three
     * governable sections — application, subscription, and per-KM key generation. Each
     * section synthesizes a payload that mirrors the runtime sync-validation shape so a
     * rule that fires at runtime also fires in the publish dry-run.
     *
     * The returned list aggregates violations from every section. Paths that don't
     * correspond to a hidden default in this template are filtered out so unrelated
     * runtime-only checks don't block publish.
     */
    private List<RuleViolation> evaluateHiddenDefaultsAgainstRulesets(DevportalGovernanceTemplate template,
                                                                        String organization)
            throws APIMGovernanceException {

        List<DevportalGovernanceRulesetSnapshot> rulesets = buildRulesetSnapshots(template, organization);
        if (rulesets.isEmpty()) {
            return new ArrayList<>();
        }

        List<RuleViolation> all = new ArrayList<>();
        all.addAll(evaluateApplicationHidden(template,
                filterRulesetsByRuleType(rulesets, "APP_INFO"), organization));
        // Subscription-level Devportal Governance is out of scope for the current iteration.
        // The synthesizer remains in the codebase for binary compat / future re-enable, but
        // the publish-time dry-run pipeline does not invoke it.
        all.addAll(evaluateKeyManagerHidden(template,
                filterRulesetsByRuleType(rulesets, "APP_OAUTH"), organization));
        return all;
    }

    private List<DevportalGovernanceRulesetSnapshot> filterRulesetsByRuleType(
            List<DevportalGovernanceRulesetSnapshot> rulesets, String expectedRuleType) {

        List<DevportalGovernanceRulesetSnapshot> matched = new ArrayList<>();
        for (DevportalGovernanceRulesetSnapshot rs : rulesets) {
            if (expectedRuleType.equalsIgnoreCase(rs.getRulesetType())) {
                matched.add(rs);
            }
        }
        return matched;
    }

    private List<RuleViolation> evaluateApplicationHidden(DevportalGovernanceTemplate template,
                                                          List<DevportalGovernanceRulesetSnapshot> rulesets,
                                                          String organization) throws APIMGovernanceException {

        Map<String, Object> formConfig = template.getFormConfig();
        Map<String, Object> appSection = asMap(formConfig.get("application"));

        Set<String> hiddenPaths = new HashSet<>();
        Map<String, Object> application = new LinkedHashMap<>();

        addHiddenField(appSection, "throttlingPolicy", application, hiddenPaths, "application");
        addHiddenField(appSection, "description", application, hiddenPaths, "application");

        Map<String, Object> groups = asMap(appSection != null ? appSection.get("groups") : null);
        if (isActiveHidden(groups)) {
            hiddenPaths.add("application.groups");
            Object defaultValue = groups.get("defaultValue");
            List<String> groupList = defaultValue != null ? toStringList(defaultValue) : new ArrayList<>();
            application.put("groups", groupList);
        }

        Map<String, Object> attributes = asMap(appSection != null ? appSection.get("attributes") : null);
        if (attributes != null) {
            Map<String, Object> hiddenAttrs = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                Map<String, Object> attrConfig = asMap(entry.getValue());
                if (attrConfig == null) {
                    continue;
                }
                if (isActiveHidden(attrConfig)) {
                    hiddenPaths.add("application.attributes." + entry.getKey());
                    Object defaultValue = attrConfig.get("defaultValue");
                    hiddenAttrs.put(entry.getKey(), defaultValue != null ? defaultValue : "");
                }
            }
            if (!hiddenAttrs.isEmpty()) {
                application.put("attributes", hiddenAttrs);
            }
        }

        if (hiddenPaths.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("operation", "POST /applications");
        payload.put("organization", organization);
        payload.put("templateId", template.getId());
        payload.put("application", application);
        return runValidation(payload, rulesets, null, organization, hiddenPaths, template.getId());
    }

    /**
     * Synthesize a per-KM key-generation payload for every governed Key Manager in the template.
     * Each KM gets its own validation pass scoped to rulesets that match by KM UUID/name (or that
     * are unscoped). Field names match the runtime sync payload (grantTypesToBeSupported,
     * callbackUrl, additionalProperties.{application_access_token_expiry_time, …}) so rules
     * authored once fire in both places.
     */
    private List<RuleViolation> evaluateKeyManagerHidden(DevportalGovernanceTemplate template,
                                                        List<DevportalGovernanceRulesetSnapshot> rulesets,
                                                        String organization) throws APIMGovernanceException {

        Map<String, Map<String, Object>> governedKms = governedKeyManagerSections(template.getFormConfig());
        if (governedKms.isEmpty()) {
            return new ArrayList<>();
        }
        List<RuleViolation> all = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> entry : governedKms.entrySet()) {
            String kmIdentifier = entry.getKey();
            Map<String, Object> section = entry.getValue();

            Set<String> hiddenPaths = new HashSet<>();
            Map<String, Object> additionalProperties = new LinkedHashMap<>();

            // grantTypes (template) → grantTypesToBeSupported (runtime payload)
            Map<String, Object> grantTypesConfig = asMap(section.get("grantTypes"));
            List<String> hiddenGrantTypes = null;
            if (isActiveHidden(grantTypesConfig)) {
                hiddenPaths.add("grantTypesToBeSupported");
                Object def = grantTypesConfig.get("defaultValue");
                hiddenGrantTypes = def != null ? toStringList(def) : new ArrayList<>();
            }

            String hiddenCallbackUrl = readHiddenStringValue(section, "callbackUrl", hiddenPaths,
                    "callbackUrl");

            populateExpiryAndPkce(section, additionalProperties, hiddenPaths);

            if (hiddenPaths.isEmpty()) {
                continue;
            }

            KeyManagerGovernanceContext kmContext = KeyManagerContextResolver.resolve(kmIdentifier, organization);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("operation", "POST /applications/{id}/generate-keys");
            payload.put("action", "OAUTH_APP_CREATE");
            payload.put("organization", organization);
            payload.put("templateId", template.getId());
            payload.put("keyManager", kmContext == null ? kmIdentifier : kmContext.getName());
            if (hiddenGrantTypes != null) {
                payload.put("grantTypesToBeSupported", hiddenGrantTypes);
            }
            if (hiddenCallbackUrl != null) {
                payload.put("callbackUrl", hiddenCallbackUrl);
            }
            if (!additionalProperties.isEmpty()) {
                payload.put("additionalProperties", additionalProperties);
            }
            payload.put("keyManagerContext", kmContext == null ? null : kmContext.toPayloadMap());

            List<DevportalGovernanceRulesetSnapshot> kmScoped =
                    filterRulesetsByKeyManager(rulesets, kmContext, kmIdentifier);
            all.addAll(runValidation(payload, kmScoped, null, organization, hiddenPaths, template.getId()));
        }
        return all;
    }

    /**
     * Returns the per-KM defaults section for every Key Manager the template governs. Reads the
     * canonical {@code formConfig.keyManagers.<km>} shape and falls back to legacy flat
     * {@code formConfig.keyGeneration} (treated as Resident-only, matching pre-migration semantics).
     */
    private Map<String, Map<String, Object>> governedKeyManagerSections(Map<String, Object> formConfig) {

        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        if (formConfig == null) {
            return result;
        }
        Map<String, Object> keyManagersSection = asMap(formConfig.get("keyManagers"));
        if (keyManagersSection != null && !keyManagersSection.isEmpty()) {
            for (Map.Entry<String, Object> entry : keyManagersSection.entrySet()) {
                Map<String, Object> kmConfig = asMap(entry.getValue());
                if (kmConfig != null && isTrue(kmConfig.get("enabled"))) {
                    result.put(entry.getKey(), kmConfig);
                }
            }
            if (!result.isEmpty()) {
                return result;
            }
        }
        Map<String, Object> legacy = asMap(formConfig.get("keyGeneration"));
        if (legacy != null) {
            result.put("Resident Key Manager", legacy);
        }
        return result;
    }

    private String readHiddenStringValue(Map<String, Object> section, String fieldName, Set<String> hiddenPaths,
                                         String payloadPath) {

        Map<String, Object> fieldConfig = asMap(section.get(fieldName));
        if (!isActiveHidden(fieldConfig)) {
            return null;
        }
        hiddenPaths.add(payloadPath);
        Object def = fieldConfig.get("defaultValue");
        return def == null ? "" : String.valueOf(def);
    }

    private void populateExpiryAndPkce(Map<String, Object> section, Map<String, Object> additionalProperties,
                                       Set<String> hiddenPaths) {

        addAdditionalPropertyIfHidden(section, "appAccessTokenExpiry",
                "application_access_token_expiry_time", additionalProperties, hiddenPaths);
        addAdditionalPropertyIfHidden(section, "userAccessTokenExpiry",
                "user_access_token_expiry_time", additionalProperties, hiddenPaths);
        addAdditionalPropertyIfHidden(section, "refreshTokenExpiry",
                "refresh_token_expiry_time", additionalProperties, hiddenPaths);
        addAdditionalPropertyIfHidden(section, "idTokenExpiry",
                "id_token_expiry_time", additionalProperties, hiddenPaths);
        addAdditionalPropertyIfHidden(section, "enablePKCE",
                "pkceMandatory", additionalProperties, hiddenPaths);
        addAdditionalPropertyIfHidden(section, "pkceSupportsPlainText",
                "pkceSupportPlain", additionalProperties, hiddenPaths);
        addAdditionalPropertyIfHidden(section, "publicClient",
                "bypassClientCredentials", additionalProperties, hiddenPaths);
    }

    private void addAdditionalPropertyIfHidden(Map<String, Object> section, String fieldName, String runtimeKey,
                                               Map<String, Object> additionalProperties, Set<String> hiddenPaths) {

        Map<String, Object> fieldConfig = asMap(section.get(fieldName));
        if (!isActiveHidden(fieldConfig)) {
            return;
        }
        hiddenPaths.add("additionalProperties." + runtimeKey);
        Object def = fieldConfig.get("defaultValue");
        additionalProperties.put(runtimeKey, def == null ? "" : def);
    }

    /**
     * Filters rulesets so a KM's dry-run only sees rules either unscoped or scoped to that KM.
     * Mirrors the runtime filter in {@code DevportalGovernanceValidationUtil} so dry-run results
     * match what the developer hits at request time.
     */
    private List<DevportalGovernanceRulesetSnapshot> filterRulesetsByKeyManager(
            List<DevportalGovernanceRulesetSnapshot> rulesets, KeyManagerGovernanceContext kmContext,
            String fallbackIdentifier) {

        List<DevportalGovernanceRulesetSnapshot> matched = new ArrayList<>();
        for (DevportalGovernanceRulesetSnapshot rs : rulesets) {
            List<DevportalGovernanceRulesetSnapshotKeyManagerScope> scopes = rs.getKeyManagerScopes();
            if (scopes == null || scopes.isEmpty()) {
                matched.add(rs);
                continue;
            }
            for (DevportalGovernanceRulesetSnapshotKeyManagerScope scope : scopes) {
                if (matchesKm(scope, kmContext, fallbackIdentifier)) {
                    matched.add(rs);
                    break;
                }
            }
        }
        return matched;
    }

    private boolean matchesKm(DevportalGovernanceRulesetSnapshotKeyManagerScope scope,
                              KeyManagerGovernanceContext kmContext, String fallbackIdentifier) {

        if (kmContext != null) {
            if (kmContext.getUuid() != null && kmContext.getUuid().equals(scope.getKeyManagerUuid())) {
                return true;
            }
            if (kmContext.getName() != null && kmContext.getName().equals(scope.getKeyManagerName())) {
                return true;
            }
        }
        if (fallbackIdentifier != null) {
            return fallbackIdentifier.equals(scope.getKeyManagerUuid())
                    || fallbackIdentifier.equals(scope.getKeyManagerName());
        }
        return false;
    }

    private List<RuleViolation> runValidation(Map<String, Object> payload,
                                              List<DevportalGovernanceRulesetSnapshot> rulesets,
                                              String applicationUuid, String organization,
                                              Set<String> hiddenPaths, String templateId)
            throws APIMGovernanceException {

        if (rulesets.isEmpty() || hiddenPaths.isEmpty()) {
            return new ArrayList<>();
        }
        String payloadJson;
        try {
            payloadJson = new ObjectMapper().writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_VALIDATING_TEMPLATE_DEFAULTS,
                    e, templateId);
        }
        List<RuleViolation> all = new DevportalGovernanceValidator()
                .validate(payloadJson, rulesets, applicationUuid, organization);
        List<RuleViolation> relevant = new ArrayList<>();
        for (RuleViolation v : all) {
            String path = v.getViolatedPath() == null ? "" : v.getViolatedPath();
            for (String hiddenPath : hiddenPaths) {
                if (pathMatches(path, hiddenPath)) {
                    relevant.add(v);
                    break;
                }
            }
        }
        return relevant;
    }

    private boolean pathMatches(String violatedPath, String hiddenPath) {

        if (violatedPath == null || hiddenPath == null) {
            return false;
        }
        // Spectral emits paths in two notations:
        //   dot:    "subscription.throttlingPolicy"
        //   bracket:"[subscription][throttlingPolicy]"
        // Normalize bracket → dot for comparison.
        String normalized = normalizePath(violatedPath);
        if (normalized.equals(hiddenPath)) {
            return true;
        }
        if (normalized.startsWith(hiddenPath) && normalized.length() > hiddenPath.length()) {
            char next = normalized.charAt(hiddenPath.length());
            return next == '.' || next == '[';
        }
        return false;
    }

    private String normalizePath(String path) {

        if (path == null || path.isEmpty()) {
            return path;
        }
        // [a][b][c] → a.b.c ; [a][0][b] → a.0.b. Array indices lose their brackets — fine
        // for the equality/prefix check we use here.
        return path.replace("][", ".").replace("[", "").replace("]", "");
    }

    private List<DevportalGovernanceRulesetSnapshot> buildRulesetSnapshots(DevportalGovernanceTemplate template,
                                                                           String organization)
            throws APIMGovernanceException {

        List<DevportalGovernanceRulesetSnapshot> rulesets = new ArrayList<>();
        for (DevportalGovernanceRulesetBinding binding : template.getRulesetBindings()) {
            RulesetInfo rulesetInfo = rulesetMgtDAO.getRulesetById(binding.getRulesetId(), organization);
            RulesetContent rulesetContent = rulesetMgtDAO.getRulesetContent(binding.getRulesetId(), organization);
            if (rulesetInfo == null || rulesetContent == null) {
                throw new APIMGovernanceException(APIMGovExceptionCodes.RULESET_NOT_FOUND,
                        binding.getRulesetId());
            }

            String yamlContent = new String(rulesetContent.getContent(), StandardCharsets.UTF_8);
            DevportalGovernanceRulesetSnapshot rulesetSnapshot = new DevportalGovernanceRulesetSnapshot();
            rulesetSnapshot.setSnapshotRulesetId(APIMGovernanceUtil.generateUUID());
            rulesetSnapshot.setSourceRulesetId(binding.getRulesetId());
            rulesetSnapshot.setRulesetName(rulesetInfo.getName());
            rulesetSnapshot.setRulesetDescription(rulesetInfo.getDescription());
            rulesetSnapshot.setArtifactType(rulesetInfo.getArtifactType() == null
                    ? null : rulesetInfo.getArtifactType().name());
            rulesetSnapshot.setRulesetType(rulesetInfo.getRuleType() == null
                    ? null : rulesetInfo.getRuleType().name());
            rulesetSnapshot.setYamlContent(yamlContent);
            rulesetSnapshot.setBindingOrder(binding.getBindingOrder());
            rulesetSnapshot.setKeyManagerScopes(buildRulesetSnapshotKeyManagerScopes(
                    binding.getKeyManagerScopes(), rulesetSnapshot.getSnapshotRulesetId(), organization));
            rulesets.add(rulesetSnapshot);
        }
        return rulesets;
    }

    private List<DevportalGovernanceRulesetSnapshotKeyManagerScope> buildRulesetSnapshotKeyManagerScopes(
            List<DevportalGovernanceKeyManagerScope> keyManagerScopes, String snapshotRulesetId, String organization)
            throws APIMGovernanceException {

        List<DevportalGovernanceRulesetSnapshotKeyManagerScope> snapshotScopes = new ArrayList<>();
        for (DevportalGovernanceKeyManagerScope keyManagerScope : keyManagerScopes) {
            DevportalGovernanceRulesetSnapshotKeyManagerScope snapshotScope =
                    new DevportalGovernanceRulesetSnapshotKeyManagerScope();
            snapshotScope.setSnapshotRulesetId(snapshotRulesetId);
            snapshotScope.setKeyManagerUuid(keyManagerScope.getKeyManagerUuid());
            KeyManagerConfigurationDTO kmConfig = getKeyManagerConfig(keyManagerScope.getKeyManagerUuid(),
                    organization);
            snapshotScope.setKeyManagerName(kmConfig == null ? null : kmConfig.getName());
            snapshotScopes.add(snapshotScope);
        }
        return snapshotScopes;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {

        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return null;
    }

    private boolean isActiveHidden(Map<String, Object> fieldConfig) {

        if (fieldConfig == null) {
            return false;
        }
        if (!isActive(fieldConfig)) {
            return false;
        }
        Object hidden = fieldConfig.get("hidden");
        return isTrue(hidden);
    }

    private boolean isActive(Map<String, Object> fieldConfig) {

        Object active = fieldConfig.get("active");
        return active == null || (!Boolean.FALSE.equals(active) && !"false".equals(active));
    }

    private boolean isTrue(Object value) {

        return Boolean.TRUE.equals(value) || "true".equals(value);
    }

    private void addHiddenField(Map<String, Object> section, String key,
                                 Map<String, Object> target, Set<String> hiddenPaths,
                                 String parentPath) {

        if (section == null) {
            return;
        }
        Map<String, Object> fieldConfig = asMap(section.get(key));
        if (fieldConfig == null) {
            return;
        }
        Object hidden = fieldConfig.get("hidden");
        if (!isTrue(hidden)) {
            return;
        }
        String fullPath = parentPath == null || parentPath.isEmpty() ? key : parentPath + "." + key;
        hiddenPaths.add(fullPath);
        Object defaultValue = fieldConfig.get("defaultValue");
        // Always include the field even with an empty/null default so that Spectral rules
        // (e.g. minLength, pattern) evaluate the actual value the developer will receive.
        target.put(key, defaultValue != null ? defaultValue : "");
    }

    private List<String> toStringList(Object value) {

        List<String> result = new ArrayList<>();
        if (value instanceof List) {
            for (Object item : (List<?>) value) {
                if (item != null && !item.toString().trim().isEmpty()) {
                    result.add(item.toString().trim());
                }
            }
        } else if (value instanceof String) {
            for (String part : ((String) value).split(",")) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    result.add(trimmed);
                }
            }
        }
        return result;
    }

    private boolean isBlank(String value) {

        return value == null || value.trim().isEmpty();
    }
}
