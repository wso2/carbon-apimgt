package org.wso2.carbon.apimgt.model;

import org.wso2.carbon.apimgt.model.APIBusinessInformation;
import java.util.*;



@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-10-14T14:19:24.818+05:30")
public class APIProduct  {
  
  private String id = null;
  private String name = null;
  private String version = null;
  private String description = null;
  private String provider = null;
  public enum VisibilityEnum {
     PUBLIC,  PRIVATE,  RESTRICTED,  CONTROLLED, 
  };
  private VisibilityEnum visibility = null;
  private List<String> visibleRoles = new ArrayList<String>();
  private List<String> visibleTenants = new ArrayList<String>();
  private List<String> tags = new ArrayList<String>();
  private String thumbnailUri = null;
  private List<String> throttlingTier = new ArrayList<String>();
  private String status = null;
  private List<String> apis = new ArrayList<String>();
  private APIBusinessInformation businessInformation = null;
  public enum SubscriptionAvailabilityEnum {
     current_tenant,  all_tenants,  specific_tenants, 
  };
  private SubscriptionAvailabilityEnum subscriptionAvailability = null;
  private List<String> subscriptionAvailableTenants = new ArrayList<String>();

  /**
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
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
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
  public String getProvider() {
    return provider;
  }
  public void setProvider(String provider) {
    this.provider = provider;
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
  public List<String> getTags() {
    return tags;
  }
  public void setTags(List<String> tags) {
    this.tags = tags;
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
  public List<String> getThrottlingTier() {
    return throttlingTier;
  }
  public void setThrottlingTier(List<String> throttlingTier) {
    this.throttlingTier = throttlingTier;
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
  public List<String> getApis() {
    return apis;
  }
  public void setApis(List<String> apis) {
    this.apis = apis;
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


  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIProduct {\n");
    
    sb.append("  id: ").append(id).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("  version: ").append(version).append("\n");
    sb.append("  description: ").append(description).append("\n");
    sb.append("  provider: ").append(provider).append("\n");
    sb.append("  visibility: ").append(visibility).append("\n");
    sb.append("  visibleRoles: ").append(visibleRoles).append("\n");
    sb.append("  visibleTenants: ").append(visibleTenants).append("\n");
    sb.append("  tags: ").append(tags).append("\n");
    sb.append("  thumbnailUri: ").append(thumbnailUri).append("\n");
    sb.append("  throttlingTier: ").append(throttlingTier).append("\n");
    sb.append("  status: ").append(status).append("\n");
    sb.append("  apis: ").append(apis).append("\n");
    sb.append("  businessInformation: ").append(businessInformation).append("\n");
    sb.append("  subscriptionAvailability: ").append(subscriptionAvailability).append("\n");
    sb.append("  subscriptionAvailableTenants: ").append(subscriptionAvailableTenants).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
