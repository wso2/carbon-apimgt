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

package org.wso2.carbon.apimgt.rest.api.store.v1.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.DevportalGovernanceApplicationSnapshot;
import org.wso2.carbon.apimgt.governance.api.model.DevportalGovernanceTemplate;
import org.wso2.carbon.apimgt.governance.impl.DevportalGovernanceManager;
import org.wso2.carbon.apimgt.governance.impl.dao.DevportalGovernanceDAO;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hydrates request DTOs from a Devportal Governance template's {@code formConfig}
 * before validation/persistence.
 *
 * Behavior per field under {@code formConfig.<section>.<field>}:
 *   - hidden=true  -> always force the value to {@code defaultValue} (admin override)
 *   - hidden=false -> only fill {@code defaultValue} when the incoming value is blank/empty
 *
 * Application creation reads the live template (via templateId on the request).
 * Subscription and key-generation read the snapshot already attached to the
 * application, so admin-locked defaults stay consistent with the snapshot the
 * compliance evaluator uses.
 *
 * Silent no-op when the template/snapshot lookup fails — governance defaults
 * must never break a happy-path consumer flow. Failures are logged at debug.
 */
public final class TemplateDefaultsApplier {

    private static final String SECTION_APPLICATION = "application";
    private static final String SECTION_SUBSCRIPTION = "subscription";
    /** Legacy section — kept readable for old snapshots; new templates write under {@link #SECTION_KEY_MANAGERS}. */
    private static final String SECTION_KEY_GENERATION = "keyGeneration";
    private static final String SECTION_KEY_MANAGERS = "keyManagers";

    private static final String KEY_DEFAULT_VALUE = "defaultValue";
    private static final String KEY_HIDDEN = "hidden";
    private static final String KEY_REQUIRED = "required";
    private static final String KEY_ENABLED = "enabled";
    private static final String RESIDENT_KEY_MANAGER_NAME = "Resident Key Manager";

    private static final String FIELD_THROTTLING_POLICY = "throttlingPolicy";
    private static final String FIELD_TOKEN_TYPE = "tokenType";
    private static final String FIELD_DESCRIPTION = "description";
    private static final String FIELD_GROUPS = "groups";
    private static final String FIELD_ATTRIBUTES = "attributes";
    private static final String FIELD_GRANT_TYPES = "grantTypes";
    private static final String FIELD_CALLBACK_URL = "callbackUrl";

    // Expiry fields (new schema)
    private static final String FIELD_APP_ACCESS_TOKEN_EXPIRY = "appAccessTokenExpiry";
    private static final String FIELD_USER_ACCESS_TOKEN_EXPIRY = "userAccessTokenExpiry";
    private static final String FIELD_REFRESH_TOKEN_EXPIRY = "refreshTokenExpiry";
    private static final String FIELD_ID_TOKEN_EXPIRY = "idTokenExpiry";

    // PKCE / client fields
    private static final String FIELD_ENABLE_PKCE = "enablePKCE";
    private static final String FIELD_PKCE_SUPPORTS_PLAIN_TEXT = "pkceSupportsPlainText";
    private static final String FIELD_PUBLIC_CLIENT = "publicClient";

    // Backward compat: old single-field name still stored in some templates
    private static final String FIELD_VALIDITY_PERIOD = "validityPeriod";

    // WSO2 internal additionalProperties keys
    private static final String APIM_KEY_APP_TOKEN_EXPIRY = "application_access_token_expiry_time";
    private static final String APIM_KEY_USER_TOKEN_EXPIRY = "user_access_token_expiry_time";
    private static final String APIM_KEY_REFRESH_TOKEN_EXPIRY = "refresh_token_expiry_time";
    private static final String APIM_KEY_ID_TOKEN_EXPIRY = "id_token_expiry_time";
    private static final String APIM_KEY_PKCE_MANDATORY = "pkceMandatory";
    private static final String APIM_KEY_PKCE_SUPPORT_PLAIN = "pkceSupportPlain";
    private static final String APIM_KEY_BYPASS_CLIENT_CREDENTIALS = "bypassClientCredentials";

