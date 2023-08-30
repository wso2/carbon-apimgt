package org.wso2.carbon.apimgt.gateway.handlers.transaction.exception;

public class TransactionCounterInitializationException extends Exception {

    public TransactionCounterInitializationException() {
        super("Error while initializing the transaction counter");
    }

    public TransactionCounterInitializationException(Exception e) {
        super("Error while initializing the transaction counter", e);
    }
    public TransactionCounterInitializationException(String msg) {
        super(msg);
    }

    public TransactionCounterInitializationException(String msg, Exception e) {
        super(msg, e);
    }
}
