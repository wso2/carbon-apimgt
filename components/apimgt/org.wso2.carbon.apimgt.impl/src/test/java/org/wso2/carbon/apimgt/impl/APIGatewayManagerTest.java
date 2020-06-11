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
import org.apache.axiom.om.util.AXIOMUtil;
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
import org.wso2.carbon.apimgt.impl.dao.CertificateMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.dto.GatewayArtifactSynchronizerProperties;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.template.APITemplateBuilder;
import org.wso2.carbon.apimgt.impl.template.APITemplateBuilderImpl;
import org.wso2.carbon.apimgt.impl.utils.APIGatewayAdminClient;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.LocalEntryAdminClient;
import org.wso2.carbon.apimgt.localentry.stub.APILocalEntryAdminStub;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.xml.stream.XMLStreamException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * APIGatewayManager test cases
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ APIGatewayManager.class, PrivilegedCarbonContext.class, APIUtil.class, CertificateMgtDAO.class })
public class APIGatewayManagerTest {

    private APIManagerConfiguration config;
    private APIGatewayManager gatewayManager;
    private APIGatewayAdminClient apiGatewayAdminClient;
    private PrivilegedCarbonContext carbonContext;
    private GenericArtifact genericArtifact;
    private APIData apiData;
    private APIData defaultAPIdata;
    private APIIdentifier apiIdentifier;
    private String apiName = "weatherAPI";
    private String apiUUId = "123455";
    private String provider = "admin";
    private String version = "v1";
    private String apiContext = "/weather";
    private String prodEnvironmentName = "Production";
    private String sandBoxEnvironmentName = "sandbox";
    private String tenantDomain = "carbon.super";
    private String inSequenceName = "in-sequence";
    private String outSequenceName = "out-sequence";
    private String faultSequenceName = "fault-sequence";
    private String swaggerDefinition = "swagger definition";
    private int tenantID = -1234;
    private String testSequenceDefinition =
            "<sequence xmlns=\"http://ws.apache.org/ns/synapse\" name=\"test-sequence\">\n"
                    + " <log level=\"custom\">\n" + "    <property name=\"Test\" value=\"Test Sequence\"/>\n"
                    + " </log>\n" + "</sequence>";
    private String prodEndpointConfig = "{\n" + "   \"production_endpoints\":{\n"
            + "      \"url\":\"https://localhost:9443/am/sample/pizzashack/v1/api/\",\n" + "      \"config\":null\n"
            + "   }\n" + "}";
    private String sandBoxEndpointConfig = "{\n" + "   \"sandbox_endpoints\":{\n"
            + "      \"url\":\"https://localhost:9443/am/sample/pizzashack/v1/api/\",\n" + "      \"config\":null\n"
            + "   }\n" + "}";

    private String localEntry = "<localEntry key=\"" + apiUUId + "\">" +
            swaggerDefinition.replaceAll("&(?!amp;)", "&amp;").
                    replaceAll("<", "&lt;").replaceAll(">", "&gt;")
            + "</localEntry>";

    @Before
    public void init() throws Exception {
        System.setProperty("carbon.home", "");
        config = Mockito.mock(APIManagerConfiguration.class);
        carbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        apiData = Mockito.mock(APIData.class);
        defaultAPIdata = Mockito.mock(APIData.class);
        genericArtifact = Mockito.mock(GenericArtifact.class);
        apiGatewayAdminClient = Mockito.mock(APIGatewayAdminClient.class);
        RealmService realmService = Mockito.mock(RealmService.class);

        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        Mockito.doReturn(tenantManager).when(realmService).getTenantManager();
        Mockito.doReturn(MultitenantConstants.SUPER_TENANT_ID).when(tenantManager).getTenantId(Mockito.anyString());
        APIManagerConfigurationService apiManagerConfigurationService = new APIManagerConfigurationServiceImpl(config);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(apiManagerConfigurationService);
        ServiceReferenceHolder.getInstance().setRealmService(realmService);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.mockStatic(CertificateMgtDAO.class);
        CertificateMgtDAO certificateMgtDAO = Mockito.mock(CertificateMgtDAO.class);
        PowerMockito.when(CertificateMgtDAO.getInstance()).thenReturn(certificateMgtDAO);
        PowerMockito.when(APIUtil.isSandboxEndpointsExists(Mockito.anyString())).thenCallRealMethod();
        PowerMockito.when(APIUtil.getSequenceExtensionName((API) Mockito.anyObject())).thenCallRealMethod();
        PowerMockito.when(APIUtil.extractEnvironmentsForAPI(Mockito.anyString())).thenCallRealMethod();
        PowerMockito.when(APIUtil.getGAConfigFromRegistry(Mockito.anyString())).thenReturn(null);

        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(carbonContext);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId()).thenReturn(tenantID);
        PowerMockito.doNothing().when(carbonContext).setTenantDomain(tenantDomain, true);
        PowerMockito.whenNew(APIGatewayAdminClient.class).withAnyArguments().thenReturn(apiGatewayAdminClient);

