package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.AlertTypeDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class AlertsSubscriptionDTO   {
  
    private List<AlertTypeDTO> alerts = new ArrayList<AlertTypeDTO>();
    private List<String> emailList = new ArrayList<String>();

  /**
   **/
  public AlertsSubscriptionDTO alerts(List<AlertTypeDTO> alerts) {
    this.alerts = alerts;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("alerts")
  public List<AlertTypeDTO> getAlerts() {
    return alerts;
  }
  public void setAlerts(List<AlertTypeDTO> alerts) {
    this.alerts = alerts;
  }

  /**
   **/
  public AlertsSubscriptionDTO emailList(List<String> emailList) {
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


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AlertsSubscriptionDTO alertsSubscription = (AlertsSubscriptionDTO) o;
    return Objects.equals(alerts, alertsSubscription.alerts) &&
        Objects.equals(emailList, alertsSubscription.emailList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(alerts, emailList);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AlertsSubscriptionDTO {\n");
    
    sb.append("    alerts: ").append(toIndentedString(alerts)).append("\n");
    sb.append("    emailList: ").append(toIndentedString(emailList)).append("\n");
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

