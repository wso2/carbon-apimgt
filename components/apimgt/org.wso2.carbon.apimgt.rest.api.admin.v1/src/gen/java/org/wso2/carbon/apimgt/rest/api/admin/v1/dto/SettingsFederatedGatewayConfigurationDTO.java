package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ConfigurationDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class SettingsFederatedGatewayConfigurationDTO   {
  
    private String type = null;
    private String displayName = null;
    private List<ConfigurationDTO> configurations = new ArrayList<ConfigurationDTO>();

  /**
   **/
  public SettingsFederatedGatewayConfigurationDTO type(String type) {
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
  public SettingsFederatedGatewayConfigurationDTO displayName(String displayName) {
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
  public SettingsFederatedGatewayConfigurationDTO configurations(List<ConfigurationDTO> configurations) {
    this.configurations = configurations;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("configurations")
  public List<ConfigurationDTO> getConfigurations() {
    return configurations;
  }
  public void setConfigurations(List<ConfigurationDTO> configurations) {
    this.configurations = configurations;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SettingsFederatedGatewayConfigurationDTO settingsFederatedGatewayConfiguration = (SettingsFederatedGatewayConfigurationDTO) o;
    return Objects.equals(type, settingsFederatedGatewayConfiguration.type) &&
        Objects.equals(displayName, settingsFederatedGatewayConfiguration.displayName) &&
        Objects.equals(configurations, settingsFederatedGatewayConfiguration.configurations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, displayName, configurations);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SettingsFederatedGatewayConfigurationDTO {\n");
    
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    configurations: ").append(toIndentedString(configurations)).append("\n");
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

