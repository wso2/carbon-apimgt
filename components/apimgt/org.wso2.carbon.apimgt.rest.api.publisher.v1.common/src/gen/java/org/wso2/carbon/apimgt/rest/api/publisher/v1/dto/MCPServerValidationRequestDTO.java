package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SecurityInfoDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class MCPServerValidationRequestDTO   {
  
    private String url = null;
    private SecurityInfoDTO securityInfo = null;
    private String mcpServerId = null;

    @XmlType(name="EndpointTypeEnum")
    @XmlEnum(String.class)
    public enum EndpointTypeEnum {
        PRODUCTION("PRODUCTION"),
        SANDBOX("SANDBOX");
        private String value;

        EndpointTypeEnum (String v) {
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
        public static EndpointTypeEnum fromValue(String v) {
            for (EndpointTypeEnum b : EndpointTypeEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private EndpointTypeEnum endpointType = null;

  /**
   * The URL to be validated.
   **/
  public MCPServerValidationRequestDTO url(String url) {
    this.url = url;
    return this;
  }

  
  @ApiModelProperty(example = "https://example.com/mcp", value = "The URL to be validated.")
  @JsonProperty("url")
  public String getUrl() {
    return url;
  }
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   **/
  public MCPServerValidationRequestDTO securityInfo(SecurityInfoDTO securityInfo) {
    this.securityInfo = securityInfo;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("securityInfo")
  public SecurityInfoDTO getSecurityInfo() {
    return securityInfo;
  }
  public void setSecurityInfo(SecurityInfoDTO securityInfo) {
    this.securityInfo = securityInfo;
  }

  /**
   * UUID of the MCP Server whose stored endpoint security should be used when securityInfo is absent.
   **/
  public MCPServerValidationRequestDTO mcpServerId(String mcpServerId) {
    this.mcpServerId = mcpServerId;
    return this;
  }

  
  @ApiModelProperty(example = "2962f50e-1c05-4b5d-a9c7-12345abcdef0", value = "UUID of the MCP Server whose stored endpoint security should be used when securityInfo is absent.")
  @JsonProperty("mcpServerId")
  public String getMcpServerId() {
    return mcpServerId;
  }
  public void setMcpServerId(String mcpServerId) {
    this.mcpServerId = mcpServerId;
  }

  /**
   * The endpoint environment whose stored security config should be used. Required when mcpServerId is provided.
   **/
  public MCPServerValidationRequestDTO endpointType(EndpointTypeEnum endpointType) {
    this.endpointType = endpointType;
    return this;
  }

  
  @ApiModelProperty(example = "PRODUCTION", value = "The endpoint environment whose stored security config should be used. Required when mcpServerId is provided.")
  @JsonProperty("endpointType")
  public EndpointTypeEnum getEndpointType() {
    return endpointType;
  }
  public void setEndpointType(EndpointTypeEnum endpointType) {
    this.endpointType = endpointType;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MCPServerValidationRequestDTO mcPServerValidationRequest = (MCPServerValidationRequestDTO) o;
    return Objects.equals(url, mcPServerValidationRequest.url) &&
        Objects.equals(securityInfo, mcPServerValidationRequest.securityInfo) &&
        Objects.equals(mcpServerId, mcPServerValidationRequest.mcpServerId) &&
        Objects.equals(endpointType, mcPServerValidationRequest.endpointType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(url, securityInfo, mcpServerId, endpointType);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MCPServerValidationRequestDTO {\n");
    
    sb.append("    url: ").append(toIndentedString(url)).append("\n");
    sb.append("    securityInfo: ").append(toIndentedString(securityInfo)).append("\n");
    sb.append("    mcpServerId: ").append(toIndentedString(mcpServerId)).append("\n");
    sb.append("    endpointType: ").append(toIndentedString(endpointType)).append("\n");
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

