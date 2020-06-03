package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ThrottleConditionBaseDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class HeaderConditionDTO   {
  
    private Boolean invertCondition = false;
    private String headerName = null;
    private String headerValue = null;

  /**
   * Specifies whether inversion of the condition to be matched against the request.  **Note:** When you add conditional groups for advanced throttling policies, this paramater should have the same value (&#39;true&#39; or &#39;false&#39;) for the same type of conditional group. 
   **/
  public HeaderConditionDTO invertCondition(Boolean invertCondition) {
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
   * Name of the header
   **/
  public HeaderConditionDTO headerName(String headerName) {
    this.headerName = headerName;
    return this;
  }

  
  @ApiModelProperty(value = "Name of the header")
  @JsonProperty("headerName")
  public String getHeaderName() {
    return headerName;
  }
  public void setHeaderName(String headerName) {
    this.headerName = headerName;
  }

  /**
   * Value of the header
   **/
  public HeaderConditionDTO headerValue(String headerValue) {
    this.headerValue = headerValue;
    return this;
  }

  
  @ApiModelProperty(value = "Value of the header")
  @JsonProperty("headerValue")
  public String getHeaderValue() {
    return headerValue;
  }
  public void setHeaderValue(String headerValue) {
    this.headerValue = headerValue;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    HeaderConditionDTO headerCondition = (HeaderConditionDTO) o;
    return Objects.equals(invertCondition, headerCondition.invertCondition) &&
        Objects.equals(headerName, headerCondition.headerName) &&
        Objects.equals(headerValue, headerCondition.headerValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(invertCondition, headerName, headerValue);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class HeaderConditionDTO {\n");
    
    sb.append("    invertCondition: ").append(toIndentedString(invertCondition)).append("\n");
    sb.append("    headerName: ").append(toIndentedString(headerName)).append("\n");
    sb.append("    headerValue: ").append(toIndentedString(headerValue)).append("\n");
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

