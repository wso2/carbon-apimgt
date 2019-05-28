package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductBusinessInformationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ProductAPIDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;



public class APIProductDTO   {
  
    private String id = null;
    private String name = null;
    private String description = null;
    private String provider = null;
    private String thumbnailUri = null;

@XmlType(name="StateEnum")
@XmlEnum(String.class)
public enum StateEnum {

    @XmlEnumValue("CREATED") CREATED(String.valueOf("CREATED")), @XmlEnumValue("PUBLISHED") PUBLISHED(String.valueOf("PUBLISHED"));


    private String value;

    StateEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static StateEnum fromValue(String v) {
        for (StateEnum b : StateEnum.values()) {
            if (String.valueOf(b.value).equals(v)) {
                return b;
            }
        }
        return null;
    }
}

    private StateEnum state = null;

@XmlType(name="VisibilityEnum")
@XmlEnum(String.class)
public enum VisibilityEnum {

    @XmlEnumValue("PUBLIC") PUBLIC(String.valueOf("PUBLIC")), @XmlEnumValue("PRIVATE") PRIVATE(String.valueOf("PRIVATE")), @XmlEnumValue("RESTRICTED") RESTRICTED(String.valueOf("RESTRICTED"));


    private String value;

    VisibilityEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static VisibilityEnum fromValue(String v) {
        for (VisibilityEnum b : VisibilityEnum.values()) {
            if (String.valueOf(b.value).equals(v)) {
                return b;
            }
        }
        return null;
    }
}

    private VisibilityEnum visibility = null;
    private List<String> visibleRoles = new ArrayList<>();
    private List<String> visibleTenants = new ArrayList<>();
    private List<String> policies = new ArrayList<>();

@XmlType(name="SubscriptionAvailabilityEnum")
@XmlEnum(String.class)
public enum SubscriptionAvailabilityEnum {

    @XmlEnumValue("current_tenant") CURRENT_TENANT(String.valueOf("current_tenant")), @XmlEnumValue("all_tenants") ALL_TENANTS(String.valueOf("all_tenants")), @XmlEnumValue("specific_tenants") SPECIFIC_TENANTS(String.valueOf("specific_tenants"));


    private String value;

    SubscriptionAvailabilityEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static SubscriptionAvailabilityEnum fromValue(String v) {
        for (SubscriptionAvailabilityEnum b : SubscriptionAvailabilityEnum.values()) {
            if (String.valueOf(b.value).equals(v)) {
                return b;
            }
        }
        return null;
    }
}

