package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

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



public class TopicDTO   {
  
    private String apiId = null;
    private String name = null;
    private String type = null;

  /**
   **/
  public TopicDTO apiId(String apiId) {
    this.apiId = apiId;
    return this;
  }

  
  @ApiModelProperty(example = "faae5fcc-cbae-40c4-bf43-89931630d313", value = "")
  @JsonProperty("apiId")
  public String getApiId() {
    return apiId;
  }
  public void setApiId(String apiId) {
    this.apiId = apiId;
  }

  /**
   **/
  public TopicDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "orderBooks", value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public TopicDTO type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(example = "publisher", value = "")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TopicDTO topic = (TopicDTO) o;
    return Objects.equals(apiId, topic.apiId) &&
        Objects.equals(name, topic.name) &&
        Objects.equals(type, topic.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apiId, name, type);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TopicDTO {\n");
    
    sb.append("    apiId: ").append(toIndentedString(apiId)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
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

