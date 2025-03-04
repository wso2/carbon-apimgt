/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
 */

package org.wso2.carbon.apimgt.solace.api.v2;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import feign.Feign;
import feign.FeignException;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.slf4j.Slf4jLogger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.SolaceConfig;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.kmclient.ApacheFeignHttpClient;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.solace.api.exception.SolaceApiClientException;
import org.wso2.carbon.apimgt.solace.api.v2.model.AppRegistration;
import org.wso2.carbon.apimgt.solace.api.v2.model.SolaceEventApiProductsResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contains methods to interact with Solace V2 APIs.
 */
public class SolaceV2Apis {

    private static final Log log = LogFactory.getLog(SolaceV2Apis.class);
    private SolaceV2ApimApisClient solaceV2ApimApisClient;

    public SolaceV2Apis() {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        SolaceConfig solaceConfig = config.getSolaceConfig();
        if (solaceConfig != null && solaceConfig.isEnabled()) {
            String solaceApimApiEndpoint = solaceConfig.getSolaceApimApiEndpoint();
            String token = solaceConfig.getSolaceToken();
            try {
                solaceV2ApimApisClient = Feign.builder()
                        .client(new ApacheFeignHttpClient(APIUtil.getHttpClient(solaceApimApiEndpoint)))
                        .encoder(new GsonEncoder())
                        .decoder(new GsonDecoder())
                        .logger(new Slf4jLogger())
                        .requestInterceptor(template -> template.header("Authorization", "Bearer " + token))
                        .target(SolaceV2ApimApisClient.class, solaceApimApiEndpoint);
            } catch (APIManagementException e) {
                log.error("Error while creating Solace APIM API client", e);
            }
        }
    }

    /**
     * Gets a list of Solace Event API Products - that contain only one Solace Event API.
     * A Solace Event API will be represented as an API in WSO2 APIM. In order to access a Solace Event API, we need a
     * Solace Plan, which is associated via a Solace Event API Product.
     * Therefore, in order to access a Solace Event API, we consider Solace Event API Products that contain only one
     * Solace Event API.
     * @return                          List of Solace Event API Products.
     * @throws APIManagementException   If an error occurs while getting event API products.
     */
    public SolaceEventApiProductsResponse getEventApiProducts() throws APIManagementException {
        try {
            SolaceEventApiProductsResponse eventApiProducts = solaceV2ApimApisClient.getEventApiProducts();
            List<SolaceEventApiProductsResponse.EventApiProduct> eventApiProductsWithSingleEventApi = new ArrayList<>();
            for (SolaceEventApiProductsResponse.EventApiProduct eventApiProduct : eventApiProducts.getData()) {
                // We only care about Solace Event API Products - that contain only one Solace Event API
                if (eventApiProduct.getApis().size() == 1) {
                    eventApiProductsWithSingleEventApi.add(eventApiProduct);
                }
            }
            SolaceEventApiProductsResponse filteredEventApiProducts = new SolaceEventApiProductsResponse();
            filteredEventApiProducts.setData(eventApiProductsWithSingleEventApi);
            return filteredEventApiProducts;
        } catch (SolaceApiClientException e) {
            throw new APIManagementException("Error while getting event API products", e);
        }
    }

    /**
     * Gets the async API definition of a Solace Event API, associated with the given Solace Event API Product
     * and the Plan. A Solace Event API Product has at least one plan, so using the first plan from the
     * Solace Event API Product is sufficient - since the API call mandates a plan ID, although we haven't chosen a
     * Plan at the time of retrieving the async API definition.
     *
     * @param eventApiProductId         ID of the Solace Event API Product, which contains the Solace Event API.
     * @param planId                    ID of the plan, associated with the Solace Event API Product and the
     *                                  Solace Event API.
     * @param eventApiId                ID of the Solace Event API.
     * @return                          Async API definition of the Solace Event API.
     * @throws APIManagementException   If an error occurs while getting event API async API definition.
     */
    public JsonObject getEventApiAsyncApiDefinition(String eventApiProductId, String planId, String eventApiId)
            throws APIManagementException {
        try {
            return solaceV2ApimApisClient.getEventApiAsyncApiDefinition(
                    eventApiProductId, planId, eventApiId, Collections.singletonMap("asyncApiVersion", "2.2.0"));
        } catch (SolaceApiClientException e) {
            throw new APIManagementException("Error while getting event API async API definition", e);
        }
    }

