package org.wso2.carbon.apimgt.gateway.handlers.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.AbstractSynapseHandler;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.tracing.OpenTracer;
import org.wso2.carbon.apimgt.tracing.TracingSpan;
import org.wso2.carbon.apimgt.tracing.TracingTracer;

import java.util.UUID;

public class APIMgtLatencySynapseHandler extends AbstractSynapseHandler {

    private static final Log log = LogFactory.getLog(APIMgtLatencySynapseHandler.class);

    private long handleRequestInFlowTime;
    private long handleRequestOutFlowTime;
    private long handleResponseInFlowTime;
    private long handleResponseOutFlowTime;
    private TracingTracer tracer;


    public APIMgtLatencySynapseHandler() {
        tracer = ServiceReferenceHolder.getInstance().getTracingService().buildTracer("Latency");
    }

    @Override
    public boolean handleRequestInFlow(MessageContext messageContext) {

        String requestId = UUID.randomUUID().toString();
        messageContext.setProperty(APIMgtGatewayConstants.REQUEST_ID, requestId);

        TracingSpan responseLatencySpan = OpenTracer.startSpan("ResponseLatency", null, tracer);
        OpenTracer.setTag(responseLatencySpan,"RequestID", requestId);
        messageContext.setProperty("ResponseLatency", responseLatencySpan);
        messageContext.setProperty("Tracer",tracer);

        handleRequestInFlowTime = System.currentTimeMillis();
        if (messageContext.getProperty(APIMgtGatewayConstants.HANDLE_REQUEST_INFLOW_TIME) == null) {
            messageContext.setProperty(APIMgtGatewayConstants.HANDLE_REQUEST_INFLOW_TIME, Long.toString(handleRequestInFlowTime));
        }
        return true;
    }

    @Override
    public boolean handleRequestOutFlow(MessageContext messageContext) {

        TracingSpan parentSpan = (TracingSpan) messageContext.getProperty("ResponseLatency");
        TracingSpan backendLatencySpan = OpenTracer.startSpan("BackendLatency", parentSpan, tracer);
        OpenTracer.setTag(backendLatencySpan,"RequestID", String.valueOf(messageContext.getProperty(APIMgtGatewayConstants.REQUEST_ID)));
        messageContext.setProperty("BackendLatency", backendLatencySpan);

        handleRequestOutFlowTime = System.currentTimeMillis();
        if (messageContext.getProperty(APIMgtGatewayConstants.HANDLE_REQUEST_OUTFLOW_TIME) == null) {
            messageContext.setProperty(APIMgtGatewayConstants.HANDLE_REQUEST_OUTFLOW_TIME, Long.toString(handleRequestOutFlowTime));
        }
        return true;
    }

    @Override
    public boolean handleResponseInFlow(MessageContext messageContext) {

        handleResponseInFlowTime = System.currentTimeMillis();
        if (messageContext.getProperty(APIMgtGatewayConstants.HANDLE_RESPONSE_INFLOW_TIME) == null) {
            messageContext.setProperty(APIMgtGatewayConstants.HANDLE_RESPONSE_INFLOW_TIME, Long.toString(handleResponseInFlowTime));
        }

        TracingSpan backendLatencySpan = (TracingSpan) messageContext.getProperty("BackendLatency");
        OpenTracer.finishSpan(backendLatencySpan);
        return true;
    }

    @Override
    public boolean handleResponseOutFlow(MessageContext messageContext) {

        handleResponseOutFlowTime = System.currentTimeMillis();
        if (messageContext.getProperty(APIMgtGatewayConstants.HANDLE_RESPONSE_OUTFLOW_TIME) == null) {
            messageContext.setProperty(APIMgtGatewayConstants.HANDLE_RESPONSE_OUTFLOW_TIME, Long.toString(handleResponseOutFlowTime));
        }
        long backendLatency = handleResponseInFlowTime - handleRequestOutFlowTime;
        long responseLatency = handleResponseOutFlowTime - handleRequestInFlowTime;

        messageContext.setProperty(APIMgtGatewayConstants.SYNAPSE_BACKEND_LATENCY, Long.toString(backendLatency));
        messageContext.setProperty(APIMgtGatewayConstants.SYNAPSE_RESPONSE_LATENCY, Long.toString(responseLatency));

        log.info(messageContext.getProperty(APIMgtGatewayConstants.REQUEST_ID) + " Backend Latency : " + backendLatency + " - Response Latency : " + responseLatency);

        TracingSpan responseLatencySpan = (TracingSpan) messageContext.getProperty("ResponseLatency");
        OpenTracer.finishSpan(responseLatencySpan);

        return true;
    }

}
