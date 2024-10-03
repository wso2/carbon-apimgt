package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class AIEndpointConfigurationDTO   {
  
    private String sandboxAuthValue = null;
    private String productionAuthValue = null;

  /**
   * Authorization value for the sandbox environment.
   **/
  public AIEndpointConfigurationDTO sandboxAuthValue(String sandboxAuthValue) {
    this.sandboxAuthValue = sandboxAuthValue;
    return this;
  }

  
  @ApiModelProperty(value = "Authorization value for the sandbox environment.")
  @JsonProperty("sandboxAuthValue")
  public String getSandboxAuthValue() {
    return sandboxAuthValue;
  }
  public void setSandboxAuthValue(String sandboxAuthValue) {
    this.sandboxAuthValue = sandboxAuthValue;
  }

  /**
   * Authorization value for the production environment.
   **/
  public AIEndpointConfigurationDTO productionAuthValue(String productionAuthValue) {
    this.productionAuthValue = productionAuthValue;
    return this;
  }

  
  @ApiModelProperty(value = "Authorization value for the production environment.")
  @JsonProperty("productionAuthValue")
  public String getProductionAuthValue() {
    return productionAuthValue;
  }
  public void setProductionAuthValue(String productionAuthValue) {
    this.productionAuthValue = productionAuthValue;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AIEndpointConfigurationDTO aiEndpointConfiguration = (AIEndpointConfigurationDTO) o;
    return Objects.equals(sandboxAuthValue, aiEndpointConfiguration.sandboxAuthValue) &&
        Objects.equals(productionAuthValue, aiEndpointConfiguration.productionAuthValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sandboxAuthValue, productionAuthValue);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AIEndpointConfigurationDTO {\n");
    
    sb.append("    sandboxAuthValue: ").append(toIndentedString(sandboxAuthValue)).append("\n");
    sb.append("    productionAuthValue: ").append(toIndentedString(productionAuthValue)).append("\n");
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