    private TemplateDefaultsApplier() {

    }

    public static void applyTo(ApplicationDTO body, String username, String organization, Log log) {

        if (body == null) {
            return;
        }
        String templateId = body.getTemplateId();
        if (StringUtils.isBlank(templateId)) {
            return;
        }

        DevportalGovernanceTemplate template;
        try {
            template = new DevportalGovernanceManager().getTemplateById(templateId, organization);
        } catch (APIMGovernanceException e) {
            if (log != null && log.isDebugEnabled()) {
                log.debug("Skipping template defaults — template lookup failed for id=" + templateId, e);
            }
            return;
        }
        if (template == null || template.getFormConfig() == null) {
            return;
        }

        Map<String, Object> appSection = asMap(template.getFormConfig().get(SECTION_APPLICATION));
        if (appSection == null) {
            return;
        }
        Map<String, Boolean> configuredAttributes = getConfiguredApplicationAttributeVisibility(username, log);

        applyStringField(appSection, FIELD_THROTTLING_POLICY, body.getThrottlingPolicy(), body::setThrottlingPolicy);
        applyStringField(appSection, FIELD_DESCRIPTION, body.getDescription(), body::setDescription);
        validateRequiredString(asMap(appSection.get(FIELD_DESCRIPTION)), body.getDescription(),
                "Application description", log);

        applyGroupsField(appSection, body, log);

        Map<String, Object> attributesSection = asMap(appSection.get(FIELD_ATTRIBUTES));
        if (attributesSection != null && !attributesSection.isEmpty()) {
            Map<String, String> attributes = body.getAttributes();
            if (attributes == null) {
                attributes = new HashMap<>();
            } else {
                attributes = new HashMap<>(attributes);
            }
            Map<String, String> mutableAttributes = attributes;
            for (Map.Entry<String, Object> entry : attributesSection.entrySet()) {
                String attributeName = entry.getKey();
                if (StringUtils.isBlank(attributeName)) {
                    continue;
                }
                if (configuredAttributes != null
                        && (!configuredAttributes.containsKey(attributeName)
                        || configuredAttributes.get(attributeName))) {
                    continue;
                }
                Map<String, Object> attributeConfig = asMap(entry.getValue());
                applyValueField(attributeConfig, mutableAttributes.get(attributeName),
                        value -> mutableAttributes.put(attributeName, value));
                validateRequiredString(attributeConfig, mutableAttributes.get(attributeName),
                        "Application attribute " + attributeName, log);
            }
            body.setAttributes(mutableAttributes);
        }

        // tokenType kept for backward compatibility with older templates that still carry the field
        String currentTokenType = body.getTokenType() == null ? null : body.getTokenType().toString();
        applyStringField(appSection, FIELD_TOKEN_TYPE, currentTokenType, value -> {
            try {
                body.setTokenType(ApplicationDTO.TokenTypeEnum.valueOf(value));
            } catch (IllegalArgumentException ignored) {
                // unknown enum value in template default — leave the existing value alone
            }
        });
    }

    private static void applyGroupsField(Map<String, Object> appSection, ApplicationDTO body, Log log) {

        Map<String, Object> fieldConfig = asMap(appSection.get(FIELD_GROUPS));
        if (fieldConfig == null) {
            return;
        }
        if (!APIUtil.isMultiGroupAppSharingEnabled()) {
            body.setGroups(null);
            return;
        }

        List<String> defaultList = coerceStringList(fieldConfig.get(KEY_DEFAULT_VALUE));
        boolean hidden = isTrue(fieldConfig.get(KEY_HIDDEN));
        List<String> currentGroups = body.getGroups();
        if (defaultList != null) {
            if (hidden) {
                body.setGroups(new ArrayList<>(defaultList));
            } else if ((currentGroups == null || currentGroups.isEmpty()) && !defaultList.isEmpty()) {
                body.setGroups(new ArrayList<>(defaultList));
            }
        }
        validateRequiredList(fieldConfig, body.getGroups(), "Application groups", log);
    }

