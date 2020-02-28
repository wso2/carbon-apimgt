package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class MessageTraceEventDTO   {
  
    private String componentType = null;
    private String componentName = null;
    private Map<String, String> transportHeaders = new HashMap<>();
    private Map<String, String> synapseMessagecontext = new HashMap<>();
    private Map<String, String> axis2MessageContext = new HashMap<>();
    private Map<String, String> payload = new HashMap<>();

  /**
   **/
  public MessageTraceEventDTO componentType(String componentType) {
    this.componentType = componentType;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("componentType")
  public String getComponentType() {
    return componentType;
  }
  public void setComponentType(String componentType) {
    this.componentType = componentType;
  }

  /**
   **/
  public MessageTraceEventDTO componentName(String componentName) {
    this.componentName = componentName;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("componentName")
  public String getComponentName() {
    return componentName;
  }
  public void setComponentName(String componentName) {
    this.componentName = componentName;
  }

  /**
   **/
  public MessageTraceEventDTO transportHeaders(Map<String, String> transportHeaders) {
    this.transportHeaders = transportHeaders;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("transportHeaders")
  public Map<String, String> getTransportHeaders() {
    return transportHeaders;
  }
  public void setTransportHeaders(Map<String, String> transportHeaders) {
    this.transportHeaders = transportHeaders;
  }

  /**
   **/
  public MessageTraceEventDTO synapseMessagecontext(Map<String, String> synapseMessagecontext) {
    this.synapseMessagecontext = synapseMessagecontext;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("synapseMessagecontext")
  public Map<String, String> getSynapseMessagecontext() {
    return synapseMessagecontext;
  }
  public void setSynapseMessagecontext(Map<String, String> synapseMessagecontext) {
    this.synapseMessagecontext = synapseMessagecontext;
  }

  /**
   **/
  public MessageTraceEventDTO axis2MessageContext(Map<String, String> axis2MessageContext) {
    this.axis2MessageContext = axis2MessageContext;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("axis2MessageContext")
  public Map<String, String> getAxis2MessageContext() {
    return axis2MessageContext;
  }
  public void setAxis2MessageContext(Map<String, String> axis2MessageContext) {
    this.axis2MessageContext = axis2MessageContext;
  }

  /**
   **/
  public MessageTraceEventDTO payload(Map<String, String> payload) {
    this.payload = payload;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("payload")
  public Map<String, String> getPayload() {
    return payload;
  }
  public void setPayload(Map<String, String> payload) {
    this.payload = payload;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MessageTraceEventDTO messageTraceEvent = (MessageTraceEventDTO) o;
    return Objects.equals(componentType, messageTraceEvent.componentType) &&
        Objects.equals(componentName, messageTraceEvent.componentName) &&
        Objects.equals(transportHeaders, messageTraceEvent.transportHeaders) &&
        Objects.equals(synapseMessagecontext, messageTraceEvent.synapseMessagecontext) &&
        Objects.equals(axis2MessageContext, messageTraceEvent.axis2MessageContext) &&
        Objects.equals(payload, messageTraceEvent.payload);
  }

  @Override
  public int hashCode() {
    return Objects.hash(componentType, componentName, transportHeaders, synapseMessagecontext, axis2MessageContext, payload);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MessageTraceEventDTO {\n");
    
    sb.append("    componentType: ").append(toIndentedString(componentType)).append("\n");
    sb.append("    componentName: ").append(toIndentedString(componentName)).append("\n");
    sb.append("    transportHeaders: ").append(toIndentedString(transportHeaders)).append("\n");
    sb.append("    synapseMessagecontext: ").append(toIndentedString(synapseMessagecontext)).append("\n");
    sb.append("    axis2MessageContext: ").append(toIndentedString(axis2MessageContext)).append("\n");
    sb.append("    payload: ").append(toIndentedString(payload)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

