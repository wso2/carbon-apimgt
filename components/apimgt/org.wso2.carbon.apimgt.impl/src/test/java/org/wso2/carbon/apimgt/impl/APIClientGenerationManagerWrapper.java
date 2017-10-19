/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;

import java.io.File;

public class APIClientGenerationManagerWrapper extends APIClientGenerationManager {

    private final Log log = LogFactory.getLog(APIClientGenerationManagerWrapper.class);

    @Override
    protected int getTenantId(String requestedTenant) throws UserStoreException {
        return -1234;
    }

    @Override
    protected UserRegistry getGovernanceUserRegistry(String apiProvider, int tenantId) throws RegistryException {
        return new UserRegistry("admin",tenantId, null,null,null);
    }

    @Override
    protected APIManagerConfiguration getAPIManagerConfiguration() {
        APIManagerConfiguration apiManagerConfiguration = new APIManagerConfiguration();
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("amConfig.xml").getFile());
        try {
            apiManagerConfiguration.load(file.getPath().toString());
        } catch (APIManagementException e) {
            log.error("Error while reading configs from file api-manager.xml", e);
        }
        return apiManagerConfiguration;
    }
}