    /**
     * Returns the ID of the Solace plan that has the provided name.
     * We have to 'mirror' a Solace plan by creating a WSO2 APIM plan with the same name. The WSO2 APIM plan is then
     * mapped to a Solace Plan by matching the name.
     * @param eventApiProductId         ID of the Solace Event API Product, which contains the Solace plan.
     * @param planName                  Name of the WSO2 plan.
     * @return                          ID of the Solace plan.
     * @throws APIManagementException   If an error occurs while getting event API product plans.
     */
    public String getEventApiProductPlanId(String eventApiProductId, String planName)
            throws APIManagementException {
        try {
            JsonObject response = solaceV2ApimApisClient.getEventApiProductPlans(eventApiProductId);
            for (JsonElement planElement : response.getAsJsonArray("data")) {
                JsonObject planObject = planElement.getAsJsonObject();
                if (planName.equals(planObject.get("name").getAsString())) {
                    return planObject.get("id").getAsString();
                }
            }
        } catch (SolaceApiClientException e) {
            throw new APIManagementException("Error while getting event API product plans", e);
        }
        return null;
    }

    /**
     * Creates a Solace App Registration.
     * A Solace App Registration represents a WSO2 APIM DevPortal application, and this is used to subscribe to a
     * Solace Event API Product.
     * @param appRegistrationId         ID of the Solace App Registration.
     * @param appSource                 Source of the Solace App Registration.
     * @param appName                   Name of the Solace App Registration.
     * @param appSourceOwner            Owner of the source of the Solace App Registration.
     * @param applicationDomainId       ID of the Solace Application Domain.
     * @return                          Created Solace App Registration.
     * @throws APIManagementException   If an error occurs while creating app registration.
     */
    public AppRegistration createAppRegistration(String appRegistrationId, String appSource, String appName,
                                                 String appSourceOwner, String applicationDomainId)
            throws APIManagementException {
        AppRegistration appRegistration = new AppRegistration();
        appRegistration.setRegistrationId(appRegistrationId);
        appRegistration.setSource(appSource);
        appRegistration.setName(appName);
        appRegistration.setSourceOwner(appSourceOwner);
        appRegistration.setApplicationDomainId(applicationDomainId);
        try {
            JsonObject createdAppRegistrationResponse = solaceV2ApimApisClient.createAppRegistration(appRegistration);
            JsonObject data = createdAppRegistrationResponse.getAsJsonObject("data");
            return new Gson().fromJson(data, AppRegistration.class);
        } catch (SolaceApiClientException e) {
            throw new APIManagementException("Error while creating app registration", e);
        }
    }

    /**
     * Checks if a Solace App Registration denoted by the given ID exists.
     * @param appRegistrationId         ID of the Solace App Registration.
     * @return                          True if the app registration exists, false otherwise.
     * @throws APIManagementException   If an error occurs while checking if app registration exists.
     */
    public boolean isAppRegistrationExists(String appRegistrationId) throws APIManagementException {
        try {
            JsonObject response = solaceV2ApimApisClient.getAppRegistration(appRegistrationId);
            if (response.has("data")) {
                return true;
            }
        } catch (FeignException e) {
            if (404 == e.status()) {
                return false;
            }
        } catch (SolaceApiClientException e) {
            throw new APIManagementException("Error while checking if app registration exists with id: " +
                    appRegistrationId, e);
        }
        return false;
    }

    /**
     * Deletes a Solace App Registration.
     * @param registrationId            ID of the Solace App Registration.
     * @throws APIManagementException   If an error occurs while deleting app registration.
     */
    public void deleteAppRegistration(String registrationId) throws APIManagementException {
        try {
            solaceV2ApimApisClient.deleteAppRegistration(registrationId);
        } catch (SolaceApiClientException e) {
            throw new APIManagementException("Error while deleting app registration", e);
        }
    }

