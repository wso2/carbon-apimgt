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

import com.google.gson.JsonObject;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.wso2.carbon.apimgt.solace.api.exception.SolaceApiClientException;
import org.wso2.carbon.apimgt.solace.api.v2.model.AppRegistration;
import org.wso2.carbon.apimgt.solace.api.v2.model.SolaceEventApiProductsResponse;

/**
 * Contains methods used to interact with Solace V2 APIM APIs.
 */
public interface SolaceV2ApimApisClient {

    @RequestLine("GET /eventApiProducts")
    @Headers("Content-Type: application/json")
    SolaceEventApiProductsResponse getEventApiProducts() throws SolaceApiClientException;

    @RequestLine("GET /eventApiProducts/{eventApiProductId}/plans/{planId}/eventApis/{eventApiId}")
    @Headers("Content-Type: application/json")
    JsonObject getEventApiAsyncApiDefinition(@Param("eventApiProductId") String eventApiProductId,
                                             @Param("planId") String planId,
                                             @Param("eventApiId") String eventApiId) throws SolaceApiClientException;

    @RequestLine("GET /eventApiProducts/{eventApiProductId}/plans")
    @Headers("Content-Type: application/json")
    JsonObject getEventApiProductPlans(@Param("eventApiProductId") String eventApiProductId)
            throws SolaceApiClientException;

    @RequestLine("POST /appRegistrations")
    @Headers("Content-Type: application/json")
    JsonObject createAppRegistration(AppRegistration appRegistration) throws SolaceApiClientException;

    @RequestLine("GET /appRegistrations/{registrationId}")
    @Headers("Content-Type: application/json")
    JsonObject getAppRegistration(@Param("registrationId") String registrationId) throws SolaceApiClientException;

    @RequestLine("DELETE /appRegistrations/{registrationId}")
    @Headers("Content-Type: application/json")
    JsonObject deleteAppRegistration(@Param("registrationId") String registrationId) throws SolaceApiClientException;

    @RequestLine("POST /appRegistrations/{registrationId}/credentials")
    @Headers("Content-Type: application/json")
    JsonObject createCredentials(@Param("registrationId") String registrationId, JsonObject credentialsRequest)
            throws SolaceApiClientException;

    @RequestLine("POST /appRegistrations/{registrationId}/accessRequests")
    @Headers("Content-Type: application/json")
    JsonObject createAccessRequest(@Param("registrationId") String registrationId,
                                   AppRegistration.AccessRequest accessRequest) throws SolaceApiClientException;

    @RequestLine("GET /appRegistrations/{registrationId}/accessRequests")
    @Headers("Content-Type: application/json")
    JsonObject getAccessRequests(@Param("registrationId") String registrationId) throws SolaceApiClientException;

    @RequestLine("DELETE /appRegistrations/{registrationId}/accessRequests/{accessRequestId}")
    @Headers("Content-Type: application/json")
    JsonObject deleteAccessRequest(@Param("registrationId") String registrationId,
                                   @Param("accessRequestId") String accessRequestId) throws SolaceApiClientException;
}
