package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class CertificateValidityDTO  {
  
  
  
  private String from = null;
  
  
  private String to = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("from")
  public String getFrom() {
    return from;
  }
  public void setFrom(String from) {
    this.from = from;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("to")
  public String getTo() {
    return to;
  }
  public void setTo(String to) {
    this.to = to;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class CertificateValidityDTO {\n");
    
    sb.append("  from: ").append(from).append("\n");
    sb.append("  to: ").append(to).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
