package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;


import io.swagger.annotations.*;
import java.util.Objects;




public class ThrottleLimitDTO   {
  
    private String quotaType = null;
    private RequestCountLimitDTO requestCount = null;
    private BandwidthLimitDTO bandwidth = null;
    private EventCountLimitDTO eventCount = null;

  /**
   **/
  public ThrottleLimitDTO quotaType(String quotaType) {
    this.quotaType = quotaType;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("quotaType")
  public String getQuotaType() {
    return quotaType;
  }
  public void setQuotaType(String quotaType) {
    this.quotaType = quotaType;
  }

  /**
   **/
  public ThrottleLimitDTO requestCount(RequestCountLimitDTO requestCount) {
    this.requestCount = requestCount;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("requestCount")
  public RequestCountLimitDTO getRequestCount() {
    return requestCount;
  }
  public void setRequestCount(RequestCountLimitDTO requestCount) {
    this.requestCount = requestCount;
  }

  /**
   **/
  public ThrottleLimitDTO bandwidth(BandwidthLimitDTO bandwidth) {
    this.bandwidth = bandwidth;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("bandwidth")
  public BandwidthLimitDTO getBandwidth() {
    return bandwidth;
  }
  public void setBandwidth(BandwidthLimitDTO bandwidth) {
    this.bandwidth = bandwidth;
  }

  /**
   **/
  public ThrottleLimitDTO eventCount(EventCountLimitDTO eventCount) {
    this.eventCount = eventCount;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("eventCount")
  public EventCountLimitDTO getEventCount() {
    return eventCount;
  }
  public void setEventCount(EventCountLimitDTO eventCount) {
    this.eventCount = eventCount;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ThrottleLimitDTO throttleLimit = (ThrottleLimitDTO) o;
    return Objects.equals(quotaType, throttleLimit.quotaType) &&
        Objects.equals(requestCount, throttleLimit.requestCount) &&
        Objects.equals(bandwidth, throttleLimit.bandwidth) &&
        Objects.equals(eventCount, throttleLimit.eventCount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(quotaType, requestCount, bandwidth, eventCount);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ThrottleLimitDTO {\n");
    
    sb.append("    quotaType: ").append(toIndentedString(quotaType)).append("\n");
    sb.append("    requestCount: ").append(toIndentedString(requestCount)).append("\n");
    sb.append("    bandwidth: ").append(toIndentedString(bandwidth)).append("\n");
    sb.append("    eventCount: ").append(toIndentedString(eventCount)).append("\n");
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

