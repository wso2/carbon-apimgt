package org.wso2.carbon.apimgt.rest.api.core.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ThreatProtectionPolicyIdListDTO
 */
public class ThreatProtectionPolicyIdListDTO   {
  @JsonProperty("policyIds")
  private List<String> policyIds = new ArrayList<String>();

  public ThreatProtectionPolicyIdListDTO policyIds(List<String> policyIds) {
    this.policyIds = policyIds;
    return this;
  }

  public ThreatProtectionPolicyIdListDTO addPolicyIdsItem(String policyIdsItem) {
    this.policyIds.add(policyIdsItem);
    return this;
  }

   /**
   * Get policyIds
   * @return policyIds
  **/
  @ApiModelProperty(required = true, value = "")
  public List<String> getPolicyIds() {
    return policyIds;
  }

  public void setPolicyIds(List<String> policyIds) {
    this.policyIds = policyIds;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ThreatProtectionPolicyIdListDTO threatProtectionPolicyIdList = (ThreatProtectionPolicyIdListDTO) o;
    return Objects.equals(this.policyIds, threatProtectionPolicyIdList.policyIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(policyIds);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ThreatProtectionPolicyIdListDTO {\n");
    
    sb.append("    policyIds: ").append(toIndentedString(policyIds)).append("\n");
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

