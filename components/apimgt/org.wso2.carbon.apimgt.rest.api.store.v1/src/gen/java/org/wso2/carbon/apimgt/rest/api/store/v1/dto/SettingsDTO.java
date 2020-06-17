package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.SettingsIdentityProviderDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class SettingsDTO   {
  
    private List<String> grantTypes = new ArrayList<>();
    private List<String> scopes = new ArrayList<>();
    private Boolean applicationSharingEnabled = false;
    private Boolean mapExistingAuthApps = false;
    private String apiGatewayEndpoint = null;
    private Boolean monetizationEnabled = false;
    private Boolean recommendationEnabled = false;
    private Boolean isUnlimitedTierPaid = false;
    private SettingsIdentityProviderDTO identityProvider = null;
    private Boolean isAnonymousModeEnabled = true;

  /**
   **/
  public SettingsDTO grantTypes(List<String> grantTypes) {
    this.grantTypes = grantTypes;
    return this;
  }

  
  @ApiModelProperty(value = "")
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

  
  @ApiModelProperty(value = "")
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
        Objects.equals(isAnonymousModeEnabled, settings.isAnonymousModeEnabled);
  }

  @Override
  public int hashCode() {
    return Objects.hash(grantTypes, scopes, applicationSharingEnabled, mapExistingAuthApps, apiGatewayEndpoint, monetizationEnabled, recommendationEnabled, isUnlimitedTierPaid, identityProvider, isAnonymousModeEnabled);
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

