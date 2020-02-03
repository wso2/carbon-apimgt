/*
 * Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl.clients.scopemgt;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.wso2.carbon.apimgt.impl.clients.scopemgt.dto.Scope;

import java.util.List;

/**
 * Client interface for Scope Management Services
 */
public interface ScopeMgtServiceClient {

    @RequestLine("GET")
    List<Scope> getAllScopes();

    @RequestLine("POST")
    @Headers("Content-Type: application/json")
    Scope registerScope(Scope scope);

    @RequestLine("GET /name/{name}")
    Scope getScope(@Param("name") String name);

    @RequestLine("PUT /name/{name}")
    @Headers("Content-Type: application/json")
    Scope updateScope(Scope scope, @Param("name") String name);

    @RequestLine("DELETE /name/{name}")
    void deleteScope(@Param("name") String name);

}

