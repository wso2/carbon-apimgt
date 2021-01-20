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


    public static final String GET_API_DATA = "SELECT * FROM AM_API API WHERE API.API_UUID = ? ";
    public static final String GET_API_NAME = "SELECT API.API_NAME FROM AM_API API WHERE API.API_UUID = ? ";
    public static final String GET_API_CONTEXT = "SELECT API.CONTEXT FROM AM_API API WHERE API.API_UUID = ? ";
    public static final String GET_API_VERSION = "SELECT API.API_VERSION FROM AM_API API WHERE API.API_UUID = ? ";
    public static final String GET_API_PROVIDER = "SELECT API.API_PROVIDER FROM AM_API API WHERE API.API_UUID = ? ";
    public static final String GET_API_TYPE = "SELECT API.API_TYPE FROM AM_API API WHERE API.API_UUID = ? ";
    public static final String GET_API_CREATED_TIME = "SELECT API.CREATED_TIME FROM AM_API API WHERE API.API_UUID = ? ";
    public static final String GET_API_UPDATED_TIME = "SELECT API.UPDATED_TIME FROM AM_API API WHERE API.API_UUID = ? ";


    public static final String GET_API_ID = "SELECT API.API_ID FROM AM_API API ";

//    public APIDTO getApiData(String Id){
//        //List<APIDTO> apidtos = new ArrayList<>();
//
//        String uuid = null;
//        String apiName = null;
//        String context= null;
//        String version= null;
//        String provider= null;
//        String type= null;
//        String createdTime= null;
//        String lastUpdate= null;
//        try (Connection connection = APIMgtDBUtil.getConnection();
//             PreparedStatement statement = connection.prepareStatement(GET_API_DATA)) {
//             statement.setString(1, Id);
//
//            ResultSet resultSet = statement.executeQuery();
//
//            while (resultSet.next()) {
//               uuid = resultSet.getString("API_UUID");
//                 apiName = resultSet.getString("API_NAME");
//                 context = resultSet.getString("CONTEXT");
//                 version = resultSet.getString("API_VERSION");
//                 provider = resultSet.getString("API_PROVIDER");
//                 type = resultSet.getString("API_TYPE");
//                 createdTime = resultSet.getString("CREATED_TIME");
//                 lastUpdate = resultSet.getString("UPDATED_TIME");
//
//
//            }
//        } catch (SQLException e) {
//
//        }
//
//        return new APIDTO(uuid,apiName,context,version,provider,type,createdTime,lastUpdate);
//
//    }
    public int getApiCount() throws RegistryException, UserStoreException, APIManagementException, APIPersistenceException {
        int count = 0;
        //int apiId = 0;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_API_ID)) {
            //statement.setString(1, Id);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                //apiId = resultSet.getInt("API_ID");
                count = count+1;
            }
        } catch (SQLException e) {

        }
        return count;

//        ArtifactData artifactData = new ArtifactData();
//        return artifactData.getDevportalAPIS();
    }

//    public String getApiName(String Id){
//        String apiName = null;
//        try (Connection connection = APIMgtDBUtil.getConnection();
//             PreparedStatement statement = connection.prepareStatement(GET_API_NAME)) {
//            statement.setString(1, Id);
//
//            ResultSet resultSet = statement.executeQuery();
//
//            while (resultSet.next()) {
//                apiName = resultSet.getString("API_NAME");
//            }
//        } catch (SQLException e) {
//
//        }
//        return apiName;
//    }
//    public String getApiContext(String Id){
//        String Context = null;
//        try (Connection connection = APIMgtDBUtil.getConnection();
//             PreparedStatement statement = connection.prepareStatement(GET_API_CONTEXT)) {
//            statement.setString(1, Id);
//
//            ResultSet resultSet = statement.executeQuery();
//
//            while (resultSet.next()) {
//                Context = resultSet.getString("CONTEXT");
//            }
//        } catch (SQLException e) {
//
//        }
//        return Context;
//    }
//    public String getApiVersion(String Id){
//        String version = null;
//        try (Connection connection = APIMgtDBUtil.getConnection();
//             PreparedStatement statement = connection.prepareStatement(GET_API_VERSION)) {
//            statement.setString(1, Id);
//
//            ResultSet resultSet = statement.executeQuery();
//
//            while (resultSet.next()) {
//                version = resultSet.getString("API_VERSION");
//            }
//        } catch (SQLException e) {
//
//        }
//        return version;
//    }
//    public String getApiProvider(String Id){
//        String provider = null;
//        try (Connection connection = APIMgtDBUtil.getConnection();
//             PreparedStatement statement = connection.prepareStatement(GET_API_PROVIDER)) {
//            statement.setString(1, Id);
//
//            ResultSet resultSet = statement.executeQuery();
//
//            while (resultSet.next()) {
//                provider = resultSet.getString("API_PROVIDER");
//            }
//        } catch (SQLException e) {
//
//        }
//        return provider;
//    }
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

