package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class APIProductInfoDTO   {
  
    private String id = null;
    private String name = null;
    private String description = null;
    private String provider = null;
    private String thumbnailUri = null;
    private List<String> throttlingPolicies = new ArrayList<>();

  /**
   **/
  public APIProductInfoDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public APIProductInfoDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "CalculatorAPIProduct", value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public APIProductInfoDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "A calculator API product that supports basic operations", value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * If the provider value is not given, the user invoking the API will be used as the provider. 
   **/
  public APIProductInfoDTO provider(String provider) {
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
   **/
  public APIProductInfoDTO thumbnailUri(String thumbnailUri) {
    this.thumbnailUri = thumbnailUri;
    return this;
  }

  
  @ApiModelProperty(example = "/api-products/01234567-0123-0123-0123-012345678901/thumbnail", value = "")
  @JsonProperty("thumbnailUri")
  public String getThumbnailUri() {
    return thumbnailUri;
  }
  public void setThumbnailUri(String thumbnailUri) {
    this.thumbnailUri = thumbnailUri;
  }

  /**
   * The subscription tiers selected for the particular API product
   **/
  public APIProductInfoDTO throttlingPolicies(List<String> throttlingPolicies) {
    this.throttlingPolicies = throttlingPolicies;
    return this;
  }

  
  @ApiModelProperty(example = "[\"Unlimited\"]", value = "The subscription tiers selected for the particular API product")
  @JsonProperty("throttlingPolicies")
  public List<String> getThrottlingPolicies() {
    return throttlingPolicies;
  }
  public void setThrottlingPolicies(List<String> throttlingPolicies) {
    this.throttlingPolicies = throttlingPolicies;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIProductInfoDTO apIProductInfo = (APIProductInfoDTO) o;
    return Objects.equals(id, apIProductInfo.id) &&
        Objects.equals(name, apIProductInfo.name) &&
        Objects.equals(description, apIProductInfo.description) &&
        Objects.equals(provider, apIProductInfo.provider) &&
        Objects.equals(thumbnailUri, apIProductInfo.thumbnailUri) &&
        Objects.equals(throttlingPolicies, apIProductInfo.throttlingPolicies);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, provider, thumbnailUri, throttlingPolicies);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIProductInfoDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
    sb.append("    thumbnailUri: ").append(toIndentedString(thumbnailUri)).append("\n");
    sb.append("    throttlingPolicies: ").append(toIndentedString(throttlingPolicies)).append("\n");
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

