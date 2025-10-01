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



public class ConsumerSecretDTO   {
  
    private String id = null;
    private String description = null;
    private String secretValue = null;
    private Long expiresAt = null;

  /**
   * Unique identifier for the secret
   **/
  public ConsumerSecretDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "sec_123456", value = "Unique identifier for the secret")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Human-readable label for the secret
   **/
  public ConsumerSecretDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "pizza application secret", value = "Human-readable label for the secret")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
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
   * expiry timestamp in seconds since epoch
   **/
  public ConsumerSecretDTO expiresAt(Long expiresAt) {
    this.expiresAt = expiresAt;
    return this;
  }

  
  @ApiModelProperty(example = "1755756933", value = "expiry timestamp in seconds since epoch")
  @JsonProperty("expiresAt")
  public Long getExpiresAt() {
    return expiresAt;
  }
  public void setExpiresAt(Long expiresAt) {
    this.expiresAt = expiresAt;
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
    return Objects.equals(id, consumerSecret.id) &&
        Objects.equals(description, consumerSecret.description) &&
        Objects.equals(secretValue, consumerSecret.secretValue) &&
        Objects.equals(expiresAt, consumerSecret.expiresAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, description, secretValue, expiresAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConsumerSecretDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    secretValue: ").append(toIndentedString(secretValue)).append("\n");
    sb.append("    expiresAt: ").append(toIndentedString(expiresAt)).append("\n");
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

