package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.SubscriptionDTO;
import java.util.Objects;

/**
 * SubscriptionListDTO
 */
public class SubscriptionListDTO   {
  @SerializedName("count")
  private Integer count = null;

  @SerializedName("next")
  private String next = null;

  @SerializedName("previous")
  private String previous = null;

  @SerializedName("list")
  private List<SubscriptionDTO> list = new ArrayList<SubscriptionDTO>();

  public SubscriptionListDTO count(Integer count) {
    this.count = count;
    return this;
  }

   /**
   * Number of Subscriptions returned. 
   * @return count
  **/
  @ApiModelProperty(example = "1", value = "Number of Subscriptions returned. ")
  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }

  public SubscriptionListDTO next(String next) {
    this.next = next;
    return this;
  }

   /**
   * Link to the next subset of resources qualified. Empty if no more resources are to be returned. 
   * @return next
  **/
  @ApiModelProperty(example = "/subscriptions?limit=1&offset=2&apiId=01234567-0123-0123-0123-012345678901", value = "Link to the next subset of resources qualified. Empty if no more resources are to be returned. ")
  public String getNext() {
    return next;
  }

  public void setNext(String next) {
    this.next = next;
  }

  public SubscriptionListDTO previous(String previous) {
    this.previous = previous;
    return this;
  }

   /**
   * Link to the previous subset of resources qualified. Empty if current subset is the first subset returned. 
   * @return previous
  **/
  @ApiModelProperty(example = "/subscriptions?limit=1&offset=0&apiId=01234567-0123-0123-0123-012345678901", value = "Link to the previous subset of resources qualified. Empty if current subset is the first subset returned. ")
  public String getPrevious() {
    return previous;
  }

  public void setPrevious(String previous) {
    this.previous = previous;
  }

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

