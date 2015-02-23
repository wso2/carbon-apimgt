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
package org.apache.synapse.securevault;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.securevault.secret.SecretCallback;
import org.apache.synapse.securevault.secret.SecretCallbackHandler;
import org.apache.synapse.securevault.secret.SecretLoadingModule;
import org.apache.synapse.securevault.secret.SingleSecretCallback;

import java.util.ArrayList;

/**
 * Responsible for resolving secrets such as password. The secrets this SecretResolver should be
 * resolved , can be  given as protected Tokens and the use of this class can explicitly check
 * whether a token is protected.
 */
public class SecretResolver {

    private static Log log = LogFactory.getLog(SecretResolver.class);

    private boolean initialized = false;

    private final ArrayList<String> protectedTokens = new ArrayList<String>();

    private SecretLoadingModule secretLoadingModule;

    private final static String DEFAULT_PROMPT = "password > ";

    /**
     * Initializes by giving an instance of <code>SecretCallbackHandler </code> to be used to
     * retrieve secrets
     *
     * @param secretCallbackHandler <code>SecretCallbackHandler </code> instance
     */
    public void init(SecretCallbackHandler secretCallbackHandler) {

        if (initialized) {
            if (log.isDebugEnabled()) {
                log.debug("SecretResolver already has been started.");
            }
            return;
        }

        if (secretCallbackHandler == null) {
            throw new SecureVaultException("SecretResolver cannot be initialized. " +
                    "The provided SecretCallbackHandler is null", log);

        }

        this.secretLoadingModule = new SecretLoadingModule();
        this.secretLoadingModule.init(new SecretCallbackHandler[]{secretCallbackHandler});
        this.initialized = true;
    }

    /**
     * Resolved given password using an instance of a PasswordProvider
     *
     * @param encryptedPassword Encrypted password
     * @return resolved password
     */
    public String resolve(String encryptedPassword) {

        return resolve(encryptedPassword, DEFAULT_PROMPT);
    }

    /**
     * Resolved given password using an instance of a PasswordProvider
     *
     * @param encryptedPassword Encrypted password
     * @param prompt            to be used to interact with user
     * @return resolved password
     */
    public String resolve(String encryptedPassword, String prompt) {

        assertInitialized();

        if (encryptedPassword == null || "".equals(encryptedPassword)) {
            if (log.isDebugEnabled()) {
                log.debug("Given Encrypted Password is empty or null. Returning itself");
            }
            return encryptedPassword;
        }

        SingleSecretCallback secretCallback = new SingleSecretCallback(encryptedPassword);

        secretCallback.setPrompt(prompt);

        secretLoadingModule.load(new SecretCallback[]{secretCallback});

        String plainText = secretCallback.getSecret();

        return plainText;
    }

    /**
     * Registers a token as a Protected Token
     *
     * @param token <code>String</code> representation of a token
     */
    public void addProtectedToken(String token) {
        assertInitialized();
        if (token != null && !"".equals(token)) {
            protectedTokens.add(token.trim());
        }
    }

    /**
     * Checks whether a token is a Protected Token
     *
     * @param token <code>String</code> representation of a token
     * @return <code>true</code> if the token is a Protected Token
     */
    public boolean isTokenProtected(String token) {
        assertInitialized();
        return token != null && !"".equals(token) && protectedTokens.contains(token.trim());
    }

    /**
     * Checks the state of the rule engine.
     * It is recommended to check state of the this component prior to access any methods of this
     *
     * @return <code>true<code> if the rule engine has been initialized
     */
    public boolean isInitialized() {
        return initialized;
    }

    private void assertInitialized() {
        if (!initialized) {
            throw new SecureVaultException("SecretResolver has not been initialized, " +
                    "it requires to be initialized, with the required " +
                    "configurations before starting", log);
        }
    }

    /**
     * Shutdown the secret resolver
     */
    public void shutDown() {
        initialized = false;
        secretLoadingModule = null;
        protectedTokens.clear();
    }
}
