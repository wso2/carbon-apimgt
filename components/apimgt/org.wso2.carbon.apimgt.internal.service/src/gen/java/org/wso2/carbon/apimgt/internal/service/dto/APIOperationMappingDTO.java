package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.internal.service.dto.BackendOperationDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class APIOperationMappingDTO   {
  
    private String apiUUID = null;
    private String apiName = null;
    private String apiVersion = null;
    private String apiContext = null;
    private BackendOperationDTO backendOperation = null;

  /**
   **/
  public APIOperationMappingDTO apiUUID(String apiUUID) {
    this.apiUUID = apiUUID;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("apiUUID")
  public String getApiUUID() {
    return apiUUID;
  }
  public void setApiUUID(String apiUUID) {
    this.apiUUID = apiUUID;
  }

  /**
   **/
  public APIOperationMappingDTO apiName(String apiName) {
    this.apiName = apiName;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("apiName")
  public String getApiName() {
    return apiName;
  }
  public void setApiName(String apiName) {
    this.apiName = apiName;
  }

  /**
   **/
  public APIOperationMappingDTO apiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("apiVersion")
  public String getApiVersion() {
    return apiVersion;
  }
  public void setApiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
  }

  /**
   **/
  public APIOperationMappingDTO apiContext(String apiContext) {
    this.apiContext = apiContext;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("apiContext")
  public String getApiContext() {
    return apiContext;
  }
  public void setApiContext(String apiContext) {
    this.apiContext = apiContext;
  }

  /**
   **/
  public APIOperationMappingDTO backendOperation(BackendOperationDTO backendOperation) {
    this.backendOperation = backendOperation;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("backendOperation")
  public BackendOperationDTO getBackendOperation() {
    return backendOperation;
  }
  public void setBackendOperation(BackendOperationDTO backendOperation) {
    this.backendOperation = backendOperation;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIOperationMappingDTO apIOperationMapping = (APIOperationMappingDTO) o;
    return Objects.equals(apiUUID, apIOperationMapping.apiUUID) &&
        Objects.equals(apiName, apIOperationMapping.apiName) &&
        Objects.equals(apiVersion, apIOperationMapping.apiVersion) &&
        Objects.equals(apiContext, apIOperationMapping.apiContext) &&
        Objects.equals(backendOperation, apIOperationMapping.backendOperation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apiUUID, apiName, apiVersion, apiContext, backendOperation);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIOperationMappingDTO {\n");
    
    sb.append("    apiUUID: ").append(toIndentedString(apiUUID)).append("\n");
    sb.append("    apiName: ").append(toIndentedString(apiName)).append("\n");
    sb.append("    apiVersion: ").append(toIndentedString(apiVersion)).append("\n");
    sb.append("    apiContext: ").append(toIndentedString(apiContext)).append("\n");
    sb.append("    backendOperation: ").append(toIndentedString(backendOperation)).append("\n");
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

