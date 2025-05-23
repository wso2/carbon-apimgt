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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.Constants;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.wso2.carbon.apimgt.api.dto.CertificateInformationDTO;
import org.wso2.carbon.apimgt.api.dto.CertificateMetadataDTO;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.certificatemgt.ResponseCode;
import org.wso2.carbon.apimgt.impl.certificatemgt.TrustStoreUtils;
import org.wso2.carbon.apimgt.impl.certificatemgt.exceptions.CertificateManagementException;
import org.wso2.carbon.apimgt.impl.certificatemgt.reloader.CertificateReLoaderUtil;
import org.wso2.carbon.apimgt.impl.dto.TrustStoreDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.wsdl.util.SOAPToRESTConstants;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;
import org.wso2.securevault.commons.MiscellaneousUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * This class holds the utility methods for certificate management.
 */
public class CertificateMgtUtils {

    private static Log log = LogFactory.getLog(CertificateMgtUtils.class);
    private static char[] trustStorePassword = System.getProperty("javax.net.ssl.trustStorePassword").toCharArray();
    private static String trustStoreLocation = System.getProperty("javax.net.ssl.trustStore");
    private static String certificateType = "X.509";
    private static final String KEY_STORE_TYPE = "JKS";
    private static final CertificateMgtUtils instance = new CertificateMgtUtils();
    private static final String COMMON_CERT_NAME = "client-truststore-temp.jks";
    private static final String LISTER_PROFILE_JKS_NAME = "client-truststore-listener.jks";
    public static final String SENDER_PROFILE_JKS_NAME = "client-truststore-sender.ks";

    private CertificateMgtUtils() {

    }

    /**
     * To get the instance of CertificateMgtUtils class.
     *
     * @return instance of {@link CertificateMgtUtils}
     */
    public static CertificateMgtUtils getInstance() {

        return instance;
    }

    public ResponseCode addCertificateToSenderTrustStore(String base64Cert, String alias) {

        try {
            TrustStoreDTO senderProfileTrustStore = getSenderProfileTrustStore();
            return addCertificateToTrustStore(senderProfileTrustStore, base64Cert, alias);

        } catch (FileNotFoundException | XMLStreamException e) {
            log.error("Error reading/ writing to the certificate file.", e);
            return ResponseCode.INTERNAL_SERVER_ERROR;
        }
    }

