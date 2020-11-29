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



public class ExternalStoreDTO   {
  
    private String id = null;
    private String displayName = null;
    private String type = null;
    private String endpoint = null;

  /**
   * The external store identifier, which is a unique value. 
   **/
  public ExternalStoreDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "Store123#", value = "The external store identifier, which is a unique value. ")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   * The name of the external API Store that is displayed in the Publisher UI. 
   **/
  public ExternalStoreDTO displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(example = "UKStore", value = "The name of the external API Store that is displayed in the Publisher UI. ")
  @JsonProperty("displayName")
  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * The type of the Store. This can be a WSO2-specific API Store or an external one. 
   **/
  public ExternalStoreDTO type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(example = "wso2", value = "The type of the Store. This can be a WSO2-specific API Store or an external one. ")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  /**
   * The endpoint URL of the external store 
   **/
  public ExternalStoreDTO endpoint(String endpoint) {
    this.endpoint = endpoint;
    return this;
  }

  
  @ApiModelProperty(example = "http://localhost:9764/store", value = "The endpoint URL of the external store ")
  @JsonProperty("endpoint")
  public String getEndpoint() {
    return endpoint;
  }
  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExternalStoreDTO externalStore = (ExternalStoreDTO) o;
    return Objects.equals(id, externalStore.id) &&
        Objects.equals(displayName, externalStore.displayName) &&
        Objects.equals(type, externalStore.type) &&
        Objects.equals(endpoint, externalStore.endpoint);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, displayName, type, endpoint);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExternalStoreDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    endpoint: ").append(toIndentedString(endpoint)).append("\n");
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

