/*
* Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
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
import org.wso2.carbon.apimgt.usage.client.util.RestClientUtil;
import org.wso2.carbon.application.mgt.stub.upload.CarbonAppUploaderStub;
import org.wso2.carbon.application.mgt.stub.upload.types.carbon.UploadedFileItem;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.activation.DataHandler;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Usage statistics class implementation for the APIUsageStatisticsClient.
 * Use the Siddhi Rest API to query and fetch the data for getting usage Statistics
 */
public class APIUsageStatisticsRestClientImpl extends APIUsageStatisticsClient {

    private static PaymentPlan paymentPlan;
    private APIProvider apiProviderImpl;
    private static final Log log = LogFactory.getLog(APIUsageStatisticsRestClientImpl.class);

    /**
     * default constructor
     */
    public APIUsageStatisticsRestClientImpl() {
    }

    /**
     * Initialize the Rest client with logged user
     *
     * @param username logged user ID
     * @throws APIMgtUsageQueryServiceClientException throws when error occurred
     */
    public APIUsageStatisticsRestClientImpl(String username) throws APIMgtUsageQueryServiceClientException {
        OMElement element;
        APIManagerConfiguration config;
        APIManagerAnalyticsConfiguration apiManagerAnalyticsConfiguration;
        try {
            config = APIUsageClientServiceComponent.getAPIManagerConfiguration();
            apiManagerAnalyticsConfiguration = APIManagerAnalyticsConfiguration.getInstance();
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
     * This method Initialises the datasource
     *
     * @throws APIMgtUsageQueryServiceClientException throws if error occurred
     */
    @Override
    public void initializeDataSource() throws APIMgtUsageQueryServiceClientException {
        //do nothing
    }

    /**
     * This method read XML content from the given stream
     *
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
     * This methods return the api invocation fault count data per applications
     *
     * @param subscriberName subscriber name
     * @param groupId        group id of the subscriber
     * @param fromDate       starting date
     * @param toDate         ending date
     * @param limit          limit of the result
     * @return list of fault count data
     * @throws APIMgtUsageQueryServiceClientException throws when error occurred
     */
    @Override
    public List<FaultCountDTO> getPerAppAPIFaultCount(String subscriberName, String groupId, String fromDate,
            String toDate, int limit) throws APIMgtUsageQueryServiceClientException {

        List<String> subscriberApps = getAppsAndIdsBySubscriber(subscriberName, groupId);
        return getFaultAppUsageData(APIUsageStatisticsClientConstants.API_FAULTY_INVOCATION_AGG,
                subscriberApps, fromDate, toDate, limit);
    }

    /**
     * this method return the top users for the list of applications.
     *
     * @param subscriberName subscriber name
     * @param groupId        group id of the subscriber
     * @param fromDate       starting date
     * @param toDate         ending date
     * @param limit          limit of the result
     * @return list of AppUsageDTO
     * @throws APIMgtUsageQueryServiceClientException
     */
    @Override
    public List<AppUsageDTO> getTopAppUsers(String subscriberName, String groupId, String fromDate, String toDate,
            int limit) throws APIMgtUsageQueryServiceClientException {

        List<String> subscriberApps = getAppsAndIdsBySubscriber(subscriberName, groupId);
        return getTopAppUsageData(APIUsageStatisticsClientConstants.API_USER_PER_APP_AGG,
                subscriberApps, fromDate, toDate, limit);
    }

    /**
     * This method gets the app usage data for invoking APIs
     *
     * @param tableName name of the required table in the database
     * @param idList    Id list of applications
     * @return a collection containing the data related to App usage
     * @throws APIMgtUsageQueryServiceClientException if an error occurs while querying the database
     */
    private List<AppUsageDTO> getTopAppUsageData(String tableName, List<String> idList, String fromDate, String toDate,
            int limit) throws APIMgtUsageQueryServiceClientException {
        List<AppUsageDTO> topAppUsageDataList = new ArrayList<AppUsageDTO>();
        try {
            if (!idList.isEmpty()) {
                String startDate = fromDate + ":00";
                String endDate = toDate + ":00";
                String granularity = APIUsageStatisticsClientConstants.HOURS_GRANULARITY;//default granularity

                Map<String, Integer> durationBreakdown = this.getDurationBreakdown(startDate, endDate);

                if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_YEARS) > 0) {
                    granularity = APIUsageStatisticsClientConstants.YEARS_GRANULARITY;
                } else if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_MONTHS) > 0) {
                    granularity = APIUsageStatisticsClientConstants.MONTHS_GRANULARITY;
                } else if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_DAYS) > 0) {
                    granularity = APIUsageStatisticsClientConstants.DAYS_GRANULARITY;
                }
                StringBuilder idListQuery = new StringBuilder();
                for (int i = 0; i < idList.size(); i++) {
                    if (i > 0) {
                        idListQuery.append(" or ");
                    }
                    idListQuery.append(APIUsageStatisticsClientConstants.APPLICATION_ID + "==");
                    idListQuery.append("'" + idList.get(i) + "'");
                }
                StringBuilder query = new StringBuilder(
                        "from " + tableName + " on " + idListQuery.toString() + " within " + getTimestamp(startDate)
                                + "L, " + getTimestamp(endDate) + "L per '" + granularity + "' select "
                                + APIUsageStatisticsClientConstants.APPLICATION_ID + ", "
                                + APIUsageStatisticsClientConstants.USERNAME + ", sum("
                                + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT
                                + ") as net_total_requests group by " + APIUsageStatisticsClientConstants.APPLICATION_ID
                                + ", " + APIUsageStatisticsClientConstants.USERNAME
                                + " order by net_total_requests DESC");
                // limit enforced
                if (limit >= 0) {
                    query.append(" limit" + limit);
                }
                query.append(";");
                JSONObject jsonObj = APIUtil
                        .executeQueryOnStreamProcessor(APIUsageStatisticsClientConstants.APIM_ACCESS_SUMMARY_SIDDHI_APP,
                                query.toString());
                String applicationId;
                String username;
                long requestCount;
                AppUsageDTO appUsageDTO;
                if (jsonObj != null) {
                    JSONArray jArray = (JSONArray) jsonObj.get(APIUsageStatisticsClientConstants.RECORDS_DELIMITER);
                    for (Object record : jArray) {
                        JSONArray recordArray = (JSONArray) record;
                        if (recordArray.size() == 3) {
                            applicationId = (String) recordArray.get(0);
                            username = (String) recordArray.get(1);
                            requestCount = (Long) recordArray.get(2);
                            String appName = subscriberAppsMap.get(applicationId);
                            boolean found = false;
                            for (AppUsageDTO dto : topAppUsageDataList) {
                                if (dto.getAppName().equals(appName)) {
                                    dto.addToUserCountArray(username, requestCount);
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                appUsageDTO = new AppUsageDTO();
                                appUsageDTO.setAppName(appName);
                                appUsageDTO.addToUserCountArray(username, requestCount);
                                topAppUsageDataList.add(appUsageDTO);
                            }
                        }
                    }
                }
            }
        } catch (APIManagementException e) {
            handleException("Error occurred while querying top app usage data from Stream Processor ", e);
        }
        return topAppUsageDataList;
    }

    /**
     * this method return the top users for the for the provided API.
     *
     * @param apiName  API name
     * @param version  version of the required API
     * @param fromDate Start date of the time span
     * @param toDate   End date of time span
     * @param start    starting index of the result
     * @param limit    number of results to return
     * @return a collection containing the data related to Api usage
     * @throws APIMgtUsageQueryServiceClientException
     */
    @Override
    public ApiTopUsersListDTO getTopApiUsers(String apiName, String version, String tenantDomain, String fromDate,
            String toDate, int start, int limit) throws APIMgtUsageQueryServiceClientException {
        List<ApiTopUsersDTO> tenantFilteredTopUsersDTOs = getTopApiUsers(
                APIUsageStatisticsClientConstants.API_USER_PER_APP_AGG, apiName, tenantDomain, version, fromDate,
                toDate);

        //filter based on pagination
        List<ApiTopUsersDTO> paginationFilteredTopUsersDTOs = new ArrayList<ApiTopUsersDTO>();
        ApiTopUsersListDTO apiTopUsersListDTO = new ApiTopUsersListDTO();
        int end = (start + limit) <= tenantFilteredTopUsersDTOs.size() ?
                (start + limit) :
                tenantFilteredTopUsersDTOs.size();
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
     * @param apiName   API name
     * @param version   version of the required API
     * @param fromDate  Start date of the time span
     * @param toDate    End date of time span
     * @return a collection containing the data related to Api usage
     * @throws APIMgtUsageQueryServiceClientException if an error occurs while querying the database
     */
    private List<ApiTopUsersDTO> getTopApiUsers(String tableName, String apiName, String tenantDomain, String version,
            String fromDate, String toDate) throws APIMgtUsageQueryServiceClientException {
        List<ApiTopUsersDTO> apiTopUsersDataList = new ArrayList<ApiTopUsersDTO>();
        try {
            StringBuilder topApiUserQuery;
            long totalRequestCount = getTotalRequestCountOfAPIVersion(tableName, apiName, tenantDomain, version,
                    fromDate, toDate);

            String granularity = APIUsageStatisticsClientConstants.HOURS_GRANULARITY;//default granularity

            Map<String, Integer> durationBreakdown = this.getDurationBreakdown(fromDate, toDate);

            if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_YEARS) > 0) {
                granularity = APIUsageStatisticsClientConstants.YEARS_GRANULARITY;
            } else if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_MONTHS) > 0) {
                granularity = APIUsageStatisticsClientConstants.MONTHS_GRANULARITY;
            } else if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_DAYS) > 0) {
                granularity = APIUsageStatisticsClientConstants.DAYS_GRANULARITY;
            }

            topApiUserQuery = new StringBuilder(
                    "from " + APIUsageStatisticsClientConstants.API_USER_PER_APP_AGG + " on");

            if (!APIUsageStatisticsClientConstants.FOR_ALL_API_VERSIONS.equals(version)) {
                topApiUserQuery.append("(" + APIUsageStatisticsClientConstants.API_NAME + "=='" + apiName + "' AND "
                        + APIUsageStatisticsClientConstants.API_VERSION + "=='" + version + "') ");
            } else {
                topApiUserQuery.append(" " + APIUsageStatisticsClientConstants.API_NAME + "=='" + apiName + "' ");
            }

            topApiUserQuery
                    .append("within " + getTimestamp(fromDate) + "L, " + getTimestamp(toDate) + "L per '" + granularity
                            + "' select " + APIUsageStatisticsClientConstants.USERNAME + ", "
                            + APIUsageStatisticsClientConstants.API_CREATOR + ", sum("
                            + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT
                            + ") as net_total_requests group by " + APIUsageStatisticsClientConstants.USERNAME + ", "
                            + APIUsageStatisticsClientConstants.API_CREATOR + " order by net_total_requests DESC;");

            JSONObject jsonObj = APIUtil
                    .executeQueryOnStreamProcessor(APIUsageStatisticsClientConstants.APIM_ACCESS_SUMMARY_SIDDHI_APP,
                            topApiUserQuery.toString());

            String username;
            Long requestCount;
            if (jsonObj != null) {
                JSONArray jArray = (JSONArray) jsonObj.get(APIUsageStatisticsClientConstants.RECORDS_DELIMITER);
                for (Object record : jArray) {
                    JSONArray recordArray = (JSONArray) record;
                    if (recordArray.size() == 3) {
                        String creator = (String) recordArray.get(1);
                        if (creator != null && MultitenantUtils.getTenantDomain(creator).equals(tenantDomain)) {
                            username = (String) recordArray.get(0);
                            requestCount = (Long) recordArray.get(2);
                            ApiTopUsersDTO apiTopUsersDTO = new ApiTopUsersDTO();
                            apiTopUsersDTO.setApiName(apiName);
                            apiTopUsersDTO.setFromDate(fromDate);
                            apiTopUsersDTO.setToDate(toDate);
                            apiTopUsersDTO.setVersion(version);
                            apiTopUsersDTO.setProvider(creator);

                            //remove @carbon.super from super tenant users
                            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME
                                    .equals(MultitenantUtils.getTenantDomain(username))) {
                                username = MultitenantUtils.getTenantAwareUsername(username);
                            }
                            apiTopUsersDTO.setUser(username);
                            apiTopUsersDTO.setRequestCount(requestCount);
                            apiTopUsersDTO.setTotalRequestCount(totalRequestCount);
                            apiTopUsersDataList.add(apiTopUsersDTO);
                        }
                    }
                }
            }
        } catch (APIManagementException e) {
            handleException("Error occurred while querying top api users data from Stream Processor ", e);
        }
        return apiTopUsersDataList;
    }

    /**
     * This method gets the API faulty invocation data
     *
     * @param tableName name of the required table in the database
     * @param idList    Ids List of applications
     * @return a collection containing the data related to API faulty invocations
     * @throws APIMgtUsageQueryServiceClientException if an error occurs while querying the database
     */
    private List<FaultCountDTO> getFaultAppUsageData(String tableName, List<String> idList, String fromDate,
            String toDate, int limit) throws APIMgtUsageQueryServiceClientException {
        List<FaultCountDTO> falseAppUsageDataList = new ArrayList<FaultCountDTO>();
        try {
            if (!idList.isEmpty()) {
                String startDate = fromDate + ":00";
                String endDate = toDate + ":00";
                String granularity = APIUsageStatisticsClientConstants.MINUTES_GRANULARITY;//default granularity

                Map<String, Integer> durationBreakdown = this.getDurationBreakdown(startDate, endDate);

                if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_YEARS) > 0) {
                    granularity = APIUsageStatisticsClientConstants.MONTHS_GRANULARITY;
                } else if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_MONTHS) > 0
                        || durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_WEEKS) > 0) {
                    granularity = APIUsageStatisticsClientConstants.DAYS_GRANULARITY;
                } else if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_DAYS) > 0) {
                    granularity = APIUsageStatisticsClientConstants.HOURS_GRANULARITY;
                }
                StringBuilder idListQuery = new StringBuilder();
                for (int i = 0; i < idList.size(); i++) {
                    if (i > 0) {
                        idListQuery.append(" or ");
                    }
                    idListQuery.append(APIUsageStatisticsClientConstants.APPLICATION_ID + "==");
                    idListQuery.append("'" + idList.get(i) + "'");
                }
                String query =
                        "from " + tableName + " on " + idListQuery.toString() + " within " + getTimestamp(startDate)
                                + "L, " + getTimestamp(endDate) + "L per '" + granularity + "' select "
                                + APIUsageStatisticsClientConstants.APPLICATION_ID + ", "
                                + APIUsageStatisticsClientConstants.API_NAME + ", "
                                + APIUsageStatisticsClientConstants.API_CREATOR + ", sum("
                                + APIUsageStatisticsClientConstants.TOTAL_FAULT_COUNT + ") as total_faults group by "
                                + APIUsageStatisticsClientConstants.APPLICATION_ID + ", "
                                + APIUsageStatisticsClientConstants.API_CREATOR + ", "
                                + APIUsageStatisticsClientConstants.API_NAME + ";";
                JSONObject jsonObj = APIUtil
                        .executeQueryOnStreamProcessor(APIUsageStatisticsClientConstants.APIM_FAULT_SUMMARY_SIDDHI_APP,
                                query);
                String applicationId;
                String apiName;
                String apiCreator;
                long faultCount;
                FaultCountDTO faultCountDTO;
                if (jsonObj != null) {
                    JSONArray jArray = (JSONArray) jsonObj.get(APIUsageStatisticsClientConstants.RECORDS_DELIMITER);
                    for (Object record : jArray) {
                        JSONArray recordArray = (JSONArray) record;
                        if (recordArray.size() == 4) {
                            applicationId = (String) recordArray.get(0);
                            apiName = (String) recordArray.get(1);
                            apiCreator = (String) recordArray.get(2);
                            apiName = apiName + " (" + apiCreator + ")";
                            faultCount = (Long) recordArray.get(3);
                            String appName = subscriberAppsMap.get(applicationId);
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
                }
            }
        } catch (APIManagementException e) {
            handleException("Error occurred while querying API faulty invocation data from Stream Processor ", e);
        }
        return falseAppUsageDataList;
    }

    /**
     * This method retrieve and return the usage path invocations per applications
     *
     * @param subscriberName subscriber name
     * @param groupId        group id of the subscriber
     * @param fromDate       starting date
     * @param toDate         ending date
     * @param limit          limit of the result
     * @return list if AppCallTypeDTO
     * @throws APIMgtUsageQueryServiceClientException throws if error occurred
     */
    @Override
    public List<AppCallTypeDTO> getAppApiCallType(String subscriberName, String groupId, String fromDate, String toDate,
            int limit) throws APIMgtUsageQueryServiceClientException {

        List<String> subscriberApps = getAppsAndIdsBySubscriber(subscriberName, groupId);
        return getAPICallTypeUsageData(APIUsageStatisticsClientConstants.API_RESOURCE_PATH_PER_APP_AGG,
                subscriberApps, fromDate, toDate, limit);
    }

    /**
     * This method gets the API usage data per API call type
     *
     * @param tableName name of the required table in the database
     * @param idList    Ids List of applications
     * @return a collection containing the data related to API call types
     * @throws APIMgtUsageQueryServiceClientException if an error occurs while querying the database
     */
    private List<AppCallTypeDTO> getAPICallTypeUsageData(String tableName, List<String> idList, String fromDate,
            String toDate, int limit) throws APIMgtUsageQueryServiceClientException {
        List<AppCallTypeDTO> appApiCallTypeList = new ArrayList<AppCallTypeDTO>();
        try {
            if (!idList.isEmpty()) {
                String startDate = fromDate + ":00";
                String endDate = toDate + ":00";
                String granularity = APIUsageStatisticsClientConstants.HOURS_GRANULARITY;//default granularity

                Map<String, Integer> durationBreakdown = this.getDurationBreakdown(startDate, endDate);

                if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_YEARS) > 0) {
                    granularity = APIUsageStatisticsClientConstants.YEARS_GRANULARITY;
                } else if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_MONTHS) > 0) {
                    granularity = APIUsageStatisticsClientConstants.MONTHS_GRANULARITY;
                } else if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_DAYS) > 0) {
                    granularity = APIUsageStatisticsClientConstants.DAYS_GRANULARITY;
                }
                StringBuilder idListQuery = new StringBuilder();
                for (int i = 0; i < idList.size(); i++) {
                    if (i > 0) {
                        idListQuery.append(" or ");
                    }
                    idListQuery.append(APIUsageStatisticsClientConstants.APPLICATION_ID + "==");
                    idListQuery.append("'" + idList.get(i) + "'");
                }
                String query =
                        "from " + tableName + " on " + idListQuery.toString() + " within " + getTimestamp(startDate)
                                + "L, " + getTimestamp(endDate) + "L per '" + granularity + "' select "
                                + APIUsageStatisticsClientConstants.API_NAME + ", "
                                + APIUsageStatisticsClientConstants.API_CREATOR + ", "
                                + APIUsageStatisticsClientConstants.API_METHOD + ", "
                                + APIUsageStatisticsClientConstants.APPLICATION_ID + ", "
                                + APIUsageStatisticsClientConstants.API_RESOURCE_TEMPLATE + " group by "
                                + APIUsageStatisticsClientConstants.APPLICATION_ID + ", "
                                + APIUsageStatisticsClientConstants.API_NAME + ", "
                                + APIUsageStatisticsClientConstants.API_CREATOR + ", "
                                + APIUsageStatisticsClientConstants.API_METHOD + ", "
                                + APIUsageStatisticsClientConstants.API_RESOURCE_TEMPLATE + ";";
                JSONObject jsonObj = APIUtil
                        .executeQueryOnStreamProcessor(APIUsageStatisticsClientConstants.APIM_ACCESS_SUMMARY_SIDDHI_APP,
                                query);
                String apiName;
                String apiCreator;
                String callType;
                String applicationId;
                String apiResourceTemplate;
                AppCallTypeDTO appCallTypeDTO;
                if (jsonObj != null) {
                    JSONArray jArray = (JSONArray) jsonObj.get(APIUsageStatisticsClientConstants.RECORDS_DELIMITER);
                    for (Object record : jArray) {
                        JSONArray recordArray = (JSONArray) record;
                        if (recordArray.size() == 5) {
                            apiName = (String) recordArray.get(0);
                            apiCreator = (String) recordArray.get(1);
                            apiName = apiName + " (" + apiCreator + ")";
                            callType = (String) recordArray.get(2);
                            applicationId = (String) recordArray.get(3);
                            apiResourceTemplate = (String) recordArray.get(4);
                            List<String> callTypeList = new ArrayList<String>();
                            callTypeList.add(apiResourceTemplate + " (" + callType + ")");
                            String appName = subscriberAppsMap.get(applicationId);
                            boolean found = false;
                            for (AppCallTypeDTO dto : appApiCallTypeList) {
                                if (dto.getAppName().equals(appName)) {
                                    dto.addToApiCallTypeArray(apiName, callTypeList);
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                appCallTypeDTO = new AppCallTypeDTO();
                                appCallTypeDTO.setAppName(appName);
                                appCallTypeDTO.addToApiCallTypeArray(apiName, callTypeList);
                                appApiCallTypeList.add(appCallTypeDTO);
                            }
                        }
                    }
                }
            }
        } catch (APIManagementException e) {
            handleException("Error occurred while querying API call type data from Stream Processor ", e);
        }
        return appApiCallTypeList;
    }

    /**
     * this method find the API Usage per Application data
     *
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

        List<String> subscriberApps = getAppsAndIdsBySubscriber(subscriberName, groupId);
        return getPerAppAPIUsageData(APIUsageStatisticsClientConstants.API_USER_PER_APP_AGG, subscriberApps, fromDate,
                toDate, limit);
    }

    /**
     * This method gets the API usage data per application
     *
     * @param tableName name of the required table in the database
     * @param idList    Ids list of applications
     * @return a collection containing the data related to per App API usage
     * @throws APIMgtUsageQueryServiceClientException if an error occurs while querying the database
     */
    private List<PerAppApiCountDTO> getPerAppAPIUsageData(String tableName, List<String> idList, String fromDate,
            String toDate, int limit) throws APIMgtUsageQueryServiceClientException {
        List<PerAppApiCountDTO> perAppUsageDataList = new ArrayList<PerAppApiCountDTO>();
        try {
            if (!idList.isEmpty()) {
                String startDate = fromDate + ":00";
                String endDate = toDate + ":00";
                String granularity = APIUsageStatisticsClientConstants.HOURS_GRANULARITY;//default granularity
                Map<String, Integer> durationBreakdown = this.getDurationBreakdown(startDate, endDate);

                if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_YEARS) > 0) {
                    granularity = APIUsageStatisticsClientConstants.YEARS_GRANULARITY;
                } else if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_MONTHS) > 0) {
                    granularity = APIUsageStatisticsClientConstants.MONTHS_GRANULARITY;
                } else if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_DAYS) > 0) {
                    granularity = APIUsageStatisticsClientConstants.DAYS_GRANULARITY;
                }
                StringBuilder idListQuery = new StringBuilder();
                for (int i = 0; i < idList.size(); i++) {
                    if (i > 0) {
                        idListQuery.append(" or ");
                    }
                    idListQuery.append(APIUsageStatisticsClientConstants.APPLICATION_ID + "==");
                    idListQuery.append("'" + idList.get(i) + "'");
                }
                String query =
                        "from " + tableName + " on " + idListQuery.toString() + " within " + getTimestamp(startDate)
                                + "L, " + getTimestamp(endDate) + "L per '" + granularity + "' select "
                                + APIUsageStatisticsClientConstants.API_NAME + ", "
                                + APIUsageStatisticsClientConstants.API_CREATOR + ", "
                                + APIUsageStatisticsClientConstants.APPLICATION_ID + ", sum("
                                + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ") as total_calls group by "
                                + APIUsageStatisticsClientConstants.API_NAME + ", "
                                + APIUsageStatisticsClientConstants.API_CREATOR + ", "
                                + APIUsageStatisticsClientConstants.APPLICATION_ID + ";";
                JSONObject jsonObj = APIUtil
                        .executeQueryOnStreamProcessor(APIUsageStatisticsClientConstants.APIM_ACCESS_SUMMARY_SIDDHI_APP,
                                query);
                String apiName;
                String apiCreator;
                String applicationId;
                long requestCount;
                PerAppApiCountDTO apiUsageDTO;
                if (jsonObj != null) {
                    JSONArray jArray = (JSONArray) jsonObj.get(APIUsageStatisticsClientConstants.RECORDS_DELIMITER);
                    for (Object record : jArray) {
                        JSONArray recordArray = (JSONArray) record;
                        if (recordArray.size() == 4) {
                            apiName = (String) recordArray.get(0);
                            apiCreator = (String) recordArray.get(1);
                            apiName = apiName + " (" + apiCreator + ")";
                            applicationId = (String) recordArray.get(2);
                            requestCount = (Long) recordArray.get(3);
                            String appName = subscriberAppsMap.get(applicationId);
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
                }
            }
        } catch (APIManagementException e) {
            handleException("Error occurred while querying per App usage data from Stream Processor", e);
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
     *                                                                                              while contacting
     *                                                                                              backend services
     */
    @Override
    public List<APIUsageDTO> getProviderAPIUsage(String providerName, String fromDate, String toDate, int limit)
            throws APIMgtUsageQueryServiceClientException {

        Collection<APIUsage> usageData = getAPIUsageData(APIUsageStatisticsClientConstants.API_VERSION_PER_APP_AGG,
                fromDate, toDate);
        List<API> providerAPIs = getAPIsByProvider(providerName);
        Map<String, APIUsageDTO> usageByAPIs = new TreeMap<String, APIUsageDTO>();
        for (APIUsage usage : usageData) {
            for (API providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(usage.getApiName()) && providerAPI.getId().getVersion()
                        .equals(usage.getApiVersion()) && providerAPI.getContext().equals(usage.getContext())) {
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

        Collection<APIUsage> usageDataList = new ArrayList<APIUsage>();
        try {
            String granularity = APIUsageStatisticsClientConstants.HOURS_GRANULARITY;//default granularity

            Map<String, Integer> durationBreakdown = this.getDurationBreakdown(fromDate, toDate);

            if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_YEARS) > 0) {
                granularity = APIUsageStatisticsClientConstants.YEARS_GRANULARITY;
            } else if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_MONTHS) > 0) {
                granularity = APIUsageStatisticsClientConstants.MONTHS_GRANULARITY;
            } else if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_DAYS) > 0) {
                granularity = APIUsageStatisticsClientConstants.DAYS_GRANULARITY;
            }
            String query = "from " + tableName + " within " + getTimestamp(fromDate) + "L, " + getTimestamp(toDate)
                    + "L per '" + granularity + "' select " + APIUsageStatisticsClientConstants.API_NAME + ", "
                    + APIUsageStatisticsClientConstants.API_CONTEXT + ", "
                    + APIUsageStatisticsClientConstants.API_VERSION + ", sum("
                    + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ") as aggregateSum group by "
                    + APIUsageStatisticsClientConstants.API_NAME + ", " + APIUsageStatisticsClientConstants.API_CONTEXT
                    + ", " + APIUsageStatisticsClientConstants.API_VERSION + ";";
            JSONObject jsonObj = APIUtil
                    .executeQueryOnStreamProcessor(APIUsageStatisticsClientConstants.APIM_ACCESS_SUMMARY_SIDDHI_APP,
                            query);

            String apiName;
            String apiContext;
            String apiVersion;
            Long requestCount;
            if (jsonObj != null) {
                JSONArray jArray = (JSONArray) jsonObj.get(APIUsageStatisticsClientConstants.RECORDS_DELIMITER);
                for (Object record : jArray) {
                    JSONArray recordArray = (JSONArray) record;
                    if (recordArray.size() == 4) {
                        apiName = (String) recordArray.get(0);
                        apiContext = (String) recordArray.get(1);
                        apiVersion = (String) recordArray.get(2);
                        requestCount = (Long) recordArray.get(3);
                        usageDataList.add(new APIUsage(apiName, apiContext, apiVersion, requestCount));
                    }
                }
            }
        } catch (APIManagementException e) {
            handleException("Error occurred while querying API usage data from Stream Processor ", e);
        }
        return usageDataList;
    }

    /**
     * Returns a list of APIVersionUsageDTO objects that contain information related to a
     * particular API of a specified provider, along with the number of API calls processed
     * by each version of that API for a particular time preriod.
     *
     * @param providerName API publisher username
     * @param apiName      API name
     * @param fromDate     Starting date
     * @param toDate       Ending date
     * @return list of APIVersionUsageDTO
     * @throws org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException if error occurred
     */
    @Override
    public List<APIVersionUsageDTO> getUsageByAPIVersions(String providerName, String apiName, String fromDate,
            String toDate) throws APIMgtUsageQueryServiceClientException {

        List<APIUsage> usageData = this
                .getUsageByAPIVersionsData(APIUsageStatisticsClientConstants.API_USER_PER_APP_AGG, fromDate, toDate,
                        apiName);
        List<API> providerAPIs = getAPIsByProvider(providerName);
        Map<String, APIVersionUsageDTO> usageByVersions = new TreeMap<String, APIVersionUsageDTO>();
        for (APIUsage usage : usageData) {
            for (API providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(usage.getApiName()) && providerAPI.getId().getVersion()
                        .equals(usage.getApiVersion()) && providerAPI.getContext().equals(usage.getContext())) {

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
                .getAPIUsageByResourcePathData(APIUsageStatisticsClientConstants.API_RESOURCE_PATH_PER_APP_AGG,
                        fromDate, toDate);
        List<API> providerAPIs = getAPIsByProvider(providerName);
        List<APIResourcePathUsageDTO> usageByResourcePath = new ArrayList<APIResourcePathUsageDTO>();
        for (APIUsageByResourcePath usage : usageData) {
            for (API providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(usage.getApiName()) && providerAPI.getId().getVersion()
                        .equals(usage.getApiVersion()) && providerAPI.getContext().equals(usage.getContext())) {

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
     * This method finds the destination of the apis
     *
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
                .getAPIUsageByDestinationData(APIUsageStatisticsClientConstants.API_PER_DESTINATION_AGG, fromDate,
                        toDate);
        List<API> providerAPIs = getAPIsByProvider(providerName);
        List<APIDestinationUsageDTO> usageByDestination = new ArrayList<APIDestinationUsageDTO>();

        for (APIUsageByDestination usage : usageData) {
            for (API providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(usage.getApiName()) && providerAPI.getId().getVersion()
                        .equals(usage.getApiVersion()) && providerAPI.getContext().equals(usage.getContext())) {
                    APIDestinationUsageDTO usageDTO = new APIDestinationUsageDTO();
                    usageDTO.setApiName(usage.getApiName());
                    usageDTO.setVersion(usage.getApiVersion());
                    usageDTO.setDestination(usage.getDestination());
                    usageDTO.setContext(usage.getContext());
                    usageDTO.setCount(usage.getRequestCount());
                    usageByDestination.add(usageDTO);
                }
            }
        }
        return usageByDestination;
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

        List<APIUsageByUserName> usageData = this.getAPIUsageByUserData(providerName, fromDate, toDate, null);
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

        //do nothing
        return null;
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
        //do nothing
        return null;
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
                APIUsageStatisticsClientConstants.API_LAST_ACCESS_SUMMARY, providerName);
        if (providerName.startsWith(APIUsageStatisticsClientConstants.ALL_PROVIDERS)) {
            providerName = APIUsageStatisticsClientConstants.ALL_PROVIDERS;
        }
        List<API> providerAPIs = getAPIsByProvider(providerName);
        List<APIVersionLastAccessTimeDTO> accessTimeByAPI = new ArrayList<APIVersionLastAccessTimeDTO>();
        APIVersionLastAccessTimeDTO accessTimeDTO;
        for (APIAccessTime accessTime : accessTimes) {
            for (API providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(accessTime.getApiName()) && providerAPI.getId().getVersion()
                        .equals(accessTime.getApiVersion()) && providerAPI.getContext()
                        .equals(accessTime.getContext())) {

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

        Collection<APIAccessTime> lastAccessTimeData = new ArrayList<APIAccessTime>();
        String tenantDomain = MultitenantUtils.getTenantDomain(providerName);
        try {
            StringBuilder lastAccessQuery = new StringBuilder("from " + tableName);
            if (!providerName.startsWith(APIUsageStatisticsClientConstants.ALL_PROVIDERS)) {
                lastAccessQuery.append(" on(" + APIUsageStatisticsClientConstants.API_CREATOR_TENANT_DOMAIN + "=='" + tenantDomain
                        + "' AND (" + APIUsageStatisticsClientConstants.API_CREATOR + "=='" + providerName + "' OR "
                        + APIUsageStatisticsClientConstants.API_CREATOR + "=='" + APIUtil
                        .getUserNameWithTenantSuffix(providerName) + "'))");
            } else {
                lastAccessQuery
                        .append(" on " + APIUsageStatisticsClientConstants.API_CREATOR_TENANT_DOMAIN + "=='" + tenantDomain + "'");
            }
            lastAccessQuery.append(" select " + APIUsageStatisticsClientConstants.API_NAME + ", "
                    + APIUsageStatisticsClientConstants.API_VERSION + ", "
                    + APIUsageStatisticsClientConstants.API_CONTEXT + ", " + APIUsageStatisticsClientConstants.APP_OWNER
                    + ", " + APIUsageStatisticsClientConstants.LAST_ACCESS_TIME + " order by "
                    + APIUsageStatisticsClientConstants.LAST_ACCESS_TIME + " DESC;");

            JSONObject jsonObj = APIUtil
                    .executeQueryOnStreamProcessor(APIUsageStatisticsClientConstants.APIM_ACCESS_SUMMARY_SIDDHI_APP,
                            lastAccessQuery.toString());

            String apiName;
            String apiVersion;
            String apiContext;
            Long accessTime;
            String username;

            if (jsonObj != null) {
                JSONArray jArray = (JSONArray) jsonObj.get(APIUsageStatisticsClientConstants.RECORDS_DELIMITER);
                for (Object record : jArray) {
                    JSONArray recordArray = (JSONArray) record;
                    if (recordArray.size() == 5) {
                        apiName = (String) recordArray.get(0);
                        apiVersion = (String) recordArray.get(1);
                        apiContext = (String) recordArray.get(2);
                        username = (String) recordArray.get(3);
                        accessTime = (Long) recordArray.get(4);
                        lastAccessTimeData
                                .add(new APIAccessTime(apiName, apiVersion, apiContext, accessTime, username));
                    }
                }
            }
        } catch (APIManagementException e) {
            handleException("Error occurred while querying last access data for APIs from Stream Processor ", e);
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
     *
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
                .getAPIResponseFaultCountData(APIUsageStatisticsClientConstants.API_FAULTY_INVOCATION_AGG, fromDate,
                        toDate);
        List<API> providerAPIs = getAPIsByProvider(providerName);
        List<APIResponseFaultCountDTO> faultyCount = new ArrayList<APIResponseFaultCountDTO>();
        List<APIVersionUsageDTO> apiVersionUsageList;

        for (APIResponseFaultCount fault : faultyData) {
            for (API providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(fault.getApiName()) && providerAPI.getId().getVersion()
                        .equals(fault.getApiVersion()) && providerAPI.getContext().equals(fault.getContext())) {

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
                                    ((double) fault.getFaultCount()) / (requestCount + fault.getFaultCount()) * 100;
                            DecimalFormat twoDForm = new DecimalFormat("#.##");
                            NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
                            try {
                                faultPercentage = numberFormat.parse(twoDForm.format(faultPercentage)).doubleValue();
                            } catch (ParseException e) {
                                handleException("Parse exception while formatting time");
                            }
                            faultyDTO.setFaultPercentage(faultPercentage);
                            faultyDTO.setTotalRequestCount(requestCount + fault.getFaultCount());
                            break;
                        }
                    }
                    //if no success request within that period, fault percentage is 100%
                    if (apiVersionUsageList.isEmpty()) {
                        faultyDTO.setFaultPercentage(100);
                        faultyDTO.setTotalRequestCount(fault.getFaultCount());
                    }
                    faultyCount.add(faultyDTO);
                }
            }
        }
        return faultyCount;
    }

    /**
     * find the API usage
     *
     * @param providerName API provider name
     * @param apiName      Name of the API
     * @param apiVersion   API version
     * @param limit        Number of sorted entries to return
     * @return list of PerUserAPIUsageDTO
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
                if (api.getContext().equals(usageEntry.getContext()) && api.getId().getApiName().equals(apiName) && api
                        .getId().getVersion().equals(apiVersion) && apiVersion.equals(usageEntry.getApiVersion())) {
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
     * @param limit     value to be limited
     * @return list of APIResponseTimeDTO
     */
    private List<APIResponseTimeDTO> getResponseTimeTopEntries(List<APIResponseTimeDTO> usageData, int limit) {
        //do nothing
        return null;
    }

    /**
     * This method sort and limit the result size for API Last access time data
     *
     * @param usageData data to be sort and limit
     * @param limit     value to be limited
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
     *
     * @param tableName Name of the table data exist
     * @param fromDate  starting data
     * @param toDate    ending date
     * @return list of APIResponseFaultCount
     * @throws APIMgtUsageQueryServiceClientException throws if error occurred
     */
    private List<APIResponseFaultCount> getAPIResponseFaultCountData(String tableName, String fromDate, String toDate)
            throws APIMgtUsageQueryServiceClientException {
        List<APIResponseFaultCount> faultUsage = new ArrayList<APIResponseFaultCount>();
        try {
            String granularity = APIUsageStatisticsClientConstants.HOURS_GRANULARITY;//default granularity

            Map<String, Integer> durationBreakdown = this.getDurationBreakdown(fromDate, toDate);

            if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_YEARS) > 0) {
                granularity = APIUsageStatisticsClientConstants.YEARS_GRANULARITY;
            } else if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_MONTHS) > 0) {
                granularity = APIUsageStatisticsClientConstants.MONTHS_GRANULARITY;
            } else if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_DAYS) > 0) {
                granularity = APIUsageStatisticsClientConstants.DAYS_GRANULARITY;
            }
            String query =
                    "from " + tableName + " within " + getTimestamp(fromDate) + "L, " + getTimestamp(toDate) + "L per '"
                            + granularity + "' select " + APIUsageStatisticsClientConstants.API_NAME + ", "
                            + APIUsageStatisticsClientConstants.API_VERSION + ", "
                            + APIUsageStatisticsClientConstants.API_CREATOR + ", "
                            + APIUsageStatisticsClientConstants.API_CONTEXT + ", sum("
                            + APIUsageStatisticsClientConstants.TOTAL_FAULT_COUNT + ") as total_fault_count group by "
                            + APIUsageStatisticsClientConstants.API_NAME + ", "
                            + APIUsageStatisticsClientConstants.API_VERSION + ", "
                            + APIUsageStatisticsClientConstants.API_CREATOR + ", "
                            + APIUsageStatisticsClientConstants.API_CONTEXT + "  order by " 
                            + APIUsageStatisticsClientConstants.API_NAME + " ASC ;";
            JSONObject jsonObj = APIUtil
                    .executeQueryOnStreamProcessor(APIUsageStatisticsClientConstants.APIM_FAULT_SUMMARY_SIDDHI_APP,
                            query);
            String apiName;
            String apiVersion;
            String apiContext;
            long faultCount;
            APIResponseFaultCount apiResponseFaultCount;
            if (jsonObj != null) {
                JSONArray jArray = (JSONArray) jsonObj.get(APIUsageStatisticsClientConstants.RECORDS_DELIMITER);
                for (Object record : jArray) {
                    JSONArray recordArray = (JSONArray) record;
                    if (recordArray.size() == 5) {
                        apiName = (String) recordArray.get(0);
                        apiVersion = (String) recordArray.get(1);
                        apiContext = (String) recordArray.get(3); //omitting the creator
                        faultCount = (Long) recordArray.get(4);
                        apiResponseFaultCount = new APIResponseFaultCount(apiName, apiVersion, apiContext, faultCount);
                        faultUsage.add(apiResponseFaultCount);
                    }
                }
            }
            return faultUsage;
        } catch (APIManagementException e) {
            log.error("Error occurred while querying from Stream Processor " + e.getMessage(), e);
            throw new APIMgtUsageQueryServiceClientException("Error occurred while querying from Stream Processor ", e);
        }
    }

    /**
     * This method finds the Resource path usage of APIs
     *
     * @param tableName Name of the aggregation where the data exist
     * @param fromDate  starting date
     * @param toDate    ending date
     * @return list of APIUsageByResourcePath
     * @throws APIMgtUsageQueryServiceClientException throws if error occurred
     */
    private List<APIUsageByResourcePath> getAPIUsageByResourcePathData(String tableName, String fromDate, String toDate)
            throws APIMgtUsageQueryServiceClientException {
        List<APIUsageByResourcePath> usage = new ArrayList<APIUsageByResourcePath>();
        try {
            String granularity = APIUsageStatisticsClientConstants.HOURS_GRANULARITY;//default granularity

            Map<String, Integer> durationBreakdown = this.getDurationBreakdown(fromDate, toDate);

            if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_YEARS) > 0) {
                granularity = APIUsageStatisticsClientConstants.YEARS_GRANULARITY;
            } else if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_MONTHS) > 0) {
                granularity = APIUsageStatisticsClientConstants.MONTHS_GRANULARITY;
            } else if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_DAYS) > 0) {
                granularity = APIUsageStatisticsClientConstants.DAYS_GRANULARITY;
            }

            String query =
                    "from " + tableName + " within " + getTimestamp(fromDate) + "L, " + getTimestamp(toDate) + "L per '"
                            + granularity + "' select " + APIUsageStatisticsClientConstants.API_NAME + ", "
                            + APIUsageStatisticsClientConstants.API_VERSION + ", "
                            + APIUsageStatisticsClientConstants.API_CREATOR + ", "
                            + APIUsageStatisticsClientConstants.API_CONTEXT + ", "
                            + APIUsageStatisticsClientConstants.API_METHOD + ", "
                            + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ", "
                            + APIUsageStatisticsClientConstants.API_RESOURCE_TEMPLATE + ", "
                            + APIUsageStatisticsClientConstants.TIME_STAMP + ";";

            JSONObject jsonObj = APIUtil
                    .executeQueryOnStreamProcessor(APIUsageStatisticsClientConstants.APIM_ACCESS_SUMMARY_SIDDHI_APP,
                            query);
            String apiName;
            String version;
            String context;
            String method;
            Long hits;
            String resourcePaths;
            Long time;
            APIUsageByResourcePath apiUsageByResourcePath;
            DateTimeFormatter formatter = DateTimeFormat
                    .forPattern(APIUsageStatisticsClientConstants.TIMESTAMP_PATTERN);
            if (jsonObj != null) {
                JSONArray jArray = (JSONArray) jsonObj.get(APIUsageStatisticsClientConstants.RECORDS_DELIMITER);
                for (Object record : jArray) {
                    JSONArray recordArray = (JSONArray) record;
                    if (recordArray.size() == 8) {
                        apiName = (String) recordArray.get(0);
                        version = (String) recordArray.get(1);
                        context = (String) recordArray.get(3);//omitting apiCreator
                        method = (String) recordArray.get(4);
                        hits = (Long) recordArray.get(5);
                        resourcePaths = (String) recordArray.get(6);
                        time = (Long) recordArray.get(7);
                        DateTime date = new DateTime(time);
                        apiUsageByResourcePath = new APIUsageByResourcePath(apiName, version, method, context, hits,
                                date.toString(formatter), resourcePaths);
                        usage.add(apiUsageByResourcePath);
                    }
                }
            }
            return usage;
        } catch (APIManagementException e) {
            log.error("Error occurred while querying from stream processor " + e.getMessage(), e);
            throw new APIMgtUsageQueryServiceClientException("Error occurred while querying from stream processor ", e);
        }
    }

    /**
     * This method finds the API Destination usage of APIs
     *
     * @param tableName Name of the table where the data exist
     * @param fromDate  starting date
     * @param toDate    ending date
     * @return list of APIUsageByDestination
     * @throws APIMgtUsageQueryServiceClientException throws if error occurred
     */
    private List<APIUsageByDestination> getAPIUsageByDestinationData(String tableName, String fromDate, String toDate)
            throws APIMgtUsageQueryServiceClientException {

        List<APIUsageByDestination> usageByDestination = new ArrayList<APIUsageByDestination>();
        try {
            String granularity = APIUsageStatisticsClientConstants.HOURS_GRANULARITY;//default granularity

            Map<String, Integer> durationBreakdown = this.getDurationBreakdown(fromDate, toDate);

            if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_YEARS) > 0) {
                granularity = APIUsageStatisticsClientConstants.YEARS_GRANULARITY;
            } else if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_MONTHS) > 0) {
                granularity = APIUsageStatisticsClientConstants.MONTHS_GRANULARITY;
            } else if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_DAYS) > 0) {
                granularity = APIUsageStatisticsClientConstants.DAYS_GRANULARITY;
            }

            String query =
                    "from " + tableName + " within " + getTimestamp(fromDate) + "L, " + getTimestamp(toDate) + "L per '"
                            + granularity + "' select " + APIUsageStatisticsClientConstants.API_NAME + ", "
                            + APIUsageStatisticsClientConstants.API_VERSION + ", "
                            + APIUsageStatisticsClientConstants.API_CREATOR + ", "
                            + APIUsageStatisticsClientConstants.API_CONTEXT + ", "
                            + APIUsageStatisticsClientConstants.DESTINATION + ", " + "sum("
                            + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT
                            + ") as total_request_count group by " + APIUsageStatisticsClientConstants.API_NAME + ", "
                            + APIUsageStatisticsClientConstants.API_VERSION + ", "
                            + APIUsageStatisticsClientConstants.API_CREATOR + ", "
                            + APIUsageStatisticsClientConstants.API_CONTEXT + ", "
                            + APIUsageStatisticsClientConstants.DESTINATION + ";";
            JSONObject jsonObj = APIUtil
                    .executeQueryOnStreamProcessor(APIUsageStatisticsClientConstants.APIM_ACCESS_SUMMARY_SIDDHI_APP,
                            query);
            String apiName;
            String version;
            String context;
            String destination;
            Long requestCount;
            APIUsageByDestination apiUsageByDestination;

            if (jsonObj != null) {
                JSONArray jArray = (JSONArray) jsonObj.get(APIUsageStatisticsClientConstants.RECORDS_DELIMITER);
                for (Object record : jArray) {
                    JSONArray recordArray = (JSONArray) record;
                    if (recordArray.size() == 6) {
                        apiName = (String) recordArray.get(0);
                        version = (String) recordArray.get(1);
                        context = (String) recordArray.get(3);//omitting apiCreator
                        destination = (String) recordArray.get(4);
                        requestCount = (Long) recordArray.get(5);
                        apiUsageByDestination = new APIUsageByDestination(apiName, version, context, destination,
                                requestCount);
                        usageByDestination.add(apiUsageByDestination);
                    }
                }
            }
            return usageByDestination;
        } catch (APIManagementException e) {
            log.error("Error occurred while querying from stream processor " + e.getMessage(), e);
            throw new APIMgtUsageQueryServiceClientException("Error occurred while querying from stream processor", e);
        }
    }

    /**
     * Retrieves total request count for the given period of time for particular API and Version. If version provided
     * as FOR_ALL_API_VERSIONS it will get total aggregated request count for all api versions
     *
     * @param tableName  tableName
     * @param apiName    API name
     * @param apiVersion API version
     * @param fromDate   Start date of the time span
     * @param toDate     End date of time span
     * @return Total request count
     * @throws APIMgtUsageQueryServiceClientException
     */
    private long getTotalRequestCountOfAPIVersion(String tableName, String apiName, String tenantDomain,
            String apiVersion, String fromDate, String toDate) throws APIMgtUsageQueryServiceClientException {
        List<APIUsage> apiUsages = getUsageByAPIVersionsData(tableName, fromDate, toDate, apiName);
        long totalRequestCount = 0;
        Pattern tenantContextPattern;
        boolean match;
        if (tenantDomain != null && !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            tenantContextPattern = Pattern.compile("^/t/" + tenantDomain + "/.*");
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
     * This method find the API version wise usage
     *
     * @param tableName Name of the table data exist
     * @param fromDate  starting data
     * @param toDate    ending date
     * @param apiName   API name
     * @return list of APIUsage
     * @throws APIMgtUsageQueryServiceClientException throws if error occurred
     */
    private List<APIUsage> getUsageByAPIVersionsData(String tableName, String fromDate, String toDate, String apiName)
            throws APIMgtUsageQueryServiceClientException {

        List<APIUsage> usageDataList = new ArrayList<APIUsage>();
        try {
            String query;
            if (fromDate != null && toDate != null) {
                String granularity = APIUsageStatisticsClientConstants.HOURS_GRANULARITY;//default granularity

                Map<String, Integer> durationBreakdown = this.getDurationBreakdown(fromDate, toDate);

                if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_YEARS) > 0) {
                    granularity = APIUsageStatisticsClientConstants.YEARS_GRANULARITY;
                } else if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_MONTHS) > 0) {
                    granularity = APIUsageStatisticsClientConstants.MONTHS_GRANULARITY;
                } else if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_DAYS) > 0) {
                    granularity = APIUsageStatisticsClientConstants.DAYS_GRANULARITY;
                }

                query = "from " + tableName + " on " + APIUsageStatisticsClientConstants.API_NAME + "=='" + apiName
                        + "' within " + getTimestamp(fromDate) + "L, " + getTimestamp(toDate) + "L per '" + granularity
                        + "' select " + APIUsageStatisticsClientConstants.API_NAME + ", "
                        + APIUsageStatisticsClientConstants.API_VERSION + ", "
                        + APIUsageStatisticsClientConstants.API_CREATOR + ", "
                        + APIUsageStatisticsClientConstants.API_CONTEXT + ", sum("
                        + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ") as total_request_count group by "
                        + APIUsageStatisticsClientConstants.API_NAME + ", "
                        + APIUsageStatisticsClientConstants.API_VERSION + ", "
                        + APIUsageStatisticsClientConstants.API_CREATOR + ", "
                        + APIUsageStatisticsClientConstants.API_CONTEXT + ";";
            } else {
                query = "from " + APIUsageStatisticsClientConstants.API_USER_PER_APP_AGG + " on "
                        + APIUsageStatisticsClientConstants.API_NAME + "=='" + apiName + "'" + " within " + 0 + "L, "
                        + new Date().getTime() + "L per 'months' select " + APIUsageStatisticsClientConstants.API_NAME
                        + ", " + APIUsageStatisticsClientConstants.API_VERSION + ", "
                        + APIUsageStatisticsClientConstants.API_CREATOR + ", "
                        + APIUsageStatisticsClientConstants.API_CONTEXT + ", sum("
                        + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ") as total_request_count group by "
                        + APIUsageStatisticsClientConstants.API_NAME + ", "
                        + APIUsageStatisticsClientConstants.API_VERSION + ", "
                        + APIUsageStatisticsClientConstants.API_CREATOR + ", "
                        + APIUsageStatisticsClientConstants.API_CONTEXT + ";";
            }

            JSONObject jsonObj = APIUtil
                    .executeQueryOnStreamProcessor(APIUsageStatisticsClientConstants.APIM_ACCESS_SUMMARY_SIDDHI_APP,
                            query);

            String apiContext;
            String apiVersion;
            Long requestCount;
            if (jsonObj != null) {
                JSONArray jArray = (JSONArray) jsonObj.get(APIUsageStatisticsClientConstants.RECORDS_DELIMITER);
                for (Object record : jArray) {
                    JSONArray recordArray = (JSONArray) record;
                    if (recordArray.size() == 5) {
                        apiVersion = (String) recordArray.get(1);
                        apiContext = (String) recordArray.get(3);
                        requestCount = (Long) recordArray.get(4);
                        usageDataList.add(new APIUsage(apiName, apiContext, apiVersion, requestCount));
                    }
                }
            }
            return usageDataList;
        } catch (Exception e) {
            log.error("Error occurred while querying from Stream Processor " + e.getMessage(), e);
            throw new APIMgtUsageQueryServiceClientException("Error occurred while querying from Stream Processor ", e);
        }
    }

    /**
     * This method find the api usage count and its subscribers
     *
     * @param providerName logged API publisher
     * @param fromDate     starting date
     * @param toDate       ending date
     * @param limit        result to be limited
     * @return list of APIUsageByUserName
     * @throws APIMgtUsageQueryServiceClientException throws if error occurred
     */
    private List<APIUsageByUserName> getAPIUsageByUserData(String providerName, String fromDate, String toDate,
            Integer limit) throws APIMgtUsageQueryServiceClientException {

        String tenantDomain = MultitenantUtils.getTenantDomain(providerName);
        try {
            String query;
            String filter;
            if (providerName.contains(APIUsageStatisticsClientConstants.ALL_PROVIDERS)) {
                filter = APIUsageStatisticsClientConstants.API_CREATOR_TENANT_DOMAIN + "=='" + tenantDomain + "'";
            } else {
                filter = APIUsageStatisticsClientConstants.API_CREATOR + "=='" + providerName + "'";
            }

            if (fromDate != null && toDate != null) {
                String granularity = APIUsageStatisticsClientConstants.HOURS_GRANULARITY;//default granularity

                Map<String, Integer> durationBreakdown = this.getDurationBreakdown(fromDate, toDate);

                if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_YEARS) > 0) {
                    granularity = APIUsageStatisticsClientConstants.YEARS_GRANULARITY;
                } else if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_MONTHS) > 0) {
                    granularity = APIUsageStatisticsClientConstants.MONTHS_GRANULARITY;
                } else if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_DAYS) > 0) {
                    granularity = APIUsageStatisticsClientConstants.DAYS_GRANULARITY;
                }
                query = "from " + APIUsageStatisticsClientConstants.API_USER_PER_APP_AGG + " on " + filter + " within "
                        + getTimestamp(fromDate) + "L, " + getTimestamp(toDate) + "L per '" + granularity + "' select "
                        + APIUsageStatisticsClientConstants.API_NAME + ", "
                        + APIUsageStatisticsClientConstants.API_VERSION + ", "
                        + APIUsageStatisticsClientConstants.API_CREATOR + ", "
                        + APIUsageStatisticsClientConstants.USERNAME + ", sum("
                        + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ") as total_request_count, "
                        + APIUsageStatisticsClientConstants.API_CONTEXT + " group by "
                        + APIUsageStatisticsClientConstants.API_NAME + ", "
                        + APIUsageStatisticsClientConstants.API_VERSION + ", "
                        + APIUsageStatisticsClientConstants.USERNAME + ", "
                        + APIUsageStatisticsClientConstants.API_CREATOR + ", "
                        + APIUsageStatisticsClientConstants.API_CONTEXT + " order by total_request_count DESC;";
            } else {
                query = "from " + APIUsageStatisticsClientConstants.API_USER_PER_APP_AGG + " on " + filter + " select "
                        + APIUsageStatisticsClientConstants.API_NAME + ", "
                        + APIUsageStatisticsClientConstants.API_VERSION + ", "
                        + APIUsageStatisticsClientConstants.API_CREATOR + ", "
                        + APIUsageStatisticsClientConstants.USERNAME + ", sum("
                        + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ") as total_request_count, "
                        + APIUsageStatisticsClientConstants.API_CONTEXT + " group by "
                        + APIUsageStatisticsClientConstants.API_NAME + ", "
                        + APIUsageStatisticsClientConstants.API_VERSION + ", "
                        + APIUsageStatisticsClientConstants.USERNAME + ", "
                        + APIUsageStatisticsClientConstants.API_CREATOR + ", "
                        + APIUsageStatisticsClientConstants.API_CONTEXT + " order by total_request_count DESC;";
            }

            JSONObject jsonObj = APIUtil
                    .executeQueryOnStreamProcessor(APIUsageStatisticsClientConstants.APIM_ACCESS_SUMMARY_SIDDHI_APP,
                            query);
            String apiName;
            String apiVersion;
            String apiContext;
            String username;
            Long requestCount;
            String creator;
            List<APIUsageByUserName> usageByName = new ArrayList<APIUsageByUserName>();

            if (jsonObj != null) {
                JSONArray jArray = (JSONArray) jsonObj.get(APIUsageStatisticsClientConstants.RECORDS_DELIMITER);
                for (Object record : jArray) {
                    JSONArray recordArray = (JSONArray) record;
                    if (recordArray.size() == 6) {
                        apiName = (String) recordArray.get(0);
                        apiVersion = (String) recordArray.get(1);
                        creator = (String) recordArray.get(2);
                        username = (String) recordArray.get(3);
                        requestCount = (Long) recordArray.get(4);
                        apiContext = (String) recordArray.get(5);
                        if (creator != null) {
                            APIUsageByUserName usage = new APIUsageByUserName(apiName, apiVersion, apiContext, username,
                                    creator, requestCount);
                            usageByName.add(usage);
                        }
                    }
                }
            }
            return usageByName;
        } catch (APIManagementException e) {
            log.error("Error occurred while querying from Stream Processor " + e.getMessage(), e);
            throw new APIMgtUsageQueryServiceClientException("Error occurred while querying from Stream Processor ", e);
        }
    }

    /**
     * This method find the list of API published by particular Pulisher
     *
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
     * This method returns a default first access time for the API
     *
     * @param providerName provider name
     * @return APIFirstAccess Object with the first access data
     * @throws APIMgtUsageQueryServiceClientException when error occurs while connecting to the stream processor
     */
    @Override
    public List<APIFirstAccess> getFirstAccessTime(String providerName) throws APIMgtUsageQueryServiceClientException {
        List<APIFirstAccess> APIFirstAccessList = new ArrayList<APIFirstAccess>();
        try {
            //Check availability of the analytics server
            String query = "from " + APIUsageStatisticsClientConstants.API_USER_PER_APP_AGG + "_SECONDS select "
                    + APIUsageStatisticsClientConstants.TIME_STAMP + " limit 1;";
            APIUtil.executeQueryOnStreamProcessor(APIUsageStatisticsClientConstants.APIM_ACCESS_SUMMARY_SIDDHI_APP,
                    query);
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -1); //get 1 month from the current date
            String year = Integer.toString(calendar.get(Calendar.YEAR));
            String month = Integer.toString(calendar.get(Calendar.MONTH) + 1); //Month is 0 based
            String day = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));
            APIFirstAccess firstAccess = new APIFirstAccess(year, month, day);
            APIFirstAccess fTime;
            if (firstAccess != null) {
                fTime = new APIFirstAccess(firstAccess.getYear(), firstAccess.getMonth(), firstAccess.getDay());
                APIFirstAccessList.add(fTime);
            }
        } catch (APIManagementException e) {
            log.error("Error occurred while querying from the stream processor " + e.getMessage(), e);
            throw new APIMgtUsageQueryServiceClientException(
                    "Error occurred while querying from the stream processor " + e.getMessage(), e);
        }
        return APIFirstAccessList;
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
        Collection<APIUsageByUser> usageData = new ArrayList<APIUsageByUser>();
        try {
            StringBuilder query = new StringBuilder("from " + APIUsageStatisticsClientConstants.API_USER_PER_APP_AGG);
            if (apiVersion != null) {
                query.append(" on (" + APIUsageStatisticsClientConstants.API_NAME + "=='" + apiName + "' " + " AND "
                        + APIUsageStatisticsClientConstants.API_VERSION + "=='" + apiVersion + "') ");
            } else {
                query.append(" on " + APIUsageStatisticsClientConstants.API_NAME + "=='" + apiName + "' ");
            }
            query.append("within " + 0 + "L, " + new Date().getTime() + "L per 'months' select "
                    + APIUsageStatisticsClientConstants.API_CONTEXT + ", " + APIUsageStatisticsClientConstants.USERNAME
                    + ", " + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ", "
                    + APIUsageStatisticsClientConstants.API_VERSION + ";");

            JSONObject jsonObj = APIUtil
                    .executeQueryOnStreamProcessor(APIUsageStatisticsClientConstants.APIM_ACCESS_SUMMARY_SIDDHI_APP,
                            query.toString());
            String apiContext;
            String username;
            Long requestCount;
            String version;

            if (jsonObj != null) {
                JSONArray jArray = (JSONArray) jsonObj.get(APIUsageStatisticsClientConstants.RECORDS_DELIMITER);
                for (Object record : jArray) {
                    JSONArray recordArray = (JSONArray) record;
                    if (recordArray.size() == 4) {
                        apiContext = (String) recordArray.get(0);
                        username = (String) recordArray.get(1);
                        requestCount = (Long) recordArray.get(2);
                        version = (String) recordArray.get(3);
                        usageData.add(new APIUsageByUser(apiContext, username, requestCount, version));
                    }
                }
            }
            return usageData;
        } catch (APIManagementException e) {
            log.error("Error occurred while querying from Stream Processor " + e.getMessage(), e);
            throw new APIMgtUsageQueryServiceClientException("Error occurred while querying from Stream Processor", e);
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
     * @throws APIMgtUsageQueryServiceClientException throws when there is an error
     */
    @Override
    public List<APIThrottlingOverTimeDTO> getThrottleDataOfAPIAndApplication(String apiName, String provider,
            String appName, String fromDate, String toDate, String groupBy)
            throws APIMgtUsageQueryServiceClientException {
        try {
            List<APIThrottlingOverTimeDTO> throttlingData = new ArrayList<APIThrottlingOverTimeDTO>();
            String tenantDomain = MultitenantUtils.getTenantDomain(provider);
            String granularity = APIUsageStatisticsClientConstants.MINUTES_GRANULARITY;//default granularity

            Map<String, Integer> durationBreakdown = this.getDurationBreakdown(fromDate, toDate);

            if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_YEARS) > 0) {
                granularity = APIUsageStatisticsClientConstants.MONTHS_GRANULARITY;
            } else if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_MONTHS) > 0
                    || durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_WEEKS) > 0) {
                granularity = APIUsageStatisticsClientConstants.DAYS_GRANULARITY;
            } else if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_DAYS) > 0) {
                granularity = APIUsageStatisticsClientConstants.HOURS_GRANULARITY;
            }
            StringBuilder query = new StringBuilder(
                    "from " + APIUsageStatisticsClientConstants.APIM_REQ_COUNT_AGG + " on("
                            + APIUsageStatisticsClientConstants.API_CREATOR_TENANT_DOMAIN + "=='" + tenantDomain
                            + "' AND " + APIUsageStatisticsClientConstants.API_NAME + "=='" + apiName + "'");
            if (!provider.startsWith(APIUsageStatisticsClientConstants.ALL_PROVIDERS)) {
                query.append(" AND " + APIUsageStatisticsClientConstants.API_CREATOR + "=='" + provider + "'");
            }
            if (!StringUtils.isEmpty(appName)) {
                query.append(" AND " + APIUsageStatisticsClientConstants.APPLICATION_NAME + "=='" + appName + "'");
            }
            query.append(") within " + getTimestamp(fromDate) + "L, " + getTimestamp(toDate) + "L per '" + granularity
                    + "' select " + APIUsageStatisticsClientConstants.TIME_STAMP + ", sum(coalesce("
                    + APIUsageStatisticsClientConstants.SUCCESS_COUNT + ",0L)) as success_request_count, sum(coalesce("
                    + APIUsageStatisticsClientConstants.THROTTLE_COUNT + ",0L)) as throttled_out_count group by "
                    + APIUsageStatisticsClientConstants.TIME_STAMP + " order by "
                    + APIUsageStatisticsClientConstants.TIME_STAMP + " ASC;");
            JSONObject jsonObj = APIUtil.executeQueryOnStreamProcessor(
                    APIUsageStatisticsClientConstants.APIM_THROTTLED_OUT_SUMMARY_SIDDHI_APP, query.toString());
            Long timeStamp;
            String time;
            long successRequestCount;
            long throttledOutCount;
            DateTimeFormatter formatter = DateTimeFormat
                    .forPattern(APIUsageStatisticsClientConstants.TIMESTAMP_PATTERN);
            if (jsonObj != null) {
                JSONArray jArray = (JSONArray) jsonObj.get(APIUsageStatisticsClientConstants.RECORDS_DELIMITER);
                for (Object record : jArray) {
                    JSONArray recordArray = (JSONArray) record;
                    if (recordArray.size() == 3) {
                        timeStamp = (Long) recordArray.get(0);
                        time = new DateTime(timeStamp).withZone(DateTimeZone.UTC).toString(formatter);
                        successRequestCount = (Long) recordArray.get(1);
                        throttledOutCount = (Long) recordArray.get(2);
                        throttlingData.add(new APIThrottlingOverTimeDTO(apiName, provider, (int) successRequestCount,
                                (int) throttledOutCount, time));
                    }
                }
            }
            return throttlingData;
        } catch (APIManagementException e) {
            log.error("Error occurred while querying from Stream Processor " + e.getMessage(), e);
            throw new APIMgtUsageQueryServiceClientException("Error occurred while querying from Stream Processor ", e);
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
        try {
            List<APIThrottlingOverTimeDTO> throttlingData = new ArrayList<APIThrottlingOverTimeDTO>();
            String tenantDomain = MultitenantUtils.getTenantDomain(provider);

            String granularity = APIUsageStatisticsClientConstants.MINUTES_GRANULARITY;//default granularity

            Map<String, Integer> durationBreakdown = this.getDurationBreakdown(fromDate, toDate);

            if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_YEARS) > 0) {
                granularity = APIUsageStatisticsClientConstants.MONTHS_GRANULARITY;
            } else if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_MONTHS) > 0
                    || durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_WEEKS) > 0) {
                granularity = APIUsageStatisticsClientConstants.DAYS_GRANULARITY;
            } else if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_DAYS) > 0) {
                granularity = APIUsageStatisticsClientConstants.HOURS_GRANULARITY;
            }

            StringBuilder query = new StringBuilder(
                    "from " + APIUsageStatisticsClientConstants.APIM_REQ_COUNT_AGG + " on ("
                            + APIUsageStatisticsClientConstants.API_CREATOR_TENANT_DOMAIN + "=='" + tenantDomain
                            + "' AND " + APIUsageStatisticsClientConstants.APPLICATION_NAME + "=='" + appName + "'");
            if (!provider.startsWith(APIUsageStatisticsClientConstants.ALL_PROVIDERS)) {
                query.append("AND " + APIUsageStatisticsClientConstants.API_CREATOR + "=='" + provider + "'");
            }
            query.append(") within " + getTimestamp(fromDate) + "L, " + getTimestamp(toDate) + "L per '" + granularity
                    + "' select " + APIUsageStatisticsClientConstants.API_NAME + ", "
                    + APIUsageStatisticsClientConstants.API_CREATOR + ", sum(coalesce("
                    + APIUsageStatisticsClientConstants.SUCCESS_COUNT + ",0L)) as success_request_count, sum(coalesce("
                    + APIUsageStatisticsClientConstants.THROTTLE_COUNT + ",0L)) as throttleout_count group by "
                    + APIUsageStatisticsClientConstants.API_NAME + ", " + APIUsageStatisticsClientConstants.API_CREATOR
                    + " order by " + APIUsageStatisticsClientConstants.API_NAME + " ASC;");
            JSONObject jsonObj = APIUtil.executeQueryOnStreamProcessor(
                    APIUsageStatisticsClientConstants.APIM_THROTTLED_OUT_SUMMARY_SIDDHI_APP, query.toString());

            String apiName;
            String apiCreator;
            long successRequestCount;
            long throttledOutCount;

            if (jsonObj != null) {
                JSONArray jArray = (JSONArray) jsonObj.get(APIUsageStatisticsClientConstants.RECORDS_DELIMITER);
                for (Object record : jArray) {
                    JSONArray recordArray = (JSONArray) record;
                    if (recordArray.size() == 4) {
                        apiName = (String) recordArray.get(0);
                        apiCreator = (String) recordArray.get(1);
                        successRequestCount = (Long) recordArray.get(2);
                        throttledOutCount = (Long) recordArray.get(3);
                        throttlingData.add(new APIThrottlingOverTimeDTO(apiName, apiCreator, (int) successRequestCount,
                                (int) throttledOutCount, null));
                    }
                }
            }
            return throttlingData;
        } catch (APIManagementException e) {
            log.error("Error occurred while querying from Stream Processor " + e.getMessage(), e);
            throw new APIMgtUsageQueryServiceClientException("Error occurred while querying from Stream Processor ", e);
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
        try {
            List<String> throttlingAPIData = new ArrayList<String>();
            String tenantDomain = MultitenantUtils.getTenantDomain(provider);
            StringBuilder query = new StringBuilder("from " + APIUsageStatisticsClientConstants.API_THROTTLED_OUT_AGG);

            if (!provider.startsWith(APIUsageStatisticsClientConstants.ALL_PROVIDERS)) {
                query.append(
                        " on (" + APIUsageStatisticsClientConstants.API_CREATOR_TENANT_DOMAIN + "=='" + tenantDomain
                                + "' AND " + APIUsageStatisticsClientConstants.API_CREATOR + "=='" + APIUtil
                                .getUserNameWithTenantSuffix(provider) + "')");
            } else {
                query.append(" on " + APIUsageStatisticsClientConstants.API_CREATOR_TENANT_DOMAIN + "=='" + tenantDomain
                        + "'");
            }
            query.append(" within " + 0 + "L, " + new Date().getTime() + "L per 'months' select "
                    + APIUsageStatisticsClientConstants.API_NAME + " group by "
                    + APIUsageStatisticsClientConstants.API_NAME + " order by "
                    + APIUsageStatisticsClientConstants.API_NAME + " ASC;");

            JSONObject jsonObj = APIUtil.executeQueryOnStreamProcessor(
                    APIUsageStatisticsClientConstants.APIM_THROTTLED_OUT_SUMMARY_SIDDHI_APP, query.toString());
            String apiName;
            if (jsonObj != null) {
                JSONArray jArray = (JSONArray) jsonObj.get(APIUsageStatisticsClientConstants.RECORDS_DELIMITER);
                for (Object record : jArray) {
                    JSONArray recordArray = (JSONArray) record;
                    if (recordArray.size() == 1) {
                        apiName = (String) recordArray.get(0);
                        throttlingAPIData.add(apiName);
                    }
                }
            }
            return throttlingAPIData;
        } catch (APIManagementException e) {
            log.error("Error occurred while querying from Stream Processor " + e.getMessage(), e);
            throw new APIMgtUsageQueryServiceClientException("Error occurred while querying from Stream Processor ", e);
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
        try {
            List<String> throttlingAppData = new ArrayList<String>();
            String tenantDomain = MultitenantUtils.getTenantDomain(provider);
            StringBuilder query = new StringBuilder("from " + APIUsageStatisticsClientConstants.API_THROTTLED_OUT_AGG);
            query.append(" on " + APIUsageStatisticsClientConstants.API_CREATOR_TENANT_DOMAIN + "=='" + tenantDomain + "'");
            if (!provider.startsWith(APIUsageStatisticsClientConstants.ALL_PROVIDERS)) {
                query.append(" AND " + APIUsageStatisticsClientConstants.API_CREATOR + "=='" + APIUtil
                        .getUserNameWithTenantSuffix(provider) + "'");
            }
            if (apiName != null) {
                query.append(" AND " + APIUsageStatisticsClientConstants.API_NAME + "=='" + apiName + "'");
            }
            query.append(" within " + 0 + "L, " + new Date().getTime() + "L per 'months' select "
                    + APIUsageStatisticsClientConstants.APPLICATION_NAME + " group by "
                    + APIUsageStatisticsClientConstants.APPLICATION_NAME + " order by "
                    + APIUsageStatisticsClientConstants.APPLICATION_NAME + " DESC;");
            JSONObject jsonObj = APIUtil.executeQueryOnStreamProcessor(
                    APIUsageStatisticsClientConstants.APIM_THROTTLED_OUT_SUMMARY_SIDDHI_APP, query.toString());

            String applicationName;
            if (jsonObj != null) {
                JSONArray jArray = (JSONArray) jsonObj.get(APIUsageStatisticsClientConstants.RECORDS_DELIMITER);
                for (Object record : jArray) {
                    JSONArray recordArray = (JSONArray) record;
                    if (recordArray.size() == 1) {
                        applicationName = (String) recordArray.get(0);
                        throttlingAppData.add(applicationName);
                    }
                }
            }
            return throttlingAppData;
        } catch (APIManagementException e) {
            log.error("Error occurred while querying from Stream Processor " + e.getMessage(), e);
            throw new APIMgtUsageQueryServiceClientException("Error occurred while querying from Stream Processor ", e);
        }
    }

    /**
     * return a string to indicate type of statistics client
     *
     * @return String
     */
    @Override
    public String getClientType() {
        return APIUsageStatisticsClientConstants.REST_STATISTICS_CLIENT_TYPE;
    }

    @Override
    public List<Result<ExecutionTimeOfAPIValues>> getExecutionTimeByAPI(String apiName, String version,
            String tenantDomain, String fromDate, String toDate, String drillDown)
            throws APIMgtUsageQueryServiceClientException {

        return getExecutionTimeByAPI(apiName, version, tenantDomain, fromDate, toDate, drillDown, "ALL");
    }

    @Override
    public List<Result<ExecutionTimeOfAPIValues>> getExecutionTimeByAPI(String apiName, String version,
            String tenantDomain, String fromDate, String toDate, String drillDown, String mediationType)
            throws APIMgtUsageQueryServiceClientException {
        List<Result<ExecutionTimeOfAPIValues>> result = new ArrayList<Result<ExecutionTimeOfAPIValues>>();
        try {
            StringBuilder query = new StringBuilder(
                    "from " + APIUsageStatisticsClientConstants.API_EXECUTION_TIME_AGG + " on("
                            + APIUsageStatisticsClientConstants.API_NAME + "=='" + apiName + "'");
            if (version != null) {
                query.append(" AND " + APIUsageStatisticsClientConstants.API_VERSION + "=='" + version + "'");
            }
            if (tenantDomain != null) {
                query.append(
                        " AND " + APIUsageStatisticsClientConstants.API_CREATOR_TENANT_DOMAIN + "=='" + tenantDomain
                                + "'");
            }
            if (fromDate != null && toDate != null) {
                String granularity = APIUsageStatisticsClientConstants.SECONDS_GRANULARITY;

                Map<String, Integer> durationBreakdown = this.getDurationBreakdown(fromDate, toDate);

                if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_YEARS) > 0) {
                    granularity = APIUsageStatisticsClientConstants.MONTHS_GRANULARITY;
                } else if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_MONTHS) > 0
                        || durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_WEEKS) > 0) {
                    granularity = APIUsageStatisticsClientConstants.DAYS_GRANULARITY;
                } else if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_DAYS) > 0) {
                    granularity = APIUsageStatisticsClientConstants.HOURS_GRANULARITY;
                } else if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_HOURS) > 0) {
                    granularity = APIUsageStatisticsClientConstants.MINUTES_GRANULARITY;
                }
                query.append(
                        ") within " + getTimestamp(fromDate) + "L, " + getTimestamp(toDate) + "L per '" + granularity
                                + "'");
            } else {
                query.append(") within " + 0 + "L, " + new Date().getTime() + "L per 'months'");
            }
            query.append(" select " + APIUsageStatisticsClientConstants.API_NAME + ", "
                    + APIUsageStatisticsClientConstants.API_CONTEXT + ", "
                    + APIUsageStatisticsClientConstants.API_CREATOR + ", "
                    + APIUsageStatisticsClientConstants.API_VERSION + ", "
                    + APIUsageStatisticsClientConstants.TIME_STAMP + ", "
                    + APIUsageStatisticsClientConstants.RESPONSE_TIME + ", "
                    + APIUsageStatisticsClientConstants.SECURITY_LATENCY + ", "
                    + APIUsageStatisticsClientConstants.THROTTLING_LATENCY + ", "
                    + APIUsageStatisticsClientConstants.REQUEST_MEDIATION_LATENCY + ", "
                    + APIUsageStatisticsClientConstants.RESPONSE_MEDIATION_LATENCY + ", "
                    + APIUsageStatisticsClientConstants.BACKEND_LATENCY + ", "
                    + APIUsageStatisticsClientConstants.OTHER_LATENCY + ";");
            JSONObject jsonObj = APIUtil
                    .executeQueryOnStreamProcessor(APIUsageStatisticsClientConstants.APIM_ACCESS_SUMMARY_SIDDHI_APP,
                            query.toString());
            long timeStamp;
            if (jsonObj != null) {
                JSONArray jArray = (JSONArray) jsonObj.get(APIUsageStatisticsClientConstants.RECORDS_DELIMITER);
                for (Object record : jArray) {
                    JSONArray recordArray = (JSONArray) record;
                    if (recordArray.size() == 12) {
                        Result<ExecutionTimeOfAPIValues> result1 = new Result<ExecutionTimeOfAPIValues>();
                        ExecutionTimeOfAPIValues executionTimeOfAPIValues = new ExecutionTimeOfAPIValues();
                        executionTimeOfAPIValues.setApi((String) recordArray.get(0));
                        executionTimeOfAPIValues.setContext((String) recordArray.get(1));
                        executionTimeOfAPIValues.setApiPublisher((String) recordArray.get(2));
                        executionTimeOfAPIValues.setVersion((String) recordArray.get(3));
                        timeStamp = (Long) recordArray.get(4);
                        DateTime time = new DateTime(timeStamp).withZone(DateTimeZone.UTC);
                        executionTimeOfAPIValues.setYear(time.getYear());
                        executionTimeOfAPIValues.setMonth(time.getMonthOfYear());
                        executionTimeOfAPIValues.setDay(time.getDayOfMonth());
                        executionTimeOfAPIValues.setHour(time.getHourOfDay());
                        executionTimeOfAPIValues.setMinutes(time.getMinuteOfHour());
                        executionTimeOfAPIValues.setSeconds(time.getSecondOfMinute());
                        executionTimeOfAPIValues.setApiResponseTime((Long) recordArray.get(5));
                        executionTimeOfAPIValues.setSecurityLatency((Long) recordArray.get(6));
                        executionTimeOfAPIValues.setThrottlingLatency((Long) recordArray.get(7));
                        executionTimeOfAPIValues.setRequestMediationLatency((Long) recordArray.get(8));
                        executionTimeOfAPIValues.setResponseMediationLatency((Long) recordArray.get(9));
                        executionTimeOfAPIValues.setBackendLatency((Long) recordArray.get(10));
                        executionTimeOfAPIValues.setOtherLatency((Long) recordArray.get(11));
                        result1.setValues(executionTimeOfAPIValues);
                        result1.setTableName(APIUsageStatisticsClientConstants.API_EXECUTION_TIME_AGG);
                        result1.setTimestamp(RestClientUtil.longToDate(new Date().getTime()));
                        result.add(result1);
                    }
                }
            }
            if (!result.isEmpty() && fromDate != null && toDate != null) {
                insertZeroElementsAndSort(result, drillDown, getDateToLong(fromDate), getDateToLong(toDate));
            }
        } catch (APIManagementException e) {
            handleException("Error occurred while querying from Stream Processor ", e);
        } catch (ParseException e) {
            handleException("Couldn't parse the date", e);
        }
        return result;
    }

    @Override
    public List<Result<PerGeoLocationUsageCount>> getGeoLocationsByApi(String apiName, String version,
            String tenantDomain, String fromDate, String toDate, String drillDown)
            throws APIMgtUsageQueryServiceClientException {
        List<Result<PerGeoLocationUsageCount>> result = new ArrayList<Result<PerGeoLocationUsageCount>>();
        try {
            StringBuilder query = new StringBuilder(
                    "from " + APIUsageStatisticsClientConstants.GEO_LOCATION_AGG + " on("
                            + APIUsageStatisticsClientConstants.API_NAME + "=='" + apiName + "'");
            if (version != null && !"ALL".equals(version)) {
                query.append(" AND " + APIUsageStatisticsClientConstants.API_VERSION + "=='" + version + "'");
            }
            if (tenantDomain != null) {
                query.append(
                        " AND " + APIUsageStatisticsClientConstants.API_CREATOR_TENANT_DOMAIN + "=='" + tenantDomain
                                + "'");
            }
            if (!"ALL".equals(drillDown)) {
                query.append(" AND " + APIUsageStatisticsClientConstants.COUNTRY + "=='" + drillDown + "'");
            }
            if (fromDate != null && toDate != null) {
                String granularity = APIUsageStatisticsClientConstants.HOURS_GRANULARITY;//default granularity

                Map<String, Integer> durationBreakdown = this.getDurationBreakdown(fromDate, toDate);

                if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_YEARS) > 0) {
                    granularity = APIUsageStatisticsClientConstants.YEARS_GRANULARITY;
                } else if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_MONTHS) > 0) {
                    granularity = APIUsageStatisticsClientConstants.MONTHS_GRANULARITY;
                } else if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_DAYS) > 0) {
                    granularity = APIUsageStatisticsClientConstants.DAYS_GRANULARITY;
                }
                query.append(
                        ") within " + getTimestamp(fromDate) + "L, " + getTimestamp(toDate) + "L per '" + granularity
                                + "' select sum(" + APIUsageStatisticsClientConstants.TOTAL_COUNT);
            } else {
                query.append(") within " + 0 + "L, " + new Date().getTime() + "L per 'months' select sum("
                        + APIUsageStatisticsClientConstants.TOTAL_COUNT);
            }
            query.append(") as count, " + APIUsageStatisticsClientConstants.COUNTRY);
            if (!"ALL".equals(drillDown)) {
                query.append(", " + APIUsageStatisticsClientConstants.CITY);
            }
            query.append(" group by " + APIUsageStatisticsClientConstants.COUNTRY);
            if (!"ALL".equals(drillDown)) {
                query.append(", " + APIUsageStatisticsClientConstants.CITY);
            }
            query.append(";");
            JSONObject jsonObj = APIUtil
                    .executeQueryOnStreamProcessor(APIUsageStatisticsClientConstants.APIM_ACCESS_SUMMARY_SIDDHI_APP,
                            query.toString());
            long count;
            String country;
            String city;
            if (jsonObj != null) {
                JSONArray jArray = (JSONArray) jsonObj.get(APIUsageStatisticsClientConstants.RECORDS_DELIMITER);
                for (Object record : jArray) {
                    JSONArray recordArray = (JSONArray) record;
                    if (recordArray.size() >= 2) {
                        Result<PerGeoLocationUsageCount> result1 = new Result<PerGeoLocationUsageCount>();
                        count = (Long) recordArray.get(0);
                        country = (String) recordArray.get(1);
                        List<String> facetValues = new ArrayList<String>();
                        facetValues.add(country);
                        if (!"ALL".equals(drillDown)) {
                            city = (String) recordArray.get(2);
                            facetValues.add(city);
                        }
                        PerGeoLocationUsageCount perGeoLocationUsageCount = new PerGeoLocationUsageCount((int) count,
                                facetValues);
                        result1.setValues(perGeoLocationUsageCount);
                        result1.setTableName(APIUsageStatisticsClientConstants.GEO_LOCATION_AGG);
                        result1.setTimestamp(RestClientUtil.longToDate(new Date().getTime()));
                        result.add(result1);
                    }
                }
            }
        } catch (APIManagementException e) {
            handleException("Error occurred while querying from Stream Processor ", e);
        }
        return result;

    }

    @Override
    public List<Result<UserAgentUsageCount>> getUserAgentUsageByAPI(String apiName, String version, String tenantDomain,
            String fromDate, String toDate, String drillDown) throws APIMgtUsageQueryServiceClientException {
        List<Result<UserAgentUsageCount>> result = new ArrayList<Result<UserAgentUsageCount>>();
        try {
            StringBuilder query = new StringBuilder(
                    "from " + APIUsageStatisticsClientConstants.API_USER_BROWSER_AGG + " on("
                            + APIUsageStatisticsClientConstants.API_NAME + "=='" + apiName + "'");
            if (version != null && !"ALL".equals(version)) {
                query.append(" AND " + APIUsageStatisticsClientConstants.API_VERSION + "=='" + version + "'");
            }
            if (tenantDomain != null) {
                query.append(
                        " AND " + APIUsageStatisticsClientConstants.API_CREATOR_TENANT_DOMAIN + "=='" + tenantDomain
                                + "'");
            }
            if (!"ALL".equals(drillDown)) {
                query.append(" AND " + APIUsageStatisticsClientConstants.OPERATING_SYSTEM + "=='" + drillDown + "'");
            }
            if (fromDate != null && toDate != null) {
                String granularity = APIUsageStatisticsClientConstants.HOURS_GRANULARITY;//default granularity

                Map<String, Integer> durationBreakdown = this.getDurationBreakdown(fromDate, toDate);

                if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_YEARS) > 0) {
                    granularity = APIUsageStatisticsClientConstants.YEARS_GRANULARITY;
                } else if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_MONTHS) > 0) {
                    granularity = APIUsageStatisticsClientConstants.MONTHS_GRANULARITY;
                } else if (durationBreakdown.get(APIUsageStatisticsClientConstants.DURATION_DAYS) > 0) {
                    granularity = APIUsageStatisticsClientConstants.DAYS_GRANULARITY;
                }
                query.append(
                        ") within " + getTimestamp(fromDate) + "L, " + getTimestamp(toDate) + "L per '" + granularity
                                + "' select sum(" + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT);
            } else {
                query.append(") within " + 0 + "L, " + new Date().getTime() + "L per 'months' select sum("
                        + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT);
            }
            query.append(") as count, " + APIUsageStatisticsClientConstants.OPERATING_SYSTEM + ", "
                    + APIUsageStatisticsClientConstants.BROWSER + " group by "
                    + APIUsageStatisticsClientConstants.OPERATING_SYSTEM + ", "
                    + APIUsageStatisticsClientConstants.BROWSER + ";");

            JSONObject jsonObj = APIUtil
                    .executeQueryOnStreamProcessor(APIUsageStatisticsClientConstants.APIM_ACCESS_SUMMARY_SIDDHI_APP,
                            query.toString());
            long count;
            String operatingSystem;
            String browser;
            if (jsonObj != null) {
                JSONArray jArray = (JSONArray) jsonObj.get(APIUsageStatisticsClientConstants.RECORDS_DELIMITER);
                for (Object record : jArray) {
                    JSONArray recordArray = (JSONArray) record;
                    if (recordArray.size() == 3) {
                        Result<UserAgentUsageCount> result1 = new Result<UserAgentUsageCount>();
                        count = (Long) recordArray.get(0);
                        operatingSystem = (String) recordArray.get(1);
                        browser = (String) recordArray.get(2);
                        List<String> facetValues = new ArrayList<String>();
                        facetValues.add(operatingSystem);
                        facetValues.add(browser);
                        UserAgentUsageCount perUserAgentUsageCount = new UserAgentUsageCount((int) count, facetValues);
                        result1.setValues(perUserAgentUsageCount);
                        result1.setTableName(APIUsageStatisticsClientConstants.API_USER_BROWSER_AGG);
                        result1.setTimestamp(RestClientUtil.longToDate(new Date().getTime()));
                        result.add(result1);
                    }
                }
            }
        } catch (APIManagementException e) {
            handleException("Error occurred while querying from Stream Processor ", e);
        }
        return result;
    }

    /**
     * This method is used to get the breakdown of the duration between 2 days/timestamps in terms of years,
     * months, days, hours, minutes and seconds
     *
     * @param fromDate Start timestamp of the duration
     * @param toDate   End timestamp of the duration
     * @return A map containing the breakdown
     * @throws APIMgtUsageQueryServiceClientException when there is an error during date parsing
     */
    private Map<String, Integer> getDurationBreakdown(String fromDate, String toDate)
            throws APIMgtUsageQueryServiceClientException {
        Map<String, Integer> durationBreakdown = new HashMap<String, Integer>();

        DateTimeFormatter formatter = DateTimeFormat.forPattern(APIUsageStatisticsClientConstants.TIMESTAMP_PATTERN);
        LocalDateTime startDate = LocalDateTime.parse(fromDate, formatter);
        LocalDateTime endDate = LocalDateTime.parse(toDate, formatter);
        Period period = new Period(startDate, endDate);
        int numOfYears = period.getYears();
        int numOfMonths = period.getMonths();
        int numOfWeeks = period.getWeeks();
        int numOfDays = period.getDays();
        if (numOfWeeks > 0) {
            numOfDays += numOfWeeks * 7;
        }
        int numOfHours = period.getHours();
        int numOfMinutes = period.getMinutes();
        int numOfSeconds = period.getSeconds();
        durationBreakdown.put(APIUsageStatisticsClientConstants.DURATION_YEARS, numOfYears);
        durationBreakdown.put(APIUsageStatisticsClientConstants.DURATION_MONTHS, numOfMonths);
        durationBreakdown.put(APIUsageStatisticsClientConstants.DURATION_DAYS, numOfDays);
        durationBreakdown.put(APIUsageStatisticsClientConstants.DURATION_WEEKS, numOfWeeks);
        durationBreakdown.put(APIUsageStatisticsClientConstants.DURATION_HOURS, numOfHours);
        durationBreakdown.put(APIUsageStatisticsClientConstants.DURATION_MINUTES, numOfMinutes);
        durationBreakdown.put(APIUsageStatisticsClientConstants.DURATION_SECONDS, numOfSeconds);
        return durationBreakdown;
    }

    private long getTimestamp(String date) throws APIMgtUsageQueryServiceClientException {
       
        SimpleDateFormat formatter = new SimpleDateFormat(APIUsageStatisticsClientConstants.TIMESTAMP_PATTERN);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        long time = 0;
        Date parsedDate = null;
        try {
            parsedDate = formatter.parse(date);
            time = parsedDate.getTime();
        } catch (ParseException e) {
            handleException("Error while parsing the date ", e);
        }
        return time;
    }
}
