package org.wso2.carbon.apimgt.rest.api.service.catalog.dto;

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



public class ServiceInfoDTO   {
  
    private String id = null;
    private String name = null;
    private String key = null;
    private String version = null;
    private String md5 = null;

  /**
   **/
  public ServiceInfoDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public ServiceInfoDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "PizzashackEndpoint", value = "")
  @JsonProperty("name")
 @Pattern(regexp="^[^\\*]+$")  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public ServiceInfoDTO key(String key) {
    this.key = key;
    return this;
  }

  
  @ApiModelProperty(example = "PizzashackEndpoint-1.0.0", value = "")
  @JsonProperty("key")
  public String getKey() {
    return key;
  }
  public void setKey(String key) {
    this.key = key;
  }

  /**
   **/
  public ServiceInfoDTO version(String version) {
    this.version = version;
    return this;
  }

  
  @ApiModelProperty(example = "v1", value = "")
  @JsonProperty("version")
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   **/
  public ServiceInfoDTO md5(String md5) {
    this.md5 = md5;
    return this;
  }

  
  @ApiModelProperty(example = "36583a6a249b410e7fc4f892029709cac09763ddb230e1a829d5f9134d1abd07", value = "")
  @JsonProperty("md5")
  public String getMd5() {
    return md5;
  }
  public void setMd5(String md5) {
    this.md5 = md5;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ServiceInfoDTO serviceInfo = (ServiceInfoDTO) o;
    return Objects.equals(id, serviceInfo.id) &&
        Objects.equals(name, serviceInfo.name) &&
        Objects.equals(key, serviceInfo.key) &&
        Objects.equals(version, serviceInfo.version) &&
        Objects.equals(md5, serviceInfo.md5);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, key, version, md5);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ServiceInfoDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    key: ").append(toIndentedString(key)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    md5: ").append(toIndentedString(md5)).append("\n");
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

