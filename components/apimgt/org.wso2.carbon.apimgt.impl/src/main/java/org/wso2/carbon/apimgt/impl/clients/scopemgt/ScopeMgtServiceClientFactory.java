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

import feign.Feign;
import feign.auth.BasicAuthRequestInterceptor;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.httpclient.ApacheHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * Factory class to create Scope Management Service Clients
 */
public class ScopeMgtServiceClientFactory {

    /**
     * Create and return Scope Management Service Client for tenant
     *
     * @param keyManagerConfiguration KeyManager Configuration
     * @return ScopeMgtServiceClient
     */
    public static ScopeMgtServiceClient getScopeMgtServiceClient(KeyManagerConfiguration keyManagerConfiguration,
                                                                 String tenantDomain) throws APIManagementException {

        String authServerURL = keyManagerConfiguration.getParameter(APIConstants.AUTHSERVER_URL);
        if (StringUtils.isEmpty(authServerURL)) {
            throw new APIManagementException("API Key Validator Server URL cannot be empty or null");
        }
        String scopeMgtTenantEndpoint = authServerURL.split("/" + APIConstants.SERVICES_URL_RELATIVE_PATH)[0];
        if (StringUtils.isNoneEmpty(tenantDomain) &&
                !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            scopeMgtTenantEndpoint += "/t/" + tenantDomain;
        }
        scopeMgtTenantEndpoint += APIConstants.KEY_MANAGER_OAUTH2_SCOPES_REST_API_BASE_PATH;
        return getScopeMgtServiceClient(scopeMgtTenantEndpoint,
                keyManagerConfiguration.getParameter(APIConstants.KEY_MANAGER_USERNAME),
                keyManagerConfiguration.getParameter(APIConstants.KEY_MANAGER_PASSWORD));
    }

    /**
     * Create and return Scope Management Service Client
     *
     * @param scopeMgtEndpoint Scope Management Endpoint
     * @param username         Username
     * @param password         Password
     * @return ScopeMgtServiceClient object
     */
    public static ScopeMgtServiceClient getScopeMgtServiceClient(String scopeMgtEndpoint, String username,
                                                                 String password) {

        return Feign.builder()
                .requestInterceptor(new BasicAuthRequestInterceptor(username, password))
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .errorDecoder(new ScopeErrorDecoder())
                .client(new ApacheHttpClient())
                .target(ScopeMgtServiceClient.class, scopeMgtEndpoint);
    }

}
