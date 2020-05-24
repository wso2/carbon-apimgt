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

package org.wso2.carbon.apimgt.api;

import org.wso2.carbon.apimgt.api.model.EndpointRegistryEntry;
import org.wso2.carbon.apimgt.api.model.EndpointRegistryInfo;

import java.util.List;

/**
 * EndpointRegistry Interface
 */
public interface EndpointRegistry {

    /**
     * Adds a new Endpoint Registry
     *
     * @param endpointRegistry EndpointRegistryInfo
     * @return registryId UUID of the created Endpoint Registry ID
     * @throws APIManagementException if failed to add EndpointRegistryInfo
     */
    String addEndpointRegistry(EndpointRegistryInfo endpointRegistry) throws APIManagementException;

    /**
     * Returns details of an Endpoint Registry
     *
     * @param registryId   Registry Identifier
     * @param tenantDomain
     * @return An EndpointRegistryInfo object related to the given identifier or null
     * @throws APIManagementException if failed to get details of an Endpoint Registry
     */
    EndpointRegistryInfo getEndpointRegistryByUUID(String registryId, String tenantDomain)
            throws APIManagementException;

    /**
     * Deletes an Endpoint Registry
     *
     * @param registryUUID Registry Identifier(UUID)
     * @throws APIManagementException if failed to delete the Endpoint Registry
     */
    void deleteEndpointRegistry(String registryUUID) throws APIManagementException;

    /**
     * Returns details of all Endpoint Registries belong to a given tenant
     *
     * @param sortBy       Name of the sorting field
     * @param sortOrder    Order of sorting (asc or desc)
     * @param limit        Limit
     * @param offset       Offset
     * @param tenantDomain
     * @return A list of EndpointRegistryInfo objects
     * @throws APIManagementException if failed to get details of Endpoint Registries
     */
    List<EndpointRegistryInfo> getEndpointRegistries(String sortBy, String sortOrder, int limit, int offset,
                                                     String tenantDomain) throws APIManagementException;

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
    List<EndpointRegistryEntry> getEndpointRegistryEntries(String sortBy, String sortOrder, int limit,
                                                           int offset, String registryId, String serviceType,
                                                           String definitionType, String entryName,
                                                           String serviceCategory) throws APIManagementException;

    /**
     * Returns details of a specific Endpoint Registry Entry
     *
     * @return an EndpointRegistryEntry object
     * @throws APIManagementException if failed get details of an Endpoint Registry Entry
     */
    EndpointRegistryEntry getEndpointRegistryEntryByUUID(String registryId, String registryEntryUuid) throws APIManagementException;

    /**
     * Adds a new Registry Entry
     *
     * @param registryEntry EndpointRegistryEntry
     * @return entryID UUID of the created Registry Entry
     * @throws APIManagementException if failed to add EndpointRegistryEntry
     */
    String addEndpointRegistryEntry(EndpointRegistryEntry registryEntry) throws APIManagementException;

    /**
     * Updates Registry Entry
     *
     * @param entryName     original name of the registry entry
     * @param registryEntry EndpointRegistryEntry
     * @throws APIManagementException if failed to update EndpointRegistryEntry
     */
    void updateEndpointRegistryEntry(String entryName, EndpointRegistryEntry registryEntry)
            throws APIManagementException;

    /**
     * Deletes an Endpoint Registry Entry
     *
     * @param entryId Registry Entry Identifier(UUID)
     * @throws APIManagementException if failed to delete the Endpoint Registry Entry
     */
    void deleteEndpointRegistryEntry(String entryId) throws APIManagementException;

    /**
     * Updates an existing endpoint registry
     *
     * @param registryId           uuid of the endpoint registry which needs to be updated
     * @param registryName         original name of the registry for the given registryId
     * @param endpointRegistryInfo EndpointRegistryInfo object with details to be updated
     * @return uuid of the endpoint registry
     * @throws APIManagementException if failed to update the endpoint registry
     */
    void updateEndpointRegistry(String registryId, String registryName, EndpointRegistryInfo endpointRegistryInfo)
            throws APIManagementException;
}
