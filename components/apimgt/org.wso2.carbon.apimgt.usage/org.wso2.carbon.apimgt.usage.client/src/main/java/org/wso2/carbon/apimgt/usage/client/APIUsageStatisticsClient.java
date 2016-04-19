/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.carbon.apimgt.usage.client;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.usage.client.bean.ExecutionTimeOfAPIValues;
import org.wso2.carbon.apimgt.usage.client.bean.PerGeoLocationUsageCount;
import org.wso2.carbon.apimgt.usage.client.bean.Result;
import org.wso2.carbon.apimgt.usage.client.bean.UserAgentUsageCount;
import org.wso2.carbon.apimgt.usage.client.billing.APIUsageRangeCost;
import org.wso2.carbon.apimgt.usage.client.dto.*;
import org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException;
import org.wso2.carbon.apimgt.usage.client.internal.APIUsageClientServiceComponent;
import org.wso2.carbon.apimgt.usage.client.pojo.APIFirstAccess;
import org.wso2.carbon.core.util.CryptoUtil;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

import static java.util.Collections.sort;

/**
 * Abstract class and act as a interface for the Statistic usage client for APIM.
 * Known implementations are,
 * org.wso2.carbon.apimgt.usage.client.impl.APIUsageStatisticsRestClientImpl
 * org.wso2.carbon.apimgt.usage.client.impl.APIUsageStatisticsRdbmsClientImpl
 */
public abstract class APIUsageStatisticsClient {

    protected final Map<String, String> subscriberAppsMap = new HashMap<String, String>();
    private static final Log log = LogFactory.getLog(APIUsageStatisticsClient.class);

    /**
     * initialize datasource of implemented APIUsageStatisticsRestClient
     *
     * @throws APIMgtUsageQueryServiceClientException
     */
    public abstract void initializeDataSource() throws APIMgtUsageQueryServiceClientException;

    /**
     * This method gets the API usage data per application
     *
     * @param subscriberName subscriber name
     * @param groupId        group id of the subscriber
     * @param fromDate       starting date
     * @param toDate         ending data
     * @param limit          limit of the result
     * @return return list of PerAppApiCountDTO result
     * @throws APIMgtUsageQueryServiceClientException
     */
    public abstract List<PerAppApiCountDTO> perAppPerAPIUsage(String subscriberName, String groupId, String fromDate,
            String toDate, int limit) throws APIMgtUsageQueryServiceClientException;

    /**
     * This method gets the app users stat for invoking APIs
     *
     * @param subscriberName subscriber name
     * @param groupId        group id of the subscriber
     * @param fromDate       starting date
     * @param toDate         ending data
     * @param limit          limit of the result
     * @return return list of AppUsageDTO result
     * @throws APIMgtUsageQueryServiceClientException
     */
    public abstract List<AppUsageDTO> getTopAppUsers(String subscriberName, String groupId, String fromDate,
            String toDate, int limit) throws APIMgtUsageQueryServiceClientException;

    /**
     * @param subscriberName subscriber name
     * @param groupId        group id of the subscriber
     * @param fromDate       starting date
     * @param toDate         ending data
     * @param limit          limit of the result
     * @return list of AppCallTypeDTO result
     * @throws APIMgtUsageQueryServiceClientException
     */
    public abstract List<AppCallTypeDTO> getAppApiCallType(String subscriberName, String groupId, String fromDate,
            String toDate, int limit) throws APIMgtUsageQueryServiceClientException;

    /**
     * @param subscriberName subscriber name
     * @param groupId        group id of the subscriber
     * @param fromDate       starting date
     * @param toDate         ending data
     * @param limit          limit of the result
     * @return list of FaultCountDTO result
     * @throws APIMgtUsageQueryServiceClientException
     */
    public abstract List<FaultCountDTO> getPerAppAPIFaultCount(String subscriberName, String groupId, String fromDate,
            String toDate, int limit) throws APIMgtUsageQueryServiceClientException;

