package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import javax.validation.constraints.*;

/**
 * Blocking Conditions
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;

@ApiModel(description = "Blocking Conditions")

public class BlockingConditionDTO   {
  
    private String conditionId = null;
    private String conditionType = null;
    private Object conditionValue = null;
    private Boolean conditionStatus = null;

  /**
   * Id of the blocking condition
   **/
  public BlockingConditionDTO conditionId(String conditionId) {
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
   * Type of the blocking condition
   **/
  public BlockingConditionDTO conditionType(String conditionType) {
    this.conditionType = conditionType;
    return this;
  }

  
  @ApiModelProperty(example = "IP", required = true, value = "Type of the blocking condition")
  @JsonProperty("conditionType")
  @NotNull
  public String getConditionType() {
    return conditionType;
  }
  public void setConditionType(String conditionType) {
    this.conditionType = conditionType;
  }

  /**
   * Value of the blocking condition
   **/
  public BlockingConditionDTO conditionValue(Object conditionValue) {
    this.conditionValue = conditionValue;
    return this;
  }

  
  @ApiModelProperty(example = "\"{\\\"fixedIp\\\":\\\"192.168.1.1\\\":\\\"invert\\\":false}\"", required = true, value = "Value of the blocking condition")
  @JsonProperty("conditionValue")
  @NotNull
  public Object getConditionValue() {
    return conditionValue;
  }
  public void setConditionValue(Object conditionValue) {
    this.conditionValue = conditionValue;
  }

  /**
   * Status of the blocking condition
   **/
  public BlockingConditionDTO conditionStatus(Boolean conditionStatus) {
    this.conditionStatus = conditionStatus;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "Status of the blocking condition")
  @JsonProperty("conditionStatus")
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
    BlockingConditionDTO blockingCondition = (BlockingConditionDTO) o;
    return Objects.equals(conditionId, blockingCondition.conditionId) &&
        Objects.equals(conditionType, blockingCondition.conditionType) &&
        Objects.equals(conditionValue, blockingCondition.conditionValue) &&
        Objects.equals(conditionStatus, blockingCondition.conditionStatus);
  }

  @Override
  public int hashCode() {
    return Objects.hash(conditionId, conditionType, conditionValue, conditionStatus);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BlockingConditionDTO {\n");
    
    sb.append("    conditionId: ").append(toIndentedString(conditionId)).append("\n");
    sb.append("    conditionType: ").append(toIndentedString(conditionType)).append("\n");
    sb.append("    conditionValue: ").append(toIndentedString(conditionValue)).append("\n");
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

