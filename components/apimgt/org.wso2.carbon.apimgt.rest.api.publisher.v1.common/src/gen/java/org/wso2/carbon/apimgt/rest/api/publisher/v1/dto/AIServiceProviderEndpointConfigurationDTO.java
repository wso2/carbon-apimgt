package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.AIServiceProviderEndpointAuthenticationConfigurationDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class AIServiceProviderEndpointConfigurationDTO   {
  
    private AIServiceProviderEndpointAuthenticationConfigurationDTO authenticationConfiguration = null;
    private String authHeader = null;
    private String authQueryParameter = null;

  /**
   **/
  public AIServiceProviderEndpointConfigurationDTO authenticationConfiguration(AIServiceProviderEndpointAuthenticationConfigurationDTO authenticationConfiguration) {
    this.authenticationConfiguration = authenticationConfiguration;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("authenticationConfiguration")
  public AIServiceProviderEndpointAuthenticationConfigurationDTO getAuthenticationConfiguration() {
    return authenticationConfiguration;
  }
  public void setAuthenticationConfiguration(AIServiceProviderEndpointAuthenticationConfigurationDTO authenticationConfiguration) {
    this.authenticationConfiguration = authenticationConfiguration;
  }

  /**
   **/
  public AIServiceProviderEndpointConfigurationDTO authHeader(String authHeader) {
    this.authHeader = authHeader;
    return this;
  }

  
  @ApiModelProperty(example = "Authorization", value = "")
  @JsonProperty("authHeader")
  public String getAuthHeader() {
    return authHeader;
  }
  public void setAuthHeader(String authHeader) {
    this.authHeader = authHeader;
  }

  /**
   **/
  public AIServiceProviderEndpointConfigurationDTO authQueryParameter(String authQueryParameter) {
    this.authQueryParameter = authQueryParameter;
    return this;
  }

  
  @ApiModelProperty(example = "ApiKey", value = "")
  @JsonProperty("authQueryParameter")
  public String getAuthQueryParameter() {
    return authQueryParameter;
  }
  public void setAuthQueryParameter(String authQueryParameter) {
    this.authQueryParameter = authQueryParameter;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AIServiceProviderEndpointConfigurationDTO aiServiceProviderEndpointConfiguration = (AIServiceProviderEndpointConfigurationDTO) o;
    return Objects.equals(authenticationConfiguration, aiServiceProviderEndpointConfiguration.authenticationConfiguration) &&
        Objects.equals(authHeader, aiServiceProviderEndpointConfiguration.authHeader) &&
        Objects.equals(authQueryParameter, aiServiceProviderEndpointConfiguration.authQueryParameter);
  }

  @Override
  public int hashCode() {
    return Objects.hash(authenticationConfiguration, authHeader, authQueryParameter);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AIServiceProviderEndpointConfigurationDTO {\n");
    
    sb.append("    authenticationConfiguration: ").append(toIndentedString(authenticationConfiguration)).append("\n");
    sb.append("    authHeader: ").append(toIndentedString(authHeader)).append("\n");
    sb.append("    authQueryParameter: ").append(toIndentedString(authQueryParameter)).append("\n");
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

