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

package org.wso2.carbon.apimgt.governance.impl.util;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.governance.api.model.KeyManagerGovernanceContext;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Single resolution path that turns a (uuid|name, organization) pair into a normalized
 * {@link KeyManagerGovernanceContext}. Every governance hook MUST go through here.
 *
 * Resolution order:
 *   1. blank identifier  → Resident Key Manager in the org.
 *   2. UUID first        → {@link ApiMgtDAO#getKeyManagerConfigurationByID(String, String)}.
 *   3. Name fallback     → {@link ApiMgtDAO#getKeyManagerConfigurationByName(String, String)}.
 *   4. Super-tenant walk → re-resolve in the super-tenant org so Global KMs declared
 *                          at super-tenant scope are visible to tenant-scoped callers.
 *
 * The resolver never throws on missing KMs; callers receive {@code null} and decide
 * whether to block or skip enforcement. Failures from the DAO are swallowed for the
 * UUID-then-name fallback so a UUID miss doesn't short-circuit the name attempt.
 */
public final class KeyManagerContextResolver {

    private KeyManagerContextResolver() {

    }

    /**
     * Resolve a Key Manager into a normalized governance context.
     *
     * @param identifier UUID or name (or blank → Resident).
     * @param organization tenant organization to resolve in.
     * @return populated context, or {@code null} when the KM cannot be located.
     */
    public static KeyManagerGovernanceContext resolve(String identifier, String organization) {

        KeyManagerConfigurationDTO config = lookup(identifier, organization);
        if (config == null) {
            return null;
        }
        return toContext(config, organization);
    }

    private static KeyManagerConfigurationDTO lookup(String identifier, String organization) {

        String resolvedIdentifier = StringUtils.isBlank(identifier)
                ? APIConstants.KeyManager.DEFAULT_KEY_MANAGER : identifier.trim();

        KeyManagerConfigurationDTO config = lookupInOrganization(resolvedIdentifier, organization);
        if (config != null) {
            return config;
        }
        // Walk up to the super-tenant for Global KMs when the caller is in a sub-tenant.
        if (StringUtils.isNotBlank(organization)
                && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(organization)) {
            return lookupInOrganization(resolvedIdentifier, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        }
        return null;
    }

    private static KeyManagerConfigurationDTO lookupInOrganization(String identifier, String organization) {

        ApiMgtDAO dao = ApiMgtDAO.getInstance();
        if (looksLikeUuid(identifier)) {
            try {
                KeyManagerConfigurationDTO config = dao.getKeyManagerConfigurationByID(organization, identifier);
                if (config != null) {
                    return config;
                }
            } catch (APIManagementException ignored) {
                // Fall through to name lookup — UUID miss should not short-circuit.
            }
        }
        try {
            return dao.getKeyManagerConfigurationByName(organization, identifier);
        } catch (APIManagementException ignored) {
            return null;
        }
    }

    private static boolean looksLikeUuid(String value) {

        return value != null && value.matches(APIConstants.KeyManager.UUID_REGEX);
    }

    private static KeyManagerGovernanceContext toContext(KeyManagerConfigurationDTO config,
                                                         String requestOrganization) {

        KeyManagerGovernanceContext ctx = new KeyManagerGovernanceContext();
        ctx.setUuid(config.getUuid());
        ctx.setName(config.getName());
        ctx.setDisplayName(StringUtils.defaultIfBlank(config.getDisplayName(), config.getName()));
        ctx.setAlias(config.getAlias());
        ctx.setType(config.getType());
        ctx.setEnabled(config.isEnabled());
        ctx.setResident(APIConstants.KeyManager.DEFAULT_KEY_MANAGER.equals(config.getName())
                || APIConstants.KeyManager.DEFAULT_KEY_MANAGER_TYPE.equalsIgnoreCase(config.getType()));
        ctx.setGlobal(StringUtils.isNotBlank(config.getOrganization())
                && MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(config.getOrganization())
                && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(requestOrganization));

        Map<String, Object> additional = config.getAdditionalProperties();
        if (additional == null) {
            additional = new LinkedHashMap<>();
        }
        ctx.setRawProperties(new LinkedHashMap<>(additional));

        // Capability flags — sourced from the KM's additionalProperties using the canonical
        // constant names in APIConstants.KeyManager. Defaults match WSO2 IS resident-KM behavior
        // (create/token-gen on, OOB-mapping off) so a misconfigured KM doesn't accidentally allow
        // governance-disabled flows.
        ctx.setEnableOAuthAppCreation(boolProp(additional,
                APIConstants.KeyManager.ENABLE_OAUTH_APP_CREATION, true));
        ctx.setEnableMapOAuthConsumerApps(boolProp(additional,
                APIConstants.KeyManager.ENABLE_MAP_OAUTH_CONSUMER_APPS, false));
        ctx.setEnableTokenGeneration(boolProp(additional,
                APIConstants.KeyManager.ENABLE_TOKEN_GENERATION, true));
        ctx.setEnableTokenHash(boolProp(additional,
                APIConstants.KeyManager.ENABLE_TOKEN_HASH, false));
        ctx.setEnableTokenEncryption(boolProp(additional,
                APIConstants.KeyManager.ENABLE_TOKEN_ENCRYPTION, false));
        ctx.setEnableMultipleClientSecrets(boolProp(additional,
                APIConstants.KeyManager.ENABLE_MULTIPLE_CLIENT_SECRETS, false));
        Object secretCount = additional.get(APIConstants.KeyManager.CLIENT_SECRET_COUNT);
        ctx.setClientSecretCount(intOrNull(secretCount));
        Object secretExpiresIn = additional.get(APIConstants.KeyManager.CLIENT_SECRET_EXPIRES_IN);
        ctx.setClientSecretExpiresIn(longOrNull(secretExpiresIn));
        ctx.setBypassClientCredentials(boolProp(additional,
                APIConstants.KeyManager.BYPASS_CLIENT_CREDENTIALS, false));
        ctx.setSelfValidateJwt(boolProp(additional,
                APIConstants.KeyManager.SELF_VALIDATE_JWT, false));

        Object validationType = additional.get(APIConstants.KeyManager.VALIDATION_TYPE);
        ctx.setValidationType(validationType == null ? null : String.valueOf(validationType));
        Object tokenFormat = additional.get(APIConstants.KeyManager.TOKEN_FORMAT_STRING);
        ctx.setTokenFormatString(tokenFormat == null ? null : String.valueOf(tokenFormat));

        ctx.setAvailableGrantTypes(coerceStringList(additional.get(APIConstants.KeyManager.AVAILABLE_GRANT_TYPE)));
        return ctx;
    }

    private static boolean boolProp(Map<String, Object> properties, String key, boolean defaultValue) {

        Object value = properties.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private static Integer intOrNull(Object value) {

        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Long longOrNull(Object value) {

        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static List<String> coerceStringList(Object value) {

        if (value == null) {
            return new ArrayList<>();
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
                return new ArrayList<>();
            }
            // KM advertises grant_types as a comma- or space-separated string in some configurations.
            String[] parts = s.split("[,\\s]+");
            List<String> result = new ArrayList<>(Arrays.asList(parts));
            result.removeIf(String::isEmpty);
            return result;
        }
        return new ArrayList<>();
    }
}
