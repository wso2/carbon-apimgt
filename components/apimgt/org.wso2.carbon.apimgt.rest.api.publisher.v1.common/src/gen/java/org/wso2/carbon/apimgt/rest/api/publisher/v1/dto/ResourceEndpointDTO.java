package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class ResourceEndpointDTO   {
  
    private String id = null;
    private String name = null;

    @XmlType(name="EndpointTypeEnum")
    @XmlEnum(String.class)
    public enum EndpointTypeEnum {
        HTTP("HTTP"),
        ADDRESS("ADDRESS");
        private String value;

        EndpointTypeEnum (String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static EndpointTypeEnum fromValue(String v) {
            for (EndpointTypeEnum b : EndpointTypeEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private EndpointTypeEnum endpointType = null;
    private String url = null;
    private Map<String, String> security = new HashMap<String, String>();
    private Map<String, String> config = new HashMap<String, String>();

  /**
   **/
  public ResourceEndpointDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public ResourceEndpointDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("name")
  @NotNull
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public ResourceEndpointDTO endpointType(EndpointTypeEnum endpointType) {
    this.endpointType = endpointType;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("endpointType")
  @NotNull
  public EndpointTypeEnum getEndpointType() {
    return endpointType;
  }
  public void setEndpointType(EndpointTypeEnum endpointType) {
    this.endpointType = endpointType;
  }

  /**
   **/
  public ResourceEndpointDTO url(String url) {
    this.url = url;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("url")
  @NotNull
  public String getUrl() {
    return url;
  }
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   **/
  public ResourceEndpointDTO security(Map<String, String> security) {
    this.security = security;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("security")
  public Map<String, String> getSecurity() {
    return security;
  }
  public void setSecurity(Map<String, String> security) {
    this.security = security;
  }

  /**
   **/
  public ResourceEndpointDTO config(Map<String, String> config) {
    this.config = config;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("config")
  public Map<String, String> getConfig() {
    return config;
  }
  public void setConfig(Map<String, String> config) {
    this.config = config;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResourceEndpointDTO resourceEndpoint = (ResourceEndpointDTO) o;
    return Objects.equals(id, resourceEndpoint.id) &&
        Objects.equals(name, resourceEndpoint.name) &&
        Objects.equals(endpointType, resourceEndpoint.endpointType) &&
        Objects.equals(url, resourceEndpoint.url) &&
        Objects.equals(security, resourceEndpoint.security) &&
        Objects.equals(config, resourceEndpoint.config);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, endpointType, url, security, config);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ResourceEndpointDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    endpointType: ").append(toIndentedString(endpointType)).append("\n");
    sb.append("    url: ").append(toIndentedString(url)).append("\n");
    sb.append("    security: ").append(toIndentedString(security)).append("\n");
    sb.append("    config: ").append(toIndentedString(config)).append("\n");
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

