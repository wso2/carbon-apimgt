package org.wso2.carbon.apimgt.rest.api.admin.dto;

import org.wso2.carbon.apimgt.rest.api.admin.dto.ThrottleConditionDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class IPConditionDTO extends ThrottleConditionDTO {
  
  
  public enum IpConditionTypeEnum {
     IPRange,  IPSpecific, 
  };
  
  private IpConditionTypeEnum ipConditionType = null;
  
  
  private String specificIP = null;
  
  
  private String startingIP = null;
  
  
  private String endingIP = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("ipConditionType")
  public IpConditionTypeEnum getIpConditionType() {
    return ipConditionType;
  }
  public void setIpConditionType(IpConditionTypeEnum ipConditionType) {
    this.ipConditionType = ipConditionType;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("specificIP")
  public String getSpecificIP() {
    return specificIP;
  }
  public void setSpecificIP(String specificIP) {
    this.specificIP = specificIP;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("startingIP")
  public String getStartingIP() {
    return startingIP;
  }
  public void setStartingIP(String startingIP) {
    this.startingIP = startingIP;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("endingIP")
  public String getEndingIP() {
    return endingIP;
  }
  public void setEndingIP(String endingIP) {
    this.endingIP = endingIP;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class IPConditionDTO {\n");
    sb.append("  " + super.toString()).append("\n");
    sb.append("  ipConditionType: ").append(ipConditionType).append("\n");
    sb.append("  specificIP: ").append(specificIP).append("\n");
    sb.append("  startingIP: ").append(startingIP).append("\n");
    sb.append("  endingIP: ").append(endingIP).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
