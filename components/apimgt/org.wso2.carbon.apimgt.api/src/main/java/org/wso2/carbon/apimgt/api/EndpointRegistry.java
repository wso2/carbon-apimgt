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
     * @param registryId Registry Identifier
     * @return An EndpointRegistryInfo object related to the given identifier or null
     * @throws APIManagementException if failed to get details of an Endpoint Registry
     */
    EndpointRegistryInfo getEndpointRegistryByUUID(String registryId) throws APIManagementException;

    /**
     * Deletes an Endpoint Registry
     *
     * @param registryId Registry Identifier(UUID)
     * @throws APIManagementException if failed to delete the Endpoint Registry
     */
    void deleteEndpointRegistry(String registryId) throws APIManagementException;

    /**
     * Returns details of all Endpoint Registries belong to a given tenant
     *
     * @param tenantDomain
     * @return A list of EndpointRegistryInfo objects
     * @throws APIManagementException if failed to get details of Endpoint Registries
     */
    List<EndpointRegistryInfo> getEndpointRegistries(String tenantDomain) throws APIManagementException;

    /**
     * Returns all entries belong to a given endpoint registry
     *
     * @param registryId UUID of the endpoint registry
     * @return A list of EndpointRegistryEntry objects
     * @throws APIManagementException if failed to get entries of an Endpoint Registry
     */
    List<EndpointRegistryEntry> getEndpointRegistryEntries(String registryId) throws APIManagementException;

    /**
     * Returns details of a specific Endpoint Registry Entry
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
}
