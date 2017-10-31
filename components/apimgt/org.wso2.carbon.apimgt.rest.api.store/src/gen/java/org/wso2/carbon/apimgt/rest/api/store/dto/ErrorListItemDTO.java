package org.wso2.carbon.apimgt.rest.api.store.dto;


<<<<<<< HEAD
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
=======
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * ErrorListItemDTO
 */
public class ErrorListItemDTO   {
  @JsonProperty("code")
  private String code = null;

  @JsonProperty("message")
  private String message = null;
>>>>>>> upstream/master

  public ErrorListItemDTO code(String code) {
    this.code = code;
    return this;
  }

   /**
   * Get code
   * @return code
  **/
<<<<<<< HEAD

  @JsonIgnore
  public String getCreatedTime(){
    return createdTime;
  }
  public void setCreatedTime(String createdTime){
    this.createdTime=createdTime;
  }

  
  /**
   **/
=======
>>>>>>> upstream/master
  @ApiModelProperty(required = true, value = "")
  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

<<<<<<< HEAD
  
  /**
   * Description about Individual errors occurred\n
   **/
  @ApiModelProperty(required = true, value = "Description about Individual errors occurred\n")
  @JsonProperty("message")
=======
  public ErrorListItemDTO message(String message) {
    this.message = message;
    return this;
  }

   /**
   * Description about individual errors occurred 
   * @return message
  **/
  @ApiModelProperty(required = true, value = "Description about individual errors occurred ")
>>>>>>> upstream/master
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ErrorListItemDTO errorListItem = (ErrorListItemDTO) o;
    return Objects.equals(this.code, errorListItem.code) &&
        Objects.equals(this.message, errorListItem.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, message);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ErrorListItemDTO {\n");
    
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
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

