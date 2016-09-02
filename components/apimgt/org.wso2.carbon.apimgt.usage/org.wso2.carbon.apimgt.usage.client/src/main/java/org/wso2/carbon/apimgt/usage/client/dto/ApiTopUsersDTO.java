/*
 *
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.usage.client.dto;


public class ApiTopUsersDTO {
    private String apiName;
    private String version;
    private String user;
    private long requestCount;
    private long totalRequestCount;
    private String fromDate;
    private String toDate;

    public String getApiName() {
        return apiName;
    }

    public String getVersion() {
        return version;
    }

    public long getRequestCount() {
        return requestCount;
    }

    public long getTotalRequestCount() {
        return totalRequestCount;
    }

    public String getUser() {
        return user;
    }

    public String getFromDate() {
        return fromDate;
    }
    
    public String getToDate() {
        return toDate;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public void setTotalRequestCount(long totalRequestCount) {
        this.totalRequestCount = totalRequestCount;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setRequestCount(long requestCount) {
        this.requestCount = requestCount;
    }

}
