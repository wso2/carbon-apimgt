package org.wso2.carbon.apimgt.gateway.handlers.transaction;

import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;


/**
 * This class contains the logic for counting transactions.
 * In case of changing the logic, this class should be modified, replaced or extended.
 * @author - Isuru Wijesiri
 * @version - 1.0.0
 */
public class TransactionCountingLogic {

    public static int handleRequestInFlow(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        // Setting this property to identify request-response pairs
        messageContext.setProperty(TransactionCounterConstants.IS_THERE_ASSOCIATED_INCOMING_REQUEST, true);

        // Counting message received via an open WebSocket
        String transport = axis2MessageContext.getIncomingTransportName();
        if (transport.equals(TransactionCounterConstants.TRANSPORT_WS) ||
                transport.equals(TransactionCounterConstants.TRANSPORT_WSS)){
            return 1;
        }
        return 0;
    }

    public static int handleRequestOutFlow(MessageContext messageContext) {
        Object isThereAnAssociatedIncomingRequest = messageContext.getProperty(
                TransactionCounterConstants.IS_THERE_ASSOCIATED_INCOMING_REQUEST);

        // Counting outgoing messages that are not related to any request-response pair
        if (isThereAnAssociatedIncomingRequest == null) {
            return 1;
        }
        return 0;
    }

    public static int handleResponseInFlow(MessageContext messageContext) {
        return 0;
    }

    public static int handleResponseOutFlow(MessageContext messageContext) {
        Object isThereAnAssociatedIncomingRequest = messageContext.getProperty(
                TransactionCounterConstants.IS_THERE_ASSOCIATED_INCOMING_REQUEST);

        // Counting request-response pairs
        if (isThereAnAssociatedIncomingRequest instanceof Boolean) {
            return 1;
        }
        return 0;
    }
}
