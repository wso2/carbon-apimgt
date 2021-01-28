package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.TopicPropertyDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class TopicDTO   {
  
    private Integer id = null;
    private String name = null;
    private String description = null;
    private String mode = null;
    private String payloadType = null;
    private List<TopicPropertyDTO> payloadProperties = new ArrayList<TopicPropertyDTO>();

  /**
   **/
  public TopicDTO id(Integer id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "")
  @JsonProperty("id")
  public Integer getId() {
    return id;
  }
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   **/
  public TopicDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "OrderbookUpdates", value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public TopicDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "Dolor sit amet", value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   **/
  public TopicDTO mode(String mode) {
    this.mode = mode;
    return this;
  }

  
  @ApiModelProperty(example = "subscribe", value = "")
  @JsonProperty("mode")
  public String getMode() {
    return mode;
  }
  public void setMode(String mode) {
    this.mode = mode;
  }

  /**
   **/
  public TopicDTO payloadType(String payloadType) {
    this.payloadType = payloadType;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("payloadType")
  public String getPayloadType() {
    return payloadType;
  }
  public void setPayloadType(String payloadType) {
    this.payloadType = payloadType;
  }

  /**
   **/
  public TopicDTO payloadProperties(List<TopicPropertyDTO> payloadProperties) {
    this.payloadProperties = payloadProperties;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("payloadProperties")
  public List<TopicPropertyDTO> getPayloadProperties() {
    return payloadProperties;
  }
  public void setPayloadProperties(List<TopicPropertyDTO> payloadProperties) {
    this.payloadProperties = payloadProperties;
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
    return Objects.equals(id, topic.id) &&
        Objects.equals(name, topic.name) &&
        Objects.equals(description, topic.description) &&
        Objects.equals(mode, topic.mode) &&
        Objects.equals(payloadType, topic.payloadType) &&
        Objects.equals(payloadProperties, topic.payloadProperties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, mode, payloadType, payloadProperties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TopicDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    mode: ").append(toIndentedString(mode)).append("\n");
    sb.append("    payloadType: ").append(toIndentedString(payloadType)).append("\n");
    sb.append("    payloadProperties: ").append(toIndentedString(payloadProperties)).append("\n");
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

