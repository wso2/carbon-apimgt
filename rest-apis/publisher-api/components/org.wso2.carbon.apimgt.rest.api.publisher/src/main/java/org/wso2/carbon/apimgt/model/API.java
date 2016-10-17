package org.wso2.carbon.apimgt.model;

import org.wso2.carbon.apimgt.model.APICorsConfiguration;
import org.wso2.carbon.apimgt.model.APIMaxTps;
import org.wso2.carbon.apimgt.model.APIEndpointSecurity;
import org.wso2.carbon.apimgt.model.APIBusinessInformation;
import java.util.*;
import org.wso2.carbon.apimgt.model.Sequence;



@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-10-14T14:19:24.818+05:30")
public class API  {
  
  private String id = null;
  private String name = null;
  private String description = null;
  private String context = null;
  private String version = null;
  private String provider = null;
  private String apiDefinition = null;
  private String wsdlUri = null;
  private String status = null;
  private String responseCaching = null;
  private Integer cacheTimeout = null;
  private String destinationStatsEnabled = null;
  private Boolean isDefaultVersion = null;
  private List<String> transport = new ArrayList<String>();
  private List<String> tags = new ArrayList<String>();
  private List<String> tiers = new ArrayList<String>();
  private APIMaxTps maxTps = null;
  private String thumbnailUri = null;
  public enum VisibilityEnum {
     PUBLIC,  PRIVATE,  RESTRICTED,  CONTROLLED, 
  };
  private VisibilityEnum visibility = null;
  private List<String> visibleRoles = new ArrayList<String>();
  private List<String> visibleTenants = new ArrayList<String>();
  private String endpointConfig = null;
  private APIEndpointSecurity endpointSecurity = null;
  private String gatewayEnvironments = null;
  private List<Sequence> sequences = new ArrayList<Sequence>();
  public enum SubscriptionAvailabilityEnum {
     current_tenant,  all_tenants,  specific_tenants, 
  };
  private SubscriptionAvailabilityEnum subscriptionAvailability = null;
  private List<String> subscriptionAvailableTenants = new ArrayList<String>();
  private APIBusinessInformation businessInformation = null;
  private APICorsConfiguration corsConfiguration = null;

  /**
   * UUID of the api registry artifact

   **/
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   **/
  public String getContext() {
    return context;
  }
  public void setContext(String context) {
    this.context = context;
  }

  /**
   **/
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * If the provider value is not given user invoking the api will be used as the provider.

   **/
  public String getProvider() {
    return provider;
  }
  public void setProvider(String provider) {
    this.provider = provider;
  }

  /**
   * Swagger definition of the API which contains details about URI templates and scopes

   **/
  public String getApiDefinition() {
    return apiDefinition;
  }
  public void setApiDefinition(String apiDefinition) {
    this.apiDefinition = apiDefinition;
  }

  /**
   * WSDL URL if the API is based on a WSDL endpoint

   **/
  public String getWsdlUri() {
    return wsdlUri;
  }
  public void setWsdlUri(String wsdlUri) {
    this.wsdlUri = wsdlUri;
  }

  /**
   **/
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   **/
  public String getResponseCaching() {
    return responseCaching;
  }
  public void setResponseCaching(String responseCaching) {
    this.responseCaching = responseCaching;
  }

  /**
   **/
  public Integer getCacheTimeout() {
    return cacheTimeout;
  }
  public void setCacheTimeout(Integer cacheTimeout) {
    this.cacheTimeout = cacheTimeout;
  }

  /**
   **/
  public String getDestinationStatsEnabled() {
    return destinationStatsEnabled;
  }
  public void setDestinationStatsEnabled(String destinationStatsEnabled) {
    this.destinationStatsEnabled = destinationStatsEnabled;
  }

  /**
   **/
  public Boolean getIsDefaultVersion() {
    return isDefaultVersion;
  }
  public void setIsDefaultVersion(Boolean isDefaultVersion) {
    this.isDefaultVersion = isDefaultVersion;
  }

  /**
   * Supported transports for the API (http and/or https).

   **/
  public List<String> getTransport() {
    return transport;
  }
  public void setTransport(List<String> transport) {
    this.transport = transport;
  }

  /**
   **/
  public List<String> getTags() {
    return tags;
  }
  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  /**
   **/
  public List<String> getTiers() {
    return tiers;
  }
  public void setTiers(List<String> tiers) {
    this.tiers = tiers;
  }

  /**
   **/
  public APIMaxTps getMaxTps() {
    return maxTps;
  }
  public void setMaxTps(APIMaxTps maxTps) {
    this.maxTps = maxTps;
  }

  /**
   **/
  public String getThumbnailUri() {
    return thumbnailUri;
  }
  public void setThumbnailUri(String thumbnailUri) {
    this.thumbnailUri = thumbnailUri;
  }

