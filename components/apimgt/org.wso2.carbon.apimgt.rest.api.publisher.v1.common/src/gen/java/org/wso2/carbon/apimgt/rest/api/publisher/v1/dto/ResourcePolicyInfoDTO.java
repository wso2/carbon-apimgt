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



public class ResourcePolicyInfoDTO   {
  
    private String id = null;
    private String httpVerb = null;
    private String resourcePath = null;
    private String content = null;

  /**
   * UUID of the resource policy registry artifact 
   **/
  public ResourcePolicyInfoDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "UUID of the resource policy registry artifact ")
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
  public ResourcePolicyInfoDTO httpVerb(String httpVerb) {
    this.httpVerb = httpVerb;
    return this;
  }

  
  @ApiModelProperty(example = "get", value = "HTTP verb used for the resource path")
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
  public ResourcePolicyInfoDTO resourcePath(String resourcePath) {
    this.resourcePath = resourcePath;
    return this;
  }

  
  @ApiModelProperty(example = "checkPhoneNumber", value = "A string that represents the resource path of the api for the related resource policy")
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
  public ResourcePolicyInfoDTO content(String content) {
    this.content = content;
    return this;
  }

  
  @ApiModelProperty(example = "<header description=\"SOAPAction\" name=\"SOAPAction\" scope=\"transport\" value=\"http://ws.cdyne.com/PhoneVerify/query/CheckPhoneNumber\"/>", value = "The resource policy content")
  @JsonProperty("content")
  public String getContent() {
    return content;
  }
  public void setContent(String content) {
    this.content = content;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResourcePolicyInfoDTO resourcePolicyInfo = (ResourcePolicyInfoDTO) o;
    return Objects.equals(id, resourcePolicyInfo.id) &&
        Objects.equals(httpVerb, resourcePolicyInfo.httpVerb) &&
        Objects.equals(resourcePath, resourcePolicyInfo.resourcePath) &&
        Objects.equals(content, resourcePolicyInfo.content);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, httpVerb, resourcePath, content);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ResourcePolicyInfoDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    httpVerb: ").append(toIndentedString(httpVerb)).append("\n");
    sb.append("    resourcePath: ").append(toIndentedString(resourcePath)).append("\n");
    sb.append("    content: ").append(toIndentedString(content)).append("\n");
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

