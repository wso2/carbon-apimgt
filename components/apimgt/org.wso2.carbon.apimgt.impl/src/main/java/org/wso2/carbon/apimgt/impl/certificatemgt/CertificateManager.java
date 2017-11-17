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

import org.wso2.carbon.apimgt.api.dto.CertificateMetadataDTO;

import java.util.List;

/**
 * Interface to mange the server certificates.
 * This interface contains methods that will be used by both publisher and gateway nodes.
 */
public interface CertificateManager {

    /**
     * Method to add certificate to publisher node.
     *
     * @param certificate : Base64 encoded certificate string.
     * @param alias       : Alias for the certificate.
     * @param endpoint    : The endpoint which the certificate will be mapped to.
     * @param tenantId    : The tenant id which the certificate is belongs to.
     * @return :    SUCCESS : If Operation executed successfully
     *              INTERNAL_SERVER_ERROR : If any internal error occurred.
     *              ALIAS_EXISTS_IN_TRUST_STORE : If the alias already present in the trust store.
     *              CERTIFICATE_EXPIRED : If the certificate is expired.
     *              CERTIFICATE_FOR_ENDPOINT_EXISTS : If the endpoint exists in the database.
     */
    public ResponseCode addCertificateToParentNode(String certificate, String alias, String endpoint, int tenantId);

    /**
     * Method to delete certificate from publisher trust store.
     *
     * @param alias    : Alias of the certificate which needs to be removed.
     * @param endpoint : The endpoint which the certificate is mapped to.
     * @param tenantId : The owner tenant id.
     * @return :    SUCCESS: If operation success
     *              INTERNAL_SERVER_ERROR: If any internal error occurred
     *              CERTIFICATE_NOT_FOUND : If Certificate is not found in the trust store.
     */
    public ResponseCode deleteCertificateFromParentNode(String alias, String endpoint, int tenantId);

    /**
     * Method to add the certificate to gateway nodes.
     *
     * @param certificate : The Base64 encoded certificate string.
     * @param alias       : Certificate alias.
     * @return : True if the certificate is added to gateway node successfully. False otherwise.
     */
    public boolean addCertificateToGateway(String certificate, String alias);

    /**
     * This method is to remove the certificate from client-truststore.jks of gateway nodes.
     *
     * @param alias : The alias of the certificate to be removed.
     * @return : True if the certificate is removed successfully, false otherwise.
     */
    public boolean deleteCertificateFromGateway(String alias);

    /**
     * This method is to check whether the API-Manager is configured for Certificate Management feature.
     *
     * @return : True if configured else false.
     */
    public boolean isConfigured();

    /**
     * This method will return the Certificate Metadata object which maps to the endpoint and belongs to the provided
     * tenant.
     *
     * @param endpoint : The endpoint to which the certificate is mapped.
     * @param tenantId : The Id of the tenant that endpoint belongs to.
     * @return CertificateMetadataDTO object which contains the certificate meta data.
     */
    public CertificateMetadataDTO getCertificate(String endpoint, int tenantId);

    /**
     * This method is used to retrieve all the certificates which belong to the given tenant.
     *
     * @param tenantId : The id of the tenant which the certificates should be retrieved.
     * @return : List of Certificate metadata objects.
     */
    public List<CertificateMetadataDTO> getCertificates(int tenantId);
}
