/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.usage.client;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.*;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.usage.client.billing.APIUsageRangeCost;
import org.wso2.carbon.apimgt.usage.client.billing.PaymentPlan;
import org.wso2.carbon.apimgt.usage.client.dto.*;
import org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException;
import org.wso2.carbon.apimgt.usage.client.internal.APIUsageClientServiceComponent;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import org.json.simple.JSONArray;


public class APIUsageStatisticsClient {

    private static final String API_USAGE_TRACKING = "APIUsageTracking.";
    private static final String DATA_SOURCE_NAME = "jdbc/WSO2AM_STATS_DB";
    private static volatile DataSource dataSource = null;
    private static PaymentPlan paymentPlan;
    private static Map<String, String> subscriberAppsMap = new HashMap<String, String>();
    private APIProvider apiProviderImpl;
    private APIConsumer apiConsumerImpl;
    private static final Log log = LogFactory.getLog(APIUsageStatisticsClient.class);

    /* private static String text = "    <PaymentPlan name=\"platinam\">    <parameter name=\"call\">  " +
             "      <range0><start>0</start><end>5</end><value>0.0</value></range0>      " +
             "  <range1><start>5</start><end>10</end><value>2.0</value></range1>   " +
             "     <range2><start>10</start><end>15000</end><value>5.0</value></range2>  " +
             "  </parameter>    <parameter name=\"data\">        " +
             "<range0><start>0</start><end>10</end><value>0.0</value></range0>    " +
             "    <range1><start>10</start><end>20</end><value>1.0</value></range1>   " +
             "     <range2><start>20</start><end>30000</end><value>2.0</value></range2>   " +
             " </parameter>    <parameter name=\"messages\">        " +
             "<range0><start>0</start><end>10</end><value>0.5</value></range0>       " +
             " <range1><start>10</start><end>20000</end><value>1.0</value></range1>    </parameter></PaymentPlan>";
      */
    /* public APIUsageStatisticsClient(String username) throws APIMgtUsageQueryServiceClientException {
        APIManagerConfiguration config = APIUsageClientServiceComponent.getAPIManagerConfiguration();
        String targetEndpoint = config.getFirstProperty(APIMgtUsagePublisherConstants.API_USAGE_BAM_SERVER_URL_GROUPS);
        if (targetEndpoint == null || targetEndpoint.equals("")) {
            throw new APIMgtUsageQueryServiceClientException("Required BAM server URL parameter unspecified");
        }

        try {
            apiProviderImpl = APIManagerFactory.getInstance().getAPIProvider(username);
        } catch (APIManagementException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while instantiating API manager core objects", e);
        }

    }*/
    public APIUsageStatisticsClient(String username)
            throws APIMgtUsageQueryServiceClientException {
        OMElement element = null;
        APIManagerConfiguration config;
        APIManagerAnalyticsConfiguration apiManagerAnalyticsConfiguration;
        try {
            config = APIUsageClientServiceComponent.getAPIManagerConfiguration();
            apiManagerAnalyticsConfiguration = APIManagerAnalyticsConfiguration.getInstance();
            if (apiManagerAnalyticsConfiguration.isAnalyticsEnabled() && dataSource == null) {
                initializeDataSource();
            }
            // text = config.getFirstProperty("BillingConfig");
            String billingConfig = config.getFirstProperty("EnableBillingAndUsage");
            boolean isBillingEnabled = Boolean.parseBoolean(billingConfig);
            if (isBillingEnabled) {
                String filePath = (new StringBuilder()).append(CarbonUtils.getCarbonHome()).append(File.separator).append("repository").append(File.separator).append("conf").append(File.separator).append("billing-conf.xml").toString();
                element = buildOMElement(new FileInputStream(filePath));
                paymentPlan = new PaymentPlan(element);
            }
            String targetEndpoint = apiManagerAnalyticsConfiguration.getBamServerUrlGroups();
            if (targetEndpoint == null || targetEndpoint.equals(""))
                throw new APIMgtUsageQueryServiceClientException("Required BAM server URL parameter unspecified");
            apiProviderImpl = APIManagerFactory.getInstance().getAPIProvider(username);

        } catch (Exception e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while instantiating API manager core objects", e);
        }


    }

    public static void initializeDataSource() throws APIMgtUsageQueryServiceClientException {
        try {
            Context ctx = new InitialContext();
            dataSource = (DataSource) ctx.lookup(DATA_SOURCE_NAME);
        } catch (NamingException e) {
            throw new APIMgtUsageQueryServiceClientException("Error while looking up the data " +
                    "source: " + DATA_SOURCE_NAME);
        }
    }

    public static OMElement buildOMElement(InputStream inputStream) throws Exception {
        XMLStreamReader parser;
        try {
            parser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
        } catch (XMLStreamException e) {
            String msg = "Error in initializing the parser to build the OMElement.";
            throw new Exception(msg, e);
        } finally {
        }
        StAXOMBuilder builder = new StAXOMBuilder(parser);
        return builder.getDocumentElement();
    }

    public List<APIResponseFaultCountDTO> getPerAppFaultCount(String subscriberName, String groupId, String fromDate, String toDate, int limit)
            throws APIMgtUsageQueryServiceClientException {

        List<String> subscriberApps = getAppsbySubscriber(subscriberName, groupId);
        String concatenatedKeySetString = "";

        int size = subscriberApps.size();
        if (size > 0) {
            concatenatedKeySetString += "'" + subscriberApps.get(0) + "'";
        } else {
            return new ArrayList<APIResponseFaultCountDTO>();
        }
        for (int i = 1; i < subscriberApps.size(); i++) {
            concatenatedKeySetString += ",'" + subscriberApps.get(i) + "'";
        }

        return getFaultAppUsageData(APIUsageStatisticsClientConstants.API_FAULT_SUMMARY, concatenatedKeySetString,
                fromDate, toDate, limit);

    }

    public List<AppUsageDTO> getTopAppUsers(String subscriberName, String groupId, String fromDate, String toDate, int limit)
            throws APIMgtUsageQueryServiceClientException {

        List<String> subscriberApps = getAppsbySubscriber(subscriberName, groupId);
        String concatenatedKeySetString = "";

        int size = subscriberApps.size();
        if (size > 0) {
            concatenatedKeySetString += "'" + subscriberApps.get(0) + "'";
        } else {
            return new ArrayList<AppUsageDTO>();
        }
        for (int i = 1; i < subscriberApps.size(); i++) {
            concatenatedKeySetString += ",'" + subscriberApps.get(i) + "'";
        }

        return getTopAppUsageData(APIUsageStatisticsClientConstants.API_REQUEST_SUMMARY, concatenatedKeySetString,
                fromDate, toDate, limit);

    }

