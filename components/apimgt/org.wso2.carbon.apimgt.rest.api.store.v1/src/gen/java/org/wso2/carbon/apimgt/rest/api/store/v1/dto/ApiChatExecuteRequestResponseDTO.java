package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

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



public class ApiChatExecuteRequestResponseDTO   {
  
    private Integer code = null;
    private Object headers = null;
    private Object body = null;

  /**
   * HTTP status code of the response
   **/
  public ApiChatExecuteRequestResponseDTO code(Integer code) {
    this.code = code;
    return this;
  }

  
  @ApiModelProperty(example = "201", value = "HTTP status code of the response")
  @JsonProperty("code")
  public Integer getCode() {
    return code;
  }
  public void setCode(Integer code) {
    this.code = code;
  }

  /**
   * Response headers
   **/
  public ApiChatExecuteRequestResponseDTO headers(Object headers) {
    this.headers = headers;
    return this;
  }

  
  @ApiModelProperty(value = "Response headers")
      @Valid
  @JsonProperty("headers")
  public Object getHeaders() {
    return headers;
  }
  public void setHeaders(Object headers) {
    this.headers = headers;
  }

  /**
   * Response payload
   **/
  public ApiChatExecuteRequestResponseDTO body(Object body) {
    this.body = body;
    return this;
  }

  
  @ApiModelProperty(value = "Response payload")
      @Valid
  @JsonProperty("body")
  public Object getBody() {
    return body;
  }
  public void setBody(Object body) {
    this.body = body;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiChatExecuteRequestResponseDTO apiChatExecuteRequestResponse = (ApiChatExecuteRequestResponseDTO) o;
    return Objects.equals(code, apiChatExecuteRequestResponse.code) &&
        Objects.equals(headers, apiChatExecuteRequestResponse.headers) &&
        Objects.equals(body, apiChatExecuteRequestResponse.body);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, headers, body);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiChatExecuteRequestResponseDTO {\n");
    
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    headers: ").append(toIndentedString(headers)).append("\n");
    sb.append("    body: ").append(toIndentedString(body)).append("\n");
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

