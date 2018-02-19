package org.wso2.carbon.apimgt.micro.gateway.api.synchronizer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;


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
