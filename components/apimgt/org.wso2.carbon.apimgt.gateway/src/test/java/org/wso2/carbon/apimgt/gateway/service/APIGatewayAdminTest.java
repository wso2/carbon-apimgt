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
import org.apache.axis2.AxisFault;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;
import org.wso2.carbon.apimgt.api.gateway.GatewayPolicyDTO;
import org.wso2.carbon.apimgt.gateway.utils.EndpointAdminServiceProxy;
import org.wso2.carbon.apimgt.gateway.utils.RESTAPIAdminServiceProxy;
import org.wso2.carbon.apimgt.gateway.utils.SequenceAdminServiceProxy;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.rest.api.APIData;
import org.wso2.carbon.rest.api.ResourceData;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

public class APIGatewayAdminTest {
    String provider = "admin";
    String name = "API";
    String version = "1.0.0";
    String config = "abcdef";
    String tenantDomain = "carbon.super";
    String apiName = APIConstants.SYNAPSE_API_NAME_PREFIX + "--" + name + ":v" + version;
    String apiDefaultName = APIConstants.SYNAPSE_API_NAME_PREFIX + "--" + name;

    @Before
    public void setUp() {
        // Every test below targets "carbon.super" as the tenant to act on; establish the caller
        // as the super tenant so the tenant-access check added to APIGatewayAdmin's *ForTenant
        // methods does not affect any of them. The negative (non-super, mismatched-tenant) case
        // is covered separately below with its own caller context.
        System.setProperty("carbon.home", APIGatewayAdminTest.class.getResource("/").getFile());
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
    }

