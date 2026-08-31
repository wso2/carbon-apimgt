/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.service;

import org.apache.axis2.AxisFault;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.gateway.utils.LocalEntryServiceProxy;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * API Local Entry Admin Service.
 */
public class APILocalEntryAdmin extends org.wso2.carbon.core.AbstractAdmin {

    private static Log log = LogFactory.getLog(APILocalEntryAdmin.class);

    /**
     * Add Local Entry to the gateway.
     *
     * @param content
     * @param tenantDomain Tenant Domain
     * @return Status of the operation
     * @throws AxisFault
     */
    public boolean addLocalEntry(String content, String tenantDomain) throws AxisFault {

        LocalEntryServiceProxy localEntryServiceProxy = getLocalEntryAdminClient(tenantDomain);
        return localEntryServiceProxy.addLocalEntry(content);
    }

    /**
     * Get the Local entry client.
     *
     * @param tenantDomain Tenant Domain
     * @return LocalEntryServiceProxy
     * @throws AxisFault
     */
    protected LocalEntryServiceProxy getLocalEntryAdminClient(String tenantDomain) throws AxisFault {
        assertTenantAccessAllowed(tenantDomain);
        return new LocalEntryServiceProxy(tenantDomain);
    }

    /**
     * Rejects the request unless the caller is the super tenant or is naming their own tenant.
     * Every operation on this service is reachable by any tenant administrator holding the default
     * admin permission in their own realm; the tenant to operate on is otherwise taken from the
     * request unchecked.
     */
    static void assertTenantAccessAllowed(String tenantDomain) throws AxisFault {

        String callerTenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        boolean callerIsSuperTenant = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(
                callerTenantDomain);
        if (!callerIsSuperTenant && !StringUtils.equalsIgnoreCase(tenantDomain, callerTenantDomain)) {
            throw new AxisFault("Tenant admin '" + callerTenantDomain
                    + "' is not permitted to act on tenant '" + tenantDomain + "'");
        }
    }

    /**
     * Get the Local entry for given API.
     *
     * @param key          key of the existing local entry.
     * @param tenantDomain Tenant Domain
     * @return LocalEntry
     * @throws AxisFault
     */
    public Object getEntry(String key, String tenantDomain) throws AxisFault {
        LocalEntryServiceProxy localEntryServiceProxy = getLocalEntryAdminClient(tenantDomain);
        return localEntryServiceProxy.getEntry(key);
    }

    /**
     *
     * LocalEntry Exists in Gateway.
     * @param key localentry key
     * @param tenantDomain tenantDomain
     * @return true if exists.
     * @throws AxisFault
     */
    public boolean isLocalEntryExists(String key, String tenantDomain) throws AxisFault {

        LocalEntryServiceProxy localEntryServiceProxy = getLocalEntryAdminClient(tenantDomain);
        return localEntryServiceProxy.isEntryExists(key);
    }

    /**
     * Delete the local entry.
     *
     * @param key          Key of the local entry to be deleted.
     * @param tenantDomain Tenant Domain
     * @return Status of the operation
     * @throws AxisFault
     */
    public Boolean deleteLocalEntry(String key, String tenantDomain) throws AxisFault {
        LocalEntryServiceProxy localEntryServiceProxy = getLocalEntryAdminClient(tenantDomain);
        return localEntryServiceProxy.deleteEntry(key);
    }
}