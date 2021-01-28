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



public class TopicPropertyDTO   {
  
    private String name = null;
    private String dataType = null;
    private String advanced = null;
    private String description = null;

  /**
   **/
  public TopicPropertyDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public TopicPropertyDTO dataType(String dataType) {
    this.dataType = dataType;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("dataType")
  public String getDataType() {
    return dataType;
  }
  public void setDataType(String dataType) {
    this.dataType = dataType;
  }

  /**
   **/
  public TopicPropertyDTO advanced(String advanced) {
    this.advanced = advanced;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("advanced")
  public String getAdvanced() {
    return advanced;
  }
  public void setAdvanced(String advanced) {
    this.advanced = advanced;
  }

  /**
   **/
  public TopicPropertyDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TopicPropertyDTO topicProperty = (TopicPropertyDTO) o;
    return Objects.equals(name, topicProperty.name) &&
        Objects.equals(dataType, topicProperty.dataType) &&
        Objects.equals(advanced, topicProperty.advanced) &&
        Objects.equals(description, topicProperty.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, dataType, advanced, description);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TopicPropertyDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    dataType: ").append(toIndentedString(dataType)).append("\n");
    sb.append("    advanced: ").append(toIndentedString(advanced)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
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

