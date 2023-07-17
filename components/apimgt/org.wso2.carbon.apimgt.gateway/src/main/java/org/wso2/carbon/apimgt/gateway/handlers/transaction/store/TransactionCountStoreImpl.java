package org.wso2.carbon.apimgt.gateway.handlers.transaction.store;

import org.wso2.carbon.apimgt.gateway.handlers.transaction.TransactionCount;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class TransactionCountStoreImpl implements TransactionCountStore {

    private BlockingQueue<TransactionCount> transactionCountQueue;

    public TransactionCountStoreImpl() {
        this.transactionCountQueue = new LinkedBlockingDeque<TransactionCount>();
    }

    @Override
    public boolean add(TransactionCount transactionCount) {
        transactionCountQueue.add(transactionCount);
        return true;
    }

    @Override
    public boolean commit() {
        // To be implemented
        return true;
    }
}
