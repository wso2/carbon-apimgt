package org.wso2.carbon.apimgt.rest.api.publisher.dto;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.DocumentDTO;
import java.util.*;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class DocumentListDTO  {
  
  
  
  private Integer count = null;
  
  
  private String next = null;
  
  
  private String previous = null;
  
  
  private List<DocumentDTO> list = new ArrayList<DocumentDTO>();

  
  /**
   * Number of Documents returned.
   **/
  @ApiModelProperty(value = "Number of Documents returned.")
  @JsonProperty("count")
  public Integer getCount() {
    return count;
  }
  public void setCount(Integer count) {
    this.count = count;
  }

  
  /**
   * Link to the next subset of resources qualified. \nEmpty if no more resources are to be returned.
   **/
  @ApiModelProperty(value = "Link to the next subset of resources qualified. \nEmpty if no more resources are to be returned.")
  @JsonProperty("next")
  public String getNext() {
    return next;
  }
  public void setNext(String next) {
    this.next = next;
  }

  
  /**
   * Link to the previous subset of resources qualified. \nEmpty if current subset is the first subset returned.
   **/
  @ApiModelProperty(value = "Link to the previous subset of resources qualified. \nEmpty if current subset is the first subset returned.")
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
  public List<DocumentDTO> getList() {
    return list;
  }
  public void setList(List<DocumentDTO> list) {
    this.list = list;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class DocumentListDTO {\n");
    
    sb.append("  count: ").append(count).append("\n");
    sb.append("  next: ").append(next).append("\n");
    sb.append("  previous: ").append(previous).append("\n");
    sb.append("  list: ").append(list).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
