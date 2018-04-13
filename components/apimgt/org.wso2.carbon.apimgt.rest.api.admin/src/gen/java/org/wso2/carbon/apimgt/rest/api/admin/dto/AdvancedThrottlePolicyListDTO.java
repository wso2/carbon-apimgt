package org.wso2.carbon.apimgt.rest.api.admin.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.dto.AdvancedThrottlePolicyDTO;
import java.util.Objects;

/**
 * AdvancedThrottlePolicyListDTO
 */
public class AdvancedThrottlePolicyListDTO   {
  @SerializedName("count")
  private Integer count = null;

  @SerializedName("list")
  private List<AdvancedThrottlePolicyDTO> list = new ArrayList<AdvancedThrottlePolicyDTO>();

  public AdvancedThrottlePolicyListDTO count(Integer count) {
    this.count = count;
    return this;
  }

   /**
   * Number of Advanced throttle policies returned. 
   * @return count
  **/
  @ApiModelProperty(example = "1", value = "Number of Advanced throttle policies returned. ")
  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }

  public AdvancedThrottlePolicyListDTO list(List<AdvancedThrottlePolicyDTO> list) {
    this.list = list;
    return this;
  }

  public AdvancedThrottlePolicyListDTO addListItem(AdvancedThrottlePolicyDTO listItem) {
    this.list.add(listItem);
    return this;
  }

   /**
   * Get list
   * @return list
  **/
  @ApiModelProperty(value = "")
  public List<AdvancedThrottlePolicyDTO> getList() {
    return list;
  }

  public void setList(List<AdvancedThrottlePolicyDTO> list) {
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
    AdvancedThrottlePolicyListDTO advancedThrottlePolicyList = (AdvancedThrottlePolicyListDTO) o;
    return Objects.equals(this.count, advancedThrottlePolicyList.count) &&
        Objects.equals(this.list, advancedThrottlePolicyList.list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, list);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AdvancedThrottlePolicyListDTO {\n");
    
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

