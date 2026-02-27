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



public class APIAPIKeyAssociateRequestDTO   {
  
    private String keyName = null;
    private String applicationName = null;

  /**
   * API Key name
   **/
  public APIAPIKeyAssociateRequestDTO keyName(String keyName) {
    this.keyName = keyName;
    return this;
  }

  
  @ApiModelProperty(example = "Test_Key", required = true, value = "API Key name")
  @JsonProperty("keyName")
  @NotNull
  public String getKeyName() {
    return keyName;
  }
  public void setKeyName(String keyName) {
    this.keyName = keyName;
  }

  /**
   * Application name to be associated
   **/
  public APIAPIKeyAssociateRequestDTO applicationName(String applicationName) {
    this.applicationName = applicationName;
    return this;
  }

  
  @ApiModelProperty(example = "DefaultApplication", required = true, value = "Application name to be associated")
  @JsonProperty("applicationName")
  @NotNull
  public String getApplicationName() {
    return applicationName;
  }
  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIAPIKeyAssociateRequestDTO apIAPIKeyAssociateRequest = (APIAPIKeyAssociateRequestDTO) o;
    return Objects.equals(keyName, apIAPIKeyAssociateRequest.keyName) &&
        Objects.equals(applicationName, apIAPIKeyAssociateRequest.applicationName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(keyName, applicationName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIAPIKeyAssociateRequestDTO {\n");
    
    sb.append("    keyName: ").append(toIndentedString(keyName)).append("\n");
    sb.append("    applicationName: ").append(toIndentedString(applicationName)).append("\n");
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

