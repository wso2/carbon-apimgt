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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
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
import org.wso2.carbon.apimgt.impl.internal.APIManagerComponent;
import org.wso2.carbon.apimgt.usage.client.APIUsageStatisticsClient;
import org.wso2.carbon.apimgt.usage.client.APIUsageStatisticsClientConstants;
import org.wso2.carbon.apimgt.usage.client.bean.*;
import org.wso2.carbon.apimgt.usage.client.billing.APIUsageRangeCost;
import org.wso2.carbon.apimgt.usage.client.billing.PaymentPlan;
import org.wso2.carbon.apimgt.usage.client.dto.*;
import org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException;
import org.wso2.carbon.apimgt.usage.client.internal.APIUsageClientServiceComponent;
import org.wso2.carbon.apimgt.usage.client.pojo.*;
import org.wso2.carbon.apimgt.usage.client.DASRestClient;
import org.wso2.carbon.apimgt.usage.client.util.RestClientUtil;
import org.wso2.carbon.application.mgt.stub.upload.CarbonAppUploaderStub;
import org.wso2.carbon.application.mgt.stub.upload.types.carbon.UploadedFileItem;
import org.wso2.carbon.bam.service.data.publisher.conf.RESTAPIConfigData;
import org.wso2.carbon.bam.service.data.publisher.services.ServiceDataPublisherAdmin;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.activation.DataHandler;
import javax.sql.DataSource;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class APIUsageStatisticsRestClientImpl extends APIUsageStatisticsClient {

    private static volatile DataSource dataSource = null;
    private static PaymentPlan paymentPlan;
    private APIProvider apiProviderImpl;
    private static final Log log = LogFactory.getLog(APIUsageStatisticsRestClientImpl.class);
    private DASRestClient restClient;
    private final Gson gson = new Gson();

    public APIUsageStatisticsRestClientImpl(String username) throws APIMgtUsageQueryServiceClientException {
        OMElement element = null;
        APIManagerConfiguration config;
        APIManagerAnalyticsConfiguration apiManagerAnalyticsConfiguration;
        try {
            config = APIUsageClientServiceComponent.getAPIManagerConfiguration();
            apiManagerAnalyticsConfiguration = APIManagerAnalyticsConfiguration.getInstance();

            if (!apiManagerAnalyticsConfiguration.isAnalyticsEnabled()) {
                throw new APIMgtUsageQueryServiceClientException("Analytics not enabled");
            }

            if (restClient == null) {
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
            if (targetEndpoint == null || targetEndpoint.equals(""))
                throw new APIMgtUsageQueryServiceClientException("Required BAM server URL parameter unspecified");
            apiProviderImpl = APIManagerFactory.getInstance().getAPIProvider(username);

        } catch (Exception e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while instantiating API manager core objects",
                    e);
        }

    }/**/

    public void initializeDataSource() throws APIMgtUsageQueryServiceClientException {
        ServiceDataPublisherAdmin serviceDataPublisherAdmin = APIManagerComponent.getDataPublisherAdminService();
        if (serviceDataPublisherAdmin != null) {
            if (serviceDataPublisherAdmin.getEventingConfigData().isServiceStatsEnable()) {
                RESTAPIConfigData restData = serviceDataPublisherAdmin.getRestAPIConfigData();
                String url = restData.getUrl();
                String user = restData.getUserName();
                String pass = restData.getPassword();
                restClient = new DASRestClient(url, user, pass);
                log.info("Initialised DASRestClient");
            }
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

    // Store Statistic Methods

    @Override public List<PerAppApiCountDTO> perAppPerAPIUsage(String subscriberName, String groupId, String fromDate,
            String toDate, int limit) throws APIMgtUsageQueryServiceClientException {

        List<String> subscriberApps = getAppsBySubscriber(subscriberName, groupId);
        String firstKey;

        int size = subscriberApps.size();
        if (size > 0) {
            firstKey = APIUsageStatisticsClientConstants.CONSUMERKEY + ":" + subscriberApps.get(0);
        } else {
            return new ArrayList<PerAppApiCountDTO>();
        }
        StringBuilder concatenatedKeys = new StringBuilder(firstKey);
        for (int i = 1; i < subscriberApps.size(); i++) {
            concatenatedKeys
                    .append(" OR " + APIUsageStatisticsClientConstants.CONSUMERKEY + ':' + subscriberApps.get(i));
        }

        List<PerAppApiCountDTO> usage = getPerAppAPIUsageData(APIUsageStatisticsClientConstants.API_REQUEST_SUMMARY,
                concatenatedKeys.toString(), fromDate, toDate, limit);
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

        String query = null;

        try {
            query = "max_request_time: [" + RestClientUtil.dateToLong(fromDate) + " TO " + RestClientUtil
                    .dateToLong(toDate) + "] AND ( " + keyString + " )";
        } catch (ParseException e) {
            handleException("Error occurred while Error parsing date", e);
        }

        RequestSearchBean request = new RequestSearchBean(query, 1, "key_api_facet", tableName);

        ArrayList<AggregateField> fields = new ArrayList<AggregateField>();
        AggregateField f = new AggregateField("total_request_count", "SUM", "count");
        fields.add(f);
        request.setAggregateFields(fields);

        Type ty = new TypeToken<List<Result<PerAppPerAPIUsageValues>>>() {
        }.getType();

        List<Result<PerAppPerAPIUsageValues>> obj = null;

        try {
            obj = restClient.sendAndGetPost(request, ty);
        } catch (JsonSyntaxException e) {
            handleException("Error occurred while parsing response", e);
        } catch (IOException e) {
            handleException("Error occurred while Connecting to DAS REST API", e);
        }

        List<PerAppApiCountDTO> perAppUsageDataList = new ArrayList<PerAppApiCountDTO>();

        if (obj == null || obj.isEmpty()) {
            return perAppUsageDataList;
        }

        PerAppApiCountDTO apiUsageDTO;
        for (Result<PerAppPerAPIUsageValues> result : obj) {
            PerAppPerAPIUsageValues v = result.getValues();

            String appName = subscriberAppsMap.get(v.getColumnNames().get(0));

            boolean found = false;
            for (PerAppApiCountDTO dto : perAppUsageDataList) {
                if (dto.getAppName().equals(appName)) {
                    dto.addToApiCountArray(v.getColumnNames().get(1), v.getCount_sum());
                    found = true;
                    break;
                }
            }

            if (!found) {
                apiUsageDTO = new PerAppApiCountDTO();
                apiUsageDTO.setAppName(appName);
                apiUsageDTO.addToApiCountArray(v.getColumnNames().get(1), v.getCount_sum());
                perAppUsageDataList.add(apiUsageDTO);
            }

        }

        return perAppUsageDataList;
    }

    @Override public List<AppUsageDTO> getTopAppUsers(String subscriberName, String groupId, String fromDate,
            String toDate, int limit) throws APIMgtUsageQueryServiceClientException {

        List<String> subscriberApps = getAppsBySubscriber(subscriberName, groupId);
        String firstKey;

        int size = subscriberApps.size();
        if (size > 0) {
            firstKey = APIUsageStatisticsClientConstants.CONSUMERKEY + ":" + subscriberApps.get(0);
        } else {
            return new ArrayList<AppUsageDTO>();
        }
        StringBuilder concatenatedKeys = new StringBuilder(firstKey);
        for (int i = 1; i < subscriberApps.size(); i++) {
            concatenatedKeys
                    .append(" OR " + APIUsageStatisticsClientConstants.CONSUMERKEY + ':' + subscriberApps.get(i));
        }

        List<AppUsageDTO> usage = getTopAppUsageData(APIUsageStatisticsClientConstants.API_REQUEST_SUMMARY,
                concatenatedKeys.toString(), fromDate, toDate, limit);
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

        String query = null;

        try {
            query = "max_request_time: [" + RestClientUtil.dateToLong(fromDate) + " TO " + RestClientUtil
                    .dateToLong(toDate) + "] AND ( " + keyString + " )";
        } catch (ParseException e) {
            handleException("Error occurred while Error parsing date", e);
        }

        RequestSearchBean request = new RequestSearchBean(query, 1, "key_userId_facet", tableName);

        ArrayList<AggregateField> fields = new ArrayList<AggregateField>();
        AggregateField f = new AggregateField("total_request_count", "SUM", "count");
        fields.add(f);
        request.setAggregateFields(fields);

        Type ty = new TypeToken<List<Result<TopAppUsersValues>>>() {
        }.getType();

        List<Result<TopAppUsersValues>> obj = null;

        try {
            obj = restClient.sendAndGetPost(request, ty);
        } catch (JsonSyntaxException e) {
            handleException("Error occurred while parsing response", e);
        } catch (IOException e) {
            handleException("Error occurred while Connecting to DAS REST API", e);
        }

        List<AppUsageDTO> topAppUsageDataList = new ArrayList<AppUsageDTO>();

        if (obj == null || obj.isEmpty()) {
            return topAppUsageDataList;
        }

        AppUsageDTO appUsageDTO;
        for (Result<TopAppUsersValues> result : obj) {
            TopAppUsersValues v = result.getValues();

            String appName = subscriberAppsMap.get(v.getColumnNames().get(0));

            boolean found = false;
            for (AppUsageDTO dto : topAppUsageDataList) {
                if (dto.getAppName().equals(appName)) {
                    dto.addToUserCountArray(v.getColumnNames().get(1), v.getCount());
                    found = true;
                    break;
                }
            }

            if (!found) {
                appUsageDTO = new AppUsageDTO();
                appUsageDTO.setAppName(appName);
                appUsageDTO.addToUserCountArray(v.getColumnNames().get(1), v.getCount());
                topAppUsageDataList.add(appUsageDTO);
            }

        }

        return topAppUsageDataList;
    }

    @Override public List<AppCallTypeDTO> getAppApiCallType(String subscriberName, String groupId, String fromDate,
            String toDate, int limit) throws APIMgtUsageQueryServiceClientException {

        List<String> subscriberApps = getAppsBySubscriber(subscriberName, groupId);
        String firstKey;

        int size = subscriberApps.size();
        if (size > 0) {
            firstKey = APIUsageStatisticsClientConstants.CONSUMERKEY + ":" + subscriberApps.get(0);
        } else {
            return new ArrayList<AppCallTypeDTO>();
        }
        StringBuilder concatenatedKeys = new StringBuilder(firstKey);
        for (int i = 1; i < subscriberApps.size(); i++) {
            concatenatedKeys
                    .append(" OR " + APIUsageStatisticsClientConstants.CONSUMERKEY + ':' + subscriberApps.get(i));
        }

        List<AppCallTypeDTO> usage = getAPICallTypeUsageData(
                APIUsageStatisticsClientConstants.API_Resource_Path_USAGE_SUMMARY, concatenatedKeys.toString(),
                fromDate, toDate, limit);

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

        String query = null;

        try {
            query = "max_request_time: [" + RestClientUtil.dateToLong(fromDate) + " TO " + RestClientUtil
                    .dateToLong(toDate) + "] AND ( " + keyString + " )";
        } catch (ParseException e) {
            handleException("Error occurred while Error parsing date", e);
        }

        RequestSearchBean request = new RequestSearchBean(query, 3, "key_api_method_path_facet", tableName);
        ArrayList<AggregateField> fields = new ArrayList<AggregateField>();
        AggregateField f = new AggregateField("total_request_count", "SUM", "count");
        fields.add(f);
        request.setAggregateFields(fields);

        Type ty = new TypeToken<List<Result<AppApiCallTypeValues>>>() {
        }.getType();

        List<Result<AppApiCallTypeValues>> obj = null;

        try {
            obj = restClient.sendAndGetPost(request, ty);
        } catch (JsonSyntaxException e) {
            handleException("Error occurred while parsing response", e);
        } catch (IOException e) {
            handleException("Error occurred while Connecting to DAS REST API", e);
        }

        List<AppCallTypeDTO> appApiCallTypeList = new ArrayList<AppCallTypeDTO>();

        if (obj == null || obj.isEmpty()) {
            return appApiCallTypeList;
        }

        AppCallTypeDTO appCallTypeDTO;
        for (Result<AppApiCallTypeValues> result : obj) {
            AppApiCallTypeValues v = result.getValues();

            List<String> callTypeList = new ArrayList<String>();
            callTypeList.add(v.getColumnNames().get(3) + " (" + v.getColumnNames().get(2) + ")");

            String appName = subscriberAppsMap.get(v.getColumnNames().get(0));

            boolean found = false;
            for (AppCallTypeDTO dto : appApiCallTypeList) {
                if (dto.getAppName().equals(appName)) {
                    dto.addGToApiCallTypeArray(v.getColumnNames().get(1), callTypeList);
                    found = true;
                    break;
                }
            }

            if (!found) {
                appCallTypeDTO = new AppCallTypeDTO();
                appCallTypeDTO.setAppName(appName);
                appCallTypeDTO.addGToApiCallTypeArray(v.getColumnNames().get(1), callTypeList);
                appApiCallTypeList.add(appCallTypeDTO);
            }

        }

        return appApiCallTypeList;
    }

    @Override public List<FaultCountDTO> getPerAppAPIFaultCount(String subscriberName, String groupId, String fromDate,
            String toDate, int limit) throws APIMgtUsageQueryServiceClientException {

        List<String> subscriberApps = getAppsBySubscriber(subscriberName, groupId);
        String firstKey;

        int size = subscriberApps.size();
        if (size > 0) {
            firstKey = APIUsageStatisticsClientConstants.CONSUMERKEY + ":" + subscriberApps.get(0);
        } else {
            return new ArrayList<FaultCountDTO>();
        }
        StringBuilder concatenatedKeys = new StringBuilder(firstKey);
        for (int i = 1; i < subscriberApps.size(); i++) {
            concatenatedKeys
                    .append(" OR " + APIUsageStatisticsClientConstants.CONSUMERKEY + ':' + subscriberApps.get(i));
        }

        List<FaultCountDTO> usage = getFaultAppUsageData(APIUsageStatisticsClientConstants.API_FAULT_SUMMARY,
                concatenatedKeys.toString(), fromDate, toDate, limit);
        return usage;
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

        String query = null;

        try {
            query = "max_request_time: [" + RestClientUtil.dateToLong(fromDate) + " TO " + RestClientUtil
                    .dateToLong(toDate) + "] AND ( " + keyString + " )";
        } catch (ParseException e) {
            handleException("Error occurred while Error parsing date", e);
        }

        RequestSearchBean request = new RequestSearchBean(query, 1, "consumerKey_api_facet", "API_FAULT_SUMMARY");
        ArrayList<AggregateField> fields = new ArrayList<AggregateField>();
        AggregateField f = new AggregateField("total_fault_count", "SUM", "count");
        fields.add(f);
        request.setAggregateFields(fields);

        Type ty = new TypeToken<List<Result<FaultAppUsageDataValue>>>() {
        }.getType();

        List<Result<FaultAppUsageDataValue>> obj = null;

        try {
            obj = restClient.sendAndGetPost(request, ty);
        } catch (JsonSyntaxException e) {
            handleException("Error occurred while parsing response", e);
        } catch (IOException e) {
            handleException("Error occurred while Connecting to DAS REST API", e);
        }

        List<FaultCountDTO> falseAppUsageDataList = new ArrayList<FaultCountDTO>();

        if (obj == null || obj.isEmpty()) {
            return falseAppUsageDataList;
        }

        FaultCountDTO faultCountDTO;
        for (Result<FaultAppUsageDataValue> result : obj) {
            FaultAppUsageDataValue v = result.getValues();

            String appName = subscriberAppsMap.get(v.getColumnNames().get(0));
            String apiName = v.getColumnNames().get(1);

            boolean found = false;
            for (FaultCountDTO dto : falseAppUsageDataList) {
                if (dto.getAppName().equals(appName)) {
                    dto.addToApiFaultCountArray(apiName, v.getCount());
                    found = true;
                    break;
                }
            }

            if (!found) {
                faultCountDTO = new FaultCountDTO();
                faultCountDTO.setAppName(appName);
                faultCountDTO.addToApiFaultCountArray(apiName, v.getCount());
                falseAppUsageDataList.add(faultCountDTO);
            }

        }

        return falseAppUsageDataList;
    }

    // Publisher Statistic Methods

    /**
     * Returns a list of APIUsageByUserDTO objects that contain information related to
     * User wise API Usage, along with the number of invocations, and API Version
     *
     * @param providerName Name of the API provider
     * @return a List of APIUsageByUserDTO objects, possibly empty
     * @throws org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException on error
     */
    @Override public List<APIUsageByUserDTO> getAPIUsageByUser(String providerName, String fromDate, String toDate)
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
        //        return gson.toJson(usageByName);
        return usageByName;
    }

    private List<APIUsageByUserName> getAPIUsageByUserData(String providerName, String fromDate, String toDate,
            Integer limit) throws APIMgtUsageQueryServiceClientException {

        String query = null;

        try {
            query = "max_request_time: [" + RestClientUtil.getFloorDateAsLong(fromDate) + " TO " + RestClientUtil
                    .getCeilingDateAsLong(toDate) + "]";
        } catch (ParseException e) {
            handleException("Error occurred while Error parsing date", e);
        }

        RequestSearchBean request = new RequestSearchBean(query, 3, "api_version_userId_apiPublisher_facet",
                "API_REQUEST_SUMMARY");

        ArrayList<AggregateField> fields = new ArrayList<AggregateField>();
        AggregateField f = new AggregateField("total_request_count", "SUM", "count");
        fields.add(f);
        request.setAggregateFields(fields);

        Type ty = new TypeToken<List<Result<APIUsageByUserValues>>>() {
        }.getType();

        List<Result<APIUsageByUserValues>> obj = null;

        try {
            obj = restClient.sendAndGetPost(request, ty);
        } catch (JsonSyntaxException e) {
            handleException("Error occurred while parsing response", e);
        } catch (IOException e) {
            handleException("Error occurred while Connecting to DAS REST API", e);
        }

        List<APIUsageByUserName> usageByName = new ArrayList<APIUsageByUserName>();

        if (obj == null || obj.isEmpty()) {
            return usageByName;
        }

        APIUsageByUserName usage;
        for (Result<APIUsageByUserValues> result : obj) {
            APIUsageByUserValues v = result.getValues();

            usage = new APIUsageByUserName();
            usage.setRequestCount(v.getCount_sum());

            usage.setApiName(v.getColumnNames().get(0));
            usage.setApiVersion(v.getColumnNames().get(1));
            usage.setUserID(v.getColumnNames().get(2));
            usage.setApipublisher(v.getColumnNames().get(3));

            usageByName.add(usage);
        }

        return usageByName;
        /*if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure " +
                    "that the data source is properly configured in the APIUsageTracker configuration.");
        }*/
    }

    /**
     * Gets a list of APIResponseTimeDTO objects containing information related to APIs belonging
     * to a particular provider along with their average response times.
     *
     * @param providerName Name of the API provider
     * @return a List of APIResponseTimeDTO objects, possibly empty
     * @throws org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException on error
     */
    @Override public List<APIResponseTimeDTO> getProviderAPIServiceTime(String providerName, String fromDate,
            String toDate, int limit) throws APIMgtUsageQueryServiceClientException {

        Collection<APIResponseTime> responseTimes = getAPIResponseTimeData(
                APIUsageStatisticsClientConstants.API_VERSION_SERVICE_TIME_SUMMARY, fromDate, toDate, limit);
        List<API> providerAPIs = getAPIsByProvider(providerName);
        //        Map<String, Double> apiCumulativeServiceTimeMap = new HashMap<String, Double>();
        //        Map<String, Long> apiUsageMap = new TreeMap<String, Long>();
        DecimalFormat format = new DecimalFormat("#.##");
        List<APIResponseTimeDTO> apiResponseTimeUsage = new ArrayList<APIResponseTimeDTO>();

        for (APIResponseTime responseTime : responseTimes) {
            for (API providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(responseTime.getApiName()) &&
                        providerAPI.getId().getVersion().equals(responseTime.getApiVersion()) &&
                        providerAPI.getContext().equals(responseTime.getContext())) {

                    /*String apiName = responseTime.apiName + " (" + providerAPI.getId().getProviderName() + ")";
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
                    }*/
                    APIResponseTimeDTO responseTimeDTO = new APIResponseTimeDTO();
                    responseTimeDTO.setApiName(responseTime.getApiName());
                    double avgTime = responseTime.getResponseTime() / responseTime.getResponseCount();
                    responseTimeDTO.setServiceTime(Double.parseDouble(format.format(avgTime)));
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
        }
        return getResponseTimeTopEntries(new ArrayList<APIResponseTimeDTO>(responseTimeByAPI.values()), limit);*/
        return apiResponseTimeUsage;
    }

    /**
     * This method gets the response times for APIs
     *
     * @param tableName name of the required table in the database
     * @return a collection containing the data related to API response times
     * @throws APIMgtUsageQueryServiceClientException if an error occurs while querying the database
     */
    private Collection<APIResponseTime> getAPIResponseTimeData(String tableName, String fromDate, String toDate,
            int limit) throws APIMgtUsageQueryServiceClientException {

        String query = null;

        try {
            query = "max_request_time: [" + RestClientUtil.getFloorDateAsLong(fromDate) + " TO " + RestClientUtil
                    .getCeilingDateAsLong(toDate) + "]";
        } catch (ParseException e) {
            handleException("Error occurred while Error parsing date", e);
        }

        RequestSearchBean request = new RequestSearchBean(query, 2, "api_version_context_facet",
                "API_RESPONSE_SUMMARY");

        ArrayList<AggregateField> fields = new ArrayList<AggregateField>();
        AggregateField f0 = new AggregateField("serviceTime", "SUM", "totalServiceTime");
        AggregateField f1 = new AggregateField("total_response_count", "SUM", "totalResponseCount");
        fields.add(f0);
        fields.add(f1);
        request.setAggregateFields(fields);

        Type ty = new TypeToken<List<Result<ResponseTimesByAPIsValue>>>() {
        }.getType();

        List<Result<ResponseTimesByAPIsValue>> obj = null;

        try {
            obj = restClient.sendAndGetPost(request, ty);
        } catch (JsonSyntaxException e) {
            handleException("Error occurred while parsing response", e);
        } catch (IOException e) {
            handleException("Error occurred while Connecting to DAS REST API", e);
        }

        List<APIResponseTime> responseTimeData = new ArrayList<APIResponseTime>();

        if (obj == null || obj.isEmpty()) {
            return responseTimeData;
        }

        APIResponseTime usage;
        for (Result<ResponseTimesByAPIsValue> result : obj) {
            ResponseTimesByAPIsValue v = result.getValues();

            usage = new APIResponseTime();
            usage.setApiName(v.getColumnNames().get(0));
            usage.setApiVersion(v.getColumnNames().get(1));
            usage.setContext(v.getColumnNames().get(2));
            usage.setResponseTime(v.getTotalServiceTime());
            usage.setResponseCount(v.getTotalResponseCount());

            responseTimeData.add(usage);
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
    @Override public List<APIVersionLastAccessTimeDTO> getProviderAPIVersionUserLastAccess(String providerName,
            String fromDate, String toDate, int limit) throws APIMgtUsageQueryServiceClientException {

        Collection<APIAccessTime> accessTimes = getLastAccessTimesByAPIData(
                APIUsageStatisticsClientConstants.API_VERSION_KEY_LAST_ACCESS_SUMMARY, fromDate, toDate, limit);
        List<API> providerAPIs = getAPIsByProvider(providerName);
        //        Map<String, APIAccessTime> lastAccessTimes = new TreeMap<String, APIAccessTime>();
        List<APIVersionLastAccessTimeDTO> apiVersionLastAccessTimeUsage = new ArrayList<APIVersionLastAccessTimeDTO>();

        APIVersionLastAccessTimeDTO accessTimeDTO;
        String apiName;

        for (APIAccessTime accessTime : accessTimes) {
            for (API providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(accessTime.getApiName()) &&
                        providerAPI.getId().getVersion().equals(accessTime.getApiVersion()) &&
                        providerAPI.getContext().equals(accessTime.getContext())) {

                    /*String apiName = accessTime.apiName + " (" + providerAPI.getId().getProviderName() + ")";
                    APIAccessTime lastAccessTime = lastAccessTimes.get(apiName);
                    if (lastAccessTime == null || lastAccessTime.accessTime < accessTime.accessTime) {
                        lastAccessTimes.put(apiName, accessTime);
                        break;
                    }*/
                    apiName = accessTime.getApiName() + " (" + providerAPI.getId().getProviderName() + ")";
                    accessTimeDTO = new APIVersionLastAccessTimeDTO();
                    accessTimeDTO.setApiName(apiName);
                    accessTimeDTO.setApiVersion(accessTime.getApiVersion());
                    accessTimeDTO.setUser(accessTime.getUsername());
                    accessTimeDTO.setLastAccessTime(accessTime.getAccessTime() + "");
                    apiVersionLastAccessTimeUsage.add(accessTimeDTO);
                }
            }
        }

        /*Map<String, APIVersionLastAccessTimeDTO> accessTimeByAPI = new TreeMap<String, APIVersionLastAccessTimeDTO>();
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
        return getLastAccessTimeTopEntries(new ArrayList<APIVersionLastAccessTimeDTO>(accessTimeByAPI.values()), limit);*/
        return apiVersionLastAccessTimeUsage;
    }

    /**
     * This method gets the last access times for APIs
     *
     * @param tableName name of the required table in the database
     * @return a collection containing the data related to API last access times
     * @throws APIMgtUsageQueryServiceClientException if an error occurs while querying the database
     */
    private Collection<APIAccessTime> getLastAccessTimesByAPIData(String tableName, String fromDate, String toDate,
            int limit) throws APIMgtUsageQueryServiceClientException {

        String query = null;

        try {
            query = "max_request_time: [" + RestClientUtil.getFloorDateAsLong(fromDate) + " TO " + RestClientUtil
                    .getCeilingDateAsLong(toDate) + "]";
        } catch (ParseException e) {
            handleException("Error occurred while Error parsing date", e);
        }

        RequestSearchBean request = new RequestSearchBean(query, 3, "api_version_userId_context_facet",
                "API_REQUEST_SUMMARY");

        ArrayList<AggregateField> fields = new ArrayList<AggregateField>();
        AggregateField f0 = new AggregateField("max_request_time", "MAX", "lastAccessTime");
        fields.add(f0);
        request.setAggregateFields(fields);

        Type ty = new TypeToken<List<Result<LastAccessTimesByAPIValue>>>() {
        }.getType();

        List<Result<LastAccessTimesByAPIValue>> obj = null;

        try {
            obj = restClient.sendAndGetPost(request, ty);
        } catch (JsonSyntaxException e) {
            handleException("Error occurred while parsing response", e);
        } catch (IOException e) {
            handleException("Error occurred while Connecting to DAS REST API", e);
        }

        List<APIAccessTime> lastAccessTimeData = new ArrayList<APIAccessTime>();

        if (obj == null || obj.isEmpty()) {
            return lastAccessTimeData;
        }

        APIAccessTime usage;
        for (Result<LastAccessTimesByAPIValue> result : obj) {
            LastAccessTimesByAPIValue v = result.getValues();

            usage = new APIAccessTime();
            usage.setAccessTime(v.getLastAccessTime());
            usage.setApiName(v.getColumnNames().get(0));
            usage.setApiVersion(v.getColumnNames().get(1));
            usage.setUsername(v.getColumnNames().get(2));
            usage.setContext(v.getColumnNames().get(3));
            lastAccessTimeData.add(usage);
        }

        return lastAccessTimeData;

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
    @Override public List<APIResourcePathUsageDTO> getAPIUsageByResourcePath(String providerName, String fromDate,
            String toDate) throws APIMgtUsageQueryServiceClientException {

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
                    usageByResourcePath.add(usageDTO);
                }
            }
        }
        return usageByResourcePath;
    }

    private List<APIUsageByResourcePath> getAPIUsageByResourcePathData(String tableName, String fromDate, String toDate)
            throws APIMgtUsageQueryServiceClientException {

        String query = null;

        try {
            query = "max_request_time: [" + RestClientUtil.getFloorDateAsLong(fromDate) + " TO " + RestClientUtil
                    .getCeilingDateAsLong(toDate) + "]";
        } catch (ParseException e) {
            handleException("Error occurred while Error parsing date", e);
        }

        RequestSearchBean request = new RequestSearchBean(query, 3, "api_version_context_method_facet",
                "API_RESOURCE_USAGE_SUMMARY");

        ArrayList<AggregateField> fields = new ArrayList<AggregateField>();
        AggregateField f0 = new AggregateField("total_request_count", "SUM", "totalRequesCount");
        fields.add(f0);
        request.setAggregateFields(fields);

        Type ty = new TypeToken<List<Result<APIUsageByResourcePathValue>>>() {
        }.getType();

        List<Result<APIUsageByResourcePathValue>> obj = null;

        try {
            obj = restClient.sendAndGetPost(request, ty);
        } catch (JsonSyntaxException e) {
            handleException("Error occurred while parsing response", e);
        } catch (IOException e) {
            handleException("Error occurred while Connecting to DAS REST API", e);
        }

        List<APIUsageByResourcePath> usageByResourcePath = new ArrayList<APIUsageByResourcePath>();

        if (obj == null || obj.isEmpty()) {
            return usageByResourcePath;
        }

        APIUsageByResourcePath usage;
        for (Result<APIUsageByResourcePathValue> result : obj) {
            APIUsageByResourcePathValue v = result.getValues();

            usage = new APIUsageByResourcePath();
            usage.setRequestCount(v.getTotalRequesCount());
            usage.setApiName(v.getColumnNames().get(0));
            usage.setApiVersion(v.getColumnNames().get(1));
            usage.setContext(v.getColumnNames().get(2));
            usage.setMethod(v.getColumnNames().get(3));

            usageByResourcePath.add(usage);
        }

        return usageByResourcePath;

    }

    @Override public List<APIDestinationUsageDTO> getAPIUsageByDestination(String providerName, String fromDate,
            String toDate) throws APIMgtUsageQueryServiceClientException {

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

    private List<APIUsageByDestination> getAPIUsageByDestinationData(String tableName, String fromDate, String toDate)
            throws APIMgtUsageQueryServiceClientException {

        String query = null;

        try {
            query = "max_request_time: [" + RestClientUtil.getFloorDateAsLong(fromDate) + " TO " + RestClientUtil
                    .getCeilingDateAsLong(toDate) + "]";
        } catch (ParseException e) {
            handleException("Error occurred while Error parsing date", e);
        }

        RequestSearchBean request = new RequestSearchBean(query, 3, "api_version_context_dest_facet",
                "API_DESTINATION_SUMMARY");

        ArrayList<AggregateField> fields = new ArrayList<AggregateField>();
        AggregateField f0 = new AggregateField("total_request_count", "SUM", "totalRequestCount");
        fields.add(f0);
        request.setAggregateFields(fields);

        Type ty = new TypeToken<List<Result<APIUsageByDestinationValue>>>() {
        }.getType();

        List<Result<APIUsageByDestinationValue>> obj = null;

        try {
            obj = restClient.sendAndGetPost(request, ty);
        } catch (JsonSyntaxException e) {
            handleException("Error occurred while parsing response", e);
        } catch (IOException e) {
            handleException("Error occurred while Connecting to DAS REST API", e);
        }

        List<APIUsageByDestination> usageByResourcePath = new ArrayList<APIUsageByDestination>();

        if (obj == null || obj.isEmpty()) {
            return usageByResourcePath;
        }

        APIUsageByDestination usage;
        for (Result<APIUsageByDestinationValue> result : obj) {
            APIUsageByDestinationValue v = result.getValues();

            usage = new APIUsageByDestination();
            usage.setRequestCount(v.getTotalRequesCount());
            usage.setApiName(v.getColumnNames().get(0));
            usage.setApiVersion(v.getColumnNames().get(1));
            usage.setContext(v.getColumnNames().get(2));
            usage.setDestination(v.getColumnNames().get(3));

            usageByResourcePath.add(usage);
        }

        return usageByResourcePath;

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
    @Override public List<APIUsageDTO> getProviderAPIUsage(String providerName, String fromDate, String toDate,
            int limit) throws APIMgtUsageQueryServiceClientException {

        Collection<APIUsage> usageData = getUsageByAPIsData(APIUsageStatisticsClientConstants.API_VERSION_USAGE_SUMMARY,
                fromDate, toDate, limit);
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
    private Collection<APIUsage> getUsageByAPIsData(String tableName, String fromDate, String toDate, int limit)
            throws APIMgtUsageQueryServiceClientException {

        String query = null;

        try {
            query = "max_request_time: [" + RestClientUtil.getFloorDateAsLong(fromDate) + " TO " + RestClientUtil
                    .getCeilingDateAsLong(toDate) + "]";
        } catch (ParseException e) {
            handleException("Error occurred while Error parsing date", e);
        }

        RequestSearchBean request = new RequestSearchBean(query, 2, "api_version_context_facet",
                "API_VERSION_USAGE_SUMMARY");

        ArrayList<AggregateField> fields = new ArrayList<AggregateField>();
        AggregateField f0 = new AggregateField("total_request_count", "SUM", "totalRequestCount");
        fields.add(f0);
        request.setAggregateFields(fields);

        Type ty = new TypeToken<List<Result<UsageByAPIsValue>>>() {
        }.getType();

        List<Result<UsageByAPIsValue>> obj = null;

        try {
            obj = restClient.sendAndGetPost(request, ty);
        } catch (JsonSyntaxException e) {
            handleException("Error occurred while parsing response", e);
        } catch (IOException e) {
            handleException("Error occurred while Connecting to DAS REST API", e);
        }

        List<APIUsage> usageDataList = new ArrayList<APIUsage>();

        if (obj == null || obj.isEmpty()) {
            return usageDataList;
        }

        APIUsage usage;
        for (Result<UsageByAPIsValue> result : obj) {
            UsageByAPIsValue v = result.getValues();

            usage = new APIUsage();
            usage.setRequestCount(v.getTotalRequestCount());
            usage.setApiName(v.getColumnNames().get(0));
            usage.setApiVersion(v.getColumnNames().get(1));
            usage.setContext(v.getColumnNames().get(2));

            usageDataList.add(usage);
        }

        return usageDataList;
    }

    @Override public List<APIResponseFaultCountDTO> getAPIResponseFaultCount(String providerName, String fromDate,
            String toDate) throws APIMgtUsageQueryServiceClientException {

        List<APIResponseFaultCount> faultyData = this
                .getAPIResponseFaultCountData(APIUsageStatisticsClientConstants.API_FAULT_SUMMARY, fromDate, toDate);
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
                    if (apiVersionUsageList.size() > 0) {
                        apiVersionUsageDTO = apiVersionUsageList.get(0);
                        if (apiVersionUsageDTO.getVersion().equals(fault.getApiVersion())) {
                            long requestCount = apiVersionUsageDTO.getCount();
                            double faultPercentage =
                                    ((double) requestCount - fault.getFaultCount()) / requestCount * 100;
                            DecimalFormat twoDForm = new DecimalFormat("#.##");
                            faultPercentage = 100 - Double.valueOf(twoDForm.format(faultPercentage));
                            faultyDTO.setFaultPercentage(faultPercentage);
                            faultyDTO.setRequestCount(requestCount);
                        }
                    }
                    /*for (int i = 0; i < apiVersionUsageList.size(); i++) {
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
                    }*/

                    faultyCount.add(faultyDTO);

                }
            }
        }
        return faultyCount;
    }

    private List<APIResponseFaultCount> getAPIResponseFaultCountData(String tableName, String fromDate, String toDate)
            throws APIMgtUsageQueryServiceClientException {

        String query = null;

        try {
            query = "max_request_time: [" + RestClientUtil.getFloorDateAsLong(fromDate) + " TO " + RestClientUtil
                    .getCeilingDateAsLong(toDate) + "]";
        } catch (ParseException e) {
            handleException("Error occurred while Error parsing date", e);
        }

        RequestSearchBean request = new RequestSearchBean(query, 3, "api_version_apiPublisher_context_facet",
                "API_FAULT_SUMMARY");

        ArrayList<AggregateField> fields = new ArrayList<AggregateField>();
        AggregateField f0 = new AggregateField("total_fault_count", "SUM", "totalFaultCount");
        fields.add(f0);
        request.setAggregateFields(fields);

        Type ty = new TypeToken<List<Result<APIResponseFaultCountValue>>>() {
        }.getType();

        List<Result<APIResponseFaultCountValue>> obj = null;

        try {
            obj = restClient.sendAndGetPost(request, ty);
        } catch (JsonSyntaxException e) {
            handleException("Error occurred while parsing response", e);
        } catch (IOException e) {
            handleException("Error occurred while Connecting to DAS REST API", e);
        }

        List<APIResponseFaultCount> faultUsage = new ArrayList<APIResponseFaultCount>();

        if (obj == null || obj.isEmpty()) {
            return faultUsage;
        }

        APIResponseFaultCount usage;
        for (Result<APIResponseFaultCountValue> result : obj) {
            APIResponseFaultCountValue v = result.getValues();

            usage = new APIResponseFaultCount();
            usage.setFaultCount(v.getTotalFaultCount());
            usage.setApiName(v.getColumnNames().get(0));
            usage.setApiVersion(v.getColumnNames().get(1));
            usage.setContext(v.getColumnNames().get(3));

            faultUsage.add(usage);
        }

        return faultUsage;

    }

    //Throttling related Methods

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
    @Override public List<APIThrottlingOverTimeDTO> getThrottleDataOfAPIAndApplication(String apiName, String provider,
            String appName, String fromDate, String toDate, String groupBy)
            throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure "
                    + "that the data source is properly configured in the APIUsageTracker configuration.");
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
            if (isTableExist(APIUsageStatisticsClientConstants.API_THROTTLED_OUT_SUMMARY)) { //Table exists

                if (APIUsageStatisticsClientConstants.GROUP_BY_DAY.equals(groupBy)) {
                    groupByStmt = "year, month, day";
                } else if (APIUsageStatisticsClientConstants.GROUP_BY_HOUR.equals(groupBy)) {
                    groupByStmt = "year, month, day, time";
                } else {
                    throw new APIMgtUsageQueryServiceClientException("Unsupported group by parameter " + groupBy +
                            " for retrieving throttle data of API and app.");
                }

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
    @Override public List<APIThrottlingOverTimeDTO> getThrottleDataOfApplication(String appName, String provider,
            String fromDate, String toDate) throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure "
                    + "that the data source is properly configured in the APIUsageTracker configuration.");
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            String query = null;
            List<APIThrottlingOverTimeDTO> throttlingData = new ArrayList<APIThrottlingOverTimeDTO>();
            String tenantDomain = MultitenantUtils.getTenantDomain(provider);

            if (isTableExist(APIUsageStatisticsClientConstants.API_THROTTLED_OUT_SUMMARY)) { //Table exists

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

    /**
     * Get APIs of the provider that consist of throttle data
     *
     * @param provider Provider name
     * @return List of APIs of the provider that consist of throttle data
     * @throws APIMgtUsageQueryServiceClientException
     */
    @Override public List<String> getAPIsForThrottleStats(String provider)
            throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure "
                    + "that the data source is properly configured in the APIUsageTracker configuration.");
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            String query = null;
            List<String> throttlingAPIData = new ArrayList<String>();
            String tenantDomain = MultitenantUtils.getTenantDomain(provider);

            //check whether table exist first
            if (isTableExist(APIUsageStatisticsClientConstants.API_THROTTLED_OUT_SUMMARY)) { //Tables exist

                query = "SELECT DISTINCT api FROM API_THROTTLED_OUT_SUMMARY " +
                        "WHERE tenantDomain = ? " +
                        (provider.startsWith(APIUsageStatisticsClientConstants.ALL_PROVIDERS) ?
                                "" :
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

    /**
     * Given provider name and the API name, returns a list of applications through which the corresponding API is
     * invoked and which consist of success/throttled requests
     *
     * @param provider Provider name
     * @param apiName  Name of th API
     * @return A list of applications through which the corresponding API is invoked and which consist of throttle data
     * @throws APIMgtUsageQueryServiceClientException
     */
    @Override public List<String> getAppsForThrottleStats(String provider, String apiName)
            throws APIMgtUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new APIMgtUsageQueryServiceClientException("BAM data source hasn't been initialized. Ensure "
                    + "that the data source is properly configured in the APIUsageTracker configuration.");
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            String query = null;
            List<String> throttlingAppData = new ArrayList<String>();
            String tenantDomain = MultitenantUtils.getTenantDomain(provider);

            //check whether table exist first
            if (isTableExist(APIUsageStatisticsClientConstants.API_THROTTLED_OUT_SUMMARY)) { //Tables exist
                query = "SELECT DISTINCT applicationName FROM API_THROTTLED_OUT_SUMMARY " +
                        "WHERE tenantDomain = ? " +
                        (provider.startsWith(APIUsageStatisticsClientConstants.ALL_PROVIDERS) ?
                                "" :
                                "AND apiPublisher = ? ") +
                        (apiName == null ? "" : "AND api = ? ") +
                        "ORDER BY applicationName ASC";

                statement = connection.prepareStatement(query);
                int index = 1;
                statement.setString(index++, tenantDomain);
                if (!provider.startsWith(APIUsageStatisticsClientConstants.ALL_PROVIDERS)) {
                    statement.setString(index++, provider);
                }
                if (apiName != null) {
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
    @Override public List<APIVersionUsageDTO> getUsageByAPIVersions(String providerName, String apiName,
            String fromDate, String toDate) throws APIMgtUsageQueryServiceClientException {

        List<APIUsage> usageData = this
                .getUsageByAPIVersionsData(APIUsageStatisticsClientConstants.API_VERSION_USAGE_SUMMARY, fromDate,
                        toDate, apiName);
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

    private List<APIUsage> getUsageByAPIVersionsData(String tableName, String fromDate, String toDate, String apiName)
            throws APIMgtUsageQueryServiceClientException {

        String query = null;

        try {
            query = "api:" + apiName + " AND max_request_time: [" + RestClientUtil.getFloorDateAsLong(fromDate) + " TO "
                    + RestClientUtil.getCeilingDateAsLong(toDate) + "]";
        } catch (ParseException e) {
            handleException("Error occurred while Error parsing date", e);
        }

        RequestSearchBean request = new RequestSearchBean(query, 2, "api_version_context_facet",
                "API_VERSION_USAGE_SUMMARY");

        ArrayList<AggregateField> fields = new ArrayList<AggregateField>();
        AggregateField f0 = new AggregateField("total_request_count", "SUM", "totalRequestCount");
        fields.add(f0);
        request.setAggregateFields(fields);

        Type ty = new TypeToken<List<Result<UsageByAPIVersionsValue>>>() {
        }.getType();

        List<Result<UsageByAPIVersionsValue>> obj = null;

        try {
            obj = restClient.sendAndGetPost(request, ty);
        } catch (JsonSyntaxException e) {
            handleException("Error occurred while parsing response", e);
        } catch (IOException e) {
            handleException("Error occurred while Connecting to DAS REST API", e);
        }

        List<APIUsage> usageDataList = new ArrayList<APIUsage>();

        if (obj == null || obj.isEmpty()) {
            return usageDataList;
        }

        APIUsage usage;
        for (Result<UsageByAPIVersionsValue> result : obj) {
            UsageByAPIVersionsValue v = result.getValues();

            usage = new APIUsage();
            usage.setRequestCount(v.getTotalRequestCount());
            usage.setApiName(v.getColumnNames().get(0));
            usage.setApiVersion(v.getColumnNames().get(1));
            usage.setContext(v.getColumnNames().get(2));

            usageDataList.add(usage);
        }

        return usageDataList;

    }

    @Override public List<APIFirstAccess> getFirstAccessTime(String providerName)
            throws APIMgtUsageQueryServiceClientException {

        if (!isTableExist("API_UTIL")) {
            return new ArrayList<APIFirstAccess>();
        }

        APIFirstAccess firstAccess = this.queryFirstAccess("API_UTIL");
        List<APIFirstAccess> APIFirstAccessList = new ArrayList<APIFirstAccess>();

        APIFirstAccess fTime;

        if (firstAccess != null) {
            fTime = new APIFirstAccess(firstAccess.getYear(), firstAccess.getMonth(), firstAccess.getDay());
            APIFirstAccessList.add(fTime);
        }
        return APIFirstAccessList;
    }

    private APIFirstAccess queryFirstAccess(String columnFamily) throws APIMgtUsageQueryServiceClientException {

        FirstAccessRequestSearchBean request = new FirstAccessRequestSearchBean("", 0, 1, columnFamily);

        Type ty = new TypeToken<List<Result<FirstAccessValue>>>() {
        }.getType();

        List<Result<FirstAccessValue>> obj = null;

        try {
            obj = restClient.sendAndGetPost(request, ty);
        } catch (JsonSyntaxException e) {
            handleException("Error occurred while parsing response", e);
        } catch (IOException e) {
            handleException("Error occurred while Connecting to DAS REST API", e);
        }

        APIFirstAccess firstAccess = null;
        long l = obj.get(0).getValues().getFirst_access_time();
        if (l > 0) {
            Calendar cc = Calendar.getInstance();
            cc.setTimeInMillis(l);

            String year = cc.get(Calendar.YEAR) + "";
            String month = cc.get(Calendar.MONTH) + "";
            String day = cc.get(Calendar.DATE) + "";

            firstAccess = new APIFirstAccess(year, month, day);
        }
        return firstAccess;

    }

    private boolean isTableExist(String tableName) throws APIMgtUsageQueryServiceClientException {

        TableExistResponseBean status;
        try {
            status = restClient.isTableExist(tableName);
        } catch (JsonSyntaxException e) {
            log.error("Error occurred while parsing response", e);
            throw new APIMgtUsageQueryServiceClientException("Error occurred while parsing response", e);
        } catch (IOException e) {
            log.error("Error occurred while Connecting to DAS REST API");
            throw new APIMgtUsageQueryServiceClientException("Error occurred while Connecting to DAS REST API", e);
        }

        boolean isExist = status.getStatus().equalsIgnoreCase("success");
        return isExist;

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

    @Override public List<APIUsageRangeCost> evaluate(String param, int calls) throws Exception {
        return paymentPlan.evaluate(param, calls);
    }

    @Override public void deployArtifacts(String url, String user, String pass) throws Exception {

        String cAppName = "API_Manager_Analytics.car";
        String cAppPath = System.getProperty("carbon.home") + "/statistics";
        cAppPath = cAppPath + '/' + cAppName;
        File file = new File(cAppPath);

        byte[] byteArray = FileUtils.readFileToByteArray(file);
        DataHandler dataHandler = new DataHandler(byteArray, "application/octet-stream");

        CarbonAppUploaderStub stub = new CarbonAppUploaderStub(url + "/services/CarbonAppUploader");
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        HttpTransportProperties.Authenticator authenticator = new HttpTransportProperties.Authenticator();
        authenticator.setUsername(user);
        authenticator.setPassword(pass);
        authenticator.setPreemptiveAuthentication(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, authenticator);
        client.setOptions(options);
        log.info("Deploying DAS cApp '" + cAppName + "'...");
        UploadedFileItem[] fileItem = new UploadedFileItem[1];
        fileItem[0] = new UploadedFileItem();
        fileItem[0].setDataHandler(dataHandler);
        fileItem[0].setFileName(cAppName);
        fileItem[0].setFileType("jar");
        stub.uploadApp(fileItem);
    }

    private static void handleException(String msg, Throwable e) throws APIMgtUsageQueryServiceClientException {
        log.error(msg, e);
        throw new APIMgtUsageQueryServiceClientException(msg, e);
    }
}