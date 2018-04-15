package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * API_threatProtectionPolicies_listDTO
 */
public class API_threatProtectionPolicies_listDTO   {
  @SerializedName("policyId")
  private String policyId = null;

  @SerializedName("priority")
  private Integer priority = null;

  public API_threatProtectionPolicies_listDTO policyId(String policyId) {
    this.policyId = policyId;
    return this;
  }

   /**
   * Get policyId
   * @return policyId
  **/
  @ApiModelProperty(value = "")
  public String getPolicyId() {
    return policyId;
  }

  public void setPolicyId(String policyId) {
    this.policyId = policyId;
  }

  public API_threatProtectionPolicies_listDTO priority(Integer priority) {
    this.priority = priority;
    return this;
  }

   /**
   * Get priority
   * @return priority
  **/
  @ApiModelProperty(value = "")
  public Integer getPriority() {
    return priority;
  }

  public void setPriority(Integer priority) {
    this.priority = priority;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    API_threatProtectionPolicies_listDTO apIThreatProtectionPoliciesList = (API_threatProtectionPolicies_listDTO) o;
    return Objects.equals(this.policyId, apIThreatProtectionPoliciesList.policyId) &&
        Objects.equals(this.priority, apIThreatProtectionPoliciesList.priority);
  }

  @Override
  public int hashCode() {
    return Objects.hash(policyId, priority);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class API_threatProtectionPolicies_listDTO {\n");
    
    sb.append("    policyId: ").append(toIndentedString(policyId)).append("\n");
    sb.append("    priority: ").append(toIndentedString(priority)).append("\n");
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

