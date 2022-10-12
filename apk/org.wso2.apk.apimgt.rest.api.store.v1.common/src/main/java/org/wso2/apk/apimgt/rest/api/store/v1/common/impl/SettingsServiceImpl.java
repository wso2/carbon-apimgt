/*
*  Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.apk.apimgt.rest.api.store.v1.common.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.apk.apimgt.api.APIConsumer;
import org.wso2.apk.apimgt.api.APIDefinition;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.model.Scope;
import org.wso2.apk.apimgt.impl.APIConstants;
import org.wso2.apk.apimgt.impl.APIManagerFactory;
import org.wso2.apk.apimgt.impl.definitions.OASParserUtil;
import org.wso2.apk.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.apk.apimgt.rest.api.store.v1.common.mappings.ApplicationMappingUtil;
import org.wso2.apk.apimgt.rest.api.store.v1.common.mappings.SettingsMappingUtil;
import org.wso2.apk.apimgt.rest.api.store.v1.dto.ApplicationAttributeDTO;
import org.wso2.apk.apimgt.rest.api.store.v1.dto.ApplicationAttributeListDTO;
import org.wso2.apk.apimgt.rest.api.store.v1.dto.SettingsDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This class has Setting API service related Implementation
 */
public class SettingsServiceImpl {

    private SettingsServiceImpl() {
    }

    /**
     *
     * @param organization
     * @param requestedTenantDomain
     * @param anonymousEnabled
     * @return
     * @throws APIManagementException
     */
    public static SettingsDTO getSettings(String organization, String requestedTenantDomain, boolean anonymousEnabled)
            throws APIManagementException {
        try {
            String username = RestApiCommonUtil.getLoggedInUsername();
            APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);

            boolean monetizationEnabled = apiConsumer.isMonetizationEnabled(requestedTenantDomain);
            boolean recommendationEnabled = apiConsumer.isRecommendationEnabled(requestedTenantDomain);
            boolean isUserAvailable = false;
            if (!APIConstants.WSO2_ANONYMOUS_USER.equalsIgnoreCase(username)) {
                isUserAvailable = true;
            }
            SettingsMappingUtil settingsMappingUtil = new SettingsMappingUtil();
            return settingsMappingUtil.fromSettingstoDTO(isUserAvailable, monetizationEnabled,
                    recommendationEnabled, anonymousEnabled, organization);
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Store Settings";
            throw new APIManagementException(errorMessage, e.getErrorHandler());
        }
    }

    /**
     *
     * @return ApplicationAttributeListDTO
     */
    public static ApplicationAttributeListDTO getSettingAttributes() throws APIManagementException {
        String username = RestApiCommonUtil.getLoggedInUsername();
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            List<ApplicationAttributeDTO> applicationAttributeDTOList = new ArrayList<>();
            JSONArray attributeArray = apiConsumer.getAppAttributesFromConfig(username);
            for (int i = 0; i < attributeArray.size(); i++) {
                JSONObject obj = (JSONObject) attributeArray.get(i);
                ApplicationAttributeDTO applicationAttributeDTO = ApplicationMappingUtil
                        .fromApplicationAttributeJsonToDTO(obj);
                applicationAttributeDTOList.add(applicationAttributeDTO);
            }
            return ApplicationMappingUtil
                    .fromApplicationAttributeListToDTO(applicationAttributeDTOList);
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred in reading application attributes from config";
            throw new APIManagementException(errorMessage, e.getErrorHandler());
        }
    }

    /**
     *
     * @param definition
     * @return
     * @throws APIManagementException
     */
    public static List<String> getScopes(String definition) throws APIManagementException {
        APIDefinition oasParser = OASParserUtil.getOASParser(definition);
        Set<Scope> scopeSet = oasParser.getScopes(definition);
        List<String> scopeList = new ArrayList<>();
        for (Scope entry : scopeSet) {
            scopeList.add(entry.getKey());
        }
        return scopeList;
    }

}
