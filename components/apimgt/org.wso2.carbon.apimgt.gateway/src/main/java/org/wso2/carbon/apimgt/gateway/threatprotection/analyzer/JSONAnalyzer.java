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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.wso2.carbon.apimgt.gateway.threatprotection.APIMThreatAnalyzerException;
import org.wso2.carbon.apimgt.gateway.threatprotection.configuration.JSONConfig;
import org.wso2.carbon.apimgt.gateway.threatprotection.configuration.XMLConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Implementation of APIMThreatAnalyzer for JSON Payloads
 */
public class JSONAnalyzer implements APIMThreatAnalyzer {

    private static final String JSON_THREAT_PROTECTION_MSG_PREFIX = "Threat Protection-JSON: ";
    private JsonFactory factory;
    private boolean enabled = true;
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
        enabled = config.isEnabled();
        maxFieldCount = config.getMaxPropertyCount();
        maxStringLength = config.getMaxStringLength();
        maxArrayElementCount = config.getMaxArrayElementCount();
        maxFieldLength = config.getMaxKeyLength();
        maxJsonDepth = config.getMaxJsonDepth();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void clearConfiguration() {
        this.enabled = true;
        this.maxFieldCount = 0;
        this.maxStringLength = 0;
        this.maxArrayElementCount = 0;
        this.maxFieldLength = 0;
        this.maxJsonDepth = 0;
    }

    @Override
    public void configure(XMLConfig config) {
        throw new UnsupportedOperationException("This method is not supported on this instance");
    }

    /** Analyze the JSON payload against limitations.
     * @param in input stream of the request payload.
     * @param apiContext request api context.
     * @throws APIMThreatAnalyzerException if defined limits for json payload exceeds
     */
    @Override
    public void analyze(InputStream in, String apiContext) throws APIMThreatAnalyzerException {
        try (JsonParser parser = factory.createParser(new InputStreamReader(in))) {
            int currentDepth = 0;
            int currentFieldCount = 0;
            JsonToken token;
            while ((token = parser.nextToken()) != null) {
                switch (token) {
                    case START_OBJECT:
                        currentDepth += 1;
                        analyzeDepth(maxJsonDepth, currentDepth, apiContext);
                        break;

                    case END_OBJECT:
                        currentDepth -= 1;
                        break;

                    case FIELD_NAME:
                        currentFieldCount += 1;
                        String name = parser.getCurrentName();
                        analyzeField(name, maxFieldCount, currentFieldCount, maxFieldLength);
                        break;

                    case VALUE_STRING:
                        String value = parser.getText();
                        analyzeString(value, maxStringLength);
                        break;

                    case START_ARRAY:
                        analyzeArray(parser, maxArrayElementCount, maxStringLength);
                }
            }
        }
        catch (JsonParseException e) {
            throw new APIMThreatAnalyzerException("Error occurred while parsing the JSON payload", e);
        } catch (IOException e) {
            throw new APIMThreatAnalyzerException("Error occurred while reading the JSON payload.", e);
        }
    }

    /**
     * @param maxDepth     maximum depth allowed for json payload
     * @param currentDepth current depth of json payload
     * @param apiContext   current api context
     * @throws APIMThreatAnalyzerException if currentDepth is greater than maxDepth
     */
    public void analyzeDepth(int maxDepth, int currentDepth, String apiContext) throws APIMThreatAnalyzerException {
        if (currentDepth > maxDepth) {
            throw new APIMThreatAnalyzerException(JSON_THREAT_PROTECTION_MSG_PREFIX
                    + apiContext + " - Depth Limit [" + maxDepth + "] Reached");
        }
    }

    /**
     * Analyzes json fields using defined limits
     *
     * @param field             value of the json field
     * @param maxFieldCount     maximum number of fields allowed
     * @param currentFieldCount current field count
     * @param maxFieldLength    maximum field length allowed
     * @throws APIMThreatAnalyzerException if current values exceed maximum values
     */
    private void analyzeField(String field, int maxFieldCount, int currentFieldCount, int maxFieldLength)
            throws APIMThreatAnalyzerException {
        if (field == null) {
            return;
        }
        if (field.length() > maxFieldLength) {
            throw new APIMThreatAnalyzerException(" Max Key Length [" + maxFieldLength + "] Reached");
        }
        if (currentFieldCount > maxFieldCount) {
            throw new APIMThreatAnalyzerException("Max Property Count [" + maxFieldCount + "] Reached");
        }
    }

    /**
     * Analyzes json string values using defined limits
     *
     * @param value      value of the string
     * @param maxLength  maximum string length allowed
     * @throws APIMThreatAnalyzerException if string length is greater than maximum length provided
     */
    private void analyzeString(String value, int maxLength) throws APIMThreatAnalyzerException {
        if (value == null) {
            return;
        }
        if (value.length() > maxLength) {
            throw new APIMThreatAnalyzerException("Max String Length [" + maxLength + "] Reached");
        }
    }

    /**
     * Analyzes json arrays using defined limits
     *
     * @param parser               JsonParser instance (Current token should be at JsonToken.START_ARRAY state)
     * @param maxArrayElementCount maximum array element count allowed
     * @param maxStringLength      maximum string length allowed
     * @throws APIMThreatAnalyzerException if array/string length is greater than maximum values provided
     */
    private void analyzeArray(JsonParser parser, int maxArrayElementCount, int maxStringLength)
            throws APIMThreatAnalyzerException {
        JsonToken token;
        try {
            int arrayElementCount = 0;
            while ((token = parser.nextToken()) != JsonToken.END_ARRAY) {
                //analyzing string values inside the array
                if (token == JsonToken.VALUE_STRING) {
                    String value = parser.getText();
                    analyzeString(value, maxStringLength);
                }
                arrayElementCount += 1;
                if (arrayElementCount > maxArrayElementCount) {
                    throw new APIMThreatAnalyzerException(" Max Array Length [" + maxArrayElementCount + "] Reached");
                }
            }
        } catch (IOException e) {
            throw new APIMThreatAnalyzerException("Array Parsing Error", e);
        }
    }
}
