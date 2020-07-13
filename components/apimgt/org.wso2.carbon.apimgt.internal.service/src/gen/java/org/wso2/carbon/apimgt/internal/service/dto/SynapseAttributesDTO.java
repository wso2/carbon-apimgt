package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class SynapseAttributesDTO   {
  
    private Integer count = null;
    private String label = null;
    private String apiId = null;

  /**
   **/
  public SynapseAttributesDTO count(Integer count) {
    this.count = count;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("count")
  public Integer getCount() {
    return count;
  }
  public void setCount(Integer count) {
    this.count = count;
  }

  /**
   **/
  public SynapseAttributesDTO label(String label) {
    this.label = label;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("label")
  public String getLabel() {
    return label;
  }
  public void setLabel(String label) {
    this.label = label;
  }

  /**
   **/
  public SynapseAttributesDTO apiId(String apiId) {
    this.apiId = apiId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("apiId")
  public String getApiId() {
    return apiId;
  }
  public void setApiId(String apiId) {
    this.apiId = apiId;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SynapseAttributesDTO synapseAttributes = (SynapseAttributesDTO) o;
    return Objects.equals(count, synapseAttributes.count) &&
        Objects.equals(label, synapseAttributes.label) &&
        Objects.equals(apiId, synapseAttributes.apiId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, label, apiId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SynapseAttributesDTO {\n");
    
    sb.append("    count: ").append(toIndentedString(count)).append("\n");
    sb.append("    label: ").append(toIndentedString(label)).append("\n");
    sb.append("    apiId: ").append(toIndentedString(apiId)).append("\n");
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

