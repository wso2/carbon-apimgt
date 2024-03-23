package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApiChatRequestApiSpecDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApiChatRequestResponseDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class ApiChatRequestDTO   {
  
    private String apiChatRequestId = null;
    private String command = null;
    private ApiChatRequestApiSpecDTO apiSpec = null;
    private ApiChatRequestResponseDTO response = null;

  /**
   * Request UUID 
   **/
  public ApiChatRequestDTO apiChatRequestId(String apiChatRequestId) {
    this.apiChatRequestId = apiChatRequestId;
    return this;
  }

  
  @ApiModelProperty(example = "faae5fcc-cbae-40c4-bf43-89931630d313", value = "Request UUID ")
  @JsonProperty("apiChatRequestId")
  public String getApiChatRequestId() {
    return apiChatRequestId;
  }
  public void setApiChatRequestId(String apiChatRequestId) {
    this.apiChatRequestId = apiChatRequestId;
  }

  /**
   * User specified testcase to be tested against the API 
   **/
  public ApiChatRequestDTO command(String command) {
    this.command = command;
    return this;
  }

  
  @ApiModelProperty(example = "Get pet with id 123", value = "User specified testcase to be tested against the API ")
  @JsonProperty("command")
  public String getCommand() {
    return command;
  }
  public void setCommand(String command) {
    this.command = command;
  }

  /**
   **/
  public ApiChatRequestDTO apiSpec(ApiChatRequestApiSpecDTO apiSpec) {
    this.apiSpec = apiSpec;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("apiSpec")
  public ApiChatRequestApiSpecDTO getApiSpec() {
    return apiSpec;
  }
  public void setApiSpec(ApiChatRequestApiSpecDTO apiSpec) {
    this.apiSpec = apiSpec;
  }

  /**
   **/
  public ApiChatRequestDTO response(ApiChatRequestResponseDTO response) {
    this.response = response;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("response")
  public ApiChatRequestResponseDTO getResponse() {
    return response;
  }
  public void setResponse(ApiChatRequestResponseDTO response) {
    this.response = response;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiChatRequestDTO apiChatRequest = (ApiChatRequestDTO) o;
    return Objects.equals(apiChatRequestId, apiChatRequest.apiChatRequestId) &&
        Objects.equals(command, apiChatRequest.command) &&
        Objects.equals(apiSpec, apiChatRequest.apiSpec) &&
        Objects.equals(response, apiChatRequest.response);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apiChatRequestId, command, apiSpec, response);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiChatRequestDTO {\n");
    
    sb.append("    apiChatRequestId: ").append(toIndentedString(apiChatRequestId)).append("\n");
    sb.append("    command: ").append(toIndentedString(command)).append("\n");
    sb.append("    apiSpec: ").append(toIndentedString(apiSpec)).append("\n");
    sb.append("    response: ").append(toIndentedString(response)).append("\n");
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

