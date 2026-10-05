package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.GatewayResponseWithTokenAllOfDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.PlatformGatewayResponseDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.PlatformGatewayResponsePermissionsDTO;
import javax.validation.constraints.*;

/**
 * Platform gateway response including the one-time registration token (POST create or regenerate-token).
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;
@ApiModel(description = "Platform gateway response including the one-time registration token (POST create or regenerate-token).")


public class GatewayResponseWithTokenDTO   {
  
    private String id = null;
    private String name = null;
    private String displayName = null;
    private String description = null;
    private Map<String, Object> properties = new HashMap<String, Object>();
    private URI vhost = null;
    private Boolean isActive = null;
    private PlatformGatewayResponsePermissionsDTO permissions = null;
    private java.util.Date createdAt = null;
    private java.util.Date updatedAt = null;
    private String registrationToken = null;

  /**
   * Gateway UUID
   **/
  public GatewayResponseWithTokenDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(value = "Gateway UUID")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public GatewayResponseWithTokenDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public GatewayResponseWithTokenDTO displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("displayName")
  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   **/
  public GatewayResponseWithTokenDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Custom key-value properties
   **/
  public GatewayResponseWithTokenDTO properties(Map<String, Object> properties) {
    this.properties = properties;
    return this;
  }

  
  @ApiModelProperty(value = "Custom key-value properties")
  @JsonProperty("properties")
  public Map<String, Object> getProperties() {
    return properties;
  }
  public void setProperties(Map<String, Object> properties) {
    this.properties = properties;
  }

  /**
   * Gateway URL (e.g. https://host or https://host:9443). Same name as platform API; type is URL.
   **/
  public GatewayResponseWithTokenDTO vhost(URI vhost) {
    this.vhost = vhost;
    return this;
  }

  
  @ApiModelProperty(value = "Gateway URL (e.g. https://host or https://host:9443). Same name as platform API; type is URL.")
  @JsonProperty("vhost")
  public URI getVhost() {
    return vhost;
  }
  public void setVhost(URI vhost) {
    this.vhost = vhost;
  }

  /**
   * Indicates if the gateway is currently connected to the control plane via WebSocket
   **/
  public GatewayResponseWithTokenDTO isActive(Boolean isActive) {
    this.isActive = isActive;
    return this;
  }

  
  @ApiModelProperty(value = "Indicates if the gateway is currently connected to the control plane via WebSocket")
  @JsonProperty("isActive")
  public Boolean isIsActive() {
    return isActive;
  }
  public void setIsActive(Boolean isActive) {
    this.isActive = isActive;
  }

  /**
   **/
  public GatewayResponseWithTokenDTO permissions(PlatformGatewayResponsePermissionsDTO permissions) {
    this.permissions = permissions;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("permissions")
  public PlatformGatewayResponsePermissionsDTO getPermissions() {
    return permissions;
  }
  public void setPermissions(PlatformGatewayResponsePermissionsDTO permissions) {
    this.permissions = permissions;
  }

  /**
   **/
  public GatewayResponseWithTokenDTO createdAt(java.util.Date createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("createdAt")
  public java.util.Date getCreatedAt() {
    return createdAt;
  }
  public void setCreatedAt(java.util.Date createdAt) {
    this.createdAt = createdAt;
  }

  /**
   **/
  public GatewayResponseWithTokenDTO updatedAt(java.util.Date updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("updatedAt")
  public java.util.Date getUpdatedAt() {
    return updatedAt;
  }
  public void setUpdatedAt(java.util.Date updatedAt) {
    this.updatedAt = updatedAt;
  }

  /**
   * Registration token (returned only once on create or regenerate). Use as api-key when connecting the gateway to the control plane WebSocket. Store e.g. as GATEWAY_REGISTRATION_TOKEN. 
   **/
  public GatewayResponseWithTokenDTO registrationToken(String registrationToken) {
    this.registrationToken = registrationToken;
    return this;
  }

  
  @ApiModelProperty(value = "Registration token (returned only once on create or regenerate). Use as api-key when connecting the gateway to the control plane WebSocket. Store e.g. as GATEWAY_REGISTRATION_TOKEN. ")
  @JsonProperty("registrationToken")
  public String getRegistrationToken() {
    return registrationToken;
  }
  public void setRegistrationToken(String registrationToken) {
    this.registrationToken = registrationToken;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GatewayResponseWithTokenDTO gatewayResponseWithToken = (GatewayResponseWithTokenDTO) o;
    return Objects.equals(id, gatewayResponseWithToken.id) &&
        Objects.equals(name, gatewayResponseWithToken.name) &&
        Objects.equals(displayName, gatewayResponseWithToken.displayName) &&
        Objects.equals(description, gatewayResponseWithToken.description) &&
        Objects.equals(properties, gatewayResponseWithToken.properties) &&
        Objects.equals(vhost, gatewayResponseWithToken.vhost) &&
        Objects.equals(isActive, gatewayResponseWithToken.isActive) &&
        Objects.equals(permissions, gatewayResponseWithToken.permissions) &&
        Objects.equals(createdAt, gatewayResponseWithToken.createdAt) &&
        Objects.equals(updatedAt, gatewayResponseWithToken.updatedAt) &&
        Objects.equals(registrationToken, gatewayResponseWithToken.registrationToken);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, displayName, description, properties, vhost, isActive, permissions, createdAt, updatedAt, registrationToken);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GatewayResponseWithTokenDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    properties: ").append(toIndentedString(properties)).append("\n");
    sb.append("    vhost: ").append(toIndentedString(vhost)).append("\n");
    sb.append("    isActive: ").append(toIndentedString(isActive)).append("\n");
    sb.append("    permissions: ").append(toIndentedString(permissions)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
    sb.append("    registrationToken: ").append(toIndentedString(registrationToken)).append("\n");
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

