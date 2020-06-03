package org.wso2.carbon.apimgt.rest.api.admin.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class TokenValidationDTO  {
  
  
  
  private Boolean enable = true;
  
  public enum TypeEnum {
     REGEX,  JWT,  CUSTOM, 
  };
  
  private TypeEnum type = null;
  
  
  private Object value = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("enable")
  public Boolean getEnable() {
    return enable;
  }
  public void setEnable(Boolean enable) {
    this.enable = enable;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("type")
  public TypeEnum getType() {
    return type;
  }
  public void setType(TypeEnum type) {
    this.type = type;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("value")
  public Object getValue() {
    return value;
  }
  public void setValue(Object value) {
    this.value = value;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class TokenValidationDTO {\n");
    
    sb.append("  enable: ").append(enable).append("\n");
    sb.append("  type: ").append(type).append("\n");
    sb.append("  value: ").append(value).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
