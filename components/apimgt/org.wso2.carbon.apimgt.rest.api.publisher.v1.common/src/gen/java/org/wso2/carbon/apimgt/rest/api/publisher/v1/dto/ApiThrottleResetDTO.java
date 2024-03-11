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



public class ApiThrottleResetDTO   {
  
    private String policyLevel = null;
    private String apiName = null;
    private String apiContext = null;
    private String apiVersion = null;
    private String resource = null;
    private String apiTier = null;
    private String resourceTier = null;

  /**
   * the policy Level which the counters should be reset
   **/
  public ApiThrottleResetDTO policyLevel(String policyLevel) {
    this.policyLevel = policyLevel;
    return this;
  }

  
  @ApiModelProperty(example = "api", required = true, value = "the policy Level which the counters should be reset")
  @JsonProperty("policyLevel")
  @NotNull
  public String getPolicyLevel() {
    return policyLevel;
  }
  public void setPolicyLevel(String policyLevel) {
    this.policyLevel = policyLevel;
  }

  /**
   **/
  public ApiThrottleResetDTO apiName(String apiName) {
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

  /**
   **/
  public ApiThrottleResetDTO apiContext(String apiContext) {
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
  public ApiThrottleResetDTO apiVersion(String apiVersion) {
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
  public ApiThrottleResetDTO resource(String resource) {
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
  public ApiThrottleResetDTO apiTier(String apiTier) {
    this.apiTier = apiTier;
    return this;
  }

  
  @ApiModelProperty(example = "Unlimited", value = "")
  @JsonProperty("apiTier")
  public String getApiTier() {
    return apiTier;
  }
  public void setApiTier(String apiTier) {
    this.apiTier = apiTier;
  }

  /**
   **/
  public ApiThrottleResetDTO resourceTier(String resourceTier) {
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


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiThrottleResetDTO apiThrottleReset = (ApiThrottleResetDTO) o;
    return Objects.equals(policyLevel, apiThrottleReset.policyLevel) &&
        Objects.equals(apiName, apiThrottleReset.apiName) &&
        Objects.equals(apiContext, apiThrottleReset.apiContext) &&
        Objects.equals(apiVersion, apiThrottleReset.apiVersion) &&
        Objects.equals(resource, apiThrottleReset.resource) &&
        Objects.equals(apiTier, apiThrottleReset.apiTier) &&
        Objects.equals(resourceTier, apiThrottleReset.resourceTier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(policyLevel, apiName, apiContext, apiVersion, resource, apiTier, resourceTier);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiThrottleResetDTO {\n");
    
    sb.append("    policyLevel: ").append(toIndentedString(policyLevel)).append("\n");
    sb.append("    apiName: ").append(toIndentedString(apiName)).append("\n");
    sb.append("    apiContext: ").append(toIndentedString(apiContext)).append("\n");
    sb.append("    apiVersion: ").append(toIndentedString(apiVersion)).append("\n");
    sb.append("    resource: ").append(toIndentedString(resource)).append("\n");
    sb.append("    apiTier: ").append(toIndentedString(apiTier)).append("\n");
    sb.append("    resourceTier: ").append(toIndentedString(resourceTier)).append("\n");
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