    /**
     * Hydrate a {@link SubscriptionDTO} from the governance snapshot attached to its application.
     * Currently only {@code subscription.throttlingPolicy} is wired; extend by adding more
     * {@link #applyStringField} calls.
     */
    public static void applyToSubscription(SubscriptionDTO body, String organization, Log log) {

        if (body == null) {
            return;
        }
        Map<String, Object> formConfig = lookupFormConfigByApplicationUuid(body.getApplicationId(), log);
        if (formConfig == null) {
            return;
        }
        Map<String, Object> section = asMap(formConfig.get(SECTION_SUBSCRIPTION));
        if (section == null) {
            return;
        }

        applyStringField(section, FIELD_THROTTLING_POLICY, body.getThrottlingPolicy(), body::setThrottlingPolicy);
    }

    /**
     * Hydrate an {@link ApplicationKeyGenerateRequestDTO} from the governance snapshot attached
     * to the application identified by {@code applicationUuid}. Honors:
     *   - {@code keyGeneration.grantTypes} (list, or comma-separated string)
     *   - {@code keyGeneration.callbackUrl}
     *   - {@code keyGeneration.appAccessTokenExpiry} → additionalProperties["application_access_token_expiry_time"]
     *   - {@code keyGeneration.userAccessTokenExpiry} → additionalProperties["user_access_token_expiry_time"]
     *   - {@code keyGeneration.refreshTokenExpiry}   → additionalProperties["refresh_token_expiry_time"]
     *   - {@code keyGeneration.idTokenExpiry}        → additionalProperties["id_token_expiry_time"]
     *   - {@code keyGeneration.enablePKCE}           → additionalProperties["pkceMandatory"]
     *   - {@code keyGeneration.pkceSupportsPlainText}→ additionalProperties["pkceSupportPlain"]
     *
     * Backward compat: old {@code validityPeriod} field (single expiry) is mapped to
     * {@code application_access_token_expiry_time} when the new field is absent.
     */
    public static void applyToKeyGen(String applicationUuid, ApplicationKeyGenerateRequestDTO body,
                                     String organization, Log log) {

        if (body == null) {
            return;
        }
        Map<String, Object> formConfig = lookupFormConfigByApplicationUuid(applicationUuid, log);
        if (formConfig == null) {
            return;
        }
        String requestedKm = StringUtils.defaultIfBlank(body.getKeyManager(), RESIDENT_KEY_MANAGER_NAME);
        Map<String, Object> section = resolveKeyManagerSection(formConfig, requestedKm);
        if (section == null) {
            return;
        }

        // Grant types are an ALLOWED-LIST contract, NOT a "hideable scalar":
        //   - When the developer left the list empty, fill it with the admin's allowed set
        //     so the request can proceed against the KM.
        //   - When the developer supplied something, leave their selection alone here —
        //     subset enforcement (rejecting grants outside the allowed list) lives in
        //     {@code DevportalGovernanceValidationUtil#validateKeyGeneration}, where it
        //     can produce a clean 400 with the offending grant name.
        // The legacy `hidden=true` flag (present on old snapshots) is intentionally ignored;
        // the model is no longer "force-override".
        applyAllowedGrantTypes(section, body);

        applyStringField(section, FIELD_CALLBACK_URL, body.getCallbackUrl(), body::setCallbackUrl);

        // Ensure additionalProperties map is mutable before writing into it.
        Map<String, String> additionalProps = coerceStringMap(body.getAdditionalProperties());
        body.setAdditionalProperties(additionalProps);

        // Expiry times — new schema fields
        applyAdditionalPropField(section, FIELD_APP_ACCESS_TOKEN_EXPIRY, additionalProps, APIM_KEY_APP_TOKEN_EXPIRY);
        applyAdditionalPropField(section, FIELD_USER_ACCESS_TOKEN_EXPIRY, additionalProps, APIM_KEY_USER_TOKEN_EXPIRY);
        applyAdditionalPropField(section, FIELD_REFRESH_TOKEN_EXPIRY, additionalProps, APIM_KEY_REFRESH_TOKEN_EXPIRY);
        applyAdditionalPropField(section, FIELD_ID_TOKEN_EXPIRY, additionalProps, APIM_KEY_ID_TOKEN_EXPIRY);

        // Backward compat: old single validityPeriod maps to app access token expiry when new field absent
        if (!additionalProps.containsKey(APIM_KEY_APP_TOKEN_EXPIRY)) {
            applyAdditionalPropField(section, FIELD_VALIDITY_PERIOD, additionalProps, APIM_KEY_APP_TOKEN_EXPIRY);
        }

        // PKCE + public client
        applyAdditionalPropField(section, FIELD_ENABLE_PKCE, additionalProps, APIM_KEY_PKCE_MANDATORY);
        applyAdditionalPropField(section, FIELD_PKCE_SUPPORTS_PLAIN_TEXT, additionalProps, APIM_KEY_PKCE_SUPPORT_PLAIN);
        applyAdditionalPropField(section, FIELD_PUBLIC_CLIENT, additionalProps, APIM_KEY_BYPASS_CLIENT_CREDENTIALS);
    }

