package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class LifecycleStateValidationBeansDTO  {
  
  
  
  private String targetName = null;
  
  
  private Object classObject = null;
  
  
  private String customMessage = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("targetName")
  public String getTargetName() {
    return targetName;
  }
  public void setTargetName(String targetName) {
    this.targetName = targetName;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("classObject")
  public Object getClassObject() {
    return classObject;
  }
  public void setClassObject(Object classObject) {
    this.classObject = classObject;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("customMessage")
  public String getCustomMessage() {
    return customMessage;
  }
  public void setCustomMessage(String customMessage) {
    this.customMessage = customMessage;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class LifecycleStateValidationBeansDTO {\n");
    
    sb.append("  targetName: ").append(targetName).append("\n");
    sb.append("  classObject: ").append(classObject).append("\n");
    sb.append("  customMessage: ").append(customMessage).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
