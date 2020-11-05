package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.CustomUrlInfoDevPortalDTO;
import javax.validation.constraints.*;

/**
 * The custom url information of the tenant domain
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

@ApiModel(description = "The custom url information of the tenant domain")

public class CustomUrlInfoDTO   {
  
    private String tenantDomain = null;
    private String tenantAdminUsername = null;
    private Boolean enabled = null;
    private CustomUrlInfoDevPortalDTO devPortal = null;

  /**
   **/
  public CustomUrlInfoDTO tenantDomain(String tenantDomain) {
    this.tenantDomain = tenantDomain;
    return this;
  }

  
  @ApiModelProperty(example = "carbon.super", value = "")
  @JsonProperty("tenantDomain")
  public String getTenantDomain() {
    return tenantDomain;
  }
  public void setTenantDomain(String tenantDomain) {
    this.tenantDomain = tenantDomain;
  }

  /**
   **/
  public CustomUrlInfoDTO tenantAdminUsername(String tenantAdminUsername) {
    this.tenantAdminUsername = tenantAdminUsername;
    return this;
  }

  
  @ApiModelProperty(example = "john@foo.com", value = "")
  @JsonProperty("tenantAdminUsername")
  public String getTenantAdminUsername() {
    return tenantAdminUsername;
  }
  public void setTenantAdminUsername(String tenantAdminUsername) {
    this.tenantAdminUsername = tenantAdminUsername;
  }

  /**
   **/
  public CustomUrlInfoDTO enabled(Boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "")
  @JsonProperty("enabled")
  public Boolean isEnabled() {
    return enabled;
  }
  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  /**
   **/
  public CustomUrlInfoDTO devPortal(CustomUrlInfoDevPortalDTO devPortal) {
    this.devPortal = devPortal;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("devPortal")
  public CustomUrlInfoDevPortalDTO getDevPortal() {
    return devPortal;
  }
  public void setDevPortal(CustomUrlInfoDevPortalDTO devPortal) {
    this.devPortal = devPortal;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CustomUrlInfoDTO customUrlInfo = (CustomUrlInfoDTO) o;
    return Objects.equals(tenantDomain, customUrlInfo.tenantDomain) &&
        Objects.equals(tenantAdminUsername, customUrlInfo.tenantAdminUsername) &&
        Objects.equals(enabled, customUrlInfo.enabled) &&
        Objects.equals(devPortal, customUrlInfo.devPortal);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tenantDomain, tenantAdminUsername, enabled, devPortal);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CustomUrlInfoDTO {\n");
    
    sb.append("    tenantDomain: ").append(toIndentedString(tenantDomain)).append("\n");
    sb.append("    tenantAdminUsername: ").append(toIndentedString(tenantAdminUsername)).append("\n");
    sb.append("    enabled: ").append(toIndentedString(enabled)).append("\n");
    sb.append("    devPortal: ").append(toIndentedString(devPortal)).append("\n");
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

