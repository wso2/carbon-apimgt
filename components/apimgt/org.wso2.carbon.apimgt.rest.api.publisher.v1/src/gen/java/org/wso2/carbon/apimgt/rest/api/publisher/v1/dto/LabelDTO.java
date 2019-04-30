package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class LabelDTO  {
  
  
  @NotNull
  private String labelId = null;
  
  @NotNull
  private String name = null;
  
  @NotNull
  private String type = null;
  
  @NotNull
  private List<String> accessUrls = new ArrayList<String>();

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("labelId")
  public String getLabelId() {
    return labelId;
  }
  public void setLabelId(String labelId) {
    this.labelId = labelId;
  }

  
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
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
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
    
    sb.append("  labelId: ").append(labelId).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("  type: ").append(type).append("\n");
    sb.append("  accessUrls: ").append(accessUrls).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
