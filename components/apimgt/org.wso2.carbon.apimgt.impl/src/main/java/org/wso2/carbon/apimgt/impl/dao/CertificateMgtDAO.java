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
package org.wso2.carbon.apimgt.impl.dao;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.CertificateMetadataDTO;
import org.wso2.carbon.apimgt.api.dto.ClientCertificateDTO;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.certificatemgt.exceptions.CertificateAliasExistsException;
import org.wso2.carbon.apimgt.impl.certificatemgt.exceptions.CertificateManagementException;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handles data base transactions related to the server certificate management.
 * Here the certificate information will be,
 * Alias
 * Server Endpoint
 * Tenant Id
 * The alias and server ep has one-to-one mapping, which there could be one certificate for a particular endpoint.
 * A tenant could have multiple alias-ep entries.
 */
public class CertificateMgtDAO {

    private static Log log = LogFactory.getLog(CertificateMgtDAO.class);
    private static CertificateMgtDAO certificateMgtDAO = new CertificateMgtDAO();

    /**
     * Private constructor
     */
    private CertificateMgtDAO() {

    }

    /**
     * Returns an instance of CertificateMgtDao.
     */
    public static CertificateMgtDAO getInstance() {
        return certificateMgtDAO;
    }


    private boolean addClientCertificate(Connection connection, String certificate, APIIdentifier apiIdentifier,
                                         String alias, String tierName,
                                         int tenantId) throws SQLException {

        boolean result;
        String addCertQuery = SQLConstants.ClientCertificateConstants.INSERT_CERTIFICATE;
        try (PreparedStatement preparedStatement = connection.prepareStatement(addCertQuery)) {
            preparedStatement.setBinaryStream(1, getInputStream(certificate));
            preparedStatement.setInt(2, tenantId);
            preparedStatement.setString(3, alias);
            preparedStatement.setString(4, APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
            preparedStatement.setString(5, apiIdentifier.getName());
            preparedStatement.setString(6, apiIdentifier.getVersion());
            preparedStatement.setString(7, tierName);
            result = preparedStatement.executeUpdate() >= 1;
        }
        return result;
    }

    /**
     * To update an already existing client certificate.
     *
     * @param certificate : Specific certificate.
     * @param alias       : Alias of the certificate.
     * @param tier        : Name of tier related with the certificate.
     * @param tenantId    : ID of the tenant.
     * @return true if the update succeeds, unless false.
     * @throws CertificateManagementException Certificate Management Exception.
     */
    public boolean updateClientCertificate(String certificate, String alias, String tier, int tenantId)
            throws CertificateManagementException {

        List<ClientCertificateDTO> clientCertificateDTOList = getClientCertificates(tenantId, alias, null);
        ClientCertificateDTO clientCertificateDTO;

        if (clientCertificateDTOList.size() == 0) {
            if (log.isDebugEnabled()) {
                log.debug("Client certificate update request is received for a non-existing alias " + alias + " of "
                        + "tenant " + tenantId);
            }
            return false;
        }
        clientCertificateDTO = clientCertificateDTOList.get(0);
        if (StringUtils.isNotEmpty(certificate)) {
            clientCertificateDTO.setCertificate(certificate);
        }
        if (StringUtils.isNotEmpty(tier)) {
            clientCertificateDTO.setTierName(tier);
        }
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                deleteClientCertificate(connection, null, alias, tenantId);
                addClientCertificate(connection, clientCertificateDTO.getCertificate(),
                        clientCertificateDTO.getApiIdentifier(), alias, clientCertificateDTO.getTierName(),
                        tenantId);
                connection.commit();
            } catch (SQLException e) {
                handleConnectionRollBack(connection);
                handleException("Error while updating client certificate for the API for the alias " + alias, e);
            }
        } catch (SQLException e) {
            handleException("Error while updating client certificate for the API for the alias " + alias, e);
        }
        return true;
    }

    /**
     * To get the input stream from string.
     *
     * @param value : Relevant string that need to be converted to input stream.
     * @return input stream.
     */
    private InputStream getInputStream(String value) {

        byte[] cert = value.getBytes(StandardCharsets.UTF_8);
        return new ByteArrayInputStream(cert);
    }

    /**
     * This method handles the connection roll back.
     *
     * @param connection Relevant database connection that need to be rolled back.
     */
    private void handleConnectionRollBack(Connection connection) {

        try {
            if (connection != null) {
                connection.rollback();
            } else {
                log.warn("Could not perform rollback since the connection is null.");
            }
        } catch (SQLException e1) {
            log.error("Error while rolling back the transaction.", e1);
        }
    }

    /**
     * Method to add a new certificate to the database.
     *
     * @param alias    : Alias for the new certificate.
     * @param endpoint : The endpoint/ server url which the certificate will be mapped to.
     * @param tenantId : The Id of the tenant who uploaded the certificate.
     * @return : True if the information is added successfully, false otherwise.
     * @throws CertificateManagementException if existing entry is found for the given endpoint or alias.
     */
    public boolean addCertificate(String alias, String endpoint, int tenantId) throws CertificateManagementException,
            CertificateAliasExistsException {

        boolean result = false;
        String addCertQuery = SQLConstants.CertificateConstants.INSERT_CERTIFICATE;

        //Check whether any certificate is uploaded for the same alias or endpoint by another user/ tenant.
        CertificateMetadataDTO existingCertificate = getCertificate(alias);

        if (existingCertificate != null) {
            if (log.isDebugEnabled()) {
                log.debug("A certificate for the endpoint " + endpoint + " has already added with alias " +
                        existingCertificate.getAlias());
            }
            String message = "Alias or Endpoint exists in the database!";
            if (existingCertificate.getAlias().equals(alias)) {
                throw new CertificateAliasExistsException(message);
            }
        }

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(addCertQuery)) {
                preparedStatement.setInt(1, tenantId);
                preparedStatement.setString(2, endpoint);
                preparedStatement.setString(3, alias);
                result = preparedStatement.executeUpdate() == 1;
                connection.commit();
            } catch (SQLException e) {
                handleConnectionRollBack(connection);
                if (log.isDebugEnabled()) {
                    log.debug("Error occurred while adding certificate metadata to database.", e);
                }
                handleException("Error while persisting certificate metadata.", e);
            }

        } catch (SQLException e) {
            handleException("Error while retrieving connection", e);
        }
        return result;
    }

    /**
     * Method to retrieve certificate metadata from db for specific alias or endpoint.
     * From alias and endpoint, only one parameter is required. This will be used to query all the certificates
     * without a limitation for tenant.
     * Addresses : If some tenant is trying to add a certificate with the same alias, proper error should be shown in
     * the UI.
     *
     * @param alias : Alias for the certificate. (Optional)
     * @return : A CertificateMetadataDTO object if the certificate is retrieved successfully, null otherwise.
     */
    private CertificateMetadataDTO getCertificate(String alias) throws CertificateManagementException {

        CertificateMetadataDTO certificateMetadataDTO = null;
        String getCertQuery = SQLConstants.CertificateConstants.GET_CERTIFICATE_ALL_TENANTS;

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(getCertQuery)) {
                preparedStatement.setString(1, alias);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        certificateMetadataDTO = new CertificateMetadataDTO();
                        certificateMetadataDTO.setAlias(resultSet.getString("ALIAS"));
                        certificateMetadataDTO.setEndpoint(resultSet.getString("END_POINT"));
                    }
                }
            }
        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while retrieving certificate metadata for alias " + alias);
            }
            handleException("Error while retrieving certificate metadata for alias " + alias, e);
        }
        return certificateMetadataDTO;
    }

    /**
     * Method to retrieve certificate metadata from db for specific tenant which matches alias or api identifier.
     * Both alias and api identifier are optional
     *
     * @param tenantId      : The id of the tenant which the certificate belongs to.
     * @param alias         : Alias for the certificate. (Optional)
     * @param apiIdentifier : The API which the certificate is mapped to. (Optional)
     * @return : A CertificateMetadataDTO object if the certificate is retrieved successfully, null otherwise.
     */
    public List<ClientCertificateDTO> getClientCertificates(int tenantId, String alias, APIIdentifier apiIdentifier)
            throws CertificateManagementException {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<ClientCertificateDTO> clientCertificateDTOS = new ArrayList<>();
        int apiId = 0;
        int index = 1;
        String selectQuery = SQLConstants.ClientCertificateConstants.SELECT_CERTIFICATE_FOR_TENANT;
        if (StringUtils.isNotEmpty(alias) && apiIdentifier != null) {
            selectQuery = SQLConstants.ClientCertificateConstants.SELECT_CERTIFICATE_FOR_TENANT_ALIAS_APIID;
        } else if (StringUtils.isNotEmpty(alias)) {
            selectQuery = SQLConstants.ClientCertificateConstants.SELECT_CERTIFICATE_FOR_TENANT_ALIAS;
        } else if (apiIdentifier != null) {
            selectQuery = SQLConstants.ClientCertificateConstants.SELECT_CERTIFICATE_FOR_TENANT_APIID;
        }

        try {
            connection = APIMgtDBUtil.getConnection();
            if (apiIdentifier != null) {
                apiId = ApiMgtDAO.getInstance().getAPIID(apiIdentifier, connection);
            }
            preparedStatement = connection.prepareStatement(selectQuery);
            preparedStatement.setBoolean(index, false);
            index++;
            preparedStatement.setInt(index, tenantId);
            index++;
            if (alias != null) {
                preparedStatement.setString(index, alias);
                index++;
            }
            if (apiIdentifier != null) {
                preparedStatement.setInt(index, apiId);
            }
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                alias = resultSet.getString("ALIAS");
                ClientCertificateDTO clientCertificateDTO = new ClientCertificateDTO();
                clientCertificateDTO.setTierName(resultSet.getString("TIER_NAME"));
                clientCertificateDTO.setAlias(alias);
                clientCertificateDTO.setCertificate(
                        APIMgtDBUtil.getStringFromInputStream(resultSet.getBinaryStream("CERTIFICATE")));
                if (apiIdentifier == null) {
                    apiIdentifier = new APIIdentifier(APIUtil.replaceEmailDomain(resultSet.getString("API_PROVIDER")),
                            resultSet.getString("API_NAME"), resultSet.getString("API_VERSION"));
                }
                clientCertificateDTO.setApiIdentifier(apiIdentifier);
                clientCertificateDTOS.add(clientCertificateDTO);
            }
        } catch (SQLException e) {
            handleException("Error while searching client certificate details for the tenant " + tenantId, e);
        } catch (APIManagementException e) {
            handleException(
                    "API Management Exception while searching client certificate details for the tenant " + tenantId,
                    e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, resultSet);
        }
        return clientCertificateDTOS;
    }

    /**
     * Method to retrieve certificate metadata from db for specific tenant which matches alias or endpoint.
     * From alias and endpoint, only one parameter is required.
     *
     * @param tenantId : The id of the tenant which the certificate belongs to.
     * @param alias    : Alias for the certificate. (Optional)
     * @param endpoint : The endpoint/ server url which the certificate is mapped to. (Optional)
     * @return : A CertificateMetadataDTO object if the certificate is retrieved successfully, null otherwise.
     */
    public List<CertificateMetadataDTO> getCertificates(String alias, String endpoint, int tenantId)
            throws CertificateManagementException {

        String getCertQuery;
        CertificateMetadataDTO certificateMetadataDTO;
        List<CertificateMetadataDTO> certificateMetadataList = new ArrayList<>();

        if (StringUtils.isNotEmpty(alias) || StringUtils.isNotEmpty(endpoint)) {
            if (log.isDebugEnabled()) {
                log.debug("The alias and endpoint are not empty. Invoking the search query with parameters " +
                        "alias = " + alias + " endpoint = " + endpoint);
            }
            getCertQuery = SQLConstants.CertificateConstants.GET_CERTIFICATE_TENANT;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("The alias and endpoint are empty. Invoking the get all certificates for tenant " +
                        tenantId);
            }
            getCertQuery = SQLConstants.CertificateConstants.GET_CERTIFICATES;
        }

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(getCertQuery)) {
                preparedStatement.setInt(1, tenantId);

                if (StringUtils.isNotEmpty(alias) || StringUtils.isNotEmpty(endpoint)) {
                    preparedStatement.setString(2, alias);
                    preparedStatement.setString(3, endpoint);
                }
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        certificateMetadataDTO = new CertificateMetadataDTO();
                        certificateMetadataDTO.setAlias(resultSet.getString("ALIAS"));
                        certificateMetadataDTO.setEndpoint(resultSet.getString("END_POINT"));
                        certificateMetadataList.add(certificateMetadataDTO);
                    }

                }
            }

        } catch (SQLException e) {
            handleException("Error while retrieving certificate metadata.", e);
        }
        return certificateMetadataList;
    }

    /**
     * To remove the entries of updated certificates from gateway.
     *
     * @param apiIdentifier : Identifier of the API.
     * @param tenantId      : ID of the tenant.
     * @throws CertificateManagementException Certificate Management Exception.
     */
    public void updateRemovedCertificatesFromGateways(APIIdentifier apiIdentifier, int tenantId)
            throws CertificateManagementException {

        String getCertQuery = SQLConstants.ClientCertificateConstants.DELETE_CERTIFICATES_FOR_API;

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                try (PreparedStatement preparedStatement = connection.prepareStatement(getCertQuery)) {
                    preparedStatement.setInt(1, tenantId);
                    preparedStatement.setString(2, APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                    preparedStatement.setString(3, apiIdentifier.getName());
                    preparedStatement.setString(4, apiIdentifier.getVersion());
                    preparedStatement.setBoolean(5, true);
                    preparedStatement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException e) {
                handleConnectionRollBack(connection);
                handleException("SQL exception while updating removed certificates from gateway for the api " +
                        apiIdentifier.toString(), e);
            }
        } catch (SQLException e) {
            handleException("API management exception while updating removed certificates from gateway for the api "
                    + apiIdentifier.toString(), e);
        }
    }

    /**
     * To get the set of alias of client certificates that are removed from publisher, but not removed from the gateway.
     *
     * @param apiIdentifier : Identifier of the API.
     * @param tenantId      : ID of the tenant.
     * @return list of alias of client certificates that need to be removed from the gateway.
     * @throws CertificateManagementException Certificate Management Exception.
     */
    public List<String> getDeletedClientCertificateAlias(APIIdentifier apiIdentifier, int tenantId)
            throws CertificateManagementException {

        String getCertQuery = SQLConstants.ClientCertificateConstants.GET_CERTIFICATES_FOR_API;
        List<String> aliasList = new ArrayList<>();

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(getCertQuery)) {
            preparedStatement.setInt(1, tenantId);
            preparedStatement.setString(2, APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
            preparedStatement.setString(3, apiIdentifier.getName());
            preparedStatement.setString(4, apiIdentifier.getVersion());
            preparedStatement.setBoolean(5, true);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    aliasList.add(resultSet.getString("ALIAS"));
                }
            }
        } catch (SQLException e) {
            handleException(
                    "SQL exception while retrieving deleted client certificate details for the API " + apiIdentifier
                            .toString(), e);
        }

        return aliasList;
    }

    /**
     * Method to delete a certificate from the database.
     *
     * @param alias    : Alias for the certificate.
     * @param endpoint : The endpoint/ server url which the certificate is mapped to.
     * @param tenantId : The Id of the tenant who owns the certificate.
     * @return : true if certificate deletion is successful, false otherwise.
     */
    public boolean deleteCertificate(String alias, String endpoint, int tenantId)
            throws CertificateManagementException {

        boolean result = false;
        String deleteCertQuery = SQLConstants.CertificateConstants.DELETE_CERTIFICATES;

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(deleteCertQuery)) {
                preparedStatement.setInt(1, tenantId);
                preparedStatement.setString(2, alias);
                result = preparedStatement.executeUpdate() == 1;
                connection.commit();
            } catch (SQLException e) {
                handleConnectionRollBack(connection);
                handleException("Error while deleting certificate metadata. ", e);
            }
        } catch (SQLException e) {
            handleException("Error while deleting certificate metadata. ", e);
        }
        return result;
    }

    /**
     * Method to delete client certificate from the database.
     *
     * @param apiIdentifier : Identifier of the API.
     * @param alias         : Alias for the certificate.
     * @param tenantId      : The Id of the tenant who owns the certificate.
     * @return : true if certificate deletion is successful, false otherwise.
     */
    private boolean deleteClientCertificate(Connection connection, APIIdentifier apiIdentifier, String alias,
                                            int tenantId) throws SQLException {

        boolean result;
        String deleteCertQuery = SQLConstants.ClientCertificateConstants.PRE_DELETE_CERTIFICATES;
        if (apiIdentifier == null) {
            deleteCertQuery = SQLConstants.ClientCertificateConstants.PRE_DELETE_CERTIFICATES_WITHOUT_APIID;
        }
            /* If an entry exists already with "Removed" true, remove that particular entry and update the current
             entry with removed true */
        try (PreparedStatement preparedStatement = connection.prepareStatement(deleteCertQuery)) {
            preparedStatement.setInt(1, tenantId);
            preparedStatement.setBoolean(2, true);
            preparedStatement.setString(3, alias);
            if (apiIdentifier != null) {
                preparedStatement.setString(4, APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                preparedStatement.setString(5, apiIdentifier.getName());
                preparedStatement.setString(6, apiIdentifier.getVersion());
            }
            preparedStatement.executeUpdate();
        }
        deleteCertQuery = SQLConstants.ClientCertificateConstants.DELETE_CERTIFICATES;
        if (apiIdentifier == null) {
            deleteCertQuery = SQLConstants.ClientCertificateConstants.DELETE_CERTIFICATES_WITHOUT_APIID;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(deleteCertQuery)) {
            preparedStatement.setBoolean(1, true);
            preparedStatement.setInt(2, tenantId);
            preparedStatement.setString(3, alias);
            if (apiIdentifier != null) {
                preparedStatement.setString(4, APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                preparedStatement.setString(5, apiIdentifier.getName());
                preparedStatement.setString(6, apiIdentifier.getVersion());
            }
            result = preparedStatement.executeUpdate() >= 1;
        }

        return result;
    }

    public boolean deleteClientCertificate(APIIdentifier apiIdentifier, String alias, int tenantId)
            throws CertificateManagementException {

        boolean result = false;
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                result = deleteClientCertificate(connection, apiIdentifier, alias, tenantId);
                connection.commit();
            } catch (SQLException e) {
                handleConnectionRollBack(connection);
                handleException(
                        "API Management exception while trying deleting certificate metadata with the alias " + alias,
                        e);
            }
        } catch (SQLException e) {
            handleException(
                    "API Management exception while trying deleting certificate metadata with the alias " + alias, e);
        }
        return result;
    }

    /**
     * Retrieve the number of total certificates which a tenant has uploaded.
     *
     * @param tenantId : The id of the tenant.
     * @return : The total certificate count of the tenant.
     * @throws CertificateManagementException :
     */
    public int getCertificateCount(int tenantId) throws CertificateManagementException {

        String certificateCountQuery = SQLConstants.CertificateConstants.CERTIFICATE_COUNT_QUERY;
        int count = 0;

        if (log.isDebugEnabled()) {
            log.debug("Get the certificate count for tenantId" + tenantId);
        }

        try {
            try (Connection connection = APIMgtDBUtil.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(certificateCountQuery)) {
                preparedStatement.setInt(1, tenantId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getInt("count");
                    }
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving the certificate count for tenantId " + tenantId + ".", e);
        }
        return count;
    }

    /**
     * Retrieve the number of total client certificates which a tenant has uploaded.
     *
     * @param tenantId : The id of the tenant.
     * @return : The total certificate count of the tenant.
     * @throws CertificateManagementException :
     */
    public int getClientCertificateCount(int tenantId) throws CertificateManagementException {

        String clientCertificateCountQuery = SQLConstants.ClientCertificateConstants.CERTIFICATE_COUNT_QUERY;
        int count = 0;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(clientCertificateCountQuery)) {
            preparedStatement.setInt(1, tenantId);
            preparedStatement.setBoolean(2, false);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving the client certificate count for tenantId " + tenantId + ".", e);
        }
        return count;
    }

    /**
     * To check whether alias with the given value exist already.
     *
     * @param alias Relevant alias.
     * @return true if the alias exist, false if not.
     * @throws CertificateManagementException Certificate Management Exception.
     */
    public boolean checkWhetherAliasExist(String alias, int tenantId) throws CertificateManagementException {

        String selectCertificateForAlias = SQLConstants.ClientCertificateConstants.SELECT_CERTIFICATE_FOR_ALIAS;
        boolean isExist = false;
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(selectCertificateForAlias)) {
                preparedStatement.setString(1, alias);
                preparedStatement.setBoolean(2, false);
                preparedStatement.setInt(3, tenantId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        isExist = true;
                        if (log.isDebugEnabled()) {
                            log.debug("Alias " + alias + " exist already and uploaded as a client certificate");
                        }
                    }
                }
            }
            if (!isExist) {
                selectCertificateForAlias = SQLConstants.CertificateConstants.SELECT_CERTIFICATE_FOR_ALIAS;
                try (PreparedStatement preparedStatement = connection.prepareStatement(selectCertificateForAlias)) {
                    preparedStatement.setString(1, alias + "_" + tenantId);
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        if (resultSet.next()) {
                            isExist = true;
                            if (log.isDebugEnabled()) {
                                log.debug(
                                        "Alias " + alias +
                                                " exist already and uploaded as a certificate for the backend");
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            handleException("Database error while checking whether alias " + alias + " exist in the database.", e);
        }
        return isExist;
    }

    /**
     * Method to handle the SQL Exception.
     *
     * @param message : Error message.
     * @param e       : Throwable cause.
     * @throws CertificateManagementException :
     */
    private void handleException(String message, Throwable e) throws CertificateManagementException {

        throw new CertificateManagementException(message, e);
    }

    /**
     * Method to add a new client certificate to the database.
     *
     * @param certificate   : Client certificate that need to be added.
     * @param apiIdentifier : API which the client certificate is uploaded against.
     * @param alias         : Alias for the new certificate.
     * @param tenantId      : The Id of the tenant who uploaded the certificate.
     * @return : True if the information is added successfully, false otherwise.
     * @throws CertificateManagementException if existing entry is found for the given endpoint or alias.
     */
    public boolean addClientCertificate(String certificate, APIIdentifier apiIdentifier, String alias, String tierName,
                                        int tenantId) throws CertificateManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                boolean status = addClientCertificate(connection, certificate, apiIdentifier, alias, tierName,
                        tenantId);
                connection.commit();
                return status;
            } catch (SQLException e) {
                handleConnectionRollBack(connection);
                if (log.isDebugEnabled()) {
                    log.debug("Error occurred while adding client certificate details to database for the API "
                            + apiIdentifier.toString(), e);
                }
                handleException("Error while persisting client certificate for the API " + apiIdentifier.toString(), e);
            }
        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error occurred while adding client certificate details to database for the API "
                        + apiIdentifier.toString(), e);
            }
            handleException("Error while persisting client certificate for the API " + apiIdentifier.toString(), e);
        }
        return false;
    }
}
