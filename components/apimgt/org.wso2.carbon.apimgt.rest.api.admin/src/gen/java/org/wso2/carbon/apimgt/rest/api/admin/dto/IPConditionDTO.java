package org.wso2.carbon.apimgt.rest.api.admin.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ThrottleConditionDTO;

/**
 * IPConditionDTO
 */
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-05-03T09:48:03.292+05:30")
public class IPConditionDTO extends ThrottleConditionDTO  {
  /**
   * Gets or Sets ipConditionType
   */
  public enum IpConditionTypeEnum {
    IPRANGE("IPRange"),
    
    IPSPECIFIC("IPSpecific");

    private String value;

    IpConditionTypeEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static IpConditionTypeEnum fromValue(String text) {
      for (IpConditionTypeEnum b : IpConditionTypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("ipConditionType")
  private IpConditionTypeEnum ipConditionType = null;

  @JsonProperty("specificIP")
  private String specificIP = null;

  @JsonProperty("startingIP")
  private String startingIP = null;

  @JsonProperty("endingIP")
  private String endingIP = null;

  public IPConditionDTO ipConditionType(IpConditionTypeEnum ipConditionType) {
    this.ipConditionType = ipConditionType;
    return this;
  }

   /**
   * Get ipConditionType
   * @return ipConditionType
  **/
  @ApiModelProperty(value = "")
  public IpConditionTypeEnum getIpConditionType() {
    return ipConditionType;
  }

  public void setIpConditionType(IpConditionTypeEnum ipConditionType) {
    this.ipConditionType = ipConditionType;
  }

  public IPConditionDTO specificIP(String specificIP) {
    this.specificIP = specificIP;
    return this;
  }

   /**
   * Get specificIP
   * @return specificIP
  **/
  @ApiModelProperty(value = "")
  public String getSpecificIP() {
    return specificIP;
  }

  public void setSpecificIP(String specificIP) {
    this.specificIP = specificIP;
  }

  public IPConditionDTO startingIP(String startingIP) {
    this.startingIP = startingIP;
    return this;
  }

   /**
   * Get startingIP
   * @return startingIP
  **/
  @ApiModelProperty(value = "")
  public String getStartingIP() {
    return startingIP;
  }

  public void setStartingIP(String startingIP) {
    this.startingIP = startingIP;
  }

  public IPConditionDTO endingIP(String endingIP) {
    this.endingIP = endingIP;
    return this;
  }

   /**
   * Get endingIP
   * @return endingIP
  **/
  @ApiModelProperty(value = "")
  public String getEndingIP() {
    return endingIP;
  }

  public void setEndingIP(String endingIP) {
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
    IPConditionDTO ipCondition = (IPConditionDTO) o;
    return Objects.equals(this.ipConditionType, ipCondition.ipConditionType) &&
        Objects.equals(this.specificIP, ipCondition.specificIP) &&
        Objects.equals(this.startingIP, ipCondition.startingIP) &&
        Objects.equals(this.endingIP, ipCondition.endingIP) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ipConditionType, specificIP, startingIP, endingIP, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class IPConditionDTO {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    ipConditionType: ").append(toIndentedString(ipConditionType)).append("\n");
    sb.append("    specificIP: ").append(toIndentedString(specificIP)).append("\n");
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

