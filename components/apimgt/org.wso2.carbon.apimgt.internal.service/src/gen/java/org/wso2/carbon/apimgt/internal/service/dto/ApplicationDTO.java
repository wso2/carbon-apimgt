package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class ApplicationDTO   {
  
    private Integer id = null;
    private String name = null;
    private Integer subId = null;
    private String policy = null;
    private String tokenType = null;

  /**
   **/
  public ApplicationDTO id(Integer id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("id")
  public Integer getId() {
    return id;
  }
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   **/
  public ApplicationDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public ApplicationDTO subId(Integer subId) {
    this.subId = subId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("subId")
  public Integer getSubId() {
    return subId;
  }
  public void setSubId(Integer subId) {
    this.subId = subId;
  }

  /**
   * Application level throtting policy.
   **/
  public ApplicationDTO policy(String policy) {
    this.policy = policy;
    return this;
  }

  
  @ApiModelProperty(value = "Application level throtting policy.")
  @JsonProperty("policy")
  public String getPolicy() {
    return policy;
  }
  public void setPolicy(String policy) {
    this.policy = policy;
  }

  /**
   * type of the token.
   **/
  public ApplicationDTO tokenType(String tokenType) {
    this.tokenType = tokenType;
    return this;
  }

  
  @ApiModelProperty(example = "JWT", value = "type of the token.")
  @JsonProperty("tokenType")
  public String getTokenType() {
    return tokenType;
  }
  public void setTokenType(String tokenType) {
    this.tokenType = tokenType;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApplicationDTO application = (ApplicationDTO) o;
    return Objects.equals(id, application.id) &&
        Objects.equals(name, application.name) &&
        Objects.equals(subId, application.subId) &&
        Objects.equals(policy, application.policy) &&
        Objects.equals(tokenType, application.tokenType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, subId, policy, tokenType);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    subId: ").append(toIndentedString(subId)).append("\n");
    sb.append("    policy: ").append(toIndentedString(policy)).append("\n");
    sb.append("    tokenType: ").append(toIndentedString(tokenType)).append("\n");
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

