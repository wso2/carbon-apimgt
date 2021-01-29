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



public class MediationPolicyDTO   {
  
    private String id = null;
    private String name = null;
    private String type = null;
    private Boolean shared = null;

  /**
   **/
  public MediationPolicyDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "69ea3fa6-55c6-472e-896d-e449dd34a824", value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public MediationPolicyDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "log_in_message", required = true, value = "")
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
  public MediationPolicyDTO type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(example = "in", value = "")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  /**
   **/
  public MediationPolicyDTO shared(Boolean shared) {
    this.shared = shared;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "")
  @JsonProperty("shared")
  public Boolean isShared() {
    return shared;
  }
  public void setShared(Boolean shared) {
    this.shared = shared;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MediationPolicyDTO mediationPolicy = (MediationPolicyDTO) o;
    return Objects.equals(id, mediationPolicy.id) &&
        Objects.equals(name, mediationPolicy.name) &&
        Objects.equals(type, mediationPolicy.type) &&
        Objects.equals(shared, mediationPolicy.shared);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, type, shared);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MediationPolicyDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    shared: ").append(toIndentedString(shared)).append("\n");
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

