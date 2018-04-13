package org.wso2.carbon.apimgt.rest.api.admin.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ThreatProtectionPolicyDTO;
import java.util.Objects;

/**
 * ThreatProtectionPolicyListDTO
 */
public class ThreatProtectionPolicyListDTO   {
  @SerializedName("list")
  private List<ThreatProtectionPolicyDTO> list = new ArrayList<ThreatProtectionPolicyDTO>();

  public ThreatProtectionPolicyListDTO list(List<ThreatProtectionPolicyDTO> list) {
    this.list = list;
    return this;
  }

  public ThreatProtectionPolicyListDTO addListItem(ThreatProtectionPolicyDTO listItem) {
    this.list.add(listItem);
    return this;
  }

   /**
   * Get list
   * @return list
  **/
  @ApiModelProperty(value = "")
  public List<ThreatProtectionPolicyDTO> getList() {
    return list;
  }

  public void setList(List<ThreatProtectionPolicyDTO> list) {
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
    ThreatProtectionPolicyListDTO threatProtectionPolicyList = (ThreatProtectionPolicyListDTO) o;
    return Objects.equals(this.list, threatProtectionPolicyList.list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(list);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ThreatProtectionPolicyListDTO {\n");
    
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

