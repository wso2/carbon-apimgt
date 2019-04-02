package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.BaseAPIInfoDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class CompositeAPIInfoDTO extends BaseAPIInfoDTO {
  
  
  
  private String applicationId = null;

  
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

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class CompositeAPIInfoDTO {\n");
    sb.append("  " + super.toString()).append("\n");
    sb.append("  applicationId: ").append(applicationId).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
