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

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.endpoint.registry.model.EndpointRegistryEntryFilterParams;
import org.wso2.carbon.apimgt.impl.endpoint.registry.constants.EndpointRegistryConstants;
import org.wso2.carbon.apimgt.api.endpoint.registry.model.EndpointRegistryEntry;
import org.wso2.carbon.apimgt.api.endpoint.registry.model.EndpointRegistryInfo;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.RegistriesApi;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.dto.RegistryDTO;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.dto.RegistryEntryDTO;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;

import java.io.InputStream;

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
        registry.setDisplayName(registryDTO.getDisplayName());
        registry.setOwner(owner);
        if (registryDTO.getType() != null) {
            registry.setType(registryDTO.getType().toString());
        } else {
            registry.setType(RegistryDTO.TypeEnum.WSO2.toString());
        }
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
        if (StringUtils.isEmpty(registry.getDisplayName())) {
            registryDTO.setDisplayName(registry.getName());
        } else {
            registryDTO.setDisplayName(registry.getDisplayName());
        }
        registryDTO.setType(RegistryDTO.TypeEnum.fromValue(registry.getType()));
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
        registryEntryDTO.setEntryName(registryEntry.getEntryName());
        registryEntryDTO.setDisplayName(registryEntry.getDisplayName());
        registryEntryDTO.setVersion(registryEntry.getVersion());
        registryEntryDTO.setDescription(registryEntry.getDescription());
        registryEntryDTO.setDefinitionType(
                RegistryEntryDTO.DefinitionTypeEnum.fromValue(registryEntry.getDefinitionType()));
        registryEntryDTO.setDefinitionUrl(registryEntry.getDefinitionURL());
        registryEntryDTO.setServiceType(RegistryEntryDTO.ServiceTypeEnum.fromValue(registryEntry.getServiceType()));
        registryEntryDTO.setProductionServiceUrl(registryEntry.getProductionServiceURL());
        registryEntryDTO.setSandboxServiceUrl(registryEntry.getSandboxServiceUrl());
        registryEntryDTO.setServiceCategory(RegistryEntryDTO.ServiceCategoryEnum.fromValue(registryEntry
                .getServiceCategory()));
        return registryEntryDTO;
    }

    /**
     * Converts a RegistryEntryDTO object with endpointDefinition file into EndpointRegistryEntry object
     *
     * @param registryEntryDTO   RegistryEntryDTO object
     * @param entryUUID          Registry Entry Identifier(UUID)
     * @param endpointDefinition endpointDefinition file
     * @return EndpointRegistryEntry corresponds to RegistryEntryDTO object
     */
    public static EndpointRegistryEntry fromDTOToRegistryEntry(RegistryEntryDTO registryEntryDTO, String entryUUID,
                                                               InputStream endpointDefinition, int registryId) {
        EndpointRegistryEntry registryEntry = new EndpointRegistryEntry();
        registryEntry.setEntryId(entryUUID);
        registryEntry.setEntryName(registryEntryDTO.getEntryName());
        if (StringUtils.isEmpty(registryEntryDTO.getDisplayName())) {
            registryEntry.setDisplayName(registryEntryDTO.getEntryName());
        } else {
            registryEntry.setDisplayName(registryEntryDTO.getDisplayName());
        }
        registryEntry.setVersion(registryEntryDTO.getVersion());
        registryEntry.setDescription(registryEntryDTO.getDescription());
        if (registryEntryDTO.getDefinitionType() != null) {
            registryEntry.setDefinitionType(registryEntryDTO.getDefinitionType().toString());
        }
        registryEntry.setDefinitionURL(registryEntryDTO.getDefinitionUrl());
        registryEntry.setEndpointDefinition(endpointDefinition);
        if (registryEntryDTO.getServiceType() != null) {
            registryEntry.setServiceType(registryEntryDTO.getServiceType().toString());
        }
        registryEntry.setProductionServiceURL(registryEntryDTO.getProductionServiceUrl());
        registryEntry.setSandboxServiceUrl(registryEntryDTO.getSandboxServiceUrl());
        registryEntry.setRegistryId(registryId);
        if (registryEntryDTO.getServiceCategory() != null) {
            registryEntry.setServiceCategory(registryEntryDTO.getServiceCategory().toString());
        }
        return registryEntry;
    }

    /***
     * Converts the sort by object according to the input
     *
     * @param sortBy Sort By field name
     * @return Updated sort by field
     */
    public static String getRegistryEntriesSortByField(RegistriesApi.SortEntryByEnum sortBy) {
        String updatedSortBy = StringUtils.EMPTY;
        if (sortBy == null) {
            updatedSortBy = EndpointRegistryConstants.COLUMN_ENTRY_NAME; // default sortBy field
        } else if (RestApiConstants.ENDPOINT_REG_ENTRY_DEFINITION_TYPE.equals(sortBy.toString())) {
            updatedSortBy = EndpointRegistryConstants.COLUMN_DEFINITION_TYPE;
        } else if (RestApiConstants.ENDPOINT_REG_ENTRY_SERVICE_TYPE.equals(sortBy.toString())) {
            updatedSortBy = EndpointRegistryConstants.COLUMN_SERVICE_TYPE;
        }
        return updatedSortBy;
    }

    /**
     * Creates a EndpointRegistryEntryFilterParams object
     *
     * @param name            Entry name
     * @param displayName     Entry display name
     * @param version         Entry version
     * @param serviceType     Service Type
     * @param serviceCategory Service category
     * @param definitionType  Definition type
     * @param sortEntryBy     Sort by field name
     * @param sortEntryOrder  Sorting order
     * @param limit           Pagination limit
     * @param offset          Pagination offset
     * @return EndpointRegistryEntry corresponds to RegistryEntryDTO object
     */
    public static EndpointRegistryEntryFilterParams getRegistryEntryFilterParams(
            String name, String displayName, String version, RegistriesApi.ServiceTypeEnum serviceType,
            RegistriesApi.ServiceCategoryEnum serviceCategory, RegistriesApi.DefinitionTypeEnum definitionType,
            RegistriesApi.SortEntryByEnum sortEntryBy, RegistriesApi.SortEntryOrderEnum sortEntryOrder,
            Integer limit, Integer offset) {

        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        String sortOrder = sortEntryOrder != null ? sortEntryOrder.toString() : RestApiConstants.DEFAULT_SORT_ORDER;
        String sortBy = EndpointRegistryMappingUtils.getRegistryEntriesSortByField(sortEntryBy);
        name = name == null ? StringUtils.EMPTY : name;
        displayName = displayName == null ? StringUtils.EMPTY : displayName;
        version = version == null ? StringUtils.EMPTY : version;
        String serviceTypeStr = serviceType == null ? StringUtils.EMPTY : serviceType.toString();
        String definitionTypeStr = definitionType == null ? StringUtils.EMPTY : definitionType.toString();
        String serviceCategoryStr = serviceCategory == null ? StringUtils.EMPTY : serviceCategory.toString();

        EndpointRegistryEntryFilterParams filterParams = new EndpointRegistryEntryFilterParams();
        filterParams.setEntryName(name);
        filterParams.setDisplayName(displayName);
        filterParams.setVersion(version);
        filterParams.setServiceType(serviceTypeStr);
        filterParams.setServiceCategory(serviceCategoryStr);
        filterParams.setDefinitionType(definitionTypeStr);
        filterParams.setSortBy(sortBy);
        filterParams.setSortOrder(sortOrder);
        filterParams.setLimit(limit);
        filterParams.setOffset(offset);

        return filterParams;
    }

}
