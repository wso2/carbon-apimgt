package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.PlatformGatewayDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.PlatformGatewayWithTokenAllOfDTO;
import javax.validation.constraints.*;

/**
 * Platform gateway response including the one-time registration token (POST create only).
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Platform gateway response including the one-time registration token (POST create only).")

public class PlatformGatewayWithTokenDTO   {
  
    private String id = null;
    private String organizationId = null;
    private String name = null;
    private String displayName = null;
    private String description = null;
    private Map<String, Object> properties = new HashMap<String, Object>();
    private String vhost = null;
    private Boolean isCritical = null;

    @XmlType(name="FunctionalityTypeEnum")
    @XmlEnum(String.class)
    public enum FunctionalityTypeEnum {
        REGULAR("regular"),
        AI("ai"),
        EVENT("event");
        private String value;

        FunctionalityTypeEnum (String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static FunctionalityTypeEnum fromValue(String v) {
            for (FunctionalityTypeEnum b : FunctionalityTypeEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private FunctionalityTypeEnum functionalityType = null;
    private Boolean isActive = null;
    private java.util.Date createdAt = null;
    private java.util.Date updatedAt = null;
    private String registrationToken = null;

  /**
   * Gateway UUID
   **/
  public PlatformGatewayWithTokenDTO id(String id) {
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
  public PlatformGatewayWithTokenDTO organizationId(String organizationId) {
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
  public PlatformGatewayWithTokenDTO name(String name) {
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
  public PlatformGatewayWithTokenDTO displayName(String displayName) {
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
  public PlatformGatewayWithTokenDTO description(String description) {
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
  public PlatformGatewayWithTokenDTO properties(Map<String, Object> properties) {
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
   **/
  public PlatformGatewayWithTokenDTO vhost(String vhost) {
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
  public PlatformGatewayWithTokenDTO isCritical(Boolean isCritical) {
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
  public PlatformGatewayWithTokenDTO functionalityType(FunctionalityTypeEnum functionalityType) {
    this.functionalityType = functionalityType;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("functionalityType")
  public FunctionalityTypeEnum getFunctionalityType() {
    return functionalityType;
  }
  public void setFunctionalityType(FunctionalityTypeEnum functionalityType) {
    this.functionalityType = functionalityType;
  }

  /**
   * Indicates if the gateway is currently connected to the control plane via WebSocket
   **/
  public PlatformGatewayWithTokenDTO isActive(Boolean isActive) {
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
  public PlatformGatewayWithTokenDTO createdAt(java.util.Date createdAt) {
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
  public PlatformGatewayWithTokenDTO updatedAt(java.util.Date updatedAt) {
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
   * Registration token (returned only once on create). Use as api-key when connecting the gateway to the control plane WebSocket. Store e.g. as GATEWAY_REGISTRATION_TOKEN. 
   **/
  public PlatformGatewayWithTokenDTO registrationToken(String registrationToken) {
    this.registrationToken = registrationToken;
    return this;
  }

  
  @ApiModelProperty(value = "Registration token (returned only once on create). Use as api-key when connecting the gateway to the control plane WebSocket. Store e.g. as GATEWAY_REGISTRATION_TOKEN. ")
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
    PlatformGatewayWithTokenDTO platformGatewayWithToken = (PlatformGatewayWithTokenDTO) o;
    return Objects.equals(id, platformGatewayWithToken.id) &&
        Objects.equals(organizationId, platformGatewayWithToken.organizationId) &&
        Objects.equals(name, platformGatewayWithToken.name) &&
        Objects.equals(displayName, platformGatewayWithToken.displayName) &&
        Objects.equals(description, platformGatewayWithToken.description) &&
        Objects.equals(properties, platformGatewayWithToken.properties) &&
        Objects.equals(vhost, platformGatewayWithToken.vhost) &&
        Objects.equals(isCritical, platformGatewayWithToken.isCritical) &&
        Objects.equals(functionalityType, platformGatewayWithToken.functionalityType) &&
        Objects.equals(isActive, platformGatewayWithToken.isActive) &&
        Objects.equals(createdAt, platformGatewayWithToken.createdAt) &&
        Objects.equals(updatedAt, platformGatewayWithToken.updatedAt) &&
        Objects.equals(registrationToken, platformGatewayWithToken.registrationToken);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, organizationId, name, displayName, description, properties, vhost, isCritical, functionalityType, isActive, createdAt, updatedAt, registrationToken);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PlatformGatewayWithTokenDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    organizationId: ").append(toIndentedString(organizationId)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    properties: ").append(toIndentedString(properties)).append("\n");
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

