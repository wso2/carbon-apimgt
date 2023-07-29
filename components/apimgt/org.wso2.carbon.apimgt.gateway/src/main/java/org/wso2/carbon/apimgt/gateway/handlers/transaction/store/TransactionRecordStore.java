package org.wso2.carbon.apimgt.gateway.handlers.transaction.store;

import org.wso2.carbon.apimgt.gateway.handlers.transaction.TransactionRecord;

import java.util.ArrayList;

public interface TransactionRecordStore {
    boolean commit(ArrayList<TransactionRecord> transactionRecordList, int retryCount);
    void clenUp();
}
