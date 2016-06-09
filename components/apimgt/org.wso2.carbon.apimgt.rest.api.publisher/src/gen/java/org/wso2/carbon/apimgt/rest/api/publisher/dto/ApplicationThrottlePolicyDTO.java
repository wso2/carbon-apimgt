package org.wso2.carbon.apimgt.rest.api.publisher.dto;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.QuotaPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ThrottlePolicyDTO;

import io.swagger.annotations.*;
import org.codehaus.jackson.annotate.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ApplicationThrottlePolicyDTO extends ThrottlePolicyDTO {
  
  
  
  private String applicationId = null;
  
  
  private String customAttributes = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("applicationId")
  public String getApplicationId() {
    return applicationId;
  }
  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  
  /**
   * Base64 encoded custom attributes string
   **/
  @ApiModelProperty(value = "Base64 encoded custom attributes string")
  @JsonProperty("customAttributes")
  public String getCustomAttributes() {
    return customAttributes;
  }
  public void setCustomAttributes(String customAttributes) {
    this.customAttributes = customAttributes;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationThrottlePolicyDTO {\n");
    sb.append("  " + super.toString()).append("\n");
    sb.append("  applicationId: ").append(applicationId).append("\n");
    sb.append("  customAttributes: ").append(customAttributes).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
