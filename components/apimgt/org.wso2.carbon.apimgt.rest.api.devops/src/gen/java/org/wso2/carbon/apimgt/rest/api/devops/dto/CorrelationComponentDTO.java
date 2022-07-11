package org.wso2.carbon.apimgt.rest.api.devops.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.devops.dto.CorrelationComponentPropertyDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class CorrelationComponentDTO   {
  
    private String name = null;
    private String enabled = null;
    private List<CorrelationComponentPropertyDTO> properties = new ArrayList<CorrelationComponentPropertyDTO>();

  /**
   **/
  public CorrelationComponentDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "http", value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public CorrelationComponentDTO enabled(String enabled) {
    this.enabled = enabled;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "")
  @JsonProperty("enabled")
  public String getEnabled() {
    return enabled;
  }
  public void setEnabled(String enabled) {
    this.enabled = enabled;
  }

  /**
   **/
  public CorrelationComponentDTO properties(List<CorrelationComponentPropertyDTO> properties) {
    this.properties = properties;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("properties")
  public List<CorrelationComponentPropertyDTO> getProperties() {
    return properties;
  }
  public void setProperties(List<CorrelationComponentPropertyDTO> properties) {
    this.properties = properties;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CorrelationComponentDTO correlationComponent = (CorrelationComponentDTO) o;
    return Objects.equals(name, correlationComponent.name) &&
        Objects.equals(enabled, correlationComponent.enabled) &&
        Objects.equals(properties, correlationComponent.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, enabled, properties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CorrelationComponentDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    enabled: ").append(toIndentedString(enabled)).append("\n");
    sb.append("    properties: ").append(toIndentedString(properties)).append("\n");
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

