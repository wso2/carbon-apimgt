package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDefinitionSearchResultAllOfDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SearchResultDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class APIDefinitionSearchResultDTO extends SearchResultDTO  {
  
    private String apiName = null;
    private String apiVersion = null;
    private String apiContext = null;
    private String apiUUID = null;
    private String apiProvider = null;
    private String apiType = null;
    private String associatedType = null;

  /**
   * The name of the associated API
   **/
  public APIDefinitionSearchResultDTO apiName(String apiName) {
    this.apiName = apiName;
    return this;
  }

  
  @ApiModelProperty(example = "TestAPI", value = "The name of the associated API")
  @JsonProperty("apiName")
  public String getApiName() {
    return apiName;
  }
  public void setApiName(String apiName) {
    this.apiName = apiName;
  }

  /**
   * The version of the associated API
   **/
  public APIDefinitionSearchResultDTO apiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
    return this;
  }

  
  @ApiModelProperty(example = "1.0.0", value = "The version of the associated API")
  @JsonProperty("apiVersion")
  public String getApiVersion() {
    return apiVersion;
  }
  public void setApiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
  }

  /**
   * The context of the associated API
   **/
  public APIDefinitionSearchResultDTO apiContext(String apiContext) {
    this.apiContext = apiContext;
    return this;
  }

  
  @ApiModelProperty(example = "/test", value = "The context of the associated API")
  @JsonProperty("apiContext")
  public String getApiContext() {
    return apiContext;
  }
  public void setApiContext(String apiContext) {
    this.apiContext = apiContext;
  }

  /**
   * The UUID of the associated API
   **/
  public APIDefinitionSearchResultDTO apiUUID(String apiUUID) {
    this.apiUUID = apiUUID;
    return this;
  }

  
  @ApiModelProperty(value = "The UUID of the associated API")
  @JsonProperty("apiUUID")
  public String getApiUUID() {
    return apiUUID;
  }
  public void setApiUUID(String apiUUID) {
    this.apiUUID = apiUUID;
  }

  /**
   * The provider name of the associated API
   **/
  public APIDefinitionSearchResultDTO apiProvider(String apiProvider) {
    this.apiProvider = apiProvider;
    return this;
  }

  
  @ApiModelProperty(example = "publisher", value = "The provider name of the associated API")
  @JsonProperty("apiProvider")
  public String getApiProvider() {
    return apiProvider;
  }
  public void setApiProvider(String apiProvider) {
    this.apiProvider = apiProvider;
  }

  /**
   * The type of the associated API
   **/
  public APIDefinitionSearchResultDTO apiType(String apiType) {
    this.apiType = apiType;
    return this;
  }

  
  @ApiModelProperty(example = "REST", value = "The type of the associated API")
  @JsonProperty("apiType")
  public String getApiType() {
    return apiType;
  }
  public void setApiType(String apiType) {
    this.apiType = apiType;
  }

  /**
   * API or APIProduct
   **/
  public APIDefinitionSearchResultDTO associatedType(String associatedType) {
    this.associatedType = associatedType;
    return this;
  }

  
  @ApiModelProperty(example = "API", value = "API or APIProduct")
  @JsonProperty("associatedType")
  public String getAssociatedType() {
    return associatedType;
  }
  public void setAssociatedType(String associatedType) {
    this.associatedType = associatedType;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIDefinitionSearchResultDTO apIDefinitionSearchResult = (APIDefinitionSearchResultDTO) o;
    return Objects.equals(apiName, apIDefinitionSearchResult.apiName) &&
        Objects.equals(apiVersion, apIDefinitionSearchResult.apiVersion) &&
        Objects.equals(apiContext, apIDefinitionSearchResult.apiContext) &&
        Objects.equals(apiUUID, apIDefinitionSearchResult.apiUUID) &&
        Objects.equals(apiProvider, apIDefinitionSearchResult.apiProvider) &&
        Objects.equals(apiType, apIDefinitionSearchResult.apiType) &&
        Objects.equals(associatedType, apIDefinitionSearchResult.associatedType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apiName, apiVersion, apiContext, apiUUID, apiProvider, apiType, associatedType);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIDefinitionSearchResultDTO {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    apiName: ").append(toIndentedString(apiName)).append("\n");
    sb.append("    apiVersion: ").append(toIndentedString(apiVersion)).append("\n");
    sb.append("    apiContext: ").append(toIndentedString(apiContext)).append("\n");
    sb.append("    apiUUID: ").append(toIndentedString(apiUUID)).append("\n");
    sb.append("    apiProvider: ").append(toIndentedString(apiProvider)).append("\n");
    sb.append("    apiType: ").append(toIndentedString(apiType)).append("\n");
    sb.append("    associatedType: ").append(toIndentedString(associatedType)).append("\n");
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

