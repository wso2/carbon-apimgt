package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIBusinessInformationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APICorsConfigurationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIInfoAdditionalPropertiesMapDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIMonetizationInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MCPServerOperationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MCPServerOperationPoliciesDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MCPServerScopeDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MaxTpsDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OrganizationPoliciesDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SubtypeConfigurationDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



@Scope(name = "apim:mcp_server_create", description="", value ="")
@Scope(name = "apim:mcp_server_import_export", description="", value ="")
@Scope(name = "apim:mcp_server_manage", description="", value ="")
public class MCPServerDTO   {
  
    private String id = null;
    private String name = null;
    private String displayName = null;
    private String description = null;
    private String context = null;
    private Object endpointConfig = null;
    private String version = null;
    private String provider = null;
    @Scope(name = "apim:mcp_server_publish", description="", value ="")
    @Scope(name = "apim:mcp_server_manage", description="", value ="")
    private String lifeCycleStatus = null;
    private Boolean hasThumbnail = null;
    private Boolean isDefaultVersion = null;
    private Boolean isRevision = null;
    private String revisionedMCPServerId = null;
    private Integer revisionId = null;
    private Boolean enableSchemaValidation = null;
    private List<String> audiences = new ArrayList<String>();
    private List<String> transport = new ArrayList<String>();
    @Scope(name = "apim:mcp_server_publish", description="", value ="")
    @Scope(name = "apim:mcp_server_manage", description="", value ="")
    private List<String> tags = new ArrayList<String>();
    @Scope(name = "apim:mcp_server_publish", description="", value ="")
    @Scope(name = "apim:mcp_server_manage", description="", value ="")
    private List<String> policies = new ArrayList<String>();
    @Scope(name = "apim:api_publish", description="", value ="")
    @Scope(name = "apim:api_manage", description="", value ="")
    private List<OrganizationPoliciesDTO> organizationPolicies = new ArrayList<OrganizationPoliciesDTO>();
    @Scope(name = "apim:mcp_server_publish", description="", value ="")
    @Scope(name = "apim:mcp_server_manage", description="", value ="")
    private String throttlingPolicy = null;
    private String authorizationHeader = null;
    private String apiKeyHeader = null;
    private List<String> securityScheme = new ArrayList<String>();
    private MaxTpsDTO maxTps = null;

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
    @Scope(name = "apim:mcp_server_publish", description="", value ="")
    @Scope(name = "apim:mcp_server_manage", description="", value ="")
    private VisibilityEnum visibility = VisibilityEnum.PUBLIC;
    @Scope(name = "apim:mcp_server_publish", description="", value ="")
    @Scope(name = "apim:mcp_server_manage", description="", value ="")
    private List<String> visibleRoles = new ArrayList<String>();
    private List<String> visibleTenants = new ArrayList<String>();
    @Scope(name = "apim:mcp_server_publish", description="", value ="")
    @Scope(name = "apim:mcp_server_manage", description="", value ="")
    private List<String> visibleOrganizations = new ArrayList<String>();
    private MCPServerOperationPoliciesDTO mcpServerPolicies = null;

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
    @Scope(name = "apim:mcp_server_publish", description="", value ="")
    @Scope(name = "apim:mcp_server_manage", description="", value ="")
    private SubscriptionAvailabilityEnum subscriptionAvailability = SubscriptionAvailabilityEnum.CURRENT_TENANT;
    private List<String> subscriptionAvailableTenants = new ArrayList<String>();
    @Scope(name = "apim:mcp_server_publish", description="", value ="")
    @Scope(name = "apim:mcp_server_manage", description="", value ="")
    private Map<String, APIInfoAdditionalPropertiesMapDTO> additionalPropertiesMap = new HashMap<String, APIInfoAdditionalPropertiesMapDTO>();
    private APIMonetizationInfoDTO monetization = null;

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
    @Scope(name = "apim:mcp_server_publish", description="", value ="")
    @Scope(name = "apim:mcp_server_manage", description="", value ="")
    private APIBusinessInformationDTO businessInformation = null;
    private APICorsConfigurationDTO corsConfiguration = null;
    private String workflowStatus = null;
    private String protocolVersion = null;
    private String createdTime = null;
    private String lastUpdatedTimestamp = null;
    @Scope(name = "apim:mcp_server_publish", description="", value ="")
    @Scope(name = "apim:mcp_server_manage", description="", value ="")
    private String lastUpdatedTime = null;
    private SubtypeConfigurationDTO subtypeConfiguration = null;
    private List<MCPServerScopeDTO> scopes = new ArrayList<MCPServerScopeDTO>();
    private List<MCPServerOperationDTO> operations = new ArrayList<MCPServerOperationDTO>();
    @Scope(name = "apim:mcp_server_publish", description="", value ="")
    private List<String> categories = new ArrayList<String>();
    private Object keyManagers = null;
    private String gatewayVendor = null;
    private String gatewayType = "wso2/synapse";
    private Boolean initiatedFromGateway = false;