    /**
     * This method gets the app usage data for invoking APIs
     *
     * @param tableName name of the required table in the database
     * @param keyString concatenated key set of applications
     * @return a collection containing the data related to App usage
     * @throws APIMgtUsageQueryServiceClientException if an error occurs while querying the database
     */
    private List<AppUsageDTO> getTopAppUsageData(String tableName, String keyString, String fromDate, String toDate,
            int limit)
            throws APIMgtUsageQueryServiceClientException {

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        List<AppUsageDTO> topAppUsageDataList = new ArrayList<AppUsageDTO>();

        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String query;

            //check whether table exist first
            if (isTableExist(tableName, connection)) {

                query = "SELECT " +
                        APIUsageStatisticsClientConstants.USER_ID + "," +
                        APIUsageStatisticsClientConstants.CONSUMERKEY + "," +
                        "SUM(" + APIUsageStatisticsClientConstants.REQUEST + ") AS net_total_requests" +
                        " FROM " + tableName +
                        " WHERE " + APIUsageStatisticsClientConstants.CONSUMERKEY + " IN (" + keyString + ")" +
                        " AND time BETWEEN " + "'" + fromDate + "' AND \'" + toDate + "' " +
                        " GROUP BY " + APIUsageStatisticsClientConstants.CONSUMERKEY + "," +
                        APIUsageStatisticsClientConstants.USER_ID
                        + " ORDER BY net_total_requests DESC";

                resultSet = statement.executeQuery(query);
                AppUsageDTO appUsageDTO;
                while (resultSet.next()) {
                    String userId = resultSet.getString(APIUsageStatisticsClientConstants.USER_ID);
                    long requestCount = resultSet.getLong("net_total_requests");
                    String consumerKey = resultSet.getString(APIUsageStatisticsClientConstants.CONSUMERKEY);

                    appUsageDTO = new AppUsageDTO();
                    appUsageDTO.setUserid(userId);
                    appUsageDTO.setappName(subscriberAppsMap.get(consumerKey));
                    appUsageDTO.setconsumerKey(consumerKey);
                    appUsageDTO.setCount(requestCount);
                    topAppUsageDataList.add(appUsageDTO);
                }
            }
        } catch (SQLException e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while querying top app usage data from JDBC database", e);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {

                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing the result set from JDBC database.", e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {

                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing the statement from JDBC database.", e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {

                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing JDBC database connection.", e);
                }
            }
        }
        return topAppUsageDataList;
    }

    /**
     * This method gets the API faulty invocation data
     *
     * @param tableName name of the required table in the database
     * @param keyString concatenated key set of applications
     * @return a collection containing the data related to API faulty invocations
     * @throws APIMgtUsageQueryServiceClientException if an error occurs while querying the database
     */
    private List<APIResponseFaultCountDTO> getFaultAppUsageData(String tableName, String keyString, String fromDate,
            String toDate, int limit)
            throws APIMgtUsageQueryServiceClientException {

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        List<APIResponseFaultCountDTO> falseAppUsageDataList = new ArrayList<APIResponseFaultCountDTO>();

        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String query;

            //check whether table exist first
            if (isTableExist(tableName, connection)) {

                query = "SELECT " +
                        "consumerKey, api,SUM(" + APIUsageStatisticsClientConstants.FAULT + ") AS total_faults " +
                        " FROM " + tableName +
                        " WHERE " + APIUsageStatisticsClientConstants.CONSUMERKEY + " IN (" + keyString + ") " +
                        " AND time BETWEEN " + "'" + fromDate + "' AND \'" + toDate + "' " +
                        " GROUP BY " + APIUsageStatisticsClientConstants.CONSUMERKEY + ","
                        + APIUsageStatisticsClientConstants.API;

                resultSet = statement.executeQuery(query);
                APIResponseFaultCountDTO apiUsageDTO;
                while (resultSet.next()) {
                    String apiName = resultSet.getString(APIUsageStatisticsClientConstants.API);
                    long faultCount = resultSet.getLong("total_faults");
                    String consumerKey = resultSet.getString(APIUsageStatisticsClientConstants.CONSUMERKEY);

                    apiUsageDTO = new APIResponseFaultCountDTO();
                    apiUsageDTO.setApiName(apiName);
                    apiUsageDTO.setappName(subscriberAppsMap.get(consumerKey));
                    apiUsageDTO.setconsumerKey(consumerKey);
                    apiUsageDTO.setCount(faultCount);
                    falseAppUsageDataList.add(apiUsageDTO);
                }
            }
        } catch (SQLException e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while querying API faulty invocation data from JDBC database", e);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {

                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing the result set from JDBC database.", e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {

                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing the statement from JDBC database.", e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {

                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing JDBC database connection.", e);
                }
            }
        }
        return falseAppUsageDataList;
    }

    @Deprecated
    private Collection<AppUsage> getAppUsageData(OMElement data) {
        List<AppUsage> usageData = new ArrayList<AppUsage>();
        OMElement rowsElement = data.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.ROWS));
        Iterator rowIterator = rowsElement.getChildrenWithName(new QName(
                APIUsageStatisticsClientConstants.ROW));
        if (rowIterator != null) {
            while (rowIterator.hasNext()) {
                OMElement rowElement = (OMElement) rowIterator.next();
                usageData.add(new AppUsage(rowElement));
            }
        }
        return usageData;


    }

    public List<AppCallTypeDTO> getAppApiCallType(String subscriberName, String groupId, String fromDate, String toDate, int limit)
            throws APIMgtUsageQueryServiceClientException {

        List<String> subscriberApps = getAppsbySubscriber(subscriberName, groupId);
        String concatenatedKeySetString = "";

        int size = subscriberApps.size();
        if (size > 0) {
            concatenatedKeySetString += "'" + subscriberApps.get(0) + "'";
        } else {
            return new ArrayList<AppCallTypeDTO>();
        }
        for (int i = 1; i < subscriberApps.size(); i++) {
            concatenatedKeySetString += ",'" + subscriberApps.get(i) + "'";
        }

        return getAPICallTypeUsageData(APIUsageStatisticsClientConstants.API_Resource_Path_USAGE_SUMMARY,
                concatenatedKeySetString, fromDate, toDate, limit);

    }

    /**
     * This method gets the API usage data per API call type
     *
     * @param tableName name of the required table in the database
     * @param keyString concatenated key set of applications
     * @return a collection containing the data related to API call types
     * @throws APIMgtUsageQueryServiceClientException if an error occurs while querying the database
     */
    private List<AppCallTypeDTO> getAPICallTypeUsageData(String tableName, String keyString, String fromDate,
            String toDate, int limit)
            throws APIMgtUsageQueryServiceClientException {

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        List<AppCallTypeDTO> appApiCallTypeList = new ArrayList<AppCallTypeDTO>();

        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String query;

            //check whether table exist first
            if (isTableExist(tableName, connection)) {

                query = "SELECT " +
                        APIUsageStatisticsClientConstants.API + "," +
                        APIUsageStatisticsClientConstants.METHOD + "," +
                        APIUsageStatisticsClientConstants.CONSUMERKEY + "," +
                        APIUsageStatisticsClientConstants.RESOURCE +
                        " FROM " + tableName +
                        " WHERE " +
                        APIUsageStatisticsClientConstants.CONSUMERKEY + " IN (" + keyString + ") " +
                        " AND time BETWEEN " + "'" + fromDate + "' AND \'" + toDate + "' " +
                        " GROUP BY " + APIUsageStatisticsClientConstants.CONSUMERKEY + "," +
                        APIUsageStatisticsClientConstants.API + "," + APIUsageStatisticsClientConstants.METHOD + "," +
                        APIUsageStatisticsClientConstants.RESOURCE;


                resultSet = statement.executeQuery(query);
                AppCallTypeDTO appCallTypeDTO;
                while (resultSet.next()) {
                    String apiName = resultSet.getString(APIUsageStatisticsClientConstants.API);
                    String callType = resultSet.getString(APIUsageStatisticsClientConstants.METHOD);
                    String consumerKey = resultSet.getString(APIUsageStatisticsClientConstants.CONSUMERKEY);
                    String resource = resultSet.getString(APIUsageStatisticsClientConstants.RESOURCE);

                    List<String> callTypeList = new ArrayList<String>();
                    callTypeList.add(resource + " (" + callType + ")");

                    appCallTypeDTO = new AppCallTypeDTO();
                    appCallTypeDTO.setApiName(apiName);
                    appCallTypeDTO.setappName(subscriberAppsMap.get(consumerKey));
                    appCallTypeDTO.setconsumerKey(consumerKey);
                    appCallTypeDTO.setCallType(callTypeList);
                    appApiCallTypeList.add(appCallTypeDTO);

                }
            }
        } catch (SQLException e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while querying API call type data from JDBC database", e);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {

                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing the result set from JDBC database.", e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {

                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing the statement from JDBC database.", e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {

                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing JDBC database connection.", e);
                }
            }
        }
        return appApiCallTypeList;
    }

    @Deprecated
    private Collection<AppCallType> getCallTypeUsageData(OMElement data) {
        List<AppCallType> usageData = new ArrayList<AppCallType>();
        OMElement rowsElement = data.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.ROWS));
        Iterator rowIterator = rowsElement.getChildrenWithName(new QName(
                APIUsageStatisticsClientConstants.ROW));
        if (rowIterator != null) {
            while (rowIterator.hasNext()) {
                OMElement rowElement = (OMElement) rowIterator.next();
                usageData.add(new AppCallType(rowElement));
            }
        }
        return usageData;


    }

    public List<AppRegisteredUsersDTO> getAppRegisteredUsers(String subscriberName, String groupId) throws APIMgtUsageQueryServiceClientException {


        List<String> subscriberApps = getAppsbySubscriber(subscriberName, groupId);

        Collection<AppRegisteredUsersDTO> usageData = getAppUsers();


        List<AppRegisteredUsersDTO> appUserList = new ArrayList<AppRegisteredUsersDTO>();
        AppRegisteredUsersDTO appUsers;
        for (AppRegisteredUsersDTO usage : usageData) {
            for (String subscriberApp : subscriberApps) {
                if (subscriberApp != null && subscriberApp.equals(usage.getconsumerKey())) {
                    appUsers = new AppRegisteredUsersDTO();
                    appUsers.setappName(subscriberAppsMap.get(usage.getconsumerKey()));
                    appUsers.setUser(usage.getUser());
                    appUserList.add(appUsers);
                }
            }
        }

        return appUserList;
    }

    private List<AppRegisteredUsersDTO> getAppUsers() throws APIMgtUsageQueryServiceClientException {


        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            connection = APIMgtDBUtil.getConnection();
            statement = connection.createStatement();
            String query;


            query = "SELECT CONSUMER_KEY , AUTHZ_USER FROM IDN_OAUTH2_ACCESS_TOKEN GROUP BY CONSUMER_KEY,AUTHZ_USER   ";


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

    public List<APIUsageDTO> perAppPerAPIUsage(String subscriberName, String groupId, String fromDate, String toDate, int limit)
            throws APIMgtUsageQueryServiceClientException {

        List<String> subscriberApps = getAppsbySubscriber(subscriberName, groupId);
        String concatenatedKeySetString = "";

        int size = subscriberApps.size();
        if (size > 0) {
            concatenatedKeySetString += "'" + subscriberApps.get(0) + "'";
        } else {
            return new ArrayList<APIUsageDTO>();
        }
        for (int i = 1; i < subscriberApps.size(); i++) {
            concatenatedKeySetString += ",'" + subscriberApps.get(i) + "'";
        }

        return getPerAppAPIUsageData(APIUsageStatisticsClientConstants.API_REQUEST_SUMMARY, concatenatedKeySetString,
                fromDate, toDate, limit);
    }

    /**
     * This method builds a single string from a set of strings in a string array, to be used in database query
     *
     * @param keyArray string array containing the keys
     * @return set of keys as a comma separated single string
     */
    private String buildKeySetString(String[] keyArray){

        String keySetString = "";

        for (int i = 0; i < keyArray.length; i++) {
            keySetString = keySetString + "'" + keyArray[i] + "'";
            if (i != keyArray.length - 1) {
                //adds a comma to the end of the string if the current key is not the last in the array
                keySetString = keySetString + ",";
            }
        }
        if (keySetString.isEmpty()) {
            keySetString = "''";
        }
        return keySetString;
    }

    /**
     * This method gets the API usage data per application
     *
     * @param tableName name of the required table in the database
     * @param keyString concatenated key set of applications
     * @return a collection containing the data related to per App API usage
     * @throws APIMgtUsageQueryServiceClientException if an error occurs while querying the database
     */
    private List<APIUsageDTO> getPerAppAPIUsageData(String tableName, String keyString, String fromDate, String toDate,
            int limit)
            throws APIMgtUsageQueryServiceClientException {

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        List<APIUsageDTO> perAppUsageDataList = new ArrayList<APIUsageDTO>();

        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String query;

            //check whether table exist first
            if (isTableExist(tableName, connection)) {

                query = "SELECT " +
                        APIUsageStatisticsClientConstants.API + "," +
                        APIUsageStatisticsClientConstants.CONSUMERKEY + "," +
                        " SUM(" + APIUsageStatisticsClientConstants.REQUEST + ") AS total_calls " +
                        " FROM " + APIUsageStatisticsClientConstants.API_REQUEST_SUMMARY +
                        " WHERE " +
                        APIUsageStatisticsClientConstants.CONSUMERKEY + " IN (" + keyString + ") " +
                        " AND time BETWEEN " + "'" + fromDate + "' AND \'" + toDate + "' " +
                        " GROUP BY " +
                        APIUsageStatisticsClientConstants.API + "," + APIUsageStatisticsClientConstants.CONSUMERKEY;

                resultSet = statement.executeQuery(query);
                APIUsageDTO apiUsageDTO;

                while (resultSet.next()) {
                    String apiName = resultSet.getString(APIUsageStatisticsClientConstants.API);
                    long requestCount = resultSet.getLong("total_calls");
                    String consumerKey = resultSet.getString(APIUsageStatisticsClientConstants.CONSUMERKEY);

                    apiUsageDTO = new APIUsageDTO();
                    apiUsageDTO.setApiName(apiName);
                    apiUsageDTO.setappName(subscriberAppsMap.get(consumerKey));
                    apiUsageDTO.setconsumerKey(consumerKey);
                    apiUsageDTO.setCount(requestCount);

                    perAppUsageDataList.add(apiUsageDTO);
                }
            }
        } catch (SQLException e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while querying per App usage data from JDBC database", e);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {

                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing the result set from JDBC database.", e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {

                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing the statement from JDBC database.", e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {

                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing JDBC database connection.", e);
                }
            }
        }
        return perAppUsageDataList;
    }

    private List<String> getAppsbySubscriber(String subscriberName, String groupId) throws APIMgtUsageQueryServiceClientException {

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

                if (isTokenEncryptionEnabled)   {
                    String decryptedConsumerKey = new String(CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(consumerKey));
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
     * Returns a list of APIUsageDTO objects that contain information related to APIs that
     * belong to a particular provider and the number of total API calls each API has processed
     * up to now. This method does not distinguish between different API versions. That is all
     * versions of a single API are treated as one, and their individual request counts are summed
     * up to calculate a grand total per each API.
     *
     * @param providerName Name of the API provider
     * @return a List of APIUsageDTO objects - possibly empty
     * @throws org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException if an error occurs while contacting backend services
     */
    public List<APIUsageDTO> getUsageByAPIs(String providerName, String fromDate, String toDate, int limit)
            throws APIMgtUsageQueryServiceClientException {

        Collection<APIUsage> usageData = getAPIUsageData(APIUsageStatisticsClientConstants.API_VERSION_USAGE_SUMMARY);
        List<API> providerAPIs = getAPIsByProvider(providerName);
        Map<String, APIUsageDTO> usageByAPIs = new TreeMap<String, APIUsageDTO>();
        for (APIUsage usage : usageData) {
            for (API providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(usage.apiName) &&
                        providerAPI.getId().getVersion().equals(usage.apiVersion) &&
                        providerAPI.getContext().equals(usage.context)) {
                    String[] apiData = {usage.apiName, usage.apiVersion,  providerAPI.getId().getProviderName()};

                    JSONArray jsonArray = new JSONArray();
                    jsonArray.add(0,apiData[0]);
                    jsonArray.add(1,apiData[1]);
                    jsonArray.add(2,apiData[2]);
                    String apiName = jsonArray.toJSONString();

                    APIUsageDTO usageDTO = usageByAPIs.get(apiName);
                    if (usageDTO != null) {
                        usageDTO.setCount(usageDTO.getCount() + usage.requestCount);
                    } else {
                        usageDTO = new APIUsageDTO();
                        usageDTO.setApiName(apiName);
                        usageDTO.setCount(usage.requestCount);
                        usageByAPIs.put(apiName, usageDTO);
                    }
                }
            }
        }
        return getAPIUsageTopEntries(new ArrayList<APIUsageDTO>(usageByAPIs.values()), limit);
    }

    /**
     * This method gets the usage data for a given API across all versions
     *
     * @param tableName name of the table in the database
     * @return a collection containing the API usage data
     * @throws APIMgtUsageQueryServiceClientException if an error occurs while querying the database
     */
    private Collection<APIUsage> getAPIUsageData(String tableName) throws APIMgtUsageQueryServiceClientException {

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        Collection<APIUsage> usageDataList = new ArrayList<APIUsage>();

        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String query;

            //check whether table exist first
            if (isTableExist(tableName, connection)) {

                query = "SELECT " +
                        APIUsageStatisticsClientConstants.API + "," +
                        APIUsageStatisticsClientConstants.CONTEXT + "," +
                        APIUsageStatisticsClientConstants.VERSION + "," +
                        " SUM(" + APIUsageStatisticsClientConstants.REQUEST + ") AS aggregateSum " +
                        " FROM " +
                        tableName +
                        " GROUP BY " +
                        APIUsageStatisticsClientConstants.API +","+
                        APIUsageStatisticsClientConstants.CONTEXT+","+
                        APIUsageStatisticsClientConstants.VERSION;

                resultSet = statement.executeQuery(query);

                while (resultSet.next()) {
                    String apiName = resultSet.getString(APIUsageStatisticsClientConstants.API);
                    String context = resultSet.getString(APIUsageStatisticsClientConstants.CONTEXT);
                    String version = resultSet.getString(APIUsageStatisticsClientConstants.VERSION);
                    long requestCount = resultSet.getLong("aggregateSum");
                    usageDataList.add(new APIUsage(apiName, context, version, requestCount));
                }
            }
        } catch (SQLException e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while querying API usage data from JDBC database", e);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {

                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing the result set from JDBC database.", e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {

                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing the statement from JDBC database.", e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {

                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing JDBC database connection.", e);
                }
            }
        }
        return usageDataList;
    }

    /**
     * Returns a list of APIVersionUsageDTO objects that contain information related to a
     * particular API of a specified provider, along with the number of API calls processed
     * by each version of that API.
     *
     * @param providerName Name of the API provider
     * @param apiName      Name of th API
     * @return a List of APIVersionUsageDTO objects, possibly empty
     * @throws org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException on error
     */
    public List<APIVersionUsageDTO> getUsageByAPIVersions(String providerName, String apiName)
            throws APIMgtUsageQueryServiceClientException {

        List<APIUsage> usageData = this
                .queryBetweenTwoDaysForAPIUsageByVersion(APIUsageStatisticsClientConstants.API_VERSION_USAGE_SUMMARY,
                        null, null, apiName);
//        Collection<APIUsage> usageData = getUsageData(omElement);
        List<API> providerAPIs = getAPIsByProvider(providerName);
        Map<String, APIVersionUsageDTO> usageByVersions = new TreeMap<String, APIVersionUsageDTO>();

        for (APIUsage usage : usageData) {
            for (API providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(usage.apiName) &&
                        providerAPI.getId().getVersion().equals(usage.apiVersion) &&
                        providerAPI.getContext().equals(usage.context)) {

                    APIVersionUsageDTO usageDTO = new APIVersionUsageDTO();
                    usageDTO.setVersion(usage.apiVersion);
                    usageDTO.setCount(usage.requestCount);
                    usageByVersions.put(usage.apiVersion, usageDTO);
                }
            }
        }

        return new ArrayList<APIVersionUsageDTO>(usageByVersions.values());
    }

    /**
     * Returns a list of APIVersionUsageDTO objects that contain information related to a
     * particular API of a specified provider, along with the number of API calls processed
     * by each version of that API for a particular time preriod.
     *
     * @param providerName
     * @param apiName
     * @param fromDate
     * @param toDate
     * @return
     * @throws org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException
     */
    public List<APIVersionUsageDTO> getUsageByAPIVersions(String providerName, String apiName,
                                                          String fromDate, String toDate) throws APIMgtUsageQueryServiceClientException {

        List<APIUsage> usageData = this.queryBetweenTwoDaysForAPIUsageByVersion(
                APIUsageStatisticsClientConstants.API_VERSION_USAGE_SUMMARY, fromDate, toDate, apiName);
//        Collection<APIUsage> usageData = getUsageData(omElement);
        List<API> providerAPIs = getAPIsByProvider(providerName);
        Map<String, APIVersionUsageDTO> usageByVersions = new TreeMap<String, APIVersionUsageDTO>();

        for (APIUsage usage : usageData) {
            for (API providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(usage.apiName) &&
                    providerAPI.getId().getVersion().equals(usage.apiVersion) &&
                    providerAPI.getContext().equals(usage.context)) {

                    APIVersionUsageDTO usageDTO = new APIVersionUsageDTO();
                    usageDTO.setVersion(usage.apiVersion);
                    usageDTO.setCount(usage.requestCount);
                    usageByVersions.put(usage.apiVersion, usageDTO);
                }
            }
        }
        return new ArrayList<APIVersionUsageDTO>(usageByVersions.values());
    }

    /**
     * Returns a list of APIVersionUsageDTO objects that contain information related to a
     * particular API of a specified provider, along with the number of API calls processed
     * by each resource path of that API.
     *
     * @param providerName Name of the API provider
     * @return a List of APIResourcePathUsageDTO objects, possibly empty
     * @throws org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException on error
     */
    public List<APIResourcePathUsageDTO> getAPIUsageByResourcePath(String providerName, String fromDate, String toDate)
            throws APIMgtUsageQueryServiceClientException {

        Collection<APIUsageByResourcePath> usageData = this
                .queryToGetAPIUsageByResourcePath(APIUsageStatisticsClientConstants.API_Resource_Path_USAGE_SUMMARY,
                        fromDate, toDate);
        List<API> providerAPIs = getAPIsByProvider(providerName);
        List<APIResourcePathUsageDTO> usageByResourcePath = new ArrayList<APIResourcePathUsageDTO>();

        for (APIUsageByResourcePath usage : usageData) {
            for (API providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(usage.apiName) &&
                        providerAPI.getId().getVersion().equals(usage.apiVersion) &&
                        providerAPI.getContext().equals(usage.context)) {

                    APIResourcePathUsageDTO usageDTO = new APIResourcePathUsageDTO();
                    usageDTO.setApiName(usage.apiName);
                    usageDTO.setVersion(usage.apiVersion);
                    usageDTO.setMethod(usage.method);
                    usageDTO.setContext(usage.context);
                    usageDTO.setCount(usage.requestCount);
                    usageDTO.setTime(usage.time);
                    usageByResourcePath.add(usageDTO);
                }
            }
        }
        return usageByResourcePath;
    }

    public List<APIDestinationUsageDTO> getAPIUsageByDestination(String providerName, String fromDate, String toDate)
            throws APIMgtUsageQueryServiceClientException {

        List<APIUsageByDestination> usageData= this.queryToGetAPIUsageByDestination(
                APIUsageStatisticsClientConstants.API_USAGEBY_DESTINATION_SUMMARY, fromDate, toDate);

        List<API> providerAPIs = getAPIsByProvider(providerName);
        List<APIDestinationUsageDTO> usageByResourcePath = new ArrayList<APIDestinationUsageDTO>();

        for (APIUsageByDestination usage : usageData) {
            for (API providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(usage.apiName) &&
                        providerAPI.getId().getVersion().equals(usage.apiVersion) &&
                        providerAPI.getContext().equals(usage.context)) {

                    APIDestinationUsageDTO usageDTO = new APIDestinationUsageDTO();
                    usageDTO.setApiName(usage.apiName);
                    usageDTO.setVersion(usage.apiVersion);
                    usageDTO.setDestination(usage.destination);
                    usageDTO.setContext(usage.context);
                    usageDTO.setCount(usage.requestCount);
                    usageByResourcePath.add(usageDTO);
                }
            }
        }
        return usageByResourcePath;
    }

    /**
     * Returns a list of APIUsageByUserDTO objects that contain information related to
     * User wise API Usage, along with the number of invocations, and API Version
     *
     * @param providerName Name of the API provider
     * @return a List of APIUsageByUserDTO objects, possibly empty
     * @throws org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException on error
     */
    public List<APIUsageByUserDTO> getAPIUsageByUser(String providerName, String fromDate, String toDate)
            throws APIMgtUsageQueryServiceClientException {

        List<APIUsageByUserName> usageData = this.queryBetweenTwoDaysForAPIUsageByUser(providerName, fromDate, toDate, null);
        
        String tenantDomain = MultitenantUtils.getTenantDomain(providerName);
        
        List<APIUsageByUserDTO> usageByName = new ArrayList<APIUsageByUserDTO>();

        for (APIUsageByUserName usage : usageData) {
            if (tenantDomain.equals(MultitenantUtils.getTenantDomain(usage.apipublisher))) {
                APIUsageByUserDTO usageDTO = new APIUsageByUserDTO();
                usageDTO.setApiName(usage.apiName);
                usageDTO.setVersion(usage.apiVersion);
                usageDTO.setUserID(usage.userID);
                usageDTO.setCount(usage.requestCount);
                usageByName.add(usageDTO);
            }
        }

        return usageByName;
    }

    /**
     * Gets a list of APIResponseTimeDTO objects containing information related to APIs belonging
     * to a particular provider along with their average response times.
     *
     * @param providerName Name of the API provider
     * @return a List of APIResponseTimeDTO objects, possibly empty
     * @throws org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException on error
     */
    public List<APIResponseTimeDTO> getResponseTimesByAPIs(String providerName, String fromDate, String toDate, int limit)
            throws APIMgtUsageQueryServiceClientException {

        Collection<APIResponseTime> responseTimes =
                getAPIResponseTimeData(APIUsageStatisticsClientConstants.API_VERSION_SERVICE_TIME_SUMMARY);
        List<API> providerAPIs = getAPIsByProvider(providerName);
        Map<String, Double> apiCumulativeServiceTimeMap = new HashMap<String, Double>();
        Map<String, Long> apiUsageMap = new TreeMap<String, Long>();
        for (APIResponseTime responseTime : responseTimes) {
            for (API providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(responseTime.apiName) &&
                        providerAPI.getId().getVersion().equals(responseTime.apiVersion) &&
                        providerAPI.getContext().equals(responseTime.context)) {

                    String apiName = responseTime.apiName + " (" + providerAPI.getId().getProviderName() + ")";
                    Double cumulativeResponseTime = apiCumulativeServiceTimeMap.get(apiName);

                    if (cumulativeResponseTime != null) {
                        apiCumulativeServiceTimeMap.put(apiName,
                                cumulativeResponseTime + responseTime.responseTime * responseTime.responseCount);
                        apiUsageMap.put(apiName,
                                apiUsageMap.get(apiName) + responseTime.responseCount);
                    } else {
                        apiCumulativeServiceTimeMap.put(apiName,
                                responseTime.responseTime * responseTime.responseCount);
                        apiUsageMap.put(apiName, responseTime.responseCount);
                    }
                }
            }
        }

        Map<String, APIResponseTimeDTO> responseTimeByAPI = new TreeMap<String, APIResponseTimeDTO>();
        DecimalFormat format = new DecimalFormat("#.##");
        for (String key : apiUsageMap.keySet()) {
            APIResponseTimeDTO responseTimeDTO = new APIResponseTimeDTO();
            responseTimeDTO.setApiName(key);
            double responseTime = apiCumulativeServiceTimeMap.get(key) / apiUsageMap.get(key);
            responseTimeDTO.setServiceTime(Double.parseDouble(format.format(responseTime)));
            responseTimeByAPI.put(key, responseTimeDTO);
        }
        return getResponseTimeTopEntries(new ArrayList<APIResponseTimeDTO>(responseTimeByAPI.values()), limit);
    }

    /**
     * This method gets the response times for APIs
     *
     * @param tableName name of the required table in the database
     * @return a collection containing the data related to API response times
     * @throws APIMgtUsageQueryServiceClientException if an error occurs while querying the database
     */
    private Collection<APIResponseTime> getAPIResponseTimeData(String tableName)
            throws APIMgtUsageQueryServiceClientException {

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        Collection<APIResponseTime> responseTimeData = new ArrayList<APIResponseTime>();

        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String tempTableName = "TempTable";
            String query;

            query = "SELECT " +
                    tempTableName + "." + APIUsageStatisticsClientConstants.API_VERSION + ", " +
                    tempTableName + "." + APIUsageStatisticsClientConstants.CONTEXT + ", " +
                    "SUM(" + APIUsageStatisticsClientConstants.RESPONSE + ") AS totalTime ," +
                    "SUM(weighted_service_time) AS totalWeightTime " +
                    " FROM " +
                    "(SELECT " +
                    APIUsageStatisticsClientConstants.RESPONSE + "," +
                    APIUsageStatisticsClientConstants.API_VERSION + "," +
                    APIUsageStatisticsClientConstants.CONTEXT + "," + "(" +
                    APIUsageStatisticsClientConstants.SERVICE_TIME + " * " +
                    APIUsageStatisticsClientConstants.RESPONSE + ") AS weighted_service_time " +
                    " FROM " +
                    APIUsageStatisticsClientConstants.API_VERSION_SERVICE_TIME_SUMMARY + ") " + "TempTable " +
                    " GROUP BY " +
                    tempTableName + "." + APIUsageStatisticsClientConstants.API_VERSION + ", " +
                    tempTableName + "." + APIUsageStatisticsClientConstants.CONTEXT;

            resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String apiName = resultSet.getString(APIUsageStatisticsClientConstants.API_VERSION).split(":v")[0];
                String version = resultSet.getString(APIUsageStatisticsClientConstants.API_VERSION).split(":v")[1];
                String context = resultSet.getString(APIUsageStatisticsClientConstants.CONTEXT);
                long responseCount = resultSet.getLong("totalTime");
                double responseTime = resultSet.getDouble("totalWeightTime") / responseCount;
                responseTimeData.add(new APIResponseTime(apiName, version, context, responseTime, responseCount));
            }

        } catch (SQLException e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while querying API response times from JDBC database", e);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {

                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing the result set from JDBC database.", e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {

                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing the statement from JDBC database.", e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {

                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing JDBC database connection.", e);
                }
            }
        }
        return responseTimeData;
    }

    /**
     * Returns a list of APIVersionLastAccessTimeDTO objects for all the APIs belonging to the
     * specified provider. Last access times are calculated without taking API versions into
     * account. That is all the versions of an API are treated as one.
     *
     * @param providerName Name of the API provider
     * @return a list of APIVersionLastAccessTimeDTO objects, possibly empty
     * @throws org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException on error
     */
    public List<APIVersionLastAccessTimeDTO> getLastAccessTimesByAPI(String providerName, String fromDate, String toDate, int limit)
            throws APIMgtUsageQueryServiceClientException {

        Collection<APIAccessTime> accessTimes =
                getLastAccessData(APIUsageStatisticsClientConstants.API_VERSION_KEY_LAST_ACCESS_SUMMARY);
        List<API> providerAPIs = getAPIsByProvider(providerName);
        Map<String, APIAccessTime> lastAccessTimes = new TreeMap<String, APIAccessTime>();
        for (APIAccessTime accessTime : accessTimes) {
            for (API providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(accessTime.apiName) &&
                        providerAPI.getId().getVersion().equals(accessTime.apiVersion) &&
                        providerAPI.getContext().equals(accessTime.context)) {

                    String apiName = accessTime.apiName + " (" + providerAPI.getId().getProviderName() + ")";
                    APIAccessTime lastAccessTime = lastAccessTimes.get(apiName);
                    if (lastAccessTime == null || lastAccessTime.accessTime < accessTime.accessTime) {
                        lastAccessTimes.put(apiName, accessTime);
                        break;
                    }
                }
            }
        }
        Map<String, APIVersionLastAccessTimeDTO> accessTimeByAPI = new TreeMap<String, APIVersionLastAccessTimeDTO>();
        List<APIVersionLastAccessTimeDTO> accessTimeDTOs = new ArrayList<APIVersionLastAccessTimeDTO>();
        DateFormat dateFormat = new SimpleDateFormat();
        for (Map.Entry<String, APIAccessTime> entry : lastAccessTimes.entrySet()) {
            APIVersionLastAccessTimeDTO accessTimeDTO = new APIVersionLastAccessTimeDTO();
            accessTimeDTO.setApiName(entry.getKey());
            APIAccessTime lastAccessTime = entry.getValue();
            accessTimeDTO.setApiVersion(lastAccessTime.apiVersion);
            accessTimeDTO.setLastAccessTime(dateFormat.format(lastAccessTime.accessTime));
            accessTimeDTO.setUser(lastAccessTime.username);
            accessTimeByAPI.put(entry.getKey(), accessTimeDTO);
        }
        return getLastAccessTimeTopEntries(new ArrayList<APIVersionLastAccessTimeDTO>(accessTimeByAPI.values()), limit);

    }

    /**
     * This method gets the last access times for APIs
     *
     * @param tableName name of the required table in the database
     * @return a collection containing the data related to API last access times
     * @throws APIMgtUsageQueryServiceClientException if an error occurs while querying the database
     */
    private Collection<APIAccessTime> getLastAccessData(String tableName)
            throws APIMgtUsageQueryServiceClientException {

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        Collection<APIAccessTime> lastAccessTimeData = new ArrayList<APIAccessTime>();

        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String dataTableName = "dataTable";
            String maxTimesTable = "maxTimesTable";
            String query;

            query = "SELECT " +
                    dataTableName + "." + APIUsageStatisticsClientConstants.API + "," +
                    dataTableName + "." + APIUsageStatisticsClientConstants.VERSION + "," +
                    dataTableName + "." + APIUsageStatisticsClientConstants.CONTEXT + "," +
                    dataTableName + "." + APIUsageStatisticsClientConstants.REQUEST_TIME + "," +
                    dataTableName + "." + APIUsageStatisticsClientConstants.USER_ID +
                    " FROM (" +
                    " SELECT " + APIUsageStatisticsClientConstants.API + "," +
                    "apiPublisher" + "," + "MAX(" + APIUsageStatisticsClientConstants.TIME + ")" +
                    "AS maxTime FROM " + tableName + " GROUP BY " +
                    APIUsageStatisticsClientConstants.API + ",apiPublisher) maxTimesTable INNER JOIN " +
                    " (SELECT " +
                    APIUsageStatisticsClientConstants.API + "," +
                    APIUsageStatisticsClientConstants.VERSION + "," +
                    APIUsageStatisticsClientConstants.CONTEXT + "," +
                    "apiPublisher" + "," +
                    APIUsageStatisticsClientConstants.REQUEST_TIME + "," +
                    APIUsageStatisticsClientConstants.TIME + "," +
                    APIUsageStatisticsClientConstants.USER_ID +
                    " FROM " + tableName + ")"+
                    dataTableName + " ON " +
                    maxTimesTable+"." + APIUsageStatisticsClientConstants.API + "=" +
                    dataTableName + "." + APIUsageStatisticsClientConstants.API + " AND " +
                    maxTimesTable+"."+"apiPublisher" + "=" + dataTableName+"."+"apiPublisher" + " AND " +
                    maxTimesTable+"."+"maxTime="+dataTableName+"." + APIUsageStatisticsClientConstants.TIME;

            resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String apiName = resultSet.getString(APIUsageStatisticsClientConstants.API);
                String version = resultSet.getString(APIUsageStatisticsClientConstants.VERSION);
                String context = resultSet.getString(APIUsageStatisticsClientConstants.CONTEXT);
                double accessTime = resultSet.getDouble(APIUsageStatisticsClientConstants.REQUEST_TIME);
                String username = resultSet.getString(APIUsageStatisticsClientConstants.USER_ID);
                lastAccessTimeData.add(new APIAccessTime(apiName, version, context, accessTime, username));
            }

        } catch (SQLException e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while querying last access data for APIs from JDBC database", e);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {

                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing the result set from JDBC database.", e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {

                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing the statement from JDBC database.", e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {

                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing JDBC database connection.", e);
                }
            }
        }
        return lastAccessTimeData;
    }

    /**
     * Returns a sorted list of PerUserAPIUsageDTO objects related to a particular API. The returned
     * list will only have at most limit + 1 entries. This method does not differentiate between
     * API versions.
     *
     * @param providerName API provider name
     * @param apiName      Name of the API
     * @param limit        Number of sorted entries to return
     * @return a List of PerUserAPIUsageDTO objects - Possibly empty
     * @throws org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException on error
     */
    public List<PerUserAPIUsageDTO> getUsageBySubscribers(String providerName, String apiName, int limit)
            throws APIMgtUsageQueryServiceClientException {

        Collection<APIUsageByUser> usageData = getUsageOfAPI(apiName, null);
        Map<String, PerUserAPIUsageDTO> usageByUsername = new TreeMap<String, PerUserAPIUsageDTO>();
        List<API> apiList = getAPIsByProvider(providerName);
        for (APIUsageByUser usageEntry : usageData) {
            for (API api : apiList) {
                if (api.getContext().equals(usageEntry.context) &&
                        api.getId().getApiName().equals(apiName)) {
                    PerUserAPIUsageDTO usageDTO = usageByUsername.get(usageEntry.username);
                    if (usageDTO != null) {
                        usageDTO.setCount(usageDTO.getCount() + usageEntry.requestCount);
                    } else {
                        usageDTO = new PerUserAPIUsageDTO();
                        usageDTO.setUsername(usageEntry.username);
                        usageDTO.setCount(usageEntry.requestCount);
                        usageByUsername.put(usageEntry.username, usageDTO);
                    }
                    break;
                }
            }
        }

        return getTopEntries(new ArrayList<PerUserAPIUsageDTO>(usageByUsername.values()), limit);
    }

    public List<APIRequestsByUserAgentsDTO> getUserAgentSummaryForALLAPIs() throws APIMgtUsageQueryServiceClientException{

        OMElement omElement = this.buildOMElementFromDatabaseTable("API_USERAGENT_SUMMARY");
        Collection<APIUserAgent> userAgentData = getUserAgent(omElement);
        Map<String, APIRequestsByUserAgentsDTO> apiRequestByUserAgents = new TreeMap<String, APIRequestsByUserAgentsDTO>();
        APIRequestsByUserAgentsDTO userAgentsDTO = null;
        for (APIUserAgent usageEntry : userAgentData) {
            if(!apiRequestByUserAgents.containsKey(usageEntry.userAgent)) {
                userAgentsDTO = new APIRequestsByUserAgentsDTO();
                userAgentsDTO.setUserAgent(usageEntry.userAgent);
                userAgentsDTO.setCount(usageEntry.totalRequestCount);
                apiRequestByUserAgents.put(usageEntry.userAgent, userAgentsDTO);
            }else{
                userAgentsDTO = new APIRequestsByUserAgentsDTO();
                userAgentsDTO=(APIRequestsByUserAgentsDTO)apiRequestByUserAgents.get(usageEntry.userAgent);
                userAgentsDTO.setCount(userAgentsDTO.getCount()+usageEntry.totalRequestCount);
                apiRequestByUserAgents.remove(usageEntry.userAgent);
                apiRequestByUserAgents.put(usageEntry.userAgent, userAgentsDTO);
            }
        }
        return new ArrayList<APIRequestsByUserAgentsDTO>(apiRequestByUserAgents.values());
    }

    public List<APIRequestsByHourDTO> getAPIRequestsByHour(String fromDate, String toDate,String apiName) throws APIMgtUsageQueryServiceClientException{
        String Date = null ;
        OMElement omElement = this.queryBetweenTwoDaysForAPIRequestsByHour("API_REQUESTS_PERHOUR", fromDate, toDate,apiName);
        Collection<APIRequestsByHour> apiRequestsByHoursData = getAPIRequestsByHour(omElement);
        Map<String, APIRequestsByHourDTO> apiRequestsByHour = new TreeMap<String, APIRequestsByHourDTO>();
        APIRequestsByHourDTO apiRequestsByHourDTO = null;
        for (APIRequestsByHour usageEntry : apiRequestsByHoursData) {
            apiRequestsByHourDTO = new APIRequestsByHourDTO();
            apiRequestsByHourDTO.setApi(usageEntry.apiName);
            apiRequestsByHourDTO.setApi_version(usageEntry.apiVersion);
            apiRequestsByHourDTO.setDate(usageEntry.date);
            apiRequestsByHourDTO.setRequestCount(usageEntry.requestCount);
            apiRequestsByHourDTO.setTier(usageEntry.tier);
            apiRequestsByHour.put(usageEntry.date.concat(usageEntry.tier),apiRequestsByHourDTO);
        }
        return new ArrayList<APIRequestsByHourDTO>(apiRequestsByHour.values());
    }

    public List<String> getAPIsFromAPIRequestsPerHourTable(String fromDate, String toDate) throws APIMgtUsageQueryServiceClientException{
        String Date = null ;
        OMElement omElement = this.queryBetweenTwoDaysForAPIsFromAPIRequestsPerHourTable("API_REQUESTS_PERHOUR", fromDate, toDate);
        Collection<String> apisList = getAPIsFromAPIRequestByHour(omElement);

        return new ArrayList<String>(apisList);
    }

    public List<APIResponseFaultCountDTO> getAPIResponseFaultCount(String providerName, String fromDate, String toDate)
            throws APIMgtUsageQueryServiceClientException {

        List<APIResponseFaultCount> faultyData = this
                .queryBetweenTwoDaysForFaulty(APIUsageStatisticsClientConstants.API_FAULT_SUMMARY, fromDate, toDate);
        List<API> providerAPIs = getAPIsByProvider(providerName);
        List<APIResponseFaultCountDTO> faultyCount = new ArrayList<APIResponseFaultCountDTO>();
        List<APIVersionUsageDTO> apiVersionUsageList;
        APIVersionUsageDTO apiVersionUsageDTO;
        for (APIResponseFaultCount fault : faultyData) {
            for (API providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(fault.apiName) &&
                        providerAPI.getId().getVersion().equals(fault.apiVersion) &&
                        providerAPI.getContext().equals(fault.context)) {

                    APIResponseFaultCountDTO faultyDTO = new APIResponseFaultCountDTO();
                    faultyDTO.setApiName(fault.apiName);
                    faultyDTO.setVersion(fault.apiVersion);
                    faultyDTO.setContext(fault.context);
                    faultyDTO.setCount(fault.faultCount);

                    apiVersionUsageList = getUsageByAPIVersions(providerName, fault.apiName, fromDate, toDate);
                    for (int i = 0; i < apiVersionUsageList.size(); i++) {
                        apiVersionUsageDTO = apiVersionUsageList.get(i);
                        if (apiVersionUsageDTO.getVersion().equals(fault.apiVersion)) {
                            long requestCount = apiVersionUsageDTO.getCount();
                            double faultPercentage = ((double)requestCount - fault.faultCount) / requestCount * 100;
                            DecimalFormat twoDForm = new DecimalFormat("#.##");
                            faultPercentage = 100 - Double.valueOf(twoDForm.format(faultPercentage));
                            faultyDTO.setFaultPercentage(faultPercentage);
                            faultyDTO.setRequestCount(requestCount);
                            break;
                        }
                    }

                    faultyCount.add(faultyDTO);

                }
            }
        }
        return faultyCount;
    }

    public List<PerUserAPIUsageDTO> getUsageBySubscribers(String providerName, String apiName,
                                                          String apiVersion, int limit) throws APIMgtUsageQueryServiceClientException {

        Collection<APIUsageByUser> usageData = getUsageOfAPI(apiName, apiVersion);
        Map<String, PerUserAPIUsageDTO> usageByUsername = new TreeMap<String, PerUserAPIUsageDTO>();
        List<API> apiList = getAPIsByProvider(providerName);
        for (APIUsageByUser usageEntry : usageData) {
            for (API api : apiList) {
                if (api.getContext().equals(usageEntry.context) &&
                        api.getId().getApiName().equals(apiName) &&
                        api.getId().getVersion().equals(apiVersion) &&
                        apiVersion.equals(usageEntry.apiVersion)) {
                    PerUserAPIUsageDTO usageDTO = usageByUsername.get(usageEntry.username);
                    if (usageDTO != null) {
                        usageDTO.setCount(usageDTO.getCount() + usageEntry.requestCount);
                    } else {
                        usageDTO = new PerUserAPIUsageDTO();
                        usageDTO.setUsername(usageEntry.username);
                        usageDTO.setCount(usageEntry.requestCount);
                        usageByUsername.put(usageEntry.username, usageDTO);
                    }
                    break;
                }
            }
        }

        return getTopEntries(new ArrayList<PerUserAPIUsageDTO>(usageByUsername.values()), limit);
    }

    public List<APIVersionUserUsageDTO> getUsageBySubscriber(String subscriberName, String period) throws Exception, APIManagementException {

        List<APIVersionUserUsageDTO> apiUserUsages = new ArrayList<APIVersionUserUsageDTO>();

        String periodYear = period.split("-")[0];
        String periodMonth = period.split("-")[1];
        if (periodMonth.length() == 1) {
            periodMonth = "0" + periodMonth;
        }
        period = periodYear + "-" + periodMonth;

        if (subscriberName.equals(MultitenantUtils.getTenantAwareUsername(subscriberName))) {
            subscriberName = subscriberName + "@" + MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }

        Collection<APIVersionUsageByUserMonth> usageData = getUsageAPIBySubscriberMonthly(subscriberName, period);

            for (APIVersionUsageByUserMonth usageEntry : usageData) {

                if (usageEntry.username.equals(subscriberName) && usageEntry.month.equals(period)) {

                    List<APIUsageRangeCost> rangeCosts = evaluate(usageEntry.apiName, (int) usageEntry.requestCount);

                    for (APIUsageRangeCost rangeCost : rangeCosts)  {
                        APIVersionUserUsageDTO userUsageDTO = new APIVersionUserUsageDTO();
                        userUsageDTO.setApiname(usageEntry.apiName);
                        userUsageDTO.setContext(usageEntry.context);
                        userUsageDTO.setVersion(usageEntry.apiVersion);
                        userUsageDTO.setCount(rangeCost.getRangeInvocationCount());
                        userUsageDTO.setCost(rangeCost.getCost().toString());
                        userUsageDTO.setCostPerAPI(rangeCost.getCostPerUnit().toString());
                        apiUserUsages.add(userUsageDTO);

                    }
                }
            }

        return apiUserUsages;
    }

    private Set<SubscribedAPI> getSubscribedAPIs(String subscriberName) throws APIManagementException {
        return apiConsumerImpl.getSubscribedAPIs(new Subscriber(subscriberName));
    }

    private List<PerUserAPIUsageDTO> getTopEntries(List<PerUserAPIUsageDTO> usageData, int limit) {
        Collections.sort(usageData, new Comparator<PerUserAPIUsageDTO>() {
            public int compare(PerUserAPIUsageDTO o1, PerUserAPIUsageDTO o2) {
                // Note that o2 appears before o1
                // This is because we need to sort in the descending order
                return (int) (o2.getCount() - o1.getCount());
            }
        });
        if (usageData.size() > limit) {
            PerUserAPIUsageDTO other = new PerUserAPIUsageDTO();
            other.setUsername("[Other]");
            for (int i = limit; i < usageData.size(); i++) {
                other.setCount(other.getCount() + usageData.get(i).getCount());
            }
            while (usageData.size() > limit) {
                usageData.remove(limit);
            }
            usageData.add(other);
        }

        return usageData;
    }

    private List<APIUsageDTO> getAPIUsageTopEntries(List<APIUsageDTO> usageData, int limit) {
        Collections.sort(usageData, new Comparator<APIUsageDTO>() {
            public int compare(APIUsageDTO o1, APIUsageDTO o2) {
                // Note that o2 appears before o1
                // This is because we need to sort in the descending order
                return (int) (o2.getCount() - o1.getCount());
            }
        });
        if (usageData.size() > limit) {
            APIUsageDTO other = new APIUsageDTO();
            other.setApiName("[\"Other\"]");
            for (int i = limit; i < usageData.size(); i++) {
                other.setCount(other.getCount() + usageData.get(i).getCount());
            }
            while (usageData.size() > limit) {
                usageData.remove(limit);
            }
            usageData.add(other);
        }

        return usageData;
    }

    private List<APIResponseTimeDTO> getResponseTimeTopEntries(List<APIResponseTimeDTO> usageData,
                                                               int limit) {
        Collections.sort(usageData, new Comparator<APIResponseTimeDTO>() {
            public int compare(APIResponseTimeDTO o1, APIResponseTimeDTO o2) {
                // Note that o2 appears before o1
                // This is because we need to sort in the descending order
                return (int) (o2.getServiceTime() - o1.getServiceTime());
            }
        });
        if (usageData.size() > limit) {
            while (usageData.size() > limit) {
                usageData.remove(limit);
            }
        }
        return usageData;
    }

    private List<APIVersionLastAccessTimeDTO> getLastAccessTimeTopEntries(
            List<APIVersionLastAccessTimeDTO> usageData, int limit) {
        Collections.sort(usageData, new Comparator<APIVersionLastAccessTimeDTO>() {
            public int compare(APIVersionLastAccessTimeDTO o1, APIVersionLastAccessTimeDTO o2) {
                // Note that o2 appears before o1
                // This is because we need to sort in the descending order
                return o2.getLastAccessTime().compareToIgnoreCase(o1.getLastAccessTime());
            }
        });
        if (usageData.size() > limit) {
            while (usageData.size() > limit) {
                usageData.remove(limit);
            }
        }

        return usageData;
    }

    private String getNextStringInLexicalOrder(String str) {
        if ((str == null) || (str.equals(""))) {
            return str;
        }
        byte[] bytes = str.getBytes();
        byte last = bytes[bytes.length - 1];
        last = (byte) (last + 1);        // Not very accurate. Need to improve this more to handle overflows.
        bytes[bytes.length - 1] = last;
        return new String(bytes);
    }

    /**
     * @deprecated please do not use this function as this may cause memory overflow. This loads a whole database table into memory as XML object
     * @param tableName - database table
     * @return OMElement
     * @throws APIMgtUsageQueryServiceClientException
     * Fetches the data from the passed table and builds a OEMElemnet
     */
    @Deprecated
    private OMElement buildOMElementFromDatabaseTable(String tableName) throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure " +
                    "that the data source is properly configured in the APIUsageTracker configuration.");
        }

        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String query;
            StringBuilder returnStringBuilder = new StringBuilder("<omElement><rows>");
            //check whether table exist first
            if (isTableExist(tableName, connection)) {//Table Exist

                query = "SELECT * FROM  " + tableName;

                rs = statement.executeQuery(query);
                int columnCount = rs.getMetaData().getColumnCount();

                while (rs.next()) {
                    returnStringBuilder.append("<row>");
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = rs.getMetaData().getColumnName(i);
                        String columnValue = rs.getString(columnName);
                        returnStringBuilder.append("<" + columnName.toLowerCase() + ">" + columnValue +
                                "</" + columnName.toLowerCase() + ">");
                    }
                    returnStringBuilder.append("</row>");
                }
            }
            returnStringBuilder.append("</rows></omElement>");
            String returnString = returnStringBuilder.toString();
            return AXIOMUtil.stringToOM(returnString);

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
     * @deprecated please do not use this function as this may cause memory overflow.
     * This loads a whole database table into memory as XML object
     *
     * @param columnFamily name of the table
     * @param fromDate starting date of the duration for the query
     * @param toDate last date of the duration for the query
     * @return XML object containing the specified table
     * @throws APIMgtUsageQueryServiceClientException if an error occurs while retrieving data
     */
    @Deprecated
    private OMElement queryBetweenTwoDays(String columnFamily, String fromDate, String toDate)
            throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure " +
                    "that the data source is properly configured in the APIUsageTracker configuration.");
        }

        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String query;
            //TODO: API_FAULT_COUNT need to populate according to match with given time range
            if (!columnFamily.equals(APIUsageStatisticsClientConstants.API_FAULT_SUMMARY)) {
                query = "SELECT * FROM  " + columnFamily + " WHERE " + APIUsageStatisticsClientConstants.TIME + " BETWEEN " +
                        "\'" + fromDate + "\' AND \'" + toDate + "\'";
            } else {
                query = "SELECT * FROM  " + columnFamily;
            }
            rs = statement.executeQuery(query);
            StringBuilder returnStringBuilder = new StringBuilder("<omElement><rows>");
            int columnCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                returnStringBuilder.append("<row>");
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rs.getMetaData().getColumnName(i);
                    String columnValue = rs.getString(columnName);
                    String xmlEscapedValue = StringEscapeUtils.escapeXml(columnValue);
                    returnStringBuilder.append("<" + columnName.toLowerCase() + ">" + xmlEscapedValue +
                            "</" + columnName.toLowerCase() + ">");
                }
                returnStringBuilder.append("</row>");
            }
            returnStringBuilder.append("</rows></omElement>");
            String returnString = returnStringBuilder.toString();
            return AXIOMUtil.stringToOM(returnString);

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

    private OMElement queryBetweenTwoDaysForAPIRequestsByHour(String columnFamily, String fromDate, String toDate,String apiName)
            throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure " +
                                                             "that the data source is properly configured in the APIUsageTracker configuration.");
        }

        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String query;
            //TODO: API_FAULT_COUNT need to populate according to match withQuery given time range

            query = "SELECT * FROM  " + columnFamily + " WHERE " + " API =\'" + apiName + "\' AND "+" requestTime "+ " BETWEEN " +
                    "\'" + fromDate + "\' AND \'" + toDate + "\' ";

            rs = statement.executeQuery(query);
            StringBuilder returnStringBuilder = new StringBuilder("<omElement><rows>");
            int columnCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                returnStringBuilder.append("<row>");
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rs.getMetaData().getColumnName(i);
                    String columnValue = rs.getString(columnName);
                    String xmlEscapedValue = StringEscapeUtils.escapeXml(columnValue);
                    returnStringBuilder.append("<" + columnName.toLowerCase() + ">" + xmlEscapedValue +
                                               "</" + columnName.toLowerCase() + ">");
                }
                returnStringBuilder.append("</row>");
            }
            returnStringBuilder.append("</rows></omElement>");
            String returnString = returnStringBuilder.toString();
            return AXIOMUtil.stringToOM(returnString);

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

    @Deprecated
    private OMElement queryBetweenTwoDaysForAPIsFromAPIRequestsPerHourTable(String columnFamily, String fromDate, String toDate)
            throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure " +
                                                             "that the data source is properly configured in the APIUsageTracker configuration.");
        }

        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String query;
            //TODO: API_FAULT_COUNT need to populate according to match with given time range

            query = "SELECT DISTINCT API FROM  " + columnFamily + " WHERE TIER<>\'Unauthenticated\' AND"+" requestTime "+ " BETWEEN " +
                    "\'" + fromDate + "\' AND \'" + toDate + "\' ";

            rs = statement.executeQuery(query);
            StringBuilder returnStringBuilder = new StringBuilder("<omElement><rows>");
            int columnCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                returnStringBuilder.append("<row>");
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rs.getMetaData().getColumnName(i);
                    String columnValue = rs.getString(columnName);
                    String xmlEscapedValue = StringEscapeUtils.escapeXml(columnValue);
                    returnStringBuilder.append("<" + columnName.toLowerCase() + ">" + xmlEscapedValue +
                                               "</" + columnName.toLowerCase() + ">");
                }
                returnStringBuilder.append("</row>");
            }
            returnStringBuilder.append("</rows></omElement>");
            String returnString = returnStringBuilder.toString();
            return AXIOMUtil.stringToOM(returnString);

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

    private List<APIResponseFaultCount> queryBetweenTwoDaysForFaulty(String tableName, String fromDate,
            String toDate)
            throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure " +
                    "that the data source is properly configured in the APIUsageTracker configuration.");
        }

        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        List<APIResponseFaultCount> faultusage = new ArrayList<APIResponseFaultCount>();

        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String query;

            query = "SELECT api,version,apiPublisher,context,SUM(total_fault_count) as total_fault_count FROM  "
                    + tableName + " WHERE " + APIUsageStatisticsClientConstants.TIME + " BETWEEN " +
                    "\'" + fromDate + "\' AND \'" + toDate + "\'" + " GROUP BY api,version,apiPublisher,context";

            rs = statement.executeQuery(query);
            APIResponseFaultCount apiResponseFaultCount;

            while (rs.next()) {
                String apiName = rs.getString("api");
                String version = rs.getString("version");
                String context = rs.getString("context");
                long requestCount = rs.getLong("total_fault_count");
                apiResponseFaultCount = new APIResponseFaultCount(apiName, version, context, requestCount);
                faultusage.add(apiResponseFaultCount);
            }
            return faultusage;

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

    private List<APIUsageByResourcePath> queryToGetAPIUsageByResourcePath(String tableName, String fromDate,
            String toDate)
            throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure " +
                    "that the data source is properly configured in the APIUsageTracker configuration.");
        }

        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        List<APIUsageByResourcePath> usage=new ArrayList<APIUsageByResourcePath>();
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String query;

            query = "SELECT api,version,apiPublisher,context,method,total_request_count,time FROM "
                    + tableName + " WHERE " + APIUsageStatisticsClientConstants.TIME + " BETWEEN " +
                    "\'" + fromDate + "\' AND \'" + toDate + "\'";
            rs = statement.executeQuery(query);
            APIUsageByResourcePath apiUsageByResourcePath;

            while (rs.next()) {
                String apiName = rs.getString("api");
                String version = rs.getString("version");
                String context = rs.getString("context");
                String method = rs.getString("method");
                long hits = rs.getLong("total_request_count");
                String time = rs.getString("time");
                apiUsageByResourcePath = new APIUsageByResourcePath(apiName, version, method, context, hits, time);
                usage.add(apiUsageByResourcePath);
            }
            return usage;

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

    private List<APIUsageByDestination> queryToGetAPIUsageByDestination(String tableName, String fromDate,
            String toDate)
            throws APIMgtUsageQueryServiceClientException {
        if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure " +
                    "that the data source is properly configured in the APIUsageTracker configuration.");
        }

        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        List<APIUsageByDestination> usageByResourcePath = new ArrayList<APIUsageByDestination>();

        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String query;

            query = "SELECT api,version,apiPublisher,context,destination,SUM(total_request_count) as total_request_count FROM  "
                    + tableName + " WHERE " + APIUsageStatisticsClientConstants.TIME + " BETWEEN " +
                    "\'" + fromDate + "\' AND \'" + toDate + "\'" + " GROUP BY api,version,apiPublisher,context,destination";

            rs = statement.executeQuery(query);
            APIUsageByDestination apiUsageByDestination;

            while (rs.next()) {
                String apiName = rs.getString("api");
                String version = rs.getString("version");
                String context = rs.getString("context");
                String destination = rs.getString("destination");
                long requestCount = rs.getLong("total_request_count");
                apiUsageByDestination = new APIUsageByDestination(apiName, version, context, destination,
                        requestCount);
                usageByResourcePath.add(apiUsageByDestination);
            }
            return usageByResourcePath;

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

    private List<APIUsage> queryBetweenTwoDaysForAPIUsageByVersion(String tableName, String fromDate, String toDate,
            String apiName)
            throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure " +
                    "that the data source is properly configured in the APIUsageTracker configuration.");
        }

        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        List<APIUsage> usageDataList = new ArrayList<APIUsage>();

        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String query;
            if (fromDate != null && toDate != null) {
                query = "SELECT api,version,apiPublisher,context,SUM(total_request_count) as total_request_count" +
                        " FROM  " + tableName +
                        " WHERE api =\'" + apiName + "\' " +
                        " AND " + APIUsageStatisticsClientConstants.TIME +
                        " BETWEEN " + "\'" + fromDate + "\' " +
                        " AND \'" + toDate + "\'" +
                        " GROUP BY api,version,apiPublisher,context";
            } else {
                query = "SELECT api,version,apiPublisher,context,SUM(total_request_count) as total_request_count" +
                        " FROM  " + tableName +
                        " WHERE api =\'" + apiName + "\' " +
                        " GROUP BY api,version,apiPublisher,context";
            }
            rs = statement.executeQuery(query);

            while (rs.next()) {
                String context = rs.getString(APIUsageStatisticsClientConstants.CONTEXT);
                String version = rs.getString(APIUsageStatisticsClientConstants.VERSION);
                long requestCount = rs.getLong("total_request_count");
                usageDataList.add(new APIUsage(apiName, context, version, requestCount));
            }

            return usageDataList;

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

    private List<APIUsageByUserName> queryBetweenTwoDaysForAPIUsageByUser(String providerName, String fromDate, String toDate, Integer limit)
            throws APIMgtUsageQueryServiceClientException {
        if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure " +
                    "that the data source is properly configured in the APIUsageTracker configuration.");
        }

        int resultsLimit = APIUsageStatisticsClientConstants.DEFAULT_RESULTS_LIMIT;
        if (limit != null) {
            resultsLimit = limit.intValue();
        }

        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String query;
            String oracleQuery;
            String mssqlQuery;
            if (fromDate != null && toDate != null) {
                query = "SELECT API, API_VERSION,VERSION, APIPUBLISHER, USERID, SUM(TOTAL_REQUEST_COUNT) AS TOTAL_REQUEST_COUNT, CONTEXT " +
                        "FROM API_REQUEST_SUMMARY" + " WHERE " + APIUsageStatisticsClientConstants.TIME + " BETWEEN " +
                        "\'" + fromDate + "\' AND \'" + toDate + "\'" + 
                        " GROUP BY API, API_VERSION, USERID, VERSION, APIPUBLISHER, CONTEXT ORDER BY TOTAL_REQUEST_COUNT DESC ";

                oracleQuery = "SELECT API, API_VERSION, VERSION, APIPUBLISHER, USERID, SUM(TOTAL_REQUEST_COUNT) AS TOTAL_REQUEST_COUNT, CONTEXT " +
                              "FROM API_REQUEST_SUMMARY" + " WHERE " + APIUsageStatisticsClientConstants.TIME + " BETWEEN " +
                              "\'" + fromDate + "\' AND \'" + toDate + "\'" + 
                              " GROUP BY API, API_VERSION, VERSION, USERID, APIPUBLISHER, CONTEXT ORDER BY TOTAL_REQUEST_COUNT DESC";


                mssqlQuery = "SELECT API, API_VERSION, VERSION, APIPUBLISHER, USERID, SUM(TOTAL_REQUEST_COUNT) AS TOTAL_REQUEST_COUNT, CONTEXT " +
                             "FROM API_REQUEST_SUMMARY" + " WHERE " + APIUsageStatisticsClientConstants.TIME + " BETWEEN " +
                             "\'" + fromDate + "\' AND \'" + toDate + "\'" + 
                             " GROUP BY API, API_VERSION, USERID, VERSION, APIPUBLISHER, CONTEXT ORDER BY TOTAL_REQUEST_COUNT DESC";
            } else {
                query = "SELECT API, API_VERSION, VERSION, APIPUBLISHER, USERID, SUM(TOTAL_REQUEST_COUNT) AS TOTAL_REQUEST_COUNT, CONTEXT " +
                        "FROM API_REQUEST_SUMMARY" + 
                        " GROUP BY API, API_VERSION, APIPUBLISHER, USERID ORDER BY TOTAL_REQUEST_COUNT DESC ";

                oracleQuery = "SELECT API, API_VERSION, VERSION, APIPUBLISHER, USERID, SUM(TOTAL_REQUEST_COUNT) AS TOTAL_REQUEST_COUNT, CONTEXT " +
                              "FROM API_REQUEST_SUMMARY" + 
                              " GROUP BY API, API_VERSION, VERSION, APIPUBLISHER, USERID, CONTEXT ORDER BY TOTAL_REQUEST_COUNT DESC ";

                mssqlQuery = "SELECT  API, API_VERSION, VERSION, APIPUBLISHER, USERID, SUM(TOTAL_REQUEST_COUNT) AS TOTAL_REQUEST_COUNT, CONTEXT " +
                             "FROM API_REQUEST_SUMMARY" + 
                             " GROUP BY API, API_VERSION, APIPUBLISHER, USERID ORDER BY TOTAL_REQUEST_COUNT DESC ";

            }
            if ((connection.getMetaData().getDriverName()).contains("Oracle")) {
                query = oracleQuery;
            }
            if(connection.getMetaData().getDatabaseProductName().contains("Microsoft")){
                query = mssqlQuery;
            }

            rs = statement.executeQuery(query);
            List<APIUsageByUserName> usageByName = new ArrayList<APIUsageByUserName>();
            String apiName;
            String apiVersion;
            String context;
            String userID;
            long requestCount;
            String publisher;

            while (rs.next()) {
                apiName = rs.getString("api");
                apiVersion = rs.getString("version");
                context = rs.getString("api");
                userID = rs.getString("userid");
                requestCount = rs.getLong("total_request_count");
                publisher = rs.getString("apipublisher");
                if (publisher != null) {
                    APIUsageByUserName usage = new APIUsageByUserName(apiName, apiVersion, context, userID,
                            requestCount, publisher);
                    usageByName.add(usage);
                }
            }
            return usageByName;

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

    public boolean isTableExist(String tableName, Connection connection) throws SQLException {
        //This return all tables,use this because it is not db specific, Passing table name doesn't
        //work with every database
        ResultSet tables = connection.getMetaData().getTables(null, null, "%", null);
        while (tables.next()) {
            if (tables.getString(3).equalsIgnoreCase(tableName)) {
                return true;
            }
        }
        tables.close();
        return false;
    }

    private List<API> getAPIsByProvider(String providerId) throws APIMgtUsageQueryServiceClientException {
        try {
            if (APIUsageStatisticsClientConstants.ALL_PROVIDERS.equals(providerId)) {
                return apiProviderImpl.getAllAPIs();
            } else {
                return apiProviderImpl.getAPIsByProvider(providerId);
            }
        } catch (APIManagementException e) {
            throw new APIMgtUsageQueryServiceClientException("Error while retrieving APIs by " + providerId, e);
        }
    }

    @Deprecated
    private Collection<APIUsage> getUsageData(OMElement data) {
        List<APIUsage> usageData = new ArrayList<APIUsage>();
        OMElement rowsElement = data.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.ROWS));
        Iterator rowIterator = rowsElement.getChildrenWithName(new QName(
                APIUsageStatisticsClientConstants.ROW));
        if (rowIterator != null) {
            while (rowIterator.hasNext()) {
                OMElement rowElement = (OMElement) rowIterator.next();
                usageData.add(new APIUsage(rowElement));
            }
        }
        return usageData;
    }

    @Deprecated
    private Collection<AppAPIUsage> getAppAPIUsageData(OMElement data) {
        List<AppAPIUsage> usageData = new ArrayList<AppAPIUsage>();
        OMElement rowsElement = data.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.ROWS));
        Iterator rowIterator = rowsElement.getChildrenWithName(new QName(
                APIUsageStatisticsClientConstants.ROW));
        if (rowIterator != null) {
            while (rowIterator.hasNext()) {
                OMElement rowElement = (OMElement) rowIterator.next();
                usageData.add(new AppAPIUsage(rowElement));
            }
        }
        return usageData;
    }

    @Deprecated
    private Collection<APIUsageByResourcePath> getUsageDataByResourcePath(OMElement data) {
        List<APIUsageByResourcePath> usageData = new ArrayList<APIUsageByResourcePath>();
        OMElement rowsElement = data.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.ROWS));
        Iterator rowIterator = rowsElement.getChildrenWithName(new QName(
                APIUsageStatisticsClientConstants.ROW));
        if (rowIterator != null) {
            while (rowIterator.hasNext()) {
                OMElement rowElement = (OMElement) rowIterator.next();
                usageData.add(new APIUsageByResourcePath(rowElement));
            }
        }
        return usageData;
    }

    @Deprecated
    private Collection<APIUsageByDestination> getUsageDataByDestination(OMElement data) {
        List<APIUsageByDestination> usageData = new ArrayList<APIUsageByDestination>();
        OMElement rowsElement = data.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.ROWS));
        Iterator rowIterator = rowsElement.getChildrenWithName(new QName(
                APIUsageStatisticsClientConstants.ROW));
        if (rowIterator != null) {
            while (rowIterator.hasNext()) {
                OMElement rowElement = (OMElement) rowIterator.next();
                usageData.add(new APIUsageByDestination(rowElement));
            }
        }
        return usageData;
    }

    @Deprecated
    private Collection<APIUsageByUserName> getUsageDataByAPIName(OMElement data, String tenantDomain) {
        List<APIUsageByUserName> usageData = new ArrayList<APIUsageByUserName>();
        OMElement rowsElement = data.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.ROWS));
        Iterator rowIterator = rowsElement.getChildrenWithName(new QName(
                APIUsageStatisticsClientConstants.ROW));
        if (rowIterator != null) {
            while (rowIterator.hasNext()) {
                OMElement rowElement = (OMElement) rowIterator.next();
                String apiProvider = null;
                if (rowElement.getFirstChildWithName(new QName("apipublisher")) != null) {
                    apiProvider = rowElement.getFirstChildWithName(new QName("apipublisher")).getText();
                    if (apiProvider != null && tenantDomain.equals(MultitenantUtils.getTenantDomain(apiProvider))) {
                        usageData.add(new APIUsageByUserName(rowElement));
                    }
                }

            }
        }
        return usageData;
    }

    @Deprecated
    private Collection<APIResponseFaultCount> getAPIResponseFaultCount(OMElement data) {
        List<APIResponseFaultCount> faultyData = new ArrayList<APIResponseFaultCount>();
        OMElement rowsElement = data.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.ROWS));
        Iterator rowIterator = rowsElement.getChildrenWithName(new QName(
                APIUsageStatisticsClientConstants.ROW));
        if (rowIterator != null) {
            while (rowIterator.hasNext()) {
                OMElement rowElement = (OMElement) rowIterator.next();
                faultyData.add(new APIResponseFaultCount(rowElement));
            }
        }
        return faultyData;
    }

    @Deprecated
    private Collection<AppAPIResponseFaultCount> getAppAPIResponseFaultCount(OMElement data) {
        List<AppAPIResponseFaultCount> faultyData = new ArrayList<AppAPIResponseFaultCount>();
        OMElement rowsElement = data.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.ROWS));
        Iterator rowIterator = rowsElement.getChildrenWithName(new QName(
                APIUsageStatisticsClientConstants.ROW));
        if (rowIterator != null) {
            while (rowIterator.hasNext()) {
                OMElement rowElement = (OMElement) rowIterator.next();
                faultyData.add(new AppAPIResponseFaultCount(rowElement));
            }
        }
        return faultyData;
    }

    @Deprecated
    private Collection<APIResponseTime> getResponseTimeData(OMElement data) {
        List<APIResponseTime> responseTimeData = new ArrayList<APIResponseTime>();

        OMElement rowsElement = data.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.ROWS));

        Iterator rowIterator = rowsElement.getChildrenWithName(new QName(
                APIUsageStatisticsClientConstants.ROW));
        if (rowIterator != null) {
            while (rowIterator.hasNext()) {
                OMElement rowElement = (OMElement) rowIterator.next();
                if (rowElement.getFirstChildWithName(new QName(
                        APIUsageStatisticsClientConstants.SERVICE_TIME)) != null) {
                    responseTimeData.add(new APIResponseTime(rowElement));
                }
            }
        }
        return responseTimeData;
    }

    @Deprecated
    private Collection<APIAccessTime> getAccessTimeData(OMElement data) {
        List<APIAccessTime> accessTimeData = new ArrayList<APIAccessTime>();
        OMElement rowsElement = data.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.ROWS));
        Iterator rowIterator = rowsElement.getChildrenWithName(new QName(
                APIUsageStatisticsClientConstants.ROW));
        if (rowIterator != null) {
            while (rowIterator.hasNext()) {
                OMElement rowElement = (OMElement) rowIterator.next();
                accessTimeData.add(new APIAccessTime(rowElement));
            }
        }
        return accessTimeData;
    }

    @Deprecated
    private Collection<APIUsageByUser> getUsageBySubscriber(OMElement data) {
        List<APIUsageByUser> usageData = new ArrayList<APIUsageByUser>();
        OMElement rowsElement = data.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.ROWS));
        Iterator rowIterator = rowsElement.getChildrenWithName(new QName(
                APIUsageStatisticsClientConstants.ROW));
        if (rowIterator != null) {
            while (rowIterator.hasNext()) {
                OMElement rowElement = (OMElement) rowIterator.next();
                usageData.add(new APIUsageByUser(rowElement));
            }
        }
        return usageData;
    }

    @Deprecated
    private Collection<APIUserAgent> getUserAgent(OMElement data) {
        List<APIUserAgent> userAgentData = new ArrayList<APIUserAgent>();
        OMElement rowsElement = data.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.ROWS));
        Iterator rowIterator = rowsElement.getChildrenWithName(new QName(
                APIUsageStatisticsClientConstants.ROW));
        if (rowIterator != null) {
            while (rowIterator.hasNext()) {
                OMElement rowElement = (OMElement) rowIterator.next();
                userAgentData.add(new APIUserAgent(rowElement));
            }
        }
        return userAgentData;
    }

    @Deprecated
    private Collection<APIRequestsByHour> getAPIRequestsByHour(OMElement data) {
        List<APIRequestsByHour> apiRequestsByHours = new ArrayList<APIRequestsByHour>();
        OMElement rowsElement = data.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.ROWS));
        Iterator rowIterator = rowsElement.getChildrenWithName(new QName(
                APIUsageStatisticsClientConstants.ROW));
        if (rowIterator != null) {
            while (rowIterator.hasNext()) {
                OMElement rowElement = (OMElement) rowIterator.next();
                if(!rowElement.getFirstChildWithName(new QName("tier")).getText().equalsIgnoreCase("Unauthenticated")){
                    apiRequestsByHours.add(new APIRequestsByHour(rowElement));
                }

            }
        }
        return apiRequestsByHours;
    }

    @Deprecated
    private Collection<String> getAPIsFromAPIRequestByHour(OMElement data) {
        List<String> apisList = new ArrayList<String>();
        OMElement rowsElement = data.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.ROWS));
        Iterator rowIterator = rowsElement.getChildrenWithName(new QName(
                APIUsageStatisticsClientConstants.ROW));
        if (rowIterator != null) {
            while (rowIterator.hasNext()) {
                OMElement rowElement = (OMElement) rowIterator.next();
                apisList.add(new String(rowElement.getFirstChildWithName(new QName("api")).getText()));

            }
        }
        return apisList;
    }

    @Deprecated
    private Collection<APIVersionUsageByUser> getUsageAPIBySubscriber(OMElement data) {
        List<APIVersionUsageByUser> usageData = new ArrayList<APIVersionUsageByUser>();
        OMElement rowsElement = data.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.ROWS));
        Iterator rowIterator = rowsElement.getChildrenWithName(new QName(
                APIUsageStatisticsClientConstants.ROW));
        if (rowIterator != null) {
            while (rowIterator.hasNext()) {
                OMElement rowElement = (OMElement) rowIterator.next();
                for (int i = 0; i < usageData.size(); i++) {
                    if (usageData.get(i).apiName.equals(rowElement.getFirstChildWithName(new QName(
                            APIUsageStatisticsClientConstants.API)).getText()) && usageData.get(i).apiVersion.equals(rowElement.getFirstChildWithName(new QName(
                            APIUsageStatisticsClientConstants.VERSION)).getText())) {
                        usageData.get(i).requestCount = usageData.get(i).requestCount + (long) Double.parseDouble(rowElement.getFirstChildWithName(new QName(
                                APIUsageStatisticsClientConstants.REQUEST)).getText());
                        //    return usageData;
                    }

                }
                usageData.add(new APIVersionUsageByUser(rowElement));
            }
        }
        return usageData;
    }

    @Deprecated
    private Collection<APIVersionUsageByUserMonth> getUsageAPIBySubscriberMonthly(OMElement data) {
        List<APIVersionUsageByUserMonth> usageData = new ArrayList<APIVersionUsageByUserMonth>();
        OMElement rowsElement = data.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.ROWS));
        Iterator rowIterator = rowsElement.getChildrenWithName(new QName(
                APIUsageStatisticsClientConstants.ROW));
        if (rowIterator != null) {
            while (rowIterator.hasNext()) {
                OMElement rowElement = (OMElement) rowIterator.next();
                for (int i = 0; i < usageData.size(); i++) {
                    if (usageData.get(i).apiName.equals(rowElement.getFirstChildWithName(new QName(
                            APIUsageStatisticsClientConstants.API)).getText()) && usageData.get(i).apiVersion.equals(rowElement.getFirstChildWithName(new QName(
                            APIUsageStatisticsClientConstants.VERSION)).getText())) {
                        usageData.get(i).requestCount = usageData.get(i).requestCount + (long) Double.parseDouble(rowElement.getFirstChildWithName(new QName(
                                APIUsageStatisticsClientConstants.REQUEST)).getText());
                    }

                }
                usageData.add(new APIVersionUsageByUserMonth(rowElement));
            }
        }
        return usageData;
    }

    private Collection<APIVersionUsageByUserMonth> getUsageAPIBySubscriberMonthly(String subscriberName, String period)
            throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure " +
                    "that the data source is properly configured in the APIUsageTracker configuration.");
        }

        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        Collection<APIVersionUsageByUserMonth> usageData = new ArrayList<APIVersionUsageByUserMonth>();
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String query;
            //check whether table exist first
            if (isTableExist(APIUsageStatisticsClientConstants.KEY_USAGE_MONTH_SUMMARY, connection)) {//Table Exist

                query = "SELECT " + APIUsageStatisticsClientConstants.API + ","
                        + APIUsageStatisticsClientConstants.VERSION + "," + APIUsageStatisticsClientConstants.CONTEXT
                        + ",sum(" + APIUsageStatisticsClientConstants.REQUEST + ") as "
                        + APIUsageStatisticsClientConstants.REQUEST + "," + APIUsageStatisticsClientConstants.MONTH
                        + "," + APIUsageStatisticsClientConstants.USER_ID + " FROM  "
                        + APIUsageStatisticsClientConstants.KEY_USAGE_MONTH_SUMMARY + " WHERE "
                        + APIUsageStatisticsClientConstants.MONTH + " = '" + period + "' AND "
                        + APIUsageStatisticsClientConstants.USER_ID + " = '" + subscriberName + "' GROUP BY "
                        + APIUsageStatisticsClientConstants.API_VERSION + ", "
                        + APIUsageStatisticsClientConstants.USER_ID + ", " + APIUsageStatisticsClientConstants.MONTH;

                rs = statement.executeQuery(query);

                while (rs.next()) {
                    String apiName = rs.getString(APIUsageStatisticsClientConstants.API);
                    String apiVersion = rs.getString(APIUsageStatisticsClientConstants.VERSION);
                    String context = rs.getString(APIUsageStatisticsClientConstants.CONTEXT);
                    String username = rs.getString(APIUsageStatisticsClientConstants.USER_ID);
                    long requestCount = rs.getLong(APIUsageStatisticsClientConstants.REQUEST);
                    String month = rs.getString(APIUsageStatisticsClientConstants.MONTH);
                    usageData.add(new APIVersionUsageByUserMonth(apiName, apiVersion, context, username, requestCount,
                            month));
                }
            }

            return usageData;

        } catch (SQLException e) {
            throw new APIMgtUsageQueryServiceClientException("Error occurred while querying from JDBC database", e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing the result set from JDBC database.", e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing the statement from JDBC database.", e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing the JDBC database connection.", e);
                }
            }
        }
    }

    public List<APIUsageRangeCost> evaluate(String param, int calls) throws Exception {
        return paymentPlan.evaluate(param, calls);
    }

    @Deprecated
    private Collection<APIFirstAccess> getFirstAccessTime(OMElement data) {
        List<APIFirstAccess> usageData = new ArrayList<APIFirstAccess>();
        OMElement rowsElement = data.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.ROWS));
        OMElement rowElement = rowsElement.getFirstChildWithName(new QName(APIUsageStatisticsClientConstants.ROW));

        if (rowElement!=null) {
            usageData.add(new APIFirstAccess(rowElement));
        }

        return usageData;
    }

    public List<String> getFirstAccessTime(String providerName, int limit)
            throws APIMgtUsageQueryServiceClientException {

        APIFirstAccess firstAccess = this.queryFirstAccess(
                APIUsageStatisticsClientConstants.KEY_USAGE_SUMMARY);
        List<String> APIFirstAccessList = new ArrayList<String>();

        if (firstAccess != null) {
            APIFirstAccessList.add(firstAccess.getYear());
            APIFirstAccessList.add(firstAccess.getMonth());
            APIFirstAccessList.add(firstAccess.getDay());
        }
        return APIFirstAccessList;
    }

    private APIFirstAccess queryFirstAccess(String columnFamily)
            throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure " +
                    "that the data source is properly configured in the APIUsageTracker configuration.");
        }

        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String query;
            if (connection != null && connection.getMetaData().getDatabaseProductName().equalsIgnoreCase("oracle")) {

                query = "SELECT time,year,month,day FROM  " + columnFamily + " WHERE ROWNUM <= 1 order by time ASC";

            } else if (connection != null && connection.getMetaData().getDatabaseProductName().contains("Microsoft")) {

                query = "SELECT TOP 1 time,year,month,day FROM  " + columnFamily + " order by time ASC";

            } else {

                query = "SELECT time,year,month,day FROM  " + columnFamily + " order by time ASC limit 1";

            }
            rs = statement.executeQuery(query);
            String year;
            String month;
            String day;
            APIFirstAccess firstAccess = null;

            while (rs.next()) {
                year = rs.getString("year");
                month = rs.getString("month");
                day = rs.getString("day");
                firstAccess = new APIFirstAccess(year, month, day);
            }

            return firstAccess;

        } catch (Exception e) {
            throw new APIMgtUsageQueryServiceClientException("Error occurred while querying from JDBC database" +
                                                             e.getMessage(), e);
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

    private Collection<APIUsageByUser> getUsageOfAPI(String apiName, String apiVersion)
            throws APIMgtUsageQueryServiceClientException {
        if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure " +
                    "that the data source is properly configured in the APIUsageTracker configuration.");
        }

        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        Collection<APIUsageByUser> usageData = new ArrayList<APIUsageByUser>();
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String query;
            //check whether table exist first
            if (isTableExist(APIUsageStatisticsClientConstants.KEY_USAGE_SUMMARY, connection)) {//Table Exists
                query = "SELECT * FROM " + APIUsageStatisticsClientConstants.KEY_USAGE_SUMMARY
                        + " WHERE " + APIUsageStatisticsClientConstants.API + " = '" + apiName + "'";
                if (apiVersion != null) {
                    query += " AND " + APIUsageStatisticsClientConstants.VERSION + " = '" + apiVersion + "'";
                }
                rs = statement.executeQuery(query);
                while (rs.next()) {
                    String context = rs.getString(APIUsageStatisticsClientConstants.CONTEXT);
                    String username = rs.getString(APIUsageStatisticsClientConstants.USER_ID);
                    long requestCount = rs.getLong(APIUsageStatisticsClientConstants.REQUEST);
                    String version = rs.getString(APIUsageStatisticsClientConstants.VERSION);
                    usageData.add(new APIUsageByUser(context,username,requestCount,version));
                }
            }
            return usageData;

        } catch (SQLException e) {
            throw new APIMgtUsageQueryServiceClientException("Error occurred while querying from JDBC database", e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing the result set from JDBC database.",e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing the statement from JDBC database.",e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing the JDBC database connection.",e);
                }
            }
        }
    }

    /** Given API name and Application, returns throttling request counts over time for a given time span
     *
     * @param apiName Name of the API
     * @param provider Provider name
     * @param appName Application name
     * @param fromDate Start date of the time span
     * @param toDate End date of time span
     * @param groupBy Group by parameter. Supported parameters are :day,hour
     * @return Throttling counts over time
     * @throws APIMgtUsageQueryServiceClientException
     */
    public List<APIThrottlingOverTimeDTO> getThrottleDataOfAPIAndApplication(String apiName, String provider,
            String appName, String fromDate, String toDate, String groupBy)
            throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure " +
                    "that the data source is properly configured in the APIUsageTracker configuration.");
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            String query, groupByStmt;
            List<APIThrottlingOverTimeDTO> throttlingData = new ArrayList<APIThrottlingOverTimeDTO>();
            String tenantDomain = MultitenantUtils.getTenantDomain(provider);

            //check whether table exist first
            if (isTableExist(APIUsageStatisticsClientConstants.API_THROTTLED_OUT_SUMMARY, connection)) { //Table exists

                if (APIUsageStatisticsClientConstants.GROUP_BY_DAY.equals(groupBy)){
                    groupByStmt = "year, month, day";
                } else if (APIUsageStatisticsClientConstants.GROUP_BY_HOUR.equals(groupBy)){
                    groupByStmt = "year, month, day, time";
                } else {
                    throw new APIMgtUsageQueryServiceClientException(
                            "Unsupported group by parameter " + groupBy +
                                    " for retrieving throttle data of API and app.");
                }

                query = "SELECT " + groupByStmt + " ," +
                        "SUM(COALESCE(success_request_count,0)) AS success_request_count, " +
                        "SUM(COALESCE(throttleout_count,0)) AS throttleout_count " +
                        "FROM API_THROTTLED_OUT_SUMMARY " +
                        "WHERE tenantDomain = ? " +
                        "AND api = ? " +
                        (provider.startsWith(APIUsageStatisticsClientConstants.ALL_PROVIDERS) ? "" :
                                "AND apiPublisher = ?") +
                        (StringUtils.isEmpty(appName) ? "" : " AND applicationName = ?") +
                        "AND time BETWEEN ? AND ? " +
                        "GROUP BY " + groupByStmt + " " +
                        "ORDER BY " + groupByStmt + " ASC";

                statement = connection.prepareStatement(query);
                int index = 1;
                statement.setString(index++, tenantDomain);
                statement.setString(index++, apiName);
                if (!provider.startsWith(APIUsageStatisticsClientConstants.ALL_PROVIDERS)) {
                    statement.setString(index++, provider);
                }
                if (!StringUtils.isEmpty(appName)) {
                    statement.setString(index++, appName);
                }
                statement.setString(index++, fromDate);
                statement.setString(index, toDate);

                rs = statement.executeQuery();
                while (rs.next()) {
                    int successRequestCount = rs.getInt(APIUsageStatisticsClientConstants.SUCCESS_REQUEST_COUNT);
                    int throttledOutCount = rs.getInt(APIUsageStatisticsClientConstants.THROTTLED_OUT_COUNT);
                    int year =  rs.getInt(APIUsageStatisticsClientConstants.YEAR);
                    int month =  rs.getInt(APIUsageStatisticsClientConstants.MONTH);
                    String time;
                    if (APIUsageStatisticsClientConstants.GROUP_BY_HOUR.equals(groupBy)) {
                        time = rs.getString(APIUsageStatisticsClientConstants.TIME);
                    } else {
                        int day =  rs.getInt(APIUsageStatisticsClientConstants.DAY);
                        time = year + "-" + month + "-" + day + " 00:00:00";
                    }
                    throttlingData.add(
                            new APIThrottlingOverTimeDTO(apiName, provider, successRequestCount, throttledOutCount,
                                    time)
                    );
                }

            } else {
                throw new APIMgtUsageQueryServiceClientException(
                        "Statistics Table:" + APIUsageStatisticsClientConstants.API_THROTTLED_OUT_SUMMARY +
                                " does not exist.");
            }

            return throttlingData;

        } catch (SQLException e) {
            throw new APIMgtUsageQueryServiceClientException("Error occurred while querying from JDBC database", e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing the result set from JDBC database.", e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing the prepared statement from JDBC database.", e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing the JDBC database connection.", e);
                }
            }
        }
    }

    /** Given Application name and the provider, returns throttle data for the APIs of the provider invoked by the
     *  given application
     *
     * @param appName Application name
     * @param provider Provider name
     * @param fromDate Start date of the time span
     * @param toDate End date of time span
     * @return Throttling counts of APIs of the provider invoked by the given app
     * @throws APIMgtUsageQueryServiceClientException
     */
    public List<APIThrottlingOverTimeDTO> getThrottleDataOfApplication(String appName, String provider,
            String fromDate, String toDate)
            throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure " +
                    "that the data source is properly configured in the APIUsageTracker configuration.");
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            String query;
            List<APIThrottlingOverTimeDTO> throttlingData = new ArrayList<APIThrottlingOverTimeDTO>();
            String tenantDomain = MultitenantUtils.getTenantDomain(provider);

            if (isTableExist(APIUsageStatisticsClientConstants.API_THROTTLED_OUT_SUMMARY, connection)) { //Table exists

                query = "SELECT api, apiPublisher, SUM(COALESCE(success_request_count,0)) " +
                        "AS success_request_count, SUM(COALESCE(throttleout_count,0)) as throttleout_count " +
                        "FROM API_THROTTLED_OUT_SUMMARY " +
                        "WHERE tenantDomain = ? " +
                        "AND applicationName = ? " +
                        (provider.startsWith(APIUsageStatisticsClientConstants.ALL_PROVIDERS) ? "" :
                                "AND apiPublisher = ?") +
                        "AND time BETWEEN ? AND ? " +
                        "GROUP BY api, apiPublisher " +
                        "ORDER BY api ASC";

                statement = connection.prepareStatement(query);
                int index = 1;
                statement.setString(index++, tenantDomain);
                statement.setString(index++, appName);
                if (!provider.startsWith(APIUsageStatisticsClientConstants.ALL_PROVIDERS)) {
                    statement.setString(index++, provider);
                }
                statement.setString(index++, fromDate);
                statement.setString(index, toDate);

                rs = statement.executeQuery();
                while (rs.next()) {
                    String api = rs.getString(APIUsageStatisticsClientConstants.API);
                    String apiPublisher = rs.getString(APIUsageStatisticsClientConstants.API_PUBLISHER_THROTTLE_TABLE);
                    int successRequestCount = rs.getInt(APIUsageStatisticsClientConstants.SUCCESS_REQUEST_COUNT);
                    int throttledOutCount = rs.getInt(APIUsageStatisticsClientConstants.THROTTLED_OUT_COUNT);
                    throttlingData
                            .add(new APIThrottlingOverTimeDTO(api, apiPublisher, successRequestCount, throttledOutCount,
                                    null));
                }

            } else {
                throw new APIMgtUsageQueryServiceClientException(
                        "Statistics Table:" + APIUsageStatisticsClientConstants.API_THROTTLED_OUT_SUMMARY +
                                " does not exist.");
            }

            return throttlingData;

        } catch (SQLException e) {
            throw new APIMgtUsageQueryServiceClientException("Error occurred while querying from JDBC database", e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing the result set from JDBC database.", e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing the prepared statement from JDBC database.", e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing the JDBC database connection.", e);
                }
            }
        }
    }

    /** Get APIs of the provider that consist of throttle data
     *
     * @param provider Provider name
     * @return List of APIs of the provider that consist of throttle data
     * @throws APIMgtUsageQueryServiceClientException
     */
    public List<String> getAPIsForThrottleStats(String provider)
            throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure " +
                    "that the data source is properly configured in the APIUsageTracker configuration.");
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            String query;
            List<String> throttlingAPIData = new ArrayList<String>();
            String tenantDomain = MultitenantUtils.getTenantDomain(provider);

            //check whether table exist first
            if (isTableExist(APIUsageStatisticsClientConstants.API_THROTTLED_OUT_SUMMARY, connection)) { //Tables exist

                query = "SELECT DISTINCT api FROM API_THROTTLED_OUT_SUMMARY " +
                        "WHERE tenantDomain = ? " +
                        (provider.startsWith(APIUsageStatisticsClientConstants.ALL_PROVIDERS) ? "" :
                                "AND apiPublisher = ? ") +
                        "ORDER BY api ASC";

                statement = connection.prepareStatement(query);
                statement.setString(1, tenantDomain);
                if (!provider.startsWith(APIUsageStatisticsClientConstants.ALL_PROVIDERS)) {
                    statement.setString(2, provider);
                }

                rs = statement.executeQuery();
                while (rs.next()) {
                    String api = rs.getString(APIUsageStatisticsClientConstants.API);
                    throttlingAPIData.add(api);
                }
            } else {
                throw new APIMgtUsageQueryServiceClientException(
                        "Statistics Table:" + APIUsageStatisticsClientConstants.API_THROTTLED_OUT_SUMMARY +
                                " does not exist.");
            }

            return throttlingAPIData;

        } catch (SQLException e) {
            throw new APIMgtUsageQueryServiceClientException("Error occurred while querying from JDBC database", e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing the result set from JDBC database.", e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing the prepared statement from JDBC database.", e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing the JDBC database connection.", e);
                }
            }
        }
    }

    /** Given provider name and the API name, returns a list of applications through which the corresponding API is
     *  invoked and which consist of success/throttled requests
     *
     * @param provider Provider name
     * @param apiName Name of th API
     * @return A list of applications through which the corresponding API is invoked and which consist of throttle data
     * @throws APIMgtUsageQueryServiceClientException
     */
    public List<String> getAppsForThrottleStats(String provider, String apiName)
            throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure " +
                    "that the data source is properly configured in the APIUsageTracker configuration.");
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            String query;
            List<String> throttlingAppData = new ArrayList<String>();
            String tenantDomain = MultitenantUtils.getTenantDomain(provider);

            //check whether table exist first
            if (isTableExist(APIUsageStatisticsClientConstants.API_THROTTLED_OUT_SUMMARY, connection)) { //Tables exist
                query = "SELECT DISTINCT applicationName FROM API_THROTTLED_OUT_SUMMARY " +
                        "WHERE tenantDomain = ? " +
                        (provider.startsWith(APIUsageStatisticsClientConstants.ALL_PROVIDERS) ? "" :
                                "AND apiPublisher = ? ") +
                        (apiName == null ? "" : "AND api = ? ") +
                        "ORDER BY applicationName ASC";

                statement = connection.prepareStatement(query);
                int index = 1;
                statement.setString(index++, tenantDomain);
                if (!provider.startsWith(APIUsageStatisticsClientConstants.ALL_PROVIDERS)) {
                    statement.setString(index++, provider);
                }
                if( apiName != null ){
                    statement.setString(index, apiName);
                }

                rs = statement.executeQuery();
                while (rs.next()) {
                    String applicationName = rs.getString(APIUsageStatisticsClientConstants.APPLICATION_NAME);
                    throttlingAppData.add(applicationName);
                }

            } else {
                throw new APIMgtUsageQueryServiceClientException(
                        "Statistics Table:" + APIUsageStatisticsClientConstants.API_THROTTLED_OUT_SUMMARY +
                                " does not exist.");
            }

            return throttlingAppData;

        } catch (SQLException e) {
            throw new APIMgtUsageQueryServiceClientException("Error occurred while querying from JDBC database", e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing the result set from JDBC database.", e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing the prepared statement from JDBC database.", e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    //this is logged and the process is continued because the query has executed
                    log.error("Error occurred while closing the JDBC database connection.", e);
                }
            }
        }
    }

    private static class AppUsage {


        private String userid;
        private long requestCount;
        private String consumerKey;

        @Deprecated
        public AppUsage(OMElement row) {

            userid = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.USER_ID)).getText();
            requestCount = (long) Double.parseDouble(row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.REQUEST)).getText());
            consumerKey = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.CONSUMERKEY)).getText();

        }

        public AppUsage(String userId, long requestCount, String consumerKey) {

            this.userid = userId;
            this.requestCount = requestCount;
            this.consumerKey = consumerKey;
        }
    }

    private static class AppCallType {

        private String apiName;
        private String callType;
        private String consumerKey;
        private String resource;

        @Deprecated
        public AppCallType(OMElement row) {
            apiName = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.API)).getText();
            consumerKey = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.CONSUMERKEY)).getText();
            callType = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.METHOD)).getText();
            resource = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.RESOURCE)).getText();
        }

        public AppCallType(String apiName, String callType, String consumerKey, String resource) {

            this.apiName = apiName;
            this.callType = callType;
            this.consumerKey = consumerKey;
            this.resource = resource;
        }
    }

    private static class APIUsage {

        private String apiName;
        private String apiVersion;
        private String context;
        private long requestCount;

        @Deprecated
        public APIUsage(OMElement row) {
            apiName = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.API)).getText();
            apiVersion = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.VERSION)).getText();
            context = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.CONTEXT)).getText();
            requestCount = (long) Double.parseDouble(row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.REQUEST)).getText());
        }

        public APIUsage(String apiName, String context, String apiVersion, long requestCount) {

            this.apiName = apiName;
            this.context = context;
            this.apiVersion = apiVersion;
            this.requestCount = requestCount;
        }
    }

    private static class AppAPIUsage {

        private String apiName;
        private String apiVersion;
        private String context;
        private long requestCount;
        private String consumerKey;

        @Deprecated
        public AppAPIUsage(OMElement row) {
            apiName = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.API)).getText();
            apiVersion = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.VERSION)).getText();
            context = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.CONTEXT)).getText();
            requestCount = (long) Double.parseDouble(row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.REQUEST)).getText());
            consumerKey = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.CONSUMERKEY)).getText();
        }

        public AppAPIUsage(String apiName, String apiVersion, String context, long requestCount, String consumerKey) {

            this.apiName = apiName;
            this.apiVersion = apiVersion;
            this.context = context;
            this.requestCount = requestCount;
            this.consumerKey = consumerKey;
        }
    }

    private static class APIUsageByUser {

        private String context;
        private String username;
        private long requestCount;
        private String apiVersion;

        @Deprecated
        public APIUsageByUser(OMElement row) {
            context = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.CONTEXT)).getText();
            username = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.USER_ID)).getText();
            requestCount = (long) Double.parseDouble(row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.REQUEST)).getText());
            apiVersion = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.VERSION)).getText();
        }

        public APIUsageByUser(String context, String username, long requestCount, String apiVersion) {
            this.context = context;
            this.username = username;
            this.requestCount = requestCount;
            this.apiVersion = apiVersion;
        }
    }

    private static class APIUsageByResourcePath {

        private String apiName;
        private String apiVersion;
        private String method;
        private String context;
        private long requestCount;
        private String time;

        public APIUsageByResourcePath(String apiName, String apiVersion, String method, String context,
                long requestCount, String time) {
            this.apiName = apiName;
            this.apiVersion = apiVersion;
            this.method = method;
            this.context = context;
            this.requestCount = requestCount;
            this.time = time;
        }

        @Deprecated
        public APIUsageByResourcePath(OMElement row) {
            apiName = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.API)).getText();
            apiVersion = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.VERSION)).getText();
            method = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.METHOD)).getText();
            context = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.CONTEXT)).getText();
            requestCount = (long) Double.parseDouble(row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.REQUEST)).getText());
            time = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.TIME)).getText();
        }
    }

    private static class APIUsageByDestination {

        private String apiName;
        private String apiVersion;
        private String context;
        private String destination;
        private long requestCount;

        public APIUsageByDestination(String apiName, String apiVersion, String context, String destination,
                long requestCount) {
            this.apiName = apiName;
            this.apiVersion = apiVersion;
            this.context = context;
            this.destination = destination;
            this.requestCount = requestCount;
        }

        @Deprecated
        public APIUsageByDestination(OMElement row) {
            apiName = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.API)).getText();
            apiVersion = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.VERSION)).getText();
            destination = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.DESTINATION)).getText();
            context = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.CONTEXT)).getText();
            requestCount = (long) Double.parseDouble(row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.REQUEST)).getText());
        }
    }

    private static class APIUsageByUserName {

        private String apiName;
        private String apiVersion;
        private String context;
        private String userID;
        private String apipublisher;
        private long requestCount;

        public APIUsageByUserName(String apiName, String apiVersion, String context, String userID, long requestCount,
                String apipublisher) {
            this.apiName = apiName;
            this.apiVersion = apiVersion;
            this.context = context;
            this.userID = userID;
            this.requestCount = requestCount;
            this.apipublisher = apipublisher;
        }

        @Deprecated
        public APIUsageByUserName(OMElement row) {
            apiName = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.API)).getText();
            apiVersion = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.VERSION)).getText();
            userID = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.USER_ID)).getText();
            requestCount = (long) Double.parseDouble(row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.REQUEST)).getText());
            context = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.CONTEXT)).getText();
        }
    }

    private static class APIResponseFaultCount {

        private String apiName;
        private String apiVersion;
        private String context;
//        private String requestTime;
        private long faultCount;

        public APIResponseFaultCount(String apiName, String apiVersion, String context, long faultCount) {
            this.apiName = apiName;
            this.apiVersion = apiVersion;
            this.context = context;
            this.faultCount = faultCount;
        }

        @Deprecated
        public APIResponseFaultCount(OMElement row) {
            apiName = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.API)).getText();
            apiVersion = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.VERSION)).getText();
            context = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.CONTEXT)).getText();
            OMElement invocationTimeEle = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.INVOCATION_TIME));
            OMElement faultCountEle = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.FAULT));
