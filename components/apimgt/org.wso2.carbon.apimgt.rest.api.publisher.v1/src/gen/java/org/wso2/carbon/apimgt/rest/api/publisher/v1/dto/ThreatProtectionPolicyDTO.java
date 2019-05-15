package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ThreatProtectionPolicyDTO  {
  
  
  
  private String uuid = null;
  
  @NotNull
  private String name = null;
  
  @NotNull
  private String type = null;
  
  @NotNull
  private String policy = null;

  
  /**
   * Policy ID
   **/
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
  @ApiModelProperty(required = true, value = "Name of the policy")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  
  /**
   * Type of the policy
   **/
  @ApiModelProperty(required = true, value = "Type of the policy")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  
  /**
   * policy as a json string
   **/
  @ApiModelProperty(required = true, value = "policy as a json string")
  @JsonProperty("policy")
  public String getPolicy() {
    return policy;
  }
  public void setPolicy(String policy) {
    this.policy = policy;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ThreatProtectionPolicyDTO {\n");
    
    sb.append("  uuid: ").append(uuid).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("  type: ").append(type).append("\n");
    sb.append("  policy: ").append(policy).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
