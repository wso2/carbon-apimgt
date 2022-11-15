package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class OperationPolicyAttachmentStatusDTO   {
  
    private Boolean isAttached = null;

  /**
   * True if attached, false if else.
   **/
  public OperationPolicyAttachmentStatusDTO isAttached(Boolean isAttached) {
    this.isAttached = isAttached;
    return this;
  }

  
  @ApiModelProperty(value = "True if attached, false if else.")
  @JsonProperty("isAttached")
  public Boolean isIsAttached() {
    return isAttached;
  }
  public void setIsAttached(Boolean isAttached) {
    this.isAttached = isAttached;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OperationPolicyAttachmentStatusDTO operationPolicyAttachmentStatus = (OperationPolicyAttachmentStatusDTO) o;
    return Objects.equals(isAttached, operationPolicyAttachmentStatus.isAttached);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isAttached);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OperationPolicyAttachmentStatusDTO {\n");
    
    sb.append("    isAttached: ").append(toIndentedString(isAttached)).append("\n");
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

