package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class AlertConfigDTO   {
  
    private String apiName = null;
    private String apiVersion = null;
    private String applicationId = null;
    private Integer requestCount = null;

  /**
   * The name of the api.
   **/
  public AlertConfigDTO apiName(String apiName) {
    this.apiName = apiName;
    return this;
  }

  
  @ApiModelProperty(example = "PizzaShackAPI", value = "The name of the api.")
  @JsonProperty("apiName")
  public String getApiName() {
    return apiName;
  }
  public void setApiName(String apiName) {
    this.apiName = apiName;
  }

  /**
   * The version of the api.
   **/
  public AlertConfigDTO apiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
    return this;
  }

  
  @ApiModelProperty(example = "1.0.0", value = "The version of the api.")
  @JsonProperty("apiVersion")
  public String getApiVersion() {
    return apiVersion;
  }
  public void setApiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
  }

  /**
   * The id of the application
   **/
  public AlertConfigDTO applicationId(String applicationId) {
    this.applicationId = applicationId;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "The id of the application")
  @JsonProperty("applicationId")
  public String getApplicationId() {
    return applicationId;
  }
  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  /**
   * The abnormal request count per minute.
   **/
  public AlertConfigDTO requestCount(Integer requestCount) {
    this.requestCount = requestCount;
    return this;
  }

  
  @ApiModelProperty(example = "20", value = "The abnormal request count per minute.")
  @JsonProperty("requestCount")
  public Integer getRequestCount() {
    return requestCount;
  }
  public void setRequestCount(Integer requestCount) {
    this.requestCount = requestCount;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AlertConfigDTO alertConfig = (AlertConfigDTO) o;
    return Objects.equals(apiName, alertConfig.apiName) &&
        Objects.equals(apiVersion, alertConfig.apiVersion) &&
        Objects.equals(applicationId, alertConfig.applicationId) &&
        Objects.equals(requestCount, alertConfig.requestCount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apiName, apiVersion, applicationId, requestCount);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AlertConfigDTO {\n");
    
    sb.append("    apiName: ").append(toIndentedString(apiName)).append("\n");
    sb.append("    apiVersion: ").append(toIndentedString(apiVersion)).append("\n");
    sb.append("    applicationId: ").append(toIndentedString(applicationId)).append("\n");
    sb.append("    requestCount: ").append(toIndentedString(requestCount)).append("\n");
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

