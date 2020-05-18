package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class ThrottledEventDTO   {
  
    private String throttleKey = null;
    private String lastUpdatedTime = null;
    private String throttleState = null;

  /**
   * throttle key.
   **/
  public ThrottledEventDTO throttleKey(String throttleKey) {
    this.throttleKey = throttleKey;
    return this;
  }

  
  @ApiModelProperty(value = "throttle key.")
  @JsonProperty("throttle_key")
  public String getThrottleKey() {
    return throttleKey;
  }
  public void setThrottleKey(String throttleKey) {
    this.throttleKey = throttleKey;
  }

  /**
   * Last time decision updated.
   **/
  public ThrottledEventDTO lastUpdatedTime(String lastUpdatedTime) {
    this.lastUpdatedTime = lastUpdatedTime;
    return this;
  }

  
  @ApiModelProperty(value = "Last time decision updated.")
  @JsonProperty("last_updated_time")
  public String getLastUpdatedTime() {
    return lastUpdatedTime;
  }
  public void setLastUpdatedTime(String lastUpdatedTime) {
    this.lastUpdatedTime = lastUpdatedTime;
  }

  /**
   * throttle state.
   **/
  public ThrottledEventDTO throttleState(String throttleState) {
    this.throttleState = throttleState;
    return this;
  }

  
  @ApiModelProperty(value = "throttle state.")
  @JsonProperty("throttle_state")
  public String getThrottleState() {
    return throttleState;
  }
  public void setThrottleState(String throttleState) {
    this.throttleState = throttleState;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ThrottledEventDTO throttledEvent = (ThrottledEventDTO) o;
    return Objects.equals(throttleKey, throttledEvent.throttleKey) &&
        Objects.equals(lastUpdatedTime, throttledEvent.lastUpdatedTime) &&
        Objects.equals(throttleState, throttledEvent.throttleState);
  }

  @Override
  public int hashCode() {
    return Objects.hash(throttleKey, lastUpdatedTime, throttleState);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ThrottledEventDTO {\n");
    
    sb.append("    throttleKey: ").append(toIndentedString(throttleKey)).append("\n");
    sb.append("    lastUpdatedTime: ").append(toIndentedString(lastUpdatedTime)).append("\n");
    sb.append("    throttleState: ").append(toIndentedString(throttleState)).append("\n");
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

