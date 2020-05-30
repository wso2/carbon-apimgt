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

package org.wso2.carbon.apimgt.rest.api.endpoint.registry.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.wso2.carbon.apimgt.api.endpoint.registry.api.EndpointRegistryException;
import org.wso2.carbon.apimgt.api.endpoint.registry.api.EndpointRegistryResourceAlreadyExistsException;
import org.wso2.carbon.apimgt.impl.endpoint.registry.constants.EndpointRegistryConstants;
import org.wso2.carbon.apimgt.impl.endpoint.registry.impl.EndpointRegistryImpl;
import org.wso2.carbon.apimgt.api.endpoint.registry.model.EndpointRegistryEntry;
import org.wso2.carbon.apimgt.api.endpoint.registry.model.EndpointRegistryInfo;
import org.wso2.carbon.apimgt.impl.endpoint.registry.util.EndpointRegistryUtil;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.RegistriesApiService;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.dto.RegistryArrayDTO;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.dto.RegistryDTO;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.dto.RegistryEntryArrayDTO;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.dto.RegistryEntryDTO;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.util.EndpointRegistryMappingUtils;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor({"org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil"})
@PrepareForTest({RestApiUtil.class, RegistriesApiServiceImpl.class, ServiceReferenceHolder.class,
        MultitenantUtils.class, EndpointRegistryUtil.class})
public class RegistriesApiServiceImplTest {
    private final String ADMIN_USERNAME = "admin";
    private final String TENANT_DOMAIN = "carbon.super";

    private RegistriesApiService registriesApiService;
    private EndpointRegistryImpl registryProvider;
    private MessageContext messageContext;

    @Before
    public void init() throws Exception {
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        RealmService realmService = Mockito.mock(RealmService.class);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        int TENANT_ID = -1234;
        Mockito.when(tenantManager.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);

        PowerMockito.mockStatic(MultitenantUtils.class);
        PowerMockito.doReturn(TENANT_DOMAIN)
                .when(MultitenantUtils.class, "getTenantDomain", Mockito.eq(ADMIN_USERNAME));

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.doReturn(ADMIN_USERNAME).when(RestApiUtil.class, "getLoggedInUsername");
        PowerMockito.doReturn(TENANT_DOMAIN).when(RestApiUtil.class, "getLoggedInUserTenantDomain");

        PowerMockito.mockStatic(EndpointRegistryUtil.class);
        PowerMockito.doReturn(true).when(EndpointRegistryUtil.class, "isValidDefinition",
                Mockito.any(URL.class), Mockito.anyString());
        PowerMockito.doReturn(true).when(EndpointRegistryUtil.class, "isValidDefinition",
                Mockito.any(byte[].class), Mockito.anyString());

        messageContext = Mockito.mock(MessageContext.class);
        registryProvider = Mockito.mock(EndpointRegistryImpl.class);
        PowerMockito.whenNew(EndpointRegistryImpl.class).withAnyArguments().thenReturn(registryProvider);
        registriesApiService = new RegistriesApiServiceImpl();
    }

    @Test
    public void getRegistryByUUID() throws Exception {
        EndpointRegistryInfo endpointRegistryInfo = createRegistryWithDefaultParams();
        final String REGISTRY_UUID = endpointRegistryInfo.getUuid();

        Mockito.when(registryProvider.getEndpointRegistryByUUID(REGISTRY_UUID, TENANT_DOMAIN))
                .thenReturn(endpointRegistryInfo);

        Response response = registriesApiService.getRegistryByUUID(REGISTRY_UUID, messageContext);

        Assert.assertNotNull("Endpoint Registry retrieval with existing UUID failed",
                response.getEntity());
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        RegistryDTO responseDTO = (RegistryDTO) response.getEntity();
        compareRegistryDTOs(EndpointRegistryMappingUtils.fromEndpointRegistryToDTO(endpointRegistryInfo),
                responseDTO);
    }

    @Test
    public void getRegistryByUUID_NonExistingUUID() throws EndpointRegistryException {
        final String REGISTRY_UUID = "abc-1";

        Mockito.when(registryProvider.getEndpointRegistryByUUID(REGISTRY_UUID, TENANT_DOMAIN)).thenReturn(null);

        Response response = registriesApiService.getRegistryByUUID(REGISTRY_UUID, messageContext);

        Assert.assertNull("Endpoint Registry retrieval succeded for a wrong UUID ", response);
    }

    @Test
    public void getRegistries() throws EndpointRegistryException {
        List<EndpointRegistryInfo> endpointRegistryInfoList = new ArrayList<>();

        EndpointRegistryInfo endpointRegistryInfo1 = createRegistry("abc1", 1, "Endpoint Registry 1",
                RegistryDTO.TypeEnum.WSO2, ADMIN_USERNAME);
        endpointRegistryInfoList.add(endpointRegistryInfo1);

        EndpointRegistryInfo endpointRegistryInfo2 = createRegistry("abc2", 2, "Endpoint Registry 2",
                RegistryDTO.TypeEnum.WSO2, ADMIN_USERNAME);
        endpointRegistryInfoList.add(endpointRegistryInfo2);

        Mockito.when(registryProvider.getEndpointRegistries(TENANT_DOMAIN))
                .thenReturn(endpointRegistryInfoList);

        Response response = registriesApiService.getRegistries(messageContext);

        Assert.assertNotNull("Endpoint Registries retrieval failed", response);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        RegistryArrayDTO responseArrayDTO = (RegistryArrayDTO) response.getEntity();

        for (int i = 0; i < responseArrayDTO.size(); i++) {
            compareRegistryDTOs(
                    EndpointRegistryMappingUtils.fromEndpointRegistryToDTO(endpointRegistryInfoList.get(i)),
                    responseArrayDTO.get(i));
        }
    }

    @Test
    public void addRegistry() throws EndpointRegistryException {
        EndpointRegistryInfo endpointRegistryInfo = createRegistryWithDefaultParams();
        RegistryDTO payloadDTO = EndpointRegistryMappingUtils.fromEndpointRegistryToDTO(endpointRegistryInfo);

        Mockito.when(registryProvider.addEndpointRegistry(Mockito.any(EndpointRegistryInfo.class)))
                .thenReturn(endpointRegistryInfo.getUuid());
        Mockito.when(registryProvider.getEndpointRegistryByUUID(payloadDTO.getId(), TENANT_DOMAIN))
                .thenReturn(endpointRegistryInfo);

        Response response = registriesApiService.addRegistry(payloadDTO, messageContext);

        Assert.assertNotNull("Endpoint Registry creation failed", response);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        RegistryDTO responseDTO = (RegistryDTO) response.getEntity();
        compareRegistryDTOs(payloadDTO, responseDTO);
    }

