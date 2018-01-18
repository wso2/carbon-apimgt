package org.wso2.carbon.apimgt.rest.api.admin.dto;

import org.wso2.carbon.apimgt.rest.api.admin.dto.ThrottleConditionDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class HeaderConditionDTO extends ThrottleConditionDTO {
  
  
  
  private String headerName = null;
  
  
  private String headerValue = null;

  
  /**
   * Name of the header
   **/
  @ApiModelProperty(value = "Name of the header")
  @JsonProperty("headerName")
  public String getHeaderName() {
    return headerName;
  }
  public void setHeaderName(String headerName) {
    this.headerName = headerName;
  }

  
  /**
   * Value of the header
   **/
  @ApiModelProperty(value = "Value of the header")
  @JsonProperty("headerValue")
  public String getHeaderValue() {
    return headerValue;
  }
  public void setHeaderValue(String headerValue) {
    this.headerValue = headerValue;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class HeaderConditionDTO {\n");
    sb.append("  " + super.toString()).append("\n");
    sb.append("  headerName: ").append(headerName).append("\n");
    sb.append("  headerValue: ").append(headerValue).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
