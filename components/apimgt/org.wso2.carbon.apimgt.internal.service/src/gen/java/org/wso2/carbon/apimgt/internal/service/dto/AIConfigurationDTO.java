package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class AIConfigurationDTO   {
  
    private String llmProviderId = null;

  /**
   * UUID of the LLM (Large Language Model) provider.
   **/
  public AIConfigurationDTO llmProviderId(String llmProviderId) {
    this.llmProviderId = llmProviderId;
    return this;
  }

  
  @ApiModelProperty(value = "UUID of the LLM (Large Language Model) provider.")
  @JsonProperty("llmProviderId")
  public String getLlmProviderId() {
    return llmProviderId;
  }
  public void setLlmProviderId(String llmProviderId) {
    this.llmProviderId = llmProviderId;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AIConfigurationDTO aiConfiguration = (AIConfigurationDTO) o;
    return Objects.equals(llmProviderId, aiConfiguration.llmProviderId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(llmProviderId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AIConfigurationDTO {\n");
    
    sb.append("    llmProviderId: ").append(toIndentedString(llmProviderId)).append("\n");
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

