package org.wso2.carbon.apimgt.rest.api.store.v1.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class PaginationDTO  {
  
  
  
  private Integer offset = null;
  
  
  private Integer limit = null;
  
  
  private Integer total = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("offset")
  public Integer getOffset() {
    return offset;
  }
  public void setOffset(Integer offset) {
    this.offset = offset;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("limit")
  public Integer getLimit() {
    return limit;
  }
  public void setLimit(Integer limit) {
    this.limit = limit;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("total")
  public Integer getTotal() {
    return total;
  }
  public void setTotal(Integer total) {
    this.total = total;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class PaginationDTO {\n");
    
    sb.append("  offset: ").append(offset).append("\n");
    sb.append("  limit: ").append(limit).append("\n");
    sb.append("  total: ").append(total).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
