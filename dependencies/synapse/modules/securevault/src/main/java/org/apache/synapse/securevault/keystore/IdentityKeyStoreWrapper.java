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
package org.apache.synapse.securevault.keystore;

import org.apache.synapse.securevault.definition.IdentityKeyStoreInformation;

import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;

/**
 * Represents the private keyStore entry
 * To provide that abstraction , this class exposes both getter methods to public,
 * private and secret keys
 */
public class IdentityKeyStoreWrapper extends KeyStoreWrapper {

    public void init(IdentityKeyStoreInformation information, String keyPassword) {
        super.init(information, keyPassword);
    }

    /**
     * Returns the private or secret  key based on given password and alias
     *
     * @param alias       The alias of the certificate in the specified keyStore
     * @param keyPassword Password to access private key
     * @return PrivateKey if there is a one , otherwise null
     */
    public PrivateKey getPrivateKey(String alias, String keyPassword) {
        Key key = super.getKey(alias, keyPassword);
        if (key instanceof PrivateKey) {
            return (PrivateKey) key;
        }
        return null;
    }

    /**
     * Returns the private key based on initialization data
     *
     * @return PrivateKey if there is a one , otherwise null
     */
    public PrivateKey getPrivateKey() {
        Key key = super.getDefaultPrivateKey();
        if (key instanceof PrivateKey) {
            return (PrivateKey) key;
        }
        return null;
    }

    /**
     * Returns the private key based on initialization data
     *
     * @return PrivateKey if there is a one , otherwise null
     */
    public PrivateKey getPrivateKey(String alias) {
        Key key = super.getPrivateKey(alias);
        if (key instanceof PrivateKey) {
            return (PrivateKey) key;
        }
        return null;
    }

    /**
     * Abstraction for getting Private Entry KeyStore(Identity)
     *
     * @return KeyStore Instance
     */
    public KeyStore getIdentityKeyStore() {
        return getKeyStore();
    }
}
