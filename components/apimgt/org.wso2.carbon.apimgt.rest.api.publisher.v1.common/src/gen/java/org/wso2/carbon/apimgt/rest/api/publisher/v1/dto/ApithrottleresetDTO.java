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



public class ApithrottleresetDTO   {
  
    private String policylevel = null;
    private String apitier = null;
    private String resourceTier = null;
    private String apiContext = null;
    private String apiVersion = null;
    private String resource = null;
    private String apiTenant = null;
    private String apiName = null;

  /**
   * the policy Level which the counters should be reset
   **/
  public ApithrottleresetDTO policylevel(String policylevel) {
    this.policylevel = policylevel;
    return this;
  }

  
  @ApiModelProperty(example = "api", required = true, value = "the policy Level which the counters should be reset")
  @JsonProperty("policylevel")
  @NotNull
  public String getPolicylevel() {
    return policylevel;
  }
  public void setPolicylevel(String policylevel) {
    this.policylevel = policylevel;
  }

  /**
   **/
  public ApithrottleresetDTO apitier(String apitier) {
    this.apitier = apitier;
    return this;
  }

  
  @ApiModelProperty(example = "Unlimited", value = "")
  @JsonProperty("apitier")
  public String getApitier() {
    return apitier;
  }
  public void setApitier(String apitier) {
    this.apitier = apitier;
  }

  /**
   **/
  public ApithrottleresetDTO resourceTier(String resourceTier) {
    this.resourceTier = resourceTier;
    return this;
  }

  
  @ApiModelProperty(example = "Unlimited", value = "")
  @JsonProperty("resourceTier")
  public String getResourceTier() {
    return resourceTier;
  }
  public void setResourceTier(String resourceTier) {
    this.resourceTier = resourceTier;
  }

  /**
   **/
  public ApithrottleresetDTO apiContext(String apiContext) {
    this.apiContext = apiContext;
    return this;
  }

  
  @ApiModelProperty(example = "/pizzashack/1.0.0", required = true, value = "")
  @JsonProperty("apiContext")
  @NotNull
  public String getApiContext() {
    return apiContext;
  }
  public void setApiContext(String apiContext) {
    this.apiContext = apiContext;
  }

  /**
   **/
  public ApithrottleresetDTO apiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
    return this;
  }

  
  @ApiModelProperty(example = "1.0.0", required = true, value = "")
  @JsonProperty("apiVersion")
  @NotNull
  public String getApiVersion() {
    return apiVersion;
  }
  public void setApiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
  }

  /**
   **/
  public ApithrottleresetDTO resource(String resource) {
    this.resource = resource;
    return this;
  }

  
  @ApiModelProperty(example = "/menu:GET", value = "")
  @JsonProperty("resource")
  public String getResource() {
    return resource;
  }
  public void setResource(String resource) {
    this.resource = resource;
  }

  /**
   **/
  public ApithrottleresetDTO apiTenant(String apiTenant) {
    this.apiTenant = apiTenant;
    return this;
  }

  
  @ApiModelProperty(example = "carbon.super", required = true, value = "")
  @JsonProperty("apiTenant")
  @NotNull
  public String getApiTenant() {
    return apiTenant;
  }
  public void setApiTenant(String apiTenant) {
    this.apiTenant = apiTenant;
  }

  /**
   **/
  public ApithrottleresetDTO apiName(String apiName) {
    this.apiName = apiName;
    return this;
  }

  
  @ApiModelProperty(example = "pizzashack", required = true, value = "")
  @JsonProperty("apiName")
  @NotNull
  public String getApiName() {
    return apiName;
  }
  public void setApiName(String apiName) {
    this.apiName = apiName;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApithrottleresetDTO apithrottlereset = (ApithrottleresetDTO) o;
    return Objects.equals(policylevel, apithrottlereset.policylevel) &&
        Objects.equals(apitier, apithrottlereset.apitier) &&
        Objects.equals(resourceTier, apithrottlereset.resourceTier) &&
        Objects.equals(apiContext, apithrottlereset.apiContext) &&
        Objects.equals(apiVersion, apithrottlereset.apiVersion) &&
        Objects.equals(resource, apithrottlereset.resource) &&
        Objects.equals(apiTenant, apithrottlereset.apiTenant) &&
        Objects.equals(apiName, apithrottlereset.apiName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(policylevel, apitier, resourceTier, apiContext, apiVersion, resource, apiTenant, apiName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApithrottleresetDTO {\n");
    
    sb.append("    policylevel: ").append(toIndentedString(policylevel)).append("\n");
    sb.append("    apitier: ").append(toIndentedString(apitier)).append("\n");
    sb.append("    resourceTier: ").append(toIndentedString(resourceTier)).append("\n");
    sb.append("    apiContext: ").append(toIndentedString(apiContext)).append("\n");
    sb.append("    apiVersion: ").append(toIndentedString(apiVersion)).append("\n");
    sb.append("    resource: ").append(toIndentedString(resource)).append("\n");
    sb.append("    apiTenant: ").append(toIndentedString(apiTenant)).append("\n");
    sb.append("    apiName: ").append(toIndentedString(apiName)).append("\n");
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

