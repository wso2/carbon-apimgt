package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class WebhooksSubscriptionDTO   {
  
    private String apiKey = null;
    private String appID = null;
    private String callbackURL = null;
    private String topicName = null;
    private String secret = null;
    private Long expiryTime = null;

  /**
   * The API key
   **/
  public WebhooksSubscriptionDTO apiKey(String apiKey) {
    this.apiKey = apiKey;
    return this;
  }

  
  @ApiModelProperty(value = "The API key")
  @JsonProperty("apiKey")
  public String getApiKey() {
    return apiKey;
  }
  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  /**
   * The application ID of the subscription.
   **/
  public WebhooksSubscriptionDTO appID(String appID) {
    this.appID = appID;
    return this;
  }

  
  @ApiModelProperty(value = "The application ID of the subscription.")
  @JsonProperty("appID")
  public String getAppID() {
    return appID;
  }
  public void setAppID(String appID) {
    this.appID = appID;
  }

  /**
   * The callback URL
   **/
  public WebhooksSubscriptionDTO callbackURL(String callbackURL) {
    this.callbackURL = callbackURL;
    return this;
  }

  
  @ApiModelProperty(value = "The callback URL")
  @JsonProperty("callbackURL")
  public String getCallbackURL() {
    return callbackURL;
  }
  public void setCallbackURL(String callbackURL) {
    this.callbackURL = callbackURL;
  }

  /**
   * The topic name.
   **/
  public WebhooksSubscriptionDTO topicName(String topicName) {
    this.topicName = topicName;
    return this;
  }

  
  @ApiModelProperty(value = "The topic name.")
  @JsonProperty("topicName")
  public String getTopicName() {
    return topicName;
  }
  public void setTopicName(String topicName) {
    this.topicName = topicName;
  }

  /**
   * Secret value of the subscription.
   **/
  public WebhooksSubscriptionDTO secret(String secret) {
    this.secret = secret;
    return this;
  }

  
  @ApiModelProperty(value = "Secret value of the subscription.")
  @JsonProperty("secret")
  public String getSecret() {
    return secret;
  }
  public void setSecret(String secret) {
    this.secret = secret;
  }

  /**
   * the expiry time in millis
   **/
  public WebhooksSubscriptionDTO expiryTime(Long expiryTime) {
    this.expiryTime = expiryTime;
    return this;
  }

  
  @ApiModelProperty(value = "the expiry time in millis")
  @JsonProperty("expiryTime")
  public Long getExpiryTime() {
    return expiryTime;
  }
  public void setExpiryTime(Long expiryTime) {
    this.expiryTime = expiryTime;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WebhooksSubscriptionDTO webhooksSubscription = (WebhooksSubscriptionDTO) o;
    return Objects.equals(apiKey, webhooksSubscription.apiKey) &&
        Objects.equals(appID, webhooksSubscription.appID) &&
        Objects.equals(callbackURL, webhooksSubscription.callbackURL) &&
        Objects.equals(topicName, webhooksSubscription.topicName) &&
        Objects.equals(secret, webhooksSubscription.secret) &&
        Objects.equals(expiryTime, webhooksSubscription.expiryTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apiKey, appID, callbackURL, topicName, secret, expiryTime);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class WebhooksSubscriptionDTO {\n");
    
    sb.append("    apiKey: ").append(toIndentedString(apiKey)).append("\n");
    sb.append("    appID: ").append(toIndentedString(appID)).append("\n");
    sb.append("    callbackURL: ").append(toIndentedString(callbackURL)).append("\n");
    sb.append("    topicName: ").append(toIndentedString(topicName)).append("\n");
    sb.append("    secret: ").append(toIndentedString(secret)).append("\n");
    sb.append("    expiryTime: ").append(toIndentedString(expiryTime)).append("\n");
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

