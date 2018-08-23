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
package org.wso2.carbon.apimgt.impl.certificatemgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.CertificateInformationDTO;
import org.wso2.carbon.apimgt.api.dto.CertificateMetadataDTO;
import org.wso2.carbon.apimgt.impl.certificatemgt.exceptions.CertificateAliasExistsException;
import org.wso2.carbon.apimgt.impl.certificatemgt.exceptions.CertificateManagementException;
import org.wso2.carbon.apimgt.impl.dao.CertificateMgtDAO;
import org.wso2.carbon.apimgt.impl.utils.CertificateMgtUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;

/**
 * This class holds the implementation of the CertificateManager interface.
 */
public class CertificateManagerImpl implements CertificateManager {

    private static Log log = LogFactory.getLog(CertificateManagerImpl.class);
    private static final String PROFILE_CONFIG = "sslprofiles.xml";
    private static final String CARBON_HOME_STRING = "carbon.home";
    private static final char SEP = File.separatorChar;
    private static String CARBON_HOME = System.getProperty(CARBON_HOME_STRING);
    private static String SSL_PROFILE_FILE_PATH = CARBON_HOME + SEP + "repository" + SEP + "resources" + SEP
            + "security" + SEP + PROFILE_CONFIG;
    private static CertificateMgtDAO certificateMgtDAO = CertificateMgtDAO.getInstance();
    private CertificateMgtUtils certificateMgtUtils = new CertificateMgtUtils();

    @Override
    public ResponseCode addCertificateToParentNode(String certificate, String alias, String endpoint, int tenantId) {

        try {
            if (certificateMgtDAO.addCertificate(alias, endpoint, tenantId)) {
                ResponseCode responseCode = certificateMgtUtils.addCertificateToTrustStore(certificate, alias);
                if (responseCode.getResponseCode() ==
                        ResponseCode.INTERNAL_SERVER_ERROR.getResponseCode()) {
                    log.error("Error adding the certificate to Publisher Trust Store. Rolling back...");
                    certificateMgtDAO.deleteCertificate(alias, endpoint, tenantId);
                } else if (responseCode.getResponseCode() == ResponseCode.ALIAS_EXISTS_IN_TRUST_STORE
                        .getResponseCode()) {
                    log.error("Could not add Certificate to Trust Store. Certificate Exists. Rolling back...");
                    certificateMgtDAO.deleteCertificate(alias, endpoint, tenantId);
                } else if (responseCode.getResponseCode() == ResponseCode.CERTIFICATE_EXPIRED.getResponseCode()) {
                    log.error("Could not add Certificate. Certificate expired.");
                    certificateMgtDAO.deleteCertificate(alias, endpoint, tenantId);
                } else {
                    log.info("Certificate is successfully added to the Publisher client Trust Store with Alias '"
                            + alias + "'");
                }
                return responseCode;
            } else {
                log.error("Error persisting the certificate meta data in db. Certificate could not be added to " +
                        "publisher Trust Store.");
                return ResponseCode.INTERNAL_SERVER_ERROR;
            }
        } catch (CertificateManagementException e) {
            log.error("Error when persisting/ deleting certificate metadata. ", e);
            return ResponseCode.INTERNAL_SERVER_ERROR;
        } catch (CertificateAliasExistsException e) {
            return ResponseCode.ALIAS_EXISTS_IN_TRUST_STORE;
        }
    }

    @Override
    public ResponseCode deleteCertificateFromParentNode(String alias, String endpoint, int tenantId) {

        try {
            boolean removeFromDB = certificateMgtDAO.deleteCertificate(alias, endpoint, tenantId);
            if (removeFromDB) {
                ResponseCode responseCode = certificateMgtUtils.removeCertificateFromTrustStore(alias);
                if (responseCode == ResponseCode.INTERNAL_SERVER_ERROR) {
                    certificateMgtDAO.addCertificate(alias, endpoint, tenantId);
                    log.error("Error removing the Certificate from Trust Store. Rolling back...");
                } else if (responseCode.getResponseCode() == ResponseCode.CERTIFICATE_NOT_FOUND.getResponseCode()) {
                    log.warn("The Certificate for Alias '" + alias + "' has been previously removed from " +
                            "Trust Store. Hence DB entry is removed.");
                } else {
                    log.info("Certificate is successfully removed from the Publisher Trust Store with Alias '"
                            + alias + "'");
                }
                return responseCode;
            } else {
                log.error("Failed to remove certificate from the data base. No certificate changes will be affected.");
                return ResponseCode.INTERNAL_SERVER_ERROR;
            }
        } catch (CertificateManagementException e) {
            log.error("Error persisting/ deleting certificate metadata. ", e);
            return ResponseCode.INTERNAL_SERVER_ERROR;
        } catch (CertificateAliasExistsException e) {
            return ResponseCode.ALIAS_EXISTS_IN_TRUST_STORE;
        }
    }

    @Override
    public boolean addCertificateToGateway(String certificate, String alias) {

        boolean result;
        ResponseCode responseCode = certificateMgtUtils.addCertificateToTrustStore(certificate, alias);
        if (responseCode == ResponseCode.ALIAS_EXISTS_IN_TRUST_STORE) {
            log.info("The Alias '" + alias + "' exists in the Gateway Trust Store.");
            result = true;
        } else {
            result = responseCode != ResponseCode.INTERNAL_SERVER_ERROR;
        }
        result = result && touchConfigFile();
        if (result) {
            log.info("The certificate with Alias '" + alias + "' is successfully added to the Gateway " +
                    "Trust Store.");
        } else {
            log.error("Error adding the certificate with Alias '" + alias + "' to the Gateway Trust Store");
        }
        return result;
    }

