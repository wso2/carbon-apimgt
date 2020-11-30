/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.persistence.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
  Represents the API information stored in persistence layer, that is used for DevPortal operations
 */
public class DevPortalAPI extends DevPortalAPIInfo {
    private String status; // needs when decide whether to allow return api or not.
    private Boolean isDefaultVersion;
    private String description;
    private String wsdlUrl;
    private String technicalOwner;
    private String technicalOwnerEmail;
    private String businessOwner;
    private String businessOwnerEmail;
    private String transports;
    private String redirectURL;  // (originalStoreUrl)
    private String apiOwner;
    private boolean advertiseOnly;
    private String subscriptionAvailability; // need to decide isSubscriptionAvailable
    private String subscriptionAvailableOrgs; // (subscriptionAvailableTenants): need to decide the value of "isSubscriptionAvailable"
    private String authorizationHeader;
    private List<String> securityScheme = new ArrayList<>();
    private Set<String> availableTierNames;
    private Set<String> environments;
    private Set<String> gatewayLabels;
    private Set<String> apiCategories;
    private boolean isMonetizationEnabled; //(monetizationStatus)
    private List<String> keyManagers = new ArrayList<>();
    private Set<DeploymentEnvironments> deploymentEnvironments; // returned in apiGet call as ingressURLs
    private List<String> tags = new ArrayList<>();
    private Map<String, String> additionalProperties;
    private String endpointConfig;
    private String type;
    private Boolean advertisedOnly;
    private String swaggerDefinition;
    private String contextTemplate;

    public String getContextTemplate() {
        return contextTemplate;
    }

