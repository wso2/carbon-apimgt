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

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.CertificateInformationDTO;
import org.wso2.carbon.apimgt.api.dto.CertificateMetadataDTO;
import org.wso2.carbon.apimgt.api.dto.ClientCertificateDTO;
import org.wso2.carbon.apimgt.api.model.Identifier;

import java.io.ByteArrayInputStream;
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
     * INTERNAL_SERVER_ERROR : If any internal error occurred.
     * ALIAS_EXISTS_IN_TRUST_STORE : If the alias already present in the trust store.
     * CERTIFICATE_EXPIRED : If the certificate is expired.
     * CERTIFICATE_FOR_ENDPOINT_EXISTS : If the endpoint exists in the database.
     */
    ResponseCode addCertificateToParentNode(String certificate, String alias, String endpoint, int tenantId);

    /**
     * Method to delete certificate from publisher trust store.
     *
     * @param alias    : Alias of the certificate which needs to be removed.
     * @param endpoint : The endpoint which the certificate is mapped to.
     * @param tenantId : The owner tenant id.
     * @return :    SUCCESS: If operation success
     * INTERNAL_SERVER_ERROR: If any internal error occurred
     * CERTIFICATE_NOT_FOUND : If Certificate is not found in the trust store.
     */
    ResponseCode deleteCertificateFromParentNode(String alias, String endpoint, int tenantId);

    /**
     * Method to add the certificate to gateway nodes.
     *
     * @param certificate : The Base64 encoded certificate string.
     * @param alias       : Certificate alias.
     * @return : True if the certificate is added to gateway node successfully. False otherwise.
     */
    boolean addCertificateToGateway(String certificate, String alias);

    /**
     * This method is to remove the certificate from client-truststore.jks of gateway nodes.
     *
     * @param alias : The alias of the certificate to be removed.
     * @return : True if the certificate is removed successfully, false otherwise.
     */
    boolean deleteCertificateFromGateway(String alias);

    /**
     * This method is to check whether the API-Manager is configured for Certificate Management feature.
     *
     * @return : True if configured else false.
     */
    boolean isConfigured();

    /**
     * This method will return the Certificate Metadata object which maps to the alias and belongs to the provided
     * tenant.
     *
     * @param alias : The alias to which the certificate is mapped.
     * @param tenantId : The Id of the tenant that endpoint belongs to.
     * @return CertificateMetadataDTO object which contains the certificate meta data.
     */
    CertificateMetadataDTO getCertificate(String alias, int tenantId);

    /**
     * This method will return the Certificate Metadata object which maps to the endpoint and belongs to the provided
     * tenant.
     *
     * @param endpoint : The endpoint to which the certificate is mapped.
     * @param tenantId : The Id of the tenant that endpoint belongs to.
     * @return CertificateMetadataDTO object which contains the certificate meta data.
     */
    @Deprecated
    List<CertificateMetadataDTO> getCertificates(String endpoint, int tenantId);

    /**
     * This method is used to retrieve all the certificates which belong to the given tenant.
     *
     * @param tenantId : The id of the tenant which the certificates should be retrieved.
     * @return : List of Certificate metadata objects.
     */
    List<CertificateMetadataDTO> getCertificates(int tenantId);

    /**
     * This method is used to search the certificate metadata based on the given parameters.
     *
     * @param tenantId : The id of the tenant which the certificates are belong to.
     * @param alias    : Alias of the certificate
     * @param endpoint : The endpoint which the certificate is mapped to.
     * @return : If any matching certificates is found, it will be returned as an array list. Otherwise and empty
     * array list will be returned.
     */
    List<CertificateMetadataDTO> getCertificates(int tenantId, String alias, String endpoint) throws APIManagementException;

    /**
     * Check whether a certificate for the given alias is present in the database.
     *
     * @param tenantId: The Id of the tenant
     * @param alias     : The alias of the certificate
     * @return : True if the certificate is present. False otherwise.
     */
    boolean isCertificatePresent(int tenantId, String alias) throws APIManagementException;

    /**
     * Method to retrieve the properties (expiry date etc) of the certificate which matches the given alias.
     *
     * @param tenantId : The id of the tenant.
     * @param alias       : The alias of the certificate
     * @return : The common information of the certificate.
     * @throws APIManagementException :
     */
    CertificateInformationDTO getCertificateInformation(int tenantId, String alias) throws APIManagementException;

    /**
     * Method to update an existing certificate.
     *
     * @param certificate : The base64 encoded certificate string.
     * @param alias       : The alias of the certificate that should be updated.
     * @return : Operation status code.
     */
    ResponseCode updateCertificate(String certificate, String alias) throws APIManagementException;

    /**
     * Get the number of certificates which a tenant has uploaded.
     *
     * @param tenantId : The id of the tenant.
     * @return : The total count of certificates.
     */
    int getCertificateCount(int tenantId) throws APIManagementException;

    /**
     * Get the certificate which matches the provided alias from the trust store.
     *
     * @param tenantId : The id of the tenant.
     * @param alias : The alias of the certificate.
     * @return : The Certificate object.
     */
    ByteArrayInputStream getCertificateContent(int tenantId, String alias) throws APIManagementException;

    /**
     * Method to add client certificate (i.e. Client certificate that can be used to connect the client with gateway)
     * to publisher node.
     *
     * @param apiIdentifier : Identifier of the relevant API, which the client certificate is added against.
     * @param certificate   : Base64 encoded certificate string.
     * @param alias         : Alias of the certificate.
     * @param keyType       : Key type for the certificate (PRODUCTION or SANDBOX).
     * @param tenantId      : The tenant which the client certificate is added against
     * @param organization  : Organization
     * @return SUCCESS : If Operation succeeded, INTERNAL_SERVER_ERROR : If any internal error occurred,
     * ALIAS_EXISTS_IN_TRUST_STORE : If the alias already present in the trust store,CERTIFICATE_EXPIRED : If the
     * certificate is expired.
     */
    ResponseCode addClientCertificate(Identifier apiIdentifier, String certificate, String alias, String tierName,
                                      String keyType, int tenantId, String organization);

    /**
     * Method to delete the client certificate from publisher node.
     *
     * @param apiIdentifier : Identifier of the API which particular client certificate is added against.
     * @param alias         : Alias of the certificate which needs to be removed.
     * @param keyType       : Key type of the certificate
     * @param tenantId      : The owner tenant id.
     * @return : SUCCESS: If operation success
     * INTERNAL_SERVER_ERROR: If any internal error occurred
     * CERTIFICATE_NOT_FOUND : If Certificate is not found in the trust store.
     */
    ResponseCode deleteClientCertificateFromParentNode(Identifier apiIdentifier, String alias, String keyType,
                                                       int tenantId);

    /**
     * Method to add client certificate to gateway nodes.
     *
     * @param certificate : The Base64 encoded certificate string.
     * @param keyType     : Key type of the certificate.
     * @param alias       : Certificate alias.
     * @return : True if the certificate is added to gateway node successfully. False otherwise.
     */
    boolean addClientCertificateToGateway(String certificate, String alias);

    /**
     * This method is to remove the client certificate from client-truststore.jks of gateway nodes.
     *
     * @param alias : The alias of the certificate to be removed.
     * @return : True if the certificate is removed successfully, false otherwise.
     */
    boolean deleteClientCertificateFromGateway(String alias);

    /**
     * This method is used to search client certificates based on different parameters.
     *
     * @param tenantId      : ID of the tenant.
     * @param alias         : Alias of the certificate.
     * @param apiIdentifier : Identifier of the API.
     * @param organization  : Organization
     * @return List of certificates that match the criteria.
     * @throws APIManagementException API Management Exception.
     */
    List<ClientCertificateDTO> searchClientCertificates(int tenantId, String alias, String keyType, Identifier apiIdentifier,
            String organization) throws APIManagementException;

    /**
     * Method to update an existing client certificate.
     *
     * @param certificate : The base64 encoded certificate string.
     * @param alias       : The alias of the certificate that should be updated.
     * @param tenantId    : Id of the tenant.
     * @param tier        : Name of the tier
     * @param keyType       : Key type for the certificate (PRODUCTION or SANDBOX).
     * @param organization : Organization
     * @return : true if update succeeds, false if fails
     */
    ResponseCode updateClientCertificate(String certificate, String alias, String tier, String keyType,
                                         int tenantId, String organization) throws APIManagementException;

    /**
     * To get the count of the client certificates updated for the particular tenant.
     *
     * @param tenantId : ID of the tenant.
     * @return count of client certificates.
     * @throws APIManagementException API Management Exception.
     */
    int getClientCertificateCount(int tenantId, String keyType) throws APIManagementException;

    /**
     * Method to add the certificate to gateway nodes.
     *
     * @param certificate : The Base64 encoded certificate string.
     * @param alias       : Certificate alias.
     * @param tenantId    : Tenant id.
     * @return : True if the certificate is added to gateway node successfully. False otherwise.
     */
    boolean addAllCertificateToGateway(String certificate, String alias, int tenantId);

    /**
     * Method to add the all tenant's certificate to gateway nodes.
     *
     * @param certificateMetadataDTOList : The list of all certificates of a tenant
     */
    void addAllTenantCertificatesToGateway(List<CertificateMetadataDTO> certificateMetadataDTOList);


    /**
     * This method is used to retrieve all the certificates.
     *
     * @return : List of Certificate metadata objects.
     */
    List<CertificateMetadataDTO> getAllCertificates();
}
