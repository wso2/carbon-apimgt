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



public class OperationProxyMappingDTO   {
  
    private String id = null;
    private String name = null;
    private String context = null;
    private String version = null;
    private BackendOperationDTO backendOperation = null;

  /**
   * UUID of the targetAPI 
   **/
  public OperationProxyMappingDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "UUID of the targetAPI ")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public OperationProxyMappingDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "PizzaShackAPI", value = "")
  @JsonProperty("name")
 @Pattern(regexp="(^[^~!@#;:%^*()+={}|\\\\<>\"',&$\\[\\]/]*$)") @Size(min=1,max=150)  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public OperationProxyMappingDTO context(String context) {
    this.context = context;
    return this;
  }

  
  @ApiModelProperty(example = "pizza", value = "")
  @JsonProperty("context")
 @Size(min=1,max=232)  public String getContext() {
    return context;
  }
  public void setContext(String context) {
    this.context = context;
  }

  /**
   **/
  public OperationProxyMappingDTO version(String version) {
    this.version = version;
    return this;
  }

  
  @ApiModelProperty(example = "1.0.0", value = "")
  @JsonProperty("version")
 @Pattern(regexp="^[^~!@#;:%^*()+={}|\\\\<>\"',&/$\\[\\]\\s+/]+$") @Size(min=1,max=30)  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   **/
  public OperationProxyMappingDTO backendOperation(BackendOperationDTO backendOperation) {
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
    OperationProxyMappingDTO operationProxyMapping = (OperationProxyMappingDTO) o;
    return Objects.equals(id, operationProxyMapping.id) &&
        Objects.equals(name, operationProxyMapping.name) &&
        Objects.equals(context, operationProxyMapping.context) &&
        Objects.equals(version, operationProxyMapping.version) &&
        Objects.equals(backendOperation, operationProxyMapping.backendOperation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, context, version, backendOperation);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OperationProxyMappingDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    context: ").append(toIndentedString(context)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
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

