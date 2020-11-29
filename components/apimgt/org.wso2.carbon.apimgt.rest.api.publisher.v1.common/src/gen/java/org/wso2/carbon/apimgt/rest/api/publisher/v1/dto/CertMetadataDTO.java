package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;

/**
 * Representation of the details of a certificate
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Representation of the details of a certificate")

public class CertMetadataDTO   {
  
    private String alias = null;
    private String endpoint = null;

  /**
   **/
  public CertMetadataDTO alias(String alias) {
    this.alias = alias;
    return this;
  }

  
  @ApiModelProperty(example = "wso2carbon", value = "")
  @JsonProperty("alias")
  public String getAlias() {
    return alias;
  }
  public void setAlias(String alias) {
    this.alias = alias;
  }

  /**
   **/
  public CertMetadataDTO endpoint(String endpoint) {
    this.endpoint = endpoint;
    return this;
  }

  
  @ApiModelProperty(example = "www.abc.com", value = "")
  @JsonProperty("endpoint")
  public String getEndpoint() {
    return endpoint;
  }
  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CertMetadataDTO certMetadata = (CertMetadataDTO) o;
    return Objects.equals(alias, certMetadata.alias) &&
        Objects.equals(endpoint, certMetadata.endpoint);
  }

  @Override
  public int hashCode() {
    return Objects.hash(alias, endpoint);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CertMetadataDTO {\n");
    
    sb.append("    alias: ").append(toIndentedString(alias)).append("\n");
    sb.append("    endpoint: ").append(toIndentedString(endpoint)).append("\n");
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

