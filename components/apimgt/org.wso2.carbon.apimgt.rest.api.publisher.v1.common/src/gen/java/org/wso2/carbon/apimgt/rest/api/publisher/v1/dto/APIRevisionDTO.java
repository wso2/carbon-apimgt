package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionAPIInfoDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class APIRevisionDTO   {
  
    private String id = null;
    private String description = null;
    private APIRevisionAPIInfoDTO apiInfo = null;

  /**
   **/
  public APIRevisionDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public APIRevisionDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "removed a post resource", value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   **/
  public APIRevisionDTO apiInfo(APIRevisionAPIInfoDTO apiInfo) {
    this.apiInfo = apiInfo;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("apiInfo")
  public APIRevisionAPIInfoDTO getApiInfo() {
    return apiInfo;
  }
  public void setApiInfo(APIRevisionAPIInfoDTO apiInfo) {
    this.apiInfo = apiInfo;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIRevisionDTO apIRevision = (APIRevisionDTO) o;
    return Objects.equals(id, apIRevision.id) &&
        Objects.equals(description, apIRevision.description) &&
        Objects.equals(apiInfo, apIRevision.apiInfo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, description, apiInfo);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIRevisionDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    apiInfo: ").append(toIndentedString(apiInfo)).append("\n");
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

