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

package org.wso2.carbon.apimgt.usage.client.impl;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.usage.client.APIUsageStatisticsClient;
import org.wso2.carbon.apimgt.usage.client.APIUsageStatisticsClientConstants;
import org.wso2.carbon.apimgt.usage.client.billing.APIUsageRangeCost;
import org.wso2.carbon.apimgt.usage.client.billing.PaymentPlan;
import org.wso2.carbon.apimgt.usage.client.dto.*;
import org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException;
import org.wso2.carbon.apimgt.usage.client.internal.APIUsageClientServiceComponent;
import org.wso2.carbon.apimgt.usage.client.pojo.*;
import org.wso2.carbon.application.mgt.stub.upload.CarbonAppUploaderStub;
import org.wso2.carbon.application.mgt.stub.upload.types.carbon.UploadedFileItem;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.activation.DataHandler;
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
import java.text.ParseException;
import java.text.NumberFormat;
import java.util.*;

/**
 * Usage statistics class implementation for the APIUsageStatisticsClient.
 * Use the RDBMS to query and fetch the data for getting usage Statistics
 */
public class APIUsageStatisticsRdbmsClientImpl extends APIUsageStatisticsClient {

    private static final String API_USAGE_TRACKING = "APIUsageTracking.";
    private static final String DATA_SOURCE_NAME = "jdbc/WSO2AM_STATS_DB";
    private static volatile DataSource dataSource = null;
    private static PaymentPlan paymentPlan;
    private APIProvider apiProviderImpl;
    private APIConsumer apiConsumerImpl;
    private static final Log log = LogFactory.getLog(APIUsageStatisticsRdbmsClientImpl.class);
    private final String clientType = "RDBMS";

    /**
     * default constructor
     */
    public APIUsageStatisticsRdbmsClientImpl() {

    }

