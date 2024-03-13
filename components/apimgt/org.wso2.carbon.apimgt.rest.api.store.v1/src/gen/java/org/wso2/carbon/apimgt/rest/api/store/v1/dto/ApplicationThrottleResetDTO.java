package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class ApplicationThrottleResetDTO   {
  
    private String userName = null;
    private String applicationId = null;
    private String applicationTier = null;

  /**
   * The username for which the throttle policy needs to be reset
   **/
  public ApplicationThrottleResetDTO userName(String userName) {
    this.userName = userName;
    return this;
  }

  
  @ApiModelProperty(example = "admin", value = "The username for which the throttle policy needs to be reset")
  @JsonProperty("userName")
  public String getUserName() {
    return userName;
  }
  public void setUserName(String userName) {
    this.userName = userName;
  }

  /**
   **/
  public ApplicationThrottleResetDTO applicationId(String applicationId) {
    this.applicationId = applicationId;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "")
  @JsonProperty("applicationId")
  public String getApplicationId() {
    return applicationId;
  }
  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  /**
   **/
  public ApplicationThrottleResetDTO applicationTier(String applicationTier) {
    this.applicationTier = applicationTier;
    return this;
  }

  
  @ApiModelProperty(example = "50PerMin", value = "")
  @JsonProperty("applicationTier")
  public String getApplicationTier() {
    return applicationTier;
  }
  public void setApplicationTier(String applicationTier) {
    this.applicationTier = applicationTier;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApplicationThrottleResetDTO applicationThrottleReset = (ApplicationThrottleResetDTO) o;
    return Objects.equals(userName, applicationThrottleReset.userName) &&
        Objects.equals(applicationId, applicationThrottleReset.applicationId) &&
        Objects.equals(applicationTier, applicationThrottleReset.applicationTier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userName, applicationId, applicationTier);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationThrottleResetDTO {\n");
    
    sb.append("    userName: ").append(toIndentedString(userName)).append("\n");
    sb.append("    applicationId: ").append(toIndentedString(applicationId)).append("\n");
    sb.append("    applicationTier: ").append(toIndentedString(applicationTier)).append("\n");
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

