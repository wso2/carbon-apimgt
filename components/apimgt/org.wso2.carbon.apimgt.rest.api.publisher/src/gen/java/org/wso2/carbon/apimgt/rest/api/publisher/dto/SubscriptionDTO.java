package org.wso2.carbon.apimgt.rest.api.publisher.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * SubscriptionDTO
 */
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-03-29T11:00:00.292+05:30")
public class SubscriptionDTO   {
  @JsonProperty("subscriptionId")
  private String subscriptionId = null;

  @JsonProperty("applicationInfo")
  private ApplicationDTO applicationInfo = null;

  @JsonProperty("subscriptionTier")
  private String subscriptionTier = null;

  /**
   * Gets or Sets subscriptionStatus
   */
  public enum SubscriptionStatusEnum {
    BLOCKED("BLOCKED"),
    
    PROD_ONLY_BLOCKED("PROD_ONLY_BLOCKED"),
    
    SANDBOX_ONLY_BLOCKED("SANDBOX_ONLY_BLOCKED"),
    
    ACTIVE("ACTIVE"),
    
    ON_HOLD("ON_HOLD"),
    
    REJECTED("REJECTED");

    private String value;

    SubscriptionStatusEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static SubscriptionStatusEnum fromValue(String text) {
      for (SubscriptionStatusEnum b : SubscriptionStatusEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("subscriptionStatus")
  private SubscriptionStatusEnum subscriptionStatus = null;

  public SubscriptionDTO subscriptionId(String subscriptionId) {
    this.subscriptionId = subscriptionId;
    return this;
  }

   /**
   * Get subscriptionId
   * @return subscriptionId
  **/
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", required = true, value = "")
  public String getSubscriptionId() {
    return subscriptionId;
  }

  public void setSubscriptionId(String subscriptionId) {
    this.subscriptionId = subscriptionId;
  }

  public SubscriptionDTO applicationInfo(ApplicationDTO applicationInfo) {
    this.applicationInfo = applicationInfo;
    return this;
  }

   /**
   * Get applicationInfo
   * @return applicationInfo
  **/
  @ApiModelProperty(required = true, value = "")
  public ApplicationDTO getApplicationInfo() {
    return applicationInfo;
  }

  public void setApplicationInfo(ApplicationDTO applicationInfo) {
    this.applicationInfo = applicationInfo;
  }

  public SubscriptionDTO subscriptionTier(String subscriptionTier) {
    this.subscriptionTier = subscriptionTier;
    return this;
  }

   /**
   * Get subscriptionTier
   * @return subscriptionTier
  **/
  @ApiModelProperty(example = "Unlimited", required = true, value = "")
  public String getSubscriptionTier() {
    return subscriptionTier;
  }

  public void setSubscriptionTier(String subscriptionTier) {
    this.subscriptionTier = subscriptionTier;
  }

  public SubscriptionDTO subscriptionStatus(SubscriptionStatusEnum subscriptionStatus) {
    this.subscriptionStatus = subscriptionStatus;
    return this;
  }

   /**
   * Get subscriptionStatus
   * @return subscriptionStatus
  **/
  @ApiModelProperty(example = "BLOCKED", value = "")
  public SubscriptionStatusEnum getSubscriptionStatus() {
    return subscriptionStatus;
  }

  public void setSubscriptionStatus(SubscriptionStatusEnum subscriptionStatus) {
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
    SubscriptionDTO subscription = (SubscriptionDTO) o;
    return Objects.equals(this.subscriptionId, subscription.subscriptionId) &&
        Objects.equals(this.applicationInfo, subscription.applicationInfo) &&
        Objects.equals(this.subscriptionTier, subscription.subscriptionTier) &&
        Objects.equals(this.subscriptionStatus, subscription.subscriptionStatus);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subscriptionId, applicationInfo, subscriptionTier, subscriptionStatus);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubscriptionDTO {\n");
    
    sb.append("    subscriptionId: ").append(toIndentedString(subscriptionId)).append("\n");
    sb.append("    applicationInfo: ").append(toIndentedString(applicationInfo)).append("\n");
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

