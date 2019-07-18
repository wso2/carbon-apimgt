package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;



public class ProductAPIOperationsDTO   {
  
    private String uritemplate = null;
    private String httpVerb = null;

  /**
   **/
  public ProductAPIOperationsDTO uritemplate(String uritemplate) {
    this.uritemplate = uritemplate;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("uritemplate")
  public String getUritemplate() {
    return uritemplate;
  }
  public void setUritemplate(String uritemplate) {
    this.uritemplate = uritemplate;
  }

  /**
   **/
  public ProductAPIOperationsDTO httpVerb(String httpVerb) {
    this.httpVerb = httpVerb;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("httpVerb")
  public String getHttpVerb() {
    return httpVerb;
  }
  public void setHttpVerb(String httpVerb) {
    this.httpVerb = httpVerb;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ProductAPIOperationsDTO productAPIOperations = (ProductAPIOperationsDTO) o;
    return Objects.equals(uritemplate, productAPIOperations.uritemplate) &&
        Objects.equals(httpVerb, productAPIOperations.httpVerb);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uritemplate, httpVerb);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ProductAPIOperationsDTO {\n");
    
    sb.append("    uritemplate: ").append(toIndentedString(uritemplate)).append("\n");
    sb.append("    httpVerb: ").append(toIndentedString(httpVerb)).append("\n");
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

