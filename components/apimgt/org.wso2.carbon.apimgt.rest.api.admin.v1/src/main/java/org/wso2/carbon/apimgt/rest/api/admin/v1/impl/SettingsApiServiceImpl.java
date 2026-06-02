/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIConstants.ApplicationAttributes;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.admin.v1.SettingsApiService;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.SettingsDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings.SettingsMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;

public class SettingsApiServiceImpl implements SettingsApiService {

    private static final Log log = LogFactory.getLog(SettingsApiServiceImpl.class);

    /**
     * Retrieves admin portal related server settings
     *
     * @param messageContext
     * @return settings list
     * @throws APIManagementException
     */
    @Override
    public Response settingsGet(MessageContext messageContext) throws APIManagementException {

            String username = RestApiCommonUtil.getLoggedInUsername();
            boolean isUserAvailable = false;
            if (!APIConstants.WSO2_ANONYMOUS_USER.equalsIgnoreCase(username)) {
                isUserAvailable = true;
            }
            SettingsMappingUtil settingsMappingUtil = new SettingsMappingUtil();
            SettingsDTO settingsDTO = settingsMappingUtil.fromSettingsToDTO(isUserAvailable);
            settingsDTO.setTransactionCounterEnable(APIUtil.getTransactionCounterEnable());
            try {
                APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
                JSONArray attrArray = apiConsumer.getAppAttributesFromConfig(username);
                List<Map<String, Object>> attrs = new ArrayList<>();
                for (int i = 0; i < attrArray.size(); i++) {
                    JSONObject obj = (JSONObject) attrArray.get(i);
                    // Map capitalized config keys to lowercase to match the devportal API response shape
                    Map<String, Object> attr = new HashMap<>();
                    attr.put("attribute", obj.get(APIConstants.ApplicationAttributes.ATTRIBUTE));
                    attr.put("description", obj.get(APIConstants.ApplicationAttributes.DESCRIPTION));
                    attr.put("required", String.valueOf(obj.get(APIConstants.ApplicationAttributes.REQUIRED)));
                    attr.put("hidden", String.valueOf(obj.get(APIConstants.ApplicationAttributes.HIDDEN)));
                    attr.put("type", String.valueOf(obj.get(APIConstants.ApplicationAttributes.TYPE)));
                    attrs.add(attr);
                }
                settingsDTO.setApplicationAttributes(attrs);
            } catch (APIManagementException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Failed to load application attributes for settings response", e);
                }
            }
            return Response.ok().entity(settingsDTO).build();
    }
}
