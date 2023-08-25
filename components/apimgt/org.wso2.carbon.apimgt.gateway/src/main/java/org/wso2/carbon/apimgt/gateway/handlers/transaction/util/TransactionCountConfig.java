package org.wso2.carbon.apimgt.gateway.handlers.transaction.util;

import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

public class TransactionCountConfig {

    private static int PRODUCER_THREAD_POOL_SIZE;
    private static int CONSUMER_COMMIT_INTERVAL;
    private static int TRANSACTION_RECORD_QUEUE_SIZE;
    private static String TRANSACTION_COUNT_STORE_CLASS;
    private static double MAX_TRANSACTION_COUNT;
    private static int TRANSACTION_COUNT_RECORD_INTERVAL;
    private static int MAX_RETRY_COUNT;
    private static int MAX_TRANSACTION_RECORDS_PER_COMMIT;
    private static String SERVER_ID;
    private static String TRANSACTION_COUNT_SERVICE;
    private static String TRANSACTION_COUNT_SERVICE_USERNAME;
    private static String TRANSACTION_COUNT_SERVICE_PASSWORD;

    static {
        APIManagerConfiguration apiManagerConfiguration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();

        if (apiManagerConfiguration != null) {
            SERVER_ID = apiManagerConfiguration.getFirstProperty(
                    APIMgtGatewayConstants.TRANSACTION_COUNTER_SERVER_ID);
            TRANSACTION_COUNT_STORE_CLASS = apiManagerConfiguration.getFirstProperty(
                    APIMgtGatewayConstants.TRANSACTION_COUNTER_STORE_CLASS);
            TRANSACTION_RECORD_QUEUE_SIZE = Integer.parseInt(apiManagerConfiguration.getFirstProperty(
                    APIMgtGatewayConstants.TRANSACTION_COUNTER_QUEUE_SIZE));
            PRODUCER_THREAD_POOL_SIZE = Integer.parseInt(apiManagerConfiguration.getFirstProperty(
                    APIMgtGatewayConstants.TRANSACTION_COUNTER_PRODUCER_THREAD_POOL_SIZE));
            TRANSACTION_COUNT_RECORD_INTERVAL = Integer.parseInt(apiManagerConfiguration.getFirstProperty(
                    APIMgtGatewayConstants.TRANSACTION_COUNTER_RECORD_INTERVAL));
            MAX_TRANSACTION_COUNT = Double.parseDouble(apiManagerConfiguration.getFirstProperty(
                    APIMgtGatewayConstants.TRANSACTION_COUNTER_MAX_TRANSACTION_COUNT));
            CONSUMER_COMMIT_INTERVAL = Integer.parseInt(apiManagerConfiguration.getFirstProperty(
                    APIMgtGatewayConstants.TRANSACTION_COUNTER_CONSUMER_COMMIT_INTERVAL));
            MAX_TRANSACTION_RECORDS_PER_COMMIT = Integer.parseInt(apiManagerConfiguration.getFirstProperty(
                    APIMgtGatewayConstants.TRANSACTION_COUNTER_MAX_TRANSACTION_RECORDS_PER_COMMIT));
            MAX_RETRY_COUNT = Integer.parseInt(apiManagerConfiguration.getFirstProperty(
                    APIMgtGatewayConstants.TRANSACTION_COUNTER_MAX_RETRY_COUNT));
            TRANSACTION_COUNT_SERVICE = apiManagerConfiguration.getFirstProperty(
                    APIMgtGatewayConstants.TRANSACTION_COUNTER_SERVICE);
            TRANSACTION_COUNT_SERVICE_USERNAME = apiManagerConfiguration.getFirstProperty(
                    APIMgtGatewayConstants.TRANSACTION_COUNTER_SERVICE_USERNAME);
            TRANSACTION_COUNT_SERVICE_PASSWORD = apiManagerConfiguration.getFirstProperty(
                    APIMgtGatewayConstants.TRANSACTION_COUNTER_SERVICE_PASSWORD);
        }
    }

    public static int getProducerThreadPoolSize() {
        return PRODUCER_THREAD_POOL_SIZE;
    }

    public static double getMaxTransactionCount() {
        return MAX_TRANSACTION_COUNT;
    }

    public static int getTransactionCountRecordInterval() {
        return TRANSACTION_COUNT_RECORD_INTERVAL;
    }

    public static int getMaxRetryCount() {
        return MAX_RETRY_COUNT;
    }

    public static int getMaxTransactionRecordsPerCommit() {
        return MAX_TRANSACTION_RECORDS_PER_COMMIT;
    }

    public static int getTransactionRecordQueueSize() {
        return TRANSACTION_RECORD_QUEUE_SIZE;
    }

    public static String getTransactionCountStoreClass() {
        return TRANSACTION_COUNT_STORE_CLASS;
    }

    public static String getServerID() {
        return SERVER_ID;
    }

    public static String getTransactionCountService() {
        return TRANSACTION_COUNT_SERVICE;
    }

    public static String getTransactionCountServiceUsername() {
        return TRANSACTION_COUNT_SERVICE_USERNAME;
    }

    public static String getTransactionCountServicePassword() {
        return TRANSACTION_COUNT_SERVICE_PASSWORD;
    }

    public static int getConsumerCommitInterval() {
        return CONSUMER_COMMIT_INTERVAL;
    }
}
