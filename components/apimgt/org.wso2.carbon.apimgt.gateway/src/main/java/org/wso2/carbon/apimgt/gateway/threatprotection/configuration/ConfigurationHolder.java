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
