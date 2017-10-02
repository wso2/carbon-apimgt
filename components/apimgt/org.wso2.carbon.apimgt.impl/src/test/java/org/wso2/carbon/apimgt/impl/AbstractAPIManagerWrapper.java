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

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.tenant.TenantManager;

public class AbstractAPIManagerWrapper extends AbstractAPIManager {

    private GenericArtifactManager genericArtifactManager;
    private RegistryService registryService;
    private TenantManager tenantManager;

    public AbstractAPIManagerWrapper(GenericArtifactManager genericArtifactManager) throws APIManagementException {
        this.genericArtifactManager = genericArtifactManager;
    }

    public AbstractAPIManagerWrapper(GenericArtifactManager genericArtifactManager, RegistryService registryService,
            TenantManager tenantManager) throws APIManagementException {
        this.genericArtifactManager = genericArtifactManager;
        this.registryService = registryService;
        this.tenantManager = tenantManager;
    }

    public AbstractAPIManagerWrapper(Registry registry) throws APIManagementException {
        this.registry = registry;
    }

    public AbstractAPIManagerWrapper(GenericArtifactManager genericArtifactManager, Registry registry,
            TenantManager tenantManager) throws APIManagementException {
        this.genericArtifactManager = genericArtifactManager;
        this.registry = registry;
        this.tenantManager = tenantManager;
    }

    public AbstractAPIManagerWrapper(GenericArtifactManager genericArtifactManager, RegistryService registryService,
            Registry registry, TenantManager tenantManager) throws APIManagementException {
        this.genericArtifactManager = genericArtifactManager;
        this.registry = registry;
        this.tenantManager = tenantManager;
        this.registryService = registryService;
    }

    @Override protected GenericArtifactManager getGenericArtifactManager() throws APIManagementException {
        return genericArtifactManager;
    }

    @Override protected GenericArtifactManager getNewGenericArtifactManager() {
        return genericArtifactManager;
    }

    @Override protected GenericArtifactManager getGenericArtifactManager(Registry registry) throws RegistryException {
        return genericArtifactManager;
    }

    @Override protected RegistryService getRegistryService() {
        return registryService;
    }

    @Override protected TenantManager getTenantManager() {
        return tenantManager;
    }

    protected API getApi(GovernanceArtifact artifact) throws APIManagementException {
        try {

            APIIdentifier apiIdentifier = new APIIdentifier(artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER),
                    artifact.getAttribute(APIConstants.API_OVERVIEW_NAME),
                    artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION));
            API api = new API(apiIdentifier);
            return api;
        } catch (GovernanceException e) {
            throw new APIManagementException("Error while getting attribute", e);
        }
    }

    protected API getApiForPublishing(Registry registry, GovernanceArtifact apiArtifact) throws APIManagementException {
        try {

            APIIdentifier apiIdentifier = new APIIdentifier(
                    apiArtifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER),
                    apiArtifact.getAttribute(APIConstants.API_OVERVIEW_NAME),
                    apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VERSION));
            API api = new API(apiIdentifier);
            return api;
        } catch (GovernanceException e) {
            throw new APIManagementException("Error while getting attribute", e);
        }
    }

    protected API getApiInformation(Registry registry, GovernanceArtifact apiArtifact) throws APIManagementException {
        return getApi(apiArtifact);
    }

    protected String getTenantDomain(APIIdentifier identifier) {
        return "carbon.super";
    }

    protected void startTenantFlow(String tenantDomain) {
        // Do Nothing
    }

    protected void endTenantFlow() {
        // Do Nothing
    }

}
