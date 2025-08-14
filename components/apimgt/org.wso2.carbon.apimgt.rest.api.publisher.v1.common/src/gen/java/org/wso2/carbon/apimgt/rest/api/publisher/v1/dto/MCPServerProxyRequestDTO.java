package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

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



public class MCPServerProxyRequestDTO   {
  
    private String url = null;
    private String additionalProperties = null;
    private String securityInfo = null;

  /**
   * Definition url
   **/
  public MCPServerProxyRequestDTO url(String url) {
    this.url = url;
    return this;
  }

  
  @ApiModelProperty(value = "Definition url")
  @JsonProperty("url")
  public String getUrl() {
    return url;
  }
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * Additional attributes specified as a stringified JSON with MCP Server&#39;s schema
   **/
  public MCPServerProxyRequestDTO additionalProperties(String additionalProperties) {
    this.additionalProperties = additionalProperties;
    return this;
  }

  
  @ApiModelProperty(value = "Additional attributes specified as a stringified JSON with MCP Server's schema")
  @JsonProperty("additionalProperties")
  public String getAdditionalProperties() {
    return additionalProperties;
  }
  public void setAdditionalProperties(String additionalProperties) {
    this.additionalProperties = additionalProperties;
  }

  /**
   * SecurityInfo object specified as a stringified JSON
   **/
  public MCPServerProxyRequestDTO securityInfo(String securityInfo) {
    this.securityInfo = securityInfo;
    return this;
  }

  
  @ApiModelProperty(value = "SecurityInfo object specified as a stringified JSON")
  @JsonProperty("securityInfo")
  public String getSecurityInfo() {
    return securityInfo;
  }
  public void setSecurityInfo(String securityInfo) {
    this.securityInfo = securityInfo;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MCPServerProxyRequestDTO mcPServerProxyRequest = (MCPServerProxyRequestDTO) o;
    return Objects.equals(url, mcPServerProxyRequest.url) &&
        Objects.equals(additionalProperties, mcPServerProxyRequest.additionalProperties) &&
        Objects.equals(securityInfo, mcPServerProxyRequest.securityInfo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(url, additionalProperties, securityInfo);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MCPServerProxyRequestDTO {\n");
    
    sb.append("    url: ").append(toIndentedString(url)).append("\n");
    sb.append("    additionalProperties: ").append(toIndentedString(additionalProperties)).append("\n");
    sb.append("    securityInfo: ").append(toIndentedString(securityInfo)).append("\n");
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

