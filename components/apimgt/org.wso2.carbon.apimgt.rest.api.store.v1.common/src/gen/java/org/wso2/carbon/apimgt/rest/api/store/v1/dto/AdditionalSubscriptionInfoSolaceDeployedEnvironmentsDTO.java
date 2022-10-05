package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AdditionalSubscriptionInfoSolaceTopicsObjectDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AdditionalSubscriptionInfoSolaceURLsDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class AdditionalSubscriptionInfoSolaceDeployedEnvironmentsDTO   {
  
    private String environmentName = null;
    private String environmentDisplayName = null;
    private String organizationName = null;
    private List<AdditionalSubscriptionInfoSolaceURLsDTO> solaceURLs = new ArrayList<AdditionalSubscriptionInfoSolaceURLsDTO>();
    private AdditionalSubscriptionInfoSolaceTopicsObjectDTO solaceTopicsObject = null;

  /**
   **/
  public AdditionalSubscriptionInfoSolaceDeployedEnvironmentsDTO environmentName(String environmentName) {
    this.environmentName = environmentName;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("environmentName")
  public String getEnvironmentName() {
    return environmentName;
  }
  public void setEnvironmentName(String environmentName) {
    this.environmentName = environmentName;
  }

  /**
   **/
  public AdditionalSubscriptionInfoSolaceDeployedEnvironmentsDTO environmentDisplayName(String environmentDisplayName) {
    this.environmentDisplayName = environmentDisplayName;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("environmentDisplayName")
  public String getEnvironmentDisplayName() {
    return environmentDisplayName;
  }
  public void setEnvironmentDisplayName(String environmentDisplayName) {
    this.environmentDisplayName = environmentDisplayName;
  }

  /**
   **/
  public AdditionalSubscriptionInfoSolaceDeployedEnvironmentsDTO organizationName(String organizationName) {
    this.organizationName = organizationName;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("organizationName")
  public String getOrganizationName() {
    return organizationName;
  }
  public void setOrganizationName(String organizationName) {
    this.organizationName = organizationName;
  }

  /**
   **/
  public AdditionalSubscriptionInfoSolaceDeployedEnvironmentsDTO solaceURLs(List<AdditionalSubscriptionInfoSolaceURLsDTO> solaceURLs) {
    this.solaceURLs = solaceURLs;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("solaceURLs")
  public List<AdditionalSubscriptionInfoSolaceURLsDTO> getSolaceURLs() {
    return solaceURLs;
  }
  public void setSolaceURLs(List<AdditionalSubscriptionInfoSolaceURLsDTO> solaceURLs) {
    this.solaceURLs = solaceURLs;
  }

  /**
   **/
  public AdditionalSubscriptionInfoSolaceDeployedEnvironmentsDTO solaceTopicsObject(AdditionalSubscriptionInfoSolaceTopicsObjectDTO solaceTopicsObject) {
    this.solaceTopicsObject = solaceTopicsObject;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("SolaceTopicsObject")
  public AdditionalSubscriptionInfoSolaceTopicsObjectDTO getSolaceTopicsObject() {
    return solaceTopicsObject;
  }
  public void setSolaceTopicsObject(AdditionalSubscriptionInfoSolaceTopicsObjectDTO solaceTopicsObject) {
    this.solaceTopicsObject = solaceTopicsObject;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AdditionalSubscriptionInfoSolaceDeployedEnvironmentsDTO additionalSubscriptionInfoSolaceDeployedEnvironments = (AdditionalSubscriptionInfoSolaceDeployedEnvironmentsDTO) o;
    return Objects.equals(environmentName, additionalSubscriptionInfoSolaceDeployedEnvironments.environmentName) &&
        Objects.equals(environmentDisplayName, additionalSubscriptionInfoSolaceDeployedEnvironments.environmentDisplayName) &&
        Objects.equals(organizationName, additionalSubscriptionInfoSolaceDeployedEnvironments.organizationName) &&
        Objects.equals(solaceURLs, additionalSubscriptionInfoSolaceDeployedEnvironments.solaceURLs) &&
        Objects.equals(solaceTopicsObject, additionalSubscriptionInfoSolaceDeployedEnvironments.solaceTopicsObject);
  }

  @Override
  public int hashCode() {
    return Objects.hash(environmentName, environmentDisplayName, organizationName, solaceURLs, solaceTopicsObject);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AdditionalSubscriptionInfoSolaceDeployedEnvironmentsDTO {\n");
    
    sb.append("    environmentName: ").append(toIndentedString(environmentName)).append("\n");
    sb.append("    environmentDisplayName: ").append(toIndentedString(environmentDisplayName)).append("\n");
    sb.append("    organizationName: ").append(toIndentedString(organizationName)).append("\n");
    sb.append("    solaceURLs: ").append(toIndentedString(solaceURLs)).append("\n");
    sb.append("    solaceTopicsObject: ").append(toIndentedString(solaceTopicsObject)).append("\n");
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

