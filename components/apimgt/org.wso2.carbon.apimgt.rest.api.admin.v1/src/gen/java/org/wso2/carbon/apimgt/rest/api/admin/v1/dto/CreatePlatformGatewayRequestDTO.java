package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.CreatePlatformGatewayRequestPermissionsDTO;
import javax.validation.constraints.*;

/**
 * Request body for creating a platform gateway (name, displayName, vhost as URL, optional properties). Same property name as platform API; type is URL.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;
@ApiModel(description = "Request body for creating a platform gateway (name, displayName, vhost as URL, optional properties). Same property name as platform API; type is URL.")


public class CreatePlatformGatewayRequestDTO   {
  
    private String name = null;
    private String displayName = null;
    private String description = null;
    private URI vhost = null;
    private Map<String, Object> properties = new HashMap<String, Object>();
    private CreatePlatformGatewayRequestPermissionsDTO permissions = null;

  /**
   * URL-friendly gateway identifier (lowercase alphanumeric with hyphens, unique per organization)
   **/
  public CreatePlatformGatewayRequestDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "prod-gateway-01", required = true, value = "URL-friendly gateway identifier (lowercase alphanumeric with hyphens, unique per organization)")
  @JsonProperty("name")
  @NotNull
 @Pattern(regexp="^[a-z0-9-]+$") @Size(min=3,max=64)  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Human-readable gateway name
   **/
  public CreatePlatformGatewayRequestDTO displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(example = "Production Gateway 01", required = true, value = "Human-readable gateway name")
  @JsonProperty("displayName")
  @NotNull
 @Size(min=1,max=128)  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * Optional description
   **/
  public CreatePlatformGatewayRequestDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(value = "Optional description")
  @JsonProperty("description")
 @Size(max=1023)  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Gateway URL (e.g. https://mg.example.com:9443). Same name as platform API; type is URL. Server persists host internally.
   **/
  public CreatePlatformGatewayRequestDTO vhost(URI vhost) {
    this.vhost = vhost;
    return this;
  }

  
  @ApiModelProperty(example = "https://mg.wso2.com", required = true, value = "Gateway URL (e.g. https://mg.example.com:9443). Same name as platform API; type is URL. Server persists host internally.")
  @JsonProperty("vhost")
  @NotNull
  public URI getVhost() {
    return vhost;
  }
  public void setVhost(URI vhost) {
    this.vhost = vhost;
  }

  /**
   * Custom key-value properties for the gateway
   **/
  public CreatePlatformGatewayRequestDTO properties(Map<String, Object> properties) {
    this.properties = properties;
    return this;
  }

  
  @ApiModelProperty(example = "{\"region\":\"us-west\",\"tier\":\"premium\"}", value = "Custom key-value properties for the gateway")
  @JsonProperty("properties")
  public Map<String, Object> getProperties() {
    return properties;
  }
  public void setProperties(Map<String, Object> properties) {
    this.properties = properties;
  }

  /**
   **/
  public CreatePlatformGatewayRequestDTO permissions(CreatePlatformGatewayRequestPermissionsDTO permissions) {
    this.permissions = permissions;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("permissions")
  public CreatePlatformGatewayRequestPermissionsDTO getPermissions() {
    return permissions;
  }
  public void setPermissions(CreatePlatformGatewayRequestPermissionsDTO permissions) {
    this.permissions = permissions;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreatePlatformGatewayRequestDTO createPlatformGatewayRequest = (CreatePlatformGatewayRequestDTO) o;
    return Objects.equals(name, createPlatformGatewayRequest.name) &&
        Objects.equals(displayName, createPlatformGatewayRequest.displayName) &&
        Objects.equals(description, createPlatformGatewayRequest.description) &&
        Objects.equals(vhost, createPlatformGatewayRequest.vhost) &&
        Objects.equals(properties, createPlatformGatewayRequest.properties) &&
        Objects.equals(permissions, createPlatformGatewayRequest.permissions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, displayName, description, vhost, properties, permissions);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreatePlatformGatewayRequestDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    vhost: ").append(toIndentedString(vhost)).append("\n");
    sb.append("    properties: ").append(toIndentedString(properties)).append("\n");
    sb.append("    permissions: ").append(toIndentedString(permissions)).append("\n");
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

