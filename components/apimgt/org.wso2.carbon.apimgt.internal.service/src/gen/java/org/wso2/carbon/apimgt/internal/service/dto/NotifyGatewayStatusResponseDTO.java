package org.wso2.carbon.apimgt.internal.service.dto;

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



public class NotifyGatewayStatusResponseDTO   {
  

    @XmlType(name="StatusEnum")
    @XmlEnum(String.class)
    public enum StatusEnum {
        REGISTERED("registered"),
        ACKNOWLEDGED("acknowledged");
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
    private StatusEnum status = null;
    private String gatewayId = null;

  /**
   * Response status
   **/
  public NotifyGatewayStatusResponseDTO status(StatusEnum status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(example = "registered", required = true, value = "Response status")
  @JsonProperty("status")
  @NotNull
  public StatusEnum getStatus() {
    return status;
  }
  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  /**
   * Returned if registration was successful.
   **/
  public NotifyGatewayStatusResponseDTO gatewayId(String gatewayId) {
    this.gatewayId = gatewayId;
    return this;
  }

  
  @ApiModelProperty(example = "ID_1", value = "Returned if registration was successful.")
  @JsonProperty("gatewayId")
  public String getGatewayId() {
    return gatewayId;
  }
  public void setGatewayId(String gatewayId) {
    this.gatewayId = gatewayId;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NotifyGatewayStatusResponseDTO notifyGatewayStatusResponse = (NotifyGatewayStatusResponseDTO) o;
    return Objects.equals(status, notifyGatewayStatusResponse.status) &&
        Objects.equals(gatewayId, notifyGatewayStatusResponse.gatewayId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, gatewayId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class NotifyGatewayStatusResponseDTO {\n");
    
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    gatewayId: ").append(toIndentedString(gatewayId)).append("\n");
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

