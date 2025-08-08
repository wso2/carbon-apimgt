package org.wso2.carbon.apimgt.internal.service.dto;

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



public class GatewayDeploymentStatusAcknowledgmentDTO   {
  
    private String gatewayId = null;
    private String apiId = null;
    private String tenantDomain = null;

    @XmlType(name="DeploymentStatusEnum")
    @XmlEnum(String.class)
    public enum DeploymentStatusEnum {
        SUCCESS("SUCCESS"),
        FAILURE("FAILURE");
        private String value;

        DeploymentStatusEnum (String v) {
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
        public static DeploymentStatusEnum fromValue(String v) {
            for (DeploymentStatusEnum b : DeploymentStatusEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private DeploymentStatusEnum deploymentStatus = null;
    private Long timeStamp = null;

    @XmlType(name="ActionEnum")
    @XmlEnum(String.class)
    public enum ActionEnum {
        DEPLOY("DEPLOY"),
        UNDEPLOY("UNDEPLOY");
        private String value;

        ActionEnum (String v) {
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
        public static ActionEnum fromValue(String v) {
            for (ActionEnum b : ActionEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private ActionEnum action = null;
    private String revisionId = null;
    private Integer errorCode = null;
    private String errorMessage = null;

  /**
   * The unique identifier assigned to the newly registered gateway.
   **/
  public GatewayDeploymentStatusAcknowledgmentDTO gatewayId(String gatewayId) {
    this.gatewayId = gatewayId;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The unique identifier assigned to the newly registered gateway.")
  @JsonProperty("gatewayId")
  @NotNull
  public String getGatewayId() {
    return gatewayId;
  }
  public void setGatewayId(String gatewayId) {
    this.gatewayId = gatewayId;
  }

  /**
   * Unique identifier of the deployed API.
   **/
  public GatewayDeploymentStatusAcknowledgmentDTO apiId(String apiId) {
    this.apiId = apiId;
    return this;
  }

  
  @ApiModelProperty(example = "3c2240f2-33f1-4c6d-936e-74172ea864ab", required = true, value = "Unique identifier of the deployed API.")
  @JsonProperty("apiId")
  @NotNull
  public String getApiId() {
    return apiId;
  }
  public void setApiId(String apiId) {
    this.apiId = apiId;
  }

  /**
   **/
  public GatewayDeploymentStatusAcknowledgmentDTO tenantDomain(String tenantDomain) {
    this.tenantDomain = tenantDomain;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("tenantDomain")
  @NotNull
  public String getTenantDomain() {
    return tenantDomain;
  }
  public void setTenantDomain(String tenantDomain) {
    this.tenantDomain = tenantDomain;
  }

  /**
   * The final status of the deployment action.
   **/
  public GatewayDeploymentStatusAcknowledgmentDTO deploymentStatus(DeploymentStatusEnum deploymentStatus) {
    this.deploymentStatus = deploymentStatus;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The final status of the deployment action.")
  @JsonProperty("deploymentStatus")
  @NotNull
  public DeploymentStatusEnum getDeploymentStatus() {
    return deploymentStatus;
  }
  public void setDeploymentStatus(DeploymentStatusEnum deploymentStatus) {
    this.deploymentStatus = deploymentStatus;
  }

  /**
   * The timestamp when the heartbeat was generated.
   **/
  public GatewayDeploymentStatusAcknowledgmentDTO timeStamp(Long timeStamp) {
    this.timeStamp = timeStamp;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The timestamp when the heartbeat was generated.")
  @JsonProperty("timeStamp")
  @NotNull
  public Long getTimeStamp() {
    return timeStamp;
  }
  public void setTimeStamp(Long timeStamp) {
    this.timeStamp = timeStamp;
  }

  /**
   * The action performed by the gateway.
   **/
  public GatewayDeploymentStatusAcknowledgmentDTO action(ActionEnum action) {
    this.action = action;
    return this;
  }

  
  @ApiModelProperty(value = "The action performed by the gateway.")
  @JsonProperty("action")
  public ActionEnum getAction() {
    return action;
  }
  public void setAction(ActionEnum action) {
    this.action = action;
  }

  /**
   * Unique identifier of the API revision that was deployed or undeployed.
   **/
  public GatewayDeploymentStatusAcknowledgmentDTO revisionId(String revisionId) {
    this.revisionId = revisionId;
    return this;
  }

  
  @ApiModelProperty(example = "f46c7962-cd36-4aa6-b804-8d66ad6a0dc4", value = "Unique identifier of the API revision that was deployed or undeployed.")
  @JsonProperty("revisionId")
  public String getRevisionId() {
    return revisionId;
  }
  public void setRevisionId(String revisionId) {
    this.revisionId = revisionId;
  }

  /**
   * A unique code identifying the error, present only if deploymentStatus is &#39;FAILURE&#39;.
   **/
  public GatewayDeploymentStatusAcknowledgmentDTO errorCode(Integer errorCode) {
    this.errorCode = errorCode;
    return this;
  }

  
  @ApiModelProperty(value = "A unique code identifying the error, present only if deploymentStatus is 'FAILURE'.")
  @JsonProperty("errorCode")
  public Integer getErrorCode() {
    return errorCode;
  }
  public void setErrorCode(Integer errorCode) {
    this.errorCode = errorCode;
  }

  /**
   * A descriptive error message, present only if deploymentStatus is &#39;FAILURE&#39;.
   **/
  public GatewayDeploymentStatusAcknowledgmentDTO errorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
    return this;
  }

  
  @ApiModelProperty(value = "A descriptive error message, present only if deploymentStatus is 'FAILURE'.")
  @JsonProperty("errorMessage")
  public String getErrorMessage() {
    return errorMessage;
  }
  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GatewayDeploymentStatusAcknowledgmentDTO gatewayDeploymentStatusAcknowledgment = (GatewayDeploymentStatusAcknowledgmentDTO) o;
    return Objects.equals(gatewayId, gatewayDeploymentStatusAcknowledgment.gatewayId) &&
        Objects.equals(apiId, gatewayDeploymentStatusAcknowledgment.apiId) &&
        Objects.equals(tenantDomain, gatewayDeploymentStatusAcknowledgment.tenantDomain) &&
        Objects.equals(deploymentStatus, gatewayDeploymentStatusAcknowledgment.deploymentStatus) &&
        Objects.equals(timeStamp, gatewayDeploymentStatusAcknowledgment.timeStamp) &&
        Objects.equals(action, gatewayDeploymentStatusAcknowledgment.action) &&
        Objects.equals(revisionId, gatewayDeploymentStatusAcknowledgment.revisionId) &&
        Objects.equals(errorCode, gatewayDeploymentStatusAcknowledgment.errorCode) &&
        Objects.equals(errorMessage, gatewayDeploymentStatusAcknowledgment.errorMessage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(gatewayId, apiId, tenantDomain, deploymentStatus, timeStamp, action, revisionId, errorCode, errorMessage);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GatewayDeploymentStatusAcknowledgmentDTO {\n");
    
    sb.append("    gatewayId: ").append(toIndentedString(gatewayId)).append("\n");
    sb.append("    apiId: ").append(toIndentedString(apiId)).append("\n");
    sb.append("    tenantDomain: ").append(toIndentedString(tenantDomain)).append("\n");
    sb.append("    deploymentStatus: ").append(toIndentedString(deploymentStatus)).append("\n");
    sb.append("    timeStamp: ").append(toIndentedString(timeStamp)).append("\n");
    sb.append("    action: ").append(toIndentedString(action)).append("\n");
    sb.append("    revisionId: ").append(toIndentedString(revisionId)).append("\n");
    sb.append("    errorCode: ").append(toIndentedString(errorCode)).append("\n");
    sb.append("    errorMessage: ").append(toIndentedString(errorMessage)).append("\n");
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

