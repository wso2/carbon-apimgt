/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.apimgt.gateway.threatprotection.pool.AnalyzerPool;
import org.wso2.carbon.apimgt.gateway.threatprotection.pool.JSONAnalyzerFactory;
import org.wso2.carbon.apimgt.gateway.threatprotection.pool.XMLAnalyzerFactory;
import org.wso2.carbon.apimgt.gateway.threatprotection.utils.ThreatProtectorConstants;

/**
 * Holds the object pools for XML and JSON Analyzers
 */
public class AnalyzerHolder {

    private static Logger log = LoggerFactory.getLogger(AnalyzerHolder.class);
    private static AnalyzerHolder instance = new AnalyzerHolder();
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

    public static AnalyzerHolder getInstance() {
        return instance;
    }

    /**
     * Borrows an object from pools (xml or json) for threat analysis
     *
     * @param contentType Content-Type of the payload
     * @return Instance of APIMThreatAnalyzer based on content type
     */
    public static APIMThreatAnalyzer getAnalyzer(String contentType) {
        APIMThreatAnalyzer analyzer = null;
        if (ThreatProtectorConstants.TEXT_XML.equalsIgnoreCase(contentType) ||
                ThreatProtectorConstants.APPLICATION_XML.equalsIgnoreCase(contentType)) {
            try {
                analyzer = xmlAnalyzerAnalyzerPool.borrowObject();
            } catch (Exception e) {
                // here apache.commons GenericObjectPool's borrow object method throws generic exception.
                // here log the stacktrace along with the message.
                log.error("Threat Protection: Error occurred while getting an object from the pool.", e);
            }
        } else if (ThreatProtectorConstants.TEXT_JSON.equalsIgnoreCase(contentType) ||
                ThreatProtectorConstants.APPLICATION_JSON.equalsIgnoreCase(contentType)) {
            try {
                analyzer = jsonAnalyzerAnalyzerPool.borrowObject();
            } catch (Exception e) {
                log.error("Threat Protection: Error occurred while getting an object from the pool.", e);
            }
        }
        return analyzer;
    }

    /**
     * Returns objects back to the pool
     *
     * @param analyzer borrowed instance of {@link APIMThreatAnalyzer}
     */
    public static void returnObject(APIMThreatAnalyzer analyzer) {
        analyzer.clearConfiguration();
        if (analyzer instanceof JSONAnalyzer) {
            jsonAnalyzerAnalyzerPool.returnObject((JSONAnalyzer) analyzer);
        } else if (analyzer instanceof XMLAnalyzer) {
            xmlAnalyzerAnalyzerPool.returnObject((XMLAnalyzer) analyzer);
        }
    }
}
