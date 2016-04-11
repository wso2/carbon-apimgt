package org.wso2.carbon.throttle.service.dto;

import java.util.*;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class BlockConditionsDTO  {
  
  
  
  private List<String> api = new ArrayList<String>();
  
  
  private List<String> application = new ArrayList<String>();
  
  
  private List<String> ip = new ArrayList<String>();
  
  
  private List<String> user = new ArrayList<String>();
  
  
  private List<String> custom = new ArrayList<String>();

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("api")
  public List<String> getApi() {
    return api;
  }
  public void setApi(List<String> api) {
    this.api = api;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("application")
  public List<String> getApplication() {
    return application;
  }
  public void setApplication(List<String> application) {
    this.application = application;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("ip")
  public List<String> getIp() {
    return ip;
  }
  public void setIp(List<String> ip) {
    this.ip = ip;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("user")
  public List<String> getUser() {
    return user;
  }
  public void setUser(List<String> user) {
    this.user = user;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("custom")
  public List<String> getCustom() {
    return custom;
  }
  public void setCustom(List<String> custom) {
    this.custom = custom;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class BlockConditionsDTO {\n");
    
    sb.append("  api: ").append(api).append("\n");
    sb.append("  application: ").append(application).append("\n");
    sb.append("  ip: ").append(ip).append("\n");
    sb.append("  user: ").append(user).append("\n");
    sb.append("  custom: ").append(custom).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
