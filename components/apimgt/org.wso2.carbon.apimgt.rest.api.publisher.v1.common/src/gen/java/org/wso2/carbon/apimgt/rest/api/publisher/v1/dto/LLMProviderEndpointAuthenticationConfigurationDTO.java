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



public class LLMProviderEndpointAuthenticationConfigurationDTO   {
  
    private Boolean enabled = false;
    private String type = null;
    private Object parameters = null;

  /**
   * Whether the authentication configuration is enabled or not
   **/
  public LLMProviderEndpointAuthenticationConfigurationDTO enabled(Boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  
  @ApiModelProperty(value = "Whether the authentication configuration is enabled or not")
  @JsonProperty("enabled")
  public Boolean isEnabled() {
    return enabled;
  }
  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Type of the authentication configuration
   **/
  public LLMProviderEndpointAuthenticationConfigurationDTO type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(example = "apiKey", value = "Type of the authentication configuration")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Parameters required for the authentication configuration. The parameters are different based on the type of the authentication configuration. 
   **/
  public LLMProviderEndpointAuthenticationConfigurationDTO parameters(Object parameters) {
    this.parameters = parameters;
    return this;
  }

  
  @ApiModelProperty(example = "{\"headerEnabled\":true,\"headerName\":\"Authorization\"}", value = "Parameters required for the authentication configuration. The parameters are different based on the type of the authentication configuration. ")
      @Valid
  @JsonProperty("parameters")
  public Object getParameters() {
    return parameters;
  }
  public void setParameters(Object parameters) {
    this.parameters = parameters;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LLMProviderEndpointAuthenticationConfigurationDTO llMProviderEndpointAuthenticationConfiguration = (LLMProviderEndpointAuthenticationConfigurationDTO) o;
    return Objects.equals(enabled, llMProviderEndpointAuthenticationConfiguration.enabled) &&
        Objects.equals(type, llMProviderEndpointAuthenticationConfiguration.type) &&
        Objects.equals(parameters, llMProviderEndpointAuthenticationConfiguration.parameters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(enabled, type, parameters);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LLMProviderEndpointAuthenticationConfigurationDTO {\n");
    
    sb.append("    enabled: ").append(toIndentedString(enabled)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    parameters: ").append(toIndentedString(parameters)).append("\n");
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

