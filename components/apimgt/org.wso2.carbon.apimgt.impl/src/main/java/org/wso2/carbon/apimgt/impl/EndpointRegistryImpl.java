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
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.EndpointRegistry;
import org.wso2.carbon.apimgt.api.model.EndpointRegistryEntry;
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

    private static final Log log = LogFactory.getLog(EndpointRegistryImpl.class);
    private ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
    private String username;

    public EndpointRegistryImpl(String username) {
        this.username = username;
    }

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
     * @param registryId   Registry Identifier
     * @param tenantDomain
     * @return An EndpointRegistryInfo object related to the given identifier or null
     * @throws APIManagementException if failed to get details of an Endpoint Registry
     */
    public EndpointRegistryInfo getEndpointRegistryByUUID(String registryId, String tenantDomain)
            throws APIManagementException {
        int tenantId;
        try {
            tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomain);
            return apiMgtDAO.getEndpointRegistryByUUID(registryId, tenantId);
        } catch (UserStoreException e) {
            String msg = "Error while retrieving tenant information";
            log.error(msg, e);
            throw new APIManagementException("Error in retrieving Tenant Information while retrieving endpoint" +
                    " registry given by id: " + registryId, e);
        }
    }

    /**
     * Deletes an Endpoint Registry
     *
     * @param registryUUID Registry Identifier(UUID)
     * @throws APIManagementException if failed to delete the Endpoint Registry
     */
    public void deleteEndpointRegistry(String registryUUID) throws APIManagementException {
        apiMgtDAO.deleteEndpointRegistry(registryUUID);
    }

    /**
     * Returns details of all Endpoint Registries belong to a given tenant
     *
     * @param sortBy       Name of the sorting field
     * @param sortOrder    Order of sorting (asc or desc)
     * @param limit        Limit
     * @param offset       Offset
     * @param tenantDomain
     * @return A list of EndpointRegistryInfo objects
     * @throws APIManagementException if failed to get details of an Endpoint Registries
     */
    public List<EndpointRegistryInfo> getEndpointRegistries(String sortBy, String sortOrder, int limit, int offset,
                                                            String tenantDomain) throws APIManagementException {
        int tenantId;
        try {
            tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomain);
            return apiMgtDAO.getEndpointRegistries(sortBy, sortOrder, limit, offset, tenantId);
        } catch (UserStoreException e) {
            String msg = "Error while retrieving tenant information";
            log.error(msg, e);
            throw new APIManagementException("Error in retrieving Tenant Information while retrieving details of " +
                    "endpoint registries", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public EndpointRegistryEntry getEndpointRegistryEntryByUUID(String registryId, String registryEntryUuid)
            throws APIManagementException {
        return apiMgtDAO.getEndpointRegistryEntryByUUID(registryEntryUuid);
    }

    /**
     * Returns all entries belong to a given endpoint registry
     *
     * @param sortBy     Name of the sorting field
     * @param sortOrder  Order of sorting (asc or desc)
     * @param limit      Limit
     * @param offset     Offset
     * @param registryId UUID of the endpoint registry
     * @param serviceType The endpoint service type
     * @param definitionType Then endpoint definition type
     * @param entryName The registry entry name
     * @param serviceCategory The service category
     * @return A list of EndpointRegistryEntry objects
     * @throws APIManagementException if failed to get entries of an Endpoint Registry
     */
    public List<EndpointRegistryEntry> getEndpointRegistryEntries(String sortBy, String sortOrder, int limit,
                                                                  int offset, String registryId, String serviceType,
                                                                  String definitionType, String entryName,
                                                                  String serviceCategory)
            throws APIManagementException {
        return apiMgtDAO.getEndpointRegistryEntries(sortBy, sortOrder, limit, offset, registryId, serviceType,
                definitionType, entryName, serviceCategory);
    }

    /**
     * Adds a new Registry Entry
     *
     * @param registryEntry EndpointRegistryEntry
     * @return entryID UUID of the created Registry Entry
     * @throws APIManagementException if failed to add EndpointRegistryEntry
     */
    public String addEndpointRegistryEntry(EndpointRegistryEntry registryEntry) throws APIManagementException {
        if (apiMgtDAO.isRegistryEntryNameExists(registryEntry)) {
            APIUtil.handleResourceAlreadyExistsException("Endpoint Registry Entry with name '"
                    + registryEntry.getName() + "' already exists");
        }
        return apiMgtDAO.addEndpointRegistryEntry(registryEntry, username);
    }

    /**
     * Updates Registry Entry
     *
     * @param entryName     original name of the registry entry
     * @param registryEntry EndpointRegistryEntry
     * @throws APIManagementException if failed to update EndpointRegistryEntry
     */
    public void updateEndpointRegistryEntry(String entryName, EndpointRegistryEntry registryEntry)
            throws APIManagementException {
        if (!entryName.equals(registryEntry.getName()) &&
                apiMgtDAO.isRegistryEntryNameExists(registryEntry)) {
            APIUtil.handleResourceAlreadyExistsException("Endpoint Registry Entry with name '"
                    + registryEntry.getName() + "' already exists");
        }
        apiMgtDAO.updateEndpointRegistryEntry(registryEntry, username);
    }

    /**
     * Deletes an Endpoint Registry Entry
     *
     * @param entryId Registry Entry Identifier(UUID)
     * @throws APIManagementException if failed to delete the Endpoint Registry Entry
     */
    public void deleteEndpointRegistryEntry(String entryId) throws APIManagementException {
        apiMgtDAO.deleteEndpointRegistryEntry(entryId);
    }

    /**
     * {@inheritDoc}
     */
    public void updateEndpointRegistry(String registryId, String registryName, EndpointRegistryInfo
            endpointRegistryInfo) throws APIManagementException {

        String tenantDomain = MultitenantUtils
                .getTenantDomain(APIUtil.replaceEmailDomainBack(endpointRegistryInfo.getOwner()));
        int tenantId;
        try {
            tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomain);
            // if another registry with the updated name already exists, fail the operation.
            if (!registryName.equals(endpointRegistryInfo.getName()) &&
                    apiMgtDAO.isEndpointRegistryNameExists(endpointRegistryInfo.getName(), tenantId)) {
                APIUtil.handleResourceAlreadyExistsException("Endpoint Registry with name '" + endpointRegistryInfo
                        .getName() + "' already exists");
            }
            apiMgtDAO.updateEndpointRegistry(registryId, endpointRegistryInfo, username);
        } catch (UserStoreException e) {
            String msg = "Error while retrieving tenant information";
            log.error(msg, e);
            throw new APIManagementException("Error in retrieving Tenant Information while updating " +
                    "endpoint registry with id :" + registryId, e);
        }

    }

}
