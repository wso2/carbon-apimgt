package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.internal.service.dto.GatewayPropertiesDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class NotifyGatewayPayloadDTO   {
  

    @XmlType(name="PayloadTypeEnum")
    @XmlEnum(String.class)
    public enum PayloadTypeEnum {
        REGISTER("REGISTER"),
        HEARTBEAT("HEARTBEAT");
        private String value;

        PayloadTypeEnum (String v) {
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
        public static PayloadTypeEnum fromValue(String v) {
            for (PayloadTypeEnum b : PayloadTypeEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private PayloadTypeEnum payloadType = null;
    private GatewayPropertiesDTO gatewayProperties = null;
    private List<String> environmentLabels = new ArrayList<>();
    private String gatewayId = null;
    private Long timeStamp = null;

  /**
   * Indicates the type of payload. - \&quot;REGISTER\&quot;: Gateway registration payload - \&quot;HEARTBEAT\&quot;: Heartbeat update payload 
   **/
  public NotifyGatewayPayloadDTO payloadType(PayloadTypeEnum payloadType) {
    this.payloadType = payloadType;
    return this;
  }

  
  @ApiModelProperty(example = "REGISTER", required = true, value = "Indicates the type of payload. - \"REGISTER\": Gateway registration payload - \"HEARTBEAT\": Heartbeat update payload ")
  @JsonProperty("payloadType")
  @NotNull
  public PayloadTypeEnum getPayloadType() {
    return payloadType;
  }
  public void setPayloadType(PayloadTypeEnum payloadType) {
    this.payloadType = payloadType;
  }

  /**
   **/
  public NotifyGatewayPayloadDTO gatewayProperties(GatewayPropertiesDTO gatewayProperties) {
    this.gatewayProperties = gatewayProperties;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("gatewayProperties")
  public GatewayPropertiesDTO getGatewayProperties() {
    return gatewayProperties;
  }
  public void setGatewayProperties(GatewayPropertiesDTO gatewayProperties) {
    this.gatewayProperties = gatewayProperties;
  }

  /**
   * A list of environments this gateway supports.
   **/
  public NotifyGatewayPayloadDTO environmentLabels(List<String> environmentLabels) {
    this.environmentLabels = environmentLabels;
    return this;
  }

  
  @ApiModelProperty(example = "[\"default\",\"production\",\"sandbox\"]", value = "A list of environments this gateway supports.")
  @JsonProperty("environmentLabels")
  public List<String> getEnvironmentLabels() {
    return environmentLabels;
  }
  public void setEnvironmentLabels(List<String> environmentLabels) {
    this.environmentLabels = environmentLabels;
  }

  /**
   * The unique identifier assigned to the newly registered gateway.
   **/
  public NotifyGatewayPayloadDTO gatewayId(String gatewayId) {
    this.gatewayId = gatewayId;
    return this;
  }

  
  @ApiModelProperty(value = "The unique identifier assigned to the newly registered gateway.")
  @JsonProperty("gatewayId")
  public String getGatewayId() {
    return gatewayId;
  }
  public void setGatewayId(String gatewayId) {
    this.gatewayId = gatewayId;
  }

  /**
   * The timestamp when the heartbeat was generated.
   **/
  public NotifyGatewayPayloadDTO timeStamp(Long timeStamp) {
    this.timeStamp = timeStamp;
    return this;
  }

  
  @ApiModelProperty(value = "The timestamp when the heartbeat was generated.")
  @JsonProperty("timeStamp")
  public Long getTimeStamp() {
    return timeStamp;
  }
  public void setTimeStamp(Long timeStamp) {
    this.timeStamp = timeStamp;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NotifyGatewayPayloadDTO notifyGatewayPayload = (NotifyGatewayPayloadDTO) o;
    return Objects.equals(payloadType, notifyGatewayPayload.payloadType) &&
        Objects.equals(gatewayProperties, notifyGatewayPayload.gatewayProperties) &&
        Objects.equals(environmentLabels, notifyGatewayPayload.environmentLabels) &&
        Objects.equals(gatewayId, notifyGatewayPayload.gatewayId) &&
        Objects.equals(timeStamp, notifyGatewayPayload.timeStamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(payloadType, gatewayProperties, environmentLabels, gatewayId, timeStamp);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class NotifyGatewayPayloadDTO {\n");
    
    sb.append("    payloadType: ").append(toIndentedString(payloadType)).append("\n");
    sb.append("    gatewayProperties: ").append(toIndentedString(gatewayProperties)).append("\n");
    sb.append("    environmentLabels: ").append(toIndentedString(environmentLabels)).append("\n");
    sb.append("    gatewayId: ").append(toIndentedString(gatewayId)).append("\n");
    sb.append("    timeStamp: ").append(toIndentedString(timeStamp)).append("\n");
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

