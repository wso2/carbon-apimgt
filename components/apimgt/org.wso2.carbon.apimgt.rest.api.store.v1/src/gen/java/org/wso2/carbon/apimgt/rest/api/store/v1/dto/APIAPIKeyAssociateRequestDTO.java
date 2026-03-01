package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

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



public class APIAPIKeyAssociateRequestDTO   {
  
    private String keyUUID = null;
    private String applicationUUID = null;

  /**
   * The UUID of the API key
   **/
  public APIAPIKeyAssociateRequestDTO keyUUID(String keyUUID) {
    this.keyUUID = keyUUID;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The UUID of the API key")
  @JsonProperty("keyUUID")
  @NotNull
  public String getKeyUUID() {
    return keyUUID;
  }
  public void setKeyUUID(String keyUUID) {
    this.keyUUID = keyUUID;
  }

  /**
   * The UUID of the Application to be associated
   **/
  public APIAPIKeyAssociateRequestDTO applicationUUID(String applicationUUID) {
    this.applicationUUID = applicationUUID;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The UUID of the Application to be associated")
  @JsonProperty("applicationUUID")
  @NotNull
  public String getApplicationUUID() {
    return applicationUUID;
  }
  public void setApplicationUUID(String applicationUUID) {
    this.applicationUUID = applicationUUID;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIAPIKeyAssociateRequestDTO apIAPIKeyAssociateRequest = (APIAPIKeyAssociateRequestDTO) o;
    return Objects.equals(keyUUID, apIAPIKeyAssociateRequest.keyUUID) &&
        Objects.equals(applicationUUID, apIAPIKeyAssociateRequest.applicationUUID);
  }

  @Override
  public int hashCode() {
    return Objects.hash(keyUUID, applicationUUID);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIAPIKeyAssociateRequestDTO {\n");
    
    sb.append("    keyUUID: ").append(toIndentedString(keyUUID)).append("\n");
    sb.append("    applicationUUID: ").append(toIndentedString(applicationUUID)).append("\n");
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

