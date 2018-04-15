package org.wso2.carbon.apimgt.rest.api.admin.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ApplicationThrottlePolicyDTO;
import java.util.Objects;

/**
 * ApplicationThrottlePolicyListDTO
 */
public class ApplicationThrottlePolicyListDTO   {
  @SerializedName("count")
  private Integer count = null;

  @SerializedName("list")
  private List<ApplicationThrottlePolicyDTO> list = new ArrayList<ApplicationThrottlePolicyDTO>();

  public ApplicationThrottlePolicyListDTO count(Integer count) {
    this.count = count;
    return this;
  }

   /**
   * Number of Application throttle policies returned. 
   * @return count
  **/
  @ApiModelProperty(example = "1", value = "Number of Application throttle policies returned. ")
  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }

  public ApplicationThrottlePolicyListDTO list(List<ApplicationThrottlePolicyDTO> list) {
    this.list = list;
    return this;
  }

  public ApplicationThrottlePolicyListDTO addListItem(ApplicationThrottlePolicyDTO listItem) {
    this.list.add(listItem);
    return this;
  }

   /**
   * Get list
   * @return list
  **/
  @ApiModelProperty(value = "")
  public List<ApplicationThrottlePolicyDTO> getList() {
    return list;
  }

  public void setList(List<ApplicationThrottlePolicyDTO> list) {
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
    ApplicationThrottlePolicyListDTO applicationThrottlePolicyList = (ApplicationThrottlePolicyListDTO) o;
    return Objects.equals(this.count, applicationThrottlePolicyList.count) &&
        Objects.equals(this.list, applicationThrottlePolicyList.list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, list);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationThrottlePolicyListDTO {\n");
    
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

