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



public class ApiEndpointValidationResponseDTO   {
  
    private Integer statusCode = null;
    private String statusMessage = null;
    private String error = null;

  /**
   * HTTP status code
   **/
  public ApiEndpointValidationResponseDTO statusCode(Integer statusCode) {
    this.statusCode = statusCode;
    return this;
  }

  
  @ApiModelProperty(example = "200", required = true, value = "HTTP status code")
  @JsonProperty("statusCode")
  @NotNull
  public Integer getStatusCode() {
    return statusCode;
  }
  public void setStatusCode(Integer statusCode) {
    this.statusCode = statusCode;
  }

  /**
   * string
   **/
  public ApiEndpointValidationResponseDTO statusMessage(String statusMessage) {
    this.statusMessage = statusMessage;
    return this;
  }

  
  @ApiModelProperty(example = "OK", required = true, value = "string")
  @JsonProperty("statusMessage")
  @NotNull
  public String getStatusMessage() {
    return statusMessage;
  }
  public void setStatusMessage(String statusMessage) {
    this.statusMessage = statusMessage;
  }

  /**
   * If an error occurs, the error message will be set to this property. If not, this will remain null. 
   **/
  public ApiEndpointValidationResponseDTO error(String error) {
    this.error = error;
    return this;
  }

  
  @ApiModelProperty(value = "If an error occurs, the error message will be set to this property. If not, this will remain null. ")
  @JsonProperty("error")
  public String getError() {
    return error;
  }
  public void setError(String error) {
    this.error = error;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiEndpointValidationResponseDTO apiEndpointValidationResponse = (ApiEndpointValidationResponseDTO) o;
    return Objects.equals(statusCode, apiEndpointValidationResponse.statusCode) &&
        Objects.equals(statusMessage, apiEndpointValidationResponse.statusMessage) &&
        Objects.equals(error, apiEndpointValidationResponse.error);
  }

  @Override
  public int hashCode() {
    return Objects.hash(statusCode, statusMessage, error);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiEndpointValidationResponseDTO {\n");
    
    sb.append("    statusCode: ").append(toIndentedString(statusCode)).append("\n");
    sb.append("    statusMessage: ").append(toIndentedString(statusMessage)).append("\n");
    sb.append("    error: ").append(toIndentedString(error)).append("\n");
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

