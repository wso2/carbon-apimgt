package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class APIProductOutdatedStatusDTO   {
  
    private Boolean isOutdated = null;

  /**
   * Indicates if an API Product is outdated 
   **/
  public APIProductOutdatedStatusDTO isOutdated(Boolean isOutdated) {
    this.isOutdated = isOutdated;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "Indicates if an API Product is outdated ")
  @JsonProperty("isOutdated")
  public Boolean isIsOutdated() {
    return isOutdated;
  }
  public void setIsOutdated(Boolean isOutdated) {
    this.isOutdated = isOutdated;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIProductOutdatedStatusDTO apIProductOutdatedStatus = (APIProductOutdatedStatusDTO) o;
    return Objects.equals(isOutdated, apIProductOutdatedStatus.isOutdated);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isOutdated);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIProductOutdatedStatusDTO {\n");
    
    sb.append("    isOutdated: ").append(toIndentedString(isOutdated)).append("\n");
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

