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
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
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

@RunWith(PowerMockRunner.class)
@PrepareForTest({ImportUtils.class, RestApiCommonUtil.class, APIControllerUtil.class,
        MultitenantUtils.class, APIUtil.class, PublisherCommonUtils.class})
@SuppressStaticInitializationFor({"org.wso2.carbon.apimgt.impl.utils.APIUtil"})
public class ImportUtilsMCPServerTest {

    private static final String ORGANIZATION = "carbon.super";
    private static final String USERNAME = "admin";
    private static final String TENANT_DOMAIN = "carbon.super";
    private static final String API_UUID = "test-uuid-1234";
    private static final String API_NAME = "TestMCPServer";
    private static final String API_VERSION = "1.0.0";
    private static final String API_CONTEXT = "/test-mcp";
    private static final String EXTRACTED_PATH = "/tmp/test-mcp-import";

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

    @Test
    public void testImportMCPServerOverwriteWithExistingAPISubtype() throws Exception {
        SubtypeConfigurationDTO subtypeConfig = new SubtypeConfigurationDTO();
        subtypeConfig.setSubtype(APIConstants.API_SUBTYPE_EXISTING_API);
        mcpServerDTO.setSubtypeConfiguration(subtypeConfig);

        setupMCPServerDTOMock(mcpServerDTO);

        try {
            ImportUtils.importMCPServer(EXTRACTED_PATH, mcpServerDTO, true, false, true,
                    false, false, tokenScopes, null, ORGANIZATION);
        } catch (APIManagementException e) {
            Assert.fail("Import should succeed for EXISTING_API subtype without backends, but got: "
                    + e.getMessage());
        }

        Mockito.verify(apiProvider, Mockito.never()).getMCPServerBackends(
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
        PowerMockito.verifyPrivate(ImportUtils.class, Mockito.never()).invoke(
                "getMCPServerBackends", ArgumentMatchers.anyString());
        Mockito.verify(apiProvider).updateAPI(
                ArgumentMatchers.any(API.class), ArgumentMatchers.any(API.class));
    }

    @Test
    public void testImportMCPServerOverwriteWithServerProxySubtypeAndBackends() throws Exception {
        SubtypeConfigurationDTO subtypeConfig = new SubtypeConfigurationDTO();
        subtypeConfig.setSubtype(APIConstants.API_SUBTYPE_SERVER_PROXY);
        mcpServerDTO.setSubtypeConfiguration(subtypeConfig);

        setupMCPServerDTOMock(mcpServerDTO);

        Backend existingBackend = new Backend();
        existingBackend.setId("backend-1");
        existingBackend.setName("existing-backend");
        existingBackend.setEndpointConfig("{\"url\":\"https://old.example.com\"}");

        Backend importedBackend = new Backend();
        importedBackend.setId("backend-2");
        importedBackend.setName("imported-backend");
        importedBackend.setEndpointConfig("{\"url\":\"https://new.example.com\"}");

        List<Backend> existingBackends = new ArrayList<>();
        existingBackends.add(existingBackend);
        List<Backend> importedBackends = new ArrayList<>();
        importedBackends.add(importedBackend);

        Mockito.when(apiProvider.getMCPServerBackends(API_UUID, ORGANIZATION))
                .thenReturn(existingBackends);
        PowerMockito.doReturn(importedBackends).when(ImportUtils.class,
                "getMCPServerBackends", EXTRACTED_PATH);

        try {
            ImportUtils.importMCPServer(EXTRACTED_PATH, mcpServerDTO, true, false, true,
                    false, false, tokenScopes, null, ORGANIZATION);
        } catch (APIManagementException e) {
            Assert.fail("Import should succeed for SERVER_PROXY with valid backends, but got: "
                    + e.getMessage());
        }

        Mockito.verify(apiProvider).getMCPServerBackends(API_UUID, ORGANIZATION);
        PowerMockito.verifyStatic(PublisherCommonUtils.class);
        PublisherCommonUtils.updateMCPServerBackend(ArgumentMatchers.eq(API_UUID),
                ArgumentMatchers.any(Backend.class), ArgumentMatchers.any(Backend.class),
                ArgumentMatchers.eq(ORGANIZATION), ArgumentMatchers.eq(apiProvider));
        Mockito.verify(apiProvider).updateAPI(
                ArgumentMatchers.any(API.class), ArgumentMatchers.any(API.class));
    }

    @Test
    public void testImportMCPServerOverwriteWithDirectBackendSubtypeAndBackends() throws Exception {
        SubtypeConfigurationDTO subtypeConfig = new SubtypeConfigurationDTO();
        subtypeConfig.setSubtype(APIConstants.API_SUBTYPE_DIRECT_BACKEND);
        mcpServerDTO.setSubtypeConfiguration(subtypeConfig);

        setupMCPServerDTOMock(mcpServerDTO);

        Backend existingBackend = new Backend();
        existingBackend.setId("backend-1");
        existingBackend.setEndpointConfig("{\"url\":\"https://old.example.com\"}");
        Backend importedBackend = new Backend();
        importedBackend.setId("backend-2");
        importedBackend.setEndpointConfig("{\"url\":\"https://new.example.com\"}");

        Mockito.when(apiProvider.getMCPServerBackends(API_UUID, ORGANIZATION))
                .thenReturn(Collections.singletonList(existingBackend));
        PowerMockito.doReturn(Collections.singletonList(importedBackend)).when(ImportUtils.class,
                "getMCPServerBackends", EXTRACTED_PATH);

        try {
            ImportUtils.importMCPServer(EXTRACTED_PATH, mcpServerDTO, true, false, true,
                    false, false, tokenScopes, null, ORGANIZATION);
        } catch (APIManagementException e) {
            Assert.fail("Import should succeed for DIRECT_BACKEND with valid backends, but got: "
                    + e.getMessage());
        }

        Mockito.verify(apiProvider).getMCPServerBackends(API_UUID, ORGANIZATION);
        PowerMockito.verifyStatic(PublisherCommonUtils.class);
        PublisherCommonUtils.updateMCPServerBackend(ArgumentMatchers.eq(API_UUID),
                ArgumentMatchers.any(Backend.class), ArgumentMatchers.any(Backend.class),
                ArgumentMatchers.eq(ORGANIZATION), ArgumentMatchers.eq(apiProvider));
    }

    @Test
    public void testImportMCPServerOverwriteWithServerProxyEmptyExistingBackends() throws Exception {
        SubtypeConfigurationDTO subtypeConfig = new SubtypeConfigurationDTO();
        subtypeConfig.setSubtype(APIConstants.API_SUBTYPE_SERVER_PROXY);
        mcpServerDTO.setSubtypeConfiguration(subtypeConfig);

        setupMCPServerDTOMock(mcpServerDTO);

        Backend importedBackend = new Backend();
        importedBackend.setEndpointConfig("{\"url\":\"https://new.example.com\"}");

        Mockito.when(apiProvider.getMCPServerBackends(API_UUID, ORGANIZATION))
                .thenReturn(new ArrayList<>());
        PowerMockito.doReturn(Collections.singletonList(importedBackend)).when(ImportUtils.class,
                "getMCPServerBackends", EXTRACTED_PATH);

        try {
            ImportUtils.importMCPServer(EXTRACTED_PATH, mcpServerDTO, true, false, true,
                    false, false, tokenScopes, null, ORGANIZATION);
            Assert.fail("Should throw APIManagementException when existing backends are empty "
                    + "for SERVER_PROXY subtype");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("No backends found to update for API"));
        }
    }

