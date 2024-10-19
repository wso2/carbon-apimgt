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
  
    private List<LLMProviderDTO> llmProviders = new ArrayList<>();

  /**
   **/
  public LLMProviderListDTO llmProviders(List<LLMProviderDTO> llmProviders) {
    this.llmProviders = llmProviders;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("llmProviders")
  public List<LLMProviderDTO> getLlmProviders() {
    return llmProviders;
  }
  public void setLlmProviders(List<LLMProviderDTO> llmProviders) {
    this.llmProviders = llmProviders;
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
    return Objects.equals(llmProviders, llMProviderList.llmProviders);
  }

  @Override
  public int hashCode() {
    return Objects.hash(llmProviders);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LLMProviderListDTO {\n");
    
    sb.append("    llmProviders: ").append(toIndentedString(llmProviders)).append("\n");
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

