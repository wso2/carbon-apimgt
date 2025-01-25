package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

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



public class OrganizationDTO   {
  
    private String organizationId = null;
    private String externalOrganizationId = null;
    private String parentOrganizationId = null;
    private String displayName = null;
    private String description = null;

  /**
   * UUID of the organization. 
   **/
  public OrganizationDTO organizationId(String organizationId) {
    this.organizationId = organizationId;
    return this;
  }

  
  @ApiModelProperty(example = "ece92bdc-e1e6-325c-b6f4-656208a041e9", required = true, value = "UUID of the organization. ")
  @JsonProperty("organizationId")
  @NotNull
  public String getOrganizationId() {
    return organizationId;
  }
  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }

  /**
   * External id of the organization. 
   **/
  public OrganizationDTO externalOrganizationId(String externalOrganizationId) {
    this.externalOrganizationId = externalOrganizationId;
    return this;
  }

  
  @ApiModelProperty(example = "ece92bdc-e1e6-325c-b6f4-656208a041e9", value = "External id of the organization. ")
  @JsonProperty("externalOrganizationId")
  public String getExternalOrganizationId() {
    return externalOrganizationId;
  }
  public void setExternalOrganizationId(String externalOrganizationId) {
    this.externalOrganizationId = externalOrganizationId;
  }

  /**
   * UUID of the parent organization if there is any. 
   **/
  public OrganizationDTO parentOrganizationId(String parentOrganizationId) {
    this.parentOrganizationId = parentOrganizationId;
    return this;
  }

  
  @ApiModelProperty(example = "ece92bdc-e1e6-325c-b6f4-656208a041e9", value = "UUID of the parent organization if there is any. ")
  @JsonProperty("parentOrganizationId")
  public String getParentOrganizationId() {
    return parentOrganizationId;
  }
  public void setParentOrganizationId(String parentOrganizationId) {
    this.parentOrganizationId = parentOrganizationId;
  }

  /**
   **/
  public OrganizationDTO displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(example = "My Organization", value = "")
  @JsonProperty("displayName")
 @Size(min=1,max=255)  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   **/
  public OrganizationDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "My Organization Description", value = "")
  @JsonProperty("description")
 @Size(max=1023)  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OrganizationDTO organization = (OrganizationDTO) o;
    return Objects.equals(organizationId, organization.organizationId) &&
        Objects.equals(externalOrganizationId, organization.externalOrganizationId) &&
        Objects.equals(parentOrganizationId, organization.parentOrganizationId) &&
        Objects.equals(displayName, organization.displayName) &&
        Objects.equals(description, organization.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(organizationId, externalOrganizationId, parentOrganizationId, displayName, description);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OrganizationDTO {\n");
    
    sb.append("    organizationId: ").append(toIndentedString(organizationId)).append("\n");
    sb.append("    externalOrganizationId: ").append(toIndentedString(externalOrganizationId)).append("\n");
    sb.append("    parentOrganizationId: ").append(toIndentedString(parentOrganizationId)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
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

