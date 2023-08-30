package org.wso2.carbon.apimgt.gateway.handlers.transaction.consumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.gateway.handlers.transaction.config.TransactionCounterConfig;
import org.wso2.carbon.apimgt.gateway.handlers.transaction.record.TransactionRecord;
import org.wso2.carbon.apimgt.gateway.handlers.transaction.queue.TransactionRecordQueue;
import org.wso2.carbon.apimgt.gateway.handlers.transaction.store.TransactionRecordStore;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TransactionRecordConsumer {

    private int MAX_RETRY_COUNT;
    private int MAX_TRANSACTION_RECORDS_PER_COMMIT;
    private static final Log LOG = LogFactory.getLog(TransactionRecordConsumer.class);
    private static TransactionRecordConsumer instance = null;
    private TransactionRecordStore transactionRecordStore;
    private TransactionRecordQueue transactionRecordQueue;
    private ScheduledExecutorService scheduledExecutorService;
    private final int commitInterval;

    private TransactionRecordConsumer(TransactionRecordStore transactionRecordStore,
                                      TransactionRecordQueue transactionRecordQueue, int commitInterval) {

        // Obtain config values
        MAX_RETRY_COUNT = TransactionCounterConfig.getMaxRetryCount();
        MAX_TRANSACTION_RECORDS_PER_COMMIT = TransactionCounterConfig.getMaxTransactionRecordsPerCommit();

        this.transactionRecordStore = transactionRecordStore;
        this.transactionRecordQueue = transactionRecordQueue;
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        this.commitInterval = commitInterval;
    }

    public static TransactionRecordConsumer getInstance(TransactionRecordQueue transactionRecordQueue,
                                                        TransactionRecordStore transactionRecordStore,
                                                        int commitInterval) {
        if(instance == null) {
            instance = new TransactionRecordConsumer(transactionRecordStore, transactionRecordQueue, commitInterval);
        }
        return instance;
    }

    public void start() {
        LOG.info("Transaction record consumer started");
        // execute the startCommitting method in all the threads
        scheduledExecutorService.scheduleAtFixedRate(this::commitWithRetries,
                0, commitInterval, TimeUnit.SECONDS);
    }

    private void commitWithRetries() {
        // Drain the transaction count records from the queue
        ArrayList<TransactionRecord> transactionRecordList = new ArrayList<>();
        transactionRecordQueue.drain(transactionRecordList, MAX_TRANSACTION_RECORDS_PER_COMMIT);

        if(transactionRecordList.isEmpty()) {
            return;
        }

        // Committing the transaction count records to the store with retries
        // If failed to commit after MAX_RETRY_COUNT, the transaction count records will be added to the queue again
        boolean commited = this.transactionRecordStore.commit(transactionRecordList, MAX_RETRY_COUNT);
        if (!commited) {
            transactionRecordQueue.addAll(transactionRecordList);
        }
    }

    public void shutdown() {
        this.scheduledExecutorService.shutdownNow();
    }

}
