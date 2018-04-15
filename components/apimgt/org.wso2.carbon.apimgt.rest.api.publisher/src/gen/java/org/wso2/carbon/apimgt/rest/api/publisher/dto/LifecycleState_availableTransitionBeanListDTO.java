package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * LifecycleState_availableTransitionBeanListDTO
 */
public class LifecycleState_availableTransitionBeanListDTO   {
  @SerializedName("event")
  private String event = null;

  @SerializedName("targetState")
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

