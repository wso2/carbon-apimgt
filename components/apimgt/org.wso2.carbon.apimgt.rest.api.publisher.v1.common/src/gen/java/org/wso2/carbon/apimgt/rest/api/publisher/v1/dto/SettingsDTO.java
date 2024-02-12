package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.EnvironmentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MonetizationAttributeDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SettingsCustomPropertiesDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SubscriberContactAttributeDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class SettingsDTO   {
  
    private String devportalUrl = null;
    private List<EnvironmentDTO> environment = new ArrayList<EnvironmentDTO>();
    private List<String> gatewayTypes = new ArrayList<String>();
    private List<String> scopes = new ArrayList<String>();
    private List<MonetizationAttributeDTO> monetizationAttributes = new ArrayList<MonetizationAttributeDTO>();
    private List<SubscriberContactAttributeDTO> subscriberContactAttributes = new ArrayList<SubscriberContactAttributeDTO>();
    private Object securityAuditProperties = null;
    private Boolean externalStoresEnabled = null;
    private Boolean docVisibilityEnabled = null;
    private Boolean crossTenantSubscriptionEnabled = false;
    private String defaultAdvancePolicy = null;
    private String defaultSubscriptionPolicy = null;
    private String authorizationHeader = null;
    private List<SettingsCustomPropertiesDTO> customProperties = new ArrayList<SettingsCustomPropertiesDTO>();

  /**
   * The Developer Portal URL
   **/
  public SettingsDTO devportalUrl(String devportalUrl) {
    this.devportalUrl = devportalUrl;
    return this;
  }

  
  @ApiModelProperty(example = "https://localhost:9443/devportal", value = "The Developer Portal URL")
  @JsonProperty("devportalUrl")
  public String getDevportalUrl() {
    return devportalUrl;
  }
  public void setDevportalUrl(String devportalUrl) {
    this.devportalUrl = devportalUrl;
  }

  /**
   **/
  public SettingsDTO environment(List<EnvironmentDTO> environment) {
    this.environment = environment;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("environment")
  public List<EnvironmentDTO> getEnvironment() {
    return environment;
  }
  public void setEnvironment(List<EnvironmentDTO> environment) {
    this.environment = environment;
  }

  /**
   **/
  public SettingsDTO gatewayTypes(List<String> gatewayTypes) {
    this.gatewayTypes = gatewayTypes;
    return this;
  }

  
  @ApiModelProperty(example = "[\"Regular\",\"APK\"]", value = "")
  @JsonProperty("gatewayTypes")
  public List<String> getGatewayTypes() {
    return gatewayTypes;
  }
  public void setGatewayTypes(List<String> gatewayTypes) {
    this.gatewayTypes = gatewayTypes;
  }

  /**
   **/
  public SettingsDTO scopes(List<String> scopes) {
    this.scopes = scopes;
    return this;
  }

  
  @ApiModelProperty(example = "[\"apim:api_create\",\"apim:api_manage\",\"apim:api_publish\"]", value = "")
  @JsonProperty("scopes")
  public List<String> getScopes() {
    return scopes;
  }
  public void setScopes(List<String> scopes) {
    this.scopes = scopes;
  }

  /**
   **/
  public SettingsDTO monetizationAttributes(List<MonetizationAttributeDTO> monetizationAttributes) {
    this.monetizationAttributes = monetizationAttributes;
    return this;
  }

  
  @ApiModelProperty(example = "[]", value = "")
      @Valid
  @JsonProperty("monetizationAttributes")
  public List<MonetizationAttributeDTO> getMonetizationAttributes() {
    return monetizationAttributes;
  }
  public void setMonetizationAttributes(List<MonetizationAttributeDTO> monetizationAttributes) {
    this.monetizationAttributes = monetizationAttributes;
  }

  /**
   **/
  public SettingsDTO subscriberContactAttributes(List<SubscriberContactAttributeDTO> subscriberContactAttributes) {
    this.subscriberContactAttributes = subscriberContactAttributes;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("subscriberContactAttributes")
  public List<SubscriberContactAttributeDTO> getSubscriberContactAttributes() {
    return subscriberContactAttributes;
  }
  public void setSubscriberContactAttributes(List<SubscriberContactAttributeDTO> subscriberContactAttributes) {
    this.subscriberContactAttributes = subscriberContactAttributes;
  }

  /**
   **/
  public SettingsDTO securityAuditProperties(Object securityAuditProperties) {
    this.securityAuditProperties = securityAuditProperties;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("securityAuditProperties")
  public Object getSecurityAuditProperties() {
    return securityAuditProperties;
  }
  public void setSecurityAuditProperties(Object securityAuditProperties) {
    this.securityAuditProperties = securityAuditProperties;
  }

  /**
   * Is External Stores configuration enabled 
   **/
  public SettingsDTO externalStoresEnabled(Boolean externalStoresEnabled) {
    this.externalStoresEnabled = externalStoresEnabled;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "Is External Stores configuration enabled ")
  @JsonProperty("externalStoresEnabled")
  public Boolean isExternalStoresEnabled() {
    return externalStoresEnabled;
  }
  public void setExternalStoresEnabled(Boolean externalStoresEnabled) {
    this.externalStoresEnabled = externalStoresEnabled;
  }

  /**
   * Is Document Visibility configuration enabled 
   **/
  public SettingsDTO docVisibilityEnabled(Boolean docVisibilityEnabled) {
    this.docVisibilityEnabled = docVisibilityEnabled;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "Is Document Visibility configuration enabled ")
  @JsonProperty("docVisibilityEnabled")
  public Boolean isDocVisibilityEnabled() {
    return docVisibilityEnabled;
  }
  public void setDocVisibilityEnabled(Boolean docVisibilityEnabled) {
    this.docVisibilityEnabled = docVisibilityEnabled;
  }

  /**
   * Is Cross Tenant Subscriptions Enabled 
   **/
  public SettingsDTO crossTenantSubscriptionEnabled(Boolean crossTenantSubscriptionEnabled) {
    this.crossTenantSubscriptionEnabled = crossTenantSubscriptionEnabled;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "Is Cross Tenant Subscriptions Enabled ")
  @JsonProperty("crossTenantSubscriptionEnabled")
  public Boolean isCrossTenantSubscriptionEnabled() {
    return crossTenantSubscriptionEnabled;
  }
  public void setCrossTenantSubscriptionEnabled(Boolean crossTenantSubscriptionEnabled) {
    this.crossTenantSubscriptionEnabled = crossTenantSubscriptionEnabled;
  }

  /**
   * Default Advance Policy.
   **/
  public SettingsDTO defaultAdvancePolicy(String defaultAdvancePolicy) {
    this.defaultAdvancePolicy = defaultAdvancePolicy;
    return this;
  }

  
  @ApiModelProperty(value = "Default Advance Policy.")
  @JsonProperty("defaultAdvancePolicy")
  public String getDefaultAdvancePolicy() {
    return defaultAdvancePolicy;
  }
  public void setDefaultAdvancePolicy(String defaultAdvancePolicy) {
    this.defaultAdvancePolicy = defaultAdvancePolicy;
  }

  /**
   * Default Subscription Policy.
   **/
  public SettingsDTO defaultSubscriptionPolicy(String defaultSubscriptionPolicy) {
    this.defaultSubscriptionPolicy = defaultSubscriptionPolicy;
    return this;
  }

  
  @ApiModelProperty(value = "Default Subscription Policy.")
  @JsonProperty("defaultSubscriptionPolicy")
  public String getDefaultSubscriptionPolicy() {
    return defaultSubscriptionPolicy;
  }
  public void setDefaultSubscriptionPolicy(String defaultSubscriptionPolicy) {
    this.defaultSubscriptionPolicy = defaultSubscriptionPolicy;
  }

  /**
   * Authorization Header
   **/
  public SettingsDTO authorizationHeader(String authorizationHeader) {
    this.authorizationHeader = authorizationHeader;
    return this;
  }

  
  @ApiModelProperty(example = "authorization", value = "Authorization Header")
  @JsonProperty("authorizationHeader")
  public String getAuthorizationHeader() {
    return authorizationHeader;
  }
  public void setAuthorizationHeader(String authorizationHeader) {
    this.authorizationHeader = authorizationHeader;
  }

  /**
   **/
  public SettingsDTO customProperties(List<SettingsCustomPropertiesDTO> customProperties) {
    this.customProperties = customProperties;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("customProperties")
  public List<SettingsCustomPropertiesDTO> getCustomProperties() {
    return customProperties;
  }
  public void setCustomProperties(List<SettingsCustomPropertiesDTO> customProperties) {
    this.customProperties = customProperties;
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
    return Objects.equals(devportalUrl, settings.devportalUrl) &&
        Objects.equals(environment, settings.environment) &&
        Objects.equals(gatewayTypes, settings.gatewayTypes) &&
        Objects.equals(scopes, settings.scopes) &&
        Objects.equals(monetizationAttributes, settings.monetizationAttributes) &&
        Objects.equals(subscriberContactAttributes, settings.subscriberContactAttributes) &&
        Objects.equals(securityAuditProperties, settings.securityAuditProperties) &&
        Objects.equals(externalStoresEnabled, settings.externalStoresEnabled) &&
        Objects.equals(docVisibilityEnabled, settings.docVisibilityEnabled) &&
        Objects.equals(crossTenantSubscriptionEnabled, settings.crossTenantSubscriptionEnabled) &&
        Objects.equals(defaultAdvancePolicy, settings.defaultAdvancePolicy) &&
        Objects.equals(defaultSubscriptionPolicy, settings.defaultSubscriptionPolicy) &&
        Objects.equals(authorizationHeader, settings.authorizationHeader) &&
        Objects.equals(customProperties, settings.customProperties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(devportalUrl, environment, gatewayTypes, scopes, monetizationAttributes, subscriberContactAttributes, securityAuditProperties, externalStoresEnabled, docVisibilityEnabled, crossTenantSubscriptionEnabled, defaultAdvancePolicy, defaultSubscriptionPolicy, authorizationHeader, customProperties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SettingsDTO {\n");
    
    sb.append("    devportalUrl: ").append(toIndentedString(devportalUrl)).append("\n");
    sb.append("    environment: ").append(toIndentedString(environment)).append("\n");
    sb.append("    gatewayTypes: ").append(toIndentedString(gatewayTypes)).append("\n");
    sb.append("    scopes: ").append(toIndentedString(scopes)).append("\n");
    sb.append("    monetizationAttributes: ").append(toIndentedString(monetizationAttributes)).append("\n");
    sb.append("    subscriberContactAttributes: ").append(toIndentedString(subscriberContactAttributes)).append("\n");
    sb.append("    securityAuditProperties: ").append(toIndentedString(securityAuditProperties)).append("\n");
    sb.append("    externalStoresEnabled: ").append(toIndentedString(externalStoresEnabled)).append("\n");
    sb.append("    docVisibilityEnabled: ").append(toIndentedString(docVisibilityEnabled)).append("\n");
    sb.append("    crossTenantSubscriptionEnabled: ").append(toIndentedString(crossTenantSubscriptionEnabled)).append("\n");
    sb.append("    defaultAdvancePolicy: ").append(toIndentedString(defaultAdvancePolicy)).append("\n");
    sb.append("    defaultSubscriptionPolicy: ").append(toIndentedString(defaultSubscriptionPolicy)).append("\n");
    sb.append("    authorizationHeader: ").append(toIndentedString(authorizationHeader)).append("\n");
    sb.append("    customProperties: ").append(toIndentedString(customProperties)).append("\n");
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

