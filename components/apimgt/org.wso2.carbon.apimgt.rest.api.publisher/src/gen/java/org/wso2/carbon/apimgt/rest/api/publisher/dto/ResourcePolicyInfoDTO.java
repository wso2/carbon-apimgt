package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ResourcePolicyInfoDTO  {
  
  
  
  private String id = null;
  
  
  private String httpVerb = null;
  
  
  private String resourcePath = null;
  
  
  private String content = null;

  
  /**
   * UUID of the resource policy registry artifact\n
   **/
  @ApiModelProperty(value = "UUID of the resource policy registry artifact\n")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  
  /**
   * HTTP verb used for the resource path
   **/
  @ApiModelProperty(value = "HTTP verb used for the resource path")
  @JsonProperty("httpVerb")
  public String getHttpVerb() {
    return httpVerb;
  }
  public void setHttpVerb(String httpVerb) {
    this.httpVerb = httpVerb;
  }

  
  /**
   * A string that represents the resource path of the api for the related resource policy
   **/
  @ApiModelProperty(value = "A string that represents the resource path of the api for the related resource policy")
  @JsonProperty("resourcePath")
  public String getResourcePath() {
    return resourcePath;
  }
  public void setResourcePath(String resourcePath) {
    this.resourcePath = resourcePath;
  }

  
  /**
   * The resource policy content
   **/
  @ApiModelProperty(value = "The resource policy content")
  @JsonProperty("content")
  public String getContent() {
    return content;
  }
  public void setContent(String content) {
    this.content = content;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ResourcePolicyInfoDTO {\n");
    
    sb.append("  id: ").append(id).append("\n");
    sb.append("  httpVerb: ").append(httpVerb).append("\n");
    sb.append("  resourcePath: ").append(resourcePath).append("\n");
    sb.append("  content: ").append(content).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
