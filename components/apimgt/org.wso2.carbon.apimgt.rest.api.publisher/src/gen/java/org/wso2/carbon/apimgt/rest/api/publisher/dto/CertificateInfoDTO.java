package org.wso2.carbon.apimgt.rest.api.publisher.dto;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.CertificateValidityDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class CertificateInfoDTO  {
  
  
  
  private String status = null;
  
  
  private CertificateValidityDTO validity = null;
  
  
  private String version = null;
  
  
  private String subject = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("status")
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("validity")
  public CertificateValidityDTO getValidity() {
    return validity;
  }
  public void setValidity(CertificateValidityDTO validity) {
    this.validity = validity;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("version")
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("subject")
  public String getSubject() {
    return subject;
  }
  public void setSubject(String subject) {
    this.subject = subject;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class CertificateInfoDTO {\n");
    
    sb.append("  status: ").append(status).append("\n");
    sb.append("  validity: ").append(validity).append("\n");
    sb.append("  version: ").append(version).append("\n");
    sb.append("  subject: ").append(subject).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
