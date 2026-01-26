package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class ConsumerSecretDTO   {
  
    private String secretId = null;
    private String secretValue = null;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  /**
   * Unique identifier for the secret
   **/
  public ConsumerSecretDTO secretId(String secretId) {
    this.secretId = secretId;
    return this;
  }

  
  @ApiModelProperty(example = "sec_123456", value = "Unique identifier for the secret")
  @JsonProperty("secretId")
  public String getSecretId() {
    return secretId;
  }
  public void setSecretId(String secretId) {
    this.secretId = secretId;
  }

  /**
   * The actual secret string
   **/
  public ConsumerSecretDTO secretValue(String secretValue) {
    this.secretValue = secretValue;
    return this;
  }

  
  @ApiModelProperty(example = "s3cr3tV@lu3", value = "The actual secret string")
  @JsonProperty("secretValue")
  public String getSecretValue() {
    return secretValue;
  }
  public void setSecretValue(String secretValue) {
    this.secretValue = secretValue;
  }

  /**
   * Additional dynamic properties for the secret creation request.
   **/
  public ConsumerSecretDTO additionalProperties(Map<String, Object> additionalProperties) {
    this.additionalProperties = additionalProperties;
    return this;
  }

  
  @ApiModelProperty(example = "{\"expiresAt\":1761568483853,\"description\":\"pizza application secret\"}", value = "Additional dynamic properties for the secret creation request.")
  @JsonProperty("additionalProperties")
  public Map<String, Object> getAdditionalProperties() {
    return additionalProperties;
  }
  public void setAdditionalProperties(Map<String, Object> additionalProperties) {
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
    ConsumerSecretDTO consumerSecret = (ConsumerSecretDTO) o;
    return Objects.equals(secretId, consumerSecret.secretId) &&
        Objects.equals(secretValue, consumerSecret.secretValue) &&
        Objects.equals(additionalProperties, consumerSecret.additionalProperties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(secretId, secretValue, additionalProperties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConsumerSecretDTO {\n");
    
    sb.append("    secretId: ").append(toIndentedString(secretId)).append("\n");
    sb.append("    secretValue: ").append(toIndentedString(secretValue)).append("\n");
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

