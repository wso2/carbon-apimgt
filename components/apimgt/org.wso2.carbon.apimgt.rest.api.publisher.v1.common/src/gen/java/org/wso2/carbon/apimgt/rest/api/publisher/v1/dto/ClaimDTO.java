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



public class ClaimDTO   {
  
    private String name = null;
    private String URI = null;
    private String value = null;

  /**
   **/
  public ClaimDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "email", value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public ClaimDTO URI(String URI) {
    this.URI = URI;
    return this;
  }

  
  @ApiModelProperty(example = "http://wso2.org/claims/emailaddress", value = "")
  @JsonProperty("URI")
  public String getURI() {
    return URI;
  }
  public void setURI(String URI) {
    this.URI = URI;
  }

  /**
   **/
  public ClaimDTO value(String value) {
    this.value = value;
    return this;
  }

  
  @ApiModelProperty(example = "admin@wso2.com", value = "")
  @JsonProperty("value")
  public String getValue() {
    return value;
  }
  public void setValue(String value) {
    this.value = value;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ClaimDTO claim = (ClaimDTO) o;
    return Objects.equals(name, claim.name) &&
        Objects.equals(URI, claim.URI) &&
        Objects.equals(value, claim.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, URI, value);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ClaimDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    URI: ").append(toIndentedString(URI)).append("\n");
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
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

