package org.wso2.carbon.apimgt.rest.api.store.dto;

<<<<<<< HEAD
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIBusinessInformationDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIEndpointURLsDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;
=======

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.dto.API_businessInformationDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.BaseAPIDTO;
import java.util.Objects;
>>>>>>> upstream/master

/**
 * APIDTO
 */
public class APIDTO extends BaseAPIDTO  {
  @JsonProperty("lifeCycleStatus")
  private String lifeCycleStatus = null;

  @JsonProperty("isDefaultVersion")
  private Boolean isDefaultVersion = null;

  @JsonProperty("tags")
  private List<String> tags = new ArrayList<String>();

<<<<<<< HEAD
  private String lastUpdatedTime = null;
=======
  @JsonProperty("policies")
  private List<String> policies = new ArrayList<String>();

  @JsonProperty("wsdlUri")
  private String wsdlUri = null;
>>>>>>> upstream/master

  @JsonProperty("businessInformation")
  private API_businessInformationDTO businessInformation = null;

  public APIDTO lifeCycleStatus(String lifeCycleStatus) {
    this.lifeCycleStatus = lifeCycleStatus;
    return this;
  }

   /**
   * Get lifeCycleStatus
   * @return lifeCycleStatus
  **/
<<<<<<< HEAD

  @JsonIgnore
  public String getCreatedTime(){
    return createdTime;
  }
  public void setCreatedTime(String createdTime){
    this.createdTime=createdTime;
  }

  
  /**
   * UUID of the api registry artifact\n
   **/
  @ApiModelProperty(value = "UUID of the api registry artifact\n")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  
  /**
   **/
=======
>>>>>>> upstream/master
  @ApiModelProperty(value = "")
  public String getLifeCycleStatus() {
    return lifeCycleStatus;
  }

<<<<<<< HEAD
  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("context")
  public String getContext() {
    return context;
  }
  public void setContext(String context) {
    this.context = context;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("version")
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  
  /**
   * If the provider value is not given user invoking the api will be used as the provider.\n
   **/
  @ApiModelProperty(required = true, value = "If the provider value is not given user invoking the api will be used as the provider.\n")
  @JsonProperty("provider")
  public String getProvider() {
    return provider;
  }
  public void setProvider(String provider) {
    this.provider = provider;
  }

  
  /**
   * Swagger definition of the API which contains details about URI templates and scopes\n
   **/
  @ApiModelProperty(required = true, value = "Swagger definition of the API which contains details about URI templates and scopes\n")
  @JsonProperty("apiDefinition")
  public String getApiDefinition() {
    return apiDefinition;
  }
  public void setApiDefinition(String apiDefinition) {
    this.apiDefinition = apiDefinition;
  }

  
  /**
   * WSDL URL if the API is based on a WSDL endpoint\n
   **/
  @ApiModelProperty(value = "WSDL URL if the API is based on a WSDL endpoint\n")
  @JsonProperty("wsdlUri")
  public String getWsdlUri() {
    return wsdlUri;
  }
  public void setWsdlUri(String wsdlUri) {
    this.wsdlUri = wsdlUri;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("status")
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }

  
  /**
   **/
=======
  public void setLifeCycleStatus(String lifeCycleStatus) {
    this.lifeCycleStatus = lifeCycleStatus;
  }

  public APIDTO isDefaultVersion(Boolean isDefaultVersion) {
    this.isDefaultVersion = isDefaultVersion;
    return this;
  }

   /**
   * Get isDefaultVersion
   * @return isDefaultVersion
  **/
>>>>>>> upstream/master
  @ApiModelProperty(value = "")
  public Boolean getIsDefaultVersion() {
    return isDefaultVersion;
  }

  public void setIsDefaultVersion(Boolean isDefaultVersion) {
    this.isDefaultVersion = isDefaultVersion;
  }

<<<<<<< HEAD
  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("transport")
  public List<String> getTransport() {
    return transport;
=======
  public APIDTO tags(List<String> tags) {
    this.tags = tags;
    return this;
>>>>>>> upstream/master
  }

  public APIDTO addTagsItem(String tagsItem) {
    this.tags.add(tagsItem);
    return this;
  }

<<<<<<< HEAD
  
  /**
   **/
=======
   /**
   * Get tags
   * @return tags
  **/
>>>>>>> upstream/master
  @ApiModelProperty(value = "")
  public List<String> getTags() {
    return tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

<<<<<<< HEAD
  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("tiers")
  public List<String> getTiers() {
    return tiers;
=======
  public APIDTO policies(List<String> policies) {
    this.policies = policies;
    return this;
>>>>>>> upstream/master
  }

  public APIDTO addPoliciesItem(String policiesItem) {
    this.policies.add(policiesItem);
    return this;
  }

<<<<<<< HEAD
  
  /**
   **/
=======
   /**
   * Get policies
   * @return policies
  **/
>>>>>>> upstream/master
  @ApiModelProperty(value = "")
  public List<String> getPolicies() {
    return policies;
  }

  public void setPolicies(List<String> policies) {
    this.policies = policies;
  }

<<<<<<< HEAD
  
  /**
   **/
=======
  public APIDTO wsdlUri(String wsdlUri) {
    this.wsdlUri = wsdlUri;
    return this;
  }

   /**
   * Get wsdlUri
   * @return wsdlUri
  **/
>>>>>>> upstream/master
  @ApiModelProperty(value = "")
  public String getWsdlUri() {
    return wsdlUri;
  }

  public void setWsdlUri(String wsdlUri) {
    this.wsdlUri = wsdlUri;
  }

<<<<<<< HEAD
  
  /**
   **/
=======
  public APIDTO businessInformation(API_businessInformationDTO businessInformation) {
    this.businessInformation = businessInformation;
    return this;
  }

   /**
   * Get businessInformation
   * @return businessInformation
  **/
>>>>>>> upstream/master
  @ApiModelProperty(value = "")
  public API_businessInformationDTO getBusinessInformation() {
    return businessInformation;
  }

  public void setBusinessInformation(API_businessInformationDTO businessInformation) {
    this.businessInformation = businessInformation;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIDTO API = (APIDTO) o;
    return Objects.equals(this.lifeCycleStatus, API.lifeCycleStatus) &&
        Objects.equals(this.isDefaultVersion, API.isDefaultVersion) &&
        Objects.equals(this.tags, API.tags) &&
        Objects.equals(this.policies, API.policies) &&
        Objects.equals(this.wsdlUri, API.wsdlUri) &&
        Objects.equals(this.businessInformation, API.businessInformation) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lifeCycleStatus, isDefaultVersion, tags, policies, wsdlUri, businessInformation, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIDTO {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    lifeCycleStatus: ").append(toIndentedString(lifeCycleStatus)).append("\n");
    sb.append("    isDefaultVersion: ").append(toIndentedString(isDefaultVersion)).append("\n");
    sb.append("    tags: ").append(toIndentedString(tags)).append("\n");
    sb.append("    policies: ").append(toIndentedString(policies)).append("\n");
    sb.append("    wsdlUri: ").append(toIndentedString(wsdlUri)).append("\n");
    sb.append("    businessInformation: ").append(toIndentedString(businessInformation)).append("\n");
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

