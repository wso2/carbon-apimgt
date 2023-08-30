package org.wso2.carbon.apimgt.gateway.handlers.transaction;

public class TransactionCounterConstants {

    public static final String IS_THERE_ASSOCIATED_INCOMING_REQUEST = "is_there_incoming_request";
    public static final String TRANSPORT_WS = "ws";
    public static final String TRANSPORT_WSS = "wss";

    public static final String SERVER_ID = "serverId";
    public static final String TRANSACTION_COUNT_STORE_CLASS = "transactionCountStoreClass";
    public static final String TRANSACTION_RECORD_QUEUE_SIZE = "transactionRecordQueueSize";
    public static final String PRODUCER_THREAD_POOL_SIZE = "producerThreadPoolSize";
    public static final String TRANSACTION_COUNT_RECORD_INTERVAL = "transactionCountRecordInterval";
    public static final String MAX_TRANSACTION_COUNT = "maxTransactionCount";
    public static final String CONSUMER_COMMIT_INTERVAL = "consumerCommitInterval";
    public static final String MAX_TRANSACTION_RECORDS_PER_COMMIT = "maxTransactionRecordsPerCommit";
    public static final String MAX_RETRY_COUNT = "maxRetryCount";
    public static final String TRANSACTION_COUNT_SERVICE = "transactionCountService";
    public static final String TRANSACTION_COUNT_SERVICE_USERNAME = "transactionCountServiceUsername";
    public static final String TRANSACTION_COUNT_SERVICE_PASSWORD = "transactionCountServicePassword";

    // APIM Gateway related constants
    public static final String APIM_CONFIG_CLASS = "org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder";
    public static final String GATEWAY_CONFIG_ROOT = "APIGateway.TransactionCounter";
    public static final String GATEWAY_PRODUCER_THREAD_POOL_SIZE = GATEWAY_CONFIG_ROOT +
            ".ProducerThreadPoolSize";
    public static final String GATEWAY_QUEUE_SIZE = GATEWAY_CONFIG_ROOT + ".QueueSize";
    public static final String GATEWAY_STORE_CLASS = GATEWAY_CONFIG_ROOT + ".StoreClass";
    public static final String GATEWAY_MAX_TRANSACTION_COUNT = GATEWAY_CONFIG_ROOT +
            ".MaxTransactionCount";
    public static final String GATEWAY_RECORD_INTERVAL = GATEWAY_CONFIG_ROOT
            + ".ProducerScheduledInterval";
    public static final String GATEWAY_MAX_RETRY_COUNT = GATEWAY_CONFIG_ROOT + ".MaxRetryCount";
    public static final String GATEWAY_MAX_TRANSACTION_RECORDS_PER_COMMIT = GATEWAY_CONFIG_ROOT
            + ".MaxBatchSize";
    public static final String GATEWAY_CONSUMER_COMMIT_INTERVAL = GATEWAY_CONFIG_ROOT
            + ".PublisherScheduledInterval";
    public static final String GATEWAY_SERVER_ID = GATEWAY_CONFIG_ROOT + ".ServerID";
    public static final String GATEWAY_SERVICE = GATEWAY_CONFIG_ROOT + ".ServiceURL";
    public static final String GATEWAY_SERVICE_USERNAME = GATEWAY_CONFIG_ROOT
            + ".ServiceUsername";
    public static final String GATEWAY_SERVICE_PASSWORD = GATEWAY_CONFIG_ROOT
            + ".ServicePassword";

    // MI related constants
    public static final String MI_CONFIG_CLASS = "org.wso2.config.mapper.ConfigParser";
}
