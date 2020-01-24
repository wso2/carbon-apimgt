package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ScopeBindingsDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class ScopeDTO   {
  
    private String id = null;
    private String name = null;
    private String description = null;
    private ScopeBindingsDTO bindings = null;

  /**
   * UUID of the Scope 
   **/
  public ScopeDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "UUID of the Scope ")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   * name of Scope 
   **/
  public ScopeDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "apim:api_view", required = true, value = "name of Scope ")
  @JsonProperty("name")
  @NotNull
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * description of Scope 
   **/
  public ScopeDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "This Scope can used to view Apis", value = "description of Scope ")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   **/
  public ScopeDTO bindings(ScopeBindingsDTO bindings) {
    this.bindings = bindings;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("bindings")
  public ScopeBindingsDTO getBindings() {
    return bindings;
  }
  public void setBindings(ScopeBindingsDTO bindings) {
    this.bindings = bindings;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ScopeDTO scope = (ScopeDTO) o;
    return Objects.equals(id, scope.id) &&
        Objects.equals(name, scope.name) &&
        Objects.equals(description, scope.description) &&
        Objects.equals(bindings, scope.bindings);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, bindings);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ScopeDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    bindings: ").append(toIndentedString(bindings)).append("\n");
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

