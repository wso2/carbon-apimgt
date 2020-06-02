package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ThrottleConditionBaseDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class QueryParameterConditionDTO   {
  
    private Boolean invertCondition = false;
    private String parameterName = null;
    private String parameterValue = null;

  /**
   * Specifies whether inversion of the condition to be matched against the request.  **Note:** When you add conditional groups for advanced throttling policies, this paramater should have the same value (&#39;true&#39; or &#39;false&#39;) for the same type of conditional group. 
   **/
  public QueryParameterConditionDTO invertCondition(Boolean invertCondition) {
    this.invertCondition = invertCondition;
    return this;
  }

  
  @ApiModelProperty(value = "Specifies whether inversion of the condition to be matched against the request.  **Note:** When you add conditional groups for advanced throttling policies, this paramater should have the same value ('true' or 'false') for the same type of conditional group. ")
  @JsonProperty("invertCondition")
  public Boolean isInvertCondition() {
    return invertCondition;
  }
  public void setInvertCondition(Boolean invertCondition) {
    this.invertCondition = invertCondition;
  }

  /**
   * Name of the query parameter
   **/
  public QueryParameterConditionDTO parameterName(String parameterName) {
    this.parameterName = parameterName;
    return this;
  }

  
  @ApiModelProperty(value = "Name of the query parameter")
  @JsonProperty("parameterName")
  public String getParameterName() {
    return parameterName;
  }
  public void setParameterName(String parameterName) {
    this.parameterName = parameterName;
  }

  /**
   * Value of the query parameter to be matched
   **/
  public QueryParameterConditionDTO parameterValue(String parameterValue) {
    this.parameterValue = parameterValue;
    return this;
  }

  
  @ApiModelProperty(value = "Value of the query parameter to be matched")
  @JsonProperty("parameterValue")
  public String getParameterValue() {
    return parameterValue;
  }
  public void setParameterValue(String parameterValue) {
    this.parameterValue = parameterValue;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    QueryParameterConditionDTO queryParameterCondition = (QueryParameterConditionDTO) o;
    return Objects.equals(invertCondition, queryParameterCondition.invertCondition) &&
        Objects.equals(parameterName, queryParameterCondition.parameterName) &&
        Objects.equals(parameterValue, queryParameterCondition.parameterValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(invertCondition, parameterName, parameterValue);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class QueryParameterConditionDTO {\n");
    
    sb.append("    invertCondition: ").append(toIndentedString(invertCondition)).append("\n");
    sb.append("    parameterName: ").append(toIndentedString(parameterName)).append("\n");
    sb.append("    parameterValue: ").append(toIndentedString(parameterValue)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

