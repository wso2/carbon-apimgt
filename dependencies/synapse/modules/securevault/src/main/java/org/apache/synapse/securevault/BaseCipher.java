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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.securevault.definition.CipherInformation;
import org.apache.synapse.securevault.definition.IdentityKeyStoreInformation;
import org.apache.synapse.securevault.definition.KeyStoreInformation;
import org.apache.synapse.securevault.definition.TrustKeyStoreInformation;
import org.apache.synapse.securevault.keystore.IdentityKeyStoreWrapper;
import org.apache.synapse.securevault.keystore.KeyStoreWrapper;
import org.apache.synapse.securevault.keystore.TrustKeyStoreWrapper;
import org.apache.synapse.securevault.secret.SecretInformation;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

/**
 * Wraps the cipher and expose abstraction need for synapse ciphering
 */
public abstract class BaseCipher implements EncryptionProvider, DecryptionProvider {

    private CipherInformation cipherInformation;
    private KeyStoreInformation keystoreInformation;
    private static Log log = LogFactory.getLog(BaseCipher.class);
    /* Underlying cipher instance*/
    private Cipher cipher;
    protected KeyStoreWrapper keyStoreWrapper;
    private Key key;

    protected BaseCipher(CipherInformation cipherInformation,
                         KeyStoreInformation keystoreInformation) {
        this.cipherInformation = cipherInformation;
        this.keystoreInformation = keystoreInformation;
        if (keystoreInformation instanceof TrustKeyStoreInformation) {
            keyStoreWrapper = new TrustKeyStoreWrapper();
            ((TrustKeyStoreWrapper) keyStoreWrapper).init(
                    (TrustKeyStoreInformation) keystoreInformation);
        } else {
            keyStoreWrapper = new IdentityKeyStoreWrapper();
            IdentityKeyStoreInformation identityKeyStore =
                    (IdentityKeyStoreInformation) keystoreInformation;

            SecretInformation secretInformation = identityKeyStore.getKeyPasswordProvider();
            if (secretInformation != null) { //TODO validate
                ((IdentityKeyStoreWrapper) keyStoreWrapper).init(identityKeyStore,
                        secretInformation.getResolvedSecret());
            }
        }
        init();
    }

    protected BaseCipher(CipherInformation cipherInformation,
                         KeyStoreWrapper keyStoreWrapper) {
        this.keyStoreWrapper = keyStoreWrapper;
        this.cipherInformation = cipherInformation;
        init();
    }

    protected BaseCipher(CipherInformation cipherInformation, Key key) {
        this.key = key;
        this.cipherInformation = cipherInformation;
        init();
    }

    private void init() {

        String algorithm = cipherInformation.getAlgorithm();
        CipherOperationMode opMode = cipherInformation.getCipherOperationMode();
        if (key == null) {
            key = getKey(opMode);
        }
        if (log.isDebugEnabled()) {
            log.debug("Initializing cipher with algorithm " +
                    "'" + algorithm + "' in mode '" + opMode + "'");
        }
        try {
            String provider = cipherInformation.getProvider();
            if (provider != null && !"".equals(provider)) {
                try {
                    cipher = Cipher.getInstance(algorithm, provider.trim());
                } catch (NoSuchProviderException e) {
                    throw new SecureVaultException("Invalid Provider : " + provider, log);
                }
            } else {
                cipher = Cipher.getInstance(algorithm);
            }
            if (opMode == CipherOperationMode.ENCRYPT) {
                cipher.init(Cipher.ENCRYPT_MODE, key);
            } else if (opMode == CipherOperationMode.DECRYPT) {
                cipher.init(Cipher.DECRYPT_MODE, key);
            } else {
                throw new SecureVaultException("Invalid mode : " + opMode, log);
            }

        } catch (NoSuchAlgorithmException e) {
            throw new SecureVaultException("There is no algorithm support for " +
                    "'" + algorithm + "' in the operation mode '" + opMode + "'" + e, log);
        } catch (NoSuchPaddingException e) {
            throw new SecureVaultException("There is no padding scheme  for " +
                    "'" + algorithm + "' in the operation mode '" + opMode + "'" + e, log);
        } catch (InvalidKeyException e) {
            throw new SecureVaultException("Invalid key ", e, log);
        }
    }

    public CipherInformation getCipherInformation() {
        return cipherInformation;
    }

    public KeyStoreInformation getKeyStoreInformation() {
        return keystoreInformation;
    }

    /**
     * Returns the correct key for correct operation
     *
     * @param operationMode Ciper operation
     * @return A key
     */
    public abstract Key getKey(CipherOperationMode operationMode);

    /**
     * Do cryptographic operation
     *
     * @param inputStream Input Stream
     * @return result
     */
    private byte[] doCipherOperation(byte[] inputStream) {

        InputStream sourceStream = new ByteArrayInputStream(inputStream);
        if (cipherInformation.getInType() != null) {
            try {
                sourceStream = EncodingHelper.decode(
                        sourceStream, cipherInformation.getInType());
            } catch (IOException e) {
                throw new SecureVaultException("IOError when decoding the input " +
                        "stream for cipher ", e, log);
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CipherOutputStream out = new CipherOutputStream(baos, cipher);

        byte[] buffer = new byte[64];
        int length;
        try {
            while ((length = sourceStream.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
        } catch (IOException e) {
            throw new SecureVaultException("IOError when reading the input" +
                    " stream for cipher ", e, log);
        } finally {
            try {
                sourceStream.close();
                out.flush();
                out.close();
            } catch (IOException ignored) {
                // ignore exception
            }
        }

        if (cipherInformation.getOutType() != null) {
            return EncodingHelper.encode(baos, cipherInformation.getOutType());
        } else {
            return baos.toByteArray();
        }
    }

    public byte[] encrypt(byte[] plainText) {
        return doCipherOperation(plainText);
    }

    public byte[] decrypt(byte[] cipherText) {
        return doCipherOperation(cipherText);
    }
}
