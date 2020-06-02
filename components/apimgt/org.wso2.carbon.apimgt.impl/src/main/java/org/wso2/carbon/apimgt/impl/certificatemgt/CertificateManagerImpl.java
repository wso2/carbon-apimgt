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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.Constants;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.CertificateInformationDTO;
import org.wso2.carbon.apimgt.api.dto.CertificateMetadataDTO;
import org.wso2.carbon.apimgt.api.dto.ClientCertificateDTO;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.certificatemgt.exceptions.CertificateAliasExistsException;
import org.wso2.carbon.apimgt.impl.certificatemgt.exceptions.CertificateManagementException;
import org.wso2.carbon.apimgt.impl.dao.CertificateMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.CertificateMgtUtils;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.xml.namespace.QName;
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
    private static String listenerProfileFilePath;
    private static CertificateMgtDAO certificateMgtDAO = CertificateMgtDAO.getInstance();
    private CertificateMgtUtils certificateMgtUtils = CertificateMgtUtils.getInstance();
    private static boolean isMTLSConfigured = false;
    private static CertificateManager instance;

    /**
     * To get the instance of certificate manager.
     *
     * @return instance of certificate manager.
     */
    public static CertificateManager getInstance() {
        if (instance == null) {
            synchronized (CertificateManagerImpl.class) {
                if (instance == null) {
                    instance = new CertificateManagerImpl();
                }
            }
        }
        return instance;
    }

    /**
     * Initializes an instance of CertificateManagerImpl.
     */
    private CertificateManagerImpl() {
        String isMutualTLSConfigured = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration().getFirstProperty(APIConstants.ENABLE_MTLS_FOR_APIS);
        if (StringUtils.isNotEmpty(isMutualTLSConfigured) && isMutualTLSConfigured.equalsIgnoreCase("true")) {
            isMTLSConfigured = true;
        }
        if (isMTLSConfigured) {
            if (log.isDebugEnabled()) {
                log.debug("Mutual TLS based security is enabled for APIs. Hence APIs can be secured using mutual TLS "
                        + "and OAuth2");
            }
            TransportInDescription transportInDescription = ServiceReferenceHolder.getContextService()
                    .getServerConfigContext().getAxisConfiguration().getTransportIn(Constants.TRANSPORT_HTTPS);
            Parameter profilePathParam = transportInDescription.getParameter("dynamicSSLProfilesConfig");
            if (profilePathParam == null) {
                listenerProfileFilePath = null;
            } else {
                OMElement pathEl = profilePathParam.getParameterElement();
                String path = pathEl.getFirstChildWithName(new QName("filePath")).getText();
                if (path != null) {
                    String separator = path.startsWith(File.separator) ? "" : File.separator;
                    listenerProfileFilePath = System.getProperty("user.dir") + separator + path;
                }
            }
        }
        if (log.isDebugEnabled()) {
            if (isMTLSConfigured) {
                log.debug("Mutual SSL based authentication is supported for this server.");
            } else {
                log.debug("Mutual SSL based authentication is not supported for this server.");
            }
        }
    }

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
                    log.error("Could not add Certificate. Certificate has already expired.");
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
    public ResponseCode addClientCertificate(APIIdentifier apiIdentifier, String certificate, String alias,
            String tierName, int tenantId) {
        ResponseCode responseCode;
        try {
            responseCode = certificateMgtUtils.validateCertificate(alias, tenantId, certificate);
            if (responseCode == ResponseCode.SUCCESS) {
                if (certificateMgtDAO.checkWhetherAliasExist(alias, tenantId)) {
                    responseCode = ResponseCode.ALIAS_EXISTS_IN_TRUST_STORE;
                } else {
                    certificateMgtDAO.addClientCertificate(certificate, apiIdentifier, alias, tierName, tenantId, null);
                }
            }
        } catch (CertificateManagementException e) {
            log.error("Error when adding client certificate with alias " + alias + " to database for the API "
                    + apiIdentifier.toString(), e);
            responseCode = ResponseCode.INTERNAL_SERVER_ERROR;
        }
        return responseCode;
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
    public ResponseCode deleteClientCertificateFromParentNode(APIIdentifier apiIdentifier, String alias, int tenantId) {
        try {
            boolean removeFromDB = certificateMgtDAO.deleteClientCertificate(apiIdentifier, alias, tenantId, null);
            if (removeFromDB) {
                return ResponseCode.SUCCESS;
            } else {
                log.error("Failed to remove certificate with alias " + alias + " from the database for the API "
                        + apiIdentifier + "  No certificate changes will be affected.");
                return ResponseCode.INTERNAL_SERVER_ERROR;
            }
        } catch (CertificateManagementException e) {
            log.error(
                    "Error while deleting certificate metadata of the alias " + alias + " of the API " + apiIdentifier,
                    e);
            return ResponseCode.INTERNAL_SERVER_ERROR;
        }
    }

    @Override
    public boolean addCertificateToGateway(String certificate, String alias) {
        // Check whether the api is invoked via the APIGatewayAdmin service.
        int loggedInTenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (loggedInTenantId != MultitenantConstants.SUPER_TENANT_ID) {
            alias = alias + "_" + loggedInTenantId;
        }
        return addCertificateToListenerOrSenderProfile(certificate, alias, false);
    }


    @Override
    public boolean addClientCertificateToGateway(String certificate, String alias) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        /*
        Tenant ID is appended with alias to make sure, only the admins from the same tenant, can delete the
        certificates later.
         */
        if (alias.endsWith("_" + tenantId) || tenantId == org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_ID) {
            return addCertificateToListenerOrSenderProfile(certificate, alias, true);
        } else {
            log.warn("Attempt to add an alias " + alias + " by tenant " + tenantId + " has been rejected. Please "
                    + "make sure to provide a alias name that ends with '_" + tenantId + "' .");
            return false;
        }
    }

    /**
     * To add the public certificate to the relevant listener of sender profile.
     *
     * @param certificate Relevant certificate that need to be added to the trust store of gateway.
     * @param alias       Alias of the certificate.
     * @param isListener  To indicate whether the listener profile need to be reloaded.
     * @return true if the addition to gateway certificate addition succeeded.
     */
    private  boolean addCertificateToListenerOrSenderProfile(String certificate, String alias, boolean isListener) {
        boolean result;
        ResponseCode responseCode = certificateMgtUtils.addCertificateToTrustStore(certificate, alias);
        if (responseCode == ResponseCode.ALIAS_EXISTS_IN_TRUST_STORE) {
            log.info("The Alias '" + alias + "' exists in the Gateway Trust Store.");
            result = true;
        } else {
            result = responseCode != ResponseCode.INTERNAL_SERVER_ERROR;
        }
        boolean fileUpdateSucceed;
        if (isListener) {
            fileUpdateSucceed = touchSSLListenerConfigFile();
        } else {
            fileUpdateSucceed = touchSSLSenderConfigFile();
        }
        result = result && fileUpdateSucceed;
        if (result) {
            log.info("The certificate with Alias '" + alias + "' is successfully added to the Gateway "
                    + "Trust Store.");
        } else {
            log.error("Error adding the certificate with Alias '" + alias + "' to the Gateway Trust Store");
        }
        return result;
    }

    @Override
    public boolean deleteCertificateFromGateway(String alias) {
        // Check whether the api is invoked via the APIGatewayAdmin service.
        int loggedInTenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (loggedInTenantId != MultitenantConstants.SUPER_TENANT_ID) {
            alias = alias + "_" + loggedInTenantId;
        }
        return deleteCertificateFromListenerAndSenderProfiles(alias, false);
    }

    @Override
    public boolean deleteClientCertificateFromGateway(String alias) {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        /*
            Tenant ID is checked to make sure that tenant admins cannot delete the alias that do not belong their
            tenant. Super tenant is special cased, as it is required to delete the certificates from different tenants.
         */
        if (alias.endsWith("_" + tenantId) || tenantId == org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_ID) {
            return deleteCertificateFromListenerAndSenderProfiles(alias, true);
        } else {
            log.warn("Attempt to delete the alias " + alias + " by tenant " + tenantId + " has been rejected. Only "
                    + "the client certificates that belongs to " + tenantId + " can be deleted. All the client "
                    + "certificates belongs to " + tenantId + " have '_" + tenantId + "' suffix in alias");
            return false;
        }
    }

    /**
     * To delete the certificate from http listener or sender profile.
     *
     * @param alias      Alias that need to be removed.
     * @param isListener To indicate whether http listener need to be updated or sender.
     * @return true if the the update of profile succeeded.
     */
    private boolean deleteCertificateFromListenerAndSenderProfiles(String alias, boolean isListener) {
        ResponseCode responseCode = certificateMgtUtils.removeCertificateFromTrustStore(alias);
        if (responseCode != ResponseCode.INTERNAL_SERVER_ERROR) {
            log.info("The certificate with Alias '" + alias + "' is successfully removed from the Gateway "
                    + "Trust Store.");
        } else {
            log.error("Error removing the certificate with Alias '" + alias + "' from the Gateway " + "Trust Store.");
            return false;
        }
        if (isListener) {
            return touchSSLListenerConfigFile();
        } else {
            return touchSSLSenderConfigFile();
        }
    }

    @Override
    public boolean isConfigured() {
        boolean isFilePresent = new File(SSL_PROFILE_FILE_PATH).exists();
        return isFilePresent;
    }

    @Override
    public boolean isClientCertificateBasedAuthenticationConfigured() {
        return isMTLSConfigured;
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
    public List<ClientCertificateDTO> searchClientCertificates(int tenantId, String alias,
            APIIdentifier apiIdentifier) throws APIManagementException {
        try {
            return CertificateMgtDAO.getInstance().getClientCertificates(tenantId, alias, apiIdentifier);
        } catch (CertificateManagementException e) {
            throw new APIManagementException(
                    "Error while retrieving client certificate information for the tenant : " + tenantId, e);
        }
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
    public ResponseCode updateClientCertificate(String certificate, String alias, String tier, int tenantId)
            throws APIManagementException {
        ResponseCode responseCode = ResponseCode.SUCCESS;
        if (StringUtils.isNotEmpty(certificate)) {
            responseCode = certificateMgtUtils.validateCertificate(null, tenantId, certificate);
        }
        try {
            if (responseCode.getResponseCode() == ResponseCode.SUCCESS.getResponseCode()) {
                boolean isSuccess = certificateMgtDAO.updateClientCertificate(certificate, alias, tier, tenantId);
                if (isSuccess) {
                    responseCode = ResponseCode.SUCCESS;
                } else {
                    responseCode = ResponseCode.INTERNAL_SERVER_ERROR;
                }
            }
        } catch (CertificateManagementException e) {
            throw new APIManagementException(
                    "Certificate management exception while trying to update the certificate of alias " + alias
                            + " of tenant " + tenantId, e);
        }
        return responseCode;
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
    public int getClientCertificateCount(int tenantId) throws APIManagementException {
        try {
            return certificateMgtDAO.getClientCertificateCount(tenantId);
        } catch (CertificateManagementException e) {
            throw new APIManagementException(
                    "Certificate management exception while getting count of client certificates of the tenant "
                            + tenantId, e);
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
    private boolean touchSSLSenderConfigFile() {

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


    /**
     * Modify the listenerProfiles.xml file after modifying the certificate.
     *
     * @return : True if the file modification is success.
     */
    private boolean touchSSLListenerConfigFile() {
        boolean success = false;
        if (listenerProfileFilePath != null) {
            File file = new File(listenerProfileFilePath);
            if (file.exists()) {
                success = file.setLastModified(System.currentTimeMillis());
                if (success) {
                    log.info("The Transport listener will be re-initialized in few minutes.");
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug(
                                "Error when modifying listener profile config file in path " + listenerProfileFilePath);
                    }
                    log.error("Could not modify the file listener profile config file");
                }
            }
        } else {
            log.warn("Mutual SSL file path for listener is not configured correctly in axis2.xml. Please recheck the "
                    + "relevant configuration under transport listener.");
        }
        return success;
    }
}
