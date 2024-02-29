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



public class PatchNotificationRequestDTO   {
  
    private Boolean read = null;

  /**
   * Indicates whether the notification should be marked as read. 
   **/
  public PatchNotificationRequestDTO read(Boolean read) {
    this.read = read;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "Indicates whether the notification should be marked as read. ")
  @JsonProperty("read")
  public Boolean isRead() {
    return read;
  }
  public void setRead(Boolean read) {
    this.read = read;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PatchNotificationRequestDTO patchNotificationRequest = (PatchNotificationRequestDTO) o;
    return Objects.equals(read, patchNotificationRequest.read);
  }

  @Override
  public int hashCode() {
    return Objects.hash(read);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PatchNotificationRequestDTO {\n");
    
    sb.append("    read: ").append(toIndentedString(read)).append("\n");
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

