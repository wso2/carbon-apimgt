package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class ServiceDiscoveriesInfoDTO   {
  
    private String serviceName = null;
    private String serviceURL = null;
    private String properties = null;

  /**
   * service name available in the cluster
   **/
  public ServiceDiscoveriesInfoDTO serviceName(String serviceName) {
    this.serviceName = serviceName;
    return this;
  }

  
  @ApiModelProperty(example = "foo-service", value = "service name available in the cluster")
  @JsonProperty("serviceName")
  public String getServiceName() {
    return serviceName;
  }
  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  /**
   * url of the service
   **/
  public ServiceDiscoveriesInfoDTO serviceURL(String serviceURL) {
    this.serviceURL = serviceURL;
    return this;
  }

  
  @ApiModelProperty(example = "http://10.0.0.11:6379", value = "url of the service")
  @JsonProperty("serviceURL")
  public String getServiceURL() {
    return serviceURL;
  }
  public void setServiceURL(String serviceURL) {
    this.serviceURL = serviceURL;
  }

  /**
   * properties of the available services
   **/
  public ServiceDiscoveriesInfoDTO properties(String properties) {
    this.properties = properties;
    return this;
  }

  
  @ApiModelProperty(value = "properties of the available services")
  @JsonProperty("properties")
  public String getProperties() {
    return properties;
  }
  public void setProperties(String properties) {
    this.properties = properties;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ServiceDiscoveriesInfoDTO serviceDiscoveriesInfo = (ServiceDiscoveriesInfoDTO) o;
    return Objects.equals(serviceName, serviceDiscoveriesInfo.serviceName) &&
        Objects.equals(serviceURL, serviceDiscoveriesInfo.serviceURL) &&
        Objects.equals(properties, serviceDiscoveriesInfo.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(serviceName, serviceURL, properties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ServiceDiscoveriesInfoDTO {\n");
    
    sb.append("    serviceName: ").append(toIndentedString(serviceName)).append("\n");
    sb.append("    serviceURL: ").append(toIndentedString(serviceURL)).append("\n");
    sb.append("    properties: ").append(toIndentedString(properties)).append("\n");
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

