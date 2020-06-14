package org.wso2.carbon.apimgt.rest.api.gateway.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.dto.ErrorListItemDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class ErrorDTO   {
  
    private Long code = null;
    private String message = null;
    private String description = null;
    private String moreInfo = null;
    private List<ErrorListItemDTO> error = new ArrayList<>();

  /**
   * Error code
   **/
  public ErrorDTO code(Long code) {
    this.code = code;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Error code")
  @JsonProperty("code")
  @NotNull
  public Long getCode() {
    return code;
  }
  public void setCode(Long code) {
    this.code = code;
  }

  /**
   * Error message.
   **/
  public ErrorDTO message(String message) {
    this.message = message;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Error message.")
  @JsonProperty("message")
  @NotNull
  public String getMessage() {
    return message;
  }
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * A detail description about the error message. 
   **/
  public ErrorDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(value = "A detail description about the error message. ")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Preferably an url with more details about the error. 
   **/
  public ErrorDTO moreInfo(String moreInfo) {
    this.moreInfo = moreInfo;
    return this;
  }

  
  @ApiModelProperty(value = "Preferably an url with more details about the error. ")
  @JsonProperty("moreInfo")
  public String getMoreInfo() {
    return moreInfo;
  }
  public void setMoreInfo(String moreInfo) {
    this.moreInfo = moreInfo;
  }

  /**
   * If there are more than one error list them out. For example, list out validation errors by each field. 
   **/
  public ErrorDTO error(List<ErrorListItemDTO> error) {
    this.error = error;
    return this;
  }

  
  @ApiModelProperty(value = "If there are more than one error list them out. For example, list out validation errors by each field. ")
  @JsonProperty("error")
  public List<ErrorListItemDTO> getError() {
    return error;
  }
  public void setError(List<ErrorListItemDTO> error) {
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
    ErrorDTO error = (ErrorDTO) o;
    return Objects.equals(code, error.code) &&
        Objects.equals(message, error.message) &&
        Objects.equals(description, error.description) &&
        Objects.equals(moreInfo, error.moreInfo) &&
        Objects.equals(error, error.error);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, message, description, moreInfo, error);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ErrorDTO {\n");
    
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    moreInfo: ").append(toIndentedString(moreInfo)).append("\n");
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

