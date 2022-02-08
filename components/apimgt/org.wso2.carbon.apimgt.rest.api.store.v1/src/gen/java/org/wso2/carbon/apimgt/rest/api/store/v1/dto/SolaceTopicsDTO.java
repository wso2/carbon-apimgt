package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class SolaceTopicsDTO   {
  
    private List<String> publishTopics = new ArrayList<String>();
    private List<String> subscribeTopics = new ArrayList<String>();

  /**
   **/
  public SolaceTopicsDTO publishTopics(List<String> publishTopics) {
    this.publishTopics = publishTopics;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("publishTopics")
  public List<String> getPublishTopics() {
    return publishTopics;
  }
  public void setPublishTopics(List<String> publishTopics) {
    this.publishTopics = publishTopics;
  }

  /**
   **/
  public SolaceTopicsDTO subscribeTopics(List<String> subscribeTopics) {
    this.subscribeTopics = subscribeTopics;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("subscribeTopics")
  public List<String> getSubscribeTopics() {
    return subscribeTopics;
  }
  public void setSubscribeTopics(List<String> subscribeTopics) {
    this.subscribeTopics = subscribeTopics;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SolaceTopicsDTO solaceTopics = (SolaceTopicsDTO) o;
    return Objects.equals(publishTopics, solaceTopics.publishTopics) &&
        Objects.equals(subscribeTopics, solaceTopics.subscribeTopics);
  }

  @Override
  public int hashCode() {
    return Objects.hash(publishTopics, subscribeTopics);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SolaceTopicsDTO {\n");
    
    sb.append("    publishTopics: ").append(toIndentedString(publishTopics)).append("\n");
    sb.append("    subscribeTopics: ").append(toIndentedString(subscribeTopics)).append("\n");
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

