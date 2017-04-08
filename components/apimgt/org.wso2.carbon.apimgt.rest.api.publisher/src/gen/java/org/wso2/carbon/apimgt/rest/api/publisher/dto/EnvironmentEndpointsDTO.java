package org.wso2.carbon.apimgt.rest.api.publisher.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * EnvironmentEndpointsDTO
 */
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-04-06T17:02:03.158+05:30")
public class EnvironmentEndpointsDTO   {
  @JsonProperty("http")
  private String http = null;

  @JsonProperty("https")
  private String https = null;

  public EnvironmentEndpointsDTO http(String http) {
    this.http = http;
    return this;
  }

   /**
   * HTTP environment URL
   * @return http
  **/
  @ApiModelProperty(example = "http://localhost:8280", value = "HTTP environment URL")
  public String getHttp() {
    return http;
  }

  public void setHttp(String http) {
    this.http = http;
  }

  public EnvironmentEndpointsDTO https(String https) {
    this.https = https;
    return this;
  }

   /**
   * HTTPS environment URL
   * @return https
  **/
  @ApiModelProperty(example = "https://localhost:8244", value = "HTTPS environment URL")
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
    EnvironmentEndpointsDTO environmentEndpoints = (EnvironmentEndpointsDTO) o;
    return Objects.equals(this.http, environmentEndpoints.http) &&
        Objects.equals(this.https, environmentEndpoints.https);
  }

  @Override
  public int hashCode() {
    return Objects.hash(http, https);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EnvironmentEndpointsDTO {\n");
    
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

