package org.wso2.apk.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;
import javax.validation.constraints.Size;

public class PatchRequestBodyDTO   {
  
    private String content = null;
    private String category = null;

  /**
   * Content of the comment 
   **/
  public PatchRequestBodyDTO content(String content) {
    this.content = content;
    return this;
  }

  
  @ApiModelProperty(example = "This is a comment", value = "Content of the comment ")
  @JsonProperty("content")
 @Size(max=512)  public String getContent() {
    return content;
  }
  public void setContent(String content) {
    this.content = content;
  }

  /**
   * Category of the comment 
   **/
  public PatchRequestBodyDTO category(String category) {
    this.category = category;
    return this;
  }

  
  @ApiModelProperty(example = "general", value = "Category of the comment ")
  @JsonProperty("category")
  public String getCategory() {
    return category;
  }
  public void setCategory(String category) {
    this.category = category;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PatchRequestBodyDTO patchRequestBody = (PatchRequestBodyDTO) o;
    return Objects.equals(content, patchRequestBody.content) &&
        Objects.equals(category, patchRequestBody.category);
  }

  @Override
  public int hashCode() {
    return Objects.hash(content, category);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PatchRequestBodyDTO {\n");
    
    sb.append("    content: ").append(toIndentedString(content)).append("\n");
    sb.append("    category: ").append(toIndentedString(category)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

