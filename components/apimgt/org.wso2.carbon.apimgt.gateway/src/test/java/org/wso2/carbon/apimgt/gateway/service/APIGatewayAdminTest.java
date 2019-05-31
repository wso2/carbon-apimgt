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
package org.wso2.carbon.apimgt.gateway.service;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.gateway.utils.EndpointAdminServiceClient;
import org.wso2.carbon.apimgt.gateway.utils.MediationSecurityAdminServiceClient;
import org.wso2.carbon.apimgt.gateway.utils.RESTAPIAdminClient;
import org.wso2.carbon.apimgt.gateway.utils.SequenceAdminServiceClient;
import org.wso2.carbon.rest.api.stub.types.carbon.APIData;
import org.wso2.carbon.rest.api.stub.types.carbon.ResourceData;


public class APIGatewayAdminTest {
    String provider = "admin";
    String name = "API";
    String version = "1.0.0";
    String config = "abcdef";
    String tenantDomain = "carbon.super";
    String apiName = provider + "--" + name + ":v" + version;
    String apiDefaultName = provider + "--" + name;

    @Test
    public void addApiForTenant() throws Exception {
        RESTAPIAdminClient restapiAdminClient = Mockito.mock(RESTAPIAdminClient.class);
        Mockito.when(restapiAdminClient.addApi(config, tenantDomain)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminClient, null, null);
        Assert.assertTrue(apiGatewayAdmin.addApiForTenant(provider, name, version, config, tenantDomain));
    }

    @Test
    public void addApi() throws Exception {
        RESTAPIAdminClient restapiAdminClient = Mockito.mock(RESTAPIAdminClient.class);
        Mockito.when(restapiAdminClient.addApi(config)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminClient, null,null);
        Assert.assertTrue(apiGatewayAdmin.addApi(provider, name, version, config));
    }

    @Test
    public void addPrototypeApiScriptImplForTenant() throws Exception {
        RESTAPIAdminClient restapiAdminClient = Mockito.mock(RESTAPIAdminClient.class);
        Mockito.when(restapiAdminClient.addApi(config, tenantDomain)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminClient, null,null);
        Assert.assertTrue(apiGatewayAdmin.addPrototypeApiScriptImplForTenant(provider, name, version, config,
                tenantDomain));
    }

    @Test
    public void addPrototypeApiScriptImpl() throws Exception {
        RESTAPIAdminClient restapiAdminClient = Mockito.mock(RESTAPIAdminClient.class);
        Mockito.when(restapiAdminClient.addApi(config)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminClient, null,null);
        Assert.assertTrue(apiGatewayAdmin.addPrototypeApiScriptImpl(provider, name, version, config));
    }

    @Test
    public void addDefaultAPIForTenant() throws Exception {
        RESTAPIAdminClient restapiAdminClient = Mockito.mock(RESTAPIAdminClient.class);
        Mockito.when(restapiAdminClient.addApi(config, tenantDomain)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminClient, null,null);
        Assert.assertTrue(apiGatewayAdmin.addDefaultAPIForTenant(provider, name, version, config, tenantDomain));
    }

    @Test
    public void addDefaultAPI() throws Exception {
        RESTAPIAdminClient restapiAdminClient = Mockito.mock(RESTAPIAdminClient.class);
        Mockito.when(restapiAdminClient.addApi(config)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminClient, null,null);
        Assert.assertTrue(apiGatewayAdmin.addDefaultAPI(provider, name, version, config));
    }

