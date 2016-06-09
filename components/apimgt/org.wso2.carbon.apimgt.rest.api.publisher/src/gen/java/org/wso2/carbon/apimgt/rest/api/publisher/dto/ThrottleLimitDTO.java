package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import io.swagger.annotations.*;
import org.codehaus.jackson.annotate.*;

import javax.validation.constraints.NotNull;




@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = org.wso2.carbon.apimgt.rest.api.publisher.dto.RequestCountLimitDTO.class, name = "RequestCountLimit"),
    @JsonSubTypes.Type(value = org.wso2.carbon.apimgt.rest.api.publisher.dto.BandwidthLimitDTO.class, name = "BandwidthLimit"),
})
@ApiModel(description = "")
public class ThrottleLimitDTO  {
  
  
  @NotNull
  private String type = null;
  
  
  private String timeUnit = null;
  
  
  private Integer unitTime = null;

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("timeUnit")
  public String getTimeUnit() {
    return timeUnit;
  }
  public void setTimeUnit(String timeUnit) {
    this.timeUnit = timeUnit;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("unitTime")
  public Integer getUnitTime() {
    return unitTime;
  }
  public void setUnitTime(Integer unitTime) {
    this.unitTime = unitTime;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ThrottleLimitDTO {\n");
    
    sb.append("  type: ").append(type).append("\n");
    sb.append("  timeUnit: ").append(timeUnit).append("\n");
    sb.append("  unitTime: ").append(unitTime).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
