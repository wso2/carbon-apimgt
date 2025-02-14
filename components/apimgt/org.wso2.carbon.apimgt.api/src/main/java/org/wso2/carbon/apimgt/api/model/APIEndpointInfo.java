/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.api.model;

import java.util.Map;

/**
 * API Endpoint details.
 */
public class APIEndpointInfo {
    private String endpointUuid;
    private String endpointName;
    private Map<String, Object> endpointConfig;
    private String deploymentStage;

    public String getEndpointName() { return endpointName; }

    public void setEndpointName(String endpointName) {
        this.endpointName = endpointName;
    }

    public Map<String, Object> getEndpointConfig() {
        return endpointConfig;
    }

    public void setEndpointConfig(Map endpointConfig) {
        this.endpointConfig = endpointConfig;
    }

    public String getEndpointUuid() {
        return endpointUuid;
    }

    public void setEndpointUuid(String endpointUuid) {
        this.endpointUuid = endpointUuid;
    }

    public String getDeploymentStage() {
        return deploymentStage;
    }

    public void setDeploymentStage(String deploymentStage) {
        this.deploymentStage = deploymentStage;
    }
}
