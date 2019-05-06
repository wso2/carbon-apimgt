package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ScopeInfoDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ScopeListDTO  {
  
  
  
  private List<ScopeInfoDTO> list = new ArrayList<ScopeInfoDTO>();

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("list")
  public List<ScopeInfoDTO> getList() {
    return list;
  }
  public void setList(List<ScopeInfoDTO> list) {
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
