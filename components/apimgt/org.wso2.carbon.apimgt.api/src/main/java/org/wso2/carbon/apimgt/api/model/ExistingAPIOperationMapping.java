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

import java.io.Serializable;

/**
 * Represents a mapping between an existing API and its backend operation.
 * This class encapsulates the API UUID, name, version, and the corresponding backend operation.
 */
public class ExistingAPIOperationMapping implements Serializable {

    private static final long serialVersionUID = 1L;
    private String apiUuid = null;
    private String apiName = null;
    private String apiVersion = null;
    private String apiContext = null;
    private BackendOperation backendOperation = null;

    public String getApiUuid() {

        return apiUuid;
    }

    public void setApiUuid(String apiUuid) {

        this.apiUuid = apiUuid;
    }

    public BackendOperation getBackendOperation() {

        return backendOperation;
    }

    public void setBackendOperation(BackendOperation backendOperation) {

        this.backendOperation = backendOperation;
    }

    public String getApiName() {

        return apiName;
    }

    public void setApiName(String apiName) {

        this.apiName = apiName;
    }

    public String getApiVersion() {

        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {

        this.apiVersion = apiVersion;
    }

    public String getApiContext() {
        return apiContext;
    }

    public void setApiContext(String apiContext) {
        this.apiContext = apiContext;
    }

    @Override
    public String toString() {
        return "ApiOperationMapping{" +
                "apiUuid='" + apiUuid + '\'' +
                ", apiName='" + apiName + '\'' +
                ", apiVersion='" + apiVersion + '\'' +
                ", apiContext='" + apiContext + '\'' +
                ", backendOperation=" + backendOperation +
                '}';
    }
}
