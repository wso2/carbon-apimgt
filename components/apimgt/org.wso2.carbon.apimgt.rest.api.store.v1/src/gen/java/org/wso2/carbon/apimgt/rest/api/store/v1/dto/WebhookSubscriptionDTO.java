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



public class WebhookSubscriptionDTO   {
  
    private String apiId = null;
    private String appId = null;
    private String topic = null;
    private String callBackUrl = null;
    private String deliveryTime = null;
    private Integer deliveryStatus = null;

  /**
   **/
  public WebhookSubscriptionDTO apiId(String apiId) {
    this.apiId = apiId;
    return this;
  }

  
  @ApiModelProperty(example = "faae5fcc-cbae-40c4-bf43-89931630d313", value = "")
  @JsonProperty("apiId")
  public String getApiId() {
    return apiId;
  }
  public void setApiId(String apiId) {
    this.apiId = apiId;
  }

  /**
   **/
  public WebhookSubscriptionDTO appId(String appId) {
    this.appId = appId;
    return this;
  }

  
  @ApiModelProperty(example = "faae5fcc-cbae-40c4-bf43-89931630d313", value = "")
  @JsonProperty("appId")
  public String getAppId() {
    return appId;
  }
  public void setAppId(String appId) {
    this.appId = appId;
  }

  /**
   **/
  public WebhookSubscriptionDTO topic(String topic) {
    this.topic = topic;
    return this;
  }

  
  @ApiModelProperty(example = "orderBooks", value = "")
  @JsonProperty("topic")
  public String getTopic() {
    return topic;
  }
  public void setTopic(String topic) {
    this.topic = topic;
  }

  /**
   **/
  public WebhookSubscriptionDTO callBackUrl(String callBackUrl) {
    this.callBackUrl = callBackUrl;
    return this;
  }

  
  @ApiModelProperty(example = "www.orderbooksite.com", value = "")
  @JsonProperty("callBackUrl")
  public String getCallBackUrl() {
    return callBackUrl;
  }
  public void setCallBackUrl(String callBackUrl) {
    this.callBackUrl = callBackUrl;
  }

  /**
   **/
  public WebhookSubscriptionDTO deliveryTime(String deliveryTime) {
    this.deliveryTime = deliveryTime;
    return this;
  }

  
  @ApiModelProperty(example = "faae5fcc-cbae-40c4-bf43-89931630d313", value = "")
  @JsonProperty("deliveryTime")
  public String getDeliveryTime() {
    return deliveryTime;
  }
  public void setDeliveryTime(String deliveryTime) {
    this.deliveryTime = deliveryTime;
  }

  /**
   **/
  public WebhookSubscriptionDTO deliveryStatus(Integer deliveryStatus) {
    this.deliveryStatus = deliveryStatus;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "")
  @JsonProperty("deliveryStatus")
  public Integer getDeliveryStatus() {
    return deliveryStatus;
  }
  public void setDeliveryStatus(Integer deliveryStatus) {
    this.deliveryStatus = deliveryStatus;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WebhookSubscriptionDTO webhookSubscription = (WebhookSubscriptionDTO) o;
    return Objects.equals(apiId, webhookSubscription.apiId) &&
        Objects.equals(appId, webhookSubscription.appId) &&
        Objects.equals(topic, webhookSubscription.topic) &&
        Objects.equals(callBackUrl, webhookSubscription.callBackUrl) &&
        Objects.equals(deliveryTime, webhookSubscription.deliveryTime) &&
        Objects.equals(deliveryStatus, webhookSubscription.deliveryStatus);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apiId, appId, topic, callBackUrl, deliveryTime, deliveryStatus);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class WebhookSubscriptionDTO {\n");
    
    sb.append("    apiId: ").append(toIndentedString(apiId)).append("\n");
    sb.append("    appId: ").append(toIndentedString(appId)).append("\n");
    sb.append("    topic: ").append(toIndentedString(topic)).append("\n");
    sb.append("    callBackUrl: ").append(toIndentedString(callBackUrl)).append("\n");
    sb.append("    deliveryTime: ").append(toIndentedString(deliveryTime)).append("\n");
    sb.append("    deliveryStatus: ").append(toIndentedString(deliveryStatus)).append("\n");
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

