/*
 * Copyright (c) 2025, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.api.model;

/**
 * Represents a mapping between a backend API and its operations.
 * This class encapsulates the backend API ID and the corresponding backend operation.
 */
public class BackendAPIOperationMapping {

    private String backendApiId = null;
    private BackendOperation backendOperation = null;

    public String getBackendApiId() {

        return backendApiId;
    }

    public void setBackendApiId(String backendApiId) {

        this.backendApiId = backendApiId;
    }

    public BackendOperation getBackendOperation() {

        return backendOperation;
    }

    public void setBackendOperation(BackendOperation backendOperation) {

        this.backendOperation = backendOperation;
    }

    @Override
    public String toString() {
        return "BackendOperationMapping {" +
                "backendId='" + backendApiId + '\'' +
                ", backendOperation=" + backendOperation +
                '}';
    }
}
