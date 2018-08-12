package org.wso2.carbon.apimgt.gateway.handlers.common;

import io.opentracing.Span;
import io.opentracing.Tracer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.AbstractSynapseHandler;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.tracing.TracingService;

import java.util.UUID;

public class APIMgtLatencySynapseHandler extends AbstractSynapseHandler {

    private Tracer tracer;

    public APIMgtLatencySynapseHandler() {
        ServiceReferenceHolder serviceReferenceHolder = ServiceReferenceHolder.getInstance();
        TracingService tracingService = serviceReferenceHolder.getTracingService();
        tracer = tracingService.getTracer("Latency");
    }

    private static final Log log = LogFactory.getLog(APIMgtLatencySynapseHandler.class);

    private long handleRequestInFlowTime;
    private long handleRequestOutFlowTime;
    private long handleResponseInFlowTime;
    private long handleResponseOutFlowTime;
    private String requestId;

    @Override
    public boolean handleRequestInFlow(MessageContext messageContext) {

        Span parentSpan = tracer.buildSpan("ResponseLatency").start();
        messageContext.setProperty("Tracer", tracer);
        messageContext.setProperty("ParentSpan", parentSpan);

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

        Span parentSpan = (Span) messageContext.getProperty("ParentSpan");
        Span childSpan = tracer.buildSpan("BackendLatency").asChildOf(parentSpan).start();
        childSpan.setTag("ChildSpan", "BackendLatency");
        messageContext.setProperty("ChildSpan", childSpan);

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
        Span childSpan = (Span) messageContext.getProperty("ChildSpan");
        childSpan.finish();
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

        Span parentSpan = (Span) messageContext.getProperty("ParentSpan");
        parentSpan.finish();
        return true;
    }
}
