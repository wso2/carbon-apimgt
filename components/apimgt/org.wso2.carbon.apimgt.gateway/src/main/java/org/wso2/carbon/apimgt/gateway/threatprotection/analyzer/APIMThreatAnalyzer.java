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

package org.wso2.carbon.apimgt.gateway.threatprotection.analyzer;


import org.wso2.carbon.apimgt.gateway.threatprotection.APIMThreatAnalyzerException;
import org.wso2.carbon.apimgt.gateway.threatprotection.configuration.JSONConfig;
import org.wso2.carbon.apimgt.gateway.threatprotection.configuration.XMLConfig;

import java.io.InputStream;

/**
 * Interface for json/xml analyzers
 */
public interface APIMThreatAnalyzer {

    /**
     * Analyzes json/xml payloads for malicious content
     *
     * @param inputStream   Input stream
     * @param apiContext API Context
     * @throws APIMThreatAnalyzerException
     */
    void analyze(InputStream inputStream, String apiContext) throws APIMThreatAnalyzerException;

    /**
     * Configures the XMLAnalyzer using XMLConfig
     *
     * @param config instance of the XMLConfig with appropriate configuration values
     * @throws UnsupportedOperationException if called on a JSONAnalyzer instance
     */
    void configure(XMLConfig config);

    /**
     * Configures the JSONAnalyzer using JSONConfig
     *
     * @param config instance of the JSONConfig with appropriate configuration values
     * @throws UnsupportedOperationException if called on a XMLAnalyzer instance
     */
    void configure(JSONConfig config);

    /**
     * Check whether payload analyzing is enabled by configs
     *
     * @return true if payload analyzing is enabled, false otherwise
     */
    boolean isEnabled();

    void clearConfiguration();
}
