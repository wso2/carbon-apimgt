package org.wso2.carbon.apimgt.rest.api.admin.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.admin.dto.IPConditionDTO;
import java.util.Objects;

/**
 * Blocking Conditions
 */
@ApiModel(description = "Blocking Conditions")
public class BlockingConditionDTO   {
  @SerializedName("conditionId")
  private String conditionId = null;

  @SerializedName("conditionType")
  private String conditionType = null;

  @SerializedName("conditionValue")
  private String conditionValue = null;

  @SerializedName("status")
  private Boolean status = null;

  @SerializedName("ipCondition")
  private IPConditionDTO ipCondition = null;

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

  public BlockingConditionDTO status(Boolean status) {
    this.status = status;
    return this;
  }

   /**
   * Get status
   * @return status
  **/
  @ApiModelProperty(value = "")
  public Boolean getStatus() {
    return status;
  }

  public void setStatus(Boolean status) {
    this.status = status;
  }

  public BlockingConditionDTO ipCondition(IPConditionDTO ipCondition) {
    this.ipCondition = ipCondition;
    return this;
  }

   /**
   * Get ipCondition
   * @return ipCondition
  **/
  @ApiModelProperty(value = "")
  public IPConditionDTO getIpCondition() {
    return ipCondition;
  }

  public void setIpCondition(IPConditionDTO ipCondition) {
    this.ipCondition = ipCondition;
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
        Objects.equals(this.conditionValue, blockingCondition.conditionValue) &&
        Objects.equals(this.status, blockingCondition.status) &&
        Objects.equals(this.ipCondition, blockingCondition.ipCondition);
  }

  @Override
  public int hashCode() {
    return Objects.hash(conditionId, conditionType, conditionValue, status, ipCondition);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BlockingConditionDTO {\n");
    
    sb.append("    conditionId: ").append(toIndentedString(conditionId)).append("\n");
    sb.append("    conditionType: ").append(toIndentedString(conditionType)).append("\n");
    sb.append("    conditionValue: ").append(toIndentedString(conditionValue)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    ipCondition: ").append(toIndentedString(ipCondition)).append("\n");
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

