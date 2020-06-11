package org.wso2.carbon.apimgt.rest.api.publisher.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.WsdlInfoDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class WsdlListDTO  {
  
  
  
  private Integer count = null;
  
  
  private String next = null;
  
  
  private String previous = null;
  
  
  private List<WsdlInfoDTO> list = new ArrayList<WsdlInfoDTO>();

  
  /**
   * Number of mediation sequences returned.\nexample: 1\n
   **/
  @ApiModelProperty(value = "Number of mediation sequences returned.\nexample: 1\n")
  @JsonProperty("count")
  public Integer getCount() {
    return count;
  }
  public void setCount(Integer count) {
    this.count = count;
  }

  
  /**
   * Link to the next subset of sequences qualified.\nEmpty if no more sequences are to be returned.\n
   **/
  @ApiModelProperty(value = "Link to the next subset of sequences qualified.\nEmpty if no more sequences are to be returned.\n")
  @JsonProperty("next")
  public String getNext() {
    return next;
  }
  public void setNext(String next) {
    this.next = next;
  }

  
  /**
   * Link to the previous subset of sequences qualified.\nEmpty if current subset is the first subset returned.\n
   **/
  @ApiModelProperty(value = "Link to the previous subset of sequences qualified.\nEmpty if current subset is the first subset returned.\n")
  @JsonProperty("previous")
  public String getPrevious() {
    return previous;
  }
  public void setPrevious(String previous) {
    this.previous = previous;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("list")
  public List<WsdlInfoDTO> getList() {
    return list;
  }
  public void setList(List<WsdlInfoDTO> list) {
    this.list = list;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class WsdlListDTO {\n");
    
    sb.append("  count: ").append(count).append("\n");
    sb.append("  next: ").append(next).append("\n");
    sb.append("  previous: ").append(previous).append("\n");
    sb.append("  list: ").append(list).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
