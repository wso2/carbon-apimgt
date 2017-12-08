package org.wso2.carbon.apimgt.rest.api.store.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "")
public class APIListPaginationDTO {
  
  
  
  private Integer total = null;
  
  
  private Integer limit = null;
  
  
  private Integer offset = null;

  
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
  @JsonProperty("offset")
  public Integer getOffset() {
    return offset;
  }
  public void setOffset(Integer offset) {
    this.offset = offset;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIListPaginationDTO {\n");
    
    sb.append("  total: ").append(total).append("\n");
    sb.append("  limit: ").append(limit).append("\n");
    sb.append("  offset: ").append(offset).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
