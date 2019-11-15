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
import org.wso2.carbon.apimgt.gateway.utils.EndpointAdminServiceProxy;
import org.wso2.carbon.apimgt.gateway.utils.MediationSecurityAdminServiceProxy;
import org.wso2.carbon.apimgt.gateway.utils.RESTAPIAdminServiceProxy;
import org.wso2.carbon.apimgt.gateway.utils.SequenceAdminServiceProxy;
import org.wso2.carbon.rest.api.APIData;
import org.wso2.carbon.rest.api.ResourceData;

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
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = Mockito.mock(RESTAPIAdminServiceProxy.class);
        restapiAdminServiceProxy.setTenantDomain(tenantDomain);
        Mockito.when(restapiAdminServiceProxy.addApi(config)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminServiceProxy, null, null);
        Assert.assertTrue(apiGatewayAdmin.addApiForTenant(provider, name, version, config, tenantDomain));
    }

    @Test
    public void addApi() throws Exception {
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = Mockito.mock(RESTAPIAdminServiceProxy.class);
        Mockito.when(restapiAdminServiceProxy.addApi(config)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminServiceProxy, null,null);
        Assert.assertTrue(apiGatewayAdmin.addApi(provider, name, version, config));
    }

    @Test
    public void addPrototypeApiScriptImplForTenant() throws Exception {
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = Mockito.mock(RESTAPIAdminServiceProxy.class);
        restapiAdminServiceProxy.setTenantDomain(tenantDomain);
        Mockito.when(restapiAdminServiceProxy.addApi(config)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminServiceProxy, null,null);
        Assert.assertTrue(apiGatewayAdmin.addPrototypeApiScriptImplForTenant(provider, name, version, config,
                tenantDomain));
    }

    @Test
    public void addPrototypeApiScriptImpl() throws Exception {
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = Mockito.mock(RESTAPIAdminServiceProxy.class);
        Mockito.when(restapiAdminServiceProxy.addApi(config)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminServiceProxy, null,null);
        Assert.assertTrue(apiGatewayAdmin.addPrototypeApiScriptImpl(provider, name, version, config));
    }

    @Test
    public void addDefaultAPIForTenant() throws Exception {
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = Mockito.mock(RESTAPIAdminServiceProxy.class);
        restapiAdminServiceProxy.setTenantDomain(tenantDomain);
        Mockito.when(restapiAdminServiceProxy.addApi(config)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminServiceProxy, null,null);
        Assert.assertTrue(apiGatewayAdmin.addDefaultAPIForTenant(provider, name, version, config, tenantDomain));
    }

    @Test
    public void addDefaultAPI() throws Exception {
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = Mockito.mock(RESTAPIAdminServiceProxy.class);
        Mockito.when(restapiAdminServiceProxy.addApi(config)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminServiceProxy, null,null);
        Assert.assertTrue(apiGatewayAdmin.addDefaultAPI(provider, name, version, config));
    }

    @Test
    public void getApiForTenant() throws Exception {
        APIData apiData = new APIData();
        apiData.setContext("/abc");
        apiData.setName(name);
        apiData.setFileName(name);
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = Mockito.mock(RESTAPIAdminServiceProxy.class);
        restapiAdminServiceProxy.setTenantDomain(tenantDomain);
        Mockito.when(restapiAdminServiceProxy.getApi(apiName)).thenReturn(apiData);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminServiceProxy, null,null);
        Assert.assertNotNull(apiGatewayAdmin.getApiForTenant(provider, name, version, tenantDomain));
    }

    @Test
    public void getApi() throws Exception {
        APIData apiData = new APIData();
        apiData.setContext("/abc");
        apiData.setName(name);
        apiData.setFileName(name);
        ResourceData resourceData = new ResourceData();
        resourceData.setMethods(new String[]{"get"});
        resourceData.setContentType("application/json");
        resourceData.setUriTemplate("/*");
        apiData.setResources(new ResourceData[]{resourceData});
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = Mockito.mock(RESTAPIAdminServiceProxy.class);
        Mockito.when(restapiAdminServiceProxy.getApi(apiName)).thenReturn(apiData);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminServiceProxy, null,null);
        Assert.assertNotNull(apiGatewayAdmin.getApi(provider, name, version));
    }

    @Test
    public void getDefaultApiForTenant() throws Exception {
        APIData apiData = new APIData();
        apiData.setContext("/abc");
        apiData.setName(name);
        apiData.setFileName(name);
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = Mockito.mock(RESTAPIAdminServiceProxy.class);
        restapiAdminServiceProxy.setTenantDomain(tenantDomain);
        Mockito.when(restapiAdminServiceProxy.getApi(apiDefaultName)).thenReturn(apiData);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminServiceProxy, null,null);
        Assert.assertNotNull(apiGatewayAdmin.getDefaultApiForTenant(provider, name, version, tenantDomain));
    }

    @Test
    public void getDefaultApi() throws Exception {
        APIData apiData = new APIData();
        apiData.setContext("/abc");
        apiData.setName(name);
        apiData.setFileName(name);
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = Mockito.mock(RESTAPIAdminServiceProxy.class);
        Mockito.when(restapiAdminServiceProxy.getApi(apiDefaultName)).thenReturn(apiData);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminServiceProxy, null,null);
        Assert.assertNotNull(apiGatewayAdmin.getDefaultApi(provider, name, version));

    }

    @Test
    public void updateApiForTenant() throws Exception {
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = Mockito.mock(RESTAPIAdminServiceProxy.class);
        restapiAdminServiceProxy.setTenantDomain(tenantDomain);
        Mockito.when(restapiAdminServiceProxy.updateApi(apiName, config)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminServiceProxy, null,null);
        Assert.assertTrue(apiGatewayAdmin.updateApiForTenant(provider, name, version, config, tenantDomain));
    }

    @Test
    public void updateApi() throws Exception {
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = Mockito.mock(RESTAPIAdminServiceProxy.class);
        Mockito.when(restapiAdminServiceProxy.updateApi(apiName, config)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminServiceProxy, null,null);
        Assert.assertTrue(apiGatewayAdmin.updateApi(provider, name, version, config));
    }

    @Test
    public void updateApiForInlineScriptForTenant() throws Exception {
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = Mockito.mock(RESTAPIAdminServiceProxy.class);
        restapiAdminServiceProxy.setTenantDomain(tenantDomain);
        Mockito.when(restapiAdminServiceProxy.updateApi(apiName, config)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminServiceProxy, null,null);
        Assert.assertTrue(apiGatewayAdmin.updateApiForInlineScriptForTenant(provider, name, version, config,
                tenantDomain));
    }

    @Test
    public void updateApiForInlineScript() throws Exception {
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = Mockito.mock(RESTAPIAdminServiceProxy.class);
        Mockito.when(restapiAdminServiceProxy.updateApi(apiName, config)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminServiceProxy, null,null);
        Assert.assertTrue(apiGatewayAdmin.updateApiForInlineScript(provider, name, version, config));

    }

    @Test
    public void updateDefaultApiForTenant() throws Exception {
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = Mockito.mock(RESTAPIAdminServiceProxy.class);
        restapiAdminServiceProxy.setTenantDomain(tenantDomain);
        Mockito.when(restapiAdminServiceProxy.updateApi(apiDefaultName, config)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminServiceProxy, null,null);
        Assert.assertTrue(apiGatewayAdmin.updateDefaultApiForTenant(provider, name, version, config, tenantDomain));
    }

    @Test
    public void updateDefaultApi() throws Exception {
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = Mockito.mock(RESTAPIAdminServiceProxy.class);
        Mockito.when(restapiAdminServiceProxy.updateApi(apiDefaultName, config)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminServiceProxy, null,null);
        Assert.assertTrue(apiGatewayAdmin.updateDefaultApi(provider, name, version, config));
    }

    @Test
    public void deleteApiForTenant() throws Exception {
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = Mockito.mock(RESTAPIAdminServiceProxy.class);
        restapiAdminServiceProxy.setTenantDomain(tenantDomain);
        Mockito.when(restapiAdminServiceProxy.deleteApi(apiName)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminServiceProxy, null,null);
        Assert.assertTrue(apiGatewayAdmin.deleteApiForTenant(provider, name, version, tenantDomain));
    }

    @Test
    public void deleteApi() throws Exception {
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = Mockito.mock(RESTAPIAdminServiceProxy.class);
        Mockito.when(restapiAdminServiceProxy.deleteApi(apiName)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminServiceProxy, null,null);
        Assert.assertTrue(apiGatewayAdmin.deleteApi(provider, name, version));
    }

    @Test
    public void deleteDefaultApiForTenant() throws Exception {
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = Mockito.mock(RESTAPIAdminServiceProxy.class);
        restapiAdminServiceProxy.setTenantDomain(tenantDomain);
        Mockito.when(restapiAdminServiceProxy.deleteApi(apiDefaultName)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminServiceProxy, null,null);
        Assert.assertTrue(apiGatewayAdmin.deleteDefaultApiForTenant(provider, name, version, tenantDomain));
    }

    @Test
    public void deleteDefaultApi() throws Exception {
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = Mockito.mock(RESTAPIAdminServiceProxy.class);
        Mockito.when(restapiAdminServiceProxy.deleteApi(apiDefaultName)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminServiceProxy, null,null);
        Assert.assertTrue(apiGatewayAdmin.deleteDefaultApi(provider, name, version));
    }

    @Test
    public void addEndpoint() throws Exception {
        String endpointData = "<endpoint></endpoint>";
        EndpointAdminServiceProxy endpointAdminServiceProxy = Mockito.mock(EndpointAdminServiceProxy.class);
        Mockito.when(endpointAdminServiceProxy.addEndpoint(Mockito.anyString())).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(null, endpointAdminServiceProxy,
                null);
        Assert.assertTrue(apiGatewayAdmin.addEndpoint(endpointData));
    }

    @Test
    public void addEndpointForTenant() throws Exception {
        String endpointData = "<endpoint></endpoint>";
        EndpointAdminServiceProxy endpointAdminServiceProxy = Mockito.mock(EndpointAdminServiceProxy.class);
        Mockito.when(endpointAdminServiceProxy.addEndpoint(Mockito.anyString())).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(null, endpointAdminServiceProxy,
                null);
        Assert.assertTrue(apiGatewayAdmin.addEndpoint(endpointData));
    }

    @Test
    public void deleteEndpoint() throws Exception {
        String endpointName = "PizzaShackAPI--v1.0.0_APIproductionEndpoint";
        EndpointAdminServiceProxy endpointAdminServiceProxy = Mockito.mock(EndpointAdminServiceProxy.class);
        Mockito.when(endpointAdminServiceProxy.deleteEndpoint(Mockito.anyString())).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(null, endpointAdminServiceProxy,
                null);
        Assert.assertTrue(apiGatewayAdmin.deleteEndpoint(endpointName));
    }



    @Test
    public void removeEndpointsToUpdate() throws Exception {
        EndpointAdminServiceProxy endpointAdminServiceProxy = Mockito.mock(EndpointAdminServiceProxy.class);
        Mockito.when(endpointAdminServiceProxy.removeEndpointsToUpdate(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(null, endpointAdminServiceProxy,
                null);
        Assert.assertTrue(apiGatewayAdmin.removeEndpointsToUpdate(name, version, tenantDomain));
    }

    @Test
    public void addSequence() throws Exception {
        String sequence = "<api></api>";
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = Mockito.mock(RESTAPIAdminServiceProxy.class);
        EndpointAdminServiceProxy endpointAdminServiceProxy = Mockito.mock(EndpointAdminServiceProxy.class);
        SequenceAdminServiceProxy sequenceAdminServiceProxy = Mockito.mock(SequenceAdminServiceProxy.class);
        Mockito.doNothing().when(sequenceAdminServiceProxy).addSequence(Mockito.any(OMElement.class));
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminServiceProxy, endpointAdminServiceProxy,
                sequenceAdminServiceProxy);
        Assert.assertTrue(apiGatewayAdmin.addSequence(sequence));
    }

    @Test
    public void addSequenceForTenant() throws Exception {
        String sequence = "<api></api>";
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = Mockito.mock(RESTAPIAdminServiceProxy.class);
        EndpointAdminServiceProxy endpointAdminServiceProxy = Mockito.mock(EndpointAdminServiceProxy.class);
        SequenceAdminServiceProxy sequenceAdminServiceProxy = Mockito.mock(SequenceAdminServiceProxy.class);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminServiceProxy, endpointAdminServiceProxy,
                sequenceAdminServiceProxy);
        Assert.assertTrue(apiGatewayAdmin.addSequenceForTenant(sequence, tenantDomain));
    }

    @Test
    public void deleteSequence() throws Exception {
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = Mockito.mock(RESTAPIAdminServiceProxy.class);
        EndpointAdminServiceProxy endpointAdminServiceProxy = Mockito.mock(EndpointAdminServiceProxy.class);
        SequenceAdminServiceProxy sequenceAdminServiceProxy = Mockito.mock(SequenceAdminServiceProxy.class);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminServiceProxy, endpointAdminServiceProxy,
                sequenceAdminServiceProxy);
        apiGatewayAdmin.deleteSequence("name");
    }

    @Test
    public void deleteSequenceForTenant() throws Exception {
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = Mockito.mock(RESTAPIAdminServiceProxy.class);
        EndpointAdminServiceProxy endpointAdminServiceProxy = Mockito.mock(EndpointAdminServiceProxy.class);
        SequenceAdminServiceProxy sequenceAdminServiceProxy = Mockito.mock(SequenceAdminServiceProxy.class);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminServiceProxy, endpointAdminServiceProxy,
                sequenceAdminServiceProxy);
        apiGatewayAdmin.deleteSequenceForTenant("name", tenantDomain);
    }

    @Test
    public void getSequence() throws Exception {
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = Mockito.mock(RESTAPIAdminServiceProxy.class);
        EndpointAdminServiceProxy endpointAdminServiceProxy = Mockito.mock(EndpointAdminServiceProxy.class);
        SequenceAdminServiceProxy sequenceAdminServiceProxy = Mockito.mock(SequenceAdminServiceProxy.class);
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMElement test1 = fac.createOMElement("test1", "", "");
        Mockito.when(sequenceAdminServiceProxy.getSequence(name)).thenReturn(test1);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminServiceProxy, endpointAdminServiceProxy,
                sequenceAdminServiceProxy);
        Assert.assertEquals(apiGatewayAdmin.getSequence(name), test1);
    }

    @Test
    public void getSequenceForTenant() throws Exception {
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = Mockito.mock(RESTAPIAdminServiceProxy.class);
        EndpointAdminServiceProxy endpointAdminServiceProxy = Mockito.mock(EndpointAdminServiceProxy.class);
        SequenceAdminServiceProxy sequenceAdminServiceProxy = Mockito.mock(SequenceAdminServiceProxy.class);
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMElement test1 = fac.createOMElement("test1", "", "");
        Mockito.when(sequenceAdminServiceProxy.getSequence(name)).thenReturn(test1);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminServiceProxy, endpointAdminServiceProxy,
                sequenceAdminServiceProxy);
        Assert.assertEquals(apiGatewayAdmin.getSequence(name), test1);
    }

    @Test
    public void isExistingSequence() throws Exception {
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = Mockito.mock(RESTAPIAdminServiceProxy.class);
        EndpointAdminServiceProxy endpointAdminServiceProxy = Mockito.mock(EndpointAdminServiceProxy.class);
        SequenceAdminServiceProxy sequenceAdminServiceProxy = Mockito.mock(SequenceAdminServiceProxy.class);
        Mockito.when(sequenceAdminServiceProxy.isExistingSequence(name)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminServiceProxy, endpointAdminServiceProxy,
                sequenceAdminServiceProxy);
        Assert.assertEquals(apiGatewayAdmin.isExistingSequence(name), true);
    }

    @Test
    public void isExistingSequenceForTenant() throws Exception {
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = Mockito.mock(RESTAPIAdminServiceProxy.class);
        EndpointAdminServiceProxy endpointAdminServiceProxy = Mockito.mock(EndpointAdminServiceProxy.class);
        SequenceAdminServiceProxy sequenceAdminServiceProxy = Mockito.mock(SequenceAdminServiceProxy.class);
        Mockito.when(sequenceAdminServiceProxy.isExistingSequence(name)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminServiceProxy, endpointAdminServiceProxy,
                sequenceAdminServiceProxy);
        Assert.assertEquals(apiGatewayAdmin.isExistingSequenceForTenant(name, tenantDomain), true);
    }

}