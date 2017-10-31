package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.API_businessInformationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.API_corsConfigurationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.API_endpointDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.API_operationsDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.SequenceDTO;
import java.util.Objects;

/**
 * APIDTO
 */
public class APIDTO   {
  @JsonProperty("id")
  private String id = null;

  @JsonProperty("name")
  private String name = null;

  @JsonProperty("description")
  private String description = null;

  @JsonProperty("context")
  private String context = null;

  @JsonProperty("version")
  private String version = null;

  @JsonProperty("provider")
  private String provider = null;

  @JsonProperty("wsdlUri")
  private String wsdlUri = null;

  @JsonProperty("lifeCycleStatus")
  private String lifeCycleStatus = null;

  @JsonProperty("workflowStatus")
  private String workflowStatus = null;

  @JsonProperty("createdTime")
  private String createdTime = null;

  @JsonProperty("apiPolicy")
  private String apiPolicy = null;

  @JsonProperty("lastUpdatedTime")
  private String lastUpdatedTime = null;

  @JsonProperty("responseCaching")
  private String responseCaching = null;

  @JsonProperty("cacheTimeout")
  private Integer cacheTimeout = null;

  @JsonProperty("destinationStatsEnabled")
  private String destinationStatsEnabled = null;

  @JsonProperty("isDefaultVersion")
  private Boolean isDefaultVersion = null;

  @JsonProperty("transport")
  private List<String> transport = new ArrayList<String>();

  @JsonProperty("tags")
  private List<String> tags = new ArrayList<String>();

  @JsonProperty("labels")
  private List<String> labels = new ArrayList<String>();

  @JsonProperty("policies")
  private List<String> policies = new ArrayList<String>();

  /**
   * Gets or Sets visibility
   */
  public enum VisibilityEnum {
    PUBLIC("PUBLIC"),
    
    PRIVATE("PRIVATE"),
    
    RESTRICTED("RESTRICTED"),
    
    CONTROLLED("CONTROLLED");

    private String value;

    VisibilityEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static VisibilityEnum fromValue(String text) {
      for (VisibilityEnum b : VisibilityEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("visibility")
  private VisibilityEnum visibility = null;

  @JsonProperty("visibleRoles")
  private List<String> visibleRoles = new ArrayList<String>();

  @JsonProperty("permission")
  private String permission = null;

  @JsonProperty("userPermissionsForApi")
  private List<String> userPermissionsForApi = new ArrayList<String>();

  @JsonProperty("visibleTenants")
  private List<String> visibleTenants = new ArrayList<String>();

  @JsonProperty("gatewayEnvironments")
  private String gatewayEnvironments = null;

  @JsonProperty("sequences")
  private List<SequenceDTO> sequences = new ArrayList<SequenceDTO>();

  @JsonProperty("businessInformation")
  private API_businessInformationDTO businessInformation = null;

  @JsonProperty("corsConfiguration")
  private API_corsConfigurationDTO corsConfiguration = null;

  @JsonProperty("endpoint")
  private List<API_endpointDTO> endpoint = new ArrayList<API_endpointDTO>();

  @JsonProperty("securityScheme")
  private List<String> securityScheme = new ArrayList<String>();

  @JsonProperty("operations")
  private List<API_operationsDTO> operations = new ArrayList<API_operationsDTO>();

  public APIDTO id(String id) {
    this.id = id;
    return this;
  }

   /**
   * UUID of the api registry artifact 
   * @return id
  **/
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "UUID of the api registry artifact ")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public APIDTO name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Get name
   * @return name
  **/
  @ApiModelProperty(example = "CalculatorAPI", required = true, value = "")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public APIDTO description(String description) {
    this.description = description;
    return this;
  }

   /**
   * Get description
   * @return description
  **/
  @ApiModelProperty(example = "A calculator API that supports basic operations", value = "")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public APIDTO context(String context) {
    this.context = context;
    return this;
  }

   /**
   * Get context
   * @return context
  **/
  @ApiModelProperty(example = "CalculatorAPI", required = true, value = "")
  public String getContext() {
    return context;
  }

  public void setContext(String context) {
    this.context = context;
  }

  public APIDTO version(String version) {
    this.version = version;
    return this;
  }

   /**
   * Get version
   * @return version
  **/
  @ApiModelProperty(example = "1.0.0", required = true, value = "")
  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public APIDTO provider(String provider) {
    this.provider = provider;
    return this;
  }

   /**
   * If the provider value is not given user invoking the api will be used as the provider. 
   * @return provider
  **/
  @ApiModelProperty(example = "admin", value = "If the provider value is not given user invoking the api will be used as the provider. ")
  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

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

  public APIDTO lifeCycleStatus(String lifeCycleStatus) {
    this.lifeCycleStatus = lifeCycleStatus;
    return this;
  }

   /**
   * Get lifeCycleStatus
   * @return lifeCycleStatus
  **/
  @ApiModelProperty(example = "CREATED", value = "")
  public String getLifeCycleStatus() {
    return lifeCycleStatus;
  }

  public void setLifeCycleStatus(String lifeCycleStatus) {
    this.lifeCycleStatus = lifeCycleStatus;
  }

  public APIDTO workflowStatus(String workflowStatus) {
    this.workflowStatus = workflowStatus;
    return this;
  }

   /**
   * Get workflowStatus
   * @return workflowStatus
  **/
  @ApiModelProperty(example = "APPROVED", value = "")
  public String getWorkflowStatus() {
    return workflowStatus;
  }

  public void setWorkflowStatus(String workflowStatus) {
    this.workflowStatus = workflowStatus;
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
  @ApiModelProperty(example = "false", required = true, value = "")
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
  @ApiModelProperty(example = "[&quot;http&quot;,&quot;https&quot;]", required = true, value = "Supported transports for the API (http and/or https). ")
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
  @ApiModelProperty(example = "[&quot;substract&quot;,&quot;add&quot;]", value = "")
  public List<String> getTags() {
    return tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  public APIDTO labels(List<String> labels) {
    this.labels = labels;
    return this;
  }

  public APIDTO addLabelsItem(String labelsItem) {
    this.labels.add(labelsItem);
    return this;
  }

   /**
   * Get labels
   * @return labels
  **/
  @ApiModelProperty(example = "[&quot;public&quot;,&quot;private&quot;]", value = "")
  public List<String> getLabels() {
    return labels;
  }

  public void setLabels(List<String> labels) {
    this.labels = labels;
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
  @ApiModelProperty(example = "[&quot;Unlimited&quot;]", required = true, value = "")
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
  @ApiModelProperty(example = "PUBLIC", required = true, value = "")
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
  @ApiModelProperty(example = "[{&quot;groupId&quot; : 1000, &quot;permission&quot; : [&quot;READ&quot;,&quot;UPDATE&quot;]},{&quot;groupId&quot; : 1001, &quot;permission&quot; : [&quot;READ&quot;,&quot;UPDATE&quot;]}]", value = "")
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
  @ApiModelProperty(example = "[&quot;READ&quot;,&quot;UPDATE&quot;]", value = "LoggedIn user permissions for the API ")
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

  public APIDTO securityScheme(List<String> securityScheme) {
    this.securityScheme = securityScheme;
    return this;
  }

  public APIDTO addSecuritySchemeItem(String securitySchemeItem) {
    this.securityScheme.add(securitySchemeItem);
    return this;
  }

   /**
   * Get securityScheme
   * @return securityScheme
  **/
  @ApiModelProperty(value = "")
  public List<String> getSecurityScheme() {
    return securityScheme;
  }

  public void setSecurityScheme(List<String> securityScheme) {
    this.securityScheme = securityScheme;
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


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIDTO API = (APIDTO) o;
    return Objects.equals(this.id, API.id) &&
        Objects.equals(this.name, API.name) &&
        Objects.equals(this.description, API.description) &&
        Objects.equals(this.context, API.context) &&
        Objects.equals(this.version, API.version) &&
        Objects.equals(this.provider, API.provider) &&
        Objects.equals(this.wsdlUri, API.wsdlUri) &&
        Objects.equals(this.lifeCycleStatus, API.lifeCycleStatus) &&
        Objects.equals(this.workflowStatus, API.workflowStatus) &&
        Objects.equals(this.createdTime, API.createdTime) &&
        Objects.equals(this.apiPolicy, API.apiPolicy) &&
        Objects.equals(this.lastUpdatedTime, API.lastUpdatedTime) &&
        Objects.equals(this.responseCaching, API.responseCaching) &&
        Objects.equals(this.cacheTimeout, API.cacheTimeout) &&
        Objects.equals(this.destinationStatsEnabled, API.destinationStatsEnabled) &&
        Objects.equals(this.isDefaultVersion, API.isDefaultVersion) &&
        Objects.equals(this.transport, API.transport) &&
        Objects.equals(this.tags, API.tags) &&
        Objects.equals(this.labels, API.labels) &&
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
        Objects.equals(this.securityScheme, API.securityScheme) &&
        Objects.equals(this.operations, API.operations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, context, version, provider, wsdlUri, lifeCycleStatus, workflowStatus, createdTime, apiPolicy, lastUpdatedTime, responseCaching, cacheTimeout, destinationStatsEnabled, isDefaultVersion, transport, tags, labels, policies, visibility, visibleRoles, permission, userPermissionsForApi, visibleTenants, gatewayEnvironments, sequences, businessInformation, corsConfiguration, endpoint, securityScheme, operations);
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
    sb.append("    wsdlUri: ").append(toIndentedString(wsdlUri)).append("\n");
    sb.append("    lifeCycleStatus: ").append(toIndentedString(lifeCycleStatus)).append("\n");
    sb.append("    workflowStatus: ").append(toIndentedString(workflowStatus)).append("\n");
    sb.append("    createdTime: ").append(toIndentedString(createdTime)).append("\n");
    sb.append("    apiPolicy: ").append(toIndentedString(apiPolicy)).append("\n");
    sb.append("    lastUpdatedTime: ").append(toIndentedString(lastUpdatedTime)).append("\n");
    sb.append("    responseCaching: ").append(toIndentedString(responseCaching)).append("\n");
    sb.append("    cacheTimeout: ").append(toIndentedString(cacheTimeout)).append("\n");
    sb.append("    destinationStatsEnabled: ").append(toIndentedString(destinationStatsEnabled)).append("\n");
    sb.append("    isDefaultVersion: ").append(toIndentedString(isDefaultVersion)).append("\n");
    sb.append("    transport: ").append(toIndentedString(transport)).append("\n");
    sb.append("    tags: ").append(toIndentedString(tags)).append("\n");
    sb.append("    labels: ").append(toIndentedString(labels)).append("\n");
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
    sb.append("    securityScheme: ").append(toIndentedString(securityScheme)).append("\n");
    sb.append("    operations: ").append(toIndentedString(operations)).append("\n");
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