    public APIUsageStatisticsRdbmsClientImpl(String username) throws APIMgtUsageQueryServiceClientException {
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
                String filePath = (new StringBuilder()).append(CarbonUtils.getCarbonHome()).append(File.separator)
                        .append("repository").append(File.separator).append("conf").append(File.separator)
                        .append("billing-conf.xml").toString();
                element = buildOMElement(new FileInputStream(filePath));
                paymentPlan = new PaymentPlan(element);
            }
            String targetEndpoint = apiManagerAnalyticsConfiguration.getBamServerUrlGroups();
            if (targetEndpoint == null || targetEndpoint.equals("")) {
                throw new APIMgtUsageQueryServiceClientException("Required BAM server URL parameter unspecified");
            }
            apiProviderImpl = APIManagerFactory.getInstance().getAPIProvider(username);

        } catch (Exception e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while instantiating API manager core objects",
                    e);
        }

    }

    public void initializeDataSource() throws APIMgtUsageQueryServiceClientException {
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

    /**
     * This method is used to close the ResultSet, PreparedStatement and Connection after getting data from the DB
     * This is called if a "PreparedStatement" is used to fetch results from the DB
     *
     * @param resultSet         ResultSet returned from the database query
     * @param preparedStatement prepared statement used in the database query
     * @param connection        DB connection used to get data from the database
     */
    public void closeDatabaseLinks(ResultSet resultSet, PreparedStatement preparedStatement,
                                   Connection connection) {

        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                //this is logged and the process is continued because the query has executed
                log.error("Error occurred while closing the result set from JDBC database.", e);
            }
        }
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
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

    /**
     * This method is used to close the ResultSet, Statement and Connection after getting data from the DB
     * This is called if a "Statement" is used to fetch results from the DB
     *
     * @param resultSet  ResultSet returned from the database query
     * @param statement  statement used in the database query
     * @param connection DB connection used to get data from the database
     */
    public void closeDatabaseLinks(ResultSet resultSet, Statement statement,
                                   Connection connection) {

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

    @Override
    public List<FaultCountDTO> getPerAppAPIFaultCount(String subscriberName, String groupId, String fromDate,
            String toDate, int limit) throws APIMgtUsageQueryServiceClientException {

        List<String> subscriberApps = getAppsBySubscriber(subscriberName, groupId);
        String concatenatedKeySetString = "";

        int size = subscriberApps.size();
        if (size > 0) {
            concatenatedKeySetString += "'" + subscriberApps.get(0) + "'";
        } else {
            return new ArrayList<FaultCountDTO>();
        }
        for (int i = 1; i < subscriberApps.size(); i++) {
            concatenatedKeySetString += ",'" + subscriberApps.get(i) + "'";
        }

        List<FaultCountDTO> usage = getFaultAppUsageData(APIUsageStatisticsClientConstants.API_FAULT_SUMMARY,
                concatenatedKeySetString, fromDate, toDate, limit);

        return usage;
    }

    @Override
    public List<AppUsageDTO> getTopAppUsers(String subscriberName, String groupId, String fromDate, String toDate,
            int limit) throws APIMgtUsageQueryServiceClientException {

        List<String> subscriberApps = getAppsBySubscriber(subscriberName, groupId);
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

        List<AppUsageDTO> usage = getTopAppUsageData(APIUsageStatisticsClientConstants.API_REQUEST_SUMMARY,
                concatenatedKeySetString, fromDate, toDate, limit);
        return usage;
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
            int limit) throws APIMgtUsageQueryServiceClientException {

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

                if (connection != null && connection.getMetaData().getDatabaseProductName().contains("DB2")) {
                    query = "SELECT " +
                            APIUsageStatisticsClientConstants.API + "," + APIUsageStatisticsClientConstants.API_VERSION + "," +
                            APIUsageStatisticsClientConstants.VERSION + "," + APIUsageStatisticsClientConstants.API_PUBLISHER + "," +
                            APIUsageStatisticsClientConstants.CONSUMERKEY + "," + APIUsageStatisticsClientConstants.USER_ID + "," +
                            APIUsageStatisticsClientConstants.CONTEXT + "," + APIUsageStatisticsClientConstants.REQUEST_TIME + "," +
                            APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + "," + APIUsageStatisticsClientConstants.HOST_NAME + "," +
                            APIUsageStatisticsClientConstants.YEAR + "," + APIUsageStatisticsClientConstants.MONTH + "," +
                            APIUsageStatisticsClientConstants.DAY + "," + APIUsageStatisticsClientConstants.TIME +
                            ",SUM(" + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ") " +
                            "AS net_total_requests FROM " + tableName +
                            " WHERE " + APIUsageStatisticsClientConstants.CONSUMERKEY + " IN (" + keyString + ")" +
                            " AND time BETWEEN " + "'" + fromDate + "' AND \'" + toDate + "' " +
                            " GROUP BY " + APIUsageStatisticsClientConstants.API + "," + APIUsageStatisticsClientConstants.API_VERSION + "," +
                            APIUsageStatisticsClientConstants.VERSION + "," + APIUsageStatisticsClientConstants.API_PUBLISHER + "," +
                            APIUsageStatisticsClientConstants.CONSUMERKEY + "," + APIUsageStatisticsClientConstants.USER_ID + "," +
                            APIUsageStatisticsClientConstants.CONTEXT + "," + APIUsageStatisticsClientConstants.REQUEST_TIME + "," +
                            APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + "," + APIUsageStatisticsClientConstants.HOST_NAME + "," +
                            APIUsageStatisticsClientConstants.YEAR + "," + APIUsageStatisticsClientConstants.MONTH + "," +
                            APIUsageStatisticsClientConstants.DAY + "," + APIUsageStatisticsClientConstants.TIME +
                            " ORDER BY net_total_requests DESC";

                } else {
                    query = "SELECT " +
                            APIUsageStatisticsClientConstants.CONSUMERKEY + ','
                            + APIUsageStatisticsClientConstants.USER_ID + ",SUM("
                            + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ") AS net_total_requests" +
                            " FROM " + tableName +
                            " WHERE " + APIUsageStatisticsClientConstants.CONSUMERKEY + " IN (" + keyString + ")" +
                            " AND time BETWEEN " + "'" + fromDate + "' AND \'" + toDate + "' " +
                            " GROUP BY " + APIUsageStatisticsClientConstants.CONSUMERKEY + ','
                            + APIUsageStatisticsClientConstants.USER_ID
                            + " ORDER BY net_total_requests DESC";
                }

                resultSet = statement.executeQuery(query);
                AppUsageDTO appUsageDTO;
                while (resultSet.next()) {
                    String userId = resultSet.getString(APIUsageStatisticsClientConstants.USER_ID);
                    long requestCount = resultSet.getLong("net_total_requests");
                    String consumerKey = resultSet.getString(APIUsageStatisticsClientConstants.CONSUMERKEY);
                    String appName = subscriberAppsMap.get(consumerKey);

                    boolean found = false;
                    for (AppUsageDTO dto : topAppUsageDataList) {
                        if (dto.getAppName().equals(appName)) {
                            dto.addToUserCountArray(userId, requestCount);
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        appUsageDTO = new AppUsageDTO();
                        appUsageDTO.setAppName(appName);
                        appUsageDTO.addToUserCountArray(userId, requestCount);
                        topAppUsageDataList.add(appUsageDTO);
                    }

                }
            }
        } catch (SQLException e) {
            throw new APIMgtUsageQueryServiceClientException(
                    "Error occurred while querying top app usage data from JDBC database", e);
        } finally {
            closeDatabaseLinks(resultSet, statement, connection);
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
    private List<FaultCountDTO> getFaultAppUsageData(String tableName, String keyString, String fromDate, String toDate,
            int limit) throws APIMgtUsageQueryServiceClientException {

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        List<FaultCountDTO> falseAppUsageDataList = new ArrayList<FaultCountDTO>();

        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String query;

            //check whether table exist first
            if (isTableExist(tableName, connection)) {

                query = "SELECT " +
                        "consumerKey, api,SUM(" + APIUsageStatisticsClientConstants.TOTAL_FAULT_COUNT
                        + ") AS total_faults " +
                        " FROM " + tableName +
                        " WHERE " + APIUsageStatisticsClientConstants.CONSUMERKEY + " IN (" + keyString + ") " +
                        " AND time BETWEEN " + "'" + fromDate + "' AND \'" + toDate + "' " +
                        " GROUP BY " + APIUsageStatisticsClientConstants.CONSUMERKEY + ","
                        + APIUsageStatisticsClientConstants.API;

                resultSet = statement.executeQuery(query);
                FaultCountDTO faultCountDTO;
                while (resultSet.next()) {
                    String apiName = resultSet.getString(APIUsageStatisticsClientConstants.API);
                    long faultCount = resultSet.getLong("total_faults");
                    String consumerKey = resultSet.getString(APIUsageStatisticsClientConstants.CONSUMERKEY);

                    String appName = subscriberAppsMap.get(consumerKey);

                    boolean found = false;
                    for (FaultCountDTO dto : falseAppUsageDataList) {
                        if (dto.getAppName().equals(appName)) {
                            dto.addToApiFaultCountArray(apiName, faultCount);
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        faultCountDTO = new FaultCountDTO();
                        faultCountDTO.setAppName(appName);
                        faultCountDTO.addToApiFaultCountArray(apiName, faultCount);
                        falseAppUsageDataList.add(faultCountDTO);
                    }

                }
            }
        } catch (SQLException e) {
            throw new APIMgtUsageQueryServiceClientException(
                    "Error occurred while querying API faulty invocation data from JDBC database", e);
        } finally {
            closeDatabaseLinks(resultSet, statement, connection);
        }
        return falseAppUsageDataList;
    }

    @Override
    public List<AppCallTypeDTO> getAppApiCallType(String subscriberName, String groupId, String fromDate, String toDate,
            int limit) throws APIMgtUsageQueryServiceClientException {

        List<String> subscriberApps = getAppsBySubscriber(subscriberName, groupId);
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

        List<AppCallTypeDTO> usage = getAPICallTypeUsageData(
                APIUsageStatisticsClientConstants.API_Resource_Path_USAGE_SUMMARY, concatenatedKeySetString, fromDate,
                toDate, limit);
        return usage;

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
            String toDate, int limit) throws APIMgtUsageQueryServiceClientException {

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


                if (connection != null && connection.getMetaData().getDatabaseProductName().contains("DB2")) {
                    query = "SELECT " +
                            APIUsageStatisticsClientConstants.API + "," + APIUsageStatisticsClientConstants.VERSION + "," +
                            APIUsageStatisticsClientConstants.API_PUBLISHER + "," + APIUsageStatisticsClientConstants.CONSUMERKEY + "," +
                            APIUsageStatisticsClientConstants.RESOURCE + "," + APIUsageStatisticsClientConstants.CONTEXT + "," +
                            APIUsageStatisticsClientConstants.METHOD + "," + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + "," +
                            APIUsageStatisticsClientConstants.HOST_NAME + "," + APIUsageStatisticsClientConstants.YEAR + "," +
                            APIUsageStatisticsClientConstants.MONTH + "," + APIUsageStatisticsClientConstants.DAY + "," +
                            APIUsageStatisticsClientConstants.TIME + " FROM " + tableName + " WHERE " +
                            APIUsageStatisticsClientConstants.CONSUMERKEY + " IN (" + keyString + ") " +
                            " AND time BETWEEN " + "'" + fromDate + "' AND '" + toDate + "' " +
                            " GROUP BY " + APIUsageStatisticsClientConstants.API + "," + APIUsageStatisticsClientConstants.VERSION + "," +
                            APIUsageStatisticsClientConstants.API_PUBLISHER + "," + APIUsageStatisticsClientConstants.CONSUMERKEY + "," +
                            APIUsageStatisticsClientConstants.RESOURCE + "," + APIUsageStatisticsClientConstants.CONTEXT + "," +
                            APIUsageStatisticsClientConstants.METHOD + "," + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + "," +
                            APIUsageStatisticsClientConstants.HOST_NAME + "," + APIUsageStatisticsClientConstants.YEAR + "," +
                            APIUsageStatisticsClientConstants.MONTH + "," + APIUsageStatisticsClientConstants.DAY + "," +
                            APIUsageStatisticsClientConstants.TIME;
                } else {
                    query = "SELECT " +
                            APIUsageStatisticsClientConstants.API + "," +
                            APIUsageStatisticsClientConstants.METHOD + "," +
                            APIUsageStatisticsClientConstants.CONSUMERKEY + "," +
                            APIUsageStatisticsClientConstants.RESOURCE +
                            " FROM " + tableName +
                            " WHERE " +
                            APIUsageStatisticsClientConstants.CONSUMERKEY + " IN (" + keyString + ") " +
                            " AND time BETWEEN " + "'" + fromDate + "' AND '" + toDate + "' " +
                            " GROUP BY " + APIUsageStatisticsClientConstants.CONSUMERKEY + "," +
                            APIUsageStatisticsClientConstants.API + "," + APIUsageStatisticsClientConstants.METHOD + ","
                            + APIUsageStatisticsClientConstants.RESOURCE;
                }

                resultSet = statement.executeQuery(query);
                AppCallTypeDTO appCallTypeDTO;
                while (resultSet.next()) {
                    String apiName = resultSet.getString(APIUsageStatisticsClientConstants.API);
                    String callType = resultSet.getString(APIUsageStatisticsClientConstants.METHOD);
                    String consumerKey = resultSet.getString(APIUsageStatisticsClientConstants.CONSUMERKEY);
                    String resource = resultSet.getString(APIUsageStatisticsClientConstants.RESOURCE);

                    List<String> callTypeList = new ArrayList<String>();
                    callTypeList.add(resource + " (" + callType + ")");

                    String appName = subscriberAppsMap.get(consumerKey);

                    boolean found = false;
                    for (AppCallTypeDTO dto : appApiCallTypeList) {
                        if (dto.getAppName().equals(appName)) {
                            dto.addGToApiCallTypeArray(apiName, callTypeList);
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        appCallTypeDTO = new AppCallTypeDTO();
                        appCallTypeDTO.setAppName(appName);
                        appCallTypeDTO.addGToApiCallTypeArray(apiName, callTypeList);
                        appApiCallTypeList.add(appCallTypeDTO);
                    }

                }
            }
        } catch (SQLException e) {
            throw new APIMgtUsageQueryServiceClientException(
                    "Error occurred while querying API call type data from JDBC database", e);
        } finally {
            closeDatabaseLinks(resultSet, statement, connection);
        }
        return appApiCallTypeList;
    }

    @Override
    public List<PerAppApiCountDTO> perAppPerAPIUsage(String subscriberName, String groupId, String fromDate,
            String toDate, int limit) throws APIMgtUsageQueryServiceClientException {

        List<String> subscriberApps = getAppsBySubscriber(subscriberName, groupId);
        String concatenatedKeySetString = "";

        int size = subscriberApps.size();
        if (size > 0) {
            concatenatedKeySetString += "'" + subscriberApps.get(0) + "'";
        } else {
            return new ArrayList<PerAppApiCountDTO>();
        }
        for (int i = 1; i < subscriberApps.size(); i++) {
            concatenatedKeySetString += ",'" + subscriberApps.get(i) + "'";
        }

        List<PerAppApiCountDTO> usage = getPerAppAPIUsageData(APIUsageStatisticsClientConstants.API_REQUEST_SUMMARY,
                concatenatedKeySetString, fromDate, toDate, limit);
        return usage;
    }

    /**
     * This method gets the API usage data per application
     *
     * @param tableName name of the required table in the database
     * @param keyString concatenated key set of applications
     * @return a collection containing the data related to per App API usage
     * @throws APIMgtUsageQueryServiceClientException if an error occurs while querying the database
     */
    private List<PerAppApiCountDTO> getPerAppAPIUsageData(String tableName, String keyString, String fromDate,
            String toDate, int limit) throws APIMgtUsageQueryServiceClientException {

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        List<PerAppApiCountDTO> perAppUsageDataList = new ArrayList<PerAppApiCountDTO>();

        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String query;

            //check whether table exist first
            if (isTableExist(tableName, connection)) {

                if (connection != null && connection.getMetaData().getDatabaseProductName().contains("DB2")) {

                    query = "SELECT " +
                            APIUsageStatisticsClientConstants.API + "," + APIUsageStatisticsClientConstants.API_VERSION + "," +
                            APIUsageStatisticsClientConstants.VERSION + "," + APIUsageStatisticsClientConstants.API_PUBLISHER + "," +
                            APIUsageStatisticsClientConstants.CONSUMERKEY + "," + APIUsageStatisticsClientConstants.USER_ID + "," +
                            APIUsageStatisticsClientConstants.CONTEXT + "," + APIUsageStatisticsClientConstants.MAX_REQUEST_TIME + "," +
                            APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + "," + APIUsageStatisticsClientConstants.HOST_NAME + "," +
                            APIUsageStatisticsClientConstants.YEAR + "," + APIUsageStatisticsClientConstants.MONTH + "," +
                            APIUsageStatisticsClientConstants.DAY + "," + APIUsageStatisticsClientConstants.TIME + ",SUM(" +
                            APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ") AS total_calls " +
                            " FROM " + APIUsageStatisticsClientConstants.API_REQUEST_SUMMARY + " WHERE " +
                            APIUsageStatisticsClientConstants.CONSUMERKEY + " IN (" + keyString + ") " +
                            " AND time BETWEEN " + "'" + fromDate + "' AND '" + toDate + "' " +
                            " GROUP BY " +
                            APIUsageStatisticsClientConstants.API + "," + APIUsageStatisticsClientConstants.API_VERSION + "," +
                            APIUsageStatisticsClientConstants.VERSION + "," + APIUsageStatisticsClientConstants.API_PUBLISHER + "," +
                            APIUsageStatisticsClientConstants.CONSUMERKEY + "," + APIUsageStatisticsClientConstants.USER_ID + "," +
                            APIUsageStatisticsClientConstants.CONTEXT + "," + APIUsageStatisticsClientConstants.MAX_REQUEST_TIME + "," +
                            APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + "," + APIUsageStatisticsClientConstants.HOST_NAME + "," +
                            APIUsageStatisticsClientConstants.YEAR + "," + APIUsageStatisticsClientConstants.MONTH + "," +
                            APIUsageStatisticsClientConstants.DAY + "," + APIUsageStatisticsClientConstants.TIME;

                } else {
                    query = "SELECT " +
                            APIUsageStatisticsClientConstants.API + "," +
                            APIUsageStatisticsClientConstants.CONSUMERKEY + "," +
                            " SUM(" + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ") AS total_calls " +
                            " FROM " + APIUsageStatisticsClientConstants.API_REQUEST_SUMMARY +
                            " WHERE " +
                            APIUsageStatisticsClientConstants.CONSUMERKEY + " IN (" + keyString + ") " +
                            " AND time BETWEEN " + "'" + fromDate + "' AND '" + toDate + "' " +
                            " GROUP BY " +
                            APIUsageStatisticsClientConstants.API + "," + APIUsageStatisticsClientConstants.CONSUMERKEY;
                }

                resultSet = statement.executeQuery(query);
                PerAppApiCountDTO apiUsageDTO;

                while (resultSet.next()) {
                    String apiName = resultSet.getString(APIUsageStatisticsClientConstants.API);
                    long requestCount = resultSet.getLong("total_calls");
                    String consumerKey = resultSet.getString(APIUsageStatisticsClientConstants.CONSUMERKEY);

                    String appName = subscriberAppsMap.get(consumerKey);

                    boolean found = false;
                    for (PerAppApiCountDTO dto : perAppUsageDataList) {
                        if (dto.getAppName().equals(appName)) {
                            dto.addToApiCountArray(apiName, requestCount);
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        apiUsageDTO = new PerAppApiCountDTO();
                        apiUsageDTO.setAppName(appName);
                        apiUsageDTO.addToApiCountArray(apiName, requestCount);
                        perAppUsageDataList.add(apiUsageDTO);
                    }

                }
            }
        } catch (SQLException e) {
            throw new APIMgtUsageQueryServiceClientException(
                    "Error occurred while querying per App usage data from JDBC database", e);
        } finally {
            closeDatabaseLinks(resultSet, statement, connection);
        }
        return perAppUsageDataList;
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
    @Override
    public List<APIUsageDTO> getProviderAPIUsage(String providerName, String fromDate, String toDate, int limit)
            throws APIMgtUsageQueryServiceClientException {

        Collection<APIUsage> usageData = getAPIUsageData(APIUsageStatisticsClientConstants.API_VERSION_USAGE_SUMMARY,
                fromDate, toDate);
        List<API> providerAPIs = getAPIsByProvider(providerName);
        Map<String, APIUsageDTO> usageByAPIs = new TreeMap<String, APIUsageDTO>();
        for (APIUsage usage : usageData) {
            for (API providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(usage.getApiName()) &&
                        providerAPI.getId().getVersion().equals(usage.getApiVersion()) &&
                        providerAPI.getContext().equals(usage.getContext())) {
                    String[] apiData = { usage.getApiName(), usage.getApiVersion(),
                            providerAPI.getId().getProviderName() };

                    JSONArray jsonArray = new JSONArray();
                    jsonArray.add(0, apiData[0]);
                    jsonArray.add(1, apiData[1]);
                    jsonArray.add(2, apiData[2]);
                    String apiName = jsonArray.toJSONString();

                    APIUsageDTO usageDTO = usageByAPIs.get(apiName);
                    if (usageDTO != null) {
                        usageDTO.setCount(usageDTO.getCount() + usage.getRequestCount());
                    } else {
                        usageDTO = new APIUsageDTO();
                        usageDTO.setApiName(apiName);
                        usageDTO.setCount(usage.getRequestCount());
                        usageByAPIs.put(apiName, usageDTO);
                    }
                }
            }
        }

        List<APIUsageDTO> usage = getAPIUsageTopEntries(new ArrayList<APIUsageDTO>(usageByAPIs.values()), limit);
        return usage;
    }

    /**
     * This method gets the usage data for a given API across all versions
     *
     * @param tableName name of the table in the database
     * @return a collection containing the API usage data
     * @throws APIMgtUsageQueryServiceClientException if an error occurs while querying the database
     */
    private Collection<APIUsage> getAPIUsageData(String tableName, String fromDate, String toDate)
            throws APIMgtUsageQueryServiceClientException {

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

                if (connection != null && connection.getMetaData().getDatabaseProductName().contains("DB2")) {
                    query = "SELECT " +
                            APIUsageStatisticsClientConstants.API + "," +
                            APIUsageStatisticsClientConstants.CONTEXT + "," +
                            APIUsageStatisticsClientConstants.VERSION + "," +
                            "SUM(" + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ") AS aggregateSum " +
                            " FROM " + tableName + " GROUP BY " + APIUsageStatisticsClientConstants.API + "," +
                            APIUsageStatisticsClientConstants.CONTEXT + "," + APIUsageStatisticsClientConstants.VERSION;
                } else {
                    query = "SELECT " +
                            APIUsageStatisticsClientConstants.API + "," +
                            APIUsageStatisticsClientConstants.CONTEXT + "," +
                            APIUsageStatisticsClientConstants.VERSION + "," +
                            "SUM(" + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ") AS aggregateSum " +
                            " FROM " + tableName +
                            " WHERE " + APIUsageStatisticsClientConstants.TIME + " BETWEEN " +
                            "\'" + fromDate + "\' AND \'" + toDate + "\'" +
                            " GROUP BY " + APIUsageStatisticsClientConstants.API + "," +
                            APIUsageStatisticsClientConstants.CONTEXT + "," + APIUsageStatisticsClientConstants.VERSION;
                }


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
            throw new APIMgtUsageQueryServiceClientException(
                    "Error occurred while querying API usage data from JDBC database", e);
        } finally {
            closeDatabaseLinks(resultSet, statement, connection);
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
                if (providerAPI.getId().getApiName().equals(usage.getApiName()) &&
                        providerAPI.getId().getVersion().equals(usage.getApiVersion()) &&
                        providerAPI.getContext().equals(usage.getContext())) {

                    APIVersionUsageDTO usageDTO = new APIVersionUsageDTO();
                    usageDTO.setVersion(usage.getApiVersion());
                    usageDTO.setCount(usage.getRequestCount());
                    usageByVersions.put(usage.getApiVersion(), usageDTO);
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
    public List<APIVersionUsageDTO> getUsageByAPIVersions(String providerName, String apiName, String fromDate,
            String toDate) throws APIMgtUsageQueryServiceClientException {

        List<APIUsage> usageData = this
                .queryBetweenTwoDaysForAPIUsageByVersion(APIUsageStatisticsClientConstants.API_VERSION_USAGE_SUMMARY,
                        fromDate, toDate, apiName);
        //        Collection<APIUsage> usageData = getUsageData(omElement);
        List<API> providerAPIs = getAPIsByProvider(providerName);
        Map<String, APIVersionUsageDTO> usageByVersions = new TreeMap<String, APIVersionUsageDTO>();

        for (APIUsage usage : usageData) {
            for (API providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(usage.getApiName()) &&
                        providerAPI.getId().getVersion().equals(usage.getApiVersion()) &&
                        providerAPI.getContext().equals(usage.getContext())) {

                    APIVersionUsageDTO usageDTO = new APIVersionUsageDTO();
                    usageDTO.setVersion(usage.getApiVersion());
                    usageDTO.setCount(usage.getRequestCount());
                    usageByVersions.put(usage.getApiVersion(), usageDTO);
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
    @Override
    public List<APIResourcePathUsageDTO> getAPIUsageByResourcePath(String providerName, String fromDate, String toDate)
            throws APIMgtUsageQueryServiceClientException {

        Collection<APIUsageByResourcePath> usageData = this
                .queryToGetAPIUsageByResourcePath(APIUsageStatisticsClientConstants.API_Resource_Path_USAGE_SUMMARY,
                        fromDate, toDate);
        List<API> providerAPIs = getAPIsByProvider(providerName);
        List<APIResourcePathUsageDTO> usageByResourcePath = new ArrayList<APIResourcePathUsageDTO>();

        for (APIUsageByResourcePath usage : usageData) {
            for (API providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(usage.getApiName()) &&
                        providerAPI.getId().getVersion().equals(usage.getApiVersion()) &&
                        providerAPI.getContext().equals(usage.getContext())) {

                    APIResourcePathUsageDTO usageDTO = new APIResourcePathUsageDTO();
                    usageDTO.setApiName(usage.getApiName());
                    usageDTO.setVersion(usage.getApiVersion());
                    usageDTO.setMethod(usage.getMethod());
                    usageDTO.setContext(usage.getContext());
                    usageDTO.setCount(usage.getRequestCount());
                    usageDTO.setTime(usage.getTime());
                    usageByResourcePath.add(usageDTO);
                }
            }
        }
        List<APIResourcePathUsageDTO> usage = usageByResourcePath;
        return usage;
    }

    @Override
    public List<APIDestinationUsageDTO> getAPIUsageByDestination(String providerName, String fromDate, String toDate)
            throws APIMgtUsageQueryServiceClientException {

        List<APIUsageByDestination> usageData = this
                .queryToGetAPIUsageByDestination(APIUsageStatisticsClientConstants.API_USAGEBY_DESTINATION_SUMMARY,
                        fromDate, toDate);

        List<API> providerAPIs = getAPIsByProvider(providerName);
        List<APIDestinationUsageDTO> usageByResourcePath = new ArrayList<APIDestinationUsageDTO>();

        for (APIUsageByDestination usage : usageData) {
            for (API providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(usage.getApiName()) &&
                        providerAPI.getId().getVersion().equals(usage.getApiVersion()) &&
                        providerAPI.getContext().equals(usage.getContext())) {

                    APIDestinationUsageDTO usageDTO = new APIDestinationUsageDTO();
                    usageDTO.setApiName(usage.getApiName());
                    usageDTO.setVersion(usage.getApiVersion());
                    usageDTO.setDestination(usage.getDestination());
                    usageDTO.setContext(usage.getContext());
                    usageDTO.setCount(usage.getRequestCount());
                    usageByResourcePath.add(usageDTO);
                }
            }
        }
        List<APIDestinationUsageDTO> usage = usageByResourcePath;
        return usage;
    }

    /**
     * Returns a list of APIUsageByUserDTO objects that contain information related to
     * User wise API Usage, along with the number of invocations, and API Version
     *
     * @param providerName Name of the API provider
     * @return a List of APIUsageByUserDTO objects, possibly empty
     * @throws org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException on error
     */
    @Override
    public List<APIUsageByUserDTO> getAPIUsageByUser(String providerName, String fromDate, String toDate)
            throws APIMgtUsageQueryServiceClientException {

        List<APIUsageByUserName> usageData = this
                .queryBetweenTwoDaysForAPIUsageByUser(providerName, fromDate, toDate, null);

        String tenantDomain = MultitenantUtils.getTenantDomain(providerName);

        List<APIUsageByUserDTO> usageByName = new ArrayList<APIUsageByUserDTO>();

        for (APIUsageByUserName usage : usageData) {
            if (tenantDomain.equals(MultitenantUtils.getTenantDomain(usage.getApipublisher()))) {
                APIUsageByUserDTO usageDTO = new APIUsageByUserDTO();
                usageDTO.setApiName(usage.getApiName());
                usageDTO.setVersion(usage.getApiVersion());
                usageDTO.setUserID(usage.getUserID());
                usageDTO.setCount(usage.getRequestCount());
                usageByName.add(usageDTO);
            }
        }

        List<APIUsageByUserDTO> usage = usageByName;
        //        return gson.toJson(usage);
        return usage;
    }

    /**
     * Gets a list of APIResponseTimeDTO objects containing information related to APIs belonging
     * to a particular provider along with their average response times.
     *
     * @param providerName Name of the API provider
     * @return a List of APIResponseTimeDTO objects, possibly empty
     * @throws org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException on error
     */
    @Override
    public List<APIResponseTimeDTO> getProviderAPIServiceTime(String providerName, String fromDate, String toDate,
            int limit) throws APIMgtUsageQueryServiceClientException {

        Collection<APIResponseTime> responseTimes = getAPIResponseTimeData(
                APIUsageStatisticsClientConstants.API_VERSION_SERVICE_TIME_SUMMARY);
        List<API> providerAPIs = getAPIsByProvider(providerName);
        DecimalFormat format = new DecimalFormat("#.##");
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
        List<APIResponseTimeDTO> apiResponseTimeUsage = new ArrayList<APIResponseTimeDTO>();

        Map<String, Double> apiCumulativeServiceTimeMap = new HashMap<String, Double>();
        Map<String, Long> apiUsageMap = new TreeMap<String, Long>();
        for (APIResponseTime responseTime : responseTimes) {
            for (API providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(responseTime.getApiName()) &&
                        providerAPI.getId().getVersion().equals(responseTime.getApiVersion()) &&
                        providerAPI.getContext().equals(responseTime.getContext())) {

                    /*String apiName = responseTime.getApiName() + " (" + providerAPI.getId().getProviderName() + ")";
                    Double cumulativeResponseTime = apiCumulativeServiceTimeMap.get(apiName);

                    if (cumulativeResponseTime != null) {
                        apiCumulativeServiceTimeMap.put(apiName,
                                cumulativeResponseTime + responseTime.getResponseTime() * responseTime
                                        .getResponseCount());
                        apiUsageMap.put(apiName, apiUsageMap.get(apiName) + responseTime.getResponseCount());
                    } else {
                        apiCumulativeServiceTimeMap
                                .put(apiName, responseTime.getResponseTime() * responseTime.getResponseCount());
                        apiUsageMap.put(apiName, responseTime.getResponseCount());
                    }*/
                    APIResponseTimeDTO responseTimeDTO = new APIResponseTimeDTO();
                    responseTimeDTO.setApiName(responseTime.getApiName());

                    //calculate the average response time
                    double avgTime = responseTime.getResponseTime() / responseTime.getResponseCount();
                    //format the time
                    try {
                        responseTimeDTO.setServiceTime(numberFormat.parse(format.format(avgTime)).doubleValue());
                    } catch (ParseException e) {
                        throw new APIMgtUsageQueryServiceClientException("Parse exception while formatting time");
                    }
                    apiResponseTimeUsage.add(responseTimeDTO);
                }
            }
        }

        /*Map<String, APIResponseTimeDTO> responseTimeByAPI = new TreeMap<String, APIResponseTimeDTO>();
        DecimalFormat format = new DecimalFormat("#.##");
        for (String key : apiUsageMap.keySet()) {
            APIResponseTimeDTO responseTimeDTO = new APIResponseTimeDTO();
            responseTimeDTO.setApiName(key);
            double responseTime = apiCumulativeServiceTimeMap.get(key) / apiUsageMap.get(key);
            responseTimeDTO.setServiceTime(Double.parseDouble(format.format(responseTime)));
            responseTimeByAPI.put(key, responseTimeDTO);
        }*/
        List<APIResponseTimeDTO> usage = getResponseTimeTopEntries(apiResponseTimeUsage, limit);
        return usage;
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
            String query;

            if (connection != null && connection.getMetaData().getDatabaseProductName().contains("DB2")) {
                query = "SELECT TempTable.*, " +
                        "SUM(" + APIUsageStatisticsClientConstants.TOTAL_RESPONSE_COUNT + ") AS totalTime ," +
                        "SUM(weighted_service_time) AS totalWeightTime " +
                        " FROM (SELECT " +
                        APIUsageStatisticsClientConstants.API_VERSION + "," + APIUsageStatisticsClientConstants.API_PUBLISHER + "," +
                        APIUsageStatisticsClientConstants.CONTEXT + "," + APIUsageStatisticsClientConstants.SERVICE_TIME + "," +
                        APIUsageStatisticsClientConstants.TOTAL_RESPONSE_COUNT + "," + APIUsageStatisticsClientConstants.HOST_NAME + "," +
                        APIUsageStatisticsClientConstants.YEAR + "," + APIUsageStatisticsClientConstants.MONTH + "," +
                        APIUsageStatisticsClientConstants.DAY + "," + APIUsageStatisticsClientConstants.TIME +
                        ", (" + APIUsageStatisticsClientConstants.SERVICE_TIME + " * " +
                        APIUsageStatisticsClientConstants.TOTAL_RESPONSE_COUNT + ") AS weighted_service_time " +
                        " FROM " +
                        APIUsageStatisticsClientConstants.API_VERSION_SERVICE_TIME_SUMMARY + ") " + "TempTable " +
                        " GROUP BY " + APIUsageStatisticsClientConstants.API_VERSION + "," + APIUsageStatisticsClientConstants.API_PUBLISHER + "," +
                        APIUsageStatisticsClientConstants.CONTEXT + "," + APIUsageStatisticsClientConstants.SERVICE_TIME + "," +
                        APIUsageStatisticsClientConstants.TOTAL_RESPONSE_COUNT + "," + APIUsageStatisticsClientConstants.HOST_NAME + "," +
                        APIUsageStatisticsClientConstants.YEAR + "," + APIUsageStatisticsClientConstants.MONTH + "," +
                        APIUsageStatisticsClientConstants.DAY + "," + APIUsageStatisticsClientConstants.TIME + ", weighted_service_time";
            } else {

                query = "select " + APIUsageStatisticsClientConstants.API_VERSION + ','
                        + APIUsageStatisticsClientConstants.CONTEXT + ',' + "SUM("
                        + APIUsageStatisticsClientConstants.TOTAL_RESPONSE_COUNT + ") AS totalTime,SUM("
                        + APIUsageStatisticsClientConstants.SERVICE_TIME + " * "
                        + APIUsageStatisticsClientConstants.TOTAL_RESPONSE_COUNT + ") AS totalWeightTime" +
                        " from " + tableName + " GROUP BY " + APIUsageStatisticsClientConstants.CONTEXT + ','
                        + APIUsageStatisticsClientConstants.API_VERSION;

            }

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
            throw new APIMgtUsageQueryServiceClientException(
                    "Error occurred while querying API response times from JDBC database", e);
        } finally {
            closeDatabaseLinks(resultSet, statement, connection);
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
    @Override
    public List<APIVersionLastAccessTimeDTO> getProviderAPIVersionUserLastAccess(String providerName, String fromDate,
            String toDate, int limit) throws APIMgtUsageQueryServiceClientException {

        Collection<APIAccessTime> accessTimes = getLastAccessData(
                APIUsageStatisticsClientConstants.API_VERSION_KEY_LAST_ACCESS_SUMMARY, providerName);

        if (providerName.startsWith(APIUsageStatisticsClientConstants.ALL_PROVIDERS)) {
            providerName = APIUsageStatisticsClientConstants.ALL_PROVIDERS;
        }
        List<API> providerAPIs = getAPIsByProvider(providerName);

        List<APIVersionLastAccessTimeDTO> accessTimeByAPI = new ArrayList<APIVersionLastAccessTimeDTO>();
        APIVersionLastAccessTimeDTO accessTimeDTO;
        DateFormat dateFormat = new SimpleDateFormat();

        for (APIAccessTime accessTime : accessTimes) {
            for (API providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(accessTime.getApiName()) &&
                        providerAPI.getId().getVersion().equals(accessTime.getApiVersion()) &&
                        providerAPI.getContext().equals(accessTime.getContext())) {

                    accessTimeDTO = new APIVersionLastAccessTimeDTO();
                    String apiName = accessTime.getApiName() + " (" + providerAPI.getId().getProviderName() + ")";
                    accessTimeDTO.setApiName(apiName);
                    accessTimeDTO.setApiVersion(accessTime.getApiVersion());
                    accessTimeDTO.setLastAccessTime(dateFormat.format(accessTime.getAccessTime()));
                    accessTimeDTO.setUser(accessTime.getUsername());
                    accessTimeByAPI.add(accessTimeDTO);
                }
            }
        }
        return getLastAccessTimeTopEntries(accessTimeByAPI, limit);

    }

    /**
     * This method gets the last access times for APIs
     *
     * @param tableName name of the required table in the database
     * @return a collection containing the data related to API last access times
     * @throws APIMgtUsageQueryServiceClientException if an error occurs while querying the database
     */
    private Collection<APIAccessTime> getLastAccessData(String tableName, String providerName)
            throws APIMgtUsageQueryServiceClientException {

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        Collection<APIAccessTime> lastAccessTimeData = new ArrayList<APIAccessTime>();

        String tenantDomain = MultitenantUtils.getTenantDomain(providerName);
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();

            StringBuilder lastAccessQuery = new StringBuilder(
                    "SELECT " + APIUsageStatisticsClientConstants.API + "," + APIUsageStatisticsClientConstants.VERSION
                            + "," + APIUsageStatisticsClientConstants.CONTEXT + ","
                            + APIUsageStatisticsClientConstants.USER_ID + ","
                            + APIUsageStatisticsClientConstants.REQUEST_TIME + " FROM "
                            + APIUsageStatisticsClientConstants.API_LAST_ACCESS_TIME_SUMMARY);

            lastAccessQuery.append(" where tenantDomain= \'" + tenantDomain + "\'");

            if (!providerName.startsWith(APIUsageStatisticsClientConstants.ALL_PROVIDERS)) {
                lastAccessQuery
                        .append(" AND (" + APIUsageStatisticsClientConstants.API_PUBLISHER_THROTTLE_TABLE + "= \'"
                                + providerName + "\' OR "
                                + APIUsageStatisticsClientConstants.API_PUBLISHER_THROTTLE_TABLE + "= \'" + APIUtil
                                .getUserNameWithTenantSuffix(providerName) + "\')");
            }

            lastAccessQuery.append(" order by " + APIUsageStatisticsClientConstants.REQUEST_TIME + " DESC");

            resultSet = statement.executeQuery(lastAccessQuery.toString());

            while (resultSet.next()) {
                String apiName = resultSet.getString(APIUsageStatisticsClientConstants.API);
                String version = resultSet.getString(APIUsageStatisticsClientConstants.VERSION);
                String context = resultSet.getString(APIUsageStatisticsClientConstants.CONTEXT);
                long accessTime = resultSet.getLong(APIUsageStatisticsClientConstants.REQUEST_TIME);
                String username = resultSet.getString(APIUsageStatisticsClientConstants.USER_ID);
                lastAccessTimeData.add(new APIAccessTime(apiName, version, context, accessTime, username));
            }

        } catch (SQLException e) {
            throw new APIMgtUsageQueryServiceClientException(
                    "Error occurred while querying last access data for APIs from JDBC database", e);
        } finally {
            closeDatabaseLinks(resultSet, statement, connection);
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
    @Override
    public List<PerUserAPIUsageDTO> getUsageBySubscribers(String providerName, String apiName, int limit)
            throws APIMgtUsageQueryServiceClientException {

        Collection<APIUsageByUser> usageData = getUsageOfAPI(apiName, null);
        Map<String, PerUserAPIUsageDTO> usageByUsername = new TreeMap<String, PerUserAPIUsageDTO>();
        List<API> apiList = getAPIsByProvider(providerName);
        for (APIUsageByUser usageEntry : usageData) {
            for (API api : apiList) {
                if (api.getContext().equals(usageEntry.getContext()) && api.getId().getApiName().equals(apiName)) {
                    PerUserAPIUsageDTO usageDTO = usageByUsername.get(usageEntry.getUsername());
                    if (usageDTO != null) {
                        usageDTO.setCount(usageDTO.getCount() + usageEntry.getRequestCount());
                    } else {
                        usageDTO = new PerUserAPIUsageDTO();
                        usageDTO.setUsername(usageEntry.getUsername());
                        usageDTO.setCount(usageEntry.getRequestCount());
                        usageByUsername.put(usageEntry.getUsername(), usageDTO);
                    }
                    break;
                }
            }
        }

        return getTopEntries(new ArrayList<PerUserAPIUsageDTO>(usageByUsername.values()), limit);
    }

    public List<APIRequestsByUserAgentsDTO> getUserAgentSummaryForALLAPIs()
            throws APIMgtUsageQueryServiceClientException {

        OMElement omElement = this.buildOMElementFromDatabaseTable("API_USERAGENT_SUMMARY");
        Collection<APIUserAgent> userAgentData = getUserAgent(omElement);
        Map<String, APIRequestsByUserAgentsDTO> apiRequestByUserAgents = new TreeMap<String, APIRequestsByUserAgentsDTO>();
        APIRequestsByUserAgentsDTO userAgentsDTO = null;
        for (APIUserAgent usageEntry : userAgentData) {
            if (!apiRequestByUserAgents.containsKey(usageEntry.userAgent)) {
                userAgentsDTO = new APIRequestsByUserAgentsDTO();
                userAgentsDTO.setUserAgent(usageEntry.userAgent);
                userAgentsDTO.setCount(usageEntry.totalRequestCount);
                apiRequestByUserAgents.put(usageEntry.userAgent, userAgentsDTO);
            } else {
                userAgentsDTO = new APIRequestsByUserAgentsDTO();
                userAgentsDTO = (APIRequestsByUserAgentsDTO) apiRequestByUserAgents.get(usageEntry.userAgent);
                userAgentsDTO.setCount(userAgentsDTO.getCount() + usageEntry.totalRequestCount);
                apiRequestByUserAgents.remove(usageEntry.userAgent);
                apiRequestByUserAgents.put(usageEntry.userAgent, userAgentsDTO);
            }
        }
        return new ArrayList<APIRequestsByUserAgentsDTO>(apiRequestByUserAgents.values());
    }

    public List<APIRequestsByHourDTO> getAPIRequestsByHour(String fromDate, String toDate, String apiName)
            throws APIMgtUsageQueryServiceClientException {
        String Date = null;
        OMElement omElement = this
                .queryBetweenTwoDaysForAPIRequestsByHour("API_REQUESTS_PERHOUR", fromDate, toDate, apiName);
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
            apiRequestsByHour.put(usageEntry.date.concat(usageEntry.tier), apiRequestsByHourDTO);
        }
        return new ArrayList<APIRequestsByHourDTO>(apiRequestsByHour.values());
    }

    public List<String> getAPIsFromAPIRequestsPerHourTable(String fromDate, String toDate)
            throws APIMgtUsageQueryServiceClientException {
        String Date = null;
        OMElement omElement = this
                .queryBetweenTwoDaysForAPIsFromAPIRequestsPerHourTable("API_REQUESTS_PERHOUR", fromDate, toDate);
        Collection<String> apisList = getAPIsFromAPIRequestByHour(omElement);

        return new ArrayList<String>(apisList);
    }

    @Override
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
                if (providerAPI.getId().getApiName().equals(fault.getApiName()) &&
                        providerAPI.getId().getVersion().equals(fault.getApiVersion()) &&
                        providerAPI.getContext().equals(fault.getContext())) {

                    APIResponseFaultCountDTO faultyDTO = new APIResponseFaultCountDTO();
                    faultyDTO.setApiName(fault.getApiName());
                    faultyDTO.setVersion(fault.getApiVersion());
                    faultyDTO.setContext(fault.getContext());
                    faultyDTO.setCount(fault.getFaultCount());

                    apiVersionUsageList = getUsageByAPIVersions(providerName, fault.getApiName(), fromDate, toDate);
                    for (int i = 0; i < apiVersionUsageList.size(); i++) {
                        apiVersionUsageDTO = apiVersionUsageList.get(i);
                        if (apiVersionUsageDTO.getVersion().equals(fault.getApiVersion())) {
                            long requestCount = apiVersionUsageDTO.getCount();
                            double faultPercentage =
                                    ((double) requestCount - fault.getFaultCount()) / requestCount * 100;
                            DecimalFormat twoDForm = new DecimalFormat("#.##");

                            NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
                            try {
                                faultPercentage = 100 - numberFormat.parse(twoDForm.format(faultPercentage)).doubleValue();
                            } catch (ParseException e) {
                                throw new APIMgtUsageQueryServiceClientException("Parse exception while formatting time");
                            }
                            faultyDTO.setFaultPercentage(faultPercentage);
                            faultyDTO.setTotalRequestCount(requestCount);
                            break;
                        }
                    }

                    faultyCount.add(faultyDTO);

                }
            }
        }
        return faultyCount;
    }

    @Override
    public List<PerUserAPIUsageDTO> getUsageBySubscribers(String providerName, String apiName, String apiVersion,
            int limit) throws APIMgtUsageQueryServiceClientException {

        Collection<APIUsageByUser> usageData = getUsageOfAPI(apiName, apiVersion);
        Map<String, PerUserAPIUsageDTO> usageByUsername = new TreeMap<String, PerUserAPIUsageDTO>();
        List<API> apiList = getAPIsByProvider(providerName);
        for (APIUsageByUser usageEntry : usageData) {
            for (API api : apiList) {
                if (api.getContext().equals(usageEntry.getContext()) &&
                        api.getId().getApiName().equals(apiName) &&
                        api.getId().getVersion().equals(apiVersion) &&
                        apiVersion.equals(usageEntry.getApiVersion())) {
                    PerUserAPIUsageDTO usageDTO = usageByUsername.get(usageEntry.getUsername());
                    if (usageDTO != null) {
                        usageDTO.setCount(usageDTO.getCount() + usageEntry.getRequestCount());
                    } else {
                        usageDTO = new PerUserAPIUsageDTO();
                        usageDTO.setUsername(usageEntry.getUsername());
                        usageDTO.setCount(usageEntry.getRequestCount());
                        usageByUsername.put(usageEntry.getUsername(), usageDTO);
                    }
                    break;
                }
            }
        }

        return getTopEntries(new ArrayList<PerUserAPIUsageDTO>(usageByUsername.values()), limit);
    }

    public List<APIVersionUserUsageDTO> getUsageBySubscriber(String subscriberName, String period)
            throws Exception, APIManagementException {

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

                for (APIUsageRangeCost rangeCost : rangeCosts) {
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

    private List<APIResponseTimeDTO> getResponseTimeTopEntries(List<APIResponseTimeDTO> usageData, int limit) {
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

    private List<APIVersionLastAccessTimeDTO> getLastAccessTimeTopEntries(List<APIVersionLastAccessTimeDTO> usageData,
            int limit) {
        Collections.sort(usageData, new Comparator<APIVersionLastAccessTimeDTO>() {
            public int compare(APIVersionLastAccessTimeDTO o1, APIVersionLastAccessTimeDTO o2) {
                // Note that o2 appears before o1
                // This is because we need to sort in the descending order
                return (int) (o2.getLastAccessTime().compareToIgnoreCase(o1.getLastAccessTime()));
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
     * @param tableName - database table
     * @return OMElement
     * @throws APIMgtUsageQueryServiceClientException Fetches the data from the passed table and builds a OEMElemnet
     * @deprecated please do not use this function as this may cause memory overflow. This loads a whole database table into memory as XML object
     */
    @Deprecated
    private OMElement buildOMElementFromDatabaseTable(String tableName) throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure "
                    + "that the data source is properly configured in the APIUsageTracker configuration.");
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
            closeDatabaseLinks(rs, statement, connection);
        }
    }

    /**
     * @param columnFamily name of the table
     * @param fromDate     starting date of the duration for the query
     * @param toDate       last date of the duration for the query
     * @return XML object containing the specified table
     * @throws APIMgtUsageQueryServiceClientException if an error occurs while retrieving data
     * @deprecated please do not use this function as this may cause memory overflow.
     * This loads a whole database table into memory as XML object
     */
    @Deprecated
    private OMElement queryBetweenTwoDays(String columnFamily, String fromDate, String toDate)
            throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure "
                    + "that the data source is properly configured in the APIUsageTracker configuration.");
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
                query = "SELECT * FROM  " + columnFamily + " WHERE " + APIUsageStatisticsClientConstants.TIME
                        + " BETWEEN " +
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
            closeDatabaseLinks(rs, statement, connection);
        }
    }

    private OMElement queryBetweenTwoDaysForAPIRequestsByHour(String columnFamily, String fromDate, String toDate,
            String apiName) throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure "
                    + "that the data source is properly configured in the APIUsageTracker configuration.");
        }

        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String query;
            //TODO: API_FAULT_COUNT need to populate according to match withQuery given time range

            query = "SELECT * FROM  " + columnFamily + " WHERE " + " API =\'" + apiName + "\' AND " + " requestTime "
                    + " BETWEEN " +
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
            closeDatabaseLinks(rs, statement, connection);
        }
    }

    @Deprecated
    private OMElement queryBetweenTwoDaysForAPIsFromAPIRequestsPerHourTable(String columnFamily, String fromDate,
            String toDate) throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure "
                    + "that the data source is properly configured in the APIUsageTracker configuration.");
        }

        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String query;
            //TODO: API_FAULT_COUNT need to populate according to match with given time range

            query = "SELECT DISTINCT API FROM  " + columnFamily + " WHERE TIER<>\'Unauthenticated\' AND"
                    + " requestTime " + " BETWEEN " +
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
            closeDatabaseLinks(rs, statement, connection);
        }
    }

    private List<APIResponseFaultCount> queryBetweenTwoDaysForFaulty(String tableName, String fromDate, String toDate)
            throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure "
                    + "that the data source is properly configured in the APIUsageTracker configuration.");
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
            closeDatabaseLinks(rs, statement, connection);
        }
    }

    private List<APIUsageByResourcePath> queryToGetAPIUsageByResourcePath(String tableName, String fromDate,
            String toDate) throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure "
                    + "that the data source is properly configured in the APIUsageTracker configuration.");
        }

        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        List<APIUsageByResourcePath> usage = new ArrayList<APIUsageByResourcePath>();
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String query;

            query = "SELECT api,version,apiPublisher,context,method,total_request_count,time FROM " + tableName
                    + " WHERE " + APIUsageStatisticsClientConstants.TIME + " BETWEEN " +
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
            closeDatabaseLinks(rs, statement, connection);
        }
    }

    private List<APIUsageByDestination> queryToGetAPIUsageByDestination(String tableName, String fromDate,
            String toDate) throws APIMgtUsageQueryServiceClientException {
        if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure "
                    + "that the data source is properly configured in the APIUsageTracker configuration.");
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
                    "\'" + fromDate + "\' AND \'" + toDate + "\'"
                    + " GROUP BY api,version,apiPublisher,context,destination";

            rs = statement.executeQuery(query);
            APIUsageByDestination apiUsageByDestination;

            while (rs.next()) {
                String apiName = rs.getString("api");
                String version = rs.getString("version");
                String context = rs.getString("context");
                String destination = rs.getString("destination");
                long requestCount = rs.getLong("total_request_count");
                apiUsageByDestination = new APIUsageByDestination(apiName, version, context, destination, requestCount);
                usageByResourcePath.add(apiUsageByDestination);
            }
            return usageByResourcePath;

        } catch (Exception e) {
            throw new APIMgtUsageQueryServiceClientException("Error occurred while querying from JDBC database", e);
        } finally {
            closeDatabaseLinks(rs, statement, connection);
        }
    }

    private List<APIUsage> queryBetweenTwoDaysForAPIUsageByVersion(String tableName, String fromDate, String toDate,
            String apiName) throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure "
                    + "that the data source is properly configured in the APIUsageTracker configuration.");
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
            closeDatabaseLinks(rs, statement, connection);
        }
    }

    private List<APIUsageByUserName> queryBetweenTwoDaysForAPIUsageByUser(String providerName, String fromDate,
            String toDate, Integer limit) throws APIMgtUsageQueryServiceClientException {
        if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure "
                    + "that the data source is properly configured in the APIUsageTracker configuration.");
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
                query = "SELECT API, API_VERSION,VERSION, APIPUBLISHER, USERID, SUM(TOTAL_REQUEST_COUNT) AS TOTAL_REQUEST_COUNT, CONTEXT "
                        +
                        "FROM API_REQUEST_SUMMARY" + " WHERE " + APIUsageStatisticsClientConstants.TIME + " BETWEEN " +
                        "\'" + fromDate + "\' AND \'" + toDate + "\'" +
                        " GROUP BY API, API_VERSION, USERID, VERSION, APIPUBLISHER, CONTEXT ORDER BY TOTAL_REQUEST_COUNT DESC ";

                oracleQuery =
                        "SELECT API, API_VERSION, VERSION, APIPUBLISHER, USERID, SUM(TOTAL_REQUEST_COUNT) AS TOTAL_REQUEST_COUNT, CONTEXT "
                                +
                                "FROM API_REQUEST_SUMMARY" + " WHERE " + APIUsageStatisticsClientConstants.TIME
                                + " BETWEEN " +
                                "\'" + fromDate + "\' AND \'" + toDate + "\'" +
                                " GROUP BY API, API_VERSION, VERSION, USERID, APIPUBLISHER, CONTEXT ORDER BY TOTAL_REQUEST_COUNT DESC";

                mssqlQuery =
                        "SELECT API, API_VERSION, VERSION, APIPUBLISHER, USERID, SUM(TOTAL_REQUEST_COUNT) AS TOTAL_REQUEST_COUNT, CONTEXT "
                                +
                                "FROM API_REQUEST_SUMMARY" + " WHERE " + APIUsageStatisticsClientConstants.TIME
                                + " BETWEEN " +
                                "\'" + fromDate + "\' AND \'" + toDate + "\'" +
                                " GROUP BY API, API_VERSION, USERID, VERSION, APIPUBLISHER, CONTEXT ORDER BY TOTAL_REQUEST_COUNT DESC";
            } else {
                query = "SELECT API, API_VERSION, VERSION, APIPUBLISHER, USERID, SUM(TOTAL_REQUEST_COUNT) AS TOTAL_REQUEST_COUNT, CONTEXT "
                        +
                        "FROM API_REQUEST_SUMMARY" +
                        " GROUP BY API, API_VERSION, APIPUBLISHER, USERID ORDER BY TOTAL_REQUEST_COUNT DESC ";

                oracleQuery =
                        "SELECT API, API_VERSION, VERSION, APIPUBLISHER, USERID, SUM(TOTAL_REQUEST_COUNT) AS TOTAL_REQUEST_COUNT, CONTEXT "
                                +
                                "FROM API_REQUEST_SUMMARY" +
                                " GROUP BY API, API_VERSION, VERSION, APIPUBLISHER, USERID, CONTEXT ORDER BY TOTAL_REQUEST_COUNT DESC ";

                mssqlQuery =
                        "SELECT  API, API_VERSION, VERSION, APIPUBLISHER, USERID, SUM(TOTAL_REQUEST_COUNT) AS TOTAL_REQUEST_COUNT, CONTEXT "
                                +
                                "FROM API_REQUEST_SUMMARY" +
                                " GROUP BY API, API_VERSION, APIPUBLISHER, USERID ORDER BY TOTAL_REQUEST_COUNT DESC ";

            }
            if ((connection.getMetaData().getDriverName()).contains("Oracle")) {
                query = oracleQuery;
            }
            if (connection.getMetaData().getDatabaseProductName().contains("Microsoft")) {
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
                    APIUsageByUserName usage = new APIUsageByUserName(apiName, apiVersion, context, userID, publisher,
                            requestCount);
                    usageByName.add(usage);
                }
            }
            return usageByName;

        } catch (Exception e) {
            throw new APIMgtUsageQueryServiceClientException("Error occurred while querying from JDBC database", e);
        } finally {
            closeDatabaseLinks(rs, statement, connection);
        }
    }

    public boolean isTableExist(String tableName, Connection connection) throws SQLException {
        final String checkTableSQLQuery = "SELECT DISTINCT 1 FROM " + tableName;
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            rs = statement.executeQuery(checkTableSQLQuery);
            return true;
        } catch (SQLException e) {
            // SQL error related to table not exist is db specific
            // error is logged and continues.
            log.error("Error occurred while checking existence of the table:" + tableName, e);
            return false;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    // this is logged and the process is continued because the
                    // query has executed
                    log.error("Error occurred while closing the result set from JDBC database.", e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    // this is logged and the process is continued because the
                    // query has executed
                    log.error("Error occurred while closing the prepared statement from JDBC database.", e);
                }
            }
            // connection object will not be closed as it should be handled by
            // the parent method.
        }
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
    private Collection<APIUserAgent> getUserAgent(OMElement data) {
        List<APIUserAgent> userAgentData = new ArrayList<APIUserAgent>();
        OMElement rowsElement = data.getFirstChildWithName(new QName(APIUsageStatisticsClientConstants.ROWS));
        Iterator rowIterator = rowsElement.getChildrenWithName(new QName(APIUsageStatisticsClientConstants.ROW));
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
        OMElement rowsElement = data.getFirstChildWithName(new QName(APIUsageStatisticsClientConstants.ROWS));
        Iterator rowIterator = rowsElement.getChildrenWithName(new QName(APIUsageStatisticsClientConstants.ROW));
        if (rowIterator != null) {
            while (rowIterator.hasNext()) {
                OMElement rowElement = (OMElement) rowIterator.next();
                if (!rowElement.getFirstChildWithName(new QName("tier")).getText()
                        .equalsIgnoreCase("Unauthenticated")) {
                    apiRequestsByHours.add(new APIRequestsByHour(rowElement));
                }

            }
        }
        return apiRequestsByHours;
    }

    @Deprecated
    private Collection<String> getAPIsFromAPIRequestByHour(OMElement data) {
        List<String> apisList = new ArrayList<String>();
        OMElement rowsElement = data.getFirstChildWithName(new QName(APIUsageStatisticsClientConstants.ROWS));
        Iterator rowIterator = rowsElement.getChildrenWithName(new QName(APIUsageStatisticsClientConstants.ROW));
        if (rowIterator != null) {
            while (rowIterator.hasNext()) {
                OMElement rowElement = (OMElement) rowIterator.next();
                apisList.add(new String(rowElement.getFirstChildWithName(new QName("api")).getText()));

            }
        }
        return apisList;
    }

    private Collection<APIVersionUsageByUserMonth> getUsageAPIBySubscriberMonthly(String subscriberName, String period)
            throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure "
                    + "that the data source is properly configured in the APIUsageTracker configuration.");
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
                        + ",sum(" + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ") as "
                        + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ","
                        + APIUsageStatisticsClientConstants.MONTH + "," + APIUsageStatisticsClientConstants.USER_ID
                        + " FROM  " + APIUsageStatisticsClientConstants.KEY_USAGE_MONTH_SUMMARY + " WHERE "
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
                    long requestCount = rs.getLong(APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT);
                    String month = rs.getString(APIUsageStatisticsClientConstants.MONTH);
                    usageData.add(new APIVersionUsageByUserMonth(apiName, apiVersion, context, username, requestCount,
                            month));
                }
            }

            return usageData;

        } catch (SQLException e) {
            throw new APIMgtUsageQueryServiceClientException("Error occurred while querying from JDBC database", e);
        } finally {
            closeDatabaseLinks(rs, statement, connection);
        }
    }

    public List<APIUsageRangeCost> evaluate(String param, int calls) throws Exception {
        return paymentPlan.evaluate(param, calls);
    }

    /**
     * Custom artifacts deployment. deploy capp related to RDBMS on DAS
     *
     * @param url  url of the DAS
     * @param user user name
     * @param pass password
     * @throws Exception general exception throws, because different exception can occur
     */
    @Override
    public void deployArtifacts(String url, String user, String pass) throws Exception {

        if (url.trim().equals("")) {
            String message = "Data Analyzer URL is empty. cApp will not be deployed.";
            log.warn(message);
            return;
        }

        //name of the capp to deploy
        String cAppName = "API_Manager_Analytics_RDBMS.car";
        String cAppPath = System.getProperty("carbon.home") + File.separator + "statistics";
        cAppPath = cAppPath + File.separator + cAppName;
        File file = new File(cAppPath);

        //get the byte array of file
        byte[] byteArray = FileUtils.readFileToByteArray(file);
        DataHandler dataHandler = new DataHandler(byteArray,
                APIUsageStatisticsClientConstants.APPLICATION_OCTET_STREAM);

        //create the stub to deploy artifacts
        CarbonAppUploaderStub stub = new CarbonAppUploaderStub(url + "/services/CarbonAppUploader");
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        //set the security
        HttpTransportProperties.Authenticator authenticator = new HttpTransportProperties.Authenticator();
        authenticator.setUsername(user);
        authenticator.setPassword(pass);
        authenticator.setPreemptiveAuthentication(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, authenticator);
        client.setOptions(options);
        log.info("Deploying DAS cApp '" + cAppName + "'...");
        //create UploadedFileItem array and 1st element contain the deploy artifact
        UploadedFileItem[] fileItem = new UploadedFileItem[1];
        fileItem[0] = new UploadedFileItem();
        fileItem[0].setDataHandler(dataHandler);
        fileItem[0].setFileName(cAppName);
        fileItem[0].setFileType("jar");
        //upload the artifacts
        stub.uploadApp(fileItem);
    }

    @Override
    public List<APIFirstAccess> getFirstAccessTime(String providerName) throws APIMgtUsageQueryServiceClientException {
        return getFirstAccessTime(providerName, 1);
    }

    public List<APIFirstAccess> getFirstAccessTime(String providerName, int limit)
            throws APIMgtUsageQueryServiceClientException {

        APIFirstAccess firstAccess = this.queryFirstAccess(APIUsageStatisticsClientConstants.KEY_USAGE_SUMMARY);
        List<APIFirstAccess> APIFirstAccessList = new ArrayList<APIFirstAccess>();

        APIFirstAccess fTime;

        if (firstAccess != null) {
            fTime = new APIFirstAccess(firstAccess.getYear(), firstAccess.getMonth(), firstAccess.getDay());
            APIFirstAccessList.add(fTime);
        }
        return APIFirstAccessList;
    }

    private APIFirstAccess queryFirstAccess(String columnFamily) throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure "
                    + "that the data source is properly configured in the APIUsageTracker configuration.");
        }

        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String query;
            if (connection != null && connection.getMetaData().getDatabaseProductName().equalsIgnoreCase("oracle")) {

                query = "SELECT time,year,month,day FROM (SELECT time,year,month,day FROM " + columnFamily
                        + " order by time ASC) where ROWNUM <= 1";

            } else if (connection != null && connection.getMetaData().getDatabaseProductName().contains("Microsoft")) {

                query = "SELECT TOP 1 time,year,month,day FROM  " + columnFamily + " order by time ASC";

            } else if (connection != null && connection.getMetaData().getDatabaseProductName().contains("DB2")) {

                query = "SELECT time,year,month,day FROM  " + columnFamily + " order by time ASC FETCH FIRST 1 ROWS ONLY";

            } else {

                query = "SELECT time,year,month,day FROM  " + columnFamily + " order by time ASC limit 1";

            }
            rs = statement.executeQuery(query);
            String year;
            String month;
            String day;
            APIFirstAccess firstAccess = null;

            while (rs.next()) {
                year = rs.getInt("year") + "";
                month = rs.getInt("month") - 1 + "";
                day = rs.getInt("day") + "";
                firstAccess = new APIFirstAccess(year, month, day);
            }

            return firstAccess;

        } catch (Exception e) {
            throw new APIMgtUsageQueryServiceClientException(
                    "Error occurred while querying from JDBC database" + e.getMessage(), e);
        } finally {
            closeDatabaseLinks(rs, statement, connection);
        }
    }

    private Collection<APIUsageByUser> getUsageOfAPI(String apiName, String apiVersion)
            throws APIMgtUsageQueryServiceClientException {
        if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure "
                    + "that the data source is properly configured in the APIUsageTracker configuration.");
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
                query = "SELECT * FROM " + APIUsageStatisticsClientConstants.KEY_USAGE_SUMMARY + " WHERE "
                        + APIUsageStatisticsClientConstants.API + " = '" + apiName + "'";
                if (apiVersion != null) {
                    query += " AND " + APIUsageStatisticsClientConstants.VERSION + " = '" + apiVersion + "'";
                }
                rs = statement.executeQuery(query);
                while (rs.next()) {
                    String context = rs.getString(APIUsageStatisticsClientConstants.CONTEXT);
                    String username = rs.getString(APIUsageStatisticsClientConstants.USER_ID);
                    long requestCount = rs.getLong(APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT);
                    String version = rs.getString(APIUsageStatisticsClientConstants.VERSION);
                    usageData.add(new APIUsageByUser(context, username, requestCount, version));
                }
            }
            return usageData;

        } catch (SQLException e) {
            throw new APIMgtUsageQueryServiceClientException("Error occurred while querying from JDBC database", e);
        } finally {
            closeDatabaseLinks(rs,statement,connection);
        }
    }

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
    public List<APIThrottlingOverTimeDTO> getThrottleDataOfAPIAndApplication(String apiName, String provider,
            String appName, String fromDate, String toDate, String groupBy)
            throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure "
                    + "that the data source is properly configured in the APIUsageTracker configuration.");
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            String query, groupByStmt;
            List<APIThrottlingOverTimeDTO> throttlingData = new ArrayList<APIThrottlingOverTimeDTO>();
            String tenantDomain = MultitenantUtils.getTenantDomain(provider);

            //check whether table exist first
            if (isTableExist(APIUsageStatisticsClientConstants.API_THROTTLED_OUT_SUMMARY, connection)) { //Table exists

                groupByStmt = "year, month, day";

                /*if (APIUsageStatisticsClientConstants.GROUP_BY_DAY.equals(groupBy)) {
                    groupByStmt = "year, month, day";
                } else if (APIUsageStatisticsClientConstants.GROUP_BY_HOUR.equals(groupBy)) {
                    groupByStmt = "year, month, day, time";
                } else {
                    throw new APIMgtUsageQueryServiceClientException("Unsupported group by parameter " + groupBy +
                            " for retrieving throttle data of API and app.");
                }*/

                query = "SELECT " + groupByStmt + " ," +
                        "SUM(COALESCE(success_request_count,0)) AS success_request_count, " +
                        "SUM(COALESCE(throttleout_count,0)) AS throttleout_count " +
                        "FROM API_THROTTLED_OUT_SUMMARY " +
                        "WHERE tenantDomain = ? " +
                        "AND api = ? " +
                        (provider.startsWith(APIUsageStatisticsClientConstants.ALL_PROVIDERS) ?
                                "" :
                                "AND apiPublisher = ?") +
                        (StringUtils.isEmpty(appName) ? "" : " AND applicationName = ?") +
                        "AND time BETWEEN ? AND ? " +
                        "GROUP BY " + groupByStmt + " " +
                        "ORDER BY " + groupByStmt + " ASC";

                preparedStatement = connection.prepareStatement(query);
                int index = 1;
                preparedStatement.setString(index++, tenantDomain);
                preparedStatement.setString(index++, apiName);
                if (!provider.startsWith(APIUsageStatisticsClientConstants.ALL_PROVIDERS)) {
                    provider = APIUtil.getUserNameWithTenantSuffix(provider);
                    preparedStatement.setString(index++, provider);
                }
                if (!StringUtils.isEmpty(appName)) {
                    preparedStatement.setString(index++, appName);
                }
                preparedStatement.setString(index++, fromDate);
                preparedStatement.setString(index, toDate);

                rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    int successRequestCount = rs.getInt(APIUsageStatisticsClientConstants.SUCCESS_REQUEST_COUNT);
                    int throttledOutCount = rs.getInt(APIUsageStatisticsClientConstants.THROTTLED_OUT_COUNT);
                    int year = rs.getInt(APIUsageStatisticsClientConstants.YEAR);
                    int month = rs.getInt(APIUsageStatisticsClientConstants.MONTH);
                    String time;
                    if (APIUsageStatisticsClientConstants.GROUP_BY_HOUR.equals(groupBy)) {
                        time = rs.getString(APIUsageStatisticsClientConstants.TIME);
                    } else {
                        int day = rs.getInt(APIUsageStatisticsClientConstants.DAY);
                        time = year + "-" + month + "-" + day + " 00:00:00";
                    }
                    throttlingData
                            .add(new APIThrottlingOverTimeDTO(apiName, provider, successRequestCount, throttledOutCount,
                                    time));
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
            closeDatabaseLinks(rs, preparedStatement, connection);
        }
    }

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
    public List<APIThrottlingOverTimeDTO> getThrottleDataOfApplication(String appName, String provider, String fromDate,
            String toDate) throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure "
                    + "that the data source is properly configured in the APIUsageTracker configuration.");
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
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
                        (provider.startsWith(APIUsageStatisticsClientConstants.ALL_PROVIDERS) ?
                                "" :
                                "AND apiPublisher = ?") +
                        "AND time BETWEEN ? AND ? " +
                        "GROUP BY api, apiPublisher " +
                        "ORDER BY api ASC";

                preparedStatement = connection.prepareStatement(query);
                int index = 1;
                preparedStatement.setString(index++, tenantDomain);
                preparedStatement.setString(index++, appName);
                if (!provider.startsWith(APIUsageStatisticsClientConstants.ALL_PROVIDERS)) {
                    provider = APIUtil.getUserNameWithTenantSuffix(provider);
                    preparedStatement.setString(index++, provider);
                }
                preparedStatement.setString(index++, fromDate);
                preparedStatement.setString(index, toDate);

                rs = preparedStatement.executeQuery();
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
            closeDatabaseLinks(rs, preparedStatement, connection);
        }
    }

    /**
     * Get APIs of the provider that consist of throttle data
     *
     * @param provider Provider name
     * @return List of APIs of the provider that consist of throttle data
     * @throws APIMgtUsageQueryServiceClientException
     */
    public List<String> getAPIsForThrottleStats(String provider) throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure "
                    + "that the data source is properly configured in the APIUsageTracker configuration.");
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
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
                        (provider.startsWith(APIUsageStatisticsClientConstants.ALL_PROVIDERS) ?
                                "" :
                                "AND apiPublisher = ? ") +
                        "ORDER BY api ASC";

                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, tenantDomain);
                if (!provider.startsWith(APIUsageStatisticsClientConstants.ALL_PROVIDERS)) {
                    provider = APIUtil.getUserNameWithTenantSuffix(provider);
                    preparedStatement.setString(2, provider);
                }

                rs = preparedStatement.executeQuery();
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
            closeDatabaseLinks(rs, preparedStatement, connection);
        }
    }

    /**
     * Given provider name and the API name, returns a list of applications through which the corresponding API is
     * invoked and which consist of success/throttled requests
     *
     * @param provider Provider name
     * @param apiName  Name of th API
     * @return A list of applications through which the corresponding API is invoked and which consist of throttle data
     * @throws APIMgtUsageQueryServiceClientException
     */
    public List<String> getAppsForThrottleStats(String provider, String apiName)
            throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure "
                    + "that the data source is properly configured in the APIUsageTracker configuration.");
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
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
                        (provider.startsWith(APIUsageStatisticsClientConstants.ALL_PROVIDERS) ?
                                "" :
                                "AND apiPublisher = ? ") +
                        (apiName == null ? "" : "AND api = ? ") +
                        "ORDER BY applicationName ASC";

                preparedStatement = connection.prepareStatement(query);
                int index = 1;
                preparedStatement.setString(index++, tenantDomain);
                if (!provider.startsWith(APIUsageStatisticsClientConstants.ALL_PROVIDERS)) {
                    provider = APIUtil.getUserNameWithTenantSuffix(provider);
                    preparedStatement.setString(index++, provider);
                }
                if (apiName != null) {
                    preparedStatement.setString(index, apiName);
                }

                rs = preparedStatement.executeQuery();
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
            closeDatabaseLinks(rs, preparedStatement, connection);
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
            apiName = row.getFirstChildWithName(new QName(APIUsageStatisticsClientConstants.API)).getText();
            context = row.getFirstChildWithName(new QName(APIUsageStatisticsClientConstants.CONTEXT)).getText();
            username = row.getFirstChildWithName(new QName(APIUsageStatisticsClientConstants.USER_ID)).getText();
            requestCount = (long) Double.parseDouble(
                    row.getFirstChildWithName(new QName(APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT))
                            .getText());
            apiVersion = row.getFirstChildWithName(new QName(APIUsageStatisticsClientConstants.VERSION)).getText();
            month = row.getFirstChildWithName(new QName(APIUsageStatisticsClientConstants.MONTH)).getText();
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

    public static class APIUserAgent {
        private String apiName;
        private String apiVersion;
        private String userAgent;
        private int totalRequestCount;

        @Deprecated
        public APIUserAgent(OMElement row) {
            String nameVersion = row.getFirstChildWithName(new QName(APIUsageStatisticsClientConstants.API_VERSION))
                    .getText();
            int index = nameVersion.lastIndexOf(":v");
            apiName = nameVersion.substring(0, index);
            apiVersion = nameVersion.substring(index + 2);
            userAgent = row.getFirstChildWithName(new QName("useragent")).getText();
            totalRequestCount = Integer.parseInt(row.getFirstChildWithName(new QName("total_request_count")).getText());
        }

    }

    public static class APIRequestsByHour {
        private String apiName;
        private String apiVersion;
        private String requestCount;
        private String date;
        private String tier;

        @Deprecated
        public APIRequestsByHour(OMElement row) {
            apiName = row.getFirstChildWithName(new QName("api")).getText();
            apiVersion = row.getFirstChildWithName(new QName("api_version")).getText();
            requestCount = row.getFirstChildWithName(new QName("total_request_count")).getText();
            date = row.getFirstChildWithName(new QName("requesttime")).getText();
            tier = row.getFirstChildWithName(new QName("tier")).getText();
        }

    }

    /**
     * return a string to indicate type of statistics client
     *
     * @return String
     */
    @Override
    public String getClientType() {
        return clientType;
    }
}