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

import org.apache.synapse.securevault.CipherFactory;
import org.apache.synapse.securevault.CipherOperationMode;
import org.apache.synapse.securevault.DecryptionProvider;
import org.apache.synapse.securevault.EncodingType;
import org.apache.synapse.securevault.definition.CipherInformation;
import org.apache.synapse.securevault.secret.AbstractSecretCallbackHandler;
import org.apache.synapse.securevault.secret.SecretManager;
import org.apache.synapse.securevault.secret.SingleSecretCallback;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

/**
 * SecretCallbackHandler implementation which is compatible to the default encryption used
 * within the JBoss Application Server to decrypt database passwords.
 */
public class JBossEncryptionSecretCallbackHandler extends AbstractSecretCallbackHandler {

    private static final String ALGORITHM = "Blowfish";
    private static Key key = new SecretKeySpec("jaas is the way".getBytes(), ALGORITHM);
        private final SecretManager secretManager = SecretManager.getInstance();
    /**
     * Decrypts the encrypted secret provided by the specified callback handler.
     *
     * @param singleSecretCallback The singleSecretCallback which secret has to be decrypted
     */
    @Override
    protected void handleSingleSecretCallback(SingleSecretCallback singleSecretCallback) {

        if (!secretManager.isInitialized()) {
            if (log.isWarnEnabled()) {
                log.warn("SecretManager has not been initialized.Cannot collect secrets.");
            }
            return;
        }

        String id = singleSecretCallback.getId();
        if (id != null && !"".equals(id)) {
            singleSecretCallback.setSecret(decrypt(secretManager.getEncryptedData(id)));
        }        
    }

    /**
     * Decrypts the encrypted secret using the Blowfish algorithm and the same hard-coded
     * passphrase the JBoss application server uses to decrypt database passwords.
     *
     * @param encryptedSecret the encrypted secret
     * @return the decrypted secret.
     */
    private static String decrypt(String encryptedSecret) {
        CipherInformation cipherInformation = new CipherInformation();
        cipherInformation.setAlgorithm(ALGORITHM);
        cipherInformation.setCipherOperationMode(CipherOperationMode.DECRYPT);
        cipherInformation.setInType(EncodingType.BIGINTEGER16); //TODO
        DecryptionProvider decryptionProvider = CipherFactory.createCipher(cipherInformation, key);
        return new String(decryptionProvider.decrypt(encryptedSecret.getBytes()));
    }
}
