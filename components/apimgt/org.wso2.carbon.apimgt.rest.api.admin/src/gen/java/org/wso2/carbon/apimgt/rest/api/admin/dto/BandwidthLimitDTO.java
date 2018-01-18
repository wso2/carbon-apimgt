package org.wso2.carbon.apimgt.rest.api.admin.dto;

import org.wso2.carbon.apimgt.rest.api.admin.dto.ThrottleLimitDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class BandwidthLimitDTO extends ThrottleLimitDTO {
  
  
  
  private Long dataAmount = null;
  
  
  private String dataUnit = null;

  
  /**
   * Amount of data allowed to be transfered
   **/
  @ApiModelProperty(value = "Amount of data allowed to be transfered")
  @JsonProperty("dataAmount")
  public Long getDataAmount() {
    return dataAmount;
  }
  public void setDataAmount(Long dataAmount) {
    this.dataAmount = dataAmount;
  }

  
  /**
   * Unit of data allowed to be transfered. Allowed values are \"KB\", \"MB\" and \"GB\"
   **/
  @ApiModelProperty(value = "Unit of data allowed to be transfered. Allowed values are \"KB\", \"MB\" and \"GB\"")
  @JsonProperty("dataUnit")
  public String getDataUnit() {
    return dataUnit;
  }
  public void setDataUnit(String dataUnit) {
    this.dataUnit = dataUnit;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class BandwidthLimitDTO {\n");
    sb.append("  " + super.toString()).append("\n");
    sb.append("  dataAmount: ").append(dataAmount).append("\n");
    sb.append("  dataUnit: ").append(dataUnit).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
