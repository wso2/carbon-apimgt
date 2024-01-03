package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ClaimMappingEntryDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.KeyManagerCertificatesDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.KeyManagerEndpointDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.KeyManagerPermissionsDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.TokenValidationDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



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
    private String displayTokenEndpoint = null;
    private String revokeEndpoint = null;
    private String displayRevokeEndpoint = null;
    private String userInfoEndpoint = null;
    private String authorizeEndpoint = null;
    private List<KeyManagerEndpointDTO> endpoints = new ArrayList<KeyManagerEndpointDTO>();
    private KeyManagerCertificatesDTO certificates = null;
    private String issuer = null;
    private String alias = null;
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
    private Boolean global = null;
    private Object additionalProperties = null;
    private KeyManagerPermissionsDTO permissions = null;

    @XmlType(name="TokenTypeEnum")
    @XmlEnum(String.class)
    public enum TokenTypeEnum {
        EXCHANGED("EXCHANGED"),
        DIRECT("DIRECT"),
        BOTH("BOTH");
        private String value;

        TokenTypeEnum (String v) {
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
        public static TokenTypeEnum fromValue(String v) {
            for (TokenTypeEnum b : TokenTypeEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private TokenTypeEnum tokenType = TokenTypeEnum.DIRECT;

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

  
  @ApiModelProperty(example = "WSO2 Identity Server", required = true, value = "")
  @JsonProperty("name")
  @NotNull
 @Size(min=1,max=100)  public String getName() {
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

  
  @ApiModelProperty(example = "WSO2 Identity Server", value = "display name of Key Manager to  show in UI ")
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

  
  @ApiModelProperty(example = "WSO2-IS", required = true, value = "")
  @JsonProperty("type")
  @NotNull
 @Size(min=1,max=45)  public String getType() {
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

  
  @ApiModelProperty(value = "Well-Known Endpoint of Identity Provider. ")
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

  
  @ApiModelProperty(example = "https://localhost:9444/oauth2/introspect", value = "")
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

  
  @ApiModelProperty(example = "https://localhost:9444/keymanager-operations/dcr/register", value = "")
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

  
  @ApiModelProperty(example = "https://localhost:9444/oauth2/token", value = "")
  @JsonProperty("tokenEndpoint")
  public String getTokenEndpoint() {
    return tokenEndpoint;
  }
  public void setTokenEndpoint(String tokenEndpoint) {
    this.tokenEndpoint = tokenEndpoint;
  }

  /**
   **/
  public KeyManagerDTO displayTokenEndpoint(String displayTokenEndpoint) {
    this.displayTokenEndpoint = displayTokenEndpoint;
    return this;
  }

  
  @ApiModelProperty(example = "https://localhost:9444/oauth2/token", value = "")
  @JsonProperty("displayTokenEndpoint")
  public String getDisplayTokenEndpoint() {
    return displayTokenEndpoint;
  }
  public void setDisplayTokenEndpoint(String displayTokenEndpoint) {
    this.displayTokenEndpoint = displayTokenEndpoint;
  }

  /**
   **/
  public KeyManagerDTO revokeEndpoint(String revokeEndpoint) {
    this.revokeEndpoint = revokeEndpoint;
    return this;
  }

  
  @ApiModelProperty(example = "https://localhost:9444/oauth2/revoke", value = "")
  @JsonProperty("revokeEndpoint")
  public String getRevokeEndpoint() {
    return revokeEndpoint;
  }
  public void setRevokeEndpoint(String revokeEndpoint) {
    this.revokeEndpoint = revokeEndpoint;
  }

  /**
   **/
  public KeyManagerDTO displayRevokeEndpoint(String displayRevokeEndpoint) {
    this.displayRevokeEndpoint = displayRevokeEndpoint;
    return this;
  }

  
  @ApiModelProperty(example = "https://localhost:9444/oauth2/revoke", value = "")
  @JsonProperty("displayRevokeEndpoint")
  public String getDisplayRevokeEndpoint() {
    return displayRevokeEndpoint;
  }
  public void setDisplayRevokeEndpoint(String displayRevokeEndpoint) {
    this.displayRevokeEndpoint = displayRevokeEndpoint;
  }

  /**
   **/
  public KeyManagerDTO userInfoEndpoint(String userInfoEndpoint) {
    this.userInfoEndpoint = userInfoEndpoint;
    return this;
  }

  
  @ApiModelProperty(example = "https://localhost:9444/oauth2/userinfo?schema=openid", value = "")
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

  
  @ApiModelProperty(example = "https://localhost:9444/oauth2/authorize", value = "")
  @JsonProperty("authorizeEndpoint")
  public String getAuthorizeEndpoint() {
    return authorizeEndpoint;
  }
  public void setAuthorizeEndpoint(String authorizeEndpoint) {
    this.authorizeEndpoint = authorizeEndpoint;
  }

  /**
   **/
  public KeyManagerDTO endpoints(List<KeyManagerEndpointDTO> endpoints) {
    this.endpoints = endpoints;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("endpoints")
  public List<KeyManagerEndpointDTO> getEndpoints() {
    return endpoints;
  }
  public void setEndpoints(List<KeyManagerEndpointDTO> endpoints) {
    this.endpoints = endpoints;
  }

  /**
   **/
  public KeyManagerDTO certificates(KeyManagerCertificatesDTO certificates) {
    this.certificates = certificates;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
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

  
  @ApiModelProperty(example = "https://localhost:9444/services", value = "")
  @JsonProperty("issuer")
  public String getIssuer() {
    return issuer;
  }
  public void setIssuer(String issuer) {
    this.issuer = issuer;
  }

  /**
   * The alias of Identity Provider. If the tokenType is EXCHANGED, the alias value should be inclusive in the audience values of the JWT token 
   **/
  public KeyManagerDTO alias(String alias) {
    this.alias = alias;
    return this;
  }

  
  @ApiModelProperty(example = "https://localhost:9443/oauth2/token", value = "The alias of Identity Provider. If the tokenType is EXCHANGED, the alias value should be inclusive in the audience values of the JWT token ")
  @JsonProperty("alias")
  public String getAlias() {
    return alias;
  }
  public void setAlias(String alias) {
    this.alias = alias;
  }

  /**
   **/
  public KeyManagerDTO scopeManagementEndpoint(String scopeManagementEndpoint) {
    this.scopeManagementEndpoint = scopeManagementEndpoint;
    return this;
  }

  
  @ApiModelProperty(example = "https://wso2is.com:9444/api/identity/oauth2/v1.0/scopes", value = "")
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
      @Valid
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
      @Valid
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
  public KeyManagerDTO global(Boolean global) {
    this.global = global;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "")
  @JsonProperty("global")
  public Boolean isGlobal() {
    return global;
  }
  public void setGlobal(Boolean global) {
    this.global = global;
  }

  /**
   **/
  public KeyManagerDTO additionalProperties(Object additionalProperties) {
    this.additionalProperties = additionalProperties;
    return this;
  }

  
  @ApiModelProperty(example = "{\"self_validate_jwt\":true,\"Username\":\"admin\",\"Password\":\"admin\"}", value = "")
      @Valid
  @JsonProperty("additionalProperties")
  public Object getAdditionalProperties() {
    return additionalProperties;
  }
  public void setAdditionalProperties(Object additionalProperties) {
    this.additionalProperties = additionalProperties;
  }

  /**
   **/
  public KeyManagerDTO permissions(KeyManagerPermissionsDTO permissions) {
    this.permissions = permissions;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("permissions")
  public KeyManagerPermissionsDTO getPermissions() {
    return permissions;
  }
  public void setPermissions(KeyManagerPermissionsDTO permissions) {
    this.permissions = permissions;
  }

  /**
   * The type of the tokens to be used (exchanged or without exchanged). Accepted values are EXCHANGED, DIRECT and BOTH.
   **/
  public KeyManagerDTO tokenType(TokenTypeEnum tokenType) {
    this.tokenType = tokenType;
    return this;
  }

  
  @ApiModelProperty(example = "EXCHANGED", value = "The type of the tokens to be used (exchanged or without exchanged). Accepted values are EXCHANGED, DIRECT and BOTH.")
  @JsonProperty("tokenType")
  public TokenTypeEnum getTokenType() {
    return tokenType;
  }
  public void setTokenType(TokenTypeEnum tokenType) {
    this.tokenType = tokenType;
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
        Objects.equals(displayTokenEndpoint, keyManager.displayTokenEndpoint) &&
        Objects.equals(revokeEndpoint, keyManager.revokeEndpoint) &&
        Objects.equals(displayRevokeEndpoint, keyManager.displayRevokeEndpoint) &&
        Objects.equals(userInfoEndpoint, keyManager.userInfoEndpoint) &&
        Objects.equals(authorizeEndpoint, keyManager.authorizeEndpoint) &&
        Objects.equals(endpoints, keyManager.endpoints) &&
        Objects.equals(certificates, keyManager.certificates) &&
        Objects.equals(issuer, keyManager.issuer) &&
        Objects.equals(alias, keyManager.alias) &&
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
        Objects.equals(global, keyManager.global) &&
        Objects.equals(additionalProperties, keyManager.additionalProperties) &&
        Objects.equals(permissions, keyManager.permissions) &&
        Objects.equals(tokenType, keyManager.tokenType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, displayName, type, description, wellKnownEndpoint, introspectionEndpoint, clientRegistrationEndpoint, tokenEndpoint, displayTokenEndpoint, revokeEndpoint, displayRevokeEndpoint, userInfoEndpoint, authorizeEndpoint, endpoints, certificates, issuer, alias, scopeManagementEndpoint, availableGrantTypes, enableTokenGeneration, enableTokenEncryption, enableTokenHashing, enableMapOAuthConsumerApps, enableOAuthAppCreation, enableSelfValidationJWT, claimMapping, consumerKeyClaim, scopesClaim, tokenValidation, enabled, global, additionalProperties, permissions, tokenType);
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
    sb.append("    displayTokenEndpoint: ").append(toIndentedString(displayTokenEndpoint)).append("\n");
    sb.append("    revokeEndpoint: ").append(toIndentedString(revokeEndpoint)).append("\n");
    sb.append("    displayRevokeEndpoint: ").append(toIndentedString(displayRevokeEndpoint)).append("\n");
    sb.append("    userInfoEndpoint: ").append(toIndentedString(userInfoEndpoint)).append("\n");
    sb.append("    authorizeEndpoint: ").append(toIndentedString(authorizeEndpoint)).append("\n");
    sb.append("    endpoints: ").append(toIndentedString(endpoints)).append("\n");
    sb.append("    certificates: ").append(toIndentedString(certificates)).append("\n");
    sb.append("    issuer: ").append(toIndentedString(issuer)).append("\n");
    sb.append("    alias: ").append(toIndentedString(alias)).append("\n");
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
    sb.append("    global: ").append(toIndentedString(global)).append("\n");
    sb.append("    additionalProperties: ").append(toIndentedString(additionalProperties)).append("\n");
    sb.append("    permissions: ").append(toIndentedString(permissions)).append("\n");
    sb.append("    tokenType: ").append(toIndentedString(tokenType)).append("\n");
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

