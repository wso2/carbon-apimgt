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
import java.util.Set;
import java.util.UUID;

public class BackendEndpointData implements Serializable {

    private String backendId;
    private String backendDefinition;
    private String endpointConfig;
    private String backendName;
    private Set<BackendOperation> backendOperation;

    public BackendEndpointData(String backendId, String backendName) {

        if (backendId == null || backendId.isEmpty()) {
            this.backendId = UUID.randomUUID().toString();
        } else {
            this.backendId = backendId;
        }
        this.backendName = backendName;
    }

    public String getBackendId() {

        return backendId;
    }

    public void setBackendId(String backendId) {

        this.backendId = backendId;
    }

    public String getBackendDefinition() {

        return backendDefinition;
    }

    public void setBackendDefinition(String backendDefinition) {

        this.backendDefinition = backendDefinition;
    }

    public String getEndpointConfig() {

        return endpointConfig;
    }

    public void setEndpointConfig(String endpointConfig) {

        this.endpointConfig = endpointConfig;

    }

    public String getBackendName() {

        return backendName;
    }

    public void setBackendName(String backendName) {

        this.backendName = backendName;
    }

    public Set<BackendOperation> getBackendOperation() {

        return backendOperation;
    }

    public void setBackendOperation(Set<BackendOperation> backendOperation) {

        this.backendOperation = backendOperation;
    }
}
