/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.wso2.carbon.apimgt.api.model;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a Backend service.
 * This class encapsulates the details of a backend service including its ID, name,
 * definition, and endpoint configuration.
 */
public class Backend implements Serializable {

    private String id;
    private String name;
    private String definition;
    private String endpointConfig;

    public Backend() {

    }

    public Backend(Backend backend) {

        this.id = backend.id;
        this.name = backend.name;
        this.definition = backend.definition;
        this.endpointConfig = backend.endpointConfig;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getDefinition() {

        return definition;
    }

    public void setDefinition(String definition) {

        this.definition = definition;
    }

    public String getEndpointConfig() {

        return endpointConfig;
    }

    public void setEndpointConfig(String endpointConfig) {

        this.endpointConfig = endpointConfig;

    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    /**
     * Returns the endpoint configuration as a Map.
     *
     * @return A Map representing the endpoint configuration, or null if the configuration is empty or null.
     * @throws ParseException If there is an error parsing the JSON string.
     */
    public Map<String, Object> getEndpointConfigAsMap() throws ParseException {

        if (endpointConfig == null || endpointConfig.isEmpty()) return null;
        JSONObject obj = (JSONObject) new JSONParser().parse(endpointConfig);
        return new LinkedHashMap<>(obj);
    }

    /**
     * Sets the endpoint configuration from a Map.
     *
     * @param map A Map representing the endpoint configuration. If null, the endpoint configuration will be set to
     *            null.
     */
    public void setEndpointConfigFromMap(Map<String, Object> map) {

        if (map == null) {
            this.endpointConfig = null;
            return;
        }
        this.endpointConfig = JSONObject.toJSONString(map);
    }
}
