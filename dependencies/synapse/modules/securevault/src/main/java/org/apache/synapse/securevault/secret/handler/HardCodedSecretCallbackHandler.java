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
import org.apache.synapse.securevault.secret.SingleSecretCallback;

/**
 * Hard-coded passwords as secrets .This is just a demonstration example and need to be adopted
 * as user requirements. In the production environment, this may be 'close sourced' - only provided
 * a binary contains implementation class. Then, it is needed to use de- compilers to see password.
 */
public class HardCodedSecretCallbackHandler extends AbstractSecretCallbackHandler {


    protected void handleSingleSecretCallback(SingleSecretCallback singleSecretCallback) {
        // can set multiple passwords based id of SingleSecretCallback
        singleSecretCallback.setSecret("password");
    }

}
