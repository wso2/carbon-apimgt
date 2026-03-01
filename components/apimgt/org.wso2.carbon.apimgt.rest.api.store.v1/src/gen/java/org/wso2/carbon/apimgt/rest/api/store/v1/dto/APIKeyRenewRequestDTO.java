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



public class APIKeyRenewRequestDTO   {
  
    private String keyUUID = null;
    private Integer validityPeriod = null;
    private Object additionalProperties = null;

  /**
   * The UUID of the API key
   **/
  public APIKeyRenewRequestDTO keyUUID(String keyUUID) {
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
   * API key validity period
   **/
  public APIKeyRenewRequestDTO validityPeriod(Integer validityPeriod) {
    this.validityPeriod = validityPeriod;
    return this;
  }

  
  @ApiModelProperty(example = "3600", value = "API key validity period")
  @JsonProperty("validityPeriod")
  public Integer getValidityPeriod() {
    return validityPeriod;
  }
  public void setValidityPeriod(Integer validityPeriod) {
    this.validityPeriod = validityPeriod;
  }

  /**
   * Additional parameters if Authorization server needs any
   **/
  public APIKeyRenewRequestDTO additionalProperties(Object additionalProperties) {
    this.additionalProperties = additionalProperties;
    return this;
  }

  
  @ApiModelProperty(value = "Additional parameters if Authorization server needs any")
      @Valid
  @JsonProperty("additionalProperties")
  public Object getAdditionalProperties() {
    return additionalProperties;
  }
  public void setAdditionalProperties(Object additionalProperties) {
    this.additionalProperties = additionalProperties;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIKeyRenewRequestDTO apIKeyRenewRequest = (APIKeyRenewRequestDTO) o;
    return Objects.equals(keyUUID, apIKeyRenewRequest.keyUUID) &&
        Objects.equals(validityPeriod, apIKeyRenewRequest.validityPeriod) &&
        Objects.equals(additionalProperties, apIKeyRenewRequest.additionalProperties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(keyUUID, validityPeriod, additionalProperties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIKeyRenewRequestDTO {\n");
    
    sb.append("    keyUUID: ").append(toIndentedString(keyUUID)).append("\n");
    sb.append("    validityPeriod: ").append(toIndentedString(validityPeriod)).append("\n");
    sb.append("    additionalProperties: ").append(toIndentedString(additionalProperties)).append("\n");
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

