/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.gateway.dto;

/**
 * DTO class which holds information on throttle response of websocket frames.
 */
public class WebSocketThrottleResponseDTO implements InboundProcessorResponseError {

    private boolean isThrottled;
    private String throttledOutReason;
    private String apiContext;
    private String user;

    public WebSocketThrottleResponseDTO() {
    }

    public WebSocketThrottleResponseDTO(boolean throttled, String throttledOutReason) {
        isThrottled = throttled;
        this.throttledOutReason = throttledOutReason;
    }

    public WebSocketThrottleResponseDTO(boolean throttled, String throttledOutReason, String apiContext, String user) {
        isThrottled = throttled;
        this.throttledOutReason = throttledOutReason;
        this.apiContext = apiContext;
        this.user = user;
    }

    public boolean isThrottled() {
        return isThrottled;
    }

    public void setThrottled(boolean throttled) {
        isThrottled = throttled;
    }

    public String getThrottledOutReason() {
        return throttledOutReason;
    }

    public void setThrottledOutReason(String throttledOutReason) {
        this.throttledOutReason = throttledOutReason;
    }

    public String getApiContext() {
        return apiContext;
    }

    public void setApiContext(String apiContext) {
        this.apiContext = apiContext;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
