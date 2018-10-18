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
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.usage.client.APIUsageStatisticsClient;
import org.wso2.carbon.apimgt.usage.client.APIUsageStatisticsClientConstants;
import org.wso2.carbon.apimgt.usage.client.bean.ExecutionTimeOfAPIValues;
import org.wso2.carbon.apimgt.usage.client.bean.PerGeoLocationUsageCount;
import org.wso2.carbon.apimgt.usage.client.bean.Result;
import org.wso2.carbon.apimgt.usage.client.bean.UserAgentUsageCount;
import org.wso2.carbon.apimgt.usage.client.billing.APIUsageRangeCost;
import org.wso2.carbon.apimgt.usage.client.billing.PaymentPlan;
import org.wso2.carbon.apimgt.usage.client.dto.APIDestinationUsageDTO;
import org.wso2.carbon.apimgt.usage.client.dto.APIResourcePathUsageDTO;
import org.wso2.carbon.apimgt.usage.client.dto.APIResponseFaultCountDTO;
import org.wso2.carbon.apimgt.usage.client.dto.APIResponseTimeDTO;
import org.wso2.carbon.apimgt.usage.client.dto.APIThrottlingOverTimeDTO;
import org.wso2.carbon.apimgt.usage.client.dto.APIUsageByUserDTO;
import org.wso2.carbon.apimgt.usage.client.dto.APIUsageDTO;
import org.wso2.carbon.apimgt.usage.client.dto.APIVersionLastAccessTimeDTO;
import org.wso2.carbon.apimgt.usage.client.dto.APIVersionUsageDTO;
import org.wso2.carbon.apimgt.usage.client.dto.ApiTopUsersDTO;
import org.wso2.carbon.apimgt.usage.client.dto.ApiTopUsersListDTO;
import org.wso2.carbon.apimgt.usage.client.dto.AppCallTypeDTO;
import org.wso2.carbon.apimgt.usage.client.dto.AppUsageDTO;
import org.wso2.carbon.apimgt.usage.client.dto.FaultCountDTO;
import org.wso2.carbon.apimgt.usage.client.dto.PerAppApiCountDTO;
import org.wso2.carbon.apimgt.usage.client.dto.PerUserAPIUsageDTO;
import org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException;
import org.wso2.carbon.apimgt.usage.client.internal.APIUsageClientServiceComponent;
import org.wso2.carbon.apimgt.usage.client.pojo.APIAccessTime;
import org.wso2.carbon.apimgt.usage.client.pojo.APIFirstAccess;
import org.wso2.carbon.apimgt.usage.client.pojo.APIResponseFaultCount;
import org.wso2.carbon.apimgt.usage.client.pojo.APIResponseTime;
import org.wso2.carbon.apimgt.usage.client.pojo.APIUsage;
import org.wso2.carbon.apimgt.usage.client.pojo.APIUsageByDestination;
import org.wso2.carbon.apimgt.usage.client.pojo.APIUsageByResourcePath;
import org.wso2.carbon.apimgt.usage.client.pojo.APIUsageByUser;
import org.wso2.carbon.apimgt.usage.client.pojo.APIUsageByUserName;
import org.wso2.carbon.apimgt.usage.client.util.APIUsageClientUtil;
import org.wso2.carbon.apimgt.usage.client.util.RestClientUtil;
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
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Usage statistics class implementation for the APIUsageStatisticsClient.
 * Use the RDBMS to query and fetch the data for getting usage Statistics
 */
public class APIUsageStatisticsRdbmsClientImpl extends APIUsageStatisticsClient {

    private static final String DATA_SOURCE_NAME = "jdbc/WSO2AM_STATS_DB";
    private static volatile DataSource dataSource = null;
    private static PaymentPlan paymentPlan;
    private APIProvider apiProviderImpl;
    private static final Log log = LogFactory.getLog(APIUsageStatisticsRdbmsClientImpl.class);
    private static final  Object lock = new Object();

    /**
     * default constructor
     */
    public APIUsageStatisticsRdbmsClientImpl() {
    }

