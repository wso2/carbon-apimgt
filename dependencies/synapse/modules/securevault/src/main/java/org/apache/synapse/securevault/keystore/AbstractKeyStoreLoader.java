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
package org.apache.synapse.securevault.keystore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.securevault.IKeyStoreLoader;
import org.apache.synapse.securevault.SecureVaultException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

/**
 * Provides the base for loading KeyStores
 */
public abstract class AbstractKeyStoreLoader implements IKeyStoreLoader {

    protected Log log;

    protected AbstractKeyStoreLoader() {
        log = LogFactory.getLog(this.getClass());
    }

    /**
     * Constructs a KeyStore based on keystore location , keystore password , keystore type and
     * provider
     *
     * @param location      The location of the KeyStore
     * @param storePassword Password to unlock KeyStore
     * @param storeType     KeyStore type
     * @param provider      Provider
     * @return KeyStore Instance
     */
    protected KeyStore getKeyStore(String location, String storePassword,
                                   String storeType,
                                   String provider) {

        File keyStoreFile = new File(location);
        if (!keyStoreFile.exists()) {
            handleException("KeyStore can not be found at ' " + keyStoreFile + " '");
        }

        BufferedInputStream bis = null;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Loading KeyStore from : " + location + " Store-Type : " +
                        storeType + " Provider : " + provider);
            }
            bis = new BufferedInputStream(new FileInputStream(keyStoreFile));
            KeyStore keyStore;
            if (provider != null) {
                keyStore = KeyStore.getInstance(storeType, provider);
            } else {
                keyStore = KeyStore.getInstance(storeType);
            }
            keyStore.load(bis, storePassword.toCharArray());
            return keyStore;
        } catch (KeyStoreException e) {
            handleException("Error loading keyStore from ' " + location + " ' ", e);
        } catch (IOException e) {
            handleException("IOError loading keyStore from ' " + location + " ' ", e);
        } catch (NoSuchAlgorithmException e) {
            handleException("Error loading keyStore from ' " + location + " ' ", e);
        } catch (CertificateException e) {
            handleException("Error loading keyStore from ' " + location + " ' ", e);
        } catch (NoSuchProviderException e) {
            handleException("Error loading keyStore from ' " + location + " ' ", e);
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException ignored) {
                }
            }
        }
        return null;
    }

    protected void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SecureVaultException(msg, e);
    }

    protected void handleException(String msg) {
        log.error(msg);
        throw new SecureVaultException(msg);
    }
}
