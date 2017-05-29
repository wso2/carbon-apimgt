package org.wso2.carbon.apimgt.rest.api.admin.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ConditionalGroupDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ThrottleLimitDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ThrottlePolicyDTO;
import java.util.Objects;

/**
 * AdvancedThrottlePolicyDTO
 */
public class AdvancedThrottlePolicyDTO extends ThrottlePolicyDTO  {
  @JsonProperty("quotaPolicy")
  private ThrottleLimitDTO quotaPolicy = null;

  @JsonProperty("conditionalGroups")
  private List<ConditionalGroupDTO> conditionalGroups = new ArrayList<ConditionalGroupDTO>();

  public AdvancedThrottlePolicyDTO quotaPolicy(ThrottleLimitDTO quotaPolicy) {
    this.quotaPolicy = quotaPolicy;
    return this;
  }

   /**
   * Get quotaPolicy
   * @return quotaPolicy
  **/
  @ApiModelProperty(value = "")
  public ThrottleLimitDTO getQuotaPolicy() {
    return quotaPolicy;
  }

  public void setQuotaPolicy(ThrottleLimitDTO quotaPolicy) {
    this.quotaPolicy = quotaPolicy;
  }

  public AdvancedThrottlePolicyDTO conditionalGroups(List<ConditionalGroupDTO> conditionalGroups) {
    this.conditionalGroups = conditionalGroups;
    return this;
  }

  public AdvancedThrottlePolicyDTO addConditionalGroupsItem(ConditionalGroupDTO conditionalGroupsItem) {
    this.conditionalGroups.add(conditionalGroupsItem);
    return this;
  }

   /**
   * Get conditionalGroups
   * @return conditionalGroups
  **/
  @ApiModelProperty(value = "")
  public List<ConditionalGroupDTO> getConditionalGroups() {
    return conditionalGroups;
  }

  public void setConditionalGroups(List<ConditionalGroupDTO> conditionalGroups) {
    this.conditionalGroups = conditionalGroups;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AdvancedThrottlePolicyDTO advancedThrottlePolicy = (AdvancedThrottlePolicyDTO) o;
    return Objects.equals(this.quotaPolicy, advancedThrottlePolicy.quotaPolicy) &&
        Objects.equals(this.conditionalGroups, advancedThrottlePolicy.conditionalGroups) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(quotaPolicy, conditionalGroups, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AdvancedThrottlePolicyDTO {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    quotaPolicy: ").append(toIndentedString(quotaPolicy)).append("\n");
    sb.append("    conditionalGroups: ").append(toIndentedString(conditionalGroups)).append("\n");
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

