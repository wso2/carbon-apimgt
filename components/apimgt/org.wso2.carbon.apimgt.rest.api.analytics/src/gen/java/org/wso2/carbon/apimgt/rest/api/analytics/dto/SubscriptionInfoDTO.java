package org.wso2.carbon.apimgt.rest.api.analytics.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * SubscriptionInfoDTO
 */
public class SubscriptionInfoDTO   {
  @SerializedName("id")
  private String id = null;

  @SerializedName("name")
  private String name = null;

  @SerializedName("version")
  private String version = null;

  @SerializedName("appName")
  private String appName = null;

  @SerializedName("description")
  private String description = null;

  @SerializedName("createdTime")
  private String createdTime = null;

  @SerializedName("subscriptionTier")
  private String subscriptionTier = null;

  @SerializedName("subscriptionStatus")
  private String subscriptionStatus = null;

  public SubscriptionInfoDTO id(String id) {
    this.id = id;
    return this;
  }

   /**
   * Get id
   * @return id
  **/
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public SubscriptionInfoDTO name(String name) {
    this.name = name;
    return this;
  }

   /**
   * API Name 
   * @return name
  **/
  @ApiModelProperty(example = "CalculatorAPI", value = "API Name ")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public SubscriptionInfoDTO version(String version) {
    this.version = version;
    return this;
  }

   /**
   * API version 
   * @return version
  **/
  @ApiModelProperty(example = "1.0.0", value = "API version ")
  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public SubscriptionInfoDTO appName(String appName) {
    this.appName = appName;
    return this;
  }

   /**
   * Name of the subscribed Application 
   * @return appName
  **/
  @ApiModelProperty(example = "Calculator Application", value = "Name of the subscribed Application ")
  public String getAppName() {
    return appName;
  }

  public void setAppName(String appName) {
    this.appName = appName;
  }

  public SubscriptionInfoDTO description(String description) {
    this.description = description;
    return this;
  }

   /**
   * Application desciprtion 
   * @return description
  **/
  @ApiModelProperty(example = "A calculator Application that supports basic operations", value = "Application desciprtion ")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public SubscriptionInfoDTO createdTime(String createdTime) {
    this.createdTime = createdTime;
    return this;
  }

   /**
   * Timestamp 
   * @return createdTime
  **/
  @ApiModelProperty(value = "Timestamp ")
  public String getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(String createdTime) {
    this.createdTime = createdTime;
  }

  public SubscriptionInfoDTO subscriptionTier(String subscriptionTier) {
    this.subscriptionTier = subscriptionTier;
    return this;
  }

   /**
   * Subscribed application tier 
   * @return subscriptionTier
  **/
  @ApiModelProperty(example = "Gold", value = "Subscribed application tier ")
  public String getSubscriptionTier() {
    return subscriptionTier;
  }

  public void setSubscriptionTier(String subscriptionTier) {
    this.subscriptionTier = subscriptionTier;
  }

  public SubscriptionInfoDTO subscriptionStatus(String subscriptionStatus) {
    this.subscriptionStatus = subscriptionStatus;
    return this;
  }

   /**
   * Subscription Status 
   * @return subscriptionStatus
  **/
  @ApiModelProperty(example = "ACTIVE", value = "Subscription Status ")
  public String getSubscriptionStatus() {
    return subscriptionStatus;
  }

  public void setSubscriptionStatus(String subscriptionStatus) {
    this.subscriptionStatus = subscriptionStatus;
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
    return Objects.equals(this.id, subscriptionInfo.id) &&
        Objects.equals(this.name, subscriptionInfo.name) &&
        Objects.equals(this.version, subscriptionInfo.version) &&
        Objects.equals(this.appName, subscriptionInfo.appName) &&
        Objects.equals(this.description, subscriptionInfo.description) &&
        Objects.equals(this.createdTime, subscriptionInfo.createdTime) &&
        Objects.equals(this.subscriptionTier, subscriptionInfo.subscriptionTier) &&
        Objects.equals(this.subscriptionStatus, subscriptionInfo.subscriptionStatus);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, version, appName, description, createdTime, subscriptionTier, subscriptionStatus);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubscriptionInfoDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    appName: ").append(toIndentedString(appName)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    createdTime: ").append(toIndentedString(createdTime)).append("\n");
    sb.append("    subscriptionTier: ").append(toIndentedString(subscriptionTier)).append("\n");
    sb.append("    subscriptionStatus: ").append(toIndentedString(subscriptionStatus)).append("\n");
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

