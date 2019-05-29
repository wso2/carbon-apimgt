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
  
    private String dcrUrl = null;
    private String authorizeUrl = null;
    private String tokenUrl = null;
    private String revokeTokenUrl = null;
    private String oidcLogoutUrl = null;
    private List<EnvironmentDTO> environment = new ArrayList<>();
    private List<String> scopes = new ArrayList<>();

  /**
   **/
  public SettingsDTO dcrUrl(String dcrUrl) {
    this.dcrUrl = dcrUrl;
    return this;
  }

  
  @ApiModelProperty(example = "https://localhost:9443/client-registration/v0.14/register", value = "")
  @JsonProperty("dcrUrl")
  public String getDcrUrl() {
    return dcrUrl;
  }
  public void setDcrUrl(String dcrUrl) {
    this.dcrUrl = dcrUrl;
  }

  /**
   **/
  public SettingsDTO authorizeUrl(String authorizeUrl) {
    this.authorizeUrl = authorizeUrl;
    return this;
  }

  
  @ApiModelProperty(example = "https://localhost:8243/authorize", value = "")
  @JsonProperty("authorizeUrl")
  public String getAuthorizeUrl() {
    return authorizeUrl;
  }
  public void setAuthorizeUrl(String authorizeUrl) {
    this.authorizeUrl = authorizeUrl;
  }

  /**
   **/
  public SettingsDTO tokenUrl(String tokenUrl) {
    this.tokenUrl = tokenUrl;
    return this;
  }

  
  @ApiModelProperty(example = "https://localhost:8243/token", value = "")
  @JsonProperty("tokenUrl")
  public String getTokenUrl() {
    return tokenUrl;
  }
  public void setTokenUrl(String tokenUrl) {
    this.tokenUrl = tokenUrl;
  }

  /**
   **/
  public SettingsDTO revokeTokenUrl(String revokeTokenUrl) {
    this.revokeTokenUrl = revokeTokenUrl;
    return this;
  }

  
  @ApiModelProperty(example = "https://localhost:8243/revoke", value = "")
  @JsonProperty("revokeTokenUrl")
  public String getRevokeTokenUrl() {
    return revokeTokenUrl;
  }
  public void setRevokeTokenUrl(String revokeTokenUrl) {
    this.revokeTokenUrl = revokeTokenUrl;
  }

  /**
   **/
  public SettingsDTO oidcLogoutUrl(String oidcLogoutUrl) {
    this.oidcLogoutUrl = oidcLogoutUrl;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("oidcLogoutUrl")
  public String getOidcLogoutUrl() {
    return oidcLogoutUrl;
  }
  public void setOidcLogoutUrl(String oidcLogoutUrl) {
    this.oidcLogoutUrl = oidcLogoutUrl;
  }

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


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SettingsDTO settings = (SettingsDTO) o;
    return Objects.equals(dcrUrl, settings.dcrUrl) &&
        Objects.equals(authorizeUrl, settings.authorizeUrl) &&
        Objects.equals(tokenUrl, settings.tokenUrl) &&
        Objects.equals(revokeTokenUrl, settings.revokeTokenUrl) &&
        Objects.equals(oidcLogoutUrl, settings.oidcLogoutUrl) &&
        Objects.equals(environment, settings.environment) &&
        Objects.equals(scopes, settings.scopes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dcrUrl, authorizeUrl, tokenUrl, revokeTokenUrl, oidcLogoutUrl, environment, scopes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SettingsDTO {\n");
    
    sb.append("    dcrUrl: ").append(toIndentedString(dcrUrl)).append("\n");
    sb.append("    authorizeUrl: ").append(toIndentedString(authorizeUrl)).append("\n");
    sb.append("    tokenUrl: ").append(toIndentedString(tokenUrl)).append("\n");
    sb.append("    revokeTokenUrl: ").append(toIndentedString(revokeTokenUrl)).append("\n");
    sb.append("    oidcLogoutUrl: ").append(toIndentedString(oidcLogoutUrl)).append("\n");
    sb.append("    environment: ").append(toIndentedString(environment)).append("\n");
    sb.append("    scopes: ").append(toIndentedString(scopes)).append("\n");
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

