package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class URLMappingDTO   {
  
    private Integer id = null;
    private Integer apiId = null;
    private String authScheme = null;
    private String throttlingPolicy = null;

  /**
   **/
  public URLMappingDTO id(Integer id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("id")
  public Integer getId() {
    return id;
  }
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   **/
  public URLMappingDTO apiId(Integer apiId) {
    this.apiId = apiId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("apiId")
  public Integer getApiId() {
    return apiId;
  }
  public void setApiId(Integer apiId) {
    this.apiId = apiId;
  }

  /**
   **/
  public URLMappingDTO authScheme(String authScheme) {
    this.authScheme = authScheme;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("authScheme")
  public String getAuthScheme() {
    return authScheme;
  }
  public void setAuthScheme(String authScheme) {
    this.authScheme = authScheme;
  }

  /**
   **/
  public URLMappingDTO throttlingPolicy(String throttlingPolicy) {
    this.throttlingPolicy = throttlingPolicy;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("throttlingPolicy")
  public String getThrottlingPolicy() {
    return throttlingPolicy;
  }
  public void setThrottlingPolicy(String throttlingPolicy) {
    this.throttlingPolicy = throttlingPolicy;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    URLMappingDTO urLMapping = (URLMappingDTO) o;
    return Objects.equals(id, urLMapping.id) &&
        Objects.equals(apiId, urLMapping.apiId) &&
        Objects.equals(authScheme, urLMapping.authScheme) &&
        Objects.equals(throttlingPolicy, urLMapping.throttlingPolicy);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, apiId, authScheme, throttlingPolicy);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class URLMappingDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    apiId: ").append(toIndentedString(apiId)).append("\n");
    sb.append("    authScheme: ").append(toIndentedString(authScheme)).append("\n");
    sb.append("    throttlingPolicy: ").append(toIndentedString(throttlingPolicy)).append("\n");
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

