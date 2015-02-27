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

import org.apache.synapse.securevault.secret.SecretInformation;

import javax.net.ssl.KeyManagerFactory;
import java.security.KeyStore;

/**
 * Represents the abstraction private key entry store (identity) information
 */
public class IdentityKeyStoreInformation extends KeyStoreInformation {

    /* Password for access private key*/
    private SecretInformation keyPasswordProvider;

    public void setKeyPasswordProvider(SecretInformation keyPasswordProvider) {
        this.keyPasswordProvider = keyPasswordProvider;
    }

    /**
     * Returns the IdentityKeyManagerFactory instance
     *
     * @return IdentityKeyManagerFactory instance
     */
    public KeyManagerFactory getIdentityKeyManagerFactoryInstance() {

        try {
            if (log.isDebugEnabled()) {
                log.debug("Creating a IdentityKeyManagerFactory instance");
            }

            KeyStore keyStore = this.getIdentityKeyStore();
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                    KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keyPasswordProvider.getResolvedSecret().toCharArray());

            return keyManagerFactory;
        } catch (Exception e) {
            handleException("Error getting KeyManagerFactory: ", e);
        }

        return null;
    }

    /**
     * Returns a KeyStore instance that has been created from identity keystore
     *
     * @return KeyStore Instance
     */
    public KeyStore getIdentityKeyStore() {
        return super.getKeyStore();
    }

    public SecretInformation getKeyPasswordProvider() {
        return keyPasswordProvider;
    }
}
