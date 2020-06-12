package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIBusinessInformationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIEndpointURLsDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIMonetizationInfoDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIOperationsDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APITiersDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AdvertiseInfoDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.LabelDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ScopeInfoDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class APIDTO   {
  
    private String id = null;
    private String name = null;
    private String description = null;
    private String context = null;
    private String version = null;
    private String provider = null;
    private String apiDefinition = null;
    private String wsdlUri = null;
    private String lifeCycleStatus = null;
    private Boolean isDefaultVersion = null;
    private String type = null;
    private List<String> transport = new ArrayList<>();
    private List<APIOperationsDTO> operations = new ArrayList<>();
    private String authorizationHeader = null;
    private List<String> securityScheme = new ArrayList<>();
    private List<String> tags = new ArrayList<>();
    private List<APITiersDTO> tiers = new ArrayList<>();
    private Boolean hasThumbnail = false;
    private Map<String, String> additionalProperties = new HashMap<>();
    private APIMonetizationInfoDTO monetization = null;
    private List<APIEndpointURLsDTO> endpointURLs = new ArrayList<>();
    private APIBusinessInformationDTO businessInformation = null;
    private List<LabelDTO> labels = new ArrayList<>();
    private List<String> environmentList = new ArrayList<>();
    private List<ScopeInfoDTO> scopes = new ArrayList<>();
    private String avgRating = null;
    private AdvertiseInfoDTO advertiseInfo = null;
    private Boolean isSubscriptionAvailable = null;
    private List<String> categories = new ArrayList<>();

  /**
   * UUID of the api 
   **/
  public APIDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "UUID of the api ")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Name of the API
   **/
  public APIDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "CalculatorAPI", required = true, value = "Name of the API")
  @JsonProperty("name")
  @NotNull
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * A brief description about the API
   **/
  public APIDTO description(String description) {
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
   * A string that represents thecontext of the user&#39;s request
   **/
  public APIDTO context(String context) {
    this.context = context;
    return this;
  }

  
  @ApiModelProperty(example = "CalculatorAPI", required = true, value = "A string that represents thecontext of the user's request")
  @JsonProperty("context")
  @NotNull
  public String getContext() {
    return context;
  }
  public void setContext(String context) {
    this.context = context;
  }

  /**
   * The version of the API
   **/
  public APIDTO version(String version) {
    this.version = version;
    return this;
  }

  
  @ApiModelProperty(example = "1.0.0", required = true, value = "The version of the API")
  @JsonProperty("version")
  @NotNull
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * If the provider value is not given user invoking the api will be used as the provider. 
   **/
  public APIDTO provider(String provider) {
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
   * Swagger definition of the API which contains details about URI templates and scopes 
   **/
  public APIDTO apiDefinition(String apiDefinition) {
    this.apiDefinition = apiDefinition;
    return this;
  }

  
  @ApiModelProperty(example = "{\"paths\":{\"/substract\":{\"get\":{\"x-auth-type\":\"Application & Application User\",\"x-throttling-tier\":\"Unlimited\",\"parameters\":[{\"name\":\"x\",\"required\":true,\"type\":\"string\",\"in\":\"query\"},{\"name\":\"y\",\"required\":true,\"type\":\"string\",\"in\":\"query\"}],\"responses\":{\"200\":{}}}},\"/add\":{\"get\":{\"x-auth-type\":\"Application & Application User\",\"x-throttling-tier\":\"Unlimited\",\"parameters\":[{\"name\":\"x\",\"required\":true,\"type\":\"string\",\"in\":\"query\"},{\"name\":\"y\",\"required\":true,\"type\":\"string\",\"in\":\"query\"}],\"responses\":{\"200\":{}}}}},\"swagger\":\"2.0\",\"info\":{\"title\":\"CalculatorAPI\",\"version\":\"1.0.0\"}}", value = "Swagger definition of the API which contains details about URI templates and scopes ")
  @JsonProperty("apiDefinition")
  public String getApiDefinition() {
    return apiDefinition;
  }
  public void setApiDefinition(String apiDefinition) {
    this.apiDefinition = apiDefinition;
  }

  /**
   * WSDL URL if the API is based on a WSDL endpoint 
   **/
  public APIDTO wsdlUri(String wsdlUri) {
    this.wsdlUri = wsdlUri;
    return this;
  }

  
  @ApiModelProperty(example = "http://www.webservicex.com/globalweather.asmx?wsdl", value = "WSDL URL if the API is based on a WSDL endpoint ")
  @JsonProperty("wsdlUri")
  public String getWsdlUri() {
    return wsdlUri;
  }
  public void setWsdlUri(String wsdlUri) {
    this.wsdlUri = wsdlUri;
  }

  /**
   * This describes in which status of the lifecycle the API is.
   **/
  public APIDTO lifeCycleStatus(String lifeCycleStatus) {
    this.lifeCycleStatus = lifeCycleStatus;
    return this;
  }

  
  @ApiModelProperty(example = "PUBLISHED", required = true, value = "This describes in which status of the lifecycle the API is.")
  @JsonProperty("lifeCycleStatus")
  @NotNull
  public String getLifeCycleStatus() {
    return lifeCycleStatus;
  }
  public void setLifeCycleStatus(String lifeCycleStatus) {
    this.lifeCycleStatus = lifeCycleStatus;
  }

  /**
   **/
  public APIDTO isDefaultVersion(Boolean isDefaultVersion) {
    this.isDefaultVersion = isDefaultVersion;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "")
  @JsonProperty("isDefaultVersion")
  public Boolean isIsDefaultVersion() {
    return isDefaultVersion;
  }
  public void setIsDefaultVersion(Boolean isDefaultVersion) {
    this.isDefaultVersion = isDefaultVersion;
  }

  /**
   * This describes the transport type of the API
   **/
  public APIDTO type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(example = "WS", value = "This describes the transport type of the API")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  /**
   **/
  public APIDTO transport(List<String> transport) {
    this.transport = transport;
    return this;
  }

  
  @ApiModelProperty(example = "[\"http\",\"https\"]", value = "")
  @JsonProperty("transport")
  public List<String> getTransport() {
    return transport;
  }
  public void setTransport(List<String> transport) {
    this.transport = transport;
  }

  /**
   **/
  public APIDTO operations(List<APIOperationsDTO> operations) {
    this.operations = operations;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("operations")
  public List<APIOperationsDTO> getOperations() {
    return operations;
  }
  public void setOperations(List<APIOperationsDTO> operations) {
    this.operations = operations;
  }

  /**
   * Name of the Authorization header used for invoking the API. If it is not set, Authorization header name specified in tenant or system level will be used. 
   **/
  public APIDTO authorizationHeader(String authorizationHeader) {
    this.authorizationHeader = authorizationHeader;
    return this;
  }

  
  @ApiModelProperty(value = "Name of the Authorization header used for invoking the API. If it is not set, Authorization header name specified in tenant or system level will be used. ")
  @JsonProperty("authorizationHeader")
  public String getAuthorizationHeader() {
    return authorizationHeader;
  }
  public void setAuthorizationHeader(String authorizationHeader) {
    this.authorizationHeader = authorizationHeader;
  }

  /**
   * Types of API security, the current API secured with. It can be either OAuth2 or mutual SSL or both. If it is not set OAuth2 will be set as the security for the current API. 
   **/
  public APIDTO securityScheme(List<String> securityScheme) {
    this.securityScheme = securityScheme;
    return this;
  }

  
  @ApiModelProperty(value = "Types of API security, the current API secured with. It can be either OAuth2 or mutual SSL or both. If it is not set OAuth2 will be set as the security for the current API. ")
  @JsonProperty("securityScheme")
  public List<String> getSecurityScheme() {
    return securityScheme;
  }
  public void setSecurityScheme(List<String> securityScheme) {
    this.securityScheme = securityScheme;
  }

  /**
   * Search keywords related to the API
   **/
  public APIDTO tags(List<String> tags) {
    this.tags = tags;
    return this;
  }

  
  @ApiModelProperty(example = "[\"substract\",\"add\"]", value = "Search keywords related to the API")
  @JsonProperty("tags")
  public List<String> getTags() {
    return tags;
  }
  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  /**
   * The subscription tiers selected for the particular API
   **/
  public APIDTO tiers(List<APITiersDTO> tiers) {
    this.tiers = tiers;
    return this;
  }

  
  @ApiModelProperty(value = "The subscription tiers selected for the particular API")
  @JsonProperty("tiers")
  public List<APITiersDTO> getTiers() {
    return tiers;
  }
  public void setTiers(List<APITiersDTO> tiers) {
    this.tiers = tiers;
  }

  /**
   **/
  public APIDTO hasThumbnail(Boolean hasThumbnail) {
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

  /**
   * Custom(user defined) properties of API 
   **/
  public APIDTO additionalProperties(Map<String, String> additionalProperties) {
    this.additionalProperties = additionalProperties;
    return this;
  }

  
  @ApiModelProperty(example = "{}", value = "Custom(user defined) properties of API ")
  @JsonProperty("additionalProperties")
  public Map<String, String> getAdditionalProperties() {
    return additionalProperties;
  }
  public void setAdditionalProperties(Map<String, String> additionalProperties) {
    this.additionalProperties = additionalProperties;
  }

  /**
   **/
  public APIDTO monetization(APIMonetizationInfoDTO monetization) {
    this.monetization = monetization;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("monetization")
  public APIMonetizationInfoDTO getMonetization() {
    return monetization;
  }
  public void setMonetization(APIMonetizationInfoDTO monetization) {
    this.monetization = monetization;
  }

  /**
   **/
  public APIDTO endpointURLs(List<APIEndpointURLsDTO> endpointURLs) {
    this.endpointURLs = endpointURLs;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("endpointURLs")
  public List<APIEndpointURLsDTO> getEndpointURLs() {
    return endpointURLs;
  }
  public void setEndpointURLs(List<APIEndpointURLsDTO> endpointURLs) {
    this.endpointURLs = endpointURLs;
  }

  /**
   **/
  public APIDTO businessInformation(APIBusinessInformationDTO businessInformation) {
    this.businessInformation = businessInformation;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("businessInformation")
  public APIBusinessInformationDTO getBusinessInformation() {
    return businessInformation;
  }
  public void setBusinessInformation(APIBusinessInformationDTO businessInformation) {
    this.businessInformation = businessInformation;
  }

  /**
   * Labels of micro-gateway environments attached to the API. 
   **/
  public APIDTO labels(List<LabelDTO> labels) {
    this.labels = labels;
    return this;
  }

  
  @ApiModelProperty(value = "Labels of micro-gateway environments attached to the API. ")
  @JsonProperty("labels")
  public List<LabelDTO> getLabels() {
    return labels;
  }
  public void setLabels(List<LabelDTO> labels) {
    this.labels = labels;
  }

  /**
   * The environment list configured with non empty endpoint URLs for the particular API.
   **/
  public APIDTO environmentList(List<String> environmentList) {
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

  /**
   **/
  public APIDTO scopes(List<ScopeInfoDTO> scopes) {
    this.scopes = scopes;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("scopes")
  public List<ScopeInfoDTO> getScopes() {
    return scopes;
  }
  public void setScopes(List<ScopeInfoDTO> scopes) {
    this.scopes = scopes;
  }

  /**
   * The average rating of the API
   **/
  public APIDTO avgRating(String avgRating) {
    this.avgRating = avgRating;
    return this;
  }

  
  @ApiModelProperty(example = "4.5", value = "The average rating of the API")
  @JsonProperty("avgRating")
  public String getAvgRating() {
    return avgRating;
  }
  public void setAvgRating(String avgRating) {
    this.avgRating = avgRating;
  }

  /**
   * The advertise info of the API
   **/
  public APIDTO advertiseInfo(AdvertiseInfoDTO advertiseInfo) {
    this.advertiseInfo = advertiseInfo;
    return this;
  }

  
  @ApiModelProperty(value = "The advertise info of the API")
  @JsonProperty("advertiseInfo")
  public AdvertiseInfoDTO getAdvertiseInfo() {
    return advertiseInfo;
  }
  public void setAdvertiseInfo(AdvertiseInfoDTO advertiseInfo) {
    this.advertiseInfo = advertiseInfo;
  }

  /**
   **/
  public APIDTO isSubscriptionAvailable(Boolean isSubscriptionAvailable) {
    this.isSubscriptionAvailable = isSubscriptionAvailable;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "")
  @JsonProperty("isSubscriptionAvailable")
  public Boolean isIsSubscriptionAvailable() {
    return isSubscriptionAvailable;
  }
  public void setIsSubscriptionAvailable(Boolean isSubscriptionAvailable) {
    this.isSubscriptionAvailable = isSubscriptionAvailable;
  }

  /**
   * API categories 
   **/
  public APIDTO categories(List<String> categories) {
    this.categories = categories;
    return this;
  }

  
  @ApiModelProperty(value = "API categories ")
  @JsonProperty("categories")
  public List<String> getCategories() {
    return categories;
  }
  public void setCategories(List<String> categories) {
    this.categories = categories;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIDTO API = (APIDTO) o;
    return Objects.equals(id, API.id) &&
        Objects.equals(name, API.name) &&
        Objects.equals(description, API.description) &&
        Objects.equals(context, API.context) &&
        Objects.equals(version, API.version) &&
        Objects.equals(provider, API.provider) &&
        Objects.equals(apiDefinition, API.apiDefinition) &&
        Objects.equals(wsdlUri, API.wsdlUri) &&
        Objects.equals(lifeCycleStatus, API.lifeCycleStatus) &&
        Objects.equals(isDefaultVersion, API.isDefaultVersion) &&
        Objects.equals(type, API.type) &&
        Objects.equals(transport, API.transport) &&
        Objects.equals(operations, API.operations) &&
        Objects.equals(authorizationHeader, API.authorizationHeader) &&
        Objects.equals(securityScheme, API.securityScheme) &&
        Objects.equals(tags, API.tags) &&
        Objects.equals(tiers, API.tiers) &&
        Objects.equals(hasThumbnail, API.hasThumbnail) &&
        Objects.equals(additionalProperties, API.additionalProperties) &&
        Objects.equals(monetization, API.monetization) &&
        Objects.equals(endpointURLs, API.endpointURLs) &&
        Objects.equals(businessInformation, API.businessInformation) &&
        Objects.equals(labels, API.labels) &&
        Objects.equals(environmentList, API.environmentList) &&
        Objects.equals(scopes, API.scopes) &&
        Objects.equals(avgRating, API.avgRating) &&
        Objects.equals(advertiseInfo, API.advertiseInfo) &&
        Objects.equals(isSubscriptionAvailable, API.isSubscriptionAvailable) &&
        Objects.equals(categories, API.categories);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, context, version, provider, apiDefinition, wsdlUri, lifeCycleStatus, isDefaultVersion, type, transport, operations, authorizationHeader, securityScheme, tags, tiers, hasThumbnail, additionalProperties, monetization, endpointURLs, businessInformation, labels, environmentList, scopes, avgRating, advertiseInfo, isSubscriptionAvailable, categories);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    context: ").append(toIndentedString(context)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
    sb.append("    apiDefinition: ").append(toIndentedString(apiDefinition)).append("\n");
    sb.append("    wsdlUri: ").append(toIndentedString(wsdlUri)).append("\n");
    sb.append("    lifeCycleStatus: ").append(toIndentedString(lifeCycleStatus)).append("\n");
    sb.append("    isDefaultVersion: ").append(toIndentedString(isDefaultVersion)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    transport: ").append(toIndentedString(transport)).append("\n");
    sb.append("    operations: ").append(toIndentedString(operations)).append("\n");
    sb.append("    authorizationHeader: ").append(toIndentedString(authorizationHeader)).append("\n");
    sb.append("    securityScheme: ").append(toIndentedString(securityScheme)).append("\n");
    sb.append("    tags: ").append(toIndentedString(tags)).append("\n");
    sb.append("    tiers: ").append(toIndentedString(tiers)).append("\n");
    sb.append("    hasThumbnail: ").append(toIndentedString(hasThumbnail)).append("\n");
    sb.append("    additionalProperties: ").append(toIndentedString(additionalProperties)).append("\n");
    sb.append("    monetization: ").append(toIndentedString(monetization)).append("\n");
    sb.append("    endpointURLs: ").append(toIndentedString(endpointURLs)).append("\n");
    sb.append("    businessInformation: ").append(toIndentedString(businessInformation)).append("\n");
    sb.append("    labels: ").append(toIndentedString(labels)).append("\n");
    sb.append("    environmentList: ").append(toIndentedString(environmentList)).append("\n");
    sb.append("    scopes: ").append(toIndentedString(scopes)).append("\n");
    sb.append("    avgRating: ").append(toIndentedString(avgRating)).append("\n");
    sb.append("    advertiseInfo: ").append(toIndentedString(advertiseInfo)).append("\n");
    sb.append("    isSubscriptionAvailable: ").append(toIndentedString(isSubscriptionAvailable)).append("\n");
    sb.append("    categories: ").append(toIndentedString(categories)).append("\n");
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