    @Test
    public void addRegistry_ResourceNameExists() throws EndpointRegistryException {
        EndpointRegistryInfo endpointRegistryInfo = createRegistryWithDefaultParams();
        RegistryDTO payloadDTO = EndpointRegistryMappingUtils.fromEndpointRegistryToDTO(endpointRegistryInfo);

        EndpointRegistryResourceAlreadyExistsException resourceAlreadyExistsException
                = Mockito.mock(EndpointRegistryResourceAlreadyExistsException.class);
        Mockito.when(registryProvider.addEndpointRegistry(Mockito.any(EndpointRegistryInfo.class)))
                .thenThrow(resourceAlreadyExistsException);

        Response response = registriesApiService.addRegistry(payloadDTO, messageContext);

        Assert.assertNull("Endpoint Registry creation succeeded for a existing name", response);
    }

    @Test
    public void updateRegistry() throws EndpointRegistryException {
        EndpointRegistryInfo endpointRegistryInfoOld = createRegistryWithDefaultParams();
        EndpointRegistryInfo endpointRegistryInfoNew = createRegistry(endpointRegistryInfoOld.getUuid(), 2,
                "Endpoint Registry 2", RegistryDTO.TypeEnum.WSO2, "user1");

        RegistryDTO payloadDTO = EndpointRegistryMappingUtils.fromEndpointRegistryToDTO(endpointRegistryInfoNew);

        Mockito.when(registryProvider.getEndpointRegistryByUUID(payloadDTO.getId(), TENANT_DOMAIN))
                .thenReturn(endpointRegistryInfoOld, endpointRegistryInfoNew);

        Response response = registriesApiService.updateRegistry(payloadDTO, payloadDTO.getId(), messageContext);

        Mockito.verify(registryProvider)
                .updateEndpointRegistry(Mockito.eq(payloadDTO.getId()), Mockito.eq(endpointRegistryInfoOld.getName()),
                        Mockito.eq(endpointRegistryInfoOld.getType()), Mockito.any(EndpointRegistryInfo.class));
        Assert.assertNotNull("Endpoint Registry update failed", response);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        RegistryDTO responseDTO = (RegistryDTO) response.getEntity();
        compareRegistryDTOs(payloadDTO, responseDTO);
    }

    @Test
    public void updateRegistry_existingName() throws EndpointRegistryException {
        EndpointRegistryInfo endpointRegistryInfoOld = createRegistryWithDefaultParams();
        EndpointRegistryInfo endpointRegistryInfoNew = createRegistry(endpointRegistryInfoOld.getUuid(), 2,
                "Endpoint Registry 2", RegistryDTO.TypeEnum.WSO2, "user1");

        RegistryDTO payloadDTO = EndpointRegistryMappingUtils.fromEndpointRegistryToDTO(endpointRegistryInfoNew);

        EndpointRegistryResourceAlreadyExistsException resourceAlreadyExistsException
                = Mockito.mock(EndpointRegistryResourceAlreadyExistsException.class);
        Mockito.doThrow(resourceAlreadyExistsException).when(registryProvider)
                .updateEndpointRegistry(Mockito.eq(endpointRegistryInfoOld.getUuid()),
                        Mockito.eq(endpointRegistryInfoOld.getName()), Mockito.eq(endpointRegistryInfoOld.getType()),
                        Mockito.any(EndpointRegistryInfo.class));
        Mockito.when(registryProvider.getEndpointRegistryByUUID(payloadDTO.getId(), TENANT_DOMAIN))
                .thenReturn(endpointRegistryInfoOld, endpointRegistryInfoNew);

        Response response = registriesApiService.updateRegistry(payloadDTO, payloadDTO.getId(), messageContext);

        Assert.assertNull("Endpoint Registry update failed", response);
    }

