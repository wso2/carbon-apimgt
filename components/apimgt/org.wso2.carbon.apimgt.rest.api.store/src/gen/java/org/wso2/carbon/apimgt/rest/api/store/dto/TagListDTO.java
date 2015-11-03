package org.wso2.carbon.apimgt.rest.api.store.dto;

import org.wso2.carbon.apimgt.rest.api.store.dto.TagDTO;
import java.util.*;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class TagListDTO  {
  
  
  
  private Integer count = null;
  
  
  private List<TagDTO> list = new ArrayList<TagDTO>();

  
  /**
   * Number of Tags returned.
   **/
  @ApiModelProperty(value = "Number of Tags returned.")
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
  public List<TagDTO> getList() {
    return list;
  }
  public void setList(List<TagDTO> list) {
    this.list = list;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class TagListDTO {\n");
    
    sb.append("  count: ").append(count).append("\n");
    sb.append("  list: ").append(list).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