    /**
     * Initialize the RDBMS client with logged user
     * @param username logged user ID
     * @throws APIMgtUsageQueryServiceClientException throws when error occured
     */
    public APIUsageStatisticsRdbmsClientImpl(String username) throws APIMgtUsageQueryServiceClientException {
        OMElement element;
        APIManagerConfiguration config;
        APIManagerAnalyticsConfiguration apiManagerAnalyticsConfiguration;
        try {
            config = APIUsageClientServiceComponent.getAPIManagerConfiguration();
            apiManagerAnalyticsConfiguration = APIManagerAnalyticsConfiguration.getInstance();
            if (APIUtil.isAnalyticsEnabled() && dataSource == null) {
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
            String targetEndpoint = apiManagerAnalyticsConfiguration.getDasReceiverUrlGroups();
            if (targetEndpoint == null || targetEndpoint.equals("")) {
                handleException("Required BAM server URL parameter unspecified");
            }
            apiProviderImpl = APIManagerFactory.getInstance().getAPIProvider(username);

        } catch (Exception e) {
            handleException("Exception while instantiating API manager core objects", e);
        }
    }

    /**
     * This method Initialised the datasource
     * @throws APIMgtUsageQueryServiceClientException throws if error occurred
     */
    @Override
    public void initializeDataSource() throws APIMgtUsageQueryServiceClientException {
        try {
            synchronized (lock) {
                if(dataSource == null){
                    Context ctx = new InitialContext();
                    dataSource = (DataSource) ctx.lookup(DATA_SOURCE_NAME);
                }
            }
        } catch (NamingException e) {
            handleException("Error while looking up the data source: " + DATA_SOURCE_NAME, e);
        }
    }

    /**
     * This method read XML content from the given stream
     * @param inputStream Stream to read XML
     * @return XML represented by OMElement
     * @throws Exception throws generic exception
     */
    public static OMElement buildOMElement(InputStream inputStream) throws Exception {
        XMLStreamReader parser;
        try {
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            parser = xmlInputFactory.createXMLStreamReader(inputStream);
        } catch (XMLStreamException e) {
            String msg = "Error in initializing the parser to build the OMElement.";
            log.error(msg, e);
            throw new Exception(msg, e);
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
    public void closeDatabaseLinks(ResultSet resultSet, PreparedStatement preparedStatement, Connection connection) {

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

    /**
     * This methods return the api invocation fault count data per applications
     *
     * @param subscriberName subscriber name
     * @param groupId        group id of the subscriber
     * @param fromDate       starting date
     * @param toDate         ending data
     * @param limit          limit of the result
     * @return list of fault count data
     * @throws APIMgtUsageQueryServiceClientException throws when error occurred
     */
    @Override
    public List<FaultCountDTO> getPerAppAPIFaultCount(String subscriberName, String groupId, String fromDate,
            String toDate, int limit) throws APIMgtUsageQueryServiceClientException {

        List<String> subscriberApps = getAppsBySubscriber(subscriberName, groupId);
        StringBuilder concatenatedKeySetString = new StringBuilder();

        int size = subscriberApps.size();
        if (size > 0) {
            concatenatedKeySetString.append("'").append(subscriberApps.get(0)).append("'");
        } else {
            return Collections.emptyList();
        }
        for (int i = 1; i < subscriberApps.size(); i++) {
            concatenatedKeySetString.append(",'").append(subscriberApps.get(i)).append("'");
        }
        return getFaultAppUsageData(APIUsageStatisticsClientConstants.API_FAULT_SUMMARY,
                concatenatedKeySetString.toString(), fromDate, toDate, limit);
    }

    /**
     * this method return the top users for the list of applications.
     *
     * @param subscriberName subscriber name
     * @param groupId        group id of the subscriber
     * @param fromDate       starting date
     * @param toDate         ending data
     * @param limit          limit of the result
     * @return list of AppUsageDTO
     * @throws APIMgtUsageQueryServiceClientException
     */
    @Override
    public List<AppUsageDTO> getTopAppUsers(String subscriberName, String groupId, String fromDate, String toDate,
            int limit) throws APIMgtUsageQueryServiceClientException {

        List<String> subscriberApps = getAppsBySubscriber(subscriberName, groupId);
        StringBuilder concatenatedKeys = new StringBuilder();
        int size = subscriberApps.size();
        if (size > 0) {
            concatenatedKeys.append("'").append(subscriberApps.get(0)).append("'");
        } else {
            return Collections.emptyList();
        }
        for (int i = 1; i < subscriberApps.size(); i++) {
            concatenatedKeys.append(",'").append(subscriberApps.get(i)).append("'");
        }
        return getTopAppUsageData(APIUsageStatisticsClientConstants.API_REQUEST_SUMMARY, concatenatedKeys.toString(),
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
            int limit) throws APIMgtUsageQueryServiceClientException {
        //ignoring sql injection for keyString since it construct locally and no public access
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<AppUsageDTO> topAppUsageDataList = new ArrayList<AppUsageDTO>();
        try {
            connection = dataSource.getConnection();
            String dbProductName = connection.getMetaData().getDatabaseProductName();
            String query;
            //check whether table exist first
            if (isTableExist(tableName, connection)) {
                // no limit enforced
                if (limit < 0) {
                    if (dbProductName.contains("DB2")) {
                        query = "SELECT " + APIUsageStatisticsClientConstants.API + ","
                                + APIUsageStatisticsClientConstants.API_VERSION + ","
                                + APIUsageStatisticsClientConstants.VERSION + ","
                                + APIUsageStatisticsClientConstants.API_PUBLISHER + ","
                                + APIUsageStatisticsClientConstants.CONSUMERKEY + ","
                                + APIUsageStatisticsClientConstants.USER_ID + ","
                                + APIUsageStatisticsClientConstants.CONTEXT + ","
                                + APIUsageStatisticsClientConstants.REQUEST_TIME + ","
                                + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ","
                                + APIUsageStatisticsClientConstants.HOST_NAME + ","
                                + APIUsageStatisticsClientConstants.YEAR + "," + APIUsageStatisticsClientConstants.MONTH
                                + "," + APIUsageStatisticsClientConstants.DAY + ","
                                + APIUsageStatisticsClientConstants.TIME + ",SUM("
                                + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ") "
                                + "AS net_total_requests FROM " + tableName + " WHERE "
                                + APIUsageStatisticsClientConstants.CONSUMERKEY + " IN ( " + keyString + " )"
                                + " AND time BETWEEN  ? AND ? " + " GROUP BY " + APIUsageStatisticsClientConstants.API
                                + "," + APIUsageStatisticsClientConstants.API_VERSION + ","
                                + APIUsageStatisticsClientConstants.VERSION + ","
                                + APIUsageStatisticsClientConstants.API_PUBLISHER + ","
                                + APIUsageStatisticsClientConstants.CONSUMERKEY + ","
                                + APIUsageStatisticsClientConstants.USER_ID + ","
                                + APIUsageStatisticsClientConstants.CONTEXT + ","
                                + APIUsageStatisticsClientConstants.REQUEST_TIME + ","
                                + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ","
                                + APIUsageStatisticsClientConstants.HOST_NAME + ","
                                + APIUsageStatisticsClientConstants.YEAR + "," + APIUsageStatisticsClientConstants.MONTH
                                + "," + APIUsageStatisticsClientConstants.DAY + ","
                                + APIUsageStatisticsClientConstants.TIME + " ORDER BY net_total_requests DESC";
                    } else {
                        query = "SELECT " + APIUsageStatisticsClientConstants.CONSUMERKEY + ','
                                + APIUsageStatisticsClientConstants.USER_ID + ",SUM("
                                + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ") AS net_total_requests"
                                + " FROM " + tableName + " WHERE " + APIUsageStatisticsClientConstants.CONSUMERKEY
                                + " IN ( " + keyString + " )" + " AND " + APIUsageStatisticsClientConstants.TIME
                                + " BETWEEN ? AND ? " + " GROUP BY " + APIUsageStatisticsClientConstants.CONSUMERKEY
                                + ',' + APIUsageStatisticsClientConstants.USER_ID + " ORDER BY net_total_requests DESC";
                    }

                    statement = connection.prepareStatement(query);
                    int index = 1;
                    statement.setString(index++, fromDate);
                    statement.setString(index, toDate);
                } else {
                    if (dbProductName.contains("DB2")) {
                        query = "SELECT " + APIUsageStatisticsClientConstants.API + ","
                                + APIUsageStatisticsClientConstants.API_VERSION + ","
                                + APIUsageStatisticsClientConstants.VERSION + ","
                                + APIUsageStatisticsClientConstants.API_PUBLISHER + ","
                                + APIUsageStatisticsClientConstants.CONSUMERKEY + ","
                                + APIUsageStatisticsClientConstants.USER_ID + ","
                                + APIUsageStatisticsClientConstants.CONTEXT + ","
                                + APIUsageStatisticsClientConstants.REQUEST_TIME + ","
                                + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ","
                                + APIUsageStatisticsClientConstants.HOST_NAME + ","
                                + APIUsageStatisticsClientConstants.YEAR + "," + APIUsageStatisticsClientConstants.MONTH
                                + "," + APIUsageStatisticsClientConstants.DAY + ","
                                + APIUsageStatisticsClientConstants.TIME + ",SUM("
                                + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ") "
                                + "AS net_total_requests FROM " + tableName + " WHERE "
                                + APIUsageStatisticsClientConstants.CONSUMERKEY + " IN ( " + keyString + " )"
                                + " AND time BETWEEN  ? AND ? " + " GROUP BY " + APIUsageStatisticsClientConstants.API
                                + "," + APIUsageStatisticsClientConstants.API_VERSION + ","
                                + APIUsageStatisticsClientConstants.VERSION + ","
                                + APIUsageStatisticsClientConstants.API_PUBLISHER + ","
                                + APIUsageStatisticsClientConstants.CONSUMERKEY + ","
                                + APIUsageStatisticsClientConstants.USER_ID + ","
                                + APIUsageStatisticsClientConstants.CONTEXT + ","
                                + APIUsageStatisticsClientConstants.REQUEST_TIME + ","
                                + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ","
                                + APIUsageStatisticsClientConstants.HOST_NAME + ","
                                + APIUsageStatisticsClientConstants.YEAR + "," + APIUsageStatisticsClientConstants.MONTH
                                + "," + APIUsageStatisticsClientConstants.DAY + ","
                                + APIUsageStatisticsClientConstants.TIME + " ORDER BY net_total_requests DESC "
                                + "FETCH FIRST ? ROWS ONLY";
                    } else if (dbProductName.contains("Microsoft") || dbProductName.contains("MS SQL")) {

                        query = "SET ROWCOUNT ? SELECT " + APIUsageStatisticsClientConstants.CONSUMERKEY + ','
                                + APIUsageStatisticsClientConstants.USER_ID + ",SUM("
                                + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ") AS net_total_requests"
                                + " FROM " + tableName + " WHERE " + APIUsageStatisticsClientConstants.CONSUMERKEY
                                + " IN ( " + keyString + " )" + " AND " + APIUsageStatisticsClientConstants.TIME
                                + " BETWEEN ? AND ? " + " GROUP BY " + APIUsageStatisticsClientConstants.CONSUMERKEY
                                + ',' + APIUsageStatisticsClientConstants.USER_ID + " ORDER BY net_total_requests DESC";
                    } else if (dbProductName.contains("Oracle")) {

                        query = "SELECT " + APIUsageStatisticsClientConstants.CONSUMERKEY + ','
                                + APIUsageStatisticsClientConstants.USER_ID + ",SUM("
                                + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ") AS net_total_requests"
                                + " FROM " + tableName + " WHERE " + APIUsageStatisticsClientConstants.CONSUMERKEY
                                + " IN ( " + keyString + " )" + " AND " + APIUsageStatisticsClientConstants.TIME
                                + " BETWEEN ? AND ? " + " AND ROWNUM <= ?" + " GROUP BY "
                                + APIUsageStatisticsClientConstants.CONSUMERKEY + ','
                                + APIUsageStatisticsClientConstants.USER_ID + " ORDER BY net_total_requests DESC";
                    } else {
                        // MYSql, H2, Postgres
                        query = "SELECT " + APIUsageStatisticsClientConstants.CONSUMERKEY + ','
                                + APIUsageStatisticsClientConstants.USER_ID + ",SUM("
                                + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ") AS net_total_requests"
                                + " FROM " + tableName + " WHERE " + APIUsageStatisticsClientConstants.CONSUMERKEY
                                + " IN ( " + keyString + " )" + " AND " + APIUsageStatisticsClientConstants.TIME
                                + " BETWEEN ? AND ? " + " GROUP BY " + APIUsageStatisticsClientConstants.CONSUMERKEY
                                + ',' + APIUsageStatisticsClientConstants.USER_ID
                                + " ORDER BY net_total_requests DESC LIMIT ?";
                    }

                    statement = connection.prepareStatement(query);
                    if (dbProductName.contains("Microsoft") || dbProductName.contains("MS SQL")) {
                        statement.setInt(1, limit);
                        statement.setString(2, fromDate);
                        statement.setString(3, toDate);

                    } else {
                        int index = 1;
                        statement.setString(index++, fromDate);
                        statement.setString(index, toDate);
                        statement.setInt(3, limit);
                    }
                }

                resultSet = statement.executeQuery();
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
            handleException("Error occurred while querying top app usage data from JDBC database", e);
        } finally {
            closeDatabaseLinks(resultSet, statement, connection);
        }
        return topAppUsageDataList;
    }

    /**
     * this method return the top users for the for the provided API.
     *
     * @param apiName API name
     * @param version version of the required API
     * @param fromDate Start date of the time span
     * @param toDate End date of time span
     * @param start starting index of the result
     * @param limit number of results to return
     * @return a collection containing the data related to Api usage
     * @throws APIMgtUsageQueryServiceClientException
     */
    @Override
    public ApiTopUsersListDTO getTopApiUsers(String apiName, String version, String tenantDomain, String fromDate,
            String toDate, int start, int limit) throws APIMgtUsageQueryServiceClientException {
        List<ApiTopUsersDTO> tenantFilteredTopUsersDTOs = getTopApiUsers(
                APIUsageStatisticsClientConstants.API_REQUEST_SUMMARY, apiName, tenantDomain, version, fromDate,
                toDate);

        //filter based on pagination
        List<ApiTopUsersDTO> paginationFilteredTopUsersDTOs = new ArrayList<ApiTopUsersDTO>();
        ApiTopUsersListDTO apiTopUsersListDTO = new ApiTopUsersListDTO();
        int end = (start + limit) <= tenantFilteredTopUsersDTOs.size() ? (start + limit) : tenantFilteredTopUsersDTOs.size();
        for (int i = start; i < end; i++) {
            paginationFilteredTopUsersDTOs.add(tenantFilteredTopUsersDTOs.get(i));
        }
        apiTopUsersListDTO.setApiTopUsersDTOs(paginationFilteredTopUsersDTOs);
        apiTopUsersListDTO.setLimit(limit);
        apiTopUsersListDTO.setOffset(start);
        apiTopUsersListDTO.setTotalRecordCount(tenantFilteredTopUsersDTOs.size());
        return apiTopUsersListDTO;
    }

    /**
     * This method gets the top user usage data for invoking APIs
     *
     * @param tableName name of the required table in the database
     * @param apiName API name
     * @param version version of the required API
     * @param fromDate Start date of the time span
     * @param toDate End date of time span
     * @return a collection containing the data related to Api usage
     * @throws APIMgtUsageQueryServiceClientException if an error occurs while querying the database
     */
    private List<ApiTopUsersDTO> getTopApiUsers(String tableName, String apiName, String tenantDomain, String version,
            String fromDate, String toDate) throws APIMgtUsageQueryServiceClientException {
        //ignoring sql injection for keyString since it construct locally and no public access
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<ApiTopUsersDTO> apiTopUsersDataList = new ArrayList<ApiTopUsersDTO>();
        try {
            connection = dataSource.getConnection();
            StringBuilder topApiUserQuery;
            //check whether table exist first
            if (isTableExist(tableName, connection)) {
                long totalRequestCount = getTotalRequestCountOfAPIVersion(tableName, apiName, tenantDomain, version,
                        fromDate, toDate);
                topApiUserQuery = new StringBuilder("SELECT " + APIUsageStatisticsClientConstants.USER_ID + ","
                        + APIUsageStatisticsClientConstants.API_PUBLISHER + ","
                        + "SUM(" + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ") "
                        + "AS net_total_requests FROM "  + tableName + " WHERE "
                        + APIUsageStatisticsClientConstants.API + "= ? AND ");

                if (!APIUsageStatisticsClientConstants.FOR_ALL_API_VERSIONS.equals(version)) {
                    topApiUserQuery.append(APIUsageStatisticsClientConstants.VERSION + "= ? AND ");
                }

                topApiUserQuery.append( "time BETWEEN  ? AND ? " + " GROUP BY "
                        + APIUsageStatisticsClientConstants.USER_ID + ","
                        + APIUsageStatisticsClientConstants.API_PUBLISHER
                        + " ORDER BY net_total_requests DESC");

                statement = connection.prepareStatement(topApiUserQuery.toString());
                int index = 1;
                statement.setString(index++, apiName);
                if (!APIUsageStatisticsClientConstants.FOR_ALL_API_VERSIONS.equals(version)) {
                    statement.setString(index++, version);
                }
                statement.setString(index++, fromDate);
                statement.setString(index, toDate);
                resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    String provider = resultSet.getString(APIUsageStatisticsClientConstants.API_PUBLISHER);
                    if (provider != null && MultitenantUtils.getTenantDomain(provider).equals(tenantDomain)) {
                        String userId = resultSet.getString(APIUsageStatisticsClientConstants.USER_ID);
                        long requestCount = resultSet.getLong("net_total_requests");
                        ApiTopUsersDTO apiTopUsersDTO = new ApiTopUsersDTO();
                        apiTopUsersDTO.setApiName(apiName);
                        apiTopUsersDTO.setFromDate(fromDate);
                        apiTopUsersDTO.setToDate(toDate);
                        apiTopUsersDTO.setVersion(version);
                        apiTopUsersDTO.setProvider(provider);

                        //remove @carbon.super from super tenant users
                        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(MultitenantUtils
                                .getTenantDomain(userId))) {
                            userId = MultitenantUtils.getTenantAwareUsername(userId);
                        }
                        apiTopUsersDTO.setUser(userId);
                        apiTopUsersDTO.setRequestCount(requestCount);
                        apiTopUsersDTO.setTotalRequestCount(totalRequestCount);
                        apiTopUsersDataList.add(apiTopUsersDTO);
                    }
                }
            }
        } catch (SQLException e) {
            handleException("Error occurred while querying top api users data from JDBC database", e);
        } finally {
            closeDatabaseLinks(resultSet, statement, connection);
        }
        return apiTopUsersDataList;
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
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<FaultCountDTO> falseAppUsageDataList = new ArrayList<FaultCountDTO>();

        try {
            connection = dataSource.getConnection();
            String query;
            //check whether table exist first
            if (isTableExist(tableName, connection)) {
                //ignoring sql injection for keyString since it construct locally and no public access
                query = "SELECT " +
                        APIUsageStatisticsClientConstants.CONSUMERKEY + ',' + APIUsageStatisticsClientConstants.API
                        + ',' + APIUsageStatisticsClientConstants.API_PUBLISHER + ',' + "SUM("
                        + APIUsageStatisticsClientConstants.TOTAL_FAULT_COUNT + ") AS total_faults " +
                        " FROM " + tableName +
                        " WHERE " + APIUsageStatisticsClientConstants.CONSUMERKEY + " IN (" + keyString
                        + ") AND time BETWEEN ? AND ? GROUP BY " + APIUsageStatisticsClientConstants.CONSUMERKEY + ","
                        + APIUsageStatisticsClientConstants.API_PUBLISHER + "," + APIUsageStatisticsClientConstants.API;

                statement = connection.prepareStatement(query);
                int index = 1;
                statement.setString(index++, fromDate);
                statement.setString(index, toDate);
                resultSet = statement.executeQuery();
                FaultCountDTO faultCountDTO;
                while (resultSet.next()) {
                    String apiName = resultSet.getString(APIUsageStatisticsClientConstants.API);
                    String publisher = resultSet.getString(APIUsageStatisticsClientConstants.API_PUBLISHER);
                    apiName = apiName + " (" + publisher + ")";
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
            handleException("Error occurred while querying API faulty invocation data from JDBC database", e);
        } finally {
            closeDatabaseLinks(resultSet, statement, connection);
        }
        return falseAppUsageDataList;
    }

    /**
     * This method retrieve and return the usage parth invocations per applications
     * @param subscriberName subscriber name
     * @param groupId        group id of the subscriber
     * @param fromDate       starting date
     * @param toDate         ending data
     * @param limit          limit of the result
     * @return list if AppCallTypeDTO
     * @throws APIMgtUsageQueryServiceClientException throws if error occurred
     */
    @Override
    public List<AppCallTypeDTO> getAppApiCallType(String subscriberName, String groupId, String fromDate, String toDate,
            int limit) throws APIMgtUsageQueryServiceClientException {

        List<String> subscriberApps = getAppsBySubscriber(subscriberName, groupId);
        StringBuilder concatenatedKeys = new StringBuilder();
        int size = subscriberApps.size();
        if (size > 0) {
            concatenatedKeys.append("'").append(subscriberApps.get(0)).append("'");
        } else {
            return Collections.emptyList();
        }
        for (int i = 1; i < subscriberApps.size(); i++) {
            concatenatedKeys.append(",'").append(subscriberApps.get(i)).append("'");
        }
        return getAPICallTypeUsageData(APIUsageStatisticsClientConstants.API_Resource_Path_USAGE_SUMMARY,
                concatenatedKeys.toString(), fromDate, toDate, limit);
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
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<AppCallTypeDTO> appApiCallTypeList = new ArrayList<AppCallTypeDTO>();
        try {
            connection = dataSource.getConnection();
            String query;
            //check whether table exist first
            if (isTableExist(tableName, connection)) {
                //ignoring sql injection for keyString since it construct locally and no public access
                if (connection.getMetaData().getDatabaseProductName().contains("DB2")) {
                    query = "SELECT " + APIUsageStatisticsClientConstants.API + ","
                            + APIUsageStatisticsClientConstants.VERSION + ","
                            + APIUsageStatisticsClientConstants.API_PUBLISHER + ","
                            + APIUsageStatisticsClientConstants.CONSUMERKEY + ","
                            + APIUsageStatisticsClientConstants.RESOURCE + ","
                            + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ","
                            + APIUsageStatisticsClientConstants.CONTEXT + "," + APIUsageStatisticsClientConstants.METHOD
                            + "," + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ","
                            + APIUsageStatisticsClientConstants.HOST_NAME + "," + APIUsageStatisticsClientConstants.YEAR
                            + "," + APIUsageStatisticsClientConstants.MONTH + ","
                            + APIUsageStatisticsClientConstants.DAY + "," + APIUsageStatisticsClientConstants.TIME
                            + " FROM " + tableName + "  WHERE " + APIUsageStatisticsClientConstants.CONSUMERKEY
                            + " IN (" + keyString + " ) " + " AND " + APIUsageStatisticsClientConstants.TIME
                            + " BETWEEN ? AND ? "
                            + " GROUP BY " + APIUsageStatisticsClientConstants.API + ","
                            + APIUsageStatisticsClientConstants.VERSION + ","
                            + APIUsageStatisticsClientConstants.API_PUBLISHER + ","
                            + APIUsageStatisticsClientConstants.CONSUMERKEY + ","
                            + APIUsageStatisticsClientConstants.RESOURCE + ","
                            + APIUsageStatisticsClientConstants.CONTEXT + "," + APIUsageStatisticsClientConstants.METHOD
                            + "," + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ","
                            + APIUsageStatisticsClientConstants.HOST_NAME + "," + APIUsageStatisticsClientConstants.YEAR
                            + "," + APIUsageStatisticsClientConstants.MONTH + ","
                            + APIUsageStatisticsClientConstants.DAY + "," +
                            APIUsageStatisticsClientConstants.TIME;
                } else {
                    query = "SELECT " + APIUsageStatisticsClientConstants.API + ","
                            + APIUsageStatisticsClientConstants.API_PUBLISHER + ","
                            + APIUsageStatisticsClientConstants.METHOD + ","
                            + APIUsageStatisticsClientConstants.CONSUMERKEY + ","
                            + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ","
                            + APIUsageStatisticsClientConstants.RESOURCE + " FROM " + tableName + " WHERE "
                            + APIUsageStatisticsClientConstants.CONSUMERKEY + " IN (" + keyString + ") " +
                            " AND " + APIUsageStatisticsClientConstants.TIME + " BETWEEN ? AND ?  GROUP BY "
                            + APIUsageStatisticsClientConstants.CONSUMERKEY + ","
                            + APIUsageStatisticsClientConstants.API + ","
                            + APIUsageStatisticsClientConstants.API_PUBLISHER + ","
                            + APIUsageStatisticsClientConstants.METHOD + ","
                            + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ","
                            + APIUsageStatisticsClientConstants.RESOURCE;
                }

                statement = connection.prepareStatement(query);
                int index = 1;
                statement.setString(index++, fromDate);
                statement.setString(index, toDate);
                resultSet = statement.executeQuery();
                AppCallTypeDTO appCallTypeDTO;
                while (resultSet.next()) {
                    String apiName = resultSet.getString(APIUsageStatisticsClientConstants.API);
                    String publisher = resultSet.getString(APIUsageStatisticsClientConstants.API_PUBLISHER);
                    apiName = apiName + " (" + publisher + ")";
                    String callType = resultSet.getString(APIUsageStatisticsClientConstants.METHOD);
                    String consumerKey = resultSet.getString(APIUsageStatisticsClientConstants.CONSUMERKEY);
                    String resource = resultSet.getString(APIUsageStatisticsClientConstants.RESOURCE);
                    int hitCount = resultSet.getInt(APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT);
                    List<String> callTypeList = new ArrayList<String>();
                    List<Integer> hitCountList = new ArrayList<Integer>();
                    callTypeList.add(resource + " (" + callType + ")");
                    hitCountList.add(hitCount);
                    String appName = subscriberAppsMap.get(consumerKey);

                    boolean found = false;
                    for (AppCallTypeDTO dto : appApiCallTypeList) {
                        if (dto.getAppName().equals(appName)) {
                            dto.addToApiCallTypeArray(apiName, callTypeList, hitCountList);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        appCallTypeDTO = new AppCallTypeDTO();
                        appCallTypeDTO.setAppName(appName);
                        appCallTypeDTO.addToApiCallTypeArray(apiName, callTypeList, hitCountList);
                        appApiCallTypeList.add(appCallTypeDTO);
                    }
                }
            }
        } catch (SQLException e) {
            handleException("Error occurred while querying API call type data from JDBC database", e);
        } finally {
            closeDatabaseLinks(resultSet, statement, connection);
        }
        return appApiCallTypeList;
    }

    /**
     * this method find the API Usage per Application data
     * @param subscriberName subscriber name
     * @param groupId        group id of the subscriber
     * @param fromDate       starting date
     * @param toDate         ending data
     * @param limit          limit of the result
     * @return list of PerAppApiCountDTO
     * @throws APIMgtUsageQueryServiceClientException throws if error occured
     */
    @Override
    public List<PerAppApiCountDTO> perAppPerAPIUsage(String subscriberName, String groupId, String fromDate,
            String toDate, int limit) throws APIMgtUsageQueryServiceClientException {

        List<String> subscriberApps = getAppsBySubscriber(subscriberName, groupId);
        StringBuilder concatenatedKeys = new StringBuilder();
        int size = subscriberApps.size();
        if (size > 0) {
            concatenatedKeys.append("'").append(subscriberApps.get(0)).append("'");
        } else {
            return Collections.emptyList();
        }
        for (int i = 1; i < subscriberApps.size(); i++) {
            concatenatedKeys.append(",'").append(subscriberApps.get(i)).append("'");
        }
        return getPerAppAPIUsageData(APIUsageStatisticsClientConstants.API_REQUEST_SUMMARY, concatenatedKeys.toString(),
                fromDate, toDate, limit);
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
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<PerAppApiCountDTO> perAppUsageDataList = new ArrayList<PerAppApiCountDTO>();
        try {
            connection = dataSource.getConnection();
            String query;
            //check whether table exist first
            if (isTableExist(tableName, connection)) {
                //ignoring sql injection for keyString since it construct locally and no public access
                if (connection.getMetaData().getDatabaseProductName().contains("DB2")) {
                    query = "SELECT " + APIUsageStatisticsClientConstants.API + ","
                            + APIUsageStatisticsClientConstants.API_VERSION + ","
                            + APIUsageStatisticsClientConstants.VERSION + ","
                            + APIUsageStatisticsClientConstants.API_PUBLISHER + ","
                            + APIUsageStatisticsClientConstants.CONSUMERKEY + ","
                            + APIUsageStatisticsClientConstants.USER_ID + ","
                            + APIUsageStatisticsClientConstants.CONTEXT + ","
                            + APIUsageStatisticsClientConstants.MAX_REQUEST_TIME + ","
                            + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ","
                            + APIUsageStatisticsClientConstants.HOST_NAME + ","
                            + APIUsageStatisticsClientConstants.YEAR + "," + APIUsageStatisticsClientConstants.MONTH + ","
                            + APIUsageStatisticsClientConstants.DAY + ","
                            + APIUsageStatisticsClientConstants.TIME + ",SUM("
                            + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ") AS total_calls " + " FROM "
                            + APIUsageStatisticsClientConstants.API_REQUEST_SUMMARY + " WHERE "
                            + APIUsageStatisticsClientConstants.CONSUMERKEY + " IN (" + keyString + ") AND "
                            + APIUsageStatisticsClientConstants.TIME + " BETWEEN ? AND ?  GROUP BY "
                            + APIUsageStatisticsClientConstants.API + ","
                            + APIUsageStatisticsClientConstants.API_VERSION + ","
                            + APIUsageStatisticsClientConstants.VERSION + ","
                            + APIUsageStatisticsClientConstants.API_PUBLISHER + ","
                            + APIUsageStatisticsClientConstants.CONSUMERKEY + ","
                            + APIUsageStatisticsClientConstants.USER_ID + ","
                            + APIUsageStatisticsClientConstants.CONTEXT + ","
                            + APIUsageStatisticsClientConstants.MAX_REQUEST_TIME + ","
                            + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ","
                            + APIUsageStatisticsClientConstants.HOST_NAME + ","
                            + APIUsageStatisticsClientConstants.YEAR + "," + APIUsageStatisticsClientConstants.MONTH + ","
                            + APIUsageStatisticsClientConstants.DAY + "," + APIUsageStatisticsClientConstants.TIME;
                } else {
                    query = "SELECT " + APIUsageStatisticsClientConstants.API + ","
                            + APIUsageStatisticsClientConstants.API_PUBLISHER + ","
                            + APIUsageStatisticsClientConstants.CONSUMERKEY + "," + " SUM("
                            + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ") AS total_calls " + " FROM "
                            + APIUsageStatisticsClientConstants.API_REQUEST_SUMMARY + " WHERE "
                            + APIUsageStatisticsClientConstants.CONSUMERKEY + " IN (" + keyString + ")  AND "
                            + APIUsageStatisticsClientConstants.TIME + " BETWEEN ? AND ?  GROUP BY "
                            + APIUsageStatisticsClientConstants.API + ","
                            + APIUsageStatisticsClientConstants.API_PUBLISHER + ","
                            + APIUsageStatisticsClientConstants.CONSUMERKEY;
                }

                statement = connection.prepareStatement(query);
                int index = 1;
                statement.setEscapeProcessing(true);
                statement.setString(index++, fromDate);
                statement.setString(index, toDate);
                resultSet = statement.executeQuery();
                PerAppApiCountDTO apiUsageDTO;
                while (resultSet.next()) {
                    String apiName = resultSet.getString(APIUsageStatisticsClientConstants.API);
                    String publisher = resultSet.getString(APIUsageStatisticsClientConstants.API_PUBLISHER);
                    apiName = apiName + " (" + publisher + ")";
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
            handleException("Error occurred while querying per App usage data from JDBC database", e);
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
     * @throws org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException if an error occurs
     *             while contacting backend services
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
        return getAPIUsageTopEntries(new ArrayList<APIUsageDTO>(usageByAPIs.values()), limit);
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
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        Collection<APIUsage> usageDataList = new ArrayList<APIUsage>();
        try {
            connection = dataSource.getConnection();
            String query;
            //check whether table exist first
            if (isTableExist(tableName, connection)) {

                if (connection.getMetaData().getDatabaseProductName().contains("DB2")) {
                    query = "SELECT " +
                            APIUsageStatisticsClientConstants.API + "," +
                            APIUsageStatisticsClientConstants.CONTEXT + "," +
                            APIUsageStatisticsClientConstants.VERSION + "," +
                            "SUM(" + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ") AS aggregateSum " +
                            " FROM " + tableName + " GROUP BY " + APIUsageStatisticsClientConstants.API + "," +
                            APIUsageStatisticsClientConstants.CONTEXT + "," + APIUsageStatisticsClientConstants.VERSION;
                    statement = connection.prepareStatement(query);
                } else {
                    query = "SELECT " +
                            APIUsageStatisticsClientConstants.API + "," +
                            APIUsageStatisticsClientConstants.CONTEXT + "," +
                            APIUsageStatisticsClientConstants.VERSION + "," +
                            "SUM(" + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ") AS aggregateSum " +
                            " FROM " + tableName + " WHERE " + APIUsageStatisticsClientConstants.TIME + " BETWEEN ? AND ? " +
                            " GROUP BY " + APIUsageStatisticsClientConstants.API + "," +
                            APIUsageStatisticsClientConstants.CONTEXT + "," + APIUsageStatisticsClientConstants.VERSION;
                    statement = connection.prepareStatement(query);
                    statement.setString(1, fromDate);
                    statement.setString(2, toDate);
                }

                resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    String apiName = resultSet.getString(APIUsageStatisticsClientConstants.API);
                    String context = resultSet.getString(APIUsageStatisticsClientConstants.CONTEXT);
                    String version = resultSet.getString(APIUsageStatisticsClientConstants.VERSION);
                    long requestCount = resultSet.getLong("aggregateSum");
                    usageDataList.add(new APIUsage(apiName, context, version, requestCount));
                }
            }
        } catch (SQLException e) {
            handleException("Error occurred while querying API usage data from JDBC database", e);
        } finally {
            closeDatabaseLinks(resultSet, statement, connection);
        }
        return usageDataList;
    }

    /**
     * Returns a list of APIVersionUsageDTO objects that contain information related to a
     * particular API of a specified provider, along with the number of API calls processed
     * by each version of that API for a particular time preriod.
     *
     * @param providerName API publisher username
     * @param apiName API name
     * @param fromDate Starting date
     * @param toDate Ending date
     * @return list of APIVersionUsageDTO
     * @throws org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException if error occurred
     */
    @Override
    public List<APIVersionUsageDTO> getUsageByAPIVersions(String providerName, String apiName, String fromDate,
            String toDate) throws APIMgtUsageQueryServiceClientException {

        List<APIUsage> usageData = this
                .getUsageByAPIVersionsData(APIUsageStatisticsClientConstants.API_VERSION_USAGE_SUMMARY, fromDate,
                        toDate, apiName);
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
                .getAPIUsageByResourcePathData(APIUsageStatisticsClientConstants.API_Resource_Path_USAGE_SUMMARY,
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
                    usageDTO.setResourcePath(usage.getResourcePath());
                    usageByResourcePath.add(usageDTO);
                }
            }
        }
        return usageByResourcePath;
    }

    /**
     * This method find the destination of the apis
     * @param providerName Name of the API provider
     * @param fromDate     starting date of the results
     * @param toDate       ending date of the results
     * @return list of APIDestinationUsageDTO
     * @throws APIMgtUsageQueryServiceClientException throws if error occurred
     */
    @Override
    public List<APIDestinationUsageDTO> getAPIUsageByDestination(String providerName, String fromDate, String toDate)
            throws APIMgtUsageQueryServiceClientException {

        List<APIUsageByDestination> usageData = this
                .getAPIUsageByDestinationData(APIUsageStatisticsClientConstants.API_USAGEBY_DESTINATION_SUMMARY,
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
    @Override
    public List<APIUsageByUserDTO> getAPIUsageByUser(String providerName, String fromDate, String toDate)
            throws APIMgtUsageQueryServiceClientException {

        List<APIUsageByUserName> usageData = this
                .getAPIUsageByUserData(providerName, fromDate, toDate, null);
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
    @Override
    public List<APIResponseTimeDTO> getProviderAPIServiceTime(String providerName, String fromDate, String toDate,
            int limit) throws APIMgtUsageQueryServiceClientException {

        Collection<APIResponseTime> responseTimes = getAPIResponseTimeData(
                APIUsageStatisticsClientConstants.API_VERSION_SERVICE_TIME_SUMMARY);
        List<API> providerAPIs = getAPIsByProvider(providerName);
        DecimalFormat format = new DecimalFormat("#.##");
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
        List<APIResponseTimeDTO> apiResponseTimeUsage = new ArrayList<APIResponseTimeDTO>();

        for (APIResponseTime responseTime : responseTimes) {
            for (API providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(responseTime.getApiName()) &&
                        providerAPI.getId().getVersion().equals(responseTime.getApiVersion()) &&
                        providerAPI.getContext().equals(responseTime.getContext())) {
                    APIResponseTimeDTO responseTimeDTO = new APIResponseTimeDTO();
                    responseTimeDTO.setApiName(responseTime.getApiName());
                    //calculate the average response time
                    double avgTime = responseTime.getResponseTime() / responseTime.getResponseCount();
                    //format the time
                    try {
                        responseTimeDTO.setServiceTime(numberFormat.parse(format.format(avgTime)).doubleValue());
                    } catch (ParseException e) {
                        handleException("Parse exception while formatting time");
                    }
                    apiResponseTimeUsage.add(responseTimeDTO);
                }
            }
        }
        return getResponseTimeTopEntries(apiResponseTimeUsage, limit);
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
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        Collection<APIResponseTime> responseTimeData = new ArrayList<APIResponseTime>();
        try {
            connection = dataSource.getConnection();
            String query;
            if (connection.getMetaData().getDatabaseProductName().contains("DB2")) {
                query = "SELECT TempTable.*, " + "SUM(" + APIUsageStatisticsClientConstants.TOTAL_RESPONSE_COUNT
                        + ") AS totalTime ," + "SUM(weighted_service_time) AS totalWeightTime " + " FROM (SELECT "
                        + APIUsageStatisticsClientConstants.API_VERSION + ","
                        + APIUsageStatisticsClientConstants.API_PUBLISHER + ","
                        + APIUsageStatisticsClientConstants.CONTEXT + ","
                        + APIUsageStatisticsClientConstants.SERVICE_TIME + ","
                        + APIUsageStatisticsClientConstants.TOTAL_RESPONSE_COUNT + ","
                        + APIUsageStatisticsClientConstants.HOST_NAME + "," + APIUsageStatisticsClientConstants.YEAR
                        + "," + APIUsageStatisticsClientConstants.MONTH + "," + APIUsageStatisticsClientConstants.DAY
                        + "," + APIUsageStatisticsClientConstants.TIME + ", ("
                        + APIUsageStatisticsClientConstants.SERVICE_TIME + " * "
                        + APIUsageStatisticsClientConstants.TOTAL_RESPONSE_COUNT + ") AS weighted_service_time "
                        + " FROM " + APIUsageStatisticsClientConstants.API_VERSION_SERVICE_TIME_SUMMARY + ") "
                        + "TempTable " + " GROUP BY " + APIUsageStatisticsClientConstants.API_VERSION + ","
                        + APIUsageStatisticsClientConstants.API_PUBLISHER + ","
                        + APIUsageStatisticsClientConstants.CONTEXT + ","
                        + APIUsageStatisticsClientConstants.SERVICE_TIME + ","
                        + APIUsageStatisticsClientConstants.TOTAL_RESPONSE_COUNT + ","
                        + APIUsageStatisticsClientConstants.HOST_NAME + "," + APIUsageStatisticsClientConstants.YEAR
                        + "," + APIUsageStatisticsClientConstants.MONTH + "," + APIUsageStatisticsClientConstants.DAY
                        + "," + APIUsageStatisticsClientConstants.TIME + ", weighted_service_time";
            } else if (connection != null && connection.getMetaData().getDatabaseProductName()
                    .equalsIgnoreCase("oracle")) {
                query = "select " + APIUsageStatisticsClientConstants.API_VERSION + ','
                        + APIUsageStatisticsClientConstants.CONTEXT + ',' + "SUM("
                        + APIUsageStatisticsClientConstants.TOTAL_RESPONSE_COUNT + ") AS totalTime,SUM(CAST("
                        + APIUsageStatisticsClientConstants.SERVICE_TIME + " as FLOAT) * CAST("
                        + APIUsageStatisticsClientConstants.TOTAL_RESPONSE_COUNT + " as FLOAT)) AS totalWeightTime" +
                        " from " + tableName + " GROUP BY " + APIUsageStatisticsClientConstants.CONTEXT + ','
                        + APIUsageStatisticsClientConstants.API_VERSION;
            } else {
                query = "select " + APIUsageStatisticsClientConstants.API_VERSION + ','
                        + APIUsageStatisticsClientConstants.CONTEXT + ',' + "SUM("
                        + APIUsageStatisticsClientConstants.TOTAL_RESPONSE_COUNT + ") AS totalTime,SUM(CAST("
                        + APIUsageStatisticsClientConstants.SERVICE_TIME + " as bigint) * CAST("
                        + APIUsageStatisticsClientConstants.TOTAL_RESPONSE_COUNT + " as bigint)) AS totalWeightTime" +
                        " from " + tableName + " GROUP BY " + APIUsageStatisticsClientConstants.CONTEXT + ','
                        + APIUsageStatisticsClientConstants.API_VERSION;
            }

            statement = connection.prepareStatement(query);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String apiVersion = resultSet.getString(APIUsageStatisticsClientConstants.API_VERSION).split("--")[1];
                String apiName = apiVersion.split(":v")[0];
                String version = apiVersion.split(":v")[1];
                String context = resultSet.getString(APIUsageStatisticsClientConstants.CONTEXT);
                long responseCount = resultSet.getLong("totalTime");
                double responseTime = resultSet.getDouble("totalWeightTime") / responseCount;
                responseTimeData.add(new APIResponseTime(apiName, version, context, responseTime, responseCount));
            }
        } catch (SQLException e) {
            handleException("Error occurred while querying API response times from JDBC database", e);
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
        for (APIAccessTime accessTime : accessTimes) {
            for (API providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(accessTime.getApiName()) &&
                        providerAPI.getId().getVersion().equals(accessTime.getApiVersion()) &&
                        providerAPI.getContext().equals(accessTime.getContext())) {

                    accessTimeDTO = new APIVersionLastAccessTimeDTO();
                    String apiName = accessTime.getApiName() + " (" + providerAPI.getId().getProviderName() + ")";
                    accessTimeDTO.setApiName(apiName);
                    accessTimeDTO.setApiVersion(accessTime.getApiVersion());
                    accessTimeDTO.setLastAccessTime(Long.toString(accessTime.getAccessTime()));
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
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        Collection<APIAccessTime> lastAccessTimeData = new ArrayList<APIAccessTime>();
        String tenantDomain = MultitenantUtils.getTenantDomain(providerName);
        try {
            connection = dataSource.getConnection();
            StringBuilder lastAccessQuery = new StringBuilder(
                    "SELECT " + APIUsageStatisticsClientConstants.API + "," + APIUsageStatisticsClientConstants.VERSION
                            + "," + APIUsageStatisticsClientConstants.CONTEXT + ","
                            + APIUsageStatisticsClientConstants.USER_ID + ","
                            + APIUsageStatisticsClientConstants.REQUEST_TIME + " FROM "
                            + APIUsageStatisticsClientConstants.API_LAST_ACCESS_TIME_SUMMARY);

            lastAccessQuery.append(" where " + APIUsageStatisticsClientConstants.TENANT_DOMAIN + "= ?");
            if (!providerName.startsWith(APIUsageStatisticsClientConstants.ALL_PROVIDERS)) {
                lastAccessQuery
                        .append(" AND (" + APIUsageStatisticsClientConstants.API_PUBLISHER_THROTTLE_TABLE + "= ? OR "
                                + APIUsageStatisticsClientConstants.API_PUBLISHER_THROTTLE_TABLE + "= ?)");
            }
            lastAccessQuery.append(" order by " + APIUsageStatisticsClientConstants.REQUEST_TIME + " DESC");

            statement = connection.prepareStatement(lastAccessQuery.toString());
            statement.setString(1, tenantDomain);
            if (!providerName.startsWith(APIUsageStatisticsClientConstants.ALL_PROVIDERS)) {
                statement.setString(2, providerName);
                statement.setString(3, APIUtil.getUserNameWithTenantSuffix(providerName));
            }
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String apiName = resultSet.getString(APIUsageStatisticsClientConstants.API);
                String version = resultSet.getString(APIUsageStatisticsClientConstants.VERSION);
                String context = resultSet.getString(APIUsageStatisticsClientConstants.CONTEXT);
                long accessTime = resultSet.getLong(APIUsageStatisticsClientConstants.REQUEST_TIME);
                String username = resultSet.getString(APIUsageStatisticsClientConstants.USER_ID);
                lastAccessTimeData.add(new APIAccessTime(apiName, version, context, accessTime, username));
            }
        } catch (SQLException e) {
            handleException("Error occurred while querying last access data for APIs from JDBC database", e);
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

    /**
     * This method find the fault count of the APIs
     * @param providerName Name of the API provider
     * @param fromDate     starting date of the results
     * @param toDate       ending date of the results
     * @return list of APIResponseFaultCountDTO
     * @throws APIMgtUsageQueryServiceClientException throws if error occurred
     */
    @Override
    public List<APIResponseFaultCountDTO> getAPIResponseFaultCount(String providerName, String fromDate, String toDate)
            throws APIMgtUsageQueryServiceClientException {

        List<APIResponseFaultCount> faultyData = this
                .getAPIResponseFaultCountData(APIUsageStatisticsClientConstants.API_FAULT_SUMMARY, fromDate, toDate);
        List<API> providerAPIs = getAPIsByProvider(providerName);
        List<APIResponseFaultCountDTO> faultyCount = new ArrayList<APIResponseFaultCountDTO>();
        List<APIVersionUsageDTO> apiVersionUsageList;

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
                    for (APIVersionUsageDTO apiVersionUsageDTO : apiVersionUsageList) {
                        if (apiVersionUsageDTO.getVersion().equals(fault.getApiVersion())) {
                            long requestCount = apiVersionUsageDTO.getCount();
                            double faultPercentage =
                                    ((double) requestCount - fault.getFaultCount()) / requestCount * 100;
                            DecimalFormat twoDForm = new DecimalFormat("#.##");
                            NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
                            try {
                                faultPercentage = 100 - numberFormat.parse(twoDForm.format(faultPercentage)).doubleValue();
                            } catch (ParseException e) {
                                handleException("Parse exception while formatting time");
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

    /**
     * find the API usage
     * @param providerName API provider name
     * @param apiName      Name of the API
     * @param apiVersion   API version
     * @param limit        Number of sorted entries to return
     * @return  list of PerUserAPIUsageDTO
     * @throws APIMgtUsageQueryServiceClientException throws if error occurred
     */
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

    /**
     * This method sort and set the result size
     *
     * @param usageData result to be sort
     * @param limit     value to limit
     * @return list of PerUserAPIUsageDTO
     */
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

    /**
     * This method sort and limit the result size for API usage data
     *
     * @param usageData data to be sort and limit
     * @param limit     value to be limited
     * @return list of APIUsageDTO
     */
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

    /**
     * This method sort and limit the result size for API Response time data
     *
     * @param usageData data to be sort and limit
     * @param limit value to be limited
     * @return list of APIResponseTimeDTO
     */
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

    /**
     * This method sort and limit the result size for API Last access time data
     *
     * @param usageData data to be sort and limit
     * @param limit value to be limited
     * @return list of APIVersionLastAccessTimeDTO
     */
    private List<APIVersionLastAccessTimeDTO> getLastAccessTimeTopEntries(List<APIVersionLastAccessTimeDTO> usageData,
            int limit) {
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

    /**
     * This method find the API fault count data
     * @param tableName Name of the table data exist
     * @param fromDate starting data
     * @param toDate ending date
     * @return list of APIResponseFaultCount
     * @throws APIMgtUsageQueryServiceClientException throws if error occurred
     */
    private List<APIResponseFaultCount> getAPIResponseFaultCountData(String tableName, String fromDate, String toDate)
            throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            handleException("BAM data source hasn't been initialized. Ensure that the data source" +
                    " is properly configured in the APIUsageTracker configuration.");
        }
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        List<APIResponseFaultCount> faultUsage = new ArrayList<APIResponseFaultCount>();
        try {
            connection = dataSource.getConnection();
            String query =
                    "SELECT " + APIUsageStatisticsClientConstants.API + ',' + APIUsageStatisticsClientConstants.VERSION
                            + ',' + APIUsageStatisticsClientConstants.API_PUBLISHER + ','
                            + APIUsageStatisticsClientConstants.CONTEXT + ',' + "SUM("
                            + APIUsageStatisticsClientConstants.TOTAL_FAULT_COUNT + ") as total_fault_count FROM "
                            + tableName + " WHERE " + APIUsageStatisticsClientConstants.TIME
                            + " BETWEEN ? AND ? GROUP BY " + APIUsageStatisticsClientConstants.API + ','
                            + APIUsageStatisticsClientConstants.VERSION + ','
                            + APIUsageStatisticsClientConstants.API_PUBLISHER + ','
                            + APIUsageStatisticsClientConstants.CONTEXT;

            statement = connection.prepareStatement(query);
            statement.setString(1, fromDate);
            statement.setString(2, toDate);
            rs = statement.executeQuery();
            APIResponseFaultCount apiResponseFaultCount;
            while (rs.next()) {
                String apiName = rs.getString(APIUsageStatisticsClientConstants.API);
                String version = rs.getString(APIUsageStatisticsClientConstants.VERSION);
                String context = rs.getString(APIUsageStatisticsClientConstants.CONTEXT);
                //total_fault_count is not set as constance, since it is temporary variable for sql
                long requestCount = rs.getLong("total_fault_count");
                apiResponseFaultCount = new APIResponseFaultCount(apiName, version, context, requestCount);
                faultUsage.add(apiResponseFaultCount);
            }
            return faultUsage;
        } catch (Exception e) {
            log.error("Error occurred while querying from JDBC database " + e.getMessage(), e);
            throw new APIMgtUsageQueryServiceClientException("Error occurred while querying from JDBC database", e);
        } finally {
            closeDatabaseLinks(rs, statement, connection);
        }
    }

    /**
     * This method find the Resource path usage of APIs
     *
     * @param tableName Name of the table data exist
     * @param fromDate starting data
     * @param toDate ending date
     * @return list of APIUsageByResourcePath
     * @throws APIMgtUsageQueryServiceClientException throws if error occurred
     */
    private List<APIUsageByResourcePath> getAPIUsageByResourcePathData(String tableName, String fromDate, String toDate) throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure "
                    + "that the data source is properly configured in the APIUsageTracker configuration.");
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        List<APIUsageByResourcePath> usage = new ArrayList<APIUsageByResourcePath>();
        try {
            connection = dataSource.getConnection();
            String query =
                    "SELECT " + APIUsageStatisticsClientConstants.API + ',' + APIUsageStatisticsClientConstants.VERSION
                            + ',' + APIUsageStatisticsClientConstants.API_PUBLISHER + ','
                            + APIUsageStatisticsClientConstants.CONTEXT + ',' + APIUsageStatisticsClientConstants.METHOD
                            + ',' + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT
                            + ',' + APIUsageStatisticsClientConstants.RESOURCE + ','
                            + APIUsageStatisticsClientConstants.TIME + " FROM " + tableName + " WHERE "
                            + APIUsageStatisticsClientConstants.TIME + " BETWEEN ?  AND ?";
            statement = connection.prepareStatement(query);
            statement.setString(1, fromDate);
            statement.setString(2, toDate);
            rs = statement.executeQuery();
            APIUsageByResourcePath apiUsageByResourcePath;

            while (rs.next()) {
                String apiName = rs.getString(APIUsageStatisticsClientConstants.API);
                String version = rs.getString(APIUsageStatisticsClientConstants.VERSION);
                String context = rs.getString(APIUsageStatisticsClientConstants.CONTEXT);
                String method = rs.getString(APIUsageStatisticsClientConstants.METHOD);
                long hits = rs.getLong(APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT);
                String resourcePaths = rs.getString(APIUsageStatisticsClientConstants.RESOURCE);
                String time = rs.getString(APIUsageStatisticsClientConstants.TIME);
                apiUsageByResourcePath = new APIUsageByResourcePath(apiName, version, method, context, hits, time,resourcePaths);
                usage.add(apiUsageByResourcePath);
            }
            return usage;
        } catch (Exception e) {
            log.error("Error occurred while querying from JDBC database " + e.getMessage(), e);
            throw new APIMgtUsageQueryServiceClientException("Error occurred while querying from JDBC database", e);
        } finally {
            closeDatabaseLinks(rs, statement, connection);
        }
    }

    /**
     * This method find the API Destination usage of APIs
     *
     * @param tableName Name of the table data exist
     * @param fromDate starting data
     * @param toDate ending date
     * @return list of APIUsageByDestination
     * @throws APIMgtUsageQueryServiceClientException throws if error occurred
     */
    private List<APIUsageByDestination> getAPIUsageByDestinationData(String tableName, String fromDate, String toDate) throws APIMgtUsageQueryServiceClientException {
        if (dataSource == null) {
            handleException("BAM data source hasn't been initialized. Ensure that the data source " +
                    "is properly configured in the APIUsageTracker configuration.");
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        List<APIUsageByDestination> usageByResourcePath = new ArrayList<APIUsageByDestination>();
        try {
            connection = dataSource.getConnection();
            String query =
                    "SELECT " + APIUsageStatisticsClientConstants.API + ',' + APIUsageStatisticsClientConstants.VERSION
                            + ',' + APIUsageStatisticsClientConstants.API_PUBLISHER + ','
                            + APIUsageStatisticsClientConstants.CONTEXT + ','
                            + APIUsageStatisticsClientConstants.DESTINATION + ',' + "SUM("
                            + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ") as total_request_count"
                            + " FROM " + tableName + " WHERE " + APIUsageStatisticsClientConstants.TIME
                            + " BETWEEN ? AND ?" + " GROUP BY " + APIUsageStatisticsClientConstants.API + ','
                            + APIUsageStatisticsClientConstants.VERSION + ','
                            + APIUsageStatisticsClientConstants.API_PUBLISHER + ','
                            + APIUsageStatisticsClientConstants.CONTEXT + ','
                            + APIUsageStatisticsClientConstants.DESTINATION;

            statement = connection.prepareStatement(query);
            statement.setString(1, fromDate);
            statement.setString(2, toDate);
            rs = statement.executeQuery();
            APIUsageByDestination apiUsageByDestination;

            while (rs.next()) {
                String apiName = rs.getString(APIUsageStatisticsClientConstants.API);
                String version = rs.getString(APIUsageStatisticsClientConstants.VERSION);
                String context = rs.getString(APIUsageStatisticsClientConstants.CONTEXT);
                String destination = rs.getString(APIUsageStatisticsClientConstants.DESTINATION);
                long requestCount = rs.getLong("total_request_count");
                apiUsageByDestination = new APIUsageByDestination(apiName, version, context, destination, requestCount);
                usageByResourcePath.add(apiUsageByDestination);
            }
            return usageByResourcePath;
        } catch (Exception e) {
            log.error("Error occurred while querying from JDBC database " + e.getMessage(), e);
            throw new APIMgtUsageQueryServiceClientException("Error occurred while querying from JDBC database", e);
        } finally {
            closeDatabaseLinks(rs, statement, connection);
        }
    }

    /**
     * Retrieves total request count for the given period of time for particular API and Version. If version provided
     * as FOR_ALL_API_VERSIONS it will get total aggregated request count for all api versions
     *
     * @param tableName tableName
     * @param apiName API name
     * @param apiVersion API version
     * @param fromDate Start date of the time span
     * @param toDate End date of time span
     * @return Total request count
     * @throws APIMgtUsageQueryServiceClientException
     */
    private long getTotalRequestCountOfAPIVersion(String tableName, String apiName, String tenantDomain,
            String apiVersion, String fromDate, String toDate)
            throws APIMgtUsageQueryServiceClientException {
        List<APIUsage> apiUsages = getUsageByAPIVersionsData (tableName, fromDate, toDate, apiName);
        long totalRequestCount = 0;
        Pattern tenantContextPattern;
        boolean match;
        if (tenantDomain != null && !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            tenantContextPattern = Pattern.compile("^/t/" + tenantDomain +"/.*");
            //Context should match /t/<tenant-domain>/.. pattern
            match = true;
        } else {
            tenantContextPattern = Pattern.compile("^/t/.*");
            //Context should NOT match /t/<tenant-domain>/.. pattern
            match = false;
        }
        for (APIUsage usage : apiUsages) {
            if (tenantContextPattern.matcher(usage.getContext()).find() == match) {
                if (APIUsageStatisticsClientConstants.FOR_ALL_API_VERSIONS.equals(apiVersion)) {
                    totalRequestCount += usage.getRequestCount();
                } else if (apiVersion.equals(usage.getApiVersion())) {
                    totalRequestCount += usage.getRequestCount();
                }
            }
        }
        return totalRequestCount;
    }

    /**
     *  This method find the API version wise usage
     *
     * @param tableName Name of the table data exist
     * @param fromDate starting data
     * @param toDate ending date
     * @param apiName API name
     * @return list of APIUsage
     * @throws APIMgtUsageQueryServiceClientException throws if error occurred
     */
    private List<APIUsage> getUsageByAPIVersionsData(String tableName, String fromDate, String toDate, String apiName) throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            handleException("BAM data source hasn't been initialized. Ensure that the data source " +
                    "is properly configured in the APIUsageTracker configuration.");
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        List<APIUsage> usageDataList = new ArrayList<APIUsage>();
        try {
            connection = dataSource.getConnection();
            String query;
            if (fromDate != null && toDate != null) {
                query = "SELECT " + APIUsageStatisticsClientConstants.API + ','
                        + APIUsageStatisticsClientConstants.VERSION + ','
                        + APIUsageStatisticsClientConstants.API_PUBLISHER + ','
                        + APIUsageStatisticsClientConstants.CONTEXT + ',' + "SUM("
                        + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ") as total_request_count" +
                        " FROM  " + tableName +
                        " WHERE " + APIUsageStatisticsClientConstants.API + "= ? " +
                        " AND " + APIUsageStatisticsClientConstants.TIME +
                        " BETWEEN ? AND ?" +
                        " GROUP BY " + APIUsageStatisticsClientConstants.API + ','
                        + APIUsageStatisticsClientConstants.VERSION + ','
                        + APIUsageStatisticsClientConstants.API_PUBLISHER + ','
                        + APIUsageStatisticsClientConstants.CONTEXT;

                statement = connection.prepareStatement(query);
                statement.setString(1, apiName);
                statement.setString(2, fromDate);
                statement.setString(3, toDate);
            } else {
                query = "SELECT " + APIUsageStatisticsClientConstants.API + ','
                        + APIUsageStatisticsClientConstants.VERSION + ','
                        + APIUsageStatisticsClientConstants.API_PUBLISHER + ','
                        + APIUsageStatisticsClientConstants.CONTEXT + ",SUM("
                        + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ") as total_request_count" +
                        " FROM  " + tableName +
                        " WHERE " + APIUsageStatisticsClientConstants.API + " = ? " +
                        " GROUP BY " + APIUsageStatisticsClientConstants.API + ','
                        + APIUsageStatisticsClientConstants.VERSION + ','
                        + APIUsageStatisticsClientConstants.API_PUBLISHER + ','
                        + APIUsageStatisticsClientConstants.CONTEXT;

                statement = connection.prepareStatement(query);
                statement.setString(1, apiName);
            }
            rs = statement.executeQuery();
            while (rs.next()) {
                String context = rs.getString(APIUsageStatisticsClientConstants.CONTEXT);
                String version = rs.getString(APIUsageStatisticsClientConstants.VERSION);
                long requestCount = rs.getLong("total_request_count");
                usageDataList.add(new APIUsage(apiName, context, version, requestCount));
            }
            return usageDataList;
        } catch (Exception e) {
            log.error("Error occurred while querying from JDBC database " + e.getMessage(), e);
            throw new APIMgtUsageQueryServiceClientException("Error occurred while querying from JDBC database", e);
        } finally {
            closeDatabaseLinks(rs, statement, connection);
        }
    }

    /**
     * This method find the api usage count and it's subscribers
     * @param providerName logged API publisher
     * @param fromDate starting data
     * @param toDate ending date
     * @param limit result to be limited
     * @return list of APIUsageByUserName
     * @throws APIMgtUsageQueryServiceClientException throws if error occurred
     */
    private List<APIUsageByUserName> getAPIUsageByUserData(String providerName, String fromDate, String toDate,
            Integer limit) throws APIMgtUsageQueryServiceClientException {
        if (dataSource == null) {
            handleException("BAM data source hasn't been initialized. Ensure that the data source " +
                    "is properly configured in the APIUsageTracker configuration.");
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        String tenantDomain = MultitenantUtils.getTenantDomain(providerName);
        try {
            connection = dataSource.getConnection();
            String query;
            String oracleQuery;
            String msSqlQuery;
            String filter;
            if (providerName.contains(APIUsageStatisticsClientConstants.ALL_PROVIDERS)) {
                if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    filter = APIUsageStatisticsClientConstants.CONTEXT + " not like '%/t/%'";
                } else {
                    filter = APIUsageStatisticsClientConstants.CONTEXT + " like '%" + tenantDomain + "%'";
                }
            } else {
                filter = APIUsageStatisticsClientConstants.API_PUBLISHER + " = '" + providerName + "'";
            }

            if (fromDate != null && toDate != null) {
                query = "SELECT " + APIUsageStatisticsClientConstants.API + ','
                        + APIUsageStatisticsClientConstants.API_VERSION + ','
                        + APIUsageStatisticsClientConstants.VERSION + ','
                        + APIUsageStatisticsClientConstants.API_PUBLISHER + ','
                        + APIUsageStatisticsClientConstants.USER_ID + ", SUM("
                        + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ") AS TOTAL_REQUEST_COUNT, "
                        + APIUsageStatisticsClientConstants.CONTEXT +
                        " FROM " + APIUsageStatisticsClientConstants.API_REQUEST_SUMMARY + " WHERE "
                        + APIUsageStatisticsClientConstants.TIME + " BETWEEN " +
                        " ? AND ? AND " + filter +
                        " GROUP BY " + APIUsageStatisticsClientConstants.API + ','
                        + APIUsageStatisticsClientConstants.API_VERSION + ','
                        + APIUsageStatisticsClientConstants.USER_ID + ',' + APIUsageStatisticsClientConstants.VERSION
                        + ',' + APIUsageStatisticsClientConstants.API_PUBLISHER + ','
                        + APIUsageStatisticsClientConstants.CONTEXT + " ORDER BY "
                        + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + " DESC ";
                oracleQuery = "SELECT " + APIUsageStatisticsClientConstants.API + ','
                        + APIUsageStatisticsClientConstants.API_VERSION + ','
                        + APIUsageStatisticsClientConstants.VERSION + ','
                        + APIUsageStatisticsClientConstants.API_PUBLISHER + ','
                        + APIUsageStatisticsClientConstants.USER_ID + ", SUM("
                        + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ") AS TOTAL_REQUEST_COUNT, "
                        + APIUsageStatisticsClientConstants.CONTEXT +
                        " FROM " + APIUsageStatisticsClientConstants.API_REQUEST_SUMMARY + " WHERE "
                        + APIUsageStatisticsClientConstants.TIME + " BETWEEN " +
                        "? AND ? AND " + filter +
                        " GROUP BY " + APIUsageStatisticsClientConstants.API + ','
                        + APIUsageStatisticsClientConstants.API_VERSION + ','
                        + APIUsageStatisticsClientConstants.VERSION + ',' + APIUsageStatisticsClientConstants.USER_ID
                        + ',' + APIUsageStatisticsClientConstants.API_PUBLISHER + ','
                        + APIUsageStatisticsClientConstants.CONTEXT + " ORDER BY "
                        + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + " DESC";
                msSqlQuery = "SELECT " + APIUsageStatisticsClientConstants.API + ','
                        + APIUsageStatisticsClientConstants.API_VERSION + ','
                        + APIUsageStatisticsClientConstants.VERSION + ','
                        + APIUsageStatisticsClientConstants.API_PUBLISHER + ','
                        + APIUsageStatisticsClientConstants.USER_ID + ", SUM("
                        + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ") AS TOTAL_REQUEST_COUNT, "
                        + APIUsageStatisticsClientConstants.CONTEXT +
                        " FROM " + APIUsageStatisticsClientConstants.API_REQUEST_SUMMARY + " WHERE "
                        + APIUsageStatisticsClientConstants.TIME + " BETWEEN " +
                        "? AND ? AND " + filter +
                        " GROUP BY " + APIUsageStatisticsClientConstants.API + ','
                        + APIUsageStatisticsClientConstants.API_VERSION + ','
                        + APIUsageStatisticsClientConstants.USER_ID + ',' + APIUsageStatisticsClientConstants.VERSION
                        + ',' + APIUsageStatisticsClientConstants.API_PUBLISHER + ','
                        + APIUsageStatisticsClientConstants.CONTEXT + " ORDER BY "
                        + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + " DESC";
            } else {
                query = "SELECT " + APIUsageStatisticsClientConstants.API + ','
                        + APIUsageStatisticsClientConstants.API_VERSION + ','
                        + APIUsageStatisticsClientConstants.VERSION + ','
                        + APIUsageStatisticsClientConstants.API_PUBLISHER + ','
                        + APIUsageStatisticsClientConstants.USER_ID + ", SUM("
                        + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ") AS TOTAL_REQUEST_COUNT, "
                        + APIUsageStatisticsClientConstants.CONTEXT +
                        " FROM " + APIUsageStatisticsClientConstants.API_REQUEST_SUMMARY +
                        " WHERE " + filter +
                        " GROUP BY " + APIUsageStatisticsClientConstants.API + ','
                        + APIUsageStatisticsClientConstants.API_VERSION + ','
                        + APIUsageStatisticsClientConstants.API_PUBLISHER + ','
                        + APIUsageStatisticsClientConstants.USER_ID + " ORDER BY "
                        + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + " DESC ";
                oracleQuery = "SELECT " + APIUsageStatisticsClientConstants.API + ','
                        + APIUsageStatisticsClientConstants.API_VERSION + ','
                        + APIUsageStatisticsClientConstants.VERSION + ','
                        + APIUsageStatisticsClientConstants.API_PUBLISHER + ','
                        + APIUsageStatisticsClientConstants.USER_ID + ", SUM("
                        + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ") AS TOTAL_REQUEST_COUNT, "
                        + APIUsageStatisticsClientConstants.CONTEXT +
                        " FROM " + APIUsageStatisticsClientConstants.API_REQUEST_SUMMARY +
                        " WHERE " + filter +
                        " GROUP BY " + APIUsageStatisticsClientConstants.API + ','
                        + APIUsageStatisticsClientConstants.API_VERSION + ','
                        + APIUsageStatisticsClientConstants.VERSION + ','
                        + APIUsageStatisticsClientConstants.API_PUBLISHER + ','
                        + APIUsageStatisticsClientConstants.USER_ID + ',' + APIUsageStatisticsClientConstants.CONTEXT
                        + " ORDER BY " + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + " DESC ";
                msSqlQuery = "SELECT  " + APIUsageStatisticsClientConstants.API + ','
                        + APIUsageStatisticsClientConstants.API_VERSION + ','
                        + APIUsageStatisticsClientConstants.VERSION + ','
                        + APIUsageStatisticsClientConstants.API_PUBLISHER + ','
                        + APIUsageStatisticsClientConstants.USER_ID + ", SUM("
                        + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ") AS TOTAL_REQUEST_COUNT, "
                        + APIUsageStatisticsClientConstants.CONTEXT +
                        " FROM " + APIUsageStatisticsClientConstants.API_REQUEST_SUMMARY +
                        " WHERE " + filter +
                        " GROUP BY " + APIUsageStatisticsClientConstants.API + ','
                        + APIUsageStatisticsClientConstants.API_VERSION + ','
                        + APIUsageStatisticsClientConstants.API_PUBLISHER + ','
                        + APIUsageStatisticsClientConstants.USER_ID + " ORDER BY "
                        + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + " DESC ";
            }
            if ((connection.getMetaData().getDriverName()).contains("Oracle")) {
                query = oracleQuery;
            }
            if (connection.getMetaData().getDatabaseProductName().contains("Microsoft")) {
                query = msSqlQuery;
            }
            preparedStatement = connection.prepareStatement(query);
            if(query.contains("?")){
                preparedStatement.setString(1, fromDate);
                preparedStatement.setString(2, toDate);
            }

            rs = preparedStatement.executeQuery();
            List<APIUsageByUserName> usageByName = new ArrayList<APIUsageByUserName>();
            String apiName;
            String apiVersion;
            String context;
            String userID;
            long requestCount;
            String publisher;

            while (rs.next()) {
                apiName = rs.getString(APIUsageStatisticsClientConstants.API);
                apiVersion = rs.getString(APIUsageStatisticsClientConstants.VERSION);
                context = rs.getString("api");
                userID = rs.getString(APIUsageStatisticsClientConstants.USER_ID);
                requestCount = rs.getLong(APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT);
                publisher = rs.getString(APIUsageStatisticsClientConstants.API_PUBLISHER);
                if (publisher != null) {
                    APIUsageByUserName usage = new APIUsageByUserName(apiName, apiVersion, context, userID, publisher,
                            requestCount);
                    usageByName.add(usage);
                }
            }
            return usageByName;
        } catch (Exception e) {
            log.error("Error occurred while querying from JDBC database " + e.getMessage(), e);
            throw new APIMgtUsageQueryServiceClientException("Error occurred while querying from JDBC database", e);
        } finally {
            closeDatabaseLinks(rs, preparedStatement, connection);
        }
    }

    /**
     * This method find the existence of the table in given RDBMS
     * @param tableName Name of the table
     * @param connection Database connection
     * @return return boolean to indicate it's existence
     * @throws SQLException throws if database exception occurred
     */
    private boolean isTableExist(String tableName, Connection connection) throws SQLException {
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

    /**
     * This method find the list of API published by particular Pulisher
     * @param providerId Provider username
     * @return list of APIs
     * @throws APIMgtUsageQueryServiceClientException throws if error occurred
     */
    private List<API> getAPIsByProvider(String providerId) throws APIMgtUsageQueryServiceClientException {
        try {
            if (APIUsageStatisticsClientConstants.ALL_PROVIDERS.equals(providerId)) {
                return apiProviderImpl.getAllAPIs();
            } else {
                return apiProviderImpl.getAPIsByProvider(providerId);
            }
        } catch (APIManagementException e) {
            log.error("Error while retrieving APIs by " + providerId, e);
            throw new APIMgtUsageQueryServiceClientException("Error while retrieving APIs by " + providerId, e);
        }
    }

    /**
     * Not used in the current implementation
     *
     * @param param parameters
     * @param calls no of calls
     * @return list of APIUsageRangeCost
     * @throws Exception if error occured
     */
    @Override
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

    /**
     * This method find the first access time of the API
     * @param providerName provider name
     * @return APIFirstAccess
     * @throws APIMgtUsageQueryServiceClientException
     */
    @Override
    public List<APIFirstAccess> getFirstAccessTime(String providerName) throws APIMgtUsageQueryServiceClientException {
        APIFirstAccess firstAccess = this.queryFirstAccess(APIUsageStatisticsClientConstants.KEY_USAGE_SUMMARY);
        List<APIFirstAccess> APIFirstAccessList = new ArrayList<APIFirstAccess>();
        APIFirstAccess fTime;

        if (firstAccess != null) {
            fTime = new APIFirstAccess(firstAccess.getYear(), firstAccess.getMonth(), firstAccess.getDay());
            APIFirstAccessList.add(fTime);
        }
        return APIFirstAccessList;
    }

    /**
     * Finf first accesstime form the database
     * @param columnFamily table name in the RDBMS
     * @return APIFirstAccess represnting the time
     * @throws APIMgtUsageQueryServiceClientException throws if database error occurred
     */
    private APIFirstAccess queryFirstAccess(String columnFamily) throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            handleException("BAM data source hasn't been initialized. Ensure that the data source " +
                    "is properly configured in the APIUsageTracker configuration.");
        }

        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String query;
            if (connection.getMetaData().getDatabaseProductName().equalsIgnoreCase("oracle")) {
                query = "SELECT " + APIUsageStatisticsClientConstants.TIME + ','
                        + APIUsageStatisticsClientConstants.YEAR + ',' + APIUsageStatisticsClientConstants.MONTH + ','
                        + APIUsageStatisticsClientConstants.DAY + " FROM (SELECT "
                        + APIUsageStatisticsClientConstants.TIME + ',' + APIUsageStatisticsClientConstants.YEAR + ','
                        + APIUsageStatisticsClientConstants.MONTH + ',' + APIUsageStatisticsClientConstants.DAY
                        + " FROM " + columnFamily + " order by " + APIUsageStatisticsClientConstants.TIME
                        + " ASC) where ROWNUM <= 1";
            } else if (connection.getMetaData().getDatabaseProductName().contains("Microsoft")) {
                query = "SELECT TOP 1 " + APIUsageStatisticsClientConstants.TIME + ','
                        + APIUsageStatisticsClientConstants.YEAR + ',' + APIUsageStatisticsClientConstants.MONTH + ','
                        + APIUsageStatisticsClientConstants.DAY + " FROM  " + columnFamily + " order by "
                        + APIUsageStatisticsClientConstants.TIME + " ASC";
            } else if (connection.getMetaData().getDatabaseProductName().contains("DB2")) {
                query = "SELECT " + APIUsageStatisticsClientConstants.TIME + ','
                        + APIUsageStatisticsClientConstants.YEAR + ',' + APIUsageStatisticsClientConstants.MONTH + ','
                        + APIUsageStatisticsClientConstants.DAY + " FROM  " + columnFamily + " order by "
                        + APIUsageStatisticsClientConstants.TIME + " ASC FETCH FIRST 1 ROWS ONLY";
            } else {
                query = "SELECT " + APIUsageStatisticsClientConstants.TIME + ','
                        + APIUsageStatisticsClientConstants.YEAR + ',' + APIUsageStatisticsClientConstants.MONTH + ','
                        + APIUsageStatisticsClientConstants.DAY + " FROM  " + columnFamily + " order by "
                        + APIUsageStatisticsClientConstants.TIME + " ASC limit 1";
            }
            rs = statement.executeQuery(query);
            String year;
            String month;
            String day;
            APIFirstAccess firstAccess = null;
            while (rs.next()) {
                year = rs.getInt(APIUsageStatisticsClientConstants.YEAR) + "";
                month = rs.getInt(APIUsageStatisticsClientConstants.MONTH) - 1 + "";
                day = rs.getInt(APIUsageStatisticsClientConstants.DAY) + "";
                firstAccess = new APIFirstAccess(year, month, day);
            }

            return firstAccess;
        } catch (Exception e) {
            log.error("Error occurred while querying from JDBC database " + e.getMessage(), e);
            throw new APIMgtUsageQueryServiceClientException(
                    "Error occurred while querying from JDBC database" + e.getMessage(), e);
        } finally {
            closeDatabaseLinks(rs, statement, connection);
        }
    }

    /**
     * This method find the API usage
     *
     * @param apiName    API name
     * @param apiVersion API version
     * @return list of APIUsageByUser
     * @throws APIMgtUsageQueryServiceClientException throws if error occurred
     */
    private Collection<APIUsageByUser> getUsageOfAPI(String apiName, String apiVersion)
            throws APIMgtUsageQueryServiceClientException {
        if (dataSource == null) {
            handleException("BAM data source hasn't been initialized. Ensure that the data source " +
                    "is properly configured in the APIUsageTracker configuration.");
        }
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;
        Collection<APIUsageByUser> usageData = new ArrayList<APIUsageByUser>();
        try {
            connection = dataSource.getConnection();
            String query;
            //check whether table exist first
            if (isTableExist(APIUsageStatisticsClientConstants.KEY_USAGE_SUMMARY, connection)) {//Table Exists
                query = "SELECT * FROM " + APIUsageStatisticsClientConstants.KEY_USAGE_SUMMARY + " WHERE "
                        + APIUsageStatisticsClientConstants.API + " = ? ";
                if (apiVersion != null) {
                    query += " AND " + APIUsageStatisticsClientConstants.VERSION + " = ? ";
                }
                prepareStatement = connection.prepareStatement(query);
                prepareStatement.setString(1, apiName);
                if (apiVersion != null) {
                    prepareStatement.setString(2, apiVersion);
                }

                rs = prepareStatement.executeQuery();
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
            log.error("Error occurred while querying from JDBC database " + e.getMessage(), e);
            throw new APIMgtUsageQueryServiceClientException("Error occurred while querying from JDBC database", e);
        } finally {
            closeDatabaseLinks(rs,prepareStatement,connection);
        }
    }

    /**
     * Given API name and Application, returns throttling request counts over time for a given time span.
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
    @Override
    public List<APIThrottlingOverTimeDTO> getThrottleDataOfAPIAndApplication(String apiName, String provider,
            String appName, String fromDate, String toDate, String groupBy)
            throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            handleException("BAM data source hasn't been initialized. Ensure that the data source " +
                    "is properly configured in the APIUsageTracker configuration.");
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

                groupByStmt =
                        APIUsageStatisticsClientConstants.YEAR + ',' + APIUsageStatisticsClientConstants.MONTH + ','
                                + APIUsageStatisticsClientConstants.DAY;
                query = "SELECT " + groupByStmt + " ," +
                        "SUM(COALESCE(" + APIUsageStatisticsClientConstants.SUCCESS_REQUEST_COUNT
                        + ",0)) AS success_request_count, " +
                        "SUM(COALESCE(" + APIUsageStatisticsClientConstants.THROTTLED_OUT_COUNT
                        + ",0)) AS throttleout_count " +
                        "FROM " + APIUsageStatisticsClientConstants.API_THROTTLED_OUT_SUMMARY +
                        " WHERE " + APIUsageStatisticsClientConstants.TENANT_DOMAIN + " = ? " +
                        "AND " + APIUsageStatisticsClientConstants.API + " = ? " +
                        (provider.startsWith(APIUsageStatisticsClientConstants.ALL_PROVIDERS) ?
                                "" :
                                "AND " + APIUsageStatisticsClientConstants.API_PUBLISHER + " = ?") +
                        (StringUtils.isEmpty(appName) ?
                                "" :
                                " AND " + APIUsageStatisticsClientConstants.APPLICATION_NAME + " = ?") +
                        " AND " + APIUsageStatisticsClientConstants.TIME + " BETWEEN ? AND ? " +
                        "GROUP BY " + groupByStmt +
                        " ORDER BY " + groupByStmt + " ASC";

                preparedStatement = connection.prepareStatement(query);
                int index = 1;
                preparedStatement.setString(index++, tenantDomain);
                preparedStatement.setString(index++, apiName);
                if (!provider.startsWith(APIUsageStatisticsClientConstants.ALL_PROVIDERS)) {
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
                handleException("Statistics Table:" + APIUsageStatisticsClientConstants.API_THROTTLED_OUT_SUMMARY +
                        " does not exist.");
            }
            return throttlingData;
        } catch (SQLException e) {
            log.error("Error occurred while querying from JDBC database " + e.getMessage(), e);
            throw new APIMgtUsageQueryServiceClientException("Error occurred while querying from JDBC database", e);
        } finally {
            closeDatabaseLinks(rs, preparedStatement, connection);
        }
    }

    /**
     * Given Application name and the provider, returns throttle data for the APIs of the provider invoked by the
     * given application.
     *
     * @param appName  Application name
     * @param provider Provider name
     * @param fromDate Start date of the time span
     * @param toDate   End date of time span
     * @return Throttling counts of APIs of the provider invoked by the given app
     * @throws APIMgtUsageQueryServiceClientException
     */
    @Override
    public List<APIThrottlingOverTimeDTO> getThrottleDataOfApplication(String appName, String provider, String fromDate,
            String toDate) throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            handleException("BAM data source hasn't been initialized. Ensure that the data source " +
                    "is properly configured in the APIUsageTracker configuration.");
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

                query = "SELECT " + APIUsageStatisticsClientConstants.API + ','
                        + APIUsageStatisticsClientConstants.API_PUBLISHER + ',' + " SUM(COALESCE("
                        + APIUsageStatisticsClientConstants.SUCCESS_REQUEST_COUNT + ",0)) " +
                        "AS success_request_count, SUM(COALESCE("
                        + APIUsageStatisticsClientConstants.THROTTLED_OUT_COUNT + ",0)) as throttleout_count " +
                        "FROM " + APIUsageStatisticsClientConstants.API_THROTTLED_OUT_SUMMARY +
                        " WHERE " + APIUsageStatisticsClientConstants.TENANT_DOMAIN + " = ? " +
                        "AND " + APIUsageStatisticsClientConstants.APPLICATION_NAME + " = ? " +
                        (provider.startsWith(APIUsageStatisticsClientConstants.ALL_PROVIDERS) ?
                                "" :
                                "AND " + APIUsageStatisticsClientConstants.API_PUBLISHER + " = ?") +
                        "AND " + APIUsageStatisticsClientConstants.TIME + " BETWEEN ? AND ? " +
                        "GROUP BY " + APIUsageStatisticsClientConstants.API + ','
                        + APIUsageStatisticsClientConstants.API_PUBLISHER +
                        " ORDER BY api ASC";

                preparedStatement = connection.prepareStatement(query);
                int index = 1;
                preparedStatement.setString(index++, tenantDomain);
                preparedStatement.setString(index++, appName);
                if (!provider.startsWith(APIUsageStatisticsClientConstants.ALL_PROVIDERS)) {
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
                handleException("Statistics Table:" + APIUsageStatisticsClientConstants.API_THROTTLED_OUT_SUMMARY +
                        " does not exist.");
            }
            return throttlingData;
        } catch (SQLException e) {
            log.error("Error occurred while querying from JDBC database " + e.getMessage(), e);
            throw new APIMgtUsageQueryServiceClientException("Error occurred while querying from JDBC database", e);
        } finally {
            closeDatabaseLinks(rs, preparedStatement, connection);
        }
    }

    /**
     * Get APIs of the provider that consist of throttle data.
     *
     * @param provider Provider name
     * @return List of APIs of the provider that consist of throttle data
     * @throws APIMgtUsageQueryServiceClientException
     */
    @Override
    public List<String> getAPIsForThrottleStats(String provider) throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            handleException("BAM data source hasn't been initialized. Ensure that the data source " +
                    "is properly configured in the APIUsageTracker configuration.");
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

                query = "SELECT DISTINCT " + APIUsageStatisticsClientConstants.API + " FROM "
                        + APIUsageStatisticsClientConstants.API_THROTTLED_OUT_SUMMARY +
                        " WHERE " + APIUsageStatisticsClientConstants.TENANT_DOMAIN + " = ? " +
                        (provider.startsWith(APIUsageStatisticsClientConstants.ALL_PROVIDERS) ?
                                "" :
                                "AND " + APIUsageStatisticsClientConstants.API_PUBLISHER + " = ? ") +
                        "ORDER BY " + APIUsageStatisticsClientConstants.API + " ASC";
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
                handleException("Statistics Table:" + APIUsageStatisticsClientConstants.API_THROTTLED_OUT_SUMMARY +
                        " does not exist.");
            }
            return throttlingAPIData;
        } catch (SQLException e) {
            log.error("Error occurred while querying from JDBC database " + e.getMessage(), e);
            throw new APIMgtUsageQueryServiceClientException("Error occurred while querying from JDBC database", e);
        } finally {
            closeDatabaseLinks(rs, preparedStatement, connection);
        }
    }

    /**
     * Given provider name and the API name, returns a list of applications through which the corresponding API is
     * invoked and which consist of success/throttled requests.
     *
     * @param provider Provider name
     * @param apiName  Name of th API
     * @return A list of applications through which the corresponding API is invoked and which consist of throttle data
     * @throws APIMgtUsageQueryServiceClientException
     */
    @Override
    public List<String> getAppsForThrottleStats(String provider, String apiName)
            throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            handleException("BAM data source hasn't been initialized. Ensure that the data source " +
                    "is properly configured in the APIUsageTracker configuration.");
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
                query = "SELECT DISTINCT " + APIUsageStatisticsClientConstants.APPLICATION_NAME + " FROM "
                        + APIUsageStatisticsClientConstants.API_THROTTLED_OUT_SUMMARY +
                        " WHERE " + APIUsageStatisticsClientConstants.TENANT_DOMAIN + " = ? " +
                        (provider.startsWith(APIUsageStatisticsClientConstants.ALL_PROVIDERS) ?
                                "" :
                                "AND " + APIUsageStatisticsClientConstants.API_PUBLISHER + " = ? ") +
                        (apiName == null ? "" : "AND " + APIUsageStatisticsClientConstants.API + " = ? ") +
                        "ORDER BY " + APIUsageStatisticsClientConstants.APPLICATION_NAME + " ASC";

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
                handleException("Statistics Table:" + APIUsageStatisticsClientConstants.API_THROTTLED_OUT_SUMMARY +
                        " does not exist.");
            }
            return throttlingAppData;
        } catch (SQLException e) {
            log.error("Error occurred while querying from JDBC database " + e.getMessage(), e);
            throw new APIMgtUsageQueryServiceClientException("Error occurred while querying from JDBC database", e);
        } finally {
            closeDatabaseLinks(rs, preparedStatement, connection);
        }
    }

    /**
     * return a string to indicate type of statistics client
     *
     * @return String
     */
    @Override
    public String getClientType() {
        return APIUsageStatisticsClientConstants.RDBMS_STATISTICS_CLIENT_TYPE;
    }

    @Override
    public List<Result<ExecutionTimeOfAPIValues>> getExecutionTimeByAPI(String apiName, String version, String
            tenantDomain, String fromDate, String toDate, String drillDown) throws
            APIMgtUsageQueryServiceClientException {

        return getExecutionTimeByAPI(apiName, version, tenantDomain, fromDate, toDate, drillDown, "ALL");
    }

    @Override
    public List<Result<ExecutionTimeOfAPIValues>> getExecutionTimeByAPI(String apiName, String version, String
            tenantDomain, String fromDate, String toDate, String drillDown, String mediationType) throws
            APIMgtUsageQueryServiceClientException {
        if (dataSource == null) {
            handleException("BAM data source hasn't been initialized. Ensure that the data source " +
                    "is properly configured in the APIUsageTracker configuration.");
        }
        List<Result<ExecutionTimeOfAPIValues>> result = new ArrayList<Result<ExecutionTimeOfAPIValues>>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        try {
            boolean isVersionSet = false;
            boolean isTenantSet = false;
            boolean isMediationTypeSet = false;
            connection = dataSource.getConnection();
            StringBuilder query = new StringBuilder("SELECT * FROM ");
            String tableName = getExecutionTimeTableByView(drillDown);
            query.append(tableName).append(" WHERE api = ?");
            if (version != null) {
                query.append(" AND ").append(APIUsageStatisticsClientConstants.VERSION).append("= ?");
                isVersionSet = true;
            }
            if (tenantDomain != null) {
                query.append(" AND ").append(APIUsageStatisticsClientConstants.TENANT_DOMAIN).append("= ?");
                isTenantSet = true;
            }
            if (fromDate != null && toDate != null) {
                try {
                    query.append(" AND ").append(getDateToLong(fromDate)).append(" <= ").append(" " +
                            "" + APIUsageStatisticsClientConstants.TIME + " ").append(" AND ").append(" " +
                            "" + APIUsageStatisticsClientConstants.TIME + " ").append("<=").append(getDateToLong
                            (toDate));
                } catch (ParseException e) {
                    handleException("Error occurred while Error parsing date", e);
                }
            }
            if (mediationType != null && mediationType != "ALL") {
                query.append(" AND ").append(APIUsageStatisticsClientConstants.MEDIATION).append(" = ?");
                isMediationTypeSet = true;
            }
            if (isTableExist(tableName, connection)) { //Tables exist

                // dynamically set parameters to prepared statement according to what is appended before
                int counter = 2;
                preparedStatement = connection.prepareStatement(query.toString());
                preparedStatement.setString(1, apiName);
                if (isVersionSet) {
                    preparedStatement.setString(counter, version);
                    counter++;
                }
                if (isTenantSet) {
                    preparedStatement.setString(counter, tenantDomain);
                    counter++;
                }
                if (isMediationTypeSet) {
                    preparedStatement.setString(counter, mediationType);
                }
                rs = preparedStatement.executeQuery();
                int hour = 0;
                int minute = 0;
                int seconds = 0;
                while (rs.next()) {
                    if ("HOUR".equals(drillDown)) {
                        hour = rs.getInt(APIUsageStatisticsClientConstants.HOUR);
                    } else if ("MINUTES".equals(drillDown)) {
                        hour = rs.getInt(APIUsageStatisticsClientConstants.HOUR);
                        minute = rs.getInt(APIUsageStatisticsClientConstants.MINUTES);
                    } else if ("SECONDS".equals(drillDown)) {
                        hour = rs.getInt(APIUsageStatisticsClientConstants.HOUR);
                        minute = rs.getInt(APIUsageStatisticsClientConstants.MINUTES);
                        seconds = rs.getInt(APIUsageStatisticsClientConstants.SECONDS);
                    }
                    Result<ExecutionTimeOfAPIValues> result1 = new Result<ExecutionTimeOfAPIValues>();
                    ExecutionTimeOfAPIValues executionTimeOfAPIValues = new ExecutionTimeOfAPIValues();
                    executionTimeOfAPIValues.setApi(rs.getString(APIUsageStatisticsClientConstants.API));
                    executionTimeOfAPIValues.setContext(rs.getString(APIUsageStatisticsClientConstants.CONTEXT));
                    executionTimeOfAPIValues.setApiPublisher(rs.getString(APIUsageStatisticsClientConstants
                            .API_PUBLISHER));
                    executionTimeOfAPIValues.setVersion(rs.getString(APIUsageStatisticsClientConstants.VERSION));
                    executionTimeOfAPIValues.setYear(rs.getInt(APIUsageStatisticsClientConstants.YEAR));
                    executionTimeOfAPIValues.setMonth(rs.getInt(APIUsageStatisticsClientConstants.MONTH));
                    executionTimeOfAPIValues.setDay(rs.getInt(APIUsageStatisticsClientConstants.DAY));
                    executionTimeOfAPIValues.setHour(hour);
                    executionTimeOfAPIValues.setMinutes(minute);
                    executionTimeOfAPIValues.setSeconds(seconds);
                    executionTimeOfAPIValues.setApiResponseTime(rs.getLong(APIUsageStatisticsClientConstants
                            .API_RESPONSE_TIME));
                    executionTimeOfAPIValues.setSecurityLatency(rs.getLong(APIUsageStatisticsClientConstants
                            .SECURITY_LATENCY));
                    executionTimeOfAPIValues.setThrottlingLatency(rs.getLong(APIUsageStatisticsClientConstants.
                            THROTTLING_LATENCY));
                    executionTimeOfAPIValues.setRequestMediationLatency(rs.getLong(APIUsageStatisticsClientConstants.
                            REQ_MEDIATION_LATENCY));
                    executionTimeOfAPIValues.setResponseMediationLatency(rs.getLong(APIUsageStatisticsClientConstants.
                            RES_MEDIATION_LATENCY));
                    executionTimeOfAPIValues.setBackendLatency(rs.getLong(APIUsageStatisticsClientConstants.
                            BACKEND_LATENCY));
                    executionTimeOfAPIValues.setOtherLatency(rs.getLong(APIUsageStatisticsClientConstants.
                            OTHER_LATENCY));
                    result1.setValues(executionTimeOfAPIValues);
                    result1.setTableName(tableName);
                    result1.setTimestamp(RestClientUtil.longToDate(new Date().getTime()));
                    result.add(result1);
                }
            } else {
                handleException("Statistics Table:" + tableName + " does not exist.");
            }
            if (!result.isEmpty()) {
                insertZeroElementsAndSort(result, drillDown, getDateToLong(fromDate), getDateToLong(toDate));
            }
        } catch (SQLException e) {
            handleException("Error occurred while querying from JDBC database", e);
        } catch (ParseException e) {
            handleException("Couldn't parse the date", e);
        } finally {
            closeDatabaseLinks(rs, preparedStatement, connection);
        }
        return result;
    }

    @Override
    public List<Result<PerGeoLocationUsageCount>> getGeoLocationsByApi(String apiName, String version, String
            tenantDomain, String fromDate, String toDate, String drillDown) throws
            APIMgtUsageQueryServiceClientException {
        if (dataSource == null) {
            handleException("BAM data source hasn't been initialized. Ensure that the data source " +
                    "is properly configured in the APIUsageTracker configuration.");
        }
        List<Result<PerGeoLocationUsageCount>> result = new ArrayList<Result<PerGeoLocationUsageCount>>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        try {
            boolean isVersionSet = false;
            boolean isTenantSet = false;
            boolean isDrilldownSet = false;
            connection = dataSource.getConnection();
            StringBuilder query = new StringBuilder("SELECT sum(total_request_count) as count,country ");
            if (!"ALL".equals(drillDown)) {
                query.append(",city " );
            }
            query.append("FROM ");
            String tableName = APIUsageStatisticsClientConstants.API_REQUEST_GEO_LOCATION_SUMMARY;
            query.append(tableName).append(" WHERE api= ?");
            if (version != null && !"ALL".equals(version)) {
                query.append(" AND ").append(APIUsageStatisticsClientConstants.VERSION).append("= ?");
                isVersionSet = true;
            }
            if (tenantDomain != null) {
                query.append(" AND ").append(APIUsageStatisticsClientConstants.TENANT_DOMAIN).append("= ?");
                isTenantSet = true;
            }
            if (fromDate != null && toDate != null) {
                try {
                    query.append(" AND ").append(getDateToLong(fromDate)).append(" <= ").append("requestTime")
                            .append(" AND ").append(" requestTime ").append("<=").append(getDateToLong(toDate));
                } catch (ParseException e) {
                    handleException("Error occurred while Error parsing date", e);
                }
            }
            if (!"ALL".equals(drillDown)) {
                query.append(" AND country = ?");
                isDrilldownSet = true;
            }
            query.append(" GROUP BY country ");
            if (!"ALL".equals(drillDown)) {
                query.append(",city");
            }
            if (isTableExist(tableName, connection)) { //Tables exist

                // dynamically set parameters to prepared statement according to what is appended before
                int counter = 2;
                preparedStatement = connection.prepareStatement(query.toString());
                preparedStatement.setString(1, apiName);
                if (isVersionSet) {
                    preparedStatement.setString(counter, version);
                    counter++;
                }
                if (isTenantSet) {
                    preparedStatement.setString(counter, tenantDomain);
                    counter++;
                }
                if (isDrilldownSet) {
                    preparedStatement.setString(counter, drillDown);
                }

                rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    Result<PerGeoLocationUsageCount> result1 = new Result<PerGeoLocationUsageCount>();
                    int count = rs.getInt("count");
                    String country1 = rs.getString("country");
                    List<String> facetValues = new ArrayList<String>();
                    facetValues.add(country1);
                    if (!"ALL".equals(drillDown)) {
                        String city = rs.getString("city");
                        facetValues.add(city);
                    }
                    PerGeoLocationUsageCount perGeoLocationUsageCount = new PerGeoLocationUsageCount(count,
                            facetValues);
                    result1.setValues(perGeoLocationUsageCount);
                    result1.setTableName(tableName);
                    result1.setTimestamp(RestClientUtil.longToDate(new Date().getTime()));
                    result.add(result1);
                }
            } else {
                handleException("Statistics Table:" + tableName + " does not exist.");
            }
        } catch (SQLException e) {
            handleException("Error occurred while querying from JDBC database", e);
        } finally {
            closeDatabaseLinks(rs, preparedStatement, connection);
        }
        return result;

    }

    @Override
    public List<Result<UserAgentUsageCount>> getUserAgentUsageByAPI(String apiName, String version, String
            tenantDomain, String fromDate, String toDate, String drillDown) throws APIMgtUsageQueryServiceClientException {
        if (dataSource == null) {
            handleException("DAS data source hasn't been initialized. Ensure that the data source " +
                    "is properly configured in the APIUsageTracker configuration.");
        }
        List<Result<UserAgentUsageCount>> result = new ArrayList<Result<UserAgentUsageCount>>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        try {
            boolean isVersionSet = false;
            boolean isTenantSet = false;
            boolean isDrilldownSet = false;
            connection = dataSource.getConnection();
            StringBuilder query = new StringBuilder("SELECT sum(total_request_count) as count,os,browser " +
                    "FROM ");
            String tableName = APIUsageStatisticsClientConstants.API_REQUEST_USER_BROWSER_SUMMARY;
            query.append(tableName).append(" WHERE api= ?");
            if (version != null && !"ALL".equals(version)) {
                query.append(" AND ").append(APIUsageStatisticsClientConstants.VERSION).append("= ?");
                isVersionSet = true;
            }
            if (tenantDomain != null) {
                query.append(" AND ").append(APIUsageStatisticsClientConstants.TENANT_DOMAIN).append("= ?");
                isTenantSet = true;
            }
            if (fromDate != null && toDate != null) {
                try {
                    query.append(" AND ").append(getDateToLong(fromDate)).append(" <= ").append("requestTime")
                            .append(" AND ").append(" requestTime ").append("<=").append(getDateToLong(toDate));
                } catch (ParseException e) {
                    handleException("Error occurred while Error parsing date", e);
                }
            }
            if (!"ALL".equals(drillDown)) {
                query.append(" AND os = ?");
                isDrilldownSet = true;
            }
            query.append(" GROUP BY os, browser ");

            if (isTableExist(tableName, connection)) { //Tables exist

                // dynamically set parameters to prepared statement according to what is appended before
                int counter = 2;
                preparedStatement = connection.prepareStatement(query.toString());
                preparedStatement.setString(1, apiName);
                if (isVersionSet) {
                    preparedStatement.setString(counter, version);
                    counter++;
                }
                if (isTenantSet) {
                    preparedStatement.setString(counter, tenantDomain);
                    counter++;
                }
                if (isDrilldownSet) {
                    preparedStatement.setString(counter, drillDown);
                }

                rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    Result<UserAgentUsageCount> result1 = new Result<UserAgentUsageCount>();
                    int count = rs.getInt("count");
                    String country1 = rs.getString("os");
                    String city = rs.getString("browser");
                    List<String> facetValues = new ArrayList<String>();
                    facetValues.add(country1);
                    facetValues.add(city);
                    UserAgentUsageCount perGeoLocationUsageCount = new UserAgentUsageCount(count,
                            facetValues);
                    result1.setValues(perGeoLocationUsageCount);
                    result1.setTableName(tableName);
                    result1.setTimestamp(RestClientUtil.longToDate(new Date().getTime()));
                    result.add(result1);
                }
            } else {
                handleException("Statistics Table:" + tableName + " does not exist.");
            }
        } catch (SQLException e) {
            handleException("Error occurred while querying from JDBC database", e);
        } finally {
            closeDatabaseLinks(rs, preparedStatement, connection);
        }
        return result;
    }
}