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



public class DiscoverySummaryByServiceDTO   {
  
    private String serviceIdentity = null;
    private Boolean fullyGoverned = null;
    private Integer shadow = null;
    private Integer drift = null;

  /**
   **/
  public DiscoverySummaryByServiceDTO serviceIdentity(String serviceIdentity) {
    this.serviceIdentity = serviceIdentity;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("serviceIdentity")
  public String getServiceIdentity() {
    return serviceIdentity;
  }
  public void setServiceIdentity(String serviceIdentity) {
    this.serviceIdentity = serviceIdentity;
  }

  /**
   **/
  public DiscoverySummaryByServiceDTO fullyGoverned(Boolean fullyGoverned) {
    this.fullyGoverned = fullyGoverned;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("fullyGoverned")
  public Boolean isFullyGoverned() {
    return fullyGoverned;
  }
  public void setFullyGoverned(Boolean fullyGoverned) {
    this.fullyGoverned = fullyGoverned;
  }

  /**
   **/
  public DiscoverySummaryByServiceDTO shadow(Integer shadow) {
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
  public DiscoverySummaryByServiceDTO drift(Integer drift) {
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
    DiscoverySummaryByServiceDTO discoverySummaryByService = (DiscoverySummaryByServiceDTO) o;
    return Objects.equals(serviceIdentity, discoverySummaryByService.serviceIdentity) &&
        Objects.equals(fullyGoverned, discoverySummaryByService.fullyGoverned) &&
        Objects.equals(shadow, discoverySummaryByService.shadow) &&
        Objects.equals(drift, discoverySummaryByService.drift);
  }

  @Override
  public int hashCode() {
    return Objects.hash(serviceIdentity, fullyGoverned, shadow, drift);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DiscoverySummaryByServiceDTO {\n");
    
    sb.append("    serviceIdentity: ").append(toIndentedString(serviceIdentity)).append("\n");
    sb.append("    fullyGoverned: ").append(toIndentedString(fullyGoverned)).append("\n");
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

