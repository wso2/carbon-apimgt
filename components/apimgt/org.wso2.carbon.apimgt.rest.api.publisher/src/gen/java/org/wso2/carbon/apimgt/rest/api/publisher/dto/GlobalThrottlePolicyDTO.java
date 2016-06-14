package org.wso2.carbon.apimgt.rest.api.publisher.dto;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.ThrottleLimitDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ThrottlePolicyDTO;

import io.swagger.annotations.*;
import org.codehaus.jackson.annotate.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class GlobalThrottlePolicyDTO extends ThrottlePolicyDTO {
  
  
  
  private String siddhiQuery = null;
  
  
  private String keyTemplate = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("siddhiQuery")
  public String getSiddhiQuery() {
    return siddhiQuery;
  }
  public void setSiddhiQuery(String siddhiQuery) {
    this.siddhiQuery = siddhiQuery;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("keyTemplate")
  public String getKeyTemplate() {
    return keyTemplate;
  }
  public void setKeyTemplate(String keyTemplate) {
    this.keyTemplate = keyTemplate;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class GlobalThrottlePolicyDTO {\n");
    sb.append("  " + super.toString()).append("\n");
    sb.append("  siddhiQuery: ").append(siddhiQuery).append("\n");
    sb.append("  keyTemplate: ").append(keyTemplate).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
