package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.API_businessInformationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.API_corsConfigurationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.API_endpointDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.API_operationsDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.API_threatProtectionPoliciesDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.SequenceDTO;
import java.util.Objects;

/**
 * APIDTO
 */
public class APIDTO extends APIInfoDTO  {
  @SerializedName("wsdlUri")
  private String wsdlUri = null;

  @SerializedName("createdTime")
  private String createdTime = null;

  @SerializedName("apiPolicy")
  private String apiPolicy = null;

  @SerializedName("lastUpdatedTime")
  private String lastUpdatedTime = null;

  @SerializedName("responseCaching")
  private String responseCaching = null;

  @SerializedName("cacheTimeout")
  private Integer cacheTimeout = null;

  @SerializedName("destinationStatsEnabled")
  private String destinationStatsEnabled = null;

  @SerializedName("isDefaultVersion")
  private Boolean isDefaultVersion = null;

  @SerializedName("transport")
  private List<String> transport = new ArrayList<String>();

  @SerializedName("tags")
  private List<String> tags = new ArrayList<String>();

  @SerializedName("hasOwnGateway")
  private Boolean hasOwnGateway = null;

  @SerializedName("gatewayLabels")
  private List<String> gatewayLabels = new ArrayList<String>();

  @SerializedName("storeLabels")
  private List<String> storeLabels = new ArrayList<String>();

  @SerializedName("policies")
  private List<String> policies = new ArrayList<String>();

  /**
   * Gets or Sets visibility
   */
  public enum VisibilityEnum {
    @SerializedName("PUBLIC")
    PUBLIC("PUBLIC"),
    
    @SerializedName("PRIVATE")
    PRIVATE("PRIVATE"),
    
    @SerializedName("RESTRICTED")
    RESTRICTED("RESTRICTED");

    private String value;

