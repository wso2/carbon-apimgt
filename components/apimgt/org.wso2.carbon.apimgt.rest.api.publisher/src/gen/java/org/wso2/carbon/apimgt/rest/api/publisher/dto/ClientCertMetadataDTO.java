package org.wso2.carbon.apimgt.rest.api.publisher.dto;

import io.swagger.annotations.ApiModel;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;



/**
 * Meta data of certificate
 **/


@ApiModel(description = "Meta data of certificate")
public class ClientCertMetadataDTO  {
  
  
  
  private String alias = null;
  
  
  private String apiId = null;
  
  
  private String tier = null;

  
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
  @JsonProperty("apiId")
  public String getApiId() {
    return apiId;
  }
  public void setApiId(String apiId) {
    this.apiId = apiId;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("tier")
  public String getTier() {
    return tier;
  }
  public void setTier(String tier) {
    this.tier = tier;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ClientCertMetadataDTO {\n");
    
    sb.append("  alias: ").append(alias).append("\n");
    sb.append("  apiId: ").append(apiId).append("\n");
    sb.append("  tier: ").append(tier).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
