package org.wso2.carbon.apimgt.usage.client.impl;

import org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;
import org.wso2.carbon.apimgt.usage.client.APIUsageStatisticsClient;
import org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException;

/**
 * Created by rukshan on 10/6/15.
 */
public class UsageClient {
    public static APIUsageStatisticsClient apiUsageStatisticsClient;

    public static void initializeDataSource() throws APIMgtUsageQueryServiceClientException {
        APIUsageStatisticsRestClientImpl.initializeDataSource();
        //APIUsageStatisticsRdbmsClientImpl.initializeDataSource();

    }

    public UsageClient(String name) {
        try {
            apiUsageStatisticsClient = new APIUsageStatisticsRestClientImpl(name);
        } catch (APIMgtUsageQueryServiceClientException e) {
            e.printStackTrace();
        }
    }

    public UsageClient() {
        System.out.println("init");
    }

    public APIUsageStatisticsClient getClient() {
        return apiUsageStatisticsClient;
    }

    public static boolean isDataPublishingEnabled(){
        APIManagerAnalyticsConfiguration con=APIManagerAnalyticsConfiguration.getInstance();
        return con.isAnalyticsEnabled();
//        return true;
    }



}
