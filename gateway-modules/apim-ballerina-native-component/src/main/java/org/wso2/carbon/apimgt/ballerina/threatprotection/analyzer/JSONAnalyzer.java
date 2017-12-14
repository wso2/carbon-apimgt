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

package org.wso2.carbon.apimgt.ballerina.threatprotection.analyzer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.ballerina.threatprotection.APIMThreatAnalyzerException;
import org.wso2.carbon.apimgt.ballerina.threatprotection.configurations.JSONConfig;
import org.wso2.carbon.apimgt.ballerina.threatprotection.configurations.XMLConfig;

import java.io.IOException;
import java.io.StringReader;

/**
 * Implementation of APIMThreatAnalyzer for JSON Payloads
 */
public class JSONAnalyzer implements APIMThreatAnalyzer {
    private static final String JSON_THREAT_PROTECTION_MSG_PREFIX = "Threat Protection-JSON: ";

    private Logger logger = LoggerFactory.getLogger(JSONAnalyzer.class);

    private JsonFactory factory;

    //0 means unlimited
    private int maxFieldCount = 0;
    private int maxStringLength = 0;
    private int maxArrayElementCount = 0;
    private int maxFieldLength = 0;
    private int maxJsonDepth = 0;

    public JSONAnalyzer() {
        factory = new JsonFactory();
    }

    /**
     * Create a JSONAnalyzer using default configuration values
     */
    public void configure(JSONConfig config) {
        maxFieldCount = config.getMaxPropertyCount();
        maxStringLength = config.getMaxStringLength();
        maxArrayElementCount = config.getMaxArrayElementCount();
        maxFieldLength = config.getMaxKeyLength();
        maxJsonDepth = config.getMaxJsonDepth();
    }

    @Override
    public void configure(XMLConfig config) {
        throw new UnsupportedOperationException("This method is not supported on this instance");
    }

    /**
     *
     * @param payload json payload
     * @throws APIMThreatAnalyzerException if defined limits for json payload exceeds
     */
    @Override
    public void analyze(String payload, String apiContext) throws APIMThreatAnalyzerException {
        try (JsonParser parser = factory.createParser(new StringReader(payload))) {
            int currentDepth = 0;
            int currentFieldCount = 0;
            JsonToken token;
            while ((token = parser.nextToken()) != null) {
                switch (token) {
                    case START_OBJECT:
                        currentDepth += 1;
                        try {
                            analyzeDepth(maxJsonDepth, currentDepth, apiContext);
                        } catch (APIMThreatAnalyzerException e) {
                            throw e;
                        }
                        break;

                    case END_OBJECT:
                        currentDepth -= 1;
                        break;

                    case FIELD_NAME:
                        currentFieldCount += 1;
                        String name = parser.getCurrentName();
                        try {
                            analyzeField(name, maxFieldCount, currentFieldCount, maxFieldLength, apiContext);
                        } catch (APIMThreatAnalyzerException e) {
                            throw e;
                        }
                        break;

                    case VALUE_STRING:
                        String value = parser.getText();
                        try {
                            analyzeString(value, maxStringLength, apiContext);
                        } catch (APIMThreatAnalyzerException e) {
                            throw e;
                        }
                        break;

                    case START_ARRAY:
                        try {
                            analyzeArray(parser, maxArrayElementCount, maxStringLength, apiContext);
                        } catch (APIMThreatAnalyzerException e) {
                            throw e;
                        }
                        break;
                }
            }
        } catch (JsonParseException e) {
            logger.error(JSON_THREAT_PROTECTION_MSG_PREFIX + apiContext + " - Payload parsing failed", e);
            throw new APIMThreatAnalyzerException(JSON_THREAT_PROTECTION_MSG_PREFIX + apiContext
                    + " - Payload parsing failed", e);
        } catch (IOException e) {
            logger.error(JSON_THREAT_PROTECTION_MSG_PREFIX + apiContext + " - Payload build failed", e);
            throw new APIMThreatAnalyzerException(JSON_THREAT_PROTECTION_MSG_PREFIX + apiContext
                    + " - Payload build failed", e);
        }
    }

