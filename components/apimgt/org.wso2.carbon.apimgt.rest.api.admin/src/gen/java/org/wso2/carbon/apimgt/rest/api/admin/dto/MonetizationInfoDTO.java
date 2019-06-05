package org.wso2.carbon.apimgt.rest.api.admin.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class MonetizationInfoDTO  {
  
  
  public enum MonetizationPlanEnum {
     FixedRate,  DynamicRate, 
  };
  @NotNull
  private MonetizationPlanEnum monetizationPlan = null;
  
  @NotNull
  private Map<String, String> properties = new HashMap<String, String>();

  
  /**
   * Flag to indicate the monetization plan
   **/
  @ApiModelProperty(required = true, value = "Flag to indicate the monetization plan")
  @JsonProperty("monetizationPlan")
  public MonetizationPlanEnum getMonetizationPlan() {
    return monetizationPlan;
  }
  public void setMonetizationPlan(MonetizationPlanEnum monetizationPlan) {
    this.monetizationPlan = monetizationPlan;
  }

  
  /**
   * Map of custom properties related to each monetization plan
   **/
  @ApiModelProperty(required = true, value = "Map of custom properties related to each monetization plan")
  @JsonProperty("properties")
  public Map<String, String> getProperties() {
    return properties;
  }
  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class MonetizationInfoDTO {\n");
    
    sb.append("  monetizationPlan: ").append(monetizationPlan).append("\n");
    sb.append("  properties: ").append(properties).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
