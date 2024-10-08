package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.internal.service.dto.LLMProviderDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class LLMProviderListDTO   {
  
    private List<LLMProviderDTO> apis = new ArrayList<>();

  /**
   **/
  public LLMProviderListDTO apis(List<LLMProviderDTO> apis) {
    this.apis = apis;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("apis")
  public List<LLMProviderDTO> getApis() {
    return apis;
  }
  public void setApis(List<LLMProviderDTO> apis) {
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
    LLMProviderListDTO llMProviderList = (LLMProviderListDTO) o;
    return Objects.equals(apis, llMProviderList.apis);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apis);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LLMProviderListDTO {\n");
    
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

