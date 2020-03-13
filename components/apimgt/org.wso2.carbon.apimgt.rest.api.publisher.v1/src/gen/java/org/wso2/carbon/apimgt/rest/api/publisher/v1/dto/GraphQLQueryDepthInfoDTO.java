package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class GraphQLQueryDepthInfoDTO   {
  
    private String uuid = null;
    private String role = null;
    private Integer depthValue = null;

  /**
   **/
  public GraphQLQueryDepthInfoDTO uuid(String uuid) {
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
   * The user role to which the allowed maximum depth is being specified 
   **/
  public GraphQLQueryDepthInfoDTO role(String role) {
    this.role = role;
    return this;
  }

  
  @ApiModelProperty(example = "admin", value = "The user role to which the allowed maximum depth is being specified ")
  @JsonProperty("role")
  public String getRole() {
    return role;
  }
  public void setRole(String role) {
    this.role = role;
  }

  /**
   * The depth value allocated for the associated role 
   **/
  public GraphQLQueryDepthInfoDTO depthValue(Integer depthValue) {
    this.depthValue = depthValue;
    return this;
  }

  
  @ApiModelProperty(example = "20", value = "The depth value allocated for the associated role ")
  @JsonProperty("depthValue")
  public Integer getDepthValue() {
    return depthValue;
  }
  public void setDepthValue(Integer depthValue) {
    this.depthValue = depthValue;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GraphQLQueryDepthInfoDTO graphQLQueryDepthInfo = (GraphQLQueryDepthInfoDTO) o;
    return Objects.equals(uuid, graphQLQueryDepthInfo.uuid) &&
        Objects.equals(role, graphQLQueryDepthInfo.role) &&
        Objects.equals(depthValue, graphQLQueryDepthInfo.depthValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uuid, role, depthValue);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GraphQLQueryDepthInfoDTO {\n");
    
    sb.append("    uuid: ").append(toIndentedString(uuid)).append("\n");
    sb.append("    role: ").append(toIndentedString(role)).append("\n");
    sb.append("    depthValue: ").append(toIndentedString(depthValue)).append("\n");
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

