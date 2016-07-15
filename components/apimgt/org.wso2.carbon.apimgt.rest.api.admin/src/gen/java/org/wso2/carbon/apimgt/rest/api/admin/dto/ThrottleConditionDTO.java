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
   **/
  @ApiModelProperty(required = true, value = "")
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
