package org.wso2.carbon.apimgt.core.template.dto;
/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 * data and config holder dto to publish as json
 */
public class GatewayConfigDTO {
    private String apiName;
    private String type;
    private String context;
    private String version;
    private String creator;
    private String config;

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getApiName() {
        return apiName;
    }

    public String getContext() {
        return context;
    }

    public String getVersion() {
        return version;
    }

    public String getCreator() {
        return creator;
    }

    public String getConfig() {
        return config;
    }
}
