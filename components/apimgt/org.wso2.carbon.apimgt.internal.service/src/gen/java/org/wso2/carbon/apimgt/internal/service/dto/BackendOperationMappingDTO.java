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



public class BackendOperationMappingDTO   {
  
    private String backendId = null;
    private BackendOperationDTO backendOperation = null;

  /**
   **/
  public BackendOperationMappingDTO backendId(String backendId) {
    this.backendId = backendId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("backendId")
  public String getBackendId() {
    return backendId;
  }
  public void setBackendId(String backendId) {
    this.backendId = backendId;
  }

  /**
   **/
  public BackendOperationMappingDTO backendOperation(BackendOperationDTO backendOperation) {
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
    BackendOperationMappingDTO backendOperationMapping = (BackendOperationMappingDTO) o;
    return Objects.equals(backendId, backendOperationMapping.backendId) &&
        Objects.equals(backendOperation, backendOperationMapping.backendOperation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(backendId, backendOperation);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BackendOperationMappingDTO {\n");
    
    sb.append("    backendId: ").append(toIndentedString(backendId)).append("\n");
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

