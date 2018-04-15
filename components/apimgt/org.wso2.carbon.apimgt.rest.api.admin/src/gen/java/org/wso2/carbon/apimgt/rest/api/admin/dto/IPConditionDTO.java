package org.wso2.carbon.apimgt.rest.api.admin.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * IPConditionDTO
 */
public class IPConditionDTO   {
  @SerializedName("ipConditionType")
  private String ipConditionType = null;

  @SerializedName("specificIP")
  private String specificIP = null;

  @SerializedName("startingIP")
  private String startingIP = null;

  @SerializedName("endingIP")
  private String endingIP = null;

  public IPConditionDTO ipConditionType(String ipConditionType) {
    this.ipConditionType = ipConditionType;
    return this;
  }

   /**
   * IPRange and IPSpecific are the supported values 
   * @return ipConditionType
  **/
  @ApiModelProperty(value = "IPRange and IPSpecific are the supported values ")
  public String getIpConditionType() {
    return ipConditionType;
  }

  public void setIpConditionType(String ipConditionType) {
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
        Objects.equals(this.endingIP, ipCondition.endingIP);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ipConditionType, specificIP, startingIP, endingIP);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class IPConditionDTO {\n");
    
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

