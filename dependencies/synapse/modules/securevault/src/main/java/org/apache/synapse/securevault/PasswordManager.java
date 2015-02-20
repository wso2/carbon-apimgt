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

import java.util.Properties;

/**
 * Wraps a SecretResolver  and exposes as a singleton PasswordManager
 * TODO - more doc
 */
public class PasswordManager {

    private static final Log log = LogFactory.getLog(PasswordManager.class);

    private static PasswordManager ourInstance = new PasswordManager();

    private SecretResolver secretResolver;

    private boolean initialized = false;

    public static PasswordManager getInstance() {
        return ourInstance;
    }

    private PasswordManager() {
    }

    public void init(Properties properties, String prefix) {
        secretResolver = SecretResolverFactory.create(properties, prefix);
        initialized = secretResolver.isInitialized();
    }

    public String resolve(String encryptedPassword) {
        assertInitialized();
        return secretResolver.resolve(encryptedPassword);
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void addProtectedToken(String token) {
        assertInitialized();
        secretResolver.addProtectedToken(token);

    }

    public boolean isTokenProtected(String token) {
        assertInitialized();
        return secretResolver.isTokenProtected(token);
    }

    public void shutDown() {
        assertInitialized();
        initialized = false;
        secretResolver = null;
    }

    private void assertInitialized() {
        if (!initialized) {
            handleException("PasswordManager has not been initialized");
        }
    }

    private void handleException(String msg) {
        log.error(msg);
        throw new SecureVaultException(msg);
    }

    public SecretResolver getSecretResolver() {
        assertInitialized();
        return secretResolver;
    }
}
