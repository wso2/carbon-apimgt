/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.api.model.subscription;

public class URLMapping {

    private int id;
    private int apiId;
    private String throttlingPolicy;
    private String authScheme;

    public String getHttpMethod() {

        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {

        this.httpMethod = httpMethod;
    }

    private String httpMethod;

    public int getId() {

        return id;
    }

    public void setId(int id) {

        this.id = id;
    }

    public int getApiId() {

        return apiId;
    }

    public void setApiId(int apiId) {

        this.apiId = apiId;
    }

    public String getThrottlingPolicy() {

        return throttlingPolicy;
    }

    public void setThrottlingPolicy(String throttlingPolicy) {

        this.throttlingPolicy = throttlingPolicy;
    }

    public String getAuthScheme() {

        return authScheme;
    }

    public void setAuthScheme(String authScheme) {

        this.authScheme = authScheme;
    }
}
