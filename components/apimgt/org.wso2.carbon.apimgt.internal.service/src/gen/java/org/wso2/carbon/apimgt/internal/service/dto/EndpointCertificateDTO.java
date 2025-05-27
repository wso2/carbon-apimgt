package org.wso2.carbon.apimgt.internal.service.dto;

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



public class EndpointCertificateDTO   {
  
    private String alias = null;
    private String endpoint = null;
    private String certificate = null;
    private Integer tenantId = null;
    private String organization = null;

  /**
   **/
  public EndpointCertificateDTO alias(String alias) {
    this.alias = alias;
    return this;
  }

  
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
  public EndpointCertificateDTO endpoint(String endpoint) {
    this.endpoint = endpoint;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("endpoint")
  public String getEndpoint() {
    return endpoint;
  }
  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  /**
   **/
  public EndpointCertificateDTO certificate(String certificate) {
    this.certificate = certificate;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("certificate")
  public String getCertificate() {
    return certificate;
  }
  public void setCertificate(String certificate) {
    this.certificate = certificate;
  }

  /**
   **/
  public EndpointCertificateDTO tenantId(Integer tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("tenantId")
  public Integer getTenantId() {
    return tenantId;
  }
  public void setTenantId(Integer tenantId) {
    this.tenantId = tenantId;
  }

  /**
   **/
  public EndpointCertificateDTO organization(String organization) {
    this.organization = organization;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("organization")
  public String getOrganization() {
    return organization;
  }
  public void setOrganization(String organization) {
    this.organization = organization;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EndpointCertificateDTO endpointCertificate = (EndpointCertificateDTO) o;
    return Objects.equals(alias, endpointCertificate.alias) &&
        Objects.equals(endpoint, endpointCertificate.endpoint) &&
        Objects.equals(certificate, endpointCertificate.certificate) &&
        Objects.equals(tenantId, endpointCertificate.tenantId) &&
        Objects.equals(organization, endpointCertificate.organization);
  }

  @Override
  public int hashCode() {
    return Objects.hash(alias, endpoint, certificate, tenantId, organization);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EndpointCertificateDTO {\n");
    
    sb.append("    alias: ").append(toIndentedString(alias)).append("\n");
    sb.append("    endpoint: ").append(toIndentedString(endpoint)).append("\n");
    sb.append("    certificate: ").append(toIndentedString(certificate)).append("\n");
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
    sb.append("    organization: ").append(toIndentedString(organization)).append("\n");
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

