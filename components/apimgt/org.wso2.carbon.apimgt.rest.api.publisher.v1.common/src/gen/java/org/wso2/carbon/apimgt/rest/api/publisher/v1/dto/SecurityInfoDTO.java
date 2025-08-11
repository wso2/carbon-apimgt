package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;

/**
 * Optional security headers to use during URL validation.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Optional security headers to use during URL validation.")

public class SecurityInfoDTO   {
  
    private Boolean isSecure = false;
    private String header = null;
    private String value = null;

  /**
   * Indicates whether the URL is secure (HTTPS) or not (HTTP).
   **/
  public SecurityInfoDTO isSecure(Boolean isSecure) {
    this.isSecure = isSecure;
    return this;
  }

  
  @ApiModelProperty(value = "Indicates whether the URL is secure (HTTPS) or not (HTTP).")
  @JsonProperty("isSecure")
  public Boolean isIsSecure() {
    return isSecure;
  }
  public void setIsSecure(Boolean isSecure) {
    this.isSecure = isSecure;
  }

  /**
   * Header name used for authentication, if required.
   **/
  public SecurityInfoDTO header(String header) {
    this.header = header;
    return this;
  }

  
  @ApiModelProperty(example = "Authorization", value = "Header name used for authentication, if required.")
  @JsonProperty("header")
  public String getHeader() {
    return header;
  }
  public void setHeader(String header) {
    this.header = header;
  }

  /**
   * Value used for the authentication header.
   **/
  public SecurityInfoDTO value(String value) {
    this.value = value;
    return this;
  }

  
  @ApiModelProperty(example = "Bearer <token>", value = "Value used for the authentication header.")
  @JsonProperty("value")
  public String getValue() {
    return value;
  }
  public void setValue(String value) {
    this.value = value;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SecurityInfoDTO securityInfo = (SecurityInfoDTO) o;
    return Objects.equals(isSecure, securityInfo.isSecure) &&
        Objects.equals(header, securityInfo.header) &&
        Objects.equals(value, securityInfo.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isSecure, header, value);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SecurityInfoDTO {\n");
    
    sb.append("    isSecure: ").append(toIndentedString(isSecure)).append("\n");
    sb.append("    header: ").append(toIndentedString(header)).append("\n");
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
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