    /**
     * Resolves the per-KM defaults section out of {@code formConfig}, preferring the new
     * {@code keyManagers.<kmName>} shape and falling back to the legacy flat
     * {@code keyGeneration} section. Returns {@code null} when:
     *   - the new-shape entry exists but is disabled (governance is opt-in per KM),
     *   - or neither shape carries any defaults for the requested KM.
     *
     * Old snapshots stored under {@code keyGeneration} apply to every KM (the legacy semantic);
     * new per-KM entries apply only to the matching KM, which is what admins now expect.
     */
    /**
     * Read the admin's allowed grant-types list from a per-KM template section.
     * Returns an empty list when the template doesn't constrain grants
     * (i.e. "any grant the KM advertises is allowed"). The legacy {@code hidden}
     * flag is deliberately ignored — the new model treats this field as an
     * allowed-list constraint, not a force-override.
     */
    public static List<String> readAllowedGrantTypes(Map<String, Object> section) {

        if (section == null) {
            return new ArrayList<>();
        }
        Map<String, Object> field = asMap(section.get(FIELD_GRANT_TYPES));
        if (field == null) {
            return new ArrayList<>();
        }
        List<String> allowed = coerceStringList(field.get(KEY_DEFAULT_VALUE));
        return allowed == null ? new ArrayList<>() : allowed;
    }

    /**
     * Same as {@link #readAllowedGrantTypes(Map)} but resolves the per-KM section
     * straight from the application's snapshot, with the same legacy fallback the
     * runtime {@link #applyToKeyGen} uses.
     */
    public static List<String> readAllowedGrantTypes(Map<String, Object> formConfig, String keyManagerName) {

        if (formConfig == null) {
            return new ArrayList<>();
        }
        String resolvedKm = StringUtils.defaultIfBlank(keyManagerName, RESIDENT_KEY_MANAGER_NAME);
        Map<String, Object> section = resolveKeyManagerSection(formConfig, resolvedKm);
        return readAllowedGrantTypes(section);
    }

    /**
     * Allowed-list "fill if empty" semantic for grant types. When the developer
     * supplied any grants we leave them alone here; the validator runs subset
     * enforcement and returns a clean 400 if a requested grant isn't in the
     * allowed list.
     */
    private static void applyAllowedGrantTypes(Map<String, Object> section,
                                                ApplicationKeyGenerateRequestDTO body) {

        List<String> allowed = readAllowedGrantTypes(section);
        if (allowed.isEmpty()) {
            return;
        }
        List<String> current = body.getGrantTypesToBeSupported();
        if (current == null || current.isEmpty()) {
            body.setGrantTypesToBeSupported(new ArrayList<>(allowed));
        }
    }

