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

package org.wso2.carbon.apimgt.gateway.handlers.streaming;

import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.wso2.carbon.apimgt.common.gateway.analytics.publishers.dto.Latencies;
import org.wso2.carbon.apimgt.common.gateway.analytics.publishers.dto.Operation;
import org.wso2.carbon.apimgt.common.gateway.analytics.publishers.dto.Target;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.Constants;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.SynapseAnalyticsDataProvider;
import org.wso2.carbon.apimgt.impl.APIConstants;

public class AsyncAnalyticsDataProvider extends SynapseAnalyticsDataProvider {

    private MessageContext messageContext;
    String apiType;

    public AsyncAnalyticsDataProvider(MessageContext messageContext) {
        super(messageContext);
        org.apache.axis2.context.MessageContext axisCtx = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        apiType = (String) axisCtx.getProperty(PassThroughConstants.SYNAPSE_ARTIFACT_TYPE);
        this.messageContext = messageContext;
    }

    @Override
    public Latencies getLatencies() {
        Latencies latencies = new Latencies();
        latencies.setResponseLatency(0L);
        latencies.setBackendLatency(0L);
        latencies.setRequestMediationLatency(0L);
        latencies.setResponseMediationLatency(0L);
        return latencies;
    }

    @Override
    public Operation getOperation() {

        Operation operation = super.getOperation();
        String eventName = ""; // // todo get this from smg ctx
        if (!eventName.isEmpty()) {
            operation.setApiResourceTemplate(eventName + operation.getApiResourceTemplate());
        }
        return operation;
    }

    @Override
    public int getTargetResponseCode() {
        if (APIConstants.API_TYPE_WEBSUB.equalsIgnoreCase(apiType)) {
            return -1;
        }
        return super.getTargetResponseCode();
    }

    @Override
    public Target getTarget() {
        Target target = super.getTarget();
        if (APIConstants.API_TYPE_WEBSUB.equalsIgnoreCase(apiType)) {
            target.setDestination(Constants.UNKNOWN_VALUE);
        }
        return target;
    }

}