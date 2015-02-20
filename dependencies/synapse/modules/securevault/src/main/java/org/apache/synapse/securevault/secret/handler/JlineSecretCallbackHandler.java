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

import jline.ConsoleReader;
import org.apache.synapse.securevault.secret.AbstractSecretCallbackHandler;
import org.apache.synapse.securevault.secret.SingleSecretCallback;

import java.io.IOException;

/**
 * JlineBased SecretCallbackHandler , get the required secret using command
 * line and propagates to the application that need secret *
 */
public class JlineSecretCallbackHandler extends AbstractSecretCallbackHandler {

    private final static String DEFAULT_PROMPT = "enter password> ";

    protected void handleSingleSecretCallback(SingleSecretCallback singleSecretCallback) {
        String prompt = singleSecretCallback.getPrompt();
        if (prompt == null || "".equals(prompt)) {
            prompt = DEFAULT_PROMPT;
        }
        String password = readPassword(prompt);
        if (password != null && !"".equals(password)) {
            singleSecretCallback.setSecret(password);
        }
    }

    /**
     * Heper method to read the password from Jline cmd
     *
     * @param prompt The prompt to request password
     * @return Password
     */
    private String readPassword(String prompt) {
        ConsoleReader reader;
        try {
            reader = new ConsoleReader();
            Character mask = 0;
            return reader.readLine(prompt, mask);
        } catch (IOException ignored) {
        }
        return null;
    }
}
