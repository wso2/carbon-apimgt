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

import feign.Client;
import feign.Feign;
import feign.auth.BasicAuthRequestInterceptor;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import org.wso2.carbon.apimgt.core.configuration.models.KeyMgtConfigurations;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.core.util.AMSSLSocketFactory;

/**
 * Factory class to create Scope Registration service stubs
 */
public class ScopeRegistrationServiceStubFactory {

    /**
     * Create and return Scope Registration service stubs
     *
     * @return ScopeRegistrationServiceStub service stubs
     * @throws APIManagementException if error occurs while crating {@link ScopeRegistrationServiceStub} service stub
     */
    public static ScopeRegistrationServiceStub getScopeRegistrationServiceStub() throws APIManagementException {
        KeyMgtConfigurations keyManagerConfigs = ServiceReferenceHolder.getInstance().getAPIMConfiguration()
                .getKeyManagerConfigs();
        return getScopeRegistrationServiceStub(keyManagerConfigs.getScopeRegistrationEndpoint(),
                keyManagerConfigs.getKeyManagerCredentials().getUsername(),
                keyManagerConfigs.getKeyManagerCredentials().getPassword(),
                keyManagerConfigs.getKeyManagerCertAlias());
    }

    /**
     * Create and return Resource Registration service stubs
     *
     * @param scopeRegistrationEndpoint Scope Registration Endpoint
     * @param username    Username of Key Manager
     * @param password    Password of Key Manager
     * @param kmCertAlias Alias of Public Key of Key Manager
     * @return ScopeRegistrationServiceStub service stubs
     * @throws APIManagementException if error occurs while {@link ScopeRegistrationServiceStub} service stub
     */
    public static ScopeRegistrationServiceStub getScopeRegistrationServiceStub(String scopeRegistrationEndpoint, String
            username, String password, String kmCertAlias) throws APIManagementException {
        return Feign.builder()
                .requestInterceptor(new BasicAuthRequestInterceptor(username, password))
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .client(new Client.Default(AMSSLSocketFactory.getSSLSocketFactory(kmCertAlias),
                        (hostname, sslSession) -> true))
                .target(ScopeRegistrationServiceStub.class, scopeRegistrationEndpoint);

    }

}
