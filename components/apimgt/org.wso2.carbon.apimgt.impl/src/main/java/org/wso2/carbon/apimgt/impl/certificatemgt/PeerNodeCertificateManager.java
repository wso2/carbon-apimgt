/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com/).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.certificatemgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.dto.CertificateMetadataDTO;
import org.wso2.carbon.apimgt.impl.certificatemgt.exceptions.CertificateManagementException;
import org.wso2.carbon.apimgt.impl.dao.CertificateMgtDAO;
import org.wso2.carbon.apimgt.impl.utils.CertificateMgtUtils;

/**
 * This class is responsible for managing certificates for peer nodes in a specific context(Active-Active Deployment).
 *
 * NOTE: This class is designed for use only within this specific implementation. Do not reuse or adapt this class or
 * its methods for any other contexts or implementations.
 */
public class PeerNodeCertificateManager {

    private static final Log log = LogFactory.getLog(PeerNodeCertificateManager.class);
    private static final PeerNodeCertificateManager instance = new PeerNodeCertificateManager();

    private PeerNodeCertificateManager() {
    }

    public static PeerNodeCertificateManager getInstance() {
        return instance;
    }

    /**
     * Adds the certificate to the trust stores of peer nodes.
     *
     * This method is invoked when the ENDPOINT_CERTIFICATE_ADD event is received by a peer node. It ensures that the
     * certificate is added to the trust stores of the peer nodes.
     *
     * NOTE: This method is designed for use only within this specific implementation. Do not reuse or adapt this method
     * for any other contexts or implementations.
     *
     * @param alias    The alias of the certificate.
     * @param endpoint The endpoint associated with the certificate.
     * @param tenantId The tenant identifier.
     */
    public void addCertificateToPeerNode(String alias, String endpoint, int tenantId) {

        if (log.isDebugEnabled()) {
            log.debug("Adding certificate to peer node trust store. Alias: " + alias + ", Endpoint: " + endpoint +
                    ", TenantId: " + tenantId);
        }
        try {
            CertificateMetadataDTO certificateMetadataDTO = CertificateMgtDAO.getInstance().getCertificate(alias,
                    endpoint, tenantId);
            ResponseCode responseCode = CertificateMgtUtils.getInstance().addCertificateToTrustStore(certificateMetadataDTO.
                    getCertificate(), certificateMetadataDTO.getAlias());
            switch (responseCode) {
                case SUCCESS:
                    if (log.isDebugEnabled()) {
                        log.debug("Certificate added to trust store. alias='" + alias + "', endpoint='" + endpoint + 
                                "', tenantId=" + tenantId);
                    }
                    break;
                case ALIAS_EXISTS_IN_TRUST_STORE:
                    if (log.isDebugEnabled()) {
                        log.debug("Certificate already exists in trust store. alias='" + alias + "', endpoint='" + 
                                endpoint + "', tenantId=" + tenantId);
                    }
                    break;
                case CERTIFICATE_EXPIRED:
                    log.warn("Certificate expired; skipping add. alias='" + alias + "', endpoint='" + endpoint + 
                            "', tenantId=" + tenantId);
                    break;
                default:
                    log.error("Failed to add certificate to trust store. alias='" + alias + "', endpoint='" + 
                            endpoint + "', tenantId=" + tenantId + ", responseCode=" + responseCode);
            }
        } catch (CertificateManagementException e) {
            log.error("Error when fetching certificate metadata for alias " + alias + " from database.", e);
        }
    }

    /**
     * Deletes the certificate from the trust stores of peer nodes.
     *
     * This method is invoked when the ENDPOINT_CERTIFICATE_REMOVE event is received by a peer node. It ensures that
     * the certificate identified by the provided alias is removed from the trust stores of the peer nodes.
     *
     * NOTE: This method is designed for use only within this specific implementation. Do not reuse or adapt this method
     * for any other contexts or implementations.
     *
     * @param alias The alias of the certificate to be removed.
     */
    public void deleteCertificateFromPeerNode(String alias) {

        if (log.isDebugEnabled()) {
            log.debug("Removing certificate from peer node trust store. Alias: " + alias);
        }
        ResponseCode responseCode = CertificateMgtUtils.getInstance().removeCertificateFromTrustStore(alias);
        switch (responseCode) {
            case SUCCESS:
                if (log.isDebugEnabled()) {
                    log.debug("Certificate removed from trust store. alias='" + alias + "'.");
                }
                break;
            case CERTIFICATE_NOT_FOUND:
                if (log.isDebugEnabled()) {
                    log.debug("Certificate not found in trust store. Treating as no-op. alias='" + alias + "'.");
                }
                break;
            default:
                log.error("Failed to remove certificate from trust store. alias='" + alias + "', responseCode=" + responseCode);
        }
    }
}
