/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.gateway.handlers.streaming.sse.analytics;

import org.apache.synapse.MessageContext;

/**
 * Data provider for the response events of server sent events.
 */
public class SseResponseEventDataProvider extends SseEventDataProvider {

    private int responseCode;

    public SseResponseEventDataProvider(MessageContext messageContext) {
        super(messageContext);
    }

    @Override
    public int getTargetResponseCode() {
        return responseCode;
    }

    @Override
    public int getProxyResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }
}
