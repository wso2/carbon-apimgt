package org.wso2.carbon.apimgt.rest.api.store.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * SubscriptionDTO
 */
@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-04T10:24:30.459+05:30")
public class SubscriptionDTO   {
  @JsonProperty("subscriptionId")
  private String subscriptionId = null;

  @JsonProperty("applicationId")
  private String applicationId = null;

  @JsonProperty("apiIdentifier")
  private String apiIdentifier = null;

  @JsonProperty("tier")
  private String tier = null;

  /**
   * Gets or Sets status
   */
  public enum StatusEnum {
    BLOCKED("BLOCKED"),
    
    PROD_ONLY_BLOCKED("PROD_ONLY_BLOCKED"),

    ACTIVE("ACTIVE"),
    
    ON_HOLD("ON_HOLD"),
    
    REJECTED("REJECTED");

    private String value;

    StatusEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static StatusEnum fromValue(String text) {
      for (StatusEnum b : StatusEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("status")
  private StatusEnum status = null;

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

  public SubscriptionDTO tier(String tier) {
    this.tier = tier;
    return this;
  }

   /**
   * Get tier
   * @return tier
  **/
  @ApiModelProperty(required = true, value = "")
  public String getTier() {
    return tier;
  }

  public void setTier(String tier) {
    this.tier = tier;
  }

  public SubscriptionDTO status(StatusEnum status) {
    this.status = status;
    return this;
  }

   /**
   * Get status
   * @return status
  **/
  @ApiModelProperty(value = "")
  public StatusEnum getStatus() {
    return status;
  }

  public void setStatus(StatusEnum status) {
    this.status = status;
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
        Objects.equals(this.tier, subscription.tier) &&
        Objects.equals(this.status, subscription.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subscriptionId, applicationId, apiIdentifier, tier, status);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubscriptionDTO {\n");
    
    sb.append("    subscriptionId: ").append(toIndentedString(subscriptionId)).append("\n");
    sb.append("    applicationId: ").append(toIndentedString(applicationId)).append("\n");
    sb.append("    apiIdentifier: ").append(toIndentedString(apiIdentifier)).append("\n");
    sb.append("    tier: ").append(toIndentedString(tier)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
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

