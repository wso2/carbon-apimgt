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



public class EnvironmentPropertiesDTO   {
  
    private String productionEndpoint = null;
    private String sandboxEndpoint = null;

  /**
   * Production endpoints
   **/
  public EnvironmentPropertiesDTO productionEndpoint(String productionEndpoint) {
    this.productionEndpoint = productionEndpoint;
    return this;
  }

  
  @ApiModelProperty(example = "https://localhost:9443/am/sample/pizzashack/v1/api/", value = "Production endpoints")
  @JsonProperty("productionEndpoint")
  public String getProductionEndpoint() {
    return productionEndpoint;
  }
  public void setProductionEndpoint(String productionEndpoint) {
    this.productionEndpoint = productionEndpoint;
  }

  /**
   * Sandbox endpoints
   **/
  public EnvironmentPropertiesDTO sandboxEndpoint(String sandboxEndpoint) {
    this.sandboxEndpoint = sandboxEndpoint;
    return this;
  }

  
  @ApiModelProperty(example = "https://localhost:9443/am/sample/pizzashack/v1/api/", value = "Sandbox endpoints")
  @JsonProperty("sandboxEndpoint")
  public String getSandboxEndpoint() {
    return sandboxEndpoint;
  }
  public void setSandboxEndpoint(String sandboxEndpoint) {
    this.sandboxEndpoint = sandboxEndpoint;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EnvironmentPropertiesDTO environmentProperties = (EnvironmentPropertiesDTO) o;
    return Objects.equals(productionEndpoint, environmentProperties.productionEndpoint) &&
        Objects.equals(sandboxEndpoint, environmentProperties.sandboxEndpoint);
  }

  @Override
  public int hashCode() {
    return Objects.hash(productionEndpoint, sandboxEndpoint);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EnvironmentPropertiesDTO {\n");
    
    sb.append("    productionEndpoint: ").append(toIndentedString(productionEndpoint)).append("\n");
    sb.append("    sandboxEndpoint: ").append(toIndentedString(sandboxEndpoint)).append("\n");
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

