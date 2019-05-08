package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIBusinessInformationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APICorsConfigurationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIEndpointDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIEndpointSecurityDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIMaxTpsDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIOperationsDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIThreatProtectionPoliciesDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LabelDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MediationPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ScopeDTO;

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
  
  
  private String lifeCycleStatus = null;
  
  
  private String wsdlUri = null;
  
  
  private String responseCaching = null;
  
  
  private Integer cacheTimeout = null;
  
  
  private String destinationStatsEnabled = null;
  
  
  private Boolean isDefaultVersion = null;
  
  public enum TypeEnum {
     HTTP,  WS,  SOAPTOREST, 
  };
  
  private TypeEnum type = TypeEnum.HTTP;
  
  
  private List<String> transport = new ArrayList<String>();
  
  
  private List<String> tags = new ArrayList<String>();
  
  
  private List<String> policies = new ArrayList<String>();
  
  
  private String apiPolicy = null;
  
  
  private String authorizationHeader = null;
  
  
  private List<String> securityScheme = new ArrayList<String>();
  
  
  private APIMaxTpsDTO maxTps = null;
  
  public enum VisibilityEnum {
     PUBLIC,  PRIVATE,  RESTRICTED, 
  };
  
  private VisibilityEnum visibility = null;
  
  
  private List<String> visibleRoles = new ArrayList<String>();
  
  
  private List<String> visibleTenants = new ArrayList<String>();
  
  
  private APIEndpointSecurityDTO endpointSecurity = null;
  
  
  private List<String> gatewayEnvironments = new ArrayList<String>();
  
  
  private List<LabelDTO> labels = new ArrayList<LabelDTO>();
  
  
  private List<MediationPolicyDTO> mediationPolicies = new ArrayList<MediationPolicyDTO>();
  
  public enum SubscriptionAvailabilityEnum {
     current_tenant,  all_tenants,  specific_tenants, 
  };
  
  private SubscriptionAvailabilityEnum subscriptionAvailability = null;
  
  
  private List<String> subscriptionAvailableTenants = new ArrayList<String>();
  
  
  private Map<String, String> additionalProperties = new HashMap<String, String>();
  
  public enum AccessControlEnum {
     NONE,  RESTRICTED, 
  };
  
  private AccessControlEnum accessControl = null;
  
  
  private List<String> accessControlRoles = new ArrayList<String>();
  
  
  private APIBusinessInformationDTO businessInformation = null;
  
  
  private APICorsConfigurationDTO corsConfiguration = null;
  
  
  private String workflowStatus = null;
  
  
  private String createdTime = null;
  
  
  private String lastUpdatedTime = null;
  
  
  private List<APIEndpointDTO> endpoint = new ArrayList<APIEndpointDTO>();
  
  
  private List<ScopeDTO> scopes = new ArrayList<ScopeDTO>();
  
  
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
  @ApiModelProperty(value = "")
  @JsonProperty("isDefaultVersion")
  public Boolean getIsDefaultVersion() {
    return isDefaultVersion;
  }
  public void setIsDefaultVersion(Boolean isDefaultVersion) {
    this.isDefaultVersion = isDefaultVersion;
  }

  
  /**
   * The api creation type to be used. Accepted values are HTTP, WS, SOAPTOREST
   **/
  @ApiModelProperty(value = "The api creation type to be used. Accepted values are HTTP, WS, SOAPTOREST")
  @JsonProperty("type")
  public TypeEnum getType() {
    return type;
  }
  public void setType(TypeEnum type) {
    this.type = type;
  }

  
  /**
   * Supported transports for the API (http and/or https).\n
   **/
  @ApiModelProperty(value = "Supported transports for the API (http and/or https).\n")
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
  @JsonProperty("policies")
  public List<String> getPolicies() {
    return policies;
  }
  public void setPolicies(List<String> policies) {
    this.policies = policies;
  }

  
  /**
   * The policy selected for the particular API
   **/
  @ApiModelProperty(value = "The policy selected for the particular API")
  @JsonProperty("apiPolicy")
  public String getApiPolicy() {
    return apiPolicy;
  }
  public void setApiPolicy(String apiPolicy) {
    this.apiPolicy = apiPolicy;
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
   * Types of API security, the current API secured with. It can be either OAuth2 or mutual SSL or both. If\nit is not set OAuth2 will be set as the security for the current API.\n
   **/
  @ApiModelProperty(value = "Types of API security, the current API secured with. It can be either OAuth2 or mutual SSL or both. If\nit is not set OAuth2 will be set as the security for the current API.\n")
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
  @JsonProperty("maxTps")
  public APIMaxTpsDTO getMaxTps() {
    return maxTps;
  }
  public void setMaxTps(APIMaxTpsDTO maxTps) {
    this.maxTps = maxTps;
  }

  
  /**
   * The visibility level of the API. Accepts one of the following. PUBLIC, PRIVATE, RESTRICTED.
   **/
  @ApiModelProperty(value = "The visibility level of the API. Accepts one of the following. PUBLIC, PRIVATE, RESTRICTED.")
  @JsonProperty("visibility")
  public VisibilityEnum getVisibility() {
    return visibility;
  }
  public void setVisibility(VisibilityEnum visibility) {
    this.visibility = visibility;
  }

  
  /**
   * The user roles that are able to access the API in Store
   **/
  @ApiModelProperty(value = "The user roles that are able to access the API in Store")
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
  @JsonProperty("visibleTenants")
  public List<String> getVisibleTenants() {
    return visibleTenants;
  }
  public void setVisibleTenants(List<String> visibleTenants) {
    this.visibleTenants = visibleTenants;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("endpointSecurity")
  public APIEndpointSecurityDTO getEndpointSecurity() {
    return endpointSecurity;
  }
  public void setEndpointSecurity(APIEndpointSecurityDTO endpointSecurity) {
    this.endpointSecurity = endpointSecurity;
  }

  
  /**
   * List of gateway environments the API is available\n
   **/
  @ApiModelProperty(value = "List of gateway environments the API is available\n")
  @JsonProperty("gatewayEnvironments")
  public List<String> getGatewayEnvironments() {
    return gatewayEnvironments;
  }
  public void setGatewayEnvironments(List<String> gatewayEnvironments) {
    this.gatewayEnvironments = gatewayEnvironments;
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
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("mediationPolicies")
  public List<MediationPolicyDTO> getMediationPolicies() {
    return mediationPolicies;
  }
  public void setMediationPolicies(List<MediationPolicyDTO> mediationPolicies) {
    this.mediationPolicies = mediationPolicies;
  }

  
  /**
   * The subscription availability. Accepts one of the following. current_tenant, all_tenants or specific_tenants.
   **/
  @ApiModelProperty(value = "The subscription availability. Accepts one of the following. current_tenant, all_tenants or specific_tenants.")
  @JsonProperty("subscriptionAvailability")
  public SubscriptionAvailabilityEnum getSubscriptionAvailability() {
    return subscriptionAvailability;
  }
  public void setSubscriptionAvailability(SubscriptionAvailabilityEnum subscriptionAvailability) {
    this.subscriptionAvailability = subscriptionAvailability;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("subscriptionAvailableTenants")
  public List<String> getSubscriptionAvailableTenants() {
    return subscriptionAvailableTenants;
  }
  public void setSubscriptionAvailableTenants(List<String> subscriptionAvailableTenants) {
    this.subscriptionAvailableTenants = subscriptionAvailableTenants;
  }

  
  /**
   * Map of custom properties of API
   **/
  @ApiModelProperty(value = "Map of custom properties of API")
  @JsonProperty("additionalProperties")
  public Map<String, String> getAdditionalProperties() {
    return additionalProperties;
  }
  public void setAdditionalProperties(Map<String, String> additionalProperties) {
    this.additionalProperties = additionalProperties;
  }

  
  /**
   * Is the API is restricted to certain set of publishers or creators or is it visible to all the\npublishers and creators. If the accessControl restriction is none, this API can be modified by all the\npublishers and creators, if not it can only be viewable/modifiable by certain set of publishers and creators,\n based on the restriction.\n
   **/
  @ApiModelProperty(value = "Is the API is restricted to certain set of publishers or creators or is it visible to all the\npublishers and creators. If the accessControl restriction is none, this API can be modified by all the\npublishers and creators, if not it can only be viewable/modifiable by certain set of publishers and creators,\n based on the restriction.\n")
  @JsonProperty("accessControl")
  public AccessControlEnum getAccessControl() {
    return accessControl;
  }
  public void setAccessControl(AccessControlEnum accessControl) {
    this.accessControl = accessControl;
  }

  
  /**
   * The user roles that are able to view/modify as API publisher or creator.
   **/
  @ApiModelProperty(value = "The user roles that are able to view/modify as API publisher or creator.")
  @JsonProperty("accessControlRoles")
  public List<String> getAccessControlRoles() {
    return accessControlRoles;
  }
  public void setAccessControlRoles(List<String> accessControlRoles) {
    this.accessControlRoles = accessControlRoles;
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
  @JsonProperty("scopes")
  public List<ScopeDTO> getScopes() {
    return scopes;
  }
  public void setScopes(List<ScopeDTO> scopes) {
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
    sb.append("  lifeCycleStatus: ").append(lifeCycleStatus).append("\n");
    sb.append("  wsdlUri: ").append(wsdlUri).append("\n");
    sb.append("  responseCaching: ").append(responseCaching).append("\n");
    sb.append("  cacheTimeout: ").append(cacheTimeout).append("\n");
    sb.append("  destinationStatsEnabled: ").append(destinationStatsEnabled).append("\n");
    sb.append("  isDefaultVersion: ").append(isDefaultVersion).append("\n");
    sb.append("  type: ").append(type).append("\n");
    sb.append("  transport: ").append(transport).append("\n");
    sb.append("  tags: ").append(tags).append("\n");
    sb.append("  policies: ").append(policies).append("\n");
    sb.append("  apiPolicy: ").append(apiPolicy).append("\n");
    sb.append("  authorizationHeader: ").append(authorizationHeader).append("\n");
    sb.append("  securityScheme: ").append(securityScheme).append("\n");
    sb.append("  maxTps: ").append(maxTps).append("\n");
    sb.append("  visibility: ").append(visibility).append("\n");
    sb.append("  visibleRoles: ").append(visibleRoles).append("\n");
    sb.append("  visibleTenants: ").append(visibleTenants).append("\n");
    sb.append("  endpointSecurity: ").append(endpointSecurity).append("\n");
    sb.append("  gatewayEnvironments: ").append(gatewayEnvironments).append("\n");
    sb.append("  labels: ").append(labels).append("\n");
    sb.append("  mediationPolicies: ").append(mediationPolicies).append("\n");
    sb.append("  subscriptionAvailability: ").append(subscriptionAvailability).append("\n");
    sb.append("  subscriptionAvailableTenants: ").append(subscriptionAvailableTenants).append("\n");
    sb.append("  additionalProperties: ").append(additionalProperties).append("\n");
    sb.append("  accessControl: ").append(accessControl).append("\n");
    sb.append("  accessControlRoles: ").append(accessControlRoles).append("\n");
    sb.append("  businessInformation: ").append(businessInformation).append("\n");
    sb.append("  corsConfiguration: ").append(corsConfiguration).append("\n");
    sb.append("  workflowStatus: ").append(workflowStatus).append("\n");
    sb.append("  createdTime: ").append(createdTime).append("\n");
    sb.append("  lastUpdatedTime: ").append(lastUpdatedTime).append("\n");
    sb.append("  endpoint: ").append(endpoint).append("\n");
    sb.append("  scopes: ").append(scopes).append("\n");
    sb.append("  operations: ").append(operations).append("\n");
    sb.append("  threatProtectionPolicies: ").append(threatProtectionPolicies).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
