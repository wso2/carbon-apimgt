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



public class GatewayInstanceDTO   {
  
    private String gatewayId = null;
    private String lastActive = null;

    @XmlType(name="StatusEnum")
    @XmlEnum(String.class)
    public enum StatusEnum {
        ACTIVE("ACTIVE"),
        EXPIRED("EXPIRED");
        private String value;

        StatusEnum (String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static StatusEnum fromValue(String v) {
            for (StatusEnum b : StatusEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private StatusEnum status = StatusEnum.ACTIVE;

  /**
   **/
  public GatewayInstanceDTO gatewayId(String gatewayId) {
    this.gatewayId = gatewayId;
    return this;
  }

  
  @ApiModelProperty(example = "Env1_1372344", required = true, value = "")
  @JsonProperty("gatewayId")
  @NotNull
  public String getGatewayId() {
    return gatewayId;
  }
  public void setGatewayId(String gatewayId) {
    this.gatewayId = gatewayId;
  }

  /**
   **/
  public GatewayInstanceDTO lastActive(String lastActive) {
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
  public GatewayInstanceDTO status(StatusEnum status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(example = "ACTIVE", value = "")
  @JsonProperty("status")
  public StatusEnum getStatus() {
    return status;
  }
  public void setStatus(StatusEnum status) {
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
    GatewayInstanceDTO gatewayInstance = (GatewayInstanceDTO) o;
    return Objects.equals(gatewayId, gatewayInstance.gatewayId) &&
        Objects.equals(lastActive, gatewayInstance.lastActive) &&
        Objects.equals(status, gatewayInstance.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(gatewayId, lastActive, status);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GatewayInstanceDTO {\n");
    
    sb.append("    gatewayId: ").append(toIndentedString(gatewayId)).append("\n");
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