//            if (invocationTimeEle != null) {
//                requestTime = invocationTimeEle.getText();
//            }
            if (faultCountEle != null) {
                faultCount = (long) Double.parseDouble(faultCountEle.getText());
            }
        }
    }

    private static class AppAPIResponseFaultCount {

        private String apiName;
        private String apiVersion;
        private String context;
        private String requestTime;
        private long faultCount;
        private String consumerKey;

        @Deprecated
        public AppAPIResponseFaultCount(OMElement row) {
            apiName = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.API)).getText();
            apiVersion = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.VERSION)).getText();
            context = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.CONTEXT)).getText();
            OMElement invocationTimeEle = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.INVOCATION_TIME));
            OMElement faultCountEle = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.FAULT));
            consumerKey = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.CONSUMERKEY)).getText();
            if (invocationTimeEle != null) {
                requestTime = invocationTimeEle.getText();
            }
            if (faultCountEle != null) {
                faultCount = (long) Double.parseDouble(faultCountEle.getText());
            }
        }

        public AppAPIResponseFaultCount
                (String apiName, String apiVersion, String context, long faultCount, String consumerKey) {

            this.apiName = apiName;
            this.apiVersion = apiVersion;
            this.context = context;
            this.consumerKey = consumerKey;
            if (faultCount != 0) {
                this.faultCount = faultCount;
            }
        }
    }

    private static class APIVersionUsageByUser {

        private String context;
        private String username;
        private long requestCount;
        private String apiVersion;
        private String apiName;

        @Deprecated
        public APIVersionUsageByUser(OMElement row) {
            apiName = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.API)).getText();
            context = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.CONTEXT)).getText();
            username = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.USER_ID)).getText();
            requestCount = (long) Double.parseDouble(row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.REQUEST)).getText());
            apiVersion = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.VERSION)).getText();

        }
    }

    private static class APIVersionUsageByUserMonth {

        private String context;
        private String username;
        private long requestCount;
        private String apiVersion;
        private String apiName;
        private String month;

        @Deprecated
        public APIVersionUsageByUserMonth(OMElement row) {
            apiName = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.API)).getText();
            context = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.CONTEXT)).getText();
            username = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.USER_ID)).getText();
            requestCount = (long) Double.parseDouble(row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.REQUEST)).getText());
            apiVersion = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.VERSION)).getText();
            month = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.MONTH)).getText();
        }

        public APIVersionUsageByUserMonth(String apiName, String apiVersion, String context, String username,
                long requestCount, String month) {
            this.apiName = apiName;
            this.apiVersion = apiVersion;
            this.context = context;
            this.username = username;
            this.requestCount = requestCount;
            this.month = month;
        }
    }

    private static class APIResponseTime {

        private String apiName;
        private String apiVersion;
        private String context;
        private double responseTime;
        private long responseCount;

        @Deprecated
        public APIResponseTime(OMElement row) {
            String nameVersion = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.API_VERSION)).getText();
            int index = nameVersion.lastIndexOf(":v");
            apiName = nameVersion.substring(0, index);
            apiVersion = nameVersion.substring(index + 2);
            context = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.CONTEXT)).getText();
            responseTime = Double.parseDouble(row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.SERVICE_TIME)).getText());
            responseCount = (long) Double.parseDouble(row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.RESPONSE)).getText());
        }

        public APIResponseTime
                (String apiName, String apiVersion, String context, double responseTime, long responseCount) {

            this.apiName = apiName;
            this.apiVersion = apiVersion;
            this.context = context;
            this.responseTime = responseTime;
            this.responseCount = responseCount;
        }
    }

    private static class APIAccessTime {

        private String apiName;
        private String apiVersion;
        private String context;
        private double accessTime;
        private String username;

        @Deprecated
        public APIAccessTime(OMElement row) {
            String nameVersion = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.API_VERSION)).getText();
            int index = nameVersion.lastIndexOf(":v");
            apiName = nameVersion.substring(0, index);
            apiVersion = nameVersion.substring(index + 2);
            context = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.CONTEXT)).getText();
            accessTime = Double.parseDouble(row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.REQUEST_TIME)).getText());
            username = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.USER_ID)).getText();
        }

        public APIAccessTime(String apiName, String apiVersion, String context, double accessTime, String username) {

            this.apiName = apiName;
            this.apiVersion = apiVersion;
            this.context = context;
            this.accessTime = accessTime;
            this.username = username;
        }
    }

    private static class APIFirstAccess {

        private String year;
        private String month;
        private String day;
        //private long requestCount;

        public APIFirstAccess(String year, String month, String day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }

        public String getYear() {
            return year;
        }

        public String getMonth() {
            return month;
        }

        public String getDay() {
            return day;
        }

        @Deprecated
        public APIFirstAccess(OMElement row) {
            year = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.YEAR)).getText();
            month = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.MONTH)).getText();
            day = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.DAY)).getText();
            /*requestCount = (long) Double.parseDouble(row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.REQUEST)).getText());*/
        }
    }

    public static class APIUserAgent{
        private String apiName;
        private String apiVersion;
        private String userAgent;
        private int totalRequestCount;

        @Deprecated
        public APIUserAgent(OMElement row){
            String nameVersion = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.API_VERSION)).getText();
            int index = nameVersion.lastIndexOf(":v");
            apiName = nameVersion.substring(0, index);
            apiVersion = nameVersion.substring(index + 2);
            userAgent = row.getFirstChildWithName(new QName("useragent")).getText();
            totalRequestCount =  Integer.parseInt(row.getFirstChildWithName(new QName("total_request_count")).getText());
        }

    }

    public static class APIRequestsByHour{
        private String apiName;
        private String apiVersion;
        private String requestCount;
        private String date;
        private String tier;

        @Deprecated
        public APIRequestsByHour(OMElement row){
            apiName = row.getFirstChildWithName(new QName("api")).getText();
            apiVersion = row.getFirstChildWithName(new QName("api_version")).getText();
            requestCount = row.getFirstChildWithName(new QName("total_request_count")).getText();
            date = row.getFirstChildWithName(new QName("requesttime")).getText();
            tier = row.getFirstChildWithName(new QName("tier")).getText();
        }

    }

}