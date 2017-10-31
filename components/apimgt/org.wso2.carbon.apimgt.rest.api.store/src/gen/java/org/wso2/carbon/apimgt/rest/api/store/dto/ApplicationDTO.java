package org.wso2.carbon.apimgt.rest.api.store.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationKeysDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationTokenDTO;
import java.util.Objects;

<<<<<<< HEAD
import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;
=======
/**
 * ApplicationDTO
 */
public class ApplicationDTO   {
  @JsonProperty("applicationId")
  private String applicationId = null;
>>>>>>> upstream/master

  @JsonProperty("name")
  private String name = null;

  @JsonProperty("subscriber")
  private String subscriber = null;

  @JsonProperty("throttlingTier")
  private String throttlingTier = null;

  @JsonProperty("permission")
  private String permission = null;

  @JsonProperty("description")
  private String description = null;

<<<<<<< HEAD
  private String lastUpdatedTime = null;
=======
  @JsonProperty("lifeCycleStatus")
  private String lifeCycleStatus = null;

  @JsonProperty("token")
  private ApplicationTokenDTO token = null;
>>>>>>> upstream/master

  @JsonProperty("keys")
  private List<ApplicationKeysDTO> keys = new ArrayList<ApplicationKeysDTO>();

  public ApplicationDTO applicationId(String applicationId) {
    this.applicationId = applicationId;
    return this;
  }

   /**
   * Get applicationId
   * @return applicationId
  **/
<<<<<<< HEAD

  @JsonIgnore
  public String getCreatedTime(){
    return createdTime;
  }
  public void setCreatedTime(String createdTime){
    this.createdTime=createdTime;
  }

  
  /**
   **/
=======
>>>>>>> upstream/master
  @ApiModelProperty(value = "")
  public String getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

<<<<<<< HEAD
  
  /**
   **/
=======
  public ApplicationDTO name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Get name
   * @return name
  **/
>>>>>>> upstream/master
  @ApiModelProperty(required = true, value = "")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

<<<<<<< HEAD
  
  /**
   * If subscriber is not given user invoking the API will be taken as the subscriber.\n
   **/
  @ApiModelProperty(value = "If subscriber is not given user invoking the API will be taken as the subscriber.\n")
  @JsonProperty("subscriber")
=======
  public ApplicationDTO subscriber(String subscriber) {
    this.subscriber = subscriber;
    return this;
  }

   /**
   * If subscriber is not given user invoking the API will be taken as the subscriber. 
   * @return subscriber
  **/
  @ApiModelProperty(value = "If subscriber is not given user invoking the API will be taken as the subscriber. ")
>>>>>>> upstream/master
  public String getSubscriber() {
    return subscriber;
  }

  public void setSubscriber(String subscriber) {
    this.subscriber = subscriber;
  }

<<<<<<< HEAD
  
  /**
   **/
=======
  public ApplicationDTO throttlingTier(String throttlingTier) {
    this.throttlingTier = throttlingTier;
    return this;
  }

   /**
   * Get throttlingTier
   * @return throttlingTier
  **/
>>>>>>> upstream/master
  @ApiModelProperty(required = true, value = "")
  public String getThrottlingTier() {
    return throttlingTier;
  }

  public void setThrottlingTier(String throttlingTier) {
    this.throttlingTier = throttlingTier;
  }

<<<<<<< HEAD
  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("callbackUrl")
  public String getCallbackUrl() {
    return callbackUrl;
=======
  public ApplicationDTO permission(String permission) {
    this.permission = permission;
    return this;
>>>>>>> upstream/master
  }

   /**
   * Get permission
   * @return permission
  **/
  @ApiModelProperty(example = "[{&quot;groupId&quot; : 1000, &quot;permission&quot; : [&quot;READ&quot;,&quot;UPDATE&quot;]},{&quot;groupId&quot; : 1001, &quot;permission&quot; : [&quot;READ&quot;,&quot;UPDATE&quot;]}]", value = "")
  public String getPermission() {
    return permission;
  }

  public void setPermission(String permission) {
    this.permission = permission;
  }

<<<<<<< HEAD
  
  /**
   **/
=======
  public ApplicationDTO description(String description) {
    this.description = description;
    return this;
  }

   /**
   * Get description
   * @return description
  **/
>>>>>>> upstream/master
  @ApiModelProperty(value = "")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

<<<<<<< HEAD
  
  /**
   **/
=======
  public ApplicationDTO lifeCycleStatus(String lifeCycleStatus) {
    this.lifeCycleStatus = lifeCycleStatus;
    return this;
  }

   /**
   * Get lifeCycleStatus
   * @return lifeCycleStatus
  **/
>>>>>>> upstream/master
  @ApiModelProperty(value = "")
  public String getLifeCycleStatus() {
    return lifeCycleStatus;
  }

  public void setLifeCycleStatus(String lifeCycleStatus) {
    this.lifeCycleStatus = lifeCycleStatus;
  }

  public ApplicationDTO token(ApplicationTokenDTO token) {
    this.token = token;
    return this;
  }

<<<<<<< HEAD
  
  /**
   **/
=======
   /**
   * Get token
   * @return token
  **/
>>>>>>> upstream/master
  @ApiModelProperty(value = "")
  public ApplicationTokenDTO getToken() {
    return token;
  }

  public void setToken(ApplicationTokenDTO token) {
    this.token = token;
  }

  public ApplicationDTO keys(List<ApplicationKeysDTO> keys) {
    this.keys = keys;
    return this;
  }

  public ApplicationDTO addKeysItem(ApplicationKeysDTO keysItem) {
    this.keys.add(keysItem);
    return this;
  }

<<<<<<< HEAD
  
  /**
   **/
=======
   /**
   * Get keys
   * @return keys
  **/
>>>>>>> upstream/master
  @ApiModelProperty(value = "")
  public List<ApplicationKeysDTO> getKeys() {
    return keys;
  }

  public void setKeys(List<ApplicationKeysDTO> keys) {
    this.keys = keys;
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
    return Objects.equals(this.applicationId, application.applicationId) &&
        Objects.equals(this.name, application.name) &&
        Objects.equals(this.subscriber, application.subscriber) &&
        Objects.equals(this.throttlingTier, application.throttlingTier) &&
        Objects.equals(this.permission, application.permission) &&
        Objects.equals(this.description, application.description) &&
        Objects.equals(this.lifeCycleStatus, application.lifeCycleStatus) &&
        Objects.equals(this.token, application.token) &&
        Objects.equals(this.keys, application.keys);
  }

  @Override
  public int hashCode() {
    return Objects.hash(applicationId, name, subscriber, throttlingTier, permission, description, lifeCycleStatus, token, keys);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationDTO {\n");
    
    sb.append("    applicationId: ").append(toIndentedString(applicationId)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    subscriber: ").append(toIndentedString(subscriber)).append("\n");
    sb.append("    throttlingTier: ").append(toIndentedString(throttlingTier)).append("\n");
    sb.append("    permission: ").append(toIndentedString(permission)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    lifeCycleStatus: ").append(toIndentedString(lifeCycleStatus)).append("\n");
    sb.append("    token: ").append(toIndentedString(token)).append("\n");
    sb.append("    keys: ").append(toIndentedString(keys)).append("\n");
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

