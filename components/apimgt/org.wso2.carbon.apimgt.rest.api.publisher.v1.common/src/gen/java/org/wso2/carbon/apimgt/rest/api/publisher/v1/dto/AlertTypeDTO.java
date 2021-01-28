package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class AlertTypeDTO   {
  
    private Integer id = null;
    private String name = null;
    private Boolean requireConfiguration = null;

  /**
   * The alert Id
   **/
  public AlertTypeDTO id(Integer id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "The alert Id")
  @JsonProperty("id")
  public Integer getId() {
    return id;
  }
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   * The name of the alert.
   **/
  public AlertTypeDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "AbnormalRequestTime", value = "The name of the alert.")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Whether the alert type require additional configurations.
   **/
  public AlertTypeDTO requireConfiguration(Boolean requireConfiguration) {
    this.requireConfiguration = requireConfiguration;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "Whether the alert type require additional configurations.")
  @JsonProperty("requireConfiguration")
  public Boolean isRequireConfiguration() {
    return requireConfiguration;
  }
  public void setRequireConfiguration(Boolean requireConfiguration) {
    this.requireConfiguration = requireConfiguration;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AlertTypeDTO alertType = (AlertTypeDTO) o;
    return Objects.equals(id, alertType.id) &&
        Objects.equals(name, alertType.name) &&
        Objects.equals(requireConfiguration, alertType.requireConfiguration);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, requireConfiguration);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AlertTypeDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    requireConfiguration: ").append(toIndentedString(requireConfiguration)).append("\n");
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

