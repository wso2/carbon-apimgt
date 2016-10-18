package org.wso2.carbon.apimgt.rest.api.publisher.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.EnvironmentDTO;

import io.swagger.annotations.*;
import javax.ws.rs.*;
import com.fasterxml.jackson.annotation.*;
import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class EnvironmentListDTO  {
  
  
  
  private Integer count = null;
  
  
  private List<EnvironmentDTO> list = new ArrayList<EnvironmentDTO>();

  
  /**
   * Number of Environments returned.\n
   **/
  @ApiModelProperty(value = "Number of Environments returned.\n")
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
  public List<EnvironmentDTO> getList() {
    return list;
  }
  public void setList(List<EnvironmentDTO> list) {
    this.list = list;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class EnvironmentListDTO {\n");
    
    sb.append("  count: ").append(count).append("\n");
    sb.append("  list: ").append(list).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
