package org.wso2.carbon.apimgt.model;

import org.wso2.carbon.apimgt.model.ErrorListItem;
import java.util.*;



@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-10-14T14:19:24.818+05:30")
public class Error  {
  
  private Long code = null;
  private String message = null;
  private String description = null;
  private String moreInfo = null;
  private List<ErrorListItem> error = new ArrayList<ErrorListItem>();

  /**
   **/
  public Long getCode() {
    return code;
  }
  public void setCode(Long code) {
    this.code = code;
  }

  /**
   * Error message.
   **/
  public String getMessage() {
    return message;
  }
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * A detail description about the error message.

   **/
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Preferably an url with more details about the error.

   **/
  public String getMoreInfo() {
    return moreInfo;
  }
  public void setMoreInfo(String moreInfo) {
    this.moreInfo = moreInfo;
  }

  /**
   * If there are more than one error list them out.
For example, list out validation errors by each field.

   **/
  public List<ErrorListItem> getError() {
    return error;
  }
  public void setError(List<ErrorListItem> error) {
    this.error = error;
  }


  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class Error {\n");
    
    sb.append("  code: ").append(code).append("\n");
    sb.append("  message: ").append(message).append("\n");
    sb.append("  description: ").append(description).append("\n");
    sb.append("  moreInfo: ").append(moreInfo).append("\n");
    sb.append("  error: ").append(error).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
