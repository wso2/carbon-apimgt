/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.gateway.dto.stub.APIData;
import org.wso2.carbon.apimgt.gateway.dto.stub.ResourceData;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.template.APITemplateBuilder;
import org.wso2.carbon.apimgt.impl.utils.APIGatewayAdminClient;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * APIGatewayManager test cases
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({APIGatewayManager.class, PrivilegedCarbonContext.class})
public class APIGatewayManagerTest {

    private APIManagerConfiguration config;
    private APIGatewayManager gatewayManager;
    private APIGatewayAdminClient apiGatewayAdminClient;
    private PrivilegedCarbonContext carbonContext;
    private GenericArtifact genericArtifact;
    private APIData apiData;
    private APIData defaultAPIdata;
    private APIIdentifier apiIdentifier;
    private APITemplateBuilder apiTemplateBuilder;
    private String apiName = "weatherAPI";
    private String provider = "admin";
    private String version = "v1";
    private String apiContext = "/weather";
    private String prodEnvironmentName = "production";
    private String sandBoxEnvironmentName = "sandbox";
    private String tenantDomain = "carbon.super";
    private String inSequence =
            "<sequence xmlns=\"http://ws.apache.org/ns/synapse\" name=\"in-sequence\">\n" +
                    " <log level=\"custom\">\n" +
                    "    <property name=\"Test\" value=\"API In Sequence\"/>\n" +
                    " </log>\n" +
                    "</sequence>";
    private String outSequence =
            "<sequence xmlns=\"http://ws.apache.org/ns/synapse\" name=\"out-sequence\">\n" +
                    " <log level=\"custom\">\n" +
                    "    <property name=\"Test\" value=\"API Out Sequence\"/>\n" +
                    " </log>\n" +
                    "</sequence>";
    private String faultSequence =
            "<sequence xmlns=\"http://ws.apache.org/ns/synapse\" name=\"fault-sequence\">\n" +
                    " <log level=\"custom\">\n" +
                    "    <property name=\"Test\" value=\"API Fault Sequence\"/>\n" +
                    " </log>\n" +
                    "</sequence>";
    private String prodEndpointConfig =
            "{\n" +
                    "   \"production_endpoints\":{\n" +
                    "      \"url\":\"https://localhost:9443/am/sample/pizzashack/v1/api/\",\n" +
                    "      \"config\":null\n" +
                    "   }\n" +
                    "}";
    private   String sandBoxEndpointConfig =
            "{\n" +
                    "   \"sandbox_endpoints\":{\n" +
                    "      \"url\":\"https://localhost:9443/am/sample/pizzashack/v1/api/\",\n" +
                    "      \"config\":null\n" +
                    "   }\n" +
                    "}";

    @Before
    public void init() throws Exception {
        System.setProperty("carbon.home", "");
        config = Mockito.mock(APIManagerConfiguration.class);
        carbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        apiData = Mockito.mock(APIData.class);
        defaultAPIdata = Mockito.mock(APIData.class);
        genericArtifact = Mockito.mock(GenericArtifact.class);
        apiGatewayAdminClient = Mockito.mock(APIGatewayAdminClient.class);
        apiTemplateBuilder = Mockito.mock(APITemplateBuilder.class);
        APIManagerConfigurationService apiManagerConfigurationService = new APIManagerConfigurationServiceImpl(config);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(apiManagerConfigurationService);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(carbonContext);
        PowerMockito.doNothing().when(carbonContext).setTenantDomain(tenantDomain, true);
        PowerMockito.whenNew(APIGatewayAdminClient.class).withAnyArguments().thenReturn(apiGatewayAdminClient);

        Environment prodEnvironment = new Environment();
        prodEnvironment.setApiGatewayEndpoint("http://localhost:8280/");
        prodEnvironment.setName("Production");
        prodEnvironment.setType("production");
        prodEnvironment.setServerURL("https://localhost:9443/services");
        prodEnvironment.setUserName("admin");
        prodEnvironment.setPassword("admin");

        Environment sandboxEnvironment = new Environment();
        prodEnvironment.setApiGatewayEndpoint("http://localhost:8281/");
        prodEnvironment.setName("Sandbox");
        prodEnvironment.setType("sandbox");
        prodEnvironment.setServerURL("https://localhost:9444/services");
        prodEnvironment.setUserName("admin");
        prodEnvironment.setPassword("admin");

        Map<String, Environment> environments = new HashMap<String, Environment>(0);
        environments.put(prodEnvironmentName, prodEnvironment);
        environments.put(sandBoxEnvironmentName, sandboxEnvironment);
        Mockito.when(config.getApiGatewayEnvironments()).thenReturn(environments);
        apiIdentifier = new APIIdentifier(provider, apiName, version);
        gatewayManager = APIGatewayManager.getInstance();
    }

