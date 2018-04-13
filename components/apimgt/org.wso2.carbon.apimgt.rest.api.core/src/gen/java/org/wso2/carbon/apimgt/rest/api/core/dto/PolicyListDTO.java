package org.wso2.carbon.apimgt.rest.api.core.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.core.dto.PolicyDTO;
import java.util.Objects;

/**
 * PolicyListDTO
 */
public class PolicyListDTO   {
  @SerializedName("count")
  private Integer count = null;

  @SerializedName("list")
  private List<PolicyDTO> list = new ArrayList<PolicyDTO>();

  public PolicyListDTO count(Integer count) {
    this.count = count;
    return this;
  }

   /**
   * Number of Polcies returned. 
   * @return count
  **/
  @ApiModelProperty(example = "1", value = "Number of Polcies returned. ")
  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }

  public PolicyListDTO list(List<PolicyDTO> list) {
    this.list = list;
    return this;
  }

  public PolicyListDTO addListItem(PolicyDTO listItem) {
    this.list.add(listItem);
    return this;
  }

   /**
   * Get list
   * @return list
  **/
  @ApiModelProperty(value = "")
  public List<PolicyDTO> getList() {
    return list;
  }

  public void setList(List<PolicyDTO> list) {
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
    PolicyListDTO policyList = (PolicyListDTO) o;
    return Objects.equals(this.count, policyList.count) &&
        Objects.equals(this.list, policyList.list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, list);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PolicyListDTO {\n");
    
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

