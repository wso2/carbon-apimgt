package org.wso2.carbon.apimgt.rest.api.core.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * ApplicationDTO
 */
public class ApplicationDTO   {
  @SerializedName("applicationId")
  private String applicationId = null;

  @SerializedName("name")
  private String name = null;

  @SerializedName("subscriber")
  private String subscriber = null;

  @SerializedName("throttlingTier")
  private String throttlingTier = null;

  public ApplicationDTO applicationId(String applicationId) {
    this.applicationId = applicationId;
    return this;
  }

   /**
   * Get applicationId
   * @return applicationId
  **/
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "")
  public String getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  public ApplicationDTO name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Get name
   * @return name
  **/
  @ApiModelProperty(example = "CalculatorApp", required = true, value = "")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ApplicationDTO subscriber(String subscriber) {
    this.subscriber = subscriber;
    return this;
  }

   /**
   * Get subscriber
   * @return subscriber
  **/
  @ApiModelProperty(example = "admin", value = "")
  public String getSubscriber() {
    return subscriber;
  }

  public void setSubscriber(String subscriber) {
    this.subscriber = subscriber;
  }

  public ApplicationDTO throttlingTier(String throttlingTier) {
    this.throttlingTier = throttlingTier;
    return this;
  }

   /**
   * Get throttlingTier
   * @return throttlingTier
  **/
  @ApiModelProperty(example = "Unlimited", required = true, value = "")
  public String getThrottlingTier() {
    return throttlingTier;
  }

  public void setThrottlingTier(String throttlingTier) {
    this.throttlingTier = throttlingTier;
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
        Objects.equals(this.throttlingTier, application.throttlingTier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(applicationId, name, subscriber, throttlingTier);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationDTO {\n");
    
    sb.append("    applicationId: ").append(toIndentedString(applicationId)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    subscriber: ").append(toIndentedString(subscriber)).append("\n");
    sb.append("    throttlingTier: ").append(toIndentedString(throttlingTier)).append("\n");
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

