/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.rest.api.endpoint.registry.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.SettingsApiService;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.dto.SettingsDTO;

import org.wso2.carbon.apimgt.rest.api.endpoint.registry.util.SettingsMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.core.Response;

@RequestScoped
public class SettingsApiServiceImpl implements SettingsApiService {

    private static final Log log = LogFactory.getLog(SettingsApiServiceImpl.class);

    @Override
    public Response settingsGet(MessageContext messageContext) {
        try {
            SettingsMappingUtil settingsMappingUtil = new SettingsMappingUtil();
            SettingsDTO settingsDTO = settingsMappingUtil.fromSettingsToDTO();
            return Response.ok().entity(settingsDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Endpoint Registry Settings";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }
}
