package org.wso2.carbon.apimgt.gateway.handlers.common;

import io.opentracing.Span;
import io.opentracing.Tracer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.AbstractSynapseHandler;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.tracing.OpenTracer;
import org.wso2.carbon.apimgt.tracing.TracingService;
import java.util.UUID;

public class APIMgtLatencySynapseHandler extends AbstractSynapseHandler {

    private static final Log log = LogFactory.getLog(APIMgtLatencySynapseHandler.class);

    private long handleRequestInFlowTime;
    private long handleRequestOutFlowTime;
    private long handleResponseInFlowTime;
    private long handleResponseOutFlowTime;
    private String requestId;
    private TracingService tracingService;
    private Tracer tracer;


    public APIMgtLatencySynapseHandler() {
        ServiceReferenceHolder serviceReferenceHolder = ServiceReferenceHolder.getInstance();
        tracingService = serviceReferenceHolder.getTracingService();
        tracer = tracingService.buildTracer("Latency");
    }

    @Override
    public boolean handleRequestInFlow(MessageContext messageContext) {
        Span responseLatencySpan = OpenTracer.startSpan("ResponseLatency", null, tracer);
        messageContext.setProperty("ResponseLatency", responseLatencySpan);
        messageContext.setProperty("Tracer",tracer);

        handleRequestInFlowTime = System.currentTimeMillis();
        if (messageContext.getProperty(APIMgtGatewayConstants.HANDLE_REQUEST_INFLOW_TIME) == null) {
            messageContext.setProperty(APIMgtGatewayConstants.HANDLE_REQUEST_INFLOW_TIME, Long.toString(handleRequestInFlowTime));
        }

        requestId = UUID.randomUUID().toString();
        messageContext.setProperty(APIMgtGatewayConstants.REQUEST_ID, requestId);

        return true;
    }

    @Override
    public boolean handleRequestOutFlow(MessageContext messageContext) {

        Span parentSpan = (Span)messageContext.getProperty("ResponseLatency");
        Span backendLatencySpan = OpenTracer.startSpan("BackendLatency", parentSpan, tracer);
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

        Span backendLatencySpan = (Span) messageContext.getProperty("BackendLatency");
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

        Span responseLatencySpan = (Span) messageContext.getProperty("ResponseLatency");
        OpenTracer.finishSpan(responseLatencySpan);

        return true;
    }
}
