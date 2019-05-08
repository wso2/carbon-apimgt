package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class LifecycleStateCheckItemsDTO  {
  
  
  
  private List<String> requiredStates = new ArrayList<String>();
  
  
  private String name = null;
  
  
  private Boolean value = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("requiredStates")
  public List<String> getRequiredStates() {
    return requiredStates;
  }
  public void setRequiredStates(List<String> requiredStates) {
    this.requiredStates = requiredStates;
  }

  
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
    sb.append("class LifecycleStateCheckItemsDTO {\n");
    
    sb.append("  requiredStates: ").append(requiredStates).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("  value: ").append(value).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
