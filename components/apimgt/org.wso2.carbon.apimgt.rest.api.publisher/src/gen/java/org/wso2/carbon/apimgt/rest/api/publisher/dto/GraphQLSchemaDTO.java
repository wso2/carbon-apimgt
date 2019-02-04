package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class GraphQLSchemaDTO  {
  
  
  @NotNull
  private String name = null;
  
  
  private String schemaDefinition = null;

  
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
  @JsonProperty("schemaDefinition")
  public String getSchemaDefinition() {
    return schemaDefinition;
  }
  public void setSchemaDefinition(String schemaDefinition) {
    this.schemaDefinition = schemaDefinition;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class GraphQLSchemaDTO {\n");
    
    sb.append("  name: ").append(name).append("\n");
    sb.append("  schemaDefinition: ").append(schemaDefinition).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
