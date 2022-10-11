package org.wso2.carbon.apimgt.impl.dao.impl;

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ErrorHandler;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.impl.dao.KeyManagerDAO;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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



}
