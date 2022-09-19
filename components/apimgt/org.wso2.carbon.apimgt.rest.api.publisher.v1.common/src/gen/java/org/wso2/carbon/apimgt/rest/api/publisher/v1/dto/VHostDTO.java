package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;


import java.util.Objects;




public class VHostDTO   {
  
    private String host = null;
    private String httpContext = null;
    private Integer httpPort = null;
    private Integer httpsPort = null;
    private Integer wsPort = null;
    private Integer wssPort = null;
    private Integer websubHttpPort = null;
    private Integer websubHttpsPort = null;

  /**
   **/
  public VHostDTO host(String host) {
    this.host = host;
    return this;
  }

  
  @ApiModelProperty(example = "mg.wso2.com", value = "")
  @JsonProperty("host")
  public String getHost() {
    return host;
  }
  public void setHost(String host) {
    this.host = host;
  }

  /**
   **/
  public VHostDTO httpContext(String httpContext) {
    this.httpContext = httpContext;
    return this;
  }

  
  @ApiModelProperty(example = "pets", value = "")
  @JsonProperty("httpContext")
  public String getHttpContext() {
    return httpContext;
  }
  public void setHttpContext(String httpContext) {
    this.httpContext = httpContext;
  }

  /**
   **/
  public VHostDTO httpPort(Integer httpPort) {
    this.httpPort = httpPort;
    return this;
  }

  
  @ApiModelProperty(example = "80", value = "")
  @JsonProperty("httpPort")
  public Integer getHttpPort() {
    return httpPort;
  }
  public void setHttpPort(Integer httpPort) {
    this.httpPort = httpPort;
  }

  /**
   **/
  public VHostDTO httpsPort(Integer httpsPort) {
    this.httpsPort = httpsPort;
    return this;
  }

  
  @ApiModelProperty(example = "443", value = "")
  @JsonProperty("httpsPort")
  public Integer getHttpsPort() {
    return httpsPort;
  }
  public void setHttpsPort(Integer httpsPort) {
    this.httpsPort = httpsPort;
  }

  /**
   **/
  public VHostDTO wsPort(Integer wsPort) {
    this.wsPort = wsPort;
    return this;
  }

  
  @ApiModelProperty(example = "9099", value = "")
  @JsonProperty("wsPort")
  public Integer getWsPort() {
    return wsPort;
  }
  public void setWsPort(Integer wsPort) {
    this.wsPort = wsPort;
  }

  /**
   **/
  public VHostDTO wssPort(Integer wssPort) {
    this.wssPort = wssPort;
    return this;
  }

  
  @ApiModelProperty(example = "8099", value = "")
  @JsonProperty("wssPort")
  public Integer getWssPort() {
    return wssPort;
  }
  public void setWssPort(Integer wssPort) {
    this.wssPort = wssPort;
  }

  /**
   **/
  public VHostDTO websubHttpPort(Integer websubHttpPort) {
    this.websubHttpPort = websubHttpPort;
    return this;
  }

  
  @ApiModelProperty(example = "9021", value = "")
  @JsonProperty("websubHttpPort")
  public Integer getWebsubHttpPort() {
    return websubHttpPort;
  }
  public void setWebsubHttpPort(Integer websubHttpPort) {
    this.websubHttpPort = websubHttpPort;
  }

  /**
   **/
  public VHostDTO websubHttpsPort(Integer websubHttpsPort) {
    this.websubHttpsPort = websubHttpsPort;
    return this;
  }

  
  @ApiModelProperty(example = "8021", value = "")
  @JsonProperty("websubHttpsPort")
  public Integer getWebsubHttpsPort() {
    return websubHttpsPort;
  }
  public void setWebsubHttpsPort(Integer websubHttpsPort) {
    this.websubHttpsPort = websubHttpsPort;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    VHostDTO vhost = (VHostDTO) o;
    return Objects.equals(host, vhost.host) &&
        Objects.equals(httpContext, vhost.httpContext) &&
        Objects.equals(httpPort, vhost.httpPort) &&
        Objects.equals(httpsPort, vhost.httpsPort) &&
        Objects.equals(wsPort, vhost.wsPort) &&
        Objects.equals(wssPort, vhost.wssPort) &&
        Objects.equals(websubHttpPort, vhost.websubHttpPort) &&
        Objects.equals(websubHttpsPort, vhost.websubHttpsPort);
  }

  @Override
  public int hashCode() {
    return Objects.hash(host, httpContext, httpPort, httpsPort, wsPort, wssPort, websubHttpPort, websubHttpsPort);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class VHostDTO {\n");
    
    sb.append("    host: ").append(toIndentedString(host)).append("\n");
    sb.append("    httpContext: ").append(toIndentedString(httpContext)).append("\n");
    sb.append("    httpPort: ").append(toIndentedString(httpPort)).append("\n");
    sb.append("    httpsPort: ").append(toIndentedString(httpsPort)).append("\n");
    sb.append("    wsPort: ").append(toIndentedString(wsPort)).append("\n");
    sb.append("    wssPort: ").append(toIndentedString(wssPort)).append("\n");
    sb.append("    websubHttpPort: ").append(toIndentedString(websubHttpPort)).append("\n");
    sb.append("    websubHttpsPort: ").append(toIndentedString(websubHttpsPort)).append("\n");
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