    private SubscriptionAvailabilityEnum subscriptionAvailability = null;
    private List<String> subscriptionAvailableTenants = new ArrayList<>();
    private Map<String, String> additionalProperties = new HashMap<>();
    private APIProductBusinessInformationDTO businessInformation = null;
    private List<ProductAPIDTO> apis = new ArrayList<>();

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
   * Name of the API Product
   **/
  public APIProductDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "CalculatorAPIProduct", required = true, value = "Name of the API Product")
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
  public APIProductDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "A calculator API Product that supports basic operations", value = "A brief description about the API")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * If the provider value is not given, the user invoking the API will be used as the provider. 
   **/
  public APIProductDTO provider(String provider) {
    this.provider = provider;
    return this;
  }

  
  @ApiModelProperty(example = "admin", value = "If the provider value is not given, the user invoking the API will be used as the provider. ")
  @JsonProperty("provider")
  public String getProvider() {
    return provider;
  }
  public void setProvider(String provider) {
    this.provider = provider;
  }

  /**
   **/
  public APIProductDTO thumbnailUri(String thumbnailUri) {
    this.thumbnailUri = thumbnailUri;
    return this;
  }

  
  @ApiModelProperty(example = "/api-products/01234567-0123-0123-0123-012345678901/thumbnail", value = "")
  @JsonProperty("thumbnailUri")
  public String getThumbnailUri() {
    return thumbnailUri;
  }
  public void setThumbnailUri(String thumbnailUri) {
    this.thumbnailUri = thumbnailUri;
  }

  /**
   * State of the API product. Only published api products are visible on the store 
   **/
  public APIProductDTO state(StateEnum state) {
    this.state = state;
    return this;
  }

  
  @ApiModelProperty(value = "State of the API product. Only published api products are visible on the store ")
  @JsonProperty("state")
  public StateEnum getState() {
    return state;
  }
  public void setState(StateEnum state) {
    this.state = state;
  }

  /**
   * The visibility level of the API. Accepts one of the following. PUBLIC, PRIVATE, RESTRICTED.
   **/
  public APIProductDTO visibility(VisibilityEnum visibility) {
    this.visibility = visibility;
    return this;
  }

  
  @ApiModelProperty(example = "PUBLIC", value = "The visibility level of the API. Accepts one of the following. PUBLIC, PRIVATE, RESTRICTED.")
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
  public APIProductDTO visibleRoles(List<String> visibleRoles) {
    this.visibleRoles = visibleRoles;
    return this;
  }

  
  @ApiModelProperty(example = "[]", value = "The user roles that are able to access the API")
  @JsonProperty("visibleRoles")
  public List<String> getVisibleRoles() {
    return visibleRoles;
  }
  public void setVisibleRoles(List<String> visibleRoles) {
    this.visibleRoles = visibleRoles;
  }

  /**
   **/
  public APIProductDTO visibleTenants(List<String> visibleTenants) {
    this.visibleTenants = visibleTenants;
    return this;
  }

  
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
  public APIProductDTO policies(List<String> policies) {
    this.policies = policies;
    return this;
  }

  
  @ApiModelProperty(example = "[\"Unlimited\"]", value = "")
  @JsonProperty("policies")
  public List<String> getPolicies() {
    return policies;
  }
  public void setPolicies(List<String> policies) {
    this.policies = policies;
  }

  /**
   * The subscription availability. Accepts one of the following. current_tenant, all_tenants or specific_tenants.
   **/
  public APIProductDTO subscriptionAvailability(SubscriptionAvailabilityEnum subscriptionAvailability) {
    this.subscriptionAvailability = subscriptionAvailability;
    return this;
  }

  
  @ApiModelProperty(example = "current_tenant", value = "The subscription availability. Accepts one of the following. current_tenant, all_tenants or specific_tenants.")
  @JsonProperty("subscriptionAvailability")
  public SubscriptionAvailabilityEnum getSubscriptionAvailability() {
    return subscriptionAvailability;
  }
  public void setSubscriptionAvailability(SubscriptionAvailabilityEnum subscriptionAvailability) {
    this.subscriptionAvailability = subscriptionAvailability;
  }

  /**
   **/
  public APIProductDTO subscriptionAvailableTenants(List<String> subscriptionAvailableTenants) {
    this.subscriptionAvailableTenants = subscriptionAvailableTenants;
    return this;
  }

  
  @ApiModelProperty(example = "[\"tenant1\",\"tenant2\"]", value = "")
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
  public APIProductDTO additionalProperties(Map<String, String> additionalProperties) {
    this.additionalProperties = additionalProperties;
    return this;
  }

  
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
   * APIs and resources in the API Product. 
   **/
  public APIProductDTO apis(List<ProductAPIDTO> apis) {
    this.apis = apis;
    return this;
  }

  
  @ApiModelProperty(value = "APIs and resources in the API Product. ")
  @JsonProperty("apis")
  public List<ProductAPIDTO> getApis() {
    return apis;
  }
  public void setApis(List<ProductAPIDTO> apis) {
    this.apis = apis;
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
        Objects.equals(provider, apIProduct.provider) &&
        Objects.equals(thumbnailUri, apIProduct.thumbnailUri) &&
        Objects.equals(state, apIProduct.state) &&
        Objects.equals(visibility, apIProduct.visibility) &&
        Objects.equals(visibleRoles, apIProduct.visibleRoles) &&
        Objects.equals(visibleTenants, apIProduct.visibleTenants) &&
        Objects.equals(policies, apIProduct.policies) &&
        Objects.equals(subscriptionAvailability, apIProduct.subscriptionAvailability) &&
        Objects.equals(subscriptionAvailableTenants, apIProduct.subscriptionAvailableTenants) &&
        Objects.equals(additionalProperties, apIProduct.additionalProperties) &&
        Objects.equals(businessInformation, apIProduct.businessInformation) &&
        Objects.equals(apis, apIProduct.apis);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, provider, thumbnailUri, state, visibility, visibleRoles, visibleTenants, policies, subscriptionAvailability, subscriptionAvailableTenants, additionalProperties, businessInformation, apis);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIProductDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
    sb.append("    thumbnailUri: ").append(toIndentedString(thumbnailUri)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
    sb.append("    visibility: ").append(toIndentedString(visibility)).append("\n");
    sb.append("    visibleRoles: ").append(toIndentedString(visibleRoles)).append("\n");
    sb.append("    visibleTenants: ").append(toIndentedString(visibleTenants)).append("\n");
    sb.append("    policies: ").append(toIndentedString(policies)).append("\n");
    sb.append("    subscriptionAvailability: ").append(toIndentedString(subscriptionAvailability)).append("\n");
    sb.append("    subscriptionAvailableTenants: ").append(toIndentedString(subscriptionAvailableTenants)).append("\n");
    sb.append("    additionalProperties: ").append(toIndentedString(additionalProperties)).append("\n");
    sb.append("    businessInformation: ").append(toIndentedString(businessInformation)).append("\n");
    sb.append("    apis: ").append(toIndentedString(apis)).append("\n");
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

