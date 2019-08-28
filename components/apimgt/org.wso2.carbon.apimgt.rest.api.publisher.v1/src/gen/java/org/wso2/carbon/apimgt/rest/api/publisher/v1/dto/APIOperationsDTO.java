package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;



public class APIOperationsDTO   {
  
    private String id = null;
    private String uritemplate = "/_*";
    private String httpVerb = "GET";
    private String authType = "Any";
    private String throttlingPolicy = null;
    private List<String> scopes = new ArrayList<>();

  /**
   **/
  public APIOperationsDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "postapiresource", value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public APIOperationsDTO uritemplate(String uritemplate) {
    this.uritemplate = uritemplate;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("uritemplate")
  public String getUritemplate() {
    return uritemplate;
  }
  public void setUritemplate(String uritemplate) {
    this.uritemplate = uritemplate;
  }

  /**
   **/
  public APIOperationsDTO httpVerb(String httpVerb) {
    this.httpVerb = httpVerb;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("httpVerb")
  public String getHttpVerb() {
    return httpVerb;
  }
  public void setHttpVerb(String httpVerb) {
    this.httpVerb = httpVerb;
  }

  /**
   **/
  public APIOperationsDTO authType(String authType) {
    this.authType = authType;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("authType")
  public String getAuthType() {
    return authType;
  }
  public void setAuthType(String authType) {
    this.authType = authType;
  }

  /**
   **/
  public APIOperationsDTO throttlingPolicy(String throttlingPolicy) {
    this.throttlingPolicy = throttlingPolicy;
    return this;
  }

  
  @ApiModelProperty(example = "Unlimited", value = "")
  @JsonProperty("throttlingPolicy")
  public String getThrottlingPolicy() {
    return throttlingPolicy;
  }
  public void setThrottlingPolicy(String throttlingPolicy) {
    this.throttlingPolicy = throttlingPolicy;
  }

  /**
   **/
  public APIOperationsDTO scopes(List<String> scopes) {
    this.scopes = scopes;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("scopes")
  public List<String> getScopes() {
    return scopes;
  }
  public void setScopes(List<String> scopes) {
    this.scopes = scopes;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIOperationsDTO apIOperations = (APIOperationsDTO) o;
    return Objects.equals(id, apIOperations.id) &&
        Objects.equals(uritemplate, apIOperations.uritemplate) &&
        Objects.equals(httpVerb, apIOperations.httpVerb) &&
        Objects.equals(authType, apIOperations.authType) &&
        Objects.equals(throttlingPolicy, apIOperations.throttlingPolicy) &&
        Objects.equals(scopes, apIOperations.scopes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, uritemplate, httpVerb, authType, throttlingPolicy, scopes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIOperationsDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    uritemplate: ").append(toIndentedString(uritemplate)).append("\n");
    sb.append("    httpVerb: ").append(toIndentedString(httpVerb)).append("\n");
    sb.append("    authType: ").append(toIndentedString(authType)).append("\n");
    sb.append("    throttlingPolicy: ").append(toIndentedString(throttlingPolicy)).append("\n");
    sb.append("    scopes: ").append(toIndentedString(scopes)).append("\n");
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

