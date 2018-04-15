package org.wso2.carbon.apimgt.rest.api.admin.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.dto.CustomRuleDTO;
import java.util.Objects;

/**
 * CustomRuleListDTO
 */
public class CustomRuleListDTO   {
  @SerializedName("count")
  private Integer count = null;

  @SerializedName("list")
  private List<CustomRuleDTO> list = new ArrayList<CustomRuleDTO>();

  public CustomRuleListDTO count(Integer count) {
    this.count = count;
    return this;
  }

   /**
   * Number of Custom Rules returned. 
   * @return count
  **/
  @ApiModelProperty(example = "1", value = "Number of Custom Rules returned. ")
  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }

  public CustomRuleListDTO list(List<CustomRuleDTO> list) {
    this.list = list;
    return this;
  }

  public CustomRuleListDTO addListItem(CustomRuleDTO listItem) {
    this.list.add(listItem);
    return this;
  }

   /**
   * Get list
   * @return list
  **/
  @ApiModelProperty(value = "")
  public List<CustomRuleDTO> getList() {
    return list;
  }

  public void setList(List<CustomRuleDTO> list) {
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
    CustomRuleListDTO customRuleList = (CustomRuleListDTO) o;
    return Objects.equals(this.count, customRuleList.count) &&
        Objects.equals(this.list, customRuleList.list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, list);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CustomRuleListDTO {\n");
    
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

