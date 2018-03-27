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
import org.wso2.carbon.apimgt.core.impl.WSO2ISKeyManagerImpl;
import org.wso2.carbon.apimgt.core.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.core.util.AMSSLSocketFactory;

/**
 * Factory class to create Scope Registration service stubs
 */
public class ScopeRegistrationServiceStubFactory {

    /**
     * Create and return Scope Registration service stubs
     *
     * @return ScopeRegistration implementation
     * @throws APIManagementException if error occurs while crating {@link ScopeRegistration}
     */
    public static ScopeRegistration getScopeRegistration() throws APIManagementException {
        KeyMgtConfigurations keyManagerConfigs = ServiceReferenceHolder.getInstance().getAPIMConfiguration()
                .getKeyManagerConfigs();
        if (WSO2ISKeyManagerImpl.class.getCanonicalName().equals(keyManagerConfigs.getKeyManagerImplClass())) {
            WSO2ISScopeRegistrationServiceStub wso2ISScopeRegistrationServiceStub = Feign.builder().requestInterceptor
                    (new BasicAuthRequestInterceptor(keyManagerConfigs.getKeyManagerCredentials().getUsername(),
                            keyManagerConfigs.getKeyManagerCredentials().getPassword())).encoder(new GsonEncoder())
                    .decoder(new GsonDecoder()).client(new Client.Default(AMSSLSocketFactory.getSSLSocketFactory
                            (keyManagerConfigs.getKeyManagerCertAlias()), (hostname, sslSession) -> true)).target
                            (WSO2ISScopeRegistrationServiceStub.class, keyManagerConfigs.getDcrEndpoint());
            return new WSO2ISScopeRegistrationImpl(wso2ISScopeRegistrationServiceStub);
        } else {
            DefaultScopeRegistrationServiceStub defaultScopeRegistrationServiceStub = Feign.builder().requestInterceptor
                    (new BasicAuthRequestInterceptor(keyManagerConfigs.getKeyManagerCredentials().getUsername(),
                            keyManagerConfigs.getKeyManagerCredentials().getPassword())).encoder(new GsonEncoder())
                    .decoder(new GsonDecoder()).client(new Client.Default(AMSSLSocketFactory.getSSLSocketFactory
                            (keyManagerConfigs.getKeyManagerCertAlias()), (hostname, sslSession) -> true)).target
                            (DefaultScopeRegistrationServiceStub.class, keyManagerConfigs.getDcrEndpoint());
            return new DefaultScopeRegistrationImpl(defaultScopeRegistrationServiceStub);
        }
    }
}
