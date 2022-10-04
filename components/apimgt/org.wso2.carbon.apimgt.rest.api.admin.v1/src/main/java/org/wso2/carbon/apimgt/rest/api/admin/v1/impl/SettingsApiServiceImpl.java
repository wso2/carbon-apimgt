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

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.admin.v1.SettingsApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.common.impl.SettingsCommonImpl;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.SettingsDTO;

import javax.ws.rs.core.Response;

public class SettingsApiServiceImpl implements SettingsApiService {

    /**
     * Retrieves admin portal related server settings
     *
     * @param messageContext
     * @return settings list
     * @throws APIManagementException
     */
    @Override
    public Response getAdminSettings(MessageContext messageContext) throws APIManagementException {
        SettingsDTO settingsDTO = SettingsCommonImpl.getAdminSettings();
        return Response.ok().entity(settingsDTO).build();
    }
}
