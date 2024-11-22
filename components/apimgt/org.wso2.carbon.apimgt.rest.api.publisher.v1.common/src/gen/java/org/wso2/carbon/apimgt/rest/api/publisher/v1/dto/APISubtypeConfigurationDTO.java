package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;

/**
 * Configuration settings for the API subtype.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Configuration settings for the API subtype.")

public class APISubtypeConfigurationDTO   {
  
    private String subtype = "DEFAULT";
    private Object _configuration = null;

  /**
   * The designated name of the API subtype.
   **/
  public APISubtypeConfigurationDTO subtype(String subtype) {
    this.subtype = subtype;
    return this;
  }

  
  @ApiModelProperty(example = "AIAPI", value = "The designated name of the API subtype.")
  @JsonProperty("subtype")
  public String getSubtype() {
    return subtype;
  }
  public void setSubtype(String subtype) {
    this.subtype = subtype;
  }

  /**
   * Specific configuration properties related to the API subtype.
   **/
  public APISubtypeConfigurationDTO _configuration(Object _configuration) {
    this._configuration = _configuration;
    return this;
  }

  
  @ApiModelProperty(value = "Specific configuration properties related to the API subtype.")
      @Valid
  @JsonProperty("configuration")
  public Object getConfiguration() {
    return _configuration;
  }
  public void setConfiguration(Object _configuration) {
    this._configuration = _configuration;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APISubtypeConfigurationDTO apISubtypeConfiguration = (APISubtypeConfigurationDTO) o;
    return Objects.equals(subtype, apISubtypeConfiguration.subtype) &&
        Objects.equals(_configuration, apISubtypeConfiguration._configuration);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subtype, _configuration);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APISubtypeConfigurationDTO {\n");
    
    sb.append("    subtype: ").append(toIndentedString(subtype)).append("\n");
    sb.append("    _configuration: ").append(toIndentedString(_configuration)).append("\n");
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

