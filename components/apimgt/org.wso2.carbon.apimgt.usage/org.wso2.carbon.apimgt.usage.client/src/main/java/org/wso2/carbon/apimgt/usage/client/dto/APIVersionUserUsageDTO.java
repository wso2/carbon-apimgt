/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.usage.client.dto;

public class APIVersionUserUsageDTO {

    private String apiname;
    private String context;
    private String version;
    private long count;
    private String cost;
    private String costPerAPI;

    public long getCount() {
        return count;
    }
    public String getCost() {
        return cost;
    }public String getCostPerAPI() {
        return costPerAPI;
    }

    public String getVersion() {
        return version;
    }

    public String getApiname() {
        return apiname;
    }

    public String getContext() {
        return context;
    }

    public void setCount(long count) {
        this.count = count;
    }
    public void setCost(String cost) {
        this.cost = cost;
    } public void setCostPerAPI(String costPerAPI) {
        this.costPerAPI = costPerAPI;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setApiname(String apiName) {
        this.apiname = apiName;
    }

    public void setContext(String context) {
        this.context = context;
    }

}