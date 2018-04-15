package org.wso2.carbon.apimgt.rest.api.admin.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ThrottleConditionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ThrottleLimitDTO;
import java.util.Objects;

/**
 * ConditionalGroupDTO
 */
public class ConditionalGroupDTO   {
  @SerializedName("description")
  private String description = null;

  @SerializedName("conditions")
  private List<ThrottleConditionDTO> conditions = new ArrayList<ThrottleConditionDTO>();

  @SerializedName("limit")
  private ThrottleLimitDTO limit = null;

  public ConditionalGroupDTO description(String description) {
    this.description = description;
    return this;
  }

   /**
   * Get description
   * @return description
  **/
  @ApiModelProperty(value = "")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public ConditionalGroupDTO conditions(List<ThrottleConditionDTO> conditions) {
    this.conditions = conditions;
    return this;
  }

  public ConditionalGroupDTO addConditionsItem(ThrottleConditionDTO conditionsItem) {
    this.conditions.add(conditionsItem);
    return this;
  }

   /**
   * Get conditions
   * @return conditions
  **/
  @ApiModelProperty(required = true, value = "")
  public List<ThrottleConditionDTO> getConditions() {
    return conditions;
  }

  public void setConditions(List<ThrottleConditionDTO> conditions) {
    this.conditions = conditions;
  }

  public ConditionalGroupDTO limit(ThrottleLimitDTO limit) {
    this.limit = limit;
    return this;
  }

   /**
   * Get limit
   * @return limit
  **/
  @ApiModelProperty(required = true, value = "")
  public ThrottleLimitDTO getLimit() {
    return limit;
  }

  public void setLimit(ThrottleLimitDTO limit) {
    this.limit = limit;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConditionalGroupDTO conditionalGroup = (ConditionalGroupDTO) o;
    return Objects.equals(this.description, conditionalGroup.description) &&
        Objects.equals(this.conditions, conditionalGroup.conditions) &&
        Objects.equals(this.limit, conditionalGroup.limit);
  }

  @Override
  public int hashCode() {
    return Objects.hash(description, conditions, limit);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConditionalGroupDTO {\n");
    
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    conditions: ").append(toIndentedString(conditions)).append("\n");
    sb.append("    limit: ").append(toIndentedString(limit)).append("\n");
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