    private static Map<String, Object> resolveKeyManagerSection(Map<String, Object> formConfig,
                                                                 String requestedKeyManager) {

        Map<String, Object> keyManagersSection = asMap(formConfig.get(SECTION_KEY_MANAGERS));
        if (keyManagersSection != null && !keyManagersSection.isEmpty()) {
            Map<String, Object> kmConfig = asMap(keyManagersSection.get(requestedKeyManager));
            if (kmConfig != null && isTrue(kmConfig.get(KEY_ENABLED))) {
                return kmConfig;
            }
            // Per-KM entry exists but for a different KM (or disabled) — opt out, do not
            // bleed defaults across KM scopes.
            return null;
        }
        return asMap(formConfig.get(SECTION_KEY_GENERATION));
    }

    private static Map<String, Object> lookupFormConfigByApplicationUuid(String applicationUuid, Log log) {

        if (StringUtils.isBlank(applicationUuid)) {
            return null;
        }
        try {
            Application application = ApiMgtDAO.getInstance().getApplicationByUUID(applicationUuid);
            if (application == null) {
                return null;
            }
            DevportalGovernanceApplicationSnapshot snapshot = DevportalGovernanceDAO.getInstance()
                    .getApplicationSnapshot(application.getId());
            if (snapshot == null) {
                return null;
            }
            Map<String, Object> formConfig = snapshot.getFormConfig();
            return (formConfig == null || formConfig.isEmpty()) ? null : formConfig;
        } catch (APIManagementException | APIMGovernanceException e) {
            if (log != null && log.isDebugEnabled()) {
                log.debug("Skipping template defaults — snapshot lookup failed for application uuid="
                        + applicationUuid, e);
            }
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object value) {

        return value instanceof Map ? (Map<String, Object>) value : null;
    }

    private static Map<String, String> coerceStringMap(Object value) {

        Map<String, String> result = new HashMap<>();
        if (!(value instanceof Map)) {
            return result;
        }
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
            if (entry.getKey() != null) {
                Object entryValue = entry.getValue();
                result.put(String.valueOf(entry.getKey()), entryValue == null ? null : String.valueOf(entryValue));
            }
        }
        return result;
    }

