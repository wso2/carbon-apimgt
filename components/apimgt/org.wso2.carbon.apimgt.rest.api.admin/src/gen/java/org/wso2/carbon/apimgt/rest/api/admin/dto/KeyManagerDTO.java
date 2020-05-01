package org.wso2.carbon.apimgt.rest.api.admin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class KeyManagerDTO  {
  
  
  
  private String id = null;
  
  @NotNull
  private String name = null;
  
  @NotNull
  private String type = null;
  
  
  private String description = null;
  
  
  private String introspectionEndpoint = null;
  
  
  private String clientRegistrationEndpoint = null;
  
  
  private String tokenEndpoint = null;
  
  
  private String revokeEndpoint = null;
  
  
  private String userInfoEndpoint = null;
  
  
  private String authorizeEndpoint = null;
  
  
  private String jwksEndpoint = null;
  
  
  private String issuer = null;
  
  
  private String scopeManagementEndpoint = null;
  
  
  private List<String> availableGrantTypes = new ArrayList<String>();
  
  
  private Boolean enableTokenGneration = null;
  
  
  private Boolean enableTokenEncryption = false;
  
  
  private Boolean enableTokenHashing = false;
  
  
  private Boolean enableMapOauthConsumerApps = false;
  
  
  private Boolean enableSelfValidationJWT = true;
  
  
  private List<ClaimMappingEntryDTO> claimMapping = new ArrayList<ClaimMappingEntryDTO>();
  
  
  private TokenValidationDTO tokenValidation = null;
  
  
  private Boolean enabled = null;
  
  
  private Object additionalProperties = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
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
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
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
  @ApiModelProperty(value = "")
  @JsonProperty("introspectionEndpoint")
  public String getIntrospectionEndpoint() {
    return introspectionEndpoint;
  }
  public void setIntrospectionEndpoint(String introspectionEndpoint) {
    this.introspectionEndpoint = introspectionEndpoint;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("clientRegistrationEndpoint")
  public String getClientRegistrationEndpoint() {
    return clientRegistrationEndpoint;
  }
  public void setClientRegistrationEndpoint(String clientRegistrationEndpoint) {
    this.clientRegistrationEndpoint = clientRegistrationEndpoint;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("tokenEndpoint")
  public String getTokenEndpoint() {
    return tokenEndpoint;
  }
  public void setTokenEndpoint(String tokenEndpoint) {
    this.tokenEndpoint = tokenEndpoint;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("revokeEndpoint")
  public String getRevokeEndpoint() {
    return revokeEndpoint;
  }
  public void setRevokeEndpoint(String revokeEndpoint) {
    this.revokeEndpoint = revokeEndpoint;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("userInfoEndpoint")
  public String getUserInfoEndpoint() {
    return userInfoEndpoint;
  }
  public void setUserInfoEndpoint(String userInfoEndpoint) {
    this.userInfoEndpoint = userInfoEndpoint;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("authorizeEndpoint")
  public String getAuthorizeEndpoint() {
    return authorizeEndpoint;
  }
  public void setAuthorizeEndpoint(String authorizeEndpoint) {
    this.authorizeEndpoint = authorizeEndpoint;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("jwksEndpoint")
  public String getJwksEndpoint() {
    return jwksEndpoint;
  }
  public void setJwksEndpoint(String jwksEndpoint) {
    this.jwksEndpoint = jwksEndpoint;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("issuer")
  public String getIssuer() {
    return issuer;
  }
  public void setIssuer(String issuer) {
    this.issuer = issuer;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("scopeManagementEndpoint")
  public String getScopeManagementEndpoint() {
    return scopeManagementEndpoint;
  }
  public void setScopeManagementEndpoint(String scopeManagementEndpoint) {
    this.scopeManagementEndpoint = scopeManagementEndpoint;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("availableGrantTypes")
  public List<String> getAvailableGrantTypes() {
    return availableGrantTypes;
  }
  public void setAvailableGrantTypes(List<String> availableGrantTypes) {
    this.availableGrantTypes = availableGrantTypes;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("enableTokenGneration")
  public Boolean getEnableTokenGneration() {
    return enableTokenGneration;
  }
  public void setEnableTokenGneration(Boolean enableTokenGneration) {
    this.enableTokenGneration = enableTokenGneration;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("enableTokenEncryption")
  public Boolean getEnableTokenEncryption() {
    return enableTokenEncryption;
  }
  public void setEnableTokenEncryption(Boolean enableTokenEncryption) {
    this.enableTokenEncryption = enableTokenEncryption;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("enableTokenHashing")
  public Boolean getEnableTokenHashing() {
    return enableTokenHashing;
  }
  public void setEnableTokenHashing(Boolean enableTokenHashing) {
    this.enableTokenHashing = enableTokenHashing;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("enableMapOauthConsumerApps")
  public Boolean getEnableMapOauthConsumerApps() {
    return enableMapOauthConsumerApps;
  }
  public void setEnableMapOauthConsumerApps(Boolean enableMapOauthConsumerApps) {
    this.enableMapOauthConsumerApps = enableMapOauthConsumerApps;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("enableSelfValidationJWT")
  public Boolean getEnableSelfValidationJWT() {
    return enableSelfValidationJWT;
  }
  public void setEnableSelfValidationJWT(Boolean enableSelfValidationJWT) {
    this.enableSelfValidationJWT = enableSelfValidationJWT;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("claimMapping")
  public List<ClaimMappingEntryDTO> getClaimMapping() {
    return claimMapping;
  }
  public void setClaimMapping(List<ClaimMappingEntryDTO> claimMapping) {
    this.claimMapping = claimMapping;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("tokenValidation")
  public TokenValidationDTO getTokenValidation() {
    return tokenValidation;
  }
  public void setTokenValidation(TokenValidationDTO tokenValidation) {
    this.tokenValidation = tokenValidation;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("enabled")
  public Boolean getEnabled() {
    return enabled;
  }
  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("additionalProperties")
  public Object getAdditionalProperties() {
    return additionalProperties;
  }
  public void setAdditionalProperties(Object additionalProperties) {
    this.additionalProperties = additionalProperties;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class KeyManagerDTO {\n");
    
    sb.append("  id: ").append(id).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("  type: ").append(type).append("\n");
    sb.append("  description: ").append(description).append("\n");
    sb.append("  introspectionEndpoint: ").append(introspectionEndpoint).append("\n");
    sb.append("  clientRegistrationEndpoint: ").append(clientRegistrationEndpoint).append("\n");
    sb.append("  tokenEndpoint: ").append(tokenEndpoint).append("\n");
    sb.append("  revokeEndpoint: ").append(revokeEndpoint).append("\n");
    sb.append("  userInfoEndpoint: ").append(userInfoEndpoint).append("\n");
    sb.append("  authorizeEndpoint: ").append(authorizeEndpoint).append("\n");
    sb.append("  jwksEndpoint: ").append(jwksEndpoint).append("\n");
    sb.append("  issuer: ").append(issuer).append("\n");
    sb.append("  scopeManagementEndpoint: ").append(scopeManagementEndpoint).append("\n");
    sb.append("  availableGrantTypes: ").append(availableGrantTypes).append("\n");
    sb.append("  enableTokenGneration: ").append(enableTokenGneration).append("\n");
    sb.append("  enableTokenEncryption: ").append(enableTokenEncryption).append("\n");
    sb.append("  enableTokenHashing: ").append(enableTokenHashing).append("\n");
    sb.append("  enableMapOauthConsumerApps: ").append(enableMapOauthConsumerApps).append("\n");
    sb.append("  enableSelfValidationJWT: ").append(enableSelfValidationJWT).append("\n");
    sb.append("  claimMapping: ").append(claimMapping).append("\n");
    sb.append("  tokenValidation: ").append(tokenValidation).append("\n");
    sb.append("  enabled: ").append(enabled).append("\n");
    sb.append("  additionalProperties: ").append(additionalProperties).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
