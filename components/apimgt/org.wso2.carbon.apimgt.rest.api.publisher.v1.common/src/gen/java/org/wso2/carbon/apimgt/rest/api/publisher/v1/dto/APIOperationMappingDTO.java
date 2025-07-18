package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.BackendOperationDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class APIOperationMappingDTO   {
  
    private String apiId = null;
    private String apiName = null;
    private String apiVersion = null;
    private BackendOperationDTO backendOperation = null;

  /**
   * UUID of the targetAPI 
   **/
  public APIOperationMappingDTO apiId(String apiId) {
    this.apiId = apiId;
    return this;
  }

  
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "UUID of the targetAPI ")
  @JsonProperty("apiId")
  public String getApiId() {
    return apiId;
  }
  public void setApiId(String apiId) {
    this.apiId = apiId;
  }

  /**
   **/
  public APIOperationMappingDTO apiName(String apiName) {
    this.apiName = apiName;
    return this;
  }

  
  @ApiModelProperty(example = "PizzaShackAPI", value = "")
  @JsonProperty("apiName")
 @Pattern(regexp="(^[^~!@#;:%^*()+={}|\\\\<>\"',&$\\[\\]/]*$)") @Size(min=1,max=150)  public String getApiName() {
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

  
  @ApiModelProperty(example = "1.0.0", value = "")
  @JsonProperty("apiVersion")
 @Pattern(regexp="^[^~!@#;:%^*()+={}|\\\\<>\"',&/$\\[\\]\\s+/]+$") @Size(min=1,max=30)  public String getApiVersion() {
    return apiVersion;
  }
  public void setApiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
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
    return Objects.equals(apiId, apIOperationMapping.apiId) &&
        Objects.equals(apiName, apIOperationMapping.apiName) &&
        Objects.equals(apiVersion, apIOperationMapping.apiVersion) &&
        Objects.equals(backendOperation, apIOperationMapping.backendOperation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apiId, apiName, apiVersion, backendOperation);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIOperationMappingDTO {\n");
    
    sb.append("    apiId: ").append(toIndentedString(apiId)).append("\n");
    sb.append("    apiName: ").append(toIndentedString(apiName)).append("\n");
    sb.append("    apiVersion: ").append(toIndentedString(apiVersion)).append("\n");
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

