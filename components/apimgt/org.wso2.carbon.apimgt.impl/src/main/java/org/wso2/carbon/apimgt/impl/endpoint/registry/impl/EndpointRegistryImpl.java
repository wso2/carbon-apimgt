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

package org.wso2.carbon.apimgt.impl.endpoint.registry.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.endpoint.registry.api.EndpointRegistry;
import org.wso2.carbon.apimgt.impl.endpoint.registry.dao.EndpointRegistryDAO;
import org.wso2.carbon.apimgt.impl.endpoint.registry.model.EndpointRegistryInfo;
import org.wso2.carbon.apimgt.impl.endpoint.registry.model.EndpointRegistryEntry;
import org.wso2.carbon.apimgt.impl.endpoint.registry.api.EndpointRegistryException;
import org.wso2.carbon.apimgt.impl.endpoint.registry.util.EndpointRegistryUtil;
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
    private EndpointRegistryDAO registryDAO = EndpointRegistryDAO.getInstance();
    private String username;

    public EndpointRegistryImpl(String username) {

        this.username = username;
    }

    /**
     * Adds a new Endpoint Registry
     *
     * @param endpointRegistry EndpointRegistryInfo
     * @return registryId UUID of the created Endpoint Registry ID
     * @throws EndpointRegistryException if failed to add EndpointRegistryInfo
     */
    public String addEndpointRegistry(EndpointRegistryInfo endpointRegistry) throws EndpointRegistryException {

        String tenantDomain = MultitenantUtils
                .getTenantDomain(APIUtil.replaceEmailDomainBack(endpointRegistry.getOwner()));
        int tenantId;
        try {
            tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomain);
            if (registryDAO.isEndpointRegistryNameExists(endpointRegistry.getName(), tenantId)) {
                EndpointRegistryUtil.handleResourceAlreadyExistsException("Endpoint Registry with name '"
                        + endpointRegistry.getName() + "' already exists");
            }
            return registryDAO.addEndpointRegistry(endpointRegistry, tenantId);
        } catch (UserStoreException e) {
            String msg = "Error while retrieving tenant information";
            log.error(msg, e);
            throw new EndpointRegistryException("Error in retrieving Tenant Information while adding endpoint" +
                    " registry :" + endpointRegistry.getName(), e);
        }
    }

    /**
     * Returns details of an Endpoint Registry
     *
     * @param registryId   Registry Identifier
     * @param tenantDomain
     * @return An EndpointRegistryInfo object related to the given identifier or null
     * @throws EndpointRegistryException if failed to get details of an Endpoint Registry
     */
    public EndpointRegistryInfo getEndpointRegistryByUUID(String registryId, String tenantDomain)
            throws EndpointRegistryException {

        int tenantId;
        try {
            tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomain);
            return registryDAO.getEndpointRegistryByUUID(registryId, tenantId);
        } catch (UserStoreException e) {
            String msg = "Error while retrieving tenant information";
            log.error(msg, e);
            throw new EndpointRegistryException("Error in retrieving Tenant Information while retrieving endpoint" +
                    " registry given by id: " + registryId, e);
        }
    }

    /**
     * Deletes an Endpoint Registry
     *
     * @param registryUUID Registry Identifier(UUID)
     * @throws EndpointRegistryException if failed to delete the Endpoint Registry
     */
    public void deleteEndpointRegistry(String registryUUID) throws EndpointRegistryException {

        registryDAO.deleteEndpointRegistry(registryUUID);
    }

    /**
     * Returns details of all Endpoint Registries belong to a given tenant
     *
     * @param name         Registry name
     * @param sortBy       Name of the sorting field
     * @param sortOrder    Order of sorting (asc or desc)
     * @param limit        Limit
     * @param offset       Offset
     * @param tenantDomain
     * @return A list of EndpointRegistryInfo objects
     * @throws EndpointRegistryException if failed to get details of an Endpoint Registries
     */
    public List<EndpointRegistryInfo> getEndpointRegistries(String name, String sortBy, String sortOrder,
                                                            int limit, int offset,
                                                            String tenantDomain) throws EndpointRegistryException {

        int tenantId;
        try {
            tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomain);
            return registryDAO.getEndpointRegistries(name, sortBy, sortOrder, limit, offset, tenantId);
        } catch (UserStoreException e) {
            String msg = "Error while retrieving tenant information";
            log.error(msg, e);
            throw new EndpointRegistryException("Error in retrieving Tenant Information while retrieving details of " +
                    "endpoint registries", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public EndpointRegistryEntry getEndpointRegistryEntryByUUID(String registryId, String registryEntryUuid)
            throws EndpointRegistryException {

        return registryDAO.getEndpointRegistryEntryByUUID(registryEntryUuid);
    }

    /**
     * /**
     * Returns all entries belong to a given endpoint registry
     *
     * @param sortBy          Name of the sorting field
     * @param sortOrder       Order of sorting (asc or desc)
     * @param limit           Limit
     * @param offset          Offset
     * @param registryId      UUID of the endpoint registry
     * @param serviceType     The endpoint service type
     * @param definitionType  Then endpoint definition type
     * @param entryName       The registry entry name
     * @param serviceCategory The service category
     * @param version         The version of registry entry
     * @param exactNameMatch  Whether to perform exact search on name
     * @return A list of EndpointRegistryEntry objects
     * @throws EndpointRegistryException if failed to get entries of an Endpoint Registry
     */
    public List<EndpointRegistryEntry> getEndpointRegistryEntries(String sortBy, String sortOrder, int limit,
                                                                  int offset, String registryId, String serviceType,
                                                                  String definitionType, String entryName,
                                                                  String serviceCategory, String version,
                                                                  boolean exactNameMatch)
            throws EndpointRegistryException {

        return registryDAO.getEndpointRegistryEntries(sortBy, sortOrder, limit, offset, registryId, serviceType,
                definitionType, entryName, serviceCategory, version, exactNameMatch);
    }

    /**
     * Adds a new Registry Entry
     *
     * @param registryEntry EndpointRegistryEntry
     * @return entryID UUID of the created Registry Entry
     * @throws EndpointRegistryException if failed to add EndpointRegistryEntry
     */
    public String addEndpointRegistryEntry(EndpointRegistryEntry registryEntry) throws EndpointRegistryException {

        if (registryDAO.isRegistryEntryNameExists(registryEntry)) {
            EndpointRegistryUtil.handleResourceAlreadyExistsException("Endpoint Registry Entry with name '"
                    + registryEntry.getName() + "' already exists");
        }
        return registryDAO.addEndpointRegistryEntry(registryEntry, username);
    }

    /**
     * Updates Registry Entry
     *
     * @param entryName     original name of the registry entry
     * @param registryEntry EndpointRegistryEntry
     * @throws EndpointRegistryException if failed to update EndpointRegistryEntry
     */
    public void updateEndpointRegistryEntry(String entryName, EndpointRegistryEntry registryEntry)
            throws EndpointRegistryException {

        if (!entryName.equals(registryEntry.getName()) &&
                registryDAO.isRegistryEntryNameExists(registryEntry)) {
            EndpointRegistryUtil.handleResourceAlreadyExistsException("Endpoint Registry Entry with name '"
                    + registryEntry.getName() + "' already exists");
        }
        registryDAO.updateEndpointRegistryEntry(registryEntry, username);
    }

    /**
     * Deletes an Endpoint Registry Entry
     *
     * @param entryId Registry Entry Identifier(UUID)
     * @throws EndpointRegistryException if failed to delete the Endpoint Registry Entry
     */
    public void deleteEndpointRegistryEntry(String entryId) throws EndpointRegistryException {

        registryDAO.deleteEndpointRegistryEntry(entryId);
    }

    /**
     * {@inheritDoc}
     */
    public void updateEndpointRegistry(String registryId, String registryName, EndpointRegistryInfo
            endpointRegistryInfo) throws EndpointRegistryException {

        String tenantDomain = MultitenantUtils
                .getTenantDomain(APIUtil.replaceEmailDomainBack(endpointRegistryInfo.getOwner()));
        int tenantId;
        try {
            tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomain);
            // if another registry with the updated name already exists, fail the operation.
            if (!registryName.equals(endpointRegistryInfo.getName()) &&
                    registryDAO.isEndpointRegistryNameExists(endpointRegistryInfo.getName(), tenantId)) {
                EndpointRegistryUtil.handleResourceAlreadyExistsException("Endpoint Registry with name '"
                        + endpointRegistryInfo.getName() + "' already exists");
            }
            registryDAO.updateEndpointRegistry(registryId, endpointRegistryInfo, username);
        } catch (UserStoreException e) {
            String msg = "Error while retrieving tenant information";
            log.error(msg, e);
            throw new EndpointRegistryException("Error in retrieving Tenant Information while updating " +
                    "endpoint registry with id :" + registryId, e);
        }

    }

    /**
     * Creates a new version of an Endpoint Registry Entry
     *
     * @param entryId       Registry Entry Identifier(UUID)
     * @param registryEntry EndpointRegistryEntry
     * @return entryID UUID of the created Registry Entry
     * @throws EndpointRegistryException if failed to delete the Endpoint Registry Entry
     */
    public String createNewEntryVersion(String entryId, EndpointRegistryEntry registryEntry)
            throws EndpointRegistryException {

        if (registryDAO.isRegistryEntryNameAndVersionExists(registryEntry)) {
            EndpointRegistryUtil.handleResourceAlreadyExistsException("Endpoint Registry Entry with name '"
                    + registryEntry.getName() + "' and version '" + registryEntry.getVersion() + "' already exists");
        }
        return registryDAO.addEndpointRegistryEntry(registryEntry, username);
    }

}
