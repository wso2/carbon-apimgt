package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.SettingsIdentityProviderDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class SettingsDTO   {
  
    private List<String> grantTypes = new ArrayList<String>();
    private List<String> scopes = new ArrayList<String>();
    private Boolean applicationSharingEnabled = false;
    private Boolean mapExistingAuthApps = false;
    private String apiGatewayEndpoint = null;
    private Boolean monetizationEnabled = false;
    private Boolean recommendationEnabled = false;
    private Boolean isUnlimitedTierPaid = false;
    private SettingsIdentityProviderDTO identityProvider = null;
    private Boolean isAnonymousModeEnabled = true;
    private Boolean isPasswordChangeEnabled = true;
    private String userStorePasswordPattern = null;
    private String passwordPolicyPattern = null;
    private Integer passwordPolicyMinLength = null;
    private Integer passwordPolicyMaxLength = null;

  /**
   **/
  public SettingsDTO grantTypes(List<String> grantTypes) {
    this.grantTypes = grantTypes;
    return this;
  }

  
  @ApiModelProperty(example = "[\"refresh_token\",\"urn:ietf:params:oauth:grant-type:saml2-bearer\",\"password\",\"client_credentials\",\"iwa:ntlm\",\"authorization_code\",\"urn:ietf:params:oauth:grant-type:jwt-bearer\"]", value = "")
  @JsonProperty("grantTypes")
  public List<String> getGrantTypes() {
    return grantTypes;
  }
  public void setGrantTypes(List<String> grantTypes) {
    this.grantTypes = grantTypes;
  }

  /**
   **/
  public SettingsDTO scopes(List<String> scopes) {
    this.scopes = scopes;
    return this;
  }

  
  @ApiModelProperty(example = "[\"apim:api_key\",\"apim:app_import_export\",\"apim:app_manage\",\"apim:store_settings\",\"apim:sub_alert_manage\",\"apim:sub_manage\",\"apim:subscribe\",\"openid\"]", value = "")
  @JsonProperty("scopes")
  public List<String> getScopes() {
    return scopes;
  }
  public void setScopes(List<String> scopes) {
    this.scopes = scopes;
  }

  /**
   **/
  public SettingsDTO applicationSharingEnabled(Boolean applicationSharingEnabled) {
    this.applicationSharingEnabled = applicationSharingEnabled;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("applicationSharingEnabled")
  public Boolean isApplicationSharingEnabled() {
    return applicationSharingEnabled;
  }
  public void setApplicationSharingEnabled(Boolean applicationSharingEnabled) {
    this.applicationSharingEnabled = applicationSharingEnabled;
  }

  /**
   **/
  public SettingsDTO mapExistingAuthApps(Boolean mapExistingAuthApps) {
    this.mapExistingAuthApps = mapExistingAuthApps;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("mapExistingAuthApps")
  public Boolean isMapExistingAuthApps() {
    return mapExistingAuthApps;
  }
  public void setMapExistingAuthApps(Boolean mapExistingAuthApps) {
    this.mapExistingAuthApps = mapExistingAuthApps;
  }

  /**
   **/
  public SettingsDTO apiGatewayEndpoint(String apiGatewayEndpoint) {
    this.apiGatewayEndpoint = apiGatewayEndpoint;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("apiGatewayEndpoint")
  public String getApiGatewayEndpoint() {
    return apiGatewayEndpoint;
  }
  public void setApiGatewayEndpoint(String apiGatewayEndpoint) {
    this.apiGatewayEndpoint = apiGatewayEndpoint;
  }

  /**
   **/
  public SettingsDTO monetizationEnabled(Boolean monetizationEnabled) {
    this.monetizationEnabled = monetizationEnabled;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("monetizationEnabled")
  public Boolean isMonetizationEnabled() {
    return monetizationEnabled;
  }
  public void setMonetizationEnabled(Boolean monetizationEnabled) {
    this.monetizationEnabled = monetizationEnabled;
  }

  /**
   **/
  public SettingsDTO recommendationEnabled(Boolean recommendationEnabled) {
    this.recommendationEnabled = recommendationEnabled;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("recommendationEnabled")
  public Boolean isRecommendationEnabled() {
    return recommendationEnabled;
  }
  public void setRecommendationEnabled(Boolean recommendationEnabled) {
    this.recommendationEnabled = recommendationEnabled;
  }

  /**
   **/
  public SettingsDTO isUnlimitedTierPaid(Boolean isUnlimitedTierPaid) {
    this.isUnlimitedTierPaid = isUnlimitedTierPaid;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("IsUnlimitedTierPaid")
  public Boolean isIsUnlimitedTierPaid() {
    return isUnlimitedTierPaid;
  }
  public void setIsUnlimitedTierPaid(Boolean isUnlimitedTierPaid) {
    this.isUnlimitedTierPaid = isUnlimitedTierPaid;
  }

  /**
   **/
  public SettingsDTO identityProvider(SettingsIdentityProviderDTO identityProvider) {
    this.identityProvider = identityProvider;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("identityProvider")
  public SettingsIdentityProviderDTO getIdentityProvider() {
    return identityProvider;
  }
  public void setIdentityProvider(SettingsIdentityProviderDTO identityProvider) {
    this.identityProvider = identityProvider;
  }

  /**
   **/
  public SettingsDTO isAnonymousModeEnabled(Boolean isAnonymousModeEnabled) {
    this.isAnonymousModeEnabled = isAnonymousModeEnabled;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("IsAnonymousModeEnabled")
  public Boolean isIsAnonymousModeEnabled() {
    return isAnonymousModeEnabled;
  }
  public void setIsAnonymousModeEnabled(Boolean isAnonymousModeEnabled) {
    this.isAnonymousModeEnabled = isAnonymousModeEnabled;
  }

  /**
   **/
  public SettingsDTO isPasswordChangeEnabled(Boolean isPasswordChangeEnabled) {
    this.isPasswordChangeEnabled = isPasswordChangeEnabled;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("IsPasswordChangeEnabled")
  public Boolean isIsPasswordChangeEnabled() {
    return isPasswordChangeEnabled;
  }
  public void setIsPasswordChangeEnabled(Boolean isPasswordChangeEnabled) {
    this.isPasswordChangeEnabled = isPasswordChangeEnabled;
  }

  /**
   * The &#39;PasswordJavaRegEx&#39; cofigured in the UserStoreManager
   **/
  public SettingsDTO userStorePasswordPattern(String userStorePasswordPattern) {
    this.userStorePasswordPattern = userStorePasswordPattern;
    return this;
  }

  
  @ApiModelProperty(example = "^[\\S]{5,30}$", value = "The 'PasswordJavaRegEx' cofigured in the UserStoreManager")
  @JsonProperty("userStorePasswordPattern")
  public String getUserStorePasswordPattern() {
    return userStorePasswordPattern;
  }
  public void setUserStorePasswordPattern(String userStorePasswordPattern) {
    this.userStorePasswordPattern = userStorePasswordPattern;
  }

  /**
   * The regex configured in the Password Policy property &#39;passwordPolicy.pattern&#39;
   **/
  public SettingsDTO passwordPolicyPattern(String passwordPolicyPattern) {
    this.passwordPolicyPattern = passwordPolicyPattern;
    return this;
  }

  
  @ApiModelProperty(example = "^[\\S]{5,30}$", value = "The regex configured in the Password Policy property 'passwordPolicy.pattern'")
  @JsonProperty("passwordPolicyPattern")
  public String getPasswordPolicyPattern() {
    return passwordPolicyPattern;
  }
  public void setPasswordPolicyPattern(String passwordPolicyPattern) {
    this.passwordPolicyPattern = passwordPolicyPattern;
  }

  /**
   * If Password Policy Feature is enabled, the property &#39;passwordPolicy.min.length&#39; is returned as the &#39;passwordPolicyMinLength&#39;. If password policy is not enabled, default value -1 will be returned. And it should be noted that the regex pattern(s) returned in &#39;passwordPolicyPattern&#39; and &#39;userStorePasswordPattern&#39; properties too will affect the minimum password length allowed and an intersection of all conditions will be considered finally to validate the password.
   **/
  public SettingsDTO passwordPolicyMinLength(Integer passwordPolicyMinLength) {
    this.passwordPolicyMinLength = passwordPolicyMinLength;
    return this;
  }

  
  @ApiModelProperty(value = "If Password Policy Feature is enabled, the property 'passwordPolicy.min.length' is returned as the 'passwordPolicyMinLength'. If password policy is not enabled, default value -1 will be returned. And it should be noted that the regex pattern(s) returned in 'passwordPolicyPattern' and 'userStorePasswordPattern' properties too will affect the minimum password length allowed and an intersection of all conditions will be considered finally to validate the password.")
  @JsonProperty("passwordPolicyMinLength")
  public Integer getPasswordPolicyMinLength() {
    return passwordPolicyMinLength;
  }
  public void setPasswordPolicyMinLength(Integer passwordPolicyMinLength) {
    this.passwordPolicyMinLength = passwordPolicyMinLength;
  }

  /**
   * If Password Policy Feature is enabled, the property &#39;passwordPolicy.max.length&#39; is returned as the &#39;passwordPolicyMaxLength&#39;. If password policy is not enabled, default value -1 will be returned. And it should be noted that the regex pattern(s) returned in &#39;passwordPolicyPattern&#39; and &#39;userStorePasswordPattern&#39; properties too will affect the maximum password length allowed and an intersection of all conditions will be considered finally to validate the password.
   **/
  public SettingsDTO passwordPolicyMaxLength(Integer passwordPolicyMaxLength) {
    this.passwordPolicyMaxLength = passwordPolicyMaxLength;
    return this;
  }

  
  @ApiModelProperty(value = "If Password Policy Feature is enabled, the property 'passwordPolicy.max.length' is returned as the 'passwordPolicyMaxLength'. If password policy is not enabled, default value -1 will be returned. And it should be noted that the regex pattern(s) returned in 'passwordPolicyPattern' and 'userStorePasswordPattern' properties too will affect the maximum password length allowed and an intersection of all conditions will be considered finally to validate the password.")
  @JsonProperty("passwordPolicyMaxLength")
  public Integer getPasswordPolicyMaxLength() {
    return passwordPolicyMaxLength;
  }
  public void setPasswordPolicyMaxLength(Integer passwordPolicyMaxLength) {
    this.passwordPolicyMaxLength = passwordPolicyMaxLength;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SettingsDTO settings = (SettingsDTO) o;
    return Objects.equals(grantTypes, settings.grantTypes) &&
        Objects.equals(scopes, settings.scopes) &&
        Objects.equals(applicationSharingEnabled, settings.applicationSharingEnabled) &&
        Objects.equals(mapExistingAuthApps, settings.mapExistingAuthApps) &&
        Objects.equals(apiGatewayEndpoint, settings.apiGatewayEndpoint) &&
        Objects.equals(monetizationEnabled, settings.monetizationEnabled) &&
        Objects.equals(recommendationEnabled, settings.recommendationEnabled) &&
        Objects.equals(isUnlimitedTierPaid, settings.isUnlimitedTierPaid) &&
        Objects.equals(identityProvider, settings.identityProvider) &&
        Objects.equals(isAnonymousModeEnabled, settings.isAnonymousModeEnabled) &&
        Objects.equals(isPasswordChangeEnabled, settings.isPasswordChangeEnabled) &&
        Objects.equals(userStorePasswordPattern, settings.userStorePasswordPattern) &&
        Objects.equals(passwordPolicyPattern, settings.passwordPolicyPattern) &&
        Objects.equals(passwordPolicyMinLength, settings.passwordPolicyMinLength) &&
        Objects.equals(passwordPolicyMaxLength, settings.passwordPolicyMaxLength);
  }

  @Override
  public int hashCode() {
    return Objects.hash(grantTypes, scopes, applicationSharingEnabled, mapExistingAuthApps, apiGatewayEndpoint, monetizationEnabled, recommendationEnabled, isUnlimitedTierPaid, identityProvider, isAnonymousModeEnabled, isPasswordChangeEnabled, userStorePasswordPattern, passwordPolicyPattern, passwordPolicyMinLength, passwordPolicyMaxLength);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SettingsDTO {\n");
    
    sb.append("    grantTypes: ").append(toIndentedString(grantTypes)).append("\n");
    sb.append("    scopes: ").append(toIndentedString(scopes)).append("\n");
    sb.append("    applicationSharingEnabled: ").append(toIndentedString(applicationSharingEnabled)).append("\n");
    sb.append("    mapExistingAuthApps: ").append(toIndentedString(mapExistingAuthApps)).append("\n");
    sb.append("    apiGatewayEndpoint: ").append(toIndentedString(apiGatewayEndpoint)).append("\n");
    sb.append("    monetizationEnabled: ").append(toIndentedString(monetizationEnabled)).append("\n");
    sb.append("    recommendationEnabled: ").append(toIndentedString(recommendationEnabled)).append("\n");
    sb.append("    isUnlimitedTierPaid: ").append(toIndentedString(isUnlimitedTierPaid)).append("\n");
    sb.append("    identityProvider: ").append(toIndentedString(identityProvider)).append("\n");
    sb.append("    isAnonymousModeEnabled: ").append(toIndentedString(isAnonymousModeEnabled)).append("\n");
    sb.append("    isPasswordChangeEnabled: ").append(toIndentedString(isPasswordChangeEnabled)).append("\n");
    sb.append("    userStorePasswordPattern: ").append(toIndentedString(userStorePasswordPattern)).append("\n");
    sb.append("    passwordPolicyPattern: ").append(toIndentedString(passwordPolicyPattern)).append("\n");
    sb.append("    passwordPolicyMinLength: ").append(toIndentedString(passwordPolicyMinLength)).append("\n");
    sb.append("    passwordPolicyMaxLength: ").append(toIndentedString(passwordPolicyMaxLength)).append("\n");
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