    private static Map<String, Boolean> getConfiguredApplicationAttributeVisibility(String username, Log log) {

        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            JSONArray attributeArray = apiConsumer.getAppAttributesFromConfig(username);
            if (attributeArray == null) {
                return new HashMap<>();
            }
            Map<String, Boolean> attributeVisibility = new HashMap<>();
            for (Object object : attributeArray) {
                if (object instanceof JSONObject) {
                    Object attributeName = ((JSONObject) object).get(APIConstants.ApplicationAttributes.ATTRIBUTE);
                    if (attributeName != null && StringUtils.isNotBlank(String.valueOf(attributeName))) {
                        Object hidden = ((JSONObject) object).get(APIConstants.ApplicationAttributes.HIDDEN);
                        attributeVisibility.put(String.valueOf(attributeName), isTrue(hidden));
                    }
                }
            }
            return attributeVisibility;
        } catch (APIManagementException e) {
            if (log != null && log.isDebugEnabled()) {
                log.debug("Skipping application attribute capability filtering — config lookup failed", e);
            }
            return null;
        }
    }

    private static boolean isTrue(Object value) {

        return Boolean.TRUE.equals(value) || "true".equalsIgnoreCase(String.valueOf(value));
    }

    private static void applyStringField(Map<String, Object> section, String fieldName, String currentValue,
                                         java.util.function.Consumer<String> setter) {

        applyValueField(asMap(section.get(fieldName)), currentValue, setter);
    }

    private static void applyValueField(Map<String, Object> fieldConfig, String currentValue,
                                        java.util.function.Consumer<String> setter) {

        if (fieldConfig == null) {
            return;
        }
        Object defaultValue = fieldConfig.get(KEY_DEFAULT_VALUE);
        if (defaultValue == null) {
            return;
        }
        String defaultStr = defaultValue instanceof String ? (String) defaultValue : String.valueOf(defaultValue);
        boolean hidden = isTrue(fieldConfig.get(KEY_HIDDEN));

        if (hidden) {
            setter.accept(defaultStr);
        } else if (StringUtils.isBlank(currentValue) && StringUtils.isNotBlank(defaultStr)) {
            setter.accept(defaultStr);
        }
    }

    private static void validateRequiredString(Map<String, Object> fieldConfig, String value, String label, Log log) {

        if (fieldConfig == null || !isTrue(fieldConfig.get(KEY_REQUIRED))) {
            return;
        }
        if (StringUtils.isBlank(value)) {
            RestApiUtil.handleBadRequest(label + " is required by the selected governance template", log);
        }
    }

    private static void validateRequiredList(Map<String, Object> fieldConfig, List<String> value, String label,
                                             Log log) {

        if (fieldConfig == null || !isTrue(fieldConfig.get(KEY_REQUIRED))) {
            return;
        }
        if (value == null || value.isEmpty()) {
            RestApiUtil.handleBadRequest(label + " is required by the selected governance template", log);
        }
    }

    private static void applyStringListField(Map<String, Object> section, String fieldName, List<String> currentValue,
                                             java.util.function.Consumer<List<String>> setter) {

        Map<String, Object> fieldConfig = asMap(section.get(fieldName));
        if (fieldConfig == null) {
            return;
        }
        List<String> defaultList = coerceStringList(fieldConfig.get(KEY_DEFAULT_VALUE));
        if (defaultList == null) {
            return;
        }
        boolean hidden = isTrue(fieldConfig.get(KEY_HIDDEN));

        if (hidden) {
            setter.accept(new ArrayList<>(defaultList));
        } else if ((currentValue == null || currentValue.isEmpty()) && !defaultList.isEmpty()) {
            setter.accept(new ArrayList<>(defaultList));
        }
    }

    /**
     * Reads a scalar field from the section config and writes it into the DTO's
     * {@code additionalProperties} map using the WSO2 internal key name.
     * Honors the hidden/defaultValue contract: hidden=true always forces the value;
     * hidden=false only fills when the key is absent or blank in the current map.
     */
    private static void applyAdditionalPropField(Map<String, Object> section, String fieldName,
                                                  java.util.Map<String, String> additionalProps, String apimKey) {

        Map<String, Object> fieldConfig = asMap(section.get(fieldName));
        if (fieldConfig == null) {
            return;
        }
        Object defaultValue = fieldConfig.get(KEY_DEFAULT_VALUE);
        if (defaultValue == null) {
            return;
        }
        String defaultStr = String.valueOf(defaultValue);
        boolean hidden = isTrue(fieldConfig.get(KEY_HIDDEN));

        if (hidden) {
            additionalProps.put(apimKey, defaultStr);
        } else {
            String existing = additionalProps.get(apimKey);
            if (existing == null || existing.isEmpty()) {
                additionalProps.put(apimKey, defaultStr);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static List<String> coerceStringList(Object value) {

        if (value == null) {
            return null;
        }
        if (value instanceof List) {
            List<String> result = new ArrayList<>();
            for (Object item : (List<Object>) value) {
                if (item != null) {
                    String s = item.toString().trim();
                    if (!s.isEmpty()) {
                        result.add(s);
                    }
                }
            }
            return result;
        }
        if (value instanceof String) {
            String s = ((String) value).trim();
            if (s.isEmpty()) {
                return null;
            }
            List<String> result = new ArrayList<>();
            for (String part : s.split(",")) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    result.add(trimmed);
                }
            }
            return result;
        }
        return null;
    }
}
