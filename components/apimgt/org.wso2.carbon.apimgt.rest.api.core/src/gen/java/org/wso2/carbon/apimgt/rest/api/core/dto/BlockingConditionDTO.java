package org.wso2.carbon.apimgt.rest.api.core.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * Blocking Conditions
 */
@ApiModel(description = "Blocking Conditions")
public class BlockingConditionDTO   {
  @SerializedName("uuid")
  private String uuid = null;

  @SerializedName("conditionType")
  private String conditionType = null;

  @SerializedName("conditionValue")
  private String conditionValue = null;

  @SerializedName("enabled")
  private Boolean enabled = null;

  @SerializedName("fixedIp")
  private Long fixedIp = null;

  @SerializedName("startingIP")
  private Long startingIP = null;

  @SerializedName("endingIP")
  private Long endingIP = null;

  public BlockingConditionDTO uuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

   /**
   * Get uuid
   * @return uuid
  **/
  @ApiModelProperty(value = "")
  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
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

  public BlockingConditionDTO enabled(Boolean enabled) {
    this.enabled = enabled;
    return this;
  }

   /**
   * Get enabled
   * @return enabled
  **/
  @ApiModelProperty(value = "")
  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public BlockingConditionDTO fixedIp(Long fixedIp) {
    this.fixedIp = fixedIp;
    return this;
  }

   /**
   * Get fixedIp
   * @return fixedIp
  **/
  @ApiModelProperty(value = "")
  public Long getFixedIp() {
    return fixedIp;
  }

  public void setFixedIp(Long fixedIp) {
    this.fixedIp = fixedIp;
  }

  public BlockingConditionDTO startingIP(Long startingIP) {
    this.startingIP = startingIP;
    return this;
  }

   /**
   * Get startingIP
   * @return startingIP
  **/
  @ApiModelProperty(value = "")
  public Long getStartingIP() {
    return startingIP;
  }

  public void setStartingIP(Long startingIP) {
    this.startingIP = startingIP;
  }

  public BlockingConditionDTO endingIP(Long endingIP) {
    this.endingIP = endingIP;
    return this;
  }

   /**
   * Get endingIP
   * @return endingIP
  **/
  @ApiModelProperty(value = "")
  public Long getEndingIP() {
    return endingIP;
  }

  public void setEndingIP(Long endingIP) {
    this.endingIP = endingIP;
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
    return Objects.equals(this.uuid, blockingCondition.uuid) &&
        Objects.equals(this.conditionType, blockingCondition.conditionType) &&
        Objects.equals(this.conditionValue, blockingCondition.conditionValue) &&
        Objects.equals(this.enabled, blockingCondition.enabled) &&
        Objects.equals(this.fixedIp, blockingCondition.fixedIp) &&
        Objects.equals(this.startingIP, blockingCondition.startingIP) &&
        Objects.equals(this.endingIP, blockingCondition.endingIP);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uuid, conditionType, conditionValue, enabled, fixedIp, startingIP, endingIP);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BlockingConditionDTO {\n");
    
    sb.append("    uuid: ").append(toIndentedString(uuid)).append("\n");
    sb.append("    conditionType: ").append(toIndentedString(conditionType)).append("\n");
    sb.append("    conditionValue: ").append(toIndentedString(conditionValue)).append("\n");
    sb.append("    enabled: ").append(toIndentedString(enabled)).append("\n");
    sb.append("    fixedIp: ").append(toIndentedString(fixedIp)).append("\n");
    sb.append("    startingIP: ").append(toIndentedString(startingIP)).append("\n");
    sb.append("    endingIP: ").append(toIndentedString(endingIP)).append("\n");
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

