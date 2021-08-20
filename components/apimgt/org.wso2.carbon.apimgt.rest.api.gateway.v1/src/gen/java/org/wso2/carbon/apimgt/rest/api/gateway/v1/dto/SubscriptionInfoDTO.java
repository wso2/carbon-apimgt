package org.wso2.carbon.apimgt.rest.api.gateway.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.dto.ApplicationInfoDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class SubscriptionInfoDTO   {
  
    private String subscriptionUUID = null;
    private String subscriptionPolicy = null;
    private String status = null;
    private ApplicationInfoDTO application = null;

  /**
   **/
  public SubscriptionInfoDTO subscriptionUUID(String subscriptionUUID) {
    this.subscriptionUUID = subscriptionUUID;
    return this;
  }

  
  @ApiModelProperty(example = "d290f1ee-6c54-4b01-90e6-d701748f0851", value = "")
  @JsonProperty("subscriptionUUID")
  public String getSubscriptionUUID() {
    return subscriptionUUID;
  }
  public void setSubscriptionUUID(String subscriptionUUID) {
    this.subscriptionUUID = subscriptionUUID;
  }

  /**
   **/
  public SubscriptionInfoDTO subscriptionPolicy(String subscriptionPolicy) {
    this.subscriptionPolicy = subscriptionPolicy;
    return this;
  }

  
  @ApiModelProperty(example = "Gold", value = "")
  @JsonProperty("subscriptionPolicy")
  public String getSubscriptionPolicy() {
    return subscriptionPolicy;
  }
  public void setSubscriptionPolicy(String subscriptionPolicy) {
    this.subscriptionPolicy = subscriptionPolicy;
  }

  /**
   **/
  public SubscriptionInfoDTO status(String status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(example = "UnBlocked", value = "")
  @JsonProperty("status")
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   **/
  public SubscriptionInfoDTO application(ApplicationInfoDTO application) {
    this.application = application;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("application")
  public ApplicationInfoDTO getApplication() {
    return application;
  }
  public void setApplication(ApplicationInfoDTO application) {
    this.application = application;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SubscriptionInfoDTO subscriptionInfo = (SubscriptionInfoDTO) o;
    return Objects.equals(subscriptionUUID, subscriptionInfo.subscriptionUUID) &&
        Objects.equals(subscriptionPolicy, subscriptionInfo.subscriptionPolicy) &&
        Objects.equals(status, subscriptionInfo.status) &&
        Objects.equals(application, subscriptionInfo.application);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subscriptionUUID, subscriptionPolicy, status, application);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubscriptionInfoDTO {\n");
    
    sb.append("    subscriptionUUID: ").append(toIndentedString(subscriptionUUID)).append("\n");
    sb.append("    subscriptionPolicy: ").append(toIndentedString(subscriptionPolicy)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    application: ").append(toIndentedString(application)).append("\n");
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

