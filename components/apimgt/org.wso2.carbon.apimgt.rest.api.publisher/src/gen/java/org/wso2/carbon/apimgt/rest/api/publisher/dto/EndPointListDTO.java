package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.EndPointDTO;
import java.util.Objects;

/**
 * EndPointListDTO
 */
public class EndPointListDTO   {
  @SerializedName("count")
  private Integer count = null;

  @SerializedName("list")
  private List<EndPointDTO> list = new ArrayList<EndPointDTO>();

  public EndPointListDTO count(Integer count) {
    this.count = count;
    return this;
  }

   /**
   * Number of EndPoints returned. 
   * @return count
  **/
  @ApiModelProperty(example = "1", value = "Number of EndPoints returned. ")
  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }

  public EndPointListDTO list(List<EndPointDTO> list) {
    this.list = list;
    return this;
  }

  public EndPointListDTO addListItem(EndPointDTO listItem) {
    this.list.add(listItem);
    return this;
  }

   /**
   * Get list
   * @return list
  **/
  @ApiModelProperty(value = "")
  public List<EndPointDTO> getList() {
    return list;
  }

  public void setList(List<EndPointDTO> list) {
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
    EndPointListDTO endPointList = (EndPointListDTO) o;
    return Objects.equals(this.count, endPointList.count) &&
        Objects.equals(this.list, endPointList.list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, list);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EndPointListDTO {\n");
    
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

