package org.wso2.carbon.throttle.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "")
public class RevokedJWTDTO  {
  
  
  
  private String jwtSignature = null;
  
  
  private Long expiryTime = null;

  
  /**
   * signature of the JWT token.
   **/
  @ApiModelProperty(value = "signature of the JWT token.")
  @JsonProperty("jwtSignature")
  public String getJwtSignature() {
    return jwtSignature;
  }
  public void setJwtSignature(String jwtSignature) {
    this.jwtSignature = jwtSignature;
  }

  
  /**
   * expiry timestamp.
   **/
  @ApiModelProperty(value = "expiry timestamp.")
  @JsonProperty("expiryTime")
  public Long getExpiryTime() {
    return expiryTime;
  }
  public void setExpiryTime(Long expiryTime) {
    this.expiryTime = expiryTime;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class RevokedJWTDTO {\n");
    
    sb.append("  jwtSignature: ").append(jwtSignature).append("\n");
    sb.append("  expiryTime: ").append(expiryTime).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
