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



public class APIServiceInfoDTO   {
  
    private String key = null;
    private String name = null;
    private String version = null;
    private Boolean outdated = null;

  /**
   **/
  public APIServiceInfoDTO key(String key) {
    this.key = key;
    return this;
  }

  
  @ApiModelProperty(example = "PetStore-1.0.0", value = "")
  @JsonProperty("key")
  public String getKey() {
    return key;
  }
  public void setKey(String key) {
    this.key = key;
  }

  /**
   **/
  public APIServiceInfoDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "PetStore", value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public APIServiceInfoDTO version(String version) {
    this.version = version;
    return this;
  }

  
  @ApiModelProperty(example = "1.0.0", value = "")
  @JsonProperty("version")
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   **/
  public APIServiceInfoDTO outdated(Boolean outdated) {
    this.outdated = outdated;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "")
  @JsonProperty("outdated")
  public Boolean isOutdated() {
    return outdated;
  }
  public void setOutdated(Boolean outdated) {
    this.outdated = outdated;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIServiceInfoDTO apIServiceInfo = (APIServiceInfoDTO) o;
    return Objects.equals(key, apIServiceInfo.key) &&
        Objects.equals(name, apIServiceInfo.name) &&
        Objects.equals(version, apIServiceInfo.version) &&
        Objects.equals(outdated, apIServiceInfo.outdated);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, name, version, outdated);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIServiceInfoDTO {\n");
    
    sb.append("    key: ").append(toIndentedString(key)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    outdated: ").append(toIndentedString(outdated)).append("\n");
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

