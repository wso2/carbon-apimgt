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

import java.util.Map;
import java.util.UUID;

public class APIMgtLatencySynapseHandler extends AbstractSynapseHandler {

    private static final Log log = LogFactory.getLog(APIMgtLatencySynapseHandler.class);
    private static TracingTracer tracer;

    public APIMgtLatencySynapseHandler() {

        tracer = ServiceReferenceHolder.getInstance().getTracingService().buildTracer(APIMgtGatewayConstants.SERVICE_NAME);
    }

    @Override
    public boolean handleRequestInFlow(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
//        Map headersMap;
        Map headersMap = (Map) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        TracingSpan spanContext = Util.extract(tracer, headersMap);
//        if (axis2MessageContext != null) {
//            Object headers = axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
//            if (headers instanceof Map) {
//                headersMap = (Map) headers;
//                spanContext = Util.extract(tracer, headersMap);
//            }
//        }
        String requestId = UUID.randomUUID().toString();
        messageContext.setProperty(APIMgtGatewayConstants.REQUEST_ID, requestId);
        TracingSpan responseLatencySpan = Util.startSpan(APIMgtGatewayConstants.RESPONSE_LATENCY, spanContext, tracer);
        Util.setTag(responseLatencySpan, APIMgtGatewayConstants.REQUEST_ID, requestId);
        messageContext.setProperty(APIMgtGatewayConstants.RESPONSE_LATENCY, responseLatencySpan);
        return true;
    }

    @Override
    public boolean handleRequestOutFlow(MessageContext messageContext) {

        TracingSpan parentSpan = (TracingSpan) messageContext.getProperty(APIMgtGatewayConstants.RESPONSE_LATENCY);
        TracingSpan backendLatencySpan = Util.startSpan(APIMgtGatewayConstants.BACKEND_LATENCY_SPAN, parentSpan, tracer);
        messageContext.setProperty(APIMgtGatewayConstants.BACKEND_LATENCY_SPAN, backendLatencySpan);
        Util.setTag(backendLatencySpan, APIMgtGatewayConstants.REQUEST_ID,
                (String) messageContext.getProperty(APIMgtGatewayConstants.REQUEST_ID));
        return true;
    }

    @Override
    public boolean handleResponseInFlow(MessageContext messageContext) {

        TracingSpan backendLatencySpan =
                (TracingSpan) messageContext.getProperty(APIMgtGatewayConstants.BACKEND_LATENCY_SPAN);
        Util.finishSpan(backendLatencySpan);
        return true;
    }

    @Override
    public boolean handleResponseOutFlow(MessageContext messageContext) {

        TracingSpan responseLatencySpan =
                (TracingSpan) messageContext.getProperty(APIMgtGatewayConstants.RESPONSE_LATENCY);
        Util.finishSpan(responseLatencySpan);
        return true;
    }

}
