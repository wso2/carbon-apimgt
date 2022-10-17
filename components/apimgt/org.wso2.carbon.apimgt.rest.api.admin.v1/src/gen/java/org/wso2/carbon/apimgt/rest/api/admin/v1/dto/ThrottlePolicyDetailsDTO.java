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



public class ThrottlePolicyDetailsDTO   {
  
    private Integer policyId = null;
    private String uuid = null;
    private String policyName = null;
    private String displayName = null;
    private String description = null;
    private Boolean isDeployed = false;
    private String type = null;

  /**
   * Id of policy
   **/
  public ThrottlePolicyDetailsDTO policyId(Integer policyId) {
    this.policyId = policyId;
    return this;
  }

  
  @ApiModelProperty(example = "3", value = "Id of policy")
  @JsonProperty("policyId")
  public Integer getPolicyId() {
    return policyId;
  }
  public void setPolicyId(Integer policyId) {
    this.policyId = policyId;
  }

  /**
   * UUId of policy
   **/
  public ThrottlePolicyDetailsDTO uuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

  
  @ApiModelProperty(example = "0c6439fd-9b16-3c2e-be6e-1086e0b9aa93", value = "UUId of policy")
  @JsonProperty("uuid")
  public String getUuid() {
    return uuid;
  }
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  /**
   * Name of policy
   **/
  public ThrottlePolicyDetailsDTO policyName(String policyName) {
    this.policyName = policyName;
    return this;
  }

  
  @ApiModelProperty(example = "30PerMin", required = true, value = "Name of policy")
  @JsonProperty("policyName")
  @NotNull
 @Size(min=1,max=60)  public String getPolicyName() {
    return policyName;
  }
  public void setPolicyName(String policyName) {
    this.policyName = policyName;
  }

  /**
   * Display name of the policy
   **/
  public ThrottlePolicyDetailsDTO displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(example = "30PerMin", value = "Display name of the policy")
  @JsonProperty("displayName")
 @Size(max=512)  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * Description of the policy
   **/
  public ThrottlePolicyDetailsDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "Allows 30 request per minute", value = "Description of the policy")
  @JsonProperty("description")
 @Size(max=1024)  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Indicates whether the policy is deployed successfully or not.
   **/
  public ThrottlePolicyDetailsDTO isDeployed(Boolean isDeployed) {
    this.isDeployed = isDeployed;
    return this;
  }

  
  @ApiModelProperty(value = "Indicates whether the policy is deployed successfully or not.")
  @JsonProperty("isDeployed")
  public Boolean isIsDeployed() {
    return isDeployed;
  }
  public void setIsDeployed(Boolean isDeployed) {
    this.isDeployed = isDeployed;
  }

  /**
   * Indicates the type of throttle policy
   **/
  public ThrottlePolicyDetailsDTO type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(value = "Indicates the type of throttle policy")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ThrottlePolicyDetailsDTO throttlePolicyDetails = (ThrottlePolicyDetailsDTO) o;
    return Objects.equals(policyId, throttlePolicyDetails.policyId) &&
        Objects.equals(uuid, throttlePolicyDetails.uuid) &&
        Objects.equals(policyName, throttlePolicyDetails.policyName) &&
        Objects.equals(displayName, throttlePolicyDetails.displayName) &&
        Objects.equals(description, throttlePolicyDetails.description) &&
        Objects.equals(isDeployed, throttlePolicyDetails.isDeployed) &&
        Objects.equals(type, throttlePolicyDetails.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(policyId, uuid, policyName, displayName, description, isDeployed, type);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ThrottlePolicyDetailsDTO {\n");
    
    sb.append("    policyId: ").append(toIndentedString(policyId)).append("\n");
    sb.append("    uuid: ").append(toIndentedString(uuid)).append("\n");
    sb.append("    policyName: ").append(toIndentedString(policyName)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    isDeployed: ").append(toIndentedString(isDeployed)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
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

