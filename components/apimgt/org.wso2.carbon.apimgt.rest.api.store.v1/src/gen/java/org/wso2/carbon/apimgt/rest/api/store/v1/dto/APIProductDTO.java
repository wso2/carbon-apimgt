package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIProductBusinessInformationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIProductEndpointURLsDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;



public class APIProductDTO   {
  
    private String id = null;
    private String name = null;
    private String description = null;
    private String context = null;
    private String provider = null;
    private String apiDefinition = null;
    private List<String> tiers = new ArrayList<>();
    private String thumbnailUrl = null;
    private Map<String, String> additionalProperties = new HashMap<>();
    private List<APIProductEndpointURLsDTO> endpointURLs = new ArrayList<>();
    private APIProductBusinessInformationDTO businessInformation = null;
    private List<String> environmentList = new ArrayList<>();

  /**
   * UUID of the api product 
   **/
  public APIProductDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "UUID of the api product ")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Name of the API product
   **/
  public APIProductDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "CalculatorAPIProduct", required = true, value = "Name of the API product")
  @JsonProperty("name")
  @NotNull
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * A brief description about the API product
   **/
  public APIProductDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "A calculator API product that supports basic operations", value = "A brief description about the API product")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * A string that represents thecontext of the user&#39;s request
   **/
  public APIProductDTO context(String context) {
    this.context = context;
    return this;
  }

  
  @ApiModelProperty(example = "calc-prod", value = "A string that represents thecontext of the user's request")
  @JsonProperty("context")
  public String getContext() {
    return context;
  }
  public void setContext(String context) {
    this.context = context;
  }

  /**
   * If the provider value is not given user invoking the api will be used as the provider. 
   **/
  public APIProductDTO provider(String provider) {
    this.provider = provider;
    return this;
  }

  
  @ApiModelProperty(example = "admin", required = true, value = "If the provider value is not given user invoking the api will be used as the provider. ")
  @JsonProperty("provider")
  @NotNull
  public String getProvider() {
    return provider;
  }
  public void setProvider(String provider) {
    this.provider = provider;
  }

  /**
   * Swagger definition of the API product which contains details about URI templates and scopes 
   **/
  public APIProductDTO apiDefinition(String apiDefinition) {
    this.apiDefinition = apiDefinition;
    return this;
  }

  
  @ApiModelProperty(example = "", required = true, value = "Swagger definition of the API product which contains details about URI templates and scopes ")
  @JsonProperty("apiDefinition")
  @NotNull
  public String getApiDefinition() {
    return apiDefinition;
  }
  public void setApiDefinition(String apiDefinition) {
    this.apiDefinition = apiDefinition;
  }

  /**
   * The subscription tiers selected for the particular API product
   **/
  public APIProductDTO tiers(List<String> tiers) {
    this.tiers = tiers;
    return this;
  }

  
  @ApiModelProperty(example = "[\"Unlimited\"]", value = "The subscription tiers selected for the particular API product")
  @JsonProperty("tiers")
  public List<String> getTiers() {
    return tiers;
  }
  public void setTiers(List<String> tiers) {
    this.tiers = tiers;
  }

  /**
   **/
  public APIProductDTO thumbnailUrl(String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
    return this;
  }

  
  @ApiModelProperty(example = "", value = "")
  @JsonProperty("thumbnailUrl")
  public String getThumbnailUrl() {
    return thumbnailUrl;
  }
  public void setThumbnailUrl(String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
  }

  /**
   * Custom(user defined) properties of API product 
   **/
  public APIProductDTO additionalProperties(Map<String, String> additionalProperties) {
    this.additionalProperties = additionalProperties;
    return this;
  }

  
  @ApiModelProperty(example = "{}", value = "Custom(user defined) properties of API product ")
  @JsonProperty("additionalProperties")
  public Map<String, String> getAdditionalProperties() {
    return additionalProperties;
  }
  public void setAdditionalProperties(Map<String, String> additionalProperties) {
    this.additionalProperties = additionalProperties;
  }

  /**
   **/
  public APIProductDTO endpointURLs(List<APIProductEndpointURLsDTO> endpointURLs) {
    this.endpointURLs = endpointURLs;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("endpointURLs")
  public List<APIProductEndpointURLsDTO> getEndpointURLs() {
    return endpointURLs;
  }
  public void setEndpointURLs(List<APIProductEndpointURLsDTO> endpointURLs) {
    this.endpointURLs = endpointURLs;
  }

  /**
   **/
  public APIProductDTO businessInformation(APIProductBusinessInformationDTO businessInformation) {
    this.businessInformation = businessInformation;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("businessInformation")
  public APIProductBusinessInformationDTO getBusinessInformation() {
    return businessInformation;
  }
  public void setBusinessInformation(APIProductBusinessInformationDTO businessInformation) {
    this.businessInformation = businessInformation;
  }

  /**
   * The environment list configured with non empty endpoint URLs for the particular API.
   **/
  public APIProductDTO environmentList(List<String> environmentList) {
    this.environmentList = environmentList;
    return this;
  }

  
  @ApiModelProperty(example = "[\"PRODUCTION\",\"SANDBOX\"]", value = "The environment list configured with non empty endpoint URLs for the particular API.")
  @JsonProperty("environmentList")
  public List<String> getEnvironmentList() {
    return environmentList;
  }
  public void setEnvironmentList(List<String> environmentList) {
    this.environmentList = environmentList;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIProductDTO apIProduct = (APIProductDTO) o;
    return Objects.equals(id, apIProduct.id) &&
        Objects.equals(name, apIProduct.name) &&
        Objects.equals(description, apIProduct.description) &&
        Objects.equals(context, apIProduct.context) &&
        Objects.equals(provider, apIProduct.provider) &&
        Objects.equals(apiDefinition, apIProduct.apiDefinition) &&
        Objects.equals(tiers, apIProduct.tiers) &&
        Objects.equals(thumbnailUrl, apIProduct.thumbnailUrl) &&
        Objects.equals(additionalProperties, apIProduct.additionalProperties) &&
        Objects.equals(endpointURLs, apIProduct.endpointURLs) &&
        Objects.equals(businessInformation, apIProduct.businessInformation) &&
        Objects.equals(environmentList, apIProduct.environmentList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, context, provider, apiDefinition, tiers, thumbnailUrl, additionalProperties, endpointURLs, businessInformation, environmentList);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIProductDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    context: ").append(toIndentedString(context)).append("\n");
    sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
    sb.append("    apiDefinition: ").append(toIndentedString(apiDefinition)).append("\n");
    sb.append("    tiers: ").append(toIndentedString(tiers)).append("\n");
    sb.append("    thumbnailUrl: ").append(toIndentedString(thumbnailUrl)).append("\n");
    sb.append("    additionalProperties: ").append(toIndentedString(additionalProperties)).append("\n");
    sb.append("    endpointURLs: ").append(toIndentedString(endpointURLs)).append("\n");
    sb.append("    businessInformation: ").append(toIndentedString(businessInformation)).append("\n");
    sb.append("    environmentList: ").append(toIndentedString(environmentList)).append("\n");
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

