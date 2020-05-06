/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.kmclient.model;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.wso2.carbon.apimgt.impl.kmclient.KeyManagerClientException;

public interface DCRClient {

    @RequestLine("POST /api/identity/oauth2/dcr/v1.1/register")
    @Headers({"Content-Type: application/json", "Authorization: {authHeader}"})
    ClientInfo createApplication(@Param("authHeader") String authHeader, ClientInfo clientInfo)
            throws KeyManagerClientException;


    @RequestLine("GET /api/identity/oauth2/dcr/v1.1/register/{clientId}")
    @Headers({"Content-Type: application/json", "Authorization: {authHeader}"})
    ClientInfo getApplication(@Param("authHeader") String authHeader, @Param("clientId") String clientId)
            throws KeyManagerClientException;

    @RequestLine("PUT /api/identity/oauth2/dcr/v1.1/register/{clientId}")
    @Headers({"Content-Type: application/json", "Authorization: {authHeader}"})
    ClientInfo updateApplication(@Param("authHeader") String authHeader, @Param("clientId") String clientId,
                                        ClientInfo clientInfo) throws KeyManagerClientException;

    @RequestLine("DELETE /api/identity/oauth2/dcr/v1.1/register/{clientId}")
    @Headers({"Content-Type: application/json", "Authorization: {authHeader}"})
    void deleteApplication(@Param("authHeader") String authHeader, @Param("clientId") String clientId)
            throws KeyManagerClientException;

    //TODO: get url from config so that we do not need to handle it here
    @RequestLine("POST /t/{tenantDomain}/api/identity/oauth2/dcr/v1.1/register")
    @Headers({"Content-Type: application/json", "Authorization: {authHeader}"})
    ClientInfo createApplicationForTenant(@Param("tenantDomain") String tenantDomain,
                                                 @Param("authHeader") String authHeader, ClientInfo clientInfo)
            throws KeyManagerClientException;

    @RequestLine("GET /t/{tenantDomain}/api/identity/oauth2/dcr/v1.1/register/{clientId}")
    @Headers({"Content-Type: application/json", "Authorization: {authHeader}"})
    ClientInfo getApplicationForTenant(@Param("tenantDomain") String tenantDomain,
                                              @Param("authHeader") String authHeader,
                                              @Param("clientId") String clientId) throws KeyManagerClientException;

    @RequestLine("PUT /t/{tenantDomain}/api/identity/oauth2/dcr/v1.1/register/{clientId}")
    @Headers({"Content-Type: application/json", "Authorization: {authHeader}"})
    ClientInfo updateApplicationForTenant(@Param("tenantDomain") String tenantDomain,
                                                 @Param("authHeader") String authHeader,
                                                 @Param("clientId") String clientId, ClientInfo clientInfo)
            throws KeyManagerClientException;

    @RequestLine("DELETE /t/{tenantDomain}/api/identity/oauth2/dcr/v1.1/register/{clientId}")
    @Headers({"Content-Type: application/json", "Authorization: {authHeader}"})
    void deleteApplicationForTenant(@Param("tenantDomain") String tenantDomain,
                                           @Param("authHeader") String authHeader, @Param("clientId") String clientId)
            throws KeyManagerClientException;
}
