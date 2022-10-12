package org.wso2.carbon.apimgt.impl.dao.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ErrorHandler;
import org.wso2.carbon.apimgt.api.dto.CertificateMetadataDTO;
import org.wso2.carbon.apimgt.api.dto.ClientCertificateDTO;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIRevision;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.impl.certificatemgt.exceptions.CertificateAliasExistsException;
import org.wso2.carbon.apimgt.impl.certificatemgt.exceptions.CertificateManagementException;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.CertificateDAO;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CertificateDAOImpl implements CertificateDAO {
    private static final Log log = LogFactory.getLog(CertificateDAOImpl.class);
    private static CertificateDAOImpl INSTANCE = new CertificateDAOImpl();

    private CertificateDAOImpl() {

    }

    public static CertificateDAOImpl getInstance() {
        return INSTANCE;
    }

    private void handleExceptionWithCode(String msg, Throwable t, ErrorHandler code) throws APIManagementException {
        log.error(msg, t);
        throw new APIManagementException(msg, code);
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

    @Override
    public boolean addClientCertificate(String certificate, Identifier apiIdentifier, String alias, String tierName,
                                        int tenantId, String organization) throws CertificateManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                boolean status = addClientCertificate(connection, certificate, apiIdentifier, alias, tierName,
                        tenantId, organization);
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

    private boolean addClientCertificate(Connection connection, String certificate, Identifier apiIdentifier,
                                         String alias, String tierName,
                                         int tenantId, String organization) throws SQLException {

        boolean result;
        String addCertQuery = SQLConstants.ClientCertificateConstants.INSERT_CERTIFICATE;
        try (PreparedStatement preparedStatement = connection.prepareStatement(addCertQuery)) {
            preparedStatement.setBinaryStream(1, getInputStream(certificate));
            preparedStatement.setInt(2, tenantId);
            preparedStatement.setString(3, alias);
            preparedStatement.setString(4, APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
            preparedStatement.setString(5, apiIdentifier.getName());
            preparedStatement.setString(6, apiIdentifier.getVersion());
            preparedStatement.setString(7, organization);
            preparedStatement.setString(8, tierName);
            result = preparedStatement.executeUpdate() >= 1;
        }
        return result;
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

    @Override
    public boolean updateClientCertificate(String certificate, String alias, String tier, int tenantId,
                                           String organization) throws CertificateManagementException {

        List<ClientCertificateDTO> clientCertificateDTOList = getClientCertificates(tenantId, alias, null,
                organization);
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
                        tenantId, organization);
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
     * Method to delete client certificate from the database.
     *
     * @param apiIdentifier : Identifier of the API.
     * @param alias         : Alias for the certificate.
     * @param tenantId      : The Id of the tenant who owns the certificate.
     * @return : true if certificate deletion is successful, false otherwise.
     */
    private boolean deleteClientCertificate(Connection connection, Identifier apiIdentifier, String alias,
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

    @Override
    public List<ClientCertificateDTO> getClientCertificates(int tenantId, String alias, Identifier apiIdentifier,
                                                            String organization) throws CertificateManagementException {

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
                String apiUuid;
                if (apiIdentifier.getUUID() != null) {
                    apiUuid = apiIdentifier.getUUID();
                    APIRevision apiRevision = ApiMgtDAO.getInstance().checkAPIUUIDIsARevisionUUID(apiUuid);
                    if (apiRevision != null && apiRevision.getApiUUID() != null) {
                        apiUuid = apiRevision.getApiUUID();
                    }
                } else {
                    apiUuid = ApiMgtDAO.getInstance().getUUIDFromIdentifier(apiIdentifier, organization);
                }
                apiId = ApiMgtDAO.getInstance().getAPIID(apiUuid, connection);
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
                clientCertificateDTO.setApiIdentifier((APIIdentifier) apiIdentifier);
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

    @Override
    public boolean addCertificate(String certificate, String alias, String endpoint, int tenantId)
            throws CertificateManagementException,
            CertificateAliasExistsException {

        boolean result = false;
        String addCertQuery = SQLConstants.CertificateConstants.INSERT_CERTIFICATE;

        //Check whether any certificate is uploaded for the same alias or endpoint by another user/ tenant.


        try (Connection connection = APIMgtDBUtil.getConnection()) {
            boolean certificateExist = isCertificateExist(connection, alias, tenantId);
            if (certificateExist){
                if (log.isDebugEnabled()) {
                    log.debug("A certificate for the endpoint " + endpoint + " has already added with alias " + alias);
                }
                String message = "Alias or Endpoint exists in the database!";
                throw new CertificateAliasExistsException(message);
            }
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(addCertQuery)) {
                preparedStatement.setInt(1, tenantId);
                preparedStatement.setString(2, endpoint);
                preparedStatement.setString(3, alias);
                preparedStatement.setBinaryStream(4, getInputStream(certificate));
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

    private boolean isCertificateExist(Connection connection, String alias,int tenantId) throws SQLException {
        String query = SQLConstants.CertificateConstants.CERTIFICATE_EXIST;
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1,alias);
            preparedStatement.setInt(2,tenantId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
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
                        try (InputStream certificate = resultSet.getBinaryStream("CERTIFICATE")) {
                            certificateMetadataDTO.setCertificate(APIMgtDBUtil.getStringFromInputStream(certificate));
                        }
                        certificateMetadataList.add(certificateMetadataDTO);
                    }

                }
            }

        } catch (SQLException | IOException e) {
            handleException("Error while retrieving certificate metadata.", e);
        }
        return certificateMetadataList;
    }

}
