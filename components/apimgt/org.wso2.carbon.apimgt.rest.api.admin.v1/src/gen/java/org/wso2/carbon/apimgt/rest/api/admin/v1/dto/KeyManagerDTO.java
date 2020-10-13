package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ClaimMappingEntryDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.KeyManagerCertificatesDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.TokenValidationDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class KeyManagerDTO   {
  
    private String id = null;
    private String name = null;
    private String displayName = null;
    private String type = null;
    private String description = null;
    private String wellKnownEndpoint = null;
    private String introspectionEndpoint = null;
    private String clientRegistrationEndpoint = null;
    private String tokenEndpoint = null;
    private String revokeEndpoint = null;
    private String userInfoEndpoint = null;
    private String authorizeEndpoint = null;
    private KeyManagerCertificatesDTO certificates = null;
    private String issuer = null;
    private String scopeManagementEndpoint = null;
    private List<String> availableGrantTypes = new ArrayList<String>();
    private Boolean enableTokenGeneration = null;
    private Boolean enableTokenEncryption = false;
    private Boolean enableTokenHashing = false;
    private Boolean enableMapOAuthConsumerApps = false;
    private Boolean enableOAuthAppCreation = false;
    private Boolean enableSelfValidationJWT = true;
    private List<ClaimMappingEntryDTO> claimMapping = new ArrayList<ClaimMappingEntryDTO>();
    private String consumerKeyClaim = null;
    private String scopesClaim = null;
    private List<TokenValidationDTO> tokenValidation = new ArrayList<TokenValidationDTO>();
    private Boolean enabled = null;
    private Object additionalProperties = null;

  /**
   **/
  public KeyManagerDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public KeyManagerDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "WSO2 IS", required = true, value = "")
  @JsonProperty("name")
  @NotNull
 @Size(min=0,max=100)  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * display name of Key Manager to  show in UI 
   **/
  public KeyManagerDTO displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(example = "KeyManager1", value = "display name of Key Manager to  show in UI ")
  @JsonProperty("displayName")
 @Size(max=100)  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   **/
  public KeyManagerDTO type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(example = "IS", required = true, value = "")
  @JsonProperty("type")
  @NotNull
 @Size(min=0,max=45)  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  /**
   **/
  public KeyManagerDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "This is a key manager for Developers", value = "")
  @JsonProperty("description")
 @Size(max=256)  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Well-Known Endpoint of Identity Provider. 
   **/
  public KeyManagerDTO wellKnownEndpoint(String wellKnownEndpoint) {
    this.wellKnownEndpoint = wellKnownEndpoint;
    return this;
  }

  
  @ApiModelProperty(example = "", value = "Well-Known Endpoint of Identity Provider. ")
  @JsonProperty("wellKnownEndpoint")
  public String getWellKnownEndpoint() {
    return wellKnownEndpoint;
  }
  public void setWellKnownEndpoint(String wellKnownEndpoint) {
    this.wellKnownEndpoint = wellKnownEndpoint;
  }

  /**
   **/
  public KeyManagerDTO introspectionEndpoint(String introspectionEndpoint) {
    this.introspectionEndpoint = introspectionEndpoint;
    return this;
  }

  
  @ApiModelProperty(example = "", value = "")
  @JsonProperty("introspectionEndpoint")
  public String getIntrospectionEndpoint() {
    return introspectionEndpoint;
  }
  public void setIntrospectionEndpoint(String introspectionEndpoint) {
    this.introspectionEndpoint = introspectionEndpoint;
  }

  /**
   **/
  public KeyManagerDTO clientRegistrationEndpoint(String clientRegistrationEndpoint) {
    this.clientRegistrationEndpoint = clientRegistrationEndpoint;
    return this;
  }

  
  @ApiModelProperty(example = "", value = "")
  @JsonProperty("clientRegistrationEndpoint")
  public String getClientRegistrationEndpoint() {
    return clientRegistrationEndpoint;
  }
  public void setClientRegistrationEndpoint(String clientRegistrationEndpoint) {
    this.clientRegistrationEndpoint = clientRegistrationEndpoint;
  }

  /**
   **/
  public KeyManagerDTO tokenEndpoint(String tokenEndpoint) {
    this.tokenEndpoint = tokenEndpoint;
    return this;
  }

  
  @ApiModelProperty(example = "", value = "")
  @JsonProperty("tokenEndpoint")
  public String getTokenEndpoint() {
    return tokenEndpoint;
  }
  public void setTokenEndpoint(String tokenEndpoint) {
    this.tokenEndpoint = tokenEndpoint;
  }

  /**
   **/
  public KeyManagerDTO revokeEndpoint(String revokeEndpoint) {
    this.revokeEndpoint = revokeEndpoint;
    return this;
  }

  
  @ApiModelProperty(example = "", value = "")
  @JsonProperty("revokeEndpoint")
  public String getRevokeEndpoint() {
    return revokeEndpoint;
  }
  public void setRevokeEndpoint(String revokeEndpoint) {
    this.revokeEndpoint = revokeEndpoint;
  }

  /**
   **/
  public KeyManagerDTO userInfoEndpoint(String userInfoEndpoint) {
    this.userInfoEndpoint = userInfoEndpoint;
    return this;
  }

  
  @ApiModelProperty(example = "", value = "")
  @JsonProperty("userInfoEndpoint")
  public String getUserInfoEndpoint() {
    return userInfoEndpoint;
  }
  public void setUserInfoEndpoint(String userInfoEndpoint) {
    this.userInfoEndpoint = userInfoEndpoint;
  }

  /**
   **/
  public KeyManagerDTO authorizeEndpoint(String authorizeEndpoint) {
    this.authorizeEndpoint = authorizeEndpoint;
    return this;
  }

  
  @ApiModelProperty(example = "", value = "")
  @JsonProperty("authorizeEndpoint")
  public String getAuthorizeEndpoint() {
    return authorizeEndpoint;
  }
  public void setAuthorizeEndpoint(String authorizeEndpoint) {
    this.authorizeEndpoint = authorizeEndpoint;
  }

  /**
   **/
  public KeyManagerDTO certificates(KeyManagerCertificatesDTO certificates) {
    this.certificates = certificates;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("certificates")
  public KeyManagerCertificatesDTO getCertificates() {
    return certificates;
  }
  public void setCertificates(KeyManagerCertificatesDTO certificates) {
    this.certificates = certificates;
  }

  /**
   **/
  public KeyManagerDTO issuer(String issuer) {
    this.issuer = issuer;
    return this;
  }

  
  @ApiModelProperty(example = "", value = "")
  @JsonProperty("issuer")
  public String getIssuer() {
    return issuer;
  }
  public void setIssuer(String issuer) {
    this.issuer = issuer;
  }

  /**
   **/
  public KeyManagerDTO scopeManagementEndpoint(String scopeManagementEndpoint) {
    this.scopeManagementEndpoint = scopeManagementEndpoint;
    return this;
  }

  
  @ApiModelProperty(example = "", value = "")
  @JsonProperty("scopeManagementEndpoint")
  public String getScopeManagementEndpoint() {
    return scopeManagementEndpoint;
  }
  public void setScopeManagementEndpoint(String scopeManagementEndpoint) {
    this.scopeManagementEndpoint = scopeManagementEndpoint;
  }

  /**
   **/
  public KeyManagerDTO availableGrantTypes(List<String> availableGrantTypes) {
    this.availableGrantTypes = availableGrantTypes;
    return this;
  }

  
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
  public KeyManagerDTO enableTokenGeneration(Boolean enableTokenGeneration) {
    this.enableTokenGeneration = enableTokenGeneration;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "")
  @JsonProperty("enableTokenGeneration")
  public Boolean isEnableTokenGeneration() {
    return enableTokenGeneration;
  }
  public void setEnableTokenGeneration(Boolean enableTokenGeneration) {
    this.enableTokenGeneration = enableTokenGeneration;
  }

  /**
   **/
  public KeyManagerDTO enableTokenEncryption(Boolean enableTokenEncryption) {
    this.enableTokenEncryption = enableTokenEncryption;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "")
  @JsonProperty("enableTokenEncryption")
  public Boolean isEnableTokenEncryption() {
    return enableTokenEncryption;
  }
  public void setEnableTokenEncryption(Boolean enableTokenEncryption) {
    this.enableTokenEncryption = enableTokenEncryption;
  }

  /**
   **/
  public KeyManagerDTO enableTokenHashing(Boolean enableTokenHashing) {
    this.enableTokenHashing = enableTokenHashing;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "")
  @JsonProperty("enableTokenHashing")
  public Boolean isEnableTokenHashing() {
    return enableTokenHashing;
  }
  public void setEnableTokenHashing(Boolean enableTokenHashing) {
    this.enableTokenHashing = enableTokenHashing;
  }

  /**
   **/
  public KeyManagerDTO enableMapOAuthConsumerApps(Boolean enableMapOAuthConsumerApps) {
    this.enableMapOAuthConsumerApps = enableMapOAuthConsumerApps;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "")
  @JsonProperty("enableMapOAuthConsumerApps")
  public Boolean isEnableMapOAuthConsumerApps() {
    return enableMapOAuthConsumerApps;
  }
  public void setEnableMapOAuthConsumerApps(Boolean enableMapOAuthConsumerApps) {
    this.enableMapOAuthConsumerApps = enableMapOAuthConsumerApps;
  }

  /**
   **/
  public KeyManagerDTO enableOAuthAppCreation(Boolean enableOAuthAppCreation) {
    this.enableOAuthAppCreation = enableOAuthAppCreation;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "")
  @JsonProperty("enableOAuthAppCreation")
  public Boolean isEnableOAuthAppCreation() {
    return enableOAuthAppCreation;
  }
  public void setEnableOAuthAppCreation(Boolean enableOAuthAppCreation) {
    this.enableOAuthAppCreation = enableOAuthAppCreation;
  }

  /**
   **/
  public KeyManagerDTO enableSelfValidationJWT(Boolean enableSelfValidationJWT) {
    this.enableSelfValidationJWT = enableSelfValidationJWT;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "")
  @JsonProperty("enableSelfValidationJWT")
  public Boolean isEnableSelfValidationJWT() {
    return enableSelfValidationJWT;
  }
  public void setEnableSelfValidationJWT(Boolean enableSelfValidationJWT) {
    this.enableSelfValidationJWT = enableSelfValidationJWT;
  }

  /**
   **/
  public KeyManagerDTO claimMapping(List<ClaimMappingEntryDTO> claimMapping) {
    this.claimMapping = claimMapping;
    return this;
  }

  
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
  public KeyManagerDTO consumerKeyClaim(String consumerKeyClaim) {
    this.consumerKeyClaim = consumerKeyClaim;
    return this;
  }

  
  @ApiModelProperty(example = "azp", value = "")
  @JsonProperty("consumerKeyClaim")
  public String getConsumerKeyClaim() {
    return consumerKeyClaim;
  }
  public void setConsumerKeyClaim(String consumerKeyClaim) {
    this.consumerKeyClaim = consumerKeyClaim;
  }

  /**
   **/
  public KeyManagerDTO scopesClaim(String scopesClaim) {
    this.scopesClaim = scopesClaim;
    return this;
  }

  
  @ApiModelProperty(example = "scp", value = "")
  @JsonProperty("scopesClaim")
  public String getScopesClaim() {
    return scopesClaim;
  }
  public void setScopesClaim(String scopesClaim) {
    this.scopesClaim = scopesClaim;
  }

  /**
   **/
  public KeyManagerDTO tokenValidation(List<TokenValidationDTO> tokenValidation) {
    this.tokenValidation = tokenValidation;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("tokenValidation")
  public List<TokenValidationDTO> getTokenValidation() {
    return tokenValidation;
  }
  public void setTokenValidation(List<TokenValidationDTO> tokenValidation) {
    this.tokenValidation = tokenValidation;
  }

  /**
   **/
  public KeyManagerDTO enabled(Boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "")
  @JsonProperty("enabled")
  public Boolean isEnabled() {
    return enabled;
  }
  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  /**
   **/
  public KeyManagerDTO additionalProperties(Object additionalProperties) {
    this.additionalProperties = additionalProperties;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("additionalProperties")
  public Object getAdditionalProperties() {
    return additionalProperties;
  }
  public void setAdditionalProperties(Object additionalProperties) {
    this.additionalProperties = additionalProperties;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    KeyManagerDTO keyManager = (KeyManagerDTO) o;
    return Objects.equals(id, keyManager.id) &&
        Objects.equals(name, keyManager.name) &&
        Objects.equals(displayName, keyManager.displayName) &&
        Objects.equals(type, keyManager.type) &&
        Objects.equals(description, keyManager.description) &&
        Objects.equals(wellKnownEndpoint, keyManager.wellKnownEndpoint) &&
        Objects.equals(introspectionEndpoint, keyManager.introspectionEndpoint) &&
        Objects.equals(clientRegistrationEndpoint, keyManager.clientRegistrationEndpoint) &&
        Objects.equals(tokenEndpoint, keyManager.tokenEndpoint) &&
        Objects.equals(revokeEndpoint, keyManager.revokeEndpoint) &&
        Objects.equals(userInfoEndpoint, keyManager.userInfoEndpoint) &&
        Objects.equals(authorizeEndpoint, keyManager.authorizeEndpoint) &&
        Objects.equals(certificates, keyManager.certificates) &&
        Objects.equals(issuer, keyManager.issuer) &&
        Objects.equals(scopeManagementEndpoint, keyManager.scopeManagementEndpoint) &&
        Objects.equals(availableGrantTypes, keyManager.availableGrantTypes) &&
        Objects.equals(enableTokenGeneration, keyManager.enableTokenGeneration) &&
        Objects.equals(enableTokenEncryption, keyManager.enableTokenEncryption) &&
        Objects.equals(enableTokenHashing, keyManager.enableTokenHashing) &&
        Objects.equals(enableMapOAuthConsumerApps, keyManager.enableMapOAuthConsumerApps) &&
        Objects.equals(enableOAuthAppCreation, keyManager.enableOAuthAppCreation) &&
        Objects.equals(enableSelfValidationJWT, keyManager.enableSelfValidationJWT) &&
        Objects.equals(claimMapping, keyManager.claimMapping) &&
        Objects.equals(consumerKeyClaim, keyManager.consumerKeyClaim) &&
        Objects.equals(scopesClaim, keyManager.scopesClaim) &&
        Objects.equals(tokenValidation, keyManager.tokenValidation) &&
        Objects.equals(enabled, keyManager.enabled) &&
        Objects.equals(additionalProperties, keyManager.additionalProperties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, displayName, type, description, wellKnownEndpoint, introspectionEndpoint, clientRegistrationEndpoint, tokenEndpoint, revokeEndpoint, userInfoEndpoint, authorizeEndpoint, certificates, issuer, scopeManagementEndpoint, availableGrantTypes, enableTokenGeneration, enableTokenEncryption, enableTokenHashing, enableMapOAuthConsumerApps, enableOAuthAppCreation, enableSelfValidationJWT, claimMapping, consumerKeyClaim, scopesClaim, tokenValidation, enabled, additionalProperties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class KeyManagerDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    wellKnownEndpoint: ").append(toIndentedString(wellKnownEndpoint)).append("\n");
    sb.append("    introspectionEndpoint: ").append(toIndentedString(introspectionEndpoint)).append("\n");
    sb.append("    clientRegistrationEndpoint: ").append(toIndentedString(clientRegistrationEndpoint)).append("\n");
    sb.append("    tokenEndpoint: ").append(toIndentedString(tokenEndpoint)).append("\n");
    sb.append("    revokeEndpoint: ").append(toIndentedString(revokeEndpoint)).append("\n");
    sb.append("    userInfoEndpoint: ").append(toIndentedString(userInfoEndpoint)).append("\n");
    sb.append("    authorizeEndpoint: ").append(toIndentedString(authorizeEndpoint)).append("\n");
    sb.append("    certificates: ").append(toIndentedString(certificates)).append("\n");
    sb.append("    issuer: ").append(toIndentedString(issuer)).append("\n");
    sb.append("    scopeManagementEndpoint: ").append(toIndentedString(scopeManagementEndpoint)).append("\n");
    sb.append("    availableGrantTypes: ").append(toIndentedString(availableGrantTypes)).append("\n");
    sb.append("    enableTokenGeneration: ").append(toIndentedString(enableTokenGeneration)).append("\n");
    sb.append("    enableTokenEncryption: ").append(toIndentedString(enableTokenEncryption)).append("\n");
    sb.append("    enableTokenHashing: ").append(toIndentedString(enableTokenHashing)).append("\n");
    sb.append("    enableMapOAuthConsumerApps: ").append(toIndentedString(enableMapOAuthConsumerApps)).append("\n");
    sb.append("    enableOAuthAppCreation: ").append(toIndentedString(enableOAuthAppCreation)).append("\n");
    sb.append("    enableSelfValidationJWT: ").append(toIndentedString(enableSelfValidationJWT)).append("\n");
    sb.append("    claimMapping: ").append(toIndentedString(claimMapping)).append("\n");
    sb.append("    consumerKeyClaim: ").append(toIndentedString(consumerKeyClaim)).append("\n");
    sb.append("    scopesClaim: ").append(toIndentedString(scopesClaim)).append("\n");
    sb.append("    tokenValidation: ").append(toIndentedString(tokenValidation)).append("\n");
    sb.append("    enabled: ").append(toIndentedString(enabled)).append("\n");
    sb.append("    additionalProperties: ").append(toIndentedString(additionalProperties)).append("\n");
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

