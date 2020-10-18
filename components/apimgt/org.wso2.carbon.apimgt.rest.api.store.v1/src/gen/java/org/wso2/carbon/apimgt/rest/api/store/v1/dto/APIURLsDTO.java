package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class APIURLsDTO   {
  
    private String http = null;
    private String https = null;
    private String ws = null;
    private String wss = null;

  /**
   * HTTP environment URL
   **/
  public APIURLsDTO http(String http) {
    this.http = http;
    return this;
  }

  
  @ApiModelProperty(example = "http://localhost:8280/phoneverify/1.0.0", value = "HTTP environment URL")
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
  public APIURLsDTO https(String https) {
    this.https = https;
    return this;
  }

  
  @ApiModelProperty(example = "https://localhost:8243/phoneverify/1.0.0", value = "HTTPS environment URL")
  @JsonProperty("https")
  public String getHttps() {
    return https;
  }
  public void setHttps(String https) {
    this.https = https;
  }

  /**
   * WS environment URL
   **/
  public APIURLsDTO ws(String ws) {
    this.ws = ws;
    return this;
  }

  
  @ApiModelProperty(example = "ws://localhost:9099/phoneverify/1.0.0", value = "WS environment URL")
  @JsonProperty("ws")
  public String getWs() {
    return ws;
  }
  public void setWs(String ws) {
    this.ws = ws;
  }

  /**
   * WSS environment URL
   **/
  public APIURLsDTO wss(String wss) {
    this.wss = wss;
    return this;
  }

  
  @ApiModelProperty(example = "wss://localhost:9099/phoneverify/1.0.0", value = "WSS environment URL")
  @JsonProperty("wss")
  public String getWss() {
    return wss;
  }
  public void setWss(String wss) {
    this.wss = wss;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIURLsDTO apIURLs = (APIURLsDTO) o;
    return Objects.equals(http, apIURLs.http) &&
        Objects.equals(https, apIURLs.https) &&
        Objects.equals(ws, apIURLs.ws) &&
        Objects.equals(wss, apIURLs.wss);
  }

  @Override
  public int hashCode() {
    return Objects.hash(http, https, ws, wss);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIURLsDTO {\n");
    
    sb.append("    http: ").append(toIndentedString(http)).append("\n");
    sb.append("    https: ").append(toIndentedString(https)).append("\n");
    sb.append("    ws: ").append(toIndentedString(ws)).append("\n");
    sb.append("    wss: ").append(toIndentedString(wss)).append("\n");
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

