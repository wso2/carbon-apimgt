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
import org.wso2.carbon.apimgt.core.auth.dto.SCIMUser;

/**
 * This is the stub class for SCIM service
 */
public interface SCIMServiceStub {

    @Headers("Content-Type: application/json")
    @RequestLine("POST /Users")
    Response addUser(SCIMUser scimUser);

    @RequestLine("GET /Users/{id}")
    Response getUser(@Param("id") String id);

    @RequestLine("GET /Groups?filter={query}")
    Response searchGroups(@Param("query") String query);

    @RequestLine("GET /Groups/{id}")
    Response getGroup(@Param("id") String id);

    @RequestLine("GET /Users?filter={query}")
    Response searchUsers(@Param("query") String query);
}


