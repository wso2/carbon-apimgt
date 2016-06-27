package org.wso2.carbon.apimgt.rest.api.admin.dto;

import org.wso2.carbon.apimgt.rest.api.admin.dto.ThrottleLimitDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ThrottlePolicyDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ApplicationThrottlePolicyDTO extends ThrottlePolicyDTO {
  

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationThrottlePolicyDTO {\n");
    sb.append("  " + super.toString()).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
