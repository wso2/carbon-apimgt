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
        final String REGISTRY_UUID = "abc1";

        EndpointRegistryInfo endpointRegistryInfo = new EndpointRegistryInfo();
        endpointRegistryInfo.setName("Endpoint Registry 1");
        endpointRegistryInfo.setMode(RegistryDTO.ModeEnum.READONLY.toString());
        endpointRegistryInfo.setOwner("admin");
        endpointRegistryInfo.setRegistryId(1);
        endpointRegistryInfo.setType(RegistryDTO.TypeEnum.WSO2.toString());
        endpointRegistryInfo.setUuid(REGISTRY_UUID);

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

        EndpointRegistryInfo endpointRegistryInfo1 = new EndpointRegistryInfo();
        endpointRegistryInfo1.setName("Endpoint Registry 1");
        endpointRegistryInfo1.setMode(RegistryDTO.ModeEnum.READONLY.toString());
        endpointRegistryInfo1.setOwner("admin");
        endpointRegistryInfo1.setRegistryId(1);
        endpointRegistryInfo1.setType(RegistryDTO.TypeEnum.WSO2.toString());
        endpointRegistryInfo1.setUuid("abc1");
        endpointRegistryInfoList.add(endpointRegistryInfo1);

        EndpointRegistryInfo endpointRegistryInfo2 = new EndpointRegistryInfo();
        endpointRegistryInfo2.setName("Endpoint Registry 2");
        endpointRegistryInfo2.setMode(RegistryDTO.ModeEnum.READONLY.toString());
        endpointRegistryInfo2.setOwner("admin");
        endpointRegistryInfo2.setRegistryId(1);
        endpointRegistryInfo2.setType(RegistryDTO.TypeEnum.WSO2.toString());
        endpointRegistryInfo2.setUuid("abc2");
        endpointRegistryInfoList.add(endpointRegistryInfo2);

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
        RegistryDTO payloadDTO = new RegistryDTO();
        payloadDTO.setName("Endpoint Registry 1");
        payloadDTO.setMode(RegistryDTO.ModeEnum.READONLY);
        payloadDTO.setType(RegistryDTO.TypeEnum.WSO2);
        payloadDTO.setOwner(ADMIN_USERNAME);
        payloadDTO.setId("abc1");

        EndpointRegistryInfo endpointRegistryInfo =
                EndpointRegistryMappingUtils.fromDTOtoEndpointRegistry(payloadDTO, ADMIN_USERNAME);
        endpointRegistryInfo.setUuid("abc1");

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
        RegistryDTO payloadDTO = new RegistryDTO();
        payloadDTO.setName("Endpoint Registry 1");
        payloadDTO.setMode(RegistryDTO.ModeEnum.READONLY);
        payloadDTO.setType(RegistryDTO.TypeEnum.WSO2);
        payloadDTO.setOwner(ADMIN_USERNAME);
        payloadDTO.setId("abc1");

        EndpointRegistryInfo endpointRegistryInfo =
                EndpointRegistryMappingUtils.fromDTOtoEndpointRegistry(payloadDTO, ADMIN_USERNAME);
        endpointRegistryInfo.setUuid("abc1");

        APIMgtResourceAlreadyExistsException apiMgtResourceAlreadyExistsException
                = Mockito.mock(APIMgtResourceAlreadyExistsException.class);
        Mockito.when(registryProvider.addEndpointRegistry(Mockito.any(EndpointRegistryInfo.class)))
                .thenThrow(apiMgtResourceAlreadyExistsException);

        Response response = registriesApiService.addRegistry(payloadDTO, messageContext);

        Assert.assertNull("Endpoint Registry creation succeeded for a existing name", response);
    }

    @Test
    public void updateRegistry() throws APIManagementException {
        EndpointRegistryInfo endpointRegistryInfoOld = new EndpointRegistryInfo();
        endpointRegistryInfoOld.setName("Endpoint Registry 1");
        endpointRegistryInfoOld.setUuid("abc1");
        endpointRegistryInfoOld.setMode(RegistryDTO.ModeEnum.READWRITE.toString());
        endpointRegistryInfoOld.setType(RegistryDTO.TypeEnum.ETCD.toString());
        endpointRegistryInfoOld.setOwner(ADMIN_USERNAME);

        RegistryDTO payloadDTO = new RegistryDTO();
        payloadDTO.setName("Endpoint Registry 2");
        payloadDTO.setId("abc1");
        payloadDTO.setMode(RegistryDTO.ModeEnum.READONLY);
        payloadDTO.setType(RegistryDTO.TypeEnum.WSO2);
        payloadDTO.setOwner(ADMIN_USERNAME);

        EndpointRegistryInfo endpointRegistryInfoNew =
                EndpointRegistryMappingUtils.fromDTOtoEndpointRegistry(payloadDTO, ADMIN_USERNAME);
        endpointRegistryInfoNew.setUuid("abc1");

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
        EndpointRegistryInfo endpointRegistryInfoOld = new EndpointRegistryInfo();
        endpointRegistryInfoOld.setName("Endpoint Registry 1");
        endpointRegistryInfoOld.setUuid("abc1");
        endpointRegistryInfoOld.setMode(RegistryDTO.ModeEnum.READWRITE.toString());
        endpointRegistryInfoOld.setType(RegistryDTO.TypeEnum.ETCD.toString());
        endpointRegistryInfoOld.setOwner(ADMIN_USERNAME);

        RegistryDTO payloadDTO = new RegistryDTO();
        payloadDTO.setName("Endpoint Registry 2");
        payloadDTO.setId("abc1");
        payloadDTO.setMode(RegistryDTO.ModeEnum.READONLY);
        payloadDTO.setType(RegistryDTO.TypeEnum.WSO2);
        payloadDTO.setOwner(ADMIN_USERNAME);

        EndpointRegistryInfo endpointRegistryInfoNew =
                EndpointRegistryMappingUtils.fromDTOtoEndpointRegistry(payloadDTO, ADMIN_USERNAME);
        endpointRegistryInfoNew.setUuid("abc1");

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
        final String REGISTRY_UUID = "abc1";

        EndpointRegistryInfo endpointRegistryInfo = new EndpointRegistryInfo();
        endpointRegistryInfo.setName("Endpoint Registry 1");
        endpointRegistryInfo.setMode(RegistryDTO.ModeEnum.READONLY.toString());
        endpointRegistryInfo.setOwner("admin");
        endpointRegistryInfo.setRegistryId(1);
        endpointRegistryInfo.setType(RegistryDTO.TypeEnum.WSO2.toString());
        endpointRegistryInfo.setUuid(REGISTRY_UUID);

        Mockito.when(registryProvider.getEndpointRegistryByUUID(REGISTRY_UUID, TENANT_DOMAIN))
                .thenReturn(endpointRegistryInfo);

        Response response = registriesApiService.deleteRegistry(REGISTRY_UUID, messageContext);

        Mockito.verify(registryProvider).deleteEndpointRegistry(REGISTRY_UUID);
        Assert.assertNotNull("Endpoint Registry delete failed", response);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void createRegistryEntry_validOASV2Yaml() throws Exception {
        final String REGISTRY_UUID = "reg1";

        EndpointRegistryInfo endpointRegistryInfo = new EndpointRegistryInfo();
        endpointRegistryInfo.setName("Endpoint Registry 1");
        endpointRegistryInfo.setMode(RegistryDTO.ModeEnum.READONLY.toString());
        endpointRegistryInfo.setOwner("admin");
        endpointRegistryInfo.setRegistryId(1);
        endpointRegistryInfo.setType(RegistryDTO.TypeEnum.WSO2.toString());
        endpointRegistryInfo.setUuid(REGISTRY_UUID);

        RegistryEntryDTO payloadEntryDTO = new RegistryEntryDTO();
        payloadEntryDTO.setId("entry1");
        payloadEntryDTO.setEntryName("Entry Name 1");
        payloadEntryDTO.setMetadata("{mutualTLS: true}");
        payloadEntryDTO.setServiceUrl("https://xyz.com");
        payloadEntryDTO.setServiceType(RegistryEntryDTO.ServiceTypeEnum.REST);
        payloadEntryDTO.setServiceCategory(RegistryEntryDTO.ServiceCategoryEnum.UTILITY);
        payloadEntryDTO.setDefinitionUrl("https://petstore.swagger.io/v2/swagger.json");
        payloadEntryDTO.setDefinitionType(RegistryEntryDTO.DefinitionTypeEnum.OAS);

        InputStream definitionFileStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("oasV2-sample.yaml");
        Attachment definitionFileDetail = Mockito.mock(Attachment.class);

        EndpointRegistryEntry endpointRegistryEntry =
                EndpointRegistryMappingUtils.fromDTOToRegistryEntry(payloadEntryDTO, payloadEntryDTO.getId(),
                        definitionFileStream, endpointRegistryInfo.getRegistryId());

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
        final String REGISTRY_UUID = "reg1";

        EndpointRegistryInfo endpointRegistryInfo = new EndpointRegistryInfo();
        endpointRegistryInfo.setName("Endpoint Registry 1");
        endpointRegistryInfo.setMode(RegistryDTO.ModeEnum.READONLY.toString());
        endpointRegistryInfo.setOwner("admin");
        endpointRegistryInfo.setRegistryId(1);
        endpointRegistryInfo.setType(RegistryDTO.TypeEnum.WSO2.toString());
        endpointRegistryInfo.setUuid(REGISTRY_UUID);

        RegistryEntryDTO payloadEntryDTO = new RegistryEntryDTO();
        payloadEntryDTO.setId("entry1");
        payloadEntryDTO.setEntryName("Entry Name 1");
        payloadEntryDTO.setMetadata("{mutualTLS: true}");
        payloadEntryDTO.setServiceUrl("https://xyz.com");
        payloadEntryDTO.setServiceType(RegistryEntryDTO.ServiceTypeEnum.REST);
        payloadEntryDTO.setServiceCategory(RegistryEntryDTO.ServiceCategoryEnum.UTILITY);
        payloadEntryDTO.setDefinitionUrl("https://petstore.swagger.io/v2/swagger.json");
        payloadEntryDTO.setDefinitionType(RegistryEntryDTO.DefinitionTypeEnum.OAS);

        InputStream definitionFileStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("oasV2-sample.json");
        Attachment definitionFileDetail = Mockito.mock(Attachment.class);

        EndpointRegistryEntry endpointRegistryEntry =
                EndpointRegistryMappingUtils.fromDTOToRegistryEntry(payloadEntryDTO, payloadEntryDTO.getId(),
                        definitionFileStream, endpointRegistryInfo.getRegistryId());

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
        final String REGISTRY_UUID = "reg1";

        EndpointRegistryInfo endpointRegistryInfo = new EndpointRegistryInfo();
        endpointRegistryInfo.setName("Endpoint Registry 1");
        endpointRegistryInfo.setMode(RegistryDTO.ModeEnum.READONLY.toString());
        endpointRegistryInfo.setOwner("admin");
        endpointRegistryInfo.setRegistryId(1);
        endpointRegistryInfo.setType(RegistryDTO.TypeEnum.WSO2.toString());
        endpointRegistryInfo.setUuid(REGISTRY_UUID);

        RegistryEntryDTO payloadEntryDTO = new RegistryEntryDTO();
        payloadEntryDTO.setId("entry1");
        payloadEntryDTO.setEntryName("Entry Name 1");
        payloadEntryDTO.setMetadata("{mutualTLS: true}");
        payloadEntryDTO.setServiceUrl("https://xyz.com");
        payloadEntryDTO.setServiceType(RegistryEntryDTO.ServiceTypeEnum.REST);
        payloadEntryDTO.setServiceCategory(RegistryEntryDTO.ServiceCategoryEnum.UTILITY);
        payloadEntryDTO.setDefinitionUrl("https://petstore.swagger.io/v2/swagger.json");
        payloadEntryDTO.setDefinitionType(RegistryEntryDTO.DefinitionTypeEnum.OAS);

        InputStream definitionFileStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("oasV3-sample.json");
        Attachment definitionFileDetail = Mockito.mock(Attachment.class);

        EndpointRegistryEntry endpointRegistryEntry =
                EndpointRegistryMappingUtils.fromDTOToRegistryEntry(payloadEntryDTO, payloadEntryDTO.getId(),
                        definitionFileStream, endpointRegistryInfo.getRegistryId());

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
        final String REGISTRY_UUID = "reg1";

        EndpointRegistryInfo endpointRegistryInfo = new EndpointRegistryInfo();
        endpointRegistryInfo.setName("Endpoint Registry 1");
        endpointRegistryInfo.setMode(RegistryDTO.ModeEnum.READONLY.toString());
        endpointRegistryInfo.setOwner("admin");
        endpointRegistryInfo.setRegistryId(1);
        endpointRegistryInfo.setType(RegistryDTO.TypeEnum.WSO2.toString());
        endpointRegistryInfo.setUuid(REGISTRY_UUID);

        RegistryEntryDTO payloadEntryDTO = new RegistryEntryDTO();
        payloadEntryDTO.setId("entry1");
        payloadEntryDTO.setEntryName("Entry Name 1");
        payloadEntryDTO.setMetadata("{mutualTLS: true}");
        payloadEntryDTO.setServiceUrl("https://xyz.com");
        payloadEntryDTO.setServiceType(RegistryEntryDTO.ServiceTypeEnum.REST);
        payloadEntryDTO.setServiceCategory(RegistryEntryDTO.ServiceCategoryEnum.UTILITY);
        payloadEntryDTO.setDefinitionUrl("https://petstore.swagger.io/v2/swagger.json");
        payloadEntryDTO.setDefinitionType(RegistryEntryDTO.DefinitionTypeEnum.OAS);

        InputStream definitionFileStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("oasV3-sample.yaml");
        Attachment definitionFileDetail = Mockito.mock(Attachment.class);

        EndpointRegistryEntry endpointRegistryEntry =
                EndpointRegistryMappingUtils.fromDTOToRegistryEntry(payloadEntryDTO, payloadEntryDTO.getId(),
                        definitionFileStream, endpointRegistryInfo.getRegistryId());

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
        final String REGISTRY_UUID = "reg1";

        EndpointRegistryInfo endpointRegistryInfo = new EndpointRegistryInfo();
        endpointRegistryInfo.setName("Endpoint Registry 1");
        endpointRegistryInfo.setMode(RegistryDTO.ModeEnum.READONLY.toString());
        endpointRegistryInfo.setOwner("admin");
        endpointRegistryInfo.setRegistryId(1);
        endpointRegistryInfo.setType(RegistryDTO.TypeEnum.WSO2.toString());
        endpointRegistryInfo.setUuid(REGISTRY_UUID);

        RegistryEntryDTO payloadEntryDTO = new RegistryEntryDTO();
        payloadEntryDTO.setId("entry1");
        payloadEntryDTO.setEntryName("Entry Name 1");
        payloadEntryDTO.setMetadata("{mutualTLS: true}");
        payloadEntryDTO.setServiceUrl("https://xyz.com");
        payloadEntryDTO.setServiceType(RegistryEntryDTO.ServiceTypeEnum.REST);
        payloadEntryDTO.setServiceCategory(RegistryEntryDTO.ServiceCategoryEnum.UTILITY);
        payloadEntryDTO.setDefinitionUrl("https://petstore.swagger.io/v2/swagger.json");
        payloadEntryDTO.setDefinitionType(RegistryEntryDTO.DefinitionTypeEnum.WSDL1);

        InputStream definitionFileStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("wsdl1-sample.wsdl");
        Attachment definitionFileDetail = Mockito.mock(Attachment.class);

        EndpointRegistryEntry endpointRegistryEntry =
                EndpointRegistryMappingUtils.fromDTOToRegistryEntry(payloadEntryDTO, payloadEntryDTO.getId(),
                        definitionFileStream, endpointRegistryInfo.getRegistryId());

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
        final String REGISTRY_UUID = "reg1";

        EndpointRegistryInfo endpointRegistryInfo = new EndpointRegistryInfo();
        endpointRegistryInfo.setName("Endpoint Registry 1");
        endpointRegistryInfo.setMode(RegistryDTO.ModeEnum.READONLY.toString());
        endpointRegistryInfo.setOwner("admin");
        endpointRegistryInfo.setRegistryId(1);
        endpointRegistryInfo.setType(RegistryDTO.TypeEnum.WSO2.toString());
        endpointRegistryInfo.setUuid(REGISTRY_UUID);

        RegistryEntryDTO payloadEntryDTO = new RegistryEntryDTO();
        payloadEntryDTO.setId("entry1");
        payloadEntryDTO.setEntryName("Entry Name 1");
        payloadEntryDTO.setMetadata("{mutualTLS: true}");
        payloadEntryDTO.setServiceUrl("https://xyz.com");
        payloadEntryDTO.setServiceType(RegistryEntryDTO.ServiceTypeEnum.REST);
        payloadEntryDTO.setServiceCategory(RegistryEntryDTO.ServiceCategoryEnum.UTILITY);
        payloadEntryDTO.setDefinitionUrl("https://petstore.swagger.io/v2/swagger.json");
        payloadEntryDTO.setDefinitionType(RegistryEntryDTO.DefinitionTypeEnum.WSDL2);

        InputStream definitionFileStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("wsdl2-sample.wsdl");
        Attachment definitionFileDetail = Mockito.mock(Attachment.class);

        EndpointRegistryEntry endpointRegistryEntry =
                EndpointRegistryMappingUtils.fromDTOToRegistryEntry(payloadEntryDTO, payloadEntryDTO.getId(),
                        definitionFileStream, endpointRegistryInfo.getRegistryId());

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
        final String REGISTRY_UUID = "reg1";

        EndpointRegistryInfo endpointRegistryInfo = new EndpointRegistryInfo();
        endpointRegistryInfo.setName("Endpoint Registry 1");
        endpointRegistryInfo.setMode(RegistryDTO.ModeEnum.READONLY.toString());
        endpointRegistryInfo.setOwner("admin");
        endpointRegistryInfo.setRegistryId(1);
        endpointRegistryInfo.setType(RegistryDTO.TypeEnum.WSO2.toString());
        endpointRegistryInfo.setUuid(REGISTRY_UUID);

        RegistryEntryDTO payloadEntryDTO = new RegistryEntryDTO();
        payloadEntryDTO.setId("entry1");
        payloadEntryDTO.setEntryName("Entry Name 1");
        payloadEntryDTO.setMetadata("{mutualTLS: true}");
        payloadEntryDTO.setServiceUrl("https://xyz.com");
        payloadEntryDTO.setServiceType(RegistryEntryDTO.ServiceTypeEnum.REST);
        payloadEntryDTO.setServiceCategory(RegistryEntryDTO.ServiceCategoryEnum.UTILITY);
        payloadEntryDTO.setDefinitionUrl("https://petstore.swagger.io/v2/swagger.json");
        payloadEntryDTO.setDefinitionType(RegistryEntryDTO.DefinitionTypeEnum.GQL_SDL);

        InputStream definitionFileStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("graphql-sample.graphql");
        Attachment definitionFileDetail = Mockito.mock(Attachment.class);

        EndpointRegistryEntry endpointRegistryEntry =
                EndpointRegistryMappingUtils.fromDTOToRegistryEntry(payloadEntryDTO, payloadEntryDTO.getId(),
                        definitionFileStream, endpointRegistryInfo.getRegistryId());

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
        final String REGISTRY_UUID = "reg1";

        EndpointRegistryInfo endpointRegistryInfo = new EndpointRegistryInfo();
        endpointRegistryInfo.setName("Endpoint Registry 1");
        endpointRegistryInfo.setMode(RegistryDTO.ModeEnum.READONLY.toString());
        endpointRegistryInfo.setOwner("admin");
        endpointRegistryInfo.setRegistryId(1);
        endpointRegistryInfo.setType(RegistryDTO.TypeEnum.WSO2.toString());
        endpointRegistryInfo.setUuid(REGISTRY_UUID);

        RegistryEntryDTO payloadEntryDTO = new RegistryEntryDTO();
        payloadEntryDTO.setId("entry1");
        payloadEntryDTO.setEntryName("Entry Name 1");
        payloadEntryDTO.setMetadata("{mutualTLS: true}");
        payloadEntryDTO.setServiceUrl("https://xyz.com");
        payloadEntryDTO.setServiceType(RegistryEntryDTO.ServiceTypeEnum.REST);
        payloadEntryDTO.setServiceCategory(RegistryEntryDTO.ServiceCategoryEnum.UTILITY);
        payloadEntryDTO.setDefinitionUrl("https://petstore.swagger.io/v2/swagger.json");
        payloadEntryDTO.setDefinitionType(RegistryEntryDTO.DefinitionTypeEnum.OAS);

        EndpointRegistryEntry endpointRegistryEntry =
                EndpointRegistryMappingUtils.fromDTOToRegistryEntry(payloadEntryDTO, payloadEntryDTO.getId(),
                        null, endpointRegistryInfo.getRegistryId());

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
        final String REGISTRY_UUID = "reg1";

        EndpointRegistryInfo endpointRegistryInfo = new EndpointRegistryInfo();
        endpointRegistryInfo.setName("Endpoint Registry 1");
        endpointRegistryInfo.setMode(RegistryDTO.ModeEnum.READONLY.toString());
        endpointRegistryInfo.setOwner(ADMIN_USERNAME);
        endpointRegistryInfo.setRegistryId(1);
        endpointRegistryInfo.setType(RegistryDTO.TypeEnum.WSO2.toString());
        endpointRegistryInfo.setUuid(REGISTRY_UUID);

        RegistryEntryDTO payloadEntryDTO = new RegistryEntryDTO();
        payloadEntryDTO.setId("entry1");
        payloadEntryDTO.setEntryName("Entry Name 2");
        payloadEntryDTO.setMetadata("{mutualTLS: true}");
        payloadEntryDTO.setServiceUrl("https://xyz2.com");
        payloadEntryDTO.setServiceType(RegistryEntryDTO.ServiceTypeEnum.SOAP_1_1);
        payloadEntryDTO.setServiceCategory(RegistryEntryDTO.ServiceCategoryEnum.DOMAIN);
        payloadEntryDTO.setDefinitionUrl("https://petstore.swagger.io/v2/swagger.json");
        payloadEntryDTO.setDefinitionType(RegistryEntryDTO.DefinitionTypeEnum.OAS);

        InputStream definitionFileStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("oasV2-sample.yaml");
        Attachment definitionFileDetail = Mockito.mock(Attachment.class);

        EndpointRegistryEntry endpointRegistryEntryNew =
                EndpointRegistryMappingUtils.fromDTOToRegistryEntry(payloadEntryDTO, payloadEntryDTO.getId(),
                        definitionFileStream, endpointRegistryInfo.getRegistryId());

        EndpointRegistryEntry endpointRegistryEntryOld = new EndpointRegistryEntry();
        endpointRegistryEntryOld.setEntryId("entry1");
        endpointRegistryEntryOld.setName("Entry Name 1");
        endpointRegistryEntryOld.setMetaData("{mutualTLS: true}");
        endpointRegistryEntryOld.setServiceURL("https://xyz.com");
        endpointRegistryEntryOld.setServiceType(RegistryEntryDTO.ServiceTypeEnum.REST.toString());
        endpointRegistryEntryOld.setServiceCategory(RegistryEntryDTO.ServiceCategoryEnum.UTILITY.toString());
        endpointRegistryEntryOld.setDefinitionURL("https://petstore.swagger.io/v2/swagger.json");
        endpointRegistryEntryOld.setDefinitionType(RegistryEntryDTO.DefinitionTypeEnum.OAS.toString());

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
        final String REGISTRY_UUID = "reg1";

        EndpointRegistryInfo endpointRegistryInfo = new EndpointRegistryInfo();
        endpointRegistryInfo.setName("Endpoint Registry 1");
        endpointRegistryInfo.setMode(RegistryDTO.ModeEnum.READONLY.toString());
        endpointRegistryInfo.setOwner(ADMIN_USERNAME);
        endpointRegistryInfo.setRegistryId(1);
        endpointRegistryInfo.setType(RegistryDTO.TypeEnum.WSO2.toString());
        endpointRegistryInfo.setUuid(REGISTRY_UUID);

        RegistryEntryDTO payloadEntryDTO = new RegistryEntryDTO();
        payloadEntryDTO.setId("entry1");
        payloadEntryDTO.setEntryName("Entry Name 2");
        payloadEntryDTO.setMetadata("{mutualTLS: true}");
        payloadEntryDTO.setServiceUrl("https://xyz2.com");
        payloadEntryDTO.setServiceType(RegistryEntryDTO.ServiceTypeEnum.SOAP_1_1);
        payloadEntryDTO.setServiceCategory(RegistryEntryDTO.ServiceCategoryEnum.DOMAIN);
        payloadEntryDTO.setDefinitionUrl("https://petstore.swagger.io/v2/swagger.json");
        payloadEntryDTO.setDefinitionType(RegistryEntryDTO.DefinitionTypeEnum.OAS);

        EndpointRegistryEntry endpointRegistryEntryNew =
                EndpointRegistryMappingUtils.fromDTOToRegistryEntry(payloadEntryDTO, payloadEntryDTO.getId(),
                        null, endpointRegistryInfo.getRegistryId());

        EndpointRegistryEntry endpointRegistryEntryOld = new EndpointRegistryEntry();
        endpointRegistryEntryOld.setEntryId("entry1");
        endpointRegistryEntryOld.setName("Entry Name 1");
        endpointRegistryEntryOld.setMetaData("{mutualTLS: true}");
        endpointRegistryEntryOld.setServiceURL("https://xyz.com");
        endpointRegistryEntryOld.setServiceType(RegistryEntryDTO.ServiceTypeEnum.REST.toString());
        endpointRegistryEntryOld.setServiceCategory(RegistryEntryDTO.ServiceCategoryEnum.UTILITY.toString());
        endpointRegistryEntryOld.setDefinitionURL("https://petstore.swagger.io/v2/swagger.json");
        endpointRegistryEntryOld.setDefinitionType(RegistryEntryDTO.DefinitionTypeEnum.OAS.toString());

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

        RegistryEntryDTO registryEntryDTO = new RegistryEntryDTO();
        registryEntryDTO.setId("entry1");
        registryEntryDTO.setEntryName("Entry Name 2");
        registryEntryDTO.setMetadata("{mutualTLS: true}");
        registryEntryDTO.setServiceUrl("https://xyz2.com");
        registryEntryDTO.setServiceType(RegistryEntryDTO.ServiceTypeEnum.SOAP_1_1);
        registryEntryDTO.setServiceCategory(RegistryEntryDTO.ServiceCategoryEnum.DOMAIN);
        registryEntryDTO.setDefinitionUrl("https://petstore.swagger.io/v2/swagger.json");
        registryEntryDTO.setDefinitionType(RegistryEntryDTO.DefinitionTypeEnum.OAS);

        EndpointRegistryEntry endpointRegistryEntry =
                EndpointRegistryMappingUtils.fromDTOToRegistryEntry(registryEntryDTO, registryEntryDTO.getId(),
                        null, 1);
        endpointRegistryEntry.setEntryId(registryEntryDTO.getId());

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

        RegistryEntryDTO registryEntryDTO = new RegistryEntryDTO();
        registryEntryDTO.setId("entry1");
        registryEntryDTO.setEntryName("Entry Name 2");
        registryEntryDTO.setMetadata("{mutualTLS: true}");
        registryEntryDTO.setServiceUrl("https://xyz2.com");
        registryEntryDTO.setServiceType(RegistryEntryDTO.ServiceTypeEnum.SOAP_1_1);
        registryEntryDTO.setServiceCategory(RegistryEntryDTO.ServiceCategoryEnum.DOMAIN);
        registryEntryDTO.setDefinitionUrl("https://petstore.swagger.io/v2/swagger.json");
        registryEntryDTO.setDefinitionType(RegistryEntryDTO.DefinitionTypeEnum.OAS);

        EndpointRegistryEntry endpointRegistryEntry =
                EndpointRegistryMappingUtils.fromDTOToRegistryEntry(registryEntryDTO, registryEntryDTO.getId(),
                        null, 1);
        endpointRegistryEntry.setEntryId(registryEntryDTO.getId());

        Mockito.when(registryProvider.getEndpointRegistryEntryByUUID(REGISTRY_UUID,
                endpointRegistryEntry.getEntryId()))
                .thenReturn(null);

        Response response = registriesApiService.getRegistryEntryByUuid(REGISTRY_UUID,
                endpointRegistryEntry.getEntryId(), messageContext);
        Assert.assertNull("Endpoint Registry Entry retrieval succeeded for a wrong UUID", response);
    }

    @Test
    public void deleteRegistryEntry() throws APIManagementException {
        final String REGISTRY_UUID = "reg1";

        RegistryEntryDTO registryEntryDTO = new RegistryEntryDTO();
        registryEntryDTO.setId("entry1");
        registryEntryDTO.setEntryName("Entry Name 2");
        registryEntryDTO.setMetadata("{mutualTLS: true}");
        registryEntryDTO.setServiceUrl("https://xyz2.com");
        registryEntryDTO.setServiceType(RegistryEntryDTO.ServiceTypeEnum.SOAP_1_1);
        registryEntryDTO.setServiceCategory(RegistryEntryDTO.ServiceCategoryEnum.DOMAIN);
        registryEntryDTO.setDefinitionUrl("https://petstore.swagger.io/v2/swagger.json");
        registryEntryDTO.setDefinitionType(RegistryEntryDTO.DefinitionTypeEnum.OAS);

        EndpointRegistryEntry endpointRegistryEntry =
                EndpointRegistryMappingUtils.fromDTOToRegistryEntry(registryEntryDTO, registryEntryDTO.getId(),
                        null, 1);
        endpointRegistryEntry.setEntryId(registryEntryDTO.getId());

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

        EndpointRegistryEntry endpointRegistryEntry1 = new EndpointRegistryEntry();
        endpointRegistryEntry1.setEntryId("entry1");
        endpointRegistryEntry1.setName("Entry Name 1");
        endpointRegistryEntry1.setMetaData("{mutualTLS: true}");
        endpointRegistryEntry1.setServiceURL("https://xyz.com");
        endpointRegistryEntry1.setServiceType(RegistryEntryDTO.ServiceTypeEnum.REST.toString());
        endpointRegistryEntry1.setServiceCategory(RegistryEntryDTO.ServiceCategoryEnum.UTILITY.toString());
        endpointRegistryEntry1.setDefinitionURL("https://petstore.swagger.io/v2/swagger.json");
        endpointRegistryEntry1.setDefinitionType(RegistryEntryDTO.DefinitionTypeEnum.OAS.toString());
        endpointRegistryEntryList.add(endpointRegistryEntry1);

        EndpointRegistryEntry endpointRegistryEntry2 = new EndpointRegistryEntry();
        endpointRegistryEntry2.setEntryId("entry2");
        endpointRegistryEntry2.setName("Entry Name 2");
        endpointRegistryEntry2.setMetaData("{mutualTLS: true}");
        endpointRegistryEntry2.setServiceURL("https://xyz2.com");
        endpointRegistryEntry2.setServiceType(RegistryEntryDTO.ServiceTypeEnum.SOAP_1_1.toString());
        endpointRegistryEntry2.setServiceCategory(RegistryEntryDTO.ServiceCategoryEnum.DOMAIN.toString());
        endpointRegistryEntry2.setDefinitionURL("https://petstore.swagger.io/v2/swagger2.json");
        endpointRegistryEntry2.setDefinitionType(RegistryEntryDTO.DefinitionTypeEnum.GQL_SDL.toString());
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

        EndpointRegistryEntry endpointRegistryEntry = new EndpointRegistryEntry();
        endpointRegistryEntry.setEntryId("entry1");
        endpointRegistryEntry.setName("Entry Name 1");
        endpointRegistryEntry.setMetaData("{mutualTLS: true}");
        endpointRegistryEntry.setServiceURL("https://xyz.com");
        endpointRegistryEntry.setServiceType(RegistryEntryDTO.ServiceTypeEnum.REST.toString());
        endpointRegistryEntry.setServiceCategory(RegistryEntryDTO.ServiceCategoryEnum.UTILITY.toString());
        endpointRegistryEntry.setDefinitionURL("https://petstore.swagger.io/v2/swagger.json");
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

        EndpointRegistryEntry endpointRegistryEntry = new EndpointRegistryEntry();
        endpointRegistryEntry.setEntryId("entry1");
        endpointRegistryEntry.setName("Entry Name 1");
        endpointRegistryEntry.setMetaData("{mutualTLS: true}");
        endpointRegistryEntry.setServiceURL("https://xyz.com");
        endpointRegistryEntry.setServiceType(RegistryEntryDTO.ServiceTypeEnum.REST.toString());
        endpointRegistryEntry.setServiceCategory(RegistryEntryDTO.ServiceCategoryEnum.UTILITY.toString());
        endpointRegistryEntry.setDefinitionURL("https://petstore.swagger.io/v2/swagger.json");
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

        EndpointRegistryEntry endpointRegistryEntry = new EndpointRegistryEntry();
        endpointRegistryEntry.setEntryId("entry1");
        endpointRegistryEntry.setName("Entry Name 1");
        endpointRegistryEntry.setMetaData("{mutualTLS: true}");
        endpointRegistryEntry.setServiceURL("https://xyz.com");
        endpointRegistryEntry.setServiceType(RegistryEntryDTO.ServiceTypeEnum.REST.toString());
        endpointRegistryEntry.setServiceCategory(RegistryEntryDTO.ServiceCategoryEnum.UTILITY.toString());
        endpointRegistryEntry.setDefinitionURL("https://petstore.swagger.io/v2/swagger.json");
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

        EndpointRegistryEntry endpointRegistryEntry = new EndpointRegistryEntry();
        endpointRegistryEntry.setEntryId("entry1");
        endpointRegistryEntry.setName("Entry Name 1");
        endpointRegistryEntry.setMetaData("{mutualTLS: true}");
        endpointRegistryEntry.setServiceURL("https://xyz.com");
        endpointRegistryEntry.setServiceType(RegistryEntryDTO.ServiceTypeEnum.REST.toString());
        endpointRegistryEntry.setServiceCategory(RegistryEntryDTO.ServiceCategoryEnum.UTILITY.toString());
        endpointRegistryEntry.setDefinitionURL("https://petstore.swagger.io/v2/swagger.json");
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
        Assert.assertEquals(expectedDTO.getServiceUrl(), actualDTO.getServiceUrl());
        Assert.assertEquals(expectedDTO.getServiceType(), actualDTO.getServiceType());
        Assert.assertEquals(expectedDTO.getServiceCategory(), actualDTO.getServiceCategory());
        Assert.assertEquals(expectedDTO.getDefinitionUrl(), actualDTO.getDefinitionUrl());
        Assert.assertEquals(expectedDTO.getDefinitionType(), actualDTO.getDefinitionType());
    }
}