    @Test
    public void testRemovingRESTAPIWithInSequenceFromGateway() throws AxisFault {
        API api = new API(apiIdentifier);
        api.setType("HTTP");
        api.setInSequence(inSequence);
        api.setAsPublishedDefaultVersion(true);
        Set<String> environments = new HashSet<String>();
        environments.add(prodEnvironmentName);
        environments.add(null);
        api.setEnvironments(environments);
        Mockito.when(apiGatewayAdminClient.getApi(tenantDomain, apiIdentifier)).thenReturn(apiData);
        Mockito.when(apiGatewayAdminClient.getDefaultApi(tenantDomain, apiIdentifier)).thenReturn(defaultAPIdata);
        Mockito.when(apiGatewayAdminClient.isExistingSequence(Mockito.anyString(), Mockito.anyString())).thenReturn
                (true);
        Map<String, String> failedEnvironmentsMap = gatewayManager.removeFromGateway(api, tenantDomain);
        Assert.assertEquals(failedEnvironmentsMap.size(), 0);
    }

    @Test
    public void testRemovingRESTAPIWithOutSequenceFromGateway() throws AxisFault {
        API api = new API(apiIdentifier);
        api.setType("HTTP");
        api.setOutSequence(outSequence);
        api.setAsPublishedDefaultVersion(true);
        Set<String> environments = new HashSet<String>();
        environments.add(prodEnvironmentName);
        environments.add(null);
        api.setEnvironments(environments);
        Mockito.when(apiGatewayAdminClient.getApi(tenantDomain, apiIdentifier)).thenReturn(apiData);
        Mockito.when(apiGatewayAdminClient.getDefaultApi(tenantDomain, apiIdentifier)).thenReturn(defaultAPIdata);
        Mockito.when(apiGatewayAdminClient.isExistingSequence(Mockito.anyString(), Mockito.anyString())).thenReturn
                (true);
        Map<String, String> failedEnvironmentsMap = gatewayManager.removeFromGateway(api, tenantDomain);
        Assert.assertEquals(failedEnvironmentsMap.size(), 0);
    }

    @Test
    public void testRemovingRESTAPIWithFaultSequenceFromGateway() throws AxisFault {
        API api = new API(apiIdentifier);
        api.setType("HTTP");
        api.setFaultSequence(faultSequence);
        api.setInSequence(inSequence);
        api.setAsPublishedDefaultVersion(true);
        Set<String> environments = new HashSet<String>();
        environments.add(prodEnvironmentName);
        environments.add(null);
        api.setEnvironments(environments);
        Mockito.when(apiGatewayAdminClient.getApi(null, apiIdentifier)).thenReturn(apiData);
        Mockito.when(apiGatewayAdminClient.getDefaultApi(tenantDomain, apiIdentifier)).thenReturn(defaultAPIdata);
        Mockito.when(apiGatewayAdminClient.isExistingSequence(Mockito.anyString(), Mockito.anyString())).thenReturn
                (true);
        //Set tenant domain = null, so that 'carbon.super' tenant will be loaded
        Map<String, String> failedEnvironmentsMap = gatewayManager.removeFromGateway(api, null);
        Assert.assertEquals(failedEnvironmentsMap.size(), 0);
    }

    @Test
    public void testRemovingWebSocketAPIFromGateway() throws AxisFault {
        API api = new API(apiIdentifier);
        api.setType("WS");
        api.setContext(apiContext);
        api.setInSequence(inSequence);
        api.setOutSequence(outSequence);
        api.setFaultSequence(faultSequence);
        Set<String> environments = new HashSet<String>();
        environments.add(prodEnvironmentName);
        environments.add(null);
        api.setEnvironments(environments);
        Mockito.when(apiGatewayAdminClient.getApi(tenantDomain, apiIdentifier)).thenReturn(apiData);
        Mockito.when(apiGatewayAdminClient.isExistingSequence(Mockito.anyString(), Mockito.anyString())).thenReturn
                (true);
        Map<String, String> failedEnvironmentsMap = gatewayManager.removeFromGateway(api, tenantDomain);
        Assert.assertEquals(failedEnvironmentsMap.size(), 0);
    }

