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

import static org.mockito.Mockito.times;

import java.io.StringWriter;
import java.rmi.RemoteException;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
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
import org.wso2.carbon.apimgt.gateway.stub.APIGatewayAdminStub;
import org.wso2.carbon.apimgt.impl.*;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.template.APITemplateBuilder;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ APIGatewayAdminClient.class })
public class APIGatewayAdminClientTest {

    private APIGatewayAdminStub apiGatewayAdminStub;
    private Environment environment;
    private final String USERNAME = "username";
    private final String PASSWORD = "password";
    private final String ENV_NAME = "test-environment";
    private final String SERVER_URL = "https://localhost.com";

    @Before
    public void setup() throws Exception {
        environment = new Environment();
        environment.setName(ENV_NAME);
        environment.setPassword(PASSWORD);
        environment.setUserName(USERNAME);
        environment.setServerURL(SERVER_URL);
        apiGatewayAdminStub = Mockito.mock(APIGatewayAdminStub.class);

        Options options = new Options();
        ServiceContext serviceContext = new ServiceContext();
        OperationContext operationContext = Mockito.mock(OperationContext.class);
        serviceContext.setProperty(HTTPConstants.COOKIE_STRING, "");
        ServiceClient serviceClient = Mockito.mock(ServiceClient.class);
        AuthenticationAdminStub authAdminStub = Mockito.mock(AuthenticationAdminStub.class);
        Mockito.doReturn(true).when(authAdminStub).login(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        Mockito.when(authAdminStub._getServiceClient()).thenReturn(serviceClient);
        Mockito.when(serviceClient.getLastOperationContext()).thenReturn(operationContext);
        Mockito.when(operationContext.getServiceContext()).thenReturn(serviceContext);
        Mockito.when(apiGatewayAdminStub._getServiceClient()).thenReturn(serviceClient);
        Mockito.when(serviceClient.getOptions()).thenReturn(options);
        PowerMockito.whenNew(AuthenticationAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString()).thenReturn(authAdminStub);
    }

    @Test
    public void testAPIGatewayAdminClient() throws Exception {
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        Assert.assertNotNull(client);
        Mockito.verify(apiGatewayAdminStub, times(2))._getServiceClient();
    }

    @Test(expected = AxisFault.class)
    public void testAPIGatewayAdminClientException() throws Exception {
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString()).thenThrow(AxisFault.class);
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
    }

