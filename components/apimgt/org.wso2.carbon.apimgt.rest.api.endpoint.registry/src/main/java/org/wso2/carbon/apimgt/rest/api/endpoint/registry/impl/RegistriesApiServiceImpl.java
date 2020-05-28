/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.rest.api.endpoint.registry.impl;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.UnExecutableSchemaGenerator;
import graphql.schema.idl.errors.SchemaProblem;
import graphql.schema.validation.SchemaValidationError;
import graphql.schema.validation.SchemaValidator;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtResourceAlreadyExistsException;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.EndpointRegistry;
import org.wso2.carbon.apimgt.api.model.EndpointRegistryEntry;
import org.wso2.carbon.apimgt.api.model.EndpointRegistryInfo;
import org.wso2.carbon.apimgt.impl.EndpointRegistryConstants;
import org.wso2.carbon.apimgt.impl.EndpointRegistryImpl;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.impl.importexport.utils.CommonUtil;
import org.wso2.carbon.apimgt.impl.utils.APIMWSDLReader;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.RegistriesApi;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.RegistriesApiService;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import org.wso2.carbon.apimgt.rest.api.endpoint.registry.dto.RegistryArrayDTO;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.dto.RegistryDTO;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.dto.RegistryEntryArrayDTO;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.dto.RegistryEntryDTO;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.util.EndpointRegistryMappingUtils;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RequestScoped
public class RegistriesApiServiceImpl implements RegistriesApiService {

    private static final Log log = LogFactory.getLog(RegistriesApiServiceImpl.class);
    private static final Log audit = CarbonConstants.AUDIT_LOG;

