package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.internal.service.dto.PaginationDTO;
import org.wso2.carbon.apimgt.internal.service.dto.WebhooksSubscriptionDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class WebhooksSubscriptionsListDTO   {
  
    private List<WebhooksSubscriptionDTO> list = new ArrayList<>();
    private PaginationDTO pagination = null;

  /**
   **/
  public WebhooksSubscriptionsListDTO list(List<WebhooksSubscriptionDTO> list) {
    this.list = list;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("list")
  public List<WebhooksSubscriptionDTO> getList() {
    return list;
  }
  public void setList(List<WebhooksSubscriptionDTO> list) {
    this.list = list;
  }

  /**
   **/
  public WebhooksSubscriptionsListDTO pagination(PaginationDTO pagination) {
    this.pagination = pagination;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("pagination")
  public PaginationDTO getPagination() {
    return pagination;
  }
  public void setPagination(PaginationDTO pagination) {
    this.pagination = pagination;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WebhooksSubscriptionsListDTO webhooksSubscriptionsList = (WebhooksSubscriptionsListDTO) o;
    return Objects.equals(list, webhooksSubscriptionsList.list) &&
        Objects.equals(pagination, webhooksSubscriptionsList.pagination);
  }

  @Override
  public int hashCode() {
    return Objects.hash(list, pagination);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class WebhooksSubscriptionsListDTO {\n");
    
    sb.append("    list: ").append(toIndentedString(list)).append("\n");
    sb.append("    pagination: ").append(toIndentedString(pagination)).append("\n");
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

