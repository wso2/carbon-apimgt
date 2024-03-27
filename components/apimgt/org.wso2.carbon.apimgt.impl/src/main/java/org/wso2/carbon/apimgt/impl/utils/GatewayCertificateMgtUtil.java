/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.Enumeration;

public class GatewayCertificateMgtUtil {

    private static final Log log = LogFactory.getLog(GatewayCertificateMgtUtil.class);

    /**
     * Fetches all the trusted certificate aliases from listener trust store.
     *
     * @return Trusted certificate aliases
     * @throws APIManagementException
     */
    public static Enumeration<String> getAliasesFromListenerTrustStore() throws APIManagementException {

        try {
            KeyStore trustStore = ServiceReferenceHolder.getInstance().getListenerTrustStore();
            if (trustStore != null) {
                return trustStore.aliases();
            }
        } catch (KeyStoreException e) {
            String msg = "Error getting certificate aliases from trust store";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return null;
    }

    /**
     * Fetches certificate for given certificate alias from listener trust store.
     *
     * @param certAlias Certificate alias
     * @return Certificate
     * @throws APIManagementException
     */
    public static Certificate getCertificateFromListenerTrustStore(String certAlias) throws APIManagementException {

        Certificate publicCert = null;
        try {
            KeyStore trustStore = ServiceReferenceHolder.getInstance().getListenerTrustStore();
            if (trustStore != null) {
                // Read public certificate from trust store
                publicCert = trustStore.getCertificate(certAlias);
            }
        } catch (KeyStoreException e) {
            String msg = "Error while retrieving public certificate with alias : " + certAlias;
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return publicCert;
    }
}