    @Test
    public void testFailureToRemoveRESTAPIWhenClientFailedToDeleteAPIFromGW() throws AxisFault {
        String errorMessage = "Error while deleting API from Gateway";
        API api = new API(apiIdentifier);
        api.setType("HTTP");
        api.setContext(apiContext);
        api.setInSequence(inSequence);
        api.setOutSequence(outSequence);
        api.setFaultSequence(faultSequence);
        Set<String> environments = new HashSet<String>();
        environments.add(prodEnvironmentName);
        api.setEnvironments(environments);
        Mockito.when(apiGatewayAdminClient.getApi(tenantDomain, apiIdentifier)).thenReturn(apiData);
        Mockito.when(apiGatewayAdminClient.isExistingSequence(Mockito.anyString(), Mockito.anyString())).thenReturn
                (true);
        Mockito.doThrow(new AxisFault(errorMessage)).when(apiGatewayAdminClient).deleteApi(tenantDomain, apiIdentifier);
        Map<String, String> failedEnvironmentsMap = gatewayManager.removeFromGateway(api, tenantDomain);

        Assert.assertEquals(failedEnvironmentsMap.size(), 1);
        Assert.assertTrue(failedEnvironmentsMap.keySet().contains(prodEnvironmentName));
        Assert.assertEquals(failedEnvironmentsMap.get(prodEnvironmentName), errorMessage);
    }

