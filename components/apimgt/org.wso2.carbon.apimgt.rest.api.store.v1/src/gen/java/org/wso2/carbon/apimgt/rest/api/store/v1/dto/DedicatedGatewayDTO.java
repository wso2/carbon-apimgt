package org.wso2.carbon.apimgt.rest.api.store.v1.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class DedicatedGatewayDTO  {
  
  
  @NotNull
  private Boolean isEnabled = null;

  
  /**
   * This attribute declares whether an API should have a dedicated Gateway or not.\n
   **/
  @ApiModelProperty(required = true, value = "This attribute declares whether an API should have a dedicated Gateway or not.\n")
  @JsonProperty("isEnabled")
  public Boolean getIsEnabled() {
    return isEnabled;
  }
  public void setIsEnabled(Boolean isEnabled) {
    this.isEnabled = isEnabled;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class DedicatedGatewayDTO {\n");
    
    sb.append("  isEnabled: ").append(isEnabled).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
