package org.wso2.carbon.apimgt.usage.client;

import com.google.gson.Gson;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.usage.client.billing.APIUsageRangeCost;
import org.wso2.carbon.apimgt.usage.client.dto.*;
import org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException;
import org.wso2.carbon.apimgt.usage.client.internal.APIUsageClientServiceComponent;
import org.wso2.carbon.apimgt.usage.client.pojo.APIFirstAccess;
import org.wso2.carbon.core.util.CryptoUtil;

import java.sql.*;
import java.util.*;

/**
 * Created by rukshan on 10/6/15.
 */
public abstract class APIUsageStatisticsClient {

    protected static Map<String, String> subscriberAppsMap = new HashMap<String, String>();

    public abstract void initializeDataSource() throws APIMgtUsageQueryServiceClientException;

    public abstract String perAppPerAPIUsage(String subscriberName, String groupId, String fromDate, String toDate,
            int limit) throws APIMgtUsageQueryServiceClientException;

    public abstract String getTopAppUsers(String subscriberName, String groupId, String fromDate, String toDate,
            int limit) throws APIMgtUsageQueryServiceClientException;

    public abstract String getAppApiCallType(String subscriberName, String groupId, String fromDate, String toDate,
            int limit) throws APIMgtUsageQueryServiceClientException;

    public abstract String getPerAppFaultCount(String subscriberName, String groupId, String fromDate, String toDate,
            int limit) throws APIMgtUsageQueryServiceClientException;

    public abstract List<APIUsageByUserDTO> getAPIUsageByUser(String providerName, String fromDate, String toDate)
            throws APIMgtUsageQueryServiceClientException;

    public abstract List<APIResponseTimeDTO> getProviderAPIServiceTime(String providerName, String fromDate,
            String toDate, int limit)
            throws APIMgtUsageQueryServiceClientException;

    public abstract List<APIVersionLastAccessTimeDTO> getProviderAPIVersionUserLastAccess(String providerName,
            String fromDate, String toDate, int limit)
            throws APIMgtUsageQueryServiceClientException;

    public abstract List<APIResourcePathUsageDTO> getAPIUsageByResourcePath(String providerName, String fromDate,
            String toDate)
            throws APIMgtUsageQueryServiceClientException;

    public abstract List<APIDestinationUsageDTO> getAPIUsageByDestination(String providerName, String fromDate,
            String toDate)
            throws APIMgtUsageQueryServiceClientException;

    public abstract List<APIUsageDTO> getProviderAPIUsage(String providerName, String fromDate, String toDate,
            int limit)
            throws APIMgtUsageQueryServiceClientException;

    public abstract List<APIResponseFaultCountDTO> getAPIResponseFaultCount(String providerName, String fromDate,
            String toDate)
            throws APIMgtUsageQueryServiceClientException;

    public abstract List<APIThrottlingOverTimeDTO> getThrottleDataOfAPIAndApplication(String apiName, String provider,
            String appName, String fromDate, String toDate, String groupBy)
            throws APIMgtUsageQueryServiceClientException;

    public abstract List<APIThrottlingOverTimeDTO> getThrottleDataOfApplication(String appName, String provider,
            String fromDate, String toDate) throws APIMgtUsageQueryServiceClientException;

    public abstract List<String> getAPIsForThrottleStats(String provider) throws APIMgtUsageQueryServiceClientException;

    public abstract List<String> getAppsForThrottleStats(String provider, String apiName)
            throws APIMgtUsageQueryServiceClientException;

    public abstract List<APIVersionUsageDTO> getUsageByAPIVersions(String providerName, String apiName, String fromDate,
            String toDate) throws APIMgtUsageQueryServiceClientException;

    public abstract List<APIFirstAccess> getFirstAccessTime(String providerName) throws APIMgtUsageQueryServiceClientException;

