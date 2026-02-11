package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

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



public class PlatformGatewayDTO   {
  
    private String id = null;
    private String organizationId = null;
    private String name = null;
    private String displayName = null;
    private String description = null;
    private String vhost = null;
    private Boolean isCritical = null;
    private String functionalityType = null;
    private Boolean isActive = null;
    private java.util.Date createdAt = null;
    private java.util.Date updatedAt = null;
    private String registrationToken = null;

  /**
   * Gateway UUID
   **/
  public PlatformGatewayDTO id(String id) {
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
  public PlatformGatewayDTO organizationId(String organizationId) {
    this.organizationId = organizationId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("organizationId")
  public String getOrganizationId() {
    return organizationId;
  }
  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }

  /**
   **/
  public PlatformGatewayDTO name(String name) {
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
  public PlatformGatewayDTO displayName(String displayName) {
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
  public PlatformGatewayDTO description(String description) {
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
   **/
  public PlatformGatewayDTO vhost(String vhost) {
    this.vhost = vhost;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("vhost")
  public String getVhost() {
    return vhost;
  }
  public void setVhost(String vhost) {
    this.vhost = vhost;
  }

  /**
   **/
  public PlatformGatewayDTO isCritical(Boolean isCritical) {
    this.isCritical = isCritical;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("isCritical")
  public Boolean isIsCritical() {
    return isCritical;
  }
  public void setIsCritical(Boolean isCritical) {
    this.isCritical = isCritical;
  }

  /**
   **/
  public PlatformGatewayDTO functionalityType(String functionalityType) {
    this.functionalityType = functionalityType;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("functionalityType")
  public String getFunctionalityType() {
    return functionalityType;
  }
  public void setFunctionalityType(String functionalityType) {
    this.functionalityType = functionalityType;
  }

  /**
   **/
  public PlatformGatewayDTO isActive(Boolean isActive) {
    this.isActive = isActive;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("isActive")
  public Boolean isIsActive() {
    return isActive;
  }
  public void setIsActive(Boolean isActive) {
    this.isActive = isActive;
  }

  /**
   **/
  public PlatformGatewayDTO createdAt(java.util.Date createdAt) {
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
  public PlatformGatewayDTO updatedAt(java.util.Date updatedAt) {
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
   * Registration token (returned only once on create). Use as api-key when connecting the gateway to the control plane WebSocket. Store e.g. as GATEWAY_CONTROL_PLANE_TOKEN. 
   **/
  public PlatformGatewayDTO registrationToken(String registrationToken) {
    this.registrationToken = registrationToken;
    return this;
  }

  
  @ApiModelProperty(value = "Registration token (returned only once on create). Use as api-key when connecting the gateway to the control plane WebSocket. Store e.g. as GATEWAY_CONTROL_PLANE_TOKEN. ")
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
    PlatformGatewayDTO platformGateway = (PlatformGatewayDTO) o;
    return Objects.equals(id, platformGateway.id) &&
        Objects.equals(organizationId, platformGateway.organizationId) &&
        Objects.equals(name, platformGateway.name) &&
        Objects.equals(displayName, platformGateway.displayName) &&
        Objects.equals(description, platformGateway.description) &&
        Objects.equals(vhost, platformGateway.vhost) &&
        Objects.equals(isCritical, platformGateway.isCritical) &&
        Objects.equals(functionalityType, platformGateway.functionalityType) &&
        Objects.equals(isActive, platformGateway.isActive) &&
        Objects.equals(createdAt, platformGateway.createdAt) &&
        Objects.equals(updatedAt, platformGateway.updatedAt) &&
        Objects.equals(registrationToken, platformGateway.registrationToken);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, organizationId, name, displayName, description, vhost, isCritical, functionalityType, isActive, createdAt, updatedAt, registrationToken);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PlatformGatewayDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    organizationId: ").append(toIndentedString(organizationId)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    vhost: ").append(toIndentedString(vhost)).append("\n");
    sb.append("    isCritical: ").append(toIndentedString(isCritical)).append("\n");
    sb.append("    functionalityType: ").append(toIndentedString(functionalityType)).append("\n");
    sb.append("    isActive: ").append(toIndentedString(isActive)).append("\n");
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

