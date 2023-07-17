package org.wso2.carbon.apimgt.gateway.handlers.transaction.store;

import org.wso2.carbon.apimgt.gateway.handlers.transaction.TransactionCountRecord;

import java.util.ArrayList;

public class TransactionCountStoreImpl implements TransactionCountStore {

    @Override
    public boolean commit(ArrayList<TransactionCountRecord> transactionCountRecordList) {
        // To be implemented
        return true;
    }

    @Override
    public void clenUp() {
        // To be implemented
    }
}
