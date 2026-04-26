package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.DiscoverySummaryByReachabilityDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.DiscoverySummaryByServiceDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.DiscoverySummaryByTypeDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class DiscoverySummaryDTO   {
  
    private Integer total = null;
    private Integer managed = null;
    private Integer unmanaged = null;
    private Boolean skipInternal = null;
    private DiscoverySummaryByTypeDTO byType = null;
    private DiscoverySummaryByReachabilityDTO byReachability = null;
    private List<DiscoverySummaryByServiceDTO> byService = new ArrayList<DiscoverySummaryByServiceDTO>();

  /**
   **/
  public DiscoverySummaryDTO total(Integer total) {
    this.total = total;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("total")
  public Integer getTotal() {
    return total;
  }
  public void setTotal(Integer total) {
    this.total = total;
  }

  /**
   **/
  public DiscoverySummaryDTO managed(Integer managed) {
    this.managed = managed;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("managed")
  public Integer getManaged() {
    return managed;
  }
  public void setManaged(Integer managed) {
    this.managed = managed;
  }

  /**
   **/
  public DiscoverySummaryDTO unmanaged(Integer unmanaged) {
    this.unmanaged = unmanaged;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("unmanaged")
  public Integer getUnmanaged() {
    return unmanaged;
  }
  public void setUnmanaged(Integer unmanaged) {
    this.unmanaged = unmanaged;
  }

  /**
   **/
  public DiscoverySummaryDTO skipInternal(Boolean skipInternal) {
    this.skipInternal = skipInternal;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("skipInternal")
  public Boolean isSkipInternal() {
    return skipInternal;
  }
  public void setSkipInternal(Boolean skipInternal) {
    this.skipInternal = skipInternal;
  }

  /**
   **/
  public DiscoverySummaryDTO byType(DiscoverySummaryByTypeDTO byType) {
    this.byType = byType;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("byType")
  public DiscoverySummaryByTypeDTO getByType() {
    return byType;
  }
  public void setByType(DiscoverySummaryByTypeDTO byType) {
    this.byType = byType;
  }

  /**
   **/
  public DiscoverySummaryDTO byReachability(DiscoverySummaryByReachabilityDTO byReachability) {
    this.byReachability = byReachability;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("byReachability")
  public DiscoverySummaryByReachabilityDTO getByReachability() {
    return byReachability;
  }
  public void setByReachability(DiscoverySummaryByReachabilityDTO byReachability) {
    this.byReachability = byReachability;
  }

  /**
   **/
  public DiscoverySummaryDTO byService(List<DiscoverySummaryByServiceDTO> byService) {
    this.byService = byService;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("byService")
  public List<DiscoverySummaryByServiceDTO> getByService() {
    return byService;
  }
  public void setByService(List<DiscoverySummaryByServiceDTO> byService) {
    this.byService = byService;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DiscoverySummaryDTO discoverySummary = (DiscoverySummaryDTO) o;
    return Objects.equals(total, discoverySummary.total) &&
        Objects.equals(managed, discoverySummary.managed) &&
        Objects.equals(unmanaged, discoverySummary.unmanaged) &&
        Objects.equals(skipInternal, discoverySummary.skipInternal) &&
        Objects.equals(byType, discoverySummary.byType) &&
        Objects.equals(byReachability, discoverySummary.byReachability) &&
        Objects.equals(byService, discoverySummary.byService);
  }

  @Override
  public int hashCode() {
    return Objects.hash(total, managed, unmanaged, skipInternal, byType, byReachability, byService);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DiscoverySummaryDTO {\n");
    
    sb.append("    total: ").append(toIndentedString(total)).append("\n");
    sb.append("    managed: ").append(toIndentedString(managed)).append("\n");
    sb.append("    unmanaged: ").append(toIndentedString(unmanaged)).append("\n");
    sb.append("    skipInternal: ").append(toIndentedString(skipInternal)).append("\n");
    sb.append("    byType: ").append(toIndentedString(byType)).append("\n");
    sb.append("    byReachability: ").append(toIndentedString(byReachability)).append("\n");
    sb.append("    byService: ").append(toIndentedString(byService)).append("\n");
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

