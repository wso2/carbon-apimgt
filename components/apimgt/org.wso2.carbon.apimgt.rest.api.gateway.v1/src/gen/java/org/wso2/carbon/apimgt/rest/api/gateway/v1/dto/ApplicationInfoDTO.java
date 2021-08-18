package org.wso2.carbon.apimgt.rest.api.gateway.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.dto.ApplicationKeyMappingDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class ApplicationInfoDTO   {
  
    private String uuid = null;
    private Integer id = null;
    private String name = null;
    private String subName = null;
    private String policy = null;
    private String tokenType = null;
    private Map<String, String> attributes = new HashMap<>();
    private List<ApplicationKeyMappingDTO> keys = new ArrayList<>();

  /**
   **/
  public ApplicationInfoDTO uuid(String uuid) {
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
  public ApplicationInfoDTO id(Integer id) {
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
  public ApplicationInfoDTO name(String name) {
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
  public ApplicationInfoDTO subName(String subName) {
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
  public ApplicationInfoDTO policy(String policy) {
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
  public ApplicationInfoDTO tokenType(String tokenType) {
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
   **/
  public ApplicationInfoDTO attributes(Map<String, String> attributes) {
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

  /**
   **/
  public ApplicationInfoDTO keys(List<ApplicationKeyMappingDTO> keys) {
    this.keys = keys;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("keys")
  public List<ApplicationKeyMappingDTO> getKeys() {
    return keys;
  }
  public void setKeys(List<ApplicationKeyMappingDTO> keys) {
    this.keys = keys;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApplicationInfoDTO applicationInfo = (ApplicationInfoDTO) o;
    return Objects.equals(uuid, applicationInfo.uuid) &&
        Objects.equals(id, applicationInfo.id) &&
        Objects.equals(name, applicationInfo.name) &&
        Objects.equals(subName, applicationInfo.subName) &&
        Objects.equals(policy, applicationInfo.policy) &&
        Objects.equals(tokenType, applicationInfo.tokenType) &&
        Objects.equals(attributes, applicationInfo.attributes) &&
        Objects.equals(keys, applicationInfo.keys);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uuid, id, name, subName, policy, tokenType, attributes, keys);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationInfoDTO {\n");
    
    sb.append("    uuid: ").append(toIndentedString(uuid)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    subName: ").append(toIndentedString(subName)).append("\n");
    sb.append("    policy: ").append(toIndentedString(policy)).append("\n");
    sb.append("    tokenType: ").append(toIndentedString(tokenType)).append("\n");
    sb.append("    attributes: ").append(toIndentedString(attributes)).append("\n");
    sb.append("    keys: ").append(toIndentedString(keys)).append("\n");
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

