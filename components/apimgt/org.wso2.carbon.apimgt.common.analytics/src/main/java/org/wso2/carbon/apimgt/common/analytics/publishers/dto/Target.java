/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.common.analytics.publishers.dto;

/**
 * Target attribute in analytics event.
 */
public class Target {
    private int targetResponseCode;
    private boolean responseCacheHit;
    private String destination;

    public int getTargetResponseCode() {
        return targetResponseCode;
    }

    public void setTargetResponseCode(int targetResponseCode) {
        this.targetResponseCode = targetResponseCode;
    }

    public boolean isResponseCacheHit() {
        return responseCacheHit;
    }

    public void setResponseCacheHit(boolean responseCacheHit) {
        this.responseCacheHit = responseCacheHit;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
