package org.wso2.carbon.apimgt.rest.api.core.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * SubscriptionValidationInfoDTO
 */
@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-12-08T17:11:20.964+05:30")
public class SubscriptionValidationInfoDTO   {
  @JsonProperty("isValid")
  private Boolean isValid = null;

  @JsonProperty("subscriptionStatus")
  private String subscriptionStatus = null;

  @JsonProperty("applicationId")
  private String applicationId = null;

  @JsonProperty("applicationName")
  private String applicationName = null;

  @JsonProperty("applicationOwner")
  private String applicationOwner = null;

  @JsonProperty("apiId")
  private String apiId = null;

  @JsonProperty("apiName")
  private String apiName = null;

  @JsonProperty("apiProvider")
  private String apiProvider = null;

  public SubscriptionValidationInfoDTO isValid(Boolean isValid) {
    this.isValid = isValid;
    return this;
  }

   /**
   * If a valid subscription is available or not. 
   * @return isValid
  **/
  @ApiModelProperty(required = true, value = "If a valid subscription is available or not. ")
  public Boolean getIsValid() {
    return isValid;
  }

  public void setIsValid(Boolean isValid) {
    this.isValid = isValid;
  }

  public SubscriptionValidationInfoDTO subscriptionStatus(String subscriptionStatus) {
    this.subscriptionStatus = subscriptionStatus;
    return this;
  }

   /**
   * Subscription Status. 
   * @return subscriptionStatus
  **/
  @ApiModelProperty(value = "Subscription Status. ")
  public String getSubscriptionStatus() {
    return subscriptionStatus;
  }

  public void setSubscriptionStatus(String subscriptionStatus) {
    this.subscriptionStatus = subscriptionStatus;
  }

  public SubscriptionValidationInfoDTO applicationId(String applicationId) {
    this.applicationId = applicationId;
    return this;
  }

   /**
   * UUID of application. 
   * @return applicationId
  **/
  @ApiModelProperty(value = "UUID of application. ")
  public String getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  public SubscriptionValidationInfoDTO applicationName(String applicationName) {
    this.applicationName = applicationName;
    return this;
  }

   /**
   * Name of application. 
   * @return applicationName
  **/
  @ApiModelProperty(value = "Name of application. ")
  public String getApplicationName() {
    return applicationName;
  }

  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }

  public SubscriptionValidationInfoDTO applicationOwner(String applicationOwner) {
    this.applicationOwner = applicationOwner;
    return this;
  }

   /**
   * Owner of application. 
   * @return applicationOwner
  **/
  @ApiModelProperty(value = "Owner of application. ")
  public String getApplicationOwner() {
    return applicationOwner;
  }

  public void setApplicationOwner(String applicationOwner) {
    this.applicationOwner = applicationOwner;
  }

  public SubscriptionValidationInfoDTO apiId(String apiId) {
    this.apiId = apiId;
    return this;
  }

   /**
   * UUID of API. 
   * @return apiId
  **/
  @ApiModelProperty(value = "UUID of API. ")
  public String getApiId() {
    return apiId;
  }

  public void setApiId(String apiId) {
    this.apiId = apiId;
  }

  public SubscriptionValidationInfoDTO apiName(String apiName) {
    this.apiName = apiName;
    return this;
  }

   /**
   * Name of API. 
   * @return apiName
  **/
  @ApiModelProperty(value = "Name of API. ")
  public String getApiName() {
    return apiName;
  }

  public void setApiName(String apiName) {
    this.apiName = apiName;
  }

  public SubscriptionValidationInfoDTO apiProvider(String apiProvider) {
    this.apiProvider = apiProvider;
    return this;
  }

   /**
   * Name of API provider. 
   * @return apiProvider
  **/
  @ApiModelProperty(value = "Name of API provider. ")
  public String getApiProvider() {
    return apiProvider;
  }

  public void setApiProvider(String apiProvider) {
    this.apiProvider = apiProvider;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SubscriptionValidationInfoDTO subscriptionValidationInfo = (SubscriptionValidationInfoDTO) o;
    return Objects.equals(this.isValid, subscriptionValidationInfo.isValid) &&
        Objects.equals(this.subscriptionStatus, subscriptionValidationInfo.subscriptionStatus) &&
        Objects.equals(this.applicationId, subscriptionValidationInfo.applicationId) &&
        Objects.equals(this.applicationName, subscriptionValidationInfo.applicationName) &&
        Objects.equals(this.applicationOwner, subscriptionValidationInfo.applicationOwner) &&
        Objects.equals(this.apiId, subscriptionValidationInfo.apiId) &&
        Objects.equals(this.apiName, subscriptionValidationInfo.apiName) &&
        Objects.equals(this.apiProvider, subscriptionValidationInfo.apiProvider);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isValid, subscriptionStatus, applicationId, applicationName, applicationOwner, apiId, apiName, apiProvider);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubscriptionValidationInfoDTO {\n");
    
    sb.append("    isValid: ").append(toIndentedString(isValid)).append("\n");
    sb.append("    subscriptionStatus: ").append(toIndentedString(subscriptionStatus)).append("\n");
    sb.append("    applicationId: ").append(toIndentedString(applicationId)).append("\n");
    sb.append("    applicationName: ").append(toIndentedString(applicationName)).append("\n");
    sb.append("    applicationOwner: ").append(toIndentedString(applicationOwner)).append("\n");
    sb.append("    apiId: ").append(toIndentedString(apiId)).append("\n");
    sb.append("    apiName: ").append(toIndentedString(apiName)).append("\n");
    sb.append("    apiProvider: ").append(toIndentedString(apiProvider)).append("\n");
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

