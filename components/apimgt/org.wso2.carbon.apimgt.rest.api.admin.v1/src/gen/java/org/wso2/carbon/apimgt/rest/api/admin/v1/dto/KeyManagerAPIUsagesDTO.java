package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.APIInfoKeyManagerDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class KeyManagerAPIUsagesDTO   {
  
    private Integer apiCount = null;
    private List<APIInfoKeyManagerDTO> apis = new ArrayList<APIInfoKeyManagerDTO>();

  /**
   * The total count of APIs.
   **/
  public KeyManagerAPIUsagesDTO apiCount(Integer apiCount) {
    this.apiCount = apiCount;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The total count of APIs.")
  @JsonProperty("apiCount")
  @NotNull
  public Integer getApiCount() {
    return apiCount;
  }
  public void setApiCount(Integer apiCount) {
    this.apiCount = apiCount;
  }

  /**
   **/
  public KeyManagerAPIUsagesDTO apis(List<APIInfoKeyManagerDTO> apis) {
    this.apis = apis;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
      @Valid
  @JsonProperty("apis")
  @NotNull
  public List<APIInfoKeyManagerDTO> getApis() {
    return apis;
  }
  public void setApis(List<APIInfoKeyManagerDTO> apis) {
    this.apis = apis;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    KeyManagerAPIUsagesDTO keyManagerAPIUsages = (KeyManagerAPIUsagesDTO) o;
    return Objects.equals(apiCount, keyManagerAPIUsages.apiCount) &&
        Objects.equals(apis, keyManagerAPIUsages.apis);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apiCount, apis);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class KeyManagerAPIUsagesDTO {\n");
    
    sb.append("    apiCount: ").append(toIndentedString(apiCount)).append("\n");
    sb.append("    apis: ").append(toIndentedString(apis)).append("\n");
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

