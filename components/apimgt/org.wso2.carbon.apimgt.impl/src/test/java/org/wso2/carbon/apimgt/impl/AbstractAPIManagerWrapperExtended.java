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
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.jdbc.dataobjects.ResourceDO;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.tenant.TenantManager;

public class AbstractAPIManagerWrapperExtended extends AbstractAPIManagerWrapper{

    public AbstractAPIManagerWrapperExtended(GenericArtifactManager genericArtifactManager,
            RegistryService registryService, Registry registry, TenantManager tenantManager)
            throws APIManagementException {
        super(genericArtifactManager, registryService, registry, tenantManager);
    }

    @Override
    protected String getTenantDomain(APIIdentifier identifier) {
        return "abc.com";
    }

    @Override
    public Resource getCustomMediationResourceFromUuid(String mediationPolicyId){
        return new ResourceImpl("/apimgt/apis", new ResourceDO());
    }
    @Override
    public Resource getApiSpecificMediationResourceFromUuid(String uuid, String resourcePath){
        return new ResourceImpl("/apimgt/apis", new ResourceDO());
    }

}
