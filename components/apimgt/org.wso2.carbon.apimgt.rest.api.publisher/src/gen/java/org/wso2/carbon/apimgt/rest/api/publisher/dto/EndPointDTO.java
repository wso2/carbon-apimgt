package org.wso2.carbon.apimgt.rest.api.publisher.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * EndPointDTO
 */
@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-02-28T11:12:39.119+05:30")
public class EndPointDTO   {
  private String id = null;

  private String name = null;

  private String endpointConfig = null;

  private String endpointSecurity = null;

  private Long maxTps = null;

  private String type = null;

  public EndPointDTO id(String id) {
    this.id = id;
    return this;
  }

   /**
   * UUID of the Endpoint entry 
   * @return id
  **/
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "UUID of the Endpoint entry ")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public EndPointDTO name(String name) {
    this.name = name;
    return this;
  }

   /**
   * name of the Endpoint entry 
   * @return name
  **/
  @ApiModelProperty(example = "Endpoint 1", value = "name of the Endpoint entry ")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public EndPointDTO endpointConfig(String endpointConfig) {
    this.endpointConfig = endpointConfig;
    return this;
  }

   /**
   * Endpoint Configuration
   * @return endpointConfig
  **/
  @ApiModelProperty(example = "{url: http://localhost:8280, timeout: 1000}", value = "Endpoint Configuration")
  public String getEndpointConfig() {
    return endpointConfig;
  }

  public void setEndpointConfig(String endpointConfig) {
    this.endpointConfig = endpointConfig;
  }

  public EndPointDTO endpointSecurity(String endpointSecurity) {
    this.endpointSecurity = endpointSecurity;
    return this;
  }

   /**
   * Get endpointSecurity
   * @return endpointSecurity
  **/
  @ApiModelProperty(example = "", value = "")
  public String getEndpointSecurity() {
    return endpointSecurity;
  }

  public void setEndpointSecurity(String endpointSecurity) {
    this.endpointSecurity = endpointSecurity;
  }

  public EndPointDTO maxTps(Long maxTps) {
    this.maxTps = maxTps;
    return this;
  }

   /**
   * Endpoint max tps
   * @return maxTps
  **/
  @ApiModelProperty(example = "1000", value = "Endpoint max tps")
  public Long getMaxTps() {
    return maxTps;
  }

  public void setMaxTps(Long maxTps) {
    this.maxTps = maxTps;
  }

  public EndPointDTO type(String type) {
    this.type = type;
    return this;
  }

   /**
   * Get type
   * @return type
  **/
  @ApiModelProperty(example = "http", value = "")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EndPointDTO endPoint = (EndPointDTO) o;
    return Objects.equals(this.id, endPoint.id) &&
        Objects.equals(this.name, endPoint.name) &&
        Objects.equals(this.endpointConfig, endPoint.endpointConfig) &&
        Objects.equals(this.endpointSecurity, endPoint.endpointSecurity) &&
        Objects.equals(this.maxTps, endPoint.maxTps) &&
        Objects.equals(this.type, endPoint.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, endpointConfig, endpointSecurity, maxTps, type);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EndPointDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    endpointConfig: ").append(toIndentedString(endpointConfig)).append("\n");
    sb.append("    endpointSecurity: ").append(toIndentedString(endpointSecurity)).append("\n");
    sb.append("    maxTps: ").append(toIndentedString(maxTps)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
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

