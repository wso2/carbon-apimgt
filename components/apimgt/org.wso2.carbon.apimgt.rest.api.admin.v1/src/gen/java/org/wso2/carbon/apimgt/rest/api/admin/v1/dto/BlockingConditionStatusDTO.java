package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;

/**
 * Blocking Conditions Status
 **/

import java.util.Objects;


@ApiModel(description = "Blocking Conditions Status")

public class BlockingConditionStatusDTO   {
  
    private String conditionId = null;
    private Boolean conditionStatus = null;

  /**
   * Id of the blocking condition
   **/
  public BlockingConditionStatusDTO conditionId(String conditionId) {
    this.conditionId = conditionId;
    return this;
  }

  
  @ApiModelProperty(example = "b513eb68-69e8-4c32-92cf-852c101363cf", value = "Id of the blocking condition")
  @JsonProperty("conditionId")
  public String getConditionId() {
    return conditionId;
  }
  public void setConditionId(String conditionId) {
    this.conditionId = conditionId;
  }

  /**
   * Status of the blocking condition
   **/
  public BlockingConditionStatusDTO conditionStatus(Boolean conditionStatus) {
    this.conditionStatus = conditionStatus;
    return this;
  }

  
  @ApiModelProperty(example = "true", required = true, value = "Status of the blocking condition")
  @JsonProperty("conditionStatus")
  @NotNull
  public Boolean isConditionStatus() {
    return conditionStatus;
  }
  public void setConditionStatus(Boolean conditionStatus) {
    this.conditionStatus = conditionStatus;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BlockingConditionStatusDTO blockingConditionStatus = (BlockingConditionStatusDTO) o;
    return Objects.equals(conditionId, blockingConditionStatus.conditionId) &&
        Objects.equals(conditionStatus, blockingConditionStatus.conditionStatus);
  }

  @Override
  public int hashCode() {
    return Objects.hash(conditionId, conditionStatus);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BlockingConditionStatusDTO {\n");
    
    sb.append("    conditionId: ").append(toIndentedString(conditionId)).append("\n");
    sb.append("    conditionStatus: ").append(toIndentedString(conditionStatus)).append("\n");
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

