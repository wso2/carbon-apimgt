/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.apimgt.api.EndpointRegistry;
import org.wso2.carbon.apimgt.api.model.EndpointRegistryInfo;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.List;

/**
 * This class provides the core functionality related to Endpoint Registry
 */
public class EndpointRegistryImpl implements EndpointRegistry {

    private static final Log log = LogFactory.getLog(APIAdminImpl.class);
    ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();

    /**
     * Adds a new Endpoint Registry
     *
     * @param endpointRegistry EndpointRegistryInfo
     * @return registryId UUID of the created Endpoint Registry ID
     * @throws APIManagementException if failed to add EndpointRegistryInfo
     */
    public String addEndpointRegistry(EndpointRegistryInfo endpointRegistry) throws APIManagementException {
        String tenantDomain = MultitenantUtils
                .getTenantDomain(APIUtil.replaceEmailDomainBack(endpointRegistry.getOwner()));
        int tenantId;
        try {
            tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomain);
            if (apiMgtDAO.isEndpointRegistryNameExists(endpointRegistry.getName(), tenantId)) {
                APIUtil.handleResourceAlreadyExistsException("Endpoint Registry with name '" + endpointRegistry
                        .getName() + "' already exists");
            }
            return apiMgtDAO.addEndpointRegistry(endpointRegistry, tenantId);
        } catch (UserStoreException e) {
            String msg = "Error while retrieving tenant information";
            log.error(msg, e);
            throw new APIManagementException("Error in retrieving Tenant Information while adding endpoint" +
                    " registry :" + endpointRegistry.getName(), e);
        }
    }

    /**
     * Returns details of an Endpoint Registry
     *
     * @param registryId Registry Identifier
     * @return An EndpointRegistryInfo object related to the given identifier or null
     * @throws APIManagementException if failed get details of an Endpoint Registry
     */
    public EndpointRegistryInfo getEndpointRegistryByUUID(String registryId) throws APIManagementException {
        return apiMgtDAO.getEndpointRegistryByUUID(registryId);
    }

    /**
     * Returns details of all Endpoint Registries belong to a given tenant
     *
     * @param tenantDomain
     * @return A list of EndpointRegistryInfo object
     * @throws APIManagementException if failed get details of an Endpoint Registries
     */
    public List<EndpointRegistryInfo> getEndpointRegistries(String tenantDomain) throws APIManagementException {
        int tenantId;
        try {
            tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomain);
            return apiMgtDAO.getEndpointRegistries(tenantId);
        } catch (UserStoreException e) {
            String msg = "Error while retrieving tenant information";
            log.error(msg, e);
            throw new APIManagementException("Error in retrieving Tenant Information while retrieving details of " +
                    "endpoint registries", e);
        }
    }
}
