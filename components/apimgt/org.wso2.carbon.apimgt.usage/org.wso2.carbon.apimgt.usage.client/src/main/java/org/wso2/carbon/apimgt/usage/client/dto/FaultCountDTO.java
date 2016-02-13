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

/**
 * This class is used as a DTO for represent API fault count usage
 */
public class FaultCountDTO {
    private String appName;
    private List<ApiFaultCountArray> apiCountArray = new ArrayList<ApiFaultCountArray>();

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public List<ApiFaultCountArray> getApiCountArray() {
        return apiCountArray;
    }

    public void addToApiFaultCountArray(String apiName, long count) {
        ApiFaultCountArray usage = new ApiFaultCountArray();
        usage.setApiName(apiName);
        usage.setCount(count);
        this.apiCountArray.add(usage);
    }
}

/**
 * This class is used to represent API and it's count
 */
class ApiFaultCountArray {
    private String apiName;
    private long count;

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
