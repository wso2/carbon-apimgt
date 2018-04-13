package org.wso2.carbon.apimgt.rest.api.store.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorListItemDTO;
import java.util.Objects;

/**
 * ErrorDTO
 */
public class ErrorDTO   {
  @SerializedName("code")
  private Long code = null;

  @SerializedName("message")
  private String message = null;

  @SerializedName("description")
  private String description = null;

  @SerializedName("moreInfo")
  private String moreInfo = null;

  @SerializedName("error")
  private List<ErrorListItemDTO> error = new ArrayList<ErrorListItemDTO>();

  public ErrorDTO code(Long code) {
    this.code = code;
    return this;
  }

   /**
   * Get code
   * @return code
  **/
  @ApiModelProperty(required = true, value = "")
  public Long getCode() {
    return code;
  }

  public void setCode(Long code) {
    this.code = code;
  }

  public ErrorDTO message(String message) {
    this.message = message;
    return this;
  }

   /**
   * Error message.
   * @return message
  **/
  @ApiModelProperty(required = true, value = "Error message.")
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public ErrorDTO description(String description) {
    this.description = description;
    return this;
  }

   /**
   * A detail description about the error message. 
   * @return description
  **/
  @ApiModelProperty(value = "A detail description about the error message. ")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public ErrorDTO moreInfo(String moreInfo) {
    this.moreInfo = moreInfo;
    return this;
  }

   /**
   * Preferably an url with more details about the error. 
   * @return moreInfo
  **/
  @ApiModelProperty(value = "Preferably an url with more details about the error. ")
  public String getMoreInfo() {
    return moreInfo;
  }

  public void setMoreInfo(String moreInfo) {
    this.moreInfo = moreInfo;
  }

  public ErrorDTO error(List<ErrorListItemDTO> error) {
    this.error = error;
    return this;
  }

  public ErrorDTO addErrorItem(ErrorListItemDTO errorItem) {
    this.error.add(errorItem);
    return this;
  }

   /**
   * If there are more than one error list them out. For example, list out validation errors by each field. 
   * @return error
  **/
  @ApiModelProperty(value = "If there are more than one error list them out. For example, list out validation errors by each field. ")
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
    return Objects.equals(this.code, error.code) &&
        Objects.equals(this.message, error.message) &&
        Objects.equals(this.description, error.description) &&
        Objects.equals(this.moreInfo, error.moreInfo) &&
        Objects.equals(this.error, error.error);
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

