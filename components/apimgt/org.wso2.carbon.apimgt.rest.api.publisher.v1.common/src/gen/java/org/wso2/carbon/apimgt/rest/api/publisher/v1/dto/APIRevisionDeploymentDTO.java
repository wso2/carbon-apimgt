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



public class APIRevisionDeploymentDTO   {
  
    private String revisionUuid = null;
    private String name = null;
    private String vhost = null;
    private Boolean displayOnDevportal = null;
    private java.util.Date deployedTime = null;

  /**
   **/
  public APIRevisionDeploymentDTO revisionUuid(String revisionUuid) {
    this.revisionUuid = revisionUuid;
    return this;
  }

  
  @ApiModelProperty(example = "c26b2b9b-4632-4ca4-b6f3-521c8863990c", value = "")
  @JsonProperty("revisionUuid")
  public String getRevisionUuid() {
    return revisionUuid;
  }
  public void setRevisionUuid(String revisionUuid) {
    this.revisionUuid = revisionUuid;
  }

  /**
   **/
  public APIRevisionDeploymentDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "default", value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public APIRevisionDeploymentDTO vhost(String vhost) {
    this.vhost = vhost;
    return this;
  }

  
  @ApiModelProperty(example = "mg.wso2.com", value = "")
  @JsonProperty("vhost")
 @Pattern(regexp="^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$") @Size(min=1,max=255)  public String getVhost() {
    return vhost;
  }
  public void setVhost(String vhost) {
    this.vhost = vhost;
  }

  /**
   **/
  public APIRevisionDeploymentDTO displayOnDevportal(Boolean displayOnDevportal) {
    this.displayOnDevportal = displayOnDevportal;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "")
  @JsonProperty("displayOnDevportal")
  public Boolean isDisplayOnDevportal() {
    return displayOnDevportal;
  }
  public void setDisplayOnDevportal(Boolean displayOnDevportal) {
    this.displayOnDevportal = displayOnDevportal;
  }

  /**
   **/
  public APIRevisionDeploymentDTO deployedTime(java.util.Date deployedTime) {
    this.deployedTime = deployedTime;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("deployedTime")
  public java.util.Date getDeployedTime() {
    return deployedTime;
  }
  public void setDeployedTime(java.util.Date deployedTime) {
    this.deployedTime = deployedTime;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIRevisionDeploymentDTO apIRevisionDeployment = (APIRevisionDeploymentDTO) o;
    return Objects.equals(revisionUuid, apIRevisionDeployment.revisionUuid) &&
        Objects.equals(name, apIRevisionDeployment.name) &&
        Objects.equals(vhost, apIRevisionDeployment.vhost) &&
        Objects.equals(displayOnDevportal, apIRevisionDeployment.displayOnDevportal) &&
        Objects.equals(deployedTime, apIRevisionDeployment.deployedTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(revisionUuid, name, vhost, displayOnDevportal, deployedTime);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIRevisionDeploymentDTO {\n");
    
    sb.append("    revisionUuid: ").append(toIndentedString(revisionUuid)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    vhost: ").append(toIndentedString(vhost)).append("\n");
    sb.append("    displayOnDevportal: ").append(toIndentedString(displayOnDevportal)).append("\n");
    sb.append("    deployedTime: ").append(toIndentedString(deployedTime)).append("\n");
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