    @Test(expected = APIManagementException.class)
    public void testSetSecureVaultPropertyException() throws Exception {
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(apiGatewayAdminStub.doEncryption(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).
                thenThrow(RemoteException.class);
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        APIIdentifier identifier = new APIIdentifier("P1_API1_v1.0.0");
        API api = new API(identifier);
        client.setSecureVaultProperty(api, null);
    }

    @Test
    public void testSetSecureVaultProperty() throws Exception {
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(apiGatewayAdminStub.doEncryption(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).
                thenReturn("");
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        APIIdentifier identifier = new APIIdentifier("P1_API1_v1.0.0");
        API api = new API(identifier);
        client.setSecureVaultProperty(api, null);
        Mockito.verify(apiGatewayAdminStub, times(1))
                .doEncryption(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testAddApi() throws Exception {
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(apiGatewayAdminStub
                .addApi(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(apiGatewayAdminStub
                .addApiForTenant(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString())).thenReturn(true);
        APIIdentifier identifier = new APIIdentifier("P1_API1_v1.0.0");
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        APITemplateBuilder apiTemplateBuilder = Mockito.mock(APITemplateBuilder.class);
        Mockito.when(apiTemplateBuilder.getConfigStringForTemplate(environment)).thenReturn(Mockito.anyString());
        client.addApi(apiTemplateBuilder, "", identifier);
        client.addApi(apiTemplateBuilder, null, identifier);
        client.addApi(apiTemplateBuilder, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, identifier);
        Mockito.verify(apiGatewayAdminStub, times(3))
                .addApi(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        client.addApi(apiTemplateBuilder, "tenant", identifier);

        Mockito.verify(apiGatewayAdminStub, times(1))
                .addApiForTenant(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString());
    }

    @Test (expected = AxisFault.class)
    public void testAddApiException() throws Exception {
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(apiGatewayAdminStub
                .addApi(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenThrow(Exception.class);
        Mockito.when(apiGatewayAdminStub
                .addApiForTenant(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString())).thenThrow(Exception.class);
        APIIdentifier identifier = new APIIdentifier("P1_API1_v1.0.0");
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        APITemplateBuilder apiTemplateBuilder = Mockito.mock(APITemplateBuilder.class);
        Mockito.when(apiTemplateBuilder.getConfigStringForTemplate(environment)).thenReturn(Mockito.anyString());
        client.addApi(apiTemplateBuilder, "", identifier);
        client.addApi(apiTemplateBuilder, "tenant", identifier);
    }

    @Test
    public void testAddPrototypeScripImpl() throws Exception {
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(apiGatewayAdminStub
                .addApi(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(apiGatewayAdminStub
                .addApiForTenant(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString())).thenReturn(true);
        APIIdentifier identifier = new APIIdentifier("P1_API1_v1.0.0");
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        APITemplateBuilder apiTemplateBuilder = Mockito.mock(APITemplateBuilder.class);
        Mockito.when(apiTemplateBuilder.getConfigStringForPrototypeScriptAPI(environment))
                .thenReturn(Mockito.anyString());
        client.addPrototypeApiScriptImpl(apiTemplateBuilder, "", identifier);
        client.addPrototypeApiScriptImpl(apiTemplateBuilder, null, identifier);
        client.addPrototypeApiScriptImpl(apiTemplateBuilder, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, identifier);
        Mockito.verify(apiGatewayAdminStub, times(3))
                .addApi(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString());
        client.addPrototypeApiScriptImpl(apiTemplateBuilder, "tenant", identifier);
        Mockito.verify(apiGatewayAdminStub, times(1))
                .addApiForTenant(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString());
    }

    @Test (expected = AxisFault.class)
    public void testAddPrototypeScripImplException() throws Exception {
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(apiGatewayAdminStub
                .addApi(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenThrow(Exception.class);
        Mockito.when(apiGatewayAdminStub
                .addApiForTenant(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString())).thenThrow(Exception.class);
        APIIdentifier identifier = new APIIdentifier("P1_API1_v1.0.0");
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        APITemplateBuilder apiTemplateBuilder = Mockito.mock(APITemplateBuilder.class);
        Mockito.when(apiTemplateBuilder.getConfigStringForPrototypeScriptAPI(environment))
                .thenReturn(Mockito.anyString());
        client.addPrototypeApiScriptImpl(apiTemplateBuilder, "", identifier);
        client.addPrototypeApiScriptImpl(apiTemplateBuilder, "tenant", identifier);
    }

    @Test
    public void testUpdateApi() throws Exception {
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(apiGatewayAdminStub
                .updateApi(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(apiGatewayAdminStub
                .updateApiForTenant(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString())).thenReturn(true);
        APIIdentifier identifier = new APIIdentifier("P1_API1_v1.0.0");
        API api = new API(identifier);
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        APITemplateBuilder apiTemplateBuilder = Mockito.mock(APITemplateBuilder.class);
        Mockito.when(apiTemplateBuilder.getConfigStringForTemplate(environment)).thenReturn(Mockito.anyString());
        client.updateApi(apiTemplateBuilder, "", identifier);
        client.updateApi(apiTemplateBuilder, null, identifier);
        client.updateApi(apiTemplateBuilder, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, identifier);
        client.updateApi(apiTemplateBuilder, "tenant", identifier);
        Mockito.verify(apiGatewayAdminStub, times(3))
                .updateApi(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        Mockito.verify(apiGatewayAdminStub, times(1))
                .updateApiForTenant(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString());
    }

    @Test (expected = AxisFault.class)
    public void testUpdateApiException() throws Exception {
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(apiGatewayAdminStub
                .updateApi(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenThrow(Exception.class);
        Mockito.when(apiGatewayAdminStub
                .updateApiForTenant(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString())).thenThrow(Exception.class);
        APIIdentifier identifier = new APIIdentifier("P1_API1_v1.0.0");
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        APITemplateBuilder apiTemplateBuilder = Mockito.mock(APITemplateBuilder.class);
        Mockito.when(apiTemplateBuilder.getConfigStringForTemplate(environment)).thenReturn(Mockito.anyString());
        client.updateApi(apiTemplateBuilder, "", identifier);
        client.updateApi(apiTemplateBuilder, "tenant", identifier);
    }

    @Test
    public void testAddDefaultApi() throws Exception {
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(apiGatewayAdminStub
                .addDefaultAPI(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(apiGatewayAdminStub
                .addDefaultAPIForTenant(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        APIIdentifier identifier = new APIIdentifier("P1_API1_v1.0.0");
        API api = new API(identifier);
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        APITemplateBuilder apiTemplateBuilder = Mockito.mock(APITemplateBuilder.class);
        Mockito.when(apiTemplateBuilder.getConfigStringForDefaultAPITemplate(Mockito.anyString()))
                .thenReturn(Mockito.anyString());
        client.addDefaultAPI(apiTemplateBuilder, "", "1.0.0", identifier);
        client.addDefaultAPI(apiTemplateBuilder, null, "1.0.0", identifier);
        client.addDefaultAPI(apiTemplateBuilder, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, "1.0.0", identifier);
        Mockito.verify(apiGatewayAdminStub, times(3))
                .addDefaultAPI(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

        client.addDefaultAPI(apiTemplateBuilder, "tenant", "1.0.0", identifier);
        Mockito.verify(apiGatewayAdminStub, times(1))
                .addDefaultAPIForTenant(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString());
    }

    @Test (expected = AxisFault.class)
    public void testAddDefaultApiException() throws Exception {
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(apiGatewayAdminStub
                .addDefaultAPI(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenThrow(Exception.class);
        Mockito.when(apiGatewayAdminStub
                .addDefaultAPIForTenant(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString())).thenThrow(Exception.class);
        APIIdentifier identifier = new APIIdentifier("P1_API1_v1.0.0");
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        APITemplateBuilder apiTemplateBuilder = Mockito.mock(APITemplateBuilder.class);
        Mockito.when(apiTemplateBuilder.getConfigStringForDefaultAPITemplate(Mockito.anyString()))
                .thenReturn(Mockito.anyString());
        client.addDefaultAPI(apiTemplateBuilder, "", "1.0.0", identifier);
        client.addDefaultAPI(apiTemplateBuilder, "tenant", "1.0.0", identifier);
    }

    @Test
    public void testUpdateDefaultApi() throws Exception {
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(apiGatewayAdminStub
                .updateDefaultApi(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(apiGatewayAdminStub
                .updateDefaultApiForTenant(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        APIIdentifier identifier = new APIIdentifier("P1_API1_v1.0.0");
        API api = new API(identifier);
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        APITemplateBuilder apiTemplateBuilder = Mockito.mock(APITemplateBuilder.class);
        Mockito.when(apiTemplateBuilder.getConfigStringForDefaultAPITemplate(Mockito.anyString()))
                .thenReturn(Mockito.anyString());
        client.updateDefaultApi(apiTemplateBuilder, "", "1.0.0", identifier);
        client.updateDefaultApi(apiTemplateBuilder, null, "1.0.0", identifier);
        client.updateDefaultApi(apiTemplateBuilder, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, "1.0.0", identifier);
        Mockito.verify(apiGatewayAdminStub, times(3))
                .updateDefaultApi((Mockito.anyString()), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        client.updateDefaultApi(apiTemplateBuilder, "tenant", "1.0.0", identifier);

        Mockito.verify(apiGatewayAdminStub, times(1))
                .updateDefaultApiForTenant((Mockito.anyString()), (Mockito.anyString()), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString());
    }

    @Test (expected = AxisFault.class)
    public void testUpdateDefaultApiException() throws Exception {
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(apiGatewayAdminStub
                .updateDefaultApi(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenThrow(Exception.class);
        Mockito.when(apiGatewayAdminStub
                .updateDefaultApiForTenant(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString())).thenThrow(Exception.class);
        APIIdentifier identifier = new APIIdentifier("P1_API1_v1.0.0");
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        APITemplateBuilder apiTemplateBuilder = Mockito.mock(APITemplateBuilder.class);
        Mockito.when(apiTemplateBuilder.getConfigStringForDefaultAPITemplate(Mockito.anyString()))
                .thenReturn(Mockito.anyString());
        client.updateDefaultApi(apiTemplateBuilder, "", "1.0.0", identifier);
        client.updateDefaultApi(apiTemplateBuilder, "tenant", "1.0.0", identifier);
    }

    @Test
    public void testUpdateApiForInlineScript() throws Exception {
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(apiGatewayAdminStub
                .updateApi(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(apiGatewayAdminStub
                .updateApiForTenant(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString())).thenReturn(true);
        APIIdentifier identifier = new APIIdentifier("P1_API1_v1.0.0");
        API api = new API(identifier);
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        APITemplateBuilder apiTemplateBuilder = Mockito.mock(APITemplateBuilder.class);
        Mockito.when(apiTemplateBuilder.getConfigStringForPrototypeScriptAPI(environment))
                .thenReturn(Mockito.anyString());
        client.updateApiForInlineScript(apiTemplateBuilder, "", identifier);
        client.updateApiForInlineScript(apiTemplateBuilder, null, identifier);
        client.updateApiForInlineScript(apiTemplateBuilder, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, identifier);
        Mockito.verify(apiGatewayAdminStub, times(3))
                .updateApi(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        client.updateApiForInlineScript(apiTemplateBuilder, "tenant", identifier);
        Mockito.verify(apiGatewayAdminStub, times(1))
                .updateApiForTenant(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString());
    }

    @Test (expected = AxisFault.class)
    public void testUpdateApiForInlineScriptException() throws Exception {
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(apiGatewayAdminStub
                .updateApi(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenThrow(Exception.class);
        Mockito.when(apiGatewayAdminStub
                .updateApiForTenant(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString())).thenThrow(Exception.class);
        APIIdentifier identifier = new APIIdentifier("P1_API1_v1.0.0");
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        APITemplateBuilder apiTemplateBuilder = Mockito.mock(APITemplateBuilder.class);
        Mockito.when(apiTemplateBuilder.getConfigStringForPrototypeScriptAPI(environment))
                .thenReturn(Mockito.anyString());
        client.updateApiForInlineScript(apiTemplateBuilder, "", identifier);
        client.updateApiForInlineScript(apiTemplateBuilder, "tenant", identifier);
    }

    @Test
    public void testDeleteApi() throws Exception {
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(apiGatewayAdminStub.deleteApi(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(apiGatewayAdminStub
                .deleteApiForTenant(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        APIIdentifier identifier = new APIIdentifier("P1_API1_v1.0.0");
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        client.deleteApi("", identifier);
        client.deleteApi(null, identifier);
        client.deleteApi(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, identifier);
        Mockito.verify(apiGatewayAdminStub, times(3))
                .deleteApi(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        client.deleteApi("tenant", identifier);
        Mockito.verify(apiGatewayAdminStub, times(1))
                .deleteApiForTenant(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test (expected = AxisFault.class)
    public void testDeleteApiException() throws Exception {
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(apiGatewayAdminStub.deleteApi(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenThrow(Exception.class);
        Mockito.when(apiGatewayAdminStub
                .deleteApiForTenant(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenThrow(Exception.class);
        APIIdentifier identifier = new APIIdentifier("P1_API1_v1.0.0");
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        client.deleteApi("", identifier);
        client.deleteApi("tenant", identifier);
    }

    @Test
    public void testDeleteDefaultApi() throws Exception {
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(
                apiGatewayAdminStub.deleteDefaultApi(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(apiGatewayAdminStub
                .deleteDefaultApiForTenant(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString())).thenReturn(true);
        APIIdentifier identifier = new APIIdentifier("P1_API1_v1.0.0");
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        client.deleteDefaultApi("", identifier);
        client.deleteDefaultApi(null, identifier);
        client.deleteDefaultApi(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, identifier);
        Mockito.verify(apiGatewayAdminStub, times(3))
                .deleteDefaultApi(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        client.deleteDefaultApi("tenant", identifier);
        Mockito.verify(apiGatewayAdminStub, times(1))
                .deleteDefaultApiForTenant(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString());
    }

    @Test (expected = AxisFault.class)
    public void testDeleteDefaultApiException() throws Exception {
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(
                apiGatewayAdminStub.deleteDefaultApi(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenThrow(Exception.class);
        Mockito.when(apiGatewayAdminStub
                .deleteDefaultApiForTenant(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString())).thenThrow(Exception.class);
        APIIdentifier identifier = new APIIdentifier("P1_API1_v1.0.0");
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        client.deleteDefaultApi("", identifier);
        client.deleteDefaultApi("tenant", identifier);
    }

    @Test
    public void testAddEndpointForProductionEndpoint() throws Exception {
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(apiGatewayAdminStub.addEndpoint(Mockito.anyString())).thenReturn(true);
        Mockito.when(apiGatewayAdminStub.addEndpointForTenant(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        APITemplateBuilder apiTemplateBuilder = Mockito.mock(APITemplateBuilder.class);
        APIIdentifier identifier = new APIIdentifier("P1_API1_v1.0.0");
        API api = new API(identifier);
        api.setEndpointConfig("{\"production_endpoints\":\"http://ep1.com\"}");
        Mockito.when(apiTemplateBuilder.getConfigStringForEndpointTemplate(Mockito.anyString()))
                .thenReturn(Mockito.anyString());
        client.addEndpoint(api, apiTemplateBuilder, "");
        client.addEndpoint(api, apiTemplateBuilder, null);
        client.addEndpoint(api, apiTemplateBuilder, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        Mockito.verify(apiGatewayAdminStub, times(3)).addEndpoint(Mockito.anyString());
        client.addEndpoint(api, apiTemplateBuilder, "tenant");
        Mockito.verify(apiGatewayAdminStub, times(1)).addEndpointForTenant(Mockito.anyString(),
                Mockito.anyString());
    }

    @Test
    public void testAddEndpointForSandboxEndpoint() throws Exception {
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(apiGatewayAdminStub.addEndpoint(Mockito.anyString())).thenReturn(true);
        Mockito.when(apiGatewayAdminStub.addEndpointForTenant(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        APITemplateBuilder apiTemplateBuilder = Mockito.mock(APITemplateBuilder.class);
        APIIdentifier identifier = new APIIdentifier("P1_API1_v1.0.0");
        API api = new API(identifier);
        api.setEndpointConfig("{\"sandbox_endpoints\":\"http://ep2.com\"}");
        Mockito.when(apiTemplateBuilder.getConfigStringForEndpointTemplate(Mockito.anyString()))
                .thenReturn(Mockito.anyString());
        client.addEndpoint(api, apiTemplateBuilder, "");
        client.addEndpoint(api, apiTemplateBuilder, null);
        client.addEndpoint(api, apiTemplateBuilder, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        Mockito.verify(apiGatewayAdminStub, times(3)).addEndpoint(Mockito.anyString());
        client.addEndpoint(api, apiTemplateBuilder, "tenant");
        Mockito.verify(apiGatewayAdminStub, times(1)).addEndpointForTenant(Mockito.anyString(),
                Mockito.anyString());
    }

    @Test
    public void testAddEndpointForBothEndpointTypes() throws Exception {
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(apiGatewayAdminStub.addEndpoint(Mockito.anyString())).thenReturn(true);
        Mockito.when(apiGatewayAdminStub.addEndpointForTenant(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        APITemplateBuilder apiTemplateBuilder = Mockito.mock(APITemplateBuilder.class);
        APIIdentifier identifier = new APIIdentifier("P1_API1_v1.0.0");
        API api = new API(identifier);
        api.setEndpointConfig("{\"production_endpoints\":\"http://ep1.com\",\"sandbox_endpoints\":\"http://ep2.com\"}");
        Mockito.when(apiTemplateBuilder.getConfigStringForEndpointTemplate(Mockito.anyString()))
                .thenReturn(Mockito.anyString());
        client.addEndpoint(api, apiTemplateBuilder, "");
        client.addEndpoint(api, apiTemplateBuilder, null);
        client.addEndpoint(api, apiTemplateBuilder, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        Mockito.verify(apiGatewayAdminStub, times(6)).addEndpoint(Mockito.anyString());
        client.addEndpoint(api, apiTemplateBuilder, "tenant");
        Mockito.verify(apiGatewayAdminStub, times(2)).addEndpointForTenant(Mockito.anyString(),
                Mockito.anyString());
    }

    @Test (expected = EndpointAdminException.class)
    public void testAddEndpointException() throws Exception {
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(apiGatewayAdminStub.addEndpoint(Mockito.anyString())).thenThrow(EndpointAdminException.class);
        Mockito.when(apiGatewayAdminStub.addEndpointForTenant(Mockito.anyString(), Mockito.anyString()))
                .thenThrow(EndpointAdminException.class);
        APIIdentifier identifier = new APIIdentifier("P1_API1_v1.0.0");
        API api = new API(identifier);
        api.setEndpointConfig("{\"production_endpoints\":\"http://ep1.com\",\"sandbox_endpoints\":\"http://ep2.com\"}");
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        APITemplateBuilder apiTemplateBuilder = Mockito.mock(APITemplateBuilder.class);
        Mockito.when(apiTemplateBuilder.getConfigStringForEndpointTemplate(Mockito.anyString()))
                .thenReturn(Mockito.anyString());
        client.addEndpoint(api, apiTemplateBuilder, "");
        client.addEndpoint(api, apiTemplateBuilder, "tenant");
    }

    @Test
    public void testDeleteEndpointForProductionEndpoint() throws Exception {
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(apiGatewayAdminStub.addEndpoint(Mockito.anyString())).thenReturn(true);
        Mockito.when(apiGatewayAdminStub.addEndpointForTenant(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        APIIdentifier identifier = new APIIdentifier("P1_API1_v1.0.0");
        API api = new API(identifier);
        api.setEndpointConfig("{\"production_endpoints\":\"http://ep1.com\"}");
        client.deleteEndpoint(api, "");
        client.deleteEndpoint(api, null);
        client.deleteEndpoint(api, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        Mockito.verify(apiGatewayAdminStub, times(3)).deleteEndpoint(Mockito.anyString());
        client.deleteEndpoint(api, "tenant");
        Mockito.verify(apiGatewayAdminStub, times(1))
                .deleteEndpointForTenant(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testDeleteEndpointForSandboxEndpoint() throws Exception {
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(apiGatewayAdminStub.addEndpoint(Mockito.anyString())).thenReturn(true);
        Mockito.when(apiGatewayAdminStub.addEndpointForTenant(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        APIIdentifier identifier = new APIIdentifier("P1_API1_v1.0.0");
        API api = new API(identifier);
        api.setEndpointConfig("{\"sandbox_endpoints\":\"http://ep2.com\"}");
        client.deleteEndpoint(api, "");
        client.deleteEndpoint(api, null);
        client.deleteEndpoint(api, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        Mockito.verify(apiGatewayAdminStub, times(3)).deleteEndpoint(Mockito.anyString());
        client.deleteEndpoint(api, "tenant");
        Mockito.verify(apiGatewayAdminStub, times(1))
                .deleteEndpointForTenant(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testDeleteEndpointForBothEndpoint() throws Exception {
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(apiGatewayAdminStub.addEndpoint(Mockito.anyString())).thenReturn(true);
        Mockito.when(apiGatewayAdminStub.addEndpointForTenant(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        APIIdentifier identifier = new APIIdentifier("P1_API1_v1.0.0");
        API api = new API(identifier);
        api.setEndpointConfig("{\"production_endpoints\":\"http://ep1.com\",\"sandbox_endpoints\":\"http://ep2.com\"}");
        client.deleteEndpoint(api, "");
        client.deleteEndpoint(api, null);
        client.deleteEndpoint(api, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        Mockito.verify(apiGatewayAdminStub, times(6)).deleteEndpoint(Mockito.anyString());
        client.deleteEndpoint(api, "tenant");
    }

    @Test (expected = EndpointAdminException.class)
    public void testDeleteEndpointException() throws Exception {
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(apiGatewayAdminStub.deleteEndpoint(Mockito.anyString())).thenThrow(EndpointAdminException.class);
        Mockito.when(apiGatewayAdminStub.deleteEndpointForTenant(Mockito.anyString(), Mockito.anyString()))
                .thenThrow(EndpointAdminException.class);
        APIIdentifier identifier = new APIIdentifier("P1_API1_v1.0.0");
        API api = new API(identifier);
        api.setEndpointConfig("{\"production_endpoints\":\"http://ep1.com\",\"sandbox_endpoints\":\"http://ep2.com\"}");
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        client.deleteEndpoint(api, "");
        client.deleteEndpoint(api, "tenant");
    }

    @Test
    public void testSaveEndpointForProductionEndpoint() throws Exception {
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(apiGatewayAdminStub
                .removeEndpointsToUpdate(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(apiGatewayAdminStub.addEndpoint(Mockito.anyString())).thenReturn(true);
        Mockito.when(apiGatewayAdminStub.addEndpointForTenant(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        APITemplateBuilder apiTemplateBuilder = Mockito.mock(APITemplateBuilder.class);
        APIIdentifier identifier = new APIIdentifier("P1_API1_v1.0.0");
        API api = new API(identifier);
        api.setEndpointConfig("{\"production_endpoints\":\"http://ep1.com\"}");
        Mockito.when(apiTemplateBuilder.getConfigStringForEndpointTemplate(Mockito.anyString()))
                .thenReturn("endpointData");
        client.saveEndpoint(api, apiTemplateBuilder, "");
        client.saveEndpoint(api, apiTemplateBuilder, null);
        client.saveEndpoint(api, apiTemplateBuilder, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        client.saveEndpoint(api, apiTemplateBuilder, "tenant");
        Mockito.verify(apiGatewayAdminStub, times(4))
                .removeEndpointsToUpdate(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        Mockito.verify(apiGatewayAdminStub, times(3)).addEndpoint(Mockito.anyString());
        Mockito.verify(apiGatewayAdminStub, times(1))
                .addEndpointForTenant(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testSaveEndpointForSandboxEndpoint() throws Exception {
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(apiGatewayAdminStub
                .removeEndpointsToUpdate(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(apiGatewayAdminStub.addEndpoint(Mockito.anyString())).thenReturn(true);
        Mockito.when(apiGatewayAdminStub.addEndpointForTenant(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        APITemplateBuilder apiTemplateBuilder = Mockito.mock(APITemplateBuilder.class);
        APIIdentifier identifier = new APIIdentifier("P1_API1_v1.0.0");
        API api = new API(identifier);
        api.setEndpointConfig("{\"sandbox_endpoints\":\"http://ep2.com\"}");
        Mockito.when(apiTemplateBuilder.getConfigStringForEndpointTemplate(Mockito.anyString()))
                .thenReturn("endpointData");
        client.saveEndpoint(api, apiTemplateBuilder, "");
        client.saveEndpoint(api, apiTemplateBuilder, null);
        client.saveEndpoint(api, apiTemplateBuilder, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        client.saveEndpoint(api, apiTemplateBuilder, "tenant");
        Mockito.verify(apiGatewayAdminStub, times(4))
                .removeEndpointsToUpdate(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        Mockito.verify(apiGatewayAdminStub, times(3)).addEndpoint(Mockito.anyString());
        Mockito.verify(apiGatewayAdminStub, times(1))
                .addEndpointForTenant(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testSaveEndpointForBothEndpoints() throws Exception {
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(apiGatewayAdminStub
                .removeEndpointsToUpdate(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(apiGatewayAdminStub.addEndpoint(Mockito.anyString())).thenReturn(true);
        Mockito.when(apiGatewayAdminStub.addEndpointForTenant(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        APITemplateBuilder apiTemplateBuilder = Mockito.mock(APITemplateBuilder.class);
        APIIdentifier identifier = new APIIdentifier("P1_API1_v1.0.0");
        API api = new API(identifier);
        api.setEndpointConfig("{\"production_endpoints\":\"http://ep1.com\",\"sandbox_endpoints\":\"http://ep2.com\"}");
        Mockito.when(apiTemplateBuilder.getConfigStringForEndpointTemplate(Mockito.anyString()))
                .thenReturn("endpointData");
        client.saveEndpoint(api, apiTemplateBuilder, "");
        client.saveEndpoint(api, apiTemplateBuilder, null);
        client.saveEndpoint(api, apiTemplateBuilder, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        client.saveEndpoint(api, apiTemplateBuilder, "tenant");
        Mockito.verify(apiGatewayAdminStub, times(4))
                .removeEndpointsToUpdate(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        Mockito.verify(apiGatewayAdminStub, times(6)).addEndpoint(Mockito.anyString());
        Mockito.verify(apiGatewayAdminStub, times(2))
                .addEndpointForTenant(Mockito.anyString(), Mockito.anyString());
    }

    @Test (expected = EndpointAdminException.class)
    public void testSaveEndpointException() throws Exception {
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(apiGatewayAdminStub
                .removeEndpointsToUpdate(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenThrow(EndpointAdminException.class);
        Mockito.when(apiGatewayAdminStub.addEndpoint(Mockito.anyString())).thenThrow(EndpointAdminException.class);
        Mockito.when(apiGatewayAdminStub.addEndpointForTenant(Mockito.anyString(), Mockito.anyString()))
                .thenThrow(EndpointAdminException.class);
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        APITemplateBuilder apiTemplateBuilder = Mockito.mock(APITemplateBuilder.class);
        APIIdentifier identifier = new APIIdentifier("P1_API1_v1.0.0");
        API api = new API(identifier);
        api.setEndpointConfig("{\"production_endpoints\":\"http://ep1.com\",\"sandbox_endpoints\":\"http://ep2.com\"}");
        Mockito.when(apiTemplateBuilder.getConfigStringForEndpointTemplate(Mockito.anyString()))
                .thenReturn("endpointData");
        client.saveEndpoint(api, apiTemplateBuilder, "");
        client.saveEndpoint(api, apiTemplateBuilder, "tenant");
    }

    @Test
    public void testAddSequence() throws Exception {
        OMElement sequence = Mockito.mock(OMElement.class);
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(apiGatewayAdminStub.addSequence(Mockito.anyString())).thenReturn(true);
        Mockito.when(apiGatewayAdminStub.
                addSequenceForTenant(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.doNothing().when(sequence).serializeAndConsume(Mockito.any(StringWriter.class));
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        client.addSequence(sequence, "");
        client.addSequence(sequence, null);
        client.addSequence(sequence, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        Mockito.verify(apiGatewayAdminStub, times(3)).addSequence(Mockito.anyString());
        client.addSequence(sequence, "tenant");
        Mockito.verify(apiGatewayAdminStub, times(1)).addSequenceForTenant(Mockito.anyString(), Mockito.anyString());
    }

    @Test (expected = AxisFault.class)
    public void testAddSequenceException() throws Exception {
        OMElement sequence = Mockito.mock(OMElement.class);
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(apiGatewayAdminStub.addSequence(Mockito.anyString())).thenThrow(Exception.class);
        Mockito.when(apiGatewayAdminStub.addSequenceForTenant(Mockito.anyString(), Mockito.anyString()))
                .thenThrow(Exception.class);
        Mockito.doNothing().when(sequence).serializeAndConsume(Mockito.any(StringWriter.class));
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        client.addSequence(sequence, "");
        client.addSequence(sequence, "tenant");
    }

    @Test
    public void testDeleteSequence() throws Exception {
        OMElement sequence = Mockito.mock(OMElement.class);
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(apiGatewayAdminStub.deleteSequence(Mockito.anyString())).thenReturn(true);
        Mockito.when(apiGatewayAdminStub.
                deleteSequenceForTenant(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        client.deleteSequence(sequence.getLocalName(), "");
        client.deleteSequence(sequence.getLocalName(), null);
        client.deleteSequence(sequence.getLocalName(), MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        Mockito.verify(apiGatewayAdminStub, times(3)).deleteSequence(Mockito.anyString());
        client.deleteSequence(sequence.getLocalName(), "tenant");
        Mockito.verify(apiGatewayAdminStub, times(1)).deleteSequenceForTenant(Mockito.anyString(), Mockito.anyString());
    }

    @Test (expected = AxisFault.class)
    public void testDeleteSequenceException() throws Exception {
        OMElement sequence = Mockito.mock(OMElement.class);
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(apiGatewayAdminStub.deleteSequence(Mockito.anyString())).thenThrow(Exception.class);
        Mockito.when(apiGatewayAdminStub.deleteSequenceForTenant(Mockito.anyString(), Mockito.anyString()))
                .thenThrow(Exception.class);
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        client.deleteSequence(sequence.getLocalName(), "");
        client.deleteSequence(sequence.getLocalName(), "tenant");
    }

    @Test
    public void testDeployPolicy() throws Exception {
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(apiGatewayAdminStub.deployPolicy(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        client.deployPolicy("sample-policy", "sample-policy-file");
        Mockito.verify(apiGatewayAdminStub, times(1)).deployPolicy(Mockito.anyString(), Mockito.anyString());

    }

    @Test(expected = AxisFault.class)
    public void testDeployPolicyException() throws Exception {
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(apiGatewayAdminStub.deployPolicy(Mockito.anyString(), Mockito.anyString()))
                .thenThrow(RemoteException.class);
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        client.deployPolicy("sample-policy", "sample-policy-file");

    }

    @Test
    public void testUndeployPolicy() throws Exception {
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(apiGatewayAdminStub.undeployPolicy(Mockito.any(String[].class))).thenReturn(true);
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        String fileNames[] = { "file1", "file2", "file3" };
        client.undeployPolicy(fileNames);
        Mockito.verify(apiGatewayAdminStub, times(1)).undeployPolicy(Mockito.any(String[].class));
    }

    @Test(expected = AxisFault.class)
    public void testUndeployPolicyException() throws Exception {
        PowerMockito.whenNew(APIGatewayAdminStub.class)
                .withArguments(Mockito.any(ConfigurationContext.class), Mockito.anyString())
                .thenReturn(apiGatewayAdminStub);
        Mockito.when(apiGatewayAdminStub.undeployPolicy(Mockito.any(String[].class))).thenThrow(RemoteException.class);
        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
        String fileNames[] = {"file1","file2","file3"};
        client.undeployPolicy(fileNames);
    }
}