    public String getAppRegisteredUsers(String subscriberName, String groupId)
            throws APIMgtUsageQueryServiceClientException {

        List<String> subscriberApps = getAppsBySubscriber(subscriberName, groupId);

        Collection<AppRegisteredUsersDTO> usageData = getAppUsers();

        List<RegisteredAppUsersDTO> appUserList = new ArrayList<RegisteredAppUsersDTO>();
        RegisteredAppUsersDTO appUsers;
        for (AppRegisteredUsersDTO usage : usageData) {
            for (String subscriberApp : subscriberApps) {
                if (subscriberApp != null && subscriberApp.equals(usage.getconsumerKey())) {

                    String appName = subscriberAppsMap.get(usage.getconsumerKey());
                    String user = usage.getUser();

                    boolean found = false;
                    for (RegisteredAppUsersDTO dto : appUserList) {
                        if (dto.getAppName().equals(appName)) {
                            dto.addToUserArray(user);
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        appUsers = new RegisteredAppUsersDTO();
                        appUsers.setAppName(appName);
                        appUsers.addToUserArray(user);
                        appUserList.add(appUsers);
                    }

                }
            }
        }

        Gson gson = new Gson();
        return gson.toJson(appUserList);
    }

    protected List<String> getAppsBySubscriber(String subscriberName, String groupId)
            throws APIMgtUsageQueryServiceClientException {

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            connection = APIMgtDBUtil.getConnection();

            String query = "SELECT CONSUMER_KEY, NAME FROM AM_APPLICATION_KEY_MAPPING INNER JOIN AM_APPLICATION ON " +
                    "AM_APPLICATION_KEY_MAPPING.APPLICATION_ID=AM_APPLICATION.APPLICATION_ID INNER JOIN " +
                    "AM_SUBSCRIBER" +
                    " ON AM_APPLICATION.SUBSCRIBER_ID = AM_SUBSCRIBER.SUBSCRIBER_ID WHERE ";

            boolean sharedApp;
            if (groupId != null && !"".equals(groupId)) {
                query = query + "AM_APPLICATION.GROUP_ID = ? ";
                sharedApp = true;
            } else {
                query = query + "AM_SUBSCRIBER.USER_ID = ? ";
                sharedApp = false;
            }

            statement = connection.prepareStatement(query);

            if (!sharedApp) {
                statement.setString(1, subscriberName);
            } else {
                statement.setString(1, groupId);
            }

            rs = statement.executeQuery();

            List<String> consumerKeys = new ArrayList<String>();
            while (rs.next()) {
                String consumerKey = rs.getString("CONSUMER_KEY");
                String appName = rs.getString("NAME");
                APIManagerConfiguration config = APIUsageClientServiceComponent.getAPIManagerConfiguration();
                String tokenEncryptionConfig = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_ENCRYPT_TOKENS);

                boolean isTokenEncryptionEnabled = Boolean.parseBoolean(tokenEncryptionConfig);

                if (isTokenEncryptionEnabled) {
                    String decryptedConsumerKey = new String(
                            CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(consumerKey));
                    consumerKeys.add(decryptedConsumerKey);
                    subscriberAppsMap.put(decryptedConsumerKey, appName);
                } else {
                    consumerKeys.add(consumerKey);
                    subscriberAppsMap.put(consumerKey, appName);
                }
            }
            return consumerKeys;

        } catch (Exception e) {
            throw new APIMgtUsageQueryServiceClientException("Error occurred while querying from JDBC database", e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {

                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {

                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {

                }
            }
        }
    }

    private List<AppRegisteredUsersDTO> getAppUsers() throws APIMgtUsageQueryServiceClientException {

        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            connection = APIMgtDBUtil.getConnection();
            statement = connection.createStatement();
            String query;

            query = "SELECT CONSUMER_KEY,AUTHZ_USER FROM IDN_OAUTH2_ACCESS_TOKEN AS A INNER JOIN IDN_OAUTH_CONSUMER_APPS AS B ON B.ID=A.CONSUMER_KEY_ID";

            rs = statement.executeQuery(query);

            int columnCount = rs.getMetaData().getColumnCount();
            List<AppRegisteredUsersDTO> usageData = new ArrayList<AppRegisteredUsersDTO>();
            AppRegisteredUsersDTO appRegUsersDTO;
            while (rs.next()) {
                String[] appDetail = new String[2];
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rs.getMetaData().getColumnName(i);
                    String columnValue = rs.getString(columnName);
                    appDetail[i - 1] = columnValue;

                }
                appRegUsersDTO = new AppRegisteredUsersDTO();
                appRegUsersDTO.setconsumerKey(appDetail[0]);
                appRegUsersDTO.setUser(appDetail[1]);
                usageData.add(appRegUsersDTO);

            }
            return usageData;

        } catch (Exception e) {
            throw new APIMgtUsageQueryServiceClientException("Error occurred while querying from JDBC database", e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {

                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {

                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {

                }
            }
        }
    }

    public abstract List<APIUsageRangeCost> evaluate(String param, int calls) throws Exception;

    public abstract void deployArtifacts(String url,String user,String pass) throws Exception;
}