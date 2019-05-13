package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class APIOperationsDTO  {
  
  
  
  private String uritemplate = "/*";
  
  
  private String httpVerb = "GET";
  
  
  private String id = null;
  
  
  private String throttlingPolicy = null;
  
  
  private List<String> scopes = new ArrayList<String>();
  
  
  private String authType = "Any";

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("uritemplate")
  public String getUritemplate() {
    return uritemplate;
  }
  public void setUritemplate(String uritemplate) {
    this.uritemplate = uritemplate;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("httpVerb")
  public String getHttpVerb() {
    return httpVerb;
  }
  public void setHttpVerb(String httpVerb) {
    this.httpVerb = httpVerb;
  }

  
  /**
   **/
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
  @ApiModelProperty(value = "")
  @JsonProperty("throttlingPolicy")
  public String getThrottlingPolicy() {
    return throttlingPolicy;
  }
  public void setThrottlingPolicy(String throttlingPolicy) {
    this.throttlingPolicy = throttlingPolicy;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("scopes")
  public List<String> getScopes() {
    return scopes;
  }
  public void setScopes(List<String> scopes) {
    this.scopes = scopes;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("authType")
  public String getAuthType() {
    return authType;
  }
  public void setAuthType(String authType) {
    this.authType = authType;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIOperationsDTO {\n");
    
    sb.append("  uritemplate: ").append(uritemplate).append("\n");
    sb.append("  httpVerb: ").append(httpVerb).append("\n");
    sb.append("  id: ").append(id).append("\n");
    sb.append("  throttlingPolicy: ").append(throttlingPolicy).append("\n");
    sb.append("  scopes: ").append(scopes).append("\n");
    sb.append("  authType: ").append(authType).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
