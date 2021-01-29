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



public class ScopeDTO   {
  
    private String id = null;
    private String name = null;
    private String displayName = null;
    private String description = null;
    private List<String> bindings = new ArrayList<String>();
    private Integer usageCount = null;

  /**
   * UUID of the Scope. Valid only for shared scopes. 
   **/
  public ScopeDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "UUID of the Scope. Valid only for shared scopes. ")
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
 @Size(min=1,max=255)  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * display name of Scope 
   **/
  public ScopeDTO displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(example = "api_view", value = "display name of Scope ")
  @JsonProperty("displayName")
 @Size(max=255)  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
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
 @Size(max=512)  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * role bindings list of the Scope 
   **/
  public ScopeDTO bindings(List<String> bindings) {
    this.bindings = bindings;
    return this;
  }

  
  @ApiModelProperty(example = "[\"admin\",\"Internal/creator\",\"Internal/publisher\"]", value = "role bindings list of the Scope ")
  @JsonProperty("bindings")
  public List<String> getBindings() {
    return bindings;
  }
  public void setBindings(List<String> bindings) {
    this.bindings = bindings;
  }

  /**
   * usage count of Scope 
   **/
  public ScopeDTO usageCount(Integer usageCount) {
    this.usageCount = usageCount;
    return this;
  }

  
  @ApiModelProperty(example = "3", value = "usage count of Scope ")
  @JsonProperty("usageCount")
  public Integer getUsageCount() {
    return usageCount;
  }
  public void setUsageCount(Integer usageCount) {
    this.usageCount = usageCount;
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
        Objects.equals(displayName, scope.displayName) &&
        Objects.equals(description, scope.description) &&
        Objects.equals(bindings, scope.bindings) &&
        Objects.equals(usageCount, scope.usageCount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, displayName, description, bindings, usageCount);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ScopeDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    bindings: ").append(toIndentedString(bindings)).append("\n");
    sb.append("    usageCount: ").append(toIndentedString(usageCount)).append("\n");
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

