package org.wso2.carbon.apimgt.rest.api.core.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * SubscriptionDTO
 */
@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-01-16T15:55:32.437+05:30")
public class SubscriptionDTO   {
  private String apiContext = null;

  private String apiVersion = null;

  private String consumerKey = null;

  private String subscriptionPolicy = null;

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
   * Subscription Policy. 
   * @return subscriptionPolicy
  **/
  @ApiModelProperty(required = true, value = "Subscription Policy. ")
  public String getSubscriptionPolicy() {
    return subscriptionPolicy;
  }

  public void setSubscriptionPolicy(String subscriptionPolicy) {
    this.subscriptionPolicy = subscriptionPolicy;
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
    return Objects.equals(this.apiContext, subscription.apiContext) &&
        Objects.equals(this.apiVersion, subscription.apiVersion) &&
        Objects.equals(this.consumerKey, subscription.consumerKey) &&
        Objects.equals(this.subscriptionPolicy, subscription.subscriptionPolicy);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apiContext, apiVersion, consumerKey, subscriptionPolicy);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubscriptionDTO {\n");
    
    sb.append("    apiContext: ").append(toIndentedString(apiContext)).append("\n");
    sb.append("    apiVersion: ").append(toIndentedString(apiVersion)).append("\n");
    sb.append("    consumerKey: ").append(toIndentedString(consumerKey)).append("\n");
    sb.append("    subscriptionPolicy: ").append(toIndentedString(subscriptionPolicy)).append("\n");
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

