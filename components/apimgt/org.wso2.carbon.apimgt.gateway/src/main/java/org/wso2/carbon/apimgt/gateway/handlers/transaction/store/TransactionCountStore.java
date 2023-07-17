package org.wso2.carbon.apimgt.gateway.handlers.transaction.store;

import org.wso2.carbon.apimgt.gateway.handlers.transaction.TransactionCount;

public interface TransactionCountStore {
    public boolean add(TransactionCount transactionCount);
    public boolean commit();
}
