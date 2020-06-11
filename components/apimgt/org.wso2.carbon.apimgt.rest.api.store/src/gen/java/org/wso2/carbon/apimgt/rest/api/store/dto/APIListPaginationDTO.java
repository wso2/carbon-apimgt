package org.wso2.carbon.apimgt.rest.api.store.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class APIListPaginationDTO  {
  
  
  
  private Integer total = null;
  
  
  private Integer offset = null;
  
  
  private Integer limit = null;

  private String lastUpdatedTime = null;

  private String createdTime = null;

  /**
  * gets and sets the lastUpdatedTime for APIListPaginationDTO
  **/
  @JsonIgnore
  public String getLastUpdatedTime(){
    return lastUpdatedTime;
  }
  public void setLastUpdatedTime(String lastUpdatedTime){
    this.lastUpdatedTime=lastUpdatedTime;
  }

  /**
  * gets and sets the createdTime for a APIListPaginationDTO
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

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIListPaginationDTO {\n");
    
    sb.append("  total: ").append(total).append("\n");
    sb.append("  offset: ").append(offset).append("\n");
    sb.append("  limit: ").append(limit).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
