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



public class ApplicationDTO   {
  
    private String applicationId = null;
    private String name = null;
    private String subscriber = null;
    private String throttlingTier = null;
    private String description = null;
    private String groupId = null;

  /**
   **/
  public ApplicationDTO applicationId(String applicationId) {
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
  public ApplicationDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "CalculatorApp", required = true, value = "")
  @JsonProperty("name")
  @NotNull
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public ApplicationDTO subscriber(String subscriber) {
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
  public ApplicationDTO throttlingTier(String throttlingTier) {
    this.throttlingTier = throttlingTier;
    return this;
  }

  
  @ApiModelProperty(example = "Unlimited", required = true, value = "")
  @JsonProperty("throttlingTier")
  @NotNull
  public String getThrottlingTier() {
    return throttlingTier;
  }
  public void setThrottlingTier(String throttlingTier) {
    this.throttlingTier = throttlingTier;
  }

  /**
   **/
  public ApplicationDTO description(String description) {
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
  public ApplicationDTO groupId(String groupId) {
    this.groupId = groupId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("groupId")
  public String getGroupId() {
    return groupId;
  }
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApplicationDTO application = (ApplicationDTO) o;
    return Objects.equals(applicationId, application.applicationId) &&
        Objects.equals(name, application.name) &&
        Objects.equals(subscriber, application.subscriber) &&
        Objects.equals(throttlingTier, application.throttlingTier) &&
        Objects.equals(description, application.description) &&
        Objects.equals(groupId, application.groupId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(applicationId, name, subscriber, throttlingTier, description, groupId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationDTO {\n");
    
    sb.append("    applicationId: ").append(toIndentedString(applicationId)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    subscriber: ").append(toIndentedString(subscriber)).append("\n");
    sb.append("    throttlingTier: ").append(toIndentedString(throttlingTier)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    groupId: ").append(toIndentedString(groupId)).append("\n");
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

