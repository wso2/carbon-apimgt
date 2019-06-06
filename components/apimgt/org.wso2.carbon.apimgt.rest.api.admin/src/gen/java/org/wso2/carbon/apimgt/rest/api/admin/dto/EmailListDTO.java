package org.wso2.carbon.apimgt.rest.api.admin.dto;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class EmailListDTO  {
  
  
  
  private List<String> list = new ArrayList<String>();

  
  /**Set the email list
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("list")
  public List<String> getList() {
    return list;
  }
  public void setList(List<String> list) {
    this.list = list;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class EmailListDTO {\n");
    
    sb.append("  list: ").append(list).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
