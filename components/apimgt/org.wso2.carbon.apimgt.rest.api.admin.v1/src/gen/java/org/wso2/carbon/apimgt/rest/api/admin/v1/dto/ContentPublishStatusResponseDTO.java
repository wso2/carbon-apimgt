package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

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



public class ContentPublishStatusResponseDTO   {
  
    private String id = null;
    private Boolean published = null;

  /**
   * UUID of the org-theme
   **/
  public ContentPublishStatusResponseDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(value = "UUID of the org-theme")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Publish status of the org-theme
   **/
  public ContentPublishStatusResponseDTO published(Boolean published) {
    this.published = published;
    return this;
  }

  
  @ApiModelProperty(value = "Publish status of the org-theme")
  @JsonProperty("published")
  public Boolean isPublished() {
    return published;
  }
  public void setPublished(Boolean published) {
    this.published = published;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ContentPublishStatusResponseDTO contentPublishStatusResponse = (ContentPublishStatusResponseDTO) o;
    return Objects.equals(id, contentPublishStatusResponse.id) &&
        Objects.equals(published, contentPublishStatusResponse.published);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, published);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ContentPublishStatusResponseDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    published: ").append(toIndentedString(published)).append("\n");
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

