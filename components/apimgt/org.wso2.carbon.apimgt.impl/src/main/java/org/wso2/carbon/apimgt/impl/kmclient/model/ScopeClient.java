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
import feign.Response;
import org.wso2.carbon.apimgt.impl.dto.ScopeDTO;
import org.wso2.carbon.apimgt.impl.kmclient.KeyManagerClientException;

public interface ScopeClient {

    @Headers("Content-Type: application/json")
    @RequestLine("POST ")
    Response registerScope(ScopeDTO scopeDTO) throws KeyManagerClientException;

    @RequestLine("GET ")
    ScopeDTO[] getScopes() throws KeyManagerClientException;

    @RequestLine("GET /name/{name}")
    ScopeDTO getScopeByName(@Param("name") String name) throws KeyManagerClientException;

    @Headers("Content-Type: application/json")
    @RequestLine("PUT /name/{name}")
    Response updateScope(ScopeDTO scopeDTO, @Param("name") String name) throws KeyManagerClientException;

    @Headers("Content-Type: application/json")
    @RequestLine("DELETE /name/{name}")
    Response deleteScope(@Param("name") String name) throws KeyManagerClientException;

    @RequestLine("HEAD /name/{name}")
    Response isScopeExist(@Param("name") String name) throws KeyManagerClientException;
}
