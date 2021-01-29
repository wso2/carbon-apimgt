package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.AlertConfigDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.AlertDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class AlertsInfoResponseDTO   {
  
    private List<AlertDTO> alerts = new ArrayList<AlertDTO>();
    private List<String> emailList = new ArrayList<String>();
    private List<AlertConfigDTO> failedConfigurations = new ArrayList<AlertConfigDTO>();

  /**
   **/
  public AlertsInfoResponseDTO alerts(List<AlertDTO> alerts) {
    this.alerts = alerts;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("alerts")
  public List<AlertDTO> getAlerts() {
    return alerts;
  }
  public void setAlerts(List<AlertDTO> alerts) {
    this.alerts = alerts;
  }

  /**
   **/
  public AlertsInfoResponseDTO emailList(List<String> emailList) {
    this.emailList = emailList;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("emailList")
  public List<String> getEmailList() {
    return emailList;
  }
  public void setEmailList(List<String> emailList) {
    this.emailList = emailList;
  }

  /**
   **/
  public AlertsInfoResponseDTO failedConfigurations(List<AlertConfigDTO> failedConfigurations) {
    this.failedConfigurations = failedConfigurations;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("failedConfigurations")
  public List<AlertConfigDTO> getFailedConfigurations() {
    return failedConfigurations;
  }
  public void setFailedConfigurations(List<AlertConfigDTO> failedConfigurations) {
    this.failedConfigurations = failedConfigurations;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AlertsInfoResponseDTO alertsInfoResponse = (AlertsInfoResponseDTO) o;
    return Objects.equals(alerts, alertsInfoResponse.alerts) &&
        Objects.equals(emailList, alertsInfoResponse.emailList) &&
        Objects.equals(failedConfigurations, alertsInfoResponse.failedConfigurations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(alerts, emailList, failedConfigurations);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AlertsInfoResponseDTO {\n");
    
    sb.append("    alerts: ").append(toIndentedString(alerts)).append("\n");
    sb.append("    emailList: ").append(toIndentedString(emailList)).append("\n");
    sb.append("    failedConfigurations: ").append(toIndentedString(failedConfigurations)).append("\n");
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

