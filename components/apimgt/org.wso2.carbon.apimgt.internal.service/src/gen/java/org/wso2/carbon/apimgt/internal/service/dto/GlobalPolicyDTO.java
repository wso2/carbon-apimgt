package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.wso2.carbon.apimgt.internal.service.dto.PolicyDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class GlobalPolicyDTO   {
  
    private Integer id = null;
    private Integer tenantId = null;
    private String tenantDomain = null;
    private String name = null;
    private String quotaType = null;
    private String siddhiQuery = null;
    private String keyTemplate = null;

  /**
   **/
  public GlobalPolicyDTO id(Integer id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("id")
  public Integer getId() {
    return id;
  }
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   **/
  public GlobalPolicyDTO tenantId(Integer tenantId) {
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
  public GlobalPolicyDTO tenantDomain(String tenantDomain) {
    this.tenantDomain = tenantDomain;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("tenantDomain")
  public String getTenantDomain() {
    return tenantDomain;
  }
  public void setTenantDomain(String tenantDomain) {
    this.tenantDomain = tenantDomain;
  }

  /**
   **/
  public GlobalPolicyDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public GlobalPolicyDTO quotaType(String quotaType) {
    this.quotaType = quotaType;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("quotaType")
  public String getQuotaType() {
    return quotaType;
  }
  public void setQuotaType(String quotaType) {
    this.quotaType = quotaType;
  }

  /**
   **/
  public GlobalPolicyDTO siddhiQuery(String siddhiQuery) {
    this.siddhiQuery = siddhiQuery;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("siddhiQuery")
  public String getSiddhiQuery() {
    return siddhiQuery;
  }
  public void setSiddhiQuery(String siddhiQuery) {
    this.siddhiQuery = siddhiQuery;
  }

  /**
   **/
  public GlobalPolicyDTO keyTemplate(String keyTemplate) {
    this.keyTemplate = keyTemplate;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("keyTemplate")
  public String getKeyTemplate() {
    return keyTemplate;
  }
  public void setKeyTemplate(String keyTemplate) {
    this.keyTemplate = keyTemplate;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GlobalPolicyDTO globalPolicy = (GlobalPolicyDTO) o;
    return Objects.equals(id, globalPolicy.id) &&
        Objects.equals(tenantId, globalPolicy.tenantId) &&
        Objects.equals(tenantDomain, globalPolicy.tenantDomain) &&
        Objects.equals(name, globalPolicy.name) &&
        Objects.equals(quotaType, globalPolicy.quotaType) &&
        Objects.equals(siddhiQuery, globalPolicy.siddhiQuery) &&
        Objects.equals(keyTemplate, globalPolicy.keyTemplate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, tenantId, tenantDomain, name, quotaType, siddhiQuery, keyTemplate);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GlobalPolicyDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
    sb.append("    tenantDomain: ").append(toIndentedString(tenantDomain)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    quotaType: ").append(toIndentedString(quotaType)).append("\n");
    sb.append("    siddhiQuery: ").append(toIndentedString(siddhiQuery)).append("\n");
    sb.append("    keyTemplate: ").append(toIndentedString(keyTemplate)).append("\n");
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

