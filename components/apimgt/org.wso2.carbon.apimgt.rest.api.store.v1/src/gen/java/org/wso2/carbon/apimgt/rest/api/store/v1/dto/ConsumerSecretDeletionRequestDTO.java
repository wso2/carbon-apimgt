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



public class ConsumerSecretDeletionRequestDTO   {
  
    private String referenceId = null;

  /**
   * Reference identifier for the secret
   **/
  public ConsumerSecretDeletionRequestDTO referenceId(String referenceId) {
    this.referenceId = referenceId;
    return this;
  }

  
  @ApiModelProperty(example = "sec_123456", value = "Reference identifier for the secret")
  @JsonProperty("referenceId")
  public String getReferenceId() {
    return referenceId;
  }
  public void setReferenceId(String referenceId) {
    this.referenceId = referenceId;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConsumerSecretDeletionRequestDTO consumerSecretDeletionRequest = (ConsumerSecretDeletionRequestDTO) o;
    return Objects.equals(referenceId, consumerSecretDeletionRequest.referenceId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(referenceId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConsumerSecretDeletionRequestDTO {\n");
    
    sb.append("    referenceId: ").append(toIndentedString(referenceId)).append("\n");
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

