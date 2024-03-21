package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ApplicationInfoKeyManagerDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class KeyManagerAppUsagesDTO   {
  
    private Integer applicationCount = null;
    private List<ApplicationInfoKeyManagerDTO> applications = new ArrayList<ApplicationInfoKeyManagerDTO>();

  /**
   * The total count of applications.
   **/
  public KeyManagerAppUsagesDTO applicationCount(Integer applicationCount) {
    this.applicationCount = applicationCount;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The total count of applications.")
  @JsonProperty("applicationCount")
  @NotNull
  public Integer getApplicationCount() {
    return applicationCount;
  }
  public void setApplicationCount(Integer applicationCount) {
    this.applicationCount = applicationCount;
  }

  /**
   **/
  public KeyManagerAppUsagesDTO applications(List<ApplicationInfoKeyManagerDTO> applications) {
    this.applications = applications;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
      @Valid
  @JsonProperty("applications")
  @NotNull
  public List<ApplicationInfoKeyManagerDTO> getApplications() {
    return applications;
  }
  public void setApplications(List<ApplicationInfoKeyManagerDTO> applications) {
    this.applications = applications;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    KeyManagerAppUsagesDTO keyManagerAppUsages = (KeyManagerAppUsagesDTO) o;
    return Objects.equals(applicationCount, keyManagerAppUsages.applicationCount) &&
        Objects.equals(applications, keyManagerAppUsages.applications);
  }

  @Override
  public int hashCode() {
    return Objects.hash(applicationCount, applications);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class KeyManagerAppUsagesDTO {\n");
    
    sb.append("    applicationCount: ").append(toIndentedString(applicationCount)).append("\n");
    sb.append("    applications: ").append(toIndentedString(applications)).append("\n");
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

