package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIBusinessInformationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APICorsConfigurationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIEndpointDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIOperationsDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIThreatProtectionPoliciesDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SequenceDTO;

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
  
  
  private String provider = null;
  
  
  private String wsdlUri = null;
  
  
  private String lifeCycleStatus = null;
  
  
  private String workflowStatus = null;
  
  
  private String createdTime = null;
  
  
  private String apiPolicy = null;
  
  
  private String lastUpdatedTime = null;
  
  
  private String responseCaching = null;
  
  
  private Integer cacheTimeout = null;
  
  
  private String destinationStatsEnabled = null;
  
  @NotNull
  private Boolean isDefaultVersion = null;
  
  @NotNull
  private List<String> transport = new ArrayList<String>();
  
  
  private List<String> tags = new ArrayList<String>();
  
  
  private Boolean hasOwnGateway = null;
  
  
  private List<String> labels = new ArrayList<String>();
  
  @NotNull
  private List<String> policies = new ArrayList<String>();
  
  public enum VisibilityEnum {
     PUBLIC,  PRIVATE,  RESTRICTED, 
  };
  @NotNull
  private VisibilityEnum visibility = null;
  
  
  private List<String> visibleRoles = new ArrayList<String>();
  
  
  private String permission = null;
  
  
  private List<String> userPermissionsForApi = new ArrayList<String>();
  
  
  private List<String> visibleTenants = new ArrayList<String>();
  
  
  private String gatewayEnvironments = null;
  
  
  private List<SequenceDTO> sequences = new ArrayList<SequenceDTO>();
  
  
  private APIBusinessInformationDTO businessInformation = null;
  
  
  private APICorsConfigurationDTO corsConfiguration = null;
  
  
  private List<APIEndpointDTO> endpoint = new ArrayList<APIEndpointDTO>();
  
  
  private List<String> securityScheme = new ArrayList<String>();
  
  
  private List<String> scopes = new ArrayList<String>();
  
  
  private List<APIOperationsDTO> operations = new ArrayList<APIOperationsDTO>();
  
  
  private APIThreatProtectionPoliciesDTO threatProtectionPolicies = null;

  
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
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("context")
  public String getContext() {
    return context;
  }
  public void setContext(String context) {
    this.context = context;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
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
  @ApiModelProperty(value = "If the provider value is not given user invoking the api will be used as the provider.\n")
  @JsonProperty("provider")
  public String getProvider() {
    return provider;
  }
  public void setProvider(String provider) {
    this.provider = provider;
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
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("lifeCycleStatus")
  public String getLifeCycleStatus() {
    return lifeCycleStatus;
  }
  public void setLifeCycleStatus(String lifeCycleStatus) {
    this.lifeCycleStatus = lifeCycleStatus;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("workflowStatus")
  public String getWorkflowStatus() {
    return workflowStatus;
  }
  public void setWorkflowStatus(String workflowStatus) {
    this.workflowStatus = workflowStatus;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("createdTime")
  public String getCreatedTime() {
    return createdTime;
  }
  public void setCreatedTime(String createdTime) {
    this.createdTime = createdTime;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("apiPolicy")
  public String getApiPolicy() {
    return apiPolicy;
  }
  public void setApiPolicy(String apiPolicy) {
    this.apiPolicy = apiPolicy;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("lastUpdatedTime")
  public String getLastUpdatedTime() {
    return lastUpdatedTime;
  }
  public void setLastUpdatedTime(String lastUpdatedTime) {
    this.lastUpdatedTime = lastUpdatedTime;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("responseCaching")
  public String getResponseCaching() {
    return responseCaching;
  }
  public void setResponseCaching(String responseCaching) {
    this.responseCaching = responseCaching;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("cacheTimeout")
  public Integer getCacheTimeout() {
    return cacheTimeout;
  }
  public void setCacheTimeout(Integer cacheTimeout) {
    this.cacheTimeout = cacheTimeout;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("destinationStatsEnabled")
  public String getDestinationStatsEnabled() {
    return destinationStatsEnabled;
  }
  public void setDestinationStatsEnabled(String destinationStatsEnabled) {
    this.destinationStatsEnabled = destinationStatsEnabled;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("isDefaultVersion")
  public Boolean getIsDefaultVersion() {
    return isDefaultVersion;
  }
  public void setIsDefaultVersion(Boolean isDefaultVersion) {
    this.isDefaultVersion = isDefaultVersion;
  }

  
  /**
   * Supported transports for the API (http and/or https).\n
   **/
  @ApiModelProperty(required = true, value = "Supported transports for the API (http and/or https).\n")
  @JsonProperty("transport")
  public List<String> getTransport() {
    return transport;
  }
  public void setTransport(List<String> transport) {
    this.transport = transport;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("tags")
  public List<String> getTags() {
    return tags;
  }
  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("hasOwnGateway")
  public Boolean getHasOwnGateway() {
    return hasOwnGateway;
  }
  public void setHasOwnGateway(Boolean hasOwnGateway) {
    this.hasOwnGateway = hasOwnGateway;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("labels")
  public List<String> getLabels() {
    return labels;
  }
  public void setLabels(List<String> labels) {
    this.labels = labels;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("policies")
  public List<String> getPolicies() {
    return policies;
  }
  public void setPolicies(List<String> policies) {
    this.policies = policies;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("visibility")
  public VisibilityEnum getVisibility() {
    return visibility;
  }
  public void setVisibility(VisibilityEnum visibility) {
    this.visibility = visibility;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("visibleRoles")
  public List<String> getVisibleRoles() {
    return visibleRoles;
  }
  public void setVisibleRoles(List<String> visibleRoles) {
    this.visibleRoles = visibleRoles;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("permission")
  public String getPermission() {
    return permission;
  }
  public void setPermission(String permission) {
    this.permission = permission;
  }

  
  /**
   * LoggedIn user permissions for the API\n
   **/
  @ApiModelProperty(value = "LoggedIn user permissions for the API\n")
  @JsonProperty("userPermissionsForApi")
  public List<String> getUserPermissionsForApi() {
    return userPermissionsForApi;
  }
  public void setUserPermissionsForApi(List<String> userPermissionsForApi) {
    this.userPermissionsForApi = userPermissionsForApi;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("visibleTenants")
  public List<String> getVisibleTenants() {
    return visibleTenants;
  }
  public void setVisibleTenants(List<String> visibleTenants) {
    this.visibleTenants = visibleTenants;
  }

  
  /**
   * Comma separated list of gateway environments.\n
   **/
  @ApiModelProperty(value = "Comma separated list of gateway environments.\n")
  @JsonProperty("gatewayEnvironments")
  public String getGatewayEnvironments() {
    return gatewayEnvironments;
  }
  public void setGatewayEnvironments(String gatewayEnvironments) {
    this.gatewayEnvironments = gatewayEnvironments;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("sequences")
  public List<SequenceDTO> getSequences() {
    return sequences;
  }
  public void setSequences(List<SequenceDTO> sequences) {
    this.sequences = sequences;
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
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("corsConfiguration")
  public APICorsConfigurationDTO getCorsConfiguration() {
    return corsConfiguration;
  }
  public void setCorsConfiguration(APICorsConfigurationDTO corsConfiguration) {
    this.corsConfiguration = corsConfiguration;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("endpoint")
  public List<APIEndpointDTO> getEndpoint() {
    return endpoint;
  }
  public void setEndpoint(List<APIEndpointDTO> endpoint) {
    this.endpoint = endpoint;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("securityScheme")
  public List<String> getSecurityScheme() {
    return securityScheme;
  }
  public void setSecurityScheme(List<String> securityScheme) {
    this.securityScheme = securityScheme;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("scopes")
  public List<String> getScopes() {
    return scopes;
  }
  public void setScopes(List<String> scopes) {
    this.scopes = scopes;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("operations")
  public List<APIOperationsDTO> getOperations() {
    return operations;
  }
  public void setOperations(List<APIOperationsDTO> operations) {
    this.operations = operations;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("threatProtectionPolicies")
  public APIThreatProtectionPoliciesDTO getThreatProtectionPolicies() {
    return threatProtectionPolicies;
  }
  public void setThreatProtectionPolicies(APIThreatProtectionPoliciesDTO threatProtectionPolicies) {
    this.threatProtectionPolicies = threatProtectionPolicies;
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
    sb.append("  wsdlUri: ").append(wsdlUri).append("\n");
    sb.append("  lifeCycleStatus: ").append(lifeCycleStatus).append("\n");
    sb.append("  workflowStatus: ").append(workflowStatus).append("\n");
    sb.append("  createdTime: ").append(createdTime).append("\n");
    sb.append("  apiPolicy: ").append(apiPolicy).append("\n");
    sb.append("  lastUpdatedTime: ").append(lastUpdatedTime).append("\n");
    sb.append("  responseCaching: ").append(responseCaching).append("\n");
    sb.append("  cacheTimeout: ").append(cacheTimeout).append("\n");
    sb.append("  destinationStatsEnabled: ").append(destinationStatsEnabled).append("\n");
    sb.append("  isDefaultVersion: ").append(isDefaultVersion).append("\n");
    sb.append("  transport: ").append(transport).append("\n");
    sb.append("  tags: ").append(tags).append("\n");
    sb.append("  hasOwnGateway: ").append(hasOwnGateway).append("\n");
    sb.append("  labels: ").append(labels).append("\n");
    sb.append("  policies: ").append(policies).append("\n");
    sb.append("  visibility: ").append(visibility).append("\n");
    sb.append("  visibleRoles: ").append(visibleRoles).append("\n");
    sb.append("  permission: ").append(permission).append("\n");
    sb.append("  userPermissionsForApi: ").append(userPermissionsForApi).append("\n");
    sb.append("  visibleTenants: ").append(visibleTenants).append("\n");
    sb.append("  gatewayEnvironments: ").append(gatewayEnvironments).append("\n");
    sb.append("  sequences: ").append(sequences).append("\n");
    sb.append("  businessInformation: ").append(businessInformation).append("\n");
    sb.append("  corsConfiguration: ").append(corsConfiguration).append("\n");
    sb.append("  endpoint: ").append(endpoint).append("\n");
    sb.append("  securityScheme: ").append(securityScheme).append("\n");
    sb.append("  scopes: ").append(scopes).append("\n");
    sb.append("  operations: ").append(operations).append("\n");
    sb.append("  threatProtectionPolicies: ").append(threatProtectionPolicies).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
