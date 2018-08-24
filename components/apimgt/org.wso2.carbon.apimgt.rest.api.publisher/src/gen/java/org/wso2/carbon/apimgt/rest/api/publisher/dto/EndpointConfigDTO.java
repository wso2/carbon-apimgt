package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * EndpointConfigDTO
 */
public class EndpointConfigDTO   {
  @SerializedName("url")
  private String url = null;

  @SerializedName("timeout")
  private String timeout = null;

  public EndpointConfigDTO url(String url) {
    this.url = url;
    return this;
  }

   /**
   * Service url of the endpoint 
   * @return url
  **/
  @ApiModelProperty(example = "http://localhost:8280", value = "Service url of the endpoint ")
  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public EndpointConfigDTO timeout(String timeout) {
    this.timeout = timeout;
    return this;
  }

   /**
   * Time out of the endpoint 
   * @return timeout
  **/
  @ApiModelProperty(example = "1000", value = "Time out of the endpoint ")
  public String getTimeout() {
    return timeout;
  }

  public void setTimeout(String timeout) {
    this.timeout = timeout;
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
    return Objects.equals(this.url, endpointConfig.url) &&
        Objects.equals(this.timeout, endpointConfig.timeout);
  }

  @Override
  public int hashCode() {
    return Objects.hash(url, timeout);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EndpointConfigDTO {\n");
    
    sb.append("    url: ").append(toIndentedString(url)).append("\n");
    sb.append("    timeout: ").append(toIndentedString(timeout)).append("\n");
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

