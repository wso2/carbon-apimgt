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

public interface AuthClient {

    @RequestLine("POST ")
    @Headers("Content-type:application/x-www-form-urlencoded")
    void revoke(@Param("client_id") String clientId, @Param("client_secret") String clientSecret,
                @Param("token") String token) throws KeyManagerClientException;

    @RequestLine("POST ")
    @Headers("Content-type:application/x-www-form-urlencoded")
    TokenInfo generate(@Param("client_id") String clientId, @Param("client_secret") String clientSecret,
                            @Param("grant_type") String grantType, @Param("scope") String scope)
            throws KeyManagerClientException;

    @RequestLine("POST ")
    @Headers("Content-type:application/x-www-form-urlencoded")
    TokenInfo generate(@Param("client_id") String clientId, @Param("client_secret") String clientSecret,
                       @Param("grant_type") String grantType, @Param("scope") String scope, @Param("subject_token")
                               String subjectToken, @Param("subject_token_type") String subjectTokenType)
            throws KeyManagerClientException;

    @RequestLine("POST ")
    @Headers("Content-type:application/x-www-form-urlencoded")
    TokenInfo generateWithValidityPeriod(@Param("client_id") String clientId,
                                         @Param("client_secret") String clientSecret,
                                         @Param("grant_type") String grantType,
                                         @Param("scope") String scope,
                                         @Param("validity_period") String validityPeriod)
            throws KeyManagerClientException;

}
