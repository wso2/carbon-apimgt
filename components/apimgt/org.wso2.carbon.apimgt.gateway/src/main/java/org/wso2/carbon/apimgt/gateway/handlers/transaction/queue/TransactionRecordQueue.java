package org.wso2.carbon.apimgt.gateway.handlers.transaction.queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.gateway.handlers.transaction.record.TransactionRecord;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

public class TransactionRecordQueue {

    private static TransactionRecordQueue instance = null;
    private static ArrayBlockingQueue<TransactionRecord> transactionRecordQueue;

    private TransactionRecordQueue() {}

    public static TransactionRecordQueue getInstance() {
        if(instance == null) {
            instance = new TransactionRecordQueue();
        }
        return instance;
    }

    public void init(int size) {
        transactionRecordQueue = new ArrayBlockingQueue<>(size);
    }
    
    public void add(TransactionRecord transactionRecord) {
        transactionRecordQueue.add(transactionRecord);
    }
    public void addAll(ArrayList<TransactionRecord> transactionRecordList) {
        transactionRecordQueue.addAll(transactionRecordList);
    }

    public TransactionRecord take() throws InterruptedException {
        return transactionRecordQueue.take();
    }

    public void drain(ArrayList<TransactionRecord> transactionRecordList, int maxRecords) {
        transactionRecordQueue.drainTo(transactionRecordList, maxRecords);
    }

    public void clenUp() {
        transactionRecordQueue.clear();
    }
}
