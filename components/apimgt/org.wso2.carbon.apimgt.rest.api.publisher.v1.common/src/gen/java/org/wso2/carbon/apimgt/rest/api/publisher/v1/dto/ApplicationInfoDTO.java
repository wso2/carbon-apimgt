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



public class ApplicationInfoDTO   {
  
    private String applicationId = null;
    private String name = null;
    private String subscriber = null;
    private String description = null;
    private Integer subscriptionCount = null;

  /**
   **/
  public ApplicationInfoDTO applicationId(String applicationId) {
    this.applicationId = applicationId;
    return this;
  }

  
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "")
  @JsonProperty("applicationId")
  public String getApplicationId() {
    return applicationId;
  }
  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  /**
   **/
  public ApplicationInfoDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "CalculatorApp", value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public ApplicationInfoDTO subscriber(String subscriber) {
    this.subscriber = subscriber;
    return this;
  }

  
  @ApiModelProperty(example = "admin", value = "")
  @JsonProperty("subscriber")
  public String getSubscriber() {
    return subscriber;
  }
  public void setSubscriber(String subscriber) {
    this.subscriber = subscriber;
  }

  /**
   **/
  public ApplicationInfoDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "Sample calculator application", value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   **/
  public ApplicationInfoDTO subscriptionCount(Integer subscriptionCount) {
    this.subscriptionCount = subscriptionCount;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("subscriptionCount")
  public Integer getSubscriptionCount() {
    return subscriptionCount;
  }
  public void setSubscriptionCount(Integer subscriptionCount) {
    this.subscriptionCount = subscriptionCount;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApplicationInfoDTO applicationInfo = (ApplicationInfoDTO) o;
    return Objects.equals(applicationId, applicationInfo.applicationId) &&
        Objects.equals(name, applicationInfo.name) &&
        Objects.equals(subscriber, applicationInfo.subscriber) &&
        Objects.equals(description, applicationInfo.description) &&
        Objects.equals(subscriptionCount, applicationInfo.subscriptionCount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(applicationId, name, subscriber, description, subscriptionCount);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationInfoDTO {\n");
    
    sb.append("    applicationId: ").append(toIndentedString(applicationId)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    subscriber: ").append(toIndentedString(subscriber)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    subscriptionCount: ").append(toIndentedString(subscriptionCount)).append("\n");
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

