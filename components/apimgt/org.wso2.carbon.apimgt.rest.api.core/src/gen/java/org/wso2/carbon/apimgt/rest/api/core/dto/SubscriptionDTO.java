package org.wso2.carbon.apimgt.rest.api.core.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * SubscriptionDTO
 */
public class SubscriptionDTO   {
  @JsonProperty("apiName")
  private String apiName = null;

  @JsonProperty("apiContext")
  private String apiContext = null;

  @JsonProperty("apiVersion")
  private String apiVersion = null;

  @JsonProperty("apiProvider")
  private String apiProvider = null;

  @JsonProperty("consumerKey")
  private String consumerKey = null;

  @JsonProperty("subscriptionPolicy")
  private String subscriptionPolicy = null;

  @JsonProperty("applicationName")
  private String applicationName = null;

  @JsonProperty("applicationOwner")
  private String applicationOwner = null;

  @JsonProperty("keyEnvType")
  private String keyEnvType = null;

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

  public SubscriptionDTO applicationName(String applicationName) {
    this.applicationName = applicationName;
    return this;
  }

   /**
   * Application Name. 
   * @return applicationName
  **/
  @ApiModelProperty(required = true, value = "Application Name. ")
  public String getApplicationName() {
    return applicationName;
  }

  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }

  public SubscriptionDTO applicationOwner(String applicationOwner) {
    this.applicationOwner = applicationOwner;
    return this;
  }

   /**
   * Application Owner. 
   * @return applicationOwner
  **/
  @ApiModelProperty(required = true, value = "Application Owner. ")
  public String getApplicationOwner() {
    return applicationOwner;
  }

  public void setApplicationOwner(String applicationOwner) {
    this.applicationOwner = applicationOwner;
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
        Objects.equals(this.applicationName, subscription.applicationName) &&
        Objects.equals(this.applicationOwner, subscription.applicationOwner) &&
        Objects.equals(this.keyEnvType, subscription.keyEnvType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apiName, apiContext, apiVersion, apiProvider, consumerKey, subscriptionPolicy, applicationName, applicationOwner, keyEnvType);
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
    sb.append("    applicationName: ").append(toIndentedString(applicationName)).append("\n");
    sb.append("    applicationOwner: ").append(toIndentedString(applicationOwner)).append("\n");
    sb.append("    keyEnvType: ").append(toIndentedString(keyEnvType)).append("\n");
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

