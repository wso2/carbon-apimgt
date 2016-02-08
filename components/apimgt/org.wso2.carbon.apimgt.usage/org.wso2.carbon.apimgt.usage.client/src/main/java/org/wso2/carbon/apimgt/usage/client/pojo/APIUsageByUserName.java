/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.carbon.apimgt.usage.client.pojo;

/**
 * This class is used as a pojo class to represent API usage by user
 */
public class APIUsageByUserName {
    private String apiName;
    private String apiVersion;
    private String context;
    private String userID;
    private String apipublisher;
    private long requestCount;

    public APIUsageByUserName(String apiName, String apiVersion, String context, String userID, String apipublisher,
            long requestCount) {
        this.apiName = apiName;
        this.apiVersion = apiVersion;
        this.context = context;
        this.userID = userID;
        this.apipublisher = apipublisher;
        this.requestCount = requestCount;
    }

    public APIUsageByUserName() {
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

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getApipublisher() {
        return apipublisher;
    }

    public void setApipublisher(String apipublisher) {
        this.apipublisher = apipublisher;
    }

    public long getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(long requestCount) {
        this.requestCount = requestCount;
    }

}