    @Override
    public boolean deleteCertificateFromGateway(String alias) {

        ResponseCode responseCode = certificateMgtUtils.removeCertificateFromTrustStore(alias);
        if (responseCode != ResponseCode.INTERNAL_SERVER_ERROR) {
            log.info("The certificate with Alias '" + alias + "' is successfully removed from the Gateway " +
                    "Trust Store.");
        } else {
            log.error("Error removing the certificate with Alias '" + alias + "' from the Gateway " +
                    "Trust Store.");
            return false;
        }
        return touchConfigFile();
    }

    @Override
    public boolean isConfigured() {

        boolean isTableExists = false;
        boolean isFilePresent = new File(SSL_PROFILE_FILE_PATH).exists();
        try {
            isTableExists = certificateMgtDAO.isTableExists();
        } catch (CertificateManagementException e) {
            log.error("Error retrieving database metadata. ", e);
            return false;
        }
        return isFilePresent && isTableExists;
    }

    @Override
    public List<CertificateMetadataDTO> getCertificates(String endpoint, int tenantId) {

        List<CertificateMetadataDTO> certificateMetadataList = null;
        try {
            certificateMetadataList = certificateMgtDAO.getCertificates("", endpoint, tenantId);
        } catch (CertificateManagementException e) {
            log.error("Error when retrieving certificate metadata for endpoint '" + endpoint + "'", e);
        }
        return certificateMetadataList;
    }

    @Override
    public List<CertificateMetadataDTO> getCertificates(int tenantId) {

        List<CertificateMetadataDTO> certificates = null;

        if (log.isDebugEnabled()) {
            log.debug("Get all the certificates for tenant " + tenantId);
        }
        try {
            certificates = certificateMgtDAO.getCertificates(null, null, tenantId);
        } catch (CertificateManagementException e) {
            log.error("Error retrieving certificates for the tenantId '" + tenantId + "' ", e);
        }
        return certificates;
    }

    @Override
    public List<CertificateMetadataDTO> getCertificates(int tenantId, String alias, String endpoint)
            throws APIManagementException {

        List<CertificateMetadataDTO> certificateMetadataList;

        if (log.isDebugEnabled()) {
            log.debug(String.format("Retrieve certificates of tenant %d which matches alias : %s and endpoint : %s",
                    tenantId, alias, endpoint));
        }
        try {
            certificateMetadataList = certificateMgtDAO.getCertificates(alias, endpoint, tenantId);
        } catch (CertificateManagementException e) {
            throw new APIManagementException("Error retrieving certificate information for tenantId '" + tenantId +
                    "' and alias '" + alias + "'");
        }
        return certificateMetadataList;
    }

    @Override
    public boolean isCertificatePresent(int tenantId, String alias) throws APIManagementException {

        List<CertificateMetadataDTO> certificateMetadataList;

        if (log.isDebugEnabled()) {
            log.debug(String.format("Check whether the tenant %d has a certificate for alias %s", tenantId, alias));
        }
        try {
            certificateMetadataList = certificateMgtDAO.getCertificates(alias, null, tenantId);
        } catch (CertificateManagementException e) {
            throw new APIManagementException("Error retrieving certificate information for tenantId '" + tenantId +
                    "' and alias '" + alias + "'");
        }
        return certificateMetadataList.size() == 1; // The list would not be null so we check the size.
    }

    @Override
    public CertificateInformationDTO getCertificateInformation(String alias) throws APIManagementException {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Get Certificate information for alias %s", alias));
        }
        try {
            return certificateMgtUtils.getCertificateInformation(alias);
        } catch (CertificateManagementException e) {
            throw new APIManagementException(e);
        }
    }

    @Override
    public ResponseCode updateCertificate(String certificate, String alias) throws APIManagementException {

        try {
            return certificateMgtUtils.updateCertificate(certificate, alias);
        } catch (CertificateManagementException e) {
            throw new APIManagementException(e);
        }
    }

    @Override
    public int getCertificateCount(int tenantId) throws APIManagementException {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Get the number of certificates tenant %d has.", tenantId));
        }
        try {
            return certificateMgtDAO.getCertificateCount(tenantId);
        } catch (CertificateManagementException e) {
            throw new APIManagementException(e);
        }
    }

    @Override
    public ByteArrayInputStream getCertificateContent(String alias) throws APIManagementException {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Get the contents of the certificate for alias %s", alias));
        }
        try {
            return certificateMgtUtils.getCertificateContent(alias);
        } catch (CertificateManagementException e) {
            throw new APIManagementException(e);
        }
    }

    /**
     * Modify the sslProfiles.xml file after modifying the certificate.
     *
     * @return : True if the file modification is success.
     */
    private boolean touchConfigFile() {

        boolean success = false;
        File file = new File(SSL_PROFILE_FILE_PATH);
        if (file.exists()) {
            success = file.setLastModified(System.currentTimeMillis());
            if (success) {
                log.info("The Transport Sender will be re-initialized in few minutes.");
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Error when modifying the sslprofiles.xml file");
                }
                log.error("Could not modify the file '" + PROFILE_CONFIG + "'");
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("sslprofiles.xml file not found.");
            }
            log.error("Could not find the file '" + PROFILE_CONFIG + "'");
        }
        return success;
    }
}
