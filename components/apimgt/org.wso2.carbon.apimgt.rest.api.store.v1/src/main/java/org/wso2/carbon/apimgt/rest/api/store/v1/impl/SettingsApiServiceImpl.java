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
package org.wso2.carbon.apimgt.rest.api.store.v1.impl;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.SettingsApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationAttributeDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationAttributeListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.SettingsDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.mappings.ApplicationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.mappings.SettingsMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

public class SettingsApiServiceImpl implements SettingsApiService {

    private static final Log log = LogFactory.getLog(SettingsApiServiceImpl.class);

    @Override
    public Response settingsGet(String xWSO2Tenant, MessageContext messageContext)
            throws APIManagementException {
        try {
            String username = RestApiCommonUtil.getLoggedInUsername();
            APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
            String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);
            boolean monetizationEnabled = apiConsumer.isMonetizationEnabled(requestedTenantDomain);
            boolean recommendationEnabled = apiConsumer.isRecommendationEnabled(requestedTenantDomain);
            boolean anonymousEnabled = RestApiUtil.isDevPortalAnonymousEnabled(requestedTenantDomain);
            boolean isUserAvailable = false;
            if (!APIConstants.WSO2_ANONYMOUS_USER.equalsIgnoreCase(username)) {
                isUserAvailable = true;
            }
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            SettingsMappingUtil settingsMappingUtil = new SettingsMappingUtil();
            SettingsDTO settingsDTO = settingsMappingUtil.fromSettingstoDTO( isUserAvailable, monetizationEnabled,
                    recommendationEnabled, anonymousEnabled, organization);
            return Response.ok().entity(settingsDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Store Settings";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response settingsApplicationAttributesGet(String ifNoneMatch, MessageContext messageContext) {
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
            ApplicationAttributeListDTO applicationAttributeListDTO = ApplicationMappingUtil
                    .fromApplicationAttributeListToDTO(applicationAttributeDTOList);
            return Response.ok().entity(applicationAttributeListDTO).build();
        } catch (APIManagementException e) {
            RestApiUtil
                    .handleInternalServerError("Error occurred in reading application attributes from config", e, log);
        }
        return null;
    }
}
