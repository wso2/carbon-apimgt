/*
 * Copyright (c) 2024 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.LLMProvider;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.admin.v1.LlmProvidersApiService;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.LLMProviderRequestDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.LLMProviderResponseDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.LLMProviderSummaryResponseListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings.LLMProviderMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.InputStream;
import javax.ws.rs.core.Response;

public class LlmProvidersApiServiceImpl implements LlmProvidersApiService {

    private static final Log log = LogFactory.getLog(LlmProvidersApiServiceImpl.class);

    /**
     * Adds a new LLMProvider to the system and returns a response indicating the result.
     *
     * @param name                     The name of the LLMProvider.
     * @param apiVersion               The API version of the LLMProvider.
     * @param description              The description of the LLMProvider.
     * @param configurations           The configurations for the LLMProvider.
     * @param apiDefinitionInputStream InputStream containing the API definition.
     * @param apiDefinitionDetail      Attachment with additional details for the API definition.
     * @param modelList                Comma separated list of models associated with the LLMProvider.
     * @param messageContext           The MessageContext for the request.
     * @return Response indicating success or failure of the LLMProvider creation.
     * @throws APIManagementException If an error occurs while adding the LLMProvider.
     */
    @Override
    public Response addLLMProvider(String name, String apiVersion, String description,
                                   String configurations, InputStream apiDefinitionInputStream,
                                   Attachment apiDefinitionDetail, String modelList, MessageContext messageContext)
            throws APIManagementException {

        try {
            List<String> vendorModelList = null;
            // convert the 'modelList' json into a list
            if (StringUtils.isNotEmpty(modelList)) {
                ObjectMapper objectMapper = new ObjectMapper();
                vendorModelList = objectMapper.readValue(modelList, new TypeReference<List<String>>() {});
            }

            LLMProvider provider = buildLLMProvider(name, apiVersion, description, configurations,
                    apiDefinitionInputStream, vendorModelList);
            if (provider == null) {
                log.warn("Invalid provider configurations");
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIAdmin apiAdmin = new APIAdminImpl();
            LLMProvider addedLLMProvider = apiAdmin.addLLMProvider(organization, provider);
            if (addedLLMProvider != null) {
                LLMProviderResponseDTO llmProviderResponseDTO =
                        LLMProviderMappingUtil.fromProviderToProviderResponseDTO(addedLLMProvider);
                URI location = new URI(RestApiConstants.RESOURCE_PATH_LLM_PROVIDER + "/"
                        + addedLLMProvider.getId());
                String info = "{'id':'" + addedLLMProvider.getId() + "'}";
                APIUtil.logAuditMessage(
                        org.wso2.carbon.apimgt.api.APIConstants.AIAPIConstants.LLM_PROVIDER,
                        info,
                        APIConstants.AuditLogConstants.UPDATED,
                        RestApiCommonUtil.getLoggedInUsername()
                );
                return Response.created(location).entity(llmProviderResponseDTO).build();
            } else {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
        } catch (IOException e) {
            log.warn("Error occurred trying to read api definition file");
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (URISyntaxException e) {
            log.warn("Error while creating URI for new LLM Provider");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Builds and returns a new LLMProvider object with the given details.
     *
     * @param name                     The name of the LLMProvider.
     * @param apiVersion               The API version of the LLMProvider.
     * @param description              The description of the LLMProvider.
     * @param configurations           The configurations for the LLMProvider.
     * @param apiDefinitionInputStream InputStream containing the API definition, if available.
     * @param modelList                The list of models associated with the LLMProvider.
     * @return A new LLMProvider object populated with the provided data.
     * @throws IOException If an error occurs while reading the API definition from the InputStream.
     */
    private LLMProvider buildLLMProvider(String name, String apiVersion, String description, String configurations,
            InputStream apiDefinitionInputStream, List<String> modelList)
            throws IOException {

        String apiDefinition = getApiDefinitionFromStream(apiDefinitionInputStream);
        if (apiDefinition == null) {
            return null;
        }
        LLMProvider provider = new LLMProvider();
        provider.setName(name);
        provider.setApiVersion(apiVersion);
        provider.setDescription(description);
        provider.setBuiltInSupport(false);
        provider.setConfigurations(configurations);
        provider.setApiDefinition(apiDefinition);
        provider.setModelList(modelList);

        return provider;
    }

    /**
     * Deletes a LLM provider by its ID.
     *
     * @param llmProviderId  The ID of the LLM provider to be deleted.
     * @param messageContext The message context containing necessary information for the operation.
     * @return A Response object indicating the result of the delete operation.
     * @throws APIManagementException If an error occurs while deleting the LLM provider.
     */
    @Override
    public Response deleteLLMProvider(String llmProviderId, MessageContext messageContext)
            throws APIManagementException {

        APIAdmin apiAdmin = new APIAdminImpl();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        LLMProvider retrievedProvider = apiAdmin.getLLMProvider(organization, llmProviderId);
        if (retrievedProvider == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        String deletedLLMProviderId = apiAdmin
                .deleteLLMProvider(organization, retrievedProvider, false);
        if (deletedLLMProviderId != null) {
            String info = String.format("{\"id\":\"%s\"}", llmProviderId);
            APIUtil.logAuditMessage(
                    org.wso2.carbon.apimgt.api.APIConstants.AIAPIConstants.LLM_PROVIDER,
                    info,
                    APIConstants.AuditLogConstants.DELETED,
                    RestApiCommonUtil.getLoggedInUsername()
            );
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
    }

    /**
     * Retrieves a list of all LLM providers in the organization.
     *
     * @param messageContext The message context containing necessary information for the operation.
     * @return A Response object containing the list of LLM providers.
     * @throws APIManagementException If an error occurs while retrieving the LLM providers.
     */
    @Override
    public Response getLLMProviders(MessageContext messageContext) throws APIManagementException {

        APIAdmin apiAdmin = new APIAdminImpl();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        List<LLMProvider> LLMProviderList = apiAdmin.getLLMProviders(
                organization, null, null, null);
        LLMProviderSummaryResponseListDTO providerListDTO =
                LLMProviderMappingUtil.fromProviderSummaryListToProviderSummaryListDTO(LLMProviderList);
        return Response.ok().entity(providerListDTO).build();
    }

    /**
     * Updates an existing LLM Provider.
     *
     * @param llmProviderId            The ID of the LLM Provider to update.
     * @param name                     The name of the provider (unused in logic).
     * @param apiVersion               The API version of the provider (unused in logic).
     * @param description              The description of the provider.
     * @param configurations           The configurations of the provider.
     * @param apiDefinitionInputStream The InputStream for the API definition.
     * @param apiDefinitionDetail      The attachment containing API definition details.
     * @param modelList                Comma separated list of models associated with the LLMProvider.
     * @param messageContext           The message context for the request.
     * @return The response with the updated LLM Provider or an error message.
     * @throws APIManagementException If an error occurs while updating the provider.
     */
    @Override
    public Response updateLLMProvider(String llmProviderId, String name, String apiVersion,
                                      String description, String configurations, InputStream apiDefinitionInputStream,
                                      Attachment apiDefinitionDetail, String modelList, MessageContext messageContext)
            throws APIManagementException {

        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIAdmin apiAdmin = new APIAdminImpl();
            LLMProvider retrievedProvider = apiAdmin.getLLMProvider(organization, llmProviderId);

            // convert the 'modelList' json into a list
            List<String> vendorModelList = null;
            if (StringUtils.isNotEmpty(modelList)) {
                ObjectMapper objectMapper = new ObjectMapper();
                vendorModelList = objectMapper.readValue(modelList, new TypeReference<List<String>>() {});
            }

            LLMProvider provider = buildUpdatedLLMProvider(retrievedProvider, llmProviderId, description,
                    configurations, apiDefinitionInputStream, vendorModelList);

            if (provider == null) {
                log.warn("Invalid provider configurations");
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            LLMProvider updatedProvider = apiAdmin.updateLLMProvider(organization, provider);

            if (updatedProvider != null) {
                LLMProviderResponseDTO llmProviderResponseDTO =
                        LLMProviderMappingUtil.fromProviderToProviderResponseDTO(updatedProvider);
                URI location = new URI(
                        RestApiConstants.RESOURCE_PATH_LLM_PROVIDER + "/" + updatedProvider.getId());
                String info = "{'id':'" + llmProviderId + "'}";
                APIUtil.logAuditMessage(
                        org.wso2.carbon.apimgt.api.APIConstants.AIAPIConstants.LLM_PROVIDER,
                        info,
                        APIConstants.AuditLogConstants.UPDATED,
                        RestApiCommonUtil.getLoggedInUsername()
                );
                return Response.ok(location).entity(llmProviderResponseDTO).build();
            }
            return Response.status(Response.Status.NO_CONTENT).build();

        } catch (IOException e) {
            log.warn("Error occurred while reading the API definition file", e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (URISyntaxException e) {
            log.warn("Error occurred while creating URI for new LLM Provider", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Builds and returns an updated LLMProvider based on the retrieved provider details.
     *
     * @param retrievedProvider        The existing LLMProvider to update.
     * @param llmProviderId            The ID of the LLMProvider.
     * @param description              The new description to update, if provided.
     * @param configurations           The new configurations to update, if provided.
     * @param apiDefinitionInputStream InputStream containing the updated API definition, if any.
     * @param modelList                The list of models associated with the LLMProvider.
     * @return An updated LLMProvider object with the modified or retained properties.
     * @throws IOException If an error occurs while reading the API definition from the InputStream.
     */
    private LLMProvider buildUpdatedLLMProvider(LLMProvider retrievedProvider, String llmProviderId,
                                                String description, String configurations,
                                                InputStream apiDefinitionInputStream, List<String> modelList)
            throws IOException {

        LLMProvider provider = new LLMProvider();
        provider.setId(llmProviderId);
        provider.setName(retrievedProvider.getName());
        provider.setApiVersion(retrievedProvider.getApiVersion());
        provider.setBuiltInSupport(retrievedProvider.isBuiltInSupport());
        String apiDefinition = getApiDefinitionFromStream(apiDefinitionInputStream);
        boolean isBuiltIn = retrievedProvider.isBuiltInSupport();

        if (isBuiltIn && apiDefinition == null) {
            return null;
        }

        provider.setApiDefinition(apiDefinition != null ? apiDefinition : retrievedProvider.getApiDefinition());
        provider.setDescription(isBuiltIn ? retrievedProvider.getDescription() :
                (description != null ? description : retrievedProvider.getDescription()));
        provider.setConfigurations(isBuiltIn ? retrievedProvider.getConfigurations() :
                (configurations != null ? configurations : retrievedProvider.getConfigurations()));

        provider.setModelList(modelList != null ? modelList : new ArrayList<>());

        return provider;
    }

    /**
     * Reads the API definition from an InputStream and returns it as a String.
     *
     * @param apiDefinitionInputStream InputStream containing the API definition.
     * @return A non-empty API definition String if valid; null otherwise.
     * @throws IOException If an error occurs while reading from the InputStream.
     */
    private String getApiDefinitionFromStream(InputStream apiDefinitionInputStream) throws IOException {

        if (apiDefinitionInputStream == null) {
            return null;
        }
        String apiDefinition = IOUtils.toString(apiDefinitionInputStream, StandardCharsets.UTF_8);
        return StringUtils.isNotEmpty(apiDefinition) &&
                !StringUtils.equals(org.wso2.carbon.apimgt.api.APIConstants.AIAPIConstants.NULL, apiDefinition)
                ? apiDefinition : null;
    }

    /**
     * Retrieves a specific LLM provider by its ID.
     *
     * @param llmProviderId  The ID of the LLM provider to be retrieved.
     * @param messageContext The message context containing necessary information for the operation.
     * @return A Response object containing the LLM provider details.
     * @throws APIManagementException If an error occurs while retrieving the LLM provider.
     */
    @Override
    public Response getLLMProvider(String llmProviderId, MessageContext messageContext)
            throws APIManagementException {

        APIAdmin apiAdmin = new APIAdminImpl();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        LLMProviderResponseDTO result =
                LLMProviderMappingUtil.fromProviderToProviderResponseDTO(apiAdmin.getLLMProvider(organization,
                        llmProviderId));
        return Response.ok().entity(result).build();
    }
}
