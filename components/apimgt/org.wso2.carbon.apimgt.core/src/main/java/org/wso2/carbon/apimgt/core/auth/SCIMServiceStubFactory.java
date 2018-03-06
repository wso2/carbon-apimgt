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
import org.wso2.carbon.apimgt.core.configuration.models.IdentityProviderConfigurations;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.core.util.AMSSLSocketFactory;

/**
 * Factory class to create SCIM service stubs
 */
public class SCIMServiceStubFactory {

    private static final String WSO2_SCIM_BASE_PATH = "/api/identity/scim2/v1.0";

    /**
     * Create and return SCIM service stubs
     *
     * @return SCIM Service client
     * @throws APIManagementException if error occurs while crating SCIM service stub
     */
    public static SCIMServiceStub getSCIMServiceStub() throws APIManagementException {
        IdentityProviderConfigurations idpConfigs = ServiceReferenceHolder.getInstance().getAPIMConfiguration()
                .getIdentityProviderConfigs();
        return getSCIMServiceStub(idpConfigs.getIdentityProviderBaseUrl(),
                idpConfigs.getIdentityProviderCredentials().getUsername(),
                idpConfigs.getIdentityProviderCredentials().getPassword(),
                idpConfigs.getIdpCertAlias());
    }

    /**
     * Create and return SCIM service stubs
     *
     * @param idpBaseUrl   Base URL of Identity Provider Server
     * @param username     Username of IDP
     * @param password     Password of IDP
     * @param idpCertAlias Alias of Public Key of IDP
     * @return SCIM Service client
     * @throws APIManagementException if error occurs while crating SCIM service stub
     */
    public static SCIMServiceStub getSCIMServiceStub(String idpBaseUrl, String username, String password,
                                                     String idpCertAlias) throws APIManagementException {
        return Feign.builder()
                .requestInterceptor(new BasicAuthRequestInterceptor(username, password))
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .client(new Client.Default(AMSSLSocketFactory.getSSLSocketFactory(idpCertAlias),
                        (hostname, sslSession) -> true))
                .target(SCIMServiceStub.class, idpBaseUrl + WSO2_SCIM_BASE_PATH);

    }

}
