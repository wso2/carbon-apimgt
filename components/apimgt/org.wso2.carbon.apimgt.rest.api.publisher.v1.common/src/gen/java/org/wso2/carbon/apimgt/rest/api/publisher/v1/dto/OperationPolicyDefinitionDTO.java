package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class OperationPolicyDefinitionDTO   {
  
    private String name = null;
    private String apiId = null;
    private String id = null;
    private List<String> flows = new ArrayList<String>();

  /**
   **/
  public OperationPolicyDefinitionDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "removeHeaderPolicy", value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public OperationPolicyDefinitionDTO apiId(String apiId) {
    this.apiId = apiId;
    return this;
  }

  
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "")
  @JsonProperty("apiId")
  public String getApiId() {
    return apiId;
  }
  public void setApiId(String apiId) {
    this.apiId = apiId;
  }

  /**
   **/
  public OperationPolicyDefinitionDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public OperationPolicyDefinitionDTO flows(List<String> flows) {
    this.flows = flows;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("flows")
  public List<String> getFlows() {
    return flows;
  }
  public void setFlows(List<String> flows) {
    this.flows = flows;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OperationPolicyDefinitionDTO operationPolicyDefinition = (OperationPolicyDefinitionDTO) o;
    return Objects.equals(name, operationPolicyDefinition.name) &&
        Objects.equals(apiId, operationPolicyDefinition.apiId) &&
        Objects.equals(id, operationPolicyDefinition.id) &&
        Objects.equals(flows, operationPolicyDefinition.flows);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, apiId, id, flows);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OperationPolicyDefinitionDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    apiId: ").append(toIndentedString(apiId)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    flows: ").append(toIndentedString(flows)).append("\n");
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

