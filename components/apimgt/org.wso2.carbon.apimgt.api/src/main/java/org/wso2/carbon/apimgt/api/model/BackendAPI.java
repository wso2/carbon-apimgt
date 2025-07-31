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

import java.io.Serializable;

/**
 * Represents a Backend API in the API Management system.
 * This class encapsulates the details of a backend API including its ID, definition,
 * endpoint configuration, and name.
 */
public class BackendAPI implements Serializable {

    private String backendApiId;
    private String apiDefinition;
    private String endpointConfig;
    private String backendApiName;

    public String getBackendApiId() {

        return backendApiId;
    }

    public void setBackendApiId(String backendApiId) {

        this.backendApiId = backendApiId;
    }

    public String getApiDefinition() {

        return apiDefinition;
    }

    public void setApiDefinition(String apiDefinition) {

        this.apiDefinition = apiDefinition;
    }

    public String getEndpointConfig() {

        return endpointConfig;
    }

    public void setEndpointConfig(String endpointConfig) {

        this.endpointConfig = endpointConfig;

    }

    public String getBackendApiName() {

        return backendApiName;
    }

    public void setBackendApiName(String backendApiName) {

        this.backendApiName = backendApiName;
    }
}
