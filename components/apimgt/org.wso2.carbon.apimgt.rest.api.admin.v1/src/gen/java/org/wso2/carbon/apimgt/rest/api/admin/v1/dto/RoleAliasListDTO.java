package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.RoleAliasDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class RoleAliasListDTO   {
  
    private Integer count = null;
    private List<RoleAliasDTO> alerts = new ArrayList<>();

  /**
   * The number of role aliases
   **/
  public RoleAliasListDTO count(Integer count) {
    this.count = count;
    return this;
  }

  
  @ApiModelProperty(example = "3", value = "The number of role aliases")
  @JsonProperty("count")
  public Integer getCount() {
    return count;
  }
  public void setCount(Integer count) {
    this.count = count;
  }

  /**
   **/
  public RoleAliasListDTO alerts(List<RoleAliasDTO> alerts) {
    this.alerts = alerts;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("alerts")
  public List<RoleAliasDTO> getAlerts() {
    return alerts;
  }
  public void setAlerts(List<RoleAliasDTO> alerts) {
    this.alerts = alerts;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RoleAliasListDTO roleAliasList = (RoleAliasListDTO) o;
    return Objects.equals(count, roleAliasList.count) &&
        Objects.equals(alerts, roleAliasList.alerts);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, alerts);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RoleAliasListDTO {\n");
    
    sb.append("    count: ").append(toIndentedString(count)).append("\n");
    sb.append("    alerts: ").append(toIndentedString(alerts)).append("\n");
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

