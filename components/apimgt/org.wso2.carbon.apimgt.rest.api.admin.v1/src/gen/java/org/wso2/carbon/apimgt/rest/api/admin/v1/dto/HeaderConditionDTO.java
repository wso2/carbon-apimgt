package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class HeaderConditionDTO   {
  
    private String headerName = null;
    private String headerValue = null;

  /**
   * Name of the header
   **/
  public HeaderConditionDTO headerName(String headerName) {
    this.headerName = headerName;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Name of the header")
  @JsonProperty("headerName")
  @NotNull
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

  
  @ApiModelProperty(required = true, value = "Value of the header")
  @JsonProperty("headerValue")
  @NotNull
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
    return Objects.equals(headerName, headerCondition.headerName) &&
        Objects.equals(headerValue, headerCondition.headerValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(headerName, headerValue);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class HeaderConditionDTO {\n");
    
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

