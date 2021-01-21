/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.persistence.APIPersistence;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.jdbc.dataobjects.ResourceDO;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AbstractAPIManagerWrapperExtended extends AbstractAPIManagerWrapper{

    public AbstractAPIManagerWrapperExtended(GenericArtifactManager genericArtifactManager,
            RegistryService registryService, Registry registry, TenantManager tenantManager)
            throws APIManagementException {
        super(genericArtifactManager, registryService, registry, tenantManager);
    }

    public AbstractAPIManagerWrapperExtended(GenericArtifactManager genericArtifactManager,
                                             RegistryService registryService, Registry registry,
                                             TenantManager tenantManager, ApiMgtDAO apiMgtDAO, APIPersistence persistance)
            throws APIManagementException {
        super(genericArtifactManager, registryService, registry, tenantManager, apiMgtDAO, persistance);
    }

    @Override
    protected String getTenantDomain(Identifier identifier) {
        return "abc.com";
    }

    @Override
    public Resource getCustomMediationResourceFromUuid(String mediationPolicyId){
        return new ResourceImpl("/apimgt/apis", new ResourceDO());
    }

    @Override
    public Resource getApiSpecificMediationResourceFromUuid(Identifier identifier, String uuid, String resourcePath) {
        return new ResourceImpl("/apimgt/apis", new ResourceDO());
    }

    public Map<String, Object> searchPaginatedAPIs(Registry registry, int tenantId, String searchQuery, int start,
                                                   int end, boolean limitAttributes, boolean reducedPublisherAPIInfo) throws APIManagementException {
        if (searchQuery.equalsIgnoreCase("api_meta.secured=*true*")) {
            return new HashMap<String, Object>() {{
                put("apis", new ArrayList() {{
                    add(new API(new APIIdentifier("admin", "sxy", "1.0.0")));
                }});
                put("length", 1);
            }};
        } else if (searchQuery.equalsIgnoreCase("name=*test*&api_meta.secured=*true*")) {
            return new HashMap<String, Object>() {{
                put("apis", new ArrayList() {{
                    add(new API(new APIIdentifier("admin", "sxy12", "1.0.0")));
                }});
                put("length", 1);
            }};

        } else {
            return super.searchPaginatedAPIs(registry, tenantId, searchQuery, start, end, limitAttributes, reducedPublisherAPIInfo);
        }
    }
}
