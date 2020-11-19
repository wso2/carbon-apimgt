package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class ApplicationInfoDTO   {
  
    private String applicationId = null;
    private String name = null;
    private String owner = null;
    private String status = null;
    private String groupId = null;

  /**
   **/
  public ApplicationInfoDTO applicationId(String applicationId) {
    this.applicationId = applicationId;
    return this;
  }

  
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "")
  @JsonProperty("applicationId")
  public String getApplicationId() {
    return applicationId;
  }
  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  /**
   **/
  public ApplicationInfoDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "CalculatorApp", value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public ApplicationInfoDTO owner(String owner) {
    this.owner = owner;
    return this;
  }

  
  @ApiModelProperty(example = "admin", value = "")
  @JsonProperty("owner")
  public String getOwner() {
    return owner;
  }
  public void setOwner(String owner) {
    this.owner = owner;
  }

  /**
   **/
  public ApplicationInfoDTO status(String status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(example = "APPROVED", value = "")
  @JsonProperty("status")
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   **/
  public ApplicationInfoDTO groupId(String groupId) {
    this.groupId = groupId;
    return this;
  }

  
  @ApiModelProperty(example = "", value = "")
  @JsonProperty("groupId")
  public String getGroupId() {
    return groupId;
  }
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApplicationInfoDTO applicationInfo = (ApplicationInfoDTO) o;
    return Objects.equals(applicationId, applicationInfo.applicationId) &&
        Objects.equals(name, applicationInfo.name) &&
        Objects.equals(owner, applicationInfo.owner) &&
        Objects.equals(status, applicationInfo.status) &&
        Objects.equals(groupId, applicationInfo.groupId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(applicationId, name, owner, status, groupId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationInfoDTO {\n");
    
    sb.append("    applicationId: ").append(toIndentedString(applicationId)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    owner: ").append(toIndentedString(owner)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    groupId: ").append(toIndentedString(groupId)).append("\n");
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

