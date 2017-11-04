/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.certificatemgt.ResponseCode;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * This class holds the utility methods for certificate management.
 */
public class CertificateMgtUtils {
    private static Log log = LogFactory.getLog(CertificateMgtUtils.class);
    private static String TRUST_STORE_PASSWORD = System.getProperty("javax.net.ssl.trustStorePassword");
    private static String TRUST_STORE = System.getProperty("javax.net.ssl.trustStore");

    /**
     * This method generates a certificate from a base64 encoded certificate string and add to the configured trust
     * store.
     *
     * @param base64Cert : The base 64 encoded string of the server certificate.
     * @param alias      : The alias for the certificate.
     * @return : ResponseCode which matches the execution result.
     *
     * Response Codes.
     * SUCCESS : If certificate added successfully.
     * INTERNAL_SERVER_ERROR : If any internal error occurred
     * ALIAS_EXISTS_IN_TRUST_STORE : If the alias exists in trust store.
     * CERTIFICATE_EXPIRED : If the given certificate is expired.
     */
    public ResponseCode addCertificateToTrustStore(String base64Cert, String alias) {
        boolean isCertExists = false;
        boolean expired = false;
        try {
            //Decode base64 encoded certificate.
            byte[] cert = (Base64.decodeBase64(base64Cert.getBytes("UTF-8")));
            InputStream serverCert = new ByteArrayInputStream(cert);

            //Read the client-truststore.jks into a KeyStore.
            File trustStoreFile = new File(TRUST_STORE);
            InputStream localTrustStoreStream = new FileInputStream(trustStoreFile);
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(localTrustStoreStream, TRUST_STORE_PASSWORD.toCharArray());

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            while (serverCert.available() > 0) {
                Certificate certificate = cf.generateCertificate(serverCert);
                //Check whether the Alias exists in the trust store.
                if (trustStore.containsAlias(alias)) {
                    isCertExists = true;
                } else {
                    /*
                    * If alias is not exists, check whether the certificate is expired or not. If expired set the
                    * expired flag.
                    * */
                    X509Certificate x509Certificate = (X509Certificate) certificate;
                    if (x509Certificate.getNotAfter().getTime() <= System.currentTimeMillis()) {
                        expired = true;
                    } else {
                        //If not expired add the certificate to trust store.
                        trustStore.setCertificateEntry(alias, certificate);
                    }
                }
            }

            serverCert.close();
            OutputStream out = new FileOutputStream(trustStoreFile);
            trustStore.store(out, TRUST_STORE_PASSWORD.toCharArray());
            out.close();
            return expired ? ResponseCode.CERTIFICATE_EXPIRED :
                    isCertExists ? ResponseCode.ALIAS_EXISTS_IN_TRUST_STORE : ResponseCode.SUCCESS;
        } catch (CertificateException e) {
            log.error("Error loading certificate.", e);
            return ResponseCode.INTERNAL_SERVER_ERROR;
        } catch (FileNotFoundException e) {
            log.error("Error reading/ writing to the certificate file.", e);
            return ResponseCode.INTERNAL_SERVER_ERROR;
        } catch (NoSuchAlgorithmException e) {
            log.error("Could not find the algorithm to load the certificate.", e);
            return ResponseCode.INTERNAL_SERVER_ERROR;
        } catch (UnsupportedEncodingException e) {
            log.error("Error retrieving certificate from String", e);
            return ResponseCode.INTERNAL_SERVER_ERROR;
        } catch (KeyStoreException e) {
            log.error("Error reading certificate contents.", e);
            return ResponseCode.INTERNAL_SERVER_ERROR;
        } catch (IOException e) {
            log.error("Error in loading the certificate.", e);
            return ResponseCode.INTERNAL_SERVER_ERROR;
        }
    }

    /**
     * This method will remove certificate from the trust store which matches the given alias.
     *
     * @param alias : The alias which the certificate should be deleted.
     * @return : ResponseCode based on the execution.
     *
     * Response Codes
     * SUCCESS : If the certificate is deleted successfully.
     * INTERNAL_SERVER_ERROR : If any exception occurred.
     * CERTIFICATE_NOT_FOUND : If the Alias is not found in the key store.
     */
    public ResponseCode removeCertificateFromTrustStore(String alias) {
        boolean isExists;
        try {
            File trustStoreFile = new File(TRUST_STORE);
            InputStream localTrustStoreStream = new FileInputStream(trustStoreFile);
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(localTrustStoreStream, TRUST_STORE_PASSWORD.toCharArray());

            if (trustStore.containsAlias(alias)) {
                trustStore.deleteEntry(alias);
                isExists = true;
            } else {
                isExists = false;
            }

            OutputStream out = new FileOutputStream(trustStoreFile);
            trustStore.store(out, TRUST_STORE_PASSWORD.toCharArray());
            out.close();
            return isExists ? ResponseCode.SUCCESS : ResponseCode.CERTIFICATE_NOT_FOUND;
        } catch (IOException e) {
            log.error("Error in loading the certificate.", e);
            return ResponseCode.INTERNAL_SERVER_ERROR;
        } catch (CertificateException e) {
            log.error("Error loading certificate.", e);
            return ResponseCode.INTERNAL_SERVER_ERROR;
        } catch (NoSuchAlgorithmException e) {
            log.error("Could not find the algorithm to load the certificate.", e);
            return ResponseCode.INTERNAL_SERVER_ERROR;
        } catch (KeyStoreException e) {
            log.error("Error reading certificate contents.", e);
            return ResponseCode.INTERNAL_SERVER_ERROR;
        }
    }
}
