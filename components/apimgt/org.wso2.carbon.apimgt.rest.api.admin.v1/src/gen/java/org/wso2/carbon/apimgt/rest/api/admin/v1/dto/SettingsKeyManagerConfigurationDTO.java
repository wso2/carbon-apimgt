package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.KeyManagerConfigurationDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class SettingsKeyManagerConfigurationDTO   {
  
    private String type = null;
    private String displayName = null;
    private String defaultConsumerKeyClaim = null;
    private String defaultScopesClaim = null;
    private List<KeyManagerConfigurationDTO> configurations = new ArrayList<KeyManagerConfigurationDTO>();
    private List<KeyManagerConfigurationDTO> endpointConfigurations = new ArrayList<KeyManagerConfigurationDTO>();

  /**
   **/
  public SettingsKeyManagerConfigurationDTO type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(example = "default", value = "")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  /**
   **/
  public SettingsKeyManagerConfigurationDTO displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(example = "default", value = "")
  @JsonProperty("displayName")
  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   **/
  public SettingsKeyManagerConfigurationDTO defaultConsumerKeyClaim(String defaultConsumerKeyClaim) {
    this.defaultConsumerKeyClaim = defaultConsumerKeyClaim;
    return this;
  }

  
  @ApiModelProperty(example = "azp", value = "")
  @JsonProperty("defaultConsumerKeyClaim")
  public String getDefaultConsumerKeyClaim() {
    return defaultConsumerKeyClaim;
  }
  public void setDefaultConsumerKeyClaim(String defaultConsumerKeyClaim) {
    this.defaultConsumerKeyClaim = defaultConsumerKeyClaim;
  }

  /**
   **/
  public SettingsKeyManagerConfigurationDTO defaultScopesClaim(String defaultScopesClaim) {
    this.defaultScopesClaim = defaultScopesClaim;
    return this;
  }

  
  @ApiModelProperty(example = "scope", value = "")
  @JsonProperty("defaultScopesClaim")
  public String getDefaultScopesClaim() {
    return defaultScopesClaim;
  }
  public void setDefaultScopesClaim(String defaultScopesClaim) {
    this.defaultScopesClaim = defaultScopesClaim;
  }

  /**
   **/
  public SettingsKeyManagerConfigurationDTO configurations(List<KeyManagerConfigurationDTO> configurations) {
    this.configurations = configurations;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("configurations")
  public List<KeyManagerConfigurationDTO> getConfigurations() {
    return configurations;
  }
  public void setConfigurations(List<KeyManagerConfigurationDTO> configurations) {
    this.configurations = configurations;
  }

  /**
   **/
  public SettingsKeyManagerConfigurationDTO endpointConfigurations(List<KeyManagerConfigurationDTO> endpointConfigurations) {
    this.endpointConfigurations = endpointConfigurations;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("endpointConfigurations")
  public List<KeyManagerConfigurationDTO> getEndpointConfigurations() {
    return endpointConfigurations;
  }
  public void setEndpointConfigurations(List<KeyManagerConfigurationDTO> endpointConfigurations) {
    this.endpointConfigurations = endpointConfigurations;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SettingsKeyManagerConfigurationDTO settingsKeyManagerConfiguration = (SettingsKeyManagerConfigurationDTO) o;
    return Objects.equals(type, settingsKeyManagerConfiguration.type) &&
        Objects.equals(displayName, settingsKeyManagerConfiguration.displayName) &&
        Objects.equals(defaultConsumerKeyClaim, settingsKeyManagerConfiguration.defaultConsumerKeyClaim) &&
        Objects.equals(defaultScopesClaim, settingsKeyManagerConfiguration.defaultScopesClaim) &&
        Objects.equals(configurations, settingsKeyManagerConfiguration.configurations) &&
        Objects.equals(endpointConfigurations, settingsKeyManagerConfiguration.endpointConfigurations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, displayName, defaultConsumerKeyClaim, defaultScopesClaim, configurations, endpointConfigurations);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SettingsKeyManagerConfigurationDTO {\n");
    
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    defaultConsumerKeyClaim: ").append(toIndentedString(defaultConsumerKeyClaim)).append("\n");
    sb.append("    defaultScopesClaim: ").append(toIndentedString(defaultScopesClaim)).append("\n");
    sb.append("    configurations: ").append(toIndentedString(configurations)).append("\n");
    sb.append("    endpointConfigurations: ").append(toIndentedString(endpointConfigurations)).append("\n");
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

