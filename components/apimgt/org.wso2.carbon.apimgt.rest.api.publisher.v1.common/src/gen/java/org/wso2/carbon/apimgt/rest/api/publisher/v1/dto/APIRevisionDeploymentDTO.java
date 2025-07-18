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

    @XmlType(name="StatusEnum")
    @XmlEnum(String.class)
    public enum StatusEnum {
        CREATED("CREATED"),
        APPROVED("APPROVED"),
        REJECTED("REJECTED");
        private String value;

        StatusEnum (String v) {
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
        public static StatusEnum fromValue(String v) {
            for (StatusEnum b : StatusEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private StatusEnum status = StatusEnum.CREATED;
    private String vhost = null;
    private Boolean displayOnDevportal = true;
    private java.util.Date deployedTime = null;
    private java.util.Date successDeployedTime = null;
    private Integer liveGatewayCount = null;
    private Integer deployedGatewayCount = null;
    private Integer failedGatewayCount = null;

  /**
   **/
  public APIRevisionDeploymentDTO revisionUuid(String revisionUuid) {
    this.revisionUuid = revisionUuid;
    return this;
  }

  
  @ApiModelProperty(example = "c26b2b9b-4632-4ca4-b6f3-521c8863990c", value = "")
  @JsonProperty("revisionUuid")
 @Size(min=0,max=255)  public String getRevisionUuid() {
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

  
  @ApiModelProperty(example = "Default", value = "")
  @JsonProperty("name")
 @Size(min=1,max=255)  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public APIRevisionDeploymentDTO status(StatusEnum status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(example = "CREATED", value = "")
  @JsonProperty("status")
  public StatusEnum getStatus() {
    return status;
  }
  public void setStatus(StatusEnum status) {
    this.status = status;
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

  /**
   **/
  public APIRevisionDeploymentDTO successDeployedTime(java.util.Date successDeployedTime) {
    this.successDeployedTime = successDeployedTime;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("successDeployedTime")
  public java.util.Date getSuccessDeployedTime() {
    return successDeployedTime;
  }
  public void setSuccessDeployedTime(java.util.Date successDeployedTime) {
    this.successDeployedTime = successDeployedTime;
  }

  /**
   * The number of gateways that are currenty live in the gateway environment 
   **/
  public APIRevisionDeploymentDTO liveGatewayCount(Integer liveGatewayCount) {
    this.liveGatewayCount = liveGatewayCount;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "The number of gateways that are currenty live in the gateway environment ")
  @JsonProperty("liveGatewayCount")
  public Integer getLiveGatewayCount() {
    return liveGatewayCount;
  }
  public void setLiveGatewayCount(Integer liveGatewayCount) {
    this.liveGatewayCount = liveGatewayCount;
  }

  /**
   * The number of gateways in which the API revision is deployed in the gateway environment 
   **/
  public APIRevisionDeploymentDTO deployedGatewayCount(Integer deployedGatewayCount) {
    this.deployedGatewayCount = deployedGatewayCount;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "The number of gateways in which the API revision is deployed in the gateway environment ")
  @JsonProperty("deployedGatewayCount")
  public Integer getDeployedGatewayCount() {
    return deployedGatewayCount;
  }
  public void setDeployedGatewayCount(Integer deployedGatewayCount) {
    this.deployedGatewayCount = deployedGatewayCount;
  }

  /**
   * The number of gateways in which the API revision is deployed in the gateway environment 
   **/
  public APIRevisionDeploymentDTO failedGatewayCount(Integer failedGatewayCount) {
    this.failedGatewayCount = failedGatewayCount;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "The number of gateways in which the API revision is deployed in the gateway environment ")
  @JsonProperty("failedGatewayCount")
  public Integer getFailedGatewayCount() {
    return failedGatewayCount;
  }
  public void setFailedGatewayCount(Integer failedGatewayCount) {
    this.failedGatewayCount = failedGatewayCount;
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
        Objects.equals(status, apIRevisionDeployment.status) &&
        Objects.equals(vhost, apIRevisionDeployment.vhost) &&
        Objects.equals(displayOnDevportal, apIRevisionDeployment.displayOnDevportal) &&
        Objects.equals(deployedTime, apIRevisionDeployment.deployedTime) &&
        Objects.equals(successDeployedTime, apIRevisionDeployment.successDeployedTime) &&
        Objects.equals(liveGatewayCount, apIRevisionDeployment.liveGatewayCount) &&
        Objects.equals(deployedGatewayCount, apIRevisionDeployment.deployedGatewayCount) &&
        Objects.equals(failedGatewayCount, apIRevisionDeployment.failedGatewayCount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(revisionUuid, name, status, vhost, displayOnDevportal, deployedTime, successDeployedTime, liveGatewayCount, deployedGatewayCount, failedGatewayCount);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIRevisionDeploymentDTO {\n");
    
    sb.append("    revisionUuid: ").append(toIndentedString(revisionUuid)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    vhost: ").append(toIndentedString(vhost)).append("\n");
    sb.append("    displayOnDevportal: ").append(toIndentedString(displayOnDevportal)).append("\n");
    sb.append("    deployedTime: ").append(toIndentedString(deployedTime)).append("\n");
    sb.append("    successDeployedTime: ").append(toIndentedString(successDeployedTime)).append("\n");
    sb.append("    liveGatewayCount: ").append(toIndentedString(liveGatewayCount)).append("\n");
    sb.append("    deployedGatewayCount: ").append(toIndentedString(deployedGatewayCount)).append("\n");
    sb.append("    failedGatewayCount: ").append(toIndentedString(failedGatewayCount)).append("\n");
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

