/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.authenticator;

import feign.Param;
import feign.RequestLine;
import feign.Response;

/**
 * This is the stub class for SSO login service.
 */
public interface SSOLoginServiceStub {

    @RequestLine("GET /authorize?response_type={type}&client_id={id}&redirect_uri={uri}&scope={scope}")
    Response getAutherizationCode(
            @Param("type") String type, @Param("id") String clientId,
            @Param("uri") String redirectURI, @Param("scope") String scope);

}
