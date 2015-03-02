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

import org.apache.synapse.securevault.definition.CipherInformation;
import org.apache.synapse.securevault.definition.KeyStoreInformation;
import org.apache.synapse.securevault.keystore.KeyStoreWrapper;

import java.security.Key;

/**
 * Creates Ciphers
 */
public class CipherFactory {

    public static BaseCipher createCipher(CipherInformation cipherInformation,
                                          KeyStoreInformation keystoreInformation) {

        if ("symmetric".equals(cipherInformation.getType())) {
            return new SymmetricCipher(cipherInformation, keystoreInformation);
        } else {
            return new AsymmetricCipher(cipherInformation, keystoreInformation);
        }
    }

    public static BaseCipher createCipher(CipherInformation cipherInformation,
                                          KeyStoreWrapper keyStoreWrapper) {

        if ("symmetric".equals(cipherInformation.getType())) {
            return new SymmetricCipher(cipherInformation, keyStoreWrapper);
        } else {
            return new AsymmetricCipher(cipherInformation, keyStoreWrapper);
        }
    }

    public static BaseCipher createCipher(CipherInformation cipherInformation,
                                          Key key) {

        if ("symmetric".equals(cipherInformation.getType())) {
            return new SymmetricCipher(cipherInformation, key);
        } else {
            return new AsymmetricCipher(cipherInformation, key);
        }
    }
}