    /**
     * Returns a list of APIUsageByUserDTO objects that contain information related to
     * User wise API Usage, along with the number of invocations, and API Version
     *
     * @param providerName Name of the API provider
     * @param fromDate     starting date of the results
     * @param toDate       ending date of the results
     * @return list of APIUsageByUserDTO
     * @throws APIMgtUsageQueryServiceClientException
     */
    public abstract List<APIUsageByUserDTO> getAPIUsageByUser(String providerName, String fromDate, String toDate)
            throws APIMgtUsageQueryServiceClientException;

    /**
     * Gets a list of APIResponseTimeDTO objects containing information related to APIs belonging
     * to a particular provider along with their average response times.
     *
     * @param providerName Name of the API provider
     * @param fromDate     starting date of the results
     * @param toDate       ending date of the results
     * @param limit        limit of the result
     * @return a List of APIResponseTimeDTO objects, possibly empty
     * @throws APIMgtUsageQueryServiceClientException
     */
    public abstract List<APIResponseTimeDTO> getProviderAPIServiceTime(String providerName, String fromDate,
            String toDate, int limit) throws APIMgtUsageQueryServiceClientException;

    /**
     * Returns a list of APIVersionLastAccessTimeDTO objects for all the APIs belonging to the
     * specified provider. Last access times are calculated without taking API versions into
     * account. That is all the versions of an API are treated as one.
     *
     * @param providerName Name of the API provider
     * @param fromDate     starting date of the results
     * @param toDate       ending date of the results
     * @param limit        limit of the result
     * @return a list of APIVersionLastAccessTimeDTO objects, possibly empty
     * @throws APIMgtUsageQueryServiceClientException
     */
    public abstract List<APIVersionLastAccessTimeDTO> getProviderAPIVersionUserLastAccess(String providerName,
            String fromDate, String toDate, int limit) throws APIMgtUsageQueryServiceClientException;

    /**
     * Returns a list of APIVersionUsageDTO objects that contain information related to a
     * particular API of a specified provider, along with the number of API calls processed
     * by each resource path of that API.
     *
     * @param providerName Name of the API provider
     * @param fromDate     starting date of the results
     * @param toDate       ending date of the results
     * @return a List of APIResourcePathUsageDTO objects, possibly empty
     * @throws APIMgtUsageQueryServiceClientException
     */
    public abstract List<APIResourcePathUsageDTO> getAPIUsageByResourcePath(String providerName, String fromDate,
            String toDate) throws APIMgtUsageQueryServiceClientException;

    /**
     * Gets a list of APIDestinationUsageDTO objects containing information related to APIs belonging
     * to a particular provider
     *
     * @param providerName Name of the API provider
     * @param fromDate     starting date of the results
     * @param toDate       ending date of the results
     * @return a List of APIDestinationUsageDTO objects, possibly empty
     * @throws APIMgtUsageQueryServiceClientException
     */
    public abstract List<APIDestinationUsageDTO> getAPIUsageByDestination(String providerName, String fromDate,
            String toDate) throws APIMgtUsageQueryServiceClientException;

    /**
     * Returns a list of APIUsageDTO objects that contain information related to APIs that
     * belong to a particular provider and the number of total API calls each API has processed
     * up to now. This method does not distinguish between different API versions. That is all
     * versions of a single API are treated as one, and their individual request counts are summed
     * up to calculate a grand total per each API.
     *
     * @param providerName Name of the API provider
     * @param fromDate     starting date of the results
     * @param toDate       ending date of the results
     * @param limit        limit of the result
     * @return a List of APIUsageDTO objects - possibly empty
     * @throws APIMgtUsageQueryServiceClientException
     */
    public abstract List<APIUsageDTO> getProviderAPIUsage(String providerName, String fromDate, String toDate,
            int limit) throws APIMgtUsageQueryServiceClientException;

