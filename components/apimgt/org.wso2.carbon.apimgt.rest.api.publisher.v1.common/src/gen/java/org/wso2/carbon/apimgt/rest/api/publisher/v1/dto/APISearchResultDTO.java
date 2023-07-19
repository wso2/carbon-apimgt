package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APISearchResultAllOfDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SearchResultDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class APISearchResultDTO extends SearchResultDTO  {
  
    private String description = null;
    private String context = null;
    private String contextTemplate = null;
    private String version = null;
    private String provider = null;
    private String status = null;
    private String thumbnailUri = null;
    private Boolean advertiseOnly = null;
    private Boolean hasThumbnail = null;

  /**
   * A brief description about the API
   **/
  public APISearchResultDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "A calculator API that supports basic operations", value = "A brief description about the API")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * A string that represents the context of the user&#39;s request
   **/
  public APISearchResultDTO context(String context) {
    this.context = context;
    return this;
  }

  
  @ApiModelProperty(example = "CalculatorAPI", value = "A string that represents the context of the user's request")
  @JsonProperty("context")
  public String getContext() {
    return context;
  }
  public void setContext(String context) {
    this.context = context;
  }

  /**
   * The templated context of the API
   **/
  public APISearchResultDTO contextTemplate(String contextTemplate) {
    this.contextTemplate = contextTemplate;
    return this;
  }

  
  @ApiModelProperty(example = "CalculatorAPI/{version}", value = "The templated context of the API")
  @JsonProperty("contextTemplate")
  public String getContextTemplate() {
    return contextTemplate;
  }
  public void setContextTemplate(String contextTemplate) {
    this.contextTemplate = contextTemplate;
  }

  /**
   * The version of the API
   **/
  public APISearchResultDTO version(String version) {
    this.version = version;
    return this;
  }

  
  @ApiModelProperty(example = "1.0.0", value = "The version of the API")
  @JsonProperty("version")
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * If the provider value is not given, the user invoking the API will be used as the provider. 
   **/
  public APISearchResultDTO provider(String provider) {
    this.provider = provider;
    return this;
  }

  
  @ApiModelProperty(example = "admin", value = "If the provider value is not given, the user invoking the API will be used as the provider. ")
  @JsonProperty("provider")
  public String getProvider() {
    return provider;
  }
  public void setProvider(String provider) {
    this.provider = provider;
  }

  /**
   * This describes in which status of the lifecycle the API is
   **/
  public APISearchResultDTO status(String status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(example = "CREATED", value = "This describes in which status of the lifecycle the API is")
  @JsonProperty("status")
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   **/
  public APISearchResultDTO thumbnailUri(String thumbnailUri) {
    this.thumbnailUri = thumbnailUri;
    return this;
  }

  
  @ApiModelProperty(example = "/apis/01234567-0123-0123-0123-012345678901/thumbnail", value = "")
  @JsonProperty("thumbnailUri")
  public String getThumbnailUri() {
    return thumbnailUri;
  }
  public void setThumbnailUri(String thumbnailUri) {
    this.thumbnailUri = thumbnailUri;
  }

  /**
   **/
  public APISearchResultDTO advertiseOnly(Boolean advertiseOnly) {
    this.advertiseOnly = advertiseOnly;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "")
  @JsonProperty("advertiseOnly")
  public Boolean isAdvertiseOnly() {
    return advertiseOnly;
  }
  public void setAdvertiseOnly(Boolean advertiseOnly) {
    this.advertiseOnly = advertiseOnly;
  }

  /**
   **/
  public APISearchResultDTO hasThumbnail(Boolean hasThumbnail) {
    this.hasThumbnail = hasThumbnail;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "")
  @JsonProperty("hasThumbnail")
  public Boolean isHasThumbnail() {
    return hasThumbnail;
  }
  public void setHasThumbnail(Boolean hasThumbnail) {
    this.hasThumbnail = hasThumbnail;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APISearchResultDTO apISearchResult = (APISearchResultDTO) o;
    return Objects.equals(description, apISearchResult.description) &&
        Objects.equals(context, apISearchResult.context) &&
        Objects.equals(contextTemplate, apISearchResult.contextTemplate) &&
        Objects.equals(version, apISearchResult.version) &&
        Objects.equals(provider, apISearchResult.provider) &&
        Objects.equals(status, apISearchResult.status) &&
        Objects.equals(thumbnailUri, apISearchResult.thumbnailUri) &&
        Objects.equals(advertiseOnly, apISearchResult.advertiseOnly) &&
        Objects.equals(hasThumbnail, apISearchResult.hasThumbnail);
  }

  @Override
  public int hashCode() {
    return Objects.hash(description, context, contextTemplate, version, provider, status, thumbnailUri, advertiseOnly, hasThumbnail);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APISearchResultDTO {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    context: ").append(toIndentedString(context)).append("\n");
    sb.append("    contextTemplate: ").append(toIndentedString(contextTemplate)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    thumbnailUri: ").append(toIndentedString(thumbnailUri)).append("\n");
    sb.append("    advertiseOnly: ").append(toIndentedString(advertiseOnly)).append("\n");
    sb.append("    hasThumbnail: ").append(toIndentedString(hasThumbnail)).append("\n");
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

