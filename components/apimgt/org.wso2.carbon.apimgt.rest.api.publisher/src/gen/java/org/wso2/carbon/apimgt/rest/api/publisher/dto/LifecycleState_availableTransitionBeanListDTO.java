package org.wso2.carbon.apimgt.rest.api.publisher.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * LifecycleState_availableTransitionBeanListDTO
 */
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-03-19T18:14:01.803+05:30")
public class LifecycleState_availableTransitionBeanListDTO   {
  @JsonProperty("event")
  private String event = null;

  @JsonProperty("targetState")
  private String targetState = null;

  public LifecycleState_availableTransitionBeanListDTO event(String event) {
    this.event = event;
    return this;
  }

   /**
   * Get event
   * @return event
  **/
  @ApiModelProperty(example = "Promote", value = "")
  public String getEvent() {
    return event;
  }

  public void setEvent(String event) {
    this.event = event;
  }

  public LifecycleState_availableTransitionBeanListDTO targetState(String targetState) {
    this.targetState = targetState;
    return this;
  }

   /**
   * Get targetState
   * @return targetState
  **/
  @ApiModelProperty(example = "Created", value = "")
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
    LifecycleState_availableTransitionBeanListDTO lifecycleStateAvailableTransitionBeanList = (LifecycleState_availableTransitionBeanListDTO) o;
    return Objects.equals(this.event, lifecycleStateAvailableTransitionBeanList.event) &&
        Objects.equals(this.targetState, lifecycleStateAvailableTransitionBeanList.targetState);
  }

  @Override
  public int hashCode() {
    return Objects.hash(event, targetState);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LifecycleState_availableTransitionBeanListDTO {\n");
    
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

