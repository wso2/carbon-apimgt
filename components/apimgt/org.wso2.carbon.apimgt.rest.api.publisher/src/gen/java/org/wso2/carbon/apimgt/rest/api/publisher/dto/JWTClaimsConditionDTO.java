package org.wso2.carbon.apimgt.rest.api.publisher.dto;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.ThrottleConditionDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class JWTClaimsConditionDTO extends ThrottleConditionDTO {
  
  
  
  private String claimUrl = null;
  
  
  private String attribute = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("claimUrl")
  public String getClaimUrl() {
    return claimUrl;
  }
  public void setClaimUrl(String claimUrl) {
    this.claimUrl = claimUrl;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("attribute")
  public String getAttribute() {
    return attribute;
  }
  public void setAttribute(String attribute) {
    this.attribute = attribute;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class JWTClaimsConditionDTO {\n");
    sb.append("  " + super.toString()).append("\n");
    sb.append("  claimUrl: ").append(claimUrl).append("\n");
    sb.append("  attribute: ").append(attribute).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
