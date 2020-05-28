package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.BandwidthLimitDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.RequestCountLimitDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class ThrottleLimitTypeDTO   {
  
    private RequestCountLimitDTO requestCount = null;
    private BandwidthLimitDTO bandwidth = null;

  /**
   **/
  public ThrottleLimitTypeDTO requestCount(RequestCountLimitDTO requestCount) {
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
  public ThrottleLimitTypeDTO bandwidth(BandwidthLimitDTO bandwidth) {
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


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ThrottleLimitTypeDTO throttleLimitType = (ThrottleLimitTypeDTO) o;
    return Objects.equals(requestCount, throttleLimitType.requestCount) &&
        Objects.equals(bandwidth, throttleLimitType.bandwidth);
  }

  @Override
  public int hashCode() {
    return Objects.hash(requestCount, bandwidth);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ThrottleLimitTypeDTO {\n");
    
    sb.append("    requestCount: ").append(toIndentedString(requestCount)).append("\n");
    sb.append("    bandwidth: ").append(toIndentedString(bandwidth)).append("\n");
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

