package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.EndPoint_endpointConfig_circuitBreaker_rollingWindowDTO;
import java.util.Objects;

/**
 * EndPoint_endpointConfig_circuitBreakerDTO
 */
public class EndPoint_endpointConfig_circuitBreakerDTO   {
  @SerializedName("rollingWindow")
  private EndPoint_endpointConfig_circuitBreaker_rollingWindowDTO rollingWindow = null;

  @SerializedName("failureThreshold")
  private String failureThreshold = null;

  @SerializedName("resetTime")
  private Integer resetTime = null;

  public EndPoint_endpointConfig_circuitBreakerDTO rollingWindow(EndPoint_endpointConfig_circuitBreaker_rollingWindowDTO rollingWindow) {
    this.rollingWindow = rollingWindow;
    return this;
  }

   /**
   * Get rollingWindow
   * @return rollingWindow
  **/
  @ApiModelProperty(value = "")
  public EndPoint_endpointConfig_circuitBreaker_rollingWindowDTO getRollingWindow() {
    return rollingWindow;
  }

  public void setRollingWindow(EndPoint_endpointConfig_circuitBreaker_rollingWindowDTO rollingWindow) {
    this.rollingWindow = rollingWindow;
  }

  public EndPoint_endpointConfig_circuitBreakerDTO failureThreshold(String failureThreshold) {
    this.failureThreshold = failureThreshold;
    return this;
  }

   /**
   * The threshold for request failures.When this threshold exceeds, the circuit trips.This is the ratio between failures and total requests and the ratio is considered only within the configured rolling window. 
   * @return failureThreshold
  **/
  @ApiModelProperty(example = "0.2", value = "The threshold for request failures.When this threshold exceeds, the circuit trips.This is the ratio between failures and total requests and the ratio is considered only within the configured rolling window. ")
  public String getFailureThreshold() {
    return failureThreshold;
  }

  public void setFailureThreshold(String failureThreshold) {
    this.failureThreshold = failureThreshold;
  }

  public EndPoint_endpointConfig_circuitBreakerDTO resetTime(Integer resetTime) {
    this.resetTime = resetTime;
    return this;
  }

   /**
   * The time period(in milliseconds) to wait before attempting to make another request to the upstream service 
   * @return resetTime
  **/
  @ApiModelProperty(example = "1000", value = "The time period(in milliseconds) to wait before attempting to make another request to the upstream service ")
  public Integer getResetTime() {
    return resetTime;
  }

  public void setResetTime(Integer resetTime) {
    this.resetTime = resetTime;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EndPoint_endpointConfig_circuitBreakerDTO endPointEndpointConfigCircuitBreaker = (EndPoint_endpointConfig_circuitBreakerDTO) o;
    return Objects.equals(this.rollingWindow, endPointEndpointConfigCircuitBreaker.rollingWindow) &&
        Objects.equals(this.failureThreshold, endPointEndpointConfigCircuitBreaker.failureThreshold) &&
        Objects.equals(this.resetTime, endPointEndpointConfigCircuitBreaker.resetTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(rollingWindow, failureThreshold, resetTime);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EndPoint_endpointConfig_circuitBreakerDTO {\n");
    
    sb.append("    rollingWindow: ").append(toIndentedString(rollingWindow)).append("\n");
    sb.append("    failureThreshold: ").append(toIndentedString(failureThreshold)).append("\n");
    sb.append("    resetTime: ").append(toIndentedString(resetTime)).append("\n");
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

