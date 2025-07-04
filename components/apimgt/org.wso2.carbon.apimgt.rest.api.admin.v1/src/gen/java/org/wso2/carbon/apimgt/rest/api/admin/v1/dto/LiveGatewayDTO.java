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



public class LiveGatewayDTO   {
  
    private String gatewayID = null;
    private String lastActive = null;

  /**
   **/
  public LiveGatewayDTO gatewayID(String gatewayID) {
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
  public LiveGatewayDTO lastActive(String lastActive) {
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


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LiveGatewayDTO liveGateway = (LiveGatewayDTO) o;
    return Objects.equals(gatewayID, liveGateway.gatewayID) &&
        Objects.equals(lastActive, liveGateway.lastActive);
  }

  @Override
  public int hashCode() {
    return Objects.hash(gatewayID, lastActive);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LiveGatewayDTO {\n");
    
    sb.append("    gatewayID: ").append(toIndentedString(gatewayID)).append("\n");
    sb.append("    lastActive: ").append(toIndentedString(lastActive)).append("\n");
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

