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
  
    private String keyName = null;
    private String apikey = null;
    private Integer validityPeriod = null;

  /**
   * API Key name
   **/
  public APIKeyDTO keyName(String keyName) {
    this.keyName = keyName;
    return this;
  }

  
  @ApiModelProperty(example = "Test_Key", value = "API Key name")
  @JsonProperty("keyName")
  public String getKeyName() {
    return keyName;
  }
  public void setKeyName(String keyName) {
    this.keyName = keyName;
  }

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
  public APIKeyDTO validityPeriod(Integer validityPeriod) {
    this.validityPeriod = validityPeriod;
    return this;
  }

  
  @ApiModelProperty(example = "3600", value = "")
  @JsonProperty("validityPeriod")
  public Integer getValidityPeriod() {
    return validityPeriod;
  }
  public void setValidityPeriod(Integer validityPeriod) {
    this.validityPeriod = validityPeriod;
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
    return Objects.equals(keyName, apIKey.keyName) &&
        Objects.equals(apikey, apIKey.apikey) &&
        Objects.equals(validityPeriod, apIKey.validityPeriod);
  }

  @Override
  public int hashCode() {
    return Objects.hash(keyName, apikey, validityPeriod);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIKeyDTO {\n");
    
    sb.append("    keyName: ").append(toIndentedString(keyName)).append("\n");
    sb.append("    apikey: ").append(toIndentedString(apikey)).append("\n");
    sb.append("    validityPeriod: ").append(toIndentedString(validityPeriod)).append("\n");
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

