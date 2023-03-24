/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.impl.dto;

public class APILogInfoDTO {
    private String apiId;
    private String context;
    private String logLevel;
    private String resourceMethod;
    private String resourcePath;

    public APILogInfoDTO(String apiId, String context, String logLevel) {
        this.apiId = apiId;
        this.context = context;
        this.logLevel = logLevel;
    }

    public APILogInfoDTO(String apiId, String context, String logLevel, String resourceMethod, String resourcePath) {
        this.apiId = apiId;
        this.context = context;
        this.logLevel = logLevel;
        this.resourceMethod = resourceMethod;
        this.resourcePath = resourcePath;
    }

    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getResourcePath() { return resourcePath;}

    public void setResourcePath(String resourcePath) { this.resourcePath = resourcePath;}

    public String getResourceMethod() { return resourceMethod;}

    public void setResourceMethod(String resourceMethod) { this.resourceMethod = resourceMethod;}
}
