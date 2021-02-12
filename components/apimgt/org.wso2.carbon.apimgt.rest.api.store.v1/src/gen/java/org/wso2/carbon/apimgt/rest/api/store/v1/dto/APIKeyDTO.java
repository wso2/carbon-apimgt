package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

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



public class APIKeyDTO   {
  
    private String apikey = null;
    private Integer validityTime = null;

  /**
   * API Key
   **/
  public APIKeyDTO apikey(String apikey) {
    this.apikey = apikey;
    return this;
  }

  
  @ApiModelProperty(example = "eyJoZWxsbyI6IndvcmxkIn0=.eyJ3c28yIjoiYXBpbSJ9.eyJ3c28yIjoic2lnbmF0dXJlIn0=", value = "API Key")
  @JsonProperty("apikey")
  public String getApikey() {
    return apikey;
  }
  public void setApikey(String apikey) {
    this.apikey = apikey;
  }

  /**
   **/
  public APIKeyDTO validityTime(Integer validityTime) {
    this.validityTime = validityTime;
    return this;
  }

  
  @ApiModelProperty(example = "3600", value = "")
  @JsonProperty("validityTime")
  public Integer getValidityTime() {
    return validityTime;
  }
  public void setValidityTime(Integer validityTime) {
    this.validityTime = validityTime;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIKeyDTO apIKey = (APIKeyDTO) o;
    return Objects.equals(apikey, apIKey.apikey) &&
        Objects.equals(validityTime, apIKey.validityTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apikey, validityTime);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIKeyDTO {\n");
    
    sb.append("    apikey: ").append(toIndentedString(apikey)).append("\n");
    sb.append("    validityTime: ").append(toIndentedString(validityTime)).append("\n");
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