    /**
     * Gets a list of APIResponseFaultCountDTO objects containing information related to APIs belonging
     * to a particular provider
     *
     * @param providerName Name of the API provider
     * @param fromDate     starting date of the results
     * @param toDate       ending date of the results
     * @return a List of APIResponseFaultCountDTO objects, possibly empty
     * @throws APIMgtUsageQueryServiceClientException
     */
    public abstract List<APIResponseFaultCountDTO> getAPIResponseFaultCount(String providerName, String fromDate,
            String toDate) throws APIMgtUsageQueryServiceClientException;

    /**
     * Given API name and Application, returns throttling request counts over time for a given time span
     *
     * @param apiName  Name of the API
     * @param provider Provider name
     * @param appName  Application name
     * @param fromDate Start date of the time span
     * @param toDate   End date of time span
     * @param groupBy  Group by parameter. Supported parameters are :day,hour
     * @return Throttling counts over time
     * @throws APIMgtUsageQueryServiceClientException
     */
    public abstract List<APIThrottlingOverTimeDTO> getThrottleDataOfAPIAndApplication(String apiName, String provider,
            String appName, String fromDate, String toDate, String groupBy)
            throws APIMgtUsageQueryServiceClientException;

    /**
     * Given Application name and the provider, returns throttle data for the APIs of the provider invoked by the
     * given application
     *
     * @param appName  Application name
     * @param provider Provider name
     * @param fromDate Start date of the time span
     * @param toDate   End date of time span
     * @return Throttling counts of APIs of the provider invoked by the given app
     * @throws APIMgtUsageQueryServiceClientException
     */
    public abstract List<APIThrottlingOverTimeDTO> getThrottleDataOfApplication(String appName, String provider,
            String fromDate, String toDate) throws APIMgtUsageQueryServiceClientException;

    /**
     * Get APIs of the provider that consist of throttle data
     *
     * @param provider Provider name
     * @return List of APIs of the provider that consist of throttle data
     * @throws APIMgtUsageQueryServiceClientException
     */
    public abstract List<String> getAPIsForThrottleStats(String provider) throws APIMgtUsageQueryServiceClientException;

    /**
     * Given provider name and the API name, returns a list of applications through which the corresponding API is
     * invoked and which consist of success/throttled requests
     *
     * @param provider Provider name
     * @param apiName  Name of th API
     * @return A list of applications through which the corresponding API is invoked and which consist of throttle data
     * @throws APIMgtUsageQueryServiceClientException
     */
    public abstract List<String> getAppsForThrottleStats(String provider, String apiName)
            throws APIMgtUsageQueryServiceClientException;

    /**
     * Returns a list of APIVersionUsageDTO objects that contain information related to a
     * particular API of a specified provider, along with the number of API calls processed
     * by each version of that API for a particular time preriod.
     *
     * @param providerName Name of the API provider
     * @param apiName      name of the API
     * @param fromDate     starting date of the results
     * @param toDate       ending date of the results
     * @return a list containing the data related to API usage
     * @throws org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException
     */
    public abstract List<APIVersionUsageDTO> getUsageByAPIVersions(String providerName, String apiName, String fromDate,
            String toDate) throws APIMgtUsageQueryServiceClientException;

    /**
     * Return the First access date according to the REST API table data
     *
     * @param providerName provider name
     * @return APIFirstAccess containing date
     * @throws APIMgtUsageQueryServiceClientException
     */
    public abstract List<APIFirstAccess> getFirstAccessTime(String providerName)
            throws APIMgtUsageQueryServiceClientException;

