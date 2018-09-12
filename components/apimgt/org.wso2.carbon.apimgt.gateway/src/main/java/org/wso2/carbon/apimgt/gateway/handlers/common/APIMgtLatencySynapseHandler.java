/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.gateway.handlers.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.AbstractSynapseHandler;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.tracing.TracingSpan;
import org.wso2.carbon.apimgt.tracing.TracingTracer;
import org.wso2.carbon.apimgt.tracing.Util;

import java.util.UUID;

/**
 * Class which used to trace Overall latency and backend latency
 */

public class APIMgtLatencySynapseHandler extends AbstractSynapseHandler {

    private static final Log log = LogFactory.getLog(APIMgtLatencySynapseHandler.class);

    private TracingTracer tracer;

    public APIMgtLatencySynapseHandler() {

        tracer = ServiceReferenceHolder.getInstance().getTracingService().buildTracer("Latency");
    }

    @Override
    public boolean handleRequestInFlow(MessageContext messageContext) {

        String requestId = UUID.randomUUID().toString();
        messageContext.setProperty(APIMgtGatewayConstants.REQUEST_ID, requestId);

        TracingSpan responseLatencySpan = Util.startSpan("API:Response_Latency", null, tracer, null);
        Util.setTag(responseLatencySpan, APIMgtGatewayConstants.REQUEST_ID, requestId);
        Util.setLog(responseLatencySpan, "API:ResponseLatency", "responseLatency");
        messageContext.setProperty(APIMgtGatewayConstants.RESPONSE_LATENCY_SPAN, responseLatencySpan);
        messageContext.setProperty(APIMgtGatewayConstants.TRACER, tracer);

        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        axis2MC.setProperty(APIMgtGatewayConstants.RESPONSE_LATENCY_SPAN, responseLatencySpan);
        return true;
    }

    @Override
    public boolean handleRequestOutFlow(MessageContext messageContext) {

        TracingSpan parentSpan = (TracingSpan) messageContext.getProperty(APIMgtGatewayConstants.RESPONSE_LATENCY_SPAN);
        TracingSpan backendLatencySpan = Util.startSpan("BackendLatency", parentSpan, tracer, null);
        messageContext.setProperty(APIMgtGatewayConstants.BACKEND_LATENCY_SPAN, backendLatencySpan);
        return true;
    }

    @Override
    public boolean handleResponseInFlow(MessageContext messageContext) {

        TracingSpan backendLatencySpan = (TracingSpan) messageContext.getProperty(APIMgtGatewayConstants.BACKEND_LATENCY_SPAN);
        Util.finishSpan(backendLatencySpan);
        return true;
    }

    @Override
    public boolean handleResponseOutFlow(MessageContext messageContext) {

        TracingSpan responseLatencySpan = (TracingSpan) messageContext.getProperty(APIMgtGatewayConstants.RESPONSE_LATENCY_SPAN);
        Util.finishSpan(responseLatencySpan);
        return true;
    }

}