    @Test
    public void getApiForTenant() throws Exception {
        APIData apiData = new APIData();
        apiData.setContext("/abc");
        apiData.setName(name);
        apiData.setFileName(name);
        RESTAPIAdminClient restapiAdminClient = Mockito.mock(RESTAPIAdminClient.class);
        Mockito.when(restapiAdminClient.getApi(apiName, tenantDomain)).thenReturn(apiData);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminClient, null,null);
        Assert.assertNotNull(apiGatewayAdmin.getApiForTenant(provider, name, version, tenantDomain));
    }

    @Test
    public void getApi() throws Exception {
        APIData apiData = new APIData();
        apiData.setContext("/abc");
        apiData.setName(name);
        apiData.setFileName(name);
        ResourceData resourceData = new ResourceData();
        resourceData.addMethods("get");
        resourceData.setContentType("application/json");
        resourceData.setUriTemplate("/*");
        apiData.addResources(resourceData);
        RESTAPIAdminClient restapiAdminClient = Mockito.mock(RESTAPIAdminClient.class);
        Mockito.when(restapiAdminClient.getApi(apiName)).thenReturn(apiData);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminClient, null,null);
        Assert.assertNotNull(apiGatewayAdmin.getApi(provider, name, version));
    }

    @Test
    public void getDefaultApiForTenant() throws Exception {
        APIData apiData = new APIData();
        apiData.setContext("/abc");
        apiData.setName(name);
        apiData.setFileName(name);
        RESTAPIAdminClient restapiAdminClient = Mockito.mock(RESTAPIAdminClient.class);
        Mockito.when(restapiAdminClient.getApi(apiDefaultName, tenantDomain)).thenReturn(apiData);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminClient, null,null);
        Assert.assertNotNull(apiGatewayAdmin.getDefaultApiForTenant(provider, name, version, tenantDomain));
    }

    @Test
    public void getDefaultApi() throws Exception {
        APIData apiData = new APIData();
        apiData.setContext("/abc");
        apiData.setName(name);
        apiData.setFileName(name);
        RESTAPIAdminClient restapiAdminClient = Mockito.mock(RESTAPIAdminClient.class);
        Mockito.when(restapiAdminClient.getApi(apiDefaultName)).thenReturn(apiData);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminClient, null,null);
        Assert.assertNotNull(apiGatewayAdmin.getDefaultApi(provider, name, version));

    }

    @Test
    public void updateApiForTenant() throws Exception {
        RESTAPIAdminClient restapiAdminClient = Mockito.mock(RESTAPIAdminClient.class);
        Mockito.when(restapiAdminClient.updateApi(apiName, config, tenantDomain)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminClient, null,null);
        Assert.assertTrue(apiGatewayAdmin.updateApiForTenant(provider, name, version, config, tenantDomain));
    }

    @Test
    public void updateApi() throws Exception {
        RESTAPIAdminClient restapiAdminClient = Mockito.mock(RESTAPIAdminClient.class);
        Mockito.when(restapiAdminClient.updateApi(apiName, config)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminClient, null,null);
        Assert.assertTrue(apiGatewayAdmin.updateApi(provider, name, version, config));
    }

    @Test
    public void updateApiForInlineScriptForTenant() throws Exception {
        RESTAPIAdminClient restapiAdminClient = Mockito.mock(RESTAPIAdminClient.class);
        Mockito.when(restapiAdminClient.updateApi(apiName, config, tenantDomain)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminClient, null,null);
        Assert.assertTrue(apiGatewayAdmin.updateApiForInlineScriptForTenant(provider, name, version, config,
                tenantDomain));
    }

    @Test
    public void updateApiForInlineScript() throws Exception {
        RESTAPIAdminClient restapiAdminClient = Mockito.mock(RESTAPIAdminClient.class);
        Mockito.when(restapiAdminClient.updateApi(apiName, config)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminClient, null,null);
        Assert.assertTrue(apiGatewayAdmin.updateApiForInlineScript(provider, name, version, config));

    }

    @Test
    public void updateDefaultApiForTenant() throws Exception {
        RESTAPIAdminClient restapiAdminClient = Mockito.mock(RESTAPIAdminClient.class);
        Mockito.when(restapiAdminClient.updateApi(apiDefaultName, config, tenantDomain)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminClient, null,null);
        Assert.assertTrue(apiGatewayAdmin.updateDefaultApiForTenant(provider, name, version, config, tenantDomain));
    }

    @Test
    public void updateDefaultApi() throws Exception {
        RESTAPIAdminClient restapiAdminClient = Mockito.mock(RESTAPIAdminClient.class);
        Mockito.when(restapiAdminClient.updateApi(apiDefaultName, config)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminClient, null,null);
        Assert.assertTrue(apiGatewayAdmin.updateDefaultApi(provider, name, version, config));
    }

    @Test
    public void deleteApiForTenant() throws Exception {
        RESTAPIAdminClient restapiAdminClient = Mockito.mock(RESTAPIAdminClient.class);
        Mockito.when(restapiAdminClient.deleteApi(apiName, tenantDomain)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminClient, null,null);
        Assert.assertTrue(apiGatewayAdmin.deleteApiForTenant(provider, name, version, tenantDomain));
    }

    @Test
    public void deleteApi() throws Exception {
        RESTAPIAdminClient restapiAdminClient = Mockito.mock(RESTAPIAdminClient.class);
        Mockito.when(restapiAdminClient.deleteApi(apiName)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminClient, null,null);
        Assert.assertTrue(apiGatewayAdmin.deleteApi(provider, name, version));
    }

    @Test
    public void deleteDefaultApiForTenant() throws Exception {
        RESTAPIAdminClient restapiAdminClient = Mockito.mock(RESTAPIAdminClient.class);
        Mockito.when(restapiAdminClient.deleteApi(apiDefaultName, tenantDomain)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminClient, null,null);
        Assert.assertTrue(apiGatewayAdmin.deleteDefaultApiForTenant(provider, name, version, tenantDomain));
    }

    @Test
    public void deleteDefaultApi() throws Exception {
        RESTAPIAdminClient restapiAdminClient = Mockito.mock(RESTAPIAdminClient.class);
        Mockito.when(restapiAdminClient.deleteApi(apiDefaultName)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminClient, null,null);
        Assert.assertTrue(apiGatewayAdmin.deleteDefaultApi(provider, name, version));
    }

    @Test
    public void addEndpoint() throws Exception {
        String endpointData = "<endpoint></endpoint>";
        EndpointAdminServiceClient endpointAdminServiceClient = Mockito.mock(EndpointAdminServiceClient.class);
        Mockito.when(endpointAdminServiceClient.addEndpoint(Mockito.anyString())).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(null, endpointAdminServiceClient,
                null);
        Assert.assertTrue(apiGatewayAdmin.addEndpoint(endpointData));
    }

    @Test
    public void addEndpointForTenant() throws Exception {
        String endpointData = "<endpoint></endpoint>";
        EndpointAdminServiceClient endpointAdminServiceClient = Mockito.mock(EndpointAdminServiceClient.class);
        Mockito.when(endpointAdminServiceClient.addEndpoint(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(null, endpointAdminServiceClient,
                null);
        Assert.assertTrue(apiGatewayAdmin.addEndpointForTenant(endpointData, tenantDomain));
    }

    @Test
    public void deleteEndpoint() throws Exception {
        String endpointName = "PizzaShackAPI--v1.0.0_APIproductionEndpoint";
        EndpointAdminServiceClient endpointAdminServiceClient = Mockito.mock(EndpointAdminServiceClient.class);
        Mockito.when(endpointAdminServiceClient.deleteEndpoint(Mockito.anyString())).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(null, endpointAdminServiceClient,
                null);
        Assert.assertTrue(apiGatewayAdmin.deleteEndpoint(endpointName));
    }

    @Test
    public void deleteEndpointForTenant() throws Exception {
        String endpointName = "PizzaShackAPI--v1.0.0_APIproductionEndpoint";
        EndpointAdminServiceClient endpointAdminServiceClient = Mockito.mock(EndpointAdminServiceClient.class);
        Mockito.when(endpointAdminServiceClient.deleteEndpoint(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(null, endpointAdminServiceClient,
                null);
        Assert.assertTrue(apiGatewayAdmin.deleteEndpointForTenant(endpointName, tenantDomain));
    }

    @Test
    public void removeEndpointsToUpdate() throws Exception {
        EndpointAdminServiceClient endpointAdminServiceClient = Mockito.mock(EndpointAdminServiceClient.class);
        Mockito.when(endpointAdminServiceClient.removeEndpointsToUpdate(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString())).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(null, endpointAdminServiceClient,
                null);
        Assert.assertTrue(apiGatewayAdmin.removeEndpointsToUpdate(name, version, tenantDomain));
    }

    @Test
    public void addSequence() throws Exception {
        String sequence = "<api></api>";
        RESTAPIAdminClient restapiAdminClient = Mockito.mock(RESTAPIAdminClient.class);
        EndpointAdminServiceClient endpointAdminServiceClient = Mockito.mock(EndpointAdminServiceClient.class);
        SequenceAdminServiceClient sequenceAdminServiceClient = Mockito.mock(SequenceAdminServiceClient.class);
        Mockito.doNothing().when(sequenceAdminServiceClient).addSequence(Mockito.any(OMElement.class));
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminClient, endpointAdminServiceClient,
                sequenceAdminServiceClient);
        Assert.assertTrue(apiGatewayAdmin.addSequence(sequence));
    }

    @Test
    public void addSequenceForTenant() throws Exception {
        String sequence = "<api></api>";
        RESTAPIAdminClient restapiAdminClient = Mockito.mock(RESTAPIAdminClient.class);
        EndpointAdminServiceClient endpointAdminServiceClient = Mockito.mock(EndpointAdminServiceClient.class);
        SequenceAdminServiceClient sequenceAdminServiceClient = Mockito.mock(SequenceAdminServiceClient.class);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminClient, endpointAdminServiceClient,
                sequenceAdminServiceClient);
        Assert.assertTrue(apiGatewayAdmin.addSequenceForTenant(sequence, tenantDomain));
    }

    @Test
    public void deleteSequence() throws Exception {
        RESTAPIAdminClient restapiAdminClient = Mockito.mock(RESTAPIAdminClient.class);
        EndpointAdminServiceClient endpointAdminServiceClient = Mockito.mock(EndpointAdminServiceClient.class);
        SequenceAdminServiceClient sequenceAdminServiceClient = Mockito.mock(SequenceAdminServiceClient.class);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminClient, endpointAdminServiceClient,
                sequenceAdminServiceClient);
        apiGatewayAdmin.deleteSequence("name");
    }

    @Test
    public void deleteSequenceForTenant() throws Exception {
        RESTAPIAdminClient restapiAdminClient = Mockito.mock(RESTAPIAdminClient.class);
        EndpointAdminServiceClient endpointAdminServiceClient = Mockito.mock(EndpointAdminServiceClient.class);
        SequenceAdminServiceClient sequenceAdminServiceClient = Mockito.mock(SequenceAdminServiceClient.class);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminClient, endpointAdminServiceClient,
                sequenceAdminServiceClient);
        apiGatewayAdmin.deleteSequenceForTenant("name", tenantDomain);
    }

    @Test
    public void getSequence() throws Exception {
        RESTAPIAdminClient restapiAdminClient = Mockito.mock(RESTAPIAdminClient.class);
        EndpointAdminServiceClient endpointAdminServiceClient = Mockito.mock(EndpointAdminServiceClient.class);
        SequenceAdminServiceClient sequenceAdminServiceClient = Mockito.mock(SequenceAdminServiceClient.class);
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMElement test1 = fac.createOMElement("test1", "", "");
        Mockito.when(sequenceAdminServiceClient.getSequence(name)).thenReturn(test1);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminClient, endpointAdminServiceClient,
                sequenceAdminServiceClient);
        Assert.assertEquals(apiGatewayAdmin.getSequence(name), test1);
    }

    @Test
    public void getSequenceForTenant() throws Exception {
        RESTAPIAdminClient restapiAdminClient = Mockito.mock(RESTAPIAdminClient.class);
        EndpointAdminServiceClient endpointAdminServiceClient = Mockito.mock(EndpointAdminServiceClient.class);
        SequenceAdminServiceClient sequenceAdminServiceClient = Mockito.mock(SequenceAdminServiceClient.class);
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMElement test1 = fac.createOMElement("test1", "", "");
        Mockito.when(sequenceAdminServiceClient.getSequenceForTenant(name, tenantDomain)).thenReturn(test1);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminClient, endpointAdminServiceClient,
                sequenceAdminServiceClient);
        Assert.assertEquals(apiGatewayAdmin.getSequenceForTenant(name, tenantDomain), test1);
    }

    @Test
    public void isExistingSequence() throws Exception {
        RESTAPIAdminClient restapiAdminClient = Mockito.mock(RESTAPIAdminClient.class);
        EndpointAdminServiceClient endpointAdminServiceClient = Mockito.mock(EndpointAdminServiceClient.class);
        SequenceAdminServiceClient sequenceAdminServiceClient = Mockito.mock(SequenceAdminServiceClient.class);
        Mockito.when(sequenceAdminServiceClient.isExistingSequence(name)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminClient, endpointAdminServiceClient,
                sequenceAdminServiceClient);
        Assert.assertEquals(apiGatewayAdmin.isExistingSequence(name), true);
    }

    @Test
    public void isExistingSequenceForTenant() throws Exception {
        RESTAPIAdminClient restapiAdminClient = Mockito.mock(RESTAPIAdminClient.class);
        EndpointAdminServiceClient endpointAdminServiceClient = Mockito.mock(EndpointAdminServiceClient.class);
        SequenceAdminServiceClient sequenceAdminServiceClient = Mockito.mock(SequenceAdminServiceClient.class);
        Mockito.when(sequenceAdminServiceClient.isExistingSequenceForTenant(name, tenantDomain)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminClient, endpointAdminServiceClient,
                sequenceAdminServiceClient);
        Assert.assertEquals(apiGatewayAdmin.isExistingSequenceForTenant(name, tenantDomain), true);
    }

    @Test
    public void doEncryption() throws Exception {
        RESTAPIAdminClient restapiAdminClient = Mockito.mock(RESTAPIAdminClient.class);
        EndpointAdminServiceClient endpointAdminServiceClient = Mockito.mock(EndpointAdminServiceClient.class);
        SequenceAdminServiceClient sequenceAdminServiceClient = Mockito.mock(SequenceAdminServiceClient.class);
        MediationSecurityAdminServiceClient mediationSecurityAdminServiceClient = Mockito.mock
                (MediationSecurityAdminServiceClient.class);
        Mockito.when(sequenceAdminServiceClient.isExistingSequenceForTenant(name, tenantDomain)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminClient, endpointAdminServiceClient,
                sequenceAdminServiceClient, mediationSecurityAdminServiceClient);
        Mockito.when(mediationSecurityAdminServiceClient.doEncryption("abcde")).thenReturn("defg===");
        Assert.assertEquals(apiGatewayAdmin.doEncryption(tenantDomain, "wso2carbon", "abcde"),
                "defg===");
    }
}