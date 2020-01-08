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
@PrepareForTest({APIGatewayAdminClient.class})
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

    @Test(expected = AxisFault.class)
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






}