    @Test
    public void deleteRegistry() throws EndpointRegistryException {
        EndpointRegistryInfo endpointRegistryInfo = createRegistryWithDefaultParams();
        final String REGISTRY_UUID = endpointRegistryInfo.getUuid();

        Mockito.when(registryProvider.getEndpointRegistryByUUID(REGISTRY_UUID, TENANT_DOMAIN))
                .thenReturn(endpointRegistryInfo);

        Response response = registriesApiService.deleteRegistry(REGISTRY_UUID, messageContext);

        Mockito.verify(registryProvider).deleteEndpointRegistry(REGISTRY_UUID);
        Assert.assertNotNull("Endpoint Registry delete failed", response);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void createRegistryEntry_validOASV2Yaml() throws Exception {
        EndpointRegistryInfo endpointRegistryInfo = createRegistryWithDefaultParams();
        final String REGISTRY_UUID = endpointRegistryInfo.getUuid();

        EndpointRegistryEntry endpointRegistryEntry = createRegistryEntryWithDefaultParams();
        InputStream definitionFileStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("oasV2-sample.yaml");
        Attachment definitionFileDetail = Mockito.mock(Attachment.class);
        endpointRegistryEntry.setEndpointDefinition(definitionFileStream);
        RegistryEntryDTO payloadEntryDTO = EndpointRegistryMappingUtils.fromRegistryEntryToDTO(endpointRegistryEntry);

        Mockito.when(registryProvider.getEndpointRegistryByUUID(REGISTRY_UUID, TENANT_DOMAIN))
                .thenReturn(endpointRegistryInfo);
        Mockito.when(registryProvider.addEndpointRegistryEntry(Mockito.any(EndpointRegistryEntry.class)))
                .thenReturn(endpointRegistryEntry.getEntryId());
        Mockito.when(registryProvider.getEndpointRegistryEntryByUUID(REGISTRY_UUID,
                endpointRegistryEntry.getEntryId())).thenReturn(endpointRegistryEntry);

        Response response = registriesApiService.createRegistryEntry(REGISTRY_UUID, payloadEntryDTO,
                definitionFileStream, definitionFileDetail, messageContext);
        Assert.assertNotNull("Endpoint Registry Entry creation failed", response);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        RegistryEntryDTO responseEntryDTO = (RegistryEntryDTO) response.getEntity();
        compareRegistryEntryDTOs(payloadEntryDTO, responseEntryDTO);
    }

    @Test
    public void createRegistryEntry_validOASV2JSon() throws Exception {
        EndpointRegistryInfo endpointRegistryInfo = createRegistryWithDefaultParams();
        final String REGISTRY_UUID = endpointRegistryInfo.getUuid();

        EndpointRegistryEntry endpointRegistryEntry = createRegistryEntryWithDefaultParams();
        InputStream definitionFileStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("oasV2-sample.json");
        Attachment definitionFileDetail = Mockito.mock(Attachment.class);
        endpointRegistryEntry.setEndpointDefinition(definitionFileStream);
        RegistryEntryDTO payloadEntryDTO = EndpointRegistryMappingUtils.fromRegistryEntryToDTO(endpointRegistryEntry);

        Mockito.when(registryProvider.getEndpointRegistryByUUID(REGISTRY_UUID, TENANT_DOMAIN))
                .thenReturn(endpointRegistryInfo);
        Mockito.when(registryProvider.addEndpointRegistryEntry(Mockito.any(EndpointRegistryEntry.class)))
                .thenReturn(endpointRegistryEntry.getEntryId());
        Mockito.when(registryProvider.getEndpointRegistryEntryByUUID(REGISTRY_UUID,
                endpointRegistryEntry.getEntryId())).thenReturn(endpointRegistryEntry);

        Response response = registriesApiService.createRegistryEntry(REGISTRY_UUID, payloadEntryDTO,
                definitionFileStream, definitionFileDetail, messageContext);
        Assert.assertNotNull("Endpoint Registry Entry creation failed", response);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        RegistryEntryDTO responseEntryDTO = (RegistryEntryDTO) response.getEntity();
        compareRegistryEntryDTOs(payloadEntryDTO, responseEntryDTO);
    }

    @Test
    public void createRegistryEntry_validOASV3JSon() throws Exception {
        EndpointRegistryInfo endpointRegistryInfo = createRegistryWithDefaultParams();
        final String REGISTRY_UUID = endpointRegistryInfo.getUuid();

        EndpointRegistryEntry endpointRegistryEntry = createRegistryEntryWithDefaultParams();
        InputStream definitionFileStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("oasV3-sample.json");
        Attachment definitionFileDetail = Mockito.mock(Attachment.class);
        endpointRegistryEntry.setEndpointDefinition(definitionFileStream);
        RegistryEntryDTO payloadEntryDTO = EndpointRegistryMappingUtils.fromRegistryEntryToDTO(endpointRegistryEntry);

        Mockito.when(registryProvider.getEndpointRegistryByUUID(REGISTRY_UUID, TENANT_DOMAIN))
                .thenReturn(endpointRegistryInfo);
        Mockito.when(registryProvider.addEndpointRegistryEntry(Mockito.any(EndpointRegistryEntry.class)))
                .thenReturn(endpointRegistryEntry.getEntryId());
        Mockito.when(registryProvider.getEndpointRegistryEntryByUUID(REGISTRY_UUID,
                endpointRegistryEntry.getEntryId())).thenReturn(endpointRegistryEntry);

        Response response = registriesApiService.createRegistryEntry(REGISTRY_UUID, payloadEntryDTO,
                definitionFileStream, definitionFileDetail, messageContext);
        Assert.assertNotNull("Endpoint Registry Entry creation failed", response);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        RegistryEntryDTO responseEntryDTO = (RegistryEntryDTO) response.getEntity();
        compareRegistryEntryDTOs(payloadEntryDTO, responseEntryDTO);
    }

    @Test
    public void createRegistryEntry_validOASV3Yaml() throws Exception {
        EndpointRegistryInfo endpointRegistryInfo = createRegistryWithDefaultParams();
        final String REGISTRY_UUID = endpointRegistryInfo.getUuid();

        EndpointRegistryEntry endpointRegistryEntry = createRegistryEntryWithDefaultParams();
        InputStream definitionFileStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("oasV3-sample.yaml");
        Attachment definitionFileDetail = Mockito.mock(Attachment.class);
        endpointRegistryEntry.setEndpointDefinition(definitionFileStream);
        RegistryEntryDTO payloadEntryDTO = EndpointRegistryMappingUtils.fromRegistryEntryToDTO(endpointRegistryEntry);

        Mockito.when(registryProvider.getEndpointRegistryByUUID(REGISTRY_UUID, TENANT_DOMAIN))
                .thenReturn(endpointRegistryInfo);
        Mockito.when(registryProvider.addEndpointRegistryEntry(Mockito.any(EndpointRegistryEntry.class)))
                .thenReturn(endpointRegistryEntry.getEntryId());
        Mockito.when(registryProvider.getEndpointRegistryEntryByUUID(REGISTRY_UUID,
                endpointRegistryEntry.getEntryId())).thenReturn(endpointRegistryEntry);

        Response response = registriesApiService.createRegistryEntry(REGISTRY_UUID, payloadEntryDTO,
                definitionFileStream, definitionFileDetail, messageContext);
        Assert.assertNotNull("Endpoint Registry Entry creation failed", response);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        RegistryEntryDTO responseEntryDTO = (RegistryEntryDTO) response.getEntity();
        compareRegistryEntryDTOs(payloadEntryDTO, responseEntryDTO);
    }

    @Test
    public void createRegistryEntry_validWSDL1() throws EndpointRegistryException {
        EndpointRegistryInfo endpointRegistryInfo = createRegistryWithDefaultParams();
        final String REGISTRY_UUID = endpointRegistryInfo.getUuid();

        EndpointRegistryEntry endpointRegistryEntry = createRegistryEntryWithDefaultParams();
        endpointRegistryEntry.setDefinitionType(RegistryEntryDTO.DefinitionTypeEnum.WSDL1.toString());
        InputStream definitionFileStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("wsdl1-sample.wsdl");
        Attachment definitionFileDetail = Mockito.mock(Attachment.class);
        endpointRegistryEntry.setEndpointDefinition(definitionFileStream);
        RegistryEntryDTO payloadEntryDTO = EndpointRegistryMappingUtils.fromRegistryEntryToDTO(endpointRegistryEntry);

        Mockito.when(registryProvider.getEndpointRegistryByUUID(REGISTRY_UUID, TENANT_DOMAIN))
                .thenReturn(endpointRegistryInfo);
        Mockito.when(registryProvider.addEndpointRegistryEntry(Mockito.any(EndpointRegistryEntry.class)))
                .thenReturn(endpointRegistryEntry.getEntryId());
        Mockito.when(registryProvider.getEndpointRegistryEntryByUUID(REGISTRY_UUID,
                endpointRegistryEntry.getEntryId())).thenReturn(endpointRegistryEntry);

        Response response = registriesApiService.createRegistryEntry(REGISTRY_UUID, payloadEntryDTO,
                definitionFileStream, definitionFileDetail, messageContext);
        Assert.assertNotNull("Endpoint Registry Entry creation failed", response);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        RegistryEntryDTO responseEntryDTO = (RegistryEntryDTO) response.getEntity();
        compareRegistryEntryDTOs(payloadEntryDTO, responseEntryDTO);
    }

    @Test
    public void createRegistryEntry_validWSDL2() throws EndpointRegistryException {
        EndpointRegistryInfo endpointRegistryInfo = createRegistryWithDefaultParams();
        final String REGISTRY_UUID = endpointRegistryInfo.getUuid();

        EndpointRegistryEntry endpointRegistryEntry = createRegistryEntryWithDefaultParams();
        endpointRegistryEntry.setDefinitionType(RegistryEntryDTO.DefinitionTypeEnum.WSDL2.toString());
        InputStream definitionFileStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("wsdl2-sample.wsdl");
        Attachment definitionFileDetail = Mockito.mock(Attachment.class);
        endpointRegistryEntry.setEndpointDefinition(definitionFileStream);
        RegistryEntryDTO payloadEntryDTO = EndpointRegistryMappingUtils.fromRegistryEntryToDTO(endpointRegistryEntry);

        Mockito.when(registryProvider.getEndpointRegistryByUUID(REGISTRY_UUID, TENANT_DOMAIN))
                .thenReturn(endpointRegistryInfo);
        Mockito.when(registryProvider.addEndpointRegistryEntry(Mockito.any(EndpointRegistryEntry.class)))
                .thenReturn(endpointRegistryEntry.getEntryId());
        Mockito.when(registryProvider.getEndpointRegistryEntryByUUID(REGISTRY_UUID,
                endpointRegistryEntry.getEntryId())).thenReturn(endpointRegistryEntry);

        Response response = registriesApiService.createRegistryEntry(REGISTRY_UUID, payloadEntryDTO,
                definitionFileStream, definitionFileDetail, messageContext);
        Assert.assertNotNull("Endpoint Registry Entry creation failed", response);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        RegistryEntryDTO responseEntryDTO = (RegistryEntryDTO) response.getEntity();
        compareRegistryEntryDTOs(payloadEntryDTO, responseEntryDTO);
    }

    @Test
    public void createRegistryEntry_validGraphQL() throws EndpointRegistryException {
        EndpointRegistryInfo endpointRegistryInfo = createRegistryWithDefaultParams();
        final String REGISTRY_UUID = endpointRegistryInfo.getUuid();

        EndpointRegistryEntry endpointRegistryEntry = createRegistryEntryWithDefaultParams();
        endpointRegistryEntry.setDefinitionType(RegistryEntryDTO.DefinitionTypeEnum.GQL_SDL.toString());
        InputStream definitionFileStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("graphql-sample.graphql");
        Attachment definitionFileDetail = Mockito.mock(Attachment.class);
        endpointRegistryEntry.setEndpointDefinition(definitionFileStream);
        RegistryEntryDTO payloadEntryDTO = EndpointRegistryMappingUtils.fromRegistryEntryToDTO(endpointRegistryEntry);

        Mockito.when(registryProvider.getEndpointRegistryByUUID(REGISTRY_UUID, TENANT_DOMAIN))
                .thenReturn(endpointRegistryInfo);
        Mockito.when(registryProvider.addEndpointRegistryEntry(Mockito.any(EndpointRegistryEntry.class)))
                .thenReturn(endpointRegistryEntry.getEntryId());
        Mockito.when(registryProvider.getEndpointRegistryEntryByUUID(REGISTRY_UUID,
                endpointRegistryEntry.getEntryId())).thenReturn(endpointRegistryEntry);

        Response response = registriesApiService.createRegistryEntry(REGISTRY_UUID, payloadEntryDTO,
                definitionFileStream, definitionFileDetail, messageContext);
        Assert.assertNotNull("Endpoint Registry Entry creation failed", response);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        RegistryEntryDTO responseEntryDTO = (RegistryEntryDTO) response.getEntity();
        compareRegistryEntryDTOs(payloadEntryDTO, responseEntryDTO);
    }

    @Test
    public void createRegistryEntry_validOASUrl() throws EndpointRegistryException {
        EndpointRegistryInfo endpointRegistryInfo = createRegistryWithDefaultParams();
        final String REGISTRY_UUID = endpointRegistryInfo.getUuid();

        EndpointRegistryEntry endpointRegistryEntry = createRegistryEntryWithDefaultParams();
        endpointRegistryEntry.setDefinitionURL("https://petstore.swagger.io/v2/swagger.json");
        RegistryEntryDTO payloadEntryDTO = EndpointRegistryMappingUtils.fromRegistryEntryToDTO(endpointRegistryEntry);

        Mockito.when(registryProvider.getEndpointRegistryByUUID(REGISTRY_UUID, TENANT_DOMAIN))
                .thenReturn(endpointRegistryInfo);
        Mockito.when(registryProvider.addEndpointRegistryEntry(Mockito.any(EndpointRegistryEntry.class)))
                .thenReturn(endpointRegistryEntry.getEntryId());
        Mockito.when(registryProvider.getEndpointRegistryEntryByUUID(REGISTRY_UUID,
                endpointRegistryEntry.getEntryId())).thenReturn(endpointRegistryEntry);

        Response response = registriesApiService.createRegistryEntry(REGISTRY_UUID, payloadEntryDTO,
                null, null, messageContext);
        Assert.assertNotNull("Endpoint Registry Entry creation failed", response);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        RegistryEntryDTO responseEntryDTO = (RegistryEntryDTO) response.getEntity();
        compareRegistryEntryDTOs(payloadEntryDTO, responseEntryDTO);
    }

    @Test
    public void createRegistryEntryWithNoDefinitionFileAndUrl() throws EndpointRegistryException {
        EndpointRegistryInfo endpointRegistryInfo = createRegistryWithDefaultParams();
        final String REGISTRY_UUID = endpointRegistryInfo.getUuid();

        EndpointRegistryEntry endpointRegistryEntry = createRegistryEntryWithDefaultParams();
        endpointRegistryEntry.setDefinitionURL(null);
        endpointRegistryEntry.setEndpointDefinition(null);
        RegistryEntryDTO payloadEntryDTO = EndpointRegistryMappingUtils.fromRegistryEntryToDTO(endpointRegistryEntry);

        Mockito.when(registryProvider.getEndpointRegistryByUUID(REGISTRY_UUID, TENANT_DOMAIN))
                .thenReturn(endpointRegistryInfo);
        Mockito.when(registryProvider.addEndpointRegistryEntry(Mockito.any(EndpointRegistryEntry.class)))
                .thenReturn(endpointRegistryEntry.getEntryId());
        Mockito.when(registryProvider.getEndpointRegistryEntryByUUID(REGISTRY_UUID,
                endpointRegistryEntry.getEntryId())).thenReturn(endpointRegistryEntry);

        Response response = registriesApiService.createRegistryEntry(REGISTRY_UUID, payloadEntryDTO,
                null, null, messageContext);
        Assert.assertNotNull("Endpoint Registry Entry creation failed", response);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        RegistryEntryDTO responseEntryDTO = (RegistryEntryDTO) response.getEntity();
        compareRegistryEntryDTOs(payloadEntryDTO, responseEntryDTO);
    }

    @Test
    public void updateRegistryEntryWithDefinitionFile() throws EndpointRegistryException {
        EndpointRegistryInfo endpointRegistryInfo = createRegistryWithDefaultParams();
        final String REGISTRY_UUID = endpointRegistryInfo.getUuid();

        EndpointRegistryEntry endpointRegistryEntryOld = createRegistryEntryWithDefaultParams();

        InputStream definitionFileStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("oasV2-sample.yaml");
        Attachment definitionFileDetail = Mockito.mock(Attachment.class);

        EndpointRegistryEntry endpointRegistryEntryNew = createRegistryEntry(endpointRegistryEntryOld.getEntryId(),
                "Entry Name 2", "v1", "An Registry Entry that exposes a SOAP endpoint", "https://xyz2.com",
                RegistryEntryDTO.ServiceTypeEnum.SOAP_1_1, RegistryEntryDTO.ServiceCategoryEnum.DOMAIN,
                "https://petstore.swagger.io/v2/swagger.json", RegistryEntryDTO.DefinitionTypeEnum.OAS,
                definitionFileStream);
        RegistryEntryDTO payloadEntryDTO =
                EndpointRegistryMappingUtils.fromRegistryEntryToDTO(endpointRegistryEntryNew);

        Mockito.when(registryProvider.getEndpointRegistryByUUID(REGISTRY_UUID, TENANT_DOMAIN))
                .thenReturn(endpointRegistryInfo);
        Mockito.when(registryProvider.getEndpointRegistryEntryByUUID(REGISTRY_UUID,
                endpointRegistryEntryNew.getEntryId()))
                .thenReturn(endpointRegistryEntryOld, endpointRegistryEntryNew);

        Response response = registriesApiService.updateRegistryEntry(REGISTRY_UUID, payloadEntryDTO.getId(),
                payloadEntryDTO, definitionFileStream, definitionFileDetail, messageContext);

        Mockito.verify(registryProvider).updateEndpointRegistryEntry(Mockito.eq(endpointRegistryEntryOld.getName()),
                Mockito.any(EndpointRegistryEntry.class));
        Assert.assertNotNull("Endpoint Registry Entry creation failed", response);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        RegistryEntryDTO responseEntryDTO = (RegistryEntryDTO) response.getEntity();
        compareRegistryEntryDTOs(payloadEntryDTO, responseEntryDTO);
    }

    @Test
    public void updateRegistryEntryWithDefinitionUrl() throws EndpointRegistryException {
        EndpointRegistryInfo endpointRegistryInfo = createRegistryWithDefaultParams();
        final String REGISTRY_UUID = endpointRegistryInfo.getUuid();

        EndpointRegistryEntry endpointRegistryEntryOld = createRegistryEntryWithDefaultParams();

        EndpointRegistryEntry endpointRegistryEntryNew = createRegistryEntry(endpointRegistryEntryOld.getEntryId(),
                "Entry Name 2", "v1", "An Registry Entry that exposes a SOAP endpoint", "https://xyz2.com",
                RegistryEntryDTO.ServiceTypeEnum.SOAP_1_1, RegistryEntryDTO.ServiceCategoryEnum.DOMAIN,
                "https://petstore.swagger.io/v2/swagger.json", RegistryEntryDTO.DefinitionTypeEnum.OAS,
                null);

        RegistryEntryDTO payloadEntryDTO =
                EndpointRegistryMappingUtils.fromRegistryEntryToDTO(endpointRegistryEntryNew);

        Mockito.when(registryProvider.getEndpointRegistryByUUID(REGISTRY_UUID, TENANT_DOMAIN))
                .thenReturn(endpointRegistryInfo);
        Mockito.when(registryProvider.getEndpointRegistryEntryByUUID(REGISTRY_UUID,
                endpointRegistryEntryNew.getEntryId()))
                .thenReturn(endpointRegistryEntryOld, endpointRegistryEntryNew);

        Response response = registriesApiService.updateRegistryEntry(REGISTRY_UUID, payloadEntryDTO.getId(),
                payloadEntryDTO, null, null, messageContext);

        Mockito.verify(registryProvider).updateEndpointRegistryEntry(Mockito.eq(endpointRegistryEntryOld.getName()),
                Mockito.any(EndpointRegistryEntry.class));
        Assert.assertNotNull("Endpoint Registry Entry creation failed", response);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        RegistryEntryDTO responseEntryDTO = (RegistryEntryDTO) response.getEntity();
        compareRegistryEntryDTOs(payloadEntryDTO, responseEntryDTO);
    }

    @Test
    public void updateRegistryEntryWithNoDefinitionFileAndUrl() throws EndpointRegistryException {
        EndpointRegistryInfo endpointRegistryInfo = createRegistryWithDefaultParams();
        final String REGISTRY_UUID = endpointRegistryInfo.getUuid();

        EndpointRegistryEntry endpointRegistryEntryOld = createRegistryEntryWithDefaultParams();

        EndpointRegistryEntry endpointRegistryEntryNew = createRegistryEntry(endpointRegistryEntryOld.getEntryId(),
                "Entry Name 2", "v1", "An Registry Entry that exposes a SOAP endpoint", "https://xyz2.com",
                RegistryEntryDTO.ServiceTypeEnum.SOAP_1_1, RegistryEntryDTO.ServiceCategoryEnum.DOMAIN,
                null, RegistryEntryDTO.DefinitionTypeEnum.OAS, null);

        RegistryEntryDTO payloadEntryDTO =
                EndpointRegistryMappingUtils.fromRegistryEntryToDTO(endpointRegistryEntryNew);

        Mockito.when(registryProvider.getEndpointRegistryByUUID(REGISTRY_UUID, TENANT_DOMAIN))
                .thenReturn(endpointRegistryInfo);
        Mockito.when(registryProvider.getEndpointRegistryEntryByUUID(REGISTRY_UUID,
                endpointRegistryEntryNew.getEntryId()))
                .thenReturn(endpointRegistryEntryOld, endpointRegistryEntryNew);

        Response response = registriesApiService.updateRegistryEntry(REGISTRY_UUID, payloadEntryDTO.getId(),
                payloadEntryDTO, null, null, messageContext);

        Mockito.verify(registryProvider).updateEndpointRegistryEntry(Mockito.eq(endpointRegistryEntryOld.getName()),
                Mockito.any(EndpointRegistryEntry.class));
        Assert.assertNotNull("Endpoint Registry Entry creation failed", response);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        RegistryEntryDTO responseEntryDTO = (RegistryEntryDTO) response.getEntity();
        compareRegistryEntryDTOs(payloadEntryDTO, responseEntryDTO);
    }

    @Test
    public void getRegistryEntryByUuid() throws EndpointRegistryException {
        final String REGISTRY_UUID = "reg1";

        EndpointRegistryEntry endpointRegistryEntry = createRegistryEntryWithDefaultParams();
        RegistryEntryDTO registryEntryDTO = EndpointRegistryMappingUtils.fromRegistryEntryToDTO(endpointRegistryEntry);

        Mockito.when(registryProvider.getEndpointRegistryEntryByUUID(REGISTRY_UUID,
                endpointRegistryEntry.getEntryId()))
                .thenReturn(endpointRegistryEntry);

        Response response = registriesApiService.getRegistryEntryByUuid(REGISTRY_UUID,
                endpointRegistryEntry.getEntryId(), messageContext);

        Assert.assertNotNull("Endpoint Registry Entry retrieval failed", response);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        RegistryEntryDTO responseEntryDTO = (RegistryEntryDTO) response.getEntity();
        compareRegistryEntryDTOs(registryEntryDTO, responseEntryDTO);
    }

    @Test
    public void getRegistryEntryByUuid_NonExistingResource() throws EndpointRegistryException {
        final String REGISTRY_UUID = "reg1";
        final String ENTRY_UUID = "entry1";

        Mockito.when(registryProvider.getEndpointRegistryEntryByUUID(REGISTRY_UUID, ENTRY_UUID))
                .thenReturn(null);

        Response response = registriesApiService.getRegistryEntryByUuid(REGISTRY_UUID, ENTRY_UUID, messageContext);
        Assert.assertNull("Endpoint Registry Entry retrieval succeeded for a wrong UUID", response);
    }

    @Test
    public void deleteRegistryEntry() throws EndpointRegistryException {
        final String REGISTRY_UUID = "reg1";

        EndpointRegistryEntry endpointRegistryEntry = createRegistryEntryWithDefaultParams();

        Mockito.when(registryProvider.getEndpointRegistryEntryByUUID(REGISTRY_UUID,
                endpointRegistryEntry.getEntryId()))
                .thenReturn(endpointRegistryEntry);

        Response response = registriesApiService.deleteRegistryEntry(REGISTRY_UUID,
                endpointRegistryEntry.getEntryId(), messageContext);

        Mockito.verify(registryProvider).deleteEndpointRegistryEntry(endpointRegistryEntry.getEntryId());
        Assert.assertNotNull("Endpoint Registry Entry deletion failed", response);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void getAllEntriesInRegistry() throws EndpointRegistryException {
        final String REGISTRY_UUID = "reg1";

        List<EndpointRegistryEntry> endpointRegistryEntryList = new ArrayList<>();

        EndpointRegistryEntry endpointRegistryEntry1 = createRegistryEntry("entry1", "Entry Name 1",
                "v1", "A Registry Entry that exposes a REST endpoint", "https://xyz.com",
                RegistryEntryDTO.ServiceTypeEnum.REST, RegistryEntryDTO.ServiceCategoryEnum.UTILITY,
                "https://petstore.swagger.io/v2/swagger.json", RegistryEntryDTO.DefinitionTypeEnum.OAS,
                null);
        endpointRegistryEntryList.add(endpointRegistryEntry1);

        EndpointRegistryEntry endpointRegistryEntry2 = createRegistryEntry("entry2", "Entry Name 2",
                "v1", "A Registry Entry that exposes a REST endpoint", "https://xyz2.com",
                RegistryEntryDTO.ServiceTypeEnum.REST, RegistryEntryDTO.ServiceCategoryEnum.DOMAIN,
                "https://petstore.swagger.io/v2/swagger.json", RegistryEntryDTO.DefinitionTypeEnum.OAS,
                null);
        endpointRegistryEntryList.add(endpointRegistryEntry2);

        Mockito.when(registryProvider.getEndpointRegistryEntries(EndpointRegistryConstants.COLUMN_ID,
                RestApiConstants.DEFAULT_SORT_ORDER, RestApiConstants.PAGINATION_LIMIT_DEFAULT,
                RestApiConstants.PAGINATION_OFFSET_DEFAULT, REGISTRY_UUID, StringUtils.EMPTY, StringUtils.EMPTY,
                StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, false))
                .thenReturn(endpointRegistryEntryList);

        Response response = registriesApiService.getAllEntriesInRegistry(REGISTRY_UUID, null, null,
                null, null, null, null, null, null,
                null, null, messageContext);
        Assert.assertNotNull("Endpoint registries retrieval failed", response);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        RegistryEntryArrayDTO registryEntryArrayDTO = (RegistryEntryArrayDTO) response.getEntity();

        for (int i = 0; i < registryEntryArrayDTO.size(); i++) {
            compareRegistryEntryDTOs(
                    EndpointRegistryMappingUtils.fromRegistryEntryToDTO(endpointRegistryEntryList.get(i)),
                    registryEntryArrayDTO.get(i));
        }
    }

    @Test
    public void getEndpointDefinitionForOAS() throws EndpointRegistryException {
        final String REGISTRY_UUID = "reg1";

        EndpointRegistryEntry endpointRegistryEntry = createRegistryEntryWithDefaultParams();
        endpointRegistryEntry.setDefinitionType(RegistryEntryDTO.DefinitionTypeEnum.OAS.toString());

        InputStream definitionFileInputStream = Mockito.mock(InputStream.class);
        endpointRegistryEntry.setEndpointDefinition(definitionFileInputStream);

        Mockito.when(registryProvider.getEndpointRegistryEntryByUUID(REGISTRY_UUID,
                endpointRegistryEntry.getEntryId()))
                .thenReturn(endpointRegistryEntry);

        Response response =
                registriesApiService.getEndpointDefinition(REGISTRY_UUID, endpointRegistryEntry.getEntryId(),
                        messageContext);

        Assert.assertNotNull("Endpoint definition retrieval failed", response);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assert.assertEquals(endpointRegistryEntry.getEndpointDefinition(), response.getEntity());
        Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
    }

    @Test
    public void getEndpointDefinitionForWSDL1() throws EndpointRegistryException {
        final String REGISTRY_UUID = "reg1";

        EndpointRegistryEntry endpointRegistryEntry = createRegistryEntryWithDefaultParams();
        endpointRegistryEntry.setDefinitionType(RegistryEntryDTO.DefinitionTypeEnum.WSDL1.toString());

        InputStream definitionFileInputStream = Mockito.mock(InputStream.class);
        endpointRegistryEntry.setEndpointDefinition(definitionFileInputStream);

        Mockito.when(registryProvider.getEndpointRegistryEntryByUUID(REGISTRY_UUID,
                endpointRegistryEntry.getEntryId()))
                .thenReturn(endpointRegistryEntry);

        Response response =
                registriesApiService.getEndpointDefinition(REGISTRY_UUID, endpointRegistryEntry.getEntryId(),
                        messageContext);

        Assert.assertNotNull("Endpoint definition retrieval failed", response);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assert.assertEquals(endpointRegistryEntry.getEndpointDefinition(), response.getEntity());
        Assert.assertEquals(MediaType.TEXT_XML_TYPE, response.getMediaType());
    }

    @Test
    public void getEndpointDefinitionForWSDL2() throws EndpointRegistryException {
        final String REGISTRY_UUID = "reg1";

        EndpointRegistryEntry endpointRegistryEntry = createRegistryEntryWithDefaultParams();
        endpointRegistryEntry.setDefinitionType(RegistryEntryDTO.DefinitionTypeEnum.WSDL2.toString());

        InputStream definitionFileInputStream = Mockito.mock(InputStream.class);
        endpointRegistryEntry.setEndpointDefinition(definitionFileInputStream);

        Mockito.when(registryProvider.getEndpointRegistryEntryByUUID(REGISTRY_UUID,
                endpointRegistryEntry.getEntryId()))
                .thenReturn(endpointRegistryEntry);

        Response response =
                registriesApiService.getEndpointDefinition(REGISTRY_UUID, endpointRegistryEntry.getEntryId(),
                        messageContext);

        Assert.assertNotNull("Endpoint definition retrieval failed", response);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assert.assertEquals(endpointRegistryEntry.getEndpointDefinition(), response.getEntity());
        Assert.assertEquals(MediaType.TEXT_XML_TYPE, response.getMediaType());
    }

    @Test
    public void getEndpointDefinitionForGraphQL() throws EndpointRegistryException {
        final String REGISTRY_UUID = "reg1";

        EndpointRegistryEntry endpointRegistryEntry = createRegistryEntryWithDefaultParams();
        endpointRegistryEntry.setDefinitionType(RegistryEntryDTO.DefinitionTypeEnum.GQL_SDL.toString());

        InputStream definitionFileInputStream = Mockito.mock(InputStream.class);
        endpointRegistryEntry.setEndpointDefinition(definitionFileInputStream);

        Mockito.when(registryProvider.getEndpointRegistryEntryByUUID(REGISTRY_UUID,
                endpointRegistryEntry.getEntryId()))
                .thenReturn(endpointRegistryEntry);

        Response response =
                registriesApiService.getEndpointDefinition(REGISTRY_UUID, endpointRegistryEntry.getEntryId(),
                        messageContext);

        Assert.assertNotNull("Endpoint definition retrieval failed", response);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assert.assertEquals(endpointRegistryEntry.getEndpointDefinition(), response.getEntity());
        Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
    }

    @Test
    public void createNewEntryVersion() throws Exception {
        EndpointRegistryInfo endpointRegistryInfo = createRegistryWithDefaultParams();
        final String REGISTRY_UUID = endpointRegistryInfo.getUuid();
        final String NEW_VERSION = "v2";

        EndpointRegistryEntry endpointRegistryEntryOldVersion = createRegistryEntryWithDefaultParams();

        EndpointRegistryEntry endpointRegistryEntryNewVersion = createRegistryEntryWithDefaultParams();
        endpointRegistryEntryNewVersion.setEntryId("abc2");
        endpointRegistryEntryNewVersion.setVersion(NEW_VERSION);

        Mockito.when(registryProvider.getEndpointRegistryByUUID(REGISTRY_UUID, TENANT_DOMAIN))
                .thenReturn(endpointRegistryInfo);

        endpointRegistryEntryOldVersion.setVersion(NEW_VERSION);
        Mockito.when(registryProvider.createNewEntryVersion(endpointRegistryEntryOldVersion.getEntryId(),
                endpointRegistryEntryOldVersion))
                .thenReturn(endpointRegistryEntryNewVersion.getEntryId());

        Mockito.when(registryProvider.getEndpointRegistryEntryByUUID(REGISTRY_UUID,
                endpointRegistryEntryOldVersion.getEntryId()))
                .thenReturn(endpointRegistryEntryOldVersion);
        Mockito.when(registryProvider.getEndpointRegistryEntryByUUID(REGISTRY_UUID,
                endpointRegistryEntryNewVersion.getEntryId()))
                .thenReturn(endpointRegistryEntryNewVersion);

        Response response = registriesApiService.createNewEntryVersion(REGISTRY_UUID,
                endpointRegistryEntryOldVersion.getEntryId(), NEW_VERSION, messageContext);
        Assert.assertNotNull("Endpoint Registry Entry new version creation failed", response);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        RegistryEntryDTO responseEntryDTO = (RegistryEntryDTO) response.getEntity();
        compareRegistryEntryDTOs(EndpointRegistryMappingUtils.fromRegistryEntryToDTO(endpointRegistryEntryNewVersion),
                responseEntryDTO);
    }

    @Test
    public void createNewEntryVersion_withExistingVersion() throws Exception {
        EndpointRegistryInfo endpointRegistryInfo = createRegistryWithDefaultParams();
        final String REGISTRY_UUID = endpointRegistryInfo.getUuid();
        final String NEW_VERSION = "v2";

        EndpointRegistryEntry endpointRegistryEntryOldVersion = createRegistryEntryWithDefaultParams();

        Mockito.when(registryProvider.getEndpointRegistryByUUID(REGISTRY_UUID, TENANT_DOMAIN))
                .thenReturn(endpointRegistryInfo);

        Mockito.when(registryProvider.getEndpointRegistryEntryByUUID(REGISTRY_UUID,
                endpointRegistryEntryOldVersion.getEntryId()))
                .thenReturn(endpointRegistryEntryOldVersion);

        endpointRegistryEntryOldVersion.setVersion(NEW_VERSION);
        EndpointRegistryResourceAlreadyExistsException resourceAlreadyExistsException
                = Mockito.mock(EndpointRegistryResourceAlreadyExistsException.class);
        Mockito.doThrow(resourceAlreadyExistsException).when(registryProvider)
                .createNewEntryVersion(endpointRegistryEntryOldVersion.getEntryId(),
                        endpointRegistryEntryOldVersion);

        Response response = registriesApiService.createNewEntryVersion(REGISTRY_UUID,
                endpointRegistryEntryOldVersion.getEntryId(), NEW_VERSION, messageContext);
        Assert.assertNull("New version Endpoint Registry entry creation succeeded for a existing version",
                response);
    }

    private void compareRegistryDTOs(RegistryDTO expectedDTO, RegistryDTO actualDTO) {
        Assert.assertEquals(expectedDTO.getName(), actualDTO.getName());
        Assert.assertEquals(expectedDTO.getOwner(), actualDTO.getOwner());
        Assert.assertEquals(expectedDTO.getType(), actualDTO.getType());
        Assert.assertEquals(expectedDTO.getId(), actualDTO.getId());
    }

    private void compareRegistryEntryDTOs(RegistryEntryDTO expectedDTO, RegistryEntryDTO actualDTO) {
        Assert.assertEquals(expectedDTO.getId(), actualDTO.getId());
        Assert.assertEquals(expectedDTO.getEntryName(), actualDTO.getEntryName());
        Assert.assertEquals(expectedDTO.getVersion(), actualDTO.getVersion());
        Assert.assertEquals(expectedDTO.getDescription(), actualDTO.getDescription());
        Assert.assertEquals(expectedDTO.getProductionServiceUrl(), actualDTO.getProductionServiceUrl());
        Assert.assertEquals(expectedDTO.getServiceType(), actualDTO.getServiceType());
        Assert.assertEquals(expectedDTO.getServiceCategory(), actualDTO.getServiceCategory());
        Assert.assertEquals(expectedDTO.getDefinitionUrl(), actualDTO.getDefinitionUrl());
        Assert.assertEquals(expectedDTO.getDefinitionType(), actualDTO.getDefinitionType());
    }

    private EndpointRegistryInfo createRegistryWithDefaultParams() {

        EndpointRegistryInfo endpointRegistryInfo = new EndpointRegistryInfo();
        endpointRegistryInfo.setUuid("abc1");
        endpointRegistryInfo.setRegistryId(1);
        endpointRegistryInfo.setName("Endpoint Registry 1");
        endpointRegistryInfo.setType(RegistryDTO.TypeEnum.WSO2.toString());
        endpointRegistryInfo.setOwner(ADMIN_USERNAME);

        return endpointRegistryInfo;
    }

    private EndpointRegistryInfo createRegistry(String uuid, int id, String name, RegistryDTO.TypeEnum type,
                                                String owner) {
        EndpointRegistryInfo endpointRegistryInfo = new EndpointRegistryInfo();
        endpointRegistryInfo.setUuid(uuid);
        endpointRegistryInfo.setRegistryId(id);
        endpointRegistryInfo.setName(name);
        endpointRegistryInfo.setType(type.toString());
        endpointRegistryInfo.setOwner(owner);

        return endpointRegistryInfo;
    }

    private EndpointRegistryEntry createRegistryEntryWithDefaultParams() {
        EndpointRegistryEntry endpointRegistryEntry = new EndpointRegistryEntry();
        endpointRegistryEntry.setEntryId("entry1");
        endpointRegistryEntry.setName("Entry Name 1");
        endpointRegistryEntry.setDescription("A Registry Entry that exposes a REST endpoint");
        endpointRegistryEntry.setProductionServiceURL("https://xyz.com");
        endpointRegistryEntry.setServiceType(RegistryEntryDTO.ServiceTypeEnum.REST.toString());
        endpointRegistryEntry.setServiceCategory(RegistryEntryDTO.ServiceCategoryEnum.UTILITY.toString());
        endpointRegistryEntry.setDefinitionURL("https://petstore.swagger.io/v2/swagger.json");
        endpointRegistryEntry.setDefinitionType(RegistryEntryDTO.DefinitionTypeEnum.OAS.toString());

        return endpointRegistryEntry;
    }

    private EndpointRegistryEntry createRegistryEntry(String id, String name, String version, String description,
                                                      String serviceUrl, RegistryEntryDTO.ServiceTypeEnum serviceType,
                                                    RegistryEntryDTO.ServiceCategoryEnum serviceCategory,
                                                      String definitionUrl,
                                                      RegistryEntryDTO.DefinitionTypeEnum definitionType,
                                                      InputStream definitionFile) {
        EndpointRegistryEntry endpointRegistryEntry = new EndpointRegistryEntry();
        endpointRegistryEntry.setEntryId(id);
        endpointRegistryEntry.setName(name);
        endpointRegistryEntry.setVersion(version);
        endpointRegistryEntry.setDescription(description);
        endpointRegistryEntry.setProductionServiceURL(serviceUrl);
        endpointRegistryEntry.setServiceType(serviceType.toString());
        endpointRegistryEntry.setServiceCategory(serviceCategory.toString());
        endpointRegistryEntry.setDefinitionURL(definitionUrl);
        endpointRegistryEntry.setDefinitionType(definitionType.toString());
        endpointRegistryEntry.setEndpointDefinition(definitionFile);

        return endpointRegistryEntry;
    }

}
