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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.apache.axis2.description.TransportInDescription;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceReferenceHolder.class, SSLSocketFactory.class})
public class APIUtilTest {

    @Test
    public void testGetAPINamefromRESTAPI() {
        String restAPI = "admin--map";
        String apiName = APIUtil.getAPINamefromRESTAPI(restAPI);

        Assert.assertEquals(apiName, "map");
    }

    @Test
    public void testGetAPIProviderFromRESTAPI() {
        String restAPI = "admin--map";
        String providerName = APIUtil.getAPIProviderFromRESTAPI(restAPI, null);

        Assert.assertEquals(providerName, "admin@carbon.super");

        restAPI = "user@test.com--map";
        providerName = APIUtil.getAPIProviderFromRESTAPI(restAPI, "test.com");
        Assert.assertEquals(providerName, "user@test.com");

        restAPI = "user-AT-test.com--map";
        providerName = APIUtil.getAPIProviderFromRESTAPI(restAPI, "test.com");
        Assert.assertEquals(providerName, "user@test.com");

    }

    @Test
    public void testGetHttpClient() {

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        SSLSocketFactory sslSocketFactory = Mockito.mock(SSLSocketFactory.class);
        ConfigurationContextService contextService = Mockito.mock(ConfigurationContextService.class);
        ConfigurationContext configurationContext = Mockito.mock(ConfigurationContext.class);
        AxisConfiguration axisConfiguration = Mockito.mock(AxisConfiguration.class);
        TransportInDescription transportInDescription = Mockito.mock(TransportInDescription.class);

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(SSLSocketFactory.class);

        PowerMockito.when(serviceReferenceHolder.getContextService()).thenReturn(contextService);
        PowerMockito.when(SSLSocketFactory.getSocketFactory()).thenReturn(sslSocketFactory);
        Mockito.when(contextService.getServerConfigContext()).thenReturn(configurationContext);
        Mockito.when(configurationContext.getAxisConfiguration()).thenReturn(axisConfiguration);
        Mockito.when(axisConfiguration.getTransportIn(Mockito.anyString())).thenReturn(transportInDescription);
        Mockito.when(transportInDescription.getParameter(Mockito.anyString())).thenReturn(null);

        HttpClient client = APIUtil.getHttpClient(3244, "http");

        Assert.assertNotNull(client);
        Scheme scheme = client.getConnectionManager().getSchemeRegistry().get("http");
        Assert.assertEquals(3244, scheme.getDefaultPort());

        client = APIUtil.getHttpClient(3244, "https");
        Assert.assertNotNull(client);
        scheme = client.getConnectionManager().getSchemeRegistry().get("https");
        Assert.assertEquals(3244, scheme.getDefaultPort());

        client = APIUtil.getHttpClient(-1, "http");
        Assert.assertNotNull(client);
        scheme = client.getConnectionManager().getSchemeRegistry().get("http");
        Assert.assertEquals(80, scheme.getDefaultPort());

        client = APIUtil.getHttpClient(-1, "https");
        Assert.assertNotNull(client);
        scheme = client.getConnectionManager().getSchemeRegistry().get("https");
        Assert.assertEquals(443, scheme.getDefaultPort());
    }

    @Test
    public void testGetHttpClientIgnoreHostNameVerify() {

        System.setProperty("org.wso2.ignoreHostnameVerification", "true");

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        SSLSocketFactory sslSocketFactory = Mockito.mock(SSLSocketFactory.class);
        ConfigurationContextService contextService = Mockito.mock(ConfigurationContextService.class);
        ConfigurationContext configurationContext = Mockito.mock(ConfigurationContext.class);
        AxisConfiguration axisConfiguration = Mockito.mock(AxisConfiguration.class);
        TransportInDescription transportInDescription = Mockito.mock(TransportInDescription.class);

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(SSLSocketFactory.class);

        PowerMockito.when(serviceReferenceHolder.getContextService()).thenReturn(contextService);
        PowerMockito.when(SSLSocketFactory.getSocketFactory()).thenReturn(sslSocketFactory);
        Mockito.when(contextService.getServerConfigContext()).thenReturn(configurationContext);
        Mockito.when(configurationContext.getAxisConfiguration()).thenReturn(axisConfiguration);
        Mockito.when(axisConfiguration.getTransportIn(Mockito.anyString())).thenReturn(transportInDescription);
        Mockito.when(transportInDescription.getParameter(Mockito.anyString())).thenReturn(null);

        HttpClient client = APIUtil.getHttpClient(3244, "https");

        Assert.assertNotNull(client);


    }
}
