/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.impl.utils;

import org.apache.axiom.om.OMElement;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.wsdl.Definition;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceReferenceHolder.class, APIUtil.class, ApiMgtDAO.class})

public class APIMWSDLReaderTest {
    private static ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
    private static APIManagerConfigurationService apimConfigService = Mockito
            .mock(APIManagerConfigurationService.class);
    private static APIManagerConfiguration apimConfig = Mockito.mock(APIManagerConfiguration.class);

    @Test
    public void testReadAndCleanWsdl() throws Exception {
        doMockStatics();

        APIMWSDLReader wsdlReader = new APIMWSDLReader(
                Thread.currentThread().getContextClassLoader()
                        .getResource("wsdls/stockQuote.wsdl").toExternalForm());
        wsdlReader.validateBaseURI();
        OMElement element = wsdlReader.readAndCleanWsdl(getAPIForTesting());
        Assert.assertFalse("Endpoints are not properly replaced",
                element.toString().contains("location=\"http://www.webservicex.net/stockquote.asmx\""));
        Assert.assertTrue("Endpoints does not include GW endpoint",
                element.toString().contains("https://localhost:8243/abc"));
    }

    @Test
    public void endpointReferenceElementTest() throws Exception {
        doMockStatics();
        APIMWSDLReader wsdlReader = new APIMWSDLReader(
                Thread.currentThread().getContextClassLoader()
                        .getResource("wsdls/wsdl-with-EndpointReference.wsdl").toExternalForm());
        wsdlReader.validateBaseURI();
        OMElement element = wsdlReader.readAndCleanWsdl(getAPIForTesting());
        Assert.assertFalse("Endpoints are not properly replaced",
                element.toString().contains("location=\"http://www.webservicex.net/stockquote.asmx\""));
        Assert.assertTrue("Endpoints does not include GW endpoint",
                element.toString().contains("https://localhost:8243/abc"));
    }

    @Test
    public void testReadAndCleanWsdl2() throws Exception {
        doMockStatics();
        
        APIMWSDLReader wsdlReader = new APIMWSDLReader(
                Thread.currentThread().getContextClassLoader()
                        .getResource("wsdls/wsdl2-sample.wsdl").toExternalForm());
        wsdlReader.validateBaseURI();
        Assert.assertTrue(wsdlReader.isWSDL2BaseURI());
        OMElement element = wsdlReader.readAndCleanWsdl2(getAPIForTesting());
        Assert.assertFalse("Endpoints are not properly replaced",
                element.toString().contains("address = \"http://yoursite.com/MyService\""));
        Assert.assertTrue("Endpoints does not include GW endpoint",
                element.toString().contains("https://localhost:8243/abc"));
    }
    
    @Test
    public void testUpdateWsdl() throws Exception {
        doMockStatics();

        APIMWSDLReader wsdlReader = new APIMWSDLReader("");
        byte[] content = IOUtils.toByteArray(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("wsdls/stockQuote.wsdl"));
        OMElement element = wsdlReader.updateWSDL(content, getAPIForTesting());
        Assert.assertFalse("Endpoints are not properly replaced",
                element.toString().contains("location=\"http://www.webservicex.net/stockquote.asmx\""));
        Assert.assertTrue("Endpoints does not include GW endpoint",
                element.toString().contains("https://localhost:8243/abc"));
    }

    @Test
    public void testUpdateWsdl2() throws Exception {
        doMockStatics();

        APIMWSDLReader wsdlReader = new APIMWSDLReader("");
        byte[] content = IOUtils.toByteArray(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("wsdls/wsdl2-sample.wsdl"));

        OMElement element = wsdlReader.updateWSDL2(content, getAPIForTesting());
        Assert.assertFalse("Endpoints are not properly replaced",
                element.toString().contains("address = \"http://yoursite.com/MyService\""));
        Assert.assertTrue("Endpoints does not include GW endpoint",
                element.toString().contains("https://localhost:8243/abc"));
    }

