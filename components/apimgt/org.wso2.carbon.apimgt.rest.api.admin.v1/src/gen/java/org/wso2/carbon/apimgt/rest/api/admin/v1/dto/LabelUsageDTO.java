package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.LabelUsageApisDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class LabelUsageDTO   {
  
    private LabelUsageApisDTO apis = null;

  /**
   **/
  public LabelUsageDTO apis(LabelUsageApisDTO apis) {
    this.apis = apis;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("apis")
  public LabelUsageApisDTO getApis() {
    return apis;
  }
  public void setApis(LabelUsageApisDTO apis) {
    this.apis = apis;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LabelUsageDTO labelUsage = (LabelUsageDTO) o;
    return Objects.equals(apis, labelUsage.apis);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apis);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LabelUsageDTO {\n");
    
    sb.append("    apis: ").append(toIndentedString(apis)).append("\n");
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

