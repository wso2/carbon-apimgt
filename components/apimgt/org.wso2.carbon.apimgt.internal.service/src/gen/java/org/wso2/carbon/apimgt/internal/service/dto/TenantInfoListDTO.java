package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.internal.service.dto.TenantInfoDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class TenantInfoListDTO   {
  
    private List<TenantInfoDTO> tenants = new ArrayList<>();

  /**
   **/
  public TenantInfoListDTO tenants(List<TenantInfoDTO> tenants) {
    this.tenants = tenants;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("tenants")
  public List<TenantInfoDTO> getTenants() {
    return tenants;
  }
  public void setTenants(List<TenantInfoDTO> tenants) {
    this.tenants = tenants;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TenantInfoListDTO tenantInfoList = (TenantInfoListDTO) o;
    return Objects.equals(tenants, tenantInfoList.tenants);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tenants);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TenantInfoListDTO {\n");
    
    sb.append("    tenants: ").append(toIndentedString(tenants)).append("\n");
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

