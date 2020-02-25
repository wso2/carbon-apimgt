package org.wso2.carbon.apimgt.gateway.messagetracing;

import org.apache.synapse.aspects.ComponentType;
import org.apache.synapse.aspects.flow.statistics.data.raw.StatisticDataUnit;

import java.util.Map;

public class MediatorEvent {

    private String componentName;
    private String componentType;
    private String componentId;
    private Map<String, Object> transportHeaders;
    private Map<String, Object> synapseMessageContext;
    private Map<String, Object> axis2MessageContext;
    private String payload;

    public MediatorEvent() {
    }

    public String getComponentName() {
        return this.componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getComponentType() {
        return this.componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public String getComponentId() {
        return this.componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public Map<String, Object> getTransportHeaders() {
        return this.transportHeaders;
    }

    public void setTransportHeaders(Map<String, Object> transportHeaders) {
        this.transportHeaders = transportHeaders;
    }

    public Map<String, Object> getSynapseMessageContext() {
        return this.synapseMessageContext;
    }

    public void setSynapseMessageContext(Map<String, Object> synapseMessageContext) {
        this.synapseMessageContext = synapseMessageContext;
    }

    public Map<String, Object> getAXIS2MessageContext() {
        return this.axis2MessageContext;
    }

    public void setAxis2MessageContext(Map<String, Object> axis2MessageContext) {
        this.axis2MessageContext = axis2MessageContext;
    }

    public String getPayload() {
        return this.payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
