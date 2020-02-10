/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl.certificatemgt.reloader;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * This class used to load new certificate file into Memory
 */
public class CertificateReLoader implements Runnable {

    private static final Log log = LogFactory.getLog(CertificateReLoader.class);
    private static String TRUST_STORE_PASSWORD = System.getProperty("javax.net.ssl.trustStorePassword");
    private static String TRUST_STORE = System.getProperty("javax.net.ssl.trustStore");

    @Override
    public void run() {

        if (StringUtils.isNotEmpty(TRUST_STORE_PASSWORD)) {
            File trustStoreFile = new File(TRUST_STORE);
            FileInputStream localTrustStoreStream;
            try {
                long lastUpdatedTimeStamp = CertificateReLoaderUtil.getLastUpdatedTimeStamp();
                long lastModified = trustStoreFile.lastModified();
                if (lastUpdatedTimeStamp != lastModified) {
                    CertificateReLoaderUtil.setLastUpdatedTimeStamp(lastModified);
                    localTrustStoreStream = new FileInputStream(trustStoreFile);
                    KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                    trustStore.load(localTrustStoreStream, TRUST_STORE_PASSWORD.toCharArray());
                    ServiceReferenceHolder.getInstance().setTrustStore(trustStore);
                }
            } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
                log.error("Unable to find the certificate", e);
            }
        }
    }
}