    /**
     * get the applications by the subscribers
     *
     * @param subscriberName subscriber name
     * @param groupId        group id of the subscriber
     * @return list of RegisteredAppUsersDTO
     * @throws APIMgtUsageQueryServiceClientException
     */
    public List<RegisteredAppUsersDTO> getPerAppSubscribers(String subscriberName, String groupId)
            throws APIMgtUsageQueryServiceClientException {

        //get the list of app subscribers
        List<String> subscriberApps = getAppsBySubscriber(subscriberName, groupId);

        //get the app users
        List<AppRegisteredUsersDTO> usageData = getAppUsers();

        List<RegisteredAppUsersDTO> appUserList = new ArrayList<RegisteredAppUsersDTO>();
        RegisteredAppUsersDTO appUsers;
        //iterate over all the app users data
        for (AppRegisteredUsersDTO usage : usageData) {
            //iterate over all the subscribers
            for (String subscriberApp : subscriberApps) {
                if (subscriberApp != null && subscriberApp.equals(usage.getconsumerKey())) {

                    //get the app name from the key
                    String appName = subscriberAppsMap.get(usage.getconsumerKey());
                    String user = usage.getUser();

                    boolean found = false;
                    //add users to existing RegisteredAppUsersDTO object
                    for (RegisteredAppUsersDTO dto : appUserList) {
                        if (dto.getAppName().equals(appName)) {
                            dto.addToUserArray(user);
                            found = true;
                            break;
                        }
                    }

                    //if not RegisteredAppUsersDTO create new
                    if (!found) {
                        appUsers = new RegisteredAppUsersDTO();
                        appUsers.setAppName(appName);
                        appUsers.addToUserArray(user);
                        appUserList.add(appUsers);
                    }

                }
            }
        }
        return appUserList;
    }

