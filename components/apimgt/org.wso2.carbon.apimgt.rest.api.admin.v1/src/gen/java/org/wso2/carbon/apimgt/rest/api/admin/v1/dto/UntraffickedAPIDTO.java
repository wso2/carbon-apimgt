package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class UntraffickedAPIDTO   {
  
    private String apimApiId = null;
    private String apimApiName = null;
    private String apimApiVersion = null;
    private String method = null;
    private String gatewayPath = null;
    private String serviceIdentity = null;
    private String lastSyncedAt = null;

  /**
   **/
  public UntraffickedAPIDTO apimApiId(String apimApiId) {
    this.apimApiId = apimApiId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("apimApiId")
  public String getApimApiId() {
    return apimApiId;
  }
  public void setApimApiId(String apimApiId) {
    this.apimApiId = apimApiId;
  }

  /**
   **/
  public UntraffickedAPIDTO apimApiName(String apimApiName) {
    this.apimApiName = apimApiName;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("apimApiName")
  public String getApimApiName() {
    return apimApiName;
  }
  public void setApimApiName(String apimApiName) {
    this.apimApiName = apimApiName;
  }

  /**
   **/
  public UntraffickedAPIDTO apimApiVersion(String apimApiVersion) {
    this.apimApiVersion = apimApiVersion;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("apimApiVersion")
  public String getApimApiVersion() {
    return apimApiVersion;
  }
  public void setApimApiVersion(String apimApiVersion) {
    this.apimApiVersion = apimApiVersion;
  }

  /**
   **/
  public UntraffickedAPIDTO method(String method) {
    this.method = method;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("method")
  public String getMethod() {
    return method;
  }
  public void setMethod(String method) {
    this.method = method;
  }

  /**
   **/
  public UntraffickedAPIDTO gatewayPath(String gatewayPath) {
    this.gatewayPath = gatewayPath;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("gatewayPath")
  public String getGatewayPath() {
    return gatewayPath;
  }
  public void setGatewayPath(String gatewayPath) {
    this.gatewayPath = gatewayPath;
  }

  /**
   **/
  public UntraffickedAPIDTO serviceIdentity(String serviceIdentity) {
    this.serviceIdentity = serviceIdentity;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("serviceIdentity")
  public String getServiceIdentity() {
    return serviceIdentity;
  }
  public void setServiceIdentity(String serviceIdentity) {
    this.serviceIdentity = serviceIdentity;
  }

  /**
   **/
  public UntraffickedAPIDTO lastSyncedAt(String lastSyncedAt) {
    this.lastSyncedAt = lastSyncedAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("lastSyncedAt")
  public String getLastSyncedAt() {
    return lastSyncedAt;
  }
  public void setLastSyncedAt(String lastSyncedAt) {
    this.lastSyncedAt = lastSyncedAt;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UntraffickedAPIDTO untraffickedAPI = (UntraffickedAPIDTO) o;
    return Objects.equals(apimApiId, untraffickedAPI.apimApiId) &&
        Objects.equals(apimApiName, untraffickedAPI.apimApiName) &&
        Objects.equals(apimApiVersion, untraffickedAPI.apimApiVersion) &&
        Objects.equals(method, untraffickedAPI.method) &&
        Objects.equals(gatewayPath, untraffickedAPI.gatewayPath) &&
        Objects.equals(serviceIdentity, untraffickedAPI.serviceIdentity) &&
        Objects.equals(lastSyncedAt, untraffickedAPI.lastSyncedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apimApiId, apimApiName, apimApiVersion, method, gatewayPath, serviceIdentity, lastSyncedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UntraffickedAPIDTO {\n");
    
    sb.append("    apimApiId: ").append(toIndentedString(apimApiId)).append("\n");
    sb.append("    apimApiName: ").append(toIndentedString(apimApiName)).append("\n");
    sb.append("    apimApiVersion: ").append(toIndentedString(apimApiVersion)).append("\n");
    sb.append("    method: ").append(toIndentedString(method)).append("\n");
    sb.append("    gatewayPath: ").append(toIndentedString(gatewayPath)).append("\n");
    sb.append("    serviceIdentity: ").append(toIndentedString(serviceIdentity)).append("\n");
    sb.append("    lastSyncedAt: ").append(toIndentedString(lastSyncedAt)).append("\n");
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

