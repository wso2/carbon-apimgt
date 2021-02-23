/*
 * Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.common.gateway.dto;

/**
 * Representation of Invoked GW API Request Information of Request/Response.
 */
public class APIRequestInfoDTO {

    String context;
    String version;
    String username;
    String consumerKey;
    String apiId;

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

    public String getVersion() {

        return version;
    }

    public void setVersion(String version) {

        this.version = version;
    }

    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {

        this.username = username;
    }

    public String getConsumerKey() {

        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {

        this.consumerKey = consumerKey;
    }
}