    /**
     * get the list if application of subscribers
     *
     * @param subscriberName subscriber name
     * @param groupId        group of the subscriber
     * @return list of string contain the application name
     * @throws APIMgtUsageQueryServiceClientException
     */
    protected List<String> getAppsBySubscriber(String subscriberName, String groupId)
            throws APIMgtUsageQueryServiceClientException {

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            //get the connection
            connection = APIMgtDBUtil.getConnection();

            //make query
            String query = "SELECT CONSUMER_KEY, NAME FROM AM_APPLICATION_KEY_MAPPING INNER JOIN AM_APPLICATION ON " +
                    "AM_APPLICATION_KEY_MAPPING.APPLICATION_ID=AM_APPLICATION.APPLICATION_ID INNER JOIN " +
                    "AM_SUBSCRIBER" +
                    " ON AM_APPLICATION.SUBSCRIBER_ID = AM_SUBSCRIBER.SUBSCRIBER_ID WHERE ";

            //check is it shared application
            boolean sharedApp;
            if (!StringUtils.isEmpty(groupId)) {
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

            //execute
            rs = statement.executeQuery();

            List<String> consumerKeys = new ArrayList<String>();

            //iterate over the results
            while (rs.next()) {
                String consumerKey = rs.getString("CONSUMER_KEY");
                String appName = rs.getString("NAME");
                APIManagerConfiguration config = APIUsageClientServiceComponent.getAPIManagerConfiguration();
                String tokenEncryptionConfig = config.getFirstProperty(APIConstants.ENCRYPT_TOKENS_ON_PERSISTENCE);

                boolean isTokenEncryptionEnabled = Boolean.parseBoolean(tokenEncryptionConfig);

                //check encription enable
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

    /**
     * get the users of the application
     *
     * @return list of AppRegisteredUsersDTO contain the applications and users information
     * @throws APIMgtUsageQueryServiceClientException
     */
    private List<AppRegisteredUsersDTO> getAppUsers() throws APIMgtUsageQueryServiceClientException {

        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            connection = APIMgtDBUtil.getConnection();
            statement = connection.createStatement();
            String query;

            //make the query
            query = "SELECT CONSUMER_KEY,AUTHZ_USER FROM (select * from IDN_OAUTH2_ACCESS_TOKEN where "
                    + "TOKEN_STATE='ACTIVE') AS tokenTable INNER JOIN IDN_OAUTH_CONSUMER_APPS AS appsTable ON "
                    + "appsTable.ID=tokenTable.CONSUMER_KEY_ID";

            //get the result set
            rs = statement.executeQuery(query);

            int columnCount = rs.getMetaData().getColumnCount();
            List<AppRegisteredUsersDTO> usageData = new ArrayList<AppRegisteredUsersDTO>();
            AppRegisteredUsersDTO appRegUsersDTO;

            //iterate over result
            while (rs.next()) {
                String[] appDetail = new String[2];
                //put all the column value to the array
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

    /**
     * Custom artifacts deployment. deploy capp related to REST API client on DAS
     *
     * @param url  url of the DAS
     * @param user user name
     * @param pass password
     * @throws Exception general exception throws, because different exception can occur
     */
    public abstract void deployArtifacts(String url, String user, String pass) throws Exception;

    /**
     * return list of api usage for a particular api accross all versions
     *
     * @param providerName API provider name
     * @param apiName      Name of the API
     * @param limit        Number of sorted entries to return
     * @return a List of PerUserAPIUsageDTO objects - Possibly empty
     * @throws org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException on error
     */
    public abstract List<PerUserAPIUsageDTO> getUsageBySubscribers(String providerName, String apiName, int limit)
            throws APIMgtUsageQueryServiceClientException;

    /**
     * return list of api usage for a particular api and version
     *
     * @param providerName API provider name
     * @param apiName      Name of the API
     * @param limit        Number of sorted entries to return
     * @return a List of PerUserAPIUsageDTO objects - Possibly empty
     * @throws org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException on error
     */
    public abstract List<PerUserAPIUsageDTO> getUsageBySubscribers(String providerName, String apiName,
            String apiVersion, int limit) throws APIMgtUsageQueryServiceClientException;

    /**
     * return a string to indicate type of statistics client
     * @return String
     */
    public abstract String getClientType();

    /**
     * Return list of Latency time for given api and its version
     *
     * @param apiName - Name of th API
     * @param version - Version of the API
     * @param tenantDomain - TenantDomain
     * @param fromDate - Start date of the time span
     * @param toDate - End date of time span
     * @param drillDown - Type of data
     * @return List of latency Times
     * @throws APIMgtUsageQueryServiceClientException
     */

     public abstract List<Result<ExecutionTimeOfAPIValues>> getExecutionTimeByAPI(String apiName, String version,
                                                                                 String tenantDomain, String fromDate,
                                                                                 String toDate, String drillDown) throws
            APIMgtUsageQueryServiceClientException;

    /**
     * Return list of Latency time for given api and its version
     *
     * @param apiName - Name of th API
     * @param version - Version of the API
     * @param tenantDomain - TenantDomain
     * @param fromDate - Start date of the time span
     * @param toDate - End date of time span
     * @param drillDown - Type of data
     * @param mediationType - type of mediation
     * @return List of latency Times
     * @throws APIMgtUsageQueryServiceClientException
     */

    public abstract List<Result<ExecutionTimeOfAPIValues>> getExecutionTimeByAPI(String apiName, String version,
                                                                                 String tenantDomain, String fromDate,
                                                                                 String toDate, String drillDown,
                                                                                 String mediationType) throws
            APIMgtUsageQueryServiceClientException;


    /**
     *  Used to get long value of String date.
     * @param date Date string
     * @return long value of given date
     * @throws ParseException on Error
     */
    protected long getDateToLong(String date) throws ParseException {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.util.Date fDate = dateFormat.parse(date);
        Long lDate = fDate.getTime();
        return lDate;
    }
    /**
     * Use to handle exception of common type in single step
     *
     * @param msg custom message
     * @param e   throwable object of the exception
     * @throws APIMgtUsageQueryServiceClientException
     */
    protected void handleException(String msg, Throwable e) throws APIMgtUsageQueryServiceClientException {
        log.error(msg, e);
        throw new APIMgtUsageQueryServiceClientException(msg, e);
    }

    /**
     *
     * @param drillDown selected type of data
     * @return Table name for view
     */
    protected  String getExecutionTimeTableByView(String drillDown){
        String tableName = APIUsageStatisticsClientConstants.API_EXECUTION_TME_DAY_SUMMARY;
        if ("DAY".equals(drillDown)) {
            tableName =  APIUsageStatisticsClientConstants.API_EXECUTION_TME_DAY_SUMMARY;
        }else if ("HOUR".equals(drillDown)){
            tableName =  APIUsageStatisticsClientConstants.API_EXECUTION_TIME_HOUR_SUMMARY;
        }else if ("MINUTES".equals(drillDown)){
            tableName =  APIUsageStatisticsClientConstants.API_EXECUTION_TIME_MINUTE_SUMMARY;
        }else if ("SECONDS".equals(drillDown)){
            tableName =  APIUsageStatisticsClientConstants.API_EXECUTION_TIME_SECONDS_SUMMARY;
        }
        return tableName;
    }

    protected void insertZeroElementsAndSort(List<Result<ExecutionTimeOfAPIValues>> resultList, String drillDown,
                                             long fromDate, long toDate) {

        Calendar from = Calendar.getInstance();
        from.setTimeInMillis(fromDate);
        Calendar to = Calendar.getInstance();
        to.setTimeInMillis(toDate);
        if ("DAY".equals(drillDown)) {
            if (((toDate - fromDate) / (1000 * 60 * 60 * 24)) * 5 > resultList.size()) {
                insertZeroElementsByType(from, to, resultList, Calendar.DATE);
            }
        } else if ("HOUR".equals(drillDown)) {
            if (((toDate - fromDate) / (1000 * 60 * 60)) * 5 > resultList.size()) {
                insertZeroElementsByType(from, to, resultList, Calendar.HOUR_OF_DAY);
            }
        } else if ("MINUTES".equals(drillDown)) {
            if (((toDate - fromDate) / (1000 * 60)) * 5 > resultList.size()) {
                insertZeroElementsByType(from, to, resultList, Calendar.MINUTE);
            }
        } else if ("SECONDS".equals(drillDown)) {
            if (((toDate - fromDate) / (1000)) * 5 > resultList.size()) {
                insertZeroElementsByType(from, to, resultList, Calendar.SECOND);
            }
        }
        sort(resultList, new Comparator<Result<ExecutionTimeOfAPIValues>>() {
            @Override
            public int compare(Result<ExecutionTimeOfAPIValues> o1, Result<ExecutionTimeOfAPIValues> o2) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(o1.getValues().getYear(), o1.getValues().getMonth() - 1, o1.getValues().getDay(), o1
                        .getValues().getHour(), o1.getValues().getMinutes(), o1.getValues().getSeconds());
                Calendar comparedDate = Calendar.getInstance();
                comparedDate.set(o2.getValues().getYear(), o2.getValues().getMonth() - 1, o2.getValues().getDay(), o2
                        .getValues().getHour(), o2.getValues().getMinutes(), o2.getValues().getSeconds());
                return calendar.getTime().compareTo(comparedDate.getTime());
            }
        });
    }

    private void insertZeroElementsByType(Calendar from, Calendar to, List<Result<ExecutionTimeOfAPIValues>>
            resultList, int field) {
        List<Result<ExecutionTimeOfAPIValues>> tempList = new ArrayList<Result<ExecutionTimeOfAPIValues>>();
        Calendar checkedDate = Calendar.getInstance();
        Set<String> mediationTypes = new HashSet<String>();
        for (Date date = from.getTime(); from.before(to);
             from.add(field, 1), date = from.getTime()) {
            checkedDate.setTime(date);
            boolean status = false;
            for (Result<ExecutionTimeOfAPIValues> executionTimeOfAPIValuesResult : resultList) {
                    int year = executionTimeOfAPIValuesResult.getValues().getYear();
                    int month = executionTimeOfAPIValuesResult.getValues().getMonth();
                    int day = executionTimeOfAPIValuesResult.getValues().getDay();
                    int hour = executionTimeOfAPIValuesResult.getValues().getHour();
                    int minute = executionTimeOfAPIValuesResult.getValues().getMinutes();
                    int seconds = executionTimeOfAPIValuesResult.getValues().getSeconds();
                mediationTypes.add(executionTimeOfAPIValuesResult.getValues().getMediationName());
                if (checkedDate.get(Calendar.YEAR) == year && checkedDate.get(Calendar.MONTH) +1 ==
                        month) {
                    if (field == Calendar.DATE && checkedDate.get(field) == day) {
                        status = true;
                        break;
                    } else if (field == Calendar.HOUR_OF_DAY && checkedDate.get(Calendar.DATE) == day &&
                            checkedDate.get(field) == hour) {
                        status = true;
                        break;
                    } else if (field == Calendar.MINUTE && checkedDate.get(Calendar.DATE) == day
                            && checkedDate.get(Calendar.HOUR_OF_DAY) == hour &&
                            checkedDate.get(field) == minute) {
                        status = true;
                        break;
                    }else if (field == Calendar.SECOND && checkedDate.get(Calendar.DATE) == day
                            && checkedDate.get(Calendar.HOUR_OF_DAY) == hour&& checkedDate.get(Calendar
                            .MINUTE) == minute && checkedDate.get(field) == seconds) {
                        status = true;
                        break;
                    }
                }
            }
            if (!status) {
                int hour,minutes = 0,seconds = 0;
                if (field == Calendar.HOUR_OF_DAY) {
                   hour=checkedDate.get(Calendar.HOUR_OF_DAY);
                } else if (field == Calendar.MINUTE) {
                    hour=checkedDate.get(Calendar.HOUR_OF_DAY);
                    minutes = checkedDate.get(Calendar.MINUTE);
                } else {
                    hour = checkedDate.get(Calendar.HOUR_OF_DAY);
                    minutes = checkedDate.get(Calendar.MINUTE);
                    seconds = checkedDate.get(Calendar.SECOND);
                }
                for (String mediation : mediationTypes) {
                    Result<ExecutionTimeOfAPIValues> tempResult = new Result<ExecutionTimeOfAPIValues>();
                    ExecutionTimeOfAPIValues executionTimeOfAPIValues = new ExecutionTimeOfAPIValues();
                    executionTimeOfAPIValues.setYear(checkedDate.get(Calendar.YEAR));
                    executionTimeOfAPIValues.setMonth(checkedDate.get(Calendar.MONTH) + 1);
                    executionTimeOfAPIValues.setDay(checkedDate.get(Calendar.DATE));
                    executionTimeOfAPIValues.setHour(hour);
                    executionTimeOfAPIValues.setMinutes(minutes);
                    executionTimeOfAPIValues.setSeconds(seconds);
                    executionTimeOfAPIValues.setMediationName(mediation);
                    tempResult.setValues(executionTimeOfAPIValues);
                    tempList.add(tempResult);
                }
            }
        }
        resultList.addAll(tempList);
    }
    /**
     * Return list of GeoLocation Usage for given api and its version
     *
     * @param apiName - Name of th API
     * @param version - Version of the API
     * @param tenantDomain - TenantDomain
     * @param fromDate - Start date of the time span
     * @param toDate - End date of time span
     * @param drillDown - Type of data
     * @return List of Geolocation  usage
     * @throws APIMgtUsageQueryServiceClientException
     */

    public abstract List<Result<PerGeoLocationUsageCount>> getGeoLocationsByApi(String apiName, String version,
                                                                                String tenantDomain, String fromDate,
                                                                                String toDate, String drillDown)
            throws
            APIMgtUsageQueryServiceClientException;
    /**
     * Return list of UserAgent count for given api and its version
     *
     * @param apiName - Name of th API
     * @param version - Version of the API
     * @param tenantDomain - TenantDomain
     * @param fromDate - Start date of the time span
     * @param toDate - End date of time span
     * @param drillDown - Type of data
     * @return List of count per user Agent
     * @throws APIMgtUsageQueryServiceClientException
     */

    public abstract List<Result<UserAgentUsageCount>> getUserAgentUsageByAPI(String apiName, String version,
                                                                             String tenantDomain, String fromDate,
                                                                             String toDate, String drillDown)
            throws
            APIMgtUsageQueryServiceClientException;
}
