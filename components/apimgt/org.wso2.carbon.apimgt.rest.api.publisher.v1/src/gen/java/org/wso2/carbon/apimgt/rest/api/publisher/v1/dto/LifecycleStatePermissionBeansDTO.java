package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class LifecycleStatePermissionBeansDTO  {
  
  
  
  private List<String> roles = new ArrayList<String>();
  
  
  private String forTarget = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("roles")
  public List<String> getRoles() {
    return roles;
  }
  public void setRoles(List<String> roles) {
    this.roles = roles;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("forTarget")
  public String getForTarget() {
    return forTarget;
  }
  public void setForTarget(String forTarget) {
    this.forTarget = forTarget;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class LifecycleStatePermissionBeansDTO {\n");
    
    sb.append("  roles: ").append(roles).append("\n");
    sb.append("  forTarget: ").append(forTarget).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