    @Test
    public void testCreatingNewWebSocketAPIWithProductionEndpoint() throws GovernanceException, AxisFault {

        API api = new API(apiIdentifier);
        api.setType("WS");

        Mockito.when(genericArtifact.getAttribute(APIConstants.API_OVERVIEW_ENVIRONMENTS)).thenReturn
                (prodEnvironmentName);
        Mockito.when(genericArtifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT)).thenReturn(apiContext);
        Mockito.when(genericArtifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_CONFIG)).thenReturn
                (prodEndpointConfig);
        Mockito.when(apiGatewayAdminClient.isExistingSequence(Mockito.anyString(), Mockito.anyString())).thenReturn
                (true);
        gatewayManager.createNewWebsocketApiVersion(genericArtifact, api);
    }

    @Test
    public void testCreatingNewWebSocketAPIWithSandBoxEndpoint() throws GovernanceException, AxisFault {

        API api = new API(apiIdentifier);
        api.setType("WS");

        Mockito.when(genericArtifact.getAttribute(APIConstants.API_OVERVIEW_ENVIRONMENTS)).thenReturn
                (sandBoxEnvironmentName);
        Mockito.when(genericArtifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT)).thenReturn(apiContext);
        Mockito.when(genericArtifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_CONFIG)).thenReturn
                (sandBoxEndpointConfig);
        Mockito.when(apiGatewayAdminClient.isExistingSequence(Mockito.anyString(), Mockito.anyString())).thenReturn
                (true);
        gatewayManager.createNewWebsocketApiVersion(genericArtifact, api);
    }

    @Test
    public void testExceptionsWhileCreatingWebSocketAPI() throws Exception {

        API api = new API(apiIdentifier);
        api.setType("WS");

        //Test throwing AxisFault when it failed to deploy custom sequences of the WS API
        Mockito.when(genericArtifact.getAttribute(APIConstants.API_OVERVIEW_ENVIRONMENTS)).thenReturn
                (prodEnvironmentName);
        Mockito.when(genericArtifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT)).thenReturn(apiContext);
        Mockito.when(genericArtifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_CONFIG)).thenReturn
                (prodEndpointConfig);
        Mockito.doThrow(new AxisFault("Error while deploying sequence")).when(apiGatewayAdminClient).addSequence
                ((OMElement) Mockito.anyObject(), Mockito.anyString());
        gatewayManager.createNewWebsocketApiVersion(genericArtifact, api);

        //Test throwing APIManagerException while invalid endpoint configuration is provided
        Mockito.when(genericArtifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_CONFIG)).thenReturn("<xml/>");
        gatewayManager.createNewWebsocketApiVersion(genericArtifact, api);

        //Test throwing AxisFault when Gateway client initialisation failed
        PowerMockito.whenNew(APIGatewayAdminClient.class).withAnyArguments().thenThrow(new AxisFault
                ("Error while establishing connection with gateway endpoint"));
        gatewayManager.createNewWebsocketApiVersion(genericArtifact, api);

        //Test throwing GovernanceException when GenericArtifact attribute retrieval failed
        Mockito.doThrow(new GovernanceException("Error while deploying API in gateway")).when(genericArtifact)
                .getAttribute(APIConstants.API_OVERVIEW_ENVIRONMENTS);
        gatewayManager.createNewWebsocketApiVersion(genericArtifact, api);
    }

    @Test
    public void testRemovingDefaultAPIFromGateway() throws AxisFault {
        API api = new API(apiIdentifier);
        api.setType("HTTP");
        Set<String> environments = new HashSet<String>();
        environments.add(prodEnvironmentName);
        api.setEnvironments(environments);
        Mockito.when(apiGatewayAdminClient.getDefaultApi(tenantDomain, apiIdentifier))
                .thenReturn(defaultAPIdata);
        Map<String, String> failedEnvironmentsMap = gatewayManager.removeDefaultAPIFromGateway(api, tenantDomain);
        Assert.assertEquals(failedEnvironmentsMap.size(), 0);
    }

    @Test
    public void testFailureToRemovingDefaultAPIFromGateway() throws AxisFault {
        String errorMessage = "Error while deleting default API from gateway";
        API api = new API(apiIdentifier);
        api.setType("HTTP");
        Set<String> environments = new HashSet<String>();
        environments.add(prodEnvironmentName);
        api.setEnvironments(environments);
        Mockito.when(apiGatewayAdminClient.getDefaultApi(tenantDomain, apiIdentifier))
                .thenReturn(defaultAPIdata);
        Mockito.doThrow(new AxisFault(errorMessage)).when(apiGatewayAdminClient).deleteDefaultApi(tenantDomain,
                apiIdentifier);
        Map<String, String> failedEnvironmentsMap = gatewayManager.removeDefaultAPIFromGateway(api, tenantDomain);
        Assert.assertEquals(failedEnvironmentsMap.size(), 1);
        Assert.assertTrue(failedEnvironmentsMap.keySet().contains(prodEnvironmentName));
        Assert.assertEquals(failedEnvironmentsMap.get(prodEnvironmentName), errorMessage);
    }

    @Test
    public void testAPIIsPublished() throws AxisFault {
        //Test already published API's availability
        API api = new API(apiIdentifier);
        Mockito.when(apiGatewayAdminClient.getApi(tenantDomain, apiIdentifier)).thenReturn(apiData);
        try {
            boolean isPublished = gatewayManager.isAPIPublished(api, tenantDomain);
            Assert.assertTrue(isPublished);
        } catch (APIManagementException e) {
            Assert.fail("Unexpected APIManagementException occurred while checking, whether the API is published");
        }

        //Test non existing API's availability in gateway
        Mockito.when(apiGatewayAdminClient.getApi(tenantDomain, apiIdentifier)).thenReturn(null);
        try {
            boolean isPublished = gatewayManager.isAPIPublished(api, tenantDomain);
            Assert.assertFalse(isPublished);
        } catch (APIManagementException e) {
            Assert.fail("Unexpected APIManagementException occurred while checking, whether the API is published");
        }
    }

    @Test
    public void testFailureWhileCheckingAPIIsPublished() throws AxisFault {
        //Test already published API's availability in gateway
        API api = new API(apiIdentifier);
        Mockito.when(apiGatewayAdminClient.getApi(tenantDomain, apiIdentifier)).thenThrow(new AxisFault("Error while " +
                "checking whether the API is published"));
        try {
            boolean isPublished = gatewayManager.isAPIPublished(api, tenantDomain);
            Assert.assertFalse(isPublished);
        } catch (APIManagementException e) {
            Assert.fail("Unexpected APIManagementException occurred while checking, whether the API is published");
        }
    }

}
