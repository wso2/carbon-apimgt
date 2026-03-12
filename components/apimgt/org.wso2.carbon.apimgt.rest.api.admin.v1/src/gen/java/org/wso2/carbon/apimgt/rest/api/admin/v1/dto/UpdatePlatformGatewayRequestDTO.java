package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.UpdatePlatformGatewayRequestPermissionsDTO;
import javax.validation.constraints.*;

/**
 * Request body for PUT /gateways/{gatewayId}. Per PUT semantics, send the full resource representation. Name and vhost are immutable (server validates they match the existing gateway); all other fields are applied. 
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Request body for PUT /gateways/{gatewayId}. Per PUT semantics, send the full resource representation. Name and vhost are immutable (server validates they match the existing gateway); all other fields are applied. ")

public class UpdatePlatformGatewayRequestDTO   {
  
    private String name = null;
    private String vhost = null;
    private String displayName = null;
    private String description = null;
    private Map<String, Object> properties = new HashMap<String, Object>();
    private UpdatePlatformGatewayRequestPermissionsDTO permissions = null;

  /**
   * Gateway identifier (immutable; must match existing). Required for PUT full representation.
   **/
  public UpdatePlatformGatewayRequestDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Gateway identifier (immutable; must match existing). Required for PUT full representation.")
  @JsonProperty("name")
  @NotNull
 @Pattern(regexp="^[a-z0-9-]+$") @Size(min=3,max=64)  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Virtual host (immutable; must match existing). Required for PUT full representation.
   **/
  public UpdatePlatformGatewayRequestDTO vhost(String vhost) {
    this.vhost = vhost;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Virtual host (immutable; must match existing). Required for PUT full representation.")
  @JsonProperty("vhost")
  @NotNull
 @Size(min=1,max=255)  public String getVhost() {
    return vhost;
  }
  public void setVhost(String vhost) {
    this.vhost = vhost;
  }

  /**
   * Human-readable gateway name
   **/
  public UpdatePlatformGatewayRequestDTO displayName(String displayName) {
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
  public UpdatePlatformGatewayRequestDTO description(String description) {
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
   * Custom key-value properties for the gateway
   **/
  public UpdatePlatformGatewayRequestDTO properties(Map<String, Object> properties) {
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
  public UpdatePlatformGatewayRequestDTO permissions(UpdatePlatformGatewayRequestPermissionsDTO permissions) {
    this.permissions = permissions;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("permissions")
  public UpdatePlatformGatewayRequestPermissionsDTO getPermissions() {
    return permissions;
  }
  public void setPermissions(UpdatePlatformGatewayRequestPermissionsDTO permissions) {
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
    UpdatePlatformGatewayRequestDTO updatePlatformGatewayRequest = (UpdatePlatformGatewayRequestDTO) o;
    return Objects.equals(name, updatePlatformGatewayRequest.name) &&
        Objects.equals(vhost, updatePlatformGatewayRequest.vhost) &&
        Objects.equals(displayName, updatePlatformGatewayRequest.displayName) &&
        Objects.equals(description, updatePlatformGatewayRequest.description) &&
        Objects.equals(properties, updatePlatformGatewayRequest.properties) &&
        Objects.equals(permissions, updatePlatformGatewayRequest.permissions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, vhost, displayName, description, properties, permissions);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UpdatePlatformGatewayRequestDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    vhost: ").append(toIndentedString(vhost)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
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

