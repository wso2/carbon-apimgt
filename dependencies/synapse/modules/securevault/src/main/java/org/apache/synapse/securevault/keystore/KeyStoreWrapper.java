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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.securevault.SecureVaultException;
import org.apache.synapse.securevault.definition.IdentityKeyStoreInformation;
import org.apache.synapse.securevault.definition.KeyStoreInformation;
import org.apache.synapse.securevault.definition.TrustKeyStoreInformation;

import javax.crypto.SecretKey;
import java.security.*;
import java.security.cert.Certificate;

/**
 * Wraps the keyStore and provide abstraction need for ciphering.
 */
public abstract class KeyStoreWrapper {

    protected Log log;
    /* Bean that encapsulates the information about KeyStore */
    private KeyStoreInformation keyStoreInformation;
    /* Underlying KeyStore */
    private KeyStore keyStore;
    /* Password to access private key entries*/
    private String keyPassword;

    protected KeyStoreWrapper() {
        log = LogFactory.getLog(this.getClass());
    }

    /**
     * Initialize the KeyStore wrapper based on provided KeyStoreInformation and passwords
     *
     * @param information The object that has encapsulated all information for a
     *                    keyStore excepts passwords
     * @param keyPassword Specifies the password of the key within the keyStore
     */
    protected void init(KeyStoreInformation information, String keyPassword) {

        if (information == null) {
            throw new SecureVaultException("KeyStore information cannot be found", log);
        }
        this.keyStoreInformation = information;
        this.keyPassword = keyPassword;

        if (information instanceof TrustKeyStoreInformation) {
            this.keyStore = ((TrustKeyStoreInformation) information).getTrustStore();
        } else if (information instanceof IdentityKeyStoreInformation) {
            this.keyStore = ((IdentityKeyStoreInformation) information).getIdentityKeyStore();
        } else {
            throw new SecureVaultException("Invalid KeyStore type", log);
        }
    }

    /**
     * Returns the key based on provided alias and key password
     *
     * @param alias       The alias of the certificate in the specified keyStore
     * @param keyPassword Password for key within the KeyStrore
     * @return Key if there is a one , otherwise null
     */
    protected Key getKey(String alias, String keyPassword) {

        if (alias == null || "".equals(alias)) {
            throw new SecureVaultException("The alias need to provided to get certificate", log);
        }
        if (keyPassword != null) {
            try {
                return keyStore.getKey(alias, keyPassword.toCharArray());
            } catch (KeyStoreException e) {
                throw new SecureVaultException("Error loading key for alias : " + alias, e, log);
            } catch (NoSuchAlgorithmException e) {
                throw new SecureVaultException("Error loading key for alias : " + alias, e, log);
            } catch (UnrecoverableKeyException e) {
                throw new SecureVaultException("Error loading key for alias : " + alias, e, log);
            }
        }
        return null;
    }

    /**
     * Returns the key based on certificate of the owner to who given alias belong
     *
     * @param alias The alias of the certificate in the specified keyStore
     * @return Key , if there is a one , otherwise null
     */
    protected Key getPublicKeyFromCertificate(String alias) {
        try {
            Certificate certificate = keyStore.getCertificate(alias);
            if (certificate != null) {
                return certificate.getPublicKey();
            }
        } catch (KeyStoreException e) {
            throw new SecureVaultException("Error loading key for alias : " + alias, e, log);
        }
        return null;
    }

    /**
     * Returns the key based on default alias or password
     *
     * @return Key , if there is a one , otherwise null
     */
    protected Key getDefaultPrivateKey() {
        if (keyPassword != null) {
            return getKey(keyStoreInformation.getAlias(), keyPassword);
        }
        return null;
    }

    /**
     * Returns the key based on default key password
     *
     * @param alias The alias
     * @return Key , if there is a one , otherwise null
     */
    protected Key getPrivateKey(String alias) {
        return getKey(alias, keyPassword);
    }

    /**
     * Returns the public key for the given alias
     *
     * @param alias The alias of the certificate in the specified keyStore
     * @return PublicKey if there is a one , otherwise null
     */
    public PublicKey getPublicKey(String alias) {
        Key key = getPublicKeyFromCertificate(alias);
        if (key instanceof PublicKey) {
            return (PublicKey) key;
        }
        return null;
    }

    /**
     * Returns the public key based on initialization data
     *
     * @return PublicKey if there is a one , otherwise null
     */
    public PublicKey getPublicKey() {
        Key key = getPublicKeyFromCertificate(keyStoreInformation.getAlias());
        if (key instanceof PublicKey) {
            return (PublicKey) key;
        }
        return null;
    }

    /**
     * Returns KeyStore Information
     *
     * @return KeyStore Instance
     */
    protected KeyStore getKeyStore() {
        return keyStore;
    }

    /**
     * Returns the secret key
     *
     * @param alias       The alias of the certificate in the specified keyStore
     * @param keyPassword Password to access secret key
     * @return SecretKey if there is a one , otherwise null
     */
    public SecretKey getSecretKey(String alias, String keyPassword) {
        Key key = getKey(alias, keyPassword);
        if (key instanceof SecretKey) {
            return (SecretKey) key;
        }
        return null;
    }

    /**
     * Returns the secret key based on initialization data
     *
     * @return SecretKey if there is a one , otherwise null
     */
    public SecretKey getSecretKey() {
        Key key = getKey(keyStoreInformation.getAlias(),
                keyStoreInformation.getKeyStorePasswordProvider().getResolvedSecret());
        if (key instanceof SecretKey) {
            return (SecretKey) key;
        }
        return null;
    }
}
