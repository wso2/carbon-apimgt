package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.EndPointConfigDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class EndPointEndPointConfigDTO  {
  
  
  public enum EndpointTypeEnum {
     SINGLE,  LOAD_BALANCED,  FAIL_OVER, 
  };
  
  private EndpointTypeEnum endpointType = null;
  
  
  private List<EndPointConfigDTO> list = new ArrayList<EndPointConfigDTO>();

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("endpointType")
  public EndpointTypeEnum getEndpointType() {
    return endpointType;
  }
  public void setEndpointType(EndpointTypeEnum endpointType) {
    this.endpointType = endpointType;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("list")
  public List<EndPointConfigDTO> getList() {
    return list;
  }
  public void setList(List<EndPointConfigDTO> list) {
    this.list = list;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class EndPointEndPointConfigDTO {\n");
    
    sb.append("  endpointType: ").append(endpointType).append("\n");
    sb.append("  list: ").append(list).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
