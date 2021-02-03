package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class FileInfoDTO   {
  
    private String relativePath = null;
    private String mediaType = null;

  /**
   * relative location of the file (excluding the base context and host of the Publisher API)
   **/
  public FileInfoDTO relativePath(String relativePath) {
    this.relativePath = relativePath;
    return this;
  }

  
  @ApiModelProperty(example = "apis/01234567-0123-0123-0123-012345678901/thumbnail", value = "relative location of the file (excluding the base context and host of the Publisher API)")
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
  public FileInfoDTO mediaType(String mediaType) {
    this.mediaType = mediaType;
    return this;
  }

  
  @ApiModelProperty(example = "image/jpeg", value = "media-type of the file")
  @JsonProperty("mediaType")
  public String getMediaType() {
    return mediaType;
  }
  public void setMediaType(String mediaType) {
    this.mediaType = mediaType;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FileInfoDTO fileInfo = (FileInfoDTO) o;
    return Objects.equals(relativePath, fileInfo.relativePath) &&
        Objects.equals(mediaType, fileInfo.mediaType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(relativePath, mediaType);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FileInfoDTO {\n");
    
    sb.append("    relativePath: ").append(toIndentedString(relativePath)).append("\n");
    sb.append("    mediaType: ").append(toIndentedString(mediaType)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

