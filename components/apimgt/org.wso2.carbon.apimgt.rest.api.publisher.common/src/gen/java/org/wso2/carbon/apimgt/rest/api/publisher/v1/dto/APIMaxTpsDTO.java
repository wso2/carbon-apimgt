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



public class APIMaxTpsDTO   {
  
    private Long production = null;
    private Long sandbox = null;

  /**
   **/
  public APIMaxTpsDTO production(Long production) {
    this.production = production;
    return this;
  }

  
  @ApiModelProperty(example = "1000", value = "")
  @JsonProperty("production")
  public Long getProduction() {
    return production;
  }
  public void setProduction(Long production) {
    this.production = production;
  }

  /**
   **/
  public APIMaxTpsDTO sandbox(Long sandbox) {
    this.sandbox = sandbox;
    return this;
  }

  
  @ApiModelProperty(example = "1000", value = "")
  @JsonProperty("sandbox")
  public Long getSandbox() {
    return sandbox;
  }
  public void setSandbox(Long sandbox) {
    this.sandbox = sandbox;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIMaxTpsDTO apIMaxTps = (APIMaxTpsDTO) o;
    return Objects.equals(production, apIMaxTps.production) &&
        Objects.equals(sandbox, apIMaxTps.sandbox);
  }

  @Override
  public int hashCode() {
    return Objects.hash(production, sandbox);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIMaxTpsDTO {\n");
    
    sb.append("    production: ").append(toIndentedString(production)).append("\n");
    sb.append("    sandbox: ").append(toIndentedString(sandbox)).append("\n");
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

