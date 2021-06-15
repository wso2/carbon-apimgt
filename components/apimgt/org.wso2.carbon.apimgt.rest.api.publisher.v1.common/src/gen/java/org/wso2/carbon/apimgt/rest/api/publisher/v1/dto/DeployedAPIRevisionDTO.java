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



public class DeployedAPIRevisionDTO   {
  
    private String revisionID = null;
    private String name = null;
    private String vhost = null;
    private java.util.Date deployedTime = null;

  /**
   **/
  public DeployedAPIRevisionDTO revisionID(String revisionID) {
    this.revisionID = revisionID;
    return this;
  }

  
  @ApiModelProperty(example = "c26b2b9b-4632-4ca4-b6f3-521c8863990c", value = "")
  @JsonProperty("revisionID")
 @Size(min=0,max=255)  public String getRevisionID() {
    return revisionID;
  }
  public void setRevisionID(String revisionID) {
    this.revisionID = revisionID;
  }

  /**
   **/
  public DeployedAPIRevisionDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "default", value = "")
  @JsonProperty("name")
 @Size(min=1,max=255)  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public DeployedAPIRevisionDTO vhost(String vhost) {
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
  public DeployedAPIRevisionDTO deployedTime(java.util.Date deployedTime) {
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
    DeployedAPIRevisionDTO deployedAPIRevision = (DeployedAPIRevisionDTO) o;
    return Objects.equals(revisionID, deployedAPIRevision.revisionID) &&
        Objects.equals(name, deployedAPIRevision.name) &&
        Objects.equals(vhost, deployedAPIRevision.vhost) &&
        Objects.equals(deployedTime, deployedAPIRevision.deployedTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(revisionID, name, vhost, deployedTime);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DeployedAPIRevisionDTO {\n");
    
    sb.append("    revisionID: ").append(toIndentedString(revisionID)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    vhost: ").append(toIndentedString(vhost)).append("\n");
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

