package org.wso2.carbon.apimgt.gateway.handlers.transaction;

import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

public class TransactionCountConfig {

    private static int PRODUCER_THREAD_POOL_SIZE;
    private static int CONSUMER_THREAD_POOL_SIZE;
    private static int TRANSACTION_RECORD_QUEUE_SIZE;
    private static String TRANSACTION_COUNT_STORE_CLASS;
    private static double MAX_TRANSACTION_COUNT;
    private static int TRANSACTION_COUNT_RECORD_INTERVAL;
    private static int MAX_RETRY_COUNT;
    private static int MAX_TRANSACTION_RECORDS_PER_COMMIT;
    private static String SERVER_ID = "Gateway1";
    private static String TRANSACTION_COUNT_SERVVICE = "https://localhost:8080/transactioncount";
    private static String TRANSACTION_COUNT_SERVVICE_USERNAME = "admin";
    private static String TRANSACTION_COUNT_SERVVICE_PASSWORD = "admin";

    static {
        APIManagerConfiguration apiManagerConfiguration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();

        if (apiManagerConfiguration != null) {
            PRODUCER_THREAD_POOL_SIZE = Integer.parseInt(apiManagerConfiguration.getFirstProperty(
                    APIMgtGatewayConstants.TRANSACTION_COUNTER_PRODUCER_THREAD_POOL_SIZE));
            CONSUMER_THREAD_POOL_SIZE = Integer.parseInt(apiManagerConfiguration.getFirstProperty(
                    APIMgtGatewayConstants.TRANSACTION_COUNTER_CONSUMER_THREAD_POOL_SIZE));
            TRANSACTION_RECORD_QUEUE_SIZE = Integer.parseInt(apiManagerConfiguration.getFirstProperty(
                    APIMgtGatewayConstants.TRANSACTION_COUNTER_QUEUE_SIZE));
            TRANSACTION_COUNT_STORE_CLASS = apiManagerConfiguration.getFirstProperty(
                    APIMgtGatewayConstants.TRANSACTION_COUNTER_STORE_CLASS);
            MAX_TRANSACTION_COUNT = Double.parseDouble(apiManagerConfiguration.getFirstProperty(
                    APIMgtGatewayConstants.TRANSACTION_COUNTER_MAX_TRANSACTION_COUNT));
            TRANSACTION_COUNT_RECORD_INTERVAL = Integer.parseInt(apiManagerConfiguration.getFirstProperty(
                    APIMgtGatewayConstants.TRANSACTION_COUNTER_RECORD_INTERVAL));
            MAX_RETRY_COUNT = Integer.parseInt(apiManagerConfiguration.getFirstProperty(
                    APIMgtGatewayConstants.TRANSACTION_COUNTER_MAX_RETRY_COUNT));
            MAX_TRANSACTION_RECORDS_PER_COMMIT = Integer.parseInt(apiManagerConfiguration.getFirstProperty(
                    APIMgtGatewayConstants.TRANSACTION_COUNTER_MAX_TRANSACTION_RECORDS_PER_COMMIT));
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

    public static int getConsumerThreadPoolSize() {
        return CONSUMER_THREAD_POOL_SIZE;
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
        return TRANSACTION_COUNT_SERVVICE;
    }

    public static String getTransactionCountServiceUsername() {
        return TRANSACTION_COUNT_SERVVICE_USERNAME;
    }

    public static String getTransactionCountServicePassword() {
        return TRANSACTION_COUNT_SERVVICE_PASSWORD;
    }
}