    /**
     * Adds the provided WSO2 APIM DevPortal application's consumer key and consumer secret
     * as credentials to a Solace App Registration, so that the tokens generated from these keys
     * can be used to access services referenced in the Solace Event API.
     * @param appRegistrationId         ID of the Solace App Registration.
     * @param consumerKey               Consumer key of the credentials.
     * @param consumerSecret            Consumer secret of the credentials.
     * @throws APIManagementException   If an error occurs while creating credentials.
     */
    public void createCredentials(String appRegistrationId, String consumerKey, String consumerSecret)
            throws APIManagementException {
        AppRegistration.Credentials.Secret secret = new AppRegistration.Credentials.Secret();
        secret.setConsumerKey(consumerKey);
        secret.setConsumerSecret(consumerSecret);
        JsonObject credentialsPayload = new JsonObject();
        credentialsPayload.add("secret", new Gson().toJsonTree(secret));
        try {
            solaceV2ApimApisClient.createCredentials(appRegistrationId, credentialsPayload);
        } catch (SolaceApiClientException e) {
            throw new APIManagementException("Error while creating credentials", e);
        }
    }


    /**
     * Creates an access request in a Solace App Registration, to access a given Solace Event API Product with a plan.
     * @param eventApiProductId         ID of the Solace Event API Product.
     * @param planId                    ID of the Solace plan.
     * @param appRegistrationId         ID of the Solace App Registration.
     * @throws APIManagementException   If an error occurs while creating access request.
     */
    public void createAccessRequest(String eventApiProductId, String planId, String appRegistrationId)
            throws APIManagementException {
        String accessRequestId = getAccessRequestId(appRegistrationId, eventApiProductId, planId);
        AppRegistration.AccessRequest accessRequest = new AppRegistration.AccessRequest();
        accessRequest.setAccessRequestId(accessRequestId);
        accessRequest.setEventApiProductId(eventApiProductId);
        accessRequest.setPlanId(planId);
        try {
            solaceV2ApimApisClient.createAccessRequest(appRegistrationId, accessRequest);
        } catch (SolaceApiClientException e) {
            throw new APIManagementException("Error while creating access request", e);
        }
    }

    /**
     * Deletes an access request from a Solace App Registration.
     * @param appRegistrationId         ID of the Solace App Registration.
     * @param eventApiProductId         ID of the Solace Event API Product.
     * @throws APIManagementException   If an error occurs while deleting access request.
     */
    public void deleteAccessRequest(String appRegistrationId, String eventApiProductId) throws APIManagementException {
        try {
            JsonObject response = solaceV2ApimApisClient.getAccessRequests(appRegistrationId);
            JsonArray dataArray = response.getAsJsonArray("data");
            String accessRequestId = null;
            for (JsonElement element : dataArray) {
                JsonObject accessRequest = element.getAsJsonObject();
                if (eventApiProductId.equals(accessRequest.get("eventApiProductId").getAsString())) {
                    accessRequestId = accessRequest.get("accessRequestId").getAsString();
                    break;
                }
            }
            if (accessRequestId != null) {
                solaceV2ApimApisClient.deleteAccessRequest(appRegistrationId, accessRequestId);
            }
        } catch (SolaceApiClientException e) {
            throw new APIManagementException("Error while deleting access request", e);
        }
    }

    /**
     * Generates an access request ID with the provided app registration ID, event API product ID, and plan ID.
     * @param appRegistrationId ID of the Solace App Registration.
     * @param eventApiProductId ID of the Solace Event API Product.
     * @param planId            ID of the Solace plan.
     * @return                  Generated access request ID.
     */
    private static String getAccessRequestId(String appRegistrationId, String eventApiProductId, String planId) {
        return appRegistrationId + "_" + eventApiProductId + "_" + planId;
    }
}
