package org.wso2.carbon.apimgt.rest.api.publisher.dto;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.ThrottleConditionDTO;

import io.swagger.annotations.*;
import org.codehaus.jackson.annotate.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class QueryParameterConditionDTO extends ThrottleConditionDTO {
  
  
  
  private String parameterName = null;
  
  
  private String parameterValue = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("parameterName")
  public String getParameterName() {
    return parameterName;
  }
  public void setParameterName(String parameterName) {
    this.parameterName = parameterName;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("parameterValue")
  public String getParameterValue() {
    return parameterValue;
  }
  public void setParameterValue(String parameterValue) {
    this.parameterValue = parameterValue;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class QueryParameterConditionDTO {\n");
    sb.append("  " + super.toString()).append("\n");
    sb.append("  parameterName: ").append(parameterName).append("\n");
    sb.append("  parameterValue: ").append(parameterValue).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
