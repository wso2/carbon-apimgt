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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.dto.CertificateInformationDTO;
import org.wso2.carbon.apimgt.impl.certificatemgt.ResponseCode;
import org.wso2.carbon.apimgt.impl.certificatemgt.exceptions.CertificateManagementException;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
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
    private static char[] TRUST_STORE_PASSWORD = System.getProperty("javax.net.ssl.trustStorePassword").toCharArray();
    private static String TRUST_STORE = System.getProperty("javax.net.ssl.trustStore");
    private static String CERTIFICATE_TYPE = "X.509";
    private static final String CHARSET_UTF_8 = "UTF-8";
    private static InputStream localTrustStoreStream = null;
    private static OutputStream fileOutputStream = null;
    private static ResponseCode responseCode;
    private static CertificateMgtUtils instance;

    private CertificateMgtUtils(){}

    /**
     * To get the instance of CertificateMgtUtils class.
     *
     * @return instance of {@link CertificateMgtUtils}
     */
    public static CertificateMgtUtils getInstance() {
        if (instance == null) {
            synchronized (CertificateMgtUtils.class) {
                if (instance == null) {
                    instance = new CertificateMgtUtils();
                }
            }
        }
        return instance;
    }

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
        InputStream serverCert = null;
        try {
            //Decode base64 encoded certificate.
            byte[] cert = (Base64.decodeBase64(base64Cert.getBytes(CHARSET_UTF_8)));
            serverCert = new ByteArrayInputStream(cert);
            if (serverCert.available() == 0) {
                log.error("Certificate is empty for the provided alias " + alias);
                return ResponseCode.INTERNAL_SERVER_ERROR;
            }

            //Read the client-truststore.jks into a KeyStore.
            File trustStoreFile = new File(TRUST_STORE);
            localTrustStoreStream = new FileInputStream(trustStoreFile);
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(localTrustStoreStream, TRUST_STORE_PASSWORD);

            CertificateFactory cf = CertificateFactory.getInstance(CERTIFICATE_TYPE);
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
                        if (log.isDebugEnabled()) {
                            log.debug("Provided certificate is expired.");
                        }
                    } else {
                        //If not expired add the certificate to trust store.
                        trustStore.setCertificateEntry(alias, certificate);
                    }
                }
            }
            fileOutputStream = new FileOutputStream(trustStoreFile);
            trustStore.store(fileOutputStream, TRUST_STORE_PASSWORD);
            responseCode = expired ? ResponseCode.CERTIFICATE_EXPIRED :
                    isCertExists ? ResponseCode.ALIAS_EXISTS_IN_TRUST_STORE : ResponseCode.SUCCESS;
        } catch (CertificateException e) {
            log.error("Error loading certificate.", e);
            responseCode = ResponseCode.INTERNAL_SERVER_ERROR;
        } catch (FileNotFoundException e) {
            log.error("Error reading/ writing to the certificate file.", e);
            responseCode = ResponseCode.INTERNAL_SERVER_ERROR;
        } catch (NoSuchAlgorithmException e) {
            log.error("Could not find the algorithm to load the certificate.", e);
            responseCode = ResponseCode.INTERNAL_SERVER_ERROR;
        } catch (UnsupportedEncodingException e) {
            log.error("Error retrieving certificate from String", e);
            responseCode = ResponseCode.INTERNAL_SERVER_ERROR;
        } catch (KeyStoreException e) {
            log.error("Error reading certificate contents.", e);
            responseCode = ResponseCode.INTERNAL_SERVER_ERROR;
        } catch (IOException e) {
            log.error("Error in loading the certificate.", e);
            responseCode = ResponseCode.INTERNAL_SERVER_ERROR;
        } finally {
            closeStreams(localTrustStoreStream, fileOutputStream, serverCert);
        }
        return responseCode;
    }

    /**
     * To validate the current certificate and alias.
     *
     * @param alias       Alias of the certificate.
     * @param certificate Bas64 endcoded certificated.
     * @return response code based on the validation
     */
    public ResponseCode validateCertificate(String alias, int tenantId, String certificate) {
        File trustStoreFile = new File(TRUST_STORE);
        ResponseCode responseCode = ResponseCode.SUCCESS;
        ByteArrayInputStream serverCert = null;

        try {
            localTrustStoreStream = new FileInputStream(trustStoreFile);
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(localTrustStoreStream, TRUST_STORE_PASSWORD);
            if (StringUtils.isNotEmpty(alias) && trustStore.containsAlias(alias + "_" + tenantId)) {
                responseCode = ResponseCode.ALIAS_EXISTS_IN_TRUST_STORE;
            }
            if (responseCode != ResponseCode.ALIAS_EXISTS_IN_TRUST_STORE) {
                byte[] cert = (Base64.decodeBase64(certificate.getBytes(StandardCharsets.UTF_8)));
                serverCert = new ByteArrayInputStream(cert);

                if (serverCert.available() == 0) {
                    responseCode = ResponseCode.CERTIFICATE_NOT_FOUND;
                } else {
                    CertificateFactory cf = CertificateFactory.getInstance(CERTIFICATE_TYPE);
                    while (serverCert.available() > 0) {
                        Certificate generatedCertificate = cf.generateCertificate(serverCert);
                        X509Certificate x509Certificate = (X509Certificate) generatedCertificate;
                        if (x509Certificate.getNotAfter().getTime() <= System.currentTimeMillis()) {
                            responseCode = ResponseCode.CERTIFICATE_EXPIRED;
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("I/O Exception while trying to load trust store while trying to check whether alias " + alias
                    + " exists", e);
            responseCode = ResponseCode.INTERNAL_SERVER_ERROR;
        } catch (CertificateException e) {
            log.error("Certificate Exception while trying to load trust store while trying to check whether alias "
                    + alias + " exists", e);
            responseCode = ResponseCode.INTERNAL_SERVER_ERROR;
        } catch (NoSuchAlgorithmException e) {
            log.error("No Such Algorithm Exception while trying to load trust store while trying to check whether "
                    + "alias " + alias + " exists", e);
            responseCode = ResponseCode.INTERNAL_SERVER_ERROR;
        } catch (KeyStoreException e) {
            log.error("KeyStore Exception while trying to load trust store while trying to check whether alias " + alias
                    + " exists", e);
            responseCode = ResponseCode.INTERNAL_SERVER_ERROR;
        } finally {
            closeStreams(serverCert);
        }
        return responseCode;
    }

    /**
     * This method will remove certificate from the trust store which matches the given alias.
     *
     * @param alias : The alias which the certificate should be deleted.
     * @return : ResponseCode based on the execution.
     * <p>
     * Response Codes
     * SUCCESS : If the certificate is deleted successfully.
     * INTERNAL_SERVER_ERROR : If any exception occurred.
     * CERTIFICATE_NOT_FOUND : If the Alias is not found in the key store.
     */
    public ResponseCode removeCertificateFromTrustStore(String alias) {

        boolean isExists; //Check for the existence of the certificate in trust store.
        try {
            File trustStoreFile = new File(TRUST_STORE);
            localTrustStoreStream = new FileInputStream(trustStoreFile);
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(localTrustStoreStream, TRUST_STORE_PASSWORD);

            if (trustStore.containsAlias(alias)) {
                trustStore.deleteEntry(alias);
                isExists = true;
            } else {
                isExists = false;
                if (log.isDebugEnabled()) {
                    log.debug("Certificate for alias '" + alias + "' not found in the trust store.");
                }
            }

            fileOutputStream = new FileOutputStream(trustStoreFile);
            trustStore.store(fileOutputStream, TRUST_STORE_PASSWORD);
            responseCode = isExists ? ResponseCode.SUCCESS : ResponseCode.CERTIFICATE_NOT_FOUND;
        } catch (IOException e) {
            log.error("Error in loading the certificate.", e);
            responseCode = ResponseCode.INTERNAL_SERVER_ERROR;
        } catch (CertificateException e) {
            log.error("Error loading certificate.", e);
            responseCode = ResponseCode.INTERNAL_SERVER_ERROR;
        } catch (NoSuchAlgorithmException e) {
            log.error("Could not find the algorithm to load the certificate.", e);
            responseCode = ResponseCode.INTERNAL_SERVER_ERROR;
        } catch (KeyStoreException e) {
            log.error("Error reading certificate contents.", e);
            responseCode = ResponseCode.INTERNAL_SERVER_ERROR;
        } finally {
            closeStreams(localTrustStoreStream, fileOutputStream);
        }
        return responseCode;
    }

    /**
     * Method to update the certificate which matches the given alias.
     *
     * @param certificate: The base64 encoded certificate string.
     * @param alias        : Alias of the certificate that should be retrieved.
     * @return :
     */
    public ResponseCode updateCertificate(String certificate, String alias) throws CertificateManagementException {

        InputStream certificateStream = null;
        try {
            File trustStoreFile = new File(TRUST_STORE);
            localTrustStoreStream = new FileInputStream(trustStoreFile);
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(localTrustStoreStream, TRUST_STORE_PASSWORD);

            if (trustStore.getCertificate(alias) == null) {
                log.error("Could not update the certificate. The certificate for alias '" + alias + "' is not found" +
                        " in the trust store.");
                return ResponseCode.CERTIFICATE_NOT_FOUND;
            }

            //Generate the certificate from the input string.
            byte[] cert = (Base64.decodeBase64(certificate.getBytes(CHARSET_UTF_8)));
            certificateStream = new ByteArrayInputStream(cert);

            if (certificateStream.available() == 0) {
                log.error("Certificate is empty for the provided alias " + alias);
                return ResponseCode.INTERNAL_SERVER_ERROR;
            }

            CertificateFactory certificateFactory = CertificateFactory.getInstance(CERTIFICATE_TYPE);
            Certificate newCertificate = certificateFactory.generateCertificate(certificateStream);
            X509Certificate x509Certificate = (X509Certificate) newCertificate;

            if (x509Certificate.getNotAfter().getTime() <= System.currentTimeMillis()) {
                log.error("Could not update the certificate. The certificate expired.");
                return ResponseCode.CERTIFICATE_EXPIRED;
            }
            // If the certificate is not expired, delete the existing certificate and add the new cert.
            trustStore.deleteEntry(alias);
            //Store the certificate in the trust store.
            trustStore.setCertificateEntry(alias, newCertificate);
            fileOutputStream = new FileOutputStream(trustStoreFile);
            trustStore.store(fileOutputStream, TRUST_STORE_PASSWORD);
        } catch (IOException e) {
            throw new CertificateManagementException("Error updating certificate.", e);
        } catch (CertificateException e) {
            throw new CertificateManagementException("Error generating the certificate.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new CertificateManagementException("Error loading the keystore.", e);
        } catch (KeyStoreException e) {
            throw new CertificateManagementException("Error updating the certificate in the keystore.", e);
        } finally {
            closeStreams(fileOutputStream, certificateStream, localTrustStoreStream);
        }
        return ResponseCode.SUCCESS;
    }

    /**
     * Method to get the information of the certificate.
     *
     * @param alias : Alias of the certificate which information should be retrieved
     * @return : The details of the certificate as a MAP.
     */
    public CertificateInformationDTO getCertificateInformation(String alias) throws CertificateManagementException {

        CertificateInformationDTO certificateInformation = new CertificateInformationDTO();
        File trustStoreFile = new File(TRUST_STORE);
        try {
            localTrustStoreStream = new FileInputStream(trustStoreFile);
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(localTrustStoreStream, TRUST_STORE_PASSWORD);

            if (trustStore.containsAlias(alias)) {
                X509Certificate certificate = (X509Certificate) trustStore.getCertificate(alias);
                certificateInformation = getCertificateMetaData(certificate);
            }
        } catch (IOException e) {
            throw new CertificateManagementException("Error wile loading the keystore.", e);
        } catch (CertificateException e) {
            throw new CertificateManagementException("Error loading the keystore from the stream.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new CertificateManagementException("Could not find the algorithm to load the certificate.", e);
        } catch (KeyStoreException e) {
            throw new CertificateManagementException("Error reading certificate contents.", e);
        } finally {
            closeStreams(localTrustStoreStream);
        }
        return certificateInformation;
    }

    /**
     * To get the certificate meta data information such as version expiry data
     *
     * @param certificate Relevant certificate to get certificate meta data information.
     * @return Certificate meta data information.
     */
    private CertificateInformationDTO getCertificateMetaData(X509Certificate certificate) {
        CertificateInformationDTO certificateInformation = new CertificateInformationDTO();
        certificateInformation
                .setStatus(certificate.getNotAfter().getTime() > System.currentTimeMillis() ? "Active" : "Expired");
        certificateInformation.setFrom(certificate.getNotBefore().toString());
        certificateInformation.setTo(certificate.getNotAfter().toString());
        certificateInformation.setSubject(certificate.getSubjectDN().toString());
        certificateInformation.setVersion(String.valueOf(certificate.getVersion()));
        return certificateInformation;
    }

    /**
     * To get the certificate information from base64 encoded certificate.
     *
     * @param base64EncodedCertificate Base 64 encoded certificate.
     * @return Certificate information.
     */
    public CertificateInformationDTO getCertificateInfo(String base64EncodedCertificate) {
        CertificateInformationDTO certificateInformationDTO = null;
        try {
            byte[] cert = (Base64.decodeBase64(base64EncodedCertificate.getBytes(StandardCharsets.UTF_8)));
            InputStream serverCert = new ByteArrayInputStream(cert);
            if (serverCert.available() == 0) {
                log.error("Provided certificate is empty for getting certificate information. Hence please provide a "
                        + "non-empty certificate to overcome this issue.");
            }
            CertificateFactory cf = CertificateFactory.getInstance(CERTIFICATE_TYPE);
            while (serverCert.available() > 0) {
                Certificate certificate = cf.generateCertificate(serverCert);
                certificateInformationDTO = getCertificateMetaData((X509Certificate) certificate);
            }
        } catch (IOException | CertificateException e) {
            log.error("Error while getting the certificate information from the certificate", e);
        }
        return certificateInformationDTO;
    }

    /**
     * Retrieve the certificate which is represented by the given alias.
     *
     * @param alias : The alias of the required certificate.
     * @return : The Certificate as a ByteArrayInputStream.
     * @throws CertificateManagementException :
     */
    public ByteArrayInputStream getCertificateContent(String alias) throws CertificateManagementException {

        File trustStoreFile = new File(TRUST_STORE);
        Certificate certificate;
        try {
            localTrustStoreStream = new FileInputStream(trustStoreFile);
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(localTrustStoreStream, TRUST_STORE_PASSWORD);

            if (trustStore.containsAlias(alias)) {
                certificate = trustStore.getCertificate(alias);
                return new ByteArrayInputStream(certificate.getEncoded());
            }
        } catch (IOException e) {
            throw new CertificateManagementException("Error in loading the certificate.", e);
        } catch (CertificateException e) {
            throw new CertificateManagementException("Error loading certificate.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new CertificateManagementException("Could not find the algorithm to load the certificate.", e);
        } catch (KeyStoreException e) {
            throw new CertificateManagementException("Error reading certificate contents.", e);
        } finally {
            closeStreams(localTrustStoreStream);
        }
        return null;
    }

    /**
     * Closes all the provided streams.
     *
     * @param streams : One or more of streams.
     */
    private void closeStreams(Closeable... streams) {

        try {
            for (Closeable stream : streams) {
                if (stream != null) {
                    stream.close();
                }
            }
        } catch (IOException e) {
            log.error("Error closing the stream.", e);
        }
    }

    /**
     * To get the unique identifier(serialnumber_issuerdn) of the certificate.
     *
     * @param certificate Base64 encoded certificate.
     * @return unique identifier of the certification.
     */
    public String getUniqueIdentifierOfCertificate(String certificate) {
        byte[] cert = (Base64.decodeBase64(certificate.getBytes(StandardCharsets.UTF_8)));
        ByteArrayInputStream serverCert = new ByteArrayInputStream(cert);
        String uniqueIdentifier = null;
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            while (serverCert.available() > 0) {
                Certificate generatedCertificate = cf.generateCertificate(serverCert);
                X509Certificate x509Certificate = (X509Certificate) generatedCertificate;
                uniqueIdentifier = String
                        .valueOf(x509Certificate.getSerialNumber() + "_" + x509Certificate.getIssuerDN());
                uniqueIdentifier = uniqueIdentifier.replaceAll(",", "#").replaceAll("\"", "'");
            }
        } catch (CertificateException e) {
            log.error("Error while getting serial number of the certificate.", e);
        } finally {
            closeStreams(serverCert);
        }
        return uniqueIdentifier;
    }
}
