package org.wso2.carbon.apimgt.rest.api.publisher.dto;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.ErrorListItemDTO;
import java.util.*;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class ErrorDTO  {
  
  
  @NotNull
  private Long code = null;
  
  @NotNull
  private String message = null;
  
  
  private String description = null;
  
  
  private String moreInfo = null;
  
  
  private List<ErrorListItemDTO> error = new ArrayList<ErrorListItemDTO>();

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("code")
  public Long getCode() {
    return code;
  }
  public void setCode(Long code) {
    this.code = code;
  }

  
  /**
   * Error message.
   **/
  @ApiModelProperty(required = true, value = "Error message.")
  @JsonProperty("message")
  public String getMessage() {
    return message;
  }
  public void setMessage(String message) {
    this.message = message;
  }

  
  /**
   * A detail description about the error message.
   **/
  @ApiModelProperty(value = "A detail description about the error message.")
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
  @ApiModelProperty(value = "Preferably an url with more details about the error.")
  @JsonProperty("moreInfo")
  public String getMoreInfo() {
    return moreInfo;
  }
  public void setMoreInfo(String moreInfo) {
    this.moreInfo = moreInfo;
  }

  
  /**
   * If there are more than one error list them out.\nFor example, list out validation errors by each field.
   **/
  @ApiModelProperty(value = "If there are more than one error list them out.\nFor example, list out validation errors by each field.")
  @JsonProperty("error")
  public List<ErrorListItemDTO> getError() {
    return error;
  }
  public void setError(List<ErrorListItemDTO> error) {
    this.error = error;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ErrorDTO {\n");
    
    sb.append("  code: ").append(code).append("\n");
    sb.append("  message: ").append(message).append("\n");
    sb.append("  description: ").append(description).append("\n");
    sb.append("  moreInfo: ").append(moreInfo).append("\n");
    sb.append("  error: ").append(error).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
