/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.core.auth;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.Response;
import org.wso2.carbon.apimgt.core.auth.dto.ScopeInfo;

/**
 * This is the stub class for Scope Registration services
 */
public interface WSO2ISScopeRegistrationServiceStub {

    @Headers("Content-Type: application/json")
    @RequestLine("POST ")
    Response registerScope(ScopeInfo scopeInfo);

    @RequestLine("GET ")
    Response getScopes();

    @RequestLine("GET /name/{name}")
    Response getScopeByName(@Param("name") String name);

    @Headers("Content-Type: application/json")
    @RequestLine("PUT /name/{name}")
    Response updateScope(ScopeInfo scopeInfo, @Param("name") String name);

    @RequestLine("DELETE /name/{name}")
    Response deleteScope(@Param("name") String name);

    @RequestLine("HEAD /name/{name}")
    Response isScopeExist(@Param("name") String name);
}


