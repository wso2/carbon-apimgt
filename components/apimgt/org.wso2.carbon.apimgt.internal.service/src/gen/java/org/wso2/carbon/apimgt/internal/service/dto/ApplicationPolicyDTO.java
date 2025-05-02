package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.internal.service.dto.BurstLimitDTO;
import org.wso2.carbon.apimgt.internal.service.dto.PolicyDTO;
import org.wso2.carbon.apimgt.internal.service.dto.ThrottleLimitDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class ApplicationPolicyDTO extends PolicyDTO  {
  
    private ThrottleLimitDTO defaultLimit = null;
    private BurstLimitDTO burstLimit = null;

  /**
   **/
  public ApplicationPolicyDTO defaultLimit(ThrottleLimitDTO defaultLimit) {
    this.defaultLimit = defaultLimit;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("defaultLimit")
  public ThrottleLimitDTO getDefaultLimit() {
    return defaultLimit;
  }
  public void setDefaultLimit(ThrottleLimitDTO defaultLimit) {
    this.defaultLimit = defaultLimit;
  }

  /**
   **/
  public ApplicationPolicyDTO burstLimit(BurstLimitDTO burstLimit) {
    this.burstLimit = burstLimit;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("burstLimit")
  public BurstLimitDTO getBurstLimit() {
    return burstLimit;
  }
  public void setBurstLimit(BurstLimitDTO burstLimit) {
    this.burstLimit = burstLimit;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApplicationPolicyDTO applicationPolicy = (ApplicationPolicyDTO) o;
    return Objects.equals(defaultLimit, applicationPolicy.defaultLimit) &&
        Objects.equals(burstLimit, applicationPolicy.burstLimit);
  }

  @Override
  public int hashCode() {
    return Objects.hash(defaultLimit, burstLimit);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationPolicyDTO {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    defaultLimit: ").append(toIndentedString(defaultLimit)).append("\n");
    sb.append("    burstLimit: ").append(toIndentedString(burstLimit)).append("\n");
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