    /**
     *
     * @param maxDepth maximum depth allowed for json payload
     * @param currentDepth current depth of json payload
     * @param apiContext current api context
     * @throws APIMThreatAnalyzerException if currentDepth is greater than maxDepth
     */
    public void analyzeDepth(int maxDepth, int currentDepth, String apiContext) throws APIMThreatAnalyzerException {
        if (maxDepth == 0) {
            return;
        }
        if (currentDepth > maxDepth) {
            logger.error(JSON_THREAT_PROTECTION_MSG_PREFIX + apiContext + " - Depth Limit Reached");
            throw new APIMThreatAnalyzerException(JSON_THREAT_PROTECTION_MSG_PREFIX
                    + apiContext + " - Depth Limit Reached");
        }
    }

    /**
     * Analyzes json fields using defined limits
     *
     * @param field value of the json field
     * @param maxFieldCount maximum number of fields allowed
     * @param currentFieldCount current field count
     * @param maxFieldLength maximum field length allowed
     * @param apiContext current api context
     * @throws APIMThreatAnalyzerException if current values exceed maximum values
     */
    private void analyzeField(String field, int maxFieldCount, int currentFieldCount, int maxFieldLength,
            String apiContext) throws APIMThreatAnalyzerException {
        if (field == null) {
            return;
        }

        if (maxFieldLength > 0 && field.length() > maxFieldLength) {
            logger.error(JSON_THREAT_PROTECTION_MSG_PREFIX + apiContext + " - Max Key Length Reached");
            throw new APIMThreatAnalyzerException(JSON_THREAT_PROTECTION_MSG_PREFIX + apiContext
                    + " - Max Key Length Reached");
        }

        if (maxFieldCount > 0 && currentFieldCount > maxFieldCount) {
            logger.error(JSON_THREAT_PROTECTION_MSG_PREFIX +  apiContext
                    + " - Max Property Count Reached");
            throw new APIMThreatAnalyzerException(JSON_THREAT_PROTECTION_MSG_PREFIX
                    + apiContext + " - Max Property Count Reached");
        }
    }

    /**
     * Analyzes json string values using defined limits
     *
     * @param value value of the string
     * @param maxLength maximum string length allowed
     * @param apiContext current api context
     * @throws APIMThreatAnalyzerException if string length is greater than maximum length provided
     */
    private void analyzeString(String value, int maxLength, String apiContext) throws APIMThreatAnalyzerException {
        if (value == null || maxLength == 0) {
            return;
        }

        if (value.length() > maxLength) {
            logger.error(JSON_THREAT_PROTECTION_MSG_PREFIX + apiContext
                    + " - Max String Length Reached");
            throw new APIMThreatAnalyzerException(JSON_THREAT_PROTECTION_MSG_PREFIX + apiContext
                    + " - Max String Length Reached");
        }
    }

    /**
     * Analyzes json arrays using defined limits
     *
     * @param parser JsonParser instance (Current token should be at JsonToken.START_ARRAY state)
     * @param maxArrayElementCount maximum array element count allowed
     * @param maxStringLength maximum string length allowed
     * @param apiContext current api context
     * @throws APIMThreatAnalyzerException if array/string length is greater than maximum values provided
     */
    private void analyzeArray(JsonParser parser, int maxArrayElementCount, int maxStringLength, String apiContext)
            throws APIMThreatAnalyzerException {
        JsonToken token;
        try {
            //parser is in wrong state. Do nothing.
            if (parser.currentToken() != JsonToken.START_ARRAY) {
                return;
            }

            int arrayElementCount = 0;
            while ((token = parser.nextToken()) != JsonToken.END_ARRAY) {
                //analyzing string values inside array
                if (token == JsonToken.VALUE_STRING) {
                    String value = parser.getText();
                    try {
                        analyzeString(value, maxStringLength, apiContext);
                    } catch (APIMThreatAnalyzerException e) {
                        throw e;
                    }
                }

                arrayElementCount += 1;
                if (maxArrayElementCount == 0) {
                    continue;
                }
                if (arrayElementCount > maxArrayElementCount) {
                    logger.error(JSON_THREAT_PROTECTION_MSG_PREFIX + apiContext
                            + " - Max Array Length Reached");
                    throw new APIMThreatAnalyzerException(JSON_THREAT_PROTECTION_MSG_PREFIX + apiContext
                            + " - Max Array Length Reached");
                }
            }
        } catch (IOException e) {
            logger.error(JSON_THREAT_PROTECTION_MSG_PREFIX + apiContext + " - Array Parsing Error", e);
            throw new APIMThreatAnalyzerException(JSON_THREAT_PROTECTION_MSG_PREFIX + apiContext
                    + " - Array Parsing Error", e);
        }
    }
}
