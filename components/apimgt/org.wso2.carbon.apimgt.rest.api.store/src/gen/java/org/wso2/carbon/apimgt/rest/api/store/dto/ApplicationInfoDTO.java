package org.wso2.carbon.apimgt.rest.api.store.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * ApplicationInfoDTO
 */
public class ApplicationInfoDTO   {
  @SerializedName("applicationId")
  private String applicationId = null;

  @SerializedName("name")
  private String name = null;

  @SerializedName("subscriber")
  private String subscriber = null;

  @SerializedName("throttlingTier")
  private String throttlingTier = null;

  @SerializedName("description")
  private String description = null;

  @SerializedName("lifeCycleStatus")
  private String lifeCycleStatus = null;

  public ApplicationInfoDTO applicationId(String applicationId) {
    this.applicationId = applicationId;
    return this;
  }

   /**
   * Get applicationId
   * @return applicationId
  **/
  @ApiModelProperty(value = "")
  public String getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  public ApplicationInfoDTO name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Get name
   * @return name
  **/
  @ApiModelProperty(value = "")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ApplicationInfoDTO subscriber(String subscriber) {
    this.subscriber = subscriber;
    return this;
  }

   /**
   * Get subscriber
   * @return subscriber
  **/
  @ApiModelProperty(value = "")
  public String getSubscriber() {
    return subscriber;
  }

  public void setSubscriber(String subscriber) {
    this.subscriber = subscriber;
  }

  public ApplicationInfoDTO throttlingTier(String throttlingTier) {
    this.throttlingTier = throttlingTier;
    return this;
  }

   /**
   * Get throttlingTier
   * @return throttlingTier
  **/
  @ApiModelProperty(value = "")
  public String getThrottlingTier() {
    return throttlingTier;
  }

  public void setThrottlingTier(String throttlingTier) {
    this.throttlingTier = throttlingTier;
  }

  public ApplicationInfoDTO description(String description) {
    this.description = description;
    return this;
  }

   /**
   * Get description
   * @return description
  **/
  @ApiModelProperty(value = "")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public ApplicationInfoDTO lifeCycleStatus(String lifeCycleStatus) {
    this.lifeCycleStatus = lifeCycleStatus;
    return this;
  }

   /**
   * Get lifeCycleStatus
   * @return lifeCycleStatus
  **/
  @ApiModelProperty(value = "")
  public String getLifeCycleStatus() {
    return lifeCycleStatus;
  }

  public void setLifeCycleStatus(String lifeCycleStatus) {
    this.lifeCycleStatus = lifeCycleStatus;
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
    return Objects.equals(this.applicationId, applicationInfo.applicationId) &&
        Objects.equals(this.name, applicationInfo.name) &&
        Objects.equals(this.subscriber, applicationInfo.subscriber) &&
        Objects.equals(this.throttlingTier, applicationInfo.throttlingTier) &&
        Objects.equals(this.description, applicationInfo.description) &&
        Objects.equals(this.lifeCycleStatus, applicationInfo.lifeCycleStatus);
  }

  @Override
  public int hashCode() {
    return Objects.hash(applicationId, name, subscriber, throttlingTier, description, lifeCycleStatus);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationInfoDTO {\n");
    
    sb.append("    applicationId: ").append(toIndentedString(applicationId)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    subscriber: ").append(toIndentedString(subscriber)).append("\n");
    sb.append("    throttlingTier: ").append(toIndentedString(throttlingTier)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    lifeCycleStatus: ").append(toIndentedString(lifeCycleStatus)).append("\n");
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

