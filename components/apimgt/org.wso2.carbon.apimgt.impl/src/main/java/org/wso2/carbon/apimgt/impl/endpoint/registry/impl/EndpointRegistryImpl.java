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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.endpoint.registry.api.EndpointRegistry;
import org.wso2.carbon.apimgt.api.endpoint.registry.model.EndpointRegistryEntryFilterParams;
import org.wso2.carbon.apimgt.impl.endpoint.registry.constants.EndpointRegistryConstants;
import org.wso2.carbon.apimgt.impl.endpoint.registry.dao.EndpointRegistryDAO;
import org.wso2.carbon.apimgt.api.endpoint.registry.model.EndpointRegistryInfo;
import org.wso2.carbon.apimgt.api.endpoint.registry.model.EndpointRegistryEntry;
import org.wso2.carbon.apimgt.api.endpoint.registry.api.EndpointRegistryException;
import org.wso2.carbon.apimgt.impl.endpoint.registry.util.EndpointRegistryUtil;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
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

            // If the displayName is empty, set the registryName as the displayName
            if (StringUtils.isEmpty(endpointRegistry.getDisplayName())) {
                endpointRegistry.setDisplayName(endpointRegistry.getName());
            }

            // if another registry with the given name or type already exists, fail the operation.
            if (registryDAO.isEndpointRegistryTypeExists(endpointRegistry.getType(), tenantId)) {
                EndpointRegistryUtil.raiseResourceAlreadyExistsException("Endpoint Registry of type '"
                        + endpointRegistry.getType() + "' already exists");
            } else if (registryDAO.isEndpointRegistryNameExists(endpointRegistry.getName(),
                    false, tenantId)) {
                EndpointRegistryUtil.raiseResourceAlreadyExistsException("Endpoint Registry with name '"
                        + endpointRegistry.getName() + "' already exists");
            } else if (registryDAO.isEndpointRegistryNameExists(endpointRegistry.getDisplayName(),
                    true, tenantId)) {
                EndpointRegistryUtil.raiseResourceAlreadyExistsException("Endpoint Registry with display name '"
                        + endpointRegistry.getDisplayName() + "' already exists");
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
     * @param tenantDomain Tenant domain
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
     * @param tenantDomain Tenant domain
     * @return A list of EndpointRegistryInfo objects
     * @throws EndpointRegistryException if failed to get details of an Endpoint Registries
     */
    public List<EndpointRegistryInfo> getEndpointRegistries(String tenantDomain) throws EndpointRegistryException {

        int tenantId;
        try {
            tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomain);
            EndpointRegistryInfo endpointRegistryInfo = registryDAO.getEndpointRegistry(tenantId);
            if (endpointRegistryInfo == null) {
                // Create the default registry if no registry found
                EndpointRegistryInfo defaultRegistry = new EndpointRegistryInfo();
                defaultRegistry.setName(EndpointRegistryConstants.DEFAULT_REGISTRY_NAME);
                defaultRegistry.setDisplayName(EndpointRegistryConstants.DEFAULT_REGISTRY_NAME);
                defaultRegistry.setType(EndpointRegistryConstants.REGISTRY_TYPE_WSO2);
                defaultRegistry.setOwner(EndpointRegistryConstants.SYSTEM_USER_NAME);
                String regId = registryDAO.addEndpointRegistry(defaultRegistry, tenantId);
                log.info("Successfully created the default endpoint registry " + defaultRegistry.getName() +
                        " of type :" + defaultRegistry.getType() + " with id :" + regId + " by :" +
                        EndpointRegistryConstants.SYSTEM_USER_NAME);
                endpointRegistryInfo = registryDAO.getEndpointRegistry(tenantId);
            }
            List<EndpointRegistryInfo> endpointRegistryInfoList = new ArrayList<>();
            if (endpointRegistryInfo != null) {
                endpointRegistryInfoList.add(endpointRegistryInfo);
            }
            return endpointRegistryInfoList;
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
     * {@inheritDoc}
     */
    public List<EndpointRegistryEntry> getEndpointRegistryEntries(EndpointRegistryEntryFilterParams filterParams,
                                                                  String registryId) throws EndpointRegistryException {

        return registryDAO.getEndpointRegistryEntries(filterParams, registryId);
    }

    /**
     * Adds a new Registry Entry
     *
     * @param registryEntry EndpointRegistryEntry
     * @return entryID UUID of the created Registry Entry
     * @throws EndpointRegistryException if failed to add EndpointRegistryEntry
     */
    public String addEndpointRegistryEntry(EndpointRegistryEntry registryEntry) throws EndpointRegistryException {

        // If the displayName is empty, set the entryName as the displayName
        if (StringUtils.isEmpty(registryEntry.getDisplayName())) {
            registryEntry.setDisplayName(registryEntry.getEntryName());
        }

        if (registryDAO.isRegistryEntryNameExists(registryEntry, false)) {
            EndpointRegistryUtil.raiseResourceAlreadyExistsException("Endpoint Registry Entry with name '"
                    + registryEntry.getEntryName() + "' already exists");
        } else if (registryDAO.isRegistryEntryNameExists(registryEntry, true)) {
            EndpointRegistryUtil.raiseResourceAlreadyExistsException("Endpoint Registry Entry with display name '"
                    + registryEntry.getDisplayName() + "' already exists");
        }
        return registryDAO.addEndpointRegistryEntry(registryEntry, username);
    }

    /**
     * Updates Registry Entry
     *
     * @param displayName   original display name of the registry entry
     * @param registryEntry EndpointRegistryEntry
     * @throws EndpointRegistryException if failed to update EndpointRegistryEntry
     */
    public void updateEndpointRegistryEntry(String displayName, EndpointRegistryEntry registryEntry)
            throws EndpointRegistryException {

        // If the displayName is empty, set the old displayName
        if (StringUtils.isEmpty(registryEntry.getDisplayName())) {
            registryEntry.setDisplayName(displayName);
        }

        if (!displayName.equals(registryEntry.getDisplayName()) &&
                registryDAO.isRegistryEntryNameExists(registryEntry, true)) {
            EndpointRegistryUtil.raiseResourceAlreadyExistsException("Endpoint Registry Entry with name '"
                    + registryEntry.getDisplayName() + "' already exists");
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
    public void updateEndpointRegistry(String registryId, String registryDisplayName, String registryType,
                                       EndpointRegistryInfo endpointRegistryInfo) throws EndpointRegistryException {

        String tenantDomain = MultitenantUtils
                .getTenantDomain(APIUtil.replaceEmailDomainBack(endpointRegistryInfo.getOwner()));
        int tenantId;
        try {
            tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomain);

            // If the displayName is empty, set the old displayName
            if (StringUtils.isEmpty(endpointRegistryInfo.getDisplayName())) {
                endpointRegistryInfo.setDisplayName(registryDisplayName);
            }

            // if another registry with the updated name or type already exists, fail the operation.
            if (!registryType.equals(endpointRegistryInfo.getType()) &&
                    registryDAO.isEndpointRegistryTypeExists(endpointRegistryInfo.getType(), tenantId)) {
                EndpointRegistryUtil.raiseResourceAlreadyExistsException("Endpoint Registry of type '"
                        + endpointRegistryInfo.getType() + "' already exists");
            } else if (!registryDisplayName.equals(endpointRegistryInfo.getDisplayName()) &&
                    registryDAO.isEndpointRegistryNameExists(endpointRegistryInfo.getDisplayName(),
                            true, tenantId)) {
                EndpointRegistryUtil.raiseResourceAlreadyExistsException("Endpoint Registry with name '"
                        + endpointRegistryInfo.getDisplayName() + "' already exists");
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
            EndpointRegistryUtil.raiseResourceAlreadyExistsException("Endpoint Registry Entry with name '"
                    + registryEntry.getEntryName() + "' and version '" + registryEntry.getVersion() + "' already exists");
        }
        return registryDAO.addEndpointRegistryEntry(registryEntry, username);
    }

}
