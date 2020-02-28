package org.wso2.carbon.apimgt.impl.mediation.tracing;

import java.util.Map;

public class MediatorEvent {

    private String componentName;
    private String componentType;
    private String componentId;
    private Map<String, Object> transportHeaders;
    private Map<String, Object> synapseCtxProperties;
    private Map<String, Object> axis2CtxProperties;
    private String payload;

    private Map<String, Object> addedTransportHeaders;
    private Map<String, Object> addedSynapseCtxProperties;
    private Map<String, Object> addedAxis2CtxProperties;

    private Map<String, Object> removedTransportHeaders;
    private Map<String, Object> removedSynapseCtxProperties;
    private Map<String, Object> removedAxis2CtxProperties;

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

    public Map<String, Object> getSynapseCtxProperties() {
        return this.synapseCtxProperties;
    }

    public void setSynapseCtxProperties(Map<String, Object> synapseCtxProperties) {
        this.synapseCtxProperties = synapseCtxProperties;
    }

    public Map<String, Object> getAXIS2MessageContext() {
        return this.axis2CtxProperties;
    }

    public void setAxis2CtxProperties(Map<String, Object> axis2CtxProperties) {
        this.axis2CtxProperties = axis2CtxProperties;
    }

    public String getPayload() {
        return this.payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Map<String, Object> getAddedTransportHeaders() {
        return addedTransportHeaders;
    }

    public void setAddedTransportHeaders(Map<String, Object> addedTransportHeaders) {
        this.addedTransportHeaders = addedTransportHeaders;
    }

    public Map<String, Object> getAddedSynapseCtxProperties() {
        return addedSynapseCtxProperties;
    }

    public void setAddedSynapseCtxProperties(Map<String, Object> addedSynapseCtxProperties) {
        this.addedSynapseCtxProperties = addedSynapseCtxProperties;
    }

    public Map<String, Object> getAddedAxis2CtxProperties() {
        return addedAxis2CtxProperties;
    }

    public void setAddedAxis2CtxProperties(Map<String, Object> addedAxis2CtxProperties) {
        this.addedAxis2CtxProperties = addedAxis2CtxProperties;
    }

    public Map<String, Object> getRemovedTransportHeaders() {
        return removedTransportHeaders;
    }

    public void setRemovedTransportHeaders(Map<String, Object> removedTransportHeaders) {
        this.removedTransportHeaders = removedTransportHeaders;
    }

    public Map<String, Object> getRemovedSynapseCtxProperties() {
        return removedSynapseCtxProperties;
    }

    public void setRemovedSynapseCtxProperties(Map<String, Object> removedSynapseCtxProperties) {
        this.removedSynapseCtxProperties = removedSynapseCtxProperties;
    }

    public Map<String, Object> getRemovedAxis2CtxProperties() {
        return removedAxis2CtxProperties;
    }

    public void setRemovedAxis2CtxProperties(Map<String, Object> removedAxis2CtxProperties) {
        this.removedAxis2CtxProperties = removedAxis2CtxProperties;
    }
}
