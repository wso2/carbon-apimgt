package org.wso2.carbon.apimgt.rest.api.analytics.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.analytics.dto.APISubscriptionCountDTO;
import java.util.Objects;

/**
 * APISubscriptionCountListDTO
 */
public class APISubscriptionCountListDTO   {
  @SerializedName("count")
  private Integer count = null;

  @SerializedName("next")
  private String next = null;

  @SerializedName("previous")
  private String previous = null;

  @SerializedName("list")
  private List<APISubscriptionCountDTO> list = new ArrayList<APISubscriptionCountDTO>();

  public APISubscriptionCountListDTO count(Integer count) {
    this.count = count;
    return this;
  }

   /**
   * Number of APIs. 
   * @return count
  **/
  @ApiModelProperty(value = "Number of APIs. ")
  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }

  public APISubscriptionCountListDTO next(String next) {
    this.next = next;
    return this;
  }

   /**
   * Link to the next subset of resources qualified. Empty if no more resources are to be returned. 
   * @return next
  **/
  @ApiModelProperty(value = "Link to the next subset of resources qualified. Empty if no more resources are to be returned. ")
  public String getNext() {
    return next;
  }

  public void setNext(String next) {
    this.next = next;
  }

  public APISubscriptionCountListDTO previous(String previous) {
    this.previous = previous;
    return this;
  }

   /**
   * Link to the previous subset of resources qualified. Empty if current subset is the first subset returned. 
   * @return previous
  **/
  @ApiModelProperty(value = "Link to the previous subset of resources qualified. Empty if current subset is the first subset returned. ")
  public String getPrevious() {
    return previous;
  }

  public void setPrevious(String previous) {
    this.previous = previous;
  }

  public APISubscriptionCountListDTO list(List<APISubscriptionCountDTO> list) {
    this.list = list;
    return this;
  }

  public APISubscriptionCountListDTO addListItem(APISubscriptionCountDTO listItem) {
    this.list.add(listItem);
    return this;
  }

   /**
   * Get list
   * @return list
  **/
  @ApiModelProperty(value = "")
  public List<APISubscriptionCountDTO> getList() {
    return list;
  }

  public void setList(List<APISubscriptionCountDTO> list) {
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
    APISubscriptionCountListDTO apISubscriptionCountList = (APISubscriptionCountListDTO) o;
    return Objects.equals(this.count, apISubscriptionCountList.count) &&
        Objects.equals(this.next, apISubscriptionCountList.next) &&
        Objects.equals(this.previous, apISubscriptionCountList.previous) &&
        Objects.equals(this.list, apISubscriptionCountList.list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, next, previous, list);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APISubscriptionCountListDTO {\n");
    
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

