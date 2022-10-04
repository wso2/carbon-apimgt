package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class IPConditionDTO   {
  

    @XmlType(name="IpConditionTypeEnum")
    @XmlEnum(String.class)
    public enum IpConditionTypeEnum {
        IPRANGE("IPRANGE"),
        IPSPECIFIC("IPSPECIFIC");
        private String value;

        IpConditionTypeEnum (String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static IpConditionTypeEnum fromValue(String v) {
            for (IpConditionTypeEnum b : IpConditionTypeEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private IpConditionTypeEnum ipConditionType = null;
    private String specificIP = null;
    private String startingIP = null;
    private String endingIP = null;

  /**
   * Type of the IP condition. Allowed values are \&quot;IPRANGE\&quot; and \&quot;IPSPECIFIC\&quot;
   **/
  public IPConditionDTO ipConditionType(IpConditionTypeEnum ipConditionType) {
    this.ipConditionType = ipConditionType;
    return this;
  }

  
  @ApiModelProperty(value = "Type of the IP condition. Allowed values are \"IPRANGE\" and \"IPSPECIFIC\"")
  @JsonProperty("ipConditionType")
  public IpConditionTypeEnum getIpConditionType() {
    return ipConditionType;
  }
  public void setIpConditionType(IpConditionTypeEnum ipConditionType) {
    this.ipConditionType = ipConditionType;
  }

  /**
   * Specific IP when \&quot;IPSPECIFIC\&quot; is used as the ipConditionType
   **/
  public IPConditionDTO specificIP(String specificIP) {
    this.specificIP = specificIP;
    return this;
  }

  
  @ApiModelProperty(value = "Specific IP when \"IPSPECIFIC\" is used as the ipConditionType")
  @JsonProperty("specificIP")
  public String getSpecificIP() {
    return specificIP;
  }
  public void setSpecificIP(String specificIP) {
    this.specificIP = specificIP;
  }

  /**
   * Staring IP when \&quot;IPRANGE\&quot; is used as the ipConditionType
   **/
  public IPConditionDTO startingIP(String startingIP) {
    this.startingIP = startingIP;
    return this;
  }

  
  @ApiModelProperty(value = "Staring IP when \"IPRANGE\" is used as the ipConditionType")
  @JsonProperty("startingIP")
  public String getStartingIP() {
    return startingIP;
  }
  public void setStartingIP(String startingIP) {
    this.startingIP = startingIP;
  }

  /**
   * Ending IP when \&quot;IPRANGE\&quot; is used as the ipConditionType
   **/
  public IPConditionDTO endingIP(String endingIP) {
    this.endingIP = endingIP;
    return this;
  }

  
  @ApiModelProperty(value = "Ending IP when \"IPRANGE\" is used as the ipConditionType")
  @JsonProperty("endingIP")
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
    return Objects.equals(ipConditionType, ipCondition.ipConditionType) &&
        Objects.equals(specificIP, ipCondition.specificIP) &&
        Objects.equals(startingIP, ipCondition.startingIP) &&
        Objects.equals(endingIP, ipCondition.endingIP);
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

