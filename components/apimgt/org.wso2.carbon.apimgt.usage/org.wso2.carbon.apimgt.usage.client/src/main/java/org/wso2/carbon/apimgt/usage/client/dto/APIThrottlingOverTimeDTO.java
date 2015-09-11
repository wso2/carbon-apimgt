/*
*  Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public class APIThrottlingOverTimeDTO {
    
    private String apiName;
    private String apiPublisher;
    private int successRequestCount;
    private int throttleOutCount;
    private String time;

    public APIThrottlingOverTimeDTO(String apiName, String apiPublisher, int successRequestCount, int throttleOutCount,
            String time) {
        this.apiName = apiName;
        this.apiPublisher = apiPublisher;
        this.successRequestCount = successRequestCount;
        this.throttleOutCount = throttleOutCount;
        this.time = time;
    }

    public void setAPIName(String apiName) {
        this.apiName = apiName;
    }

    public void setAPIPublisher(String apiPublisher) {
        this.apiPublisher = apiPublisher;
    }

    public void setSuccessRequestCount(int successRequestCount) {
        this.successRequestCount = successRequestCount;
    }

    public void setThrottleOutCount(int throttleOutCount) {
        this.throttleOutCount = throttleOutCount;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAPIName() {
        return apiName;
    }

    public String getAPIPublisher() {
        return apiPublisher;
    }

    public int getThrottleOutCount() {
        return throttleOutCount;
    }

    public int getSuccessRequestCount() {
        return successRequestCount;
    }

    public String getTime() {
        return time;
    }
    
}
