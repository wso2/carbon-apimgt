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



public class ApiQuotaResponseDTO   {
  
    private Boolean isApiThrottled = null;

  /**
   * Flag indicating the ratelimit quota is reached or not
   **/
  public ApiQuotaResponseDTO isApiThrottled(Boolean isApiThrottled) {
    this.isApiThrottled = isApiThrottled;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Flag indicating the ratelimit quota is reached or not")
  @JsonProperty("isApiThrottled")
  @NotNull
  public Boolean isIsApiThrottled() {
    return isApiThrottled;
  }
  public void setIsApiThrottled(Boolean isApiThrottled) {
    this.isApiThrottled = isApiThrottled;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiQuotaResponseDTO apiQuotaResponse = (ApiQuotaResponseDTO) o;
    return Objects.equals(isApiThrottled, apiQuotaResponse.isApiThrottled);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isApiThrottled);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiQuotaResponseDTO {\n");
    
    sb.append("    isApiThrottled: ").append(toIndentedString(isApiThrottled)).append("\n");
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

