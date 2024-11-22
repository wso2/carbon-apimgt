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



public class APIMaxTpsTokenBasedThrottlingConfigurationDTO   {
  
    private Long productionMaxPromptTokenCount = null;
    private Long productionMaxCompletionTokenCount = null;
    private Long productionMaxTotalTokenCount = null;
    private Long sandboxMaxPromptTokenCount = null;
    private Long sandboxMaxCompletionTokenCount = null;
    private Long sandboxMaxTotalTokenCount = null;
    private Boolean isTokenBasedThrottlingEnabled = false;

  /**
   * Maximum prompt token count for production
   **/
  public APIMaxTpsTokenBasedThrottlingConfigurationDTO productionMaxPromptTokenCount(Long productionMaxPromptTokenCount) {
    this.productionMaxPromptTokenCount = productionMaxPromptTokenCount;
    return this;
  }

  
  @ApiModelProperty(value = "Maximum prompt token count for production")
  @JsonProperty("productionMaxPromptTokenCount")
  public Long getProductionMaxPromptTokenCount() {
    return productionMaxPromptTokenCount;
  }
  public void setProductionMaxPromptTokenCount(Long productionMaxPromptTokenCount) {
    this.productionMaxPromptTokenCount = productionMaxPromptTokenCount;
  }

  /**
   * Maximum completion token count for production
   **/
  public APIMaxTpsTokenBasedThrottlingConfigurationDTO productionMaxCompletionTokenCount(Long productionMaxCompletionTokenCount) {
    this.productionMaxCompletionTokenCount = productionMaxCompletionTokenCount;
    return this;
  }

  
  @ApiModelProperty(value = "Maximum completion token count for production")
  @JsonProperty("productionMaxCompletionTokenCount")
  public Long getProductionMaxCompletionTokenCount() {
    return productionMaxCompletionTokenCount;
  }
  public void setProductionMaxCompletionTokenCount(Long productionMaxCompletionTokenCount) {
    this.productionMaxCompletionTokenCount = productionMaxCompletionTokenCount;
  }

  /**
   * Maximum total token count for production
   **/
  public APIMaxTpsTokenBasedThrottlingConfigurationDTO productionMaxTotalTokenCount(Long productionMaxTotalTokenCount) {
    this.productionMaxTotalTokenCount = productionMaxTotalTokenCount;
    return this;
  }

  
  @ApiModelProperty(value = "Maximum total token count for production")
  @JsonProperty("productionMaxTotalTokenCount")
  public Long getProductionMaxTotalTokenCount() {
    return productionMaxTotalTokenCount;
  }
  public void setProductionMaxTotalTokenCount(Long productionMaxTotalTokenCount) {
    this.productionMaxTotalTokenCount = productionMaxTotalTokenCount;
  }

  /**
   * Maximum prompt token count for sandbox
   **/
  public APIMaxTpsTokenBasedThrottlingConfigurationDTO sandboxMaxPromptTokenCount(Long sandboxMaxPromptTokenCount) {
    this.sandboxMaxPromptTokenCount = sandboxMaxPromptTokenCount;
    return this;
  }

  
  @ApiModelProperty(value = "Maximum prompt token count for sandbox")
  @JsonProperty("sandboxMaxPromptTokenCount")
  public Long getSandboxMaxPromptTokenCount() {
    return sandboxMaxPromptTokenCount;
  }
  public void setSandboxMaxPromptTokenCount(Long sandboxMaxPromptTokenCount) {
    this.sandboxMaxPromptTokenCount = sandboxMaxPromptTokenCount;
  }

  /**
   * Maximum completion token count for sandbox
   **/
  public APIMaxTpsTokenBasedThrottlingConfigurationDTO sandboxMaxCompletionTokenCount(Long sandboxMaxCompletionTokenCount) {
    this.sandboxMaxCompletionTokenCount = sandboxMaxCompletionTokenCount;
    return this;
  }

  
  @ApiModelProperty(value = "Maximum completion token count for sandbox")
  @JsonProperty("sandboxMaxCompletionTokenCount")
  public Long getSandboxMaxCompletionTokenCount() {
    return sandboxMaxCompletionTokenCount;
  }
  public void setSandboxMaxCompletionTokenCount(Long sandboxMaxCompletionTokenCount) {
    this.sandboxMaxCompletionTokenCount = sandboxMaxCompletionTokenCount;
  }

