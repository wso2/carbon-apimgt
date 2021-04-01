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
import org.wso2.carbon.apimgt.common.analytics.exceptions.DataNotFoundException;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.Latencies;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.Operation;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.Target;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.Constants;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.SynapseAnalyticsDataProvider;
import org.wso2.carbon.apimgt.impl.APIConstants;

import static org.wso2.carbon.apimgt.impl.APIConstants.AsyncApi.ASYNC_MESSAGE_TYPE;

public class AsyncAnalyticsDataProvider extends SynapseAnalyticsDataProvider {

    private String apiType;
    private MessageContext messageContext;

    public AsyncAnalyticsDataProvider(MessageContext messageContext) {
        super(messageContext);
        org.apache.axis2.context.MessageContext axisCtx =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        apiType = (String) axisCtx.getProperty(PassThroughConstants.SYNAPSE_ARTIFACT_TYPE);
        this.messageContext = messageContext;
    }

    @Override
    public Latencies getLatencies() {
        return new Latencies();
    }

    @Override
    public Operation getOperation() throws DataNotFoundException {

        Operation operation = super.getOperation();
        Object eventPrefix = messageContext.getProperty(ASYNC_MESSAGE_TYPE);
        if (eventPrefix != null) {
            operation.setApiResourceTemplate(eventPrefix.toString() + operation.getApiResourceTemplate());
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
