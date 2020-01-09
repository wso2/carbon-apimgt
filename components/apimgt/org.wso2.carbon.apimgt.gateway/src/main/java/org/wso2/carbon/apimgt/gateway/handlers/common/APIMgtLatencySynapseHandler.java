/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.common;

import org.apache.synapse.AbstractSynapseHandler;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.tracing.TracingSpan;
import org.wso2.carbon.apimgt.tracing.TracingTracer;
import org.wso2.carbon.apimgt.tracing.Util;

import java.util.HashMap;
import java.util.Map;

public class APIMgtLatencySynapseHandler extends AbstractSynapseHandler {

    private TracingTracer tracer;

    @Override
    public boolean handleRequestInFlow(MessageContext messageContext) {

        if (Util.tracingEnabled()) {
            if (tracer == null) {
                synchronized (this) {
                    if (tracer == null) {
                        tracer = ServiceReferenceHolder.getInstance().getTracingService()
                                .buildTracer(APIMgtGatewayConstants.SERVICE_NAME);
                    }
                }
            }
            org.apache.axis2.context.MessageContext axis2MessageContext =
                    ((Axis2MessageContext) messageContext).getAxis2MessageContext();
            Map headersMap =
                    (Map) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
            TracingSpan spanContext = Util.extract(tracer, headersMap);
            TracingSpan responseLatencySpan =
                    Util.startSpan(APIMgtGatewayConstants.RESPONSE_LATENCY, spanContext, tracer);
            Util.setTag(responseLatencySpan, APIMgtGatewayConstants.SPAN_KIND, APIMgtGatewayConstants.SERVER);
            messageContext.setProperty(APIMgtGatewayConstants.RESPONSE_LATENCY, responseLatencySpan);
        }
        return true;
    }

    @Override
    public boolean handleRequestOutFlow(MessageContext messageContext) {
        Map<String, String> tracerSpecificCarrier = new HashMap<>();
        if (Util.tracingEnabled()) {
            TracingSpan parentSpan = (TracingSpan) messageContext.getProperty(APIMgtGatewayConstants.RESPONSE_LATENCY);
            TracingSpan backendLatencySpan =
                    Util.startSpan(APIMgtGatewayConstants.BACKEND_LATENCY_SPAN, parentSpan, tracer);
            messageContext.setProperty(APIMgtGatewayConstants.BACKEND_LATENCY_SPAN, backendLatencySpan);
            Util.inject(backendLatencySpan, tracer, tracerSpecificCarrier);
            if (org.apache.axis2.context.MessageContext.getCurrentMessageContext() != null) {
                Map headers = (Map) org.apache.axis2.context.MessageContext.getCurrentMessageContext().getProperty(
                        org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
                headers.putAll(tracerSpecificCarrier);
                org.apache.axis2.context.MessageContext.getCurrentMessageContext()
                        .setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);
            }
        }
        return true;
    }

    @Override
    public boolean handleResponseInFlow(MessageContext messageContext) {
        if (Util.tracingEnabled()) {
            TracingSpan backendLatencySpan =
                    (TracingSpan) messageContext.getProperty(APIMgtGatewayConstants.BACKEND_LATENCY_SPAN);
            Util.finishSpan(backendLatencySpan);
        }
        return true;
    }

    @Override
    public boolean handleResponseOutFlow(MessageContext messageContext) {
        if (Util.tracingEnabled()) {
            TracingSpan responseLatencySpan =
                    (TracingSpan) messageContext.getProperty(APIMgtGatewayConstants.RESPONSE_LATENCY);
            Util.finishSpan(responseLatencySpan);
        }
        return true;
    }
}
