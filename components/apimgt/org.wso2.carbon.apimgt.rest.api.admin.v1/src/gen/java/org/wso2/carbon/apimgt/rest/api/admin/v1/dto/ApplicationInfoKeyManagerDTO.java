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



public class ApplicationInfoKeyManagerDTO   {
  
    private String name = null;
    private String uuid = null;
    private String organizationId = null;
    private String owner = null;
    private String organization = null;

  /**
   * The name of the application.
   **/
  public ApplicationInfoKeyManagerDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The name of the application.")
  @JsonProperty("name")
  @NotNull
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * The UUID of the application.
   **/
  public ApplicationInfoKeyManagerDTO uuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The UUID of the application.")
  @JsonProperty("uuid")
  @NotNull
  public String getUuid() {
    return uuid;
  }
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  /**
   * The ID of the organization to which the application belongs.
   **/
  public ApplicationInfoKeyManagerDTO organizationId(String organizationId) {
    this.organizationId = organizationId;
    return this;
  }

  
  @ApiModelProperty(value = "The ID of the organization to which the application belongs.")
  @JsonProperty("organizationId")
  public String getOrganizationId() {
    return organizationId;
  }
  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }

  /**
   * The owner of the application.
   **/
  public ApplicationInfoKeyManagerDTO owner(String owner) {
    this.owner = owner;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The owner of the application.")
  @JsonProperty("owner")
  @NotNull
  public String getOwner() {
    return owner;
  }
  public void setOwner(String owner) {
    this.owner = owner;
  }

  /**
   * The organization of the application.
   **/
  public ApplicationInfoKeyManagerDTO organization(String organization) {
    this.organization = organization;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The organization of the application.")
  @JsonProperty("organization")
  @NotNull
  public String getOrganization() {
    return organization;
  }
  public void setOrganization(String organization) {
    this.organization = organization;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApplicationInfoKeyManagerDTO applicationInfoKeyManager = (ApplicationInfoKeyManagerDTO) o;
    return Objects.equals(name, applicationInfoKeyManager.name) &&
        Objects.equals(uuid, applicationInfoKeyManager.uuid) &&
        Objects.equals(organizationId, applicationInfoKeyManager.organizationId) &&
        Objects.equals(owner, applicationInfoKeyManager.owner) &&
        Objects.equals(organization, applicationInfoKeyManager.organization);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, uuid, organizationId, owner, organization);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationInfoKeyManagerDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    uuid: ").append(toIndentedString(uuid)).append("\n");
    sb.append("    organizationId: ").append(toIndentedString(organizationId)).append("\n");
    sb.append("    owner: ").append(toIndentedString(owner)).append("\n");
    sb.append("    organization: ").append(toIndentedString(organization)).append("\n");
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

