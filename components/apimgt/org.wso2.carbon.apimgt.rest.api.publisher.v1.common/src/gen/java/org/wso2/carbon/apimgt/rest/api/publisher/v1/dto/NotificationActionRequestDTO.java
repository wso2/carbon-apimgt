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



public class NotificationActionRequestDTO   {
  
    private Boolean markAsRead = null;

  /**
   * True to mark as read, false to mark as unread
   **/
  public NotificationActionRequestDTO markAsRead(Boolean markAsRead) {
    this.markAsRead = markAsRead;
    return this;
  }

  
  @ApiModelProperty(example = "true", required = true, value = "True to mark as read, false to mark as unread")
  @JsonProperty("markAsRead")
  @NotNull
  public Boolean isMarkAsRead() {
    return markAsRead;
  }
  public void setMarkAsRead(Boolean markAsRead) {
    this.markAsRead = markAsRead;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NotificationActionRequestDTO notificationActionRequest = (NotificationActionRequestDTO) o;
    return Objects.equals(markAsRead, notificationActionRequest.markAsRead);
  }

  @Override
  public int hashCode() {
    return Objects.hash(markAsRead);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class NotificationActionRequestDTO {\n");
    
    sb.append("    markAsRead: ").append(toIndentedString(markAsRead)).append("\n");
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

