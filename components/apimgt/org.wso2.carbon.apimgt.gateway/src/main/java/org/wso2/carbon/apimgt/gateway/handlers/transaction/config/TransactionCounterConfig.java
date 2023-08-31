package org.wso2.carbon.apimgt.gateway.handlers.transaction.config;

import org.wso2.carbon.apimgt.gateway.handlers.transaction.TransactionCounterConstants;
import org.wso2.carbon.apimgt.gateway.handlers.transaction.exception.TransactionCounterConfigurationException;

public class TransactionCounterConfig {

    private static ConfigFetcher configFetcher;
    private static TransactionCounterConstants.ServerType serverType;

    public static void init() throws TransactionCounterConfigurationException {
        try {
            // Check whether the APIM Config class is available
            Class.forName(TransactionCounterConstants.APIM_CONFIG_CLASS);
            configFetcher = APIMConfigFetcher.getInstance();
            serverType = TransactionCounterConstants.ServerType.GATEWAY;
        } catch (ClassNotFoundException e) {
            try {
                // Check whether the MI Config class is available
                Class.forName(TransactionCounterConstants.MI_CONFIG_CLASS);
                configFetcher = MIConfigFetcher.getInstance();
                serverType = TransactionCounterConstants.ServerType.MI;
            } catch (ClassNotFoundException ex) {
                throw new TransactionCounterConfigurationException(ex);
            }
        }
    }

    public static TransactionCounterConstants.ServerType getServerType() {
        return serverType;
    }

    public static String getServerID() {
        return configFetcher.getConfigValue(TransactionCounterConstants.SERVER_ID);
    }

    public static String getTransactionCountStoreClass() {
        return configFetcher.getConfigValue(TransactionCounterConstants.TRANSACTION_COUNT_STORE_CLASS);
    }

    public static int getProducerThreadPoolSize() {
        return Integer.parseInt(
                configFetcher.getConfigValue(TransactionCounterConstants.PRODUCER_THREAD_POOL_SIZE));
    }

    public static double getMaxTransactionCount() {
        return Double.parseDouble(
                configFetcher.getConfigValue(TransactionCounterConstants.MAX_TRANSACTION_COUNT));
    }

    public static double getMinTransactionCount() {
        return Double.parseDouble(
                configFetcher.getConfigValue(TransactionCounterConstants.MIN_TRANSACTION_COUNT));
    }

    public static int getTransactionCountRecordInterval() {
        return Integer.parseInt(
                configFetcher.getConfigValue(TransactionCounterConstants.TRANSACTION_COUNT_RECORD_INTERVAL));
    }

    public static int getMaxRetryCount() {
        return Integer.parseInt(
                configFetcher.getConfigValue(TransactionCounterConstants.MAX_RETRY_COUNT));
    }

    public static int getMaxTransactionRecordsPerCommit() {
        return Integer.parseInt(
                configFetcher.getConfigValue(TransactionCounterConstants.MAX_TRANSACTION_RECORDS_PER_COMMIT));
    }

    public static int getTransactionRecordQueueSize() {
        return Integer.parseInt(
                configFetcher.getConfigValue(TransactionCounterConstants.TRANSACTION_RECORD_QUEUE_SIZE));
    }

    public static String getTransactionCountService() {
        return configFetcher.getConfigValue(TransactionCounterConstants.TRANSACTION_COUNT_SERVICE);
    }

    public static String getTransactionCountServiceUsername() {
        return configFetcher.getConfigValue(TransactionCounterConstants.TRANSACTION_COUNT_SERVICE_USERNAME);
    }

    public static String getTransactionCountServicePassword() {
        return configFetcher.getConfigValue(TransactionCounterConstants.TRANSACTION_COUNT_SERVICE_PASSWORD);
    }

    public static int getConsumerCommitInterval() {
        return Integer.parseInt(
                configFetcher.getConfigValue(TransactionCounterConstants.CONSUMER_COMMIT_INTERVAL));
    }
}