    VisibilityEnum(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
    public static VisibilityEnum fromValue(String text) {
      for (VisibilityEnum b : VisibilityEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @SerializedName("visibility")
  private VisibilityEnum visibility = null;

  @SerializedName("visibleRoles")
  private List<String> visibleRoles = new ArrayList<String>();

  @SerializedName("permission")
  private String permission = null;

  @SerializedName("userPermissionsForApi")
  private List<String> userPermissionsForApi = new ArrayList<String>();

  @SerializedName("visibleTenants")
  private List<String> visibleTenants = new ArrayList<String>();

  @SerializedName("gatewayEnvironments")
  private String gatewayEnvironments = null;

  @SerializedName("sequences")
  private List<SequenceDTO> sequences = new ArrayList<SequenceDTO>();

  @SerializedName("businessInformation")
  private API_businessInformationDTO businessInformation = null;

  @SerializedName("corsConfiguration")
  private API_corsConfigurationDTO corsConfiguration = null;

  @SerializedName("endpoint")
  private List<API_endpointDTO> endpoint = new ArrayList<API_endpointDTO>();

  @SerializedName("scopes")
  private List<String> scopes = new ArrayList<String>();

  @SerializedName("operations")
  private List<API_operationsDTO> operations = new ArrayList<API_operationsDTO>();

  @SerializedName("threatProtectionPolicies")
  private API_threatProtectionPoliciesDTO threatProtectionPolicies = null;

  public APIDTO wsdlUri(String wsdlUri) {
    this.wsdlUri = wsdlUri;
    return this;
  }

   /**
   * WSDL URL if the API is based on a WSDL endpoint 
   * @return wsdlUri
  **/
  @ApiModelProperty(example = "http://www.webservicex.com/globalweather.asmx?wsdl", value = "WSDL URL if the API is based on a WSDL endpoint ")
  public String getWsdlUri() {
    return wsdlUri;
  }

  public void setWsdlUri(String wsdlUri) {
    this.wsdlUri = wsdlUri;
  }

  public APIDTO createdTime(String createdTime) {
    this.createdTime = createdTime;
    return this;
  }

   /**
   * Get createdTime
   * @return createdTime
  **/
  @ApiModelProperty(example = "2017-02-20T13:57:16.229+0000", value = "")
  public String getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(String createdTime) {
    this.createdTime = createdTime;
  }

  public APIDTO apiPolicy(String apiPolicy) {
    this.apiPolicy = apiPolicy;
    return this;
  }

   /**
   * Get apiPolicy
   * @return apiPolicy
  **/
  @ApiModelProperty(example = "UNLIMITED", value = "")
  public String getApiPolicy() {
    return apiPolicy;
  }

  public void setApiPolicy(String apiPolicy) {
    this.apiPolicy = apiPolicy;
  }

  public APIDTO lastUpdatedTime(String lastUpdatedTime) {
    this.lastUpdatedTime = lastUpdatedTime;
    return this;
  }

   /**
   * Get lastUpdatedTime
   * @return lastUpdatedTime
  **/
  @ApiModelProperty(example = "2017-02-20T13:57:16.229+0000", value = "")
  public String getLastUpdatedTime() {
    return lastUpdatedTime;
  }

  public void setLastUpdatedTime(String lastUpdatedTime) {
    this.lastUpdatedTime = lastUpdatedTime;
  }

  public APIDTO responseCaching(String responseCaching) {
    this.responseCaching = responseCaching;
    return this;
  }

   /**
   * Get responseCaching
   * @return responseCaching
  **/
  @ApiModelProperty(example = "Disabled", value = "")
  public String getResponseCaching() {
    return responseCaching;
  }

  public void setResponseCaching(String responseCaching) {
    this.responseCaching = responseCaching;
  }

  public APIDTO cacheTimeout(Integer cacheTimeout) {
    this.cacheTimeout = cacheTimeout;
    return this;
  }

   /**
   * Get cacheTimeout
   * @return cacheTimeout
  **/
  @ApiModelProperty(example = "300", value = "")
  public Integer getCacheTimeout() {
    return cacheTimeout;
  }

  public void setCacheTimeout(Integer cacheTimeout) {
    this.cacheTimeout = cacheTimeout;
  }

  public APIDTO destinationStatsEnabled(String destinationStatsEnabled) {
    this.destinationStatsEnabled = destinationStatsEnabled;
    return this;
  }

   /**
   * Get destinationStatsEnabled
   * @return destinationStatsEnabled
  **/
  @ApiModelProperty(example = "Disabled", value = "")
  public String getDestinationStatsEnabled() {
    return destinationStatsEnabled;
  }

  public void setDestinationStatsEnabled(String destinationStatsEnabled) {
    this.destinationStatsEnabled = destinationStatsEnabled;
  }

  public APIDTO isDefaultVersion(Boolean isDefaultVersion) {
    this.isDefaultVersion = isDefaultVersion;
    return this;
  }

   /**
   * Get isDefaultVersion
   * @return isDefaultVersion
  **/
  @ApiModelProperty(example = "false", value = "")
  public Boolean getIsDefaultVersion() {
    return isDefaultVersion;
  }

  public void setIsDefaultVersion(Boolean isDefaultVersion) {
    this.isDefaultVersion = isDefaultVersion;
  }

  public APIDTO transport(List<String> transport) {
    this.transport = transport;
    return this;
  }

  public APIDTO addTransportItem(String transportItem) {
    this.transport.add(transportItem);
    return this;
  }

   /**
   * Supported transports for the API (http and/or https). 
   * @return transport
  **/
  @ApiModelProperty(example = "[\"http\",\"https\"]", value = "Supported transports for the API (http and/or https). ")
  public List<String> getTransport() {
    return transport;
  }

  public void setTransport(List<String> transport) {
    this.transport = transport;
  }

  public APIDTO tags(List<String> tags) {
    this.tags = tags;
    return this;
  }

  public APIDTO addTagsItem(String tagsItem) {
    this.tags.add(tagsItem);
    return this;
  }

   /**
   * Get tags
   * @return tags
  **/
  @ApiModelProperty(example = "[\"substract\",\"add\"]", value = "")
  public List<String> getTags() {
    return tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  public APIDTO hasOwnGateway(Boolean hasOwnGateway) {
    this.hasOwnGateway = hasOwnGateway;
    return this;
  }

   /**
   * Get hasOwnGateway
   * @return hasOwnGateway
  **/
  @ApiModelProperty(example = "false", value = "")
  public Boolean getHasOwnGateway() {
    return hasOwnGateway;
  }

  public void setHasOwnGateway(Boolean hasOwnGateway) {
    this.hasOwnGateway = hasOwnGateway;
  }

  public APIDTO gatewayLabels(List<String> gatewayLabels) {
    this.gatewayLabels = gatewayLabels;
    return this;
  }

  public APIDTO addGatewayLabelsItem(String gatewayLabelsItem) {
    this.gatewayLabels.add(gatewayLabelsItem);
    return this;
  }

   /**
   * Get gatewayLabels
   * @return gatewayLabels
  **/
  @ApiModelProperty(example = "[\"public\",\"private\"]", value = "")
  public List<String> getGatewayLabels() {
    return gatewayLabels;
  }

  public void setGatewayLabels(List<String> gatewayLabels) {
    this.gatewayLabels = gatewayLabels;
  }

  public APIDTO storeLabels(List<String> storeLabels) {
    this.storeLabels = storeLabels;
    return this;
  }

  public APIDTO addStoreLabelsItem(String storeLabelsItem) {
    this.storeLabels.add(storeLabelsItem);
    return this;
  }

   /**
   * Get storeLabels
   * @return storeLabels
  **/
  @ApiModelProperty(example = "[\"public\",\"private\"]", value = "")
  public List<String> getStoreLabels() {
    return storeLabels;
  }

  public void setStoreLabels(List<String> storeLabels) {
    this.storeLabels = storeLabels;
  }

  public APIDTO policies(List<String> policies) {
    this.policies = policies;
    return this;
  }

  public APIDTO addPoliciesItem(String policiesItem) {
    this.policies.add(policiesItem);
    return this;
  }

   /**
   * Get policies
   * @return policies
  **/
  @ApiModelProperty(example = "[\"Unlimited\"]", value = "")
  public List<String> getPolicies() {
    return policies;
  }

  public void setPolicies(List<String> policies) {
    this.policies = policies;
  }

  public APIDTO visibility(VisibilityEnum visibility) {
    this.visibility = visibility;
    return this;
  }

   /**
   * Get visibility
   * @return visibility
  **/
  @ApiModelProperty(example = "PUBLIC", value = "")
  public VisibilityEnum getVisibility() {
    return visibility;
  }

  public void setVisibility(VisibilityEnum visibility) {
    this.visibility = visibility;
  }

  public APIDTO visibleRoles(List<String> visibleRoles) {
    this.visibleRoles = visibleRoles;
    return this;
  }

  public APIDTO addVisibleRolesItem(String visibleRolesItem) {
    this.visibleRoles.add(visibleRolesItem);
    return this;
  }

   /**
   * Get visibleRoles
   * @return visibleRoles
  **/
  @ApiModelProperty(example = "[]", value = "")
  public List<String> getVisibleRoles() {
    return visibleRoles;
  }

  public void setVisibleRoles(List<String> visibleRoles) {
    this.visibleRoles = visibleRoles;
  }

  public APIDTO permission(String permission) {
    this.permission = permission;
    return this;
  }

   /**
   * Get permission
   * @return permission
  **/
  @ApiModelProperty(example = "[{\"groupId\" : 1000, \"permission\" : [\"READ\",\"UPDATE\"]},{\"groupId\" : 1001, \"permission\" : [\"READ\",\"UPDATE\"]}]", value = "")
  public String getPermission() {
    return permission;
  }

  public void setPermission(String permission) {
    this.permission = permission;
  }

  public APIDTO userPermissionsForApi(List<String> userPermissionsForApi) {
    this.userPermissionsForApi = userPermissionsForApi;
    return this;
  }

  public APIDTO addUserPermissionsForApiItem(String userPermissionsForApiItem) {
    this.userPermissionsForApi.add(userPermissionsForApiItem);
    return this;
  }

   /**
   * LoggedIn user permissions for the API 
   * @return userPermissionsForApi
  **/
  @ApiModelProperty(example = "[\"READ\",\"UPDATE\"]", value = "LoggedIn user permissions for the API ")
  public List<String> getUserPermissionsForApi() {
    return userPermissionsForApi;
  }

  public void setUserPermissionsForApi(List<String> userPermissionsForApi) {
    this.userPermissionsForApi = userPermissionsForApi;
  }

  public APIDTO visibleTenants(List<String> visibleTenants) {
    this.visibleTenants = visibleTenants;
    return this;
  }

  public APIDTO addVisibleTenantsItem(String visibleTenantsItem) {
    this.visibleTenants.add(visibleTenantsItem);
    return this;
  }

   /**
   * Get visibleTenants
   * @return visibleTenants
  **/
  @ApiModelProperty(example = "[]", value = "")
  public List<String> getVisibleTenants() {
    return visibleTenants;
  }

  public void setVisibleTenants(List<String> visibleTenants) {
    this.visibleTenants = visibleTenants;
  }

  public APIDTO gatewayEnvironments(String gatewayEnvironments) {
    this.gatewayEnvironments = gatewayEnvironments;
    return this;
  }

   /**
   * Comma separated list of gateway environments. 
   * @return gatewayEnvironments
  **/
  @ApiModelProperty(example = "Production and Sandbox", value = "Comma separated list of gateway environments. ")
  public String getGatewayEnvironments() {
    return gatewayEnvironments;
  }

  public void setGatewayEnvironments(String gatewayEnvironments) {
    this.gatewayEnvironments = gatewayEnvironments;
  }

  public APIDTO sequences(List<SequenceDTO> sequences) {
    this.sequences = sequences;
    return this;
  }

  public APIDTO addSequencesItem(SequenceDTO sequencesItem) {
    this.sequences.add(sequencesItem);
    return this;
  }

   /**
   * Get sequences
   * @return sequences
  **/
  @ApiModelProperty(example = "[]", value = "")
  public List<SequenceDTO> getSequences() {
    return sequences;
  }

  public void setSequences(List<SequenceDTO> sequences) {
    this.sequences = sequences;
  }

  public APIDTO businessInformation(API_businessInformationDTO businessInformation) {
    this.businessInformation = businessInformation;
    return this;
  }

   /**
   * Get businessInformation
   * @return businessInformation
  **/
  @ApiModelProperty(value = "")
  public API_businessInformationDTO getBusinessInformation() {
    return businessInformation;
  }

  public void setBusinessInformation(API_businessInformationDTO businessInformation) {
    this.businessInformation = businessInformation;
  }

  public APIDTO corsConfiguration(API_corsConfigurationDTO corsConfiguration) {
    this.corsConfiguration = corsConfiguration;
    return this;
  }

   /**
   * Get corsConfiguration
   * @return corsConfiguration
  **/
  @ApiModelProperty(value = "")
  public API_corsConfigurationDTO getCorsConfiguration() {
    return corsConfiguration;
  }

  public void setCorsConfiguration(API_corsConfigurationDTO corsConfiguration) {
    this.corsConfiguration = corsConfiguration;
  }

  public APIDTO endpoint(List<API_endpointDTO> endpoint) {
    this.endpoint = endpoint;
    return this;
  }

  public APIDTO addEndpointItem(API_endpointDTO endpointItem) {
    this.endpoint.add(endpointItem);
    return this;
  }

   /**
   * Get endpoint
   * @return endpoint
  **/
  @ApiModelProperty(value = "")
  public List<API_endpointDTO> getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(List<API_endpointDTO> endpoint) {
    this.endpoint = endpoint;
  }

  public APIDTO scopes(List<String> scopes) {
    this.scopes = scopes;
    return this;
  }

  public APIDTO addScopesItem(String scopesItem) {
    this.scopes.add(scopesItem);
    return this;
  }

   /**
   * Get scopes
   * @return scopes
  **/
  @ApiModelProperty(value = "")
  public List<String> getScopes() {
    return scopes;
  }

  public void setScopes(List<String> scopes) {
    this.scopes = scopes;
  }

  public APIDTO operations(List<API_operationsDTO> operations) {
    this.operations = operations;
    return this;
  }

  public APIDTO addOperationsItem(API_operationsDTO operationsItem) {
    this.operations.add(operationsItem);
    return this;
  }

   /**
   * Get operations
   * @return operations
  **/
  @ApiModelProperty(value = "")
  public List<API_operationsDTO> getOperations() {
    return operations;
  }

  public void setOperations(List<API_operationsDTO> operations) {
    this.operations = operations;
  }

  public APIDTO threatProtectionPolicies(API_threatProtectionPoliciesDTO threatProtectionPolicies) {
    this.threatProtectionPolicies = threatProtectionPolicies;
    return this;
  }

   /**
   * Get threatProtectionPolicies
   * @return threatProtectionPolicies
  **/
  @ApiModelProperty(value = "")
  public API_threatProtectionPoliciesDTO getThreatProtectionPolicies() {
    return threatProtectionPolicies;
  }

  public void setThreatProtectionPolicies(API_threatProtectionPoliciesDTO threatProtectionPolicies) {
    this.threatProtectionPolicies = threatProtectionPolicies;
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
    return Objects.equals(this.wsdlUri, API.wsdlUri) &&
        Objects.equals(this.createdTime, API.createdTime) &&
        Objects.equals(this.apiPolicy, API.apiPolicy) &&
        Objects.equals(this.lastUpdatedTime, API.lastUpdatedTime) &&
        Objects.equals(this.responseCaching, API.responseCaching) &&
        Objects.equals(this.cacheTimeout, API.cacheTimeout) &&
        Objects.equals(this.destinationStatsEnabled, API.destinationStatsEnabled) &&
        Objects.equals(this.isDefaultVersion, API.isDefaultVersion) &&
        Objects.equals(this.transport, API.transport) &&
        Objects.equals(this.tags, API.tags) &&
        Objects.equals(this.hasOwnGateway, API.hasOwnGateway) &&
        Objects.equals(this.gatewayLabels, API.gatewayLabels) &&
        Objects.equals(this.storeLabels, API.storeLabels) &&
        Objects.equals(this.policies, API.policies) &&
        Objects.equals(this.visibility, API.visibility) &&
        Objects.equals(this.visibleRoles, API.visibleRoles) &&
        Objects.equals(this.permission, API.permission) &&
        Objects.equals(this.userPermissionsForApi, API.userPermissionsForApi) &&
        Objects.equals(this.visibleTenants, API.visibleTenants) &&
        Objects.equals(this.gatewayEnvironments, API.gatewayEnvironments) &&
        Objects.equals(this.sequences, API.sequences) &&
        Objects.equals(this.businessInformation, API.businessInformation) &&
        Objects.equals(this.corsConfiguration, API.corsConfiguration) &&
        Objects.equals(this.endpoint, API.endpoint) &&
        Objects.equals(this.scopes, API.scopes) &&
        Objects.equals(this.operations, API.operations) &&
        Objects.equals(this.threatProtectionPolicies, API.threatProtectionPolicies) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(wsdlUri, createdTime, apiPolicy, lastUpdatedTime, responseCaching, cacheTimeout, destinationStatsEnabled, isDefaultVersion, transport, tags, hasOwnGateway, gatewayLabels, storeLabels, policies, visibility, visibleRoles, permission, userPermissionsForApi, visibleTenants, gatewayEnvironments, sequences, businessInformation, corsConfiguration, endpoint, scopes, operations, threatProtectionPolicies, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIDTO {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    wsdlUri: ").append(toIndentedString(wsdlUri)).append("\n");
    sb.append("    createdTime: ").append(toIndentedString(createdTime)).append("\n");
    sb.append("    apiPolicy: ").append(toIndentedString(apiPolicy)).append("\n");
    sb.append("    lastUpdatedTime: ").append(toIndentedString(lastUpdatedTime)).append("\n");
    sb.append("    responseCaching: ").append(toIndentedString(responseCaching)).append("\n");
    sb.append("    cacheTimeout: ").append(toIndentedString(cacheTimeout)).append("\n");
    sb.append("    destinationStatsEnabled: ").append(toIndentedString(destinationStatsEnabled)).append("\n");
    sb.append("    isDefaultVersion: ").append(toIndentedString(isDefaultVersion)).append("\n");
    sb.append("    transport: ").append(toIndentedString(transport)).append("\n");
    sb.append("    tags: ").append(toIndentedString(tags)).append("\n");
    sb.append("    hasOwnGateway: ").append(toIndentedString(hasOwnGateway)).append("\n");
    sb.append("    gatewayLabels: ").append(toIndentedString(gatewayLabels)).append("\n");
    sb.append("    storeLabels: ").append(toIndentedString(storeLabels)).append("\n");
    sb.append("    policies: ").append(toIndentedString(policies)).append("\n");
    sb.append("    visibility: ").append(toIndentedString(visibility)).append("\n");
    sb.append("    visibleRoles: ").append(toIndentedString(visibleRoles)).append("\n");
    sb.append("    permission: ").append(toIndentedString(permission)).append("\n");
    sb.append("    userPermissionsForApi: ").append(toIndentedString(userPermissionsForApi)).append("\n");
    sb.append("    visibleTenants: ").append(toIndentedString(visibleTenants)).append("\n");
    sb.append("    gatewayEnvironments: ").append(toIndentedString(gatewayEnvironments)).append("\n");
    sb.append("    sequences: ").append(toIndentedString(sequences)).append("\n");
    sb.append("    businessInformation: ").append(toIndentedString(businessInformation)).append("\n");
    sb.append("    corsConfiguration: ").append(toIndentedString(corsConfiguration)).append("\n");
    sb.append("    endpoint: ").append(toIndentedString(endpoint)).append("\n");
    sb.append("    scopes: ").append(toIndentedString(scopes)).append("\n");
    sb.append("    operations: ").append(toIndentedString(operations)).append("\n");
    sb.append("    threatProtectionPolicies: ").append(toIndentedString(threatProtectionPolicies)).append("\n");
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

