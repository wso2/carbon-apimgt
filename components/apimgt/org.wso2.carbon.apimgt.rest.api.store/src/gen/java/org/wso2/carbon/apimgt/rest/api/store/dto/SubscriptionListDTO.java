package org.wso2.carbon.apimgt.rest.api.store.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.dto.SubscriptionDTO;
import java.util.Objects;

<<<<<<< HEAD
import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class SubscriptionListDTO  {
  
  
  
=======
/**
 * SubscriptionListDTO
 */
public class SubscriptionListDTO   {
  @JsonProperty("count")
>>>>>>> upstream/master
  private Integer count = null;

<<<<<<< HEAD
  private String lastUpdatedTime = null;
=======
  @JsonProperty("next")
  private String next = null;

  @JsonProperty("previous")
  private String previous = null;
>>>>>>> upstream/master

  @JsonProperty("list")
  private List<SubscriptionDTO> list = new ArrayList<SubscriptionDTO>();

  public SubscriptionListDTO count(Integer count) {
    this.count = count;
    return this;
  }

   /**
   * Number of Subscriptions returned. 
   * @return count
  **/
<<<<<<< HEAD

  @JsonIgnore
  public String getCreatedTime(){
    return createdTime;
  }
  public void setCreatedTime(String createdTime){
    this.createdTime=createdTime;
  }

  
  /**
   * Number of Subscriptions returned.\n
   **/
  @ApiModelProperty(value = "Number of Subscriptions returned.\n")
  @JsonProperty("count")
=======
  @ApiModelProperty(value = "Number of Subscriptions returned. ")
>>>>>>> upstream/master
  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }

<<<<<<< HEAD
  
  /**
   * Link to the next subset of resources qualified.\nEmpty if no more resources are to be returned.\n
   **/
  @ApiModelProperty(value = "Link to the next subset of resources qualified.\nEmpty if no more resources are to be returned.\n")
  @JsonProperty("next")
=======
  public SubscriptionListDTO next(String next) {
    this.next = next;
    return this;
  }

   /**
   * Link to the next subset of resources qualified. Empty if no more resources are to be returned. 
   * @return next
  **/
  @ApiModelProperty(value = "Link to the next subset of resources qualified. Empty if no more resources are to be returned. ")
>>>>>>> upstream/master
  public String getNext() {
    return next;
  }

  public void setNext(String next) {
    this.next = next;
  }

<<<<<<< HEAD
  
  /**
   * Link to the previous subset of resources qualified.\nEmpty if current subset is the first subset returned.\n
   **/
  @ApiModelProperty(value = "Link to the previous subset of resources qualified.\nEmpty if current subset is the first subset returned.\n")
  @JsonProperty("previous")
=======
  public SubscriptionListDTO previous(String previous) {
    this.previous = previous;
    return this;
  }

   /**
   * Link to the previous subset of resources qualified. Empty if current subset is the first subset returned. 
   * @return previous
  **/
  @ApiModelProperty(value = "Link to the previous subset of resources qualified. Empty if current subset is the first subset returned. ")
>>>>>>> upstream/master
  public String getPrevious() {
    return previous;
  }

  public void setPrevious(String previous) {
    this.previous = previous;
  }

<<<<<<< HEAD
  
  /**
   **/
=======
  public SubscriptionListDTO list(List<SubscriptionDTO> list) {
    this.list = list;
    return this;
  }

  public SubscriptionListDTO addListItem(SubscriptionDTO listItem) {
    this.list.add(listItem);
    return this;
  }

   /**
   * Get list
   * @return list
  **/
>>>>>>> upstream/master
  @ApiModelProperty(value = "")
  public List<SubscriptionDTO> getList() {
    return list;
  }

  public void setList(List<SubscriptionDTO> list) {
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
    SubscriptionListDTO subscriptionList = (SubscriptionListDTO) o;
    return Objects.equals(this.count, subscriptionList.count) &&
        Objects.equals(this.next, subscriptionList.next) &&
        Objects.equals(this.previous, subscriptionList.previous) &&
        Objects.equals(this.list, subscriptionList.list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, next, previous, list);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubscriptionListDTO {\n");
    
    sb.append("    count: ").append(toIndentedString(count)).append("\n");
    sb.append("    next: ").append(toIndentedString(next)).append("\n");
    sb.append("    previous: ").append(toIndentedString(previous)).append("\n");
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

