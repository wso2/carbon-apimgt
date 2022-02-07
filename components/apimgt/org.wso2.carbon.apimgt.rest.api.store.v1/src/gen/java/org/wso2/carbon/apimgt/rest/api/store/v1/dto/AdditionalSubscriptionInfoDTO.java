package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AdditionalSubscriptionInfoSolaceDeployedEnvironmentsDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class AdditionalSubscriptionInfoDTO   {
  
    private String subscriptionId = null;
    private String applicationId = null;
    private String applicationName = null;
    private String apiId = null;
    private Boolean isSolaceAPI = null;
    private String solaceOrganization = null;
    private List<AdditionalSubscriptionInfoSolaceDeployedEnvironmentsDTO> solaceDeployedEnvironments = new ArrayList<AdditionalSubscriptionInfoSolaceDeployedEnvironmentsDTO>();

  /**
   * The UUID of the subscription
   **/
  public AdditionalSubscriptionInfoDTO subscriptionId(String subscriptionId) {
    this.subscriptionId = subscriptionId;
    return this;
  }

  
  @ApiModelProperty(example = "faae5fcc-cbae-40c4-bf43-89931630d313", value = "The UUID of the subscription")
  @JsonProperty("subscriptionId")
  public String getSubscriptionId() {
    return subscriptionId;
  }
  public void setSubscriptionId(String subscriptionId) {
    this.subscriptionId = subscriptionId;
  }

  /**
   * The UUID of the application
   **/
  public AdditionalSubscriptionInfoDTO applicationId(String applicationId) {
    this.applicationId = applicationId;
    return this;
  }

  
  @ApiModelProperty(example = "b3ade481-30b0-4b38-9a67-498a40873a6d", value = "The UUID of the application")
  @JsonProperty("applicationId")
  public String getApplicationId() {
    return applicationId;
  }
  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  /**
   * The name of the application
   **/
  public AdditionalSubscriptionInfoDTO applicationName(String applicationName) {
    this.applicationName = applicationName;
    return this;
  }

  
  @ApiModelProperty(example = "Sample Application", value = "The name of the application")
  @JsonProperty("applicationName")
  public String getApplicationName() {
    return applicationName;
  }
  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }

  /**
   * The unique identifier of the API.
   **/
  public AdditionalSubscriptionInfoDTO apiId(String apiId) {
    this.apiId = apiId;
    return this;
  }

  
  @ApiModelProperty(example = "2962f3bb-8330-438e-baee-0ee1d6434ba4", value = "The unique identifier of the API.")
  @JsonProperty("apiId")
  public String getApiId() {
    return apiId;
  }
  public void setApiId(String apiId) {
    this.apiId = apiId;
  }

  /**
   **/
  public AdditionalSubscriptionInfoDTO isSolaceAPI(Boolean isSolaceAPI) {
    this.isSolaceAPI = isSolaceAPI;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "")
  @JsonProperty("isSolaceAPI")
  public Boolean isIsSolaceAPI() {
    return isSolaceAPI;
  }
  public void setIsSolaceAPI(Boolean isSolaceAPI) {
    this.isSolaceAPI = isSolaceAPI;
  }

  /**
   **/
  public AdditionalSubscriptionInfoDTO solaceOrganization(String solaceOrganization) {
    this.solaceOrganization = solaceOrganization;
    return this;
  }

  
  @ApiModelProperty(example = "SolaceWso2", value = "")
  @JsonProperty("solaceOrganization")
  public String getSolaceOrganization() {
    return solaceOrganization;
  }
  public void setSolaceOrganization(String solaceOrganization) {
    this.solaceOrganization = solaceOrganization;
  }

  /**
   **/
  public AdditionalSubscriptionInfoDTO solaceDeployedEnvironments(List<AdditionalSubscriptionInfoSolaceDeployedEnvironmentsDTO> solaceDeployedEnvironments) {
    this.solaceDeployedEnvironments = solaceDeployedEnvironments;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("solaceDeployedEnvironments")
  public List<AdditionalSubscriptionInfoSolaceDeployedEnvironmentsDTO> getSolaceDeployedEnvironments() {
    return solaceDeployedEnvironments;
  }
  public void setSolaceDeployedEnvironments(List<AdditionalSubscriptionInfoSolaceDeployedEnvironmentsDTO> solaceDeployedEnvironments) {
    this.solaceDeployedEnvironments = solaceDeployedEnvironments;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AdditionalSubscriptionInfoDTO additionalSubscriptionInfo = (AdditionalSubscriptionInfoDTO) o;
    return Objects.equals(subscriptionId, additionalSubscriptionInfo.subscriptionId) &&
        Objects.equals(applicationId, additionalSubscriptionInfo.applicationId) &&
        Objects.equals(applicationName, additionalSubscriptionInfo.applicationName) &&
        Objects.equals(apiId, additionalSubscriptionInfo.apiId) &&
        Objects.equals(isSolaceAPI, additionalSubscriptionInfo.isSolaceAPI) &&
        Objects.equals(solaceOrganization, additionalSubscriptionInfo.solaceOrganization) &&
        Objects.equals(solaceDeployedEnvironments, additionalSubscriptionInfo.solaceDeployedEnvironments);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subscriptionId, applicationId, applicationName, apiId, isSolaceAPI, solaceOrganization, solaceDeployedEnvironments);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AdditionalSubscriptionInfoDTO {\n");
    
    sb.append("    subscriptionId: ").append(toIndentedString(subscriptionId)).append("\n");
    sb.append("    applicationId: ").append(toIndentedString(applicationId)).append("\n");
    sb.append("    applicationName: ").append(toIndentedString(applicationName)).append("\n");
    sb.append("    apiId: ").append(toIndentedString(apiId)).append("\n");
    sb.append("    isSolaceAPI: ").append(toIndentedString(isSolaceAPI)).append("\n");
    sb.append("    solaceOrganization: ").append(toIndentedString(solaceOrganization)).append("\n");
    sb.append("    solaceDeployedEnvironments: ").append(toIndentedString(solaceDeployedEnvironments)).append("\n");
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

