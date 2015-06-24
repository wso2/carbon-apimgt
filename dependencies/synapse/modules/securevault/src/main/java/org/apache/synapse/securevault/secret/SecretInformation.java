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
package org.apache.synapse.securevault.secret;

import org.apache.synapse.securevault.SecretResolver;


/**
 * Encapsulates the All information related to a DataSource
 * TODO - properly remove SecretResolve instances
 */
public class SecretInformation {

    private String user;
    private String aliasSecret;
    private String secretPrompt;
    private SecretResolver localSecretResolver;
    private SecretResolver globalSecretResolver;
    private String token;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getAliasSecret() {
        return aliasSecret;
    }

    public void setAliasSecret(String aliasSecret) {
        this.aliasSecret = aliasSecret;
    }

    public String getSecretPrompt() {
        return secretPrompt;
    }

    public void setSecretPrompt(String secretPrompt) {
        this.secretPrompt = secretPrompt;
    }

    /**
     * Get actual password based on SecretCallbackHandler and alias password
     * If SecretCallbackHandler is null, then returns alias password
     *
     * @return Actual password
     */
    public String getResolvedSecret() {

        SecretResolver secretResolver = null;

        if (localSecretResolver != null && localSecretResolver.isInitialized()) {
            secretResolver = localSecretResolver;
        } else if (globalSecretResolver != null && globalSecretResolver.isInitialized()
                && globalSecretResolver.isTokenProtected(token)) {
            secretResolver = globalSecretResolver;
        }

        if (secretResolver != null) {
            if (aliasSecret != null && !"".equals(aliasSecret)) {
                if (secretPrompt == null) {
                    return secretResolver.resolve(aliasSecret);
                } else {
                    return secretResolver.resolve(aliasSecret, secretPrompt);
                }
            }
        }
        return aliasSecret;
    }

    public SecretResolver getLocalSecretResolver() {
        return localSecretResolver;
    }

    public void setLocalSecretResolver(SecretResolver localSecretResolver) {
        this.localSecretResolver = localSecretResolver;
    }

    public SecretResolver getGlobalSecretResolver() {
        return globalSecretResolver;
    }

    public void setGlobalSecretResolver(SecretResolver globalSecretResolver) {
        this.globalSecretResolver = globalSecretResolver;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
