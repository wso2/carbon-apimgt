package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.BaseAPIDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class CompositeAPIDTO extends BaseAPIDTO {
  
  
  
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
    sb.append("class CompositeAPIDTO {\n");
    sb.append("  " + super.toString()).append("\n");
    sb.append("  applicationId: ").append(applicationId).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
