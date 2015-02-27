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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.securevault.ICACertsLoader;
import org.apache.synapse.securevault.IKeyStoreLoader;
import org.apache.synapse.securevault.KeyStoreType;
import org.apache.synapse.securevault.SecureVaultException;
import org.apache.synapse.securevault.keystore.CACertsLoader;
import org.apache.synapse.securevault.keystore.JKSKeyStoreLoader;
import org.apache.synapse.securevault.keystore.PKCS12KeyStoreLoader;
import org.apache.synapse.securevault.keystore.PKCS8KeyStoreLoader;
import org.apache.synapse.securevault.secret.SecretInformation;

import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates the keyStore related information
 */
public abstract class KeyStoreInformation {

    protected final Log log;

    public static final String KEY_STORE_CERTIFICATE_FILE_PATH = "keyStoreCertificateFilePath";
    public static final String ENABLE_HOST_NAME_VERIFIER = "enableHostnameVerifier";
    /* KeyStore type */
    private KeyStoreType storeType;
    /* Alias who belong this key */
    private String alias;
    /* KeyStore location */
    private String location;
    /* KeyStore Password to unlock KeyStore */
    private SecretInformation keyStorePasswordProvider;
    /* KeyStore provider */
    private String provider;

    private final Map<String, String> parameters = new HashMap<String, String>();

    protected KeyStoreInformation() {
        log = LogFactory.getLog(this.getClass());
    }

    public void setStoreType(String storeType) {
        if (storeType == null || "".equals(storeType)) {
            if (log.isDebugEnabled()) {
                log.debug("Given store type is null , using default type : JKS");
            }
        }
        this.storeType = KeyStoreType.valueOf(storeType);
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        if (alias == null || "".equals(alias)) {
            if (log.isDebugEnabled()) {
                log.debug("Alias for a key entry or a certificate is null");
            }
            return;
        }
        this.alias = alias;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        if (location != null && "".equals(location)) {
            handleException("KeyStore location can not be null");
        }
        this.location = location;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setKeyStorePasswordProvider(SecretInformation keyStorePasswordProvider) {
        this.keyStorePasswordProvider = keyStorePasswordProvider;
    }

    public void addParameter(String name, String value) {
        parameters.put(name, value);
    }

    public String getParameter(String name) {
        return parameters.get(name);
    }

    /**
     * Constructs the KeyStore according to the store type
     *
     * @return KeyStore Instance
     */
    protected KeyStore getKeyStore() {

        if (log.isDebugEnabled()) {
            log.debug("Loading KeyStore with type : " + storeType);
        }
        String keyStorePassword = this.keyStorePasswordProvider.getResolvedSecret();
        switch (storeType) {
            case JKS:
                IKeyStoreLoader jksKeyStoreLoader = new JKSKeyStoreLoader(location,
                        keyStorePassword);
                return jksKeyStoreLoader.getKeyStore();

            case PKCS12:
                IKeyStoreLoader pkcs12KeyStoreLoader = new PKCS12KeyStoreLoader(location,
                        keyStorePassword);
                return pkcs12KeyStoreLoader.getKeyStore();
            case PKCS8:
                IKeyStoreLoader pkcs8KeyStoreLoader = new PKCS8KeyStoreLoader(location,
                        parameters.get(KEY_STORE_CERTIFICATE_FILE_PATH),
                        keyStorePassword, alias);
                return pkcs8KeyStoreLoader.getKeyStore();
            case CA_CERTIFICATES_PATH:
                ICACertsLoader caCertsLoader = new CACertsLoader();
                return caCertsLoader.loadTrustStore(location);
            default:
                if (log.isDebugEnabled()) {
                    log.debug("No KeyStore Found");
                }
                return null;
        }
    }

    protected void handleException(String msg) {
        log.error(msg);
        throw new SecureVaultException(msg);
    }

    protected void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SecureVaultException(msg, e);
    }

    public SecretInformation getKeyStorePasswordProvider() {
        return keyStorePasswordProvider;
    }
}