  /**
   * UUID of the MCP Server
   **/
  public MCPServerDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "UUID of the MCP Server")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public MCPServerDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "ReadingList", required = true, value = "")
  @JsonProperty("name")
  @NotNull
 @Pattern(regexp="(^[^~!@#;:%^*()+={}|\\\\<>\"',&$\\[\\]/]*$)") @Size(min=1)  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Human-friendly name shown in UI. Length limited to DB column size.
   **/
  public MCPServerDTO displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(example = "Reading List", value = "Human-friendly name shown in UI. Length limited to DB column size.")
  @JsonProperty("displayName")
 @Size(min=1)  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   **/
  public MCPServerDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "This is a simple MCP server for a book store.", value = "")
  @JsonProperty("description")
 @Size(max=32766)  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   **/
  public MCPServerDTO context(String context) {
    this.context = context;
    return this;
  }

  
  @ApiModelProperty(example = "books", required = true, value = "")
  @JsonProperty("context")
  @NotNull
 @Size(min=1,max=232)  public String getContext() {
    return context;
  }
  public void setContext(String context) {
    this.context = context;
  }

  /**
   * Endpoint configuration of the backend. 
   **/
  public MCPServerDTO endpointConfig(Object endpointConfig) {
    this.endpointConfig = endpointConfig;
    return this;
  }

  
  @ApiModelProperty(value = "Endpoint configuration of the backend. ")
      @Valid
  @JsonProperty("endpointConfig")
  public Object getEndpointConfig() {
    return endpointConfig;
  }
  public void setEndpointConfig(Object endpointConfig) {
    this.endpointConfig = endpointConfig;
  }

  /**
   **/
  public MCPServerDTO version(String version) {
    this.version = version;
    return this;
  }

  
  @ApiModelProperty(example = "1.0.0", required = true, value = "")
  @JsonProperty("version")
  @NotNull
 @Pattern(regexp="^[^~!@#;:%^*()+={}|\\\\<>\"',&/$\\[\\]\\s+/]+$") @Size(min=1,max=30)  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * If the provider value is not given user invoking the MCP Server will be used as the provider.
   **/
  public MCPServerDTO provider(String provider) {
    this.provider = provider;
    return this;
  }

  
  @ApiModelProperty(example = "admin", value = "If the provider value is not given user invoking the MCP Server will be used as the provider.")
  @JsonProperty("provider")
 @Size(max=200)  public String getProvider() {
    return provider;
  }
  public void setProvider(String provider) {
    this.provider = provider;
  }

  /**
   **/
  public MCPServerDTO lifeCycleStatus(String lifeCycleStatus) {
    this.lifeCycleStatus = lifeCycleStatus;
    return this;
  }

  
  @ApiModelProperty(example = "CREATED", value = "")
  @JsonProperty("lifeCycleStatus")
  public String getLifeCycleStatus() {
    return lifeCycleStatus;
  }
  public void setLifeCycleStatus(String lifeCycleStatus) {
    this.lifeCycleStatus = lifeCycleStatus;
  }

  /**
   **/
  public MCPServerDTO hasThumbnail(Boolean hasThumbnail) {
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
   **/
  public MCPServerDTO isDefaultVersion(Boolean isDefaultVersion) {
    this.isDefaultVersion = isDefaultVersion;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "")
  @JsonProperty("isDefaultVersion")
  public Boolean isIsDefaultVersion() {
    return isDefaultVersion;
  }
  public void setIsDefaultVersion(Boolean isDefaultVersion) {
    this.isDefaultVersion = isDefaultVersion;
  }

  /**
   **/
  public MCPServerDTO isRevision(Boolean isRevision) {
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
   * UUID of the artifact
   **/
  public MCPServerDTO revisionedMCPServerId(String revisionedMCPServerId) {
    this.revisionedMCPServerId = revisionedMCPServerId;
    return this;
  }

  
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "UUID of the artifact")
  @JsonProperty("revisionedMCPServerId")
  public String getRevisionedMCPServerId() {
    return revisionedMCPServerId;
  }
  public void setRevisionedMCPServerId(String revisionedMCPServerId) {
    this.revisionedMCPServerId = revisionedMCPServerId;
  }

  /**
   **/
  public MCPServerDTO revisionId(Integer revisionId) {
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
  public MCPServerDTO enableSchemaValidation(Boolean enableSchemaValidation) {
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
   * The audiences of the MCP Server for jwt validation. Accepted values are any String values
   **/
  public MCPServerDTO audiences(List<String> audiences) {
    this.audiences = audiences;
    return this;
  }

  
  @ApiModelProperty(value = "The audiences of the MCP Server for jwt validation. Accepted values are any String values")
  @JsonProperty("audiences")
  public List<String> getAudiences() {
    return audiences;
  }
  public void setAudiences(List<String> audiences) {
    this.audiences = audiences;
  }

  /**
   * Supported transports for the MCP Server (http and/or https).
   **/
  public MCPServerDTO transport(List<String> transport) {
    this.transport = transport;
    return this;
  }

  
  @ApiModelProperty(example = "[\"http\",\"https\"]", value = "Supported transports for the MCP Server (http and/or https).")
  @JsonProperty("transport")
  public List<String> getTransport() {
    return transport;
  }
  public void setTransport(List<String> transport) {
    this.transport = transport;
  }

  /**
   **/
  public MCPServerDTO tags(List<String> tags) {
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
  public MCPServerDTO policies(List<String> policies) {
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
   **/
  public MCPServerDTO organizationPolicies(List<OrganizationPoliciesDTO> organizationPolicies) {
    this.organizationPolicies = organizationPolicies;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("organizationPolicies")
  public List<OrganizationPoliciesDTO> getOrganizationPolicies() {
    return organizationPolicies;
  }
  public void setOrganizationPolicies(List<OrganizationPoliciesDTO> organizationPolicies) {
    this.organizationPolicies = organizationPolicies;
  }

  /**
   * The MCP Server level throttling policy selected.
   **/
  public MCPServerDTO throttlingPolicy(String throttlingPolicy) {
    this.throttlingPolicy = throttlingPolicy;
    return this;
  }

  
  @ApiModelProperty(example = "Unlimited", value = "The MCP Server level throttling policy selected.")
  @JsonProperty("throttlingPolicy")
  public String getThrottlingPolicy() {
    return throttlingPolicy;
  }
  public void setThrottlingPolicy(String throttlingPolicy) {
    this.throttlingPolicy = throttlingPolicy;
  }

  /**
   * Name of the Authorization header used for invoking the MCP Server. If it is not set,  Authorization header name specified in tenant or system level will be used. 
   **/
  public MCPServerDTO authorizationHeader(String authorizationHeader) {
    this.authorizationHeader = authorizationHeader;
    return this;
  }

  
  @ApiModelProperty(example = "Authorization", value = "Name of the Authorization header used for invoking the MCP Server. If it is not set,  Authorization header name specified in tenant or system level will be used. ")
  @JsonProperty("authorizationHeader")
 @Pattern(regexp="(^[^~!@#;:%^*()+={}|\\\\<>\"',&$\\s+]*$)")  public String getAuthorizationHeader() {
    return authorizationHeader;
  }
  public void setAuthorizationHeader(String authorizationHeader) {
    this.authorizationHeader = authorizationHeader;
  }

  /**
   * Name of the API key header used for invoking the MCP Server. If it is not set, default value&#x60;apiKey&#x60;  will be used. 
   **/
  public MCPServerDTO apiKeyHeader(String apiKeyHeader) {
    this.apiKeyHeader = apiKeyHeader;
    return this;
  }

  
  @ApiModelProperty(example = "apiKey", value = "Name of the API key header used for invoking the MCP Server. If it is not set, default value`apiKey`  will be used. ")
  @JsonProperty("apiKeyHeader")
 @Pattern(regexp="(^[^~!@#;:%^*()+={}|\\\\<>\"',&$\\s+]*$)")  public String getApiKeyHeader() {
    return apiKeyHeader;
  }
  public void setApiKeyHeader(String apiKeyHeader) {
    this.apiKeyHeader = apiKeyHeader;
  }

  /**
   * Types of API security, the current MCP Server secured with. It can be either OAuth2 or mutual SSLor both. If it is not set OAuth2 will be set as the security. 
   **/
  public MCPServerDTO securityScheme(List<String> securityScheme) {
    this.securityScheme = securityScheme;
    return this;
  }

  
  @ApiModelProperty(example = "[\"oauth2\"]", value = "Types of API security, the current MCP Server secured with. It can be either OAuth2 or mutual SSLor both. If it is not set OAuth2 will be set as the security. ")
  @JsonProperty("securityScheme")
  public List<String> getSecurityScheme() {
    return securityScheme;
  }
  public void setSecurityScheme(List<String> securityScheme) {
    this.securityScheme = securityScheme;
  }

  /**
   **/
  public MCPServerDTO maxTps(MaxTpsDTO maxTps) {
    this.maxTps = maxTps;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("maxTps")
  public MaxTpsDTO getMaxTps() {
    return maxTps;
  }
  public void setMaxTps(MaxTpsDTO maxTps) {
    this.maxTps = maxTps;
  }

  /**
   * The visibility level of the MCP Server. Accepts one of the following: PUBLIC, PRIVATE, RESTRICTED. 
   **/
  public MCPServerDTO visibility(VisibilityEnum visibility) {
    this.visibility = visibility;
    return this;
  }

  
  @ApiModelProperty(example = "PUBLIC", value = "The visibility level of the MCP Server. Accepts one of the following: PUBLIC, PRIVATE, RESTRICTED. ")
  @JsonProperty("visibility")
  public VisibilityEnum getVisibility() {
    return visibility;
  }
  public void setVisibility(VisibilityEnum visibility) {
    this.visibility = visibility;
  }

  /**
   * The user roles that are able to access the MCP Server in Developer Portal
   **/
  public MCPServerDTO visibleRoles(List<String> visibleRoles) {
    this.visibleRoles = visibleRoles;
    return this;
  }

  
  @ApiModelProperty(example = "[]", value = "The user roles that are able to access the MCP Server in Developer Portal")
  @JsonProperty("visibleRoles")
  public List<String> getVisibleRoles() {
    return visibleRoles;
  }
  public void setVisibleRoles(List<String> visibleRoles) {
    this.visibleRoles = visibleRoles;
  }

  /**
   **/
  public MCPServerDTO visibleTenants(List<String> visibleTenants) {
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
   * The organizations that are able to access the MCP server in Developer Portal
   **/
  public MCPServerDTO visibleOrganizations(List<String> visibleOrganizations) {
    this.visibleOrganizations = visibleOrganizations;
    return this;
  }

  
  @ApiModelProperty(example = "[]", value = "The organizations that are able to access the MCP server in Developer Portal")
  @JsonProperty("visibleOrganizations")
  public List<String> getVisibleOrganizations() {
    return visibleOrganizations;
  }
  public void setVisibleOrganizations(List<String> visibleOrganizations) {
    this.visibleOrganizations = visibleOrganizations;
  }

  /**
   **/
  public MCPServerDTO mcpServerPolicies(MCPServerOperationPoliciesDTO mcpServerPolicies) {
    this.mcpServerPolicies = mcpServerPolicies;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("mcpServerPolicies")
  public MCPServerOperationPoliciesDTO getMcpServerPolicies() {
    return mcpServerPolicies;
  }
  public void setMcpServerPolicies(MCPServerOperationPoliciesDTO mcpServerPolicies) {
    this.mcpServerPolicies = mcpServerPolicies;
  }

  /**
   * The subscription availability. Accepts one of the following: CURRENT_TENANT, ALL_TENANTS, or  SPECIFIC_TENANTS. 
   **/
  public MCPServerDTO subscriptionAvailability(SubscriptionAvailabilityEnum subscriptionAvailability) {
    this.subscriptionAvailability = subscriptionAvailability;
    return this;
  }

  
  @ApiModelProperty(example = "CURRENT_TENANT", value = "The subscription availability. Accepts one of the following: CURRENT_TENANT, ALL_TENANTS, or  SPECIFIC_TENANTS. ")
  @JsonProperty("subscriptionAvailability")
  public SubscriptionAvailabilityEnum getSubscriptionAvailability() {
    return subscriptionAvailability;
  }
  public void setSubscriptionAvailability(SubscriptionAvailabilityEnum subscriptionAvailability) {
    this.subscriptionAvailability = subscriptionAvailability;
  }

  /**
   **/
  public MCPServerDTO subscriptionAvailableTenants(List<String> subscriptionAvailableTenants) {
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
   **/
  public MCPServerDTO additionalPropertiesMap(Map<String, APIInfoAdditionalPropertiesMapDTO> additionalPropertiesMap) {
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
  public MCPServerDTO monetization(APIMonetizationInfoDTO monetization) {
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
   * Is the MCP server restricted to certain publishers or creators or is it visible to all publishers and  creators. If the accessControl restriction is NONE, this can be modified by all publishers and creators. Otherwise, it can only be viewable/modifiable by a specific set of users based on the restriction. 
   **/
  public MCPServerDTO accessControl(AccessControlEnum accessControl) {
    this.accessControl = accessControl;
    return this;
  }

  
  @ApiModelProperty(value = "Is the MCP server restricted to certain publishers or creators or is it visible to all publishers and  creators. If the accessControl restriction is NONE, this can be modified by all publishers and creators. Otherwise, it can only be viewable/modifiable by a specific set of users based on the restriction. ")
  @JsonProperty("accessControl")
  public AccessControlEnum getAccessControl() {
    return accessControl;
  }
  public void setAccessControl(AccessControlEnum accessControl) {
    this.accessControl = accessControl;
  }

  /**
   * The user roles that are able to view/modify as publisher or creator.
   **/
  public MCPServerDTO accessControlRoles(List<String> accessControlRoles) {
    this.accessControlRoles = accessControlRoles;
    return this;
  }

  
  @ApiModelProperty(example = "[]", value = "The user roles that are able to view/modify as publisher or creator.")
  @JsonProperty("accessControlRoles")
  public List<String> getAccessControlRoles() {
    return accessControlRoles;
  }
  public void setAccessControlRoles(List<String> accessControlRoles) {
    this.accessControlRoles = accessControlRoles;
  }

  /**
   **/
  public MCPServerDTO businessInformation(APIBusinessInformationDTO businessInformation) {
    this.businessInformation = businessInformation;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("businessInformation")
  public APIBusinessInformationDTO getBusinessInformation() {
    return businessInformation;
  }
  public void setBusinessInformation(APIBusinessInformationDTO businessInformation) {
    this.businessInformation = businessInformation;
  }

  /**
   **/
  public MCPServerDTO corsConfiguration(APICorsConfigurationDTO corsConfiguration) {
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
  public MCPServerDTO workflowStatus(String workflowStatus) {
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

  /**
   **/
  public MCPServerDTO protocolVersion(String protocolVersion) {
    this.protocolVersion = protocolVersion;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("protocolVersion")
  public String getProtocolVersion() {
    return protocolVersion;
  }
  public void setProtocolVersion(String protocolVersion) {
    this.protocolVersion = protocolVersion;
  }

  /**
   **/
  public MCPServerDTO createdTime(String createdTime) {
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
  public MCPServerDTO lastUpdatedTimestamp(String lastUpdatedTimestamp) {
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
  public MCPServerDTO lastUpdatedTime(String lastUpdatedTime) {
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
  public MCPServerDTO subtypeConfiguration(SubtypeConfigurationDTO subtypeConfiguration) {
    this.subtypeConfiguration = subtypeConfiguration;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("subtypeConfiguration")
  public SubtypeConfigurationDTO getSubtypeConfiguration() {
    return subtypeConfiguration;
  }
  public void setSubtypeConfiguration(SubtypeConfigurationDTO subtypeConfiguration) {
    this.subtypeConfiguration = subtypeConfiguration;
  }

  /**
   **/
  public MCPServerDTO scopes(List<MCPServerScopeDTO> scopes) {
    this.scopes = scopes;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("scopes")
  public List<MCPServerScopeDTO> getScopes() {
    return scopes;
  }
  public void setScopes(List<MCPServerScopeDTO> scopes) {
    this.scopes = scopes;
  }

  /**
   **/
  public MCPServerDTO operations(List<MCPServerOperationDTO> operations) {
    this.operations = operations;
    return this;
  }

  
  @ApiModelProperty(example = "[{\"target\":\"listBooks\",\"feature\":\"TOOL\",\"authType\":\"Application & Application User\",\"throttlingPolicy\":\"Unlimited\"},{\"target\":\"addBook\",\"verb\":\"TOOL\",\"authType\":\"Application & Application User\",\"throttlingPolicy\":\"Unlimited\"}]", value = "")
      @Valid
  @JsonProperty("operations")
  public List<MCPServerOperationDTO> getOperations() {
    return operations;
  }
  public void setOperations(List<MCPServerOperationDTO> operations) {
    this.operations = operations;
  }

  /**
   * MCP Server categories
   **/
  public MCPServerDTO categories(List<String> categories) {
    this.categories = categories;
    return this;
  }

  
  @ApiModelProperty(value = "MCP Server categories")
  @JsonProperty("categories")
  public List<String> getCategories() {
    return categories;
  }
  public void setCategories(List<String> categories) {
    this.categories = categories;
  }

  /**
   * Key Managers
   **/
  public MCPServerDTO keyManagers(Object keyManagers) {
    this.keyManagers = keyManagers;
    return this;
  }

  
  @ApiModelProperty(value = "Key Managers")
      @Valid
  @JsonProperty("keyManagers")
  public Object getKeyManagers() {
    return keyManagers;
  }
  public void setKeyManagers(Object keyManagers) {
    this.keyManagers = keyManagers;
  }

  /**
   **/
  public MCPServerDTO gatewayVendor(String gatewayVendor) {
    this.gatewayVendor = gatewayVendor;
    return this;
  }

  
  @ApiModelProperty(example = "wso2 external", value = "")
  @JsonProperty("gatewayVendor")
  public String getGatewayVendor() {
    return gatewayVendor;
  }
  public void setGatewayVendor(String gatewayVendor) {
    this.gatewayVendor = gatewayVendor;
  }

  /**
   * The gateway type selected for the policies. Accepts one of the following: wso2/synapse, wso2/apk, AWS. 
   **/
  public MCPServerDTO gatewayType(String gatewayType) {
    this.gatewayType = gatewayType;
    return this;
  }

  
  @ApiModelProperty(example = "wso2/synapse wso2/apk AWS", value = "The gateway type selected for the policies. Accepts one of the following: wso2/synapse, wso2/apk, AWS. ")
  @JsonProperty("gatewayType")
  public String getGatewayType() {
    return gatewayType;
  }
  public void setGatewayType(String gatewayType) {
    this.gatewayType = gatewayType;
  }

  /**
   * Whether the MCP Server is initiated from the gateway or not. This is used to identify whether the MCP Server is created from the publisher or discovered from the gateway. 
   **/
  public MCPServerDTO initiatedFromGateway(Boolean initiatedFromGateway) {
    this.initiatedFromGateway = initiatedFromGateway;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "Whether the MCP Server is initiated from the gateway or not. This is used to identify whether the MCP Server is created from the publisher or discovered from the gateway. ")
  @JsonProperty("initiatedFromGateway")
  public Boolean isInitiatedFromGateway() {
    return initiatedFromGateway;
  }
  public void setInitiatedFromGateway(Boolean initiatedFromGateway) {
    this.initiatedFromGateway = initiatedFromGateway;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MCPServerDTO mcPServer = (MCPServerDTO) o;
    return Objects.equals(id, mcPServer.id) &&
        Objects.equals(name, mcPServer.name) &&
        Objects.equals(displayName, mcPServer.displayName) &&
        Objects.equals(description, mcPServer.description) &&
        Objects.equals(context, mcPServer.context) &&
        Objects.equals(endpointConfig, mcPServer.endpointConfig) &&
        Objects.equals(version, mcPServer.version) &&
        Objects.equals(provider, mcPServer.provider) &&
        Objects.equals(lifeCycleStatus, mcPServer.lifeCycleStatus) &&
        Objects.equals(hasThumbnail, mcPServer.hasThumbnail) &&
        Objects.equals(isDefaultVersion, mcPServer.isDefaultVersion) &&
        Objects.equals(isRevision, mcPServer.isRevision) &&
        Objects.equals(revisionedMCPServerId, mcPServer.revisionedMCPServerId) &&
        Objects.equals(revisionId, mcPServer.revisionId) &&
        Objects.equals(enableSchemaValidation, mcPServer.enableSchemaValidation) &&
        Objects.equals(audiences, mcPServer.audiences) &&
        Objects.equals(transport, mcPServer.transport) &&
        Objects.equals(tags, mcPServer.tags) &&
        Objects.equals(policies, mcPServer.policies) &&
        Objects.equals(organizationPolicies, mcPServer.organizationPolicies) &&
        Objects.equals(throttlingPolicy, mcPServer.throttlingPolicy) &&
        Objects.equals(authorizationHeader, mcPServer.authorizationHeader) &&
        Objects.equals(apiKeyHeader, mcPServer.apiKeyHeader) &&
        Objects.equals(securityScheme, mcPServer.securityScheme) &&
        Objects.equals(maxTps, mcPServer.maxTps) &&
        Objects.equals(visibility, mcPServer.visibility) &&
        Objects.equals(visibleRoles, mcPServer.visibleRoles) &&
        Objects.equals(visibleTenants, mcPServer.visibleTenants) &&
        Objects.equals(visibleOrganizations, mcPServer.visibleOrganizations) &&
        Objects.equals(mcpServerPolicies, mcPServer.mcpServerPolicies) &&
        Objects.equals(subscriptionAvailability, mcPServer.subscriptionAvailability) &&
        Objects.equals(subscriptionAvailableTenants, mcPServer.subscriptionAvailableTenants) &&
        Objects.equals(additionalPropertiesMap, mcPServer.additionalPropertiesMap) &&
        Objects.equals(monetization, mcPServer.monetization) &&
        Objects.equals(accessControl, mcPServer.accessControl) &&
        Objects.equals(accessControlRoles, mcPServer.accessControlRoles) &&
        Objects.equals(businessInformation, mcPServer.businessInformation) &&
        Objects.equals(corsConfiguration, mcPServer.corsConfiguration) &&
        Objects.equals(workflowStatus, mcPServer.workflowStatus) &&
        Objects.equals(protocolVersion, mcPServer.protocolVersion) &&
        Objects.equals(createdTime, mcPServer.createdTime) &&
        Objects.equals(lastUpdatedTimestamp, mcPServer.lastUpdatedTimestamp) &&
        Objects.equals(lastUpdatedTime, mcPServer.lastUpdatedTime) &&
        Objects.equals(subtypeConfiguration, mcPServer.subtypeConfiguration) &&
        Objects.equals(scopes, mcPServer.scopes) &&
        Objects.equals(operations, mcPServer.operations) &&
        Objects.equals(categories, mcPServer.categories) &&
        Objects.equals(keyManagers, mcPServer.keyManagers) &&
        Objects.equals(gatewayVendor, mcPServer.gatewayVendor) &&
        Objects.equals(gatewayType, mcPServer.gatewayType) &&
        Objects.equals(initiatedFromGateway, mcPServer.initiatedFromGateway);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, displayName, description, context, endpointConfig, version, provider, lifeCycleStatus, hasThumbnail, isDefaultVersion, isRevision, revisionedMCPServerId, revisionId, enableSchemaValidation, audiences, transport, tags, policies, organizationPolicies, throttlingPolicy, authorizationHeader, apiKeyHeader, securityScheme, maxTps, visibility, visibleRoles, visibleTenants, visibleOrganizations, mcpServerPolicies, subscriptionAvailability, subscriptionAvailableTenants, additionalPropertiesMap, monetization, accessControl, accessControlRoles, businessInformation, corsConfiguration, workflowStatus, protocolVersion, createdTime, lastUpdatedTimestamp, lastUpdatedTime, subtypeConfiguration, scopes, operations, categories, keyManagers, gatewayVendor, gatewayType, initiatedFromGateway);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MCPServerDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    context: ").append(toIndentedString(context)).append("\n");
    sb.append("    endpointConfig: ").append(toIndentedString(endpointConfig)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
    sb.append("    lifeCycleStatus: ").append(toIndentedString(lifeCycleStatus)).append("\n");
    sb.append("    hasThumbnail: ").append(toIndentedString(hasThumbnail)).append("\n");
    sb.append("    isDefaultVersion: ").append(toIndentedString(isDefaultVersion)).append("\n");
    sb.append("    isRevision: ").append(toIndentedString(isRevision)).append("\n");
    sb.append("    revisionedMCPServerId: ").append(toIndentedString(revisionedMCPServerId)).append("\n");
    sb.append("    revisionId: ").append(toIndentedString(revisionId)).append("\n");
    sb.append("    enableSchemaValidation: ").append(toIndentedString(enableSchemaValidation)).append("\n");
    sb.append("    audiences: ").append(toIndentedString(audiences)).append("\n");
    sb.append("    transport: ").append(toIndentedString(transport)).append("\n");
    sb.append("    tags: ").append(toIndentedString(tags)).append("\n");
    sb.append("    policies: ").append(toIndentedString(policies)).append("\n");
    sb.append("    organizationPolicies: ").append(toIndentedString(organizationPolicies)).append("\n");
    sb.append("    throttlingPolicy: ").append(toIndentedString(throttlingPolicy)).append("\n");
    sb.append("    authorizationHeader: ").append(toIndentedString(authorizationHeader)).append("\n");
    sb.append("    apiKeyHeader: ").append(toIndentedString(apiKeyHeader)).append("\n");
    sb.append("    securityScheme: ").append(toIndentedString(securityScheme)).append("\n");
    sb.append("    maxTps: ").append(toIndentedString(maxTps)).append("\n");
    sb.append("    visibility: ").append(toIndentedString(visibility)).append("\n");
    sb.append("    visibleRoles: ").append(toIndentedString(visibleRoles)).append("\n");
    sb.append("    visibleTenants: ").append(toIndentedString(visibleTenants)).append("\n");
    sb.append("    visibleOrganizations: ").append(toIndentedString(visibleOrganizations)).append("\n");
    sb.append("    mcpServerPolicies: ").append(toIndentedString(mcpServerPolicies)).append("\n");
    sb.append("    subscriptionAvailability: ").append(toIndentedString(subscriptionAvailability)).append("\n");
    sb.append("    subscriptionAvailableTenants: ").append(toIndentedString(subscriptionAvailableTenants)).append("\n");
    sb.append("    additionalPropertiesMap: ").append(toIndentedString(additionalPropertiesMap)).append("\n");
    sb.append("    monetization: ").append(toIndentedString(monetization)).append("\n");
    sb.append("    accessControl: ").append(toIndentedString(accessControl)).append("\n");
    sb.append("    accessControlRoles: ").append(toIndentedString(accessControlRoles)).append("\n");
    sb.append("    businessInformation: ").append(toIndentedString(businessInformation)).append("\n");
    sb.append("    corsConfiguration: ").append(toIndentedString(corsConfiguration)).append("\n");
    sb.append("    workflowStatus: ").append(toIndentedString(workflowStatus)).append("\n");
    sb.append("    protocolVersion: ").append(toIndentedString(protocolVersion)).append("\n");
    sb.append("    createdTime: ").append(toIndentedString(createdTime)).append("\n");
    sb.append("    lastUpdatedTimestamp: ").append(toIndentedString(lastUpdatedTimestamp)).append("\n");
    sb.append("    lastUpdatedTime: ").append(toIndentedString(lastUpdatedTime)).append("\n");
    sb.append("    subtypeConfiguration: ").append(toIndentedString(subtypeConfiguration)).append("\n");
    sb.append("    scopes: ").append(toIndentedString(scopes)).append("\n");
    sb.append("    operations: ").append(toIndentedString(operations)).append("\n");
    sb.append("    categories: ").append(toIndentedString(categories)).append("\n");
    sb.append("    keyManagers: ").append(toIndentedString(keyManagers)).append("\n");
    sb.append("    gatewayVendor: ").append(toIndentedString(gatewayVendor)).append("\n");
    sb.append("    gatewayType: ").append(toIndentedString(gatewayType)).append("\n");
    sb.append("    initiatedFromGateway: ").append(toIndentedString(initiatedFromGateway)).append("\n");
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

