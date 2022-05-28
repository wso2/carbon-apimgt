package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.EndpointConfigAttributesDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class EndpointConfigDTO   {
  
    private String url = null;
    private String timeout = null;
    private List<EndpointConfigAttributesDTO> attributes = new ArrayList<EndpointConfigAttributesDTO>();

  /**
   * Service url of the endpoint 
   **/
  public EndpointConfigDTO url(String url) {
    this.url = url;
    return this;
  }

  
  @ApiModelProperty(example = "http://localhost:8280", value = "Service url of the endpoint ")
  @JsonProperty("url")
  public String getUrl() {
    return url;
  }
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * Time out of the endpoint 
   **/
  public EndpointConfigDTO timeout(String timeout) {
    this.timeout = timeout;
    return this;
  }

  
  @ApiModelProperty(example = "1000", value = "Time out of the endpoint ")
  @JsonProperty("timeout")
  public String getTimeout() {
    return timeout;
  }
  public void setTimeout(String timeout) {
    this.timeout = timeout;
  }

  /**
   **/
  public EndpointConfigDTO attributes(List<EndpointConfigAttributesDTO> attributes) {
    this.attributes = attributes;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("attributes")
  public List<EndpointConfigAttributesDTO> getAttributes() {
    return attributes;
  }
  public void setAttributes(List<EndpointConfigAttributesDTO> attributes) {
    this.attributes = attributes;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EndpointConfigDTO endpointConfig = (EndpointConfigDTO) o;
    return Objects.equals(url, endpointConfig.url) &&
        Objects.equals(timeout, endpointConfig.timeout) &&
        Objects.equals(attributes, endpointConfig.attributes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(url, timeout, attributes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EndpointConfigDTO {\n");
    
    sb.append("    url: ").append(toIndentedString(url)).append("\n");
    sb.append("    timeout: ").append(toIndentedString(timeout)).append("\n");
    sb.append("    attributes: ").append(toIndentedString(attributes)).append("\n");
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

