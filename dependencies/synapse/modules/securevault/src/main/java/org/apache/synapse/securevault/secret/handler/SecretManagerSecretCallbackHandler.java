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
package org.apache.synapse.securevault.secret.handler;

import org.apache.synapse.securevault.secret.AbstractSecretCallbackHandler;
import org.apache.synapse.securevault.secret.SecretManager;
import org.apache.synapse.securevault.secret.SingleSecretCallback;

/**
 * SecretManager based secret provider , this can be used by other application
 * to get secret form  SecretManager
 */
public class SecretManagerSecretCallbackHandler extends AbstractSecretCallbackHandler {

    private final SecretManager secretManager = SecretManager.getInstance();

    protected void handleSingleSecretCallback(SingleSecretCallback singleSecretCallback) {

        if (!secretManager.isInitialized()) {
            if (log.isWarnEnabled()) {
                log.warn("SecretManager has not been initialized.Cannot collect secrets.");
            }
            return;
        }

        String id = singleSecretCallback.getId();
        if (id != null && !"".equals(id)) {
            singleSecretCallback.setSecret(secretManager.getSecret(id));
        }
    }
}
