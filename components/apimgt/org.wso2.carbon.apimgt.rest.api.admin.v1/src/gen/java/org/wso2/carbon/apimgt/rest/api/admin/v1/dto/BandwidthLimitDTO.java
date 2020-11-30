package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.BandwidthLimitAllOfDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ThrottleLimitBaseDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class BandwidthLimitDTO   {
  
    private String timeUnit = null;
    private Integer unitTime = null;
    private Long dataAmount = null;
    private String dataUnit = null;

  /**
   * Unit of the time. Allowed values are \&quot;sec\&quot;, \&quot;min\&quot;, \&quot;hour\&quot;, \&quot;day\&quot;
   **/
  public BandwidthLimitDTO timeUnit(String timeUnit) {
    this.timeUnit = timeUnit;
    return this;
  }

  
  @ApiModelProperty(example = "min", required = true, value = "Unit of the time. Allowed values are \"sec\", \"min\", \"hour\", \"day\"")
  @JsonProperty("timeUnit")
  @NotNull
  public String getTimeUnit() {
    return timeUnit;
  }
  public void setTimeUnit(String timeUnit) {
    this.timeUnit = timeUnit;
  }

  /**
   * Time limit that the throttling limit applies.
   **/
  public BandwidthLimitDTO unitTime(Integer unitTime) {
    this.unitTime = unitTime;
    return this;
  }

  
  @ApiModelProperty(example = "10", required = true, value = "Time limit that the throttling limit applies.")
  @JsonProperty("unitTime")
  @NotNull
  public Integer getUnitTime() {
    return unitTime;
  }
  public void setUnitTime(Integer unitTime) {
    this.unitTime = unitTime;
  }

  /**
   * Amount of data allowed to be transfered
   **/
  public BandwidthLimitDTO dataAmount(Long dataAmount) {
    this.dataAmount = dataAmount;
    return this;
  }

  
  @ApiModelProperty(example = "1000", required = true, value = "Amount of data allowed to be transfered")
  @JsonProperty("dataAmount")
  @NotNull
  public Long getDataAmount() {
    return dataAmount;
  }
  public void setDataAmount(Long dataAmount) {
    this.dataAmount = dataAmount;
  }

  /**
   * Unit of data allowed to be transfered. Allowed values are \&quot;KB\&quot;, \&quot;MB\&quot; and \&quot;GB\&quot;
   **/
  public BandwidthLimitDTO dataUnit(String dataUnit) {
    this.dataUnit = dataUnit;
    return this;
  }

  
  @ApiModelProperty(example = "KB", required = true, value = "Unit of data allowed to be transfered. Allowed values are \"KB\", \"MB\" and \"GB\"")
  @JsonProperty("dataUnit")
  @NotNull
  public String getDataUnit() {
    return dataUnit;
  }
  public void setDataUnit(String dataUnit) {
    this.dataUnit = dataUnit;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BandwidthLimitDTO bandwidthLimit = (BandwidthLimitDTO) o;
    return Objects.equals(timeUnit, bandwidthLimit.timeUnit) &&
        Objects.equals(unitTime, bandwidthLimit.unitTime) &&
        Objects.equals(dataAmount, bandwidthLimit.dataAmount) &&
        Objects.equals(dataUnit, bandwidthLimit.dataUnit);
  }

  @Override
  public int hashCode() {
    return Objects.hash(timeUnit, unitTime, dataAmount, dataUnit);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BandwidthLimitDTO {\n");
    
    sb.append("    timeUnit: ").append(toIndentedString(timeUnit)).append("\n");
    sb.append("    unitTime: ").append(toIndentedString(unitTime)).append("\n");
    sb.append("    dataAmount: ").append(toIndentedString(dataAmount)).append("\n");
    sb.append("    dataUnit: ").append(toIndentedString(dataUnit)).append("\n");
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

