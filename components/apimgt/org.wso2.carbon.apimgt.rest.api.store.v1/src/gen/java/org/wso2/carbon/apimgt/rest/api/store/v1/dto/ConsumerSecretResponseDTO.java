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



public class ConsumerSecretResponseDTO   {
  
    private String id = null;
    private String description = null;
    private String consumerSecret = null;
    private Long consumerSecretExpiresAt = null;

  /**
   * Unique identifier for the secret
   **/
  public ConsumerSecretResponseDTO id(String id) {
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
  public ConsumerSecretResponseDTO description(String description) {
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
  public ConsumerSecretResponseDTO consumerSecret(String consumerSecret) {
    this.consumerSecret = consumerSecret;
    return this;
  }

  
  @ApiModelProperty(example = "s3cr3tV@lu3", value = "The actual secret string")
  @JsonProperty("consumer_secret")
  public String getConsumerSecret() {
    return consumerSecret;
  }
  public void setConsumerSecret(String consumerSecret) {
    this.consumerSecret = consumerSecret;
  }

  /**
   * expiry timestamp in seconds since epoch
   **/
  public ConsumerSecretResponseDTO consumerSecretExpiresAt(Long consumerSecretExpiresAt) {
    this.consumerSecretExpiresAt = consumerSecretExpiresAt;
    return this;
  }

  
  @ApiModelProperty(example = "1755756933", value = "expiry timestamp in seconds since epoch")
  @JsonProperty("consumer_secret_expires_at")
  public Long getConsumerSecretExpiresAt() {
    return consumerSecretExpiresAt;
  }
  public void setConsumerSecretExpiresAt(Long consumerSecretExpiresAt) {
    this.consumerSecretExpiresAt = consumerSecretExpiresAt;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConsumerSecretResponseDTO consumerSecretResponse = (ConsumerSecretResponseDTO) o;
    return Objects.equals(id, consumerSecretResponse.id) &&
        Objects.equals(description, consumerSecretResponse.description) &&
        Objects.equals(consumerSecret, consumerSecretResponse.consumerSecret) &&
        Objects.equals(consumerSecretExpiresAt, consumerSecretResponse.consumerSecretExpiresAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, description, consumerSecret, consumerSecretExpiresAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConsumerSecretResponseDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    consumerSecret: ").append(toIndentedString(consumerSecret)).append("\n");
    sb.append("    consumerSecretExpiresAt: ").append(toIndentedString(consumerSecretExpiresAt)).append("\n");
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

