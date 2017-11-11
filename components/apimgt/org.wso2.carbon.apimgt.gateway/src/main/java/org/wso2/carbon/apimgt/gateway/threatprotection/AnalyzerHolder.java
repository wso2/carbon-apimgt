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

package org.wso2.carbon.apimgt.gateway.threatprotection;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.gateway.threatprotection.analyzer.APIMThreatAnalyzer;
import org.wso2.carbon.apimgt.gateway.threatprotection.analyzer.JSONAnalyzer;
import org.wso2.carbon.apimgt.gateway.threatprotection.analyzer.XMLAnalyzer;
import org.wso2.carbon.apimgt.gateway.threatprotection.configuration.ConfigurationHolder;
import org.wso2.carbon.apimgt.gateway.threatprotection.configuration.JSONConfig;
import org.wso2.carbon.apimgt.gateway.threatprotection.configuration.XMLConfig;
import org.wso2.carbon.apimgt.gateway.threatprotection.pool.AnalyzerPool;
import org.wso2.carbon.apimgt.gateway.threatprotection.pool.JSONAnalyzerFactory;
import org.wso2.carbon.apimgt.gateway.threatprotection.pool.XMLAnalyzerFactory;

/**
 * Holds the object pools for XML and JSON Analyzers
 */
public class AnalyzerHolder {
    private static final String T_TEXT_XML = "text/xml";
    private static final String T_APPLICATION_XML = "application/xml";
    private static final String T_TEXT_JSON = "text/json";
    private static final String T_APPLICATION_JSON = "application/json";

    private static Logger log = LoggerFactory.getLogger(AnalyzerHolder.class);

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
     * @return Instance of APIMThreatAnalyzer based on content type
     */
    public static APIMThreatAnalyzer getAnalyzer(String contentType, String apiPath) {
        APIMThreatAnalyzer analyzer = null;
        if (T_TEXT_XML.equalsIgnoreCase(contentType) || T_APPLICATION_XML.equalsIgnoreCase(contentType)) {
            try {
                analyzer = xmlAnalyzerAnalyzerPool.borrowObject();

                XMLConfig xmlConfig = ConfigurationHolder.getXmlConfig(apiPath);

                analyzer.configure(xmlConfig);
            } catch (Exception e) {
                log.error("Threat Protection: Failed to create XMLAnalyzer, " + e.getMessage());
            }
        } else if (T_TEXT_JSON.equalsIgnoreCase(contentType) || T_APPLICATION_JSON.equalsIgnoreCase(contentType)) {
            try {
                analyzer = jsonAnalyzerAnalyzerPool.borrowObject();

                JSONConfig jsonConfig = ConfigurationHolder.getJsonConfig(apiPath);
                analyzer.configure(jsonConfig);
            } catch (Exception e) {
                log.error("Threat Protection: Failed to create JSONAnalyzer, " + e.getMessage());
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