  /**
   * Maximum total token count for sandbox
   **/
  public APIMaxTpsTokenBasedThrottlingConfigurationDTO sandboxMaxTotalTokenCount(Long sandboxMaxTotalTokenCount) {
    this.sandboxMaxTotalTokenCount = sandboxMaxTotalTokenCount;
    return this;
  }

  
  @ApiModelProperty(value = "Maximum total token count for sandbox")
  @JsonProperty("sandboxMaxTotalTokenCount")
  public Long getSandboxMaxTotalTokenCount() {
    return sandboxMaxTotalTokenCount;
  }
  public void setSandboxMaxTotalTokenCount(Long sandboxMaxTotalTokenCount) {
    this.sandboxMaxTotalTokenCount = sandboxMaxTotalTokenCount;
  }

  /**
   * Whether token-based throttling is enabled
   **/
  public APIMaxTpsTokenBasedThrottlingConfigurationDTO isTokenBasedThrottlingEnabled(Boolean isTokenBasedThrottlingEnabled) {
    this.isTokenBasedThrottlingEnabled = isTokenBasedThrottlingEnabled;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Whether token-based throttling is enabled")
  @JsonProperty("isTokenBasedThrottlingEnabled")
  @NotNull
  public Boolean isIsTokenBasedThrottlingEnabled() {
    return isTokenBasedThrottlingEnabled;
  }
  public void setIsTokenBasedThrottlingEnabled(Boolean isTokenBasedThrottlingEnabled) {
    this.isTokenBasedThrottlingEnabled = isTokenBasedThrottlingEnabled;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIMaxTpsTokenBasedThrottlingConfigurationDTO apIMaxTpsTokenBasedThrottlingConfiguration = (APIMaxTpsTokenBasedThrottlingConfigurationDTO) o;
    return Objects.equals(productionMaxPromptTokenCount, apIMaxTpsTokenBasedThrottlingConfiguration.productionMaxPromptTokenCount) &&
        Objects.equals(productionMaxCompletionTokenCount, apIMaxTpsTokenBasedThrottlingConfiguration.productionMaxCompletionTokenCount) &&
        Objects.equals(productionMaxTotalTokenCount, apIMaxTpsTokenBasedThrottlingConfiguration.productionMaxTotalTokenCount) &&
        Objects.equals(sandboxMaxPromptTokenCount, apIMaxTpsTokenBasedThrottlingConfiguration.sandboxMaxPromptTokenCount) &&
        Objects.equals(sandboxMaxCompletionTokenCount, apIMaxTpsTokenBasedThrottlingConfiguration.sandboxMaxCompletionTokenCount) &&
        Objects.equals(sandboxMaxTotalTokenCount, apIMaxTpsTokenBasedThrottlingConfiguration.sandboxMaxTotalTokenCount) &&
        Objects.equals(isTokenBasedThrottlingEnabled, apIMaxTpsTokenBasedThrottlingConfiguration.isTokenBasedThrottlingEnabled);
  }

  @Override
  public int hashCode() {
    return Objects.hash(productionMaxPromptTokenCount, productionMaxCompletionTokenCount, productionMaxTotalTokenCount, sandboxMaxPromptTokenCount, sandboxMaxCompletionTokenCount, sandboxMaxTotalTokenCount, isTokenBasedThrottlingEnabled);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIMaxTpsTokenBasedThrottlingConfigurationDTO {\n");
    
    sb.append("    productionMaxPromptTokenCount: ").append(toIndentedString(productionMaxPromptTokenCount)).append("\n");
    sb.append("    productionMaxCompletionTokenCount: ").append(toIndentedString(productionMaxCompletionTokenCount)).append("\n");
    sb.append("    productionMaxTotalTokenCount: ").append(toIndentedString(productionMaxTotalTokenCount)).append("\n");
    sb.append("    sandboxMaxPromptTokenCount: ").append(toIndentedString(sandboxMaxPromptTokenCount)).append("\n");
    sb.append("    sandboxMaxCompletionTokenCount: ").append(toIndentedString(sandboxMaxCompletionTokenCount)).append("\n");
    sb.append("    sandboxMaxTotalTokenCount: ").append(toIndentedString(sandboxMaxTotalTokenCount)).append("\n");
    sb.append("    isTokenBasedThrottlingEnabled: ").append(toIndentedString(isTokenBasedThrottlingEnabled)).append("\n");
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