    @Override
    public Response getAllEntriesInRegistry(String registryId, RegistriesApi.ServiceTypeEnum serviceType,
                                            RegistriesApi.DefinitionTypeEnum definitionType, String name,
                                            RegistriesApi.ServiceCategoryEnum serviceCategory,
                                            RegistriesApi.SortEntryByEnum sortEntryBy,
                                            RegistriesApi.SortEntryOrderEnum sortEntryOrder,
                                            Integer limit, Integer offset, MessageContext messageContext) {
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        String user = RestApiUtil.getLoggedInUsername();
        RegistryEntryArrayDTO registryEntryArray = new RegistryEntryArrayDTO();
        EndpointRegistry registryProvider = new EndpointRegistryImpl(user);
        try {
            EndpointRegistryInfo endpointRegistry =
                    registryProvider.getEndpointRegistryByUUID(registryId, tenantDomain);
            if (endpointRegistry == null) {
                RestApiUtil.handleResourceNotFoundError("Endpoint registry with the id: " + registryId +
                        " is not found", log);
            }

            limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
            offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
            String sortOrder = sortEntryOrder != null ? sortEntryOrder.toString() : RestApiConstants.DEFAULT_SORT_ORDER;
            String sortBy = EndpointRegistryMappingUtils.getRegistryEntriesSortByField(sortEntryBy);
            name = name == null ? StringUtils.EMPTY : name;
            String serviceTypeStr = serviceType == null ? StringUtils.EMPTY : serviceType.toString();
            String definitionTypeStr = definitionType == null ? StringUtils.EMPTY : definitionType.toString();
            String serviceCategoryStr = serviceCategory == null ? StringUtils.EMPTY : serviceCategory.toString();

            List<EndpointRegistryEntry> endpointRegistryEntryList =
                    registryProvider.getEndpointRegistryEntries(sortBy, sortOrder, limit, offset, registryId,
                            serviceTypeStr, definitionTypeStr, name, serviceCategoryStr);
            for (EndpointRegistryEntry endpointRegistryEntry : endpointRegistryEntryList) {
                registryEntryArray.add(EndpointRegistryMappingUtils.fromRegistryEntryToDTO(endpointRegistryEntry));
            }
            return Response.ok().entity(registryEntryArray).build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while retrieving entries of endpoint registry " +
                    "given by id: " + registryId, e, log);
        }
        return null;
    }

    @Override
    public Response getRegistryByUUID(String registryId, MessageContext messageContext) {
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        String user = RestApiUtil.getLoggedInUsername();
        EndpointRegistry registryProvider = new EndpointRegistryImpl(user);
        try {
            EndpointRegistryInfo endpointRegistryInfo =
                    registryProvider.getEndpointRegistryByUUID(registryId, tenantDomain);
            if (endpointRegistryInfo != null) {
                RegistryDTO registryDTO = EndpointRegistryMappingUtils.fromEndpointRegistryToDTO(endpointRegistryInfo);
                return Response.ok().entity(registryDTO).build();
            } else {
                RestApiUtil.handleResourceNotFoundError("Endpoint Registry with the id: " + registryId +
                        " is not found", log);
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while retrieving details of endpoint registry by id: "
                    + registryId, e, log);
        }
        return null;
    }

    @Override
    public Response getRegistries(String query, RegistriesApi.SortRegistryByEnum sortByRegistry, RegistriesApi
            .SortRegistryOrderEnum sortOrder, Integer limit, Integer offset, MessageContext messageContext) {
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        String user = RestApiUtil.getLoggedInUsername();
        EndpointRegistry registryProvider = new EndpointRegistryImpl(user);
        RegistryArrayDTO registryDTOList = new RegistryArrayDTO();

        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        String sortOrderStr = sortOrder != null ? sortOrder.toString() :
                RegistriesApi.SortRegistryOrderEnum.asc.toString();
        String sortBy = sortByRegistry != null ? EndpointRegistryConstants.COLUMN_REG_NAME :
                EndpointRegistryConstants.COLUMN_ID;

        try {
            List<EndpointRegistryInfo> endpointRegistryInfoList =
                    registryProvider.getEndpointRegistries(sortBy, sortOrderStr, limit, offset, tenantDomain);
            for (EndpointRegistryInfo endpointRegistryInfo : endpointRegistryInfoList) {
                registryDTOList.add(EndpointRegistryMappingUtils.fromEndpointRegistryToDTO(endpointRegistryInfo));
            }
            return Response.ok().entity(registryDTOList).build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while retrieving details of endpoint registries", e, log);
        }
        return null;
    }

    @Override
    public Response addRegistry(RegistryDTO body, MessageContext messageContext) {
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        String user = RestApiUtil.getLoggedInUsername();
        EndpointRegistryInfo registry = EndpointRegistryMappingUtils.fromDTOtoEndpointRegistry(body, user);
        try {
            EndpointRegistry registryProvider = new EndpointRegistryImpl(user);
            String registryId = registryProvider.addEndpointRegistry(registry);
            EndpointRegistryInfo createdRegistry = registryProvider.getEndpointRegistryByUUID(registryId, tenantDomain);

            audit.info("Successfully created endpoint registry " + createdRegistry.getName() + " with id :"
                    + createdRegistry.getUuid() + " by :" + user);
            return Response.ok()
                    .entity(EndpointRegistryMappingUtils.fromEndpointRegistryToDTO(createdRegistry))
                    .build();
        } catch (APIMgtResourceAlreadyExistsException e) {
            RestApiUtil.handleResourceAlreadyExistsError("Endpoint Registry with name '" + body.getName()
                    + "' already exists", e, log);
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while adding new endpoint registry: "
                    + registry.getName(), e, log);
        }
        return null;
    }

    @Override
    public Response createRegistryEntry(String registryId, RegistryEntryDTO registryEntry, InputStream
            definitionFileInputStream, Attachment definitionFileDetail, MessageContext messageContext) {
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        String user = RestApiUtil.getLoggedInUsername();
        EndpointRegistry registryProvider = new EndpointRegistryImpl(user);
        try {
            EndpointRegistryInfo endpointRegistry =
                    registryProvider.getEndpointRegistryByUUID(registryId, tenantDomain);
            if (endpointRegistry == null) {
                RestApiUtil.handleResourceNotFoundError("Endpoint registry with the id: " + registryId +
                        " is not found", log);
            }
            InputStream definitionFile = null;
            if ((definitionFileInputStream != null || registryEntry.getDefinitionUrl() != null) &&
                    registryEntry.getDefinitionType() == null) {
                RestApiUtil.handleBadRequest("Missing definitionType parameter", log);
            }
            if (definitionFileInputStream != null) {
                // Retrieve definition from the file
                byte[] definitionFileByteArray = getDefinitionFromInput(definitionFileInputStream);
                if (!isValidEndpointDefinition(null, definitionFileByteArray,
                        registryEntry.getDefinitionType().toString())) {
                    RestApiUtil.handleBadRequest("Error while validating the endpoint definition of " +
                            "the new registry entry with registry id: " + registryId, log);
                } else {
                    definitionFileByteArray = transformDefinitionContent(definitionFileByteArray,
                            registryEntry.getDefinitionType());
                    definitionFile = new ByteArrayInputStream(definitionFileByteArray);
                }
            } else if (registryEntry.getDefinitionUrl() != null) {
                // Retrieve the endpoint definition from URL
                try {
                    URL definitionURL = new URL(registryEntry.getDefinitionUrl());
                    if (!isValidEndpointDefinition(definitionURL, null,
                            registryEntry.getDefinitionType().toString())) {
                        RestApiUtil.handleBadRequest("Error while validating the endpoint URL definition of " +
                                "the new registry entry with registry id: " + registryId, log);
                    }
                } catch (MalformedURLException e) {
                    RestApiUtil.handleBadRequest("The definition url provided is invalid for the new " +
                            "Endpoint Registry Entry with Registry Id: " + registryId, e, log);
                } catch (IOException e) {
                    RestApiUtil.handleInternalServerError("Error while reaching the " +
                            "definition url: " + registryEntry.getDefinitionUrl() + " given for the new " +
                            "Endpoint Registry with Registry Id: " + registryId, e, log);
                }
            }
            EndpointRegistryEntry entryToAdd = EndpointRegistryMappingUtils.fromDTOToRegistryEntry(registryEntry,
                    null, definitionFile, endpointRegistry.getRegistryId());
            String entryId = registryProvider.addEndpointRegistryEntry(entryToAdd);
            EndpointRegistryEntry createdEntry = registryProvider.getEndpointRegistryEntryByUUID(registryId, entryId);
            audit.info("Successfully created endpoint registry entry with id :" + createdEntry.getEntryId() +
                    " in :" + registryId + " by:" + user);
            return Response.ok().entity(EndpointRegistryMappingUtils.fromRegistryEntryToDTO(createdEntry)).build();
        } catch (APIMgtResourceAlreadyExistsException e) {
            RestApiUtil.handleResourceAlreadyExistsError("Endpoint Registry Entry with name '"
                    + registryEntry.getEntryName() + "' already exists", e, log);
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while adding new entry for the endpoint registry " +
                    "with id: " + registryId, e, log);
        } catch (IOException e) {
            RestApiUtil.handleInternalServerError("Error in reading endpoint definition file content", e, log);
        }
        return null;
    }

    @Override
    public Response updateRegistry(String registryId, RegistryDTO body, MessageContext messageContext) {

        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        String user = RestApiUtil.getLoggedInUsername();
        EndpointRegistry registryProvider = new EndpointRegistryImpl(user);
        EndpointRegistryInfo registryToUpdate = EndpointRegistryMappingUtils.fromDTOtoEndpointRegistry(body, user);
        EndpointRegistryInfo endpointRegistry = null;
        try {
            endpointRegistry =
                    registryProvider.getEndpointRegistryByUUID(registryId, tenantDomain);
            if (endpointRegistry == null) {
                RestApiUtil.handleResourceNotFoundError("Endpoint registry with the id: " + registryId +
                        " is not found", log);
            }
            registryProvider.updateEndpointRegistry(registryId, endpointRegistry.getName(), registryToUpdate);
            EndpointRegistryInfo updatedEndpointRegistry
                    = registryProvider.getEndpointRegistryByUUID(registryId, tenantDomain);
            audit.info("Successfully updated endpoint registry of id :" + updatedEndpointRegistry.getUuid()
                    + " by :" + user);
            return Response.ok()
                    .entity(EndpointRegistryMappingUtils.fromEndpointRegistryToDTO(updatedEndpointRegistry)).build();
        } catch (APIMgtResourceAlreadyExistsException e) {
            RestApiUtil.handleResourceAlreadyExistsError("Endpoint Registry with name '"
                    + endpointRegistry.getName() + "' already exists", e, log);
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while updating the endpoint registry " +
                    "with id: " + registryId, e, log);
        }
        return null;
    }

    @Override
    public Response deleteRegistry(String registryId, MessageContext messageContext) {
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        String user = RestApiUtil.getLoggedInUsername();
        EndpointRegistry registryProvider = new EndpointRegistryImpl(user);
        try {
            EndpointRegistryInfo endpointRegistry =
                    registryProvider.getEndpointRegistryByUUID(registryId, tenantDomain);
            if (endpointRegistry == null) {
                RestApiUtil.handleResourceNotFoundError("Endpoint registry with the id: " + registryId +
                        " is not found", log);
            }
            registryProvider.deleteEndpointRegistry(registryId);
            audit.info("Successfully deleted endpoint registry of id :" + registryId + " by :" + user);
            return Response.ok().entity("Successfully deleted the endpoint registry").build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while deleting the endpoint registry " +
                    "with id: " + registryId, e, log);
        }
        return null;
    }

    @Override
    public Response getRegistryEntryByUuid(String registryId, String entryId, MessageContext messageContext) {
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        String user = RestApiUtil.getLoggedInUsername();
        EndpointRegistry registryProvider = new EndpointRegistryImpl(user);
        try {
            EndpointRegistryInfo endpointRegistry =
                    registryProvider.getEndpointRegistryByUUID(registryId, tenantDomain);
            if (endpointRegistry == null) {
                RestApiUtil.handleResourceNotFoundError("Endpoint registry with the id: " + registryId +
                        " is not found", log);
            }
            EndpointRegistryEntry endpointRegistryEntry
                    = registryProvider.getEndpointRegistryEntryByUUID(registryId, entryId);
            if (endpointRegistryEntry == null) {
                RestApiUtil.handleResourceNotFoundError("Endpoint registry entry with the id: " + entryId +
                        " is not found", log);
            } else {
                return Response.ok().entity(EndpointRegistryMappingUtils.fromRegistryEntryToDTO(endpointRegistryEntry))
                        .build();
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while fetching endpoint registry entry: "
                    + entryId, e, log);
        }
        return null;
    }

    @Override
    public Response updateRegistryEntry(String registryId, String entryId, RegistryEntryDTO registryEntry, InputStream
            definitionFileInputStream, Attachment definitionFileDetail, MessageContext messageContext) {
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        String user = RestApiUtil.getLoggedInUsername();
        EndpointRegistry registryProvider = new EndpointRegistryImpl(user);
        InputStream definitionFile = null;

        try {
            EndpointRegistryInfo endpointRegistry =
                    registryProvider.getEndpointRegistryByUUID(registryId, tenantDomain);
            if (endpointRegistry == null) {
                RestApiUtil.handleResourceNotFoundError("Endpoint registry with the id: " + registryId +
                        " is not found", log);
            }
            EndpointRegistryEntry endpointRegistryEntry =
                    registryProvider.getEndpointRegistryEntryByUUID(registryId, entryId);
            if (endpointRegistryEntry == null) {
                RestApiUtil.handleResourceNotFoundError("Endpoint registry entry with the id: " + entryId +
                        " is not found", log);
            }
            if ((definitionFileInputStream != null || registryEntry.getDefinitionUrl() != null)
                    && registryEntry.getDefinitionType() == null) {
                RestApiUtil.handleBadRequest("Missing definitionType of the registry " +
                        "entry with id: " + entryId, log);
            }
            if (definitionFileInputStream != null) {
                // Retrieve definition from the file
                byte[] definitionFileByteArray = getDefinitionFromInput(definitionFileInputStream);
                if (!isValidEndpointDefinition(null, definitionFileByteArray,
                        registryEntry.getDefinitionType().toString())) {
                    RestApiUtil.handleBadRequest("Error while validating the endpoint definition of " +
                            "the registry entry with id: " + entryId, log);
                }
                definitionFileByteArray = transformDefinitionContent(definitionFileByteArray,
                        registryEntry.getDefinitionType());
                definitionFile = new ByteArrayInputStream(definitionFileByteArray);
            } else if (registryEntry.getDefinitionUrl() != null) {
                // Retrieve the endpoint definition from URL
                try {
                    URL definitionURL = new URL(registryEntry.getDefinitionUrl());
                    if (!isValidEndpointDefinition(definitionURL, null,
                            registryEntry.getDefinitionType().toString())) {
                        RestApiUtil.handleBadRequest("Error while validating the endpoint URL definition of " +
                                "the registry entry with id: " + entryId, log);
                    }
                } catch (MalformedURLException e) {
                    RestApiUtil.handleBadRequest("The definition url provided is invalid for the endpoint " +
                            "registry entry with id: " + entryId, e, log);
                } catch (IOException e) {
                    RestApiUtil.handleInternalServerError("Error while reaching the definition url: "
                            + registryEntry.getDefinitionUrl() + " given for the Endpoint Registry Entry with Id: "
                            + entryId, e, log);
                }
            }
            EndpointRegistryEntry entryToUpdate = EndpointRegistryMappingUtils.fromDTOToRegistryEntry(registryEntry,
                    entryId, definitionFile, endpointRegistry.getRegistryId());
            registryProvider.updateEndpointRegistryEntry(endpointRegistryEntry.getName(), entryToUpdate);

            EndpointRegistryEntry updatedEntry = registryProvider.getEndpointRegistryEntryByUUID(registryId, entryId);
            audit.info("Successfully updated endpoint registry entry with id :" + entryId +
                    " in :" + registryId + " by:" + user);
            return Response.ok().entity(EndpointRegistryMappingUtils.fromRegistryEntryToDTO(updatedEntry)).build();
        } catch (APIMgtResourceAlreadyExistsException e) {
            RestApiUtil.handleResourceAlreadyExistsError("Endpoint Registry Entry with name '"
                    + registryEntry.getEntryName() + "' already exists", e, log);
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while updating the endpoint registry entry " +
                    "with id: " + entryId, e, log);
        } catch (IOException e) {
            RestApiUtil.handleInternalServerError("Error in reading endpoint definition file content", e, log);
        }
        return null;
    }

    @Override
    public Response deleteRegistryEntry(String registryId, String entryId, MessageContext messageContext) {
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        String user = RestApiUtil.getLoggedInUsername();
        EndpointRegistry registryProvider = new EndpointRegistryImpl(user);
        try {
            EndpointRegistryInfo endpointRegistry =
                    registryProvider.getEndpointRegistryByUUID(registryId, tenantDomain);
            if (endpointRegistry == null) {
                RestApiUtil.handleResourceNotFoundError("Endpoint registry with the id: " + registryId +
                        " is not found", log);
            }
            EndpointRegistryEntry endpointRegistryEntry = registryProvider.getEndpointRegistryEntryByUUID(registryId, entryId);
            if (endpointRegistryEntry == null) {
                RestApiUtil.handleResourceNotFoundError("Endpoint registry entry with the id: " + entryId +
                        " is not found", log);
            }
            registryProvider.deleteEndpointRegistryEntry(entryId);
            audit.info("Successfully deleted endpoint registry entry with id :" + entryId +
                    " in :" + registryId + " by:" + user);
            return Response.ok().entity("Successfully deleted the endpoint registry entry").build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while deleting the endpoint registry entry " +
                    "with id: " + registryId, e, log);
        }
        return null;
    }

    private boolean isValidEndpointDefinition(URL definitionURL, byte[] definitionFileByteArray,
                                              String definitionType) {
        String definitionContent = null;
        try {
            // definition file is given priority than the definition url
            if (definitionFileByteArray != null) {
                definitionContent = new String(definitionFileByteArray);
            } else if (definitionURL != null) {
                definitionContent = IOUtils.toString(definitionURL.openStream());
            }
        } catch (IOException e) {
            log.error("Error in reading endpoint definition content", e);
            return false;
        }
        if (StringUtils.isEmpty(definitionContent)) {
            log.error("No endpoint definition content found");
            return false;
        }

        boolean isValid = false;

        if (RegistryEntryDTO.DefinitionTypeEnum.OAS.toString().equals(definitionType)) {
            // Validate OpenAPI definitions
            try {
                APIDefinitionValidationResponse response =
                        OASParserUtil.validateAPIDefinition(definitionContent, false);
                isValid = response.isValid();
            } catch (APIManagementException | ClassCastException e) {
                log.error("Unable to parse the OpenAPI endpoint definition", e);
            }
        } else if (RegistryEntryDTO.DefinitionTypeEnum.WSDL1.toString().equals(definitionType) ||
                RegistryEntryDTO.DefinitionTypeEnum.WSDL2.toString().equals(definitionType)) {
            // Validate WSDL1 and WSDL2 definitions
            try {
                if (definitionFileByteArray != null) {
                    ByteArrayInputStream definitionFileByteStream = new ByteArrayInputStream(definitionFileByteArray);
                    isValid = APIMWSDLReader.validateWSDLFile(definitionFileByteStream).isValid();
                } else {
                    isValid = APIMWSDLReader.validateWSDLUrl(definitionURL).isValid();
                }
            } catch (APIManagementException e) {
                log.error("Unable to parse the WSDL endpoint definition", e);
            }
        } else if (RegistryEntryDTO.DefinitionTypeEnum.GQL_SDL.toString().equals(definitionType)) {
            // Validate GraphQL definitions
            try {
                SchemaParser schemaParser = new SchemaParser();
                TypeDefinitionRegistry typeRegistry = schemaParser.parse(definitionContent);
                GraphQLSchema graphQLSchema = UnExecutableSchemaGenerator.makeUnExecutableSchema(typeRegistry);
                SchemaValidator schemaValidation = new SchemaValidator();
                Set<SchemaValidationError> validationErrors = schemaValidation.validateSchema(graphQLSchema);
                isValid = (validationErrors.toArray().length == 0);
            } catch (SchemaProblem e) {
                log.error("Unable to parse the GraphQL endpoint definition", e);
            }
        }
        return isValid;
    }

    @Override
    public Response getEndpointDefinition(String registryId, String entryId, MessageContext messageContext) {
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        String user = RestApiUtil.getLoggedInUsername();
        String contentType = StringUtils.EMPTY;
        EndpointRegistry registryProvider = new EndpointRegistryImpl(user);
        try {
            EndpointRegistryInfo endpointRegistry = registryProvider.getEndpointRegistryByUUID(registryId, tenantDomain);
            if (endpointRegistry == null) {
                RestApiUtil.handleResourceNotFoundError("Endpoint registry with the id: " + registryId +
                        " is not found", log);
            }
            EndpointRegistryEntry registryEntry = registryProvider.getEndpointRegistryEntryByUUID(registryId, entryId);
            if (registryEntry == null) {
                RestApiUtil.handleResourceNotFoundError("Endpoint registry entry with the id: " + entryId +
                        " is not found", log);
            }
            String type = registryEntry.getDefinitionType();
            if (RegistryEntryDTO.DefinitionTypeEnum.OAS.equals(RegistryEntryDTO.DefinitionTypeEnum.fromValue(type))
                    || RegistryEntryDTO.DefinitionTypeEnum.GQL_SDL.equals(RegistryEntryDTO.DefinitionTypeEnum
                    .fromValue(type))) {
                contentType = MediaType.APPLICATION_JSON;
            } else if (RegistryEntryDTO.DefinitionTypeEnum.WSDL1.equals(RegistryEntryDTO.DefinitionTypeEnum
                    .fromValue(type)) || RegistryEntryDTO.DefinitionTypeEnum.WSDL2.equals(RegistryEntryDTO
                    .DefinitionTypeEnum.fromValue(type))) {
                contentType = MediaType.TEXT_XML;
            }
            InputStream endpointDefinition = registryEntry.getEndpointDefinition();
            if (endpointDefinition == null) {
                RestApiUtil.handleResourceNotFoundError("Endpoint definition not found for entry with ID: "
                        + entryId, log);
            } else {
                return Response.ok(endpointDefinition).type(contentType).build();
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while retrieving the endpoint definition of registry " +
                    "entry with id: " + entryId, e, log);
        }
        return null;
    }

    private byte[] getDefinitionFromInput(InputStream definitionFileInputStream) throws IOException {

        ByteArrayOutputStream definitionFileOutputByteStream = new ByteArrayOutputStream();
        IOUtils.copy(definitionFileInputStream, definitionFileOutputByteStream);
        return definitionFileOutputByteStream.toByteArray();
    }

    private byte[] transformDefinitionContent(byte[] definitionFileByteArray,
                                              RegistryEntryDTO.DefinitionTypeEnum type) throws IOException {

        if (RegistryEntryDTO.DefinitionTypeEnum.OAS.equals(type)) {
            String oasContent = new String(definitionFileByteArray);
            if (!oasContent.trim().startsWith("{")) {
                String jsonContent = CommonUtil.yamlToJson(oasContent);
                return jsonContent.getBytes(StandardCharsets.UTF_8);
            }
        }
        return definitionFileByteArray;
    }

}
