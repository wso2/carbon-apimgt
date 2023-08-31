package org.wso2.carbon.apimgt.gateway.handlers.transaction.producer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.gateway.handlers.transaction.record.TransactionRecord;
import org.wso2.carbon.apimgt.gateway.handlers.transaction.queue.TransactionRecordQueue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class TransactionRecordProducer {

    private static double MAX_TRANSACTION_COUNT;
    private static double MIN_TRANSACTION_COUNT;
    private static final Log LOG = LogFactory.getLog(TransactionRecordProducer.class);
    private static TransactionRecordProducer instance = null;
    private TransactionRecordQueue transactionRecordQueue;
    private ExecutorService executorService;
    private ScheduledExecutorService scheduledExecutorService;
    private static final ReentrantLock lock = new ReentrantLock();
    private static final AtomicInteger transactionCount = new AtomicInteger(0);

    private TransactionRecordProducer() {}

    public static TransactionRecordProducer getInstance() {
        if(instance == null) {
            instance = new TransactionRecordProducer();
        }
        return instance;
    }

    public void init(TransactionRecordQueue transactionRecordQueue, int threadPoolSize, double maxTransactionCount,
            double minTransactionCount, int transactionCountRecordInterval) {

        MAX_TRANSACTION_COUNT = maxTransactionCount;
        MIN_TRANSACTION_COUNT = minTransactionCount;

        this.transactionRecordQueue = transactionRecordQueue;
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        scheduledExecutorService.scheduleAtFixedRate(this::produceRecordScheduled, 0,
                transactionCountRecordInterval, TimeUnit.SECONDS);
    }

    public void addTransaction(int tCount) {
        executorService.execute(() -> this.produceRecord(tCount));
    }

    private void produceRecord(int tCount) {
        lock.lock();
        try {
            int count = transactionCount.addAndGet(tCount);
            if (count >= MAX_TRANSACTION_COUNT) {
                TransactionRecord transactionRecord = new TransactionRecord(transactionCount.get());
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
            if (transactionCountValue >= MIN_TRANSACTION_COUNT) {
                TransactionRecord transactionRecord = new TransactionRecord(transactionCountValue);
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
