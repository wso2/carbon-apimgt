package org.wso2.apk.apimgt.rest.api.backoffice.v1.dto;

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



public class ProductAPIDTO   {
  
    private String runtimeId = null;
    private String apiId = null;

  /**
   **/
  public ProductAPIDTO runtimeId(String runtimeId) {
    this.runtimeId = runtimeId;
    return this;
  }

  
  @ApiModelProperty(example = "01234567-765-0765-0123-012345678901", required = true, value = "")
  @JsonProperty("runtimeId")
  @NotNull
  public String getRuntimeId() {
    return runtimeId;
  }
  public void setRuntimeId(String runtimeId) {
    this.runtimeId = runtimeId;
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


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ProductAPIDTO productAPI = (ProductAPIDTO) o;
    return Objects.equals(runtimeId, productAPI.runtimeId) &&
        Objects.equals(apiId, productAPI.apiId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(runtimeId, apiId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ProductAPIDTO {\n");
    
    sb.append("    runtimeId: ").append(toIndentedString(runtimeId)).append("\n");
    sb.append("    apiId: ").append(toIndentedString(apiId)).append("\n");
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

