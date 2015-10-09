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

package org.wso2.carbon.apimgt.usage.client.impl;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.mozilla.javascript.*;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.internal.APIManagerComponent;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.usage.client.APIUsageStatisticsClient;
import org.wso2.carbon.apimgt.usage.client.APIUsageStatisticsClientConstants;
import org.wso2.carbon.apimgt.usage.client.bean.*;
import org.wso2.carbon.apimgt.usage.client.billing.APIUsageRangeCost;
import org.wso2.carbon.apimgt.usage.client.billing.PaymentPlan;
import org.wso2.carbon.apimgt.usage.client.dto.*;
import org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException;
import org.wso2.carbon.apimgt.usage.client.internal.APIUsageClientServiceComponent;
import org.wso2.carbon.apimgt.usage.client.pojo.APIFirstAccess;
import org.wso2.carbon.apimgt.usage.client.util.DASRestClient;
import org.wso2.carbon.apimgt.usage.client.util.RestClientUtil;
import org.wso2.carbon.bam.service.data.publisher.conf.AnalyzingConfigData;
import org.wso2.carbon.bam.service.data.publisher.services.ServiceDataPublisherAdmin;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.sql.DataSource;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.sql.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class APIUsageStatisticsRestClientImpl implements APIUsageStatisticsClient {

    private static volatile DataSource dataSource = null;
    private static PaymentPlan paymentPlan;
    private static Map<String, String> subscriberAppsMap = new HashMap<String, String>();
    private APIProvider apiProviderImpl;
    private static final Log log = LogFactory.getLog(APIUsageStatisticsRestClientImpl.class);
    private static DASRestClient restClient;
    Gson gson=new Gson();
    public APIUsageStatisticsRestClientImpl(String username)
            throws APIMgtUsageQueryServiceClientException {
        OMElement element = null;
        APIManagerConfiguration config;
        APIManagerAnalyticsConfiguration apiManagerAnalyticsConfiguration;
        try {
            config = APIUsageClientServiceComponent.getAPIManagerConfiguration();
            apiManagerAnalyticsConfiguration = APIManagerAnalyticsConfiguration.getInstance();

            if(!apiManagerAnalyticsConfiguration.isAnalyticsEnabled()){
                throw new APIMgtUsageQueryServiceClientException("Analytics not enabled");
            }

            if (restClient == null) {
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


    }/**/

    public static void initializeDataSource() throws APIMgtUsageQueryServiceClientException {
        ServiceDataPublisherAdmin serviceDataPublisherAdmin = APIManagerComponent.getDataPublisherAdminService();
        if (serviceDataPublisherAdmin != null){
            if(serviceDataPublisherAdmin.getEventingConfigData().isServiceStatsEnable()) {
                AnalyzingConfigData data = serviceDataPublisherAdmin.getAnalyzingConfigData();
                String url = data.getUrl();
                String user = data.getUserName();
                String pass = data.getPassword();
                restClient = new DASRestClient(url, user, pass);
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

//    @Override
    public String perAppPerAPIUsage(String subscriberName, String groupId, String fromDate,
            String toDate, int limit)
            throws APIMgtUsageQueryServiceClientException {

        List<String> subscriberApps = getAppsBySubscriber(subscriberName, groupId);
        String concatenatedKeySetString;

        int size = subscriberApps.size();
        if (size > 0) {
            concatenatedKeySetString = APIUsageStatisticsClientConstants.CONSUMERKEY+":" + subscriberApps.get(0);
        } else {
            return gson.toJson(new ArrayList<APIUsageDTO>());
        }
        for (int i = 1; i < subscriberApps.size(); i++) {
            concatenatedKeySetString += " OR " + APIUsageStatisticsClientConstants.CONSUMERKEY+":" + subscriberApps.get(i);
        }

        List<PerAppApiCountDTO> usage= getPerAppAPIUsageData(APIUsageStatisticsClientConstants.API_REQUEST_SUMMARY, concatenatedKeySetString,
                fromDate, toDate, limit);
        return gson.toJson(usage);

    }

    /**
     * This method gets the API usage data per application
     *
     * @param tableName name of the required table in the database
     * @param keyString concatenated key set of applications
     * @return a collection containing the data related to per App API usage
     * @throws APIMgtUsageQueryServiceClientException if an error occurs while querying the database
     */
    private List<PerAppApiCountDTO> getPerAppAPIUsageData(String tableName, String keyString, String fromDate, String toDate,
            int limit) throws APIMgtUsageQueryServiceClientException {

        String query;

        try {
            query = "max_request_time: [" + RestClientUtil.dateToLong(fromDate) + " TO " + RestClientUtil.dateToLong(
                    toDate) + "] AND ( "+keyString+" )";
        }catch(ParseException e){
            throw new APIMgtUsageQueryServiceClientException("Error parsing date");
        }

        log.info(query);
        RequestSearchBean request = new RequestSearchBean(query, 1, "key_api_facet", tableName);

        ArrayList<AggregateField> fields = new ArrayList<AggregateField>();
        AggregateField f = new AggregateField("total_request_count", "SUM", "count");
        fields.add(f);
        request.setAggregateFields(fields);

        Type ty = new TypeToken<List<Result<PerAppPerAPIUsageValues>>>() {
        }.getType();

        List<Result<PerAppPerAPIUsageValues>> obj;

        try {
            obj = restClient.sendAndGetPost(request, ty);
        } catch (JsonSyntaxException e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while parsing response", e);
        } catch (IOException e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while Connecting to DAS REST API", e);
        } catch (Exception e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while connecting to DAS REST API", e);
        }

        List<PerAppApiCountDTO> perAppUsageDataList = new ArrayList<PerAppApiCountDTO>();

        if(obj==null || obj.isEmpty()){
            return perAppUsageDataList;
        }

        PerAppApiCountDTO apiUsageDTO;
        for (Result<PerAppPerAPIUsageValues> result : obj) {
            PerAppPerAPIUsageValues v = result.getValues();

            String appName=subscriberAppsMap.get(v.getColumnNames().get(0));

            boolean found=false;
            for(PerAppApiCountDTO dto: perAppUsageDataList){
                if(dto.getAppName().equals(appName)){
                    dto.addToApiCountArray(v.getColumnNames().get(1), v.getCount_sum());
                    found=true;
                    break;
                }
            }

            if(!found){
                apiUsageDTO = new PerAppApiCountDTO();
                apiUsageDTO.setAppName(appName);
                apiUsageDTO.addToApiCountArray(v.getColumnNames().get(1), v.getCount_sum());
                perAppUsageDataList.add(apiUsageDTO);
            }

        }

        return perAppUsageDataList;
    }

//    @Override
    public String getTopAppUsers(String subscriberName, String groupId, String fromDate,
            String toDate, int limit)
            throws APIMgtUsageQueryServiceClientException {

        List<String> subscriberApps = getAppsBySubscriber(subscriberName, groupId);
        String concatenatedKeySetString;

        int size = subscriberApps.size();
        if (size > 0) {
            concatenatedKeySetString = APIUsageStatisticsClientConstants.CONSUMERKEY+":" + subscriberApps.get(0);
        } else {
            return gson.toJson(new ArrayList<AppUsageDTO>());
        }
        for (int i = 1; i < subscriberApps.size(); i++) {
            concatenatedKeySetString += " OR " + APIUsageStatisticsClientConstants.CONSUMERKEY+":" + subscriberApps.get(i);
        }

        List<AppUsageDTO> usage= getTopAppUsageData(APIUsageStatisticsClientConstants.API_REQUEST_SUMMARY, concatenatedKeySetString,
                fromDate, toDate, limit);
        return gson.toJson(usage);

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

        String query;

        try {
            query = "max_request_time: [" + RestClientUtil.dateToLong(fromDate) + " TO " + RestClientUtil.dateToLong(
                    toDate) + "] AND ( "+keyString+" )";
        }catch(ParseException e){
            throw new APIMgtUsageQueryServiceClientException("Error parsing date");
        }

        log.info(query);
        RequestSearchBean request = new RequestSearchBean(query, 1, "key_userId_facet", tableName);

        ArrayList<AggregateField> fields = new ArrayList<AggregateField>();
        AggregateField f = new AggregateField("total_request_count", "SUM", "count");
        fields.add(f);
        request.setAggregateFields(fields);

        Type ty = new TypeToken<List<Result<TopAppUsersValues>>>() {
        }.getType();



        List<Result<TopAppUsersValues>> obj;

        try {
            obj = restClient.sendAndGetPost(request, ty);
        } catch (JsonSyntaxException e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while parsing response", e);
        } catch (IOException e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while Connecting to DAS REST API", e);
        } catch (Exception e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while connecting to DAS REST API", e);
        }

        List<AppUsageDTO> topAppUsageDataList = new ArrayList<AppUsageDTO>();

        if(obj==null || obj.isEmpty()){
            return topAppUsageDataList;
        }

        AppUsageDTO appUsageDTO;
        for (Result<TopAppUsersValues> result : obj) {
            TopAppUsersValues v = result.getValues();


            String appName=subscriberAppsMap.get(v.getColumnNames().get(0));

            boolean found=false;
            for(AppUsageDTO dto: topAppUsageDataList){
                if(dto.getAppName().equals(appName)){
                    dto.addToUserCountArray(v.getColumnNames().get(1), v.getCount());
                    found=true;
                    break;
                }
            }

            if(!found){
                appUsageDTO = new AppUsageDTO();
                appUsageDTO.setAppName(appName);
                appUsageDTO.addToUserCountArray(v.getColumnNames().get(1), v.getCount());
                topAppUsageDataList.add(appUsageDTO);
            }

        }

        return topAppUsageDataList;
    }

//    @Override
    public String getAppApiCallType(String subscriberName, String groupId, String fromDate,
            String toDate, int limit)
            throws APIMgtUsageQueryServiceClientException {

        List<String> subscriberApps = getAppsBySubscriber(subscriberName, groupId);
        String concatenatedKeySetString;

        int size = subscriberApps.size();
        if (size > 0) {
            concatenatedKeySetString = APIUsageStatisticsClientConstants.CONSUMERKEY+":" + subscriberApps.get(0);
        } else {
            return gson.toJson(new ArrayList<AppCallTypeDTO>());
        }
        for (int i = 1; i < subscriberApps.size(); i++) {
            concatenatedKeySetString += " OR " + APIUsageStatisticsClientConstants.CONSUMERKEY+":" + subscriberApps.get(i);
        }

        List<AppCallTypeDTO> usage= getAPICallTypeUsageData(APIUsageStatisticsClientConstants.API_Resource_Path_USAGE_SUMMARY,
                concatenatedKeySetString, fromDate, toDate, limit);


        return gson.toJson(usage);
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

        String query;

        try {
            query = "max_request_time: [" + RestClientUtil.dateToLong(fromDate) + " TO " + RestClientUtil.dateToLong(
                    toDate) + "] AND ( "+keyString+" )";
        }catch(ParseException e){
            throw new APIMgtUsageQueryServiceClientException("Error parsing date");
        }
        log.info(query);

        RequestSearchBean request = new RequestSearchBean(query, 3,
                "key_api_method_path_facet", tableName);
        ArrayList<AggregateField> fields = new ArrayList<AggregateField>();
        AggregateField f = new AggregateField("total_request_count", "SUM", "count");
        fields.add(f);
        request.setAggregateFields(fields);

        Type ty = new TypeToken<List<Result<AppApiCallTypeValues>>>() {
        }.getType();

        List<Result<AppApiCallTypeValues>> obj;

        try {
            obj = restClient.sendAndGetPost(request, ty);
        } catch (JsonSyntaxException e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while parsing response", e);
        } catch (IOException e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while Connecting to DAS REST API", e);
        } catch (Exception e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while connecting to DAS REST API", e);
        }

        List<AppCallTypeDTO> appApiCallTypeList = new ArrayList<AppCallTypeDTO>();

        if(obj==null || obj.isEmpty()){
            return appApiCallTypeList;
        }

        AppCallTypeDTO appCallTypeDTO;
        for (Result<AppApiCallTypeValues> result : obj) {
            AppApiCallTypeValues v = result.getValues();



            List<String> callTypeList = new ArrayList<String>();
            callTypeList.add(v.getColumnNames().get(3) + " (" + v.getColumnNames().get(2) + ")");

            String appName=subscriberAppsMap.get(v.getColumnNames().get(0));


            boolean found=false;
            for(AppCallTypeDTO dto: appApiCallTypeList){
                if(dto.getAppName().equals(appName)){
                    dto.addGToApiCallTypeArray(v.getColumnNames().get(1), callTypeList);
                    found=true;
                    break;
                }
            }

            if(!found){
                appCallTypeDTO = new AppCallTypeDTO();
                appCallTypeDTO.setAppName(appName);
                appCallTypeDTO.addGToApiCallTypeArray(v.getColumnNames().get(1), callTypeList);
                appApiCallTypeList.add(appCallTypeDTO);
            }

        }

        return appApiCallTypeList;
    }

//    @Override
    public String getPerAppFaultCount(String subscriberName, String groupId,
            String fromDate, String toDate, int limit)
            throws APIMgtUsageQueryServiceClientException {

        List<String> subscriberApps = getAppsBySubscriber(subscriberName, groupId);
        String concatenatedKeySetString;

        int size = subscriberApps.size();
        if (size > 0) {
            concatenatedKeySetString = APIUsageStatisticsClientConstants.CONSUMERKEY+":" + subscriberApps.get(0);
        } else {
            return gson.toJson(new ArrayList<FaultCountDTO>());
        }
        for (int i = 1; i < subscriberApps.size(); i++) {
            concatenatedKeySetString += " OR " + APIUsageStatisticsClientConstants.CONSUMERKEY+":" + subscriberApps.get(i);
        }

        List<FaultCountDTO> usage= getFaultAppUsageData(APIUsageStatisticsClientConstants.API_FAULT_SUMMARY, concatenatedKeySetString,
                fromDate, toDate, limit);
        return gson.toJson(usage);
    }

    /**
     * This method gets the API faulty invocation data
     *
     * @param tableName name of the required table in the database
     * @param keyString concatenated key set of applications
     * @return a collection containing the data related to API faulty invocations
     * @throws APIMgtUsageQueryServiceClientException if an error occurs while querying the database
     */
    private List<FaultCountDTO> getFaultAppUsageData(String tableName, String keyString, String fromDate,
            String toDate, int limit)
            throws APIMgtUsageQueryServiceClientException {

        String query;

        try {
            query = "max_request_time: [" + RestClientUtil.dateToLong(fromDate) + " TO " + RestClientUtil.dateToLong(
                    toDate) + "] AND ( "+keyString+" )";
        }catch(ParseException e){
            throw new APIMgtUsageQueryServiceClientException("Error parsing date");
        }

        log.info(query);
        RequestSearchBean request = new RequestSearchBean(query, 1, "consumerKey_api_facet",
                "API_FAULT_SUMMARY");
        ArrayList<AggregateField> fields = new ArrayList<AggregateField>();
        AggregateField f = new AggregateField("total_fault_count", "SUM", "count");
        fields.add(f);
        request.setAggregateFields(fields);

        Type ty = new TypeToken<List<Result<FaultAppUsageDataValue>>>() {
        }.getType();

        List<Result<FaultAppUsageDataValue>> obj;

        try {
            obj = restClient.sendAndGetPost(request, ty);
        } catch (JsonSyntaxException e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while parsing response", e);
        } catch (IOException e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while Connecting to DAS REST API", e);
        } catch (Exception e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while connecting to DAS REST API", e);
        }

        List<FaultCountDTO> falseAppUsageDataList = new ArrayList<FaultCountDTO>();

        if(obj==null || obj.isEmpty()){
            return falseAppUsageDataList;
        }

        FaultCountDTO faultCountDTO;
        for (Result<FaultAppUsageDataValue> result : obj) {
            FaultAppUsageDataValue v = result.getValues();

            String appName=subscriberAppsMap.get(v.getColumnNames().get(0));
            String apiName=v.getColumnNames().get(1);

            boolean found=false;
            for(FaultCountDTO dto: falseAppUsageDataList){
                if(dto.getAppName().equals(appName)){
                    dto.addToApiFaultCountArray(apiName, v.getCount());
                    found=true;
                    break;
                }
            }

            if(!found){
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
    //@Override
    public String getAPIUsageByUser(String providerName, String fromDate, String toDate)
            throws APIMgtUsageQueryServiceClientException {

        if(!UsageClient.isDataPublishingEnabled()){
            return null;
//            throw new APIMgtUsageQueryServiceClientException("isDataPublishingEnabled");
        }

        List<APIUsageByUserName> usageData = this.getAPIUsageByUserData(providerName, fromDate, toDate, null);

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
        return gson.toJson(usageByName);
    }

    private List<APIUsageByUserName> getAPIUsageByUserData(String providerName, String fromDate, String toDate,
            Integer limit)
            throws APIMgtUsageQueryServiceClientException {

        String query;

        try {
            query = "max_request_time: [" + RestClientUtil.getFloorDateAsLong(fromDate) + " TO " + RestClientUtil.getCeilingDateAsLong(
                    toDate) + "]";
        }catch(ParseException e){
            throw new APIMgtUsageQueryServiceClientException("Error parsing date");
        }

        log.info(query);

        RequestSearchBean request = new RequestSearchBean(query, 3,
                "api_version_userId_apiPublisher_facet", "API_REQUEST_SUMMARY");

        ArrayList<AggregateField> fields = new ArrayList<AggregateField>();
        AggregateField f = new AggregateField("total_request_count", "SUM",
                "count");
        fields.add(f);
        request.setAggregateFields(fields);

        Type ty = new TypeToken<List<Result<APIUsageByUserValues>>>() {
        }.getType();

        List<Result<APIUsageByUserValues>> obj;

        try {
            obj = restClient.sendAndGetPost(request, ty);
        } catch (JsonSyntaxException e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while parsing response", e);
        } catch (IOException e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while Connecting to DAS REST API", e);
        } catch (Exception e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while connecting to DAS REST API", e);
        }

        List<APIUsageByUserName> usageByName = new ArrayList<APIUsageByUserName>();

        if(obj==null || obj.isEmpty()){
            return usageByName;
        }

        APIUsageByUserName usage;
        for (Result<APIUsageByUserValues> result : obj) {
            APIUsageByUserValues v = result.getValues();

            usage = new APIUsageByUserName();
            usage.requestCount = v.getCount_sum();

            usage.apiName = v.getColumnNames().get(0);
            usage.apiVersion = v.getColumnNames().get(1);
            usage.userID = v.getColumnNames().get(2);
            usage.apipublisher = v.getColumnNames().get(3);

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
//    @Override
    public String getResponseTimesByAPIs(String providerName, String fromDate,
            String toDate, int limit)
            throws APIMgtUsageQueryServiceClientException {

        Collection<APIResponseTime> responseTimes =
                getAPIResponseTimeData(APIUsageStatisticsClientConstants.API_VERSION_SERVICE_TIME_SUMMARY,fromDate,toDate,limit);
        List<API> providerAPIs = getAPIsByProvider(providerName);
//        Map<String, Double> apiCumulativeServiceTimeMap = new HashMap<String, Double>();
//        Map<String, Long> apiUsageMap = new TreeMap<String, Long>();
        DecimalFormat format = new DecimalFormat("#.##");
        List<APIResponseTimeDTO> apiResponseTimeUsage=new ArrayList<APIResponseTimeDTO>();

        for (APIResponseTime responseTime : responseTimes) {
            for (API providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(responseTime.apiName) &&
                        providerAPI.getId().getVersion().equals(responseTime.apiVersion) &&
                        providerAPI.getContext().equals(responseTime.context)) {

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
                    responseTimeDTO.setApiName(responseTime.apiName);
                    double avgTime = responseTime.responseTime / responseTime.responseCount;
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
        return gson.toJson(apiResponseTimeUsage);
    }

    /**
     * This method gets the response times for APIs
     *
     * @param tableName name of the required table in the database
     * @return a collection containing the data related to API response times
     * @throws APIMgtUsageQueryServiceClientException if an error occurs while querying the database
     */
    private Collection<APIResponseTime> getAPIResponseTimeData(String tableName,String fromDate, String toDate, int limit)
            throws APIMgtUsageQueryServiceClientException {

        String query;

        try {
            query = "max_request_time: [" + RestClientUtil.getFloorDateAsLong(
                    fromDate) + " TO " + RestClientUtil.getCeilingDateAsLong(toDate) + "]";
        }catch(ParseException e){
            throw new APIMgtUsageQueryServiceClientException("Error parsing date");
        }

        log.info(query);

        RequestSearchBean request = new RequestSearchBean(query, 2,
                "api_version_context_facet", "API_RESPONSE_SUMMARY");

        ArrayList<AggregateField> fields = new ArrayList<AggregateField>();
        AggregateField f0 = new AggregateField("serviceTime", "SUM",
                "totalServiceTime");
        AggregateField f1 = new AggregateField("total_response_count", "SUM",
                "totalResponseCount");
        fields.add(f0);
        fields.add(f1);
        request.setAggregateFields(fields);

        Type ty = new TypeToken<List<Result<ResponseTimesByAPIsValue>>>() {
        }.getType();

        List<Result<ResponseTimesByAPIsValue>> obj;

        try {
            obj = restClient.sendAndGetPost(request, ty);
        } catch (JsonSyntaxException e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while parsing response", e);
        } catch (IOException e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while Connecting to DAS REST API", e);
        } catch (Exception e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while connecting to DAS REST API", e);
        }

        List<APIResponseTime> responseTimeData = new ArrayList<APIResponseTime>();

        if(obj==null || obj.isEmpty()){
            return responseTimeData;
        }

        APIResponseTime usage;
        for (Result<ResponseTimesByAPIsValue> result : obj) {
            ResponseTimesByAPIsValue v = result.getValues();

            usage = new APIResponseTime();
            usage.apiName = v.getColumnNames().get(0);
            usage.apiVersion = v.getColumnNames().get(1);
            usage.context = v.getColumnNames().get(2);
            usage.responseTime = v.getTotalServiceTime();
            usage.responseCount = v.getTotalResponseCount();

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
//    @Override
    public String getLastAccessTimesByAPI(String providerName, String fromDate,
            String toDate, int limit)
            throws APIMgtUsageQueryServiceClientException {

        Collection<APIAccessTime> accessTimes =
                getLastAccessTimesByAPIData(APIUsageStatisticsClientConstants.API_VERSION_KEY_LAST_ACCESS_SUMMARY,fromDate,toDate,limit);
        List<API> providerAPIs = getAPIsByProvider(providerName);
//        Map<String, APIAccessTime> lastAccessTimes = new TreeMap<String, APIAccessTime>();
        List<APIVersionLastAccessTimeDTO> apiVersionLastAccessTimeUsage=new ArrayList<APIVersionLastAccessTimeDTO>();
        DateFormat dateFormat = new SimpleDateFormat();

        for (APIAccessTime accessTime : accessTimes) {
            for (API providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(accessTime.apiName) &&
                        providerAPI.getId().getVersion().equals(accessTime.apiVersion) &&
                        providerAPI.getContext().equals(accessTime.context)) {

                    /*String apiName = accessTime.apiName + " (" + providerAPI.getId().getProviderName() + ")";
                    APIAccessTime lastAccessTime = lastAccessTimes.get(apiName);
                    if (lastAccessTime == null || lastAccessTime.accessTime < accessTime.accessTime) {
                        lastAccessTimes.put(apiName, accessTime);
                        break;
                    }*/
                    String apiName = accessTime.apiName + " (" + providerAPI.getId().getProviderName() + ")";
                    APIVersionLastAccessTimeDTO accessTimeDTO = new APIVersionLastAccessTimeDTO();
                    accessTimeDTO.setApiName(apiName);
                    accessTimeDTO.setApiVersion(accessTime.apiVersion);
                    accessTimeDTO.setUser(accessTime.username);
                    accessTimeDTO.setLastAccessTime(accessTime.accessTime+"");
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
        return gson.toJson(apiVersionLastAccessTimeUsage);
    }

    /**
     * This method gets the last access times for APIs
     *
     * @param tableName name of the required table in the database
     * @return a collection containing the data related to API last access times
     * @throws APIMgtUsageQueryServiceClientException if an error occurs while querying the database
     */
    private Collection<APIAccessTime> getLastAccessTimesByAPIData(String tableName,String fromDate, String toDate, int limit)
            throws APIMgtUsageQueryServiceClientException {

        String query;

        try {
            query = "max_request_time: [" + RestClientUtil.getFloorDateAsLong(
                    fromDate) + " TO " + RestClientUtil.getCeilingDateAsLong(toDate) + "]";
        }catch(ParseException e){
            throw new APIMgtUsageQueryServiceClientException("Error parsing date");
        }

        log.info(query);
        RequestSearchBean request = new RequestSearchBean(query, 3,
                "api_version_userId_context_facet", "API_REQUEST_SUMMARY");

        ArrayList<AggregateField> fields = new ArrayList<AggregateField>();
        AggregateField f0 = new AggregateField("max_request_time", "MAX",
                "lastAccessTime");
        fields.add(f0);
        request.setAggregateFields(fields);

        Type ty = new TypeToken<List<Result<LastAccessTimesByAPIValue>>>() {
        }.getType();

        List<Result<LastAccessTimesByAPIValue>> obj;

        try {
            obj = restClient.sendAndGetPost(request, ty);
        } catch (JsonSyntaxException e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while parsing response", e);
        } catch (IOException e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while Connecting to DAS REST API", e);
        } catch (Exception e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while connecting to DAS REST API", e);
        }

        List<APIAccessTime> lastAccessTimeData = new ArrayList<APIAccessTime>();

        if(obj==null || obj.isEmpty()){
            return lastAccessTimeData;
        }

        APIAccessTime usage;
        for (Result<LastAccessTimesByAPIValue> result : obj) {
            LastAccessTimesByAPIValue v = result.getValues();

            usage = new APIAccessTime();
            usage.accessTime = v.getLastAccessTime();
            usage.apiName = v.getColumnNames().get(0);
            usage.apiVersion = v.getColumnNames().get(1);
            usage.username = v.getColumnNames().get(2);
            usage.context = v.getColumnNames().get(3);
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
//    @Override
    public String getAPIUsageByResourcePath(String providerName, String fromDate,
            String toDate)
            throws APIMgtUsageQueryServiceClientException {

        Collection<APIUsageByResourcePath> usageData = this
                .getAPIUsageByResourcePathData(APIUsageStatisticsClientConstants.API_Resource_Path_USAGE_SUMMARY,
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
        return gson.toJson(usageByResourcePath);
    }

    private List<APIUsageByResourcePath> getAPIUsageByResourcePathData(String tableName, String fromDate, String toDate)
            throws APIMgtUsageQueryServiceClientException {

        String query;

        try {
            query = "max_request_time: [" + RestClientUtil.getFloorDateAsLong(
                    fromDate) + " TO " + RestClientUtil.getCeilingDateAsLong(toDate) + "]";
        }catch(ParseException e){
            throw new APIMgtUsageQueryServiceClientException("Error parsing date");
        }

        log.info(query);
        RequestSearchBean request = new RequestSearchBean(query, 3,
                "api_version_context_method_facet",
                "API_RESOURCE_USAGE_SUMMARY");

        ArrayList<AggregateField> fields = new ArrayList<AggregateField>();
        AggregateField f0 = new AggregateField("total_request_count", "SUM",
                "totalRequesCount");
        fields.add(f0);
        request.setAggregateFields(fields);

        Type ty = new TypeToken<List<Result<APIUsageByResourcePathValue>>>() {
        }.getType();

        List<Result<APIUsageByResourcePathValue>> obj;

        try {
            obj = restClient.sendAndGetPost(request, ty);
        } catch (JsonSyntaxException e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while parsing response", e);
        } catch (IOException e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while Connecting to DAS REST API", e);
        } catch (Exception e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while connecting to DAS REST API", e);
        }

        List<APIUsageByResourcePath> usageByResourcePath = new ArrayList<APIUsageByResourcePath>();

        if(obj==null || obj.isEmpty()){
            return usageByResourcePath;
        }

        APIUsageByResourcePath usage;
        for (Result<APIUsageByResourcePathValue> result : obj) {
            APIUsageByResourcePathValue v = result.getValues();

            usage = new APIUsageByResourcePath();
            usage.requestCount = v.getTotalRequesCount();
            usage.apiName = v.getColumnNames().get(0);
            usage.apiVersion = v.getColumnNames().get(1);
            usage.context = v.getColumnNames().get(2);
            usage.method = v.getColumnNames().get(3);

            usageByResourcePath.add(usage);
        }

        return usageByResourcePath;

    }

//    @Override
    public String getAPIUsageByDestination(String providerName, String fromDate,
            String toDate)
            throws APIMgtUsageQueryServiceClientException {

        List<APIUsageByDestination> usageData= this.getAPIUsageByDestinationData(
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
        return gson.toJson(usageByResourcePath);
    }

    private List<APIUsageByDestination> getAPIUsageByDestinationData(String tableName, String fromDate, String toDate)
            throws APIMgtUsageQueryServiceClientException {

        String query;

        try {
            query = "max_request_time: [" + RestClientUtil.getFloorDateAsLong(
                    fromDate) + " TO " + RestClientUtil.getCeilingDateAsLong(toDate) + "]";
        }catch(ParseException e){
            throw new APIMgtUsageQueryServiceClientException("Error parsing date");
        }

        log.info(query);

        RequestSearchBean request = new RequestSearchBean(query, 3,
                "api_version_context_dest_facet", "API_DESTINATION_SUMMARY");

        ArrayList<AggregateField> fields = new ArrayList<AggregateField>();
        AggregateField f0 = new AggregateField("total_request_count", "SUM",
                "totalRequestCount");
        fields.add(f0);
        request.setAggregateFields(fields);

        Type ty = new TypeToken<List<Result<APIUsageByDestinationValue>>>() {
        }.getType();

        List<Result<APIUsageByDestinationValue>> obj;

        try {
            obj = restClient.sendAndGetPost(request, ty);
        } catch (JsonSyntaxException e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while parsing response", e);
        } catch (IOException e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while Connecting to DAS REST API", e);
        } catch (Exception e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while connecting to DAS REST API", e);
        }

        List<APIUsageByDestination> usageByResourcePath = new ArrayList<APIUsageByDestination>();

        if(obj==null || obj.isEmpty()){
            return usageByResourcePath;
        }

        APIUsageByDestination usage;
        for (Result<APIUsageByDestinationValue> result : obj) {
            APIUsageByDestinationValue v = result.getValues();

            usage = new APIUsageByDestination();
            usage.requestCount = v.getTotalRequesCount();
            usage.apiName = v.getColumnNames().get(0);
            usage.apiVersion = v.getColumnNames().get(1);
            usage.context = v.getColumnNames().get(2);
            usage.destination = v.getColumnNames().get(3);

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
//    @Override
    public String getUsageByAPIs(String providerName, String fromDate, String toDate, int limit)
            throws APIMgtUsageQueryServiceClientException {

        Collection<APIUsage> usageData = getUsageByAPIsData(APIUsageStatisticsClientConstants.API_VERSION_USAGE_SUMMARY,
                fromDate, toDate, limit);
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
        List<APIUsageDTO> usage= getAPIUsageTopEntries(new ArrayList<APIUsageDTO>(usageByAPIs.values()), limit);
        return gson.toJson(usage);
    }

    /**
     * This method gets the usage data for a given API across all versions
     *
     * @param tableName name of the table in the database
     * @return a collection containing the API usage data
     * @throws APIMgtUsageQueryServiceClientException if an error occurs while querying the database
     */
    private Collection<APIUsage> getUsageByAPIsData(String tableName, String fromDate, String toDate, int limit) throws APIMgtUsageQueryServiceClientException {

        String query;

        try {
            query = "max_request_time: [" + RestClientUtil.getFloorDateAsLong(
                    fromDate) + " TO " + RestClientUtil.getCeilingDateAsLong(toDate) + "]";
        }catch(ParseException e){
            throw new APIMgtUsageQueryServiceClientException("Error parsing date");
        }

        log.info(query);

        RequestSearchBean request = new RequestSearchBean(query, 2, "api_version_context_facet",
                "API_VERSION_USAGE_SUMMARY");

        ArrayList<AggregateField> fields = new ArrayList<AggregateField>();
        AggregateField f0 = new AggregateField("total_request_count", "SUM",
                "totalRequestCount");
        fields.add(f0);
        request.setAggregateFields(fields);

        Type ty = new TypeToken<List<Result<UsageByAPIsValue>>>() {
        }.getType();

        List<Result<UsageByAPIsValue>> obj;

        try {
            obj = restClient.sendAndGetPost(request, ty);
        } catch (JsonSyntaxException e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while parsing response", e);
        } catch (IOException e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while Connecting to DAS REST API", e);
        } catch (Exception e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while connecting to DAS REST API", e);
        }

        List<APIUsage> usageDataList = new ArrayList<APIUsage>();

        if(obj==null || obj.isEmpty()){
            return usageDataList;
        }

        APIUsage usage;
        for (Result<UsageByAPIsValue> result : obj) {
            UsageByAPIsValue v = result.getValues();

            usage = new APIUsage();
            usage.requestCount = v.getTotalRequestCount();
            usage.apiName = v.getColumnNames().get(0);
            usage.apiVersion = v.getColumnNames().get(1);
            usage.context = v.getColumnNames().get(2);

            usageDataList.add(usage);
        }

        return usageDataList;
    }

//    @Override
    public String getAPIResponseFaultCount(String providerName, String fromDate,
            String toDate)
            throws APIMgtUsageQueryServiceClientException {

        List<APIResponseFaultCount> faultyData = this
                .getAPIResponseFaultCountData(APIUsageStatisticsClientConstants.API_FAULT_SUMMARY, fromDate, toDate);
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
                    if(apiVersionUsageList.size() > 0){
                        apiVersionUsageDTO = apiVersionUsageList.get(0);
                        if (apiVersionUsageDTO.getVersion().equals(fault.apiVersion)) {
                            long requestCount = apiVersionUsageDTO.getCount();
                            double faultPercentage = ((double)requestCount - fault.faultCount) / requestCount * 100;
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
        return gson.toJson(faultyCount);
    }

    private List<APIResponseFaultCount> getAPIResponseFaultCountData(String tableName, String fromDate, String toDate)
            throws APIMgtUsageQueryServiceClientException {

        String query;

        try {
            query = "max_request_time: [" + RestClientUtil.getFloorDateAsLong(
                    fromDate) + " TO " + RestClientUtil.getCeilingDateAsLong(toDate) + "]";
        }catch(ParseException e){
            throw new APIMgtUsageQueryServiceClientException("Error parsing date");
        }

        log.info(query);

        RequestSearchBean request = new RequestSearchBean(query, 3,
                "api_version_apiPublisher_context_facet", "API_FAULT_SUMMARY");

        ArrayList<AggregateField> fields = new ArrayList<AggregateField>();
        AggregateField f0 = new AggregateField("total_fault_count", "SUM",
                "totalFaultCount");
        fields.add(f0);
        request.setAggregateFields(fields);

        Type ty = new TypeToken<List<Result<APIResponseFaultCountValue>>>() {
        }.getType();

        List<Result<APIResponseFaultCountValue>> obj;

        try {
            obj = restClient.sendAndGetPost(request, ty);
        } catch (JsonSyntaxException e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while parsing response", e);
        } catch (IOException e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while Connecting to DAS REST API", e);
        } catch (Exception e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while connecting to DAS REST API", e);
        }

        List<APIResponseFaultCount> faultUsage = new ArrayList<APIResponseFaultCount>();

        if(obj==null || obj.isEmpty()){
            return faultUsage;
        }

        APIResponseFaultCount usage;
        for (Result<APIResponseFaultCountValue> result : obj) {
            APIResponseFaultCountValue v = result.getValues();

            usage = new APIResponseFaultCount();
            usage.faultCount = v.getTotalFaultCount();
            usage.apiName = v.getColumnNames().get(0);
            usage.apiVersion = v.getColumnNames().get(1);
            usage.context = v.getColumnNames().get(3);

            faultUsage.add(usage);
        }

        return faultUsage;

    }




    //Throttling related Methods

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

        List<APIUsage> usageData = this.getUsageByAPIVersionsData(
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

    private List<APIUsage> getUsageByAPIVersionsData(String tableName, String fromDate, String toDate, String apiName)
            throws APIMgtUsageQueryServiceClientException {

        String query;

        try {
            query = "api:"+apiName +" AND max_request_time: [" + RestClientUtil.getFloorDateAsLong(
                    fromDate) + " TO " + RestClientUtil.getCeilingDateAsLong(toDate) + "]";
        }catch(ParseException e){
            throw new APIMgtUsageQueryServiceClientException("Error parsing date");
        }

        RequestSearchBean request = new RequestSearchBean(query, 2,
                "api_version_context_facet", "API_VERSION_USAGE_SUMMARY");

        ArrayList<AggregateField> fields = new ArrayList<AggregateField>();
        AggregateField f0 = new AggregateField("total_request_count", "SUM",
                "totalRequestCount");
        fields.add(f0);
        request.setAggregateFields(fields);

        Type ty = new TypeToken<List<Result<UsageByAPIVersionsValue>>>() {
        }.getType();

        List<Result<UsageByAPIVersionsValue>> obj;

        try {
            obj = restClient.sendAndGetPost(request, ty);
        } catch (JsonSyntaxException e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while parsing response", e);
        } catch (IOException e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while Connecting to DAS REST API", e);
        } catch (Exception e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while connecting to DAS REST API", e);
        }

        List<APIUsage> usageDataList = new ArrayList<APIUsage>();

        if(obj==null || obj.isEmpty()){
            return usageDataList;
        }

        APIUsage usage;
        for (Result<UsageByAPIVersionsValue> result : obj) {
            UsageByAPIVersionsValue v = result.getValues();

            usage = new APIUsage();
            usage.requestCount = v.getTotalRequestCount();
            usage.apiName = v.getColumnNames().get(0);
            usage.apiVersion = v.getColumnNames().get(1);
            usage.context = v.getColumnNames().get(2);

            usageDataList.add(usage);
        }

        return usageDataList;
        /*
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
        }*/
    }

    public List<String> getFirstAccessTime(String providerName)
            throws APIMgtUsageQueryServiceClientException {

        APIFirstAccess firstAccess = this.queryFirstAccess( "API_UTIL");
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

        FirstAccessRequestSearchBean request = new FirstAccessRequestSearchBean("", 0, 1, columnFamily);

        Type ty = new TypeToken<List<Result<FirstAccessValue>>>() {
        }.getType();

        List<Result<FirstAccessValue>> obj;

        try{
            obj=restClient.sendAndGetPost(request,ty);
        } catch (JsonSyntaxException e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while parsing response", e);
        } catch (IOException e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while Connecting to DAS REST API", e);
        } catch (Exception e) {
            throw new APIMgtUsageQueryServiceClientException
                    ("Error occurred while connecting to DAS REST API", e);
        }

        APIFirstAccess firstAccess=null;
        long l=obj.get(0).getValues().getFirst_access_time();
        if(l>0) {
            Calendar cc = Calendar.getInstance();
            cc.setTimeInMillis(l);

            String year = cc.get(Calendar.YEAR) + "";
            String month = cc.get(Calendar.MONTH) + "";
            String day = cc.get(Calendar.DATE) + "";

            firstAccess = new APIFirstAccess(year, month, day);
        }
        return firstAccess;
        /*if (dataSource == null) {
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
        }*/
    }

    private static boolean isTableExist(String tableName) throws APIMgtUsageQueryServiceClientException {

        TableExistResponseBean status;
        try {
            status = restClient.isTableExist(tableName);
        } catch (JsonSyntaxException e) {
            throw new APIMgtUsageQueryServiceClientException("Error occurred while parsing response", e);
        } catch (IOException e) {
            throw new APIMgtUsageQueryServiceClientException("Error occurred while Connecting to DAS REST API", e);
        } catch (Exception e) {
            throw new APIMgtUsageQueryServiceClientException("Error occurred while connecting to DAS REST API", e);
        }

        boolean isExist = status.getStatus().equalsIgnoreCase("success");
        return isExist;

    }

    public String getAppRegisteredUsers(String subscriberName, String groupId) throws APIMgtUsageQueryServiceClientException {


        List<String> subscriberApps = getAppsBySubscriber(subscriberName, groupId);

        Collection<AppRegisteredUsersDTO> usageData = getAppUsers();


        List<RegisteredAppUsersDTO> appUserList = new ArrayList<RegisteredAppUsersDTO>();
        RegisteredAppUsersDTO appUsers;
        for (AppRegisteredUsersDTO usage : usageData) {
            for (String subscriberApp : subscriberApps) {
                if (subscriberApp != null && subscriberApp.equals(usage.getconsumerKey())) {

                    String appName=subscriberAppsMap.get(usage.getconsumerKey());
                    String user=usage.getUser();

                    boolean found=false;
                    for(RegisteredAppUsersDTO dto: appUserList){
                        if(dto.getAppName().equals(appName)){
                            dto.addToUserArray(user);
                            found=true;
                            break;
                        }
                    }

                    if(!found){
                        appUsers = new RegisteredAppUsersDTO();
                        appUsers.setAppName(appName);
                        appUsers.addToUserArray(user);
                        appUserList.add(appUsers);
                    }

                }
            }
        }

        return gson.toJson(appUserList);
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






    private List<String> getAppsBySubscriber(String subscriberName, String groupId) throws APIMgtUsageQueryServiceClientException {

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

    public List<APIUsageRangeCost> evaluate(String param, int calls) throws Exception {
        return paymentPlan.evaluate(param, calls);
    }



    private static class APIUsage {

        private String apiName;
        private String apiVersion;
        private String context;
        private long requestCount;

        public APIUsage(){

        }
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

    private static class APIUsageByResourcePath {

        private String apiName;
        private String apiVersion;
        private String method;
        private String context;
        private long requestCount;
        private String time;

        public APIUsageByResourcePath(){

        }
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

        public APIUsageByDestination(){

        }
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

        public APIUsageByUserName(){

        }
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

        public APIResponseFaultCount(){

        }
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

    private static class APIResponseTime {

        private String apiName;
        private String apiVersion;
        private String context;
        private double responseTime;
        private long responseCount;

        public APIResponseTime(){

        }

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
        private long accessTime;
        private String username;

        public APIAccessTime(){

        }

        public APIAccessTime(String apiName, String apiVersion, String context, long accessTime, String username) {

            this.apiName = apiName;
            this.apiVersion = apiVersion;
            this.context = context;
            this.accessTime = accessTime;
            this.username = username;
        }
    }


}