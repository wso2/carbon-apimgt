package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.KeyManagerConfigurationDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class SettingsKeyManagerConfigurationDTO   {
  
    private String type = null;
    private List<KeyManagerConfigurationDTO> configurations = new ArrayList<>();

  /**
   **/
  public SettingsKeyManagerConfigurationDTO type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(example = "wso2is", value = "")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  /**
   **/
  public SettingsKeyManagerConfigurationDTO configurations(List<KeyManagerConfigurationDTO> configurations) {
    this.configurations = configurations;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("configurations")
  public List<KeyManagerConfigurationDTO> getConfigurations() {
    return configurations;
  }
  public void setConfigurations(List<KeyManagerConfigurationDTO> configurations) {
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
    SettingsKeyManagerConfigurationDTO settingsKeyManagerConfiguration = (SettingsKeyManagerConfigurationDTO) o;
    return Objects.equals(type, settingsKeyManagerConfiguration.type) &&
        Objects.equals(configurations, settingsKeyManagerConfiguration.configurations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, configurations);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SettingsKeyManagerConfigurationDTO {\n");
    
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
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

