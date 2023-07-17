package org.wso2.carbon.apimgt.gateway.handlers.transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.AbstractSynapseHandler;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.transaction.store.TransactionCountStore;

import java.lang.reflect.Constructor;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class TransactionCountHandler extends AbstractSynapseHandler {
    private static final Log LOG = LogFactory.getLog(TransactionCountHandler.class);
    private static final double MAX_TRANSACTION_COUNT = Integer.MAX_VALUE * 0.9;
    private static ReentrantLock lock = new ReentrantLock();
    private static AtomicInteger transactionCount = new AtomicInteger(0);
    private ExecutorService transactionCountExecutor;

    private TransactionCountStore trasactionCountStore;

    public TransactionCountHandler() {
        this.transactionCountExecutor = Executors.newFixedThreadPool(5);

        // Load the transaction count store
        try {
            Class<?> clazz = Class.forName("org.wso2.carbon.apimgt.gateway.handlers.transaction.store.TransactionCountStoreImpl");
            Constructor<?> constructor = clazz.getConstructor();
            this.trasactionCountStore = (TransactionCountStore) constructor.newInstance();
        } catch (Exception e) {
            LOG.error("Error while loading the transaction count store.", e);
        }
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
                transactionCountExecutor.execute(this::handleTransactionCount);
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
        transactionCount.incrementAndGet();
        if (transactionCount.get() >= MAX_TRANSACTION_COUNT) {
            lock.lock();
            try {
                if (transactionCount.get() >= MAX_TRANSACTION_COUNT) {
                    trasactionCountStore.add(new TransactionCount(transactionCount.get()));
                    transactionCount.set(0);
                    trasactionCountStore.commit();
                }
            } finally {
                lock.unlock();
            }
        }
    }

}
