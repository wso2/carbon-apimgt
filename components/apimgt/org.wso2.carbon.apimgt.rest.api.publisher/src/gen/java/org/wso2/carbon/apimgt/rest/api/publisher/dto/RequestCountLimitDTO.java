package org.wso2.carbon.apimgt.rest.api.publisher.dto;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.ThrottleLimitDTO;

import io.swagger.annotations.*;
import org.codehaus.jackson.annotate.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class RequestCountLimitDTO extends ThrottleLimitDTO {
  
  
  
  private Long requestCount = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("requestCount")
  public Long getRequestCount() {
    return requestCount;
  }
  public void setRequestCount(Long requestCount) {
    this.requestCount = requestCount;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class RequestCountLimitDTO {\n");
    sb.append("  " + super.toString()).append("\n");
    sb.append("  requestCount: ").append(requestCount).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
