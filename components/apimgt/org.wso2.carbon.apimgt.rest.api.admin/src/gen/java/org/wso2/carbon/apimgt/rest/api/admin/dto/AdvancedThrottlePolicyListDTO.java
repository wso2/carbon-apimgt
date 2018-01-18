package org.wso2.carbon.apimgt.rest.api.admin.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.dto.AdvancedThrottlePolicyInfoDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class AdvancedThrottlePolicyListDTO  {
  
  
  
  private Integer count = null;
  
  
  private List<AdvancedThrottlePolicyInfoDTO> list = new ArrayList<AdvancedThrottlePolicyInfoDTO>();

  
  /**
   * Number of Advanced Throttling Policies returned.\n
   **/
  @ApiModelProperty(value = "Number of Advanced Throttling Policies returned.\n")
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
  public List<AdvancedThrottlePolicyInfoDTO> getList() {
    return list;
  }
  public void setList(List<AdvancedThrottlePolicyInfoDTO> list) {
    this.list = list;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class AdvancedThrottlePolicyListDTO {\n");
    
    sb.append("  count: ").append(count).append("\n");
    sb.append("  list: ").append(list).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
