package org.wso2.carbon.apimgt.rest.api.admin.dto;

import org.wso2.carbon.apimgt.rest.api.admin.dto.ThrottleConditionDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class QueryParameterConditionDTO extends ThrottleConditionDTO {
  
  
  
  private String parameterName = null;
  
  
  private String parameterValue = null;

  
  /**
   * Name of the query parameter
   **/
  @ApiModelProperty(value = "Name of the query parameter")
  @JsonProperty("parameterName")
  public String getParameterName() {
    return parameterName;
  }
  public void setParameterName(String parameterName) {
    this.parameterName = parameterName;
  }

  
  /**
   * Value of the query parameter to be matched
   **/
  @ApiModelProperty(value = "Value of the query parameter to be matched")
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
