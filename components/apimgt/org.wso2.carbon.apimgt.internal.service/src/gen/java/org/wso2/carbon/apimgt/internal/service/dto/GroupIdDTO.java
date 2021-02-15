package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class GroupIdDTO   {
  
    private String groupId = null;
    private Integer applicationId = null;

  /**
   **/
  public GroupIdDTO groupId(String groupId) {
    this.groupId = groupId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("groupId")
  public String getGroupId() {
    return groupId;
  }
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  /**
   **/
  public GroupIdDTO applicationId(Integer applicationId) {
    this.applicationId = applicationId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("applicationId")
  public Integer getApplicationId() {
    return applicationId;
  }
  public void setApplicationId(Integer applicationId) {
    this.applicationId = applicationId;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GroupIdDTO groupId = (GroupIdDTO) o;
    return Objects.equals(groupId, groupId.groupId) &&
        Objects.equals(applicationId, groupId.applicationId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(groupId, applicationId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GroupIdDTO {\n");
    
    sb.append("    groupId: ").append(toIndentedString(groupId)).append("\n");
    sb.append("    applicationId: ").append(toIndentedString(applicationId)).append("\n");
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

