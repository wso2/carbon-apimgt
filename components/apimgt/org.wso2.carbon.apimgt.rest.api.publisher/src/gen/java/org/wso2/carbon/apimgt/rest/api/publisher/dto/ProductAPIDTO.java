package org.wso2.carbon.apimgt.rest.api.publisher.dto;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ProductAPIDTO  {
  
  
  @NotNull
  private String apiId = null;
  
  
  private String name = null;
  
  
  private List<String> resources = new ArrayList<String>();

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("apiId")
  public String getApiId() {
    return apiId;
  }
  public void setApiId(String apiId) {
    this.apiId = apiId;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
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
  @JsonProperty("resources")
  public List<String> getResources() {
    return resources;
  }
  public void setResources(List<String> resources) {
    this.resources = resources;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ProductAPIDTO {\n");
    
    sb.append("  apiId: ").append(apiId).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("  resources: ").append(resources).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
