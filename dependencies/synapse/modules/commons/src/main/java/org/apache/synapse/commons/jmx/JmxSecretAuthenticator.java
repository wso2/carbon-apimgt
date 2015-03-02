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

package org.apache.synapse.commons.jmx;

import org.wso2.securevault.secret.SecretInformation;

import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXPrincipal;
import javax.security.auth.Subject;
import java.util.Collections;

/**
 * Handles the authentication for JMX management.
 */

public class JmxSecretAuthenticator implements JMXAuthenticator {

    private SecretInformation secretInformation;

    public JmxSecretAuthenticator(SecretInformation secretInformation) {
        this.secretInformation = secretInformation;
    }

    public Subject authenticate(Object credentials) {

        if (credentials == null) {
            throw new SecurityException("Credentials required");
        }

        if (!(credentials instanceof String[])) {
            throw new SecurityException("Credentials should be String[]");
        }

        // Only expect username/password, therefore the credentials should have two entries
        final String[] aCredentials = (String[]) credentials;
        if (aCredentials.length < 2) {
            throw new SecurityException("Credentials should have the username and password");
        }

        String username = aCredentials[0];
        String password = (aCredentials[1] != null ? aCredentials[1] : "");

        // perform authentication
        if (secretInformation.getUser().equals(username) &&
                password.equals(secretInformation.getResolvedSecret())) {
            return new Subject(true,
                Collections.singleton(new JMXPrincipal(username)),
                Collections.EMPTY_SET,
                Collections.EMPTY_SET);
        } else {
            throw new SecurityException("Username and/or password are incorrect, " +
                "or you do not have the necessary access rights.");
        }
    }
}
