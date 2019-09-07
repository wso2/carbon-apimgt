package org.wso2.carbon.apimgt.rest.api.admin.dto;

import io.swagger.annotations.ApiModel;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;



/**
 * Throttling Conditions
 **/

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = org.wso2.carbon.apimgt.rest.api.admin.dto.HeaderConditionDTO.class, name = "HeaderCondition"),
    @JsonSubTypes.Type(value = org.wso2.carbon.apimgt.rest.api.admin.dto.IPConditionDTO.class, name = "IPCondition"),
    @JsonSubTypes.Type(value = org.wso2.carbon.apimgt.rest.api.admin.dto.JWTClaimsConditionDTO.class, name = "JWTClaimsCondition"),
    @JsonSubTypes.Type(value = org.wso2.carbon.apimgt.rest.api.admin.dto.QueryParameterConditionDTO.class, name = "QueryParameterCondition"),
})
@ApiModel(description = "Throttling Conditions")
public class ThrottleConditionDTO  {
  
  
  public enum TypeEnum {
     HeaderCondition,  IPCondition,  JWTClaimsCondition,  QueryParameterCondition, 
  };
  @NotNull
  private TypeEnum type = null;
  
  
  private Boolean invertCondition = false;

  
  /**
   * Type of the thottling condition.\nAllowed values are HeaderCondition, IPCondition, JWTClaimsCondition, QueryParameterCondition\n
   **/
  @ApiModelProperty(required = true, value = "Type of the thottling condition.\nAllowed values are HeaderCondition, IPCondition, JWTClaimsCondition, QueryParameterCondition\n")
  @JsonProperty("type")
  public TypeEnum getType() {
    return type;
  }
  public void setType(TypeEnum type) {
    this.type = type;
  }

  
  /**
   * Specifies whether inversion of the condition to be matched against the request.\n\n**Note:** When you add conditional groups for advanced throttling policies, this paramater should have the same value (`true` or `false`)\nfor the same type of conditional group.\n
   **/
  @ApiModelProperty(value = "Specifies whether inversion of the condition to be matched against the request.\n\n**Note:** When you add conditional groups for advanced throttling policies, this paramater should have the same value (`true` or `false`)\nfor the same type of conditional group.\n")
  @JsonProperty("invertCondition")
  public Boolean getInvertCondition() {
    return invertCondition;
  }
  public void setInvertCondition(Boolean invertCondition) {
    this.invertCondition = invertCondition;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ThrottleConditionDTO {\n");
    
    sb.append("  type: ").append(type).append("\n");
    sb.append("  invertCondition: ").append(invertCondition).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
