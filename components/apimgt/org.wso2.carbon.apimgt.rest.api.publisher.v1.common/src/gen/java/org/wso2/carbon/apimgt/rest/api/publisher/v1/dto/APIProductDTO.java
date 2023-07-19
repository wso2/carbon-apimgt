package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APICorsConfigurationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIInfoAdditionalPropertiesDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIInfoAdditionalPropertiesMapDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIMonetizationInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductBusinessInformationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIScopeDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ProductAPIDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class APIProductDTO   {
  
    private String id = null;
    private String name = null;
    private String context = null;
    private String description = null;
    private String provider = null;
    private Boolean hasThumbnail = null;
    private String state = "CREATED";
    private Boolean enableSchemaValidation = null;
    private Boolean isRevision = null;
    private String revisionedApiProductId = null;
    private Integer revisionId = null;
    private Boolean responseCachingEnabled = null;
    private Integer cacheTimeout = null;

    @XmlType(name="VisibilityEnum")
    @XmlEnum(String.class)
    public enum VisibilityEnum {
        PUBLIC("PUBLIC"),
        PRIVATE("PRIVATE"),
        RESTRICTED("RESTRICTED");
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

        @JsonCreator
        public static VisibilityEnum fromValue(String v) {
            for (VisibilityEnum b : VisibilityEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private VisibilityEnum visibility = VisibilityEnum.PUBLIC;
    private List<String> visibleRoles = new ArrayList<String>();
    private List<String> visibleTenants = new ArrayList<String>();

    @XmlType(name="AccessControlEnum")
    @XmlEnum(String.class)
    public enum AccessControlEnum {
        NONE("NONE"),
        RESTRICTED("RESTRICTED");
        private String value;

        AccessControlEnum (String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static AccessControlEnum fromValue(String v) {
            for (AccessControlEnum b : AccessControlEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private AccessControlEnum accessControl = AccessControlEnum.NONE;
    private List<String> accessControlRoles = new ArrayList<String>();

    @XmlType(name="ApiTypeEnum")
    @XmlEnum(String.class)
    public enum ApiTypeEnum {
        API("API"),
        APIPRODUCT("APIPRODUCT");
        private String value;

        ApiTypeEnum (String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static ApiTypeEnum fromValue(String v) {
            for (ApiTypeEnum b : ApiTypeEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private ApiTypeEnum apiType = null;
    private List<String> transport = new ArrayList<String>();
    private List<String> tags = new ArrayList<String>();
    private List<String> policies = new ArrayList<String>();
    private String apiThrottlingPolicy = null;
    private String authorizationHeader = null;
    private List<String> securityScheme = new ArrayList<String>();

    @XmlType(name="SubscriptionAvailabilityEnum")
    @XmlEnum(String.class)
    public enum SubscriptionAvailabilityEnum {
        CURRENT_TENANT("CURRENT_TENANT"),
        ALL_TENANTS("ALL_TENANTS"),
        SPECIFIC_TENANTS("SPECIFIC_TENANTS");
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

        @JsonCreator
        public static SubscriptionAvailabilityEnum fromValue(String v) {
            for (SubscriptionAvailabilityEnum b : SubscriptionAvailabilityEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private SubscriptionAvailabilityEnum subscriptionAvailability = SubscriptionAvailabilityEnum.ALL_TENANTS;
    @Scope(name = "apim:api_publish", description="", value ="")
    @Scope(name = "apim:api_manage", description="", value ="")
    private List<String> subscriptionAvailableTenants = new ArrayList<String>();
    private List<APIInfoAdditionalPropertiesDTO> additionalProperties = new ArrayList<APIInfoAdditionalPropertiesDTO>();
    private Map<String, APIInfoAdditionalPropertiesMapDTO> additionalPropertiesMap = new HashMap<String, APIInfoAdditionalPropertiesMapDTO>();
    private APIMonetizationInfoDTO monetization = null;
    private APIProductBusinessInformationDTO businessInformation = null;
    private APICorsConfigurationDTO corsConfiguration = null;
    private String createdTime = null;
    private String lastUpdatedTime = null;
    private String lastUpdatedTimestamp = null;
    private String gatewayVendor = null;
    private List<ProductAPIDTO> apis = new ArrayList<ProductAPIDTO>();
    private List<APIScopeDTO> scopes = new ArrayList<APIScopeDTO>();
    private List<String> categories = new ArrayList<String>();
    private String workflowStatus = null;

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

  
  @ApiModelProperty(example = "PizzaShackAPIProduct", required = true, value = "Name of the API Product")
  @JsonProperty("name")
  @NotNull
 @Size(min=1,max=50)  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public APIProductDTO context(String context) {
    this.context = context;
    return this;
  }

  
  @ApiModelProperty(example = "pizzaproduct", value = "")
  @JsonProperty("context")
 @Size(min=1,max=60)  public String getContext() {
    return context;
  }
  public void setContext(String context) {
    this.context = context;
  }

  /**
   * A brief description about the API
   **/
  public APIProductDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "This is a simple API for Pizza Shack online pizza delivery store", value = "A brief description about the API")
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
 @Size(max=50)  public String getProvider() {
    return provider;
  }
  public void setProvider(String provider) {
    this.provider = provider;
  }

  /**
   **/
  public APIProductDTO hasThumbnail(Boolean hasThumbnail) {
    this.hasThumbnail = hasThumbnail;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "")
  @JsonProperty("hasThumbnail")
  public Boolean isHasThumbnail() {
    return hasThumbnail;
  }
  public void setHasThumbnail(Boolean hasThumbnail) {
    this.hasThumbnail = hasThumbnail;
  }

  /**
   * State of the API product. Only published API products are visible on the Developer Portal 
   **/
  public APIProductDTO state(String state) {
    this.state = state;
    return this;
  }

  
  @ApiModelProperty(example = "CREATED", value = "State of the API product. Only published API products are visible on the Developer Portal ")
  @JsonProperty("state")
  public String getState() {
    return state;
  }
  public void setState(String state) {
    this.state = state;
  }

  /**
   **/
  public APIProductDTO enableSchemaValidation(Boolean enableSchemaValidation) {
    this.enableSchemaValidation = enableSchemaValidation;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "")
  @JsonProperty("enableSchemaValidation")
  public Boolean isEnableSchemaValidation() {
    return enableSchemaValidation;
  }
  public void setEnableSchemaValidation(Boolean enableSchemaValidation) {
    this.enableSchemaValidation = enableSchemaValidation;
  }

  /**
   **/
  public APIProductDTO isRevision(Boolean isRevision) {
    this.isRevision = isRevision;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "")
  @JsonProperty("isRevision")
  public Boolean isIsRevision() {
    return isRevision;
  }
  public void setIsRevision(Boolean isRevision) {
    this.isRevision = isRevision;
  }

  /**
   * UUID of the api product registry artifact 
   **/
  public APIProductDTO revisionedApiProductId(String revisionedApiProductId) {
    this.revisionedApiProductId = revisionedApiProductId;
    return this;
  }

  
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "UUID of the api product registry artifact ")
  @JsonProperty("revisionedApiProductId")
  public String getRevisionedApiProductId() {
    return revisionedApiProductId;
  }
  public void setRevisionedApiProductId(String revisionedApiProductId) {
    this.revisionedApiProductId = revisionedApiProductId;
  }

  /**
   **/
  public APIProductDTO revisionId(Integer revisionId) {
    this.revisionId = revisionId;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "")
  @JsonProperty("revisionId")
  public Integer getRevisionId() {
    return revisionId;
  }
  public void setRevisionId(Integer revisionId) {
    this.revisionId = revisionId;
  }

  /**
   **/
  public APIProductDTO responseCachingEnabled(Boolean responseCachingEnabled) {
    this.responseCachingEnabled = responseCachingEnabled;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "")
  @JsonProperty("responseCachingEnabled")
  public Boolean isResponseCachingEnabled() {
    return responseCachingEnabled;
  }
  public void setResponseCachingEnabled(Boolean responseCachingEnabled) {
    this.responseCachingEnabled = responseCachingEnabled;
  }

  /**
   **/
  public APIProductDTO cacheTimeout(Integer cacheTimeout) {
    this.cacheTimeout = cacheTimeout;
    return this;
  }

  
  @ApiModelProperty(example = "300", value = "")
  @JsonProperty("cacheTimeout")
  public Integer getCacheTimeout() {
    return cacheTimeout;
  }
  public void setCacheTimeout(Integer cacheTimeout) {
    this.cacheTimeout = cacheTimeout;
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

  
  @ApiModelProperty(example = "[]", value = "")
  @JsonProperty("visibleTenants")
  public List<String> getVisibleTenants() {
    return visibleTenants;
  }
  public void setVisibleTenants(List<String> visibleTenants) {
    this.visibleTenants = visibleTenants;
  }

  /**
   * Defines whether the API Product is restricted to certain set of publishers or creators or is it visible to all the publishers and creators. If the accessControl restriction is none, this API Product can be modified by all the publishers and creators, if not it can only be viewable/modifiable by certain set of publishers and creators, based on the restriction. 
   **/
  public APIProductDTO accessControl(AccessControlEnum accessControl) {
    this.accessControl = accessControl;
    return this;
  }

  
  @ApiModelProperty(value = "Defines whether the API Product is restricted to certain set of publishers or creators or is it visible to all the publishers and creators. If the accessControl restriction is none, this API Product can be modified by all the publishers and creators, if not it can only be viewable/modifiable by certain set of publishers and creators, based on the restriction. ")
  @JsonProperty("accessControl")
  public AccessControlEnum getAccessControl() {
    return accessControl;
  }
  public void setAccessControl(AccessControlEnum accessControl) {
    this.accessControl = accessControl;
  }

  /**
   * The user roles that are able to view/modify as API Product publisher or creator.
   **/
  public APIProductDTO accessControlRoles(List<String> accessControlRoles) {
    this.accessControlRoles = accessControlRoles;
    return this;
  }

  
  @ApiModelProperty(example = "[]", value = "The user roles that are able to view/modify as API Product publisher or creator.")
  @JsonProperty("accessControlRoles")
  public List<String> getAccessControlRoles() {
    return accessControlRoles;
  }
  public void setAccessControlRoles(List<String> accessControlRoles) {
    this.accessControlRoles = accessControlRoles;
  }

  /**
   * The API type to be used. Accepted values are API, APIPRODUCT
   **/
  public APIProductDTO apiType(ApiTypeEnum apiType) {
    this.apiType = apiType;
    return this;
  }

  
  @ApiModelProperty(example = "APIPRODUCT", value = "The API type to be used. Accepted values are API, APIPRODUCT")
  @JsonProperty("apiType")
  public ApiTypeEnum getApiType() {
    return apiType;
  }
  public void setApiType(ApiTypeEnum apiType) {
    this.apiType = apiType;
  }

  /**
   * Supported transports for the API (http and/or https). 
   **/
  public APIProductDTO transport(List<String> transport) {
    this.transport = transport;
    return this;
  }

  
  @ApiModelProperty(example = "[\"http\",\"https\"]", value = "Supported transports for the API (http and/or https). ")
  @JsonProperty("transport")
  public List<String> getTransport() {
    return transport;
  }
  public void setTransport(List<String> transport) {
    this.transport = transport;
  }

  /**
   **/
  public APIProductDTO tags(List<String> tags) {
    this.tags = tags;
    return this;
  }

  
  @ApiModelProperty(example = "[\"pizza\",\"food\"]", value = "")
  @JsonProperty("tags")
  public List<String> getTags() {
    return tags;
  }
  public void setTags(List<String> tags) {
    this.tags = tags;
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
   * The API level throttling policy selected for the particular API Product
   **/
  public APIProductDTO apiThrottlingPolicy(String apiThrottlingPolicy) {
    this.apiThrottlingPolicy = apiThrottlingPolicy;
    return this;
  }

  
  @ApiModelProperty(example = "Unlimited", value = "The API level throttling policy selected for the particular API Product")
  @JsonProperty("apiThrottlingPolicy")
  public String getApiThrottlingPolicy() {
    return apiThrottlingPolicy;
  }
  public void setApiThrottlingPolicy(String apiThrottlingPolicy) {
    this.apiThrottlingPolicy = apiThrottlingPolicy;
  }

  /**
   * Name of the Authorization header used for invoking the API. If it is not set, Authorization header name specified in tenant or system level will be used. 
   **/
  public APIProductDTO authorizationHeader(String authorizationHeader) {
    this.authorizationHeader = authorizationHeader;
    return this;
  }

  
  @ApiModelProperty(example = "Authorization", value = "Name of the Authorization header used for invoking the API. If it is not set, Authorization header name specified in tenant or system level will be used. ")
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
  public APIProductDTO securityScheme(List<String> securityScheme) {
    this.securityScheme = securityScheme;
    return this;
  }

  
  @ApiModelProperty(example = "[\"oauth2\"]", value = "Types of API security, the current API secured with. It can be either OAuth2 or mutual SSL or both. If it is not set OAuth2 will be set as the security for the current API. ")
  @JsonProperty("securityScheme")
  public List<String> getSecurityScheme() {
    return securityScheme;
  }
  public void setSecurityScheme(List<String> securityScheme) {
    this.securityScheme = securityScheme;
  }

  /**
   * The subscription availability. Accepts one of the following. CURRENT_TENANT, ALL_TENANTS or SPECIFIC_TENANTS.
   **/
  public APIProductDTO subscriptionAvailability(SubscriptionAvailabilityEnum subscriptionAvailability) {
    this.subscriptionAvailability = subscriptionAvailability;
    return this;
  }

  
  @ApiModelProperty(example = "CURRENT_TENANT", value = "The subscription availability. Accepts one of the following. CURRENT_TENANT, ALL_TENANTS or SPECIFIC_TENANTS.")
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

  
  @ApiModelProperty(example = "[]", value = "")
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
  public APIProductDTO additionalProperties(List<APIInfoAdditionalPropertiesDTO> additionalProperties) {
    this.additionalProperties = additionalProperties;
    return this;
  }

  
  @ApiModelProperty(value = "Map of custom properties of API")
      @Valid
  @JsonProperty("additionalProperties")
  public List<APIInfoAdditionalPropertiesDTO> getAdditionalProperties() {
    return additionalProperties;
  }
  public void setAdditionalProperties(List<APIInfoAdditionalPropertiesDTO> additionalProperties) {
    this.additionalProperties = additionalProperties;
  }

  /**
   **/
  public APIProductDTO additionalPropertiesMap(Map<String, APIInfoAdditionalPropertiesMapDTO> additionalPropertiesMap) {
    this.additionalPropertiesMap = additionalPropertiesMap;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("additionalPropertiesMap")
  public Map<String, APIInfoAdditionalPropertiesMapDTO> getAdditionalPropertiesMap() {
    return additionalPropertiesMap;
  }
  public void setAdditionalPropertiesMap(Map<String, APIInfoAdditionalPropertiesMapDTO> additionalPropertiesMap) {
    this.additionalPropertiesMap = additionalPropertiesMap;
  }

  /**
   **/
  public APIProductDTO monetization(APIMonetizationInfoDTO monetization) {
    this.monetization = monetization;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("monetization")
  public APIMonetizationInfoDTO getMonetization() {
    return monetization;
  }
  public void setMonetization(APIMonetizationInfoDTO monetization) {
    this.monetization = monetization;
  }

  /**
   **/
  public APIProductDTO businessInformation(APIProductBusinessInformationDTO businessInformation) {
    this.businessInformation = businessInformation;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("businessInformation")
  public APIProductBusinessInformationDTO getBusinessInformation() {
    return businessInformation;
  }
  public void setBusinessInformation(APIProductBusinessInformationDTO businessInformation) {
    this.businessInformation = businessInformation;
  }

  /**
   **/
  public APIProductDTO corsConfiguration(APICorsConfigurationDTO corsConfiguration) {
    this.corsConfiguration = corsConfiguration;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("corsConfiguration")
  public APICorsConfigurationDTO getCorsConfiguration() {
    return corsConfiguration;
  }
  public void setCorsConfiguration(APICorsConfigurationDTO corsConfiguration) {
    this.corsConfiguration = corsConfiguration;
  }

  /**
   **/
  public APIProductDTO createdTime(String createdTime) {
    this.createdTime = createdTime;
    return this;
  }

  
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
  public APIProductDTO lastUpdatedTime(String lastUpdatedTime) {
    this.lastUpdatedTime = lastUpdatedTime;
    return this;
  }

  
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
  public APIProductDTO lastUpdatedTimestamp(String lastUpdatedTimestamp) {
    this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("lastUpdatedTimestamp")
  public String getLastUpdatedTimestamp() {
    return lastUpdatedTimestamp;
  }
  public void setLastUpdatedTimestamp(String lastUpdatedTimestamp) {
    this.lastUpdatedTimestamp = lastUpdatedTimestamp;
  }

  /**
   **/
  public APIProductDTO gatewayVendor(String gatewayVendor) {
    this.gatewayVendor = gatewayVendor;
    return this;
  }

  
  @ApiModelProperty(example = "wso2", value = "")
  @JsonProperty("gatewayVendor")
  public String getGatewayVendor() {
    return gatewayVendor;
  }
  public void setGatewayVendor(String gatewayVendor) {
    this.gatewayVendor = gatewayVendor;
  }

  /**
   * APIs and resources in the API Product. 
   **/
  public APIProductDTO apis(List<ProductAPIDTO> apis) {
    this.apis = apis;
    return this;
  }

  
  @ApiModelProperty(example = "[{\"name\":\"PizzaShackAPI\",\"apiId\":\"01234567-0123-0123-0123-012345678901\",\"version\":\"1.0\",\"operations\":[{\"target\":\"/order/{orderId}\",\"verb\":\"POST\",\"authType\":\"Application & Application User\",\"throttlingPolicy\":\"Unlimited\"},{\"target\":\"/menu\",\"verb\":\"GET\",\"authType\":\"Application & Application User\",\"throttlingPolicy\":\"Unlimited\"}]}]", value = "APIs and resources in the API Product. ")
      @Valid
  @JsonProperty("apis")
  public List<ProductAPIDTO> getApis() {
    return apis;
  }
  public void setApis(List<ProductAPIDTO> apis) {
    this.apis = apis;
  }

  /**
   **/
  public APIProductDTO scopes(List<APIScopeDTO> scopes) {
    this.scopes = scopes;
    return this;
  }

  
  @ApiModelProperty(example = "[]", value = "")
      @Valid
  @JsonProperty("scopes")
  public List<APIScopeDTO> getScopes() {
    return scopes;
  }
  public void setScopes(List<APIScopeDTO> scopes) {
    this.scopes = scopes;
  }

  /**
   * API categories 
   **/
  public APIProductDTO categories(List<String> categories) {
    this.categories = categories;
    return this;
  }

  
  @ApiModelProperty(example = "[]", value = "API categories ")
  @JsonProperty("categories")
  public List<String> getCategories() {
    return categories;
  }
  public void setCategories(List<String> categories) {
    this.categories = categories;
  }

  /**
   **/
  public APIProductDTO workflowStatus(String workflowStatus) {
    this.workflowStatus = workflowStatus;
    return this;
  }

  
  @ApiModelProperty(example = "APPROVED", value = "")
  @JsonProperty("workflowStatus")
  public String getWorkflowStatus() {
    return workflowStatus;
  }
  public void setWorkflowStatus(String workflowStatus) {
    this.workflowStatus = workflowStatus;
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
        Objects.equals(context, apIProduct.context) &&
        Objects.equals(description, apIProduct.description) &&
        Objects.equals(provider, apIProduct.provider) &&
        Objects.equals(hasThumbnail, apIProduct.hasThumbnail) &&
        Objects.equals(state, apIProduct.state) &&
        Objects.equals(enableSchemaValidation, apIProduct.enableSchemaValidation) &&
        Objects.equals(isRevision, apIProduct.isRevision) &&
        Objects.equals(revisionedApiProductId, apIProduct.revisionedApiProductId) &&
        Objects.equals(revisionId, apIProduct.revisionId) &&
        Objects.equals(responseCachingEnabled, apIProduct.responseCachingEnabled) &&
        Objects.equals(cacheTimeout, apIProduct.cacheTimeout) &&
        Objects.equals(visibility, apIProduct.visibility) &&
        Objects.equals(visibleRoles, apIProduct.visibleRoles) &&
        Objects.equals(visibleTenants, apIProduct.visibleTenants) &&
        Objects.equals(accessControl, apIProduct.accessControl) &&
        Objects.equals(accessControlRoles, apIProduct.accessControlRoles) &&
        Objects.equals(apiType, apIProduct.apiType) &&
        Objects.equals(transport, apIProduct.transport) &&
        Objects.equals(tags, apIProduct.tags) &&
        Objects.equals(policies, apIProduct.policies) &&
        Objects.equals(apiThrottlingPolicy, apIProduct.apiThrottlingPolicy) &&
        Objects.equals(authorizationHeader, apIProduct.authorizationHeader) &&
        Objects.equals(securityScheme, apIProduct.securityScheme) &&
        Objects.equals(subscriptionAvailability, apIProduct.subscriptionAvailability) &&
        Objects.equals(subscriptionAvailableTenants, apIProduct.subscriptionAvailableTenants) &&
        Objects.equals(additionalProperties, apIProduct.additionalProperties) &&
        Objects.equals(additionalPropertiesMap, apIProduct.additionalPropertiesMap) &&
        Objects.equals(monetization, apIProduct.monetization) &&
        Objects.equals(businessInformation, apIProduct.businessInformation) &&
        Objects.equals(corsConfiguration, apIProduct.corsConfiguration) &&
        Objects.equals(createdTime, apIProduct.createdTime) &&
        Objects.equals(lastUpdatedTime, apIProduct.lastUpdatedTime) &&
        Objects.equals(lastUpdatedTimestamp, apIProduct.lastUpdatedTimestamp) &&
        Objects.equals(gatewayVendor, apIProduct.gatewayVendor) &&
        Objects.equals(apis, apIProduct.apis) &&
        Objects.equals(scopes, apIProduct.scopes) &&
        Objects.equals(categories, apIProduct.categories) &&
        Objects.equals(workflowStatus, apIProduct.workflowStatus);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, context, description, provider, hasThumbnail, state, enableSchemaValidation, isRevision, revisionedApiProductId, revisionId, responseCachingEnabled, cacheTimeout, visibility, visibleRoles, visibleTenants, accessControl, accessControlRoles, apiType, transport, tags, policies, apiThrottlingPolicy, authorizationHeader, securityScheme, subscriptionAvailability, subscriptionAvailableTenants, additionalProperties, additionalPropertiesMap, monetization, businessInformation, corsConfiguration, createdTime, lastUpdatedTime, lastUpdatedTimestamp, gatewayVendor, apis, scopes, categories, workflowStatus);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIProductDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    context: ").append(toIndentedString(context)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
    sb.append("    hasThumbnail: ").append(toIndentedString(hasThumbnail)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
    sb.append("    enableSchemaValidation: ").append(toIndentedString(enableSchemaValidation)).append("\n");
    sb.append("    isRevision: ").append(toIndentedString(isRevision)).append("\n");
    sb.append("    revisionedApiProductId: ").append(toIndentedString(revisionedApiProductId)).append("\n");
    sb.append("    revisionId: ").append(toIndentedString(revisionId)).append("\n");
    sb.append("    responseCachingEnabled: ").append(toIndentedString(responseCachingEnabled)).append("\n");
    sb.append("    cacheTimeout: ").append(toIndentedString(cacheTimeout)).append("\n");
    sb.append("    visibility: ").append(toIndentedString(visibility)).append("\n");
    sb.append("    visibleRoles: ").append(toIndentedString(visibleRoles)).append("\n");
    sb.append("    visibleTenants: ").append(toIndentedString(visibleTenants)).append("\n");
    sb.append("    accessControl: ").append(toIndentedString(accessControl)).append("\n");
    sb.append("    accessControlRoles: ").append(toIndentedString(accessControlRoles)).append("\n");
    sb.append("    apiType: ").append(toIndentedString(apiType)).append("\n");
    sb.append("    transport: ").append(toIndentedString(transport)).append("\n");
    sb.append("    tags: ").append(toIndentedString(tags)).append("\n");
    sb.append("    policies: ").append(toIndentedString(policies)).append("\n");
    sb.append("    apiThrottlingPolicy: ").append(toIndentedString(apiThrottlingPolicy)).append("\n");
    sb.append("    authorizationHeader: ").append(toIndentedString(authorizationHeader)).append("\n");
    sb.append("    securityScheme: ").append(toIndentedString(securityScheme)).append("\n");
    sb.append("    subscriptionAvailability: ").append(toIndentedString(subscriptionAvailability)).append("\n");
    sb.append("    subscriptionAvailableTenants: ").append(toIndentedString(subscriptionAvailableTenants)).append("\n");
    sb.append("    additionalProperties: ").append(toIndentedString(additionalProperties)).append("\n");
    sb.append("    additionalPropertiesMap: ").append(toIndentedString(additionalPropertiesMap)).append("\n");
    sb.append("    monetization: ").append(toIndentedString(monetization)).append("\n");
    sb.append("    businessInformation: ").append(toIndentedString(businessInformation)).append("\n");
    sb.append("    corsConfiguration: ").append(toIndentedString(corsConfiguration)).append("\n");
    sb.append("    createdTime: ").append(toIndentedString(createdTime)).append("\n");
    sb.append("    lastUpdatedTime: ").append(toIndentedString(lastUpdatedTime)).append("\n");
    sb.append("    lastUpdatedTimestamp: ").append(toIndentedString(lastUpdatedTimestamp)).append("\n");
    sb.append("    gatewayVendor: ").append(toIndentedString(gatewayVendor)).append("\n");
    sb.append("    apis: ").append(toIndentedString(apis)).append("\n");
    sb.append("    scopes: ").append(toIndentedString(scopes)).append("\n");
    sb.append("    categories: ").append(toIndentedString(categories)).append("\n");
    sb.append("    workflowStatus: ").append(toIndentedString(workflowStatus)).append("\n");
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

