package org.wso2.carbon.apimgt.rest.api.admin.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ClaimMappingEntryDTO  {
  
  
  
  private String remoteClaim = null;
  
  
  private String localClaim = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("remoteClaim")
  public String getRemoteClaim() {
    return remoteClaim;
  }
  public void setRemoteClaim(String remoteClaim) {
    this.remoteClaim = remoteClaim;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("localClaim")
  public String getLocalClaim() {
    return localClaim;
  }
  public void setLocalClaim(String localClaim) {
    this.localClaim = localClaim;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ClaimMappingEntryDTO {\n");
    
    sb.append("  remoteClaim: ").append(remoteClaim).append("\n");
    sb.append("  localClaim: ").append(localClaim).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
