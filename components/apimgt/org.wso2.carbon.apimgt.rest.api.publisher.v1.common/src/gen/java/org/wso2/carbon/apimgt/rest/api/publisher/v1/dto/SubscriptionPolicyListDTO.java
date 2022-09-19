package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;


import java.util.Objects;


import javax.validation.Valid;



public class SubscriptionPolicyListDTO   {
  
    private Integer count = null;
    private List<SubscriptionPolicyDTO> list = new ArrayList<SubscriptionPolicyDTO>();
    private PaginationDTO pagination = null;

  /**
   * Number of Tiers returned. 
   **/
  public SubscriptionPolicyListDTO count(Integer count) {
    this.count = count;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "Number of Tiers returned. ")
  @JsonProperty("count")
  public Integer getCount() {
    return count;
  }
  public void setCount(Integer count) {
    this.count = count;
  }

  /**
   * Array of SubscriptionPolicies 
   **/
  public SubscriptionPolicyListDTO list(List<SubscriptionPolicyDTO> list) {
    this.list = list;
    return this;
  }

  
  @ApiModelProperty(value = "Array of SubscriptionPolicies ")
      @Valid
  @JsonProperty("list")
  public List<SubscriptionPolicyDTO> getList() {
    return list;
  }
  public void setList(List<SubscriptionPolicyDTO> list) {
    this.list = list;
  }

  /**
   **/
  public SubscriptionPolicyListDTO pagination(PaginationDTO pagination) {
    this.pagination = pagination;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
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
    SubscriptionPolicyListDTO subscriptionPolicyList = (SubscriptionPolicyListDTO) o;
    return Objects.equals(count, subscriptionPolicyList.count) &&
        Objects.equals(list, subscriptionPolicyList.list) &&
        Objects.equals(pagination, subscriptionPolicyList.pagination);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, list, pagination);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubscriptionPolicyListDTO {\n");
    
    sb.append("    count: ").append(toIndentedString(count)).append("\n");
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

