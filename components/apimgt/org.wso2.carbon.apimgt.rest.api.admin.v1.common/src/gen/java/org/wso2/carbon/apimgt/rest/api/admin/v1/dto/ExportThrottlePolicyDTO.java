package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

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



public class ExportThrottlePolicyDTO   {
  
    private String type = null;
    private String subtype = null;
    private String version = null;
    private Object data = null;

  /**
   **/
  public ExportThrottlePolicyDTO type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  /**
   **/
  public ExportThrottlePolicyDTO subtype(String subtype) {
    this.subtype = subtype;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("subtype")
  public String getSubtype() {
    return subtype;
  }
  public void setSubtype(String subtype) {
    this.subtype = subtype;
  }

  /**
   **/
  public ExportThrottlePolicyDTO version(String version) {
    this.version = version;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("version")
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   **/
  public ExportThrottlePolicyDTO data(Object data) {
    this.data = data;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("data")
  public Object getData() {
    return data;
  }
  public void setData(Object data) {
    this.data = data;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExportThrottlePolicyDTO exportThrottlePolicy = (ExportThrottlePolicyDTO) o;
    return Objects.equals(type, exportThrottlePolicy.type) &&
        Objects.equals(subtype, exportThrottlePolicy.subtype) &&
        Objects.equals(version, exportThrottlePolicy.version) &&
        Objects.equals(data, exportThrottlePolicy.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, subtype, version, data);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExportThrottlePolicyDTO {\n");
    
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    subtype: ").append(toIndentedString(subtype)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    data: ").append(toIndentedString(data)).append("\n");
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

