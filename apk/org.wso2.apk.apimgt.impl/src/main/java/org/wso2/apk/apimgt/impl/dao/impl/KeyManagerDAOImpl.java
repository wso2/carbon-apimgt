package org.wso2.apk.apimgt.impl.dao.impl;

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.apk.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.ErrorHandler;
import org.wso2.apk.apimgt.api.ExceptionCodes;
import org.wso2.apk.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.apk.apimgt.impl.dao.KeyManagerDAO;
import org.wso2.apk.apimgt.impl.dao.constants.SQLConstants;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KeyManagerDAOImpl implements KeyManagerDAO {
    private static final Log log = LogFactory.getLog(KeyManagerDAOImpl.class);
    private static KeyManagerDAOImpl INSTANCE = new KeyManagerDAOImpl();

    private KeyManagerDAOImpl() {

    }

    public static KeyManagerDAOImpl getInstance() {
        return INSTANCE;
    }

    private void handleExceptionWithCode(String msg, Throwable t, ErrorHandler code) throws APIManagementException {
        log.error(msg, t);
        throw new APIManagementException(msg, code);
    }

    private void handleException(String msg, Throwable t) throws APIManagementException {

        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }

    @Override
    public List<KeyManagerConfigurationDTO> getKeyManagerConfigurationsByOrganization(String organization)
            throws APIManagementException {

        List<KeyManagerConfigurationDTO> keyManagerConfigurationDTOS = new ArrayList<>();
        final String query = "SELECT * FROM AM_KEY_MANAGER WHERE ORGANIZATION = ? ";
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setString(1, organization);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    KeyManagerConfigurationDTO keyManagerConfigurationDTO = new KeyManagerConfigurationDTO();
                    String uuid = resultSet.getString("UUID");
                    keyManagerConfigurationDTO.setUuid(uuid);
                    keyManagerConfigurationDTO.setName(resultSet.getString("NAME"));
                    keyManagerConfigurationDTO.setDisplayName(resultSet.getString("DISPLAY_NAME"));
                    keyManagerConfigurationDTO.setDescription(resultSet.getString("DESCRIPTION"));
                    keyManagerConfigurationDTO.setType(resultSet.getString("TYPE"));
                    keyManagerConfigurationDTO.setEnabled(resultSet.getBoolean("ENABLED"));
                    keyManagerConfigurationDTO.setOrganization(organization);
                    keyManagerConfigurationDTO.setTokenType(resultSet.getString("TOKEN_TYPE"));
                    keyManagerConfigurationDTO.setExternalReferenceId(resultSet.getString("EXTERNAL_REFERENCE_ID"));
                    try (InputStream configuration = resultSet.getBinaryStream("CONFIGURATION")) {
                        String configurationContent = IOUtils.toString(configuration);
                        Map map = new Gson().fromJson(configurationContent, Map.class);
                        keyManagerConfigurationDTO.setAdditionalProperties(map);
                    } catch (IOException e) {
                        log.error("Error while converting configurations in " + uuid, e);
                    }
                    keyManagerConfigurationDTOS.add(keyManagerConfigurationDTO);
                }
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Error while retrieving key manager configurations for organization "
                    + organization, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return keyManagerConfigurationDTOS;
    }

    @Override
    public List<KeyManagerConfigurationDTO> getKeyManagerConfigurations() throws APIManagementException {

        List<KeyManagerConfigurationDTO> keyManagerConfigurationDTOS = new ArrayList<>();
        final String query = "SELECT * FROM AM_KEY_MANAGER";
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    KeyManagerConfigurationDTO keyManagerConfigurationDTO = new KeyManagerConfigurationDTO();
                    String uuid = resultSet.getString("UUID");
                    keyManagerConfigurationDTO.setUuid(uuid);
                    keyManagerConfigurationDTO.setName(resultSet.getString("NAME"));
                    keyManagerConfigurationDTO.setDisplayName(resultSet.getString("DISPLAY_NAME"));
                    keyManagerConfigurationDTO.setDescription(resultSet.getString("DESCRIPTION"));
                    keyManagerConfigurationDTO.setType(resultSet.getString("TYPE"));
                    keyManagerConfigurationDTO.setEnabled(resultSet.getBoolean("ENABLED"));
                    keyManagerConfigurationDTO.setOrganization(resultSet.getString("ORGANIZATION"));
                    keyManagerConfigurationDTO.setTokenType(resultSet.getString("TOKEN_TYPE"));
                    keyManagerConfigurationDTO.setExternalReferenceId(resultSet.getString("EXTERNAL_REFERENCE_ID"));
                    try (InputStream configuration = resultSet.getBinaryStream("CONFIGURATION")) {
                        String configurationContent = IOUtils.toString(configuration);
                        Map map = new Gson().fromJson(configurationContent, Map.class);
                        keyManagerConfigurationDTO.setAdditionalProperties(map);
                    } catch (IOException e) {
                        log.error("Error while converting configurations in " + uuid, e);
                    }
                    keyManagerConfigurationDTOS.add(keyManagerConfigurationDTO);
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error while retrieving all key manager configurations", e);
        }

        return keyManagerConfigurationDTOS;
    }

    @Override
    public KeyManagerConfigurationDTO getKeyManagerConfigurationByID(String organization, String id)
            throws APIManagementException {

        final String query = "SELECT * FROM AM_KEY_MANAGER WHERE UUID = ? AND ORGANIZATION = ?";
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setString(1, id);
            preparedStatement.setString(2, organization);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    KeyManagerConfigurationDTO keyManagerConfigurationDTO = new KeyManagerConfigurationDTO();
                    String uuid = resultSet.getString("UUID");
                    keyManagerConfigurationDTO.setUuid(uuid);
                    keyManagerConfigurationDTO.setName(resultSet.getString("NAME"));
                    keyManagerConfigurationDTO.setDescription(resultSet.getString("DESCRIPTION"));
                    keyManagerConfigurationDTO.setType(resultSet.getString("TYPE"));
                    keyManagerConfigurationDTO.setEnabled(resultSet.getBoolean("ENABLED"));
                    keyManagerConfigurationDTO.setOrganization(organization);
                    keyManagerConfigurationDTO.setDisplayName(resultSet.getString("DISPLAY_NAME"));
                    keyManagerConfigurationDTO.setTokenType(resultSet.getString("TOKEN_TYPE"));
                    keyManagerConfigurationDTO.setExternalReferenceId(resultSet.getString("EXTERNAL_REFERENCE_ID"));
                    try (InputStream configuration = resultSet.getBinaryStream("CONFIGURATION")) {
                        String configurationContent = IOUtils.toString(configuration);
                        Map map = new Gson().fromJson(configurationContent, Map.class);
                        keyManagerConfigurationDTO.setAdditionalProperties(map);
                    }
                    return keyManagerConfigurationDTO;
                }
            }
        } catch (SQLException | IOException e) {
            String error = "Error while retrieving key manager configuration for "
                    + id + " in organization " + organization;
            throw new APIManagementException(error, e,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, error));
        }
        return null;
    }

    @Override
    public boolean isIDPExistInOrg(String organization, String resourceId) throws APIManagementException {

        final String query = "SELECT 1 FROM AM_KEY_MANAGER WHERE EXTERNAL_REFERENCE_ID  = ? AND ORGANIZATION = ?";
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setString(1, resourceId);
            preparedStatement.setString(2, organization);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException(
                    "Error while checking key manager for " + resourceId + " in organization " + organization, e);
        }
        return false;
    }

    public boolean isKeyManagerConfigurationExistById(String organization, String id) throws APIManagementException {

        final String query = "SELECT 1 FROM AM_KEY_MANAGER WHERE UUID = ? AND ORGANIZATION = ?";
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setString(1, id);
            preparedStatement.setString(2, organization);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException(
                    "Error while retrieving key manager configuration for " + id + " in organization " + organization,
                    e);
        }
        return false;

    }

    @Override
    public boolean isKeyManagerConfigurationExistByName(String organization, String name)
            throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            final String query = "SELECT 1 FROM AM_KEY_MANAGER WHERE NAME = ? AND ORGANIZATION = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, name);
                preparedStatement.setString(2, organization);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error while retrieving key manager existence", e);
        }
        return false;
    }

    @Override
    public void addKeyManagerConfiguration(KeyManagerConfigurationDTO keyManagerConfigurationDTO)
            throws APIManagementException {

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement preparedStatement = conn
                    .prepareStatement(SQLConstants.KeyManagerSqlConstants.ADD_KEY_MANAGER)) {
                preparedStatement.setString(1, keyManagerConfigurationDTO.getUuid());
                preparedStatement.setString(2, keyManagerConfigurationDTO.getName());
                preparedStatement.setString(3, keyManagerConfigurationDTO.getDescription());
                preparedStatement.setString(4, keyManagerConfigurationDTO.getType());
                String configurationJson = new Gson().toJson(keyManagerConfigurationDTO.getAdditionalProperties());
                preparedStatement.setBinaryStream(5, new ByteArrayInputStream(configurationJson.getBytes()));
                preparedStatement.setString(6, keyManagerConfigurationDTO.getOrganization());
                preparedStatement.setBoolean(7, keyManagerConfigurationDTO.isEnabled());
                preparedStatement.setString(8, keyManagerConfigurationDTO.getDisplayName());
                preparedStatement.setString(9, keyManagerConfigurationDTO.getTokenType());
                preparedStatement.setString(10, keyManagerConfigurationDTO.getExternalReferenceId());
                preparedStatement.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                if (e instanceof SQLIntegrityConstraintViolationException) {
                    if (getKeyManagerConfigurationByName(conn, keyManagerConfigurationDTO.getOrganization(),
                            keyManagerConfigurationDTO.getName()) != null) {
                        log.warn(keyManagerConfigurationDTO.getName() + " Key Manager Already Registered in tenant" +
                                keyManagerConfigurationDTO.getOrganization());
                    } else {
                        throw new APIManagementException("Error while Storing key manager configuration with name " +
                                keyManagerConfigurationDTO.getName() + " in tenant " +
                                keyManagerConfigurationDTO.getOrganization(), e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
                    }
                }
            }
        } catch (SQLException | IOException e) {
            throw new APIManagementException(
                    "Error while Storing key manager configuration with name " + keyManagerConfigurationDTO.getName() +
                            " in tenant " + keyManagerConfigurationDTO.getOrganization(),
                    e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    private KeyManagerConfigurationDTO getKeyManagerConfigurationByName(Connection connection, String organization,
                                                                        String name)
            throws SQLException, IOException {

        final String query = "SELECT * FROM AM_KEY_MANAGER WHERE NAME = ? AND ORGANIZATION = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, organization);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    KeyManagerConfigurationDTO keyManagerConfigurationDTO = new KeyManagerConfigurationDTO();
                    String uuid = resultSet.getString("UUID");
                    keyManagerConfigurationDTO.setUuid(uuid);
                    keyManagerConfigurationDTO.setName(resultSet.getString("NAME"));
                    keyManagerConfigurationDTO.setDisplayName(resultSet.getString("DISPLAY_NAME"));
                    keyManagerConfigurationDTO.setDescription(resultSet.getString("DESCRIPTION"));
                    keyManagerConfigurationDTO.setType(resultSet.getString("TYPE"));
                    keyManagerConfigurationDTO.setEnabled(resultSet.getBoolean("ENABLED"));
                    keyManagerConfigurationDTO.setOrganization(organization);
                    keyManagerConfigurationDTO.setTokenType(resultSet.getString("TOKEN_TYPE"));
                    keyManagerConfigurationDTO.setExternalReferenceId(resultSet.getString("EXTERNAL_REFERENCE_ID"));
                    try (InputStream configuration = resultSet.getBinaryStream("CONFIGURATION")) {
                        String configurationContent = IOUtils.toString(configuration);
                        Map map = new Gson().fromJson(configurationContent, Map.class);
                        keyManagerConfigurationDTO.setAdditionalProperties(map);
                    }
                    return keyManagerConfigurationDTO;
                }
            }
        }
        return null;
    }

    @Override
    public void updateKeyManagerConfiguration(KeyManagerConfigurationDTO keyManagerConfigurationDTO)
            throws APIManagementException {

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement preparedStatement = conn
                    .prepareStatement(SQLConstants.KeyManagerSqlConstants.UPDATE_KEY_MANAGER)) {
                preparedStatement.setString(1, keyManagerConfigurationDTO.getName());
                preparedStatement.setString(2, keyManagerConfigurationDTO.getDescription());
                preparedStatement.setString(3, keyManagerConfigurationDTO.getType());
                String configurationJson = new Gson().toJson(keyManagerConfigurationDTO.getAdditionalProperties());
                preparedStatement.setBinaryStream(4, new ByteArrayInputStream(configurationJson.getBytes()));
                preparedStatement.setString(5, keyManagerConfigurationDTO.getOrganization());
                preparedStatement.setBoolean(6, keyManagerConfigurationDTO.isEnabled());
                preparedStatement.setString(7, keyManagerConfigurationDTO.getDisplayName());
                preparedStatement.setString(8, keyManagerConfigurationDTO.getTokenType());
                preparedStatement.setString(9, keyManagerConfigurationDTO.getExternalReferenceId());
                preparedStatement.setString(10, keyManagerConfigurationDTO.getUuid());
                preparedStatement.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new APIManagementException(
                    "Error while Updating key manager configuration with name " + keyManagerConfigurationDTO.getName() +
                            " in tenant " + keyManagerConfigurationDTO.getOrganization(), e);
        }
    }

    @Override
    public void deleteKeyManagerConfigurationById(String id, String organization) throws APIManagementException {

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement preparedStatement = conn
                    .prepareStatement(SQLConstants.KeyManagerSqlConstants.DELETE_KEY_MANAGER)) {
                preparedStatement.setString(1, id);
                preparedStatement.setString(2, organization);
                preparedStatement.execute();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new APIManagementException(
                    "Error while deleting key manager configuration with id " + id + " in organization " + organization,
                    e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }

    }

    @Override
    public KeyManagerConfigurationDTO getKeyManagerConfigurationByName(String organization, String name)
            throws APIManagementException {

        final String query = "SELECT * FROM AM_KEY_MANAGER WHERE NAME = ? AND ORGANIZATION = ?";
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            return getKeyManagerConfigurationByName(conn, organization, name);
        } catch (SQLException | IOException e) {
            throw new APIManagementException(
                    "Error while retrieving key manager configuration for " + name + " in organization " + organization,
                    e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    public KeyManagerConfigurationDTO getKeyManagerConfigurationByUUID(String uuid)
            throws APIManagementException {

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            return getKeyManagerConfigurationByUUID(conn, uuid);
        } catch (SQLException | IOException e) {
            throw new APIManagementException(
                    "Error while retrieving key manager configuration for key manager uuid: " + uuid, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    private KeyManagerConfigurationDTO getKeyManagerConfigurationByUUID(Connection connection, String uuid)
            throws SQLException, IOException {

        final String query = "SELECT * FROM AM_KEY_MANAGER WHERE UUID = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, uuid);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    KeyManagerConfigurationDTO keyManagerConfigurationDTO = new KeyManagerConfigurationDTO();
                    keyManagerConfigurationDTO.setUuid(resultSet.getString("UUID"));
                    keyManagerConfigurationDTO.setName(resultSet.getString("NAME"));
                    keyManagerConfigurationDTO.setDisplayName(resultSet.getString("DISPLAY_NAME"));
                    keyManagerConfigurationDTO.setDescription(resultSet.getString("DESCRIPTION"));
                    keyManagerConfigurationDTO.setType(resultSet.getString("TYPE"));
                    keyManagerConfigurationDTO.setEnabled(resultSet.getBoolean("ENABLED"));
                    keyManagerConfigurationDTO.setOrganization(resultSet.getString("ORGANIZATION"));
                    keyManagerConfigurationDTO.setTokenType(resultSet.getString("TOKEN_TYPE"));
                    keyManagerConfigurationDTO.setExternalReferenceId(resultSet.getString("EXTERNAL_REFERENCE_ID"));
                    try (InputStream configuration = resultSet.getBinaryStream("CONFIGURATION")) {
                        String configurationContent = IOUtils.toString(configuration);
                        Map map = new Gson().fromJson(configurationContent, Map.class);
                        keyManagerConfigurationDTO.setAdditionalProperties(map);
                    }
                    return keyManagerConfigurationDTO;
                }
            }
        }
        return null;
    }


}
