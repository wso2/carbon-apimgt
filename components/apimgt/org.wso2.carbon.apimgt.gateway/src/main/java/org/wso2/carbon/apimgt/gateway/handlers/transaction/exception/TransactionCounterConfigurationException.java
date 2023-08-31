package org.wso2.carbon.apimgt.gateway.handlers.transaction.exception;

public class TransactionCounterConfigurationException extends Exception {

    public TransactionCounterConfigurationException() {
        super("Error while reading configuration");
    }

    public TransactionCounterConfigurationException(Exception e) {
        super("Error while reading configuration", e);
    }
    public TransactionCounterConfigurationException(String msg) {
        super(msg);
    }

    public TransactionCounterConfigurationException(String msg, Exception e) {
        super(msg, e);
    }
}
