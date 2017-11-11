/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.threatprotection.configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds the threat protection configuration objects for apis
 */
public class ConfigurationHolder {
    public static final String GLOBAL_CONFIG_KEY = "GLOBAL";

    private static Map<String, JSONConfig> jsonConfigMap;
    private static Map<String, XMLConfig> xmlConfigMap;

    static {
        jsonConfigMap = new ConcurrentHashMap<>();
        xmlConfigMap = new ConcurrentHashMap<>();
    }

    public static void addJsonConfig(String apiId, JSONConfig config) {
        jsonConfigMap.put(apiId, config);
    }

    public static void addXmlConfig(String apiId, XMLConfig config) {
        xmlConfigMap.put(apiId, config);
    }

    public static JSONConfig getJsonConfig(String apiId) {
        return jsonConfigMap.get(apiId);
    }

    public static XMLConfig getXmlConfig(String apiId) {
        return xmlConfigMap.get(apiId);
    }

    public static void removeJsonConfig(String apiId) {
        jsonConfigMap.remove(apiId);
    }

    public static void removeXmlConfig(String apiId) {
        xmlConfigMap.remove(apiId);
    }
}
