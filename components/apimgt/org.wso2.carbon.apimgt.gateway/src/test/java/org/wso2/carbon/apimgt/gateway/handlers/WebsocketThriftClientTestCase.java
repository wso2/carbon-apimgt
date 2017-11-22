/*
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
 */

package org.wso2.carbon.apimgt.gateway.handlers;

import org.apache.axis2.AxisFault;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.thrift.ThriftUtils;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.generated.thrift.APIKeyValidationService;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * Test class for WebsocketThriftClient
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({WebsocketThriftClient.class, ServiceReferenceHolder.class, CarbonUtils.class, ThriftUtils.class,
        TSSLTransportFactory.class, APIKeyValidationService.class, APIKeyValidationService.Client.class,
        TBinaryProtocol.class, TSocket.class})
public class WebsocketThriftClientTestCase {
    private String context = "/ishara";
    private String apiVersion = "1.0";
    private String apiKey = "PhoneVerify";
    private String sessionId = "ggf7uuunhced7i8ftndi";
    private ThriftUtils thriftUtils;
    private APIKeyValidationService.Client client;
    private APIKeyValidationService service;

    private String host = "192.168.0.100";
    private int port = 7711;
    private int timeout = 900;
    private org.wso2.carbon.apimgt.impl.generated.thrift.APIKeyValidationInfoDTO thriftDTO;
    private TBinaryProtocol protocol;


    @Before
    public void setup() throws Exception {
        PowerMockito.mockStatic(ThriftUtils.class);
        thriftUtils = Mockito.mock(ThriftUtils.class);
        Mockito.when(ThriftUtils.getInstance()).thenReturn(thriftUtils);
        PowerMockito.when(thriftUtils.getSessionId()).thenReturn(sessionId);
        Mockito.when(thriftUtils.getTrustStorePath()).thenReturn("path");
        Mockito.when(thriftUtils.getTrustStorePassword()).thenReturn("pw");

        PowerMockito.when(ThriftUtils.getThriftServerHost()).thenReturn(host);
        Mockito.when(thriftUtils.getThriftPort()).thenReturn(port);
        Mockito.when(thriftUtils.getThriftClientConnectionTimeOut()).thenReturn(timeout);

        TSSLTransportFactory.TSSLTransportParameters params =
                Mockito.mock(TSSLTransportFactory.TSSLTransportParameters.class);
        PowerMockito.whenNew(TSSLTransportFactory.TSSLTransportParameters.class).withAnyArguments().thenReturn(params);

        PowerMockito.mockStatic(TSSLTransportFactory.class);
        PowerMockito.mockStatic(TSocket.class);
        TTransport transport = Mockito.mock(TTransport.class);
        TSocket tSocket = Mockito.mock(TSocket.class);
        PowerMockito.when(TSSLTransportFactory.getClientSocket(host, port, timeout)).thenReturn(tSocket);

        PowerMockito.mockStatic(TBinaryProtocol.class);
        protocol = Mockito.mock(TBinaryProtocol.class);
        PowerMockito.whenNew(TBinaryProtocol.class).withAnyArguments().thenReturn(protocol);

        thriftDTO = Mockito.mock(org.wso2.carbon.apimgt.impl.generated.thrift.APIKeyValidationInfoDTO.class);

    }

    /*
    * Test for APISecurityException when Error creating the transport
    * */
    @Test
    public void testGetAPIKeyDataAPISecurityException() throws AxisFault {
        try {
            WebsocketThriftClient websocketThriftClient = new WebsocketThriftClient();
        } catch (APISecurityException e) {
            Assert.assertTrue(e.getMessage().startsWith("Error creating the transport"));
        }
    }

    /*
    * Test for APISecurityException when Error while accessing backend services
    * */
    @Test
    public void testGetAPIKeyDataAPISecurityException1() throws Exception {

        PowerMockito.mockStatic(APIKeyValidationService.class);
        PowerMockito.mockStatic(APIKeyValidationService.Client.class);
        service = Mockito.mock(APIKeyValidationService.class);
        client = Mockito.mock(APIKeyValidationService.Client.class);
        PowerMockito.whenNew(APIKeyValidationService.Client.class).withArguments(protocol).thenReturn(client);
        WebsocketThriftClient websocketThriftClient = new WebsocketThriftClient();

        Mockito.when(client.validateKeyforHandshake(context, apiVersion,
                apiKey, sessionId)).thenReturn(thriftDTO);
        APIKeyValidationInfoDTO apiKeyValidationInfoDTOActual = websocketThriftClient.getAPIKeyData(context,
                apiVersion, apiKey);
        Assert.assertFalse(apiKeyValidationInfoDTOActual.isAuthorized());

    }

    /*
    * Test for  getAPIKeyData when login failed
    * */
    @Test(expected = APISecurityException.class)
    public void testGetAPIKeyDataLodinFailed() throws Exception {
        PowerMockito.when(thriftUtils.getSessionId()).thenReturn(null);

        PowerMockito.mockStatic(APIKeyValidationService.class);
        PowerMockito.mockStatic(APIKeyValidationService.Client.class);
        service = Mockito.mock(APIKeyValidationService.class);
        WebsocketThriftClient websocketThriftClient = new WebsocketThriftClient();

        APIKeyValidationInfoDTO apiKeyValidationInfoDTOActual = websocketThriftClient.getAPIKeyData(context,
                apiVersion, "");
        Assert.assertFalse(apiKeyValidationInfoDTOActual.isAuthorized());

    }
}
