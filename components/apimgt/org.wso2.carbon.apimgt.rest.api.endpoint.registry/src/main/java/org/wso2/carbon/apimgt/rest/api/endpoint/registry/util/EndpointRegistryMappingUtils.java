/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.rest.api.endpoint.registry.util;

import org.wso2.carbon.apimgt.api.model.EndpointRegistryEntry;
import org.wso2.carbon.apimgt.api.model.EndpointRegistryInfo;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.dto.RegistryDTO;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.dto.RegistryEntryDTO;

/**
 * This class is responsible for mapping APIM core Endpoint Registry related objects into REST API
 * Endpoint Registry related DTOs
 */
public class EndpointRegistryMappingUtils {

    /**
     * Converts a RegistryDTO object into EndpointRegistryInfo object
     *
     * @param registryDTO RegistryDTO object
     * @return EndpointRegistryInfo corresponds to RegistryDTO object
     */
    public static EndpointRegistryInfo fromDTOtoEndpointRegistry(RegistryDTO registryDTO, String owner) {
        EndpointRegistryInfo registry = new EndpointRegistryInfo();
        registry.setName(registryDTO.getName());
        registry.setOwner(owner);
        registry.setType(registryDTO.getType().toString());
        registry.setMode(registryDTO.getMode().toString());
        return registry;
    }

    /**
     * Converts a EndpointRegistryInfo object into RegistryDTO object
     *
     * @param registry EndpointRegistryInfo object
     * @return RegistryDTO corresponds to EndpointRegistryInfo object
     */
    public static RegistryDTO fromEndpointRegistryToDTO(EndpointRegistryInfo registry) {
        RegistryDTO registryDTO = new RegistryDTO();
        registryDTO.setId(registry.getUuid());
        registryDTO.setName(registry.getName());
        registryDTO.setType(RegistryDTO.TypeEnum.fromValue(registry.getType()));
        registryDTO.setMode(RegistryDTO.ModeEnum.fromValue(registry.getMode()));
        registryDTO.setOwner(registry.getOwner());
        return registryDTO;
    }

    /**
     * Converts a EndpointRegistryEntry object into RegistryEntryDTO object
     *
     * @param registryEntry EndpointRegistryEntry object
     * @return RegistryEntryDTO corresponds to EndpointRegistryEntry object
     */
    public static RegistryEntryDTO fromRegistryEntryToDTO(EndpointRegistryEntry registryEntry) {
        RegistryEntryDTO registryEntryDTO = new RegistryEntryDTO();
        registryEntryDTO.setId(registryEntry.getEntryId());
        registryEntryDTO.setEntryName(registryEntry.getName());
        registryEntryDTO.setDefinitionType(
                RegistryEntryDTO.DefinitionTypeEnum.fromValue(registryEntry.getDefinitionType()));
        registryEntryDTO.setDefinitionUrl(registryEntry.getDefinitionURL());
        registryEntryDTO.setMetadata(registryEntry.getMetaData());
        registryEntryDTO.setServiceType(RegistryEntryDTO.ServiceTypeEnum.fromValue(registryEntry.getServiceType()));
        registryEntryDTO.setServiceUrl(registryEntry.getServiceURL());
        return registryEntryDTO;
    }

    /**
     * Converts a RegistryEntryDTO object with endpointDefinition file into EndpointRegistryEntry object
     *
     * @param registryEntryDTO RegistryEntryDTO object
     * @param endpointDefinition endpointDefinition file
     * @return EndpointRegistryEntry corresponds to RegistryEntryDTO object
     */
    public static EndpointRegistryEntry fromDTOToRegistryEntry(RegistryEntryDTO registryEntryDTO,
                                                               ResourceFile endpointDefinition, int registryId) {
        EndpointRegistryEntry registryEntry = new EndpointRegistryEntry();
        registryEntry.setName(registryEntryDTO.getEntryName());
        registryEntry.setDefinitionType(registryEntryDTO.getDefinitionType().toString());
        registryEntry.setDefinitionURL(registryEntryDTO.getDefinitionUrl());
        registryEntry.setEndpointDefinition(endpointDefinition);
        registryEntry.setMetaData(registryEntryDTO.getMetadata());
        registryEntry.setServiceType(registryEntryDTO.getServiceType().toString());
        registryEntry.setServiceURL(registryEntryDTO.getServiceUrl());
        registryEntry.setRegistryId(registryId);
        return registryEntry;
    }
}
