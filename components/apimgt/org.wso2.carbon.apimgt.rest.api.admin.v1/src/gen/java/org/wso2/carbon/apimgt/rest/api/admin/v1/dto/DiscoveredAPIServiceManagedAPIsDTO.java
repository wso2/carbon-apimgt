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



public class DiscoveredAPIServiceManagedAPIsDTO   {
  
    private String apimApiId = null;
    private String apimApiName = null;
    private String apimApiVersion = null;

  /**
   **/
  public DiscoveredAPIServiceManagedAPIsDTO apimApiId(String apimApiId) {
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
  public DiscoveredAPIServiceManagedAPIsDTO apimApiName(String apimApiName) {
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
  public DiscoveredAPIServiceManagedAPIsDTO apimApiVersion(String apimApiVersion) {
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


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DiscoveredAPIServiceManagedAPIsDTO discoveredAPIServiceManagedAPIs = (DiscoveredAPIServiceManagedAPIsDTO) o;
    return Objects.equals(apimApiId, discoveredAPIServiceManagedAPIs.apimApiId) &&
        Objects.equals(apimApiName, discoveredAPIServiceManagedAPIs.apimApiName) &&
        Objects.equals(apimApiVersion, discoveredAPIServiceManagedAPIs.apimApiVersion);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apimApiId, apimApiName, apimApiVersion);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DiscoveredAPIServiceManagedAPIsDTO {\n");
    
    sb.append("    apimApiId: ").append(toIndentedString(apimApiId)).append("\n");
    sb.append("    apimApiName: ").append(toIndentedString(apimApiName)).append("\n");
    sb.append("    apimApiVersion: ").append(toIndentedString(apimApiVersion)).append("\n");
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

