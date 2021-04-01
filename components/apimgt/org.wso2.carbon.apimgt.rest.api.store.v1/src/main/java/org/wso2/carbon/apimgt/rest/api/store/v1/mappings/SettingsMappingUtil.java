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
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.SettingsDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.SettingsIdentityProviderDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
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
                                         boolean recommendationEnabled, boolean anonymousEnabled) throws APIManagementException {
        SettingsDTO settingsDTO = new SettingsDTO();
        settingsDTO.setScopes(GetScopeList());
        settingsDTO.setApplicationSharingEnabled(APIUtil.isMultiGroupAppSharingEnabled());
        settingsDTO.setRecommendationEnabled(recommendationEnabled);
        settingsDTO.setMapExistingAuthApps(APIUtil.isMapExistingAuthAppsEnabled());
        settingsDTO.setMonetizationEnabled(moneatizationEnabled);
        SettingsIdentityProviderDTO identityProviderDTO = new SettingsIdentityProviderDTO();
        identityProviderDTO.setExternal(APIUtil.getIdentityProviderConfig() != null);
        settingsDTO.setIdentityProvider(identityProviderDTO);
        settingsDTO.setIsAnonymousModeEnabled(anonymousEnabled);
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

        if (isUserAvailable) {
            settingsDTO.setGrantTypes(APIUtil.getGrantTypes());
            Map<String, Environment> environments = APIUtil.getEnvironments();
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