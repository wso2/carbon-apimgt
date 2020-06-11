package org.wso2.carbon.apimgt.rest.api.store.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIBusinessInformationDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIEndpointURLsDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.LabelDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class APIDTO  {
  
  
  
  private String id = null;
  
  @NotNull
  private String name = null;
  
  
  private String description = null;
  
  @NotNull
  private String context = null;
  
  @NotNull
  private String version = null;
  
  @NotNull
  private String provider = null;
  
  @NotNull
  private String apiDefinition = null;
  
  
  private String wsdlUri = null;
  
  @NotNull
  private String status = null;
  
  
  private Boolean isDefaultVersion = null;
  
  
  private List<String> transport = new ArrayList<String>();
  
  
  private String authorizationHeader = null;
  
  
  private String apiSecurity = null;
  
  
  private List<String> tags = new ArrayList<String>();
  
  
  private List<String> tiers = new ArrayList<String>();
  
  
  private String thumbnailUrl = null;
  
  
  private Map<String, String> additionalProperties = new HashMap<String, String>();
  
  
  private List<APIEndpointURLsDTO> endpointURLs = new ArrayList<APIEndpointURLsDTO>();
  
  
  private APIBusinessInformationDTO businessInformation = null;
  
  
  private List<LabelDTO> labels = new ArrayList<LabelDTO>();
  
  
  private List<String> environmentList = new ArrayList<String>();

  private String lastUpdatedTime = null;

  private String createdTime = null;

  /**
  * gets and sets the lastUpdatedTime for APIDTO
  **/
  @JsonIgnore
  public String getLastUpdatedTime(){
    return lastUpdatedTime;
  }
  public void setLastUpdatedTime(String lastUpdatedTime){
    this.lastUpdatedTime=lastUpdatedTime;
  }

  /**
  * gets and sets the createdTime for a APIDTO
  **/

  @JsonIgnore
  public String getCreatedTime(){
    return createdTime;
  }
  public void setCreatedTime(String createdTime){
    this.createdTime=createdTime;
  }

  
  /**
   * UUID of the api registry artifact\n
   **/
  @ApiModelProperty(value = "UUID of the api registry artifact\n")
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
  @ApiModelProperty(required = true, value = "Name of the API")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  
  /**
   * A brief description about the API
   **/
  @ApiModelProperty(value = "A brief description about the API")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  
  /**
   * A string that represents thecontext of the user's request
   **/
  @ApiModelProperty(required = true, value = "A string that represents thecontext of the user's request")
  @JsonProperty("context")
  public String getContext() {
    return context;
  }
  public void setContext(String context) {
    this.context = context;
  }

  
  /**
   * The version of the API
   **/
  @ApiModelProperty(required = true, value = "The version of the API")
  @JsonProperty("version")
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  
  /**
   * If the provider value is not given user invoking the api will be used as the provider.\n
   **/
  @ApiModelProperty(required = true, value = "If the provider value is not given user invoking the api will be used as the provider.\n")
  @JsonProperty("provider")
  public String getProvider() {
    return provider;
  }
  public void setProvider(String provider) {
    this.provider = provider;
  }

  
  /**
   * Swagger definition of the API which contains details about URI templates and scopes\n
   **/
  @ApiModelProperty(required = true, value = "Swagger definition of the API which contains details about URI templates and scopes\n")
  @JsonProperty("apiDefinition")
  public String getApiDefinition() {
    return apiDefinition;
  }
  public void setApiDefinition(String apiDefinition) {
    this.apiDefinition = apiDefinition;
  }

  
  /**
   * WSDL URL if the API is based on a WSDL endpoint\n
   **/
  @ApiModelProperty(value = "WSDL URL if the API is based on a WSDL endpoint\n")
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
  @ApiModelProperty(required = true, value = "This describes in which status of the lifecycle the API is.")
  @JsonProperty("status")
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("isDefaultVersion")
  public Boolean getIsDefaultVersion() {
    return isDefaultVersion;
  }
  public void setIsDefaultVersion(Boolean isDefaultVersion) {
    this.isDefaultVersion = isDefaultVersion;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("transport")
  public List<String> getTransport() {
    return transport;
  }
  public void setTransport(List<String> transport) {
    this.transport = transport;
  }

  
  /**
   * Name of the Authorization header used for invoking the API. If it is not set, Authorization header name specified\nin tenant or system level will be used.\n
   **/
  @ApiModelProperty(value = "Name of the Authorization header used for invoking the API. If it is not set, Authorization header name specified\nin tenant or system level will be used.\n")
  @JsonProperty("authorizationHeader")
  public String getAuthorizationHeader() {
    return authorizationHeader;
  }
  public void setAuthorizationHeader(String authorizationHeader) {
    this.authorizationHeader = authorizationHeader;
  }

  
  /**
   * Supported API security for the API ( mutualssl and/or oauth2)\n
   **/
  @ApiModelProperty(value = "Supported API security for the API ( mutualssl and/or oauth2)\n")
  @JsonProperty("apiSecurity")
  public String getApiSecurity() {
    return apiSecurity;
  }
  public void setApiSecurity(String apiSecurity) {
    this.apiSecurity = apiSecurity;
  }

  
  /**
   * Search keywords related to the API
   **/
  @ApiModelProperty(value = "Search keywords related to the API")
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
  @ApiModelProperty(value = "The subscription tiers selected for the particular API")
  @JsonProperty("tiers")
  public List<String> getTiers() {
    return tiers;
  }
  public void setTiers(List<String> tiers) {
    this.tiers = tiers;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("thumbnailUrl")
  public String getThumbnailUrl() {
    return thumbnailUrl;
  }
  public void setThumbnailUrl(String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
  }

  
  /**
   * Custom(user defined) properties of API\n
   **/
  @ApiModelProperty(value = "Custom(user defined) properties of API\n")
  @JsonProperty("additionalProperties")
  public Map<String, String> getAdditionalProperties() {
    return additionalProperties;
  }
  public void setAdditionalProperties(Map<String, String> additionalProperties) {
    this.additionalProperties = additionalProperties;
  }

  
  /**
   **/
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
  @ApiModelProperty(value = "")
  @JsonProperty("businessInformation")
  public APIBusinessInformationDTO getBusinessInformation() {
    return businessInformation;
  }
  public void setBusinessInformation(APIBusinessInformationDTO businessInformation) {
    this.businessInformation = businessInformation;
  }

  
  /**
   * Labels of micro-gateway environments attached to the API.\n
   **/
  @ApiModelProperty(value = "Labels of micro-gateway environments attached to the API.\n")
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
  @ApiModelProperty(value = "The environment list configured with non empty endpoint URLs for the particular API.")
  @JsonProperty("environmentList")
  public List<String> getEnvironmentList() {
    return environmentList;
  }
  public void setEnvironmentList(List<String> environmentList) {
    this.environmentList = environmentList;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIDTO {\n");
    
    sb.append("  id: ").append(id).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("  description: ").append(description).append("\n");
    sb.append("  context: ").append(context).append("\n");
    sb.append("  version: ").append(version).append("\n");
    sb.append("  provider: ").append(provider).append("\n");
    sb.append("  apiDefinition: ").append(apiDefinition).append("\n");
    sb.append("  wsdlUri: ").append(wsdlUri).append("\n");
    sb.append("  status: ").append(status).append("\n");
    sb.append("  isDefaultVersion: ").append(isDefaultVersion).append("\n");
    sb.append("  transport: ").append(transport).append("\n");
    sb.append("  authorizationHeader: ").append(authorizationHeader).append("\n");
    sb.append("  apiSecurity: ").append(apiSecurity).append("\n");
    sb.append("  tags: ").append(tags).append("\n");
    sb.append("  tiers: ").append(tiers).append("\n");
    sb.append("  thumbnailUrl: ").append(thumbnailUrl).append("\n");
    sb.append("  additionalProperties: ").append(additionalProperties).append("\n");
    sb.append("  endpointURLs: ").append(endpointURLs).append("\n");
    sb.append("  businessInformation: ").append(businessInformation).append("\n");
    sb.append("  labels: ").append(labels).append("\n");
    sb.append("  environmentList: ").append(environmentList).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
