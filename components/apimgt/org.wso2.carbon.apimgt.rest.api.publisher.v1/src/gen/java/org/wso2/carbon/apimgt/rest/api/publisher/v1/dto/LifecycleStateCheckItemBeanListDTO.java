package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LifecycleStatePermissionBeansDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LifecycleStateValidationBeansDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class LifecycleStateCheckItemBeanListDTO  {
  
  
  
  private String name = null;
  
  
  private List<LifecycleStateValidationBeansDTO> validationBeans = new ArrayList<LifecycleStateValidationBeansDTO>();
  
  
  private List<LifecycleStatePermissionBeansDTO> permissionBeans = new ArrayList<LifecycleStatePermissionBeansDTO>();
  
  
  private List<String> targets = new ArrayList<String>();
  
  
  private Boolean value = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("validationBeans")
  public List<LifecycleStateValidationBeansDTO> getValidationBeans() {
    return validationBeans;
  }
  public void setValidationBeans(List<LifecycleStateValidationBeansDTO> validationBeans) {
    this.validationBeans = validationBeans;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("permissionBeans")
  public List<LifecycleStatePermissionBeansDTO> getPermissionBeans() {
    return permissionBeans;
  }
  public void setPermissionBeans(List<LifecycleStatePermissionBeansDTO> permissionBeans) {
    this.permissionBeans = permissionBeans;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("targets")
  public List<String> getTargets() {
    return targets;
  }
  public void setTargets(List<String> targets) {
    this.targets = targets;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("value")
  public Boolean getValue() {
    return value;
  }
  public void setValue(Boolean value) {
    this.value = value;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class LifecycleStateCheckItemBeanListDTO {\n");
    
    sb.append("  name: ").append(name).append("\n");
    sb.append("  validationBeans: ").append(validationBeans).append("\n");
    sb.append("  permissionBeans: ").append(permissionBeans).append("\n");
    sb.append("  targets: ").append(targets).append("\n");
    sb.append("  value: ").append(value).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