    @Test
    public void testGetWSDL() throws Exception {
        doMockStatics();
        APIMWSDLReader wsdlReader = new APIMWSDLReader("");
        byte[] content = IOUtils.toByteArray(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("wsdls/stockQuote.wsdl"));
        Definition definition = wsdlReader.getWSDLDefinitionFromByteContent(content, false);
        Assert.assertNotNull(new String(wsdlReader.getWSDL(definition)));
    }

    @Test
    public void testSetServiceDefinition() throws Exception {
        doMockStatics();
        PowerMockito.mockStatic(APIUtil.class);
        API api = getAPIForTesting();
        String environmentName = "Production and Sandbox";
        String environmentType = "hybrid";
        PowerMockito.when(APIUtil.getGatewayEndpoint(api.getTransports(), environmentName, environmentType))
                .thenReturn("http://localhost:8280");

        APIMWSDLReader wsdlReader = new APIMWSDLReader("");
        byte[] content = IOUtils.toByteArray(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("wsdls/stockQuote.wsdl"));
        Definition definition = wsdlReader.getWSDLDefinitionFromByteContent(content, false);
        try {
            wsdlReader.setServiceDefinition(definition, api, environmentName, environmentType);
            wsdlReader.getWSDL(definition);
            Assert.assertNotNull(definition.getServices());
        } catch (APIManagementException e) {
            Assert.fail("Unexpected exception occurred while updating service endpoint address");
        }
    }

    @Test
    public void testSetServiceDefinitionWithInvalidAPIGatewayEndpoints() throws Exception {
        PowerMockito.mockStatic(APIUtil.class);
        API api = getAPIForTesting();
        String environmentName = "Production and Sandbox";
        String environmentType = "hybrid";

        APIMWSDLReader wsdlReader = new APIMWSDLReader("");
        byte[] content = IOUtils.toByteArray(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("wsdls/invalidEndpointURL.wsdl"));
        Definition definition = wsdlReader.getWSDLDefinitionFromByteContent(content, false);
        try {
            wsdlReader.setServiceDefinition(definition, api, environmentName, environmentType);
            wsdlReader.getWSDL(definition);
            Assert.assertNotNull(definition.getServices());
        } catch (APIManagementException e) {
            Assert.fail("Unexpected exception occurred while updating service endpoint address");
        }
    }

    public static void doMockStatics() throws APIManagementException {
        System.setProperty("carbon.home", APIUtilTest.class.getResource("/").getFile());
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants
                    .SUPER_TENANT_DOMAIN_NAME);
            Map<String, Environment> gatewayEnvironments = new HashMap<String, Environment>();
            Environment env1 = new Environment();
            env1.setType("hybrid");
            env1.setApiGatewayEndpoint("http://localhost:8280,https://localhost:8243");
            gatewayEnvironments.put("e1", env1);

            PowerMockito.mockStatic(ServiceReferenceHolder.class);
            PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
            PowerMockito.when(ServiceReferenceHolder.getInstance()
                    .getAPIManagerConfigurationService()).thenReturn(apimConfigService);
            PowerMockito.when(ServiceReferenceHolder.getInstance()
                    .getAPIManagerConfigurationService()
                    .getAPIManagerConfiguration()).thenReturn(apimConfig);

            PowerMockito.when(ServiceReferenceHolder.getInstance()
                    .getAPIManagerConfigurationService()
                    .getAPIManagerConfiguration()
                    .getApiGatewayEnvironments()).thenReturn(gatewayEnvironments);

            ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
            PowerMockito.mockStatic(ApiMgtDAO.class);
            Mockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
            Mockito.when(apiMgtDAO.getAllEnvironments(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME))
                    .thenReturn(new ArrayList<org.wso2.carbon.apimgt.api.model.Environment>());
            PowerMockito.when(APIUtil.getEnvironments()).thenReturn(gatewayEnvironments);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }
    
    public static API getAPIForTesting() {
        API api = new API(new APIIdentifier("admin", "api1", "1.0.0"));
        api.setTransports("https");
        api.setContext("/abc");
        return api;
    }
}
