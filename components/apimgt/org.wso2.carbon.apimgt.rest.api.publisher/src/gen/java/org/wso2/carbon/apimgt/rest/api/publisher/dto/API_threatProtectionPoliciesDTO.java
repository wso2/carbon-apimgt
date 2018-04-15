package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.API_threatProtectionPolicies_listDTO;
import java.util.Objects;

/**
 * API_threatProtectionPoliciesDTO
 */
public class API_threatProtectionPoliciesDTO   {
  @SerializedName("list")
  private List<API_threatProtectionPolicies_listDTO> list = new ArrayList<API_threatProtectionPolicies_listDTO>();

  public API_threatProtectionPoliciesDTO list(List<API_threatProtectionPolicies_listDTO> list) {
    this.list = list;
    return this;
  }

  public API_threatProtectionPoliciesDTO addListItem(API_threatProtectionPolicies_listDTO listItem) {
    this.list.add(listItem);
    return this;
  }

   /**
   * Get list
   * @return list
  **/
  @ApiModelProperty(value = "")
  public List<API_threatProtectionPolicies_listDTO> getList() {
    return list;
  }

  public void setList(List<API_threatProtectionPolicies_listDTO> list) {
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
    API_threatProtectionPoliciesDTO apIThreatProtectionPolicies = (API_threatProtectionPoliciesDTO) o;
    return Objects.equals(this.list, apIThreatProtectionPolicies.list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(list);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class API_threatProtectionPoliciesDTO {\n");
    
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

