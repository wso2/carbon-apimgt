package org.wso2.carbon.apimgt.rest.api.admin.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.dto.LabelDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class LabelListDTO  {
  
  
  
  private Integer count = null;
  
  
  private List<LabelDTO> list = new ArrayList<LabelDTO>();

  
  /**
   * Number of Labels returned.\n
   **/
  @ApiModelProperty(value = "Number of Labels returned.\n")
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
  public List<LabelDTO> getList() {
    return list;
  }
  public void setList(List<LabelDTO> list) {
    this.list = list;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class LabelListDTO {\n");
    
    sb.append("  count: ").append(count).append("\n");
    sb.append("  list: ").append(list).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
