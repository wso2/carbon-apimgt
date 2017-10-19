/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.hostobjects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.wso2.carbon.apimgt.impl.APIConstants;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import java.io.File;

/**
 * APIProvider Test Suite
 */
public class APIProviderHostObjectTest {

    private static final Log log = LogFactory.getLog(APIProviderHostObjectTest.class);
    private static final String KEYSTORE_FILE_PATH =
            "src" + File.separator + "test" + File.separator + "resources" + File.separator + "security"
                    + File.separator + "server" + File.separator + "wso2carbon.jks";
    private static final String TRUSTSTORE_FILE_PATH =
            "src" + File.separator + "test" + File.separator + "resources" + File.separator + "security"
                    + File.separator + "server" + File.separator + "client-truststore.jks";

    private static final String KEYSTORE_FILE_PATH_CLIENT =
            "src" + File.separator + "test" + File.separator + "resources" + File.separator + "security"
                    + File.separator + "client" + File.separator + "wso2carbon.jks";
    private static final String TRUSTSTORE_FILE_PATH_CLIENT =
            "src" + File.separator + "test" + File.separator + "resources" + File.separator + "security"
                    + File.separator + "client" + File.separator + "client-truststore.jks";

    public void testAPIProvider() {

    }

    @Rule
    public WireMockRule wireMockRule;

    @Test
    public void testMutualSSLEnabledBackend() {
        wireMockRule = new WireMockRule(wireMockConfig()
                .httpsPort(8081)
                .needClientAuth(true)
                .trustStoreType("JKS")
                .keystoreType("JKS")
                .keystorePath(KEYSTORE_FILE_PATH)
                .trustStorePath(TRUSTSTORE_FILE_PATH)
                .trustStorePassword("wso2carbon")
                .keystorePassword("wso2carbon"));
        wireMockRule.start();
        // Mock service for key manager token endpoint
        wireMockRule.stubFor(head(urlEqualTo("/test"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("{success}")
                        .withHeader("Content-Type", "application/json")
                ));
        System.setProperty("javax.net.ssl.keyStoreType", "JKS");
        System.setProperty("javax.net.ssl.keyStore", KEYSTORE_FILE_PATH_CLIENT);
        System.setProperty("javax.net.ssl.keyStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStore", TRUSTSTORE_FILE_PATH_CLIENT);
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        //Success case
        org.mozilla.javascript.NativeObject obj =
                HostObjectUtils.sendHttpHEADRequest("https://localhost:8081/test",
                        "404");
        Assert.assertEquals("success", obj.get("response"));
        //Non available path
        org.mozilla.javascript.NativeObject objError =
                HostObjectUtils.sendHttpHEADRequest("https://localhost:8081/best",
                        "404");
        Assert.assertNotEquals("success", objError.get("response"));
        //Error Port
        org.mozilla.javascript.NativeObject objErrorPort =
                HostObjectUtils.sendHttpHEADRequest("https://localhost:8082/best",
                        "404");
        Assert.assertNotEquals("success", objErrorPort.get("response"));
        //Invalid credentials
        System.setProperty("javax.net.ssl.trustStorePassword", "Wrong-Password");
        org.mozilla.javascript.NativeObject objErrorSSL =
                HostObjectUtils.sendHttpHEADRequest("https://localhost:8081/best",
                        "404");
        Assert.assertNotEquals("success", objErrorSSL.get("response"));
        //With Proxy host and port
        System.setProperty(APIConstants.HTTP_PROXY_HOST, "localhost");
        System.setProperty(APIConstants.HTTP_PROXY_PORT, "8081");
        obj = HostObjectUtils.sendHttpHEADRequest("https://localhost:8081/test",
                "404");
        Assert.assertEquals("success", obj.get("response"));
        wireMockRule.resetAll();
        wireMockRule.stop();

    }
}
