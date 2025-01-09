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
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.ScopesDAO;
import org.wso2.carbon.apimgt.persistence.APIPersistence;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.util.Map;

public class AbstractAPIManagerWrapper extends AbstractAPIManager {

    private GenericArtifactManager genericArtifactManager;
    private RegistryService registryService;
    private TenantManager tenantManager;

    public AbstractAPIManagerWrapper(GenericArtifactManager genericArtifactManager) throws APIManagementException {
        this.genericArtifactManager = genericArtifactManager;
    }

    public AbstractAPIManagerWrapper(GenericArtifactManager genericArtifactManager, RegistryService registryService,
            Registry registry, TenantManager tenantManager) throws APIManagementException {
        this.genericArtifactManager = genericArtifactManager;
        this.tenantManager = tenantManager;
        this.registryService = registryService;
    }

    public AbstractAPIManagerWrapper(ApiMgtDAO apiMgtDAO) throws APIManagementException {
        this.apiMgtDAO = apiMgtDAO;
    }
    public AbstractAPIManagerWrapper(ScopesDAO scopesDAO) throws APIManagementException {
        this.scopesDAO = scopesDAO;
    }
    public AbstractAPIManagerWrapper(APIPersistence persistance) throws APIManagementException {
        this.apiPersistenceInstance = persistance;
    }

    public AbstractAPIManagerWrapper(GenericArtifactManager genericArtifactManager, RegistryService registryService,
            Registry registry, TenantManager tenantManager, ApiMgtDAO apiMgtDAO) throws APIManagementException {
        this.genericArtifactManager = genericArtifactManager;
        this.tenantManager = tenantManager;
        this.registryService = registryService;
        this.apiMgtDAO = apiMgtDAO;
    }

    public AbstractAPIManagerWrapper(GenericArtifactManager genericArtifactManager, RegistryService registryService,
                                     Registry registry, TenantManager tenantManager, ApiMgtDAO apiMgtDAO, APIPersistence persistance)
            throws APIManagementException {
        this.genericArtifactManager = genericArtifactManager;
        this.tenantManager = tenantManager;
        this.registryService = registryService;
        this.apiMgtDAO = apiMgtDAO;
        this.apiPersistenceInstance = persistance;
    }

    @Override
    protected TenantManager getTenantManager() {return tenantManager;}

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

    protected String getTenantDomain(Identifier identifier) {
        return "carbon.super";
    }

    protected String getTenantDomain(String username) {
        return "carbon.super";
    }

    protected String getTenantDomainFromUrl(String url) {
        return "carbon.super";
    }

    protected void startTenantFlow(String tenantDomain) {
        // Do Nothing
    }

    protected void endTenantFlow() {
        // Do Nothing
    }

    protected String getTenantAwareUsername(String username){
        return "admin";
    }


    @Override
    public ApiTypeWrapper getAPIorAPIProductByUUID(String uuid, String organization) throws APIManagementException {
        return null;
    }

    @Override
    public API getLightweightAPIByUUID(String uuid, String organization) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Object> searchPaginatedAPIs(String searchQuery, String organization, int start, int end)
            throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Object> searchPaginatedContent(String searchQuery, String tenantDomain, int start, int end)
            throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }
}
