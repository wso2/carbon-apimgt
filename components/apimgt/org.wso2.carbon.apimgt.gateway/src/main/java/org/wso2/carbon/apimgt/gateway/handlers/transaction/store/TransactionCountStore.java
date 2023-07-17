package org.wso2.carbon.apimgt.gateway.handlers.transaction.store;

import org.wso2.carbon.apimgt.gateway.handlers.transaction.TransactionCountRecord;

import java.util.ArrayList;

public interface TransactionCountStore {
    boolean commit(ArrayList<TransactionCountRecord> transactionCountRecordList);
    void clenUp();
}
