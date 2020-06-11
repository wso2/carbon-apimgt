package org.wso2.carbon.apimgt.rest.api.store.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationScopeDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ScopeListDTO  {
  
  
  
  private List<ApplicationScopeDTO> list = new ArrayList<ApplicationScopeDTO>();

  private String lastUpdatedTime = null;

  private String createdTime = null;

  /**
  * gets and sets the lastUpdatedTime for ScopeListDTO
  **/
  @JsonIgnore
  public String getLastUpdatedTime(){
    return lastUpdatedTime;
  }
  public void setLastUpdatedTime(String lastUpdatedTime){
    this.lastUpdatedTime=lastUpdatedTime;
  }

  /**
  * gets and sets the createdTime for a ScopeListDTO
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
  @JsonProperty("list")
  public List<ApplicationScopeDTO> getList() {
    return list;
  }
  public void setList(List<ApplicationScopeDTO> list) {
    this.list = list;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ScopeListDTO {\n");
    
    sb.append("  list: ").append(list).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
