package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

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



public class DiscoverySummaryByTypeDTO   {
  
    private Integer shadow = null;
    private Integer drift = null;

  /**
   **/
  public DiscoverySummaryByTypeDTO shadow(Integer shadow) {
    this.shadow = shadow;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("shadow")
  public Integer getShadow() {
    return shadow;
  }
  public void setShadow(Integer shadow) {
    this.shadow = shadow;
  }

  /**
   **/
  public DiscoverySummaryByTypeDTO drift(Integer drift) {
    this.drift = drift;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("drift")
  public Integer getDrift() {
    return drift;
  }
  public void setDrift(Integer drift) {
    this.drift = drift;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DiscoverySummaryByTypeDTO discoverySummaryByType = (DiscoverySummaryByTypeDTO) o;
    return Objects.equals(shadow, discoverySummaryByType.shadow) &&
        Objects.equals(drift, discoverySummaryByType.drift);
  }

  @Override
  public int hashCode() {
    return Objects.hash(shadow, drift);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DiscoverySummaryByTypeDTO {\n");
    
    sb.append("    shadow: ").append(toIndentedString(shadow)).append("\n");
    sb.append("    drift: ").append(toIndentedString(drift)).append("\n");
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

