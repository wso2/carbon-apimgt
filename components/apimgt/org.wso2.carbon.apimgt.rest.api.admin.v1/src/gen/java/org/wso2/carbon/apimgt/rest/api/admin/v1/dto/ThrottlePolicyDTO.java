package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class ThrottlePolicyDTO   {
  
    private String policyId = null;
    private String policyName = null;
    private String displayName = null;
    private String description = null;
    private Boolean isDeployed = false;

  /**
   * Id of policy
   **/
  public ThrottlePolicyDTO policyId(String policyId) {
    this.policyId = policyId;
    return this;
  }

  
  @ApiModelProperty(example = "0c6439fd-9b16-3c2e-be6e-1086e0b9aa93", value = "Id of policy")
  @JsonProperty("policyId")
  public String getPolicyId() {
    return policyId;
  }
  public void setPolicyId(String policyId) {
    this.policyId = policyId;
  }

  /**
   * Name of policy
   **/
  public ThrottlePolicyDTO policyName(String policyName) {
    this.policyName = policyName;
    return this;
  }

  
  @ApiModelProperty(example = "Policy1", required = true, value = "Name of policy")
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
  public ThrottlePolicyDTO displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(value = "Display name of the policy")
  @JsonProperty("displayName")
  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * Description of the policy
   **/
  public ThrottlePolicyDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(value = "Description of the policy")
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
  public ThrottlePolicyDTO isDeployed(Boolean isDeployed) {
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


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ThrottlePolicyDTO throttlePolicy = (ThrottlePolicyDTO) o;
    return Objects.equals(policyId, throttlePolicy.policyId) &&
        Objects.equals(policyName, throttlePolicy.policyName) &&
        Objects.equals(displayName, throttlePolicy.displayName) &&
        Objects.equals(description, throttlePolicy.description) &&
        Objects.equals(isDeployed, throttlePolicy.isDeployed);
  }

  @Override
  public int hashCode() {
    return Objects.hash(policyId, policyName, displayName, description, isDeployed);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ThrottlePolicyDTO {\n");
    
    sb.append("    policyId: ").append(toIndentedString(policyId)).append("\n");
    sb.append("    policyName: ").append(toIndentedString(policyName)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    isDeployed: ").append(toIndentedString(isDeployed)).append("\n");
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

