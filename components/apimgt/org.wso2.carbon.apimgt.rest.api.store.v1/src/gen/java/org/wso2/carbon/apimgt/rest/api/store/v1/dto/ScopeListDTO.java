package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.PaginationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ScopeInfoDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ScopeListDTO  {
  
  
  
  private Integer count = null;
  
  
  private List<ScopeInfoDTO> list = new ArrayList<ScopeInfoDTO>();
  
  
  private PaginationDTO pagination = null;

  
  /**
   * Number of results returned.\n
   **/
  @ApiModelProperty(value = "Number of results returned.\n")
  @JsonProperty("count")
  public Integer getCount() {
    return count;
  }
  public void setCount(Integer count) {
    this.count = count;
  }

  
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

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("pagination")
  public PaginationDTO getPagination() {
    return pagination;
  }
  public void setPagination(PaginationDTO pagination) {
    this.pagination = pagination;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ScopeListDTO {\n");
    
    sb.append("  count: ").append(count).append("\n");
    sb.append("  list: ").append(list).append("\n");
    sb.append("  pagination: ").append(pagination).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
