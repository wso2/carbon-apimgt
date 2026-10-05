package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class GatewayFeatureCatalogDTO   {
  
    private Map<String, Object> gatewayFeatures = new HashMap<String, Object>();
    private Map<String, List<String>> apiTypes = new HashMap<String, List<String>>();

  /**
   **/
  public GatewayFeatureCatalogDTO gatewayFeatures(Map<String, Object> gatewayFeatures) {
    this.gatewayFeatures = gatewayFeatures;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("gatewayFeatures")
  public Map<String, Object> getGatewayFeatures() {
    return gatewayFeatures;
  }
  public void setGatewayFeatures(Map<String, Object> gatewayFeatures) {
    this.gatewayFeatures = gatewayFeatures;
  }

  /**
   **/
  public GatewayFeatureCatalogDTO apiTypes(Map<String, List<String>> apiTypes) {
    this.apiTypes = apiTypes;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("apiTypes")
  public Map<String, List<String>> getApiTypes() {
    return apiTypes;
  }
  public void setApiTypes(Map<String, List<String>> apiTypes) {
    this.apiTypes = apiTypes;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GatewayFeatureCatalogDTO gatewayFeatureCatalog = (GatewayFeatureCatalogDTO) o;
    return Objects.equals(gatewayFeatures, gatewayFeatureCatalog.gatewayFeatures) &&
        Objects.equals(apiTypes, gatewayFeatureCatalog.apiTypes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(gatewayFeatures, apiTypes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GatewayFeatureCatalogDTO {\n");
    
    sb.append("    gatewayFeatures: ").append(toIndentedString(gatewayFeatures)).append("\n");
    sb.append("    apiTypes: ").append(toIndentedString(apiTypes)).append("\n");
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