//    public String getAdditionalProperties(String Id) throws UserStoreException, RegistryException {
//
//        ArtifactData artifactData = new ArtifactData();
//        Registry registry = artifactData.getRegistry();
//
//        String artifactPath = GovernanceUtils.getArtifactPath(registry, Id);
//
//        Map<String,String> additionalProperties = new HashMap<>();
//
//
//        Resource apiResource = registry.get(artifactPath);
//        Properties properties = apiResource.getProperties();
//
//        if (properties != null) {
//            Enumeration propertyNames = properties.propertyNames();
//            while (propertyNames.hasMoreElements()) {
//                String propertyName = (String) propertyNames.nextElement();
//                if (propertyName.startsWith(APIConstants.API_RELATED_CUSTOM_PROPERTIES_PREFIX)) {
//                    additionalProperties.put(propertyName.substring(APIConstants.API_RELATED_CUSTOM_PROPERTIES_PREFIX.length()),
//                            apiResource.getProperty(propertyName));
//                }
//            }
//        }
//        return getAdditionalPropertiesFromMap(additionalProperties);
//    }

//    public String getAdditionalPropertiesFromMap(Map<String, String> additionalProperties){
//        //Map<String, String> additionalProperties = apiTypeWrapper.getApi().getAdditionalProperties();
//
//        List<String> additionalPropertiesList = new ArrayList<>();
//
//        String alladditionalProperties= null;
//
//        for (String key : additionalProperties.keySet()) {
//            String properties = key + ":" + additionalProperties.get(key);
//            additionalPropertiesList.add(properties);
//        }
//        if(additionalPropertiesList!=null){
//            alladditionalProperties= String.join(",",additionalPropertiesList);
//
//        }
//        return alladditionalProperties;
//    }

    public String getApiDefinition(String Id) throws OASPersistenceException {


        apiPersistenceInstance = PersistenceManager.getPersistenceInstance("wso2.anonymous.user");
        String TenantDomain = RestApiUtil.getRequestedTenantDomain(null);
        Organization org = new Organization(TenantDomain);
        String apiDefinition = apiPersistenceInstance.getOASDefinition(org, Id); //

        return apiDefinition;

    }
//    public String getTags(String Id) throws UserStoreException, RegistryException {
//
//        ArtifactData artifactData = new ArtifactData();
//        Registry registry = artifactData.getRegistry();
//        String artifactPath = GovernanceUtils.getArtifactPath(registry, Id);
//
//        Set<String> tagSet = new HashSet<String>();
//        org.wso2.carbon.registry.core.Tag[] tag = registry.getTags(artifactPath);
//        for (Tag tag1 : tag) {
//            tagSet.add(tag1.getTagName());
//        }
//        return getTags(tagSet);
//    }
    public String getTags(Set<String> tagset){

        String tags = null;
        if (tagset!=null){
            List<String> tagList = new ArrayList<>(tagset);
            tags = String.join(",",tagList);
        }
        return tags;
    }
}
