package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.internal.service.dto.IPLevelDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class BlockConditionsDTO   {
  
    private List<String> api = new ArrayList<>();
    private List<String> application = new ArrayList<>();
    private List<IPLevelDTO> ip = new ArrayList<>();
    private List<String> user = new ArrayList<>();
    private List<String> custom = new ArrayList<>();

  /**
   **/
  public BlockConditionsDTO api(List<String> api) {
    this.api = api;
    return this;
  }

  
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
  public BlockConditionsDTO application(List<String> application) {
    this.application = application;
    return this;
  }

  
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
  public BlockConditionsDTO ip(List<IPLevelDTO> ip) {
    this.ip = ip;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("ip")
  public List<IPLevelDTO> getIp() {
    return ip;
  }
  public void setIp(List<IPLevelDTO> ip) {
    this.ip = ip;
  }

  /**
   **/
  public BlockConditionsDTO user(List<String> user) {
    this.user = user;
    return this;
  }

  
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
  public BlockConditionsDTO custom(List<String> custom) {
    this.custom = custom;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("custom")
  public List<String> getCustom() {
    return custom;
  }
  public void setCustom(List<String> custom) {
    this.custom = custom;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BlockConditionsDTO blockConditions = (BlockConditionsDTO) o;
    return Objects.equals(api, blockConditions.api) &&
        Objects.equals(application, blockConditions.application) &&
        Objects.equals(ip, blockConditions.ip) &&
        Objects.equals(user, blockConditions.user) &&
        Objects.equals(custom, blockConditions.custom);
  }

  @Override
  public int hashCode() {
    return Objects.hash(api, application, ip, user, custom);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BlockConditionsDTO {\n");
    
    sb.append("    api: ").append(toIndentedString(api)).append("\n");
    sb.append("    application: ").append(toIndentedString(application)).append("\n");
    sb.append("    ip: ").append(toIndentedString(ip)).append("\n");
    sb.append("    user: ").append(toIndentedString(user)).append("\n");
    sb.append("    custom: ").append(toIndentedString(custom)).append("\n");
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

