package org.wso2.carbon.apimgt.gateway.handlers.transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.AbstractExtendedSynapseHandler;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.transaction.queue.TransactionRecordQueue;
import org.wso2.carbon.apimgt.gateway.handlers.transaction.store.TransactionRecordStore;

import java.lang.reflect.Constructor;
import java.util.concurrent.*;

public class TransactionCountHandler extends AbstractExtendedSynapseHandler {

    // Todo: Make these parameters configurable via deployment.toml
    private final int PRODUCER_THREAD_POOL_SIZE = 5;
    private final int CONSUMER_THREAD_POOL_SIZE = 5;
    private final int TRANSACTION_RECORD_QUEUE_SIZE = 1000;
    private final String TRANSACTION_COUNT_STORE_CLASS = "org.wso2.carbon.apimgt.gateway.handlers.transaction.store.TransactionRecordStoreImpl";

    private static final Log LOG = LogFactory.getLog(TransactionCountHandler.class);
    private TransactionRecordQueue transactionRecordQueue;
    private TransactionRecordProducer transactionRecordProducer;
    private TransactionRecordConsumer transactionRecordConsumer;
    private TransactionRecordStore trasactionCountStore;


    public TransactionCountHandler() {

        this.transactionRecordQueue = TransactionRecordQueue.getInstance(TRANSACTION_RECORD_QUEUE_SIZE);
        // Load the transaction count store
        try {
            Class<?> clazz = Class.forName(TRANSACTION_COUNT_STORE_CLASS);
            Constructor<?> constructor = clazz.getConstructor();
            this.trasactionCountStore = (TransactionRecordStore) constructor.newInstance();
        } catch (Exception e) {
            LOG.error("Error while loading the transaction count store.", e);
        }

        this.transactionRecordProducer = TransactionRecordProducer.getInstance(transactionRecordQueue,
                PRODUCER_THREAD_POOL_SIZE);
        this.transactionRecordConsumer = TransactionRecordConsumer.getInstance(transactionRecordQueue,
                trasactionCountStore, CONSUMER_THREAD_POOL_SIZE);

        this.transactionRecordProducer.start();
        this.transactionRecordConsumer.start();
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
                LOG.info("Counting WebSocket message");
                this.transactionRecordProducer.addTransaction();
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
                LOG.info("Counting async outgoing message");
                this.transactionRecordProducer.addTransaction();
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
            LOG.info("Counting request-response pair");
            this.transactionRecordProducer.addTransaction();
        }
        return true;
    }

    @Override
    public boolean handleServerInit() {
        // Nothing to implement
        return true;
    }

    @Override
    public boolean handleServerShutDown() {
        // Clen up resources
        transactionRecordProducer.shutdown();
        transactionRecordConsumer.shutdown();
        transactionRecordQueue.clenUp();
        trasactionCountStore.clenUp();
        return true;
    }

    @Override
    public boolean handleArtifactDeployment(String s, String s1, String s2) {
        // Nothing to implement
        return true;
    }

    @Override
    public boolean handleArtifactUnDeployment(String s, String s1, String s2) {
        // Nothing to implement
        return true;
    }

    @Override
    public boolean handleError(MessageContext messageContext) {
        // Nothing to implement
        return true;
    }
}
