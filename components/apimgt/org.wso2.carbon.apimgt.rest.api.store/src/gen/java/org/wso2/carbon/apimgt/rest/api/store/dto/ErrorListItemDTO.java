package org.wso2.carbon.apimgt.rest.api.store.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;
import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ErrorListItemDTO  {
  
  
  @NotNull
  private String code = null;
  
  @NotNull
  private String message = null;


  private String lastUpdatedTime = null;

  private String createdTime = null;

  /**
  * gets and sets the lastUpdatedTime for ErrorListItemDTO
  **/
  @JsonIgnore
  public String getLastUpdatedTime(){
    return lastUpdatedTime;
  }
  public void setLastUpdatedTime(String lastUpdatedTime){
    this.lastUpdatedTime=lastUpdatedTime;
  }

  /**
  * gets and sets the createdTime for a ErrorListItemDTO
  **/

  @JsonIgnore
  public String getCreatedTime(){
    return createdTime;
  }
  public void setCreatedTime(String createdTime){
    this.createdTime=createdTime;
  }

  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("code")
  public String getCode() {
    return code;
  }
  public void setCode(String code) {
    this.code = code;
  }

    /**
   * Description about Individual errors occurred\n
   **/
  @ApiModelProperty(required = true, value = "Description about Individual errors occurred\n")
  @JsonProperty("message")
  public String getMessage() {
    return message;
  }
  public void setMessage(String message) {
    this.message = message;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ErrorListItemDTO {\n");
    
    sb.append("  code: ").append(code).append("\n");
    sb.append("  message: ").append(message).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
