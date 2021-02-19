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
  
    private String apiUUID = null;
    private String apiContext = null;
    private String apiVersion = null;
    private String tenantDomain = null;
    private Integer tenantId = null;
    private String appID = null;
    private String callbackURL = null;
    private String topicName = null;
    private String secret = null;
    private Long expiryTime = null;
    private String tier = null;
    private String applicationTier = null;
    private String apiTier = null;
    private String subscriberName = null;

  /**
   * The API UUID
   **/
  public WebhooksSubscriptionDTO apiUUID(String apiUUID) {
    this.apiUUID = apiUUID;
    return this;
  }

  
  @ApiModelProperty(value = "The API UUID")
  @JsonProperty("apiUUID")
  public String getApiUUID() {
    return apiUUID;
  }
  public void setApiUUID(String apiUUID) {
    this.apiUUID = apiUUID;
  }

  /**
   * The API context.
   **/
  public WebhooksSubscriptionDTO apiContext(String apiContext) {
    this.apiContext = apiContext;
    return this;
  }

  
  @ApiModelProperty(value = "The API context.")
  @JsonProperty("apiContext")
  public String getApiContext() {
    return apiContext;
  }
  public void setApiContext(String apiContext) {
    this.apiContext = apiContext;
  }

  /**
   * The API version.
   **/
  public WebhooksSubscriptionDTO apiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
    return this;
  }

  
  @ApiModelProperty(value = "The API version.")
  @JsonProperty("apiVersion")
  public String getApiVersion() {
    return apiVersion;
  }
  public void setApiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
  }

  /**
   * The tenant domain.
   **/
  public WebhooksSubscriptionDTO tenantDomain(String tenantDomain) {
    this.tenantDomain = tenantDomain;
    return this;
  }

  
  @ApiModelProperty(value = "The tenant domain.")
  @JsonProperty("tenantDomain")
  public String getTenantDomain() {
    return tenantDomain;
  }
  public void setTenantDomain(String tenantDomain) {
    this.tenantDomain = tenantDomain;
  }

  /**
   * The tenant id.
   **/
  public WebhooksSubscriptionDTO tenantId(Integer tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  
  @ApiModelProperty(value = "The tenant id.")
  @JsonProperty("tenantId")
  public Integer getTenantId() {
    return tenantId;
  }
  public void setTenantId(Integer tenantId) {
    this.tenantId = tenantId;
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

  /**
   * the subscription tier.
   **/
  public WebhooksSubscriptionDTO tier(String tier) {
    this.tier = tier;
    return this;
  }

  
  @ApiModelProperty(value = "the subscription tier.")
  @JsonProperty("tier")
  public String getTier() {
    return tier;
  }
  public void setTier(String tier) {
    this.tier = tier;
  }

  /**
   * the application tier.
   **/
  public WebhooksSubscriptionDTO applicationTier(String applicationTier) {
    this.applicationTier = applicationTier;
    return this;
  }

  
  @ApiModelProperty(value = "the application tier.")
  @JsonProperty("applicationTier")
  public String getApplicationTier() {
    return applicationTier;
  }
  public void setApplicationTier(String applicationTier) {
    this.applicationTier = applicationTier;
  }

  /**
   * the API tier.
   **/
  public WebhooksSubscriptionDTO apiTier(String apiTier) {
    this.apiTier = apiTier;
    return this;
  }

  
  @ApiModelProperty(value = "the API tier.")
  @JsonProperty("apiTier")
  public String getApiTier() {
    return apiTier;
  }
  public void setApiTier(String apiTier) {
    this.apiTier = apiTier;
  }

  /**
   * the subscriber name.
   **/
  public WebhooksSubscriptionDTO subscriberName(String subscriberName) {
    this.subscriberName = subscriberName;
    return this;
  }

  
  @ApiModelProperty(value = "the subscriber name.")
  @JsonProperty("subscriberName")
  public String getSubscriberName() {
    return subscriberName;
  }
  public void setSubscriberName(String subscriberName) {
    this.subscriberName = subscriberName;
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
    return Objects.equals(apiUUID, webhooksSubscription.apiUUID) &&
        Objects.equals(apiContext, webhooksSubscription.apiContext) &&
        Objects.equals(apiVersion, webhooksSubscription.apiVersion) &&
        Objects.equals(tenantDomain, webhooksSubscription.tenantDomain) &&
        Objects.equals(tenantId, webhooksSubscription.tenantId) &&
        Objects.equals(appID, webhooksSubscription.appID) &&
        Objects.equals(callbackURL, webhooksSubscription.callbackURL) &&
        Objects.equals(topicName, webhooksSubscription.topicName) &&
        Objects.equals(secret, webhooksSubscription.secret) &&
        Objects.equals(expiryTime, webhooksSubscription.expiryTime) &&
        Objects.equals(tier, webhooksSubscription.tier) &&
        Objects.equals(applicationTier, webhooksSubscription.applicationTier) &&
        Objects.equals(apiTier, webhooksSubscription.apiTier) &&
        Objects.equals(subscriberName, webhooksSubscription.subscriberName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apiUUID, apiContext, apiVersion, tenantDomain, tenantId, appID, callbackURL, topicName, secret, expiryTime, tier, applicationTier, apiTier, subscriberName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class WebhooksSubscriptionDTO {\n");
    
    sb.append("    apiUUID: ").append(toIndentedString(apiUUID)).append("\n");
    sb.append("    apiContext: ").append(toIndentedString(apiContext)).append("\n");
    sb.append("    apiVersion: ").append(toIndentedString(apiVersion)).append("\n");
    sb.append("    tenantDomain: ").append(toIndentedString(tenantDomain)).append("\n");
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
    sb.append("    appID: ").append(toIndentedString(appID)).append("\n");
    sb.append("    callbackURL: ").append(toIndentedString(callbackURL)).append("\n");
    sb.append("    topicName: ").append(toIndentedString(topicName)).append("\n");
    sb.append("    secret: ").append(toIndentedString(secret)).append("\n");
    sb.append("    expiryTime: ").append(toIndentedString(expiryTime)).append("\n");
    sb.append("    tier: ").append(toIndentedString(tier)).append("\n");
    sb.append("    applicationTier: ").append(toIndentedString(applicationTier)).append("\n");
    sb.append("    apiTier: ").append(toIndentedString(apiTier)).append("\n");
    sb.append("    subscriberName: ").append(toIndentedString(subscriberName)).append("\n");
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

