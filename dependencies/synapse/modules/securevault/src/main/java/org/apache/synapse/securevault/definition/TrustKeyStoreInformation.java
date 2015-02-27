/*
*  Licensed to the Apache Software Foundation (ASF) under one
*  or more contributor license agreements.  See the NOTICE file
*  distributed with this work for additional information
*  regarding copyright ownership.  The ASF licenses this file
*  to you under the Apache License, Version 2.0 (the
*  "License"); you may not use this file except in compliance
*  with the License.  You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.apache.synapse.securevault.definition;

import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;

/**
 * Represents the abstraction - Trusted Certificate Store Information
 */
public class TrustKeyStoreInformation extends KeyStoreInformation {

    /**
     * Returns the TrustManagerFactory instance
     *
     * @return TrustManagerFactory instance
     */
    public TrustManagerFactory getTrustManagerFactoryInstance() {

        try {
            if (log.isDebugEnabled()) {
                log.debug("Creating a TrustManagerFactory instance");
            }
            KeyStore trustStore = this.getTrustStore();
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            return trustManagerFactory;
        } catch (Exception e) {
            handleException("Error getting TrustManagerFactory: ", e);
        }

        return null;
    }

    /**
     * Returns a KeyStore instance that has been created using trust store
     *
     * @return KeyStore Instance
     */
    public KeyStore getTrustStore() {
        return super.getKeyStore();

    }

}
