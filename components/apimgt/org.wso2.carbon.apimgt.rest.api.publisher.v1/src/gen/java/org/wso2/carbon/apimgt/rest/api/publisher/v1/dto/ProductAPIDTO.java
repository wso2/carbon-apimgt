package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ProductAPIOperationsDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ProductAPIDTO  {
  
  
  
  private String name = null;
  
  @NotNull
  private String apiId = null;
  
  
  private List<ProductAPIOperationsDTO> operations = new ArrayList<ProductAPIOperationsDTO>();

  
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
  @JsonProperty("operations")
  public List<ProductAPIOperationsDTO> getOperations() {
    return operations;
  }
  public void setOperations(List<ProductAPIOperationsDTO> operations) {
    this.operations = operations;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ProductAPIDTO {\n");
    
    sb.append("  name: ").append(name).append("\n");
    sb.append("  apiId: ").append(apiId).append("\n");
    sb.append("  operations: ").append(operations).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
