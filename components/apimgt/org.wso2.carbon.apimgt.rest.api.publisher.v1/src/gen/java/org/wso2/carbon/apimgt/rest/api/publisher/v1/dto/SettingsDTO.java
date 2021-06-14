package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DeploymentsDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.EnvironmentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MonetizationAttributeDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class SettingsDTO   {
  
    private String storeUrl = null;
    private List<EnvironmentDTO> environment = new ArrayList<>();
    private List<String> scopes = new ArrayList<>();
    private List<MonetizationAttributeDTO> monetizationAttributes = new ArrayList<>();
    private Object securityAuditProperties = null;
    private Boolean externalStoresEnabled = null;
    private Boolean docVisibilityEnabled = null;
    private Boolean crossTenantSubscriptionEnabled = false;
    private String defaultAdvancePolicy = null;
    private String defaultSubscriptionPolicy = null;
    private List<DeploymentsDTO> deployments = new ArrayList<>();

  /**
   * Store URL
   **/
  public SettingsDTO storeUrl(String storeUrl) {
    this.storeUrl = storeUrl;
    return this;
  }

  
  @ApiModelProperty(value = "Store URL")
  @JsonProperty("storeUrl")
  public String getStoreUrl() {
    return storeUrl;
  }
  public void setStoreUrl(String storeUrl) {
    this.storeUrl = storeUrl;
  }

  /**
   **/
  public SettingsDTO environment(List<EnvironmentDTO> environment) {
    this.environment = environment;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("environment")
  public List<EnvironmentDTO> getEnvironment() {
    return environment;
  }
  public void setEnvironment(List<EnvironmentDTO> environment) {
    this.environment = environment;
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
  public SettingsDTO monetizationAttributes(List<MonetizationAttributeDTO> monetizationAttributes) {
    this.monetizationAttributes = monetizationAttributes;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("monetizationAttributes")
  public List<MonetizationAttributeDTO> getMonetizationAttributes() {
    return monetizationAttributes;
  }
  public void setMonetizationAttributes(List<MonetizationAttributeDTO> monetizationAttributes) {
    this.monetizationAttributes = monetizationAttributes;
  }

  /**
   **/
  public SettingsDTO securityAuditProperties(Object securityAuditProperties) {
    this.securityAuditProperties = securityAuditProperties;
    return this;
  }

  
  @ApiModelProperty(value = "")
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
   **/
  public SettingsDTO deployments(List<DeploymentsDTO> deployments) {
    this.deployments = deployments;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("deployments")
  public List<DeploymentsDTO> getDeployments() {
    return deployments;
  }
  public void setDeployments(List<DeploymentsDTO> deployments) {
    this.deployments = deployments;
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
    return Objects.equals(storeUrl, settings.storeUrl) &&
        Objects.equals(environment, settings.environment) &&
        Objects.equals(scopes, settings.scopes) &&
        Objects.equals(monetizationAttributes, settings.monetizationAttributes) &&
        Objects.equals(securityAuditProperties, settings.securityAuditProperties) &&
        Objects.equals(externalStoresEnabled, settings.externalStoresEnabled) &&
        Objects.equals(docVisibilityEnabled, settings.docVisibilityEnabled) &&
        Objects.equals(crossTenantSubscriptionEnabled, settings.crossTenantSubscriptionEnabled) &&
        Objects.equals(defaultAdvancePolicy, settings.defaultAdvancePolicy) &&
        Objects.equals(defaultSubscriptionPolicy, settings.defaultSubscriptionPolicy) &&
        Objects.equals(deployments, settings.deployments);
  }

  @Override
  public int hashCode() {
    return Objects.hash(storeUrl, environment, scopes, monetizationAttributes, securityAuditProperties, externalStoresEnabled, docVisibilityEnabled, crossTenantSubscriptionEnabled, defaultAdvancePolicy, defaultSubscriptionPolicy, deployments);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SettingsDTO {\n");
    
    sb.append("    storeUrl: ").append(toIndentedString(storeUrl)).append("\n");
    sb.append("    environment: ").append(toIndentedString(environment)).append("\n");
    sb.append("    scopes: ").append(toIndentedString(scopes)).append("\n");
    sb.append("    monetizationAttributes: ").append(toIndentedString(monetizationAttributes)).append("\n");
    sb.append("    securityAuditProperties: ").append(toIndentedString(securityAuditProperties)).append("\n");
    sb.append("    externalStoresEnabled: ").append(toIndentedString(externalStoresEnabled)).append("\n");
    sb.append("    docVisibilityEnabled: ").append(toIndentedString(docVisibilityEnabled)).append("\n");
    sb.append("    crossTenantSubscriptionEnabled: ").append(toIndentedString(crossTenantSubscriptionEnabled)).append("\n");
    sb.append("    defaultAdvancePolicy: ").append(toIndentedString(defaultAdvancePolicy)).append("\n");
    sb.append("    defaultSubscriptionPolicy: ").append(toIndentedString(defaultSubscriptionPolicy)).append("\n");
    sb.append("    deployments: ").append(toIndentedString(deployments)).append("\n");
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

