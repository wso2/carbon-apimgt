package org.wso2.carbon.apimgt.rest.api.store.v1.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class APIInfoDTO  {
  
  
  
  private String id = null;
  
  @NotNull
  private String name = null;
  
  
  private String description = null;
  
  @NotNull
  private String context = null;
  
  @NotNull
  private String version = null;
  
  
  private Boolean hasOwnGateway = null;
  
  @NotNull
  private String provider = null;
  
  @NotNull
  private String lifeCycleStatus = null;

  
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
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("context")
  public String getContext() {
    return context;
  }
  public void setContext(String context) {
    this.context = context;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("version")
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("hasOwnGateway")
  public Boolean getHasOwnGateway() {
    return hasOwnGateway;
  }
  public void setHasOwnGateway(Boolean hasOwnGateway) {
    this.hasOwnGateway = hasOwnGateway;
  }

  
  /**
   * If the provider value is not given, the user invoking the API will be used as the provider.\n
   **/
  @ApiModelProperty(required = true, value = "If the provider value is not given, the user invoking the API will be used as the provider.\n")
  @JsonProperty("provider")
  public String getProvider() {
    return provider;
  }
  public void setProvider(String provider) {
    this.provider = provider;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("lifeCycleStatus")
  public String getLifeCycleStatus() {
    return lifeCycleStatus;
  }
  public void setLifeCycleStatus(String lifeCycleStatus) {
    this.lifeCycleStatus = lifeCycleStatus;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIInfoDTO {\n");
    
    sb.append("  id: ").append(id).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("  description: ").append(description).append("\n");
    sb.append("  context: ").append(context).append("\n");
    sb.append("  version: ").append(version).append("\n");
    sb.append("  hasOwnGateway: ").append(hasOwnGateway).append("\n");
    sb.append("  provider: ").append(provider).append("\n");
    sb.append("  lifeCycleStatus: ").append(lifeCycleStatus).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
