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
package org.wso2.carbon.apimgt.usage.client.dto;

import java.util.ArrayList;
import java.util.List;

public class PerAppApiCountDTO {

    private String appName;
    private List<ApiCountArray> apiCountArray=new ArrayList<ApiCountArray>();

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public List<ApiCountArray> getApiCountArray() {
        return apiCountArray;
    }

    public void addToApiCountArray(String apiName,long count) {
        ApiCountArray counts=new ApiCountArray();
        counts.setApiName(apiName);
        counts.setCount(count);
        this.apiCountArray.add(counts);
    }
}

class ApiCountArray{
    String apiName;
    long count;

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
