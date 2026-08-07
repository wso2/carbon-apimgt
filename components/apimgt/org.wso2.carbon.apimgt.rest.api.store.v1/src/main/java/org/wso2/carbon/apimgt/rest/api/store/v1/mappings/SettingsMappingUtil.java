/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.rest.api.store.v1.mappings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.io.IOUtils;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.SettingsDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.SettingsIdentityProviderDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.apimgt.spec.parser.definitions.OASParserUtil;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SettingsMappingUtil {

    private static final Log log = LogFactory.getLog(SettingsMappingUtil.class);

    public SettingsDTO fromSettingstoDTO(Boolean isUserAvailable, Boolean moneatizationEnabled,
                                         boolean recommendationEnabled, boolean anonymousEnabled, String organization)
            throws APIManagementException {
        SettingsDTO settingsDTO = new SettingsDTO();
        settingsDTO.setScopes(GetScopeList());
        settingsDTO.setApplicationSharingEnabled(APIUtil.isMultiGroupAppSharingEnabled());
        settingsDTO.setIsJWTEnabledForLoginTokens(APIUtil.isJWTEnabledForPortals());
        settingsDTO.setRecommendationEnabled(recommendationEnabled);
        settingsDTO.setMapExistingAuthApps(APIUtil.isMapExistingAuthAppsEnabled());
        settingsDTO.setMonetizationEnabled(moneatizationEnabled);
        settingsDTO.setOrgWideAppUpdateEnabled(APIUtil.isOrgWideAppUpdateEnabled());
        SettingsIdentityProviderDTO identityProviderDTO = new SettingsIdentityProviderDTO();
        identityProviderDTO.setExternal(APIUtil.getIdentityProviderConfig() != null);
        settingsDTO.setIdentityProvider(identityProviderDTO);
        settingsDTO.setIsAnonymousModeEnabled(anonymousEnabled);
        settingsDTO.setIsLegacyApiKeysEnabled(APIUtil.isLegacyApiKeysEnabled());
        settingsDTO.setOrgAccessControlEnabled(APIUtil.isOrganizationAccessControlEnabled());
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        boolean enableChangePassword =
                Boolean.parseBoolean(config.getFirstProperty(APIConstants.ENABLE_CHANGE_PASSWORD));
        settingsDTO.setIsPasswordChangeEnabled(enableChangePassword);

        String username = RestApiCommonUtil.getLoggedInUsername();
        String tenantDomain = MultitenantUtils.getTenantDomain(username);
        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);

        String userStorePasswordPattern = null;
        String passwordPolicyPattern = null;
        int passwordPolicyMinLength = -1;
        int passwordPolicyMaxLength = -1;

        try {
            // Get password pattern from the UserStoreManager configuration
            RealmConfiguration realmConfiguration = null;
            RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();

            if (realmService != null && tenantId != MultitenantConstants.INVALID_TENANT_ID) {
                UserStoreManager userStoreManager = null;
                userStoreManager = (UserStoreManager) realmService.getTenantUserRealm(tenantId).getUserStoreManager();
                realmConfiguration = userStoreManager.getRealmConfiguration();
            }

            if (realmConfiguration != null) {
                String passwordJavaRegEx = realmConfiguration.getUserStoreProperty(APIConstants.PASSWORD_JAVA_REGEX_PROPERTY);
                if (passwordJavaRegEx != null && !passwordJavaRegEx.trim().isEmpty()) {
                    userStorePasswordPattern = passwordJavaRegEx;
                }
            }

            // Get password pattern from the Password policy
            Property passwordPolicyEnabledProperty = FrameworkUtils.getResidentIdpConfiguration(
                    APIConstants.IS_PASSWORD_POLICY_ENABLED_PROPERTY, tenantDomain);
            boolean isPasswordPolicyEnabled = Boolean.parseBoolean(passwordPolicyEnabledProperty.getValue());
            if (isPasswordPolicyEnabled) {
                passwordPolicyPattern =
                        FrameworkUtils.getResidentIdpConfiguration(APIConstants.PASSWORD_POLICY_PATTERN_PROPERTY,
                                tenantDomain).getValue();
                passwordPolicyMinLength = Integer.parseInt(FrameworkUtils.getResidentIdpConfiguration(
                        APIConstants.PASSWORD_POLICY_MIN_LENGTH_PROPERTY, tenantDomain).getValue());
                passwordPolicyMaxLength = Integer.parseInt(FrameworkUtils.getResidentIdpConfiguration(
                        APIConstants.PASSWORD_POLICY_MAX_LENGTH_PROPERTY, tenantDomain).getValue());
            }
        } catch (UserStoreException e) {
            String errorMessage = "Error occurred in getting userRealm for the tenant: " + tenantId;
            throw new APIManagementException(errorMessage, e);
        } catch (FrameworkException e) {
            String errorMessage = "Error occurred in getting Resident Idp Configurations for tenant: " + tenantId;
            throw new APIManagementException(errorMessage, e);
        }
        settingsDTO.setUserStorePasswordPattern(userStorePasswordPattern);
        settingsDTO.setPasswordPolicyPattern(passwordPolicyPattern);
        settingsDTO.setPasswordPolicyMinLength(passwordPolicyMinLength);
        settingsDTO.setPasswordPolicyMaxLength(passwordPolicyMaxLength);
        settingsDTO.setApiChatEnabled(config.getApiChatConfigurationDto().isEnabled());
        settingsDTO.setMarketplaceAssistantEnabled(config.getMarketplaceAssistantConfigurationDto().isEnabled());
        settingsDTO.setAiAuthTokenProvided(isAiCredentialProvided(config));
        settingsDTO.setDevportalMode(
                SettingsDTO.DevportalModeEnum.fromValue(config.getDevportalMode()));

        if (isUserAvailable) {
            settingsDTO.setGrantTypes(APIUtil.getGrantTypes());
            Map<String, Environment> environments = APIUtil.getEnvironments(organization);
            if (environments.isEmpty()) {
                settingsDTO.apiGatewayEndpoint("http://localhost:8280, https://localhost:8243");
            } else {
                for (Map.Entry<String, Environment> entry : environments.entrySet()) {
                    Environment environment = environments.get(entry.getKey());
                    if (environment.isDefault()) {
                        settingsDTO.apiGatewayEndpoint(environment.getApiGatewayEndpoint());
                        break;
                    }
                }
                if (settingsDTO.getApiGatewayEndpoint() == null) {
                    Map.Entry<String, Environment> entry = environments.entrySet().iterator().next();
                    Environment environment = environments.get(entry.getKey());
                    settingsDTO.apiGatewayEndpoint(environment.getApiGatewayEndpoint());
                }
            }
        }
        return settingsDTO;
    }

    /**
     * Whether a credential is configured for any AI feature the Developer Portal serves.
     * <p>
     * All AI features share the same {@code apim.ai.key} and {@code apim.ai.token}, so
     * this answers "is an AI credential configured at all" rather than anything about a
     * particular feature. Whether an individual feature is usable is decided by its own
     * enabled flag, which the caller reports separately.
     * <p>
     * Both features the Developer Portal serves have to be consulted because the
     * {@code Enabled} value of a feature gates the parsing of its credential: turning
     * one feature off leaves its configuration DTO with no key and no token. Reading
     * this from a single feature therefore reported "no credential" for both of them as
     * soon as that one feature was disabled, which disabled the Developer Portal AI
     * components even though a credential was configured.
     *
     * @param config API Manager configuration
     * @return true when API Chat or the Marketplace Assistant holds a key or an auth token
     */
    private boolean isAiCredentialProvided(APIManagerConfiguration config) {

        return config.getApiChatConfigurationDto().isKeyProvided()
                || config.getApiChatConfigurationDto().isAuthTokenProvided()
                || config.getMarketplaceAssistantConfigurationDto().isKeyProvided()
                || config.getMarketplaceAssistantConfigurationDto().isAuthTokenProvided();
    }

    private List<String> GetScopeList() throws APIManagementException {
        String definition = null;
        try {
            definition = IOUtils
                    .toString(RestApiUtil.class.getResourceAsStream("/devportal-api.yaml"), "UTF-8");
        } catch (IOException e) {
            log.error("Error while reading the swagger definition", e);
        }
        APIDefinition oasParser = OASParserUtil.getOASParser(definition);
        Set<Scope> scopeSet = oasParser.getScopes(definition);
        List<String> scopeList = new ArrayList<>();
        for (Scope entry : scopeSet) {
            scopeList.add(entry.getKey());
        }
        return scopeList;
    }
}