package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class LabelDTO  {
  
  
  @NotNull
  private String name = null;
  
  
  private List<String> accessUrls = new ArrayList<String>();

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("accessUrls")
  public List<String> getAccessUrls() {
    return accessUrls;
  }
  public void setAccessUrls(List<String> accessUrls) {
    this.accessUrls = accessUrls;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class LabelDTO {\n");
    
    sb.append("  name: ").append(name).append("\n");
    sb.append("  accessUrls: ").append(accessUrls).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
