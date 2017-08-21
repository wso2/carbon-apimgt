/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.carbon.apimgt.ballerina.publisher.util;

import java.io.File;

/**
 * DAS data publisher util class, used to configure the event receiver
 */
public class DataPublisherTestUtil {
    public static final String LOCAL_HOST = "localhost";
    public static String configPath = "";

    public static void setTrustStoreParams() {
        String trustStore = configPath + File.separator + "bre" + File.separator + "security" + File.separator +
                                                                                               "client-truststore.jks";
        System.setProperty("javax.net.ssl.trustStore", trustStore);
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
    }

    public static void setKeyStoreParams() {
        String keyStore = configPath + File.separator + "bre" + File.separator + "security" + File.separator
                                                                                                    + "wso2carbon.jks";
        System.setProperty("Security.KeyStore.Location", keyStore);
        System.setProperty("Security.KeyStore.Password", "wso2carbon");
    }

    public static String getDataAgentConfigPath() {
        return configPath + File.separator + "stats" + File.separator + "configs" +
                File.separator + "data-agent-config.xml";
    }

    public static String getDataBridgeConfigPath() {
        return configPath + File.separator + "stats" + File.separator +
                "configs" + File.separator + "databridge.config.yaml";
    }
}
