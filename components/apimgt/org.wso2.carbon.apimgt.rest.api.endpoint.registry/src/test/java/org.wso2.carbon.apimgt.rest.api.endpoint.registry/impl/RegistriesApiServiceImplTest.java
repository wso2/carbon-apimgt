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

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.UnExecutableSchemaGenerator;
import graphql.schema.validation.SchemaValidationError;
import graphql.schema.validation.SchemaValidator;
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
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtResourceAlreadyExistsException;
import org.wso2.carbon.apimgt.api.model.EndpointRegistryEntry;
import org.wso2.carbon.apimgt.api.model.EndpointRegistryInfo;
import org.wso2.carbon.apimgt.impl.EndpointRegistryConstants;
import org.wso2.carbon.apimgt.impl.EndpointRegistryImpl;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIMWSDLReader;
import org.wso2.carbon.apimgt.impl.wsdl.model.WSDLValidationResponse;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor({"org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil"})
@PrepareForTest({RestApiUtil.class, RegistriesApiServiceImpl.class, ServiceReferenceHolder.class,
        MultitenantUtils.class, OASParserUtil.class, APIMWSDLReader.class, UnExecutableSchemaGenerator.class})
public class RegistriesApiServiceImplTest {
    private final String ADMIN_USERNAME = "admin";
    private final String TENANT_DOMAIN = "carbon.super";
    private final int TENANT_ID = -1234;

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
        Mockito.when(tenantManager.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);

        PowerMockito.mockStatic(MultitenantUtils.class);
        PowerMockito.doReturn(TENANT_DOMAIN)
                .when(MultitenantUtils.class, "getTenantDomain", Mockito.eq(ADMIN_USERNAME));

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.doReturn(ADMIN_USERNAME).when(RestApiUtil.class, "getLoggedInUsername");
        PowerMockito.doReturn(TENANT_DOMAIN).when(RestApiUtil.class, "getLoggedInUserTenantDomain");

        PowerMockito.mockStatic(OASParserUtil.class);
        APIDefinitionValidationResponse oasValidationResponse =
                Mockito.mock(APIDefinitionValidationResponse.class);
        Mockito.when(oasValidationResponse.isValid()).thenReturn(true);
        PowerMockito.doReturn(oasValidationResponse).when(OASParserUtil.class, "validateAPIDefinition",
                Mockito.anyString(), Mockito.anyBoolean());

        PowerMockito.mockStatic(APIMWSDLReader.class);
        WSDLValidationResponse wsdlValidationResponse = Mockito.mock(WSDLValidationResponse.class);
        Mockito.when(wsdlValidationResponse.isValid()).thenReturn(true);
        PowerMockito.when(APIMWSDLReader.class, "validateWSDLFile", Mockito.any(InputStream.class))
                .thenReturn(wsdlValidationResponse);
        PowerMockito.when(APIMWSDLReader.class, "validateWSDLUrl", Mockito.any(URL.class))
                .thenReturn(wsdlValidationResponse);


        TypeDefinitionRegistry typeRegistry = Mockito.mock(TypeDefinitionRegistry.class);
        SchemaParser schemaParser = Mockito.mock(SchemaParser.class);
        Mockito.when(schemaParser.parse(Mockito.anyString())).thenReturn(typeRegistry);
        PowerMockito.whenNew(SchemaParser.class).withNoArguments().thenReturn(schemaParser);
        PowerMockito.mockStatic(UnExecutableSchemaGenerator.class);
        GraphQLSchema graphQLSchema = Mockito.mock(GraphQLSchema.class);
        PowerMockito.when(UnExecutableSchemaGenerator.class, "makeUnExecutableSchema",
                Mockito.eq(typeRegistry)).thenReturn(graphQLSchema);
        SchemaValidator schemaValidator = Mockito.mock(SchemaValidator.class);
        PowerMockito.whenNew(SchemaValidator.class).withNoArguments().thenReturn(schemaValidator);
        Set<SchemaValidationError> validationErrors = new HashSet<>();
        Mockito.when(schemaValidator.validateSchema(graphQLSchema)).thenReturn(validationErrors);

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
    public void getRegistryByUUID_NonExistingUUID() throws APIManagementException {
        final String REGISTRY_UUID = "abc-1";

        Mockito.when(registryProvider.getEndpointRegistryByUUID(REGISTRY_UUID, TENANT_DOMAIN)).thenReturn(null);

        Response response = registriesApiService.getRegistryByUUID(REGISTRY_UUID, messageContext);

        Assert.assertNull("Endpoint Registry retrieval succeded for a wrong UUID ", response);
    }

