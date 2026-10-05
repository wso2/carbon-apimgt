package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MCPServerDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SecurityInfoDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class MCPServerProxyRequestDTO   {
  
    private String url = null;
    private MCPServerDTO additionalProperties = null;
    private SecurityInfoDTO securityInfo = null;

  /**
   * Definition url
   **/
  public MCPServerProxyRequestDTO url(String url) {
    this.url = url;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Definition url")
  @JsonProperty("url")
  @NotNull
  public String getUrl() {
    return url;
  }
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   **/
  public MCPServerProxyRequestDTO additionalProperties(MCPServerDTO additionalProperties) {
    this.additionalProperties = additionalProperties;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
      @Valid
  @JsonProperty("additionalProperties")
  @NotNull
  public MCPServerDTO getAdditionalProperties() {
    return additionalProperties;
  }
  public void setAdditionalProperties(MCPServerDTO additionalProperties) {
    this.additionalProperties = additionalProperties;
  }

  /**
   **/
  public MCPServerProxyRequestDTO securityInfo(SecurityInfoDTO securityInfo) {
    this.securityInfo = securityInfo;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
      @Valid
  @JsonProperty("securityInfo")
  @NotNull
  public SecurityInfoDTO getSecurityInfo() {
    return securityInfo;
  }
  public void setSecurityInfo(SecurityInfoDTO securityInfo) {
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

