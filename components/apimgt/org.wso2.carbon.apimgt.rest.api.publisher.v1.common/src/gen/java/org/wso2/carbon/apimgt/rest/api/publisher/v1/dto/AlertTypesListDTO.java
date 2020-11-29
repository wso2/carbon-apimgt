package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.AlertTypeDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class AlertTypesListDTO   {
  
    private Integer count = null;
    private List<AlertTypeDTO> alerts = new ArrayList<AlertTypeDTO>();

  /**
   * The number of alerts
   **/
  public AlertTypesListDTO count(Integer count) {
    this.count = count;
    return this;
  }

  
  @ApiModelProperty(example = "3", value = "The number of alerts")
  @JsonProperty("count")
  public Integer getCount() {
    return count;
  }
  public void setCount(Integer count) {
    this.count = count;
  }

  /**
   **/
  public AlertTypesListDTO alerts(List<AlertTypeDTO> alerts) {
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


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AlertTypesListDTO alertTypesList = (AlertTypesListDTO) o;
    return Objects.equals(count, alertTypesList.count) &&
        Objects.equals(alerts, alertTypesList.alerts);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, alerts);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AlertTypesListDTO {\n");
    
    sb.append("    count: ").append(toIndentedString(count)).append("\n");
    sb.append("    alerts: ").append(toIndentedString(alerts)).append("\n");
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

