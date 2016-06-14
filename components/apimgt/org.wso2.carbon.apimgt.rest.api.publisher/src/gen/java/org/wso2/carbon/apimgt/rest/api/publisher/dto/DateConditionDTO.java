package org.wso2.carbon.apimgt.rest.api.publisher.dto;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.ThrottleConditionDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class DateConditionDTO extends ThrottleConditionDTO {
  
  
  
  private String specificDate = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("specificDate")
  public String getSpecificDate() {
    return specificDate;
  }
  public void setSpecificDate(String specificDate) {
    this.specificDate = specificDate;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class DateConditionDTO {\n");
    sb.append("  " + super.toString()).append("\n");
    sb.append("  specificDate: ").append(specificDate).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
