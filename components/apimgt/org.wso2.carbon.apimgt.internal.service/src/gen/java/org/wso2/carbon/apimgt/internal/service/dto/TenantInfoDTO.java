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



public class TenantInfoDTO   {
  
    private Integer tenantId = null;
    private String admin = null;
    private String domain = null;
    private String adminFullName = null;
    private String adminFirstName = null;
    private String adminLastName = null;
    private String email = null;
    private Boolean active = null;

  /**
   **/
  public TenantInfoDTO tenantId(Integer tenantId) {
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
  public TenantInfoDTO admin(String admin) {
    this.admin = admin;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("admin")
  public String getAdmin() {
    return admin;
  }
  public void setAdmin(String admin) {
    this.admin = admin;
  }

  /**
   **/
  public TenantInfoDTO domain(String domain) {
    this.domain = domain;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("domain")
  public String getDomain() {
    return domain;
  }
  public void setDomain(String domain) {
    this.domain = domain;
  }

  /**
   **/
  public TenantInfoDTO adminFullName(String adminFullName) {
    this.adminFullName = adminFullName;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("adminFullName")
  public String getAdminFullName() {
    return adminFullName;
  }
  public void setAdminFullName(String adminFullName) {
    this.adminFullName = adminFullName;
  }

  /**
   **/
  public TenantInfoDTO adminFirstName(String adminFirstName) {
    this.adminFirstName = adminFirstName;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("adminFirstName")
  public String getAdminFirstName() {
    return adminFirstName;
  }
  public void setAdminFirstName(String adminFirstName) {
    this.adminFirstName = adminFirstName;
  }

  /**
   **/
  public TenantInfoDTO adminLastName(String adminLastName) {
    this.adminLastName = adminLastName;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("adminLastName")
  public String getAdminLastName() {
    return adminLastName;
  }
  public void setAdminLastName(String adminLastName) {
    this.adminLastName = adminLastName;
  }

  /**
   **/
  public TenantInfoDTO email(String email) {
    this.email = email;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("email")
  public String getEmail() {
    return email;
  }
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   **/
  public TenantInfoDTO active(Boolean active) {
    this.active = active;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("active")
  public Boolean isActive() {
    return active;
  }
  public void setActive(Boolean active) {
    this.active = active;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TenantInfoDTO tenantInfo = (TenantInfoDTO) o;
    return Objects.equals(tenantId, tenantInfo.tenantId) &&
        Objects.equals(admin, tenantInfo.admin) &&
        Objects.equals(domain, tenantInfo.domain) &&
        Objects.equals(adminFullName, tenantInfo.adminFullName) &&
        Objects.equals(adminFirstName, tenantInfo.adminFirstName) &&
        Objects.equals(adminLastName, tenantInfo.adminLastName) &&
        Objects.equals(email, tenantInfo.email) &&
        Objects.equals(active, tenantInfo.active);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tenantId, admin, domain, adminFullName, adminFirstName, adminLastName, email, active);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TenantInfoDTO {\n");
    
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
    sb.append("    admin: ").append(toIndentedString(admin)).append("\n");
    sb.append("    domain: ").append(toIndentedString(domain)).append("\n");
    sb.append("    adminFullName: ").append(toIndentedString(adminFullName)).append("\n");
    sb.append("    adminFirstName: ").append(toIndentedString(adminFirstName)).append("\n");
    sb.append("    adminLastName: ").append(toIndentedString(adminLastName)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    active: ").append(toIndentedString(active)).append("\n");
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