    @Test
    public void testImportMCPServerOverwriteWithServerProxyEmptyImportedBackends() throws Exception {
        SubtypeConfigurationDTO subtypeConfig = new SubtypeConfigurationDTO();
        subtypeConfig.setSubtype(APIConstants.API_SUBTYPE_SERVER_PROXY);
        mcpServerDTO.setSubtypeConfiguration(subtypeConfig);

        setupMCPServerDTOMock(mcpServerDTO);

        Backend existingBackend = new Backend();
        existingBackend.setEndpointConfig("{\"url\":\"https://old.example.com\"}");

        Mockito.when(apiProvider.getMCPServerBackends(API_UUID, ORGANIZATION))
                .thenReturn(Collections.singletonList(existingBackend));
        PowerMockito.doReturn(new ArrayList<>()).when(ImportUtils.class,
                "getMCPServerBackends", EXTRACTED_PATH);

        try {
            ImportUtils.importMCPServer(EXTRACTED_PATH, mcpServerDTO, true, false, true,
                    false, false, tokenScopes, null, ORGANIZATION);
            Assert.fail("Should throw APIManagementException when imported backends are empty "
                    + "for SERVER_PROXY subtype");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("No backends found to update for API"));
        }
    }

    @Test
    public void testImportMCPServerOverwriteWithNullSubtypeConfiguration() throws Exception {
        mcpServerDTO.setSubtypeConfiguration(null);

        setupMCPServerDTOMock(mcpServerDTO);

        try {
            ImportUtils.importMCPServer(EXTRACTED_PATH, mcpServerDTO, true, false, true,
                    false, false, tokenScopes, null, ORGANIZATION);
        } catch (APIManagementException e) {
            Assert.fail("Import should succeed when subtypeConfiguration is null, but got: "
                    + e.getMessage());
        }

        Mockito.verify(apiProvider, Mockito.never()).getMCPServerBackends(
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
        Mockito.verify(apiProvider).updateAPI(
                ArgumentMatchers.any(API.class), ArgumentMatchers.any(API.class));
    }

    @Test
    public void testImportMCPServerOverwriteWithBlankSubtype() throws Exception {
        SubtypeConfigurationDTO subtypeConfig = new SubtypeConfigurationDTO();
        subtypeConfig.setSubtype("");
        mcpServerDTO.setSubtypeConfiguration(subtypeConfig);

        setupMCPServerDTOMock(mcpServerDTO);

        try {
            ImportUtils.importMCPServer(EXTRACTED_PATH, mcpServerDTO, true, false, true,
                    false, false, tokenScopes, null, ORGANIZATION);
        } catch (APIManagementException e) {
            Assert.fail("Import should succeed when subtype is blank, but got: " + e.getMessage());
        }

        Mockito.verify(apiProvider, Mockito.never()).getMCPServerBackends(
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
        Mockito.verify(apiProvider).updateAPI(
                ArgumentMatchers.any(API.class), ArgumentMatchers.any(API.class));
    }

    @Test
    public void testImportMCPServerOverwriteWithBothBackendsEmpty() throws Exception {
        SubtypeConfigurationDTO subtypeConfig = new SubtypeConfigurationDTO();
        subtypeConfig.setSubtype(APIConstants.API_SUBTYPE_SERVER_PROXY);
        mcpServerDTO.setSubtypeConfiguration(subtypeConfig);

        setupMCPServerDTOMock(mcpServerDTO);

        Mockito.when(apiProvider.getMCPServerBackends(API_UUID, ORGANIZATION))
                .thenReturn(new ArrayList<>());
        PowerMockito.doReturn(new ArrayList<>()).when(ImportUtils.class,
                "getMCPServerBackends", EXTRACTED_PATH);

        try {
            ImportUtils.importMCPServer(EXTRACTED_PATH, mcpServerDTO, true, false, true,
                    false, false, tokenScopes, null, ORGANIZATION);
            Assert.fail("Should throw APIManagementException when both backend lists are empty "
                    + "for SERVER_PROXY subtype");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("No backends found to update for API"));
        }
    }
}
