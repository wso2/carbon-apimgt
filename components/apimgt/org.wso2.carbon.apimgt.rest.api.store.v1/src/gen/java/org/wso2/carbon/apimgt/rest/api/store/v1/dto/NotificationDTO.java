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



public class NotificationDTO   {
  
    private String notificationId = null;

    @XmlType(name="NotificationTypeEnum")
    @XmlEnum(String.class)
    public enum NotificationTypeEnum {
        APPLICATION_CREATION("APPLICATION_CREATION"),
        SUBSCRIPTION_CREATION("SUBSCRIPTION_CREATION"),
        SUBSCRIPTION_UPDATE("SUBSCRIPTION_UPDATE"),
        SUBSCRIPTION_DELETION("SUBSCRIPTION_DELETION"),
        APPLICATION_REGISTRATION_PRODUCTION("APPLICATION_REGISTRATION_PRODUCTION"),
        APPLICATION_REGISTRATION_SANDBOX("APPLICATION_REGISTRATION_SANDBOX");
        private String value;

        NotificationTypeEnum (String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static NotificationTypeEnum fromValue(String v) {
            for (NotificationTypeEnum b : NotificationTypeEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private NotificationTypeEnum notificationType = null;
    private String comments = null;
    private String createdTime = null;
    private Boolean isRead = null;

  /**
   **/
  public NotificationDTO notificationId(String notificationId) {
    this.notificationId = notificationId;
    return this;
  }

  
  @ApiModelProperty(example = "863aef13-dab4-48b4-bf58-7363cd29601a", value = "")
  @JsonProperty("notificationId")
  public String getNotificationId() {
    return notificationId;
  }
  public void setNotificationId(String notificationId) {
    this.notificationId = notificationId;
  }

  /**
   **/
  public NotificationDTO notificationType(NotificationTypeEnum notificationType) {
    this.notificationType = notificationType;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("notificationType")
  public NotificationTypeEnum getNotificationType() {
    return notificationType;
  }
  public void setNotificationType(NotificationTypeEnum notificationType) {
    this.notificationType = notificationType;
  }

  /**
   **/
  public NotificationDTO comments(String comments) {
    this.comments = comments;
    return this;
  }

  
  @ApiModelProperty(example = "Application creation is rejected due to some reason.", value = "")
  @JsonProperty("comments")
  public String getComments() {
    return comments;
  }
  public void setComments(String comments) {
    this.comments = comments;
  }

  /**
   **/
  public NotificationDTO createdTime(String createdTime) {
    this.createdTime = createdTime;
    return this;
  }

  
  @ApiModelProperty(example = "2021-02-11 09:57:25", value = "")
  @JsonProperty("createdTime")
  public String getCreatedTime() {
    return createdTime;
  }
  public void setCreatedTime(String createdTime) {
    this.createdTime = createdTime;
  }

  /**
   **/
  public NotificationDTO isRead(Boolean isRead) {
    this.isRead = isRead;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "")
  @JsonProperty("isRead")
  public Boolean isIsRead() {
    return isRead;
  }
  public void setIsRead(Boolean isRead) {
    this.isRead = isRead;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NotificationDTO notification = (NotificationDTO) o;
    return Objects.equals(notificationId, notification.notificationId) &&
        Objects.equals(notificationType, notification.notificationType) &&
        Objects.equals(comments, notification.comments) &&
        Objects.equals(createdTime, notification.createdTime) &&
        Objects.equals(isRead, notification.isRead);
  }

  @Override
  public int hashCode() {
    return Objects.hash(notificationId, notificationType, comments, createdTime, isRead);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class NotificationDTO {\n");
    
    sb.append("    notificationId: ").append(toIndentedString(notificationId)).append("\n");
    sb.append("    notificationType: ").append(toIndentedString(notificationType)).append("\n");
    sb.append("    comments: ").append(toIndentedString(comments)).append("\n");
    sb.append("    createdTime: ").append(toIndentedString(createdTime)).append("\n");
    sb.append("    isRead: ").append(toIndentedString(isRead)).append("\n");
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

