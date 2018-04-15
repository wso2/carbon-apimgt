package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * FileInfoDTO
 */
public class FileInfoDTO   {
  @SerializedName("relativePath")
  private String relativePath = null;

  @SerializedName("mediaType")
  private String mediaType = null;

  public FileInfoDTO relativePath(String relativePath) {
    this.relativePath = relativePath;
    return this;
  }

   /**
   * relative location of the file (excluding the base context and host of the Publisher API)
   * @return relativePath
  **/
  @ApiModelProperty(example = "apis/01234567-0123-0123-0123-012345678901/thumbnail", value = "relative location of the file (excluding the base context and host of the Publisher API)")
  public String getRelativePath() {
    return relativePath;
  }

  public void setRelativePath(String relativePath) {
    this.relativePath = relativePath;
  }

  public FileInfoDTO mediaType(String mediaType) {
    this.mediaType = mediaType;
    return this;
  }

   /**
   * media-type of the file
   * @return mediaType
  **/
  @ApiModelProperty(example = "image/jpeg", value = "media-type of the file")
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
    return Objects.equals(this.relativePath, fileInfo.relativePath) &&
        Objects.equals(this.mediaType, fileInfo.mediaType);
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

