package org.wso2.carbon.apimgt.rest.api.admin.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.dto.SubscriptionThrottlePolicyDTO;
import java.util.Objects;

/**
 * SubscriptionThrottlePolicyListDTO
 */
public class SubscriptionThrottlePolicyListDTO   {
  @SerializedName("count")
  private Integer count = null;

  @SerializedName("list")
  private List<SubscriptionThrottlePolicyDTO> list = new ArrayList<SubscriptionThrottlePolicyDTO>();

  public SubscriptionThrottlePolicyListDTO count(Integer count) {
    this.count = count;
    return this;
  }

   /**
   * Number of Subscription throttle policies returned. 
   * @return count
  **/
  @ApiModelProperty(example = "1", value = "Number of Subscription throttle policies returned. ")
  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }

  public SubscriptionThrottlePolicyListDTO list(List<SubscriptionThrottlePolicyDTO> list) {
    this.list = list;
    return this;
  }

  public SubscriptionThrottlePolicyListDTO addListItem(SubscriptionThrottlePolicyDTO listItem) {
    this.list.add(listItem);
    return this;
  }

   /**
   * Get list
   * @return list
  **/
  @ApiModelProperty(value = "")
  public List<SubscriptionThrottlePolicyDTO> getList() {
    return list;
  }

  public void setList(List<SubscriptionThrottlePolicyDTO> list) {
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
    SubscriptionThrottlePolicyListDTO subscriptionThrottlePolicyList = (SubscriptionThrottlePolicyListDTO) o;
    return Objects.equals(this.count, subscriptionThrottlePolicyList.count) &&
        Objects.equals(this.list, subscriptionThrottlePolicyList.list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, list);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubscriptionThrottlePolicyListDTO {\n");
    
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

