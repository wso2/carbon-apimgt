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
package org.apache.synapse.securevault.secret.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.securevault.CipherFactory;
import org.apache.synapse.securevault.CipherOperationMode;
import org.apache.synapse.securevault.DecryptionProvider;
import org.apache.synapse.securevault.EncodingType;
import org.apache.synapse.securevault.commons.MiscellaneousUtil;
import org.apache.synapse.securevault.definition.CipherInformation;
import org.apache.synapse.securevault.keystore.IdentityKeyStoreWrapper;
import org.apache.synapse.securevault.keystore.KeyStoreWrapper;
import org.apache.synapse.securevault.keystore.TrustKeyStoreWrapper;
import org.apache.synapse.securevault.secret.SecretRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Holds all secrets in a file
 */
public class FileBaseSecretRepository implements SecretRepository {

    private static Log log = LogFactory.getLog(FileBaseSecretRepository.class);

    private static final String LOCATION = "location";
    private static final String KEY_STORE = "keyStore";
    private static final String DOT = ".";
    private static final String SECRET = "secret";
    private static final String ALIAS = "alias";
    private static final String ALIASES = "aliases";
    private static final String ALGORITHM = "algorithm";
    private static final String DEFAULT_ALGORITHM = "RSA";
    private static final String TRUSTED = "trusted";
    private static final String DEFAULT_CONF_LOCATION = "cipher-text.properties";

    /* Parent secret repository */
    private SecretRepository parentRepository;
    /*Map of secrets keyed by alias for property name */
    private final Map<String, String> secrets = new HashMap<String, String>();
    /*Map of encrypted values keyed by alias for property name */
    private final Map<String, String> encryptedData = new HashMap<String, String>();
    /*Wrapper for Identity KeyStore */
    private IdentityKeyStoreWrapper identity;
    /* Wrapper for trusted KeyStore */
    private TrustKeyStoreWrapper trust;
    /* Whether this secret repository has been initiated successfully*/
    private boolean initialize = false;

    public FileBaseSecretRepository(IdentityKeyStoreWrapper identity, TrustKeyStoreWrapper trust) {
        this.identity = identity;
        this.trust = trust;
    }

    /**
     * Initializes the repository based on provided properties
     *
     * @param properties Configuration properties
     * @param id         Identifier to identify properties related to the corresponding repository
     */
    public void init(Properties properties, String id) {
        StringBuffer sb = new StringBuffer();
        sb.append(id);
        sb.append(DOT);
        sb.append(LOCATION);

        String filePath = MiscellaneousUtil.getProperty(properties,
                sb.toString(), DEFAULT_CONF_LOCATION);

        Properties cipherProperties = MiscellaneousUtil.loadProperties(filePath);
        if (cipherProperties.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Cipher texts cannot be loaded form : " + filePath);
            }
            return;
        }

        StringBuffer sbTwo = new StringBuffer();
        sbTwo.append(id);
        sbTwo.append(DOT);
        sbTwo.append(ALGORITHM);
        //Load algorithm
        String algorithm = MiscellaneousUtil.getProperty(properties,
                sbTwo.toString(), DEFAULT_ALGORITHM);
        StringBuffer buffer = new StringBuffer();
        buffer.append(DOT);
        buffer.append(KEY_STORE);

        //Load keyStore
        String keyStore = MiscellaneousUtil.getProperty(properties,
                buffer.toString(), null);
        KeyStoreWrapper keyStoreWrapper;
        if (TRUSTED.equals(keyStore)) {
            keyStoreWrapper = trust;

        } else {
            keyStoreWrapper = identity;
        }

        //Creates a cipherInformation

        CipherInformation cipherInformation = new CipherInformation();
        cipherInformation.setAlgorithm(algorithm);
        cipherInformation.setCipherOperationMode(CipherOperationMode.DECRYPT);
        cipherInformation.setInType(EncodingType.BASE64); //TODO
        DecryptionProvider baseCipher =
                CipherFactory.createCipher(cipherInformation, keyStoreWrapper);

        for (Object alias : cipherProperties.keySet()) {
            //decrypt the encrypted text 
            String key = String.valueOf(alias);
            String encryptedText = cipherProperties.getProperty(key);
            encryptedData.put(key, encryptedText);
            if (encryptedText == null || "".equals(encryptedText.trim())) {
                if (log.isDebugEnabled()) {
                    log.debug("There is no secret for the alias : " + alias);
                }
                continue;
            }

            String decryptedText = new String(baseCipher.decrypt(encryptedText.trim().getBytes()));
            secrets.put(key, decryptedText);
        }
        initialize = true;
    }

    /**
     * @param alias Alias name for look up a secret
     * @return Secret if there is any , otherwise ,alias itself
     * @see org.apache.synapse.securevault.secret.SecretRepository
     */
    public String getSecret(String alias) {

        if (alias == null || "".equals(alias)) {
            return alias; // TODO is it needed to throw an error?
        }

        if (!initialize || secrets.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("There is no secret found for alias '" + alias + "' returning itself");
            }
            return alias;
        }

        StringBuffer sb = new StringBuffer();
        sb.append(alias);

        String secret = secrets.get(sb.toString());
        if (secret == null || "".equals(secret)) {
            if (log.isDebugEnabled()) {
                log.debug("There is no secret found for alias '" + alias + "' returning itself");
            }
            return alias;
        }
        return secret;
    }

    /**
     * @param alias Alias name for look up a encrypted Value
     * @return encrypted Value if there is any , otherwise ,alias itself
     * @see org.apache.synapse.securevault.secret.SecretRepository
     */
    public String getEncryptedData(String alias) {

        if (alias == null || "".equals(alias)) {
            return alias; // TODO is it needed to throw an error?
        }

        if (!initialize || encryptedData.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("There is no secret found for alias '" + alias + "' returning itself");
            }
            return alias;
        }

        StringBuffer sb = new StringBuffer();
        sb.append(alias);

        String encryptedValue = encryptedData.get(sb.toString());
        if (encryptedValue == null || "".equals(encryptedValue)) {
            if (log.isDebugEnabled()) {
                log.debug("There is no secret found for alias '" + alias + "' returning itself");
            }
            return alias;
        }
        return encryptedValue;
    }

    public void setParent(SecretRepository parent) {
        this.parentRepository = parent;
    }

    public SecretRepository getParent() {
        return this.parentRepository;
    }
}
