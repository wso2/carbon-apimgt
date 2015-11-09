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

package org.wso2.carbon.apimgt.usage.publisher.dto;

public class RequestPublisherDTO extends PublisherDTO {

    private int requestCount = 1;

    private long requestTime;
    private String userAgent;

    private String tier;
    private boolean continuedOnThrottleOut;

    public int getRequestCount(){
        return requestCount;
    }

    public void setRequestTime(long requestTime){
        this.requestTime = requestTime;
    }

    public long getRequestTime(){
        return requestTime;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getTier(){
        return tier;
    }

    public void setTier(String tier){
        this.tier=tier;
    }

    public boolean isContinuedOnThrottleOut() {
        return continuedOnThrottleOut;
    }

    public void setContinuedOnThrottleOut(boolean continuedOnThrottleOut) {
        this.continuedOnThrottleOut = continuedOnThrottleOut;
    }
}
