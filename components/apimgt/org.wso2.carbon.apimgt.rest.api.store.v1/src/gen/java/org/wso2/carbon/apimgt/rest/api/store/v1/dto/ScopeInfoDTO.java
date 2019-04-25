package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ScopeInfoDTO  {
  
  
  
  private String key = null;
  
  
  private String name = null;
  
  
  private List<String> roles = new ArrayList<String>();
  
  
  private String description = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("key")
  public String getKey() {
    return key;
  }
  public void setKey(String key) {
    this.key = key;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  
  /**
   * Allowed roles for the scope
   **/
  @ApiModelProperty(value = "Allowed roles for the scope")
  @JsonProperty("roles")
  public List<String> getRoles() {
    return roles;
  }
  public void setRoles(List<String> roles) {
    this.roles = roles;
  }

  
  /**
   * Description of the scope
   **/
  @ApiModelProperty(value = "Description of the scope")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ScopeInfoDTO {\n");
    
    sb.append("  key: ").append(key).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("  roles: ").append(roles).append("\n");
    sb.append("  description: ").append(description).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
