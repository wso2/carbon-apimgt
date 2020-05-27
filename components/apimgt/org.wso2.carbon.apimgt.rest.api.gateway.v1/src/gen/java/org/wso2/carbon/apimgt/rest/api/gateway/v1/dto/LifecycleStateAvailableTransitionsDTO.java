package org.wso2.carbon.apimgt.rest.api.gateway.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class LifecycleStateAvailableTransitionsDTO   {
  
    private String event = null;
    private String targetState = null;

  /**
   **/
  public LifecycleStateAvailableTransitionsDTO event(String event) {
    this.event = event;
    return this;
  }

  
  @ApiModelProperty(example = "Promote", value = "")
  @JsonProperty("event")
  public String getEvent() {
    return event;
  }
  public void setEvent(String event) {
    this.event = event;
  }

  /**
   **/
  public LifecycleStateAvailableTransitionsDTO targetState(String targetState) {
    this.targetState = targetState;
    return this;
  }

  
  @ApiModelProperty(example = "Created", value = "")
  @JsonProperty("targetState")
  public String getTargetState() {
    return targetState;
  }
  public void setTargetState(String targetState) {
    this.targetState = targetState;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LifecycleStateAvailableTransitionsDTO lifecycleStateAvailableTransitions = (LifecycleStateAvailableTransitionsDTO) o;
    return Objects.equals(event, lifecycleStateAvailableTransitions.event) &&
        Objects.equals(targetState, lifecycleStateAvailableTransitions.targetState);
  }

  @Override
  public int hashCode() {
    return Objects.hash(event, targetState);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LifecycleStateAvailableTransitionsDTO {\n");
    
    sb.append("    event: ").append(toIndentedString(event)).append("\n");
    sb.append("    targetState: ").append(toIndentedString(targetState)).append("\n");
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

