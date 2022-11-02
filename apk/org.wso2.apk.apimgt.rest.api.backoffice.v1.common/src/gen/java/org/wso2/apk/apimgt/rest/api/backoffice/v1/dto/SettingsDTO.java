package org.wso2.apk.apimgt.rest.api.backoffice.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.EnvironmentDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.MonetizationAttributeDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.apk.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class SettingsDTO   {
  
    private String devportalUrl = null;
    private List<EnvironmentDTO> environment = new ArrayList<EnvironmentDTO>();
    private List<String> scopes = new ArrayList<String>();
    private List<MonetizationAttributeDTO> monetizationAttributes = new ArrayList<MonetizationAttributeDTO>();
    private Boolean docVisibilityEnabled = null;
    private String authorizationHeader = null;

  /**
   * The Developer Portal URL
   **/
  public SettingsDTO devportalUrl(String devportalUrl) {
    this.devportalUrl = devportalUrl;
    return this;
  }

  
  @ApiModelProperty(example = "https://localhost:9443/devportal", value = "The Developer Portal URL")
  @JsonProperty("devportalUrl")
  public String getDevportalUrl() {
    return devportalUrl;
  }
  public void setDevportalUrl(String devportalUrl) {
    this.devportalUrl = devportalUrl;
  }

  /**
   **/
  public SettingsDTO environment(List<EnvironmentDTO> environment) {
    this.environment = environment;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
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

  
  @ApiModelProperty(example = "[\"apim:api_create\",\"apim:api_manage\",\"apim:api_publish\"]", value = "")
  @JsonProperty("scopes")
  public List<String> getScopes() {
    return scopes;
  }
  public void setScopes(List<String> scopes) {
    this.scopes = scopes;
  }

  /**
   **/
  public SettingsDTO monetizationAttributes(List<MonetizationAttributeDTO> monetizationAttributes) {
    this.monetizationAttributes = monetizationAttributes;
    return this;
  }

  
  @ApiModelProperty(example = "[]", value = "")
      @Valid
  @JsonProperty("monetizationAttributes")
  public List<MonetizationAttributeDTO> getMonetizationAttributes() {
    return monetizationAttributes;
  }
  public void setMonetizationAttributes(List<MonetizationAttributeDTO> monetizationAttributes) {
    this.monetizationAttributes = monetizationAttributes;
  }

  /**
   * Is Document Visibility configuration enabled 
   **/
  public SettingsDTO docVisibilityEnabled(Boolean docVisibilityEnabled) {
    this.docVisibilityEnabled = docVisibilityEnabled;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "Is Document Visibility configuration enabled ")
  @JsonProperty("docVisibilityEnabled")
  public Boolean isDocVisibilityEnabled() {
    return docVisibilityEnabled;
  }
  public void setDocVisibilityEnabled(Boolean docVisibilityEnabled) {
    this.docVisibilityEnabled = docVisibilityEnabled;
  }

  /**
   * Authorization Header
   **/
  public SettingsDTO authorizationHeader(String authorizationHeader) {
    this.authorizationHeader = authorizationHeader;
    return this;
  }

  
  @ApiModelProperty(example = "authorization", value = "Authorization Header")
  @JsonProperty("authorizationHeader")
  public String getAuthorizationHeader() {
    return authorizationHeader;
  }
  public void setAuthorizationHeader(String authorizationHeader) {
    this.authorizationHeader = authorizationHeader;
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
    return Objects.equals(devportalUrl, settings.devportalUrl) &&
        Objects.equals(environment, settings.environment) &&
        Objects.equals(scopes, settings.scopes) &&
        Objects.equals(monetizationAttributes, settings.monetizationAttributes) &&
        Objects.equals(docVisibilityEnabled, settings.docVisibilityEnabled) &&
        Objects.equals(authorizationHeader, settings.authorizationHeader);
  }

  @Override
  public int hashCode() {
    return Objects.hash(devportalUrl, environment, scopes, monetizationAttributes, docVisibilityEnabled, authorizationHeader);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SettingsDTO {\n");
    
    sb.append("    devportalUrl: ").append(toIndentedString(devportalUrl)).append("\n");
    sb.append("    environment: ").append(toIndentedString(environment)).append("\n");
    sb.append("    scopes: ").append(toIndentedString(scopes)).append("\n");
    sb.append("    monetizationAttributes: ").append(toIndentedString(monetizationAttributes)).append("\n");
    sb.append("    docVisibilityEnabled: ").append(toIndentedString(docVisibilityEnabled)).append("\n");
    sb.append("    authorizationHeader: ").append(toIndentedString(authorizationHeader)).append("\n");
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

