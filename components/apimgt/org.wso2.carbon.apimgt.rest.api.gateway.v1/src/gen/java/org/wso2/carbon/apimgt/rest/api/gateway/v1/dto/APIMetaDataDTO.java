package org.wso2.carbon.apimgt.rest.api.gateway.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class APIMetaDataDTO   {
  
    private String apiUUID = null;
    private Integer apiId = null;
    private String provider = null;
    private String name = null;
    private String version = null;
    private String context = null;
    private String policy = null;
    private String apiType = null;
    private Boolean isDefaultVersion = null;

  /**
   * UUID of API.
   **/
  public APIMetaDataDTO apiUUID(String apiUUID) {
    this.apiUUID = apiUUID;
    return this;
  }

  
  @ApiModelProperty(value = "UUID of API.")
  @JsonProperty("apiUUID")
  public String getApiUUID() {
    return apiUUID;
  }
  public void setApiUUID(String apiUUID) {
    this.apiUUID = apiUUID;
  }

  /**
   **/
  public APIMetaDataDTO apiId(Integer apiId) {
    this.apiId = apiId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("apiId")
  public Integer getApiId() {
    return apiId;
  }
  public void setApiId(Integer apiId) {
    this.apiId = apiId;
  }

  /**
   * API Provider name.
   **/
  public APIMetaDataDTO provider(String provider) {
    this.provider = provider;
    return this;
  }

  
  @ApiModelProperty(value = "API Provider name.")
  @JsonProperty("provider")
  public String getProvider() {
    return provider;
  }
  public void setProvider(String provider) {
    this.provider = provider;
  }

  /**
   * Name of the API
   **/
  public APIMetaDataDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(value = "Name of the API")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Version of the API.
   **/
  public APIMetaDataDTO version(String version) {
    this.version = version;
    return this;
  }

  
  @ApiModelProperty(value = "Version of the API.")
  @JsonProperty("version")
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * Context of the API.
   **/
  public APIMetaDataDTO context(String context) {
    this.context = context;
    return this;
  }

  
  @ApiModelProperty(value = "Context of the API.")
  @JsonProperty("context")
  public String getContext() {
    return context;
  }
  public void setContext(String context) {
    this.context = context;
  }

  /**
   * API level throttling policy.
   **/
  public APIMetaDataDTO policy(String policy) {
    this.policy = policy;
    return this;
  }

  
  @ApiModelProperty(value = "API level throttling policy.")
  @JsonProperty("policy")
  public String getPolicy() {
    return policy;
  }
  public void setPolicy(String policy) {
    this.policy = policy;
  }

  /**
   * Type of the API.
   **/
  public APIMetaDataDTO apiType(String apiType) {
    this.apiType = apiType;
    return this;
  }

  
  @ApiModelProperty(example = "APIProduct", value = "Type of the API.")
  @JsonProperty("apiType")
  public String getApiType() {
    return apiType;
  }
  public void setApiType(String apiType) {
    this.apiType = apiType;
  }

  /**
   * Whether this is the default version of the API.
   **/
  public APIMetaDataDTO isDefaultVersion(Boolean isDefaultVersion) {
    this.isDefaultVersion = isDefaultVersion;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "Whether this is the default version of the API.")
  @JsonProperty("isDefaultVersion")
  public Boolean isIsDefaultVersion() {
    return isDefaultVersion;
  }
  public void setIsDefaultVersion(Boolean isDefaultVersion) {
    this.isDefaultVersion = isDefaultVersion;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIMetaDataDTO apIMetaData = (APIMetaDataDTO) o;
    return Objects.equals(apiUUID, apIMetaData.apiUUID) &&
        Objects.equals(apiId, apIMetaData.apiId) &&
        Objects.equals(provider, apIMetaData.provider) &&
        Objects.equals(name, apIMetaData.name) &&
        Objects.equals(version, apIMetaData.version) &&
        Objects.equals(context, apIMetaData.context) &&
        Objects.equals(policy, apIMetaData.policy) &&
        Objects.equals(apiType, apIMetaData.apiType) &&
        Objects.equals(isDefaultVersion, apIMetaData.isDefaultVersion);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apiUUID, apiId, provider, name, version, context, policy, apiType, isDefaultVersion);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIMetaDataDTO {\n");
    
    sb.append("    apiUUID: ").append(toIndentedString(apiUUID)).append("\n");
    sb.append("    apiId: ").append(toIndentedString(apiId)).append("\n");
    sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    context: ").append(toIndentedString(context)).append("\n");
    sb.append("    policy: ").append(toIndentedString(policy)).append("\n");
    sb.append("    apiType: ").append(toIndentedString(apiType)).append("\n");
    sb.append("    isDefaultVersion: ").append(toIndentedString(isDefaultVersion)).append("\n");
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

