package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.EndPointDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class EndPointListDTO  {
  
  
  
  private Integer count = null;
  
  
  private List<EndPointDTO> list = new ArrayList<EndPointDTO>();

  
  /**
   * Number of EndPoints returned.\n
   **/
  @ApiModelProperty(value = "Number of EndPoints returned.\n")
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
  public List<EndPointDTO> getList() {
    return list;
  }
  public void setList(List<EndPointDTO> list) {
    this.list = list;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class EndPointListDTO {\n");
    
    sb.append("  count: ").append(count).append("\n");
    sb.append("  list: ").append(list).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