  /**
   **/
  public VisibilityEnum getVisibility() {
    return visibility;
  }
  public void setVisibility(VisibilityEnum visibility) {
    this.visibility = visibility;
  }

  /**
   **/
  public List<String> getVisibleRoles() {
    return visibleRoles;
  }
  public void setVisibleRoles(List<String> visibleRoles) {
    this.visibleRoles = visibleRoles;
  }

  /**
   **/
  public List<String> getVisibleTenants() {
    return visibleTenants;
  }
  public void setVisibleTenants(List<String> visibleTenants) {
    this.visibleTenants = visibleTenants;
  }

  /**
   **/
  public String getEndpointConfig() {
    return endpointConfig;
  }
  public void setEndpointConfig(String endpointConfig) {
    this.endpointConfig = endpointConfig;
  }

  /**
   **/
  public APIEndpointSecurity getEndpointSecurity() {
    return endpointSecurity;
  }
  public void setEndpointSecurity(APIEndpointSecurity endpointSecurity) {
    this.endpointSecurity = endpointSecurity;
  }

  /**
   * Comma separated list of gateway environments.

   **/
  public String getGatewayEnvironments() {
    return gatewayEnvironments;
  }
  public void setGatewayEnvironments(String gatewayEnvironments) {
    this.gatewayEnvironments = gatewayEnvironments;
  }

  /**
   **/
  public List<Sequence> getSequences() {
    return sequences;
  }
  public void setSequences(List<Sequence> sequences) {
    this.sequences = sequences;
  }

  /**
   **/
  public SubscriptionAvailabilityEnum getSubscriptionAvailability() {
    return subscriptionAvailability;
  }
  public void setSubscriptionAvailability(SubscriptionAvailabilityEnum subscriptionAvailability) {
    this.subscriptionAvailability = subscriptionAvailability;
  }

  /**
   **/
  public List<String> getSubscriptionAvailableTenants() {
    return subscriptionAvailableTenants;
  }
  public void setSubscriptionAvailableTenants(List<String> subscriptionAvailableTenants) {
    this.subscriptionAvailableTenants = subscriptionAvailableTenants;
  }

  /**
   **/
  public APIBusinessInformation getBusinessInformation() {
    return businessInformation;
  }
  public void setBusinessInformation(APIBusinessInformation businessInformation) {
    this.businessInformation = businessInformation;
  }

  /**
   **/
  public APICorsConfiguration getCorsConfiguration() {
    return corsConfiguration;
  }
  public void setCorsConfiguration(APICorsConfiguration corsConfiguration) {
    this.corsConfiguration = corsConfiguration;
  }


  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class API {\n");
    
    sb.append("  id: ").append(id).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("  description: ").append(description).append("\n");
    sb.append("  context: ").append(context).append("\n");
    sb.append("  version: ").append(version).append("\n");
    sb.append("  provider: ").append(provider).append("\n");
    sb.append("  apiDefinition: ").append(apiDefinition).append("\n");
    sb.append("  wsdlUri: ").append(wsdlUri).append("\n");
    sb.append("  status: ").append(status).append("\n");
    sb.append("  responseCaching: ").append(responseCaching).append("\n");
    sb.append("  cacheTimeout: ").append(cacheTimeout).append("\n");
    sb.append("  destinationStatsEnabled: ").append(destinationStatsEnabled).append("\n");
    sb.append("  isDefaultVersion: ").append(isDefaultVersion).append("\n");
    sb.append("  transport: ").append(transport).append("\n");
    sb.append("  tags: ").append(tags).append("\n");
    sb.append("  tiers: ").append(tiers).append("\n");
    sb.append("  maxTps: ").append(maxTps).append("\n");
    sb.append("  thumbnailUri: ").append(thumbnailUri).append("\n");
    sb.append("  visibility: ").append(visibility).append("\n");
    sb.append("  visibleRoles: ").append(visibleRoles).append("\n");
    sb.append("  visibleTenants: ").append(visibleTenants).append("\n");
    sb.append("  endpointConfig: ").append(endpointConfig).append("\n");
    sb.append("  endpointSecurity: ").append(endpointSecurity).append("\n");
    sb.append("  gatewayEnvironments: ").append(gatewayEnvironments).append("\n");
    sb.append("  sequences: ").append(sequences).append("\n");
    sb.append("  subscriptionAvailability: ").append(subscriptionAvailability).append("\n");
    sb.append("  subscriptionAvailableTenants: ").append(subscriptionAvailableTenants).append("\n");
    sb.append("  businessInformation: ").append(businessInformation).append("\n");
    sb.append("  corsConfiguration: ").append(corsConfiguration).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
