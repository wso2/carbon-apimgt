package org.wso2.carbon.apimgt.rest.api.gateway.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class ApplicationKeyMappingDTO   {
  
    private String consumerKey = null;
    private String keyType = null;
    private String keyManager = null;

  /**
   **/
  public ApplicationKeyMappingDTO consumerKey(String consumerKey) {
    this.consumerKey = consumerKey;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("consumerKey")
  public String getConsumerKey() {
    return consumerKey;
  }
  public void setConsumerKey(String consumerKey) {
    this.consumerKey = consumerKey;
  }

  /**
   **/
  public ApplicationKeyMappingDTO keyType(String keyType) {
    this.keyType = keyType;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("keyType")
  public String getKeyType() {
    return keyType;
  }
  public void setKeyType(String keyType) {
    this.keyType = keyType;
  }

  /**
   **/
  public ApplicationKeyMappingDTO keyManager(String keyManager) {
    this.keyManager = keyManager;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("keyManager")
  public String getKeyManager() {
    return keyManager;
  }
  public void setKeyManager(String keyManager) {
    this.keyManager = keyManager;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApplicationKeyMappingDTO applicationKeyMapping = (ApplicationKeyMappingDTO) o;
    return Objects.equals(consumerKey, applicationKeyMapping.consumerKey) &&
        Objects.equals(keyType, applicationKeyMapping.keyType) &&
        Objects.equals(keyManager, applicationKeyMapping.keyManager);
  }

  @Override
  public int hashCode() {
    return Objects.hash(consumerKey, keyType, keyManager);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationKeyMappingDTO {\n");
    
    sb.append("    consumerKey: ").append(toIndentedString(consumerKey)).append("\n");
    sb.append("    keyType: ").append(toIndentedString(keyType)).append("\n");
    sb.append("    keyManager: ").append(toIndentedString(keyManager)).append("\n");
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

