package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.BotDetectionAlertSubscriptionDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class BotDetectionAlertSubscriptionListDTO   {
  
    private Integer count = null;
    private List<BotDetectionAlertSubscriptionDTO> list = new ArrayList<BotDetectionAlertSubscriptionDTO>();

  /**
   * Number of Bot Detection Alert Subscriptions returned. 
   **/
  public BotDetectionAlertSubscriptionListDTO count(Integer count) {
    this.count = count;
    return this;
  }

  
  @ApiModelProperty(example = "3", value = "Number of Bot Detection Alert Subscriptions returned. ")
  @JsonProperty("count")
  public Integer getCount() {
    return count;
  }
  public void setCount(Integer count) {
    this.count = count;
  }

  /**
   **/
  public BotDetectionAlertSubscriptionListDTO list(List<BotDetectionAlertSubscriptionDTO> list) {
    this.list = list;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("list")
  public List<BotDetectionAlertSubscriptionDTO> getList() {
    return list;
  }
  public void setList(List<BotDetectionAlertSubscriptionDTO> list) {
    this.list = list;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BotDetectionAlertSubscriptionListDTO botDetectionAlertSubscriptionList = (BotDetectionAlertSubscriptionListDTO) o;
    return Objects.equals(count, botDetectionAlertSubscriptionList.count) &&
        Objects.equals(list, botDetectionAlertSubscriptionList.list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, list);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BotDetectionAlertSubscriptionListDTO {\n");
    
    sb.append("    count: ").append(toIndentedString(count)).append("\n");
    sb.append("    list: ").append(toIndentedString(list)).append("\n");
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