    public ResponseCode addCertificateToListenerTrustStore(String base64Cert, String alias) {

        try {
            TrustStoreDTO listenerProfileTrustStore = getListenerProfileTrustStore();
            return addCertificateToTrustStore(listenerProfileTrustStore, base64Cert, alias);

        } catch (FileNotFoundException | XMLStreamException e) {
            log.error("Error reading/ writing to the certificate file.", e);
            return ResponseCode.INTERNAL_SERVER_ERROR;
        }
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

        return addCertificateToTrustStore(getParentTrustStore(), base64Cert, alias);
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
    private ResponseCode addCertificateToTrustStore(TrustStoreDTO trustStoreDTO, String base64Cert, String alias) {

        boolean isCertExists = false;
        boolean expired = false;

        try {
            byte[] cert = (Base64.decodeBase64(base64Cert.getBytes(StandardCharsets.UTF_8)));
            try (InputStream serverCert = new ByteArrayInputStream(cert)) {
                if (serverCert.available() == 0) {
                    log.error("Certificate is empty for the provided alias " + alias);
                    return ResponseCode.INTERNAL_SERVER_ERROR;
                }

                //Read the client-truststore.jks into a KeyStore.
                synchronized (this) {
                    File trustStoreFile = new File(trustStoreDTO.getLocation());
                    try (InputStream localTrustStoreStream = new FileInputStream(trustStoreFile)) {
                        KeyStore trustStore = KeyStore.getInstance(trustStoreDTO.getType());
                        TrustStoreUtils.loadCerts(trustStore, trustStoreDTO.getLocation(), trustStoreDTO.getPassword());
                        CertificateFactory cf = CertificateFactory.getInstance(certificateType);
                        while (serverCert.available() > 0) {
                            Certificate certificate = cf.generateCertificate(serverCert);
                            //Check whether the Alias exists in the trust store.
                            if (trustStore.containsAlias(alias)) {
                                isCertExists = true;
                            } else {
                                /*
                                 * If alias is not exists, check whether the certificate is expired or not. If expired
                                 * set the
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
                        try (OutputStream fileOutputStream = new FileOutputStream(trustStoreFile)) {
                            trustStore.store(fileOutputStream, trustStoreDTO.getPassword());
                        }
                        return expired ? ResponseCode.CERTIFICATE_EXPIRED :
                                                        isCertExists ? ResponseCode.ALIAS_EXISTS_IN_TRUST_STORE : ResponseCode.SUCCESS;
                    }
                }
            }
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
     * To validate the current certificate and alias.
     *
     * @param alias       Alias of the certificate.
     * @param certificate Bas64 endcoded certificated.
     * @return response code based on the validation
     */
    public ResponseCode validateCertificate(String alias, int tenantId, String certificate) {

        File trustStoreFile = new File(trustStoreLocation);
        ResponseCode responseCode = ResponseCode.SUCCESS;
        ByteArrayInputStream serverCert = null;

        try {
            synchronized (this) {
                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                try (InputStream localTrustStoreStream = new FileInputStream(trustStoreFile)) {
                    trustStore.load(localTrustStoreStream, trustStorePassword);
                }
                if (StringUtils.isNotEmpty(alias) && trustStore.containsAlias(alias + "_" + tenantId)) {
                    responseCode = ResponseCode.ALIAS_EXISTS_IN_TRUST_STORE;
                }
            }
            if (responseCode != ResponseCode.ALIAS_EXISTS_IN_TRUST_STORE) {
                byte[] cert = (Base64.decodeBase64(certificate.getBytes(StandardCharsets.UTF_8)));
                serverCert = new ByteArrayInputStream(cert);

                if (serverCert.available() == 0) {
                    responseCode = ResponseCode.CERTIFICATE_NOT_FOUND;
                } else {
                    CertificateFactory cf = CertificateFactory.getInstance(certificateType);
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

    public ResponseCode removeCertificateFromListenerTrustStore(String alias) {

        try {
            TrustStoreDTO listenerProfileTrustStore = getListenerProfileTrustStore();
            return removeCertificateFromTrustStore(listenerProfileTrustStore, alias);
        } catch (FileNotFoundException | XMLStreamException e) {
            log.error("Error reading/ writing to the certificate file.", e);
            return ResponseCode.INTERNAL_SERVER_ERROR;
        }
    }

    public ResponseCode removeCertificateFromSenderTrustStore(String alias) {

        try {
            TrustStoreDTO senderProfileTrustStore = getSenderProfileTrustStore();
            return removeCertificateFromTrustStore(senderProfileTrustStore, alias);

        } catch (FileNotFoundException | XMLStreamException e) {
            log.error("Error reading/ writing to the certificate file.", e);
            return ResponseCode.INTERNAL_SERVER_ERROR;
        }
    }

    private ResponseCode removeCertificateFromTrustStore(TrustStoreDTO trustStoreDTO, String alias) {

        boolean isExists; //Check for the existence of the certificate in trust store.
        try {
            synchronized (this) {
                File trustStoreFile = new File(trustStoreDTO.getLocation());
                KeyStore trustStore = KeyStore.getInstance(trustStoreDTO.getType());
                try (InputStream localTrustStoreStream = new FileInputStream(trustStoreFile)) {
                    TrustStoreUtils.loadCerts(trustStore, trustStoreDTO.getLocation(), trustStoreDTO.getPassword());
                }

                if (trustStore.containsAlias(alias)) {
                    trustStore.deleteEntry(alias);
                    isExists = true;
                } else {
                    isExists = false;
                    if (log.isDebugEnabled()) {
                        log.debug("Certificate for alias '" + alias + "' not found in the trust store.");
                    }
                }

                try (OutputStream fileOutputStream = new FileOutputStream(trustStoreFile)) {
                    trustStore.store(fileOutputStream, trustStoreDTO.getPassword());
                }
            }
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

        return removeCertificateFromTrustStore(getParentTrustStore(), alias);
    }

    /**
     * Method to update the certificate which matches the given alias.
     *
     * @param certificate: The base64 encoded certificate string.
     * @param alias        : Alias of the certificate that should be retrieved.
     * @return :
     */
    public synchronized ResponseCode updateCertificate(String certificate, String alias)
                                    throws CertificateManagementException {

        try {
            File trustStoreFile = new File(trustStoreLocation);

            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            try (InputStream localTrustStoreStream = new FileInputStream(trustStoreFile)) {
                trustStore.load(localTrustStoreStream, trustStorePassword);
            }

            if (trustStore.getCertificate(alias) == null) {
                log.error("Could not update the certificate. The certificate for alias '" + alias + "' is not found" +
                        " in the trust store.");
                return ResponseCode.CERTIFICATE_NOT_FOUND;
            }

            //Generate the certificate from the input string.
            byte[] cert = (Base64.decodeBase64(certificate.getBytes(StandardCharsets.UTF_8)));
            Certificate newCertificate;
            try (InputStream certificateStream = new ByteArrayInputStream(cert)) {

                if (certificateStream.available() == 0) {
                    log.error("Certificate is empty for the provided alias " + alias);
                    return ResponseCode.INTERNAL_SERVER_ERROR;
                }

                CertificateFactory certificateFactory = CertificateFactory.getInstance(certificateType);
                newCertificate = certificateFactory.generateCertificate(certificateStream);
            }
            X509Certificate x509Certificate = (X509Certificate) newCertificate;

            if (x509Certificate.getNotAfter().getTime() <= System.currentTimeMillis()) {
                log.error("Could not update the certificate. The certificate expired.");
                return ResponseCode.CERTIFICATE_EXPIRED;
            }
            // If the certificate is not expired, delete the existing certificate and add the new cert.
            trustStore.deleteEntry(alias);
            //Store the certificate in the trust store.
            trustStore.setCertificateEntry(alias, newCertificate);
            try (OutputStream fileOutputStream = new FileOutputStream(trustStoreFile)) {
                trustStore.store(fileOutputStream, trustStorePassword);
            }
        } catch (IOException e) {
            throw new CertificateManagementException("Error updating certificate.", e);
        } catch (CertificateException e) {
            throw new CertificateManagementException("Error generating the certificate.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new CertificateManagementException("Error loading the keystore.", e);
        } catch (KeyStoreException e) {
            throw new CertificateManagementException("Error updating the certificate in the keystore.", e);
        }
        return ResponseCode.SUCCESS;
    }

    /**
     * Method to get the information of the certificate.
     *
     * @param alias : Alias of the certificate which information should be retrieved
     * @return : The details of the certificate as a MAP.
     */
    public synchronized CertificateInformationDTO getCertificateInformation(String alias)
                                    throws CertificateManagementException {

        CertificateInformationDTO certificateInformation = new CertificateInformationDTO();
        File trustStoreFile = new File(trustStoreLocation);
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            try (InputStream localTrustStoreStream = new FileInputStream(trustStoreFile)) {
                trustStore.load(localTrustStoreStream, trustStorePassword);
            }

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
            CertificateFactory cf = CertificateFactory.getInstance(certificateType);
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
     * To get the certificate information from base64 encoded certificate.
     *
     * @param base64EncodedCertificate Base 64 encoded certificate.
     * @return Certificate information.
     */
    public ByteArrayInputStream getCertificateContentFromDB(String base64EncodedCertificate)
            throws CertificateManagementException {

        try {
            byte[] cert = (Base64.decodeBase64(base64EncodedCertificate.getBytes(StandardCharsets.UTF_8)));
            InputStream serverCert = new ByteArrayInputStream(cert);
            if (serverCert.available() == 0) {
                log.error("Provided certificate is empty for getting certificate information. Hence please provide a "
                        + "non-empty certificate to overcome this issue.");
            }
            CertificateFactory cf = CertificateFactory.getInstance(certificateType);
            if (serverCert.available() > 0) {
                Certificate certificate = cf.generateCertificate(serverCert);
                return new ByteArrayInputStream(certificate.getEncoded());
            }
        } catch (IOException | CertificateException e) {
            throw new CertificateManagementException(
                    "Error while getting the certificate information from the certificate", e);
        }
        return null;
    }

    /**
     * Retrieve the certificate which is represented by the given alias.
     *
     * @param alias : The alias of the required certificate.
     * @return : The Certificate as a ByteArrayInputStream.
     * @throws CertificateManagementException :
     */
    public synchronized ByteArrayInputStream getCertificateContent(String alias) throws CertificateManagementException {

        File trustStoreFile = new File(trustStoreLocation);
        Certificate certificate;
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            try (InputStream localTrustStoreStream = new FileInputStream(trustStoreFile)) {
                trustStore.load(localTrustStoreStream, trustStorePassword);
            }

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
                uniqueIdentifier = x509Certificate.getSerialNumber() + "_" + x509Certificate.getSubjectDN();
                uniqueIdentifier = uniqueIdentifier.replaceAll(",", "#").replaceAll("\"", "'")
                        .replaceAll("&(?!amp;)", "&amp;")
                        .replaceAll("<", "&lt;").replaceAll(">", "&gt;");
            }
        } catch (CertificateException e) {
            log.error("Error while getting serial number of the certificate.", e);
        } finally {
            closeStreams(serverCert);
        }
        return uniqueIdentifier;
    }

    private static TrustStoreDTO getListenerProfileTrustStore() throws FileNotFoundException, XMLStreamException {

        String fullPath = getSSLListenerProfilePath();
        if (StringUtils.isNotEmpty(fullPath)) {
            OMElement customSSLProfilesOmElement = new StAXOMBuilder(fullPath).getDocumentElement();
            SecretResolver secretResolver = SecretResolverFactory.create(customSSLProfilesOmElement, true);
            if (customSSLProfilesOmElement != null) {
                Iterator profileIterator = customSSLProfilesOmElement.getChildrenWithLocalName("profile");
                if (profileIterator != null) {
                    OMElement profile = (OMElement) profileIterator.next();
                    while (profileIterator.hasNext()) {
                        OMElement tempProfile = (OMElement) profileIterator.next();
                        OMElement bindAddress = tempProfile.getFirstChildWithName(new QName("bindAddress"));
                        if ("0.0.0.0".equals(bindAddress.getText())) {
                            {
                                profile = tempProfile;
                                break;
                            }
                        }
                    }
                    if (profile != null) {
                        OMElement trustStoreElement = profile.getFirstChildWithName(new QName("TrustStore"));
                        if (trustStoreElement != null) {
                            OMElement location = trustStoreElement.getFirstChildWithName(new QName("Location"));
                            String path = getFullPath(location.getText());
                            OMElement type = trustStoreElement.getFirstChildWithName(new QName("Type"));
                            OMElement passwordElement =
                                    trustStoreElement.getFirstChildWithName(new QName("Password"));
                            String resolvedValue = "";
                            if (passwordElement != null) {
                                resolvedValue = MiscellaneousUtil.resolve(passwordElement, secretResolver);
                            }
                            return new TrustStoreDTO(path, type.getText(), resolvedValue.toCharArray());
                        }
                    }
                }
            }
        }
        return getParentTrustStore();
    }

    private static TrustStoreDTO getSenderProfileTrustStore()
            throws FileNotFoundException, XMLStreamException {

        String fullPath = getSSLSenderProfilePath();
        if (StringUtils.isNotEmpty(fullPath)) {
            OMElement customSSLProfilesOmElement = new StAXOMBuilder(fullPath).getDocumentElement();
            SecretResolver secretResolver = SecretResolverFactory.create(customSSLProfilesOmElement, true);
            if (customSSLProfilesOmElement != null) {
                Iterator profileIterator = customSSLProfilesOmElement.getChildrenWithLocalName("profile");
                if (profileIterator != null) {
                    OMElement profile = (OMElement) profileIterator.next();
                    while (profileIterator.hasNext()) {
                        OMElement tempProfile = (OMElement) profileIterator.next();
                        OMElement servers = tempProfile.getFirstChildWithName(new QName("servers"));
                        if ("*".equals(servers.getText())) {
                            {
                                profile = tempProfile;
                                break;
                            }
                        }
                    }
                    if (profile != null) {
                        OMElement trustStoreElement = profile.getFirstChildWithName(new QName("TrustStore"));
                        if (trustStoreElement != null) {
                            OMElement location = trustStoreElement.getFirstChildWithName(new QName("Location"));
                            String path = getFullPath(location.getText());
                            OMElement type = trustStoreElement.getFirstChildWithName(new QName("Type"));
                            OMElement passwordElement =
                                    trustStoreElement.getFirstChildWithName(new QName("Password"));
                            String resolvedValue = "";
                            if (passwordElement != null) {
                                resolvedValue = MiscellaneousUtil.resolve(passwordElement, secretResolver);
                            }
                            return new TrustStoreDTO(path, type.getText(), resolvedValue.toCharArray());
                        }
                    }
                }
            }
        }
        return getParentTrustStore();
    }

    public static String getSSLSenderProfilePath() {

        AxisConfiguration axisConfiguration =
                ServiceReferenceHolder.getContextService().getServerConfigContext().getAxisConfiguration();
        TransportOutDescription transportOut = axisConfiguration.getTransportOut(APIConstants.HTTPS_PROTOCOL);
        if (transportOut != null && transportOut.getParameter("dynamicSSLProfilesConfig") != null) {
            OMElement dynamicSSLProfilesConfigElement =
                    transportOut.getParameter("dynamicSSLProfilesConfig").getParameterElement();
            if (dynamicSSLProfilesConfigElement != null) {
                OMElement filePathElement =
                        dynamicSSLProfilesConfigElement.getFirstChildWithName(new QName("filePath"));
                if (filePathElement != null) {
                    String sslProfilePath = filePathElement.getText();
                    if (sslProfilePath.contains(".xml")) {
                        return getFullPath(sslProfilePath);
                    }
                }
            }
        }

        return null;
    }

    public static String getSSLListenerProfilePath() {

        AxisConfiguration axisConfiguration =
                ServiceReferenceHolder.getContextService().getServerConfigContext().getAxisConfiguration();
        TransportInDescription transportIn = axisConfiguration.getTransportIn(APIConstants.HTTPS_PROTOCOL);
        if (transportIn != null && transportIn.getParameter("dynamicSSLProfilesConfig") != null) {
            OMElement dynamicSSLProfilesConfigElement =
                    transportIn.getParameter("dynamicSSLProfilesConfig").getParameterElement();
            if (dynamicSSLProfilesConfigElement != null) {
                OMElement filePathElement =
                        dynamicSSLProfilesConfigElement.getFirstChildWithName(new QName("filePath"));
                if (filePathElement != null) {
                    String sslProfilePath = filePathElement.getText();
                    if (sslProfilePath.contains(".xml")) {
                        return getFullPath(sslProfilePath);
                    }
                }
            }
        }
        return null;
    }

    @NotNull
    private static String getFullPath(String sslProfilePath) {

        String separator = sslProfilePath.startsWith(System.getProperty("file.separator")) ?
                "" : System.getProperty("file.separator");
        return System.getProperty("user.dir") + separator + sslProfilePath;
    }

    private static String relativePath(String jksLocation) {

        Path userDirPath = Paths.get(System.getProperty("user.dir"));
        Path fullPath = Paths.get(jksLocation);
        Path relativePath = userDirPath.relativize(fullPath);
        return relativePath.toString();
    }

    public static void backupOriginalTrustStore() throws CertificateManagementException {

        try {
            TrustStoreDTO senderProfileTrustStore = getSenderProfileTrustStore();
            TrustStoreDTO listenerProfileTrustStore = getListenerProfileTrustStore();
            File srcFile = new File(senderProfileTrustStore.getLocation());
            if (senderProfileTrustStore.getLocation().equals(listenerProfileTrustStore.getLocation())) {
                String parent = srcFile.getParent();
                String destPath = parent + File.separator + COMMON_CERT_NAME;
                File destFile = new File(destPath);
                deletePreviousBackupJKSFile(destFile);
                FileUtils.copyFile(srcFile, destFile);
                updateSenderProfileTrustStoreLocation(destPath);
                updateListenerProfileTrustStoreLocation(destPath);
            } else {
                if (srcFile.exists()) {
                    String parent = srcFile.getParent();
                    String destPath = parent + File.separator + SENDER_PROFILE_JKS_NAME;
                    File destFile = new File(destPath);
                    if (!StringUtils.equals(destPath, srcFile.getPath())) {
                        deletePreviousBackupJKSFile(destFile);
                        FileUtils.copyFile(srcFile, destFile);
                        updateSenderProfileTrustStoreLocation(destPath);
                    }
                }
                File listenerProfileTrustStoreFile = new File(listenerProfileTrustStore.getLocation());
                if (listenerProfileTrustStoreFile.exists()) {
                    String parent = listenerProfileTrustStoreFile.getParent();
                    String destPath = parent + File.separator + LISTER_PROFILE_JKS_NAME;
                    File destFile = new File(destPath);
                    deletePreviousBackupJKSFile(destFile);
                    FileUtils.copyFile(listenerProfileTrustStoreFile, destFile);
                    updateListenerProfileTrustStoreLocation(destPath);
                }
            }
        } catch (XMLStreamException | IOException e) {
            throw new CertificateManagementException("Error while backup truststore", e);
        }
    }

    private static TrustStoreDTO getParentTrustStore() {

        return new TrustStoreDTO(trustStoreLocation, KEY_STORE_TYPE, trustStorePassword);
    }

    private static void updateSenderProfileTrustStoreLocation(String jksLocation)
            throws IOException, XMLStreamException {

        String fullPath = getSSLSenderProfilePath();
        if (StringUtils.isNotEmpty(fullPath)) {
            OMElement customSSLProfilesOmElement = new StAXOMBuilder(fullPath).getDocumentElement();
            if (customSSLProfilesOmElement != null) {
                Iterator profileIterator = customSSLProfilesOmElement.getChildrenWithLocalName("profile");
                if (profileIterator != null) {
                    OMElement profile = (OMElement) profileIterator.next();
                    while (profileIterator.hasNext()) {
                        OMElement tempProfile = (OMElement) profileIterator.next();
                        OMElement servers = tempProfile.getFirstChildWithName(new QName("servers"));
                        if ("*".equals(servers.getText())) {
                            {
                                profile = tempProfile;
                                break;
                            }
                        }
                    }
                    if (profile != null) {
                        OMElement trustStoreElement = profile.getFirstChildWithName(new QName("TrustStore"));
                        if (trustStoreElement != null) {
                            OMElement location = trustStoreElement.getFirstChildWithName(new QName("Location"));
                            location.setText(relativePath(jksLocation));
                        }
                    }
                }
                try {
                    String xml = customSSLProfilesOmElement.toString();
                    DocumentBuilderFactory factory = APIUtil.getSecuredDocumentBuilder();
                    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                    factory.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.DISALLOW_DOCTYPE_DECL_FEATURE,
                            true);
                    factory.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE,
                            false);
                    factory.setFeature(Constants.SAX_FEATURE_PREFIX +
                            Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE, false);
                    factory.setNamespaceAware(true);
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document doc = builder.parse(new InputSource(new StringReader(xml)));
                    DOMSource source = new DOMSource(doc);
                    StreamResult result = new StreamResult(new File(fullPath));
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                    transformer.setOutputProperty(SOAPToRESTConstants.SequenceGen.INDENT_PROPERTY,
                            SOAPToRESTConstants.SequenceGen.INDENT_VALUE);
                    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                    transformer.transform(source, result);
                } catch (ParserConfigurationException | TransformerException | SAXException e) {
                    log.error("Error in updating sender profile truststore location.", e);
                }
            }
        }
    }

    private static void updateListenerProfileTrustStoreLocation(String jksLocation)
            throws IOException, XMLStreamException {

        String fullPath = getSSLListenerProfilePath();
        if (StringUtils.isNotEmpty(fullPath)) {
            OMElement customSSLProfilesOmElement = new StAXOMBuilder(fullPath).getDocumentElement();
            if (customSSLProfilesOmElement != null) {
                Iterator profileIterator = customSSLProfilesOmElement.getChildrenWithLocalName("profile");
                if (profileIterator != null) {
                    OMElement profile = (OMElement) profileIterator.next();
                    while (profileIterator.hasNext()) {
                        OMElement tempProfile = (OMElement) profileIterator.next();
                        OMElement bindAddress = tempProfile.getFirstChildWithName(new QName("bindAddress"));
                        if ("0.0.0.0".equals(bindAddress.getText())) {
                            {
                                profile = tempProfile;
                                break;
                            }
                        }
                    }
                    if (profile != null) {
                        OMElement trustStoreElement = profile.getFirstChildWithName(new QName("TrustStore"));
                        if (trustStoreElement != null) {
                            OMElement location = trustStoreElement.getFirstChildWithName(new QName("Location"));
                            location.setText(relativePath(jksLocation));
                        }
                    }
                }

                try (OutputStreamWriter fileWriter = new FileWriter(fullPath)) {
                    customSSLProfilesOmElement.serializeAndConsume(fileWriter);
                }
            }
        }
    }

    private static void deletePreviousBackupJKSFile(File file) {

        if (file.isFile()) {
            if (!file.delete()){
                if (log.isDebugEnabled()) {
                    log.debug("Backup JKS file " + file.getAbsolutePath() + "not deleted successfully");
                }
            }
        }
    }

    public static void startListenerCertificateReLoader() {

        try {
            TrustStoreDTO listenerProfileTrustStore = getListenerProfileTrustStore();
            File trustStoreFile = new File(listenerProfileTrustStore.getLocation());
            try (FileInputStream trustStoreStream = new FileInputStream(trustStoreFile)) {
                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(trustStoreStream, listenerProfileTrustStore.getPassword());
                CertificateReLoaderUtil.setLastUpdatedTimeStamp(trustStoreFile.lastModified());
                CertificateReLoaderUtil.setCertificate(listenerProfileTrustStore);
                CertificateReLoaderUtil.startCertificateReLoader();
                ServiceReferenceHolder.getInstance().setListenerTrustStore(trustStore);
            }
        } catch (IOException | KeyStoreException | CertificateException | NoSuchAlgorithmException | XMLStreamException e) {
            log.error("Error in loading trust store.", e);
        }

    }

    /**
     * Convert javax.security.cert.X509Certificate to java.security.cert.X509Certificate
     *
     * @param cert the certificate to be converted
     * @return java.security.cert.X509Certificate type certificate
     */
    public static Optional<X509Certificate> convert(Certificate cert) {

        if (cert != null) {
            try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(cert.getEncoded())) {

                java.security.cert.CertificateFactory certificateFactory
                        = java.security.cert.CertificateFactory.getInstance("X.509");
                return Optional.of((java.security.cert.X509Certificate) certificateFactory.generateCertificate(
                        byteArrayInputStream));
            } catch (java.security.cert.CertificateException e) {
                log.error("Error while generating the certificate", e);
            } catch (IOException e) {
                log.error("Error while retrieving the encoded certificate", e);
            }
        }
        return Optional.ofNullable(null);
    }

    public void deployTenantCertsToGatewaySenderInABatch(List<CertificateMetadataDTO> certificateMetadataDTOList) {
        //add cert to sender profile truststore
        try {
            TrustStoreDTO trustStoreDTO = getSenderProfileTrustStore();
            addCertificatesToTrustStore(trustStoreDTO, certificateMetadataDTOList);
        } catch (FileNotFoundException | XMLStreamException e) {
            log.error("Error reading/writing to the truststore file.", e);
        } catch (CertificateManagementException e) {
            log.error("Error while storing certificates to the truststore file.", e);
        }
    }

    private void addCertificatesToTrustStore(TrustStoreDTO trustStoreDTO,
                                             List<CertificateMetadataDTO> certificateMetadataDTOList) throws CertificateManagementException {
        //Read the client-truststore.jks into a KeyStore.
        File trustStoreFile = new File(trustStoreDTO.getLocation());
        int loggedInTenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try (InputStream localTrustStoreStream = new FileInputStream(trustStoreFile)) {
            KeyStore trustStore = KeyStore.getInstance(trustStoreDTO.getType());
            trustStore.load(localTrustStoreStream, trustStoreDTO.getPassword());

            String base64Cert, alias;
            for (CertificateMetadataDTO dto : certificateMetadataDTOList) {
                base64Cert = dto.getCertificate();
                alias = dto.getAlias();
                if (loggedInTenantId != MultitenantConstants.SUPER_TENANT_ID) {
                    alias = alias + "_" + loggedInTenantId;
                }

                byte[] cert = (Base64.decodeBase64(base64Cert.getBytes(StandardCharsets.UTF_8)));
                try (InputStream serverCert = new ByteArrayInputStream(cert)) {
                    if (serverCert.available() == 0) {
                        log.error("Certificate is empty for the provided alias " + alias + ". Hence skipping it. ");
                    }

                    CertificateFactory cf = CertificateFactory.getInstance(certificateType);
                    while (serverCert.available() > 0) {
                        Certificate certificate = cf.generateCertificate(serverCert);
                        //Check whether the Alias exists in the trust store.
                        if (trustStore.containsAlias(alias)) {
                            log.info("Provided certificate alias: " + alias + " already exists in the " +
                                    "truststore.");
                        } else {
                            /*
                             * If alias is not exists, check whether the certificate is expired or not. If expired
                             * set the
                             * expired flag.
                             * */
                            X509Certificate x509Certificate = (X509Certificate) certificate;
                            if (x509Certificate.getNotAfter().getTime() <= System.currentTimeMillis()) {
                                log.info("Provided certificate " + alias + " is expired.");
                            } else {
                                //If not expired add the certificate to trust store.
                                trustStore.setCertificateEntry(alias, certificate);
                            }
                        }
                    }
                } catch (CertificateException | KeyStoreException e) {
                    String msg = "Error loading certificate.";
                    log.error(msg, e);
                }
            }

            try (OutputStream fileOutputStream = new FileOutputStream(trustStoreFile)) {
                trustStore.store(fileOutputStream, trustStoreDTO.getPassword());
            }
        } catch (CertificateException e) {
            String msg = "Error storing certificate.";
            log.error(msg, e);
            throw new CertificateManagementException(msg);
        } catch (FileNotFoundException e) {
            String msg = "Error reading/ writing to the certificate file.";
            log.error(msg, e);
            throw new CertificateManagementException(msg);
        } catch (NoSuchAlgorithmException e) {
            String msg = "Could not find the algorithm to load the certificate.";
            log.error(msg, e);
            throw new CertificateManagementException(msg);
        } catch (UnsupportedEncodingException e) {
            String msg = "Error retrieving certificate from String.";
            log.error(msg, e);
            throw new CertificateManagementException(msg);
        } catch (KeyStoreException e) {
            String msg = "Error loading certificate.";
            log.error(msg, e);
            throw new CertificateManagementException(msg);
        } catch (IOException e) {
            String msg = "Error in loading the certificate.";
            log.error(msg, e);
            throw new CertificateManagementException(msg);
        }
    }
}
