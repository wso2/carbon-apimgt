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



public class ResourcePathDTO   {
  
    private Integer id = null;
    private String resourcePath = null;
    private String httpVerb = null;

  /**
   **/
  public ResourcePathDTO id(Integer id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "1", required = true, value = "")
  @JsonProperty("id")
  @NotNull
  public Integer getId() {
    return id;
  }
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   **/
  public ResourcePathDTO resourcePath(String resourcePath) {
    this.resourcePath = resourcePath;
    return this;
  }

  
  @ApiModelProperty(example = "/menu", value = "")
  @JsonProperty("resourcePath")
  public String getResourcePath() {
    return resourcePath;
  }
  public void setResourcePath(String resourcePath) {
    this.resourcePath = resourcePath;
  }

  /**
   **/
  public ResourcePathDTO httpVerb(String httpVerb) {
    this.httpVerb = httpVerb;
    return this;
  }

  
  @ApiModelProperty(example = "GET", value = "")
  @JsonProperty("httpVerb")
  public String getHttpVerb() {
    return httpVerb;
  }
  public void setHttpVerb(String httpVerb) {
    this.httpVerb = httpVerb;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResourcePathDTO resourcePath = (ResourcePathDTO) o;
    return Objects.equals(id, resourcePath.id) &&
        Objects.equals(resourcePath, resourcePath.resourcePath) &&
        Objects.equals(httpVerb, resourcePath.httpVerb);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, resourcePath, httpVerb);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ResourcePathDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    resourcePath: ").append(toIndentedString(resourcePath)).append("\n");
    sb.append("    httpVerb: ").append(toIndentedString(httpVerb)).append("\n");
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

