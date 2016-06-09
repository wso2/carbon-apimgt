package org.wso2.carbon.apimgt.rest.api.publisher.dto;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.ThrottleConditionDTO;

import io.swagger.annotations.*;
import org.codehaus.jackson.annotate.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class HTTPVerbConditionDTO extends ThrottleConditionDTO {
  
  
  
  private String httpVerb = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("httpVerb")
  public String getHttpVerb() {
    return httpVerb;
  }
  public void setHttpVerb(String httpVerb) {
    this.httpVerb = httpVerb;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class HTTPVerbConditionDTO {\n");
    sb.append("  " + super.toString()).append("\n");
    sb.append("  httpVerb: ").append(httpVerb).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
