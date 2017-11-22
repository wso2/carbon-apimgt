/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.nio.charset.Charset;

public class APIMRegistryServiceImpl implements APIMRegistryService {

    private static final Log log = LogFactory.getLog(APIMRegistryServiceImpl.class);

    @Override
    public String getConfigRegistryResourceContent(String tenantDomain, final String registryLocation)
                                        throws UserStoreException, RegistryException {
        String content = null;
        if (tenantDomain == null) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);

            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(tenantDomain);
            Registry registry = ServiceReferenceHolder.getInstance().getRegistryService().getConfigSystemRegistry(tenantId);
            APIUtil.loadTenantRegistry(tenantId);
            if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                APIUtil.loadTenantConf(tenantId);
            }

            if (registry.resourceExists(registryLocation)) {
                Resource resource = registry.get(registryLocation);
                content = getString(resource);
            }
        } catch (APIManagementException e) {
            log.error("Error occurred while loading tenant configuration for '" + tenantDomain + "'");

        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

        return content;
    }

    protected String getString(Resource resource) throws RegistryException {
        return new String((byte[]) resource.getContent(), Charset.defaultCharset());
    }

    @Override
    public String getGovernanceRegistryResourceContent(String tenantDomain, String registryLocation)
                                        throws UserStoreException, RegistryException {
        String content = null;
        if (tenantDomain == null) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);

            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(tenantDomain);
            Registry registry = ServiceReferenceHolder.getInstance().getRegistryService().getGovernanceSystemRegistry(tenantId);

            if (registry.resourceExists(registryLocation)) {
                Resource resource = registry.get(registryLocation);
                content = getString(resource);
            }
        }
        finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

        return content;
    }
}
