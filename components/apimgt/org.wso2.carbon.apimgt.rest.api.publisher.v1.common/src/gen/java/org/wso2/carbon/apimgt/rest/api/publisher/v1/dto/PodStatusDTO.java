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



public class PodStatusDTO   {
  
    private String name = null;
    private String ready = null;
    private String status = null;
    private String creationTimestamp = null;

  /**
   **/
  public PodStatusDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "petStore-677bb7cc65-shb2f", required = true, value = "")
  @JsonProperty("name")
  @NotNull
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public PodStatusDTO ready(String ready) {
    this.ready = ready;
    return this;
  }

  
  @ApiModelProperty(example = "1/1", required = true, value = "")
  @JsonProperty("ready")
  @NotNull
  public String getReady() {
    return ready;
  }
  public void setReady(String ready) {
    this.ready = ready;
  }

  /**
   **/
  public PodStatusDTO status(String status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(example = "running", required = true, value = "")
  @JsonProperty("status")
  @NotNull
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   **/
  public PodStatusDTO creationTimestamp(String creationTimestamp) {
    this.creationTimestamp = creationTimestamp;
    return this;
  }

  
  @ApiModelProperty(example = "2020-05-12T06:12:00Z", value = "")
  @JsonProperty("creationTimestamp")
  public String getCreationTimestamp() {
    return creationTimestamp;
  }
  public void setCreationTimestamp(String creationTimestamp) {
    this.creationTimestamp = creationTimestamp;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PodStatusDTO podStatus = (PodStatusDTO) o;
    return Objects.equals(name, podStatus.name) &&
        Objects.equals(ready, podStatus.ready) &&
        Objects.equals(status, podStatus.status) &&
        Objects.equals(creationTimestamp, podStatus.creationTimestamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, ready, status, creationTimestamp);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PodStatusDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    ready: ").append(toIndentedString(ready)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    creationTimestamp: ").append(toIndentedString(creationTimestamp)).append("\n");
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

