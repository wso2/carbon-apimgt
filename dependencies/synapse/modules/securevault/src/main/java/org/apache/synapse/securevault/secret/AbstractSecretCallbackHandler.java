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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;

/**
 * A Base SecretCallbackHandler
 * This expose API to handle SingleSecretCallback and all other functionality
 * is implemented by itself
 */
public abstract class AbstractSecretCallbackHandler implements SecretCallbackHandler {

    protected Log log;

    protected AbstractSecretCallbackHandler() {
        log = LogFactory.getLog(getClass());
    }

    public void handle(SecretCallback[] secretCallbacks) {

        if (secretCallbacks == null || secretCallbacks.length == 0) {
            if (log.isDebugEnabled()) {
                log.debug("Provided  SecretCallbacks are empty or null.");
            }
            return;
        }

        for (SecretCallback secretCallback : secretCallbacks) {
            if (secretCallback instanceof SingleSecretCallback) {
                handleSingleSecretCallback((SingleSecretCallback) secretCallback);
            } else if (secretCallback instanceof MultiSecretCallback) {
                handleMultiSecretCallback((MultiSecretCallback) secretCallback);
            }
        }
    }

    private void handleMultiSecretCallback(MultiSecretCallback multiSecretCallback) {

        for (Iterator<SecretCallback> callbackIterator = multiSecretCallback.getSecretCallbacks();
             callbackIterator.hasNext();) {

            SecretCallback callback = callbackIterator.next();
            if (callback instanceof SingleSecretCallback) {
                handleSingleSecretCallback((SingleSecretCallback) callback);
            } else if (callback instanceof MultiSecretCallback) {
                handleMultiSecretCallback((MultiSecretCallback) callback);
            }
        }
    }

    /**
     * Handle A SingleSecretCallback  - fill the secret
     *
     * @param singleSecretCallback SingleSecretCallback  instance
     */
    protected abstract void handleSingleSecretCallback(SingleSecretCallback singleSecretCallback);

}
