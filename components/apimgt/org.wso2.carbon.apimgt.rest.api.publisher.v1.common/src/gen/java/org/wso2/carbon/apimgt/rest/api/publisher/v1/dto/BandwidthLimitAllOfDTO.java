package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class BandwidthLimitAllOfDTO   {
  
    private Long dataAmount = null;
    private String dataUnit = null;

  /**
   * Amount of data allowed to be transfered
   **/
  public BandwidthLimitAllOfDTO dataAmount(Long dataAmount) {
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
  public BandwidthLimitAllOfDTO dataUnit(String dataUnit) {
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
    BandwidthLimitAllOfDTO bandwidthLimitAllOf = (BandwidthLimitAllOfDTO) o;
    return Objects.equals(dataAmount, bandwidthLimitAllOf.dataAmount) &&
        Objects.equals(dataUnit, bandwidthLimitAllOf.dataUnit);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dataAmount, dataUnit);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BandwidthLimitAllOfDTO {\n");
    
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