    public void setContextTemplate(String contextTemplate) {
        this.contextTemplate = contextTemplate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getDefaultVersion() {
        return isDefaultVersion;
    }

    public void setDefaultVersion(Boolean defaultVersion) {
        isDefaultVersion = defaultVersion;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWsdlUrl() {
        return wsdlUrl;
    }

    public void setWsdlUrl(String wsdlUrl) {
        this.wsdlUrl = wsdlUrl;
    }

    public String getTechnicalOwner() {
        return technicalOwner;
    }

    public void setTechnicalOwner(String technicalOwner) {
        this.technicalOwner = technicalOwner;
    }

    public String getTechnicalOwnerEmail() {
        return technicalOwnerEmail;
    }

    public void setTechnicalOwnerEmail(String technicalOwnerEmail) {
        this.technicalOwnerEmail = technicalOwnerEmail;
    }

    public String getBusinessOwner() {
        return businessOwner;
    }

    public void setBusinessOwner(String businessOwner) {
        this.businessOwner = businessOwner;
    }

    public String getBusinessOwnerEmail() {
        return businessOwnerEmail;
    }

    public void setBusinessOwnerEmail(String businessOwnerEmail) {
        this.businessOwnerEmail = businessOwnerEmail;
    }

    public Boolean getIsDefaultVersion() {
        return isDefaultVersion;
    }

    public void setIsDefaultVersion(Boolean isDefaultVersion) {
        this.isDefaultVersion = isDefaultVersion;
    }

    public String getTransports() {
        return transports;
    }

    public void setTransports(String transport) {
        this.transports = transport;
    }

    public String getRedirectURL() {
        return redirectURL;
    }

    public void setRedirectURL(String redirectURL) {
        this.redirectURL = redirectURL;
    }

    public String getApiOwner() {
        return apiOwner;
    }

    public void setApiOwner(String apiOwner) {
        this.apiOwner = apiOwner;
    }

    public boolean isAdvertiseOnly() {
        return advertiseOnly;
    }

    public void setAdvertiseOnly(boolean advertiseOnly) {
        this.advertiseOnly = advertiseOnly;
    }

    public String getSubscriptionAvailability() {
        return subscriptionAvailability;
    }

    public void setSubscriptionAvailability(String subscriptionAvailability) {
        this.subscriptionAvailability = subscriptionAvailability;
    }

    public String getSubscriptionAvailableOrgs() {
        return subscriptionAvailableOrgs;
    }

    public void setSubscriptionAvailableOrgs(String subscriptionAvailableOrgs) {
        this.subscriptionAvailableOrgs = subscriptionAvailableOrgs;
    }

    public String getAuthorizationHeader() {
        return authorizationHeader;
    }

    public void setAuthorizationHeader(String authorizationHeader) {
        this.authorizationHeader = authorizationHeader;
    }

    public List<String> getSecurityScheme() {
        return securityScheme;
    }

    public void setSecurityScheme(List<String> securityScheme) {
        this.securityScheme = securityScheme;
    }

    public Set<String> getAvailableTierNames() {
        return availableTierNames;
    }

    public void setAvailableTierNames(Set<String> availableTierNames) {
        this.availableTierNames = availableTierNames;
    }

    public Set<String> getEnvironments() {
        return environments;
    }

    public void setEnvironments(Set<String> environments) {
        this.environments = environments;
    }

    public Set<String> getGatewayLabels() {
        return gatewayLabels;
    }

    public void setGatewayLabels(Set<String> gatewayLabels) {
        this.gatewayLabels = gatewayLabels;
    }

    public Set<String> getApiCategories() {
        return apiCategories;
    }

    public void setApiCategories(Set<String> apiCategories) {
        this.apiCategories = apiCategories;
    }

    public boolean isMonetizationEnabled() {
        return isMonetizationEnabled;
    }

    public void setMonetizationEnabled(boolean monetizationEnabled) {
        isMonetizationEnabled = monetizationEnabled;
    }

    public List<String> getKeyManagers() {
        return keyManagers;
    }

    public void setKeyManagers(List<String> keyManagers) {
        this.keyManagers = keyManagers;
    }

    public Set<DeploymentEnvironments> getDeploymentEnvironments() {
        return deploymentEnvironments;
    }

    public void setDeploymentEnvironments(
            Set<DeploymentEnvironments> deploymentEnvironments) {
        this.deploymentEnvironments = deploymentEnvironments;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Map<String, String> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String, String> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public String getEndpointConfig() {
        return endpointConfig;
    }

    public void setEndpointConfig(String endpointConfig) {
        this.endpointConfig = endpointConfig;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getAdvertisedOnly() {
        return advertisedOnly;
    }

    public void setAdvertisedOnly(Boolean advertisedOnly) {
        this.advertisedOnly = advertisedOnly;
    }

    public String getSwaggerDefinition() {
        return swaggerDefinition;
    }

    public void setSwaggerDefinition(String swaggerDefinition) {
        this.swaggerDefinition = swaggerDefinition;
    }

    @Override
    public String toString() {
        return "DevPortalAPI [status=" + status + ", isDefaultVersion=" + isDefaultVersion + ", description="
                + description + ", wsdlUrl=" + wsdlUrl + ", technicalOwner=" + technicalOwner + ", technicalOwnerEmail="
                + technicalOwnerEmail + ", businessOwner=" + businessOwner + ", businessOwnerEmail="
                + businessOwnerEmail + ", transports=" + transports + ", redirectURL=" + redirectURL + ", apiOwner="
                + apiOwner + ", advertiseOnly=" + advertiseOnly + ", subscriptionAvailability="
                + subscriptionAvailability + ", subscriptionAvailableOrgs=" + subscriptionAvailableOrgs
                + ", authorizationHeader=" + authorizationHeader + ", securityScheme=" + securityScheme
                + ", availableTierNames=" + availableTierNames + ", environments=" + environments + ", gatewayLabels="
                + gatewayLabels + ", apiCategories=" + apiCategories + ", isMonetizationEnabled="
                + isMonetizationEnabled + ", keyManagers=" + keyManagers + ", deploymentEnvironments="
                + deploymentEnvironments + ", tags=" + tags + ", additionalProperties=" + additionalProperties
                + ", endpointConfig=" + endpointConfig + ", type=" + type + ", advertisedOnly=" + advertisedOnly
                + ", swaggerDefinition=" + swaggerDefinition + ", toString()=" + super.toString() + "]";
    }
    

    /*
    private String accessControl; //publisher accessControl : 'restricted', 'all' // this won't be required

     */

    /* private String apiDefinition; currently this is also returned in apiGet call. But this is not required. In
    store, when we go into an api, separate swagger get call is sent after the normal api get call. */

    /* private List<APIOperationsDTO> operations = new ArrayList<>(); NOT needed; this set for graphql apis only
     and that is resolved using uritemplates fetched from db */


    /* private Boolean isSubscriptionAvailable;  "is subscription available" for the current tenant is returned.
    this is resolved by "subscription Availability" (subscriptionAvailability: current_tenant)  property which store
    the tenants avaialbe for subscriptions
     This should not return all the other tenants available for subscrition in the store. So this property should not be
      be returned in api get response*/

    /* private Set<String> accessControlRoles; // dev portal doesn't need this */
}
