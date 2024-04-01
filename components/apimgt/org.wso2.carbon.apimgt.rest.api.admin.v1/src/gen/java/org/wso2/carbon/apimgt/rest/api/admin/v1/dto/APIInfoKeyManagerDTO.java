package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

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



public class APIInfoKeyManagerDTO   {
  
    private String id = null;
    private String type = null;
    private String name = null;
    private String transportType = null;
    private String description = null;
    private String context = null;
    private String version = null;
    private String provider = null;
    private String status = null;
    private String thumbnailUri = null;
    private Boolean advertiseOnly = null;
    private String keyManagerEntry = null;

  /**
   * The ID of the API.
   **/
  public APIInfoKeyManagerDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The ID of the API.")
  @JsonProperty("id")
  @NotNull
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   * The type of the entry (e.g., \&quot;API\&quot;).
   **/
  public APIInfoKeyManagerDTO type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(value = "The type of the entry (e.g., \"API\").")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  /**
   * The name of the API.
   **/
  public APIInfoKeyManagerDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The name of the API.")
  @JsonProperty("name")
  @NotNull
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * The transport type of the API.
   **/
  public APIInfoKeyManagerDTO transportType(String transportType) {
    this.transportType = transportType;
    return this;
  }

  
  @ApiModelProperty(value = "The transport type of the API.")
  @JsonProperty("transportType")
  public String getTransportType() {
    return transportType;
  }
  public void setTransportType(String transportType) {
    this.transportType = transportType;
  }

  /**
   * The description of the API.
   **/
  public APIInfoKeyManagerDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(value = "The description of the API.")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * The context of the API.
   **/
  public APIInfoKeyManagerDTO context(String context) {
    this.context = context;
    return this;
  }

  
  @ApiModelProperty(value = "The context of the API.")
  @JsonProperty("context")
  public String getContext() {
    return context;
  }
  public void setContext(String context) {
    this.context = context;
  }

  /**
   * The version of the API.
   **/
  public APIInfoKeyManagerDTO version(String version) {
    this.version = version;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The version of the API.")
  @JsonProperty("version")
  @NotNull
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * The provider of the API.
   **/
  public APIInfoKeyManagerDTO provider(String provider) {
    this.provider = provider;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The provider of the API.")
  @JsonProperty("provider")
  @NotNull
  public String getProvider() {
    return provider;
  }
  public void setProvider(String provider) {
    this.provider = provider;
  }

  /**
   * The status of the API.
   **/
  public APIInfoKeyManagerDTO status(String status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(value = "The status of the API.")
  @JsonProperty("status")
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * The URI of the thumbnail of the API.
   **/
  public APIInfoKeyManagerDTO thumbnailUri(String thumbnailUri) {
    this.thumbnailUri = thumbnailUri;
    return this;
  }

  
  @ApiModelProperty(value = "The URI of the thumbnail of the API.")
  @JsonProperty("thumbnailUri")
  public String getThumbnailUri() {
    return thumbnailUri;
  }
  public void setThumbnailUri(String thumbnailUri) {
    this.thumbnailUri = thumbnailUri;
  }

  /**
   * Indicates if the API is advertised only.
   **/
  public APIInfoKeyManagerDTO advertiseOnly(Boolean advertiseOnly) {
    this.advertiseOnly = advertiseOnly;
    return this;
  }

  
  @ApiModelProperty(value = "Indicates if the API is advertised only.")
  @JsonProperty("advertiseOnly")
  public Boolean isAdvertiseOnly() {
    return advertiseOnly;
  }
  public void setAdvertiseOnly(Boolean advertiseOnly) {
    this.advertiseOnly = advertiseOnly;
  }

  /**
   * The key manager entry related to the API.
   **/
  public APIInfoKeyManagerDTO keyManagerEntry(String keyManagerEntry) {
    this.keyManagerEntry = keyManagerEntry;
    return this;
  }

  
  @ApiModelProperty(value = "The key manager entry related to the API.")
  @JsonProperty("keyManagerEntry")
  public String getKeyManagerEntry() {
    return keyManagerEntry;
  }
  public void setKeyManagerEntry(String keyManagerEntry) {
    this.keyManagerEntry = keyManagerEntry;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIInfoKeyManagerDTO apIInfoKeyManager = (APIInfoKeyManagerDTO) o;
    return Objects.equals(id, apIInfoKeyManager.id) &&
        Objects.equals(type, apIInfoKeyManager.type) &&
        Objects.equals(name, apIInfoKeyManager.name) &&
        Objects.equals(transportType, apIInfoKeyManager.transportType) &&
        Objects.equals(description, apIInfoKeyManager.description) &&
        Objects.equals(context, apIInfoKeyManager.context) &&
        Objects.equals(version, apIInfoKeyManager.version) &&
        Objects.equals(provider, apIInfoKeyManager.provider) &&
        Objects.equals(status, apIInfoKeyManager.status) &&
        Objects.equals(thumbnailUri, apIInfoKeyManager.thumbnailUri) &&
        Objects.equals(advertiseOnly, apIInfoKeyManager.advertiseOnly) &&
        Objects.equals(keyManagerEntry, apIInfoKeyManager.keyManagerEntry);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, type, name, transportType, description, context, version, provider, status, thumbnailUri, advertiseOnly, keyManagerEntry);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIInfoKeyManagerDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    transportType: ").append(toIndentedString(transportType)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    context: ").append(toIndentedString(context)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    thumbnailUri: ").append(toIndentedString(thumbnailUri)).append("\n");
    sb.append("    advertiseOnly: ").append(toIndentedString(advertiseOnly)).append("\n");
    sb.append("    keyManagerEntry: ").append(toIndentedString(keyManagerEntry)).append("\n");
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

