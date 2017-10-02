/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl;

import org.apache.commons.io.IOUtils;
import org.apache.derby.iapi.services.io.ArrayInputStream;
import org.apache.http.entity.ContentType;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DocumentationType;
import org.wso2.carbon.apimgt.api.model.Mediation;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.api.model.Wsdl;
import org.wso2.carbon.apimgt.impl.definitions.APIDefinitionFromSwagger20;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifactImpl;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.dataobjects.ResourceDO;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.UUID;
import javax.xml.namespace.QName;

public class AbstractAPIManagerTestCase {

    public static final String SAMPLE_API_NAME = "test";
    public static final String API_PROVIDER = "admin";
    public static final String SAMPLE_API_VERSION = "1.0.0";
    public static final String SAMPLE_TENANT_DOMAIN = "carbon.super";
    public static final String SAMPLE_RESOURCE_ID = "xyz";

    @Test
    public void testGetAllApis() throws GovernanceException, APIManagementException {
        GenericArtifactManager genericArtifactManager = Mockito.mock(GenericArtifactManager.class);
        GenericArtifact[] genericArtifacts = new GenericArtifact[1];
        GenericArtifact genericArtifact = getGenericArtifact(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION,
                "sample");
        genericArtifacts[0] = genericArtifact;
        Mockito.when(genericArtifactManager.getAllGenericArtifacts()).thenThrow(RegistryException.class)
                .thenReturn(genericArtifacts);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(genericArtifactManager);
        abstractAPIManager.tenantDomain = SAMPLE_TENANT_DOMAIN;
        try {
            abstractAPIManager.getAllAPIs(); //error scenario
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get APIs from the registry"));
        }
        List<API> apis = abstractAPIManager.getAllAPIs();
        Assert.assertNotNull(apis);
        Assert.assertEquals(apis.size(), 1);
    }

    @Test
    public void testGetApi()
            throws APIManagementException, RegistryException, org.wso2.carbon.user.api.UserStoreException {
        GenericArtifactManager genericArtifactManager = Mockito.mock(GenericArtifactManager.class);
        Registry registry = Mockito.mock(Registry.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        Resource resource = new ResourceImpl();
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(genericArtifactManager, registry,
                tenantManager);
        abstractAPIManager.tenantDomain = SAMPLE_TENANT_DOMAIN;
        APIIdentifier identifier = getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION);
        String apiPath =
                APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + identifier.getProviderName()
                        + RegistryConstants.PATH_SEPARATOR + identifier.getApiName() + RegistryConstants.PATH_SEPARATOR
                        + identifier.getVersion() + APIConstants.API_RESOURCE_NAME;
        Mockito.when(tenantManager.getTenantId(SAMPLE_TENANT_DOMAIN)).thenThrow(UserStoreException.class)
                .thenReturn(-1234);
        try {
            abstractAPIManager.getAPI(getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION));
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get API from "));
        }
        Mockito.when(registry.get(apiPath)).thenThrow(RegistryException.class).thenReturn(resource);
        GenericArtifact genericArtifact = getGenericArtifact(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION,
                "sample");
        Mockito.when(genericArtifactManager.getGenericArtifact(SAMPLE_RESOURCE_ID)).thenReturn(genericArtifact);

