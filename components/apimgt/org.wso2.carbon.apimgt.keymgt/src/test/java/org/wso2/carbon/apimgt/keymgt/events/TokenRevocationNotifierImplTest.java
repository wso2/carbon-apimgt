/*
 *   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.keymgt.events;

import org.apache.axis2.util.URL;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPut;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.internal.ServiceReferenceHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService;
import java.util.Map;
import java.util.Properties;

import static org.wso2.carbon.base.CarbonBaseConstants.CARBON_HOME;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({ "javax.net.ssl.*" })
@PrepareForTest({ TokenRevocationNotifierImpl.class, APIUtil.class, ServiceReferenceHolder.class, PrivilegedCarbonContext.class })
public class TokenRevocationNotifierImplTest {

    private static final Log log = LogFactory.getLog(TokenRevocationNotifierImplTest.class);
    private TokenRevocationNotifierImpl tokenRevocationNotifierImpl;
    private HttpPut httpETCDPut;
    private final String DEFAULT_TTL = "3600";
    private PrivilegedCarbonContext privilegedCarbonContext;

    @Before
    public void Init() throws Exception {
        System.setProperty(CARBON_HOME, "");
        ServiceReferenceHolder serviceReferenceHolder;
        OutputEventAdapterService outputEventAdapterService;

        UrlEncodedFormEntity urlEncodedFormEntity;
        HttpResponse etcdResponse;
        StatusLine statusLine;
        HttpClient etcdEPClient;
        String etcdUrl = "https://localhost:2379/v2/keys/jti/";

        serviceReferenceHolder = PowerMockito.mock(ServiceReferenceHolder.class);
        outputEventAdapterService = Mockito.mock(OutputEventAdapterService.class);
        privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        etcdEPClient = Mockito.mock(HttpClient.class);
        httpETCDPut = Mockito.mock(HttpPut.class);
        urlEncodedFormEntity = Mockito.mock(UrlEncodedFormEntity.class);
        etcdResponse = Mockito.mock(HttpResponse.class);

        PowerMockito.mockStatic(Map.class);
        PowerMockito.mockStatic(APIUtil.class);

        statusLine = PowerMockito.mock(StatusLine.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        PowerMockito.when(serviceReferenceHolder.getOutputEventAdapterService()).
                thenReturn(outputEventAdapterService);
        PowerMockito.doNothing().when(outputEventAdapterService).
                publish(Mockito.anyString(), Mockito.anyMap(), Mockito.anyObject());

        //Related to sending ETCD request
        URL url = new URL("https://localhost:2379/v2/keys/jti/2f3c1e3a-fe4c-4cd4-b049-156e3c63fc5d");
        PowerMockito.whenNew(URL.class).withAnyArguments().thenReturn(url);
        PowerMockito.when(APIUtil.getHttpClient(Mockito.anyInt(), Mockito.anyString())).thenReturn(etcdEPClient);
        PowerMockito.whenNew(HttpPut.class).withArguments(etcdUrl + "2f3c1e3a-fe4c-4cd4-b049-156e3c63fc5d").
                thenReturn(httpETCDPut).thenReturn(httpETCDPut);
        PowerMockito.whenNew(UrlEncodedFormEntity.class).withAnyArguments().thenReturn(urlEncodedFormEntity);
        PowerMockito.doNothing().when(httpETCDPut).setEntity(urlEncodedFormEntity);
        PowerMockito.when(etcdEPClient.execute(httpETCDPut)).thenReturn(etcdResponse);
        PowerMockito.when(etcdResponse.getStatusLine()).thenReturn(statusLine);
        PowerMockito.when(statusLine.getStatusCode()).thenReturn(200);

        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
    }


    @Test
    public void testSendMessageToPersistentStorage() {
        log.info("Running the test case to check SendMessageToPersistentStorage method.");
        Properties properties = new Properties();
        String DEFAULT_PERSISTENT_NOTIFIER_HOSTNAME = "https://localhost:2379/v2/keys/jti/";
        properties.setProperty("hostname", DEFAULT_PERSISTENT_NOTIFIER_HOSTNAME);
        properties.setProperty("ttl", DEFAULT_TTL);
        String DEFAULT_PERSISTENT_NOTIFIER_USERNAME = "root";
        properties.setProperty("username", DEFAULT_PERSISTENT_NOTIFIER_USERNAME);
        String DEFAULT_PERSISTENT_NOTIFIER_PASSWORD = "root";
        properties.setProperty("password", DEFAULT_PERSISTENT_NOTIFIER_PASSWORD);
        try {
            PowerMockito.whenNew(HttpPut.class).withAnyArguments().thenReturn(httpETCDPut);
            tokenRevocationNotifierImpl = new TokenRevocationNotifierImpl();
            tokenRevocationNotifierImpl.sendMessageToPersistentStorage("1234", properties);
        } catch (Exception e) {
            Assert.fail("Should not throw any exceptions");
        }
        log.info("Finished the test case to check SendMessageToPersistentStorage method.");
    }

    @Test
    public void testSendMessageOnRealTime() {
        Properties properties = new Properties();
        properties.setProperty("ttl", DEFAULT_TTL);
        properties.setProperty("expiryTime", "1571063344");
        log.info("Running the test case to check the sendMessageOnRealTime method.");
        try {
            tokenRevocationNotifierImpl = new TokenRevocationNotifierImpl();
            tokenRevocationNotifierImpl.sendMessageOnRealtime("1234", properties);
        } catch (Exception e) {
            Assert.fail("Should not throw any exceptions");
        }
        log.info("Finished the test case to check the sendMessageOnRealTime method.");
    }
}