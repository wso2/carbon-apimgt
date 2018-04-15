package org.wso2.carbon.apimgt.rest.api.admin.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.dto.BlockingConditionDTO;
import java.util.Objects;

/**
 * BlockingConditionListDTO
 */
public class BlockingConditionListDTO   {
  @SerializedName("count")
  private Integer count = null;

  @SerializedName("list")
  private List<BlockingConditionDTO> list = new ArrayList<BlockingConditionDTO>();

  public BlockingConditionListDTO count(Integer count) {
    this.count = count;
    return this;
  }

   /**
   * Number of Blocking Conditions returned. 
   * @return count
  **/
  @ApiModelProperty(example = "1", value = "Number of Blocking Conditions returned. ")
  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }

  public BlockingConditionListDTO list(List<BlockingConditionDTO> list) {
    this.list = list;
    return this;
  }

  public BlockingConditionListDTO addListItem(BlockingConditionDTO listItem) {
    this.list.add(listItem);
    return this;
  }

   /**
   * Get list
   * @return list
  **/
  @ApiModelProperty(value = "")
  public List<BlockingConditionDTO> getList() {
    return list;
  }

  public void setList(List<BlockingConditionDTO> list) {
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
    BlockingConditionListDTO blockingConditionList = (BlockingConditionListDTO) o;
    return Objects.equals(this.count, blockingConditionList.count) &&
        Objects.equals(this.list, blockingConditionList.list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, list);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BlockingConditionListDTO {\n");
    
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

