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
        Objects.equals(securityInfo, mcPServerValidationRequest.securityInfo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(url, securityInfo);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MCPServerValidationRequestDTO {\n");
    
    sb.append("    url: ").append(toIndentedString(url)).append("\n");
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

