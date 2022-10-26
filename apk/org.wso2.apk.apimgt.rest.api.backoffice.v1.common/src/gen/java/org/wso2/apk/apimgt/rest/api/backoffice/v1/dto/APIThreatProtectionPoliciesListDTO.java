package org.wso2.apk.apimgt.rest.api.backoffice.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

public class APIThreatProtectionPoliciesListDTO   {
  
    private String policyId = null;
    private Integer priority = null;

  /**
   **/
  public APIThreatProtectionPoliciesListDTO policyId(String policyId) {
    this.policyId = policyId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("policyId")
  public String getPolicyId() {
    return policyId;
  }
  public void setPolicyId(String policyId) {
    this.policyId = policyId;
  }

  /**
   **/
  public APIThreatProtectionPoliciesListDTO priority(Integer priority) {
    this.priority = priority;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("priority")
  public Integer getPriority() {
    return priority;
  }
  public void setPriority(Integer priority) {
    this.priority = priority;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIThreatProtectionPoliciesListDTO apIThreatProtectionPoliciesList = (APIThreatProtectionPoliciesListDTO) o;
    return Objects.equals(policyId, apIThreatProtectionPoliciesList.policyId) &&
        Objects.equals(priority, apIThreatProtectionPoliciesList.priority);
  }

  @Override
  public int hashCode() {
    return Objects.hash(policyId, priority);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIThreatProtectionPoliciesListDTO {\n");
    
    sb.append("    policyId: ").append(toIndentedString(policyId)).append("\n");
    sb.append("    priority: ").append(toIndentedString(priority)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

