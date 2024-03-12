package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApiChatExecuteRequestApiSpecDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApiChatExecuteRequestResponseDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class ApiChatExecuteRequestDTO   {
  
    private String command = null;
    private ApiChatExecuteRequestApiSpecDTO apiSpec = null;
    private ApiChatExecuteRequestResponseDTO response = null;

  /**
   * User specified testcase to be tested against the API 
   **/
  public ApiChatExecuteRequestDTO command(String command) {
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
  public ApiChatExecuteRequestDTO apiSpec(ApiChatExecuteRequestApiSpecDTO apiSpec) {
    this.apiSpec = apiSpec;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("apiSpec")
  public ApiChatExecuteRequestApiSpecDTO getApiSpec() {
    return apiSpec;
  }
  public void setApiSpec(ApiChatExecuteRequestApiSpecDTO apiSpec) {
    this.apiSpec = apiSpec;
  }

  /**
   **/
  public ApiChatExecuteRequestDTO response(ApiChatExecuteRequestResponseDTO response) {
    this.response = response;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("response")
  public ApiChatExecuteRequestResponseDTO getResponse() {
    return response;
  }
  public void setResponse(ApiChatExecuteRequestResponseDTO response) {
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
    ApiChatExecuteRequestDTO apiChatExecuteRequest = (ApiChatExecuteRequestDTO) o;
    return Objects.equals(command, apiChatExecuteRequest.command) &&
        Objects.equals(apiSpec, apiChatExecuteRequest.apiSpec) &&
        Objects.equals(response, apiChatExecuteRequest.response);
  }

  @Override
  public int hashCode() {
    return Objects.hash(command, apiSpec, response);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiChatExecuteRequestDTO {\n");
    
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

