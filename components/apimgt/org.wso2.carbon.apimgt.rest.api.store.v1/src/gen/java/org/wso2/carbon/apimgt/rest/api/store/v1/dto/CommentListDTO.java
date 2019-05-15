package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.CommentDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.PaginationDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class CommentListDTO  {
  
  
  
  private Integer count = null;
  
  
  private List<CommentDTO> list = new ArrayList<CommentDTO>();
  
  
  private PaginationDTO pagination = null;

  
  /**
   * Number of Comments returned.\n
   **/
  @ApiModelProperty(value = "Number of Comments returned.\n")
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
  public List<CommentDTO> getList() {
    return list;
  }
  public void setList(List<CommentDTO> list) {
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
    sb.append("class CommentListDTO {\n");
    
    sb.append("  count: ").append(count).append("\n");
    sb.append("  list: ").append(list).append("\n");
    sb.append("  pagination: ").append(pagination).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
