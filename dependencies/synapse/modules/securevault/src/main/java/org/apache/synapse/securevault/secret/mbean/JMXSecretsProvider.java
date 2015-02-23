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
package org.apache.synapse.securevault.secret.mbean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.securevault.SecureVaultException;

import java.util.HashMap;
import java.util.Map;

/**
 * SecretsMBean implementation
 */
public class JMXSecretsProvider implements JMXSecretsProviderMBean {

    private static final Log log = LogFactory.getLog(JMXSecretsProvider.class);
    /* Secrets map - id vs secret */
    private final Map<String, String> secrets = new HashMap<String, String>();

    public void addSecret(String id, String secret) {
        assertIdEmpty(id);
        assertSecretEmpty(secret);
        secrets.put(id, secret);
    }

    public String getSecret(String id) {
        assertIdEmpty(id);
        return secrets.get(id);
    }

    public void removeSecret(String id) {
        assertIdEmpty(id);
        secrets.remove(id);
    }

    public void clear() {
        secrets.clear();
    }

    private void assertIdEmpty(String id) {
        if (id == null || "".equals(id)) {
            handleException("ID cannot be empty or null");
        }
    }

    private void assertSecretEmpty(String secret) {
        if (secret == null || "".equals(secret)) {
            handleException("Secret cannot be empty or null");
        }
    }

    private void handleException(String msg) {
        log.error(msg);
        throw new SecureVaultException(msg);
    }

}
