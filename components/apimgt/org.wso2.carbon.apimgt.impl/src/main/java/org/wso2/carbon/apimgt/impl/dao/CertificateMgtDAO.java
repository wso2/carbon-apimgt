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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.CertificateMetadataDTO;
import org.wso2.carbon.apimgt.impl.certificatemgt.exceptions.CertificateAliasExistsException;
import org.wso2.carbon.apimgt.impl.certificatemgt.exceptions.CertificateManagementException;
import org.wso2.carbon.apimgt.impl.certificatemgt.exceptions.EndpointForCertificateExistsException;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
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

    private static final String CERTIFICATE_TABLE_NAME = "AM_CERTIFICATE_METADATA";
    private static Log log = LogFactory.getLog(CertificateMgtDAO.class);
    private static CertificateMgtDAO certificateMgtDAO = null;
    private static boolean initialAutoCommit = false;

    /**
     * Private constructor
     */
    private CertificateMgtDAO() {
    }

    /**
     * Returns an instance of CertificateMgtDao.
     */
    public static synchronized CertificateMgtDAO getInstance() {
        if (certificateMgtDAO == null) {
            certificateMgtDAO = new CertificateMgtDAO();
        }
        return certificateMgtDAO;
    }

    /**
     * Checks whether the certificate management table exists in the data base.
     *
     * @return : True if exists, false otherwise.
     */
    public boolean isTableExists() throws CertificateManagementException {
        boolean isExists = false;
        Connection connection = null;
        ResultSet resultSet = null;
        DatabaseMetaData databaseMetaData = null;

        try {
            connection = APIMgtDBUtil.getConnection();
            databaseMetaData = connection.getMetaData();

            resultSet = databaseMetaData.getTables(null, null,
                    CERTIFICATE_TABLE_NAME, null);
            if (resultSet.next()) {
                isExists = true;
            }
        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while retrieving database information. ", e);
            }
            handleException("Error retrieving Database information", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(null, connection, resultSet);
        }
        return isExists;
    }


    /**
     * Returns all the currently added certificates for a particular tenant.
     *
     * @param tenantId : The tenant whose certificates should be retrieved.
     * @return : List of Certificate information objects.
     */
    public List<CertificateMetadataDTO> getCertificates(int tenantId) throws CertificateManagementException {
        List<CertificateMetadataDTO> certificates = new ArrayList<CertificateMetadataDTO>();
        CertificateMetadataDTO certificateMetadataDTO;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String getCertsQuery = SQLConstants.CertificateConstants.GET_CERTIFICATES;

        try {
            try {
                connection = APIMgtDBUtil.getConnection();
                initialAutoCommit = connection.getAutoCommit();
                connection.setAutoCommit(false);
                connection.commit();
                preparedStatement = connection.prepareStatement(getCertsQuery);
                preparedStatement.setInt(1, tenantId);
                resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    certificateMetadataDTO = new CertificateMetadataDTO();
                    certificateMetadataDTO.setAlias(resultSet.getString("ALIAS"));
                    certificateMetadataDTO.setEndpoint(resultSet.getString("END_POINT"));
                    certificates.add(certificateMetadataDTO);
                }
            } catch (SQLException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Error occurred while retrieving certificate metadata. ", e);
                }
                handleException("Error while retrieving certificates.", e);
            } finally {
                APIMgtDBUtil.closeStatement(preparedStatement);
                APIMgtDBUtil.closeAllConnections(preparedStatement, connection, resultSet);
                connection.setAutoCommit(initialAutoCommit);
            }
        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error occurred in finally block while retrieving certificate metadata. ", e);
            }
        }
        return certificates;
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
            CertificateAliasExistsException, EndpointForCertificateExistsException {
        boolean result = false;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        String addCertQuery = SQLConstants.CertificateConstants.INSERT_CERTIFICATE;

        //Check whether any certificate is uploaded for the same alias or endpoint by another user/ tenant.
        CertificateMetadataDTO existingCertificate = getCertificate(alias, endpoint);

        if (existingCertificate != null) {
            if (log.isDebugEnabled()) {
                log.debug("A certificate for the endpoint " + endpoint + " has already added with alias " +
                        existingCertificate.getAlias());
            }
            String message = "Alias or Endpoint exists in the database!";
            if (existingCertificate.getAlias().equals(alias)) {
                throw new CertificateAliasExistsException(message);
            } else {
                throw new EndpointForCertificateExistsException(message);
            }
        }

        try {
            try {
                connection = APIMgtDBUtil.getConnection();
                initialAutoCommit = connection.getAutoCommit();
                connection.setAutoCommit(false);
                preparedStatement = connection.prepareStatement(addCertQuery);
                preparedStatement.setInt(1, tenantId);
                preparedStatement.setString(2, endpoint);
                preparedStatement.setString(3, alias);
                result = preparedStatement.executeUpdate() == 1;
                connection.commit();

            } catch (SQLException e) {
                try {
                    if (connection != null) {
                        connection.rollback();
                    } else {
                        log.error("Could not perform rollback since the connection is null.");
                    }
                } catch (SQLException e1) {
                    log.error("Error while rolling back the transaction.", e1);
                }
                if (log.isDebugEnabled()) {
                    log.debug("Error occurred while adding certificate metadata to database.", e);
                }
                handleException("Error while persisting certificate metadata.", e);
            } finally {
                APIMgtDBUtil.closeStatement(preparedStatement);
                APIMgtDBUtil.closeAllConnections(preparedStatement, connection, null);
                connection.setAutoCommit(initialAutoCommit);
            }
        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error occurred in finally block while adding certificate metadata. ", e);
            }
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
     * @param alias    : Alias for the certificate. (Optional)
     * @param endpoint : The endpoint/ server url which the certificate is mapped to. (Optional)
     * @return : A CertificateMetadataDTO object if the certificate is retrieved successfully, null otherwise.
     */
    private CertificateMetadataDTO getCertificate(String alias, String endpoint) throws CertificateManagementException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        CertificateMetadataDTO certificateMetadataDTO = null;
        String getCertQuery = SQLConstants.CertificateConstants.GET_CERTIFICATE_ALL_TENANTS;

        try {
            try {
                connection = APIMgtDBUtil.getConnection();
                initialAutoCommit = connection.getAutoCommit();
                connection.setAutoCommit(false);
                connection.commit();
                preparedStatement = connection.prepareStatement(getCertQuery);
                preparedStatement.setString(1, alias);
                preparedStatement.setString(2, endpoint);
                resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    certificateMetadataDTO = new CertificateMetadataDTO();
                    certificateMetadataDTO.setAlias(resultSet.getString("ALIAS"));
                    certificateMetadataDTO.setEndpoint(resultSet.getString("END_POINT"));
                }
            } catch (SQLException e) {
                handleException("Error while retrieving certificate metadata.", e);
            } finally {
                APIMgtDBUtil.closeStatement(preparedStatement);
                APIMgtDBUtil.closeAllConnections(preparedStatement, connection, resultSet);
                connection.setAutoCommit(initialAutoCommit);
            }
        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error occurred in finally block while retrieving certificate metadata. ", e);
            }
        }
        return certificateMetadataDTO;
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
    public CertificateMetadataDTO getCertificate(String alias, String endpoint, int tenantId)
            throws CertificateManagementException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        CertificateMetadataDTO certificateMetadataDTO = null;
        String getCertQuery = SQLConstants.CertificateConstants.GET_CERTIFICATE_TENANT;

        try {
            try {
                connection = APIMgtDBUtil.getConnection();
                initialAutoCommit = connection.getAutoCommit();
                connection.setAutoCommit(false);
                connection.commit();
                preparedStatement = connection.prepareStatement(getCertQuery);
                preparedStatement.setInt(1, tenantId);
                preparedStatement.setString(2, alias);
                preparedStatement.setString(3, endpoint);
                resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    certificateMetadataDTO = new CertificateMetadataDTO();
                    certificateMetadataDTO.setAlias(resultSet.getString("ALIAS"));
                    certificateMetadataDTO.setEndpoint(resultSet.getString("END_POINT"));
                }
            } catch (SQLException e) {
                handleException("Error while retrieving certificate metadata.", e);
            } finally {
                APIMgtDBUtil.closeStatement(preparedStatement);
                APIMgtDBUtil.closeAllConnections(preparedStatement, connection, resultSet);
                connection.setAutoCommit(initialAutoCommit);
            }
        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error occurred in finally block while retrieving certificate metadata. ", e);
            }
        }
        return certificateMetadataDTO;
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
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        boolean result = false;
        String deleteCertQuery = SQLConstants.CertificateConstants.DELETE_CERTIFICATES;

        try {
            connection = APIMgtDBUtil.getConnection();
            initialAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(deleteCertQuery);
            preparedStatement.setInt(1, tenantId);
            preparedStatement.setString(2, alias);
            preparedStatement.setString(3, endpoint);
            result = preparedStatement.executeUpdate() == 1;
            connection.commit();
            connection.setAutoCommit(initialAutoCommit);
        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                } else {
                    log.error("Could not perform rollback since the connection is Null.");
                }
            } catch (SQLException e1) {
                log.error("Error while rolling back the transaction.", e1);
            }
            handleException("Error while deleting certificate metadata. ", e);
        } finally {
            APIMgtDBUtil.closeStatement(preparedStatement);
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, null);
        }
        return result;
    }

    /**
     * Method to handle the SQL Exception.
     * @param message : Error message.
     * @param e : Throwable cause.
     * @throws APIManagementException :
     */
    private void handleException(String message, Throwable e) throws CertificateManagementException {
        throw new CertificateManagementException(message, e);
    }
}
