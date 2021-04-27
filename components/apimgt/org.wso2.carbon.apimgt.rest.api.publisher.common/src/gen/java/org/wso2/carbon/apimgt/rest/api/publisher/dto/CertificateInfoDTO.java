package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CertificateValidityDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class CertificateInfoDTO   {
  
    private String status = null;
    private CertificateValidityDTO validity = null;
    private String version = null;
    private String subject = null;

  /**
   **/
  public CertificateInfoDTO status(String status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(example = "Active", value = "")
  @JsonProperty("status")
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   **/
  public CertificateInfoDTO validity(CertificateValidityDTO validity) {
    this.validity = validity;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("validity")
  public CertificateValidityDTO getValidity() {
    return validity;
  }
  public void setValidity(CertificateValidityDTO validity) {
    this.validity = validity;
  }

  /**
   **/
  public CertificateInfoDTO version(String version) {
    this.version = version;
    return this;
  }

  
  @ApiModelProperty(example = "V3", value = "")
  @JsonProperty("version")
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   **/
  public CertificateInfoDTO subject(String subject) {
    this.subject = subject;
    return this;
  }

  
  @ApiModelProperty(example = "CN=wso2.com, OU=wso2, O=wso2, L=Colombo, ST=Western, C=LK", value = "")
  @JsonProperty("subject")
  public String getSubject() {
    return subject;
  }
  public void setSubject(String subject) {
    this.subject = subject;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CertificateInfoDTO certificateInfo = (CertificateInfoDTO) o;
    return Objects.equals(status, certificateInfo.status) &&
        Objects.equals(validity, certificateInfo.validity) &&
        Objects.equals(version, certificateInfo.version) &&
        Objects.equals(subject, certificateInfo.subject);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, validity, version, subject);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CertificateInfoDTO {\n");
    
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    validity: ").append(toIndentedString(validity)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    subject: ").append(toIndentedString(subject)).append("\n");
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

