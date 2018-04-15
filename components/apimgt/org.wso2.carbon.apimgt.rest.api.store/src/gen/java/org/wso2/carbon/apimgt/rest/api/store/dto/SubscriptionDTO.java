package org.wso2.carbon.apimgt.rest.api.store.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * SubscriptionDTO
 */
public class SubscriptionDTO   {
  @SerializedName("subscriptionId")
  private String subscriptionId = null;

  @SerializedName("applicationId")
  private String applicationId = null;

  @SerializedName("apiIdentifier")
  private String apiIdentifier = null;

  @SerializedName("apiName")
  private String apiName = null;

  @SerializedName("apiVersion")
  private String apiVersion = null;

  @SerializedName("policy")
  private String policy = null;

  /**
   * Gets or Sets lifeCycleStatus
   */
  public enum LifeCycleStatusEnum {
    @SerializedName("BLOCKED")
    BLOCKED("BLOCKED"),
    
    @SerializedName("PROD_ONLY_BLOCKED")
    PROD_ONLY_BLOCKED("PROD_ONLY_BLOCKED"),
    
    @SerializedName("ACTIVE")
    ACTIVE("ACTIVE"),
    
    @SerializedName("ON_HOLD")
    ON_HOLD("ON_HOLD"),
    
    @SerializedName("REJECTED")
    REJECTED("REJECTED");

    private String value;

    LifeCycleStatusEnum(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
    public static LifeCycleStatusEnum fromValue(String text) {
      for (LifeCycleStatusEnum b : LifeCycleStatusEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @SerializedName("lifeCycleStatus")
  private LifeCycleStatusEnum lifeCycleStatus = null;

  public SubscriptionDTO subscriptionId(String subscriptionId) {
    this.subscriptionId = subscriptionId;
    return this;
  }

   /**
   * Get subscriptionId
   * @return subscriptionId
  **/
  @ApiModelProperty(value = "")
  public String getSubscriptionId() {
    return subscriptionId;
  }

  public void setSubscriptionId(String subscriptionId) {
    this.subscriptionId = subscriptionId;
  }

  public SubscriptionDTO applicationId(String applicationId) {
    this.applicationId = applicationId;
    return this;
  }

   /**
   * Get applicationId
   * @return applicationId
  **/
  @ApiModelProperty(required = true, value = "")
  public String getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  public SubscriptionDTO apiIdentifier(String apiIdentifier) {
    this.apiIdentifier = apiIdentifier;
    return this;
  }

   /**
   * Get apiIdentifier
   * @return apiIdentifier
  **/
  @ApiModelProperty(required = true, value = "")
  public String getApiIdentifier() {
    return apiIdentifier;
  }

  public void setApiIdentifier(String apiIdentifier) {
    this.apiIdentifier = apiIdentifier;
  }

  public SubscriptionDTO apiName(String apiName) {
    this.apiName = apiName;
    return this;
  }

   /**
   * Get apiName
   * @return apiName
  **/
  @ApiModelProperty(required = true, value = "")
  public String getApiName() {
    return apiName;
  }

  public void setApiName(String apiName) {
    this.apiName = apiName;
  }

  public SubscriptionDTO apiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
    return this;
  }

   /**
   * Get apiVersion
   * @return apiVersion
  **/
  @ApiModelProperty(required = true, value = "")
  public String getApiVersion() {
    return apiVersion;
  }

  public void setApiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
  }

  public SubscriptionDTO policy(String policy) {
    this.policy = policy;
    return this;
  }

   /**
   * Get policy
   * @return policy
  **/
  @ApiModelProperty(required = true, value = "")
  public String getPolicy() {
    return policy;
  }

  public void setPolicy(String policy) {
    this.policy = policy;
  }

  public SubscriptionDTO lifeCycleStatus(LifeCycleStatusEnum lifeCycleStatus) {
    this.lifeCycleStatus = lifeCycleStatus;
    return this;
  }

   /**
   * Get lifeCycleStatus
   * @return lifeCycleStatus
  **/
  @ApiModelProperty(value = "")
  public LifeCycleStatusEnum getLifeCycleStatus() {
    return lifeCycleStatus;
  }

  public void setLifeCycleStatus(LifeCycleStatusEnum lifeCycleStatus) {
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
    SubscriptionDTO subscription = (SubscriptionDTO) o;
    return Objects.equals(this.subscriptionId, subscription.subscriptionId) &&
        Objects.equals(this.applicationId, subscription.applicationId) &&
        Objects.equals(this.apiIdentifier, subscription.apiIdentifier) &&
        Objects.equals(this.apiName, subscription.apiName) &&
        Objects.equals(this.apiVersion, subscription.apiVersion) &&
        Objects.equals(this.policy, subscription.policy) &&
        Objects.equals(this.lifeCycleStatus, subscription.lifeCycleStatus);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subscriptionId, applicationId, apiIdentifier, apiName, apiVersion, policy, lifeCycleStatus);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubscriptionDTO {\n");
    
    sb.append("    subscriptionId: ").append(toIndentedString(subscriptionId)).append("\n");
    sb.append("    applicationId: ").append(toIndentedString(applicationId)).append("\n");
    sb.append("    apiIdentifier: ").append(toIndentedString(apiIdentifier)).append("\n");
    sb.append("    apiName: ").append(toIndentedString(apiName)).append("\n");
    sb.append("    apiVersion: ").append(toIndentedString(apiVersion)).append("\n");
    sb.append("    policy: ").append(toIndentedString(policy)).append("\n");
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

