package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class LifecycleStateInputBeanListDTO  {
  
  
  
  private Boolean isRequired = null;
  
  
  private String regex = null;
  
  
  private String values = null;
  
  
  private String name = null;
  
  
  private String tooltip = null;
  
  
  private String forTarget = null;
  
  
  private String label = null;
  
  
  private String placeHolder = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("isRequired")
  public Boolean getIsRequired() {
    return isRequired;
  }
  public void setIsRequired(Boolean isRequired) {
    this.isRequired = isRequired;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("regex")
  public String getRegex() {
    return regex;
  }
  public void setRegex(String regex) {
    this.regex = regex;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("values")
  public String getValues() {
    return values;
  }
  public void setValues(String values) {
    this.values = values;
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
  @JsonProperty("tooltip")
  public String getTooltip() {
    return tooltip;
  }
  public void setTooltip(String tooltip) {
    this.tooltip = tooltip;
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

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("label")
  public String getLabel() {
    return label;
  }
  public void setLabel(String label) {
    this.label = label;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("placeHolder")
  public String getPlaceHolder() {
    return placeHolder;
  }
  public void setPlaceHolder(String placeHolder) {
    this.placeHolder = placeHolder;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class LifecycleStateInputBeanListDTO {\n");
    
    sb.append("  isRequired: ").append(isRequired).append("\n");
    sb.append("  regex: ").append(regex).append("\n");
    sb.append("  values: ").append(values).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("  tooltip: ").append(tooltip).append("\n");
    sb.append("  forTarget: ").append(forTarget).append("\n");
    sb.append("  label: ").append(label).append("\n");
    sb.append("  placeHolder: ").append(placeHolder).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