        try {
            abstractAPIManager.getAPI(getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION));
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get API from"));
        }
        try {
            abstractAPIManager.getAPI(getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION));
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("artifact id is null"));
        }
        resource.setUUID(SAMPLE_RESOURCE_ID);
        API api = abstractAPIManager.getAPI(getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION));
        Assert.assertNotNull(api);
        Assert.assertEquals(api.getId().getApiName(), SAMPLE_API_NAME);
    }

    @Test
    public void testGetAPIbyUUID()
            throws APIManagementException, GovernanceException, org.wso2.carbon.user.api.UserStoreException {
        GenericArtifactManager genericArtifactManager = Mockito.mock(GenericArtifactManager.class);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        Mockito.when(tenantManager.getTenantId("test")).thenThrow(UserStoreException.class).thenReturn(-1234);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(genericArtifactManager, registryService,
                tenantManager);
        try {
            abstractAPIManager.getAPIbyUUID("1", "test");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get API"));
        }

        GenericArtifact genericArtifact = getGenericArtifact(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION,
                "sample");
        Mockito.when(genericArtifactManager.getGenericArtifact("1")).thenThrow(RegistryException.class)
                .thenReturn(null, genericArtifact);

        try {
            abstractAPIManager.getAPIbyUUID("1", SAMPLE_TENANT_DOMAIN);
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get API"));
        }
        try {
            abstractAPIManager.getAPIbyUUID("1", SAMPLE_TENANT_DOMAIN);
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("API artifact corresponding to artifactId"));
        }
        API api = abstractAPIManager.getAPIbyUUID("1", SAMPLE_TENANT_DOMAIN);
        Assert.assertNotNull(api);
    }

    @Test
    public void testGetLightweightAPIByUUID()
            throws APIManagementException, GovernanceException, org.wso2.carbon.user.api.UserStoreException {
        GenericArtifactManager genericArtifactManager = Mockito.mock(GenericArtifactManager.class);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        Mockito.when(tenantManager.getTenantId("test")).thenThrow(UserStoreException.class).thenReturn(-1234);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(genericArtifactManager, registryService,
                tenantManager);
        try {
            abstractAPIManager.getLightweightAPIByUUID("1", "test");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get tenant Id"));
        }
        GenericArtifact genericArtifact = getGenericArtifact(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION,
                "sample");
        Mockito.when(genericArtifactManager.getGenericArtifact("1")).thenThrow(RegistryException.class)
                .thenReturn(null, genericArtifact);

        try {
            abstractAPIManager.getLightweightAPIByUUID("1", SAMPLE_TENANT_DOMAIN);
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get API"));
        }
        try {
            abstractAPIManager.getLightweightAPIByUUID("1", SAMPLE_TENANT_DOMAIN);
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("API artifact corresponding to artifactId"));
        }
        API api = abstractAPIManager.getLightweightAPIByUUID("1", "1");
        Assert.assertNotNull(api);
    }

    @Test
    public void testGetLightweightAPI() throws APIManagementException, RegistryException {
        GenericArtifactManager genericArtifactManager = Mockito.mock(GenericArtifactManager.class);
        Registry registry = Mockito.mock(Registry.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        Resource resource = new ResourceImpl();
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(genericArtifactManager, registry,
                tenantManager);
        APIIdentifier apiIdentifier = getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION);
        abstractAPIManager.tenantDomain = SAMPLE_TENANT_DOMAIN;
        APIIdentifier identifier = getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION);
        String apiPath =
                APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + identifier.getProviderName()
                        + RegistryConstants.PATH_SEPARATOR + identifier.getApiName() + RegistryConstants.PATH_SEPARATOR
                        + identifier.getVersion() + APIConstants.API_RESOURCE_NAME;
        Mockito.when(registry.get(apiPath)).thenThrow(RegistryException.class).thenReturn(resource);
        GenericArtifact genericArtifact = getGenericArtifact(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION,
                "sample");
        Mockito.when(genericArtifactManager.getGenericArtifact(SAMPLE_RESOURCE_ID)).thenReturn(genericArtifact);
        try {
            abstractAPIManager.getLightweightAPI(apiIdentifier);
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get API from"));
        }
        try {
            abstractAPIManager.getLightweightAPI(apiIdentifier);
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("artifact id is null"));
        }
        resource.setUUID(SAMPLE_RESOURCE_ID);
        API api = abstractAPIManager.getLightweightAPI(apiIdentifier);
        Assert.assertNotNull(api);
        Assert.assertEquals(api.getId().getApiName(), SAMPLE_API_NAME);
    }

    @Test
    public void testGetAPIVersions() throws APIManagementException,
            RegistryException {
        Registry registry = Mockito.mock(Registry.class);
        String providerName = API_PROVIDER;
        String apiName = "sampleApi";
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(registry);
        String apiPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR + providerName
                + RegistryConstants.PATH_SEPARATOR + apiName;
        Collection collection = new CollectionImpl();
        collection.setChildren(new String[] { apiPath + "test/abc", apiPath + "test/asd" });
        Mockito.when(registry.get(apiPath)).thenReturn(collection);
        Assert.assertEquals(abstractAPIManager.getAPIVersions(providerName, apiName).size(), 2);
    }

    @Test
    public void testGetApiByPath() throws APIManagementException, RegistryException {
        GenericArtifactManager genericArtifactManager = Mockito.mock(GenericArtifactManager.class);
        Registry registry = Mockito.mock(Registry.class);
        Resource resource = new ResourceImpl();
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(genericArtifactManager, registry, null);
        String apiPath = "/test/sample/1.0.0";
        Mockito.when(registry.get(apiPath)).thenReturn(resource);
        GenericArtifact genericArtifact = getGenericArtifact(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION,
                "sample");

        try {
            abstractAPIManager.getAPI(apiPath);
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("artifact id is null"));
        }
        resource.setUUID(SAMPLE_RESOURCE_ID);
        /*try {
            abstractAPIManager.getAPI(apiPath);
        } catch (APIManagementException e){
            Assert.assertTrue(e.getMessage().contains("Failed to get API from"));
        }*/
        Mockito.when(genericArtifactManager.getGenericArtifact(SAMPLE_RESOURCE_ID)).thenReturn(genericArtifact);
        API api = abstractAPIManager.getAPI(apiPath);
        Assert.assertNotNull(api);
        Assert.assertEquals(api.getId().getApiName(), SAMPLE_API_NAME);
    }

    @Test
    public void testIsAPIAvailable() throws RegistryException, APIManagementException {
        Registry registry = Mockito.mock(Registry.class);
        APIIdentifier apiIdentifier = getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION);
        String path =
                APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + apiIdentifier.getProviderName()
                        + RegistryConstants.PATH_SEPARATOR + apiIdentifier.getApiName()
                        + RegistryConstants.PATH_SEPARATOR + apiIdentifier.getVersion();
        Mockito.when(registry.resourceExists(path)).thenReturn(true);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(registry);
        Assert.assertTrue(abstractAPIManager.isAPIAvailable(apiIdentifier));
    }

    @Test
    public void testGetAllGlobalMediationPolicies() throws RegistryException, APIManagementException {
        Registry registry = Mockito.mock(Registry.class);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(registry);
        Collection parentCollection = new CollectionImpl();
        String mediationResourcePath = APIConstants.API_CUSTOM_SEQUENCE_LOCATION;
        String childCollectionPath = mediationResourcePath + "/testMediation";
        parentCollection.setChildren(new String[] { childCollectionPath });
        Mockito.when(registry.get(mediationResourcePath)).thenReturn(parentCollection);
        Collection childCollection = new CollectionImpl();
        String resourcePath = childCollectionPath + "/policy1";
        childCollection.setChildren(new String[] { resourcePath });
        Mockito.when(registry.get(childCollectionPath)).thenReturn(childCollection);
        Resource resource = new ResourceImpl();
        resource.setUUID(SAMPLE_RESOURCE_ID);

        Mockito.when(registry.get(resourcePath)).thenReturn(resource);
        try {
            abstractAPIManager.getAllGlobalMediationPolicies();
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get global mediation policies"));
        }
        String mediationPolicyContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<sequence xmlns=\"http://ws.apache.org/ns/synapse\" name=\"default-endpoint\">\n</sequence>";
        resource.setContent(mediationPolicyContent);

        List<Mediation> policies = abstractAPIManager.getAllGlobalMediationPolicies();
        Assert.assertNotNull(policies);
        Assert.assertEquals(policies.size(), 1);

    }

    @Test
    public void testGetGlobalMediationPolicy() throws RegistryException, APIManagementException {
        Registry registry = Mockito.mock(Registry.class);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(registry);
        String resourceUUID = SAMPLE_RESOURCE_ID;
        Collection parentCollection = new CollectionImpl();
        String mediationResourcePath = APIConstants.API_CUSTOM_SEQUENCE_LOCATION;
        String childCollectionPath = mediationResourcePath + "/testMediation";
        parentCollection.setChildren(new String[] { childCollectionPath });
        Mockito.when(registry.get(mediationResourcePath)).thenReturn(parentCollection);
        Collection childCollection = new CollectionImpl();
        String resourcePath = childCollectionPath + "/policy1";
        childCollection.setChildren(new String[] { resourcePath });
        Mockito.when(registry.get(childCollectionPath)).thenReturn(childCollection);
        Resource resource = new ResourceImpl(resourcePath, new ResourceDO());
        resource.setUUID(resourceUUID);

        Mockito.when(registry.get(resourcePath)).thenReturn(resource);
        abstractAPIManager.getGlobalMediationPolicy(resourceUUID); // test for registry exception
        String mediationPolicyContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<sequence xmlns=\"http://ws.apache.org/ns/synapse\" name=\"default-endpoint\">\n</sequence>";
        resource.setContent(mediationPolicyContent);

        Mediation policy = abstractAPIManager.getGlobalMediationPolicy(resourceUUID);
        Assert.assertNotNull(policy);

    }

    @Test
    public void testGetAllWsdls() throws RegistryException, APIManagementException {
        Registry registry = Mockito.mock(Registry.class);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(registry);
        Collection parentCollection = new CollectionImpl();
        String wsdlResourcepath = APIConstants.API_WSDL_RESOURCE;
        String resourcePath = wsdlResourcepath + "/wsdl1";
        parentCollection.setChildren(new String[] { resourcePath });
        Mockito.when(registry.get(wsdlResourcepath)).thenReturn(parentCollection);
        Resource resource = new ResourceImpl();
        Mockito.when(registry.get(resourcePath)).thenThrow(RegistryException.class).thenReturn(resource);
        Mockito.when(registry.resourceExists(wsdlResourcepath)).thenReturn(true);
        try {
            abstractAPIManager.getAllWsdls();
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get wsdl list"));
        }
        resource.setUUID(SAMPLE_RESOURCE_ID);

        List<Wsdl> wsdls = abstractAPIManager.getAllWsdls();
        Assert.assertNotNull(wsdls);
        Assert.assertEquals(wsdls.size(), 1);

    }

    @Test
    public void testGetWsdlById() throws RegistryException, APIManagementException {
        String resourceId = SAMPLE_RESOURCE_ID;
        Registry registry = Mockito.mock(Registry.class);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(registry);
        Collection parentCollection = new CollectionImpl();
        String wsdlResourcepath = APIConstants.API_WSDL_RESOURCE;
        String resourcePath = wsdlResourcepath + "/wsdl1";
        parentCollection.setChildren(new String[] { resourcePath });
        Mockito.when(registry.get(wsdlResourcepath)).thenReturn(parentCollection);
        Resource resource = new ResourceImpl(resourcePath, new ResourceDO());
        Mockito.when(registry.get(resourcePath)).thenThrow(RegistryException.class).thenReturn(resource);
        Mockito.when(registry.resourceExists(wsdlResourcepath)).thenReturn(true);
        try {
            abstractAPIManager.getWsdlById(resourceId);
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Error while accessing registry objects"));
        }
        resource.setUUID(resourceId);
        try {
            abstractAPIManager.getWsdlById(resourceId);
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Error occurred while getting content stream of the wsdl"));
        }
        String wsdlContent = "sample wsdl";
        resource.setContent(wsdlContent);
        Wsdl wsdl = abstractAPIManager.getWsdlById(resourceId);
        Assert.assertNotNull(wsdl);

    }

    @Test
    public void testDeleteWsdl() throws APIManagementException, RegistryException {
        Registry registry = Mockito.mock(Registry.class);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(registry);
        Collection parentCollection = new CollectionImpl();
        String wsdlResourcePath = APIConstants.API_WSDL_RESOURCE;
        String resourcePath = wsdlResourcePath + "/wsdl1";
        parentCollection.setChildren(new String[] { resourcePath });
        Mockito.when(registry.get(wsdlResourcePath)).thenReturn(parentCollection);
        Resource resource = new ResourceImpl(resourcePath, new ResourceDO());
        Mockito.when(registry.get(resourcePath)).thenThrow(RegistryException.class).thenReturn(resource);
        Mockito.when(registry.resourceExists(wsdlResourcePath)).thenReturn(true);
        try {
            abstractAPIManager.deleteWsdl(SAMPLE_RESOURCE_ID);
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Error while accessing registry objects"));
        }

        Mockito.when(registry.resourceExists(resourcePath)).thenReturn(true, true, true, true, false);
        resource.setUUID(SAMPLE_RESOURCE_ID);
        Mockito.doThrow(RegistryException.class).doNothing().when(registry).delete(resourcePath);
        try {
            abstractAPIManager.deleteWsdl(SAMPLE_RESOURCE_ID);
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to delete wsdl"));
        }
        Assert.assertFalse(abstractAPIManager.deleteWsdl(SAMPLE_RESOURCE_ID));
        Assert.assertTrue(abstractAPIManager.deleteWsdl(SAMPLE_RESOURCE_ID));
    }

    @Test
    public void testGetWsdl() throws APIManagementException, RegistryException, IOException {
        Registry registry = Mockito.mock(Registry.class);
        Resource resourceMock = Mockito.mock(Resource.class);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(registry);
        APIIdentifier identifier = getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION);
        String wsdlName =
                identifier.getProviderName() + "--" + identifier.getApiName() + identifier.getVersion() + ".wsdl";
        String wsdlResourcePath = APIConstants.API_WSDL_RESOURCE_LOCATION + wsdlName;
        Resource resource = new ResourceImpl(wsdlResourcePath, new ResourceDO());
        Mockito.when(registry.get(wsdlResourcePath)).thenThrow(RegistryException.class).thenReturn(resource);
        Mockito.when(registry.resourceExists(wsdlResourcePath)).thenReturn(true);
        try {
            abstractAPIManager.getWsdl(identifier);
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Error while getting wsdl file from the registry"));
        }
        String wsdlContent = "sample wsdl";
        resource.setContent(wsdlContent);
        InputStream inputStream = new ArrayInputStream();
        Mockito.when(resourceMock.getContentStream()).thenReturn(inputStream);
        Assert.assertTrue(abstractAPIManager.getWsdl(identifier).contains(wsdlContent));

    }

    @Test
    public void testUploadWsdl() throws RegistryException, APIManagementException {
        Registry registry = Mockito.mock(Registry.class);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(registry);
        Resource resource = new ResourceImpl();
        String resourcePath = "/test/wsdl";
        String wsdlContent = "sample wsdl";
        Resource resourceMock = Mockito.mock(Resource.class);
        resourceMock.setContent(wsdlContent);
        resourceMock.setMediaType(String.valueOf(ContentType.APPLICATION_XML));
        Mockito.when(registry.newResource()).thenReturn(resource);
        Mockito.doThrow(RegistryException.class).when(registry).put(resourcePath, resource);
        try {
            abstractAPIManager.uploadWsdl(resourcePath, wsdlContent);
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Error while uploading wsdl to from the registry"));
        }
    }

    @Test
    public void testUpdateWsdl() throws APIManagementException, RegistryException {
        Registry registry = Mockito.mock(Registry.class);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(registry);
        Resource resource = new ResourceImpl();
        String resourcePath = "/test/wsdl";
        String wsdlContent = "sample wsdl";
        Mockito.when(registry.get(resourcePath)).thenThrow(RegistryException.class).thenReturn(resource);
        try {
            abstractAPIManager.updateWsdl(resourcePath, wsdlContent);
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Error while updating the existing wsdl"));
        }
        try {
            abstractAPIManager.updateWsdl(resourcePath, wsdlContent);
        } catch (APIManagementException e) {
            Assert.fail("Error while updating wsdl");
        }
    }

    @Test
    public void testGetSwagger20Definition() throws Exception {
        int tenantId = -1234;
        RegistryService registryService = Mockito.mock(RegistryService.class);
        APIDefinitionFromSwagger20 apiDefinition = Mockito.mock(APIDefinitionFromSwagger20.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(null, registryService, tenantManager);
        Mockito.when(tenantManager.getTenantId(SAMPLE_TENANT_DOMAIN)).thenThrow(UserStoreException.class)
                .thenReturn(tenantId);

        APIIdentifier identifier = getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION);
        try {
            abstractAPIManager.getSwagger20Definition(identifier);
            Assert.fail("Use store exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get swagger documentation of API"));
        }
        setFinalStatic(AbstractAPIManager.class.getDeclaredField("definitionFromSwagger20"), apiDefinition);
        String swaggerContent = "sample swagger";
        Mockito.when(apiDefinition.getAPIDefinition(identifier, null)).thenReturn(swaggerContent);
        Assert.assertEquals(abstractAPIManager.getSwagger20Definition(identifier), swaggerContent);
        abstractAPIManager.tenantDomain = SAMPLE_TENANT_DOMAIN;
        Assert.assertEquals(abstractAPIManager.getSwagger20Definition(identifier), swaggerContent);
        Mockito.when(registryService.getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantId))
                .thenThrow(RegistryException.class);
        abstractAPIManager.tenantDomain = null;
        try {
            abstractAPIManager.getSwagger20Definition(identifier);
            Assert.fail("Registry exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get swagger documentation of API"));
        }
    }

    @Test
    public void testAddResourceFile() throws APIManagementException, RegistryException, IOException {
        Registry registry = Mockito.mock(Registry.class);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(registry);
        Resource resource = new ResourceImpl();
        Mockito.when(registry.newResource()).thenReturn(resource);
        String resourcePath = "/test";
        String contentType = "sampleType";
        ResourceFile resourceFile = new ResourceFile(null, contentType);
        try {
            abstractAPIManager.addResourceFile(resourcePath, resourceFile);
            Assert.fail("Registry exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Error while adding the resource to the registry"));
        }
        InputStream in = IOUtils.toInputStream("sample content", "UTF-8");
        resourceFile = new ResourceFile(in, contentType);
        String returnedPath = abstractAPIManager.addResourceFile(resourcePath, resourceFile);
        Assert.assertTrue(returnedPath.contains(resourcePath) && returnedPath.contains("/t/"));
        abstractAPIManager.tenantDomain = SAMPLE_TENANT_DOMAIN;
        returnedPath = abstractAPIManager.addResourceFile(resourcePath, resourceFile);
        Assert.assertTrue(returnedPath.contains(resourcePath) && !returnedPath.contains("/t/"));

    }

    @Test
    public void testIsDocumentationExist() throws APIManagementException, RegistryException {
        Registry registry = Mockito.mock(Registry.class);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(registry);
        APIIdentifier identifier = getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION);
        String docName = "sampleDoc";
        String docPath =
                APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + identifier.getProviderName()
                        + RegistryConstants.PATH_SEPARATOR + identifier.getApiName() + RegistryConstants.PATH_SEPARATOR
                        + identifier.getVersion() + RegistryConstants.PATH_SEPARATOR + APIConstants.DOC_DIR
                        + RegistryConstants.PATH_SEPARATOR + docName;
        Mockito.when(registry.resourceExists(docPath)).thenThrow(RegistryException.class).thenReturn(true);
        try {
            abstractAPIManager.isDocumentationExist(identifier, docName);
            Assert.fail("Registry exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to check existence of the document"));
        }
        Assert.assertTrue(abstractAPIManager.isDocumentationExist(identifier, docName));
    }

    @Test
    public void testGetAllDocumentation() throws APIManagementException, RegistryException {
        Registry registry = Mockito.mock(UserRegistry.class);
        GenericArtifactManager genericArtifactManager = Mockito.mock(GenericArtifactManager.class);

        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(genericArtifactManager, registry, null);
        abstractAPIManager.registry = registry;

        GenericArtifact genericArtifact = getGenericArtifact(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION,
                "sample");
        genericArtifact.setAttribute(APIConstants.DOC_TYPE, "Other");
        genericArtifact.setAttribute(APIConstants.DOC_SOURCE_TYPE, "URL");
        Association association = new Association();
        String associationDestinationPath = "doc/destination";
        association.setDestinationPath(associationDestinationPath);
        Association[] associations = new Association[] { association };
        APIIdentifier identifier = getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION);
        String apiResourcePath =
                APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + identifier.getProviderName()
                        + RegistryConstants.PATH_SEPARATOR + identifier.getApiName() + RegistryConstants.PATH_SEPARATOR
                        + identifier.getVersion() + APIConstants.API_RESOURCE_NAME;
        Mockito.when(registry.getAssociations(apiResourcePath, APIConstants.DOCUMENTATION_ASSOCIATION))
                .thenThrow(RegistryException.class).thenReturn(associations);
        try {
            abstractAPIManager.getAllDocumentation(identifier);
            Assert.fail("Registry exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get documentations for api"));
        }

        Resource resource = new ResourceImpl();
        resource.setUUID(SAMPLE_RESOURCE_ID);
        Mockito.when(registry.get(associationDestinationPath)).thenReturn(resource);
        Mockito.when(genericArtifactManager.getGenericArtifact(SAMPLE_RESOURCE_ID)).thenReturn(genericArtifact);
        List<Documentation> documentationList = abstractAPIManager.getAllDocumentation(identifier);
        Assert.assertNotNull(documentationList);
        Assert.assertEquals(documentationList.size(), 1);
        String documentationName = "doc1";
        String contentPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR + identifier.getProviderName()
                + RegistryConstants.PATH_SEPARATOR + identifier.getApiName() + RegistryConstants.PATH_SEPARATOR
                + identifier.getVersion() + RegistryConstants.PATH_SEPARATOR + APIConstants.DOC_DIR
                + RegistryConstants.PATH_SEPARATOR + RegistryConstants.PATH_SEPARATOR + documentationName;
        genericArtifact.setAttribute(APIConstants.DOC_SOURCE_TYPE, "In line");
        genericArtifact.setAttribute(APIConstants.DOC_NAME, documentationName);
        ResourceDO resourceDO = new ResourceDO();
        resourceDO.setLastUpdatedOn(12344567);
        Resource resource1 = new ResourceImpl(contentPath, resourceDO);
        Mockito.when(registry.get(contentPath)).thenReturn(resource1);
        documentationList = abstractAPIManager.getAllDocumentation(identifier);
        Assert.assertNotNull(documentationList);
        Assert.assertEquals(documentationList.size(), 1);

    }

    @Test
    public void testGetAllDocumentationWithLoggedUser()
            throws APIManagementException, org.wso2.carbon.user.api.UserStoreException, RegistryException {
        int tenantId = -1234;
        Registry registry = Mockito.mock(UserRegistry.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        GenericArtifactManager genericArtifactManager = Mockito.mock(GenericArtifactManager.class);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(genericArtifactManager, registryService,
                registry, tenantManager);
        Mockito.when(tenantManager.getTenantId(SAMPLE_TENANT_DOMAIN)).thenThrow(UserStoreException.class)
                .thenReturn(tenantId);
        APIIdentifier identifier = getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION);
        String loggedInUser = "admin";
        try {
            abstractAPIManager.getAllDocumentation(identifier, loggedInUser);
            Assert.fail("User store exception not thrown for erroe scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get documentations for api"));
        }
        String apiResourcePath =
                APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + identifier.getProviderName()
                        + RegistryConstants.PATH_SEPARATOR + identifier.getApiName() + RegistryConstants.PATH_SEPARATOR
                        + identifier.getVersion() + APIConstants.API_RESOURCE_NAME;
        GenericArtifact genericArtifact = getGenericArtifact(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION,
                "sample");
        genericArtifact.setAttribute(APIConstants.DOC_TYPE, "Other");
        genericArtifact.setAttribute(APIConstants.DOC_SOURCE_TYPE, "URL");
        Association association = new Association();
        String associationDestinationPath = "doc/destination";
        association.setDestinationPath(associationDestinationPath);
        Association[] associations = new Association[] { association };
        abstractAPIManager.registry = registry;
        Mockito.when(registryService.getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantId))
                .thenThrow(RegistryException.class);
        Mockito.when(registry.getAssociations(apiResourcePath, APIConstants.DOCUMENTATION_ASSOCIATION))
                .thenReturn(associations);
        try {
            abstractAPIManager.getAllDocumentation(identifier, loggedInUser);
            Assert.fail("Registry exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get documentations for api"));
        }
        abstractAPIManager.tenantDomain = SAMPLE_TENANT_DOMAIN;
        Resource resource = new ResourceImpl();
        resource.setUUID(SAMPLE_RESOURCE_ID);
        Mockito.when(registry.get(associationDestinationPath)).thenThrow(RegistryException.class).thenReturn(resource);
        try {
            abstractAPIManager.getAllDocumentation(identifier, loggedInUser);
            Assert.fail("Registry exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get documentations for api"));
        }
        Mockito.when(genericArtifactManager.getGenericArtifact(SAMPLE_RESOURCE_ID)).thenReturn(genericArtifact);
        List<Documentation> documentationList = abstractAPIManager.getAllDocumentation(identifier, loggedInUser);
        Assert.assertNotNull(documentationList);
        Assert.assertEquals(documentationList.size(), 1);
        String documentationName = "doc1";
        String contentPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR + identifier.getProviderName()
                + RegistryConstants.PATH_SEPARATOR + identifier.getApiName() + RegistryConstants.PATH_SEPARATOR
                + identifier.getVersion() + RegistryConstants.PATH_SEPARATOR + APIConstants.DOC_DIR
                + RegistryConstants.PATH_SEPARATOR + RegistryConstants.PATH_SEPARATOR + documentationName;
        genericArtifact.setAttribute(APIConstants.DOC_SOURCE_TYPE, "In line");
        genericArtifact.setAttribute(APIConstants.DOC_NAME, documentationName);
        ResourceDO resourceDO = new ResourceDO();
        resourceDO.setLastUpdatedOn(12344567);
        Resource resource1 = new ResourceImpl(contentPath, resourceDO);
        Mockito.when(registry.get(contentPath)).thenReturn(resource1);
        documentationList = abstractAPIManager.getAllDocumentation(identifier, loggedInUser);
        Assert.assertNotNull(documentationList);
        Assert.assertEquals(documentationList.size(), 1);

    }

    @Test
    public void testGetDocumentation() throws APIManagementException, RegistryException {
        Registry registry = Mockito.mock(UserRegistry.class);
        GenericArtifactManager genericArtifactManager = Mockito.mock(GenericArtifactManager.class);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(genericArtifactManager, registry, null);
        APIIdentifier identifier = getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION);
        String documentationName = "doc1";
        String contentPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR + identifier.getProviderName()
                + RegistryConstants.PATH_SEPARATOR + identifier.getApiName() + RegistryConstants.PATH_SEPARATOR
                + identifier.getVersion() + RegistryConstants.PATH_SEPARATOR + APIConstants.DOC_DIR
                + RegistryConstants.PATH_SEPARATOR + documentationName;
        GenericArtifact genericArtifact = getGenericArtifact(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION,
                "sample");
        String docType = "other";
        genericArtifact.setAttribute(APIConstants.DOC_TYPE, docType);
        genericArtifact.setAttribute(APIConstants.DOC_SOURCE_TYPE, "URL");
        Resource resource = new ResourceImpl();
        resource.setUUID(SAMPLE_RESOURCE_ID);
        Mockito.when(registry.get(contentPath)).thenThrow(RegistryException.class).thenReturn(resource);
        Mockito.when(genericArtifactManager.getGenericArtifact(SAMPLE_RESOURCE_ID)).thenReturn(genericArtifact);
        try {
            abstractAPIManager.getDocumentation(identifier, DocumentationType.OTHER, documentationName);
            Assert.fail("Registry exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get documentation details"));
        }
        Assert.assertTrue(
                abstractAPIManager.getDocumentation(identifier, DocumentationType.OTHER, documentationName).getId()
                        .equals(genericArtifact.getId()));
    }

    private static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }

    private GenericArtifact getGenericArtifact(String apiName, String provider, String version, String qName)
            throws GovernanceException {
        String id = UUID.randomUUID().toString();
        GenericArtifact genericArtifact = new GenericArtifactImpl(id, new QName(qName), "");
        genericArtifact.setAttribute(APIConstants.API_OVERVIEW_PROVIDER, provider);
        genericArtifact.setAttribute(APIConstants.API_OVERVIEW_NAME, apiName);
        genericArtifact.setAttribute(APIConstants.API_OVERVIEW_VERSION, version);
        return genericArtifact;
    }

    private APIIdentifier getAPIIdentifier(String apiName, String provider, String version) {
        return new APIIdentifier(provider, apiName, version);
    }
}
