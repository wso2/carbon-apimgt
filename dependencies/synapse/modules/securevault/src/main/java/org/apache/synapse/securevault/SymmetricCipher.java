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

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

/**
 * The Cipher doing symmetric cryptographic operations
 * Uses default key when these is no SecretKey
 */
public class SymmetricCipher extends BaseCipher {

    private SecretKeySpec defaultKeySpec;

    public SymmetricCipher(CipherInformation cipherInformation,
                           KeyStoreInformation keystoreInformation) {
        super(cipherInformation, keystoreInformation);
    }

    public SymmetricCipher(CipherInformation cipherInformation, KeyStoreWrapper keyStoreWrapper) {
        super(cipherInformation, keyStoreWrapper);
    }

    public SymmetricCipher(CipherInformation cipherInformation, Key key) {
        super(cipherInformation, key);
    }

    public Key getKey(CipherOperationMode operationMode) {
        SecretKey secretKey = keyStoreWrapper.getSecretKey();
        if (secretKey == null) {
            if (defaultKeySpec == null) {
                defaultKeySpec = createDefaultKey();
            }
            return defaultKeySpec;
        } else {
            return secretKey;
        }
    }

    private SecretKeySpec createDefaultKey() {
        //TODO use akey generator
        byte[] keyBytes = new byte[]{
                0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
                0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f,
                0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01};
        return defaultKeySpec = new SecretKeySpec(keyBytes, "AES");
    }
}