package org.wso2.carbon.apimgt.gateway.handlers.transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.gateway.handlers.transaction.queue.TransactionRecordQueue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class TransactionRecordProducer {

    // Todo: Make these parameters configurable via deployment.toml
    private static final double MAX_TRANSACTION_COUNT = Integer.MAX_VALUE * 0.9;
    private int TRANSACTION_COUNT_COMMIT_INTERVAL = 10;

    private static final Log LOG = LogFactory.getLog(TransactionRecordProducer.class);
    private static TransactionRecordProducer instance = null;
    private TransactionRecordQueue transactionRecordQueue;
    private ExecutorService executorService;
    private ScheduledExecutorService scheduledExecutorService;
    private static ReentrantLock lock = new ReentrantLock();
    private static AtomicInteger transactionCount = new AtomicInteger(0);
    private TransactionRecordProducer(TransactionRecordQueue transactionRecordQueue, int threadPoolSize) {
        this.transactionRecordQueue = transactionRecordQueue;
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    public static TransactionRecordProducer getInstance(TransactionRecordQueue transactionRecordQueue,
                                                        int threadPoolSize) {
        if(instance == null) {
            instance = new TransactionRecordProducer(transactionRecordQueue, threadPoolSize);
        }
        return instance;
    }

    public void start() {
        LOG.info("Transaction record producer started");
        // Start the transaction count record scheduler
        scheduledExecutorService.scheduleAtFixedRate(this::produceRecordScheduled,
                0, TRANSACTION_COUNT_COMMIT_INTERVAL, TimeUnit.SECONDS);
    }

    public void addTransaction() {
        executorService.execute(this::produceRecord);
    }

    private void produceRecord() {
        lock.lock();
        try {
            if (transactionCount.incrementAndGet() >= MAX_TRANSACTION_COUNT) {
                TransactionRecord transactionRecord = new TransactionRecord(transactionCount.get());
                LOG.info("Transaction count is added to the queue from producer");
                transactionRecordQueue.add(transactionRecord);
                transactionCount.set(0);
            }
        } catch (Exception e) {
            LOG.error("Error while handling transaction count.", e);
        } finally {
            lock.unlock();
        }
    }

    private void produceRecordScheduled() {
        lock.lock();
        try {
            int transactionCountValue = transactionCount.get();
            if (transactionCountValue != 0) {
                TransactionRecord transactionRecord = new TransactionRecord(transactionCountValue);
                LOG.info("Transaction count is added to the queue from scheduled producer");
                transactionRecordQueue.add(transactionRecord);
                transactionCount.set(0);
            }
        } catch (Exception e) {
            LOG.error("Error while handling transaction count.", e);
        } finally {
            lock.unlock();
        }
    }

    public void shutdown() {
        scheduledExecutorService.shutdownNow();
        executorService.shutdownNow();
    }

}
