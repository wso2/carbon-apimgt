/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl.wsdl;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.internal.APIManagerComponent;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.wsdl.util.SequenceUtils;
import org.wso2.carbon.apimgt.impl.template.ConfigContext;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ APIManagerComponent.class, ServiceReferenceHolder.class,
        MultitenantUtils.class, APIUtil.class, RegistryUtils.class })
public class SequenceUtilsTestCase {

    private UserRegistry userRegistry;
    private ServiceReferenceHolder serviceReferenceHolder;
    private RegistryService registryService;
    private RealmService realmService;
    private TenantManager tenantManager;

    private static final String RESOURCE_PATH = "/apimgt/applicationdata/provider/admin/sample-api/1.0.0/soap_to_rest/in/test_get.xml";
    private static final String RESOURCE_NAME = "test";
    private static final String INSEQUENCE_RESOURCES = "/apimgt/applicationdata/provider/admin/sample-api/1.0.0/soap_to_rest/in/";
    private static final String SEQUENCE = "";
    private static final String HTTP_METHOD = "post";

    @Before
    public void setup() throws UserStoreException, RegistryException {
        userRegistry = Mockito.mock(UserRegistry.class);
        serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        registryService = Mockito.mock(RegistryService.class);
        realmService = Mockito.mock(RealmService.class);
        tenantManager = Mockito.mock(TenantManager.class);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(MultitenantUtils.class);
        PowerMockito.mockStatic(RegistryUtils.class);

        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getGovernanceSystemRegistry(Mockito.anyInt())).thenReturn(userRegistry);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
    }

    @Test
    public void testRestToSoapConvertedSequence() throws Exception {
        Resource resource = Mockito.mock(Resource.class);
        Mockito.when(userRegistry.resourceExists(RESOURCE_PATH)).thenReturn(false);
        Mockito.when(userRegistry.newResource()).thenReturn(resource);
        try {
            SequenceUtils.saveRestToSoapConvertedSequence(userRegistry, SEQUENCE, HTTP_METHOD, RESOURCE_PATH,
                    RESOURCE_NAME);
            Mockito.when(userRegistry.resourceExists(RESOURCE_PATH)).thenReturn(true);
            Mockito.when(userRegistry.get(RESOURCE_PATH)).thenReturn(resource);
            SequenceUtils.saveRestToSoapConvertedSequence(userRegistry, SEQUENCE, HTTP_METHOD, RESOURCE_PATH,
                    RESOURCE_NAME);
        } catch (APIManagementException e) {
            Assert.fail("Failed to save the sequence in the registry");
        }
    }

    @Test
    public void testUpdateRestToSoapConvertedSequences() throws Exception {
        String provider = "admin";
        String apiName = "test-api";
        String version = "1.0.0";
        String seqType = "in";
        String sequence = "{\"test\":{\"method\":\"post\",\"content\":\"<header></header>\"}}";
        Resource resource = Mockito.mock(Resource.class);
        PowerMockito.when(MultitenantUtils.getTenantDomain(Mockito.anyString())).thenReturn("carbon.super");
        PowerMockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(userRegistry.resourceExists(Mockito.anyString())).thenReturn(true);
        Mockito.when(userRegistry.get(Mockito.anyString())).thenReturn(resource);
        Mockito.when(tenantManager.getTenantId(Mockito.anyString())).thenReturn(-1);

        try {
            SequenceUtils.updateRestToSoapConvertedSequences(apiName, version, provider, seqType, sequence);
        } catch (APIManagementException e) {
            Assert.fail("Failed to update the sequence in the registry");
        }
    }


    public void testGetRestToSoapConvertedSequence() throws Exception {
        String provider = "admin";
        String apiName = "test-api";
        String version = "1.0.0";
        String seqType = "in";
        String resourceName = "test_get.xml";
        Resource resource = Mockito.mock(Resource.class);
        ResourceImpl resourceImpl = Mockito.mock(ResourceImpl.class);
        Collection collection = Mockito.mock(Collection.class);
        String[] paths = new String[0];
        byte[] content = new byte[1];
        PowerMockito.when(MultitenantUtils.getTenantDomain(Mockito.anyString())).thenReturn("carbon.super");
        PowerMockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(((Collection)userRegistry.get(Mockito.anyString()))).thenReturn(collection);
        Mockito.when(collection.getChildren()).thenReturn(paths);
        Mockito.when(userRegistry.get(Mockito.anyString())).thenReturn(resource);
        Mockito.when(resource.getContent()).thenReturn(content);
        Mockito.when(resourceImpl.getName()).thenReturn(resourceName);
        Mockito.when(tenantManager.getTenantId(Mockito.anyString())).thenReturn(-1);

        try {
            SequenceUtils.getRestToSoapConvertedSequence(apiName, version, provider, seqType);
        } catch (APIManagementException e) {
            Assert.fail("Failed to get the sequences from the registry");
        }
    }

    @Test
    public void testGetSequenceTemplateConfigContext() throws Exception {
        String seqType = "in_sequences";
        String content = "<header description=\"SOAPAction\" name=\"SOAPAction\" scope=\"transport\""
                + " value=\"http://ws.cdyne.com/PhoneVerify/query/CheckPhoneNumber\"/>";
        Collection collection = new CollectionImpl();
        ResourceImpl resource = new ResourceImpl();
        resource.setName("checkPhoneNumber_post.xml");
        collection.setChildren(new String[] { INSEQUENCE_RESOURCES + "checkPhoneNumber_post.xml" });
        Mockito.when(userRegistry.resourceExists(INSEQUENCE_RESOURCES)).thenReturn(true);
        ConfigContext configContext = Mockito.mock(ConfigContext.class);
        Mockito.when(userRegistry.get(INSEQUENCE_RESOURCES)).thenReturn(collection);
        Mockito.when(userRegistry.get(INSEQUENCE_RESOURCES + "checkPhoneNumber_post.xml")).thenReturn(resource);
        PowerMockito.when(RegistryUtils.decodeBytes(Mockito.any(byte[].class))).thenReturn(content);
        try {
            ConfigContext context = SequenceUtils
                    .getSequenceTemplateConfigContext(userRegistry, INSEQUENCE_RESOURCES, seqType, configContext);
            Assert.assertNotNull(context);
        } catch (RegistryException e) {
            Assert.fail("Failed to get the sequences from the registry");
        }
    }

    @Test
    public void testGetResourceParametersFromSwagger() throws Exception {
        JSONParser parser = new JSONParser();
        String swagger =
                "{\"paths\":{\"\\/checkPhoneNumber\":{\"post\":{\"responses\":{\"200\":{\"description\":\"\"}},"
                        + "\"parameters\":[{\"in\":\"query\",\"name\":\"PhoneNumber\",\"description\":\"\",\"type\":\"string\"},"
                        + "{\"in\":\"query\",\"name\":\"LicenseKey\",\"description\":\"\",\"type\":\"string\"}],"
                        + "\"tags\":[\"checkPhoneNumber\"]}}}}";
        String resource = "{\"post\":{\"responses\":{\"200\":{\"description\":\"\"}},\"parameters\":[{\"in\":\"query\","
                + "\"name\":\"PhoneNumber\",\"description\":\"\",\"type\":\"string\"},{\"in\":\"query\",\"name\":\"LicenseKey\""
                + ",\"description\":\"\",\"type\":\"string\"}],\"tags\":[\"checkPhoneNumber\"]}}";
        JSONObject swaggerObj = (JSONObject) parser.parse(swagger);
        JSONObject resourceObj = (JSONObject) parser.parse(resource);

        List<JSONObject> mapping = SequenceUtils.getResourceParametersFromSwagger(swaggerObj, resourceObj, HTTP_METHOD);
        Assert.assertNotNull(mapping);
        Assert.assertTrue("Failed to process the parameter mapping", mapping.size() == 2);
    }
}
