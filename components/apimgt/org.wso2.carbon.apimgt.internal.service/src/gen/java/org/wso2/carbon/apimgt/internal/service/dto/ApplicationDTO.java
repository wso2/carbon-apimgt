package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.wso2.carbon.apimgt.internal.service.dto.GroupIdDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class ApplicationDTO   {
  
    private String uuid = null;
    private Integer id = null;
    private String name = null;
    private String subName = null;
    private String policy = null;
    private String tokenType = null;
    private List<GroupIdDTO> groupIds = new ArrayList<>();
    private Map<String, String> attributes = new HashMap<>();

  /**
   **/
  public ApplicationDTO uuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("uuid")
  public String getUuid() {
    return uuid;
  }
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

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
  public ApplicationDTO subName(String subName) {
    this.subName = subName;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("subName")
  public String getSubName() {
    return subName;
  }
  public void setSubName(String subName) {
    this.subName = subName;
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

  /**
   * group ids associated with the application.
   **/
  public ApplicationDTO groupIds(List<GroupIdDTO> groupIds) {
    this.groupIds = groupIds;
    return this;
  }

  
  @ApiModelProperty(example = "\"wso2\"", value = "group ids associated with the application.")
  @JsonProperty("groupIds")
  public List<GroupIdDTO> getGroupIds() {
    return groupIds;
  }
  public void setGroupIds(List<GroupIdDTO> groupIds) {
    this.groupIds = groupIds;
  }

  /**
   **/
  public ApplicationDTO attributes(Map<String, String> attributes) {
    this.attributes = attributes;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("attributes")
  public Map<String, String> getAttributes() {
    return attributes;
  }
  public void setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
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
    return Objects.equals(uuid, application.uuid) &&
        Objects.equals(id, application.id) &&
        Objects.equals(name, application.name) &&
        Objects.equals(subName, application.subName) &&
        Objects.equals(policy, application.policy) &&
        Objects.equals(tokenType, application.tokenType) &&
        Objects.equals(groupIds, application.groupIds) &&
        Objects.equals(attributes, application.attributes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uuid, id, name, subName, policy, tokenType, groupIds, attributes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationDTO {\n");
    
    sb.append("    uuid: ").append(toIndentedString(uuid)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    subName: ").append(toIndentedString(subName)).append("\n");
    sb.append("    policy: ").append(toIndentedString(policy)).append("\n");
    sb.append("    tokenType: ").append(toIndentedString(tokenType)).append("\n");
    sb.append("    groupIds: ").append(toIndentedString(groupIds)).append("\n");
    sb.append("    attributes: ").append(toIndentedString(attributes)).append("\n");
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

