package org.wso2.carbon.apimgt.rest.api.publisher.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.EnvironmentEndpointsDTO;

/**
 * EnvironmentDTO
 */
@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-04T10:24:27.156+05:30")
public class EnvironmentDTO   {
  @JsonProperty("name")
  private String name = null;

  @JsonProperty("type")
  private String type = null;

  @JsonProperty("serverUrl")
  private String serverUrl = null;

  @JsonProperty("showInApiConsole")
  private Boolean showInApiConsole = null;

  @JsonProperty("endpoints")
  private EnvironmentEndpointsDTO endpoints = null;

  public EnvironmentDTO name(String name) {
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

  public EnvironmentDTO type(String type) {
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

  public EnvironmentDTO serverUrl(String serverUrl) {
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

  public EnvironmentDTO showInApiConsole(Boolean showInApiConsole) {
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

  public EnvironmentDTO endpoints(EnvironmentEndpointsDTO endpoints) {
    this.endpoints = endpoints;
    return this;
  }

   /**
   * Get endpoints
   * @return endpoints
  **/
  @ApiModelProperty(required = true, value = "")
  public EnvironmentEndpointsDTO getEndpoints() {
    return endpoints;
  }

  public void setEndpoints(EnvironmentEndpointsDTO endpoints) {
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
    EnvironmentDTO environment = (EnvironmentDTO) o;
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
    sb.append("class EnvironmentDTO {\n");
    
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

