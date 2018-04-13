package org.wso2.carbon.apimgt.rest.api.store.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.dto.CompositeAPIInfoDTO;
import java.util.Objects;

/**
 * CompositeAPIListDTO
 */
public class CompositeAPIListDTO   {
  @SerializedName("count")
  private Integer count = null;

  @SerializedName("next")
  private String next = null;

  @SerializedName("previous")
  private String previous = null;

  @SerializedName("list")
  private List<CompositeAPIInfoDTO> list = new ArrayList<CompositeAPIInfoDTO>();

  public CompositeAPIListDTO count(Integer count) {
    this.count = count;
    return this;
  }

   /**
   * Number of Composite APIs returned. 
   * @return count
  **/
  @ApiModelProperty(value = "Number of Composite APIs returned. ")
  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }

  public CompositeAPIListDTO next(String next) {
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

  public CompositeAPIListDTO previous(String previous) {
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

  public CompositeAPIListDTO list(List<CompositeAPIInfoDTO> list) {
    this.list = list;
    return this;
  }

  public CompositeAPIListDTO addListItem(CompositeAPIInfoDTO listItem) {
    this.list.add(listItem);
    return this;
  }

   /**
   * Get list
   * @return list
  **/
  @ApiModelProperty(value = "")
  public List<CompositeAPIInfoDTO> getList() {
    return list;
  }

  public void setList(List<CompositeAPIInfoDTO> list) {
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
    CompositeAPIListDTO compositeAPIList = (CompositeAPIListDTO) o;
    return Objects.equals(this.count, compositeAPIList.count) &&
        Objects.equals(this.next, compositeAPIList.next) &&
        Objects.equals(this.previous, compositeAPIList.previous) &&
        Objects.equals(this.list, compositeAPIList.list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, next, previous, list);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CompositeAPIListDTO {\n");
    
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

