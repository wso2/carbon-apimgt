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

package org.wso2.carbon.apimgt.gateway.handlers.streaming.sse;

import org.apache.synapse.MessageContext;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.Constants;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.SynapseAnalyticsDataProvider;

public class SseEventDataProvider extends SynapseAnalyticsDataProvider {

    private MessageContext messageContext;
    private int responseCode;

    public SseEventDataProvider(MessageContext messageContext) {
        super(messageContext);
        this.messageContext = messageContext;
    }

    @Override
    public long getResponseMediationLatency() {
        return 0L; // always 0 since there is no response mediation
    }

    @Override
    public int getTargetResponseCode() {
        return responseCode;
    }

    @Override
    public int getProxyResponseCode() {
        return responseCode;
    }

    public void setBackendEndTime() {
        messageContext.setProperty(Constants.BACKEND_END_TIME_PROPERTY, System.currentTimeMillis());
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }
}
