package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

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



public class DesignAssistantAPIPayloadResponseDTO   {
  
    private String generatedPayload = null;

  /**
   **/
  public DesignAssistantAPIPayloadResponseDTO generatedPayload(String generatedPayload) {
    this.generatedPayload = generatedPayload;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("generated_payload")
  public String getGeneratedPayload() {
    return generatedPayload;
  }
  public void setGeneratedPayload(String generatedPayload) {
    this.generatedPayload = generatedPayload;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DesignAssistantAPIPayloadResponseDTO designAssistantAPIPayloadResponse = (DesignAssistantAPIPayloadResponseDTO) o;
    return Objects.equals(generatedPayload, designAssistantAPIPayloadResponse.generatedPayload);
  }

  @Override
  public int hashCode() {
    return Objects.hash(generatedPayload);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DesignAssistantAPIPayloadResponseDTO {\n");
    
    sb.append("    generatedPayload: ").append(toIndentedString(generatedPayload)).append("\n");
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

