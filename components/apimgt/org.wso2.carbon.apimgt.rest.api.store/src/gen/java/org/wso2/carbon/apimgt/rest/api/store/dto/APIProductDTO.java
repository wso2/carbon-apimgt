
/*
 *
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.store.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class APIProductDTO  {

  private String id = null;

  @NotNull
  private String name = null;


  private String version = null;


  private String description = null;


  private String provider = null;

  public enum VisibilityEnum {
    PUBLIC,  PRIVATE,  RESTRICTED,  CONTROLLED,
  }

  private VisibilityEnum visibility = null;


  private List<String> visibleRoles = new ArrayList<String>();
  
  
  private List<String> visibleTenants = new ArrayList<String>();


  private List<String> tags = new ArrayList<String>();


  private String thumbnailUri = null;

  @NotNull
  private List<String> throttlingTier = new ArrayList<String>();


  private String status = null;


  private List<String> apis = new ArrayList<String>();


  private APIBusinessInformationDTO businessInformation = null;

  public enum SubscriptionAvailabilityEnum {
    current_tenant,  all_tenants,  specific_tenants,
  }

  private SubscriptionAvailabilityEnum subscriptionAvailability = null;


  private List<String> subscriptionAvailableTenants = new ArrayList<String>();


  /**
   **/
  @ApiModelProperty(value = "")
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
  @ApiModelProperty(value = "")
  @JsonProperty("version")
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }


  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }


  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("provider")
  public String getProvider() {
    return provider;
  }
  public void setProvider(String provider) {
    this.provider = provider;
  }


  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("visibility")
  public VisibilityEnum getVisibility() {
    return visibility;
  }
  public void setVisibility(VisibilityEnum visibility) {
    this.visibility = visibility;
  }


  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("visibleRoles")
  public List<String> getVisibleRoles() {
    return visibleRoles;
  }
  public void setVisibleRoles(List<String> visibleRoles) {
    this.visibleRoles = visibleRoles;
  }


  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("visibleTenants")
  public List<String> getVisibleTenants() {
    return visibleTenants;
  }
  public void setVisibleTenants(List<String> visibleTenants) {
    this.visibleTenants = visibleTenants;
  }


  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("tags")
  public List<String> getTags() {
    return tags;
  }
  public void setTags(List<String> tags) {
    this.tags = tags;
  }


  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("thumbnailUri")
  public String getThumbnailUri() {
    return thumbnailUri;
  }
  public void setThumbnailUri(String thumbnailUri) {
    this.thumbnailUri = thumbnailUri;
  }


  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("throttlingTier")
  public List<String> getThrottlingTier() {
    return throttlingTier;
  }
  public void setThrottlingTier(List<String> throttlingTier) {
    this.throttlingTier = throttlingTier;
  }


  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("status")
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }


  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("apis")
  public List<String> getApis() {
    return apis;
  }
  public void setApis(List<String> apis) {
    this.apis = apis;
  }


  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("businessInformation")
  public APIBusinessInformationDTO getBusinessInformation() {
    return businessInformation;
  }
  public void setBusinessInformation(APIBusinessInformationDTO businessInformation) {
    this.businessInformation = businessInformation;
  }


  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("subscriptionAvailability")
  public SubscriptionAvailabilityEnum getSubscriptionAvailability() {
    return subscriptionAvailability;
  }
  public void setSubscriptionAvailability(SubscriptionAvailabilityEnum subscriptionAvailability) {
    this.subscriptionAvailability = subscriptionAvailability;
  }


  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("subscriptionAvailableTenants")
  public List<String> getSubscriptionAvailableTenants() {
    return subscriptionAvailableTenants;
  }
  public void setSubscriptionAvailableTenants(List<String> subscriptionAvailableTenants) {
    this.subscriptionAvailableTenants = subscriptionAvailableTenants;
  }



  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIProductDTO {\n");

    sb.append("  id: ").append(id).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("  version: ").append(version).append("\n");
    sb.append("  description: ").append(description).append("\n");
    sb.append("  provider: ").append(provider).append("\n");
    sb.append("  visibility: ").append(visibility).append("\n");
    sb.append("  visibleRoles: ").append(visibleRoles).append("\n");
    sb.append("  visibleTenants: ").append(visibleTenants).append("\n");
    sb.append("  tags: ").append(tags).append("\n");
    sb.append("  thumbnailUri: ").append(thumbnailUri).append("\n");
    sb.append("  throttlingTier: ").append(throttlingTier).append("\n");
    sb.append("  status: ").append(status).append("\n");
    sb.append("  apis: ").append(apis).append("\n");
    sb.append("  businessInformation: ").append(businessInformation).append("\n");
    sb.append("  subscriptionAvailability: ").append(subscriptionAvailability).append("\n");
    sb.append("  subscriptionAvailableTenants: ").append(subscriptionAvailableTenants).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
