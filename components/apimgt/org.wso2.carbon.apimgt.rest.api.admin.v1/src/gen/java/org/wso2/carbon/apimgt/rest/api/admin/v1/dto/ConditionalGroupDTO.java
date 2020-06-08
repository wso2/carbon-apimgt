package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ThrottleConditionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ThrottleLimitDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class ConditionalGroupDTO   {
  
    private String description = null;
    private List<ThrottleConditionDTO> conditions = new ArrayList<>();
    private ThrottleLimitDTO limit = null;

  /**
   * Description of the Conditional Group
   **/
  public ConditionalGroupDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(value = "Description of the Conditional Group")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Individual throttling conditions. They can be defined as either HeaderCondition, IPCondition, JWTClaimsCondition, QueryParameterCondition Please see schemas of each of those throttling condition in Definitions section. 
   **/
  public ConditionalGroupDTO conditions(List<ThrottleConditionDTO> conditions) {
    this.conditions = conditions;
    return this;
  }

  
  @ApiModelProperty(example = "\"[\\n  {\\n    \\\"type\\\": \\\"HEADERCONDITION\\\",\\n    \\\"invertCondition\\\": false,\\n    \\\"headerCondition\\\":\\n    {\\n      \\\"headerName\\\": \\\"Host\\\",\\n      \\\"headerValue\\\": \\\"10.100.7.77\\\"\\n    }\\n\\t},\\n\\t{\\n    \\\"type\\\": \\\"IPCONDITION\\\",\\n    \\\"invertCondition\\\": false,\\n    \\\"ipCondition\\\":\\n    {\\n      \\\"ipConditionType\\\": \\\"IPSPECIFIC\\\",\\n      \\\"specificIP\\\": \\\"10.100.1.22\\\",\\n      \\\"startingIP\\\": null,\\n      \\\"endingIP\\\": null\\n    }\\n\\t},\\n\\t{\\n    \\\"type\\\": \\\"QUERYPARAMETERCONDITION\\\",\\n    \\\"invertCondition\\\": false,\\n    \\\"queryParameterCondition\\\":\\n    {\\n      \\\"parameterName\\\": \\\"name\\\",\\n      \\\"parameterValue\\\": \\\"admin\\\"\\n    }\\n\\t},\\n\\t{\\n    \\\"type\\\": \\\"JWTCLAIMSCONDITION\\\",\\n    \\\"invertCondition\\\": true,\\n    \\\"jwtClaimsCondition\\\":\\n    {\\n      \\\"claimUrl\\\": \\\"claimUrl0\\\",\\n      \\\"attribute\\\": \\\"claimAttr0\\\"\\n    }\\n  }\\n]\\n\"", required = true, value = "Individual throttling conditions. They can be defined as either HeaderCondition, IPCondition, JWTClaimsCondition, QueryParameterCondition Please see schemas of each of those throttling condition in Definitions section. ")
  @JsonProperty("conditions")
  @NotNull
 @Size(min=1)  public List<ThrottleConditionDTO> getConditions() {
    return conditions;
  }
  public void setConditions(List<ThrottleConditionDTO> conditions) {
    this.conditions = conditions;
  }

  /**
   **/
  public ConditionalGroupDTO limit(ThrottleLimitDTO limit) {
    this.limit = limit;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("limit")
  @NotNull
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
    return Objects.equals(description, conditionalGroup.description) &&
        Objects.equals(conditions, conditionalGroup.conditions) &&
        Objects.equals(limit, conditionalGroup.limit);
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

