package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;



public class APISecurityAuditInfoDTO   {
  
    private Object specfile = null;
    private String cid = null;
    private String name = null;

  /**
   * The swagger definition of the API. 
   **/
  public APISecurityAuditInfoDTO specfile(Object specfile) {
    this.specfile = specfile;
    return this;
  }

  
  @ApiModelProperty(example = "\"swagger.json\"", value = "The swagger definition of the API. ")
  @JsonProperty("specfile")
  public Object getSpecfile() {
    return specfile;
  }
  public void setSpecfile(Object specfile) {
    this.specfile = specfile;
  }

  /**
   * The ID of the API collection. 
   **/
  public APISecurityAuditInfoDTO cid(String cid) {
    this.cid = cid;
    return this;
  }

  
  @ApiModelProperty(example = "395df257-3f79-495f-23f3-0827947bdc6e", value = "The ID of the API collection. ")
  @JsonProperty("cid")
  public String getCid() {
    return cid;
  }
  public void setCid(String cid) {
    this.cid = cid;
  }

  /**
   * The name of the API. 
   **/
  public APISecurityAuditInfoDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "Swagger-Petstore", value = "The name of the API. ")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APISecurityAuditInfoDTO apISecurityAuditInfo = (APISecurityAuditInfoDTO) o;
    return Objects.equals(specfile, apISecurityAuditInfo.specfile) &&
        Objects.equals(cid, apISecurityAuditInfo.cid) &&
        Objects.equals(name, apISecurityAuditInfo.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(specfile, cid, name);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("{\n");
    sb.append("    \"specfile\": ").append(toIndentedString(specfile)).append("\n");
    sb.append("    \"cid\": ").append(toIndentedString(cid)).append("\n");
    sb.append("    \"name\": ").append(toIndentedString(name)).append("\n");
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

