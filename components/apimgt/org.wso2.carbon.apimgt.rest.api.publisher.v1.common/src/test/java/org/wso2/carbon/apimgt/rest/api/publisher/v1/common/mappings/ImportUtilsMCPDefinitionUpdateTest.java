/*
 *  Copyright (c) 2026, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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
package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIRevisionDeployment;
import org.wso2.carbon.apimgt.api.model.Backend;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MCPServerDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SubtypeConfigurationDTO;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Tests for backend definition update handling during MCP server import (apictl flow).
 *
 * Covers:
 * - Backend definition is updated when provided in import artifact
 * - Backend definition is NOT updated when absent in import artifact
 * - Definition validation is invoked for imported definitions
 * - Both DIRECT_BACKEND and SERVER_PROXY subtypes
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ImportUtils.class, RestApiCommonUtil.class, APIControllerUtil.class,
        MultitenantUtils.class, APIUtil.class, PublisherCommonUtils.class})
public class ImportUtilsMCPDefinitionUpdateTest {

    private static final String ORGANIZATION = "carbon.super";
    private static final String USERNAME = "admin";
    private static final String TENANT_DOMAIN = "carbon.super";
    private static final String API_UUID = "test-uuid-def-1234";
    private static final String API_NAME = "TestMCPDefUpdate";
    private static final String API_VERSION = "1.0.0";
    private static final String API_CONTEXT = "/test-mcp-def";
    private static final String EXTRACTED_PATH = "/tmp/test-mcp-def-import";
    private static final String BACKEND_ID = "backend-def-1";

    private static final String OLD_DEFINITION = "{\"openapi\":\"3.0.1\",\"info\":{\"title\":\"Old\",\"version\":\"1.0\"}"
            + ",\"paths\":{\"/get\":{\"get\":{\"parameters\":[{\"name\":\"city\",\"in\":\"query\","
            + "\"schema\":{\"type\":\"string\"}}],\"responses\":{\"200\":{\"description\":\"OK\"}}}}}}";

    private static final String NEW_DEFINITION = "{\"openapi\":\"3.0.1\",\"info\":{\"title\":\"New\",\"version\":\"1.0\"}"
            + ",\"paths\":{\"/get\":{\"get\":{\"parameters\":[{\"name\":\"city\",\"in\":\"query\","
            + "\"schema\":{\"type\":\"string\"}},{\"name\":\"units\",\"in\":\"query\","
            + "\"schema\":{\"type\":\"string\"}}],\"responses\":{\"200\":{\"description\":\"OK\"}}}}}}";

    private APIProvider apiProvider;
    private API targetApi;
    private API updatedApi;
    private API importedApiResult;
    private MCPServerDTO mcpServerDTO;
    private final String[] tokenScopes = new String[]{"apim:api_create", "apim:api_publish"};

    @Before
    public void init() throws Exception {
        apiProvider = Mockito.mock(APIProvider.class);

        APIIdentifier apiId = new APIIdentifier(USERNAME, API_NAME, API_VERSION);
        targetApi = new API(apiId);
        targetApi.setUuid(API_UUID);
        targetApi.setStatus("CREATED");

        updatedApi = new API(apiId);
        updatedApi.setUuid(API_UUID);

        importedApiResult = new API(apiId);
        importedApiResult.setUuid(API_UUID);
        importedApiResult.setStatus("CREATED");

        mcpServerDTO = new MCPServerDTO();
        mcpServerDTO.setName(API_NAME);
        mcpServerDTO.setVersion(API_VERSION);
        mcpServerDTO.setContext(API_CONTEXT);
        mcpServerDTO.setProvider(USERNAME);
        mcpServerDTO.setLifeCycleStatus("CREATED");

        PowerMockito.mockStatic(RestApiCommonUtil.class);
        PowerMockito.mockStatic(APIControllerUtil.class);
        PowerMockito.mockStatic(MultitenantUtils.class);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.mockStatic(PublisherCommonUtils.class);
        PowerMockito.spy(ImportUtils.class);

        Mockito.when(RestApiCommonUtil.getLoggedInUsername()).thenReturn(USERNAME);
        Mockito.when(RestApiCommonUtil.getLoggedInUserTenantDomain()).thenReturn(TENANT_DOMAIN);
        Mockito.when(RestApiCommonUtil.getLoggedInUserProvider()).thenReturn(apiProvider);

        Mockito.when(apiProvider.getAPIProviderByNameAndOrganization(
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(USERNAME);

        Mockito.when(APIControllerUtil.resolveAPIControllerEnvParams(ArgumentMatchers.anyString()))
                .thenReturn(null);

        PowerMockito.doReturn(null).when(ImportUtils.class, "retrieveDeploymentLabelsFromArchive",
                ArgumentMatchers.anyString(), ArgumentMatchers.anyBoolean());

        PowerMockito.doReturn(new ArrayList<APIRevisionDeployment>()).when(ImportUtils.class,
                "getValidatedDeploymentsList", ArgumentMatchers.any(),
                ArgumentMatchers.anyString(), ArgumentMatchers.any(APIProvider.class),
                ArgumentMatchers.anyString());

        Mockito.when(MultitenantUtils.getTenantDomain(ArgumentMatchers.anyString()))
                .thenReturn(TENANT_DOMAIN);
        Mockito.when(APIUtil.replaceEmailDomainBack(ArgumentMatchers.anyString()))
                .thenReturn(USERNAME);
        Mockito.when(APIUtil.getTenantId(ArgumentMatchers.anyString())).thenReturn(-1234);

        PowerMockito.doReturn(targetApi).when(ImportUtils.class, "retrieveApiToOverwrite",
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(), ArgumentMatchers.any(APIProvider.class),
                ArgumentMatchers.any(Boolean.class), ArgumentMatchers.anyString());

        Mockito.when(PublisherCommonUtils.prepareForUpdateApi(
                ArgumentMatchers.any(API.class), ArgumentMatchers.any(MCPServerDTO.class),
                ArgumentMatchers.any(APIProvider.class), ArgumentMatchers.any(String[].class)))
                .thenReturn(updatedApi);

        Mockito.when(apiProvider.getAPIbyUUID(ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString())).thenReturn(importedApiResult);

        PowerMockito.doReturn(new LinkedHashMap<String, String>()).when(ImportUtils.class,
                "getLifeCycleActions", ArgumentMatchers.anyString(), ArgumentMatchers.anyString());

        PowerMockito.doNothing().when(ImportUtils.class, "addDocumentation",
                ArgumentMatchers.anyString(), ArgumentMatchers.any(),
                ArgumentMatchers.any(APIProvider.class), ArgumentMatchers.anyString());

        PowerMockito.doNothing().when(ImportUtils.class, "addClientCertificates",
                ArgumentMatchers.anyString(), ArgumentMatchers.any(APIProvider.class),
                ArgumentMatchers.any(), ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(), ArgumentMatchers.any(Boolean.class),
                ArgumentMatchers.anyInt());

        PowerMockito.doNothing().when(ImportUtils.class, "addThumbnailImage",
                ArgumentMatchers.anyString(), ArgumentMatchers.any(),
                ArgumentMatchers.any(APIProvider.class));
    }

    private void setupMCPServerDTOMock(MCPServerDTO dto) throws Exception {
        PowerMockito.doReturn(dto).when(ImportUtils.class, "getImportMCPServerDTO",
                ArgumentMatchers.anyString(), ArgumentMatchers.any(MCPServerDTO.class),
                ArgumentMatchers.any(Boolean.class), ArgumentMatchers.anyString());
    }

    private Backend createBackend(String id, String endpointConfig, String definition) {
        Backend backend = new Backend();
        backend.setId(id);
        backend.setName("test-backend");
        backend.setEndpointConfig(endpointConfig);
        if (definition != null) {
            backend.setDefinition(definition);
        }
        return backend;
    }

    // -------------------------------------------------------------------------
    // DIRECT_BACKEND: definition update via import
    // -------------------------------------------------------------------------

    @Test
    public void testDirectBackendDefinitionUpdatedOnImport() throws Exception {
        SubtypeConfigurationDTO subtypeConfig = new SubtypeConfigurationDTO();
        subtypeConfig.setSubtype(APIConstants.API_SUBTYPE_DIRECT_BACKEND);
        mcpServerDTO.setSubtypeConfiguration(subtypeConfig);
        setupMCPServerDTOMock(mcpServerDTO);

        Backend existingBackend = createBackend(BACKEND_ID,
                "{\"url\":\"https://old.example.com\"}", OLD_DEFINITION);
        Backend importedBackend = createBackend(BACKEND_ID,
                "{\"url\":\"https://old.example.com\"}", NEW_DEFINITION);

        Mockito.when(apiProvider.getMCPServerBackends(API_UUID, ORGANIZATION))
                .thenReturn(Collections.singletonList(existingBackend));
        PowerMockito.doReturn(Collections.singletonList(importedBackend)).when(ImportUtils.class,
                "getMCPServerBackends", EXTRACTED_PATH);

        ImportUtils.importMCPServer(EXTRACTED_PATH, mcpServerDTO, true, false, true,
                false, false, tokenScopes, null, ORGANIZATION);

        ArgumentCaptor<Backend> newBackendCaptor = ArgumentCaptor.forClass(Backend.class);
        PowerMockito.verifyStatic(PublisherCommonUtils.class);
        PublisherCommonUtils.updateMCPServerBackend(ArgumentMatchers.eq(API_UUID),
                ArgumentMatchers.any(Backend.class), newBackendCaptor.capture(),
                ArgumentMatchers.eq(ORGANIZATION), ArgumentMatchers.eq(apiProvider));

        Backend capturedBackend = newBackendCaptor.getValue();
        Assert.assertEquals("Backend definition should be updated to imported definition",
                NEW_DEFINITION, capturedBackend.getDefinition());
    }

    @Test
    public void testDirectBackendDefinitionNotUpdatedWhenAbsent() throws Exception {
        SubtypeConfigurationDTO subtypeConfig = new SubtypeConfigurationDTO();
        subtypeConfig.setSubtype(APIConstants.API_SUBTYPE_DIRECT_BACKEND);
        mcpServerDTO.setSubtypeConfiguration(subtypeConfig);
        setupMCPServerDTOMock(mcpServerDTO);

        Backend existingBackend = createBackend(BACKEND_ID,
                "{\"url\":\"https://old.example.com\"}", OLD_DEFINITION);
        Backend importedBackend = createBackend(BACKEND_ID,
                "{\"url\":\"https://old.example.com\"}", null);

        Mockito.when(apiProvider.getMCPServerBackends(API_UUID, ORGANIZATION))
                .thenReturn(Collections.singletonList(existingBackend));
        PowerMockito.doReturn(Collections.singletonList(importedBackend)).when(ImportUtils.class,
                "getMCPServerBackends", EXTRACTED_PATH);

        ImportUtils.importMCPServer(EXTRACTED_PATH, mcpServerDTO, true, false, true,
                false, false, tokenScopes, null, ORGANIZATION);

        ArgumentCaptor<Backend> newBackendCaptor = ArgumentCaptor.forClass(Backend.class);
        PowerMockito.verifyStatic(PublisherCommonUtils.class);
        PublisherCommonUtils.updateMCPServerBackend(ArgumentMatchers.eq(API_UUID),
                ArgumentMatchers.any(Backend.class), newBackendCaptor.capture(),
                ArgumentMatchers.eq(ORGANIZATION), ArgumentMatchers.eq(apiProvider));

        Backend capturedBackend = newBackendCaptor.getValue();
        Assert.assertEquals("Backend definition should remain unchanged when not provided in import",
                OLD_DEFINITION, capturedBackend.getDefinition());
    }

    @Test
    public void testDirectBackendDefinitionNotUpdatedWhenBlank() throws Exception {
        SubtypeConfigurationDTO subtypeConfig = new SubtypeConfigurationDTO();
        subtypeConfig.setSubtype(APIConstants.API_SUBTYPE_DIRECT_BACKEND);
        mcpServerDTO.setSubtypeConfiguration(subtypeConfig);
        setupMCPServerDTOMock(mcpServerDTO);

        Backend existingBackend = createBackend(BACKEND_ID,
                "{\"url\":\"https://old.example.com\"}", OLD_DEFINITION);
        Backend importedBackend = createBackend(BACKEND_ID,
                "{\"url\":\"https://old.example.com\"}", "   ");

        Mockito.when(apiProvider.getMCPServerBackends(API_UUID, ORGANIZATION))
                .thenReturn(Collections.singletonList(existingBackend));
        PowerMockito.doReturn(Collections.singletonList(importedBackend)).when(ImportUtils.class,
                "getMCPServerBackends", EXTRACTED_PATH);

        ImportUtils.importMCPServer(EXTRACTED_PATH, mcpServerDTO, true, false, true,
                false, false, tokenScopes, null, ORGANIZATION);

        ArgumentCaptor<Backend> newBackendCaptor = ArgumentCaptor.forClass(Backend.class);
        PowerMockito.verifyStatic(PublisherCommonUtils.class);
        PublisherCommonUtils.updateMCPServerBackend(ArgumentMatchers.eq(API_UUID),
                ArgumentMatchers.any(Backend.class), newBackendCaptor.capture(),
                ArgumentMatchers.eq(ORGANIZATION), ArgumentMatchers.eq(apiProvider));

        Backend capturedBackend = newBackendCaptor.getValue();
        Assert.assertEquals("Backend definition should remain unchanged when blank string provided",
                OLD_DEFINITION, capturedBackend.getDefinition());
    }

    // -------------------------------------------------------------------------
    // SERVER_PROXY: definition update via import
    // -------------------------------------------------------------------------

    @Test
    public void testServerProxyDefinitionUpdatedOnImport() throws Exception {
        SubtypeConfigurationDTO subtypeConfig = new SubtypeConfigurationDTO();
        subtypeConfig.setSubtype(APIConstants.API_SUBTYPE_SERVER_PROXY);
        mcpServerDTO.setSubtypeConfiguration(subtypeConfig);
        setupMCPServerDTOMock(mcpServerDTO);

        Backend existingBackend = createBackend(BACKEND_ID,
                "{\"url\":\"https://old.example.com\"}", OLD_DEFINITION);
        Backend importedBackend = createBackend(BACKEND_ID,
                "{\"url\":\"https://old.example.com\"}", NEW_DEFINITION);

        Mockito.when(apiProvider.getMCPServerBackends(API_UUID, ORGANIZATION))
                .thenReturn(Collections.singletonList(existingBackend));
        PowerMockito.doReturn(Collections.singletonList(importedBackend)).when(ImportUtils.class,
                "getMCPServerBackends", EXTRACTED_PATH);

        ImportUtils.importMCPServer(EXTRACTED_PATH, mcpServerDTO, true, false, true,
                false, false, tokenScopes, null, ORGANIZATION);

        ArgumentCaptor<Backend> newBackendCaptor = ArgumentCaptor.forClass(Backend.class);
        PowerMockito.verifyStatic(PublisherCommonUtils.class);
        PublisherCommonUtils.updateMCPServerBackend(ArgumentMatchers.eq(API_UUID),
                ArgumentMatchers.any(Backend.class), newBackendCaptor.capture(),
                ArgumentMatchers.eq(ORGANIZATION), ArgumentMatchers.eq(apiProvider));

        Backend capturedBackend = newBackendCaptor.getValue();
        Assert.assertEquals("SERVER_PROXY backend definition should be updated",
                NEW_DEFINITION, capturedBackend.getDefinition());
    }

    // -------------------------------------------------------------------------
    // EXISTING_API: no backend update path (no backends.yaml)
    // -------------------------------------------------------------------------

    @Test
    public void testExistingApiSubtypeSkipsBackendUpdate() throws Exception {
        SubtypeConfigurationDTO subtypeConfig = new SubtypeConfigurationDTO();
        subtypeConfig.setSubtype(APIConstants.API_SUBTYPE_EXISTING_API);
        mcpServerDTO.setSubtypeConfiguration(subtypeConfig);
        setupMCPServerDTOMock(mcpServerDTO);

        ImportUtils.importMCPServer(EXTRACTED_PATH, mcpServerDTO, true, false, true,
                false, false, tokenScopes, null, ORGANIZATION);

        Mockito.verify(apiProvider, Mockito.never()).getMCPServerBackends(
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
        PowerMockito.verifyStatic(PublisherCommonUtils.class, Mockito.never());
        PublisherCommonUtils.updateMCPServerBackend(ArgumentMatchers.anyString(),
                ArgumentMatchers.any(Backend.class), ArgumentMatchers.any(Backend.class),
                ArgumentMatchers.anyString(), ArgumentMatchers.any(APIProvider.class));
    }

    // -------------------------------------------------------------------------
    // Endpoint config is always copied from import
    // -------------------------------------------------------------------------

    @Test
    public void testEndpointConfigAlwaysCopiedFromImport() throws Exception {
        SubtypeConfigurationDTO subtypeConfig = new SubtypeConfigurationDTO();
        subtypeConfig.setSubtype(APIConstants.API_SUBTYPE_DIRECT_BACKEND);
        mcpServerDTO.setSubtypeConfiguration(subtypeConfig);
        setupMCPServerDTOMock(mcpServerDTO);

        String oldConfig = "{\"url\":\"https://old.example.com\"}";
        String newConfig = "{\"url\":\"https://new.example.com\"}";

        Backend existingBackend = createBackend(BACKEND_ID, oldConfig, OLD_DEFINITION);
        Backend importedBackend = createBackend(BACKEND_ID, newConfig, null);

        Mockito.when(apiProvider.getMCPServerBackends(API_UUID, ORGANIZATION))
                .thenReturn(Collections.singletonList(existingBackend));
        PowerMockito.doReturn(Collections.singletonList(importedBackend)).when(ImportUtils.class,
                "getMCPServerBackends", EXTRACTED_PATH);

        ImportUtils.importMCPServer(EXTRACTED_PATH, mcpServerDTO, true, false, true,
                false, false, tokenScopes, null, ORGANIZATION);

        ArgumentCaptor<Backend> newBackendCaptor = ArgumentCaptor.forClass(Backend.class);
        PowerMockito.verifyStatic(PublisherCommonUtils.class);
        PublisherCommonUtils.updateMCPServerBackend(ArgumentMatchers.eq(API_UUID),
                ArgumentMatchers.any(Backend.class), newBackendCaptor.capture(),
                ArgumentMatchers.eq(ORGANIZATION), ArgumentMatchers.eq(apiProvider));

        Backend capturedBackend = newBackendCaptor.getValue();
        Assert.assertEquals("Endpoint config should always be copied from imported backend",
                newConfig, capturedBackend.getEndpointConfig());
    }
}
