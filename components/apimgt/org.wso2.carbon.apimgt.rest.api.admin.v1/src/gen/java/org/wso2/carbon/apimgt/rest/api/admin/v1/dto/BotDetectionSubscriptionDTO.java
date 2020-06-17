package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class BotDetectionSubscriptionDTO   {
  
    private String uuid = null;
    private String email = null;

  /**
   * UUID of the subscription
   **/
  public BotDetectionSubscriptionDTO uuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

  
  @ApiModelProperty(example = "urn:uuid:1ed6d2de-29df-4fed-a96a-46d2329dce65", value = "UUID of the subscription")
  @JsonProperty("uuid")
  public String getUuid() {
    return uuid;
  }
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  /**
   * email
   **/
  public BotDetectionSubscriptionDTO email(String email) {
    this.email = email;
    return this;
  }

  
  @ApiModelProperty(example = "abc@.com", required = true, value = "email")
  @JsonProperty("email")
  @NotNull
  public String getEmail() {
    return email;
  }
  public void setEmail(String email) {
    this.email = email;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BotDetectionSubscriptionDTO botDetectionSubscription = (BotDetectionSubscriptionDTO) o;
    return Objects.equals(uuid, botDetectionSubscription.uuid) &&
        Objects.equals(email, botDetectionSubscription.email);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uuid, email);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BotDetectionSubscriptionDTO {\n");
    
    sb.append("    uuid: ").append(toIndentedString(uuid)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
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

