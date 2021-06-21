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
package org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.EnvironmentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MonetizationAttributeDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SecurityAuditAttributeDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SettingsDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class SettingsMappingUtil {


    private static final Log log = LogFactory.getLog(SettingsMappingUtil.class);

    /**
     * This method feeds data into the settingsDTO
     * @param isUserAvailable check if user is logged in
     * @return SettingsDTO
     * @throws APIManagementException
     */
    public SettingsDTO fromSettingstoDTO(Boolean isUserAvailable) throws APIManagementException {
        SettingsDTO settingsDTO = new SettingsDTO();
        EnvironmentListDTO environmentListDTO = new EnvironmentListDTO();
        if (isUserAvailable) {
            Map<String, Environment> environments = APIUtil.getEnvironments();
            if (environments != null) {
                environmentListDTO = EnvironmentMappingUtil.fromEnvironmentCollectionToDTO(environments.values());
            }
            settingsDTO.setEnvironment(environmentListDTO.getList());
            String storeUrl = APIUtil.getStoreUrl();
            String loggedInUserTenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            int tenantId = APIUtil.getTenantIdFromTenantDomain(loggedInUserTenantDomain);
            Map<String, String> domainMappings =
                    APIUtil.getDomainMappings(loggedInUserTenantDomain, APIConstants.API_DOMAIN_MAPPINGS_STORE);
            if (domainMappings.size() != 0) {
                Iterator entries = domainMappings.entrySet().iterator();
                while (entries.hasNext()) {
                    Map.Entry thisEntry = (Map.Entry) entries.next();
                    storeUrl = "https://" + thisEntry.getValue();
                    break;
                }
            }
            settingsDTO.setDefaultAdvancePolicy(APIUtil.getDefaultAPILevelPolicy(tenantId));
            settingsDTO.setDefaultSubscriptionPolicy(APIUtil.getDefaultSubscriptionPolicy(tenantId));
            settingsDTO.setStoreUrl(storeUrl);
            settingsDTO.setMonetizationAttributes(getMonetizationAttributes());
            settingsDTO.setSecurityAuditProperties(getSecurityAuditProperties());
            settingsDTO.setExternalStoresEnabled(
                    APIUtil.isExternalStoresEnabled(RestApiUtil.getLoggedInUserTenantDomain()));
            settingsDTO.setDocVisibilityEnabled(APIUtil.isDocVisibilityLevelsEnabled());
            settingsDTO.setCrossTenantSubscriptionEnabled(APIUtil.isCrossTenantSubscriptionsEnabled());
            String authorizationHeader = APIUtil.getOAuthConfiguration(tenantId, APIConstants.AUTHORIZATION_HEADER);

            if (authorizationHeader == null) {
                authorizationHeader = APIUtil.getOAuthConfigurationFromAPIMConfig(APIConstants.AUTHORIZATION_HEADER);
            }
            settingsDTO.setAuthorizationHeader(authorizationHeader);
        }
        settingsDTO.setScopes(GetScopeList());
        return settingsDTO;
    }

    /**
     * This method returns the scope list from the publisher-api.yaml
     * @return  List<String> scope list
     * @throws APIManagementException
     */
    private List<String> GetScopeList() throws APIManagementException {
        String definition = null;
        try {
            definition = IOUtils
                    .toString(RestApiUtil.class.getResourceAsStream("/publisher-api.yaml"), "UTF-8");
        } catch (IOException e) {
            log.error("Error while reading the swagger definition", e);
        }
        APIDefinition parser = OASParserUtil.getOASParser(definition);
        Set<Scope> scopeSet = parser.getScopes(definition);
        List<String> scopeList = new ArrayList<>();
        for (Scope entry : scopeSet) {
            scopeList.add(entry.getKey());
        }
        return scopeList;
    }

    /**
     * This method returns the monetization properties from configuration
     *
     * @return List<String> monetization properties
     * @throws APIManagementException
     */
    private List<MonetizationAttributeDTO> getMonetizationAttributes() {

        List<MonetizationAttributeDTO> monetizationAttributeDTOSList = new ArrayList<MonetizationAttributeDTO>();
        JSONArray monetizationAttributes = APIUtil.getMonetizationAttributes();

        for (int i = 0; i < monetizationAttributes.size(); i++) {
            JSONObject monetizationAttribute = (JSONObject) monetizationAttributes.get(i);
            MonetizationAttributeDTO monetizationAttributeDTO = new MonetizationAttributeDTO();
            monetizationAttributeDTO.setName((String) monetizationAttribute.get(APIConstants.Monetization.ATTRIBUTE));
            monetizationAttributeDTO.setDisplayName(
                    (String) monetizationAttribute.get(APIConstants.Monetization.ATTRIBUTE_DISPLAY_NAME));
            monetizationAttributeDTO.setDescription(
                    (String) monetizationAttribute.get(APIConstants.Monetization.ATTRIBUTE_DESCRIPTION));
            monetizationAttributeDTO
                    .setRequired((Boolean) monetizationAttribute.get(APIConstants.Monetization.IS_ATTRIBITE_REQUIRED));
            monetizationAttributeDTO
                    .setHidden((Boolean) monetizationAttribute.get(APIConstants.Monetization.IS_ATTRIBUTE_HIDDEN));
            monetizationAttributeDTOSList.add(monetizationAttributeDTO);
        }
        return monetizationAttributeDTOSList;
    }

    /**
     * This method returns the Security Audit properties from the configuration
     *
     * @return SecurityAuditAttributeDTO Security Audit Attributes
     * @throws APIManagementException
     */
    private SecurityAuditAttributeDTO getSecurityAuditProperties() throws APIManagementException {
        SecurityAuditAttributeDTO properties = new SecurityAuditAttributeDTO();

        String username = RestApiUtil.getLoggedInUsername();
        APIProvider apiProvider = RestApiUtil.getProvider(username);

        JSONObject securityAuditPropertyObject = apiProvider.getSecurityAuditAttributesFromConfig(username);
        if (securityAuditPropertyObject != null) {
            String apiToken = (String) securityAuditPropertyObject.get(APIConstants.SECURITY_AUDIT_API_TOKEN);
            String collectionId = (String) securityAuditPropertyObject.get(APIConstants.SECURITY_AUDIT_COLLECTION_ID);
            String baseUrl = (String) securityAuditPropertyObject.get(APIConstants.SECURITY_AUDIT_BASE_URL);

            properties.setApiToken(apiToken);
            properties.setCollectionId(collectionId);
            properties.setBaseUrl(baseUrl);
        }
        return properties;
    }
}