        Environment prodEnvironment = new Environment();
        prodEnvironment.setApiGatewayEndpoint("http://localhost:8280/");
        prodEnvironment.setName("Production");
        prodEnvironment.setType("production");
        prodEnvironment.setServerURL("https://localhost:9443/services");
        prodEnvironment.setUserName("admin");
        prodEnvironment.setPassword("admin");
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();

        Environment sandboxEnvironment = new Environment();
        sandboxEnvironment.setApiGatewayEndpoint("http://localhost:8281/");
        sandboxEnvironment.setName("Sandbox");
        sandboxEnvironment.setType("sandbox");
        sandboxEnvironment.setServerURL("https://localhost:9444/services");
        sandboxEnvironment.setUserName("admin");
        sandboxEnvironment.setPassword("admin");

        Map<String, Environment> environments = new HashMap<String, Environment>(0);
        GatewayArtifactSynchronizerProperties synchronizerProperties = new GatewayArtifactSynchronizerProperties();
        environments.put(prodEnvironmentName, prodEnvironment);
        environments.put(sandBoxEnvironmentName, sandboxEnvironment);
        Mockito.when(config.getApiGatewayEnvironments()).thenReturn(environments);
        Mockito.when(config.getApiGatewayEnvironments()).thenReturn(environments);
        Mockito.when(config.getGatewayArtifactSynchronizerProperties()).thenReturn(synchronizerProperties);
        apiIdentifier = new APIIdentifier(provider, apiName, version);
        TestUtils.initConfigurationContextService(false);
        gatewayManager = APIGatewayManager.getInstance();

