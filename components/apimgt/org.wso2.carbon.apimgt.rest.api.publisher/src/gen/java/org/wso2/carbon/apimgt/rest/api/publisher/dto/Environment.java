package org.wso2.carbon.apimgt.rest.api.publisher.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.EnvironmentEndpoints;

/**
 * Environment
 */
@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-10-26T15:09:45.077+05:30")
public class Environment   {
  private String name = null;

  private String type = null;

  private String serverUrl = null;

  private Boolean showInApiConsole = null;

  private EnvironmentEndpoints endpoints = null;

  public Environment name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Get name
   * @return name
  **/
  @ApiModelProperty(example = "Production and Sandbox", required = true, value = "")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Environment type(String type) {
    this.type = type;
    return this;
  }

   /**
   * Get type
   * @return type
  **/
  @ApiModelProperty(example = "hybrid", required = true, value = "")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Environment serverUrl(String serverUrl) {
    this.serverUrl = serverUrl;
    return this;
  }

   /**
   * Get serverUrl
   * @return serverUrl
  **/
  @ApiModelProperty(example = "https://localhost:9443//services/", required = true, value = "")
  public String getServerUrl() {
    return serverUrl;
  }

  public void setServerUrl(String serverUrl) {
    this.serverUrl = serverUrl;
  }

  public Environment showInApiConsole(Boolean showInApiConsole) {
    this.showInApiConsole = showInApiConsole;
    return this;
  }

   /**
   * Get showInApiConsole
   * @return showInApiConsole
  **/
  @ApiModelProperty(example = "true", required = true, value = "")
  public Boolean getShowInApiConsole() {
    return showInApiConsole;
  }

  public void setShowInApiConsole(Boolean showInApiConsole) {
    this.showInApiConsole = showInApiConsole;
  }

  public Environment endpoints(EnvironmentEndpoints endpoints) {
    this.endpoints = endpoints;
    return this;
  }

   /**
   * Get endpoints
   * @return endpoints
  **/
  @ApiModelProperty(required = true, value = "")
  public EnvironmentEndpoints getEndpoints() {
    return endpoints;
  }

  public void setEndpoints(EnvironmentEndpoints endpoints) {
    this.endpoints = endpoints;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Environment environment = (Environment) o;
    return Objects.equals(this.name, environment.name) &&
        Objects.equals(this.type, environment.type) &&
        Objects.equals(this.serverUrl, environment.serverUrl) &&
        Objects.equals(this.showInApiConsole, environment.showInApiConsole) &&
        Objects.equals(this.endpoints, environment.endpoints);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type, serverUrl, showInApiConsole, endpoints);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Environment {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    serverUrl: ").append(toIndentedString(serverUrl)).append("\n");
    sb.append("    showInApiConsole: ").append(toIndentedString(showInApiConsole)).append("\n");
    sb.append("    endpoints: ").append(toIndentedString(endpoints)).append("\n");
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

