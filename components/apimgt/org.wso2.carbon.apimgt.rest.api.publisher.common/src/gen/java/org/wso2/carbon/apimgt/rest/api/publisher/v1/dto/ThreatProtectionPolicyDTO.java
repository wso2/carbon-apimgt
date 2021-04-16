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



public class ThreatProtectionPolicyDTO   {
  
    private String uuid = null;
    private String name = null;
    private String type = null;
    private String policy = null;

  /**
   * Policy ID
   **/
  public ThreatProtectionPolicyDTO uuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

  
  @ApiModelProperty(value = "Policy ID")
  @JsonProperty("uuid")
  public String getUuid() {
    return uuid;
  }
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  /**
   * Name of the policy
   **/
  public ThreatProtectionPolicyDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Name of the policy")
  @JsonProperty("name")
  @NotNull
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Type of the policy
   **/
  public ThreatProtectionPolicyDTO type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Type of the policy")
  @JsonProperty("type")
  @NotNull
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  /**
   * policy as a json string
   **/
  public ThreatProtectionPolicyDTO policy(String policy) {
    this.policy = policy;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "policy as a json string")
  @JsonProperty("policy")
  @NotNull
  public String getPolicy() {
    return policy;
  }
  public void setPolicy(String policy) {
    this.policy = policy;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ThreatProtectionPolicyDTO threatProtectionPolicy = (ThreatProtectionPolicyDTO) o;
    return Objects.equals(uuid, threatProtectionPolicy.uuid) &&
        Objects.equals(name, threatProtectionPolicy.name) &&
        Objects.equals(type, threatProtectionPolicy.type) &&
        Objects.equals(policy, threatProtectionPolicy.policy);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uuid, name, type, policy);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ThreatProtectionPolicyDTO {\n");
    
    sb.append("    uuid: ").append(toIndentedString(uuid)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    policy: ").append(toIndentedString(policy)).append("\n");
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

