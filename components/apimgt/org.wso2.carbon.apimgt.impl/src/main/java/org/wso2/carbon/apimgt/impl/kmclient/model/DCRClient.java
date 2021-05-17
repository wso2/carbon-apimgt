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

    @RequestLine("POST ")
    @Headers("Content-Type: application/json")
    ClientInfo createApplication(ClientInfo clientInfo)
            throws KeyManagerClientException;

    @RequestLine("GET /{clientId}")
    @Headers("Content-Type: application/json")
    ClientInfo getApplication(@Param("clientId") String clientId)
            throws KeyManagerClientException;

    @RequestLine("PUT /{clientId}")
    @Headers("Content-Type: application/json")
    ClientInfo updateApplication(@Param("clientId") String clientId, ClientInfo clientInfo)
            throws KeyManagerClientException;

    @RequestLine("DELETE /{clientId}")
    @Headers("Content-Type: application/json")
    void deleteApplication(@Param("clientId") String clientId) throws KeyManagerClientException;

    @RequestLine("POST /{clientId}/change-owner?applicationOwner={applicationOwner}")
    @Headers("Content-Type: application/json")
    ClientInfo updateApplicationOwner(@Param("applicationOwner") String applicationOwner,
                                      @Param("clientId") String clientId)
            throws KeyManagerClientException;

    @RequestLine("POST /{clientId}/regenerate-consumer-secret")
    @Headers("Content-Type: application/json")
    ClientInfo updateApplicationSecret(@Param("clientId") String clientId)
            throws KeyManagerClientException;

}
