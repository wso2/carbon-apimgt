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



public class GatewayInstantDTO   {
  
    private String gatewayID = null;
    private String lastActive = null;
    private String status = null;

  /**
   **/
  public GatewayInstantDTO gatewayID(String gatewayID) {
    this.gatewayID = gatewayID;
    return this;
  }

  
  @ApiModelProperty(example = "Env1_1372344", value = "")
  @JsonProperty("gatewayID")
  public String getGatewayID() {
    return gatewayID;
  }
  public void setGatewayID(String gatewayID) {
    this.gatewayID = gatewayID;
  }

  /**
   **/
  public GatewayInstantDTO lastActive(String lastActive) {
    this.lastActive = lastActive;
    return this;
  }

  
  @ApiModelProperty(example = "2025-06-26T06:47:50Z", value = "")
  @JsonProperty("lastActive")
  public String getLastActive() {
    return lastActive;
  }
  public void setLastActive(String lastActive) {
    this.lastActive = lastActive;
  }

  /**
   **/
  public GatewayInstantDTO status(String status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(example = "ACTIVE", value = "")
  @JsonProperty("status")
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GatewayInstantDTO gatewayInstant = (GatewayInstantDTO) o;
    return Objects.equals(gatewayID, gatewayInstant.gatewayID) &&
        Objects.equals(lastActive, gatewayInstant.lastActive) &&
        Objects.equals(status, gatewayInstant.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(gatewayID, lastActive, status);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GatewayInstantDTO {\n");
    
    sb.append("    gatewayID: ").append(toIndentedString(gatewayID)).append("\n");
    sb.append("    lastActive: ").append(toIndentedString(lastActive)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
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

