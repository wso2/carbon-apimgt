package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class ApplicationTokenDTO   {
  
    private String accessToken = null;
    private List<String> tokenScopes = new ArrayList<String>();
    private Long validityTime = null;

  /**
   * Access token
   **/
  public ApplicationTokenDTO accessToken(String accessToken) {
    this.accessToken = accessToken;
    return this;
  }

  
  @ApiModelProperty(example = "1.2345678901234568E+30", value = "Access token")
  @JsonProperty("accessToken")
  public String getAccessToken() {
    return accessToken;
  }
  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  /**
   * Valid comma seperated scopes for the access token
   **/
  public ApplicationTokenDTO tokenScopes(List<String> tokenScopes) {
    this.tokenScopes = tokenScopes;
    return this;
  }

  
  @ApiModelProperty(example = "[\"default\",\"read_api\",\"write_api\"]", value = "Valid comma seperated scopes for the access token")
  @JsonProperty("tokenScopes")
  public List<String> getTokenScopes() {
    return tokenScopes;
  }
  public void setTokenScopes(List<String> tokenScopes) {
    this.tokenScopes = tokenScopes;
  }

  /**
   * Maximum validity time for the access token
   **/
  public ApplicationTokenDTO validityTime(Long validityTime) {
    this.validityTime = validityTime;
    return this;
  }

  
  @ApiModelProperty(example = "3600", value = "Maximum validity time for the access token")
  @JsonProperty("validityTime")
  public Long getValidityTime() {
    return validityTime;
  }
  public void setValidityTime(Long validityTime) {
    this.validityTime = validityTime;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApplicationTokenDTO applicationToken = (ApplicationTokenDTO) o;
    return Objects.equals(accessToken, applicationToken.accessToken) &&
        Objects.equals(tokenScopes, applicationToken.tokenScopes) &&
        Objects.equals(validityTime, applicationToken.validityTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(accessToken, tokenScopes, validityTime);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationTokenDTO {\n");
    
    sb.append("    accessToken: ").append(toIndentedString(accessToken)).append("\n");
    sb.append("    tokenScopes: ").append(toIndentedString(tokenScopes)).append("\n");
    sb.append("    validityTime: ").append(toIndentedString(validityTime)).append("\n");
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

