package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;

/**
 * Meta data of certificate
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Meta data of certificate")

public class ClientCertMetadataDTO   {
  
    private String name = null;
    private String applicationId = null;
    private String type = null;
    private String UUID = null;

  /**
   **/
  public ClientCertMetadataDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "wso2carbon", value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public ClientCertMetadataDTO applicationId(String applicationId) {
    this.applicationId = applicationId;
    return this;
  }

  
  @ApiModelProperty(example = "b3ade481-30b0-4b38-9a67-498a40873a6d", value = "")
  @JsonProperty("applicationId")
  public String getApplicationId() {
    return applicationId;
  }
  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  /**
   **/
  public ClientCertMetadataDTO type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(example = "SANDBOX", value = "")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  /**
   **/
  public ClientCertMetadataDTO UUID(String UUID) {
    this.UUID = UUID;
    return this;
  }

  
  @ApiModelProperty(example = "b3ade481-30b0-4b38-9a67-498a40873a6d", value = "")
  @JsonProperty("UUID")
  public String getUUID() {
    return UUID;
  }
  public void setUUID(String UUID) {
    this.UUID = UUID;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ClientCertMetadataDTO clientCertMetadata = (ClientCertMetadataDTO) o;
    return Objects.equals(name, clientCertMetadata.name) &&
        Objects.equals(applicationId, clientCertMetadata.applicationId) &&
        Objects.equals(type, clientCertMetadata.type) &&
        Objects.equals(UUID, clientCertMetadata.UUID);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, applicationId, type, UUID);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ClientCertMetadataDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    applicationId: ").append(toIndentedString(applicationId)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    UUID: ").append(toIndentedString(UUID)).append("\n");
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