        Map<String, Environment> environmentList = config.getApiGatewayEnvironments();
        Environment environment = environmentList.get(prodEnvironmentName);
        LocalEntryAdminClient localEntryAdminClient = Mockito.mock(LocalEntryAdminClient.class);
        APILocalEntryAdminStub localEntryAdminServiceStub = Mockito.mock(APILocalEntryAdminStub.class);
        PowerMockito.whenNew(LocalEntryAdminClient.class).withArguments(environment,tenantDomain).
                thenReturn(localEntryAdminClient);
        Mockito.doCallRealMethod().when(localEntryAdminClient).deleteEntry(localEntry);
        Mockito.when(localEntryAdminServiceStub.addLocalEntry(localEntry, tenantDomain)).thenReturn(true);
    }

    @Test public void testRemovingRESTAPIWithInSequenceFromGateway() throws AxisFault {
        API api = new API(apiIdentifier);
        api.setType("HTTP");
        api.setInSequence(inSequenceName);
        api.setAsPublishedDefaultVersion(true);
        Set<String> environments = new HashSet<String>();
        environments.add(prodEnvironmentName);
        environments.add(null);
        api.setEnvironments(environments);
        Mockito.when(apiGatewayAdminClient.getApi(tenantDomain, apiIdentifier)).thenReturn(apiData);
        Mockito.when(apiGatewayAdminClient.getDefaultApi(tenantDomain, apiIdentifier)).thenReturn(defaultAPIdata);
        Map<String, String> failedEnvironmentsMap = gatewayManager.removeFromGateway(api, tenantDomain);
        Assert.assertEquals(failedEnvironmentsMap.size(), 0);
    }

    @Test
    public void testRemovingRESTAPIWithOutSequenceFromGateway() throws AxisFault {
        API api = new API(apiIdentifier);
        api.setType("HTTP");
        api.setOutSequence(outSequenceName);
        api.setAsPublishedDefaultVersion(true);
        api.setSwaggerDefinition(swaggerDefinition);
        api.setUUID(apiUUId);
        Set<String> environments = new HashSet<String>();
        environments.add(prodEnvironmentName);
        environments.add(null);
        api.setEnvironments(environments);
        Mockito.when(apiGatewayAdminClient.getApi(tenantDomain, apiIdentifier)).thenReturn(apiData);
        Mockito.when(apiGatewayAdminClient.getDefaultApi(tenantDomain, apiIdentifier)).thenReturn(defaultAPIdata);
        Map<String, String> failedEnvironmentsMap = gatewayManager.removeFromGateway(api, tenantDomain);
        Assert.assertEquals(failedEnvironmentsMap.size(), 0);
    }

    @Test public void testRemovingRESTAPIWithFaultSequenceFromGateway() throws AxisFault {
        API api = new API(apiIdentifier);
        api.setType("HTTP");
        api.setFaultSequence(faultSequenceName);
        api.setInSequence(inSequenceName);
        api.setAsPublishedDefaultVersion(true);
        api.setSwaggerDefinition(swaggerDefinition);
        api.setUUID(apiUUId);
        Set<String> environments = new HashSet<String>();
        environments.add(prodEnvironmentName);
        environments.add(null);
        api.setEnvironments(environments);
        Mockito.when(apiGatewayAdminClient.getApi(null, apiIdentifier)).thenReturn(apiData);
        Mockito.when(apiGatewayAdminClient.getDefaultApi(tenantDomain, apiIdentifier)).thenReturn(defaultAPIdata);

        //Set tenant domain = null, so that 'carbon.super' tenant will be loaded
        Map<String, String> failedEnvironmentsMap = gatewayManager.removeFromGateway(api, null);
        Assert.assertEquals(failedEnvironmentsMap.size(), 0);
    }

    @Test public void testRemovingWebSocketAPIFromGateway() throws AxisFault {
        API api = new API(apiIdentifier);
        api.setType("WS");
        api.setContext(apiContext);
        api.setInSequence(inSequenceName);
        api.setOutSequence(outSequenceName);
        api.setFaultSequence(faultSequenceName);
        api.setSwaggerDefinition(swaggerDefinition);
        api.setUUID(apiUUId);
        Set<String> environments = new HashSet<String>();
        environments.add(prodEnvironmentName);
        environments.add(null);
        api.setEnvironments(environments);
        Mockito.when(apiGatewayAdminClient.getApi(tenantDomain, apiIdentifier)).thenReturn(apiData);
        Map<String, String> failedEnvironmentsMap = gatewayManager.removeFromGateway(api, tenantDomain);
        Assert.assertEquals(failedEnvironmentsMap.size(), 0);
    }


    @Test public void testCreatingNewWebSocketAPIWithProductionEndpoint() throws GovernanceException, AxisFault {
        API api = new API(apiIdentifier);
        api.setType("WS");
        Mockito.when(genericArtifact.getAttribute(APIConstants.API_OVERVIEW_ENVIRONMENTS))
                .thenReturn(prodEnvironmentName);
        Mockito.when(genericArtifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT)).thenReturn(apiContext);
        Mockito.when(genericArtifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_CONFIG))
                .thenReturn(prodEndpointConfig);
        gatewayManager.createNewWebsocketApiVersion(genericArtifact, api);
    }

    @Test public void testCreatingNewWebSocketAPIWithSandBoxEndpoint() throws GovernanceException, AxisFault {
        API api = new API(apiIdentifier);
        api.setType("WS");
        Mockito.when(genericArtifact.getAttribute(APIConstants.API_OVERVIEW_ENVIRONMENTS))
                .thenReturn(sandBoxEnvironmentName);
        Mockito.when(genericArtifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT)).thenReturn(apiContext);
        Mockito.when(genericArtifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_CONFIG))
                .thenReturn(sandBoxEndpointConfig);
        gatewayManager.createNewWebsocketApiVersion(genericArtifact, api);
    }

    @Test public void testExceptionsWhileCreatingWebSocketAPI() throws Exception {
        API api = new API(apiIdentifier);
        api.setType("WS");

        //Test throwing AxisFault when it failed to deploy custom sequences of the WS API
        Mockito.when(genericArtifact.getAttribute(APIConstants.API_OVERVIEW_ENVIRONMENTS))
                .thenReturn(prodEnvironmentName);
        Mockito.when(genericArtifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT)).thenReturn(apiContext);
        Mockito.when(genericArtifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_CONFIG))
                .thenReturn(prodEndpointConfig);
        gatewayManager.createNewWebsocketApiVersion(genericArtifact, api);

        //Test throwing APIManagerException while invalid endpoint configuration is provided
        Mockito.when(genericArtifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_CONFIG)).thenReturn("<xml/>");
        gatewayManager.createNewWebsocketApiVersion(genericArtifact, api);

        //Test throwing AxisFault when Gateway client initialisation failed
        PowerMockito.whenNew(APIGatewayAdminClient.class).withAnyArguments()
                .thenThrow(new AxisFault("Error while establishing connection with gateway endpoint"));
        gatewayManager.createNewWebsocketApiVersion(genericArtifact, api);

        //Test throwing GovernanceException when GenericArtifact attribute retrieval failed
        Mockito.doThrow(new GovernanceException("Error while deploying API in gateway")).when(genericArtifact)
                .getAttribute(APIConstants.API_OVERVIEW_ENVIRONMENTS);
        gatewayManager.createNewWebsocketApiVersion(genericArtifact, api);
    }

    @Test public void testRemovingDefaultAPIFromGateway() throws AxisFault {
        API api = new API(apiIdentifier);
        api.setType("HTTP");
        Set<String> environments = new HashSet<String>();
        environments.add(prodEnvironmentName);
        api.setEnvironments(environments);
        Mockito.when(apiGatewayAdminClient.getDefaultApi(tenantDomain, apiIdentifier)).thenReturn(defaultAPIdata);
        Map<String, String> failedEnvironmentsMap = gatewayManager.removeDefaultAPIFromGateway(api, tenantDomain);
        Assert.assertEquals(failedEnvironmentsMap.size(), 0);
    }

    @Test public void testFailureToRemovingDefaultAPIFromGateway() throws AxisFault {
        String errorMessage = "Error while deleting default API from gateway";
        API api = new API(apiIdentifier);
        api.setType("HTTP");
        Set<String> environments = new HashSet<String>();
        environments.add(prodEnvironmentName);
        api.setEnvironments(environments);
        Mockito.when(apiGatewayAdminClient.getDefaultApi(tenantDomain, apiIdentifier)).thenReturn(defaultAPIdata);
        Mockito.doThrow(new AxisFault(errorMessage)).when(apiGatewayAdminClient)
                .deleteDefaultApi(tenantDomain, apiIdentifier);
        Map<String, String> failedEnvironmentsMap = gatewayManager.removeDefaultAPIFromGateway(api, tenantDomain);
        Assert.assertEquals(failedEnvironmentsMap.size(), 1);
        Assert.assertTrue(failedEnvironmentsMap.keySet().contains(prodEnvironmentName));
        Assert.assertEquals(failedEnvironmentsMap.get(prodEnvironmentName), errorMessage);
    }

    @Test public void testAPIIsPublished() throws AxisFault {
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

    @Test public void testFailureWhileCheckingAPIIsPublished() throws AxisFault {
        API api = new API(apiIdentifier);
        Mockito.when(apiGatewayAdminClient.getApi(tenantDomain, apiIdentifier))
                .thenThrow(new AxisFault("Error while " + "checking whether the API is published"));
        try {
            boolean isPublished = gatewayManager.isAPIPublished(api, tenantDomain);
            Assert.assertFalse(isPublished);
        } catch (APIManagementException e) {
            Assert.fail("Unexpected APIManagementException occurred while checking, whether the API is published");
        }
    }

    @Test public void testRetrievingDigestAuthAPIEndpointSecurityType() throws AxisFault {
        API api = new API(apiIdentifier);
        ResourceData resourceData = new ResourceData();
        resourceData.setInSeqXml("<sequence><DigestAuthMediator/></sequence>");
        ResourceData[] resources = { null, new ResourceData(), resourceData };
        Mockito.when(apiData.getResources()).thenReturn(resources);
        Mockito.when(apiGatewayAdminClient.getApi(tenantDomain, apiIdentifier)).thenReturn(apiData);
        try {
            //When 'DigestAuthMediator' is present in In sequence configuration, 'DigestAuth' should be retrieved as
            //the endpoint security type
            String endpointSecurityType = gatewayManager.getAPIEndpointSecurityType(api, tenantDomain);
            Assert.assertEquals(endpointSecurityType, "DigestAuth");
        } catch (APIManagementException e) {
            Assert.fail("Unexpected APIManagementException occurred while retrieving endpoint security type");
        }
    }

    @Test public void testFailureWhileRetrievingEndpointSecurityType() throws AxisFault {
        API api = new API(apiIdentifier);
        Mockito.when(apiGatewayAdminClient.getApi(tenantDomain, apiIdentifier))
                .thenThrow(new AxisFault("Error while " + "retrieving endpoint security type"));
        try {
            //When 'DigestAuthMediator' is present in In sequence configuration, 'DigestAuth' should be retrieved as
            //the endpoint security type
            String endpointSecurityType = gatewayManager.getAPIEndpointSecurityType(api, tenantDomain);
            Assert.assertEquals(endpointSecurityType, "BasicAuth");
        } catch (APIManagementException e) {
            Assert.fail("Unexpected APIManagementException occurred while retrieving endpoint security type");
        }
    }

    @Test public void testPublishingNewAPIToGateway()
            throws Exception {

        API api = new API(apiIdentifier);
        api.setType("HTTP");
        api.setAsPublishedDefaultVersion(true);
        api.setAsDefaultVersion(true);
        APITemplateBuilder apiTemplateBuilder = new APITemplateBuilderImpl(api);
        Set<String> environments = new HashSet<String>();
        environments.add(prodEnvironmentName);
        environments.add(null);
        Mockito.when(apiGatewayAdminClient.getApi(tenantDomain, apiIdentifier)).thenReturn(null);

        //Test when environments are not defined for API
        Assert.assertEquals(gatewayManager.publishToGateway(api, apiTemplateBuilder, tenantDomain).size(), 0);

        //Test adding LocalEntry
        api.setEnvironments(environments);
        api.setEndpointConfig(sandBoxEndpointConfig);
        api.setSwaggerDefinition(swaggerDefinition);
        api.setUUID(apiUUId);

        //Test when API's environment endpoint configuration is not available
        Assert.assertEquals(gatewayManager.publishToGateway(api, apiTemplateBuilder, tenantDomain).size(), 0);

        //Test deploying 'INLINE' type REST API to gateway
        api.setImplementation("INLINE");
        api.setEndpointConfig(prodEndpointConfig);
        Assert.assertEquals(gatewayManager.publishToGateway(api, apiTemplateBuilder, tenantDomain).size(), 0);

        //Test deploying 'ENDPOINT' type REST API to gateway
        api.setImplementation("ENDPOINT");
        Assert.assertEquals(gatewayManager.publishToGateway(api, apiTemplateBuilder, tenantDomain).size(), 0);

        //Test deploying default version of the API and updating the existing default API
        Mockito.when(apiGatewayAdminClient.getDefaultApi(tenantDomain, apiIdentifier)).thenReturn(defaultAPIdata);
        Assert.assertEquals(gatewayManager.publishToGateway(api, apiTemplateBuilder, tenantDomain).size(), 0);

        //Test deploying API, if secure vault is enabled
        api.setEndpointSecured(true);
        Mockito.when(config.getFirstProperty(APIConstants.API_SECUREVAULT_ENABLE)).thenReturn("true");
        Assert.assertEquals(gatewayManager.publishToGateway(api, apiTemplateBuilder, tenantDomain).size(), 0);

        //Test publishing WebSocket API
        api.setType("WS");
        Assert.assertEquals(gatewayManager.publishToGateway(api, apiTemplateBuilder, tenantDomain).size(), 0);

    }

    @Test public void testPublishingNewRESTAPIWithAPIFaultSequence()
            throws AxisFault, APIManagementException, XMLStreamException, RegistryException {
        API api = new API(apiIdentifier);
        api.setType("HTTP");
        api.setAsPublishedDefaultVersion(true);
        api.setImplementation("ENDPOINT");
        api.setEndpointConfig(prodEndpointConfig);
        api.setFaultSequence(faultSequenceName);
        api.setAsDefaultVersion(true);
        api.setSwaggerDefinition(swaggerDefinition);
        api.setUUID(apiUUId);
        APITemplateBuilder apiTemplateBuilder = new APITemplateBuilderImpl(api);
        Set<String> environments = new HashSet<String>();
        environments.add(prodEnvironmentName);
        environments.add(null);
        api.setEnvironments(environments);
        Mockito.when(apiGatewayAdminClient.getApi(tenantDomain, apiIdentifier)).thenReturn(null);
        PowerMockito.when(APIUtil.getCustomSequence(faultSequenceName, tenantID, "fault", api.getId()))
                .thenReturn(AXIOMUtil.stringToOM(testSequenceDefinition));

        //Test deploying per API defined fault sequence with API
        PowerMockito.when(APIUtil.isPerAPISequence(faultSequenceName, tenantID, apiIdentifier, "fault"))
                .thenReturn(true);
        Assert.assertEquals(gatewayManager.publishToGateway(api, apiTemplateBuilder, tenantDomain).size(), 0);

        //Test deploying newly defined global sequence with API
        PowerMockito.when(APIUtil.isPerAPISequence(faultSequenceName, tenantID, apiIdentifier, "fault"))
                .thenReturn(false);
        Assert.assertEquals(gatewayManager.publishToGateway(api, apiTemplateBuilder, tenantDomain).size(), 0);

        //If fault sequence has not been defined for the API, omit deploying/updating and remove if already exists
        api.setFaultSequence(null);
        Assert.assertEquals(gatewayManager.publishToGateway(api, apiTemplateBuilder, tenantDomain).size(), 0);
    }

    @Test public void testPublishingNewRESTAPIWithCustomInSequenceToGateway()
            throws AxisFault, APIManagementException, XMLStreamException, RegistryException {
        API api = new API(apiIdentifier);
        api.setType("HTTP");
        api.setAsPublishedDefaultVersion(true);
        api.setImplementation("ENDPOINT");
        api.setEndpointConfig(prodEndpointConfig);
        api.setInSequence(inSequenceName);
        api.setAsDefaultVersion(true);
        api.setSwaggerDefinition(swaggerDefinition);
        api.setUUID(apiUUId);
        APITemplateBuilder apiTemplateBuilder = new APITemplateBuilderImpl(api);
        Set<String> environments = new HashSet<String>();
        environments.add(prodEnvironmentName);
        environments.add(null);
        api.setEnvironments(environments);
        Mockito.when(apiGatewayAdminClient.getApi(tenantDomain, apiIdentifier)).thenReturn(null);
        PowerMockito.when(APIUtil.getCustomSequence(inSequenceName, tenantID, "in", api.getId()))
                .thenReturn(AXIOMUtil.stringToOM(testSequenceDefinition));
        Assert.assertEquals(gatewayManager.publishToGateway(api, apiTemplateBuilder, tenantDomain).size(), 0);
    }

    @Test public void testPublishingNewRESTAPIWithCustomOutSequenceToGateway()
            throws AxisFault, APIManagementException, XMLStreamException, RegistryException {
        API api = new API(apiIdentifier);
        api.setType("HTTP");
        api.setAsPublishedDefaultVersion(true);
        api.setImplementation("ENDPOINT");
        api.setEndpointConfig(prodEndpointConfig);
        api.setOutSequence(outSequenceName);
        api.setAsDefaultVersion(true);
        api.setSwaggerDefinition(swaggerDefinition);
        api.setUUID(apiUUId);
        APITemplateBuilder apiTemplateBuilder = new APITemplateBuilderImpl(api);
        Set<String> environments = new HashSet<String>();
        environments.add(prodEnvironmentName);
        environments.add(null);
        api.setEnvironments(environments);
        Mockito.when(apiGatewayAdminClient.getApi(tenantDomain, apiIdentifier)).thenReturn(null);
        PowerMockito.when(APIUtil.getCustomSequence(outSequenceName, tenantID, "out", api.getId()))
                .thenReturn(AXIOMUtil.stringToOM(testSequenceDefinition));
        Assert.assertEquals(gatewayManager.publishToGateway(api, apiTemplateBuilder, tenantDomain).size(), 0);
    }

    @Test public void testPublishingExistingRESTAPIToGateway()
            throws AxisFault, APIManagementException, XMLStreamException, RegistryException {

        API api = new API(apiIdentifier);
        api.setType("HTTP");
        api.setAsPublishedDefaultVersion(true);
        api.setAsDefaultVersion(true);
        api.setSwaggerDefinition(swaggerDefinition);
        api.setUUID(apiUUId);
        APITemplateBuilder apiTemplateBuilder = new APITemplateBuilderImpl(api);
        Set<String> environments = new HashSet<String>();
        environments.add(prodEnvironmentName);
        environments.add(null);
        api.setEnvironments(environments);
        Mockito.when(apiGatewayAdminClient.getApi(tenantDomain, apiIdentifier)).thenReturn(apiData);
        Mockito.when(apiGatewayAdminClient.getDefaultApi(tenantDomain, apiIdentifier)).thenReturn(defaultAPIdata);

        //Test deleting existing API from production environment, if matching producntion endpoint configuration is
        // not found in API's endpoint config
        api.setEndpointConfig(sandBoxEndpointConfig);
        Assert.assertEquals(gatewayManager.publishToGateway(api, apiTemplateBuilder, tenantDomain).size(), 0);

        //Test updating 'INLINE' type REST API to gateway
        api.setImplementation("INLINE");
        api.setEndpointConfig(prodEndpointConfig);
        Assert.assertEquals(gatewayManager.publishToGateway(api, apiTemplateBuilder, tenantDomain).size(), 0);

        //Test updating 'ENDPOINT' type REST API to gateway
        api.setImplementation("ENDPOINT");
        Assert.assertEquals(gatewayManager.publishToGateway(api, apiTemplateBuilder, tenantDomain).size(), 0);

        //Test updating default version of the API
        Mockito.when(apiGatewayAdminClient.getDefaultApi(tenantDomain, apiIdentifier)).thenReturn(defaultAPIdata);
        Assert.assertEquals(gatewayManager.publishToGateway(api, apiTemplateBuilder, tenantDomain).size(), 0);

        //Test deploying API, if secure vault is enabled
        api.setEndpointSecured(true);
        Mockito.when(config.getFirstProperty(APIConstants.API_SECUREVAULT_ENABLE)).thenReturn("true");
        Assert.assertEquals(gatewayManager.publishToGateway(api, apiTemplateBuilder, tenantDomain).size(), 0);
    }

    @Test public void testPublishingExistingRESTAPIWithCustomInSequenceToGateway()
            throws AxisFault, APIManagementException, XMLStreamException, RegistryException {
        API api = new API(apiIdentifier);
        api.setType("HTTP");
        api.setAsPublishedDefaultVersion(true);
        api.setImplementation("ENDPOINT");
        api.setEndpointConfig(prodEndpointConfig);
        api.setInSequence(inSequenceName);
        api.setAsDefaultVersion(true);
        api.setSwaggerDefinition(swaggerDefinition);
        api.setUUID(apiUUId);
        APITemplateBuilder apiTemplateBuilder = new APITemplateBuilderImpl(api);
        Set<String> environments = new HashSet<String>();
        environments.add(prodEnvironmentName);
        environments.add(null);
        api.setEnvironments(environments);
        Mockito.when(apiGatewayAdminClient.getApi(tenantDomain, apiIdentifier)).thenReturn(apiData);
        PowerMockito.when(APIUtil.getCustomSequence(inSequenceName, tenantID, "in", api.getId()))
                .thenReturn(AXIOMUtil.stringToOM(testSequenceDefinition));
        Assert.assertEquals(gatewayManager.publishToGateway(api, apiTemplateBuilder, tenantDomain).size(), 0);
    }

    @Test public void testPublishingExistingRESTAPIWithCustomOutSequenceToGateway()
            throws AxisFault, APIManagementException, XMLStreamException, RegistryException {
        API api = new API(apiIdentifier);
        api.setType("HTTP");
        api.setAsPublishedDefaultVersion(true);
        api.setImplementation("ENDPOINT");
        api.setEndpointConfig(prodEndpointConfig);
        api.setOutSequence(outSequenceName);
        api.setAsDefaultVersion(true);
        api.setSwaggerDefinition(swaggerDefinition);
        api.setUUID(apiUUId);
        APITemplateBuilder apiTemplateBuilder = new APITemplateBuilderImpl(api);
        Set<String> environments = new HashSet<String>();
        environments.add(prodEnvironmentName);
        environments.add(null);
        api.setEnvironments(environments);
        Mockito.when(apiGatewayAdminClient.getApi(tenantDomain, apiIdentifier)).thenReturn(apiData);
        PowerMockito.when(APIUtil.getCustomSequence(outSequenceName, tenantID, "out", api.getId()))
                .thenReturn(AXIOMUtil.stringToOM(testSequenceDefinition));
        Assert.assertEquals(gatewayManager.publishToGateway(api, apiTemplateBuilder, tenantDomain).size(), 0);
    }

    @Test public void testFailureWhilePublishingNewAPIToGateway() throws Exception {
        API api = new API(apiIdentifier);
        api.setType("HTTP");
        api.setAsPublishedDefaultVersion(true);
        api.setImplementation("ENDPOINT");
        api.setEndpointConfig(prodEndpointConfig);
        api.setInSequence(inSequenceName);
        api.setAsDefaultVersion(true);
        api.setSwaggerDefinition(swaggerDefinition);
        api.setUUID(apiUUId);
        APITemplateBuilder apiTemplateBuilder = new APITemplateBuilderImpl(api);
        Set<String> environments = new HashSet<String>();
        environments.add(prodEnvironmentName);
        environments.add(null);
        OMElement inSequence = AXIOMUtil.stringToOM(testSequenceDefinition);
        OMElement faultSequence = AXIOMUtil.stringToOM(testSequenceDefinition);
        api.setEnvironments(environments);
        Mockito.when(apiGatewayAdminClient.getApi(tenantDomain, apiIdentifier)).thenReturn(null);
        PowerMockito.when(APIUtil.getCustomSequence(inSequenceName, tenantID, "in", api.getId()))
                .thenReturn(inSequence);
        PowerMockito.when(APIUtil.isProductionEndpointsExists(Mockito.anyString())).thenReturn(true);
        PowerMockito.when(APIUtil.isSequenceDefined(Mockito.anyString())).thenReturn(true);

        Map<String, String> failedEnvironmentsMap = gatewayManager
                .publishToGateway(api, apiTemplateBuilder, tenantDomain);
        Assert.assertEquals(failedEnvironmentsMap.size(), 1);
        Assert.assertTrue(failedEnvironmentsMap.keySet().contains(prodEnvironmentName ));

        //Test failure to deploy API when setting secure vault property failed
        Mockito.doThrow(new APIManagementException("Failed to set secure vault property for the tenant"))
                .when(apiGatewayAdminClient).setSecureVaultProperty(api, tenantDomain);
        api.setEndpointSecured(true);
        Mockito.when(config.getFirstProperty(APIConstants.API_SECUREVAULT_ENABLE)).thenReturn("true");
        Mockito.when(config.getFirstProperty(APIConstants.ENABLE_MTLS_FOR_APIS)).thenReturn("true");
        Map<String, String> failedEnvironmentsMap2 = gatewayManager
                .publishToGateway(api, apiTemplateBuilder, tenantDomain);
        Assert.assertEquals(failedEnvironmentsMap2.size(), 1);
        Assert.assertTrue(failedEnvironmentsMap2.keySet().contains(prodEnvironmentName ));

        //Test failure to deploy API when fault sequence deployment failed
        api.setFaultSequence(faultSequenceName);
        PowerMockito.when(APIUtil.getCustomSequence(faultSequenceName, tenantID, "fault", api.getId()))
                .thenReturn(faultSequence);
        Map<String, String> failedEnvironmentsMap3 = gatewayManager
                .publishToGateway(api, apiTemplateBuilder, tenantDomain);
        Assert.assertEquals(failedEnvironmentsMap3.size(), 1);
        Assert.assertTrue(failedEnvironmentsMap3.keySet().contains(prodEnvironmentName));

        //Test throwing AxisFault when Gateway client initialisation failed
        Mockito.doThrow(new AxisFault("Error occurred while deploying sequence")).when(apiGatewayAdminClient)
                .getApi(tenantDomain, apiIdentifier);
        Map<String, String> failedEnvironmentsMap4 = gatewayManager
                .publishToGateway(api, apiTemplateBuilder, tenantDomain);
        Assert.assertEquals(failedEnvironmentsMap4.size(), 1);
        Assert.assertTrue(failedEnvironmentsMap4.keySet().contains(prodEnvironmentName));
    }

    @Test public void testFailureWhilePublishingAPIUpdateToGateway() throws Exception {
        API api = new API(apiIdentifier);
        api.setType("HTTP");
        api.setAsPublishedDefaultVersion(true);
        api.setImplementation("ENDPOINT");
        api.setEndpointConfig(prodEndpointConfig);
        api.setInSequence(inSequenceName);
        api.setAsDefaultVersion(true);
        api.setSwaggerDefinition(swaggerDefinition);
        api.setUUID(apiUUId);
        APITemplateBuilder apiTemplateBuilder = new APITemplateBuilderImpl(api);
        Set<String> environments = new HashSet<String>();
        environments.add(prodEnvironmentName);
        environments.add(null);
        OMElement inSequence = AXIOMUtil.stringToOM(testSequenceDefinition);
        api.setEnvironments(environments);
        Mockito.when(apiGatewayAdminClient.getApi(tenantDomain, apiIdentifier)).thenReturn(apiData);
        PowerMockito.when(APIUtil.getCustomSequence(inSequenceName, tenantID, "in", api.getId()))
                .thenReturn(inSequence);
        PowerMockito.when(APIUtil.isProductionEndpointsExists(Mockito.anyString())).thenReturn(true);
        PowerMockito.when(APIUtil.isSequenceDefined(Mockito.anyString())).thenReturn(true);
        //Test API deployment failure when custom sequence update failed
        Map<String, String> failedEnvironmentsMap = gatewayManager
                .publishToGateway(api, apiTemplateBuilder, tenantDomain);
        Assert.assertEquals(failedEnvironmentsMap.size(), 1);
        Assert.assertTrue(failedEnvironmentsMap.keySet().contains(prodEnvironmentName ));
    }
}