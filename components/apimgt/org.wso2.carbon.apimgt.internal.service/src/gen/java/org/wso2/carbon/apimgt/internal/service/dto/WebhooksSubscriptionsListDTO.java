package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;


import io.swagger.annotations.*;
import java.util.Objects;




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

