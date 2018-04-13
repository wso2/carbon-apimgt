package org.wso2.carbon.apimgt.rest.api.admin.dto;


import com.google.gson.annotations.SerializedName;
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
  @SerializedName("defaultLimit")
  private ThrottleLimitDTO defaultLimit = null;

  @SerializedName("conditionalGroups")
  private List<ConditionalGroupDTO> conditionalGroups = new ArrayList<ConditionalGroupDTO>();

  public AdvancedThrottlePolicyDTO defaultLimit(ThrottleLimitDTO defaultLimit) {
    this.defaultLimit = defaultLimit;
    return this;
  }

   /**
   * Get defaultLimit
   * @return defaultLimit
  **/
  @ApiModelProperty(value = "")
  public ThrottleLimitDTO getDefaultLimit() {
    return defaultLimit;
  }

  public void setDefaultLimit(ThrottleLimitDTO defaultLimit) {
    this.defaultLimit = defaultLimit;
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
    return Objects.equals(this.defaultLimit, advancedThrottlePolicy.defaultLimit) &&
        Objects.equals(this.conditionalGroups, advancedThrottlePolicy.conditionalGroups) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(defaultLimit, conditionalGroups, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AdvancedThrottlePolicyDTO {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    defaultLimit: ").append(toIndentedString(defaultLimit)).append("\n");
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

