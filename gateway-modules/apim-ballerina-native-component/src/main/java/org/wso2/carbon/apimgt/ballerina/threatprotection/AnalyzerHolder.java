/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.ballerina.threatprotection;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.ballerina.threatprotection.analyzer.APIMThreatAnalyzer;
import org.wso2.carbon.apimgt.ballerina.threatprotection.analyzer.JSONAnalyzer;
import org.wso2.carbon.apimgt.ballerina.threatprotection.analyzer.XMLAnalyzer;
import org.wso2.carbon.apimgt.ballerina.threatprotection.configurations.ConfigurationHolder;
import org.wso2.carbon.apimgt.ballerina.threatprotection.configurations.JSONConfig;
import org.wso2.carbon.apimgt.ballerina.threatprotection.configurations.XMLConfig;
import org.wso2.carbon.apimgt.ballerina.threatprotection.pool.AnalyzerPool;
import org.wso2.carbon.apimgt.ballerina.threatprotection.pool.JSONAnalyzerFactory;
import org.wso2.carbon.apimgt.ballerina.threatprotection.pool.XMLAnalyzerFactory;

/**
 * Holds the object pools for XML and JSON Analyzers
 */
public class AnalyzerHolder {
    private static final String T_TEXT_XML = "text/xml";
    private static final String T_APPLICATION_XML = "application/xml";
    private static final String T_TEXT_JSON = "text/json";
    private static final String T_APPLICATION_JSON = "application/json";

    private static Logger logger = LoggerFactory.getLogger(AnalyzerHolder.class);

    private static AnalyzerPool<XMLAnalyzer> xmlAnalyzerAnalyzerPool;
    private static AnalyzerPool<JSONAnalyzer> jsonAnalyzerAnalyzerPool;
    private static GenericObjectPoolConfig poolConfig;

    static {
        poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(200);

        poolConfig.setBlockWhenExhausted(false);
        poolConfig.setMaxWaitMillis(0);

        xmlAnalyzerAnalyzerPool = new AnalyzerPool<>(new XMLAnalyzerFactory(), poolConfig);
        jsonAnalyzerAnalyzerPool = new AnalyzerPool<>(new JSONAnalyzerFactory(), poolConfig);
    }

    private AnalyzerHolder() {

    }

    /**
     * Borrows an object from pools (xml or json) for threat analysis
     *
     * @param contentType Content-Type of the payload
     * @param policyId ID of the API
     * @return Instance of APIMThreatAnalyzer based on content type
     */
    public static APIMThreatAnalyzer getAnalyzer(String contentType, String policyId) {
        APIMThreatAnalyzer analyzer = null;
        if (T_TEXT_XML.equalsIgnoreCase(contentType) || T_APPLICATION_XML.equalsIgnoreCase(contentType)) {
            try {
                analyzer = xmlAnalyzerAnalyzerPool.borrowObject();

                //configure per api
                XMLConfig xmlConfig = ConfigurationHolder.getXmlConfig(policyId);
                if (xmlConfig == null) {
                    xmlConfig = ConfigurationHolder.getXmlConfig("GLOBAL-XML");
                }

                if (xmlConfig == null) {
                    return null;
                }
                analyzer.configure(xmlConfig);
            } catch (Exception e) {
                logger.error("Threat Protection: Failed to create XMLAnalyzer, " + e.getMessage());
            }
        } else if (T_TEXT_JSON.equalsIgnoreCase(contentType) || T_APPLICATION_JSON.equalsIgnoreCase(contentType)) {
            try {
                analyzer = jsonAnalyzerAnalyzerPool.borrowObject();

                //configure per api
                JSONConfig jsonConfig = ConfigurationHolder.getJsonConfig(policyId);
                if (jsonConfig == null) {
                    jsonConfig = ConfigurationHolder.getJsonConfig("GLOBAL-JSON");
                }

                if (jsonConfig == null) {
                    return null;
                }
                analyzer.configure(jsonConfig);
            } catch (Exception e) {
                logger.error("Threat Protection: Failed to create JSONAnalyzer, " + e.getMessage());
            }
        }
        return analyzer;
    }

    /**
     * Returns objects back to the pool
     *
     * @param analyzer borrowed instance of {@link APIMThreatAnalyzer} via
     * {@link AnalyzerHolder#getAnalyzer(String, String)}
     */
    public static void returnObject(APIMThreatAnalyzer analyzer) {
        if (analyzer instanceof JSONAnalyzer) {
            jsonAnalyzerAnalyzerPool.returnObject((JSONAnalyzer) analyzer);
        } else if (analyzer instanceof XMLAnalyzer) {
            xmlAnalyzerAnalyzerPool.returnObject((XMLAnalyzer) analyzer);
        }
    }
}
