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
 * Factory class to create DCR(M) service stubs
 */
public class DCRMServiceStubFactory {

    /**
     * Create and return DCR(M) service stubs
     *
     * @return DCR(M) service stubs
     * @throws APIManagementException if error occurs while crating DCR(M) service stub
     */
    public static DCRMServiceStub getDCRMServiceStub() throws APIManagementException {
        KeyMgtConfigurations keyManagerConfigs = ServiceReferenceHolder.getInstance().getAPIMConfiguration()
                .getKeyManagerConfigs();
        return getDCRMServiceStub(keyManagerConfigs.getDcrEndpoint(),
                keyManagerConfigs.getKeyManagerCredentials().getUsername(),
                keyManagerConfigs.getKeyManagerCredentials().getPassword(),
                keyManagerConfigs.getKeyManagerCertAlias());
    }

    /**
     * Create and return DCR(M) service stubs
     *
     * @param dcrEndpoint DCR(M) Endpoint
     * @param username    Username of Key Manager
     * @param password    Password of Key Manager
     * @param kmCertAlias Alias of Public Key of Key Manager
     * @return DCR(M) service stubs
     * @throws APIManagementException if error occurs while crating DCR(M) service stub
     */
    public static DCRMServiceStub getDCRMServiceStub(String dcrEndpoint, String username, String password,
                                                     String kmCertAlias) throws APIManagementException {
        return Feign.builder()
                .requestInterceptor(new BasicAuthRequestInterceptor(username, password))
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .client(new Client.Default(AMSSLSocketFactory.getSSLSocketFactory(kmCertAlias),
                        (hostname, sslSession) -> true))
                .target(DCRMServiceStub.class, dcrEndpoint);

    }

}
