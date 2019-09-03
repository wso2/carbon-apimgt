package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;



public class APIProductURLsDTO   {
  
    private String http = null;
    private String https = null;

  /**
   * HTTP environment URL
   **/
  public APIProductURLsDTO http(String http) {
    this.http = http;
    return this;
  }

  
  @ApiModelProperty(example = "http://localhost:8280/phoneverify", value = "HTTP environment URL")
  @JsonProperty("http")
  public String getHttp() {
    return http;
  }
  public void setHttp(String http) {
    this.http = http;
  }

  /**
   * HTTPS environment URL
   **/
  public APIProductURLsDTO https(String https) {
    this.https = https;
    return this;
  }

  
  @ApiModelProperty(example = "https://localhost:8243/phoneverify", value = "HTTPS environment URL")
  @JsonProperty("https")
  public String getHttps() {
    return https;
  }
  public void setHttps(String https) {
    this.https = https;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIProductURLsDTO apIProductURLs = (APIProductURLsDTO) o;
    return Objects.equals(http, apIProductURLs.http) &&
        Objects.equals(https, apIProductURLs.https);
  }

  @Override
  public int hashCode() {
    return Objects.hash(http, https);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIProductURLsDTO {\n");
    
    sb.append("    http: ").append(toIndentedString(http)).append("\n");
    sb.append("    https: ").append(toIndentedString(https)).append("\n");
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

