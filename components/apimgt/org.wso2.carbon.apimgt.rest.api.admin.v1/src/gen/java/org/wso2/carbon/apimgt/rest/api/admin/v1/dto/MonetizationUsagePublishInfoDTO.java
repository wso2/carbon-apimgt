package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class MonetizationUsagePublishInfoDTO   {
  
    private String state = null;
    private String status = null;
    private String startedTime = null;
    private String lastPublsihedTime = null;

  /**
   * State of usage publish job
   **/
  public MonetizationUsagePublishInfoDTO state(String state) {
    this.state = state;
    return this;
  }

  
  @ApiModelProperty(example = "RUNNING", value = "State of usage publish job")
  @JsonProperty("state")
  public String getState() {
    return state;
  }
  public void setState(String state) {
    this.state = state;
  }

  /**
   * Status of usage publish job
   **/
  public MonetizationUsagePublishInfoDTO status(String status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(example = "SUCCESSFULL", value = "Status of usage publish job")
  @JsonProperty("status")
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * Timestamp of the started time of the Job
   **/
  public MonetizationUsagePublishInfoDTO startedTime(String startedTime) {
    this.startedTime = startedTime;
    return this;
  }

  
  @ApiModelProperty(example = "1599196134000", value = "Timestamp of the started time of the Job")
  @JsonProperty("startedTime")
  public String getStartedTime() {
    return startedTime;
  }
  public void setStartedTime(String startedTime) {
    this.startedTime = startedTime;
  }

  /**
   * Timestamp of the last published time
   **/
  public MonetizationUsagePublishInfoDTO lastPublsihedTime(String lastPublsihedTime) {
    this.lastPublsihedTime = lastPublsihedTime;
    return this;
  }

  
  @ApiModelProperty(example = "1599196134000", value = "Timestamp of the last published time")
  @JsonProperty("lastPublsihedTime")
  public String getLastPublsihedTime() {
    return lastPublsihedTime;
  }
  public void setLastPublsihedTime(String lastPublsihedTime) {
    this.lastPublsihedTime = lastPublsihedTime;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MonetizationUsagePublishInfoDTO monetizationUsagePublishInfo = (MonetizationUsagePublishInfoDTO) o;
    return Objects.equals(state, monetizationUsagePublishInfo.state) &&
        Objects.equals(status, monetizationUsagePublishInfo.status) &&
        Objects.equals(startedTime, monetizationUsagePublishInfo.startedTime) &&
        Objects.equals(lastPublsihedTime, monetizationUsagePublishInfo.lastPublsihedTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(state, status, startedTime, lastPublsihedTime);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MonetizationUsagePublishInfoDTO {\n");
    
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    startedTime: ").append(toIndentedString(startedTime)).append("\n");
    sb.append("    lastPublsihedTime: ").append(toIndentedString(lastPublsihedTime)).append("\n");
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

