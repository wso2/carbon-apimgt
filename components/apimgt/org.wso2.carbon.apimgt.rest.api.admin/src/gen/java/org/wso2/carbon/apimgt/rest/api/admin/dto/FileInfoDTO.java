package org.wso2.carbon.apimgt.rest.api.admin.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class FileInfoDTO  {
  
  
  
  private String relativePath = null;
  
  
  private String mediaType = null;

  
  /**
   * relative location of the file (excluding the base context and host of the Admin API)
   **/
  @ApiModelProperty(value = "relative location of the file (excluding the base context and host of the Admin API)")
  @JsonProperty("relativePath")
  public String getRelativePath() {
    return relativePath;
  }
  public void setRelativePath(String relativePath) {
    this.relativePath = relativePath;
  }

  
  /**
   * media-type of the file
   **/
  @ApiModelProperty(value = "media-type of the file")
  @JsonProperty("mediaType")
  public String getMediaType() {
    return mediaType;
  }
  public void setMediaType(String mediaType) {
    this.mediaType = mediaType;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class FileInfoDTO {\n");
    
    sb.append("  relativePath: ").append(relativePath).append("\n");
    sb.append("  mediaType: ").append(mediaType).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
