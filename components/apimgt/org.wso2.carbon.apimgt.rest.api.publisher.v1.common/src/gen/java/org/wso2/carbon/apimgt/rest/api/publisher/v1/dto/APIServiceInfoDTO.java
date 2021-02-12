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



public class APIServiceInfoDTO   {
  
    private String id = null;
    private Boolean outdated = null;

  /**
   **/
  public APIServiceInfoDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "36d4988b-f4ea-46d1-bfd8-38bc1fc5a118", value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public APIServiceInfoDTO outdated(Boolean outdated) {
    this.outdated = outdated;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "")
  @JsonProperty("outdated")
  public Boolean isOutdated() {
    return outdated;
  }
  public void setOutdated(Boolean outdated) {
    this.outdated = outdated;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIServiceInfoDTO apIServiceInfo = (APIServiceInfoDTO) o;
    return Objects.equals(id, apIServiceInfo.id) &&
        Objects.equals(outdated, apIServiceInfo.outdated);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, outdated);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIServiceInfoDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    outdated: ").append(toIndentedString(outdated)).append("\n");
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

