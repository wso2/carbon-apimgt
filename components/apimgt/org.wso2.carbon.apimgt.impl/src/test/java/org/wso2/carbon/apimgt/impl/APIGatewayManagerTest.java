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
import org.wso2.carbon.apimgt.impl.utils.APIGatewayAdminClient;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.LocalEntryAdminClient;
import org.wso2.carbon.apimgt.localentry.stub.APILocalEntryAdminStub;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

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
        PowerMockito.when(APIUtil.getEnvironments()).thenReturn(environments);
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






}
