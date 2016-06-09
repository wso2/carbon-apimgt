package org.wso2.carbon.apimgt.rest.api.publisher.dto;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.ThrottleConditionDTO;

import io.swagger.annotations.*;
import org.codehaus.jackson.annotate.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class DateRangeConditionDTO extends ThrottleConditionDTO {
  
  
  
  private String startingDate = null;
  
  
  private String endingDate = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("startingDate")
  public String getStartingDate() {
    return startingDate;
  }
  public void setStartingDate(String startingDate) {
    this.startingDate = startingDate;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("endingDate")
  public String getEndingDate() {
    return endingDate;
  }
  public void setEndingDate(String endingDate) {
    this.endingDate = endingDate;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class DateRangeConditionDTO {\n");
    sb.append("  " + super.toString()).append("\n");
    sb.append("  startingDate: ").append(startingDate).append("\n");
    sb.append("  endingDate: ").append(endingDate).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
