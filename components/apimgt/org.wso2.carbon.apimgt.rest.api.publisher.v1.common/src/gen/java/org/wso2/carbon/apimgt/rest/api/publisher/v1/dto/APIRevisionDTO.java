package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionAPIInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionDeploymentDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class APIRevisionDTO   {
  
    private String displayName = null;
    private String id = null;
    private String description = null;
    private java.util.Date createdTime = null;
    private APIRevisionAPIInfoDTO apiInfo = null;
    private List<APIRevisionDeploymentDTO> deploymentInfo = new ArrayList<APIRevisionDeploymentDTO>();

  /**
   **/
  public APIRevisionDTO displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(example = "REVISION 1", value = "")
  @JsonProperty("displayName")
  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   **/
  public APIRevisionDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "c26b2b9b-4632-4ca4-b6f3-521c8863990c", value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public APIRevisionDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "removed a post resource", value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   **/
  public APIRevisionDTO createdTime(java.util.Date createdTime) {
    this.createdTime = createdTime;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("createdTime")
  public java.util.Date getCreatedTime() {
    return createdTime;
  }
  public void setCreatedTime(java.util.Date createdTime) {
    this.createdTime = createdTime;
  }

  /**
   **/
  public APIRevisionDTO apiInfo(APIRevisionAPIInfoDTO apiInfo) {
    this.apiInfo = apiInfo;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("apiInfo")
  public APIRevisionAPIInfoDTO getApiInfo() {
    return apiInfo;
  }
  public void setApiInfo(APIRevisionAPIInfoDTO apiInfo) {
    this.apiInfo = apiInfo;
  }

  /**
   **/
  public APIRevisionDTO deploymentInfo(List<APIRevisionDeploymentDTO> deploymentInfo) {
    this.deploymentInfo = deploymentInfo;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("deploymentInfo")
  public List<APIRevisionDeploymentDTO> getDeploymentInfo() {
    return deploymentInfo;
  }
  public void setDeploymentInfo(List<APIRevisionDeploymentDTO> deploymentInfo) {
    this.deploymentInfo = deploymentInfo;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIRevisionDTO apIRevision = (APIRevisionDTO) o;
    return Objects.equals(displayName, apIRevision.displayName) &&
        Objects.equals(id, apIRevision.id) &&
        Objects.equals(description, apIRevision.description) &&
        Objects.equals(createdTime, apIRevision.createdTime) &&
        Objects.equals(apiInfo, apIRevision.apiInfo) &&
        Objects.equals(deploymentInfo, apIRevision.deploymentInfo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(displayName, id, description, createdTime, apiInfo, deploymentInfo);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIRevisionDTO {\n");
    
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    createdTime: ").append(toIndentedString(createdTime)).append("\n");
    sb.append("    apiInfo: ").append(toIndentedString(apiInfo)).append("\n");
    sb.append("    deploymentInfo: ").append(toIndentedString(deploymentInfo)).append("\n");
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

