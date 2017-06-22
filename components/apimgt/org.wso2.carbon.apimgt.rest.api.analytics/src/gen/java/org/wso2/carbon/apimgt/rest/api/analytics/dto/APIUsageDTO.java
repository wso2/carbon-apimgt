package org.wso2.carbon.apimgt.rest.api.analytics.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * APIUsageDTO
 */
public class APIUsageDTO   {
  @JsonProperty("appName")
  private String appName = null;

  @JsonProperty("apiName")
  private String apiName = null;

  @JsonProperty("consumerKey")
  private String consumerKey = null;

  @JsonProperty("count")
  private Integer count = null;

  public APIUsageDTO appName(String appName) {
    this.appName = appName;
    return this;
  }

   /**
   * Name of the application. 
   * @return appName
  **/
  @ApiModelProperty(value = "Name of the application. ")
  public String getAppName() {
    return appName;
  }

  public void setAppName(String appName) {
    this.appName = appName;
  }

  public APIUsageDTO apiName(String apiName) {
    this.apiName = apiName;
    return this;
  }

   /**
   * Name of the api 
   * @return apiName
  **/
  @ApiModelProperty(value = "Name of the api ")
  public String getApiName() {
    return apiName;
  }

  public void setApiName(String apiName) {
    this.apiName = apiName;
  }

  public APIUsageDTO consumerKey(String consumerKey) {
    this.consumerKey = consumerKey;
    return this;
  }

   /**
   * consumer key of the application 
   * @return consumerKey
  **/
  @ApiModelProperty(value = "consumer key of the application ")
  public String getConsumerKey() {
    return consumerKey;
  }

  public void setConsumerKey(String consumerKey) {
    this.consumerKey = consumerKey;
  }

  public APIUsageDTO count(Integer count) {
    this.count = count;
    return this;
  }

   /**
   * Number of requests 
   * @return count
  **/
  @ApiModelProperty(value = "Number of requests ")
  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIUsageDTO apIUsage = (APIUsageDTO) o;
    return Objects.equals(this.appName, apIUsage.appName) &&
        Objects.equals(this.apiName, apIUsage.apiName) &&
        Objects.equals(this.consumerKey, apIUsage.consumerKey) &&
        Objects.equals(this.count, apIUsage.count);
  }

  @Override
  public int hashCode() {
    return Objects.hash(appName, apiName, consumerKey, count);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIUsageDTO {\n");
    
    sb.append("    appName: ").append(toIndentedString(appName)).append("\n");
    sb.append("    apiName: ").append(toIndentedString(apiName)).append("\n");
    sb.append("    consumerKey: ").append(toIndentedString(consumerKey)).append("\n");
    sb.append("    count: ").append(toIndentedString(count)).append("\n");
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

