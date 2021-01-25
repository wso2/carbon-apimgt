package org.wso2.carbon.graphql.api.devportal.data;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.persistence.APIConstants;
import org.wso2.carbon.apimgt.persistence.APIPersistence;
import org.wso2.carbon.apimgt.persistence.PersistenceManager;
import org.wso2.carbon.apimgt.persistence.dto.Organization;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.OASPersistenceException;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.graphql.api.devportal.ArtifactData;
//import org.wso2.carbon.graphql.api.devportal.modules.APIDTO;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.Tag;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class APIDTOData {

    APIPersistence apiPersistenceInstance;


   public static final String GET_API_TYPE = "SELECT API.API_TYPE FROM AM_API API WHERE API.API_UUID = ? ";
    public static final String GET_API_CREATED_TIME = "SELECT API.CREATED_TIME FROM AM_API API WHERE API.API_UUID = ? ";
    public static final String GET_API_UPDATED_TIME = "SELECT API.UPDATED_TIME FROM AM_API API WHERE API.API_UUID = ? ";


    public String getApiType(String Id){
        String type = null;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_API_TYPE)) {
            statement.setString(1, Id);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                type = resultSet.getString("API_TYPE");
            }
        } catch (SQLException e) {

        }
        return type;
    }
    public String getApiCreatedTime(String Id){
        String createdTime = null;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_API_CREATED_TIME)) {
            statement.setString(1, Id);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                createdTime = resultSet.getString("CREATED_TIME");
            }
        } catch (SQLException e) {

        }
        return createdTime;
    }
    public String getApiLastUpdateTime(String Id){
        String lastUpdate = null;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_API_UPDATED_TIME)) {
            statement.setString(1, Id);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                lastUpdate = resultSet.getString("UPDATED_TIME");
            }
        } catch (SQLException e) {

        }
        return lastUpdate;
    }
    public String getApiDefinition(String Id) throws OASPersistenceException {


        apiPersistenceInstance = PersistenceManager.getPersistenceInstance("wso2.anonymous.user");
        String TenantDomain = RestApiUtil.getRequestedTenantDomain(null);
        Organization org = new Organization(TenantDomain);
        String apiDefinition = apiPersistenceInstance.getOASDefinition(org, Id); //

        return apiDefinition;

    }

}
