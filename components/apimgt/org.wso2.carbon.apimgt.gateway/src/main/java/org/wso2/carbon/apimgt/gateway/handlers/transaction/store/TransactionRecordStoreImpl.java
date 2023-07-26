package org.wso2.carbon.apimgt.gateway.handlers.transaction.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.gateway.handlers.transaction.TransactionRecord;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class TransactionRecordStoreImpl implements TransactionRecordStore {

    private static final Log LOG = LogFactory.getLog(TransactionRecordStoreImpl.class);
    private static final AtomicInteger transactionCount = new AtomicInteger(0);

    public TransactionRecordStoreImpl() {
        LOG.info("Transaction store loaded");
    }

    @Override
    public boolean commit(ArrayList<TransactionRecord> transactionRecordList) {
        LOG.info("Transaction count is commited");
        transactionRecordList.forEach(transactionRecord -> {
            transactionCount.addAndGet(transactionRecord.getCount());
        });
        LOG.info("Global Transaction count: " + transactionCount.get());
        return true;
    }

    @Override
    public void clenUp() {
        // To be implemented
    }
}
