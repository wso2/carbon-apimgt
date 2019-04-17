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


import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.StoreSettings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.carbon.apimgt.rest.api.store.v1.SettingsApiService;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;

public class SettingsApiServiceImpl extends SettingsApiService {

    private static final Log log = LogFactory.getLog(SettingsApiServiceImpl.class);

    @Override
    public Response settingsGet(){
        try {
            String username = RestApiUtil.getLoggedInUsername();
            APIConsumer apiConsumer = RestApiUtil.getLoggedInUserConsumer();
            StoreSettings storeSettings = apiConsumer.getStoreSettings(username);
            return Response.ok().entity(storeSettings).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Settings";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }
}
