package org.wso2.carbon.apimgt.rest.api.admin.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ThrottleLimitDTO;

/**
 * BandwidthLimitDTO
 */
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-05-04T16:42:24.822+05:30")
public class BandwidthLimitDTO extends ThrottleLimitDTO  {
  @JsonProperty("dataAmount")
  private Long dataAmount = 0l;

  @JsonProperty("dataUnit")
  private String dataUnit = null;

  public BandwidthLimitDTO dataAmount(Long dataAmount) {
    this.dataAmount = dataAmount;
    return this;
  }

   /**
   * Get dataAmount
   * @return dataAmount
  **/
  @ApiModelProperty(value = "")
  public Long getDataAmount() {
    return dataAmount;
  }

  public void setDataAmount(Long dataAmount) {
    this.dataAmount = dataAmount;
  }

  public BandwidthLimitDTO dataUnit(String dataUnit) {
    this.dataUnit = dataUnit;
    return this;
  }

   /**
   * Get dataUnit
   * @return dataUnit
  **/
  @ApiModelProperty(value = "")
  public String getDataUnit() {
    return dataUnit;
  }

  public void setDataUnit(String dataUnit) {
    this.dataUnit = dataUnit;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BandwidthLimitDTO bandwidthLimit = (BandwidthLimitDTO) o;
    return Objects.equals(this.dataAmount, bandwidthLimit.dataAmount) &&
        Objects.equals(this.dataUnit, bandwidthLimit.dataUnit) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dataAmount, dataUnit, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BandwidthLimitDTO {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    dataAmount: ").append(toIndentedString(dataAmount)).append("\n");
    sb.append("    dataUnit: ").append(toIndentedString(dataUnit)).append("\n");
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

