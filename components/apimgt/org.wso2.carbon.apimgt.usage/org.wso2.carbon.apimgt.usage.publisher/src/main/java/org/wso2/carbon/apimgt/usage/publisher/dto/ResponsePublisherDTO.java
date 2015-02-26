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

public class ResponsePublisherDTO extends PublisherDTO {

    private int response = 1;

    private long responseTime;
    private boolean cacheHit;
    private long serviceTime;
    private long backendTime;
    private long eventTime;
    private long responseSize;

    public int getResponse(){
        return response;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public long getEventTime() {
        return eventTime;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setServiceTime(long serviceTime) {
        this.serviceTime = serviceTime;
    }

    public long getServiceTime() {
        return serviceTime;
    }

    public void setBackendTime(long backendTime) {
        this.backendTime= backendTime;
    }

    public long getBackendTime() {
        return backendTime;
    }
    public void setResponseSize(long size) {
        this.responseSize= size;
    }

    public long getResponseSize() {
        return responseSize;
    }

    public void setCacheHit(boolean cacheHit) {
        this.cacheHit= cacheHit;
    }

    public boolean getCacheHit() {
        return cacheHit;
    }

}
