package org.wso2.carbon.apimgt.model;




@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-10-14T14:19:24.818+05:30")
public class FileInfo  {
  
  private String relativePath = null;
  private String mediaType = null;

  /**
   * relative location of the file (excluding the base context and host of the Publisher API)
   **/
  public String getRelativePath() {
    return relativePath;
  }
  public void setRelativePath(String relativePath) {
    this.relativePath = relativePath;
  }

  /**
   * media-type of the file
   **/
  public String getMediaType() {
    return mediaType;
  }
  public void setMediaType(String mediaType) {
    this.mediaType = mediaType;
  }


  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class FileInfo {\n");
    
    sb.append("  relativePath: ").append(relativePath).append("\n");
    sb.append("  mediaType: ").append(mediaType).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
