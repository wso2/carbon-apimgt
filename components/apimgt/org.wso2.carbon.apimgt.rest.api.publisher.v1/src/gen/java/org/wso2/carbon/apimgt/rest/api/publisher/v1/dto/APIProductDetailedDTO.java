package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductBusinessInformationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ProductAPIDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class APIProductDetailedDTO extends APIProductInfoDTO {
  
  
  
  private String apiProductDefinition = null;
  
  public enum VisibilityEnum {
     PUBLIC,  PRIVATE,  RESTRICTED,  CONTROLLED, 
  };
  
  private VisibilityEnum visibility = null;
  
  
  private List<String> visibleRoles = new ArrayList<String>();
  
  
  private List<String> visibleTenants = new ArrayList<String>();
  
  
  private List<String> tiers = new ArrayList<String>();
  
  public enum SubscriptionAvailabilityEnum {
     current_tenant,  all_tenants,  specific_tenants, 
  };
  
  private SubscriptionAvailabilityEnum subscriptionAvailability = null;
  
  
  private List<String> subscriptionAvailableTenants = new ArrayList<String>();
  
  
  private Map<String, String> additionalProperties = new HashMap<String, String>();
  
  
  private APIProductBusinessInformationDTO businessInformation = null;
  
  
  private List<ProductAPIDTO> apis = new ArrayList<ProductAPIDTO>();

  
  /**
   * Swagger definition of the API Product which contains details about URI templates and scopes\n
   **/
  @ApiModelProperty(value = "Swagger definition of the API Product which contains details about URI templates and scopes\n")
  @JsonProperty("apiProductDefinition")
  public String getApiProductDefinition() {
    return apiProductDefinition;
  }
  public void setApiProductDefinition(String apiProductDefinition) {
    this.apiProductDefinition = apiProductDefinition;
  }

  
  /**
   * The visibility level of the API. Accepts one of the following. PUBLIC, PRIVATE, RESTRICTED OR CONTROLLED.
   **/
  @ApiModelProperty(value = "The visibility level of the API. Accepts one of the following. PUBLIC, PRIVATE, RESTRICTED OR CONTROLLED.")
  @JsonProperty("visibility")
  public VisibilityEnum getVisibility() {
    return visibility;
  }
  public void setVisibility(VisibilityEnum visibility) {
    this.visibility = visibility;
  }

  
  /**
   * The user roles that are able to access the API
   **/
  @ApiModelProperty(value = "The user roles that are able to access the API")
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
   * The subscription tiers selected for the particular API Product
   **/
  @ApiModelProperty(value = "The subscription tiers selected for the particular API Product")
  @JsonProperty("tiers")
  public List<String> getTiers() {
    return tiers;
  }
  public void setTiers(List<String> tiers) {
    this.tiers = tiers;
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
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("businessInformation")
  public APIProductBusinessInformationDTO getBusinessInformation() {
    return businessInformation;
  }
  public void setBusinessInformation(APIProductBusinessInformationDTO businessInformation) {
    this.businessInformation = businessInformation;
  }

  
  /**
   * APIs and resources in the API Product.\n
   **/
  @ApiModelProperty(value = "APIs and resources in the API Product.\n")
  @JsonProperty("apis")
  public List<ProductAPIDTO> getApis() {
    return apis;
  }
  public void setApis(List<ProductAPIDTO> apis) {
    this.apis = apis;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIProductDetailedDTO {\n");
    sb.append("  " + super.toString()).append("\n");
    sb.append("  apiProductDefinition: ").append(apiProductDefinition).append("\n");
    sb.append("  visibility: ").append(visibility).append("\n");
    sb.append("  visibleRoles: ").append(visibleRoles).append("\n");
    sb.append("  visibleTenants: ").append(visibleTenants).append("\n");
    sb.append("  tiers: ").append(tiers).append("\n");
    sb.append("  subscriptionAvailability: ").append(subscriptionAvailability).append("\n");
    sb.append("  subscriptionAvailableTenants: ").append(subscriptionAvailableTenants).append("\n");
    sb.append("  additionalProperties: ").append(additionalProperties).append("\n");
    sb.append("  businessInformation: ").append(businessInformation).append("\n");
    sb.append("  apis: ").append(apis).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
