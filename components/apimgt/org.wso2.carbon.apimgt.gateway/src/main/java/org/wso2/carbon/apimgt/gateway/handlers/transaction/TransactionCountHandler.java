package org.wso2.carbon.apimgt.gateway.handlers.transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.AbstractSynapseHandler;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.transaction.store.TransactionCountStore;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class TransactionCountHandler extends AbstractSynapseHandler {

    // Todo: Make these parameters configurable via deployment.toml
    private static final double MAX_TRANSACTION_COUNT = Integer.MAX_VALUE * 0.9;
    private int MAX_RETRY_COUNT = 3;
    private int TRANSACTION_COUNT_COMMIT_INTERVAL = 10;
    private int THREAD_POOL_SIZE = 5;
    private int TRANSACTION_COUNT_QUEUE_SIZE = 1000;

    private static final Log LOG = LogFactory.getLog(TransactionCountHandler.class);
    private static ReentrantLock lock = new ReentrantLock();
    private static AtomicInteger transactionCount = new AtomicInteger(0);

    private BlockingQueue<TransactionCountRecord> transactionCountRecordQueue;
    private ExecutorService transactionCountExecutor;
    private ScheduledExecutorService transactionCountScheduledExecutor;
    private TransactionCountStore trasactionCountStore;


    public TransactionCountHandler() {
        this.transactionCountRecordQueue = new LinkedBlockingDeque<>(TRANSACTION_COUNT_QUEUE_SIZE);
        this.transactionCountExecutor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        this.transactionCountScheduledExecutor = Executors.newScheduledThreadPool(1);

        // Load the transaction count store
        try {
            Class<?> clazz = Class.forName("org.wso2.carbon.apimgt.gateway.handlers.transaction.store.TransactionCountStoreImpl");
            Constructor<?> constructor = clazz.getConstructor();
            this.trasactionCountStore = (TransactionCountStore) constructor.newInstance();
        } catch (Exception e) {
            LOG.error("Error while loading the transaction count store.", e);
        }

        // Start the transaction count record scheduler
        transactionCountScheduledExecutor.scheduleAtFixedRate(this::handleTransactionCount,
                0, TRANSACTION_COUNT_COMMIT_INTERVAL, TimeUnit.SECONDS);
    }

    @Override
    public boolean handleRequestInFlow(MessageContext messageContext) {
        try {
            org.apache.axis2.context.MessageContext axis2MessageContext =
                    ((Axis2MessageContext) messageContext).getAxis2MessageContext();

            // Setting this property to identify request-response pairs
            axis2MessageContext.setProperty(APIMgtGatewayConstants.IS_THERE_ASSOCIATED_INCOMING_REQUEST, true);

            // Counting message received via an open WebSocket
            String transport = axis2MessageContext.getIncomingTransportName();
            if (transport.equals(APIMgtGatewayConstants.TRANSPORT_WS) || transport.equals(APIMgtGatewayConstants.TRANSPORT_WSS)){
                transactionCountExecutor.execute(this::handleScheduledTransactionCountCommit);
            }
        } catch (RejectedExecutionException e) {
            LOG.error("Transaction could not be counted.", e);
        }
        return true;
    }

    @Override
    public boolean handleRequestOutFlow(MessageContext messageContext) {
        try {
            org.apache.axis2.context.MessageContext axis2MessageContext =
                    ((Axis2MessageContext) messageContext).getAxis2MessageContext();
            Object isThereAnAssociatedIncomingRequest = axis2MessageContext.getProperty(
                    APIMgtGatewayConstants.IS_THERE_ASSOCIATED_INCOMING_REQUEST);

            // Counting outgoing messages that are not related to any request-response pair
            if (isThereAnAssociatedIncomingRequest == null) {
                transactionCountExecutor.execute(this::handleTransactionCount);
            }
        } catch (RejectedExecutionException e) {
            LOG.error("Transaction could not be counted.", e);
        }
        return true;
    }

    @Override
    public boolean handleResponseInFlow(MessageContext messageContext) {
        return true;
    }

    @Override
    public boolean handleResponseOutFlow(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        Object isThereAnAssociatedIncomingRequest = axis2MessageContext.getProperty(
                APIMgtGatewayConstants.IS_THERE_ASSOCIATED_INCOMING_REQUEST);

        // Counting request-response pairs
        if (isThereAnAssociatedIncomingRequest instanceof Boolean) {
            transactionCountExecutor.execute(this::handleTransactionCount);
        }
        return true;
    }

    private void handleTransactionCount() {
        lock.lock();
        try {
            if (transactionCount.incrementAndGet() >= MAX_TRANSACTION_COUNT) {
                TransactionCountRecord transactionCountRecord = new TransactionCountRecord(transactionCount.get());
                transactionCountRecordQueue.add(transactionCountRecord);
                transactionCount.set(0);
            }
        } catch (Exception e) {
            LOG.error("Error while handling transaction count.", e);
        } finally {
            lock.unlock();
        }
        this.commitWithRetries();
    }

    private void handleScheduledTransactionCountCommit() {
        lock.lock();
        try {
            TransactionCountRecord transactionCountRecord = new TransactionCountRecord(transactionCount.get());
            transactionCountRecordQueue.add(transactionCountRecord);
            transactionCount.set(0);
        } catch (Exception e) {
            LOG.error("Error while handling transaction count.", e);
        } finally {
            lock.unlock();
        }
        this.commitWithRetries();
    }

    private void commitWithRetries() {
        // Arraylist of transaction count records will be committed to the store
        ArrayList<TransactionCountRecord> transactionCountRecordList = new ArrayList<>();
        transactionCountRecordQueue.drainTo(transactionCountRecordList);

        if (transactionCountRecordList.isEmpty()) {
            return;
        }

        // Committing the transaction count records to the store with retries
        // If failed to commit after MAX_RETRY_COUNT, the transaction count records will be added to the queue again
        boolean commited = false;
        int retryCount = 0;
        while (!commited && retryCount < MAX_RETRY_COUNT) {
            commited = trasactionCountStore.commit(transactionCountRecordList);
            retryCount++;
        }
        if (!commited) {
            transactionCountRecordQueue.addAll(transactionCountRecordList);
        }
    }
}
