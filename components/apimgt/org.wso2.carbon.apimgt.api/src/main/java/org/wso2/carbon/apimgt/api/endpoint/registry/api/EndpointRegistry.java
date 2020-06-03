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
package org.wso2.carbon.apimgt.api.endpoint.registry.api;

import org.wso2.carbon.apimgt.api.endpoint.registry.api.EndpointRegistryException;
import org.wso2.carbon.apimgt.api.endpoint.registry.model.EndpointRegistryEntry;
import org.wso2.carbon.apimgt.api.endpoint.registry.model.EndpointRegistryEntryFilterParams;
import org.wso2.carbon.apimgt.api.endpoint.registry.model.EndpointRegistryInfo;

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
     * @throws EndpointRegistryException if failed to add EndpointRegistryInfo
     */
    String addEndpointRegistry(EndpointRegistryInfo endpointRegistry) throws EndpointRegistryException;

    /**
     * Returns details of an Endpoint Registry
     *
     * @param registryId   Registry Identifier
     * @param tenantDomain
     * @return An EndpointRegistryInfo object related to the given identifier or null
     * @throws EndpointRegistryException if failed to get details of an Endpoint Registry
     */
    EndpointRegistryInfo getEndpointRegistryByUUID(String registryId, String tenantDomain)
            throws EndpointRegistryException;

    /**
     * Deletes an Endpoint Registry
     *
     * @param registryUUID Registry Identifier(UUID)
     * @throws EndpointRegistryException if failed to delete the Endpoint Registry
     */
    void deleteEndpointRegistry(String registryUUID) throws EndpointRegistryException;

    /**
     * Returns details of all Endpoint Registries belong to a given tenant
     *
     * @param tenantDomain
     * @return A list of EndpointRegistryInfo objects
     * @throws EndpointRegistryException if failed to get details of Endpoint Registries
     */
    List<EndpointRegistryInfo> getEndpointRegistries(String tenantDomain) throws EndpointRegistryException;

    /**
     * Returns all entries belong to a given endpoint registry
     *
     * @param filterParams Endpoint Registry Entry Filter Parameters
     * @param registryId   UUID of the endpoint registry
     * @return A list of EndpointRegistryEntry objects
     * @throws EndpointRegistryException if failed to get entries of an Endpoint Registry
     */
    List<EndpointRegistryEntry> getEndpointRegistryEntries(EndpointRegistryEntryFilterParams filterParams,
                                                           String registryId) throws EndpointRegistryException;

    /**
     * Returns details of a specific Endpoint Registry Entry
     *
     * @return an EndpointRegistryEntry object
     * @throws EndpointRegistryException if failed get details of an Endpoint Registry Entry
     */
    EndpointRegistryEntry getEndpointRegistryEntryByUUID(String registryId, String registryEntryUuid) throws
            EndpointRegistryException;

    /**
     * Adds a new Registry Entry
     *
     * @param registryEntry EndpointRegistryEntry
     * @return entryID UUID of the created Registry Entry
     * @throws EndpointRegistryException if failed to add EndpointRegistryEntry
     */
    String addEndpointRegistryEntry(EndpointRegistryEntry registryEntry) throws EndpointRegistryException;

    /**
     * Updates Registry Entry
     *
     * @param displayName   original display name of the registry entry
     * @param registryEntry EndpointRegistryEntry
     * @throws EndpointRegistryException if failed to update EndpointRegistryEntry
     */
    void updateEndpointRegistryEntry(String displayName, EndpointRegistryEntry registryEntry)
            throws EndpointRegistryException;

    /**
     * Deletes an Endpoint Registry Entry
     *
     * @param entryId Registry Entry Identifier(UUID)
     * @throws EndpointRegistryException if failed to delete the Endpoint Registry Entry
     */
    void deleteEndpointRegistryEntry(String entryId) throws EndpointRegistryException;

    /**
     * Updates an existing endpoint registry
     *
     * @param registryId           uuid of the endpoint registry which needs to be updated
     * @param registryDisplayName  original display name of the registry for the given registryId
     * @param registryType         original type of the registry for the given registryId
     * @param endpointRegistryInfo EndpointRegistryInfo object with details to be updated
     * @return uuid of the endpoint registry
     * @throws EndpointRegistryException if failed to update the endpoint registry
     */
    void updateEndpointRegistry(String registryId, String registryDisplayName, String registryType,
                                EndpointRegistryInfo endpointRegistryInfo)
            throws EndpointRegistryException;

    /**
     * Creates a new version of an Endpoint Registry Entry
     *
     * @param entryId       Registry Entry Identifier(UUID)
     * @param registryEntry EndpointRegistryEntry
     * @return entryID UUID of the created Registry Entry
     * @throws EndpointRegistryException if failed to delete the Endpoint Registry Entry
     */
    String createNewEntryVersion(String entryId, EndpointRegistryEntry registryEntry)
            throws EndpointRegistryException;
}

