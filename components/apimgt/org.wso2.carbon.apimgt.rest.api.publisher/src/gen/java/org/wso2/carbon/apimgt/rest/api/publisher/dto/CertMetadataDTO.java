package org.wso2.carbon.apimgt.rest.api.publisher.dto;

import io.swagger.annotations.ApiModel;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;



/**
 * Representation of the details of a certificate
 **/


@ApiModel(description = "Representation of the details of a certificate")
public class CertMetadataDTO  {
  
  
  
  private String alias = null;
  
  
  private String endpoint = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("alias")
  public String getAlias() {
    return alias;
  }
  public void setAlias(String alias) {
    this.alias = alias;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("endpoint")
  public String getEndpoint() {
    return endpoint;
  }
  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class CertMetadataDTO {\n");
    
    sb.append("  alias: ").append(alias).append("\n");
    sb.append("  endpoint: ").append(endpoint).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
