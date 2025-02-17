package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.GatewayConfigurationDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class SettingsGatewayConfigurationDTO   {
  
    private String type = null;
    private String displayName = null;
    private List<GatewayConfigurationDTO> configurations = new ArrayList<GatewayConfigurationDTO>();
    private String defaultHostnameTemplate = null;

  /**
   **/
  public SettingsGatewayConfigurationDTO type(String type) {
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
  public SettingsGatewayConfigurationDTO displayName(String displayName) {
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
  public SettingsGatewayConfigurationDTO configurations(List<GatewayConfigurationDTO> configurations) {
    this.configurations = configurations;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("configurations")
  public List<GatewayConfigurationDTO> getConfigurations() {
    return configurations;
  }
  public void setConfigurations(List<GatewayConfigurationDTO> configurations) {
    this.configurations = configurations;
  }

  /**
   **/
  public SettingsGatewayConfigurationDTO defaultHostnameTemplate(String defaultHostnameTemplate) {
    this.defaultHostnameTemplate = defaultHostnameTemplate;
    return this;
  }

  
  @ApiModelProperty(example = "{apiId}.execute-api.{region}.amazonaws.com", value = "")
  @JsonProperty("defaultHostnameTemplate")
  public String getDefaultHostnameTemplate() {
    return defaultHostnameTemplate;
  }
  public void setDefaultHostnameTemplate(String defaultHostnameTemplate) {
    this.defaultHostnameTemplate = defaultHostnameTemplate;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SettingsGatewayConfigurationDTO settingsGatewayConfiguration = (SettingsGatewayConfigurationDTO) o;
    return Objects.equals(type, settingsGatewayConfiguration.type) &&
        Objects.equals(displayName, settingsGatewayConfiguration.displayName) &&
        Objects.equals(configurations, settingsGatewayConfiguration.configurations) &&
        Objects.equals(defaultHostnameTemplate, settingsGatewayConfiguration.defaultHostnameTemplate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, displayName, configurations, defaultHostnameTemplate);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SettingsGatewayConfigurationDTO {\n");
    
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    configurations: ").append(toIndentedString(configurations)).append("\n");
    sb.append("    defaultHostnameTemplate: ").append(toIndentedString(defaultHostnameTemplate)).append("\n");
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

