/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.apimgt.impl.executors;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;

public class APIExecutorWrapper extends APIExecutor {

    private final String USER_NAME = "john";
    private final int TENANT_ID = 1234;

    private GenericArtifactManager genericArtifactManager;
    private UserRegistry userRegistry;
    private APIProvider apiProvider;
    private API api;
    private String apiPath = "/api/somepath";
    private String tenantDomain;

    public APIExecutorWrapper(GenericArtifactManager genericArtifactManager, UserRegistry userRegistry,
            APIProvider apiProvider, API api, String tenantDomain) {
        this.genericArtifactManager = genericArtifactManager;
        this.userRegistry = userRegistry;
        this.apiProvider = apiProvider;
        this.api = api;
        this.tenantDomain = tenantDomain;
    }

    @Override
    protected API getApi(GenericArtifact apiArtifact) throws APIManagementException {
        return api;
    }

    @Override
    protected APIProvider getApiProvider(String userWithDomain) throws APIManagementException {
        return apiProvider;
    }

    @Override
    protected String getTenantDomain() {
        return tenantDomain;
    }

    @Override
    protected String getUsername() {
        return USER_NAME;
    }

    @Override
    protected int getTenantId(String domain) throws UserStoreException {
        return TENANT_ID;
    }

    @Override
    protected GenericArtifactManager getArtifactManager(RequestContext context)
            throws APIManagementException, RegistryException {
        return genericArtifactManager;
    }

    @Override
    protected UserRegistry getGovernanceUserRegistry(String tenantUserName, int tenantId)
            throws RegistryException {
        return userRegistry;
    }
}
