package org.wso2.apk.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

public class TenantDTO   {
  
    private String domain = null;
    private String status = null;

  /**
   * tenant domain
   **/
  public TenantDTO domain(String domain) {
    this.domain = domain;
    return this;
  }

  
  @ApiModelProperty(example = "wso2.com", value = "tenant domain")
  @JsonProperty("domain")
  public String getDomain() {
    return domain;
  }
  public void setDomain(String domain) {
    this.domain = domain;
  }

  /**
   * current status of the tenant active/inactive
   **/
  public TenantDTO status(String status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(example = "active", value = "current status of the tenant active/inactive")
  @JsonProperty("status")
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TenantDTO tenant = (TenantDTO) o;
    return Objects.equals(domain, tenant.domain) &&
        Objects.equals(status, tenant.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(domain, status);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TenantDTO {\n");
    
    sb.append("    domain: ").append(toIndentedString(domain)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
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

