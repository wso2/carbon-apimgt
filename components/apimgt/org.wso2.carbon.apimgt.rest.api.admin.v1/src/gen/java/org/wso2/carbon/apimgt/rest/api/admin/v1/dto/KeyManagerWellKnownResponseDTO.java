package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.KeyManagerDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class KeyManagerWellKnownResponseDTO   {
  
    private Boolean valid = false;
    private KeyManagerDTO value = null;

  /**
   **/
  public KeyManagerWellKnownResponseDTO valid(Boolean valid) {
    this.valid = valid;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "")
  @JsonProperty("valid")
  public Boolean isValid() {
    return valid;
  }
  public void setValid(Boolean valid) {
    this.valid = valid;
  }

  /**
   **/
  public KeyManagerWellKnownResponseDTO value(KeyManagerDTO value) {
    this.value = value;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("value")
  public KeyManagerDTO getValue() {
    return value;
  }
  public void setValue(KeyManagerDTO value) {
    this.value = value;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    KeyManagerWellKnownResponseDTO keyManagerWellKnownResponse = (KeyManagerWellKnownResponseDTO) o;
    return Objects.equals(valid, keyManagerWellKnownResponse.valid) &&
        Objects.equals(value, keyManagerWellKnownResponse.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(valid, value);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class KeyManagerWellKnownResponseDTO {\n");
    
    sb.append("    valid: ").append(toIndentedString(valid)).append("\n");
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
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

