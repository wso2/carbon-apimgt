package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIOperationsDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class ProductAPIDTO   {
  
    private String name = null;
    private String apiId = null;
    private String version = null;
    private List<APIOperationsDTO> operations = new ArrayList<APIOperationsDTO>();

  /**
   **/
  public ProductAPIDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "PizzaShackAPI", value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public ProductAPIDTO apiId(String apiId) {
    this.apiId = apiId;
    return this;
  }

  
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", required = true, value = "")
  @JsonProperty("apiId")
  @NotNull
  public String getApiId() {
    return apiId;
  }
  public void setApiId(String apiId) {
    this.apiId = apiId;
  }

  /**
   **/
  public ProductAPIDTO version(String version) {
    this.version = version;
    return this;
  }

  
  @ApiModelProperty(example = "1.0", value = "")
  @JsonProperty("version")
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   **/
  public ProductAPIDTO operations(List<APIOperationsDTO> operations) {
    this.operations = operations;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("operations")
  public List<APIOperationsDTO> getOperations() {
    return operations;
  }
  public void setOperations(List<APIOperationsDTO> operations) {
    this.operations = operations;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ProductAPIDTO productAPI = (ProductAPIDTO) o;
    return Objects.equals(name, productAPI.name) &&
        Objects.equals(apiId, productAPI.apiId) &&
        Objects.equals(version, productAPI.version) &&
        Objects.equals(operations, productAPI.operations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, apiId, version, operations);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ProductAPIDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    apiId: ").append(toIndentedString(apiId)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    operations: ").append(toIndentedString(operations)).append("\n");
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

