package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.PaginationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.TierDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class TierListDTO  {
  
  
  
  private Integer count = null;
  
  
  private List<TierDTO> list = new ArrayList<TierDTO>();
  
  
  private PaginationDTO pagination = null;

  
  /**
   * Number of Throttling Tiers returned.\n
   **/
  @ApiModelProperty(value = "Number of Throttling Tiers returned.\n")
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
  public List<TierDTO> getList() {
    return list;
  }
  public void setList(List<TierDTO> list) {
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
    sb.append("class TierListDTO {\n");
    
    sb.append("  count: ").append(count).append("\n");
    sb.append("  list: ").append(list).append("\n");
    sb.append("  pagination: ").append(pagination).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