    @Test
    public void getRegistries() throws APIManagementException {
        List<EndpointRegistryInfo> endpointRegistryInfoList = new ArrayList<>();

        EndpointRegistryInfo endpointRegistryInfo1 = createRegistry("abc1", 1, "Endpoint Registry 1",
                RegistryDTO.ModeEnum.READONLY, RegistryDTO.TypeEnum.WSO2, ADMIN_USERNAME);

        EndpointRegistryInfo endpointRegistryInfo2 = createRegistry("abc2", 2, "Endpoint Registry 2",
                RegistryDTO.ModeEnum.READWRITE, RegistryDTO.TypeEnum.ETCD, ADMIN_USERNAME);

        Mockito.when(registryProvider.getEndpointRegistries(EndpointRegistryConstants.COLUMN_ID,
                RestApiConstants.DEFAULT_SORT_ORDER, RestApiConstants.PAGINATION_LIMIT_DEFAULT,
                RestApiConstants.PAGINATION_OFFSET_DEFAULT, TENANT_DOMAIN))
                .thenReturn(endpointRegistryInfoList);

        Response response = registriesApiService.getRegistries(null, null, null,
                null, null, messageContext);

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
    public void addRegistry() throws APIManagementException {
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
    public void addRegistry_ResourceNameExists() throws APIManagementException {
        EndpointRegistryInfo endpointRegistryInfo = createRegistryWithDefaultParams();
        RegistryDTO payloadDTO = EndpointRegistryMappingUtils.fromEndpointRegistryToDTO(endpointRegistryInfo);

        APIMgtResourceAlreadyExistsException apiMgtResourceAlreadyExistsException
                = Mockito.mock(APIMgtResourceAlreadyExistsException.class);
        Mockito.when(registryProvider.addEndpointRegistry(Mockito.any(EndpointRegistryInfo.class)))
                .thenThrow(apiMgtResourceAlreadyExistsException);

        Response response = registriesApiService.addRegistry(payloadDTO, messageContext);

        Assert.assertNull("Endpoint Registry creation succeeded for a existing name", response);
    }

    @Test
    public void updateRegistry() throws APIManagementException {
        EndpointRegistryInfo endpointRegistryInfoOld = createRegistryWithDefaultParams();
        EndpointRegistryInfo endpointRegistryInfoNew = createRegistry(endpointRegistryInfoOld.getUuid(), 2,
                "Endpoint Registry 2", RegistryDTO.ModeEnum.READWRITE, RegistryDTO.TypeEnum.ETCD,
                "user1");

        RegistryDTO payloadDTO = EndpointRegistryMappingUtils.fromEndpointRegistryToDTO(endpointRegistryInfoNew);

        Mockito.when(registryProvider.getEndpointRegistryByUUID(payloadDTO.getId(), TENANT_DOMAIN))
                .thenReturn(endpointRegistryInfoOld, endpointRegistryInfoNew);

        Response response = registriesApiService.updateRegistry(payloadDTO.getId(), payloadDTO, messageContext);

        Mockito.verify(registryProvider)
                .updateEndpointRegistry(Mockito.eq(payloadDTO.getId()), Mockito.eq(endpointRegistryInfoOld.getName()),
                        Mockito.any(EndpointRegistryInfo.class));
        Assert.assertNotNull("Endpoint Registry update failed", response);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        RegistryDTO responseDTO = (RegistryDTO) response.getEntity();
        compareRegistryDTOs(payloadDTO, responseDTO);
    }

    @Test
    public void updateRegistry_existingName() throws APIManagementException {
        EndpointRegistryInfo endpointRegistryInfoOld = createRegistryWithDefaultParams();
        EndpointRegistryInfo endpointRegistryInfoNew = createRegistry(endpointRegistryInfoOld.getUuid(), 2,
                "Endpoint Registry 2", RegistryDTO.ModeEnum.READWRITE, RegistryDTO.TypeEnum.ETCD,
                "user1");

        RegistryDTO payloadDTO = EndpointRegistryMappingUtils.fromEndpointRegistryToDTO(endpointRegistryInfoNew);

        APIMgtResourceAlreadyExistsException apiMgtResourceAlreadyExistsException
                = Mockito.mock(APIMgtResourceAlreadyExistsException.class);
        Mockito.doThrow(apiMgtResourceAlreadyExistsException).when(registryProvider)
                .updateEndpointRegistry(Mockito.eq(endpointRegistryInfoOld.getUuid()),
                        Mockito.eq(endpointRegistryInfoOld.getName()),
                        Mockito.any(EndpointRegistryInfo.class));
        Mockito.when(registryProvider.getEndpointRegistryByUUID(payloadDTO.getId(), TENANT_DOMAIN))
                .thenReturn(endpointRegistryInfoOld, endpointRegistryInfoNew);

        Response response = registriesApiService.updateRegistry(payloadDTO.getId(), payloadDTO, messageContext);

        Assert.assertNull("Endpoint Registry update failed", response);
    }

    @Test
    public void deleteRegistry() throws APIManagementException {
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
    public void createRegistryEntry_validWSDL1() throws APIManagementException {
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
    public void createRegistryEntry_validWSDL2() throws APIManagementException {
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
    public void createRegistryEntry_validGraphQL() throws APIManagementException {
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
    public void createRegistryEntry_validOASUrl() throws APIManagementException {
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
    public void createRegistryEntryWithNoDefinitionFileAndUrl() throws APIManagementException {
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
    public void updateRegistryEntryWithDefinitionFile() throws APIManagementException {
        EndpointRegistryInfo endpointRegistryInfo = createRegistryWithDefaultParams();
        final String REGISTRY_UUID = endpointRegistryInfo.getUuid();

        EndpointRegistryEntry endpointRegistryEntryOld = createRegistryEntryWithDefaultParams();

        InputStream definitionFileStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("oasV2-sample.yaml");
        Attachment definitionFileDetail = Mockito.mock(Attachment.class);

        EndpointRegistryEntry endpointRegistryEntryNew = createRegistryEntry(endpointRegistryEntryOld.getEntryId(),
                "Entry Name 2", "{mutualTLS: false}", "https://xyz2.com",
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
    public void updateRegistryEntryWithDefinitionUrl() throws APIManagementException {
        EndpointRegistryInfo endpointRegistryInfo = createRegistryWithDefaultParams();
        final String REGISTRY_UUID = endpointRegistryInfo.getUuid();

        EndpointRegistryEntry endpointRegistryEntryOld = createRegistryEntryWithDefaultParams();

        EndpointRegistryEntry endpointRegistryEntryNew = createRegistryEntry(endpointRegistryEntryOld.getEntryId(),
                "Entry Name 2", "{mutualTLS: false}", "https://xyz2.com",
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
    public void updateRegistryEntryWithNoDefinitionFileAndUrl() throws APIManagementException {
        EndpointRegistryInfo endpointRegistryInfo = createRegistryWithDefaultParams();
        final String REGISTRY_UUID = endpointRegistryInfo.getUuid();

        EndpointRegistryEntry endpointRegistryEntryOld = createRegistryEntryWithDefaultParams();

        EndpointRegistryEntry endpointRegistryEntryNew = createRegistryEntry(endpointRegistryEntryOld.getEntryId(),
                "Entry Name 2", "{mutualTLS: false}", "https://xyz2.com",
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
    public void getRegistryEntryByUuid() throws APIManagementException {
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
    public void getRegistryEntryByUuid_NonExistingResource() throws APIManagementException {
        final String REGISTRY_UUID = "reg1";
        final String ENTRY_UUID = "entry1";

        Mockito.when(registryProvider.getEndpointRegistryEntryByUUID(REGISTRY_UUID, ENTRY_UUID))
                .thenReturn(null);

        Response response = registriesApiService.getRegistryEntryByUuid(REGISTRY_UUID, ENTRY_UUID, messageContext);
        Assert.assertNull("Endpoint Registry Entry retrieval succeeded for a wrong UUID", response);
    }

    @Test
    public void deleteRegistryEntry() throws APIManagementException {
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
    public void getAllEntriesInRegistry() throws APIManagementException {
        final String REGISTRY_UUID = "reg1";

        List<EndpointRegistryEntry> endpointRegistryEntryList = new ArrayList<>();

        EndpointRegistryEntry endpointRegistryEntry1 = createRegistryEntry("entry1", "Entry Name 1",
                "{mutualTLS: true}", "https://xyz.com",
                RegistryEntryDTO.ServiceTypeEnum.REST, RegistryEntryDTO.ServiceCategoryEnum.UTILITY,
                "https://petstore.swagger.io/v2/swagger.json", RegistryEntryDTO.DefinitionTypeEnum.OAS,
                null);
        endpointRegistryEntryList.add(endpointRegistryEntry1);

        EndpointRegistryEntry endpointRegistryEntry2 = createRegistryEntry("entry2", "Entry Name 2",
                "{mutualTLS: false}", "https://xyz2.com",
                RegistryEntryDTO.ServiceTypeEnum.REST, RegistryEntryDTO.ServiceCategoryEnum.DOMAIN,
                "https://petstore.swagger.io/v2/swagger.json", RegistryEntryDTO.DefinitionTypeEnum.OAS,
                null);
        endpointRegistryEntryList.add(endpointRegistryEntry2);

        Mockito.when(registryProvider.getEndpointRegistryEntries(EndpointRegistryConstants.COLUMN_ID,
                RestApiConstants.DEFAULT_SORT_ORDER, RestApiConstants.PAGINATION_LIMIT_DEFAULT,
                RestApiConstants.PAGINATION_OFFSET_DEFAULT, REGISTRY_UUID, StringUtils.EMPTY, StringUtils.EMPTY,
                StringUtils.EMPTY, StringUtils.EMPTY))
                .thenReturn(endpointRegistryEntryList);

        Response response = registriesApiService.getAllEntriesInRegistry(REGISTRY_UUID, null, null,
                null, null, null, null, null, null, messageContext);
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
    public void getEndpointDefinitionForOAS() throws APIManagementException {
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
    public void getEndpointDefinitionForWSDL1() throws APIManagementException {
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
    public void getEndpointDefinitionForWSDL2() throws APIManagementException {
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
    public void getEndpointDefinitionForGraphQL() throws APIManagementException {
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

    private void compareRegistryDTOs(RegistryDTO expectedDTO, RegistryDTO actualDTO) {
        Assert.assertEquals(expectedDTO.getName(), actualDTO.getName());
        Assert.assertEquals(expectedDTO.getMode(), actualDTO.getMode());
        Assert.assertEquals(expectedDTO.getOwner(), actualDTO.getOwner());
        Assert.assertEquals(expectedDTO.getType(), actualDTO.getType());
        Assert.assertEquals(expectedDTO.getId(), actualDTO.getId());
    }

    private void compareRegistryEntryDTOs(RegistryEntryDTO expectedDTO, RegistryEntryDTO actualDTO) {
        Assert.assertEquals(expectedDTO.getId(), actualDTO.getId());
        Assert.assertEquals(expectedDTO.getEntryName(), actualDTO.getEntryName());
        Assert.assertEquals(expectedDTO.getMetadata(), actualDTO.getMetadata());
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
        endpointRegistryInfo.setMode(RegistryDTO.ModeEnum.READONLY.toString());
        endpointRegistryInfo.setType(RegistryDTO.TypeEnum.WSO2.toString());
        endpointRegistryInfo.setOwner(ADMIN_USERNAME);

        return endpointRegistryInfo;
    }

    private EndpointRegistryInfo createRegistry(String uuid, int id, String name, RegistryDTO.ModeEnum mode,
                                                RegistryDTO.TypeEnum type, String owner) {
        EndpointRegistryInfo endpointRegistryInfo = new EndpointRegistryInfo();
        endpointRegistryInfo.setUuid(uuid);
        endpointRegistryInfo.setRegistryId(id);
        endpointRegistryInfo.setName(name);
        endpointRegistryInfo.setMode(mode.toString());
        endpointRegistryInfo.setType(type.toString());
        endpointRegistryInfo.setOwner(owner);

        return endpointRegistryInfo;
    }

    private EndpointRegistryEntry createRegistryEntryWithDefaultParams() {
        EndpointRegistryEntry endpointRegistryEntry = new EndpointRegistryEntry();
        endpointRegistryEntry.setEntryId("entry1");
        endpointRegistryEntry.setName("Entry Name 1");
        endpointRegistryEntry.setMetaData("{mutualTLS: true}");
        endpointRegistryEntry.setProductionServiceURL("https://xyz.com");
        endpointRegistryEntry.setServiceType(RegistryEntryDTO.ServiceTypeEnum.REST.toString());
        endpointRegistryEntry.setServiceCategory(RegistryEntryDTO.ServiceCategoryEnum.UTILITY.toString());
        endpointRegistryEntry.setDefinitionURL("https://petstore.swagger.io/v2/swagger.json");
        endpointRegistryEntry.setDefinitionType(RegistryEntryDTO.DefinitionTypeEnum.OAS.toString());

        return endpointRegistryEntry;
    }

    private EndpointRegistryEntry createRegistryEntry(String id, String name, String metadata, String serviceUrl,
                                                    RegistryEntryDTO.ServiceTypeEnum serviceType,
                                                    RegistryEntryDTO.ServiceCategoryEnum serviceCategory,
                                                      String definitionUrl,
                                                      RegistryEntryDTO.DefinitionTypeEnum definitionType,
                                                      InputStream definitionFile) {
        EndpointRegistryEntry endpointRegistryEntry = new EndpointRegistryEntry();
        endpointRegistryEntry.setEntryId(id);
        endpointRegistryEntry.setName(name);
        endpointRegistryEntry.setMetaData(metadata);
        endpointRegistryEntry.setProductionServiceURL(serviceUrl);
        endpointRegistryEntry.setServiceType(serviceType.toString());
        endpointRegistryEntry.setServiceCategory(serviceCategory.toString());
        endpointRegistryEntry.setDefinitionURL(definitionUrl);
        endpointRegistryEntry.setDefinitionType(definitionType.toString());
        endpointRegistryEntry.setEndpointDefinition(definitionFile);

        return endpointRegistryEntry;
    }

}
