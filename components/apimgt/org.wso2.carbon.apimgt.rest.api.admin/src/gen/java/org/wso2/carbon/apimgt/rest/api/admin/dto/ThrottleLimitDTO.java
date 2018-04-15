package org.wso2.carbon.apimgt.rest.api.admin.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.admin.dto.BandwidthLimitDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.RequestCountLimitDTO;
import java.util.Objects;

/**
 * ThrottleLimitDTO
 */
public class ThrottleLimitDTO   {
  @SerializedName("bandwidthLimit")
  private BandwidthLimitDTO bandwidthLimit = null;

  @SerializedName("requestCountLimit")
  private RequestCountLimitDTO requestCountLimit = null;

  @SerializedName("type")
  private String type = null;

  @SerializedName("timeUnit")
  private String timeUnit = null;

  @SerializedName("unitTime")
  private Integer unitTime = null;

  public ThrottleLimitDTO bandwidthLimit(BandwidthLimitDTO bandwidthLimit) {
    this.bandwidthLimit = bandwidthLimit;
    return this;
  }

   /**
   * Get bandwidthLimit
   * @return bandwidthLimit
  **/
  @ApiModelProperty(value = "")
  public BandwidthLimitDTO getBandwidthLimit() {
    return bandwidthLimit;
  }

  public void setBandwidthLimit(BandwidthLimitDTO bandwidthLimit) {
    this.bandwidthLimit = bandwidthLimit;
  }

  public ThrottleLimitDTO requestCountLimit(RequestCountLimitDTO requestCountLimit) {
    this.requestCountLimit = requestCountLimit;
    return this;
  }

   /**
   * Get requestCountLimit
   * @return requestCountLimit
  **/
  @ApiModelProperty(value = "")
  public RequestCountLimitDTO getRequestCountLimit() {
    return requestCountLimit;
  }

  public void setRequestCountLimit(RequestCountLimitDTO requestCountLimit) {
    this.requestCountLimit = requestCountLimit;
  }

  public ThrottleLimitDTO type(String type) {
    this.type = type;
    return this;
  }

   /**
   * BandwidthLimit and RequestCountLimit are the supported values. 
   * @return type
  **/
  @ApiModelProperty(required = true, value = "BandwidthLimit and RequestCountLimit are the supported values. ")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public ThrottleLimitDTO timeUnit(String timeUnit) {
    this.timeUnit = timeUnit;
    return this;
  }

   /**
   * Get timeUnit
   * @return timeUnit
  **/
  @ApiModelProperty(required = true, value = "")
  public String getTimeUnit() {
    return timeUnit;
  }

  public void setTimeUnit(String timeUnit) {
    this.timeUnit = timeUnit;
  }

  public ThrottleLimitDTO unitTime(Integer unitTime) {
    this.unitTime = unitTime;
    return this;
  }

   /**
   * Get unitTime
   * @return unitTime
  **/
  @ApiModelProperty(required = true, value = "")
  public Integer getUnitTime() {
    return unitTime;
  }

  public void setUnitTime(Integer unitTime) {
    this.unitTime = unitTime;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ThrottleLimitDTO throttleLimit = (ThrottleLimitDTO) o;
    return Objects.equals(this.bandwidthLimit, throttleLimit.bandwidthLimit) &&
        Objects.equals(this.requestCountLimit, throttleLimit.requestCountLimit) &&
        Objects.equals(this.type, throttleLimit.type) &&
        Objects.equals(this.timeUnit, throttleLimit.timeUnit) &&
        Objects.equals(this.unitTime, throttleLimit.unitTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(bandwidthLimit, requestCountLimit, type, timeUnit, unitTime);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ThrottleLimitDTO {\n");
    
    sb.append("    bandwidthLimit: ").append(toIndentedString(bandwidthLimit)).append("\n");
    sb.append("    requestCountLimit: ").append(toIndentedString(requestCountLimit)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    timeUnit: ").append(toIndentedString(timeUnit)).append("\n");
    sb.append("    unitTime: ").append(toIndentedString(unitTime)).append("\n");
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

