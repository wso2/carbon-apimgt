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

import java.util.ArrayList;
import java.util.List;

/**
 * Loads the secret on behalf of applications
 */
public class SecretLoadingModule {

    private final List<SecretCallbackHandler> secretCallbackHandlers =
            new ArrayList<SecretCallbackHandler>();

    /**
     * Initialized SecretLoadingModule with SecretCallbackHandlers
     *
     * @param secretCallbackHandlers SecretCallbackHandlers
     */
    public void init(SecretCallbackHandler[] secretCallbackHandlers) {
        for (SecretCallbackHandler secretCallbackHandler : secretCallbackHandlers) {
            if (secretCallbackHandler != null) {
                this.secretCallbackHandlers.add(secretCallbackHandler);
            }
        }
    }

    /**
     * Load secrets into given call backs. Use all registered call back handlers
     *
     * @param secretCallbacks SecretCallbacks
     */
    public void load(SecretCallback[] secretCallbacks) {
        for (SecretCallbackHandler secretCallbackHandler : secretCallbackHandlers) {
            if (secretCallbackHandler != null) {    //TODO
                secretCallbackHandler.handle(secretCallbacks);
            }
        }
    }
}