    @After
    public void tearDown() {
        PrivilegedCarbonContext.endTenantFlow();
    }

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
        Assert.assertNotNull(apiGatewayAdmin.getApiForTenant(name, version, tenantDomain));
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
        Assert.assertNotNull(apiGatewayAdmin.getApi(name, version));
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
        Assert.assertNotNull(apiGatewayAdmin.getDefaultApiForTenant(name, version, tenantDomain));
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
        Assert.assertNotNull(apiGatewayAdmin.getDefaultApi(name, version));

    }

    @Test
    public void updateApiForTenant() throws Exception {
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = Mockito.mock(RESTAPIAdminServiceProxy.class);
        restapiAdminServiceProxy.setTenantDomain(tenantDomain);
        Mockito.when(restapiAdminServiceProxy.updateApi(apiName, config)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminServiceProxy, null,null);
        Assert.assertTrue(apiGatewayAdmin.updateApiForTenant(name, version, config, tenantDomain));
    }

    @Test
    public void updateApi() throws Exception {
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = Mockito.mock(RESTAPIAdminServiceProxy.class);
        Mockito.when(restapiAdminServiceProxy.updateApi(apiName, config)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminServiceProxy, null,null);
        Assert.assertTrue(apiGatewayAdmin.updateApi(name, version, config));
    }

    @Test
    public void updateApiForInlineScriptForTenant() throws Exception {
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = Mockito.mock(RESTAPIAdminServiceProxy.class);
        restapiAdminServiceProxy.setTenantDomain(tenantDomain);
        Mockito.when(restapiAdminServiceProxy.updateApi(apiName, config)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminServiceProxy, null,null);
        Assert.assertTrue(apiGatewayAdmin.updateApiForInlineScriptForTenant(name, version, config,
                tenantDomain));
    }

    @Test
    public void updateApiForInlineScript() throws Exception {
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = Mockito.mock(RESTAPIAdminServiceProxy.class);
        Mockito.when(restapiAdminServiceProxy.updateApi(apiName, config)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminServiceProxy, null,null);
        Assert.assertTrue(apiGatewayAdmin.updateApiForInlineScript(name, version, config));

    }

    @Test
    public void updateDefaultApiForTenant() throws Exception {
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = Mockito.mock(RESTAPIAdminServiceProxy.class);
        restapiAdminServiceProxy.setTenantDomain(tenantDomain);
        Mockito.when(restapiAdminServiceProxy.updateApi(apiDefaultName, config)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminServiceProxy, null,null);
        Assert.assertTrue(apiGatewayAdmin.updateDefaultApiForTenant(name, version, config, tenantDomain));
    }

    @Test
    public void updateDefaultApi() throws Exception {
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = Mockito.mock(RESTAPIAdminServiceProxy.class);
        Mockito.when(restapiAdminServiceProxy.updateApi(apiDefaultName, config)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminServiceProxy, null,null);
        Assert.assertTrue(apiGatewayAdmin.updateDefaultApi(name, version, config));
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
        Assert.assertTrue(apiGatewayAdmin.deleteDefaultApiForTenant(name, version, tenantDomain));
    }

    @Test
    public void deleteDefaultApi() throws Exception {
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = Mockito.mock(RESTAPIAdminServiceProxy.class);
        Mockito.when(restapiAdminServiceProxy.deleteApi(apiDefaultName)).thenReturn(true);
        APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdminWrapper(restapiAdminServiceProxy, null,null);
        Assert.assertTrue(apiGatewayAdmin.deleteDefaultApi(name, version));
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

    // --- tenant-access check: not exercised by any test above, which all act as the super tenant ---

    @Test
    public void testNonSuperCallerNamingOwnTenantIsAllowed() throws Exception {

        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("tenant-a.example");
        APIGatewayAdmin.assertTenantAccessAllowed("tenant-a.example");
    }

    @Test(expected = AxisFault.class)
    public void testNonSuperCallerNamingDifferentTenantIsForbidden() throws Exception {

        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("tenant-a.example");
        APIGatewayAdmin.assertTenantAccessAllowed("tenant-b.example");
    }

    @Test
    public void testSuperTenantCallerNamingDifferentTenantIsAllowed() throws Exception {

        // setUp() already establishes the super tenant as caller.
        APIGatewayAdmin.assertTenantAccessAllowed("tenant-b.example");
    }

    // --- tenant-access check wiring: doEncryption, deploy/unDeploy API, deploy/unDeploy gateway
    // policy also take a caller-controlled target tenant and must reject a non-super caller naming
    // a different tenant, exactly like the *ForTenant methods above. The check runs before any
    // downstream proxy is touched, so no mocking is needed to observe the rejection. ---

    @Test(expected = AxisFault.class)
    public void doEncryptionRejectsDifferentTenantForNonSuperCaller() throws Exception {

        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("tenant-a.example");
        new APIGatewayAdmin().doEncryption("tenant-b.example", "alias", "secret");
    }

    @Test(expected = AxisFault.class)
    public void deployAPIRejectsDifferentTenantForNonSuperCaller() throws Exception {

        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("tenant-a.example");
        GatewayAPIDTO gatewayAPIDTO = new GatewayAPIDTO();
        gatewayAPIDTO.setTenantDomain("tenant-b.example");
        new APIGatewayAdmin().deployAPI(gatewayAPIDTO);
    }

    @Test(expected = AxisFault.class)
    public void unDeployAPIRejectsDifferentTenantForNonSuperCaller() throws Exception {

        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("tenant-a.example");
        GatewayAPIDTO gatewayAPIDTO = new GatewayAPIDTO();
        gatewayAPIDTO.setTenantDomain("tenant-b.example");
        new APIGatewayAdmin().unDeployAPI(gatewayAPIDTO);
    }

    @Test(expected = AxisFault.class)
    public void deployGatewayPolicyRejectsDifferentTenantForNonSuperCaller() throws Exception {

        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("tenant-a.example");
        GatewayPolicyDTO gatewayPolicyDTO = new GatewayPolicyDTO();
        gatewayPolicyDTO.setTenantDomain("tenant-b.example");
        new APIGatewayAdmin().deployGatewayPolicy(gatewayPolicyDTO);
    }

    @Test(expected = AxisFault.class)
    public void unDeployGatewayPolicyRejectsDifferentTenantForNonSuperCaller() throws Exception {

        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("tenant-a.example");
        GatewayPolicyDTO gatewayPolicyDTO = new GatewayPolicyDTO();
        gatewayPolicyDTO.setTenantDomain("tenant-b.example");
        new APIGatewayAdmin().unDeployGatewayPolicy(gatewayPolicyDTO);
    }
}