package org.wso2.carbon.apimgt.rest.api.store.dto;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class TierPermissionInfoDTO  {
  
  
  public enum TypeEnum {
     allow,  deny, 
  };
  
  private TypeEnum type = null;
  
  
  private List<String> roles = new ArrayList<String>();

  private String lastUpdatedTime = null;

  private String createdTime = null;

  /**
  * gets and sets the lastUpdatedTime for TierPermissionInfoDTO
  **/
  @JsonIgnore
  public String getLastUpdatedTime(){
    return lastUpdatedTime;
  }
  public void setLastUpdatedTime(String lastUpdatedTime){
    this.lastUpdatedTime=lastUpdatedTime;
  }

  /**
  * gets and sets the createdTime for a TierPermissionInfoDTO
  **/

  @JsonIgnore
  public String getCreatedTime(){
    return createdTime;
  }
  public void setCreatedTime(String createdTime){
    this.createdTime=createdTime;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("type")
  public TypeEnum getType() {
    return type;
  }
  public void setType(TypeEnum type) {
    this.type = type;
  }

  
  /**
   * roles for this permission
   **/
  @ApiModelProperty(value = "roles for this permission")
  @JsonProperty("roles")
  public List<String> getRoles() {
    return roles;
  }
  public void setRoles(List<String> roles) {
    this.roles = roles;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class TierPermissionInfoDTO {\n");
    
    sb.append("  type: ").append(type).append("\n");
    sb.append("  roles: ").append(roles).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
