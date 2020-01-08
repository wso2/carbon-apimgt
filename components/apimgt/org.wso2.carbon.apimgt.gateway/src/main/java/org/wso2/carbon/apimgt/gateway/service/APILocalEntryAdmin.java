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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.gateway.utils.LocalEntryServiceProxy;

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
        return new LocalEntryServiceProxy(tenantDomain);
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