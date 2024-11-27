/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.impl.client.apim;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.Response;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;

/**
 * This interface represents the client for the APIM
 */
public interface APIMClient {

    @RequestLine("GET /apis/export?apiId={apiId}&format={format}")
    @Headers("Authorization: {authorization}")
    Response getAPIMProject(@Param("apiId") String apiId,
                            @Param("authorization") String authorizationHeader,
                            @Param("format") String format) throws GovernanceException;

//    @RequestLine("GET /apis/{apiId}?organizationId={organizationId}")
//    @Headers("Authorization: {authorization}")
//    APIDTO getAPI(@Param("apiId") String apiId,
//                  @Param("organization") String organizationId,
//                  @Param("authorization") String encodedCredentials) throws GovernanceException;
//
//    @RequestLine("GET /apis?organizationId={organizationId}&limit={limit}")
//    @Headers("Authorization: {authorization}")
//    APIListDTO getAPIs(@Param("organization") String organizationId,
//                       @Param("limit") int limit,
//                       @Param("authorization") String encodedCredentials) throws GovernanceException;
}
