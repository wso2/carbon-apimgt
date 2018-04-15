package org.wso2.carbon.apimgt.rest.api.core.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * SubscriptionDTO
 */
public class SubscriptionDTO   {
  @SerializedName("apiName")
  private String apiName = null;

  @SerializedName("apiContext")
  private String apiContext = null;

  @SerializedName("apiVersion")
  private String apiVersion = null;

  @SerializedName("apiProvider")
  private String apiProvider = null;

  @SerializedName("consumerKey")
  private String consumerKey = null;

  @SerializedName("subscriptionPolicy")
  private String subscriptionPolicy = null;

  @SerializedName("keyEnvType")
  private String keyEnvType = null;

  @SerializedName("applicationId")
  private String applicationId = null;

  @SerializedName("status")
  private String status = null;

  public SubscriptionDTO apiName(String apiName) {
    this.apiName = apiName;
    return this;
  }

   /**
   * Name of API. 
   * @return apiName
  **/
  @ApiModelProperty(required = true, value = "Name of API. ")
  public String getApiName() {
    return apiName;
  }

  public void setApiName(String apiName) {
    this.apiName = apiName;
  }

  public SubscriptionDTO apiContext(String apiContext) {
    this.apiContext = apiContext;
    return this;
  }

   /**
   * Context of API. 
   * @return apiContext
  **/
  @ApiModelProperty(required = true, value = "Context of API. ")
  public String getApiContext() {
    return apiContext;
  }

  public void setApiContext(String apiContext) {
    this.apiContext = apiContext;
  }

  public SubscriptionDTO apiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
    return this;
  }

   /**
   * Version of API. 
   * @return apiVersion
  **/
  @ApiModelProperty(required = true, value = "Version of API. ")
  public String getApiVersion() {
    return apiVersion;
  }

  public void setApiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
  }

  public SubscriptionDTO apiProvider(String apiProvider) {
    this.apiProvider = apiProvider;
    return this;
  }

   /**
   * Provider of API. 
   * @return apiProvider
  **/
  @ApiModelProperty(required = true, value = "Provider of API. ")
  public String getApiProvider() {
    return apiProvider;
  }

  public void setApiProvider(String apiProvider) {
    this.apiProvider = apiProvider;
  }

  public SubscriptionDTO consumerKey(String consumerKey) {
    this.consumerKey = consumerKey;
    return this;
  }

   /**
   * Consumer Key of Application. 
   * @return consumerKey
  **/
  @ApiModelProperty(required = true, value = "Consumer Key of Application. ")
  public String getConsumerKey() {
    return consumerKey;
  }

  public void setConsumerKey(String consumerKey) {
    this.consumerKey = consumerKey;
  }

  public SubscriptionDTO subscriptionPolicy(String subscriptionPolicy) {
    this.subscriptionPolicy = subscriptionPolicy;
    return this;
  }

   /**
   * Name of Subscription Policy. 
   * @return subscriptionPolicy
  **/
  @ApiModelProperty(required = true, value = "Name of Subscription Policy. ")
  public String getSubscriptionPolicy() {
    return subscriptionPolicy;
  }

  public void setSubscriptionPolicy(String subscriptionPolicy) {
    this.subscriptionPolicy = subscriptionPolicy;
  }

  public SubscriptionDTO keyEnvType(String keyEnvType) {
    this.keyEnvType = keyEnvType;
    return this;
  }

   /**
   * Key type (Production or Sandbox). 
   * @return keyEnvType
  **/
  @ApiModelProperty(required = true, value = "Key type (Production or Sandbox). ")
  public String getKeyEnvType() {
    return keyEnvType;
  }

  public void setKeyEnvType(String keyEnvType) {
    this.keyEnvType = keyEnvType;
  }

  public SubscriptionDTO applicationId(String applicationId) {
    this.applicationId = applicationId;
    return this;
  }

   /**
   * UUID of Application 
   * @return applicationId
  **/
  @ApiModelProperty(value = "UUID of Application ")
  public String getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  public SubscriptionDTO status(String status) {
    this.status = status;
    return this;
  }

   /**
   * Subscription Status 
   * @return status
  **/
  @ApiModelProperty(value = "Subscription Status ")
  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
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
    return Objects.equals(this.apiName, subscription.apiName) &&
        Objects.equals(this.apiContext, subscription.apiContext) &&
        Objects.equals(this.apiVersion, subscription.apiVersion) &&
        Objects.equals(this.apiProvider, subscription.apiProvider) &&
        Objects.equals(this.consumerKey, subscription.consumerKey) &&
        Objects.equals(this.subscriptionPolicy, subscription.subscriptionPolicy) &&
        Objects.equals(this.keyEnvType, subscription.keyEnvType) &&
        Objects.equals(this.applicationId, subscription.applicationId) &&
        Objects.equals(this.status, subscription.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apiName, apiContext, apiVersion, apiProvider, consumerKey, subscriptionPolicy, keyEnvType, applicationId, status);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubscriptionDTO {\n");
    
    sb.append("    apiName: ").append(toIndentedString(apiName)).append("\n");
    sb.append("    apiContext: ").append(toIndentedString(apiContext)).append("\n");
    sb.append("    apiVersion: ").append(toIndentedString(apiVersion)).append("\n");
    sb.append("    apiProvider: ").append(toIndentedString(apiProvider)).append("\n");
    sb.append("    consumerKey: ").append(toIndentedString(consumerKey)).append("\n");
    sb.append("    subscriptionPolicy: ").append(toIndentedString(subscriptionPolicy)).append("\n");
    sb.append("    keyEnvType: ").append(toIndentedString(keyEnvType)).append("\n");
    sb.append("    applicationId: ").append(toIndentedString(applicationId)).append("\n");
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

