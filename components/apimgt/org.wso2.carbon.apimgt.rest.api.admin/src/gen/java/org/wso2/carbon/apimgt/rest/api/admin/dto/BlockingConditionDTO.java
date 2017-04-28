package org.wso2.carbon.apimgt.rest.api.admin.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Blocking Conditions
 */
@ApiModel(description = "Blocking Conditions")
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-04-28T14:28:58.278+05:30")
public class BlockingConditionDTO   {
  @JsonProperty("conditionId")
  private String conditionId = null;

  @JsonProperty("conditionType")
  private String conditionType = null;

  @JsonProperty("conditionValue")
  private String conditionValue = null;

  public BlockingConditionDTO conditionId(String conditionId) {
    this.conditionId = conditionId;
    return this;
  }

   /**
   * Get conditionId
   * @return conditionId
  **/
  @ApiModelProperty(value = "")
  public String getConditionId() {
    return conditionId;
  }

  public void setConditionId(String conditionId) {
    this.conditionId = conditionId;
  }

  public BlockingConditionDTO conditionType(String conditionType) {
    this.conditionType = conditionType;
    return this;
  }

   /**
   * Get conditionType
   * @return conditionType
  **/
  @ApiModelProperty(required = true, value = "")
  public String getConditionType() {
    return conditionType;
  }

  public void setConditionType(String conditionType) {
    this.conditionType = conditionType;
  }

  public BlockingConditionDTO conditionValue(String conditionValue) {
    this.conditionValue = conditionValue;
    return this;
  }

   /**
   * Get conditionValue
   * @return conditionValue
  **/
  @ApiModelProperty(required = true, value = "")
  public String getConditionValue() {
    return conditionValue;
  }

  public void setConditionValue(String conditionValue) {
    this.conditionValue = conditionValue;
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
    return Objects.equals(this.conditionId, blockingCondition.conditionId) &&
        Objects.equals(this.conditionType, blockingCondition.conditionType) &&
        Objects.equals(this.conditionValue, blockingCondition.conditionValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(conditionId, conditionType, conditionValue);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BlockingConditionDTO {\n");
    
    sb.append("    conditionId: ").append(toIndentedString(conditionId)).append("\n");
    sb.append("    conditionType: ").append(toIndentedString(conditionType)).append("\n");
    sb.append("    conditionValue: ").append(toIndentedString(conditionValue)).append("\n");
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

