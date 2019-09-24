package org.wso2.carbon.apimgt.rest.api.admin.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class WorkflowDTO  {
  
  
  public enum StatusEnum {
     APPROVED,  REJECTED, 
  };
  @NotNull
  private StatusEnum status = null;
  
  
  private Map<String, String> attributes = new HashMap<String, String>();
  
  
  private String description = null;

  
  /**
   * This attribute declares whether this workflow task is approved or rejected.\n
   **/
  @ApiModelProperty(required = true, value = "This attribute declares whether this workflow task is approved or rejected.\n")
  @JsonProperty("status")
  public StatusEnum getStatus() {
    return status;
  }
  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  
  /**
   * Custom attributes to complete the workflow task\n
   **/
  @ApiModelProperty(value = "Custom attributes to complete the workflow task\n")
  @JsonProperty("attributes")
  public Map<String, String> getAttributes() {
    return attributes;
  }
  public void setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class WorkflowDTO {\n");
    
    sb.append("  status: ").append(status).append("\n");
    sb.append("  attributes: ").append(attributes).append("\n");
    sb.append("  description: ").append(description).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
