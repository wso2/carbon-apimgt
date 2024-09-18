package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class TokenBaseThrottlingCountHolderDTO   {
  
    private Long productionMaxPromptTokenCount = null;
    private Long productionMaxCompletionTokenCount = null;
    private Long productionMaxTotalTokenCount = null;
    private Long sandboxMaxPromptTokenCount = null;
    private Long sandboxMaxCompletionTokenCount = null;
    private Long sandboxMaxTotalTokenCount = null;
    private Boolean isTokenBasedThrottlingEnabled = null;

  /**
   * Maximum allowed prompt token count for production.
   **/
  public TokenBaseThrottlingCountHolderDTO productionMaxPromptTokenCount(Long productionMaxPromptTokenCount) {
    this.productionMaxPromptTokenCount = productionMaxPromptTokenCount;
    return this;
  }

  
  @ApiModelProperty(value = "Maximum allowed prompt token count for production.")
  @JsonProperty("productionMaxPromptTokenCount")
  public Long getProductionMaxPromptTokenCount() {
    return productionMaxPromptTokenCount;
  }
  public void setProductionMaxPromptTokenCount(Long productionMaxPromptTokenCount) {
    this.productionMaxPromptTokenCount = productionMaxPromptTokenCount;
  }

  /**
   * Maximum allowed completion token count for production.
   **/
  public TokenBaseThrottlingCountHolderDTO productionMaxCompletionTokenCount(Long productionMaxCompletionTokenCount) {
    this.productionMaxCompletionTokenCount = productionMaxCompletionTokenCount;
    return this;
  }

  
  @ApiModelProperty(value = "Maximum allowed completion token count for production.")
  @JsonProperty("productionMaxCompletionTokenCount")
  public Long getProductionMaxCompletionTokenCount() {
    return productionMaxCompletionTokenCount;
  }
  public void setProductionMaxCompletionTokenCount(Long productionMaxCompletionTokenCount) {
    this.productionMaxCompletionTokenCount = productionMaxCompletionTokenCount;
  }

  /**
   * Maximum total token count allowed for production.
   **/
  public TokenBaseThrottlingCountHolderDTO productionMaxTotalTokenCount(Long productionMaxTotalTokenCount) {
    this.productionMaxTotalTokenCount = productionMaxTotalTokenCount;
    return this;
  }

  
  @ApiModelProperty(value = "Maximum total token count allowed for production.")
  @JsonProperty("productionMaxTotalTokenCount")
  public Long getProductionMaxTotalTokenCount() {
    return productionMaxTotalTokenCount;
  }
  public void setProductionMaxTotalTokenCount(Long productionMaxTotalTokenCount) {
    this.productionMaxTotalTokenCount = productionMaxTotalTokenCount;
  }

  /**
   * Maximum allowed prompt token count for sandbox.
   **/
  public TokenBaseThrottlingCountHolderDTO sandboxMaxPromptTokenCount(Long sandboxMaxPromptTokenCount) {
    this.sandboxMaxPromptTokenCount = sandboxMaxPromptTokenCount;
    return this;
  }

  
  @ApiModelProperty(value = "Maximum allowed prompt token count for sandbox.")
  @JsonProperty("sandboxMaxPromptTokenCount")
  public Long getSandboxMaxPromptTokenCount() {
    return sandboxMaxPromptTokenCount;
  }
  public void setSandboxMaxPromptTokenCount(Long sandboxMaxPromptTokenCount) {
    this.sandboxMaxPromptTokenCount = sandboxMaxPromptTokenCount;
  }

  /**
   * Maximum allowed completion token count for sandbox.
   **/
  public TokenBaseThrottlingCountHolderDTO sandboxMaxCompletionTokenCount(Long sandboxMaxCompletionTokenCount) {
    this.sandboxMaxCompletionTokenCount = sandboxMaxCompletionTokenCount;
    return this;
  }

  
  @ApiModelProperty(value = "Maximum allowed completion token count for sandbox.")
  @JsonProperty("sandboxMaxCompletionTokenCount")
  public Long getSandboxMaxCompletionTokenCount() {
    return sandboxMaxCompletionTokenCount;
  }
  public void setSandboxMaxCompletionTokenCount(Long sandboxMaxCompletionTokenCount) {
    this.sandboxMaxCompletionTokenCount = sandboxMaxCompletionTokenCount;
  }

  /**
   * Maximum total token count allowed for sandbox.
   **/
  public TokenBaseThrottlingCountHolderDTO sandboxMaxTotalTokenCount(Long sandboxMaxTotalTokenCount) {
    this.sandboxMaxTotalTokenCount = sandboxMaxTotalTokenCount;
    return this;
  }

  
  @ApiModelProperty(value = "Maximum total token count allowed for sandbox.")
  @JsonProperty("sandboxMaxTotalTokenCount")
  public Long getSandboxMaxTotalTokenCount() {
    return sandboxMaxTotalTokenCount;
  }
  public void setSandboxMaxTotalTokenCount(Long sandboxMaxTotalTokenCount) {
    this.sandboxMaxTotalTokenCount = sandboxMaxTotalTokenCount;
  }

  /**
   * Whether token-based throttling is enabled.
   **/
  public TokenBaseThrottlingCountHolderDTO isTokenBasedThrottlingEnabled(Boolean isTokenBasedThrottlingEnabled) {
    this.isTokenBasedThrottlingEnabled = isTokenBasedThrottlingEnabled;
    return this;
  }

  
  @ApiModelProperty(value = "Whether token-based throttling is enabled.")
  @JsonProperty("isTokenBasedThrottlingEnabled")
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
    TokenBaseThrottlingCountHolderDTO tokenBaseThrottlingCountHolder = (TokenBaseThrottlingCountHolderDTO) o;
    return Objects.equals(productionMaxPromptTokenCount, tokenBaseThrottlingCountHolder.productionMaxPromptTokenCount) &&
        Objects.equals(productionMaxCompletionTokenCount, tokenBaseThrottlingCountHolder.productionMaxCompletionTokenCount) &&
        Objects.equals(productionMaxTotalTokenCount, tokenBaseThrottlingCountHolder.productionMaxTotalTokenCount) &&
        Objects.equals(sandboxMaxPromptTokenCount, tokenBaseThrottlingCountHolder.sandboxMaxPromptTokenCount) &&
        Objects.equals(sandboxMaxCompletionTokenCount, tokenBaseThrottlingCountHolder.sandboxMaxCompletionTokenCount) &&
        Objects.equals(sandboxMaxTotalTokenCount, tokenBaseThrottlingCountHolder.sandboxMaxTotalTokenCount) &&
        Objects.equals(isTokenBasedThrottlingEnabled, tokenBaseThrottlingCountHolder.isTokenBasedThrottlingEnabled);
  }

  @Override
  public int hashCode() {
    return Objects.hash(productionMaxPromptTokenCount, productionMaxCompletionTokenCount, productionMaxTotalTokenCount, sandboxMaxPromptTokenCount, sandboxMaxCompletionTokenCount, sandboxMaxTotalTokenCount, isTokenBasedThrottlingEnabled);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TokenBaseThrottlingCountHolderDTO {\n");
    
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

