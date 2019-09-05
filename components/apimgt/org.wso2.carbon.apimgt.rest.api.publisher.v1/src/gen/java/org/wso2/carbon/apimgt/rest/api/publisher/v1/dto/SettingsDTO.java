package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.EnvironmentDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;



public class SettingsDTO   {
  
    private List<EnvironmentDTO> environment = new ArrayList<>();
    private List<String> scopes = new ArrayList<>();
    private List<String> monetizationProperties = new ArrayList<>();
    private Object securityAuditProperties = null;

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
  public SettingsDTO monetizationProperties(List<String> monetizationProperties) {
    this.monetizationProperties = monetizationProperties;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("MonetizationProperties")
  public List<String> getMonetizationProperties() {
    return monetizationProperties;
  }
  public void setMonetizationProperties(List<String> monetizationProperties) {
    this.monetizationProperties = monetizationProperties;
  }

  /**
   **/
  public SettingsDTO securityAuditProperties(Object securityAuditProperties) {
    this.securityAuditProperties = securityAuditProperties;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("SecurityAuditProperties")
  public Object getSecurityAuditProperties() {
    return securityAuditProperties;
  }
  public void setSecurityAuditProperties(Object securityAuditProperties) {
    this.securityAuditProperties = securityAuditProperties;
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
    return Objects.equals(environment, settings.environment) &&
        Objects.equals(scopes, settings.scopes) &&
        Objects.equals(monetizationProperties, settings.monetizationProperties) &&
        Objects.equals(securityAuditProperties, settings.securityAuditProperties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(environment, scopes, monetizationProperties, securityAuditProperties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SettingsDTO {\n");
    
    sb.append("    environment: ").append(toIndentedString(environment)).append("\n");
    sb.append("    scopes: ").append(toIndentedString(scopes)).append("\n");
    sb.append("    monetizationProperties: ").append(toIndentedString(monetizationProperties)).append("\n");
    sb.append("    securityAuditProperties: ").append(toIndentedString(securityAuditProperties)).append("\n");
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

