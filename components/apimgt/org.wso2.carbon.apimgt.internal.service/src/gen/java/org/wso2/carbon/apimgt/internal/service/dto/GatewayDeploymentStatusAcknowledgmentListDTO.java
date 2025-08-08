package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.internal.service.dto.GatewayDeploymentStatusAcknowledgmentDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class GatewayDeploymentStatusAcknowledgmentListDTO   {
  
    private Integer count = null;
    private List<GatewayDeploymentStatusAcknowledgmentDTO> list = new ArrayList<>();

  /**
   * Number of APIs returned. 
   **/
  public GatewayDeploymentStatusAcknowledgmentListDTO count(Integer count) {
    this.count = count;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "Number of APIs returned. ")
  @JsonProperty("count")
  public Integer getCount() {
    return count;
  }
  public void setCount(Integer count) {
    this.count = count;
  }

  /**
   **/
  public GatewayDeploymentStatusAcknowledgmentListDTO list(List<GatewayDeploymentStatusAcknowledgmentDTO> list) {
    this.list = list;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("list")
  public List<GatewayDeploymentStatusAcknowledgmentDTO> getList() {
    return list;
  }
  public void setList(List<GatewayDeploymentStatusAcknowledgmentDTO> list) {
    this.list = list;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GatewayDeploymentStatusAcknowledgmentListDTO gatewayDeploymentStatusAcknowledgmentList = (GatewayDeploymentStatusAcknowledgmentListDTO) o;
    return Objects.equals(count, gatewayDeploymentStatusAcknowledgmentList.count) &&
        Objects.equals(list, gatewayDeploymentStatusAcknowledgmentList.list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, list);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GatewayDeploymentStatusAcknowledgmentListDTO {\n");
    
    sb.append("    count: ").append(toIndentedString(count)).append("\n");
    sb.append("    list: ").append(toIndentedString(list)).append("\n");
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

