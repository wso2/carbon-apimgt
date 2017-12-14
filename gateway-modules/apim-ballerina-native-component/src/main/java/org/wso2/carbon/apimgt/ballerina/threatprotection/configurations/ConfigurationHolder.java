package org.wso2.carbon.apimgt.ballerina.threatprotection.configurations;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds the threat protection policies
 */
public class ConfigurationHolder {
    public static final String GLOBAL_CONFIG_KEY = "GLOBAL";

    private static Map<String, JSONConfig> jsonConfigMap;
    private static Map<String, XMLConfig> xmlConfigMap;

    static {
        jsonConfigMap = new ConcurrentHashMap<>();
        xmlConfigMap = new ConcurrentHashMap<>();
    }

    public static void addJsonConfig(String policyId, JSONConfig config) {
        jsonConfigMap.put(policyId, config);
    }

    public static void addXmlConfig(String policyId, XMLConfig config) {
        xmlConfigMap.put(policyId, config);
    }

    public static JSONConfig getJsonConfig(String policyId) {
        return jsonConfigMap.get(policyId);
    }

    public static XMLConfig getXmlConfig(String policyId) {
        return xmlConfigMap.get(policyId);
    }

    public static void removeJsonConfig(String policyId) {
        jsonConfigMap.remove(policyId);
    }

    public static void removeXmlConfig(String policyId) {
        xmlConfigMap.remove(policyId);
    }
}
