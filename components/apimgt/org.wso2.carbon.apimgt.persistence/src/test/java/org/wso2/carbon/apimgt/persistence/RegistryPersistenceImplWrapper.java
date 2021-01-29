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
 */
package org.wso2.carbon.apimgt.persistence;

import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.tenant.TenantManager;

public class RegistryPersistenceImplWrapper extends RegistryPersistenceImpl {
    TenantManager tenantManager;
    RegistryService registryService;
    Registry registry;
    GenericArtifact artifact;

    public RegistryPersistenceImplWrapper(TenantManager tenantManager, RegistryService registryService) {
        this.tenantManager = tenantManager;
        this.registryService = registryService;
    }

    public RegistryPersistenceImplWrapper(Registry registry) {
        this.registry = registry;
    }

    public RegistryPersistenceImplWrapper(Registry registry, GenericArtifact artifact) {
        this.registry = registry;
        this.artifact = artifact;
    }

    protected TenantManager getTenantManager() {
        return tenantManager;
    }

    protected RegistryService getRegistryService() {
        return registryService;
    }

    protected void loadTenantRegistry(int apiTenantId) throws RegistryException {
        // do nothing
    }

    @Override
    protected RegistryHolder getRegistry(String requestedTenantDomain) throws APIPersistenceException {
        if (registry != null) {
            RegistryHolder holder = new RegistryHolder();
            holder.setRegistry(registry);
            holder.setTenantId(CarbonContext.getThreadLocalCarbonContext().getTenantId());
            return holder;
        } else {
            return super.getRegistry(requestedTenantDomain);
        }

    }
    
    @Override
    protected GenericArtifact getAPIArtifact(String apiId, Registry registry)
            throws APIPersistenceException, GovernanceException {
        if (artifact != null) {
            return artifact;
        } else {
            return super.getAPIArtifact(apiId, registry);
        }

    }

}
